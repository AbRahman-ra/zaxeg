/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.keyresolver.implementations;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import javax.crypto.SecretKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import org.apache.xml.security.keys.storage.StorageResolver;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Element;

public class SingleKeyResolver
extends KeyResolverSpi {
    private static Log log = LogFactory.getLog(SingleKeyResolver.class.getName());
    private String keyName;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private SecretKey secretKey;

    public SingleKeyResolver(String keyName, PublicKey publicKey) {
        this.keyName = keyName;
        this.publicKey = publicKey;
    }

    public SingleKeyResolver(String keyName, PrivateKey privateKey) {
        this.keyName = keyName;
        this.privateKey = privateKey;
    }

    public SingleKeyResolver(String keyName, SecretKey secretKey) {
        this.keyName = keyName;
        this.secretKey = secretKey;
    }

    public boolean engineCanResolve(Element element, String baseURI, StorageResolver storage) {
        return XMLUtils.elementIsInSignatureSpace(element, "KeyName");
    }

    public PublicKey engineLookupAndResolvePublicKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        String name;
        if (log.isDebugEnabled()) {
            log.debug("Can I resolve " + element.getTagName() + "?");
        }
        if (this.publicKey != null && XMLUtils.elementIsInSignatureSpace(element, "KeyName") && this.keyName.equals(name = element.getFirstChild().getNodeValue())) {
            return this.publicKey;
        }
        log.debug("I can't");
        return null;
    }

    public X509Certificate engineLookupResolveX509Certificate(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        return null;
    }

    public SecretKey engineResolveSecretKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        String name;
        if (log.isDebugEnabled()) {
            log.debug("Can I resolve " + element.getTagName() + "?");
        }
        if (this.secretKey != null && XMLUtils.elementIsInSignatureSpace(element, "KeyName") && this.keyName.equals(name = element.getFirstChild().getNodeValue())) {
            return this.secretKey;
        }
        log.debug("I can't");
        return null;
    }

    public PrivateKey engineLookupAndResolvePrivateKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        String name;
        if (log.isDebugEnabled()) {
            log.debug("Can I resolve " + element.getTagName() + "?");
        }
        if (this.privateKey != null && XMLUtils.elementIsInSignatureSpace(element, "KeyName") && this.keyName.equals(name = element.getFirstChild().getNodeValue())) {
            return this.privateKey;
        }
        log.debug("I can't");
        return null;
    }
}

