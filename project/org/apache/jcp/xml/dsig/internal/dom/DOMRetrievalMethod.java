/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMURIReference;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;
import javax.xml.parsers.DocumentBuilder;
import org.apache.jcp.xml.dsig.internal.dom.ApacheData;
import org.apache.jcp.xml.dsig.internal.dom.DOMStructure;
import org.apache.jcp.xml.dsig.internal.dom.DOMTransform;
import org.apache.jcp.xml.dsig.internal.dom.DOMURIDereferencer;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.apache.jcp.xml.dsig.internal.dom.DOMX509Data;
import org.apache.jcp.xml.dsig.internal.dom.Utils;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class DOMRetrievalMethod
extends DOMStructure
implements RetrievalMethod,
DOMURIReference {
    private final List<Transform> transforms;
    private String uri;
    private String type;
    private Attr here;

    public DOMRetrievalMethod(String uri, String type, List<? extends Transform> transforms) {
        if (uri == null) {
            throw new NullPointerException("uri cannot be null");
        }
        if (transforms == null || transforms.isEmpty()) {
            this.transforms = Collections.emptyList();
        } else {
            this.transforms = Collections.unmodifiableList(new ArrayList<Transform>(transforms));
            int size = this.transforms.size();
            for (int i = 0; i < size; ++i) {
                if (this.transforms.get(i) instanceof Transform) continue;
                throw new ClassCastException("transforms[" + i + "] is not a valid type");
            }
        }
        this.uri = uri;
        if (!uri.equals("")) {
            try {
                new URI(uri);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        this.type = type;
    }

    public DOMRetrievalMethod(Element rmElem, XMLCryptoContext context, Provider provider) throws MarshalException {
        this.uri = DOMUtils.getAttributeValue(rmElem, "URI");
        this.type = DOMUtils.getAttributeValue(rmElem, "Type");
        this.here = rmElem.getAttributeNodeNS(null, "URI");
        boolean secVal = Utils.secureValidation(context);
        ArrayList<DOMTransform> transforms = new ArrayList<DOMTransform>();
        Element transformsElem = DOMUtils.getFirstChildElement(rmElem);
        if (transformsElem != null) {
            String localName = transformsElem.getLocalName();
            String namespace = transformsElem.getNamespaceURI();
            if (!localName.equals("Transforms") || !"http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
                throw new MarshalException("Invalid element name: " + namespace + ":" + localName + ", expected Transforms");
            }
            Element transformElem = DOMUtils.getFirstChildElement(transformsElem, "Transform", "http://www.w3.org/2000/09/xmldsig#");
            while (transformElem != null) {
                String name = transformElem.getLocalName();
                namespace = transformElem.getNamespaceURI();
                if (!name.equals("Transform") || !"http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
                    throw new MarshalException("Invalid element name: " + name + ", expected Transform");
                }
                transforms.add(new DOMTransform(transformElem, context, provider));
                if (secVal && transforms.size() > 5) {
                    String error = "A maxiumum of 5 transforms per Reference are allowed with secure validation";
                    throw new MarshalException(error);
                }
                transformElem = DOMUtils.getNextSiblingElement(transformElem);
            }
        }
        this.transforms = transforms.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(transforms);
    }

    @Override
    public String getURI() {
        return this.uri;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public List getTransforms() {
        return this.transforms;
    }

    @Override
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element rmElem = DOMUtils.createElement(ownerDoc, "RetrievalMethod", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
        DOMUtils.setAttribute(rmElem, "URI", this.uri);
        DOMUtils.setAttribute(rmElem, "Type", this.type);
        if (!this.transforms.isEmpty()) {
            Element transformsElem = DOMUtils.createElement(ownerDoc, "Transforms", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
            rmElem.appendChild(transformsElem);
            for (Transform transform : this.transforms) {
                ((DOMTransform)transform).marshal(transformsElem, dsPrefix, context);
            }
        }
        parent.appendChild(rmElem);
        this.here = rmElem.getAttributeNodeNS(null, "URI");
    }

    @Override
    public Node getHere() {
        return this.here;
    }

    @Override
    public Data dereference(XMLCryptoContext context) throws URIReferenceException {
        Node root;
        NodeSetData nsd;
        Iterator i;
        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }
        URIDereferencer deref = context.getURIDereferencer();
        if (deref == null) {
            deref = DOMURIDereferencer.INSTANCE;
        }
        Data data = deref.dereference(this, context);
        try {
            for (Transform transform : this.transforms) {
                data = ((DOMTransform)transform).transform(data, context);
            }
        } catch (Exception e) {
            throw new URIReferenceException(e);
        }
        if (data instanceof NodeSetData && Utils.secureValidation(context) && (i = (nsd = (NodeSetData)data).iterator()).hasNext() && "RetrievalMethod".equals((root = (Node)i.next()).getLocalName())) {
            throw new URIReferenceException("It is forbidden to have one RetrievalMethod point to another when secure validation is enabled");
        }
        return data;
    }

    public XMLStructure dereferenceAsXMLStructure(XMLCryptoContext context) throws URIReferenceException {
        try {
            ApacheData data = (ApacheData)this.dereference(context);
            boolean secVal = Utils.secureValidation(context);
            DocumentBuilder db = XMLUtils.createDocumentBuilder(false, secVal);
            Document doc = db.parse(new ByteArrayInputStream(data.getXMLSignatureInput().getBytes()));
            Element kiElem = doc.getDocumentElement();
            if (kiElem.getLocalName().equals("X509Data") && "http://www.w3.org/2000/09/xmldsig#".equals(kiElem.getNamespaceURI())) {
                return new DOMX509Data(kiElem);
            }
            return null;
        } catch (Exception e) {
            throw new URIReferenceException(e);
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RetrievalMethod)) {
            return false;
        }
        RetrievalMethod orm = (RetrievalMethod)obj;
        boolean typesEqual = this.type == null ? orm.getType() == null : this.type.equals(orm.getType());
        return this.uri.equals(orm.getURI()) && ((Object)this.transforms).equals(orm.getTransforms()) && typesEqual;
    }

    public int hashCode() {
        int result = 17;
        if (this.type != null) {
            result = 31 * result + this.type.hashCode();
        }
        result = 31 * result + this.uri.hashCode();
        result = 31 * result + ((Object)this.transforms).hashCode();
        return result;
    }
}

