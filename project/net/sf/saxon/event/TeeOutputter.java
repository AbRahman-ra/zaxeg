/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceReceiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class TeeOutputter
extends SequenceReceiver {
    private Receiver seq1;
    private Receiver seq2;

    public TeeOutputter(Receiver seq1, Receiver seq2) {
        super(seq1.getPipelineConfiguration());
        this.seq1 = seq1;
        this.seq2 = seq2;
    }

    protected void setFirstDestination(Receiver seq1) {
        this.seq1 = seq1;
    }

    protected void setSecondDestination(Receiver seq2) {
        this.seq2 = seq2;
    }

    protected Receiver getFirstDestination() {
        return this.seq1;
    }

    protected Receiver getSecondDestination() {
        return this.seq2;
    }

    @Override
    public void setUnparsedEntity(String name, String systemID, String publicID) throws XPathException {
        this.seq1.setUnparsedEntity(name, systemID, publicID);
        this.seq2.setUnparsedEntity(name, systemID, publicID);
    }

    @Override
    public void append(Item item, Location locationId, int properties) throws XPathException {
        this.seq1.append(item, locationId, properties);
        this.seq2.append(item, locationId, properties);
    }

    @Override
    public void open() throws XPathException {
        super.open();
        this.seq1.open();
        this.seq2.open();
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        this.seq1.startDocument(properties);
        this.seq2.startDocument(properties);
    }

    @Override
    public void endDocument() throws XPathException {
        this.seq1.endDocument();
        this.seq2.endDocument();
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.seq1.startElement(elemName, type, attributes, namespaces, location, properties);
        this.seq2.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public void endElement() throws XPathException {
        this.seq1.endElement();
        this.seq2.endElement();
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.seq1.characters(chars, locationId, properties);
        this.seq2.characters(chars, locationId, properties);
    }

    @Override
    public void processingInstruction(String name, CharSequence data, Location locationId, int properties) throws XPathException {
        this.seq1.processingInstruction(name, data, locationId, properties);
        this.seq2.processingInstruction(name, data, locationId, properties);
    }

    @Override
    public void comment(CharSequence content, Location locationId, int properties) throws XPathException {
        this.seq1.comment(content, locationId, properties);
        this.seq2.comment(content, locationId, properties);
    }

    @Override
    public void close() throws XPathException {
        this.seq1.close();
        this.seq2.close();
    }

    @Override
    public boolean usesTypeAnnotations() {
        return this.seq1.usesTypeAnnotations() || this.seq2.usesTypeAnnotations();
    }
}

