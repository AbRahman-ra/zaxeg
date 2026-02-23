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
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import org.apache.jcp.xml.dsig.internal.dom.DOMKeyName;
import org.apache.jcp.xml.dsig.internal.dom.DOMKeyValue;
import org.apache.jcp.xml.dsig.internal.dom.DOMPGPData;
import org.apache.jcp.xml.dsig.internal.dom.DOMRetrievalMethod;
import org.apache.jcp.xml.dsig.internal.dom.DOMStructure;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.apache.jcp.xml.dsig.internal.dom.DOMX509Data;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class DOMKeyInfo
extends DOMStructure
implements KeyInfo {
    private final String id;
    private final List<XMLStructure> keyInfoTypes;

    public DOMKeyInfo(List<? extends XMLStructure> content, String id) {
        if (content == null) {
            throw new NullPointerException("content cannot be null");
        }
        this.keyInfoTypes = Collections.unmodifiableList(new ArrayList<XMLStructure>(content));
        if (this.keyInfoTypes.isEmpty()) {
            throw new IllegalArgumentException("content cannot be empty");
        }
        int size = this.keyInfoTypes.size();
        for (int i = 0; i < size; ++i) {
            if (this.keyInfoTypes.get(i) instanceof XMLStructure) continue;
            throw new ClassCastException("content[" + i + "] is not a valid KeyInfo type");
        }
        this.id = id;
    }

    public DOMKeyInfo(Element kiElem, XMLCryptoContext context, Provider provider) throws MarshalException {
        Node firstChild;
        Attr attr = kiElem.getAttributeNodeNS(null, "Id");
        if (attr != null) {
            this.id = attr.getValue();
            kiElem.setIdAttributeNode(attr, true);
        } else {
            this.id = null;
        }
        ArrayList<XMLStructure> content = new ArrayList<XMLStructure>();
        if (firstChild == null) {
            throw new MarshalException("KeyInfo must contain at least one type");
        }
        for (firstChild = kiElem.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            if (firstChild.getNodeType() != 1) continue;
            Element childElem = (Element)firstChild;
            String localName = childElem.getLocalName();
            String namespace = childElem.getNamespaceURI();
            if (localName.equals("X509Data") && "http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
                content.add(new DOMX509Data(childElem));
                continue;
            }
            if (localName.equals("KeyName") && "http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
                content.add(new DOMKeyName(childElem));
                continue;
            }
            if (localName.equals("KeyValue") && "http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
                content.add(DOMKeyValue.unmarshal(childElem));
                continue;
            }
            if (localName.equals("RetrievalMethod") && "http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
                content.add(new DOMRetrievalMethod(childElem, context, provider));
                continue;
            }
            if (localName.equals("PGPData") && "http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
                content.add(new DOMPGPData(childElem));
                continue;
            }
            content.add(new javax.xml.crypto.dom.DOMStructure(childElem));
        }
        this.keyInfoTypes = Collections.unmodifiableList(content);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public List getContent() {
        return this.keyInfoTypes;
    }

    @Override
    public void marshal(XMLStructure parent, XMLCryptoContext context) throws MarshalException {
        if (parent == null) {
            throw new NullPointerException("parent is null");
        }
        if (!(parent instanceof javax.xml.crypto.dom.DOMStructure)) {
            throw new ClassCastException("parent must be of type DOMStructure");
        }
        Node pNode = ((javax.xml.crypto.dom.DOMStructure)parent).getNode();
        String dsPrefix = DOMUtils.getSignaturePrefix(context);
        Element kiElem = DOMUtils.createElement(DOMUtils.getOwnerDocument(pNode), "KeyInfo", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
        if (dsPrefix == null || dsPrefix.length() == 0) {
            kiElem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://www.w3.org/2000/09/xmldsig#");
        } else {
            kiElem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + dsPrefix, "http://www.w3.org/2000/09/xmldsig#");
        }
        this.marshal(pNode, kiElem, null, dsPrefix, (DOMCryptoContext)context);
    }

    @Override
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
        this.marshal(parent, null, dsPrefix, context);
    }

    public void marshal(Node parent, Node nextSibling, String dsPrefix, DOMCryptoContext context) throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element kiElem = DOMUtils.createElement(ownerDoc, "KeyInfo", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
        this.marshal(parent, kiElem, nextSibling, dsPrefix, context);
    }

    private void marshal(Node parent, Element kiElem, Node nextSibling, String dsPrefix, DOMCryptoContext context) throws MarshalException {
        for (XMLStructure kiType : this.keyInfoTypes) {
            if (kiType instanceof DOMStructure) {
                ((DOMStructure)kiType).marshal(kiElem, dsPrefix, context);
                continue;
            }
            DOMUtils.appendChild(kiElem, ((javax.xml.crypto.dom.DOMStructure)kiType).getNode());
        }
        DOMUtils.setAttributeID(kiElem, "Id", this.id);
        parent.insertBefore(kiElem, nextSibling);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KeyInfo)) {
            return false;
        }
        KeyInfo oki = (KeyInfo)o;
        boolean idsEqual = this.id == null ? oki.getId() == null : this.id.equals(oki.getId());
        return ((Object)this.keyInfoTypes).equals(oki.getContent()) && idsEqual;
    }

    public int hashCode() {
        int result = 17;
        if (this.id != null) {
            result = 31 * result + this.id.hashCode();
        }
        result = 31 * result + ((Object)this.keyInfoTypes).hashCode();
        return result;
    }
}

