/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import net.sf.saxon.dom.NodeOverNodeInfo;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

class DOMImplementationImpl
implements DOMImplementation {
    DOMImplementationImpl() {
    }

    @Override
    public boolean hasFeature(String feature, String version) {
        return !(!feature.equalsIgnoreCase("XML") && !feature.equalsIgnoreCase("Core") || version != null && !version.isEmpty() && !version.equals("3.0") && !version.equals("2.0") && !version.equals("1.0"));
    }

    @Override
    public Object getFeature(String feature, String version) {
        return null;
    }

    @Override
    public DocumentType createDocumentType(String qualifiedName, String publicId, String systemId) throws DOMException {
        NodeOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public Document createDocument(String namespaceURI, String qualifiedName, DocumentType doctype) throws UnsupportedOperationException {
        NodeOverNodeInfo.disallowUpdate();
        return null;
    }
}

