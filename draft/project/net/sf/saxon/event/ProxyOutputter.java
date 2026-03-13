/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;

public class ProxyOutputter
extends Outputter {
    private Outputter next;

    public ProxyOutputter(Outputter next) {
        this.next = next;
        this.setPipelineConfiguration(next.getPipelineConfiguration());
        this.setSystemId(next.getSystemId());
    }

    public Outputter getNextOutputter() {
        return this.next;
    }

    @Override
    public void open() throws XPathException {
        this.next.open();
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        this.next.startDocument(properties);
    }

    @Override
    public void endDocument() throws XPathException {
        this.next.endDocument();
    }

    @Override
    public void setUnparsedEntity(String name, String systemID, String publicID) throws XPathException {
        this.next.setUnparsedEntity(name, systemID, publicID);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType typeCode, Location location, int properties) throws XPathException {
        this.next.startElement(elemName, typeCode, location, properties);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.next.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public void namespace(String prefix, String namespaceUri, int properties) throws XPathException {
        this.next.namespace(prefix, namespaceUri, properties);
    }

    @Override
    public void attribute(NodeName attName, SimpleType typeCode, CharSequence value, Location location, int properties) throws XPathException {
        this.next.attribute(attName, typeCode, value, location, properties);
    }

    @Override
    public void startContent() throws XPathException {
        this.next.startContent();
    }

    @Override
    public void endElement() throws XPathException {
        this.next.endElement();
    }

    @Override
    public void characters(CharSequence chars, Location location, int properties) throws XPathException {
        this.next.characters(chars, location, properties);
    }

    @Override
    public void processingInstruction(String name, CharSequence data, Location location, int properties) throws XPathException {
        this.next.processingInstruction(name, data, location, properties);
    }

    @Override
    public void comment(CharSequence content, Location location, int properties) throws XPathException {
        this.next.comment(content, location, properties);
    }

    @Override
    public void append(Item item, Location locationId, int properties) throws XPathException {
        this.next.append(item, locationId, properties);
    }

    @Override
    public void append(Item item) throws XPathException {
        this.next.append(item);
    }

    @Override
    public void close() throws XPathException {
        this.next.close();
    }

    @Override
    public boolean usesTypeAnnotations() {
        return this.next.usesTypeAnnotations();
    }
}

