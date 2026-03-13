/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceNormalizerWithSpaceSeparator;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.instruct.UserFunctionParameter;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.AbstractDestination;
import net.sf.saxon.s9api.AbstractXsltTransformer;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.RawDestination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.value.SequenceType;

public class Xslt30Transformer
extends AbstractXsltTransformer {
    private GlobalParameterSet globalParameterSet = new GlobalParameterSet();
    private boolean primed = false;
    private Item globalContextItem = null;
    private boolean alreadyStripped;

    protected Xslt30Transformer(Processor processor, XsltController controller, GlobalParameterSet staticParameters) {
        super(processor, controller);
    }

    public void setGlobalContextItem(XdmItem globalContextItem) throws SaxonApiException {
        this.setGlobalContextItem(globalContextItem, false);
    }

    public synchronized void setGlobalContextItem(XdmItem globalContextItem, boolean alreadyStripped) throws SaxonApiException {
        if (this.primed) {
            throw new IllegalStateException("Stylesheet has already been evaluated");
        }
        this.globalContextItem = globalContextItem == null ? null : globalContextItem.getUnderlyingValue();
        this.alreadyStripped = alreadyStripped;
    }

    public synchronized <T extends XdmValue> void setStylesheetParameters(Map<QName, T> parameters) throws SaxonApiException {
        if (this.primed) {
            throw new IllegalStateException("Stylesheet has already been evaluated");
        }
        if (this.globalParameterSet == null) {
            this.globalParameterSet = new GlobalParameterSet();
        }
        for (Map.Entry<QName, T> param : parameters.entrySet()) {
            StructuredQName name = param.getKey().getStructuredQName();
            try {
                this.globalParameterSet.put(name, ((XdmValue)param.getValue()).getUnderlyingValue().materialize());
            } catch (XPathException e) {
                throw new SaxonApiException(e);
            }
        }
    }

    private void prime() throws SaxonApiException {
        if (!this.primed) {
            if (this.globalParameterSet == null) {
                this.globalParameterSet = new GlobalParameterSet();
            }
            try {
                this.controller.setGlobalContextItem(this.globalContextItem, this.alreadyStripped);
                this.controller.initializeController(this.globalParameterSet);
            } catch (XPathException e) {
                throw new SaxonApiException(e);
            }
        }
        this.primed = true;
    }

    public synchronized <T extends XdmValue> void setInitialTemplateParameters(Map<QName, T> parameters, boolean tunnel) throws SaxonApiException {
        HashMap<StructuredQName, Sequence> templateParams = new HashMap<StructuredQName, Sequence>();
        for (Map.Entry<QName, T> entry : parameters.entrySet()) {
            templateParams.put(entry.getKey().getStructuredQName(), ((XdmValue)entry.getValue()).getUnderlyingValue());
        }
        this.controller.setInitialTemplateParameters(templateParams, tunnel);
    }

    public synchronized void applyTemplates(Source source, Destination destination) throws SaxonApiException {
        Objects.requireNonNull(destination);
        if (source == null) {
            XPathException err = new XPathException("No initial match selection supplied", "XTDE0044");
            throw new SaxonApiException(err);
        }
        this.prime();
        try {
            Receiver sOut = this.getDestinationReceiver(this.controller, destination);
            this.applyTemplatesToSource(source, sOut);
            destination.closeAndNotify();
        } catch (XPathException e) {
            if (!e.hasBeenReported()) {
                try {
                    this.getErrorListener().fatalError(e);
                } catch (TransformerException transformerException) {
                    // empty catch block
                }
            }
            throw new SaxonApiException(e);
        }
    }

    public synchronized XdmValue applyTemplates(Source source) throws SaxonApiException {
        Objects.requireNonNull(source);
        RawDestination raw = new RawDestination();
        this.applyTemplates(source, (Destination)raw);
        return raw.getXdmValue();
    }

    public synchronized void transform(Source source, Destination destination) throws SaxonApiException {
        Objects.requireNonNull(destination);
        if (source == null) {
            XPathException err = new XPathException("No initial match selection supplied", "XTDE0044");
            throw new SaxonApiException(err);
        }
        if (this.controller.getInitialMode().isDeclaredStreamable()) {
            throw new SaxonApiException("Cannot use the transform() method when the initial mode is streamable");
        }
        this.prime();
        try {
            NodeInfo sourceNode;
            if (source instanceof NodeInfo) {
                this.controller.setGlobalContextItem((NodeInfo)source);
                sourceNode = (NodeInfo)source;
            } else if (source instanceof DOMSource) {
                sourceNode = this.controller.prepareInputTree(source);
                this.controller.setGlobalContextItem(sourceNode);
            } else {
                sourceNode = this.controller.makeSourceTree(source, this.getSchemaValidationMode().getNumber());
                this.controller.setGlobalContextItem(sourceNode);
            }
            Receiver sOut = this.getDestinationReceiver(this.controller, destination);
            this.controller.applyTemplates(sourceNode, sOut);
            destination.closeAndNotify();
        } catch (XPathException e) {
            if (!e.hasBeenReported()) {
                try {
                    this.getErrorListener().fatalError(e);
                } catch (TransformerException transformerException) {
                    // empty catch block
                }
            }
            throw new SaxonApiException(e);
        }
    }

    public synchronized void applyTemplates(XdmValue selection, Destination destination) throws SaxonApiException {
        Objects.requireNonNull(selection);
        Objects.requireNonNull(destination);
        this.prime();
        try {
            Receiver sOut = this.getDestinationReceiver(this.controller, destination);
            if (this.baseOutputUriWasSet) {
                sOut.setSystemId(this.getBaseOutputURI());
            }
            this.controller.applyTemplates(selection.getUnderlyingValue(), sOut);
            destination.closeAndNotify();
        } catch (XPathException e) {
            if (!e.hasBeenReported()) {
                try {
                    this.getErrorListener().fatalError(e);
                } catch (TransformerException transformerException) {
                    // empty catch block
                }
            }
            throw new SaxonApiException(e);
        }
    }

    public synchronized XdmValue applyTemplates(XdmValue selection) throws SaxonApiException {
        Objects.requireNonNull(selection);
        RawDestination raw = new RawDestination();
        this.applyTemplates(selection, (Destination)raw);
        return raw.getXdmValue();
    }

    public synchronized void callTemplate(QName templateName, Destination destination) throws SaxonApiException {
        Objects.requireNonNull(destination);
        this.prime();
        if (templateName == null) {
            templateName = new QName("xsl", "http://www.w3.org/1999/XSL/Transform", "initial-template");
        }
        try {
            Receiver sOut = this.getDestinationReceiver(this.controller, destination);
            if (this.baseOutputUriWasSet) {
                sOut.setSystemId(this.getBaseOutputURI());
            }
            this.controller.callTemplate(templateName.getStructuredQName(), sOut);
            destination.closeAndNotify();
        } catch (XPathException e) {
            destination.closeAndNotify();
            if (!e.hasBeenReported()) {
                this.getErrorReporter().report(new XmlProcessingException(e));
            }
            throw new SaxonApiException(e);
        }
    }

    public synchronized XdmValue callTemplate(QName templateName) throws SaxonApiException {
        RawDestination dest = new RawDestination();
        this.callTemplate(templateName, dest);
        return dest.getXdmValue();
    }

    public synchronized XdmValue callFunction(QName function, XdmValue[] arguments) throws SaxonApiException {
        Objects.requireNonNull(function);
        Objects.requireNonNull(arguments);
        this.prime();
        try {
            Component f = this.getFunctionComponent(function, arguments);
            UserFunction uf = (UserFunction)f.getActor();
            Sequence[] vr = this.typeCheckFunctionArguments(uf, arguments);
            XPathContextMajor context = this.controller.newXPathContext();
            context.setCurrentComponent(f);
            context.setTemporaryOutputState(158);
            context.setCurrentOutputUri(null);
            Sequence result = uf.call(context, vr);
            result = result.materialize();
            return XdmValue.wrap(result);
        } catch (XPathException e) {
            if (!e.hasBeenReported()) {
                this.getErrorReporter().report(new XmlProcessingException(e));
            }
            throw new SaxonApiException(e);
        }
    }

    private synchronized Component getFunctionComponent(QName function, XdmValue[] arguments) throws XPathException {
        SymbolicName.F fName = new SymbolicName.F(function.getStructuredQName(), arguments.length);
        PreparedStylesheet pss = (PreparedStylesheet)this.controller.getExecutable();
        Component f = pss.getComponent(fName);
        if (f == null) {
            throw new XPathException("No public function with name " + function.getClarkName() + " and arity " + arguments.length + " has been declared in the stylesheet", "XTDE0041");
        }
        if (f.getVisibility() != Visibility.FINAL && f.getVisibility() != Visibility.PUBLIC) {
            throw new XPathException("Cannot invoke " + fName + " externally, because it is not public", "XTDE0041");
        }
        return f;
    }

    private Sequence[] typeCheckFunctionArguments(UserFunction uf, XdmValue[] arguments) throws XPathException {
        Configuration config = this.processor.getUnderlyingConfiguration();
        UserFunctionParameter[] params = uf.getParameterDefinitions();
        Sequence[] vr = new GroundedValue[arguments.length];
        for (int i = 0; i < arguments.length; ++i) {
            SequenceType type = params[i].getRequiredType();
            vr[i] = arguments[i].getUnderlyingValue();
            if (type.matches(vr[i], config.getTypeHierarchy())) continue;
            RoleDiagnostic role = new RoleDiagnostic(0, uf.getFunctionName().getDisplayName(), i);
            Sequence converted = config.getTypeHierarchy().applyFunctionConversionRules(vr[i], type, role, Loc.NONE);
            vr[i] = converted.materialize();
        }
        return vr;
    }

    public synchronized void callFunction(QName function, XdmValue[] arguments, Destination destination) throws SaxonApiException {
        this.prime();
        try {
            Component f = this.getFunctionComponent(function, arguments);
            UserFunction uf = (UserFunction)f.getActor();
            Sequence[] vr = this.typeCheckFunctionArguments(uf, arguments);
            XPathContextMajor context = this.controller.newXPathContext();
            context.setCurrentComponent(f);
            context.setTemporaryOutputState(158);
            context.setCurrentOutputUri(null);
            SerializationProperties params = this.controller.getExecutable().getPrimarySerializationProperties();
            Receiver receiver = destination.getReceiver(this.controller.makePipelineConfiguration(), params);
            receiver.open();
            uf.process(context, vr, new ComplexContentOutputter(receiver));
            receiver.close();
        } catch (XPathException e) {
            this.getErrorReporter().report(new XmlProcessingException(e));
            throw new SaxonApiException(e);
        }
        destination.closeAndNotify();
    }

    public Destination asDocumentDestination(final Destination finalDestination) {
        return new AbstractDestination(){
            Receiver r;

            @Override
            public Receiver getReceiver(PipelineConfiguration pipe, SerializationProperties params) throws SaxonApiException {
                Receiver rt = Xslt30Transformer.this.getReceivingTransformer(Xslt30Transformer.this.controller, Xslt30Transformer.this.globalParameterSet, finalDestination);
                rt = new SequenceNormalizerWithSpaceSeparator(rt);
                rt.setPipelineConfiguration(pipe);
                this.r = rt;
                return this.r;
            }

            @Override
            public void close() throws SaxonApiException {
                try {
                    this.r.close();
                } catch (XPathException e) {
                    throw new SaxonApiException(e);
                }
            }
        };
    }

    public Serializer newSerializer() {
        Serializer serializer = this.processor.newSerializer();
        serializer.setOutputProperties(this.controller.getExecutable().getPrimarySerializationProperties());
        return serializer;
    }

    public Serializer newSerializer(File file) {
        Serializer serializer = this.processor.newSerializer(file);
        serializer.setOutputProperties(this.controller.getExecutable().getPrimarySerializationProperties());
        this.setBaseOutputURI(file.toURI().toString());
        return serializer;
    }

    public Serializer newSerializer(Writer writer) {
        Serializer serializer = this.newSerializer();
        serializer.setOutputWriter(writer);
        return serializer;
    }

    public Serializer newSerializer(OutputStream stream) {
        Serializer serializer = this.newSerializer();
        serializer.setOutputStream(stream);
        return serializer;
    }
}

