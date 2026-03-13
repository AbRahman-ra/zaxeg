/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.keyresolver.implementations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.RetrievalMethod;
import org.apache.xml.security.keys.keyresolver.KeyResolver;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import org.apache.xml.security.keys.storage.StorageResolver;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class RetrievalMethodResolver
extends KeyResolverSpi {
    private static Log log = LogFactory.getLog(RetrievalMethodResolver.class);

    @Override
    public PublicKey engineLookupAndResolvePublicKey(Element element, String baseURI, StorageResolver storage) {
        block18: {
            if (!XMLUtils.elementIsInSignatureSpace(element, "RetrievalMethod")) {
                return null;
            }
            try {
                RetrievalMethod rm = new RetrievalMethod(element, baseURI);
                String type = rm.getType();
                XMLSignatureInput resource = RetrievalMethodResolver.resolveInput(rm, baseURI, this.secureValidation);
                if ("http://www.w3.org/2000/09/xmldsig#rawX509Certificate".equals(type)) {
                    X509Certificate cert = RetrievalMethodResolver.getRawCertificate(resource);
                    if (cert != null) {
                        return cert.getPublicKey();
                    }
                    return null;
                }
                Element e = RetrievalMethodResolver.obtainReferenceElement(resource, this.secureValidation);
                if (XMLUtils.elementIsInSignatureSpace(e, "RetrievalMethod")) {
                    if (this.secureValidation) {
                        String error = "Error: It is forbidden to have one RetrievalMethod point to another with secure validation";
                        if (log.isDebugEnabled()) {
                            log.debug(error);
                        }
                        return null;
                    }
                    RetrievalMethod rm2 = new RetrievalMethod(e, baseURI);
                    XMLSignatureInput resource2 = RetrievalMethodResolver.resolveInput(rm2, baseURI, this.secureValidation);
                    Element e2 = RetrievalMethodResolver.obtainReferenceElement(resource2, this.secureValidation);
                    if (e2 == element) {
                        if (log.isDebugEnabled()) {
                            log.debug("Error: Can't have RetrievalMethods pointing to each other");
                        }
                        return null;
                    }
                }
                return RetrievalMethodResolver.resolveKey(e, baseURI, storage);
            } catch (XMLSecurityException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("XMLSecurityException", ex);
                }
            } catch (CertificateException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("CertificateException", ex);
                }
            } catch (IOException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("IOException", ex);
                }
            } catch (ParserConfigurationException e) {
                if (log.isDebugEnabled()) {
                    log.debug("ParserConfigurationException", e);
                }
            } catch (SAXException e) {
                if (!log.isDebugEnabled()) break block18;
                log.debug("SAXException", e);
            }
        }
        return null;
    }

    @Override
    public X509Certificate engineLookupResolveX509Certificate(Element element, String baseURI, StorageResolver storage) {
        block17: {
            if (!XMLUtils.elementIsInSignatureSpace(element, "RetrievalMethod")) {
                return null;
            }
            try {
                RetrievalMethod rm = new RetrievalMethod(element, baseURI);
                String type = rm.getType();
                XMLSignatureInput resource = RetrievalMethodResolver.resolveInput(rm, baseURI, this.secureValidation);
                if ("http://www.w3.org/2000/09/xmldsig#rawX509Certificate".equals(type)) {
                    return RetrievalMethodResolver.getRawCertificate(resource);
                }
                Element e = RetrievalMethodResolver.obtainReferenceElement(resource, this.secureValidation);
                if (XMLUtils.elementIsInSignatureSpace(e, "RetrievalMethod")) {
                    if (this.secureValidation) {
                        String error = "Error: It is forbidden to have one RetrievalMethod point to another with secure validation";
                        if (log.isDebugEnabled()) {
                            log.debug(error);
                        }
                        return null;
                    }
                    RetrievalMethod rm2 = new RetrievalMethod(e, baseURI);
                    XMLSignatureInput resource2 = RetrievalMethodResolver.resolveInput(rm2, baseURI, this.secureValidation);
                    Element e2 = RetrievalMethodResolver.obtainReferenceElement(resource2, this.secureValidation);
                    if (e2 == element) {
                        if (log.isDebugEnabled()) {
                            log.debug("Error: Can't have RetrievalMethods pointing to each other");
                        }
                        return null;
                    }
                }
                return RetrievalMethodResolver.resolveCertificate(e, baseURI, storage);
            } catch (XMLSecurityException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("XMLSecurityException", ex);
                }
            } catch (CertificateException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("CertificateException", ex);
                }
            } catch (IOException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("IOException", ex);
                }
            } catch (ParserConfigurationException e) {
                if (log.isDebugEnabled()) {
                    log.debug("ParserConfigurationException", e);
                }
            } catch (SAXException e) {
                if (!log.isDebugEnabled()) break block17;
                log.debug("SAXException", e);
            }
        }
        return null;
    }

    private static X509Certificate resolveCertificate(Element e, String baseURI, StorageResolver storage) throws KeyResolverException {
        if (log.isDebugEnabled()) {
            log.debug("Now we have a {" + e.getNamespaceURI() + "}" + e.getLocalName() + " Element");
        }
        if (e != null) {
            return KeyResolver.getX509Certificate(e, baseURI, storage);
        }
        return null;
    }

    private static PublicKey resolveKey(Element e, String baseURI, StorageResolver storage) throws KeyResolverException {
        if (log.isDebugEnabled()) {
            log.debug("Now we have a {" + e.getNamespaceURI() + "}" + e.getLocalName() + " Element");
        }
        if (e != null) {
            return KeyResolver.getPublicKey(e, baseURI, storage);
        }
        return null;
    }

    private static Element obtainReferenceElement(XMLSignatureInput resource, boolean secureValidation) throws CanonicalizationException, ParserConfigurationException, IOException, SAXException, KeyResolverException {
        Element e;
        if (resource.isElement()) {
            e = (Element)resource.getSubNode();
        } else if (resource.isNodeSet()) {
            e = RetrievalMethodResolver.getDocumentElement(resource.getNodeSet());
        } else {
            byte[] inputBytes = resource.getBytes();
            e = RetrievalMethodResolver.getDocFromBytes(inputBytes, secureValidation);
            if (log.isDebugEnabled()) {
                log.debug("we have to parse " + inputBytes.length + " bytes");
            }
        }
        return e;
    }

    private static X509Certificate getRawCertificate(XMLSignatureInput resource) throws CanonicalizationException, IOException, CertificateException {
        byte[] inputBytes = resource.getBytes();
        CertificateFactory certFact = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate)certFact.generateCertificate(new ByteArrayInputStream(inputBytes));
        return cert;
    }

    private static XMLSignatureInput resolveInput(RetrievalMethod rm, String baseURI, boolean secureValidation) throws XMLSecurityException {
        Attr uri = rm.getURIAttr();
        Transforms transforms = rm.getTransforms();
        ResourceResolver resRes = ResourceResolver.getInstance(uri, baseURI, secureValidation);
        XMLSignatureInput resource = resRes.resolve(uri, baseURI, secureValidation);
        if (transforms != null) {
            if (log.isDebugEnabled()) {
                log.debug("We have Transforms");
            }
            resource = transforms.performTransforms(resource);
        }
        return resource;
    }

    private static Element getDocFromBytes(byte[] bytes, boolean secureValidation) throws KeyResolverException {
        try {
            DocumentBuilder db = XMLUtils.createDocumentBuilder(false, secureValidation);
            Document doc = db.parse(new ByteArrayInputStream(bytes));
            return doc.getDocumentElement();
        } catch (SAXException ex) {
            throw new KeyResolverException("empty", ex);
        } catch (IOException ex) {
            throw new KeyResolverException("empty", ex);
        } catch (ParserConfigurationException ex) {
            throw new KeyResolverException("empty", ex);
        }
    }

    @Override
    public SecretKey engineLookupAndResolveSecretKey(Element element, String baseURI, StorageResolver storage) {
        return null;
    }

    private static Element getDocumentElement(Set<Node> set) {
        Iterator<Node> it = set.iterator();
        Node e = null;
        while (it.hasNext()) {
            Node currentNode = it.next();
            if (currentNode == null || 1 != currentNode.getNodeType()) continue;
            e = (Element)currentNode;
            break;
        }
        ArrayList<Node> parents = new ArrayList<Node>();
        while (e != null) {
            parents.add(e);
            Node n = e.getParentNode();
            if (n == null || 1 != n.getNodeType()) break;
            e = (Element)n;
        }
        ListIterator it2 = parents.listIterator(parents.size() - 1);
        Element ele = null;
        while (it2.hasPrevious()) {
            ele = (Element)it2.previous();
            if (!set.contains(ele)) continue;
            return ele;
        }
        return null;
    }
}

