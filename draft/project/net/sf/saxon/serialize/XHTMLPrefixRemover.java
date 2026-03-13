/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class XHTMLPrefixRemover
extends ProxyReceiver {
    public XHTMLPrefixRemover(Receiver next) {
        super(next);
    }

    private boolean isSpecial(String uri) {
        return uri.equals("http://www.w3.org/1999/xhtml") || uri.equals("http://www.w3.org/2000/svg") || uri.equals("http://www.w3.org/1998/Math/MathML");
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        for (NamespaceBinding ns : namespaces) {
            if (!this.isSpecial(ns.getURI())) continue;
            namespaces = namespaces.remove(ns.getPrefix());
        }
        if (this.isSpecial(elemName.getURI())) {
            String uri = elemName.getURI();
            if (!elemName.getPrefix().isEmpty()) {
                elemName = new FingerprintedQName("", uri, elemName.getLocalPart());
            }
            namespaces = namespaces.put("", uri);
        }
        for (AttributeInfo att : attributes) {
            if (!this.isSpecial(att.getNodeName().getURI())) continue;
            namespaces = namespaces.put(att.getNodeName().getPrefix(), att.getNodeName().getURI());
        }
        this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
    }
}

