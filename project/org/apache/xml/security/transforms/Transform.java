/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.transforms;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.exceptions.AlgorithmAlreadyRegisteredException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.InvalidTransformException;
import org.apache.xml.security.transforms.TransformSpi;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.transforms.implementations.TransformBase64Decode;
import org.apache.xml.security.transforms.implementations.TransformC14N;
import org.apache.xml.security.transforms.implementations.TransformC14N11;
import org.apache.xml.security.transforms.implementations.TransformC14N11_WithComments;
import org.apache.xml.security.transforms.implementations.TransformC14NExclusive;
import org.apache.xml.security.transforms.implementations.TransformC14NExclusiveWithComments;
import org.apache.xml.security.transforms.implementations.TransformC14NWithComments;
import org.apache.xml.security.transforms.implementations.TransformEnvelopedSignature;
import org.apache.xml.security.transforms.implementations.TransformXPath;
import org.apache.xml.security.transforms.implementations.TransformXPath2Filter;
import org.apache.xml.security.transforms.implementations.TransformXSLT;
import org.apache.xml.security.utils.ClassLoaderUtils;
import org.apache.xml.security.utils.HelperNodeList;
import org.apache.xml.security.utils.JavaUtils;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class Transform
extends SignatureElementProxy {
    private static Log log = LogFactory.getLog(Transform.class);
    private static Map<String, Class<? extends TransformSpi>> transformSpiHash = new ConcurrentHashMap<String, Class<? extends TransformSpi>>();
    private final TransformSpi transformSpi;
    private boolean secureValidation;

    public Transform(Document doc, String algorithmURI) throws InvalidTransformException {
        this(doc, algorithmURI, (NodeList)null);
    }

    public Transform(Document doc, String algorithmURI, Element contextChild) throws InvalidTransformException {
        super(doc);
        HelperNodeList contextNodes = null;
        if (contextChild != null) {
            contextNodes = new HelperNodeList();
            XMLUtils.addReturnToElement(doc, contextNodes);
            contextNodes.appendChild(contextChild);
            XMLUtils.addReturnToElement(doc, contextNodes);
        }
        this.transformSpi = this.initializeTransform(algorithmURI, contextNodes);
    }

    public Transform(Document doc, String algorithmURI, NodeList contextNodes) throws InvalidTransformException {
        super(doc);
        this.transformSpi = this.initializeTransform(algorithmURI, contextNodes);
    }

    public Transform(Element element, String BaseURI) throws InvalidTransformException, TransformationException, XMLSecurityException {
        super(element, BaseURI);
        String algorithmURI = element.getAttributeNS(null, "Algorithm");
        if (algorithmURI == null || algorithmURI.length() == 0) {
            Object[] exArgs = new Object[]{"Algorithm", "Transform"};
            throw new TransformationException("xml.WrongContent", exArgs);
        }
        Class<? extends TransformSpi> transformSpiClass = transformSpiHash.get(algorithmURI);
        if (transformSpiClass == null) {
            Object[] exArgs = new Object[]{algorithmURI};
            throw new InvalidTransformException("signature.Transform.UnknownTransform", exArgs);
        }
        try {
            this.transformSpi = transformSpiClass.newInstance();
        } catch (InstantiationException ex) {
            Object[] exArgs = new Object[]{algorithmURI};
            throw new InvalidTransformException("signature.Transform.UnknownTransform", exArgs, ex);
        } catch (IllegalAccessException ex) {
            Object[] exArgs = new Object[]{algorithmURI};
            throw new InvalidTransformException("signature.Transform.UnknownTransform", exArgs, ex);
        }
    }

    public static void register(String algorithmURI, String implementingClass) throws AlgorithmAlreadyRegisteredException, ClassNotFoundException, InvalidTransformException {
        JavaUtils.checkRegisterPermission();
        Class<? extends TransformSpi> transformSpi = transformSpiHash.get(algorithmURI);
        if (transformSpi != null) {
            Object[] exArgs = new Object[]{algorithmURI, transformSpi};
            throw new AlgorithmAlreadyRegisteredException("algorithm.alreadyRegistered", exArgs);
        }
        Class<?> transformSpiClass = ClassLoaderUtils.loadClass(implementingClass, Transform.class);
        transformSpiHash.put(algorithmURI, transformSpiClass);
    }

    public static void register(String algorithmURI, Class<? extends TransformSpi> implementingClass) throws AlgorithmAlreadyRegisteredException {
        JavaUtils.checkRegisterPermission();
        Class<? extends TransformSpi> transformSpi = transformSpiHash.get(algorithmURI);
        if (transformSpi != null) {
            Object[] exArgs = new Object[]{algorithmURI, transformSpi};
            throw new AlgorithmAlreadyRegisteredException("algorithm.alreadyRegistered", exArgs);
        }
        transformSpiHash.put(algorithmURI, implementingClass);
    }

    public static void registerDefaultAlgorithms() {
        transformSpiHash.put("http://www.w3.org/2000/09/xmldsig#base64", TransformBase64Decode.class);
        transformSpiHash.put("http://www.w3.org/TR/2001/REC-xml-c14n-20010315", TransformC14N.class);
        transformSpiHash.put("http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments", TransformC14NWithComments.class);
        transformSpiHash.put("http://www.w3.org/2006/12/xml-c14n11", TransformC14N11.class);
        transformSpiHash.put("http://www.w3.org/2006/12/xml-c14n11#WithComments", TransformC14N11_WithComments.class);
        transformSpiHash.put("http://www.w3.org/2001/10/xml-exc-c14n#", TransformC14NExclusive.class);
        transformSpiHash.put("http://www.w3.org/2001/10/xml-exc-c14n#WithComments", TransformC14NExclusiveWithComments.class);
        transformSpiHash.put("http://www.w3.org/TR/1999/REC-xpath-19991116", TransformXPath.class);
        transformSpiHash.put("http://www.w3.org/2000/09/xmldsig#enveloped-signature", TransformEnvelopedSignature.class);
        transformSpiHash.put("http://www.w3.org/TR/1999/REC-xslt-19991116", TransformXSLT.class);
        transformSpiHash.put("http://www.w3.org/2002/06/xmldsig-filter2", TransformXPath2Filter.class);
    }

    public String getURI() {
        return this.constructionElement.getAttributeNS(null, "Algorithm");
    }

    public XMLSignatureInput performTransform(XMLSignatureInput input) throws IOException, CanonicalizationException, InvalidCanonicalizerException, TransformationException {
        return this.performTransform(input, null);
    }

    public XMLSignatureInput performTransform(XMLSignatureInput input, OutputStream os) throws IOException, CanonicalizationException, InvalidCanonicalizerException, TransformationException {
        XMLSignatureInput result = null;
        try {
            this.transformSpi.secureValidation = this.secureValidation;
            result = this.transformSpi.enginePerformTransform(input, os, this);
        } catch (ParserConfigurationException ex) {
            Object[] exArgs = new Object[]{this.getURI(), "ParserConfigurationException"};
            throw new CanonicalizationException("signature.Transform.ErrorDuringTransform", exArgs, ex);
        } catch (SAXException ex) {
            Object[] exArgs = new Object[]{this.getURI(), "SAXException"};
            throw new CanonicalizationException("signature.Transform.ErrorDuringTransform", exArgs, ex);
        }
        return result;
    }

    @Override
    public String getBaseLocalName() {
        return "Transform";
    }

    private TransformSpi initializeTransform(String algorithmURI, NodeList contextNodes) throws InvalidTransformException {
        this.constructionElement.setAttributeNS(null, "Algorithm", algorithmURI);
        Class<? extends TransformSpi> transformSpiClass = transformSpiHash.get(algorithmURI);
        if (transformSpiClass == null) {
            Object[] exArgs = new Object[]{algorithmURI};
            throw new InvalidTransformException("signature.Transform.UnknownTransform", exArgs);
        }
        TransformSpi newTransformSpi = null;
        try {
            newTransformSpi = transformSpiClass.newInstance();
        } catch (InstantiationException ex) {
            Object[] exArgs = new Object[]{algorithmURI};
            throw new InvalidTransformException("signature.Transform.UnknownTransform", exArgs, ex);
        } catch (IllegalAccessException ex) {
            Object[] exArgs = new Object[]{algorithmURI};
            throw new InvalidTransformException("signature.Transform.UnknownTransform", exArgs, ex);
        }
        if (log.isDebugEnabled()) {
            log.debug("Create URI \"" + algorithmURI + "\" class \"" + newTransformSpi.getClass() + "\"");
            log.debug("The NodeList is " + contextNodes);
        }
        if (contextNodes != null) {
            for (int i = 0; i < contextNodes.getLength(); ++i) {
                this.constructionElement.appendChild(contextNodes.item(i).cloneNode(true));
            }
        }
        return newTransformSpi;
    }

    public boolean isSecureValidation() {
        return this.secureValidation;
    }

    public void setSecureValidation(boolean secureValidation) {
        this.secureValidation = secureValidation;
    }
}

