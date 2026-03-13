/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;

public class EventMonitor
extends Outputter {
    private boolean written = false;
    private final Outputter next;

    public EventMonitor(Outputter next) {
        this.next = next;
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        this.written = true;
        this.next.startDocument(properties);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, Location location, int properties) throws XPathException {
        this.written = true;
        this.next.startElement(elemName, type, location, properties);
    }

    @Override
    public void endElement() throws XPathException {
        this.written = true;
        this.next.endElement();
    }

    @Override
    public void attribute(NodeName name, SimpleType type, CharSequence value, Location location, int properties) throws XPathException {
        this.written = true;
        this.next.attribute(name, type, value, location, properties);
    }

    @Override
    public void namespace(String prefix, String uri, int properties) throws XPathException {
        this.written = true;
        this.next.namespace(prefix, uri, properties);
    }

    @Override
    public void startContent() throws XPathException {
        this.written = true;
        this.next.startContent();
    }

    @Override
    public void characters(CharSequence chars, Location location, int properties) throws XPathException {
        this.written = true;
        this.next.characters(chars, location, properties);
    }

    @Override
    public void processingInstruction(String name, CharSequence data, Location location, int properties) throws XPathException {
        this.written = true;
        this.next.processingInstruction(name, data, location, properties);
    }

    @Override
    public void comment(CharSequence content, Location location, int properties) throws XPathException {
        this.written = true;
        this.next.comment(content, location, properties);
    }

    @Override
    public void append(Item item, Location location, int properties) throws XPathException {
        this.written = true;
        this.next.append(item, location, properties);
    }

    @Override
    public void endDocument() throws XPathException {
        this.written = true;
        this.next.endDocument();
    }

    public boolean hasBeenWrittenTo() {
        return this.written;
    }
}

