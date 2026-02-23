/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import java.io.PrintStream;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceReceiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.Instrumentation;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class CountingFilter
extends ProxyReceiver {
    private static int nextid = 0;
    private int id = nextid++;

    public CountingFilter(Receiver nextReceiver) {
        super(nextReceiver);
    }

    public CountingFilter(Receiver nextReceiver, PrintStream diagnosticOutput) {
        super(nextReceiver);
    }

    public int getId() {
        return this.id;
    }

    private void count(String counter) {
        Instrumentation.count("Filter " + this.id + " " + counter);
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        this.count("append");
        if (this.nextReceiver instanceof SequenceReceiver) {
            ((SequenceReceiver)this.nextReceiver).append(item, locationId, copyNamespaces);
        } else {
            super.append(item, locationId, copyNamespaces);
        }
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.count("characters");
        this.nextReceiver.characters(chars, locationId, properties);
    }

    @Override
    public void close() throws XPathException {
        this.count("close");
        this.nextReceiver.close();
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.count("comment");
        this.nextReceiver.comment(chars, locationId, properties);
    }

    @Override
    public void endDocument() throws XPathException {
        this.count("endDocument");
        this.nextReceiver.endDocument();
    }

    @Override
    public void endElement() throws XPathException {
        this.count("endElement");
        this.nextReceiver.endElement();
    }

    @Override
    public void open() throws XPathException {
        this.count("open");
        this.nextReceiver.open();
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        this.count("processingInstruction");
        this.nextReceiver.processingInstruction(target, data, locationId, properties);
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        this.count("startDocument");
        this.nextReceiver.startDocument(properties);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.count("startElement");
        this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
    }
}

