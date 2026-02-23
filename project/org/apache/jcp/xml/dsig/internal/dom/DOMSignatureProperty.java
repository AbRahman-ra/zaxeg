/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.SignatureProperty;
import org.apache.jcp.xml.dsig.internal.dom.DOMStructure;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class DOMSignatureProperty
extends DOMStructure
implements SignatureProperty {
    private final String id;
    private final String target;
    private final List<XMLStructure> content;

    public DOMSignatureProperty(List<? extends XMLStructure> content, String target, String id) {
        if (target == null) {
            throw new NullPointerException("target cannot be null");
        }
        if (content == null) {
            throw new NullPointerException("content cannot be null");
        }
        if (content.isEmpty()) {
            throw new IllegalArgumentException("content cannot be empty");
        }
        this.content = Collections.unmodifiableList(new ArrayList<XMLStructure>(content));
        int size = this.content.size();
        for (int i = 0; i < size; ++i) {
            if (this.content.get(i) instanceof XMLStructure) continue;
            throw new ClassCastException("content[" + i + "] is not a valid type");
        }
        this.target = target;
        this.id = id;
    }

    public DOMSignatureProperty(Element propElem, XMLCryptoContext context) throws MarshalException {
        this.target = DOMUtils.getAttributeValue(propElem, "Target");
        if (this.target == null) {
            throw new MarshalException("target cannot be null");
        }
        Attr attr = propElem.getAttributeNodeNS(null, "Id");
        if (attr != null) {
            this.id = attr.getValue();
            propElem.setIdAttributeNode(attr, true);
        } else {
            this.id = null;
        }
        ArrayList<javax.xml.crypto.dom.DOMStructure> content = new ArrayList<javax.xml.crypto.dom.DOMStructure>();
        for (Node firstChild = propElem.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            content.add(new javax.xml.crypto.dom.DOMStructure(firstChild));
        }
        if (content.isEmpty()) {
            throw new MarshalException("content cannot be empty");
        }
        this.content = Collections.unmodifiableList(content);
    }

    @Override
    public List getContent() {
        return this.content;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getTarget() {
        return this.target;
    }

    @Override
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element propElem = DOMUtils.createElement(ownerDoc, "SignatureProperty", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
        DOMUtils.setAttributeID(propElem, "Id", this.id);
        DOMUtils.setAttribute(propElem, "Target", this.target);
        for (XMLStructure property : this.content) {
            DOMUtils.appendChild(propElem, ((javax.xml.crypto.dom.DOMStructure)property).getNode());
        }
        parent.appendChild(propElem);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SignatureProperty)) {
            return false;
        }
        SignatureProperty osp = (SignatureProperty)o;
        boolean idsEqual = this.id == null ? osp.getId() == null : this.id.equals(osp.getId());
        List ospContent = osp.getContent();
        return this.equalsContent(ospContent) && this.target.equals(osp.getTarget()) && idsEqual;
    }

    public int hashCode() {
        int result = 17;
        if (this.id != null) {
            result = 31 * result + this.id.hashCode();
        }
        result = 31 * result + this.target.hashCode();
        result = 31 * result + ((Object)this.content).hashCode();
        return result;
    }

    private boolean equalsContent(List<XMLStructure> otherContent) {
        int osize = otherContent.size();
        if (this.content.size() != osize) {
            return false;
        }
        for (int i = 0; i < osize; ++i) {
            XMLStructure oxs = otherContent.get(i);
            XMLStructure xs = this.content.get(i);
            if (oxs instanceof javax.xml.crypto.dom.DOMStructure) {
                if (!(xs instanceof javax.xml.crypto.dom.DOMStructure)) {
                    return false;
                }
                Node onode = ((javax.xml.crypto.dom.DOMStructure)oxs).getNode();
                Node node = ((javax.xml.crypto.dom.DOMStructure)xs).getNode();
                if (DOMUtils.nodesEqual(node, onode)) continue;
                return false;
            }
            if (xs.equals(oxs)) continue;
            return false;
        }
        return true;
    }
}

