/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.XMLSignatureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcp.xml.dsig.internal.dom.DOMCanonicalizationMethod;
import org.apache.jcp.xml.dsig.internal.dom.DOMReference;
import org.apache.jcp.xml.dsig.internal.dom.DOMSignatureMethod;
import org.apache.jcp.xml.dsig.internal.dom.DOMStructure;
import org.apache.jcp.xml.dsig.internal.dom.DOMSubTreeData;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.apache.jcp.xml.dsig.internal.dom.Utils;
import org.apache.xml.security.utils.Base64;
import org.apache.xml.security.utils.UnsyncBufferedOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class DOMSignedInfo
extends DOMStructure
implements SignedInfo {
    public static final int MAXIMUM_REFERENCE_COUNT = 30;
    private static Log log = LogFactory.getLog(DOMSignedInfo.class);
    private static final String ALGO_ID_SIGNATURE_NOT_RECOMMENDED_RSA_MD5 = "http://www.w3.org/2001/04/xmldsig-more#rsa-md5";
    private static final String ALGO_ID_MAC_HMAC_NOT_RECOMMENDED_MD5 = "http://www.w3.org/2001/04/xmldsig-more#hmac-md5";
    private List<Reference> references;
    private CanonicalizationMethod canonicalizationMethod;
    private SignatureMethod signatureMethod;
    private String id;
    private Document ownerDoc;
    private Element localSiElem;
    private InputStream canonData;

    public DOMSignedInfo(CanonicalizationMethod cm, SignatureMethod sm, List<? extends Reference> references) {
        if (cm == null || sm == null || references == null) {
            throw new NullPointerException();
        }
        this.canonicalizationMethod = cm;
        this.signatureMethod = sm;
        this.references = Collections.unmodifiableList(new ArrayList<Reference>(references));
        if (this.references.isEmpty()) {
            throw new IllegalArgumentException("list of references must contain at least one entry");
        }
        int size = this.references.size();
        for (int i = 0; i < size; ++i) {
            Reference obj = this.references.get(i);
            if (obj instanceof Reference) continue;
            throw new ClassCastException("list of references contains an illegal type");
        }
    }

    public DOMSignedInfo(CanonicalizationMethod cm, SignatureMethod sm, List<? extends Reference> references, String id) {
        this(cm, sm, references);
        this.id = id;
    }

    public DOMSignedInfo(Element siElem, XMLCryptoContext context, Provider provider) throws MarshalException {
        this.localSiElem = siElem;
        this.ownerDoc = siElem.getOwnerDocument();
        this.id = DOMUtils.getAttributeValue(siElem, "Id");
        Element cmElem = DOMUtils.getFirstChildElement(siElem, "CanonicalizationMethod", "http://www.w3.org/2000/09/xmldsig#");
        this.canonicalizationMethod = new DOMCanonicalizationMethod(cmElem, context, provider);
        Element smElem = DOMUtils.getNextSiblingElement(cmElem, "SignatureMethod", "http://www.w3.org/2000/09/xmldsig#");
        this.signatureMethod = DOMSignatureMethod.unmarshal(smElem);
        boolean secVal = Utils.secureValidation(context);
        String signatureMethodAlgorithm = this.signatureMethod.getAlgorithm();
        if (secVal && (ALGO_ID_MAC_HMAC_NOT_RECOMMENDED_MD5.equals(signatureMethodAlgorithm) || ALGO_ID_SIGNATURE_NOT_RECOMMENDED_RSA_MD5.equals(signatureMethodAlgorithm))) {
            throw new MarshalException("It is forbidden to use algorithm " + this.signatureMethod + " when secure validation is enabled");
        }
        ArrayList<DOMReference> refList = new ArrayList<DOMReference>(5);
        Element refElem = DOMUtils.getNextSiblingElement(smElem, "Reference", "http://www.w3.org/2000/09/xmldsig#");
        refList.add(new DOMReference(refElem, context, provider));
        refElem = DOMUtils.getNextSiblingElement(refElem);
        while (refElem != null) {
            String name = refElem.getLocalName();
            String namespace = refElem.getNamespaceURI();
            if (!name.equals("Reference") || !"http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
                throw new MarshalException("Invalid element name: " + namespace + ":" + name + ", expected Reference");
            }
            refList.add(new DOMReference(refElem, context, provider));
            if (secVal && refList.size() > 30) {
                String error = "A maxiumum of 30 references per Manifest are allowed with secure validation";
                throw new MarshalException(error);
            }
            refElem = DOMUtils.getNextSiblingElement(refElem);
        }
        this.references = Collections.unmodifiableList(refList);
    }

    @Override
    public CanonicalizationMethod getCanonicalizationMethod() {
        return this.canonicalizationMethod;
    }

    @Override
    public SignatureMethod getSignatureMethod() {
        return this.signatureMethod;
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
    public InputStream getCanonicalizedData() {
        return this.canonData;
    }

    public void canonicalize(XMLCryptoContext context, ByteArrayOutputStream bos) throws XMLSignatureException {
        block10: {
            UnsyncBufferedOutputStream os;
            block9: {
                if (context == null) {
                    throw new NullPointerException("context cannot be null");
                }
                os = new UnsyncBufferedOutputStream(bos);
                DOMSubTreeData subTree = new DOMSubTreeData(this.localSiElem, true);
                try {
                    ((DOMCanonicalizationMethod)this.canonicalizationMethod).canonicalize(subTree, context, os);
                } catch (TransformException te) {
                    throw new XMLSignatureException(te);
                }
                try {
                    ((OutputStream)os).flush();
                } catch (IOException e) {
                    if (!log.isDebugEnabled()) break block9;
                    log.debug(e);
                }
            }
            byte[] signedInfoBytes = bos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("Canonicalized SignedInfo:");
                StringBuilder sb = new StringBuilder(signedInfoBytes.length);
                for (int i = 0; i < signedInfoBytes.length; ++i) {
                    sb.append((char)signedInfoBytes[i]);
                }
                log.debug(sb.toString());
                log.debug("Data to be signed/verified:" + Base64.encode(signedInfoBytes));
            }
            this.canonData = new ByteArrayInputStream(signedInfoBytes);
            try {
                ((OutputStream)os).close();
            } catch (IOException e) {
                if (!log.isDebugEnabled()) break block10;
                log.debug(e.getMessage(), e);
            }
        }
    }

    @Override
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
        this.ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element siElem = DOMUtils.createElement(this.ownerDoc, "SignedInfo", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
        DOMCanonicalizationMethod dcm = (DOMCanonicalizationMethod)this.canonicalizationMethod;
        dcm.marshal(siElem, dsPrefix, context);
        ((DOMStructure)((Object)this.signatureMethod)).marshal(siElem, dsPrefix, context);
        for (Reference reference : this.references) {
            ((DOMReference)reference).marshal(siElem, dsPrefix, context);
        }
        DOMUtils.setAttributeID(siElem, "Id", this.id);
        parent.appendChild(siElem);
        this.localSiElem = siElem;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SignedInfo)) {
            return false;
        }
        SignedInfo osi = (SignedInfo)o;
        boolean idEqual = this.id == null ? osi.getId() == null : this.id.equals(osi.getId());
        return this.canonicalizationMethod.equals(osi.getCanonicalizationMethod()) && this.signatureMethod.equals(osi.getSignatureMethod()) && ((Object)this.references).equals(osi.getReferences()) && idEqual;
    }

    public int hashCode() {
        int result = 17;
        if (this.id != null) {
            result = 31 * result + this.id.hashCode();
        }
        result = 31 * result + this.canonicalizationMethod.hashCode();
        result = 31 * result + this.signatureMethod.hashCode();
        result = 31 * result + ((Object)this.references).hashCode();
        return result;
    }
}

