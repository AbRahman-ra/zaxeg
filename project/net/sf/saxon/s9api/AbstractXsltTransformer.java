/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.function.Function;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.EventSource;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.RegularSequenceChecker;
import net.sf.saxon.event.TreeReceiver;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.lib.AugmentedSource;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.ErrorReporterToListener;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.ResultDocumentResolver;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.MessageListener2;
import net.sf.saxon.s9api.MessageListener2Proxy;
import net.sf.saxon.s9api.MessageListenerProxy;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.ValidationMode;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.tree.tiny.TinyBuilder;

abstract class AbstractXsltTransformer {
    protected Processor processor;
    protected XsltController controller;
    protected boolean baseOutputUriWasSet = false;
    private MessageListener messageListener;
    private MessageListener2 messageListener2;

    AbstractXsltTransformer(Processor processor, XsltController controller) {
        this.processor = processor;
        this.controller = controller;
    }

    public synchronized void setBaseOutputURI(String uri) {
        this.controller.setBaseOutputURI(uri);
        this.baseOutputUriWasSet = uri != null;
    }

    public String getBaseOutputURI() {
        return this.controller.getBaseOutputURI();
    }

    public void setURIResolver(URIResolver resolver) {
        this.controller.setURIResolver(resolver);
    }

    public URIResolver getURIResolver() {
        return this.controller.getURIResolver();
    }

    public void setErrorListener(ErrorListener listener) {
        this.controller.setErrorReporter(new ErrorReporterToListener(listener));
    }

    public ErrorListener getErrorListener() {
        ErrorReporter uel = this.controller.getErrorReporter();
        if (uel instanceof ErrorReporterToListener) {
            return ((ErrorReporterToListener)uel).getErrorListener();
        }
        return null;
    }

    public void setErrorReporter(ErrorReporter reporter) {
        this.controller.setErrorReporter(reporter);
    }

    public ErrorReporter getErrorReporter() {
        return this.controller.getErrorReporter();
    }

    public void setResultDocumentHandler(final Function<URI, Destination> handler) {
        this.controller.setResultDocumentResolver(new ResultDocumentResolver(){

            @Override
            public Receiver resolve(XPathContext context, String href, String baseUri, SerializationProperties properties) throws XPathException {
                try {
                    Destination destination;
                    URI abs = ResolveURI.makeAbsolute(href, baseUri);
                    try {
                        destination = (Destination)handler.apply(abs);
                    } catch (SaxonApiUncheckedException e) {
                        XPathException xe = XPathException.makeXPathException(e);
                        xe.maybeSetErrorCode("SXRD0001");
                        throw xe;
                    }
                    try {
                        PipelineConfiguration pipe = context.getController().makePipelineConfiguration();
                        return destination.getReceiver(pipe, properties);
                    } catch (SaxonApiException e) {
                        throw XPathException.makeXPathException(e);
                    }
                } catch (URISyntaxException e) {
                    throw XPathException.makeXPathException(e);
                }
            }
        });
    }

