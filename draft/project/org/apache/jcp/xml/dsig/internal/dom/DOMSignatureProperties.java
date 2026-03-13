/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.SignatureProperties;
import javax.xml.crypto.dsig.SignatureProperty;
import org.apache.jcp.xml.dsig.internal.dom.DOMSignatureProperty;
import org.apache.jcp.xml.dsig.internal.dom.DOMStructure;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class DOMSignatureProperties
extends DOMStructure
implements SignatureProperties {
    private final String id;
    private final List<SignatureProperty> properties;

    public DOMSignatureProperties(List<? extends SignatureProperty> properties, String id) {
        if (properties == null) {
            throw new NullPointerException("properties cannot be null");
        }
        if (properties.isEmpty()) {
            throw new IllegalArgumentException("properties cannot be empty");
        }
        this.properties = Collections.unmodifiableList(new ArrayList<SignatureProperty>(properties));
        int size = this.properties.size();
        for (int i = 0; i < size; ++i) {
            if (this.properties.get(i) instanceof SignatureProperty) continue;
            throw new ClassCastException("properties[" + i + "] is not a valid type");
        }
        this.id = id;
    }

    public DOMSignatureProperties(Element propsElem, XMLCryptoContext context) throws MarshalException {
        Attr attr = propsElem.getAttributeNodeNS(null, "Id");
        if (attr != null) {
            this.id = attr.getValue();
            propsElem.setIdAttributeNode(attr, true);
        } else {
            this.id = null;
        }
        ArrayList<DOMSignatureProperty> properties = new ArrayList<DOMSignatureProperty>();
        for (Node firstChild = propsElem.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            if (firstChild.getNodeType() != 1) continue;
            String name = firstChild.getLocalName();
            String namespace = firstChild.getNamespaceURI();
            if (!name.equals("SignatureProperty") || !"http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
                throw new MarshalException("Invalid element name: " + namespace + ":" + name + ", expected SignatureProperty");
            }
            properties.add(new DOMSignatureProperty((Element)firstChild, context));
        }
        if (properties.isEmpty()) {
            throw new MarshalException("properties cannot be empty");
        }
        this.properties = Collections.unmodifiableList(properties);
    }

    @Override
    public List getProperties() {
        return this.properties;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element propsElem = DOMUtils.createElement(ownerDoc, "SignatureProperties", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
        DOMUtils.setAttributeID(propsElem, "Id", this.id);
        for (SignatureProperty property : this.properties) {
            ((DOMSignatureProperty)property).marshal(propsElem, dsPrefix, context);
        }
        parent.appendChild(propsElem);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SignatureProperties)) {
            return false;
        }
        SignatureProperties osp = (SignatureProperties)o;
        boolean idsEqual = this.id == null ? osp.getId() == null : this.id.equals(osp.getId());
        return ((Object)this.properties).equals(osp.getProperties()) && idsEqual;
    }

    public int hashCode() {
        int result = 17;
        if (this.id != null) {
            result = 31 * result + this.id.hashCode();
        }
        result = 31 * result + ((Object)this.properties).hashCode();
        return result;
    }
}

