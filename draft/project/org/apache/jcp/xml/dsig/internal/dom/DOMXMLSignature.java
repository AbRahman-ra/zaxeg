/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLValidateContext;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcp.xml.dsig.internal.dom.AbstractDOMSignatureMethod;
import org.apache.jcp.xml.dsig.internal.dom.DOMKeyInfo;
import org.apache.jcp.xml.dsig.internal.dom.DOMReference;
import org.apache.jcp.xml.dsig.internal.dom.DOMSignedInfo;
import org.apache.jcp.xml.dsig.internal.dom.DOMStructure;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.apache.jcp.xml.dsig.internal.dom.DOMXMLObject;
import org.apache.jcp.xml.dsig.internal.dom.Utils;
import org.apache.xml.security.Init;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class DOMXMLSignature
extends DOMStructure
implements XMLSignature {
    private static Log log = LogFactory.getLog(DOMXMLSignature.class);
    private String id;
    private XMLSignature.SignatureValue sv;
    private KeyInfo ki;
    private List<XMLObject> objects;
    private SignedInfo si;
    private Document ownerDoc = null;
    private Element localSigElem = null;
    private Element sigElem = null;
    private boolean validationStatus;
    private boolean validated = false;
    private KeySelectorResult ksr;
    private HashMap<String, XMLStructure> signatureIdMap;

    public DOMXMLSignature(SignedInfo si, KeyInfo ki, List<? extends XMLObject> objs, String id, String signatureValueId) {
        if (si == null) {
            throw new NullPointerException("signedInfo cannot be null");
        }
        this.si = si;
        this.id = id;
        this.sv = new DOMSignatureValue(signatureValueId);
        if (objs == null) {
            this.objects = Collections.emptyList();
        } else {
            this.objects = Collections.unmodifiableList(new ArrayList<XMLObject>(objs));
            int size = this.objects.size();
            for (int i = 0; i < size; ++i) {
                if (this.objects.get(i) instanceof XMLObject) continue;
                throw new ClassCastException("objs[" + i + "] is not an XMLObject");
            }
        }
        this.ki = ki;
    }

    public DOMXMLSignature(Element sigElem, XMLCryptoContext context, Provider provider) throws MarshalException {
        this.localSigElem = sigElem;
        this.ownerDoc = this.localSigElem.getOwnerDocument();
        this.id = DOMUtils.getAttributeValue(this.localSigElem, "Id");
        Element siElem = DOMUtils.getFirstChildElement(this.localSigElem, "SignedInfo", "http://www.w3.org/2000/09/xmldsig#");
        this.si = new DOMSignedInfo(siElem, context, provider);
        Element sigValElem = DOMUtils.getNextSiblingElement(siElem, "SignatureValue", "http://www.w3.org/2000/09/xmldsig#");
        this.sv = new DOMSignatureValue(sigValElem, context);
        Element nextSibling = DOMUtils.getNextSiblingElement(sigValElem);
        if (nextSibling != null && nextSibling.getLocalName().equals("KeyInfo") && "http://www.w3.org/2000/09/xmldsig#".equals(nextSibling.getNamespaceURI())) {
            this.ki = new DOMKeyInfo(nextSibling, context, provider);
            nextSibling = DOMUtils.getNextSiblingElement(nextSibling);
        }
        if (nextSibling == null) {
            this.objects = Collections.emptyList();
        } else {
            ArrayList<DOMXMLObject> tempObjects = new ArrayList<DOMXMLObject>();
            while (nextSibling != null) {
                String name = nextSibling.getLocalName();
                String namespace = nextSibling.getNamespaceURI();
                if (!name.equals("Object") || !"http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
                    throw new MarshalException("Invalid element name: " + namespace + ":" + name + ", expected KeyInfo or Object");
                }
                tempObjects.add(new DOMXMLObject(nextSibling, context, provider));
                nextSibling = DOMUtils.getNextSiblingElement(nextSibling);
            }
            this.objects = Collections.unmodifiableList(tempObjects);
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public KeyInfo getKeyInfo() {
        return this.ki;
    }

    @Override
    public SignedInfo getSignedInfo() {
        return this.si;
    }

    @Override
    public List getObjects() {
        return this.objects;
    }

    @Override
    public XMLSignature.SignatureValue getSignatureValue() {
        return this.sv;
    }

    @Override
    public KeySelectorResult getKeySelectorResult() {
        return this.ksr;
    }

    @Override
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
        this.marshal(parent, null, dsPrefix, context);
    }

    public void marshal(Node parent, Node nextSibling, String dsPrefix, DOMCryptoContext context) throws MarshalException {
        this.ownerDoc = DOMUtils.getOwnerDocument(parent);
        this.sigElem = DOMUtils.createElement(this.ownerDoc, "Signature", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
        if (dsPrefix == null || dsPrefix.length() == 0) {
            this.sigElem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://www.w3.org/2000/09/xmldsig#");
        } else {
            this.sigElem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + dsPrefix, "http://www.w3.org/2000/09/xmldsig#");
        }
        ((DOMSignedInfo)this.si).marshal(this.sigElem, dsPrefix, context);
        ((DOMSignatureValue)this.sv).marshal(this.sigElem, dsPrefix, context);
        if (this.ki != null) {
            ((DOMKeyInfo)this.ki).marshal(this.sigElem, null, dsPrefix, context);
        }
        int size = this.objects.size();
        for (int i = 0; i < size; ++i) {
            ((DOMXMLObject)this.objects.get(i)).marshal(this.sigElem, dsPrefix, context);
        }
        DOMUtils.setAttributeID(this.sigElem, "Id", this.id);
        parent.insertBefore(this.sigElem, nextSibling);
    }

    @Override
    public boolean validate(XMLValidateContext vc) throws XMLSignatureException {
        boolean refValid;
        if (vc == null) {
            throw new NullPointerException("validateContext is null");
        }
        if (!(vc instanceof DOMValidateContext)) {
            throw new ClassCastException("validateContext must be of type DOMValidateContext");
        }
        if (this.validated) {
            return this.validationStatus;
        }
        boolean sigValidity = this.sv.validate(vc);
        if (!sigValidity) {
            this.validationStatus = false;
            this.validated = true;
            return this.validationStatus;
        }
        List refs = this.si.getReferences();
        boolean validateRefs = true;
        int size = refs.size();
        for (int i = 0; validateRefs && i < size; validateRefs &= refValid, ++i) {
            Reference ref = (Reference)refs.get(i);
            refValid = ref.validate(vc);
            if (!log.isDebugEnabled()) continue;
            log.debug("Reference[" + ref.getURI() + "] is valid: " + refValid);
        }
        if (!validateRefs) {
            if (log.isDebugEnabled()) {
                log.debug("Couldn't validate the References");
            }
            this.validationStatus = false;
            this.validated = true;
            return this.validationStatus;
        }
        boolean validateMans = true;
        if (Boolean.TRUE.equals(vc.getProperty("org.jcp.xml.dsig.validateManifests"))) {
            int size2 = this.objects.size();
            for (int i = 0; validateMans && i < size2; ++i) {
                XMLObject xo = this.objects.get(i);
                List content = xo.getContent();
                int csize = content.size();
                for (int j = 0; validateMans && j < csize; ++j) {
                    boolean refValid2;
                    XMLStructure xs = (XMLStructure)content.get(j);
                    if (!(xs instanceof Manifest)) continue;
                    if (log.isDebugEnabled()) {
                        log.debug("validating manifest");
                    }
                    Manifest man = (Manifest)xs;
                    List manRefs = man.getReferences();
                    int rsize = manRefs.size();
                    for (int k = 0; validateMans && k < rsize; validateMans &= refValid2, ++k) {
                        Reference ref = (Reference)manRefs.get(k);
                        refValid2 = ref.validate(vc);
                        if (!log.isDebugEnabled()) continue;
                        log.debug("Manifest ref[" + ref.getURI() + "] is valid: " + refValid2);
                    }
                }
            }
        }
        this.validationStatus = validateMans;
        this.validated = true;
        return this.validationStatus;
    }

    @Override
    public void sign(XMLSignContext signContext) throws MarshalException, XMLSignatureException {
        if (signContext == null) {
            throw new NullPointerException("signContext cannot be null");
        }
        DOMSignContext context = (DOMSignContext)signContext;
        this.marshal(context.getParent(), context.getNextSibling(), DOMUtils.getSignaturePrefix(context), context);
        ArrayList<Reference> allReferences = new ArrayList<Reference>();
        this.signatureIdMap = new HashMap();
        this.signatureIdMap.put(this.id, this);
        this.signatureIdMap.put(this.si.getId(), this.si);
        List refs = this.si.getReferences();
        for (Reference ref : refs) {
            this.signatureIdMap.put(ref.getId(), ref);
        }
        for (XMLObject obj : this.objects) {
            this.signatureIdMap.put(obj.getId(), obj);
            List content = obj.getContent();
            for (XMLStructure xs : content) {
                if (!(xs instanceof Manifest)) continue;
                Manifest man = (Manifest)xs;
                this.signatureIdMap.put(man.getId(), man);
                List manRefs = man.getReferences();
                for (Reference ref : manRefs) {
                    allReferences.add(ref);
                    this.signatureIdMap.put(ref.getId(), ref);
                }
            }
        }
        allReferences.addAll(refs);
        for (Reference ref : allReferences) {
            this.digestReference((DOMReference)ref, signContext);
        }
        for (Reference ref : allReferences) {
            if (((DOMReference)ref).isDigested()) continue;
            ((DOMReference)ref).digest(signContext);
        }
        Key signingKey = null;
        KeySelectorResult ksr = null;
        try {
            ksr = signContext.getKeySelector().select(this.ki, KeySelector.Purpose.SIGN, this.si.getSignatureMethod(), signContext);
            signingKey = ksr.getKey();
            if (signingKey == null) {
                throw new XMLSignatureException("the keySelector did not find a signing key");
            }
        } catch (KeySelectorException kse) {
            throw new XMLSignatureException("cannot find signing key", kse);
        }
        try {
            byte[] val = ((AbstractDOMSignatureMethod)this.si.getSignatureMethod()).sign(signingKey, this.si, signContext);
            ((DOMSignatureValue)this.sv).setValue(val);
        } catch (InvalidKeyException ike) {
            throw new XMLSignatureException(ike);
        }
        this.localSigElem = this.sigElem;
        this.ksr = ksr;
    }

    public boolean equals(Object o) {
        boolean idEqual;
        if (this == o) {
            return true;
        }
        if (!(o instanceof XMLSignature)) {
            return false;
        }
        XMLSignature osig = (XMLSignature)o;
        boolean bl = this.id == null ? osig.getId() == null : (idEqual = this.id.equals(osig.getId()));
        boolean keyInfoEqual = this.ki == null ? osig.getKeyInfo() == null : this.ki.equals(osig.getKeyInfo());
        return idEqual && keyInfoEqual && this.sv.equals(osig.getSignatureValue()) && this.si.equals(osig.getSignedInfo()) && ((Object)this.objects).equals(osig.getObjects());
    }

    public int hashCode() {
        int result = 17;
        if (this.id != null) {
            result = 31 * result + this.id.hashCode();
        }
        if (this.ki != null) {
            result = 31 * result + this.ki.hashCode();
        }
        result = 31 * result + this.sv.hashCode();
        result = 31 * result + this.si.hashCode();
        result = 31 * result + ((Object)this.objects).hashCode();
        return result;
    }

    private void digestReference(DOMReference ref, XMLSignContext signContext) throws XMLSignatureException {
        if (ref.isDigested()) {
            return;
        }
        String uri = ref.getURI();
        if (Utils.sameDocumentURI(uri)) {
            String id = Utils.parseIdFromSameDocumentURI(uri);
            if (id != null && this.signatureIdMap.containsKey(id)) {
                XMLStructure xs = this.signatureIdMap.get(id);
                if (xs instanceof DOMReference) {
                    this.digestReference((DOMReference)xs, signContext);
                } else if (xs instanceof Manifest) {
                    Manifest man = (Manifest)xs;
                    List manRefs = man.getReferences();
                    int size = manRefs.size();
                    for (int i = 0; i < size; ++i) {
                        this.digestReference((DOMReference)manRefs.get(i), signContext);
                    }
                }
            }
            if (uri.length() == 0) {
                List transforms = ref.getTransforms();
                for (Transform transform : transforms) {
                    String transformAlg = transform.getAlgorithm();
                    if (!transformAlg.equals("http://www.w3.org/TR/1999/REC-xpath-19991116") && !transformAlg.equals("http://www.w3.org/2002/06/xmldsig-filter2")) continue;
                    return;
                }
            }
        }
        ref.digest(signContext);
    }

    static {
        Init.init();
    }

    public class DOMSignatureValue
    extends DOMStructure
    implements XMLSignature.SignatureValue {
        private String id;
        private byte[] value;
        private String valueBase64;
        private Element sigValueElem;
        private boolean validated = false;
        private boolean validationStatus;

        DOMSignatureValue(String id) {
            this.id = id;
        }

        DOMSignatureValue(Element sigValueElem, XMLCryptoContext context) throws MarshalException {
            try {
                this.value = Base64.decode(sigValueElem);
            } catch (Base64DecodingException bde) {
                throw new MarshalException(bde);
            }
            Attr attr = sigValueElem.getAttributeNodeNS(null, "Id");
            if (attr != null) {
                this.id = attr.getValue();
                sigValueElem.setIdAttributeNode(attr, true);
            } else {
                this.id = null;
            }
            this.sigValueElem = sigValueElem;
        }

        public String getId() {
            return this.id;
        }

        public byte[] getValue() {
            return this.value == null ? null : (byte[])this.value.clone();
        }

        public boolean validate(XMLValidateContext validateContext) throws XMLSignatureException {
            if (validateContext == null) {
                throw new NullPointerException("context cannot be null");
            }
            if (this.validated) {
                return this.validationStatus;
            }
            SignatureMethod sm = DOMXMLSignature.this.si.getSignatureMethod();
            Key validationKey = null;
            KeySelectorResult ksResult = null;
            try {
                KeySelector keySelector = validateContext.getKeySelector();
                if (keySelector != null && (ksResult = keySelector.select(DOMXMLSignature.this.ki, KeySelector.Purpose.VERIFY, sm, validateContext)) != null) {
                    validationKey = ksResult.getKey();
                }
                if (validationKey == null) {
                    throw new XMLSignatureException("the keyselector did not find a validation key");
                }
            } catch (KeySelectorException kse) {
                throw new XMLSignatureException("cannot find validation key", kse);
            }
            try {
                this.validationStatus = ((AbstractDOMSignatureMethod)sm).verify(validationKey, DOMXMLSignature.this.si, this.value, validateContext);
            } catch (Exception e) {
                throw new XMLSignatureException(e);
            }
            this.validated = true;
            DOMXMLSignature.this.ksr = ksResult;
            return this.validationStatus;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof XMLSignature.SignatureValue)) {
                return false;
            }
            XMLSignature.SignatureValue osv = (XMLSignature.SignatureValue)o;
            boolean idEqual = this.id == null ? osv.getId() == null : this.id.equals(osv.getId());
            return idEqual;
        }

        public int hashCode() {
            int result = 17;
            if (this.id != null) {
                result = 31 * result + this.id.hashCode();
            }
            return result;
        }

        public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
            this.sigValueElem = DOMUtils.createElement(DOMXMLSignature.this.ownerDoc, "SignatureValue", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
            if (this.valueBase64 != null) {
                this.sigValueElem.appendChild(DOMXMLSignature.this.ownerDoc.createTextNode(this.valueBase64));
            }
            DOMUtils.setAttributeID(this.sigValueElem, "Id", this.id);
            parent.appendChild(this.sigValueElem);
        }

        void setValue(byte[] value) {
            this.value = value;
            this.valueBase64 = Base64.encode(value);
            this.sigValueElem.appendChild(DOMXMLSignature.this.ownerDoc.createTextNode(this.valueBase64));
        }
    }
}