    public synchronized void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
        this.controller.setMessageFactory(() -> new MessageListenerProxy(listener, this.controller.makePipelineConfiguration()));
    }

    public synchronized void setMessageListener(MessageListener2 listener) {
        this.messageListener2 = listener;
        this.controller.setMessageFactory(() -> new MessageListener2Proxy(listener, this.controller.makePipelineConfiguration()));
    }

    public MessageListener getMessageListener() {
        return this.messageListener;
    }

    public MessageListener2 getMessageListener2() {
        return this.messageListener2;
    }

    public void setAssertionsEnabled(boolean enabled) {
        this.controller.setAssertionsEnabled(enabled);
    }

    public boolean isAssertionsEnabled() {
        return this.controller.isAssertionsEnabled();
    }

    public void setTraceListener(TraceListener listener) {
        this.controller.setTraceListener(listener);
    }

    public TraceListener getTraceListener() {
        return this.controller.getTraceListener();
    }

    public void setTraceFunctionDestination(Logger stream) {
        this.controller.setTraceFunctionDestination(stream);
    }

    public Logger getTraceFunctionDestination() {
        return this.controller.getTraceFunctionDestination();
    }

    protected void applyTemplatesToSource(Source source, Receiver out) throws XPathException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(out);
        if (this.controller.getInitialMode().isDeclaredStreamable() && this.isStreamableSource(source)) {
            this.controller.applyStreamingTemplates(source, out);
        } else {
            NodeInfo node = source instanceof NodeInfo ? (NodeInfo)source : this.controller.makeSourceTree(source, this.controller.getSchemaValidationMode());
            this.controller.applyTemplates(node, out);
        }
    }

    protected boolean isStreamableSource(Source source) {
        if (source instanceof AugmentedSource) {
            return this.isStreamableSource(((AugmentedSource)source).getContainedSource());
        }
        Configuration config = this.controller.getConfiguration();
        try {
            source = config.getSourceResolver().resolveSource(source, config);
        } catch (XPathException e) {
            return false;
        }
        return source instanceof SAXSource || source instanceof StreamSource || source instanceof EventSource;
    }

    public void setSchemaValidationMode(ValidationMode mode) {
        if (mode != null) {
            this.controller.setSchemaValidationMode(mode.getNumber());
        }
    }

    public ValidationMode getSchemaValidationMode() {
        return ValidationMode.get(this.controller.getSchemaValidationMode());
    }

    public void setInitialMode(QName modeName) throws IllegalArgumentException {
        try {
            this.controller.setInitialMode(modeName == null ? null : modeName.getStructuredQName());
        } catch (XPathException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public QName getInitialMode() {
        StructuredQName mode = this.controller.getInitialModeName();
        if (mode == null) {
            return null;
        }
        return new QName(mode);
    }

    public XsltController getUnderlyingController() {
        return this.controller;
    }

    public Receiver getDestinationReceiver(XsltController controller, Destination destination) throws SaxonApiException {
        controller.setPrincipalDestination(destination);
        PipelineConfiguration pipe = controller.makePipelineConfiguration();
        SerializationProperties params = controller.getExecutable().getPrimarySerializationProperties();
        Receiver receiver = destination.getReceiver(pipe, params);
        if (Configuration.isAssertionsEnabled()) {
            receiver = new RegularSequenceChecker(receiver, true);
        }
        receiver.getPipelineConfiguration().setController(controller);
        if (this.baseOutputUriWasSet) {
            try {
                if (destination.getDestinationBaseURI() == null) {
                    destination.setDestinationBaseURI(new URI(controller.getBaseOutputURI()));
                }
            } catch (URISyntaxException uRISyntaxException) {}
        } else if (destination.getDestinationBaseURI() != null) {
            controller.setBaseOutputURI(destination.getDestinationBaseURI().toASCIIString());
        }
        receiver.setSystemId(controller.getBaseOutputURI());
        return receiver;
    }

    protected Receiver getReceivingTransformer(final XsltController controller, final GlobalParameterSet parameters, final Destination finalDestination) throws SaxonApiException {
        Configuration config = controller.getConfiguration();
        if (controller.getInitialMode().isDeclaredStreamable()) {
            Receiver sOut = this.getDestinationReceiver(controller, finalDestination);
            try {
                controller.initializeController(parameters);
                return controller.getStreamingReceiver(controller.getInitialMode(), sOut);
            } catch (TransformerException e) {
                throw new SaxonApiException(e);
            }
        }
        final Builder sourceTreeBuilder = controller.makeBuilder();
        if (sourceTreeBuilder instanceof TinyBuilder) {
            ((TinyBuilder)sourceTreeBuilder).setStatistics(config.getTreeStatistics().SOURCE_DOCUMENT_STATISTICS);
        }
        Receiver stripper = controller.makeStripper(sourceTreeBuilder);
        if (controller.isStylesheetStrippingTypeAnnotations()) {
            stripper = controller.getConfiguration().getAnnotationStripper(stripper);
        }
        return new TreeReceiver(stripper){
            boolean closed;
            {
                super(nextInChain);
                this.closed = false;
            }

            @Override
            public void close() throws XPathException {
                if (!this.closed) {
                    block5: {
                        try {
                            NodeInfo doc = sourceTreeBuilder.getCurrentRoot();
                            if (doc == null) break block5;
                            doc.getTreeInfo().setSpaceStrippingRule(controller.getSpaceStrippingRule());
                            Receiver result = AbstractXsltTransformer.this.getDestinationReceiver(controller, finalDestination);
                            try {
                                controller.setGlobalContextItem(doc);
                                controller.initializeController(parameters);
                                controller.applyTemplates(doc, result);
                            } catch (TransformerException e) {
                                throw new SaxonApiException(e);
                            }
                        } catch (SaxonApiException e) {
                            throw XPathException.makeXPathException(e);
                        }
                    }
                    this.closed = true;
                }
            }
        };
    }
}

