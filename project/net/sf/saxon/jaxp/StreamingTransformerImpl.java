/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.jaxp;

import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.TransformerHandler;
import net.sf.saxon.Controller;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceivingContentHandler;
import net.sf.saxon.event.TreeReceiver;
import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.jaxp.AbstractTransformerImpl;
import net.sf.saxon.jaxp.StreamingFilterImpl;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import org.xml.sax.XMLFilter;

public class StreamingTransformerImpl
extends AbstractTransformerImpl {
    private Xslt30Transformer xsltTransformer;
    private Map<QName, XdmValue> convertedParameters = new HashMap<QName, XdmValue>();

    protected StreamingTransformerImpl(XsltExecutable e, Xslt30Transformer t) {
        super(e);
        this.xsltTransformer = t;
    }

    @Override
    public void transform(Source xmlSource, Result outputTarget) throws XPathException {
        try {
            Destination destination;
            this.xsltTransformer.setStylesheetParameters(this.convertedParameters);
            if (outputTarget.getSystemId() != null) {
                this.xsltTransformer.setBaseOutputURI(outputTarget.getSystemId());
            }
            if ((destination = this.makeDestination(outputTarget)) == null) {
                SerializerFactory sf = this.getConfiguration().getSerializerFactory();
                Receiver r = sf.getReceiver(outputTarget, new SerializationProperties(this.getLocalOutputProperties()), this.getConfiguration().makePipelineConfiguration());
                this.transform(xmlSource, r);
                return;
            }
            this.xsltTransformer.applyTemplates(xmlSource, destination);
        } catch (SaxonApiException e) {
            throw XPathException.makeXPathException(e);
        }
    }

    @Override
    protected void setConvertedParameter(QName name, XdmValue value) {
        this.convertedParameters.put(name, value);
    }

    @Override
    public void clearParameters() {
        super.clearParameters();
        this.convertedParameters.clear();
    }

    @Override
    public void setURIResolver(URIResolver resolver) {
        super.setURIResolver(resolver);
        this.xsltTransformer.setURIResolver(resolver);
    }

    @Override
    public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
        super.setErrorListener(listener);
        this.xsltTransformer.setErrorListener(listener);
    }

    public void setInitialMode(String name) throws IllegalArgumentException {
        this.xsltTransformer.setInitialMode(QName.fromClarkName(name));
    }

    public Xslt30Transformer getUnderlyingXsltTransformer() {
        return this.xsltTransformer;
    }

    @Override
    public Controller getUnderlyingController() {
        return this.xsltTransformer.getUnderlyingController();
    }

    @Override
    public XMLFilter newXMLFilter() {
        return new StreamingFilterImpl(this.xsltTransformer);
    }

    public TransformerHandler newTransformerHandler() throws XPathException {
        XsltController controller = this.xsltTransformer.getUnderlyingController();
        return new StreamingTransformerHandler(controller);
    }

    public class StreamingTransformerHandler
    extends ReceivingContentHandler
    implements TransformerHandler {
        private XsltController controller;
        private String systemId;

        public StreamingTransformerHandler(XsltController controller) {
            this.controller = controller;
        }

        @Override
        public void setResult(Result result) throws IllegalArgumentException {
            try {
                PipelineConfiguration pipe = this.controller.makePipelineConfiguration();
                this.setPipelineConfiguration(pipe);
                this.controller.initializeController(new GlobalParameterSet());
                Receiver out = this.controller.getConfiguration().getSerializerFactory().getReceiver(result, new SerializationProperties(), pipe);
                Receiver in = this.controller.getStreamingReceiver(this.controller.getInitialMode(), new TreeReceiver(out));
                this.setReceiver(in);
            } catch (XPathException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public void setSystemId(String systemID) {
            this.systemId = systemID;
        }

        @Override
        public String getSystemId() {
            return this.systemId;
        }

        @Override
        public Transformer getTransformer() {
            return StreamingTransformerImpl.this;
        }
    }
}

