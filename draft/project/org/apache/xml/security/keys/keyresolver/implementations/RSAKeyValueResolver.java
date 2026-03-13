/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.keyresolver.implementations;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import javax.crypto.SecretKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.keyvalues.RSAKeyValue;
import org.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import org.apache.xml.security.keys.storage.StorageResolver;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Element;

public class RSAKeyValueResolver
extends KeyResolverSpi {
    private static Log log = LogFactory.getLog(RSAKeyValueResolver.class);

    public PublicKey engineLookupAndResolvePublicKey(Element element, String BaseURI, StorageResolver storage) {
        if (log.isDebugEnabled()) {
            log.debug("Can I resolve " + element.getTagName());
        }
        if (element == null) {
            return null;
        }
        boolean isKeyValue = XMLUtils.elementIsInSignatureSpace(element, "KeyValue");
        Element rsaKeyElement = null;
        if (isKeyValue) {
            rsaKeyElement = XMLUtils.selectDsNode(element.getFirstChild(), "RSAKeyValue", 0);
        } else if (XMLUtils.elementIsInSignatureSpace(element, "RSAKeyValue")) {
            rsaKeyElement = element;
        }
        if (rsaKeyElement == null) {
            return null;
        }
        try {
            RSAKeyValue rsaKeyValue = new RSAKeyValue(rsaKeyElement, BaseURI);
            return rsaKeyValue.getPublicKey();
        } catch (XMLSecurityException ex) {
            if (log.isDebugEnabled()) {
                log.debug("XMLSecurityException", ex);
            }
            return null;
        }
    }

    public X509Certificate engineLookupResolveX509Certificate(Element element, String BaseURI, StorageResolver storage) {
        return null;
    }

    public SecretKey engineLookupAndResolveSecretKey(Element element, String BaseURI, StorageResolver storage) {
        return null;
    }
}

