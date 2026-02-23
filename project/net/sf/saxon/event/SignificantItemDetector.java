/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.ProxyOutputter;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.om.Action;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.value.AtomicValue;

public class SignificantItemDetector
extends ProxyOutputter {
    private int level = 0;
    private boolean empty = true;
    private Action trigger;

    public SignificantItemDetector(Outputter next, Action trigger) {
        super(next);
        this.trigger = trigger;
    }

    private void start() throws XPathException {
        if (this.empty) {
            this.trigger.doAction();
            this.empty = false;
        }
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        if (this.level++ != 0) {
            this.getNextOutputter().startDocument(properties);
        }
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, Location location, int properties) throws XPathException {
        this.start();
        ++this.level;
        this.getNextOutputter().startElement(elemName, type, location, properties);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.start();
        ++this.level;
        this.getNextOutputter().startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public void namespace(String prefix, String namespaceUri, int properties) throws XPathException {
        this.getNextOutputter().namespace(prefix, namespaceUri, properties);
    }

    @Override
    public void attribute(NodeName attName, SimpleType typeCode, CharSequence value, Location location, int properties) throws XPathException {
        this.getNextOutputter().attribute(attName, typeCode, value, location, properties);
    }

    @Override
    public void startContent() throws XPathException {
        this.getNextOutputter().startContent();
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (chars.length() > 0) {
            this.start();
        }
        this.getNextOutputter().characters(chars, locationId, properties);
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        this.start();
        this.getNextOutputter().processingInstruction(target, data, locationId, properties);
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.start();
        this.getNextOutputter().comment(chars, locationId, properties);
    }

    public static boolean isSignificant(Item item) {
        if (item instanceof NodeInfo) {
            NodeInfo node = (NodeInfo)item;
            return !(node.getNodeKind() == 3 && node.getStringValue().isEmpty() || node.getNodeKind() == 9 && !node.hasChildNodes());
        }
        if (item instanceof AtomicValue) {
            return !item.getStringValue().isEmpty();
        }
        if (item instanceof ArrayItem) {
            if (((ArrayItem)item).isEmpty()) {
                return true;
            }
            for (Sequence sequence : ((ArrayItem)item).members()) {
                try {
                    Item it;
                    SequenceIterator memIter = sequence.iterate();
                    while ((it = memIter.next()) != null) {
                        if (!SignificantItemDetector.isSignificant(it)) continue;
                        return true;
                    }
                } catch (XPathException e) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        if (SignificantItemDetector.isSignificant(item)) {
            this.start();
        }
        super.append(item, locationId, copyNamespaces);
    }

    @Override
    public void endDocument() throws XPathException {
        if (--this.level != 0) {
            this.getNextOutputter().endDocument();
        }
    }

    @Override
    public void endElement() throws XPathException {
        --this.level;
        this.getNextOutputter().endElement();
    }

    public boolean isEmpty() {
        return this.empty;
    }
}

