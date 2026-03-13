/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.CodedName;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class NamePoolConverter
extends ProxyReceiver {
    NamePool oldPool;
    NamePool newPool;

    public NamePoolConverter(Receiver next, NamePool oldPool, NamePool newPool) {
        super(next);
        this.oldPool = oldPool;
        this.newPool = newPool;
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.checkType(type);
        int fp = this.newPool.allocateFingerprint(elemName.getURI(), elemName.getLocalPart());
        CodedName newElemName = new CodedName(fp, elemName.getPrefix(), this.newPool);
        AttributeMap newAtts = EmptyAttributeMap.getInstance();
        for (AttributeInfo att : attributes) {
            this.checkType(att.getType());
            int afp = this.newPool.allocateFingerprint(att.getNodeName().getURI(), att.getNodeName().getLocalPart());
            CodedName newAttName = new CodedName(afp, att.getNodeName().getPrefix(), this.newPool);
            newAtts = newAtts.put(new AttributeInfo(newAttName, att.getType(), att.getValue(), att.getLocation(), att.getProperties()));
        }
        this.nextReceiver.startElement(newElemName, type, newAtts, namespaces, location, properties);
    }

    private void checkType(SchemaType type) {
        if ((type.getFingerprint() & 0xFFC00) != 0) {
            throw new UnsupportedOperationException("Cannot convert a user-typed node to a different name pool");
        }
    }
}

