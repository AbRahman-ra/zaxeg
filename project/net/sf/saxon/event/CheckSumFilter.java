/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;

public class CheckSumFilter
extends ProxyReceiver {
    private static final boolean DEBUG = false;
    private int checksum = 0;
    private int sequence = 0;
    private boolean checkExistingChecksum = false;
    private boolean checksumCorrect = false;
    private boolean checksumFound = false;
    public static final String SIGMA = "\u03a3";

    public CheckSumFilter(Receiver nextReceiver) {
        super(nextReceiver);
    }

    public void setCheckExistingChecksum(boolean check) {
        this.checkExistingChecksum = check;
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        super.startDocument(properties);
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        this.checksum ^= this.hash(item.toString(), this.sequence++);
        super.append(item, locationId, copyNamespaces);
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (!Whitespace.isWhite(chars)) {
            this.checksum ^= this.hash(chars, this.sequence++);
        }
        super.characters(chars, locationId, properties);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.checksum ^= this.hash(elemName, this.sequence++);
        this.checksumCorrect = false;
        for (AttributeInfo att : attributes) {
            this.checksum ^= this.hash(att.getNodeName(), this.sequence);
            this.checksum ^= this.hash(att.getValue(), this.sequence);
        }
        super.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public void endElement() throws XPathException {
        this.checksum ^= 1;
        super.endElement();
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        if (target.equals(SIGMA)) {
            this.checksumFound = true;
            if (this.checkExistingChecksum) {
                try {
                    int found = (int)Long.parseLong("0" + data, 16);
                    this.checksumCorrect = found == this.checksum;
                } catch (NumberFormatException e) {
                    this.checksumCorrect = false;
                }
            }
        }
        super.processingInstruction(target, data, locationId, properties);
    }

    public boolean isChecksumFound() {
        return this.checksumFound;
    }

    public int getChecksum() {
        return this.checksum;
    }

    public boolean isChecksumCorrect() {
        return this.checksumCorrect || "skip".equals(System.getProperty("saxon-checksum"));
    }

    private int hash(CharSequence s, int sequence) {
        int h = sequence << 8;
        for (int i = 0; i < s.length(); ++i) {
            h = (h << 1) + s.charAt(i);
        }
        return h;
    }

    private int hash(NodeName n, int sequence) {
        return this.hash(n.getLocalPart(), sequence) ^ this.hash(n.getURI(), sequence);
    }
}

