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
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;

public class TracingFilter
extends ProxyReceiver {
    private static int nextid = 0;
    private int id;
    private String indent = "";
    private PrintStream out = System.err;
    private boolean closed = false;

    public TracingFilter(Receiver nextReceiver) {
        super(nextReceiver);
        this.id = nextid++;
    }

    public TracingFilter(Receiver nextReceiver, PrintStream diagnosticOutput) {
        super(nextReceiver);
        this.id = nextid++;
        this.out = diagnosticOutput;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        this.out.println("RCVR " + this.id + this.indent + " APPEND " + item.getClass().getName());
        if (this.nextReceiver instanceof SequenceReceiver) {
            this.nextReceiver.append(item, locationId, copyNamespaces);
        } else {
            super.append(item, locationId, copyNamespaces);
        }
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.out.println("RCVR " + this.id + this.indent + " CHARACTERS " + (Whitespace.isWhite(chars) ? "(whitespace)" : ""));
        FastStringBuffer sb = new FastStringBuffer(chars.length() * 4);
        sb.cat(chars).append(":");
        for (int i = 0; i < chars.length(); ++i) {
            sb.append(chars.charAt(i) + " ");
        }
        this.out.println("    \"" + sb + '\"');
        this.nextReceiver.characters(chars, locationId, properties);
    }

    @Override
    public void close() throws XPathException {
        this.out.println("RCVR " + this.id + this.indent + " CLOSE");
        this.nextReceiver.close();
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.out.println("RCVR " + this.id + this.indent + " COMMENT");
        this.nextReceiver.comment(chars, locationId, properties);
    }

    @Override
    public void endDocument() throws XPathException {
        this.out.println("RCVR " + this.id + this.indent + " END DOCUMENT");
        this.nextReceiver.endDocument();
    }

    @Override
    public void endElement() throws XPathException {
        if (this.indent.isEmpty()) {
            throw new XPathException("RCVR " + this.id + " Unmatched end tag");
        }
        this.indent = this.indent.substring(2);
        this.out.println("RCVR " + this.id + this.indent + " END ELEMENT");
        this.nextReceiver.endElement();
    }

    @Override
    public void open() throws XPathException {
        this.out.println("RCVR " + this.id + this.indent + " OPEN");
        this.nextReceiver.open();
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        this.out.println("RCVR " + this.id + this.indent + " PROCESSING INSTRUCTION");
        this.nextReceiver.processingInstruction(target, data, locationId, properties);
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        this.out.println("RCVR " + this.id + this.indent + " START DOCUMENT");
        this.nextReceiver.startDocument(properties);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.out.println("RCVR " + this.id + this.indent + " START ELEMENT " + elemName.getDisplayName());
        this.indent = this.indent + "  ";
        this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
    }
}

