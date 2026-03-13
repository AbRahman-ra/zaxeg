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
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.DEREncodedKeyValue;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import org.apache.xml.security.keys.storage.StorageResolver;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Element;

public class DEREncodedKeyValueResolver
extends KeyResolverSpi {
    private static Log log = LogFactory.getLog(DEREncodedKeyValueResolver.class);

    public boolean engineCanResolve(Element element, String baseURI, StorageResolver storage) {
        return XMLUtils.elementIsInSignature11Space(element, "DEREncodedKeyValue");
    }

    public PublicKey engineLookupAndResolvePublicKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        if (log.isDebugEnabled()) {
            log.debug("Can I resolve " + element.getTagName());
        }
        if (!this.engineCanResolve(element, baseURI, storage)) {
            return null;
        }
        try {
            DEREncodedKeyValue derKeyValue = new DEREncodedKeyValue(element, baseURI);
            return derKeyValue.getPublicKey();
        } catch (XMLSecurityException e) {
            if (log.isDebugEnabled()) {
                log.debug("XMLSecurityException", e);
            }
            return null;
        }
    }

    public X509Certificate engineLookupResolveX509Certificate(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        return null;
    }

    public SecretKey engineLookupAndResolveSecretKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        return null;
    }

    public PrivateKey engineLookupAndResolvePrivateKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        return null;
    }
}

