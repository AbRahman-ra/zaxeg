/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.transforms;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.InvalidTransformException;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Transforms
extends SignatureElementProxy {
    public static final String TRANSFORM_C14N_OMIT_COMMENTS = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
    public static final String TRANSFORM_C14N_WITH_COMMENTS = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments";
    public static final String TRANSFORM_C14N11_OMIT_COMMENTS = "http://www.w3.org/2006/12/xml-c14n11";
    public static final String TRANSFORM_C14N11_WITH_COMMENTS = "http://www.w3.org/2006/12/xml-c14n11#WithComments";
    public static final String TRANSFORM_C14N_EXCL_OMIT_COMMENTS = "http://www.w3.org/2001/10/xml-exc-c14n#";
    public static final String TRANSFORM_C14N_EXCL_WITH_COMMENTS = "http://www.w3.org/2001/10/xml-exc-c14n#WithComments";
    public static final String TRANSFORM_XSLT = "http://www.w3.org/TR/1999/REC-xslt-19991116";
    public static final String TRANSFORM_BASE64_DECODE = "http://www.w3.org/2000/09/xmldsig#base64";
    public static final String TRANSFORM_XPATH = "http://www.w3.org/TR/1999/REC-xpath-19991116";
    public static final String TRANSFORM_ENVELOPED_SIGNATURE = "http://www.w3.org/2000/09/xmldsig#enveloped-signature";
    public static final String TRANSFORM_XPOINTER = "http://www.w3.org/TR/2001/WD-xptr-20010108";
    public static final String TRANSFORM_XPATH2FILTER = "http://www.w3.org/2002/06/xmldsig-filter2";
    private static Log log = LogFactory.getLog(Transforms.class);
    private Element[] transforms;
    private boolean secureValidation;

    protected Transforms() {
    }

    public Transforms(Document doc) {
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public Transforms(Element element, String BaseURI) throws DOMException, XMLSignatureException, InvalidTransformException, TransformationException, XMLSecurityException {
        super(element, BaseURI);
        int numberOfTransformElems = this.getLength();
        if (numberOfTransformElems == 0) {
            Object[] exArgs = new Object[]{"Transform", "Transforms"};
            throw new TransformationException("xml.WrongContent", exArgs);
        }
    }

    public void setSecureValidation(boolean secureValidation) {
        this.secureValidation = secureValidation;
    }

    public void addTransform(String transformURI) throws TransformationException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Transforms.addTransform(" + transformURI + ")");
            }
            Transform transform = new Transform(this.doc, transformURI);
            this.addTransform(transform);
        } catch (InvalidTransformException ex) {
            throw new TransformationException("empty", ex);
        }
    }

    public void addTransform(String transformURI, Element contextElement) throws TransformationException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Transforms.addTransform(" + transformURI + ")");
            }
            Transform transform = new Transform(this.doc, transformURI, contextElement);
            this.addTransform(transform);
        } catch (InvalidTransformException ex) {
            throw new TransformationException("empty", ex);
        }
    }

    public void addTransform(String transformURI, NodeList contextNodes) throws TransformationException {
        try {
            Transform transform = new Transform(this.doc, transformURI, contextNodes);
            this.addTransform(transform);
        } catch (InvalidTransformException ex) {
            throw new TransformationException("empty", ex);
        }
    }

    private void addTransform(Transform transform) {
        if (log.isDebugEnabled()) {
            log.debug("Transforms.addTransform(" + transform.getURI() + ")");
        }
        Element transformElement = transform.getElement();
        this.constructionElement.appendChild(transformElement);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public XMLSignatureInput performTransforms(XMLSignatureInput xmlSignatureInput) throws TransformationException {
        return this.performTransforms(xmlSignatureInput, null);
    }

    public XMLSignatureInput performTransforms(XMLSignatureInput xmlSignatureInput, OutputStream os) throws TransformationException {
        try {
            int last = this.getLength() - 1;
            for (int i = 0; i < last; ++i) {
                Transform t = this.item(i);
                String uri = t.getURI();
                if (log.isDebugEnabled()) {
                    log.debug("Perform the (" + i + ")th " + uri + " transform");
                }
                this.checkSecureValidation(t);
                xmlSignatureInput = t.performTransform(xmlSignatureInput);
            }
            if (last >= 0) {
                Transform t = this.item(last);
                this.checkSecureValidation(t);
                xmlSignatureInput = t.performTransform(xmlSignatureInput, os);
            }
            return xmlSignatureInput;
        } catch (IOException ex) {
            throw new TransformationException("empty", ex);
        } catch (CanonicalizationException ex) {
            throw new TransformationException("empty", ex);
        } catch (InvalidCanonicalizerException ex) {
            throw new TransformationException("empty", ex);
        }
    }

    private void checkSecureValidation(Transform transform) throws TransformationException {
        String uri = transform.getURI();
        if (this.secureValidation && TRANSFORM_XSLT.equals(uri)) {
            Object[] exArgs = new Object[]{uri};
            throw new TransformationException("signature.Transform.ForbiddenTransform", exArgs);
        }
        transform.setSecureValidation(this.secureValidation);
    }

    public int getLength() {
        if (this.transforms == null) {
            this.transforms = XMLUtils.selectDsNodes(this.constructionElement.getFirstChild(), "Transform");
        }
        return this.transforms.length;
    }

    public Transform item(int i) throws TransformationException {
        try {
            if (this.transforms == null) {
                this.transforms = XMLUtils.selectDsNodes(this.constructionElement.getFirstChild(), "Transform");
            }
            return new Transform(this.transforms[i], this.baseURI);
        } catch (XMLSecurityException ex) {
            throw new TransformationException("empty", ex);
        }
    }

    public String getBaseLocalName() {
        return "Transforms";
    }
}

