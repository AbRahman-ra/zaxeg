/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.c14n;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.CanonicalizerSpi;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.c14n.implementations.Canonicalizer11_OmitComments;
import org.apache.xml.security.c14n.implementations.Canonicalizer11_WithComments;
import org.apache.xml.security.c14n.implementations.Canonicalizer20010315ExclOmitComments;
import org.apache.xml.security.c14n.implementations.Canonicalizer20010315ExclWithComments;
import org.apache.xml.security.c14n.implementations.Canonicalizer20010315OmitComments;
import org.apache.xml.security.c14n.implementations.Canonicalizer20010315WithComments;
import org.apache.xml.security.c14n.implementations.CanonicalizerPhysical;
import org.apache.xml.security.exceptions.AlgorithmAlreadyRegisteredException;
import org.apache.xml.security.utils.ClassLoaderUtils;
import org.apache.xml.security.utils.IgnoreAllErrorHandler;
import org.apache.xml.security.utils.JavaUtils;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Canonicalizer {
    public static final String ENCODING = "UTF8";
    public static final String XPATH_C14N_WITH_COMMENTS_SINGLE_NODE = "(.//. | .//@* | .//namespace::*)";
    public static final String ALGO_ID_C14N_OMIT_COMMENTS = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
    public static final String ALGO_ID_C14N_WITH_COMMENTS = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments";
    public static final String ALGO_ID_C14N_EXCL_OMIT_COMMENTS = "http://www.w3.org/2001/10/xml-exc-c14n#";
    public static final String ALGO_ID_C14N_EXCL_WITH_COMMENTS = "http://www.w3.org/2001/10/xml-exc-c14n#WithComments";
    public static final String ALGO_ID_C14N11_OMIT_COMMENTS = "http://www.w3.org/2006/12/xml-c14n11";
    public static final String ALGO_ID_C14N11_WITH_COMMENTS = "http://www.w3.org/2006/12/xml-c14n11#WithComments";
    public static final String ALGO_ID_C14N_PHYSICAL = "http://santuario.apache.org/c14n/physical";
    private static Map<String, Class<? extends CanonicalizerSpi>> canonicalizerHash = new ConcurrentHashMap<String, Class<? extends CanonicalizerSpi>>();
    private final CanonicalizerSpi canonicalizerSpi;
    private boolean secureValidation;

    private Canonicalizer(String algorithmURI) throws InvalidCanonicalizerException {
        try {
            Class<? extends CanonicalizerSpi> implementingClass = canonicalizerHash.get(algorithmURI);
            this.canonicalizerSpi = implementingClass.newInstance();
            this.canonicalizerSpi.reset = true;
        } catch (Exception e) {
            Object[] exArgs = new Object[]{algorithmURI};
            throw new InvalidCanonicalizerException("signature.Canonicalizer.UnknownCanonicalizer", exArgs, e);
        }
    }

    public static final Canonicalizer getInstance(String algorithmURI) throws InvalidCanonicalizerException {
        return new Canonicalizer(algorithmURI);
    }

    public static void register(String algorithmURI, String implementingClass) throws AlgorithmAlreadyRegisteredException, ClassNotFoundException {
        JavaUtils.checkRegisterPermission();
        Class<? extends CanonicalizerSpi> registeredClass = canonicalizerHash.get(algorithmURI);
        if (registeredClass != null) {
            Object[] exArgs = new Object[]{algorithmURI, registeredClass};
            throw new AlgorithmAlreadyRegisteredException("algorithm.alreadyRegistered", exArgs);
        }
        canonicalizerHash.put(algorithmURI, ClassLoaderUtils.loadClass(implementingClass, Canonicalizer.class));
    }

    public static void register(String algorithmURI, Class<CanonicalizerSpi> implementingClass) throws AlgorithmAlreadyRegisteredException, ClassNotFoundException {
        JavaUtils.checkRegisterPermission();
        Class<? extends CanonicalizerSpi> registeredClass = canonicalizerHash.get(algorithmURI);
        if (registeredClass != null) {
            Object[] exArgs = new Object[]{algorithmURI, registeredClass};
            throw new AlgorithmAlreadyRegisteredException("algorithm.alreadyRegistered", exArgs);
        }
        canonicalizerHash.put(algorithmURI, implementingClass);
    }

    public static void registerDefaultAlgorithms() {
        canonicalizerHash.put(ALGO_ID_C14N_OMIT_COMMENTS, Canonicalizer20010315OmitComments.class);
        canonicalizerHash.put(ALGO_ID_C14N_WITH_COMMENTS, Canonicalizer20010315WithComments.class);
        canonicalizerHash.put(ALGO_ID_C14N_EXCL_OMIT_COMMENTS, Canonicalizer20010315ExclOmitComments.class);
        canonicalizerHash.put(ALGO_ID_C14N_EXCL_WITH_COMMENTS, Canonicalizer20010315ExclWithComments.class);
        canonicalizerHash.put(ALGO_ID_C14N11_OMIT_COMMENTS, Canonicalizer11_OmitComments.class);
        canonicalizerHash.put(ALGO_ID_C14N11_WITH_COMMENTS, Canonicalizer11_WithComments.class);
        canonicalizerHash.put(ALGO_ID_C14N_PHYSICAL, CanonicalizerPhysical.class);
    }

    public final String getURI() {
        return this.canonicalizerSpi.engineGetURI();
    }

    public boolean getIncludeComments() {
        return this.canonicalizerSpi.engineGetIncludeComments();
    }

    public byte[] canonicalize(byte[] inputBytes) throws ParserConfigurationException, IOException, SAXException, CanonicalizationException {
        ByteArrayInputStream bais = new ByteArrayInputStream(inputBytes);
        InputSource in = new InputSource(bais);
        DocumentBuilder db = XMLUtils.createDocumentBuilder(true, this.secureValidation);
        db.setErrorHandler(new IgnoreAllErrorHandler());
        Document document = db.parse(in);
        return this.canonicalizeSubtree(document);
    }

    public byte[] canonicalizeSubtree(Node node) throws CanonicalizationException {
        this.canonicalizerSpi.secureValidation = this.secureValidation;
        return this.canonicalizerSpi.engineCanonicalizeSubTree(node);
    }

    public byte[] canonicalizeSubtree(Node node, String inclusiveNamespaces) throws CanonicalizationException {
        this.canonicalizerSpi.secureValidation = this.secureValidation;
        return this.canonicalizerSpi.engineCanonicalizeSubTree(node, inclusiveNamespaces);
    }

    public byte[] canonicalizeXPathNodeSet(NodeList xpathNodeSet) throws CanonicalizationException {
        this.canonicalizerSpi.secureValidation = this.secureValidation;
        return this.canonicalizerSpi.engineCanonicalizeXPathNodeSet(xpathNodeSet);
    }

    public byte[] canonicalizeXPathNodeSet(NodeList xpathNodeSet, String inclusiveNamespaces) throws CanonicalizationException {
        this.canonicalizerSpi.secureValidation = this.secureValidation;
        return this.canonicalizerSpi.engineCanonicalizeXPathNodeSet(xpathNodeSet, inclusiveNamespaces);
    }

    public byte[] canonicalizeXPathNodeSet(Set<Node> xpathNodeSet) throws CanonicalizationException {
        this.canonicalizerSpi.secureValidation = this.secureValidation;
        return this.canonicalizerSpi.engineCanonicalizeXPathNodeSet(xpathNodeSet);
    }

    public byte[] canonicalizeXPathNodeSet(Set<Node> xpathNodeSet, String inclusiveNamespaces) throws CanonicalizationException {
        this.canonicalizerSpi.secureValidation = this.secureValidation;
        return this.canonicalizerSpi.engineCanonicalizeXPathNodeSet(xpathNodeSet, inclusiveNamespaces);
    }

    public void setWriter(OutputStream os) {
        this.canonicalizerSpi.setWriter(os);
    }

    public String getImplementingCanonicalizerClass() {
        return this.canonicalizerSpi.getClass().getName();
    }

    public void notReset() {
        this.canonicalizerSpi.reset = false;
    }

    public boolean isSecureValidation() {
        return this.secureValidation;
    }

    public void setSecureValidation(boolean secureValidation) {
        this.secureValidation = secureValidation;
    }
}

