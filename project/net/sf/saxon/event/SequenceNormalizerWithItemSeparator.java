/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceNormalizer;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.AtomicValue;

public class SequenceNormalizerWithItemSeparator
extends SequenceNormalizer {
    private String separator;
    private boolean first = true;

    public SequenceNormalizerWithItemSeparator(Receiver next, String separator) {
        super(next);
        this.separator = separator;
    }

    @Override
    public void open() throws XPathException {
        this.first = true;
        super.open();
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        this.sep();
        super.startDocument(properties);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.sep();
        super.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.sep();
        super.characters(chars, locationId, properties);
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        this.sep();
        super.processingInstruction(target, data, locationId, properties);
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.sep();
        super.comment(chars, locationId, properties);
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        if (item instanceof ArrayItem) {
            this.flatten((ArrayItem)item, locationId, copyNamespaces);
        } else if (item instanceof AtomicValue) {
            this.sep();
            this.nextReceiver.characters(item.getStringValueCS(), locationId, 0);
        } else {
            this.decompose(item, locationId, copyNamespaces);
        }
    }

    @Override
    public void close() throws XPathException {
        super.close();
    }

    private void sep() throws XPathException {
        if (this.level == 0 && !this.first) {
            super.characters(this.separator, Loc.NONE, 0);
        } else {
            this.first = false;
        }
    }
}

