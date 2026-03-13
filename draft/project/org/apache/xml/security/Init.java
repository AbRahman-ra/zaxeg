/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security;

import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.algorithms.SignatureAlgorithm;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.keyresolver.KeyResolver;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.utils.ElementProxy;
import org.apache.xml.security.utils.I18n;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Init {
    public static final String CONF_NS = "http://www.xmlsecurity.org/NS/#configuration";
    private static Log log = LogFactory.getLog(Init.class);
    private static boolean alreadyInitialized = false;

    public static final synchronized boolean isInitialized() {
        return alreadyInitialized;
    }

    public static synchronized void init() {
        if (alreadyInitialized) {
            return;
        }
        InputStream is = AccessController.doPrivileged(new PrivilegedAction<InputStream>(){

            @Override
            public InputStream run() {
                String cfile = System.getProperty("org.apache.xml.security.resource.config");
                if (cfile == null) {
                    return null;
                }
                return this.getClass().getResourceAsStream(cfile);
            }
        });
        if (is == null) {
            Init.dynamicInit();
        } else {
            Init.fileInit(is);
        }
        alreadyInitialized = true;
    }

    private static void dynamicInit() {
        I18n.init("en", "US");
        if (log.isDebugEnabled()) {
            log.debug("Registering default algorithms");
        }
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>(){

                @Override
                public Void run() throws XMLSecurityException {
                    ElementProxy.registerDefaultPrefixes();
                    Transform.registerDefaultAlgorithms();
                    SignatureAlgorithm.registerDefaultAlgorithms();
                    JCEMapper.registerDefaultAlgorithms();
                    Canonicalizer.registerDefaultAlgorithms();
                    ResourceResolver.registerDefaultResolvers();
                    KeyResolver.registerDefaultResolvers();
                    return null;
                }
            });
        } catch (PrivilegedActionException ex) {
            XMLSecurityException xse = (XMLSecurityException)ex.getException();
            log.error(xse);
            xse.printStackTrace();
        }
    }

    private static void fileInit(InputStream is) {
        try {
            Node config;
            DocumentBuilder db = XMLUtils.createDocumentBuilder(false);
            Document doc = db.parse(is);
            for (config = doc.getFirstChild(); config != null && !"Configuration".equals(config.getLocalName()); config = config.getNextSibling()) {
            }
            if (config == null) {
                log.error("Error in reading configuration file - Configuration element not found");
                return;
            }
            for (Node el = config.getFirstChild(); el != null; el = el.getNextSibling()) {
                Element[] resolverElem;
                Node algorithmsNode;
                Object[] exArgs;
                String javaClass;
                String uri;
                if (1 != el.getNodeType()) continue;
                String tag = el.getLocalName();
                if (tag.equals("ResourceBundles")) {
                    Element resource = (Element)el;
                    Attr langAttr = resource.getAttributeNodeNS(null, "defaultLanguageCode");
                    Attr countryAttr = resource.getAttributeNodeNS(null, "defaultCountryCode");
                    String languageCode = langAttr == null ? null : langAttr.getNodeValue();
                    String countryCode = countryAttr == null ? null : countryAttr.getNodeValue();
                    I18n.init(languageCode, countryCode);
                }
                if (tag.equals("CanonicalizationMethods")) {
                    Element[] list = XMLUtils.selectNodes(el.getFirstChild(), CONF_NS, "CanonicalizationMethod");
                    for (int i = 0; i < list.length; ++i) {
                        uri = list[i].getAttributeNS(null, "URI");
                        javaClass = list[i].getAttributeNS(null, "JAVACLASS");
                        try {
                            Canonicalizer.register(uri, javaClass);
                            if (!log.isDebugEnabled()) continue;
                            log.debug("Canonicalizer.register(" + uri + ", " + javaClass + ")");
                            continue;
                        } catch (ClassNotFoundException e) {
                            exArgs = new Object[]{uri, javaClass};
                            log.error(I18n.translate("algorithm.classDoesNotExist", exArgs));
                        }
                    }
                }
                if (tag.equals("TransformAlgorithms")) {
                    Element[] tranElem = XMLUtils.selectNodes(el.getFirstChild(), CONF_NS, "TransformAlgorithm");
                    for (int i = 0; i < tranElem.length; ++i) {
                        uri = tranElem[i].getAttributeNS(null, "URI");
                        javaClass = tranElem[i].getAttributeNS(null, "JAVACLASS");
                        try {
                            Transform.register(uri, javaClass);
                            if (!log.isDebugEnabled()) continue;
                            log.debug("Transform.register(" + uri + ", " + javaClass + ")");
                            continue;
                        } catch (ClassNotFoundException e) {
                            exArgs = new Object[]{uri, javaClass};
                            log.error(I18n.translate("algorithm.classDoesNotExist", exArgs));
                            continue;
                        } catch (NoClassDefFoundError ex) {
                            log.warn("Not able to found dependencies for algorithm, I'll keep working.");
                        }
                    }
                }
                if ("JCEAlgorithmMappings".equals(tag) && (algorithmsNode = ((Element)el).getElementsByTagName("Algorithms").item(0)) != null) {
                    Element[] algorithms = XMLUtils.selectNodes(algorithmsNode.getFirstChild(), CONF_NS, "Algorithm");
                    for (int i = 0; i < algorithms.length; ++i) {
                        Element element = algorithms[i];
                        String id = element.getAttributeNS(null, "URI");
                        JCEMapper.register(id, new JCEMapper.Algorithm(element));
                    }
                }
                if (tag.equals("SignatureAlgorithms")) {
                    Element[] sigElems = XMLUtils.selectNodes(el.getFirstChild(), CONF_NS, "SignatureAlgorithm");
                    for (int i = 0; i < sigElems.length; ++i) {
                        uri = sigElems[i].getAttributeNS(null, "URI");
                        javaClass = sigElems[i].getAttributeNS(null, "JAVACLASS");
                        try {
                            SignatureAlgorithm.register(uri, javaClass);
                            if (!log.isDebugEnabled()) continue;
                            log.debug("SignatureAlgorithm.register(" + uri + ", " + javaClass + ")");
                            continue;
                        } catch (ClassNotFoundException e) {
                            exArgs = new Object[]{uri, javaClass};
                            log.error(I18n.translate("algorithm.classDoesNotExist", exArgs));
                        }
                    }
                }
                if (tag.equals("ResourceResolvers")) {
                    resolverElem = XMLUtils.selectNodes(el.getFirstChild(), CONF_NS, "Resolver");
                    for (int i = 0; i < resolverElem.length; ++i) {
                        String javaClass2 = resolverElem[i].getAttributeNS(null, "JAVACLASS");
                        String description = resolverElem[i].getAttributeNS(null, "DESCRIPTION");
                        if (description != null && description.length() > 0) {
                            if (log.isDebugEnabled()) {
                                log.debug("Register Resolver: " + javaClass2 + ": " + description);
                            }
                        } else if (log.isDebugEnabled()) {
                            log.debug("Register Resolver: " + javaClass2 + ": For unknown purposes");
                        }
                        try {
                            ResourceResolver.register(javaClass2);
                            continue;
                        } catch (Throwable e) {
                            log.warn("Cannot register:" + javaClass2 + " perhaps some needed jars are not installed", e);
                        }
                    }
                }
                if (tag.equals("KeyResolver")) {
                    resolverElem = XMLUtils.selectNodes(el.getFirstChild(), CONF_NS, "Resolver");
                    ArrayList<String> classNames = new ArrayList<String>(resolverElem.length);
                    for (int i = 0; i < resolverElem.length; ++i) {
                        javaClass = resolverElem[i].getAttributeNS(null, "JAVACLASS");
                        String description = resolverElem[i].getAttributeNS(null, "DESCRIPTION");
                        if (description != null && description.length() > 0) {
                            if (log.isDebugEnabled()) {
                                log.debug("Register Resolver: " + javaClass + ": " + description);
                            }
                        } else if (log.isDebugEnabled()) {
                            log.debug("Register Resolver: " + javaClass + ": For unknown purposes");
                        }
                        classNames.add(javaClass);
                    }
                    KeyResolver.registerClassNames(classNames);
                }
                if (!tag.equals("PrefixMappings")) continue;
                if (log.isDebugEnabled()) {
                    log.debug("Now I try to bind prefixes:");
                }
                Element[] nl = XMLUtils.selectNodes(el.getFirstChild(), CONF_NS, "PrefixMapping");
                for (int i = 0; i < nl.length; ++i) {
                    String namespace = nl[i].getAttributeNS(null, "namespace");
                    String prefix = nl[i].getAttributeNS(null, "prefix");
                    if (log.isDebugEnabled()) {
                        log.debug("Now I try to bind " + prefix + " to " + namespace);
                    }
                    ElementProxy.setDefaultPrefix(namespace, prefix);
                }
            }
        } catch (Exception e) {
            log.error("Bad: ", e);
            e.printStackTrace();
        }
    }
}

