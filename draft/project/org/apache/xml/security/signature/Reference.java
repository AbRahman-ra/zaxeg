/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.signature;

import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.algorithms.Algorithm;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.Manifest;
import org.apache.xml.security.signature.ReferenceNotInitializedException;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.signature.reference.ReferenceData;
import org.apache.xml.security.signature.reference.ReferenceNodeSetData;
import org.apache.xml.security.signature.reference.ReferenceOctetStreamData;
import org.apache.xml.security.signature.reference.ReferenceSubTreeData;
import org.apache.xml.security.transforms.InvalidTransformException;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.transforms.params.InclusiveNamespaces;
import org.apache.xml.security.utils.Base64;
import org.apache.xml.security.utils.DigesterOutputStream;
import org.apache.xml.security.utils.ElementProxy;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.apache.xml.security.utils.UnsyncBufferedOutputStream;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class Reference
extends SignatureElementProxy {
    public static final String OBJECT_URI = "http://www.w3.org/2000/09/xmldsig#Object";
    public static final String MANIFEST_URI = "http://www.w3.org/2000/09/xmldsig#Manifest";
    public static final int MAXIMUM_TRANSFORM_COUNT = 5;
    private boolean secureValidation;
    private static boolean useC14N11 = AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

        @Override
        public Boolean run() {
            return Boolean.getBoolean("org.apache.xml.security.useC14N11");
        }
    });
    private static final Log log = LogFactory.getLog(Reference.class);
    private Manifest manifest;
    private XMLSignatureInput transformsOutput;
    private Transforms transforms;
    private Element digestMethodElem;
    private Element digestValueElement;
    private ReferenceData referenceData;

    protected Reference(Document doc, String baseURI, String referenceURI, Manifest manifest, Transforms transforms, String messageDigestAlgorithm) throws XMLSignatureException {
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        this.baseURI = baseURI;
        this.manifest = manifest;
        this.setURI(referenceURI);
        if (transforms != null) {
            this.transforms = transforms;
            this.constructionElement.appendChild(transforms.getElement());
            XMLUtils.addReturnToElement(this.constructionElement);
        }
        Algorithm digestAlgorithm = new Algorithm(this.getDocument(), messageDigestAlgorithm){

            public String getBaseNamespace() {
                return "http://www.w3.org/2000/09/xmldsig#";
            }

            public String getBaseLocalName() {
                return "DigestMethod";
            }
        };
        this.digestMethodElem = digestAlgorithm.getElement();
        this.constructionElement.appendChild(this.digestMethodElem);
        XMLUtils.addReturnToElement(this.constructionElement);
        this.digestValueElement = XMLUtils.createElementInSignatureSpace(this.doc, "DigestValue");
        this.constructionElement.appendChild(this.digestValueElement);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    protected Reference(Element element, String baseURI, Manifest manifest) throws XMLSecurityException {
        this(element, baseURI, manifest, false);
    }

    protected Reference(Element element, String baseURI, Manifest manifest, boolean secureValidation) throws XMLSecurityException {
        super(element, baseURI);
        this.secureValidation = secureValidation;
        this.baseURI = baseURI;
        Element el = XMLUtils.getNextElement(element.getFirstChild());
        if ("Transforms".equals(el.getLocalName()) && "http://www.w3.org/2000/09/xmldsig#".equals(el.getNamespaceURI())) {
            this.transforms = new Transforms(el, this.baseURI);
            this.transforms.setSecureValidation(secureValidation);
            if (secureValidation && this.transforms.getLength() > 5) {
                Object[] exArgs = new Object[]{this.transforms.getLength(), 5};
                throw new XMLSecurityException("signature.tooManyTransforms", exArgs);
            }
            el = XMLUtils.getNextElement(el.getNextSibling());
        }
        this.digestMethodElem = el;
        this.digestValueElement = XMLUtils.getNextElement(this.digestMethodElem.getNextSibling());
        this.manifest = manifest;
    }

    public MessageDigestAlgorithm getMessageDigestAlgorithm() throws XMLSignatureException {
        if (this.digestMethodElem == null) {
            return null;
        }
        String uri = this.digestMethodElem.getAttributeNS(null, "Algorithm");
        if (uri == null) {
            return null;
        }
        if (this.secureValidation && "http://www.w3.org/2001/04/xmldsig-more#md5".equals(uri)) {
            Object[] exArgs = new Object[]{uri};
            throw new XMLSignatureException("signature.signatureAlgorithm", exArgs);
        }
        return MessageDigestAlgorithm.getInstance(this.doc, uri);
    }

    public void setURI(String uri) {
        if (uri != null) {
            this.constructionElement.setAttributeNS(null, "URI", uri);
        }
    }

    public String getURI() {
        return this.constructionElement.getAttributeNS(null, "URI");
    }

    public void setId(String id) {
        if (id != null) {
            this.constructionElement.setAttributeNS(null, "Id", id);
            this.constructionElement.setIdAttributeNS(null, "Id", true);
        }
    }

    public String getId() {
        return this.constructionElement.getAttributeNS(null, "Id");
    }

    public void setType(String type) {
        if (type != null) {
            this.constructionElement.setAttributeNS(null, "Type", type);
        }
    }

    public String getType() {
        return this.constructionElement.getAttributeNS(null, "Type");
    }

    public boolean typeIsReferenceToObject() {
        return OBJECT_URI.equals(this.getType());
    }

    public boolean typeIsReferenceToManifest() {
        return MANIFEST_URI.equals(this.getType());
    }

    private void setDigestValueElement(byte[] digestValue) {
        for (Node n = this.digestValueElement.getFirstChild(); n != null; n = n.getNextSibling()) {
            this.digestValueElement.removeChild(n);
        }
        String base64codedValue = Base64.encode(digestValue);
        Text t = this.doc.createTextNode(base64codedValue);
        this.digestValueElement.appendChild(t);
    }

    public void generateDigestValue() throws XMLSignatureException, ReferenceNotInitializedException {
        this.setDigestValueElement(this.calculateDigest(false));
    }

    public XMLSignatureInput getContentsBeforeTransformation() throws ReferenceNotInitializedException {
        try {
            Attr uriAttr = this.constructionElement.getAttributeNodeNS(null, "URI");
            ResourceResolver resolver = ResourceResolver.getInstance(uriAttr, this.baseURI, this.manifest.getPerManifestResolvers(), this.secureValidation);
            resolver.addProperties(this.manifest.getResolverProperties());
            return resolver.resolve(uriAttr, this.baseURI, this.secureValidation);
        } catch (ResourceResolverException ex) {
            throw new ReferenceNotInitializedException("empty", ex);
        }
    }

    private XMLSignatureInput getContentsAfterTransformation(XMLSignatureInput input, OutputStream os) throws XMLSignatureException {
        try {
            Transforms transforms = this.getTransforms();
            XMLSignatureInput output = null;
            if (transforms != null) {
                this.transformsOutput = output = transforms.performTransforms(input, os);
            } else {
                output = input;
            }
            return output;
        } catch (ResourceResolverException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (CanonicalizationException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (InvalidCanonicalizerException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (TransformationException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (XMLSecurityException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    public XMLSignatureInput getContentsAfterTransformation() throws XMLSignatureException {
        XMLSignatureInput input = this.getContentsBeforeTransformation();
        this.cacheDereferencedElement(input);
        return this.getContentsAfterTransformation(input, null);
    }

    public XMLSignatureInput getNodesetBeforeFirstCanonicalization() throws XMLSignatureException {
        try {
            XMLSignatureInput input = this.getContentsBeforeTransformation();
            this.cacheDereferencedElement(input);
            XMLSignatureInput output = input;
            Transforms transforms = this.getTransforms();
            if (transforms != null) {
                Transform t;
                String uri;
                for (int i = 0; !(i >= transforms.getLength() || (uri = (t = transforms.item(i)).getURI()).equals("http://www.w3.org/2001/10/xml-exc-c14n#") || uri.equals("http://www.w3.org/2001/10/xml-exc-c14n#WithComments") || uri.equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315") || uri.equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments")); ++i) {
                    output = t.performTransform(output, null);
                }
                output.setSourceURI(input.getSourceURI());
            }
            return output;
        } catch (IOException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (ResourceResolverException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (CanonicalizationException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (InvalidCanonicalizerException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (TransformationException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (XMLSecurityException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    public String getHTMLRepresentation() throws XMLSignatureException {
        try {
            XMLSignatureInput nodes = this.getNodesetBeforeFirstCanonicalization();
            Transforms transforms = this.getTransforms();
            ElementProxy c14nTransform = null;
            if (transforms != null) {
                for (int i = 0; i < transforms.getLength(); ++i) {
                    Transform t = transforms.item(i);
                    String uri = t.getURI();
                    if (!uri.equals("http://www.w3.org/2001/10/xml-exc-c14n#") && !uri.equals("http://www.w3.org/2001/10/xml-exc-c14n#WithComments")) continue;
                    c14nTransform = t;
                    break;
                }
            }
            Set<String> inclusiveNamespaces = new HashSet<String>();
            if (c14nTransform != null && c14nTransform.length("http://www.w3.org/2001/10/xml-exc-c14n#", "InclusiveNamespaces") == 1) {
                InclusiveNamespaces in = new InclusiveNamespaces(XMLUtils.selectNode(c14nTransform.getElement().getFirstChild(), "http://www.w3.org/2001/10/xml-exc-c14n#", "InclusiveNamespaces", 0), this.getBaseURI());
                inclusiveNamespaces = InclusiveNamespaces.prefixStr2Set(in.getInclusiveNamespaces());
            }
            return nodes.getHTMLRepresentation(inclusiveNamespaces);
        } catch (TransformationException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (InvalidTransformException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (XMLSecurityException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    public XMLSignatureInput getTransformsOutput() {
        return this.transformsOutput;
    }

    public ReferenceData getReferenceData() {
        return this.referenceData;
    }

    protected XMLSignatureInput dereferenceURIandPerformTransforms(OutputStream os) throws XMLSignatureException {
        try {
            XMLSignatureInput output;
            XMLSignatureInput input = this.getContentsBeforeTransformation();
            this.cacheDereferencedElement(input);
            this.transformsOutput = output = this.getContentsAfterTransformation(input, os);
            return output;
        } catch (XMLSecurityException ex) {
            throw new ReferenceNotInitializedException("empty", ex);
        }
    }

    private void cacheDereferencedElement(XMLSignatureInput input) {
        if (input.isNodeSet()) {
            try {
                final Set<Node> s = input.getNodeSet();
                this.referenceData = new ReferenceNodeSetData(){

                    @Override
                    public Iterator<Node> iterator() {
                        return new Iterator<Node>(){
                            Iterator<Node> sIterator;
                            {
                                this.sIterator = s.iterator();
                            }

                            @Override
                            public boolean hasNext() {
                                return this.sIterator.hasNext();
                            }

                            @Override
                            public Node next() {
                                return this.sIterator.next();
                            }

                            @Override
                            public void remove() {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                };
            } catch (Exception e) {
                log.warn("cannot cache dereferenced data: " + e);
            }
        } else if (input.isElement()) {
            this.referenceData = new ReferenceSubTreeData(input.getSubNode(), input.isExcludeComments());
        } else if (input.isOctetStream() || input.isByteArray()) {
            try {
                this.referenceData = new ReferenceOctetStreamData(input.getOctetStream(), input.getSourceURI(), input.getMIMEType());
            } catch (IOException ioe) {
                log.warn("cannot cache dereferenced data: " + ioe);
            }
        }
    }

    public Transforms getTransforms() throws XMLSignatureException, InvalidTransformException, TransformationException, XMLSecurityException {
        return this.transforms;
    }

    public byte[] getReferencedBytes() throws ReferenceNotInitializedException, XMLSignatureException {
        try {
            XMLSignatureInput output = this.dereferenceURIandPerformTransforms(null);
            return output.getBytes();
        } catch (IOException ex) {
            throw new ReferenceNotInitializedException("empty", ex);
        } catch (CanonicalizationException ex) {
            throw new ReferenceNotInitializedException("empty", ex);
        }
    }

    private byte[] calculateDigest(boolean validating) throws ReferenceNotInitializedException, XMLSignatureException {
        OutputStream os = null;
        try {
            MessageDigestAlgorithm mda = this.getMessageDigestAlgorithm();
            mda.reset();
            DigesterOutputStream diOs = new DigesterOutputStream(mda);
            os = new UnsyncBufferedOutputStream(diOs);
            XMLSignatureInput output = this.dereferenceURIandPerformTransforms(os);
            if (useC14N11 && !validating && !output.isOutputStreamSet() && !output.isOctetStream()) {
                if (this.transforms == null) {
                    this.transforms = new Transforms(this.doc);
                    this.transforms.setSecureValidation(this.secureValidation);
                    this.constructionElement.insertBefore(this.transforms.getElement(), this.digestMethodElem);
                }
                this.transforms.addTransform("http://www.w3.org/2006/12/xml-c14n11");
                output.updateOutputStream(os, true);
            } else {
                output.updateOutputStream(os);
            }
            os.flush();
            if (output.getOctetStreamReal() != null) {
                output.getOctetStreamReal().close();
            }
            byte[] byArray = diOs.getDigestValue();
            return byArray;
        } catch (XMLSecurityException ex) {
            throw new ReferenceNotInitializedException("empty", ex);
        } catch (IOException ex) {
            throw new ReferenceNotInitializedException("empty", ex);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                    throw new ReferenceNotInitializedException("empty", ex);
                }
            }
        }
    }

    public byte[] getDigestValue() throws Base64DecodingException, XMLSecurityException {
        if (this.digestValueElement == null) {
            Object[] exArgs = new Object[]{"DigestValue", "http://www.w3.org/2000/09/xmldsig#"};
            throw new XMLSecurityException("signature.Verification.NoSignatureElement", exArgs);
        }
        return Base64.decode(this.digestValueElement);
    }

    public boolean verify() throws ReferenceNotInitializedException, XMLSecurityException {
        byte[] calcDig;
        byte[] elemDig = this.getDigestValue();
        boolean equal = MessageDigestAlgorithm.isEqual(elemDig, calcDig = this.calculateDigest(true));
        if (!equal) {
            log.warn("Verification failed for URI \"" + this.getURI() + "\"");
            log.warn("Expected Digest: " + Base64.encode(elemDig));
            log.warn("Actual Digest: " + Base64.encode(calcDig));
        } else if (log.isDebugEnabled()) {
            log.debug("Verification successful for URI \"" + this.getURI() + "\"");
        }
        return equal;
    }

    public String getBaseLocalName() {
        return "Reference";
    }
}

