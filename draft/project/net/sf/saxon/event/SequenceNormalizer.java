/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Action;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public abstract class SequenceNormalizer
extends ProxyReceiver {
    protected int level = 0;
    private List<Action> actionList;
    private boolean failed = false;

    public SequenceNormalizer(Receiver next) {
        super(next);
    }

    @Override
    public void open() throws XPathException {
        this.level = 0;
        this.previousAtomic = false;
        super.open();
        this.getNextReceiver().startDocument(0);
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        ++this.level;
        this.previousAtomic = false;
    }

    @Override
    public void endDocument() throws XPathException {
        --this.level;
        this.previousAtomic = false;
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        try {
            ++this.level;
            super.startElement(elemName, type, attributes, namespaces, location, properties);
            this.previousAtomic = false;
        } catch (XPathException e) {
            this.failed = true;
            throw e;
        }
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        try {
            super.characters(chars, locationId, properties);
            this.previousAtomic = false;
        } catch (XPathException e) {
            this.failed = true;
            throw e;
        }
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        try {
            super.processingInstruction(target, data, locationId, properties);
            this.previousAtomic = false;
        } catch (XPathException e) {
            this.failed = true;
            throw e;
        }
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        try {
            super.comment(chars, locationId, properties);
            this.previousAtomic = false;
        } catch (XPathException e) {
            this.failed = true;
            throw e;
        }
    }

    @Override
    public void endElement() throws XPathException {
        try {
            --this.level;
            super.endElement();
            this.previousAtomic = false;
        } catch (XPathException e) {
            this.failed = true;
            throw e;
        }
    }

    @Override
    public void close() throws XPathException {
        if (this.failed) {
            super.close();
        } else {
            this.getNextReceiver().endDocument();
            super.close();
            try {
                if (this.actionList != null) {
                    for (Action action : this.actionList) {
                        action.act();
                    }
                }
            } catch (SaxonApiException e) {
                throw XPathException.makeXPathException(e);
            }
        }
    }

    public void onClose(List<Action> actionList) {
        this.actionList = actionList;
    }

    public void onClose(Action action) {
        if (this.actionList == null) {
            this.actionList = new ArrayList<Action>();
        }
        this.actionList.add(action);
    }
}

