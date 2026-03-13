/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.net.URI;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceNormalizerWithSpaceSeparator;
import net.sf.saxon.expr.instruct.GlobalContextRequirement;
import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.AbstractXsltTransformer;
import net.sf.saxon.s9api.Action;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.DestinationHelper;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.tree.linked.DocumentImpl;

public class XsltTransformer
extends AbstractXsltTransformer
implements Destination {
    private QName initialTemplateName;
    private GlobalParameterSet parameters;
    private Source initialSource;
    private Destination destination;
    private DestinationHelper destinationHelper = new DestinationHelper(this);
    private URI destinationBaseUri;

    protected XsltTransformer(Processor processor, XsltController controller, GlobalParameterSet staticParameters) {
        super(processor, controller);
        this.parameters = new GlobalParameterSet();
    }

    @Override
    public void setDestinationBaseURI(URI baseURI) {
        this.destinationBaseUri = baseURI;
    }

    @Override
    public URI getDestinationBaseURI() {
        return this.destinationBaseUri;
    }

    @Override
    public void onClose(Action listener) {
        this.destinationHelper.onClose(listener);
    }

    @Override
    public void closeAndNotify() throws SaxonApiException {
        this.destinationHelper.closeAndNotify();
    }

    public void setInitialTemplate(QName templateName) {
        this.initialTemplateName = templateName;
    }

    public QName getInitialTemplate() {
        return this.initialTemplateName;
    }

    public synchronized void setSource(Source source) {
        if (source instanceof NodeInfo) {
            this.setInitialContextNode(new XdmNode((NodeInfo)source));
        } else if (source instanceof DOMSource) {
            if (((DOMSource)source).getNode() == null) {
                DocumentImpl doc = new DocumentImpl();
                doc.setConfiguration(this.controller.getConfiguration());
                this.setInitialContextNode(new XdmNode(doc));
            } else {
                NodeInfo n = this.processor.getUnderlyingConfiguration().unravel(source);
                this.setInitialContextNode(new XdmNode(n));
            }
        } else {
            this.initialSource = source;
        }
    }

    public synchronized void setInitialContextNode(XdmNode node) throws SaxonApiUncheckedException {
        try {
            if (node == null) {
                this.initialSource = null;
                this.controller.setGlobalContextItem(null);
            } else {
                this.initialSource = node.getUnderlyingNode();
                this.controller.setGlobalContextItem(node.getUnderlyingNode().getRoot());
            }
        } catch (XPathException e) {
            throw new SaxonApiUncheckedException(e);
        }
    }

    public XdmNode getInitialContextNode() {
        if (this.initialSource instanceof NodeInfo) {
            return (XdmNode)XdmValue.wrap((NodeInfo)this.initialSource);
        }
        return null;
    }

    public synchronized void setParameter(QName name, XdmValue value) {
        try {
            this.parameters.put(name.getStructuredQName(), value == null ? null : value.getUnderlyingValue().materialize());
        } catch (XPathException e) {
            throw new SaxonApiUncheckedException(e);
        }
    }

    public synchronized void clearParameters() {
        this.parameters = new GlobalParameterSet();
    }

    public synchronized XdmValue getParameter(QName name) {
        GroundedValue oval = this.parameters.get(name.getStructuredQName());
        return oval == null ? null : XdmValue.wrap(oval);
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Destination getDestination() {
        return this.destination;
    }

    public synchronized void transform() throws SaxonApiException {
        Source initialSelection = this.initialSource;
        boolean reset = false;
        if (this.destination == null) {
            throw new IllegalStateException("No destination has been supplied");
        }
        try {
            Receiver out = this.getDestinationReceiver(this.controller, this.destination);
            GlobalContextRequirement gcr = this.controller.getExecutable().getGlobalContextRequirement();
            if (!(gcr != null && gcr.isAbsentFocus() || initialSelection == null)) {
                NodeInfo node;
                if (initialSelection instanceof NodeInfo) {
                    reset = this.maybeSetGlobalContextItem((NodeInfo)initialSelection);
                } else if (initialSelection instanceof DOMSource) {
                    node = this.controller.prepareInputTree(initialSelection);
                    reset = this.maybeSetGlobalContextItem(node);
                    initialSelection = node;
                } else {
                    node = this.controller.makeSourceTree(initialSelection, this.getSchemaValidationMode().getNumber());
                    reset = this.maybeSetGlobalContextItem(node);
                    initialSelection = node;
                }
            }
            if (this.baseOutputUriWasSet) {
                out.setSystemId(this.getBaseOutputURI());
            }
            this.controller.initializeController(this.parameters);
            if (this.initialTemplateName != null) {
                this.controller.callTemplate(this.initialTemplateName.getStructuredQName(), out);
            } else if (initialSelection != null) {
                this.applyTemplatesToSource(initialSelection, out);
            } else {
                QName entryPoint = new QName("xsl", "http://www.w3.org/1999/XSL/Transform", "initial-template");
                this.controller.callTemplate(entryPoint.getStructuredQName(), out);
            }
            this.destination.closeAndNotify();
        } catch (XPathException e) {
            if (!e.hasBeenReported()) {
                this.getErrorReporter().report(new XmlProcessingException(e));
                e.setHasBeenReported(true);
            }
            throw new SaxonApiException(e);
        } finally {
            if (reset) {
                this.controller.clearGlobalContextItem();
            }
        }
    }

    private boolean maybeSetGlobalContextItem(Item item) throws XPathException {
        if (this.controller.getGlobalContextItem() == null) {
            this.controller.setGlobalContextItem(item, true);
            return true;
        }
        return false;
    }

    @Override
    public Receiver getReceiver(PipelineConfiguration pipe, SerializationProperties params) throws SaxonApiException {
        if (this.destination == null) {
            throw new IllegalStateException("No destination has been supplied");
        }
        Receiver rt = this.getReceivingTransformer(this.controller, this.parameters, this.destination);
        rt = new SequenceNormalizerWithSpaceSeparator(rt);
        rt.setPipelineConfiguration(pipe);
        return rt;
    }

    @Override
    public void close() {
    }
}

