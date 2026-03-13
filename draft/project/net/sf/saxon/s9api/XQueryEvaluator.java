/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.RegularSequenceChecker;
import net.sf.saxon.event.SequenceNormalizer;
import net.sf.saxon.event.Stripper;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.GlobalContextRequirement;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.s9api.AbstractDestination;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.ValidationMode;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.streams.XdmStream;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;

public class XQueryEvaluator
extends AbstractDestination
implements Iterable<XdmItem> {
    private Processor processor;
    private XQueryExpression expression;
    private DynamicQueryContext context;
    private Controller controller;
    private Destination destination;
    private Set<XdmNode> updatedDocuments;
    private Builder sourceTreeBuilder;

    protected XQueryEvaluator(Processor processor, XQueryExpression expression) {
        this.processor = processor;
        this.expression = expression;
        this.context = new DynamicQueryContext(expression.getConfiguration());
    }

    public void setSchemaValidationMode(ValidationMode mode) {
        if (mode != null) {
            this.context.setSchemaValidationMode(mode.getNumber());
        }
    }

    public ValidationMode getSchemaValidationMode() {
        return ValidationMode.get(this.context.getSchemaValidationMode());
    }

    public void setSource(Source source) throws SaxonApiException {
        if (source instanceof NodeInfo) {
            this.setContextItem(new XdmNode((NodeInfo)source));
        } else if (source instanceof DOMSource) {
            this.setContextItem(this.processor.newDocumentBuilder().wrap(source));
        } else {
            this.setContextItem(this.processor.newDocumentBuilder().build(source));
        }
    }

    public void setContextItem(XdmItem item) throws SaxonApiException {
        if (item != null) {
            GlobalContextRequirement gcr = this.expression.getExecutable().getGlobalContextRequirement();
            if (gcr != null && !gcr.isExternal()) {
                throw new SaxonApiException("The context item for the query is not defined as external");
            }
            this.context.setContextItem(item.getUnderlyingValue());
        }
    }

    public XdmItem getContextItem() {
        Item item = this.context.getContextItem();
        if (item == null) {
            return null;
        }
        return (XdmItem)XdmValue.wrap(item);
    }

    public void setExternalVariable(QName name, XdmValue value) {
        try {
            this.context.setParameter(name.getStructuredQName(), value == null ? null : value.getUnderlyingValue().materialize());
        } catch (XPathException e) {
            throw new SaxonApiUncheckedException(e);
        }
    }

    public XdmValue getExternalVariable(QName name) {
        GroundedValue oval = this.context.getParameter(name.getStructuredQName());
        if (oval == null) {
            return null;
        }
        return XdmValue.wrap(oval);
    }

    public void setURIResolver(URIResolver resolver) {
        this.context.setURIResolver(resolver);
    }

    public URIResolver getURIResolver() {
        return this.context.getURIResolver();
    }

    public void setErrorListener(ErrorListener listener) {
        this.context.setErrorListener(listener);
    }

    public ErrorListener getErrorListener() {
        return this.context.getErrorListener();
    }

    public void setTraceListener(TraceListener listener) {
        this.context.setTraceListener(listener);
    }

    public TraceListener getTraceListener() {
        return this.context.getTraceListener();
    }

    public void setTraceFunctionDestination(Logger stream) {
        this.context.setTraceFunctionDestination(stream);
    }

    public Logger getTraceFunctionDestination() {
        return this.context.getTraceFunctionDestination();
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public void run() throws SaxonApiException {
        try {
            if (this.expression.isUpdateQuery()) {
                Set<MutableNodeInfo> docs = this.expression.runUpdate(this.context);
                this.updatedDocuments = new HashSet<XdmNode>();
                for (MutableNodeInfo doc : docs) {
                    this.updatedDocuments.add(XdmNode.wrapItem(doc));
                }
            } else {
                if (this.destination == null) {
                    throw new IllegalStateException("No destination supplied");
                }
                this.run(this.destination);
            }
        } catch (TransformerException e) {
            throw new SaxonApiException(e);
        }
    }

    public void run(Destination destination) throws SaxonApiException {
        if (this.expression.isUpdateQuery()) {
            throw new IllegalStateException("Query is updating");
        }
        try {
            Receiver out = this.getDestinationReceiver(destination);
            this.expression.run(this.context, out, null);
            destination.closeAndNotify();
        } catch (TransformerException e) {
            throw new SaxonApiException(e);
        }
    }

    public void runStreamed(Source source, Destination destination) throws SaxonApiException {
        if (this.expression.isUpdateQuery()) {
            throw new IllegalStateException("Query is updating; cannot run with streaming");
        }
        Configuration config = this.context.getConfiguration();
        if (config.isTiming()) {
            String systemId = source.getSystemId();
            if (systemId == null) {
                systemId = "";
            }
            config.getLogger().info("Processing streamed input " + systemId);
        }
        try {
            SerializationProperties params = this.expression.getExecutable().getPrimarySerializationProperties();
            Receiver receiver = destination.getReceiver(config.makePipelineConfiguration(), params);
            this.expression.runStreamed(this.context, source, receiver, null);
        } catch (TransformerException e) {
            throw new SaxonApiException(e);
        }
    }

    public XdmValue evaluate() throws SaxonApiException {
        if (this.expression.isUpdateQuery()) {
            throw new IllegalStateException("Query is updating");
        }
        try {
            SequenceIterator iter = this.expression.iterator(this.context);
            return XdmValue.wrap(iter.materialize());
        } catch (UncheckedXPathException e) {
            throw new SaxonApiException(e.getXPathException());
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public XdmItem evaluateSingle() throws SaxonApiException {
        try {
            SequenceIterator iter = this.expression.iterator(this.context);
            Item next = iter.next();
            return next == null ? null : (XdmItem)XdmValue.wrap(next);
        } catch (UncheckedXPathException e) {
            throw new SaxonApiException(e.getXPathException());
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    @Override
    public XdmSequenceIterator<XdmItem> iterator() throws SaxonApiUncheckedException {
        if (this.expression.isUpdateQuery()) {
            throw new IllegalStateException("Query is updating");
        }
        try {
            return new XdmSequenceIterator<XdmItem>(this.expression.iterator(this.context));
        } catch (XPathException e) {
            throw new SaxonApiUncheckedException(e);
        }
    }

    public XdmStream<XdmItem> stream() throws SaxonApiUncheckedException {
        return ((XdmSequenceIterator)this.iterator()).stream();
    }

    private Receiver getDestinationReceiver(Destination destination) throws SaxonApiException {
        Executable exec = this.expression.getExecutable();
        PipelineConfiguration pipe = this.expression.getConfiguration().makePipelineConfiguration();
        Receiver out = destination.getReceiver(pipe, exec.getPrimarySerializationProperties());
        if (Configuration.isAssertionsEnabled()) {
            return new RegularSequenceChecker(out, true);
        }
        return out;
    }

    @Override
    public Receiver getReceiver(PipelineConfiguration pipe, SerializationProperties params) throws SaxonApiException {
        if (this.destination == null) {
            throw new IllegalStateException("No destination has been supplied");
        }
        try {
            if (this.controller == null) {
                this.controller = this.expression.newController(this.context);
            } else {
                this.context.initializeController(this.controller);
            }
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
        this.sourceTreeBuilder = this.controller.makeBuilder();
        if (this.sourceTreeBuilder instanceof TinyBuilder) {
            ((TinyBuilder)this.sourceTreeBuilder).setStatistics(this.context.getConfiguration().getTreeStatistics().SOURCE_DOCUMENT_STATISTICS);
        }
        Stripper out = this.controller.makeStripper(this.sourceTreeBuilder);
        SequenceNormalizer sn = params.makeSequenceNormalizer(out);
        sn.onClose(() -> {
            NodeInfo doc = this.sourceTreeBuilder.getCurrentRoot();
            if (doc == null) {
                throw new SaxonApiException("No source document has been built by the previous pipeline stage");
            }
            doc.getTreeInfo().setSpaceStrippingRule(this.controller.getSpaceStrippingRule());
            this.setSource(doc);
            this.sourceTreeBuilder = null;
            this.run(this.destination);
            this.destination.closeAndNotify();
        });
        return sn;
    }

    @Override
    public void close() throws SaxonApiException {
    }

    public Iterator<XdmNode> getUpdatedDocuments() {
        return this.updatedDocuments.iterator();
    }

    public XdmValue callFunction(QName function, XdmValue[] arguments) throws SaxonApiException {
        UserFunction fn = this.expression.getMainModule().getUserDefinedFunction(function.getNamespaceURI(), function.getLocalName(), arguments.length);
        if (fn == null) {
            throw new SaxonApiException("No function with name " + function.getClarkName() + " and arity " + arguments.length + " has been declared in the query");
        }
        try {
            if (this.controller == null) {
                this.controller = this.expression.newController(this.context);
            } else {
                this.context.initializeController(this.controller);
            }
            Configuration config = this.processor.getUnderlyingConfiguration();
            Sequence[] vr = SequenceTool.makeSequenceArray(arguments.length);
            for (int i = 0; i < arguments.length; ++i) {
                SequenceType type = fn.getParameterDefinitions()[i].getRequiredType();
                vr[i] = arguments[i].getUnderlyingValue();
                TypeHierarchy th = config.getTypeHierarchy();
                if (type.matches(vr[i], th)) continue;
                RoleDiagnostic role = new RoleDiagnostic(0, function.getStructuredQName().getDisplayName(), i);
                vr[i] = th.applyFunctionConversionRules(vr[i], type, role, Loc.NONE);
            }
            Sequence result = fn.call(vr, this.controller);
            return XdmValue.wrap(result);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public DynamicQueryContext getUnderlyingQueryContext() {
        return this.context;
    }
}

