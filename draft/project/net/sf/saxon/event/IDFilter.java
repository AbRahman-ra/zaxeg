/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.HashSet;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;

public class IDFilter
extends ProxyReceiver {
    private String requiredId;
    private int activeDepth = 0;
    private boolean matched = false;
    private HashSet<SimpleType> nonIDs;

    public IDFilter(Receiver next, String id) {
        super(next);
        this.requiredId = id;
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.matched = false;
        if (this.activeDepth == 0) {
            for (AttributeInfo att : attributes) {
                if (!att.getNodeName().equals(StandardNames.XML_ID_NAME) && !ReceiverOption.contains(att.getProperties(), 2048) || !att.getValue().equals(this.requiredId)) continue;
                this.matched = true;
            }
            if (this.matched) {
                this.activeDepth = 1;
                super.startElement(elemName, type, attributes, namespaces, location, properties);
            }
        } else {
            ++this.activeDepth;
            super.startElement(elemName, type, attributes, namespaces, location, properties);
        }
    }

    @Override
    public void endElement() throws XPathException {
        if (this.activeDepth > 0) {
            this.nextReceiver.endElement();
            --this.activeDepth;
        }
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.activeDepth > 0) {
            super.characters(chars, locationId, properties);
        }
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        if (this.activeDepth > 0) {
            super.processingInstruction(target, data, locationId, properties);
        }
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.activeDepth > 0) {
            super.comment(chars, locationId, properties);
        }
    }

    @Override
    public boolean usesTypeAnnotations() {
        return true;
    }
}

