/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMURIReference;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.TransformService;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLValidateContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcp.xml.dsig.internal.DigesterOutputStream;
import org.apache.jcp.xml.dsig.internal.dom.ApacheData;
import org.apache.jcp.xml.dsig.internal.dom.DOMDigestMethod;
import org.apache.jcp.xml.dsig.internal.dom.DOMStructure;
import org.apache.jcp.xml.dsig.internal.dom.DOMSubTreeData;
import org.apache.jcp.xml.dsig.internal.dom.DOMTransform;
import org.apache.jcp.xml.dsig.internal.dom.DOMURIDereferencer;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.apache.jcp.xml.dsig.internal.dom.Utils;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.Base64;
import org.apache.xml.security.utils.UnsyncBufferedOutputStream;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class DOMReference
extends DOMStructure
implements Reference,
DOMURIReference {
    public static final int MAXIMUM_TRANSFORM_COUNT = 5;
    private static boolean useC14N11 = AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

        @Override
        public Boolean run() {
            return Boolean.getBoolean("com.sun.org.apache.xml.internal.security.useC14N11");
        }
    });
    private static Log log = LogFactory.getLog(DOMReference.class);
    private final DigestMethod digestMethod;
    private final String id;
    private final List<Transform> transforms;
    private List<Transform> allTransforms;
    private final Data appliedTransformData;
    private Attr here;
    private final String uri;
    private final String type;
    private byte[] digestValue;
    private byte[] calcDigestValue;
    private Element refElem;
    private boolean digested = false;
    private boolean validated = false;
    private boolean validationStatus;
    private Data derefData;
    private InputStream dis;
    private MessageDigest md;
    private Provider provider;

    public DOMReference(String uri, String type, DigestMethod dm, List<? extends Transform> transforms, String id, Provider provider) {
        this(uri, type, dm, null, null, transforms, id, null, provider);
    }

    public DOMReference(String uri, String type, DigestMethod dm, List<? extends Transform> appliedTransforms, Data result, List<? extends Transform> transforms, String id, Provider provider) {
        this(uri, type, dm, appliedTransforms, result, transforms, id, null, provider);
    }

    public DOMReference(String uri, String type, DigestMethod dm, List<? extends Transform> appliedTransforms, Data result, List<? extends Transform> transforms, String id, byte[] digestValue, Provider provider) {
        int i;
        int size;
        if (dm == null) {
            throw new NullPointerException("DigestMethod must be non-null");
        }
        if (appliedTransforms == null) {
            this.allTransforms = new ArrayList<Transform>();
        } else {
            this.allTransforms = new ArrayList<Transform>(appliedTransforms);
            size = this.allTransforms.size();
            for (i = 0; i < size; ++i) {
                if (this.allTransforms.get(i) instanceof Transform) continue;
                throw new ClassCastException("appliedTransforms[" + i + "] is not a valid type");
            }
        }
        if (transforms == null) {
            this.transforms = Collections.emptyList();
        } else {
            this.transforms = new ArrayList<Transform>(transforms);
            size = this.transforms.size();
            for (i = 0; i < size; ++i) {
                if (this.transforms.get(i) instanceof Transform) continue;
                throw new ClassCastException("transforms[" + i + "] is not a valid type");
            }
            this.allTransforms.addAll(this.transforms);
        }
        this.digestMethod = dm;
        this.uri = uri;
        if (uri != null && !uri.equals("")) {
            try {
                new URI(uri);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        this.type = type;
        this.id = id;
        if (digestValue != null) {
            this.digestValue = (byte[])digestValue.clone();
            this.digested = true;
        }
        this.appliedTransformData = result;
        this.provider = provider;
    }

    public DOMReference(Element refElem, XMLCryptoContext context, Provider provider) throws MarshalException {
        boolean secVal = Utils.secureValidation(context);
        Element nextSibling = DOMUtils.getFirstChildElement(refElem);
        ArrayList<Transform> transforms = new ArrayList<Transform>(5);
        if (nextSibling.getLocalName().equals("Transforms") && "http://www.w3.org/2000/09/xmldsig#".equals(nextSibling.getNamespaceURI())) {
            Element transformElem = DOMUtils.getFirstChildElement(nextSibling, "Transform", "http://www.w3.org/2000/09/xmldsig#");
            transforms.add(new DOMTransform(transformElem, context, provider));
            transformElem = DOMUtils.getNextSiblingElement(transformElem);
            while (transformElem != null) {
                String localName = transformElem.getLocalName();
                String namespace = transformElem.getNamespaceURI();
                if (!localName.equals("Transform") || !"http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
                    throw new MarshalException("Invalid element name: " + localName + ", expected Transform");
                }
                transforms.add(new DOMTransform(transformElem, context, provider));
                if (secVal && transforms.size() > 5) {
                    String error = "A maxiumum of 5 transforms per Reference are allowed with secure validation";
                    throw new MarshalException(error);
                }
                transformElem = DOMUtils.getNextSiblingElement(transformElem);
            }
            nextSibling = DOMUtils.getNextSiblingElement(nextSibling);
        }
        if (!nextSibling.getLocalName().equals("DigestMethod") && "http://www.w3.org/2000/09/xmldsig#".equals(nextSibling.getNamespaceURI())) {
            throw new MarshalException("Invalid element name: " + nextSibling.getLocalName() + ", expected DigestMethod");
        }
        Element dmElem = nextSibling;
        this.digestMethod = DOMDigestMethod.unmarshal(dmElem);
        String digestMethodAlgorithm = this.digestMethod.getAlgorithm();
        if (secVal && "http://www.w3.org/2001/04/xmldsig-more#md5".equals(digestMethodAlgorithm)) {
            throw new MarshalException("It is forbidden to use algorithm " + this.digestMethod + " when secure validation is enabled");
        }
        Element dvElem = DOMUtils.getNextSiblingElement(dmElem, "DigestValue", "http://www.w3.org/2000/09/xmldsig#");
        try {
            this.digestValue = Base64.decode(dvElem);
        } catch (Base64DecodingException bde) {
            throw new MarshalException(bde);
        }
        if (DOMUtils.getNextSiblingElement(dvElem) != null) {
            throw new MarshalException("Unexpected element after DigestValue element");
        }
        this.uri = DOMUtils.getAttributeValue(refElem, "URI");
        Attr attr = refElem.getAttributeNodeNS(null, "Id");
        if (attr != null) {
            this.id = attr.getValue();
            refElem.setIdAttributeNode(attr, true);
        } else {
            this.id = null;
        }
        this.type = DOMUtils.getAttributeValue(refElem, "Type");
        this.here = refElem.getAttributeNodeNS(null, "URI");
        this.refElem = refElem;
        this.transforms = transforms;
        this.allTransforms = transforms;
        this.appliedTransformData = null;
        this.provider = provider;
    }

    @Override
    public DigestMethod getDigestMethod() {
        return this.digestMethod;
    }

    @Override
    public String getId() {
        return this.id;
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
        return Collections.unmodifiableList(this.allTransforms);
    }

    @Override
    public byte[] getDigestValue() {
        return this.digestValue == null ? null : (byte[])this.digestValue.clone();
    }

    @Override
    public byte[] getCalculatedDigestValue() {
        return this.calcDigestValue == null ? null : (byte[])this.calcDigestValue.clone();
    }

    @Override
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
        if (log.isDebugEnabled()) {
            log.debug("Marshalling Reference");
        }
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        this.refElem = DOMUtils.createElement(ownerDoc, "Reference", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
        DOMUtils.setAttributeID(this.refElem, "Id", this.id);
        DOMUtils.setAttribute(this.refElem, "URI", this.uri);
        DOMUtils.setAttribute(this.refElem, "Type", this.type);
        if (!this.allTransforms.isEmpty()) {
            Element transformsElem = DOMUtils.createElement(ownerDoc, "Transforms", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
            this.refElem.appendChild(transformsElem);
            for (Transform transform : this.allTransforms) {
                ((DOMStructure)((Object)transform)).marshal(transformsElem, dsPrefix, context);
            }
        }
        ((DOMDigestMethod)this.digestMethod).marshal(this.refElem, dsPrefix, context);
        if (log.isDebugEnabled()) {
            log.debug("Adding digestValueElem");
        }
        Element digestValueElem = DOMUtils.createElement(ownerDoc, "DigestValue", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
        if (this.digestValue != null) {
            digestValueElem.appendChild(ownerDoc.createTextNode(Base64.encode(this.digestValue)));
        }
        this.refElem.appendChild(digestValueElem);
        parent.appendChild(this.refElem);
        this.here = this.refElem.getAttributeNodeNS(null, "URI");
    }

    public void digest(XMLSignContext signContext) throws XMLSignatureException {
        Element digestElem;
        Data data = null;
        data = this.appliedTransformData == null ? this.dereference(signContext) : this.appliedTransformData;
        this.digestValue = this.transform(data, signContext);
        String encodedDV = Base64.encode(this.digestValue);
        if (log.isDebugEnabled()) {
            log.debug("Reference object uri = " + this.uri);
        }
        if ((digestElem = DOMUtils.getLastChildElement(this.refElem)) == null) {
            throw new XMLSignatureException("DigestValue element expected");
        }
        DOMUtils.removeAllChildren(digestElem);
        digestElem.appendChild(this.refElem.getOwnerDocument().createTextNode(encodedDV));
        this.digested = true;
        if (log.isDebugEnabled()) {
            log.debug("Reference digesting completed");
        }
    }

    @Override
    public boolean validate(XMLValidateContext validateContext) throws XMLSignatureException {
        if (validateContext == null) {
            throw new NullPointerException("validateContext cannot be null");
        }
        if (this.validated) {
            return this.validationStatus;
        }
        Data data = this.dereference(validateContext);
        this.calcDigestValue = this.transform(data, validateContext);
        if (log.isDebugEnabled()) {
            log.debug("Expected digest: " + Base64.encode(this.digestValue));
            log.debug("Actual digest: " + Base64.encode(this.calcDigestValue));
        }
        this.validationStatus = Arrays.equals(this.digestValue, this.calcDigestValue);
        this.validated = true;
        return this.validationStatus;
    }

    @Override
    public Data getDereferencedData() {
        return this.derefData;
    }

    @Override
    public InputStream getDigestInputStream() {
        return this.dis;
    }

    private Data dereference(XMLCryptoContext context) throws XMLSignatureException {
        Data data = null;
        URIDereferencer deref = context.getURIDereferencer();
        if (deref == null) {
            deref = DOMURIDereferencer.INSTANCE;
        }
        try {
            data = deref.dereference(this, context);
            if (log.isDebugEnabled()) {
                log.debug("URIDereferencer class name: " + deref.getClass().getName());
                log.debug("Data class name: " + data.getClass().getName());
            }
        } catch (URIReferenceException ure) {
            throw new XMLSignatureException(ure);
        }
        return data;
    }

    private byte[] transform(Data dereferencedData, XMLCryptoContext context) throws XMLSignatureException {
        DigesterOutputStream dos;
        if (this.md == null) {
            try {
                this.md = MessageDigest.getInstance(((DOMDigestMethod)this.digestMethod).getMessageDigestAlgorithm());
            } catch (NoSuchAlgorithmException nsae) {
                throw new XMLSignatureException(nsae);
            }
        }
        this.md.reset();
        Boolean cache = (Boolean)context.getProperty("javax.xml.crypto.dsig.cacheReference");
        if (cache != null && cache.booleanValue()) {
            this.derefData = DOMReference.copyDerefData(dereferencedData);
            dos = new DigesterOutputStream(this.md, true);
        } else {
            dos = new DigesterOutputStream(this.md);
        }
        UnsyncBufferedOutputStream os = null;
        Data data = dereferencedData;
        try {
            os = new UnsyncBufferedOutputStream(dos);
            int size = this.transforms.size();
            for (int i = 0; i < size; ++i) {
                DOMTransform transform = (DOMTransform)this.transforms.get(i);
                data = i < size - 1 ? transform.transform(data, context) : transform.transform(data, context, os);
            }
            if (data != null) {
                XMLSignatureInput xi;
                boolean c14n11 = useC14N11;
                String c14nalg = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
                if (context instanceof XMLSignContext) {
                    if (!c14n11) {
                        Boolean prop = (Boolean)context.getProperty("org.apache.xml.security.useC14N11");
                        boolean bl = c14n11 = prop != null && prop != false;
                        if (c14n11) {
                            c14nalg = "http://www.w3.org/2006/12/xml-c14n11";
                        }
                    } else {
                        c14nalg = "http://www.w3.org/2006/12/xml-c14n11";
                    }
                }
                if (data instanceof ApacheData) {
                    xi = ((ApacheData)data).getXMLSignatureInput();
                } else if (data instanceof OctetStreamData) {
                    xi = new XMLSignatureInput(((OctetStreamData)data).getOctetStream());
                } else if (data instanceof NodeSetData) {
                    TransformService spi = null;
                    if (this.provider == null) {
                        spi = TransformService.getInstance(c14nalg, "DOM");
                    } else {
                        try {
                            spi = TransformService.getInstance(c14nalg, "DOM", this.provider);
                        } catch (NoSuchAlgorithmException nsae) {
                            spi = TransformService.getInstance(c14nalg, "DOM");
                        }
                    }
                    data = spi.transform(data, context);
                    xi = new XMLSignatureInput(((OctetStreamData)data).getOctetStream());
                } else {
                    throw new XMLSignatureException("unrecognized Data type");
                }
                boolean secVal = Utils.secureValidation(context);
                xi.setSecureValidation(secVal);
                if (context instanceof XMLSignContext && c14n11 && !xi.isOctetStream() && !xi.isOutputStreamSet()) {
                    TransformService spi = null;
                    if (this.provider == null) {
                        spi = TransformService.getInstance(c14nalg, "DOM");
                    } else {
                        try {
                            spi = TransformService.getInstance(c14nalg, "DOM", this.provider);
                        } catch (NoSuchAlgorithmException nsae) {
                            spi = TransformService.getInstance(c14nalg, "DOM");
                        }
                    }
                    DOMTransform t = new DOMTransform(spi);
                    Element transformsElem = null;
                    String dsPrefix = DOMUtils.getSignaturePrefix(context);
                    if (this.allTransforms.isEmpty()) {
                        transformsElem = DOMUtils.createElement(this.refElem.getOwnerDocument(), "Transforms", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
                        this.refElem.insertBefore(transformsElem, DOMUtils.getFirstChildElement(this.refElem));
                    } else {
                        transformsElem = DOMUtils.getFirstChildElement(this.refElem);
                    }
                    t.marshal(transformsElem, dsPrefix, (DOMCryptoContext)context);
                    this.allTransforms.add(t);
                    xi.updateOutputStream(os, true);
                } else {
                    xi.updateOutputStream(os);
                }
            }
            ((OutputStream)os).flush();
            if (cache != null && cache.booleanValue()) {
                this.dis = dos.getInputStream();
            }
            byte[] xi = dos.getDigestValue();
            return xi;
        } catch (NoSuchAlgorithmException e) {
            throw new XMLSignatureException(e);
        } catch (TransformException e) {
            throw new XMLSignatureException(e);
        } catch (MarshalException e) {
            throw new XMLSignatureException(e);
        } catch (IOException e) {
            throw new XMLSignatureException(e);
        } catch (CanonicalizationException e) {
            throw new XMLSignatureException(e);
        } finally {
            if (os != null) {
                try {
                    ((OutputStream)os).close();
                } catch (IOException e) {
                    throw new XMLSignatureException(e);
                }
            }
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    throw new XMLSignatureException(e);
                }
            }
        }
    }

    @Override
    public Node getHere() {
        return this.here;
    }

    public boolean equals(Object o) {
        boolean urisEqual;
        boolean idsEqual;
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reference)) {
            return false;
        }
        Reference oref = (Reference)o;
        boolean bl = this.id == null ? oref.getId() == null : (idsEqual = this.id.equals(oref.getId()));
        boolean bl2 = this.uri == null ? oref.getURI() == null : (urisEqual = this.uri.equals(oref.getURI()));
        boolean typesEqual = this.type == null ? oref.getType() == null : this.type.equals(oref.getType());
        boolean digestValuesEqual = Arrays.equals(this.digestValue, oref.getDigestValue());
        return this.digestMethod.equals(oref.getDigestMethod()) && idsEqual && urisEqual && typesEqual && ((Object)this.allTransforms).equals(oref.getTransforms()) && digestValuesEqual;
    }

    public int hashCode() {
        int result = 17;
        if (this.id != null) {
            result = 31 * result + this.id.hashCode();
        }
        if (this.uri != null) {
            result = 31 * result + this.uri.hashCode();
        }
        if (this.type != null) {
            result = 31 * result + this.type.hashCode();
        }
        if (this.digestValue != null) {
            result = 31 * result + Arrays.hashCode(this.digestValue);
        }
        result = 31 * result + this.digestMethod.hashCode();
        result = 31 * result + ((Object)this.allTransforms).hashCode();
        return result;
    }

    boolean isDigested() {
        return this.digested;
    }

    private static Data copyDerefData(Data dereferencedData) {
        if (dereferencedData instanceof ApacheData) {
            ApacheData ad = (ApacheData)dereferencedData;
            XMLSignatureInput xsi = ad.getXMLSignatureInput();
            if (xsi.isNodeSet()) {
                try {
                    final Set<Node> s = xsi.getNodeSet();
                    return new NodeSetData(){

                        public Iterator iterator() {
                            return s.iterator();
                        }
                    };
                } catch (Exception e) {
                    log.warn("cannot cache dereferenced data: " + e);
                    return null;
                }
            }
            if (xsi.isElement()) {
                return new DOMSubTreeData(xsi.getSubNode(), xsi.isExcludeComments());
            }
            if (xsi.isOctetStream() || xsi.isByteArray()) {
                try {
                    return new OctetStreamData(xsi.getOctetStream(), xsi.getSourceURI(), xsi.getMIMEType());
                } catch (IOException ioe) {
                    log.warn("cannot cache dereferenced data: " + ioe);
                    return null;
                }
            }
        }
        return dereferencedData;
    }
}

