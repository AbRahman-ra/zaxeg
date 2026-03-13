/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.security.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
import org.apache.jcp.xml.dsig.internal.dom.DOMReference;
import org.apache.jcp.xml.dsig.internal.dom.DOMStructure;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.apache.jcp.xml.dsig.internal.dom.Utils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class DOMManifest
extends DOMStructure
implements Manifest {
    private final List<Reference> references;
    private final String id;

    public DOMManifest(List<? extends Reference> references, String id) {
        if (references == null) {
            throw new NullPointerException("references cannot be null");
        }
        this.references = Collections.unmodifiableList(new ArrayList<Reference>(references));
        if (this.references.isEmpty()) {
            throw new IllegalArgumentException("list of references must contain at least one entry");
        }
        int size = this.references.size();
        for (int i = 0; i < size; ++i) {
            if (this.references.get(i) instanceof Reference) continue;
            throw new ClassCastException("references[" + i + "] is not a valid type");
        }
        this.id = id;
    }

    public DOMManifest(Element manElem, XMLCryptoContext context, Provider provider) throws MarshalException {
        Attr attr = manElem.getAttributeNodeNS(null, "Id");
        if (attr != null) {
            this.id = attr.getValue();
            manElem.setIdAttributeNode(attr, true);
        } else {
            this.id = null;
        }
        boolean secVal = Utils.secureValidation(context);
        Element refElem = DOMUtils.getFirstChildElement(manElem, "Reference", "http://www.w3.org/2000/09/xmldsig#");
        ArrayList<DOMReference> refs = new ArrayList<DOMReference>();
        refs.add(new DOMReference(refElem, context, provider));
        refElem = DOMUtils.getNextSiblingElement(refElem);
        while (refElem != null) {
            String localName = refElem.getLocalName();
            String namespace = refElem.getNamespaceURI();
            if (!localName.equals("Reference") || !"http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
                throw new MarshalException("Invalid element name: " + namespace + ":" + localName + ", expected Reference");
            }
            refs.add(new DOMReference(refElem, context, provider));
            if (secVal && refs.size() > 30) {
                String error = "A maxiumum of 30 references per Manifest are allowed with secure validation";
                throw new MarshalException(error);
            }
            refElem = DOMUtils.getNextSiblingElement(refElem);
        }
        this.references = Collections.unmodifiableList(refs);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public List getReferences() {
        return this.references;
    }

    @Override
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element manElem = DOMUtils.createElement(ownerDoc, "Manifest", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
        DOMUtils.setAttributeID(manElem, "Id", this.id);
        for (Reference ref : this.references) {
            ((DOMReference)ref).marshal(manElem, dsPrefix, context);
        }
        parent.appendChild(manElem);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Manifest)) {
            return false;
        }
        Manifest oman = (Manifest)o;
        boolean idsEqual = this.id == null ? oman.getId() == null : this.id.equals(oman.getId());
        return idsEqual && ((Object)this.references).equals(oman.getReferences());
    }

    public int hashCode() {
        int result = 17;
        if (this.id != null) {
            result = 31 * result + this.id.hashCode();
        }
        result = 31 * result + ((Object)this.references).hashCode();
        return result;
    }
}

