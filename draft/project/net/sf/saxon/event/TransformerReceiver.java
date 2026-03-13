/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import javax.xml.transform.TransformerException;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;

public class TransformerReceiver
extends ProxyReceiver {
    private XsltController controller;
    private Builder builder;
    private Receiver destination;

    public TransformerReceiver(XsltController controller) {
        super(controller.makeBuilder());
        this.controller = controller;
        this.builder = (Builder)this.getNextReceiver();
        this.builder.setUseEventLocation(false);
    }

    @Override
    public void open() throws XPathException {
        this.builder.setSystemId(this.systemId);
        Receiver stripper = this.controller.makeStripper(this.builder);
        if (this.controller.isStylesheetStrippingTypeAnnotations()) {
            stripper = this.controller.getConfiguration().getAnnotationStripper(stripper);
        }
        this.setUnderlyingReceiver(stripper);
        this.nextReceiver.open();
    }

    public Controller getController() {
        return this.controller;
    }

    @Override
    public void setSystemId(String systemId) {
        super.setSystemId(systemId);
        this.controller.setBaseOutputURI(systemId);
    }

    public void setDestination(Receiver destination) {
        this.destination = destination;
    }

    public Receiver getDestination() {
        return this.destination;
    }

    @Override
    public void close() throws XPathException {
        if (this.builder != null) {
            this.nextReceiver.close();
            NodeInfo doc = this.builder.getCurrentRoot();
            this.builder.reset();
            this.builder = null;
            if (doc == null) {
                throw new XPathException("No source document has been built");
            }
            doc.getTreeInfo().setSpaceStrippingRule(this.controller.getSpaceStrippingRule());
            if (this.destination == null) {
                throw new XPathException("No output destination has been supplied");
            }
            try {
                this.controller.setGlobalContextItem(doc);
                this.controller.applyTemplates(doc, this.destination);
            } catch (TransformerException e) {
                throw XPathException.makeXPathException(e);
            }
        }
    }
}

