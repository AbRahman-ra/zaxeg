/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.type.SchemaType;

public class PrincipalOutputGatekeeper
extends ProxyReceiver {
    private XsltController controller;
    private boolean usedAsPrimaryResult = false;
    private boolean usedAsSecondaryResult = false;
    private boolean open = false;
    private boolean closed = false;

    public PrincipalOutputGatekeeper(XsltController controller, Receiver next) {
        super(next);
        this.controller = controller;
    }

    @Override
    public void open() throws XPathException {
        if (this.closed) {
            String uri = this.getSystemId().equals("dummy:/anonymous/principal/result") ? "(no URI supplied)" : this.getSystemId();
            XPathException err = new XPathException("Cannot write more than one result document to the principal output destination: " + uri);
            err.setErrorCode("XTDE1490");
            throw err;
        }
        super.open();
        this.open = true;
    }

    @Override
    public synchronized void startDocument(int properties) throws XPathException {
        if (!this.open) {
            this.open();
        }
        this.nextReceiver.startDocument(properties);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.useAsPrimary();
        this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public synchronized void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.useAsPrimary();
        this.nextReceiver.characters(chars, locationId, properties);
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        this.useAsPrimary();
        this.nextReceiver.processingInstruction(target, data, locationId, properties);
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.useAsPrimary();
        this.nextReceiver.comment(chars, locationId, properties);
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        this.useAsPrimary();
        this.nextReceiver.append(item, locationId, copyNamespaces);
    }

    private synchronized void useAsPrimary() throws XPathException {
        if (this.closed) {
            XPathException err = new XPathException("Cannot write to the principal output destination as it has already been closed: " + this.identifySystemId());
            err.setErrorCode("XTDE1490");
            throw err;
        }
        if (this.usedAsSecondaryResult) {
            XPathException err = new XPathException("Cannot write to the principal output destination as it has already been used by xsl:result-document: " + this.identifySystemId());
            err.setErrorCode("XTDE1490");
            throw err;
        }
        this.usedAsPrimaryResult = true;
    }

    public synchronized void useAsSecondary() throws XPathException {
        if (this.usedAsPrimaryResult) {
            XPathException err = new XPathException("Cannot use xsl:result-document to write to a destination already used for the principal output: " + this.identifySystemId());
            err.setErrorCode("XTDE1490");
            throw err;
        }
        if (this.usedAsSecondaryResult) {
            XPathException err = new XPathException("Cannot write more than one xsl:result-document to the principal output destination: " + this.identifySystemId());
            err.setErrorCode("XTDE1490");
            throw err;
        }
        this.usedAsSecondaryResult = true;
    }

    public Receiver makeReceiver(SerializationProperties params) {
        try {
            Destination dest = this.controller.getPrincipalDestination();
            if (dest != null) {
                return dest.getReceiver(this.controller.makePipelineConfiguration(), params);
            }
        } catch (SaxonApiException e) {
            return null;
        }
        return null;
    }

    private String identifySystemId() {
        String uri = this.getSystemId();
        return uri == null ? "(no URI supplied)" : uri;
    }

    @Override
    public void close() throws XPathException {
        this.closed = true;
        if (this.usedAsPrimaryResult) {
            this.nextReceiver.close();
        }
    }
}

