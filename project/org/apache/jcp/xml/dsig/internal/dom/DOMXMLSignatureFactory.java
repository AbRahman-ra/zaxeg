/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignatureProperties;
import javax.xml.crypto.dsig.SignatureProperty;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.TransformService;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.XMLValidateContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.DigestMethodParameterSpec;
import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.apache.jcp.xml.dsig.internal.dom.DOMCanonicalizationMethod;
import org.apache.jcp.xml.dsig.internal.dom.DOMDigestMethod;
import org.apache.jcp.xml.dsig.internal.dom.DOMHMACSignatureMethod;
import org.apache.jcp.xml.dsig.internal.dom.DOMManifest;
import org.apache.jcp.xml.dsig.internal.dom.DOMReference;
import org.apache.jcp.xml.dsig.internal.dom.DOMSignatureMethod;
import org.apache.jcp.xml.dsig.internal.dom.DOMSignatureProperties;
import org.apache.jcp.xml.dsig.internal.dom.DOMSignatureProperty;
import org.apache.jcp.xml.dsig.internal.dom.DOMSignedInfo;
import org.apache.jcp.xml.dsig.internal.dom.DOMTransform;
import org.apache.jcp.xml.dsig.internal.dom.DOMURIDereferencer;
import org.apache.jcp.xml.dsig.internal.dom.DOMXMLObject;
import org.apache.jcp.xml.dsig.internal.dom.DOMXMLSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMXMLSignatureFactory
extends XMLSignatureFactory {
    public XMLSignature newXMLSignature(SignedInfo si, KeyInfo ki) {
        return new DOMXMLSignature(si, ki, null, null, null);
    }

    public XMLSignature newXMLSignature(SignedInfo si, KeyInfo ki, List objects, String id, String signatureValueId) {
        return new DOMXMLSignature(si, ki, objects, id, signatureValueId);
    }

    public Reference newReference(String uri, DigestMethod dm) {
        return this.newReference(uri, dm, null, null, null);
    }

    public Reference newReference(String uri, DigestMethod dm, List transforms, String type, String id) {
        return new DOMReference(uri, type, dm, transforms, id, this.getProvider());
    }

    public Reference newReference(String uri, DigestMethod dm, List appliedTransforms, Data result, List transforms, String type, String id) {
        if (appliedTransforms == null) {
            throw new NullPointerException("appliedTransforms cannot be null");
        }
        if (appliedTransforms.isEmpty()) {
            throw new NullPointerException("appliedTransforms cannot be empty");
        }
        if (result == null) {
            throw new NullPointerException("result cannot be null");
        }
        return new DOMReference(uri, type, dm, appliedTransforms, result, transforms, id, this.getProvider());
    }

    public Reference newReference(String uri, DigestMethod dm, List transforms, String type, String id, byte[] digestValue) {
        if (digestValue == null) {
            throw new NullPointerException("digestValue cannot be null");
        }
        return new DOMReference(uri, type, dm, null, null, transforms, id, digestValue, this.getProvider());
    }

    public SignedInfo newSignedInfo(CanonicalizationMethod cm, SignatureMethod sm, List references) {
        return this.newSignedInfo(cm, sm, references, null);
    }

    public SignedInfo newSignedInfo(CanonicalizationMethod cm, SignatureMethod sm, List references, String id) {
        return new DOMSignedInfo(cm, sm, references, id);
    }

    public XMLObject newXMLObject(List content, String id, String mimeType, String encoding) {
        return new DOMXMLObject(content, id, mimeType, encoding);
    }

    public Manifest newManifest(List references) {
        return this.newManifest(references, null);
    }

    public Manifest newManifest(List references, String id) {
        return new DOMManifest(references, id);
    }

    public SignatureProperties newSignatureProperties(List props, String id) {
        return new DOMSignatureProperties(props, id);
    }

    public SignatureProperty newSignatureProperty(List info, String target, String id) {
        return new DOMSignatureProperty(info, target, id);
    }

    public XMLSignature unmarshalXMLSignature(XMLValidateContext context) throws MarshalException {
        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }
        return this.unmarshal(((DOMValidateContext)context).getNode(), context);
    }

    public XMLSignature unmarshalXMLSignature(XMLStructure xmlStructure) throws MarshalException {
        if (xmlStructure == null) {
            throw new NullPointerException("xmlStructure cannot be null");
        }
        if (!(xmlStructure instanceof DOMStructure)) {
            throw new ClassCastException("xmlStructure must be of type DOMStructure");
        }
        return this.unmarshal(((DOMStructure)xmlStructure).getNode(), new UnmarshalContext());
    }

    private XMLSignature unmarshal(Node node, XMLCryptoContext context) throws MarshalException {
        node.normalize();
        Element element = null;
        if (node.getNodeType() == 9) {
            element = ((Document)node).getDocumentElement();
        } else if (node.getNodeType() == 1) {
            element = (Element)node;
        } else {
            throw new MarshalException("Signature element is not a proper Node");
        }
        String tag = element.getLocalName();
        String namespace = element.getNamespaceURI();
        if (tag == null || namespace == null) {
            throw new MarshalException("Document implementation must support DOM Level 2 and be namespace aware");
        }
        if (tag.equals("Signature") && "http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
            return new DOMXMLSignature(element, context, this.getProvider());
        }
        throw new MarshalException("invalid Signature tag: " + namespace + ":" + tag);
    }

    public boolean isFeatureSupported(String feature) {
        if (feature == null) {
            throw new NullPointerException();
        }
        return false;
    }

    public DigestMethod newDigestMethod(String algorithm, DigestMethodParameterSpec params) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        if (algorithm == null) {
            throw new NullPointerException();
        }
        if (algorithm.equals("http://www.w3.org/2000/09/xmldsig#sha1")) {
            return new DOMDigestMethod.SHA1(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#sha224")) {
            return new DOMDigestMethod.SHA224(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmlenc#sha256")) {
            return new DOMDigestMethod.SHA256(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#sha384")) {
            return new DOMDigestMethod.SHA384(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmlenc#sha512")) {
            return new DOMDigestMethod.SHA512(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmlenc#ripemd160")) {
            return new DOMDigestMethod.RIPEMD160(params);
        }
        throw new NoSuchAlgorithmException("unsupported algorithm");
    }

    public SignatureMethod newSignatureMethod(String algorithm, SignatureMethodParameterSpec params) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        if (algorithm == null) {
            throw new NullPointerException();
        }
        if (algorithm.equals("http://www.w3.org/2000/09/xmldsig#rsa-sha1")) {
            return new DOMSignatureMethod.SHA1withRSA(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha224")) {
            return new DOMSignatureMethod.SHA224withRSA(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256")) {
            return new DOMSignatureMethod.SHA256withRSA(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha384")) {
            return new DOMSignatureMethod.SHA384withRSA(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512")) {
            return new DOMSignatureMethod.SHA512withRSA(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512")) {
            return new DOMSignatureMethod.SHA512withRSA(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160")) {
            return new DOMSignatureMethod.RIPEMD160withRSA(params);
        }
        if (algorithm.equals("http://www.w3.org/2000/09/xmldsig#dsa-sha1")) {
            return new DOMSignatureMethod.SHA1withDSA(params);
        }
        if (algorithm.equals("http://www.w3.org/2009/xmldsig11#dsa-sha256")) {
            return new DOMSignatureMethod.SHA256withDSA(params);
        }
        if (algorithm.equals("http://www.w3.org/2000/09/xmldsig#hmac-sha1")) {
            return new DOMHMACSignatureMethod.SHA1(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha224")) {
            return new DOMHMACSignatureMethod.SHA224(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha256")) {
            return new DOMHMACSignatureMethod.SHA256(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha384")) {
            return new DOMHMACSignatureMethod.SHA384(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha512")) {
            return new DOMHMACSignatureMethod.SHA512(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-ripemd160")) {
            return new DOMHMACSignatureMethod.RIPEMD160(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1")) {
            return new DOMSignatureMethod.SHA1withECDSA(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha224")) {
            return new DOMSignatureMethod.SHA224withECDSA(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256")) {
            return new DOMSignatureMethod.SHA256withECDSA(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384")) {
            return new DOMSignatureMethod.SHA384withECDSA(params);
        }
        if (algorithm.equals("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512")) {
            return new DOMSignatureMethod.SHA512withECDSA(params);
        }
        throw new NoSuchAlgorithmException("unsupported algorithm");
    }

    public Transform newTransform(String algorithm, TransformParameterSpec params) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        TransformService spi;
        if (this.getProvider() == null) {
            spi = TransformService.getInstance(algorithm, "DOM");
        } else {
            try {
                spi = TransformService.getInstance(algorithm, "DOM", this.getProvider());
            } catch (NoSuchAlgorithmException nsae) {
                spi = TransformService.getInstance(algorithm, "DOM");
            }
        }
        spi.init(params);
        return new DOMTransform(spi);
    }

    public Transform newTransform(String algorithm, XMLStructure params) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        TransformService spi;
        if (this.getProvider() == null) {
            spi = TransformService.getInstance(algorithm, "DOM");
        } else {
            try {
                spi = TransformService.getInstance(algorithm, "DOM", this.getProvider());
            } catch (NoSuchAlgorithmException nsae) {
                spi = TransformService.getInstance(algorithm, "DOM");
            }
        }
        if (params == null) {
            spi.init(null);
        } else {
            spi.init(params, null);
        }
        return new DOMTransform(spi);
    }

    public CanonicalizationMethod newCanonicalizationMethod(String algorithm, C14NMethodParameterSpec params) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        TransformService spi;
        if (this.getProvider() == null) {
            spi = TransformService.getInstance(algorithm, "DOM");
        } else {
            try {
                spi = TransformService.getInstance(algorithm, "DOM", this.getProvider());
            } catch (NoSuchAlgorithmException nsae) {
                spi = TransformService.getInstance(algorithm, "DOM");
            }
        }
        spi.init(params);
        return new DOMCanonicalizationMethod(spi);
    }

    public CanonicalizationMethod newCanonicalizationMethod(String algorithm, XMLStructure params) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        TransformService spi;
        if (this.getProvider() == null) {
            spi = TransformService.getInstance(algorithm, "DOM");
        } else {
            try {
                spi = TransformService.getInstance(algorithm, "DOM", this.getProvider());
            } catch (NoSuchAlgorithmException nsae) {
                spi = TransformService.getInstance(algorithm, "DOM");
            }
        }
        if (params == null) {
            spi.init(null);
        } else {
            spi.init(params, null);
        }
        return new DOMCanonicalizationMethod(spi);
    }

    public URIDereferencer getURIDereferencer() {
        return DOMURIDereferencer.INSTANCE;
    }

    private static class UnmarshalContext
    extends DOMCryptoContext {
        UnmarshalContext() {
        }
    }
}

