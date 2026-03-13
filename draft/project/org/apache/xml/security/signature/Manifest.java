/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.signature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.MissingResourceFailureException;
import org.apache.xml.security.signature.Reference;
import org.apache.xml.security.signature.ReferenceNotInitializedException;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.I18n;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Manifest
extends SignatureElementProxy {
    public static final int MAXIMUM_REFERENCE_COUNT = 30;
    private static Log log = LogFactory.getLog(Manifest.class);
    private List<Reference> references;
    private Element[] referencesEl;
    private boolean[] verificationResults = null;
    private Map<String, String> resolverProperties = null;
    private List<ResourceResolver> perManifestResolvers = null;
    private boolean secureValidation;

    public Manifest(Document doc) {
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        this.references = new ArrayList<Reference>();
    }

    public Manifest(Element element, String baseURI) throws XMLSecurityException {
        this(element, baseURI, false);
    }

    public Manifest(Element element, String baseURI, boolean secureValidation) throws XMLSecurityException {
        super(element, baseURI);
        Attr attr = element.getAttributeNodeNS(null, "Id");
        if (attr != null) {
            element.setIdAttributeNode(attr, true);
        }
        this.secureValidation = secureValidation;
        this.referencesEl = XMLUtils.selectDsNodes(this.constructionElement.getFirstChild(), "Reference");
        int le = this.referencesEl.length;
        if (le == 0) {
            Object[] exArgs = new Object[]{"Reference", "Manifest"};
            throw new DOMException(4, I18n.translate("xml.WrongContent", exArgs));
        }
        if (secureValidation && le > 30) {
            Object[] exArgs = new Object[]{le, 30};
            throw new XMLSecurityException("signature.tooManyReferences", exArgs);
        }
        this.references = new ArrayList<Reference>(le);
        for (int i = 0; i < le; ++i) {
            Element refElem = this.referencesEl[i];
            Attr refAttr = refElem.getAttributeNodeNS(null, "Id");
            if (refAttr != null) {
                refElem.setIdAttributeNode(refAttr, true);
            }
            this.references.add(null);
        }
    }

    public void addDocument(String baseURI, String referenceURI, Transforms transforms, String digestURI, String referenceId, String referenceType) throws XMLSignatureException {
        Reference ref = new Reference(this.doc, baseURI, referenceURI, this, transforms, digestURI);
        if (referenceId != null) {
            ref.setId(referenceId);
        }
        if (referenceType != null) {
            ref.setType(referenceType);
        }
        this.references.add(ref);
        this.constructionElement.appendChild(ref.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void generateDigestValues() throws XMLSignatureException, ReferenceNotInitializedException {
        for (int i = 0; i < this.getLength(); ++i) {
            Reference currentRef = this.references.get(i);
            currentRef.generateDigestValue();
        }
    }

    public int getLength() {
        return this.references.size();
    }

    public Reference item(int i) throws XMLSecurityException {
        if (this.references.get(i) == null) {
            Reference ref = new Reference(this.referencesEl[i], this.baseURI, this, this.secureValidation);
            this.references.set(i, ref);
        }
        return this.references.get(i);
    }

    public void setId(String Id2) {
        if (Id2 != null) {
            this.constructionElement.setAttributeNS(null, "Id", Id2);
            this.constructionElement.setIdAttributeNS(null, "Id", true);
        }
    }

    public String getId() {
        return this.constructionElement.getAttributeNS(null, "Id");
    }

    public boolean verifyReferences() throws MissingResourceFailureException, XMLSecurityException {
        return this.verifyReferences(false);
    }

    public boolean verifyReferences(boolean followManifests) throws MissingResourceFailureException, XMLSecurityException {
        if (this.referencesEl == null) {
            this.referencesEl = XMLUtils.selectDsNodes(this.constructionElement.getFirstChild(), "Reference");
        }
        if (log.isDebugEnabled()) {
            log.debug("verify " + this.referencesEl.length + " References");
            log.debug("I am " + (followManifests ? "" : "not") + " requested to follow nested Manifests");
        }
        if (this.referencesEl.length == 0) {
            throw new XMLSecurityException("empty");
        }
        if (this.secureValidation && this.referencesEl.length > 30) {
            Object[] exArgs = new Object[]{this.referencesEl.length, 30};
            throw new XMLSecurityException("signature.tooManyReferences", exArgs);
        }
        this.verificationResults = new boolean[this.referencesEl.length];
        boolean verify = true;
        for (int i = 0; i < this.referencesEl.length; ++i) {
            Reference currentRef = new Reference(this.referencesEl[i], this.baseURI, this, this.secureValidation);
            this.references.set(i, currentRef);
            try {
                boolean currentRefVerified = currentRef.verify();
                this.setVerificationResult(i, currentRefVerified);
                if (!currentRefVerified) {
                    verify = false;
                }
                if (log.isDebugEnabled()) {
                    log.debug("The Reference has Type " + currentRef.getType());
                }
                if (!verify || !followManifests || !currentRef.typeIsReferenceToManifest()) continue;
                if (log.isDebugEnabled()) {
                    log.debug("We have to follow a nested Manifest");
                }
                try {
                    XMLSignatureInput signedManifestNodes = currentRef.dereferenceURIandPerformTransforms(null);
                    Set<Node> nl = signedManifestNodes.getNodeSet();
                    Manifest referencedManifest = null;
                    for (Node n : nl) {
                        if (n.getNodeType() != 1 || !((Element)n).getNamespaceURI().equals("http://www.w3.org/2000/09/xmldsig#") || !((Element)n).getLocalName().equals("Manifest")) continue;
                        try {
                            referencedManifest = new Manifest((Element)n, signedManifestNodes.getSourceURI(), this.secureValidation);
                            break;
                        } catch (XMLSecurityException ex) {
                            if (!log.isDebugEnabled()) continue;
                            log.debug(ex);
                        }
                    }
                    if (referencedManifest == null) {
                        throw new MissingResourceFailureException("empty", currentRef);
                    }
                    referencedManifest.perManifestResolvers = this.perManifestResolvers;
                    referencedManifest.resolverProperties = this.resolverProperties;
                    boolean referencedManifestValid = referencedManifest.verifyReferences(followManifests);
                    if (!referencedManifestValid) {
                        verify = false;
                        log.warn("The nested Manifest was invalid (bad)");
                        continue;
                    }
                    if (!log.isDebugEnabled()) continue;
                    log.debug("The nested Manifest was valid (good)");
                    continue;
                } catch (IOException ex) {
                    throw new ReferenceNotInitializedException("empty", ex);
                } catch (ParserConfigurationException ex) {
                    throw new ReferenceNotInitializedException("empty", ex);
                } catch (SAXException ex) {
                    throw new ReferenceNotInitializedException("empty", ex);
                }
            } catch (ReferenceNotInitializedException ex) {
                Object[] exArgs = new Object[]{currentRef.getURI()};
                throw new MissingResourceFailureException("signature.Verification.Reference.NoInput", exArgs, ex, currentRef);
            }
        }
        return verify;
    }

    private void setVerificationResult(int index, boolean verify) {
        if (this.verificationResults == null) {
            this.verificationResults = new boolean[this.getLength()];
        }
        this.verificationResults[index] = verify;
    }

    public boolean getVerificationResult(int index) throws XMLSecurityException {
        if (index < 0 || index > this.getLength() - 1) {
            Object[] exArgs = new Object[]{Integer.toString(index), Integer.toString(this.getLength())};
            IndexOutOfBoundsException e = new IndexOutOfBoundsException(I18n.translate("signature.Verification.IndexOutOfBounds", exArgs));
            throw new XMLSecurityException("generic.EmptyMessage", e);
        }
        if (this.verificationResults == null) {
            try {
                this.verifyReferences();
            } catch (Exception ex) {
                throw new XMLSecurityException("generic.EmptyMessage", ex);
            }
        }
        return this.verificationResults[index];
    }

    public void addResourceResolver(ResourceResolver resolver) {
        if (resolver == null) {
            return;
        }
        if (this.perManifestResolvers == null) {
            this.perManifestResolvers = new ArrayList<ResourceResolver>();
        }
        this.perManifestResolvers.add(resolver);
    }

    public void addResourceResolver(ResourceResolverSpi resolverSpi) {
        if (resolverSpi == null) {
            return;
        }
        if (this.perManifestResolvers == null) {
            this.perManifestResolvers = new ArrayList<ResourceResolver>();
        }
        this.perManifestResolvers.add(new ResourceResolver(resolverSpi));
    }

    public List<ResourceResolver> getPerManifestResolvers() {
        return this.perManifestResolvers;
    }

    public Map<String, String> getResolverProperties() {
        return this.resolverProperties;
    }

    public void setResolverProperty(String key, String value) {
        if (this.resolverProperties == null) {
            this.resolverProperties = new HashMap<String, String>(10);
        }
        this.resolverProperties.put(key, value);
    }

    public String getResolverProperty(String key) {
        return this.resolverProperties.get(key);
    }

    public byte[] getSignedContentItem(int i) throws XMLSignatureException {
        try {
            return this.getReferencedContentAfterTransformsItem(i).getBytes();
        } catch (IOException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (CanonicalizationException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (InvalidCanonicalizerException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (XMLSecurityException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    public XMLSignatureInput getReferencedContentBeforeTransformsItem(int i) throws XMLSecurityException {
        return this.item(i).getContentsBeforeTransformation();
    }

    public XMLSignatureInput getReferencedContentAfterTransformsItem(int i) throws XMLSecurityException {
        return this.item(i).getContentsAfterTransformation();
    }

    public int getSignedContentLength() {
        return this.getLength();
    }

    @Override
    public String getBaseLocalName() {
        return "Manifest";
    }

    public boolean isSecureValidation() {
        return this.secureValidation;
    }
}

