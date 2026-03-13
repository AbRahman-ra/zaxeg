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
import org.apache.xml.security.keys.content.keyvalues.DSAKeyValue;
import org.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import org.apache.xml.security.keys.storage.StorageResolver;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Element;

public class DSAKeyValueResolver
extends KeyResolverSpi {
    private static Log log = LogFactory.getLog(DSAKeyValueResolver.class);

    public PublicKey engineLookupAndResolvePublicKey(Element element, String BaseURI, StorageResolver storage) {
        if (element == null) {
            return null;
        }
        Element dsaKeyElement = null;
        boolean isKeyValue = XMLUtils.elementIsInSignatureSpace(element, "KeyValue");
        if (isKeyValue) {
            dsaKeyElement = XMLUtils.selectDsNode(element.getFirstChild(), "DSAKeyValue", 0);
        } else if (XMLUtils.elementIsInSignatureSpace(element, "DSAKeyValue")) {
            dsaKeyElement = element;
        }
        if (dsaKeyElement == null) {
            return null;
        }
        try {
            DSAKeyValue dsaKeyValue = new DSAKeyValue(dsaKeyElement, BaseURI);
            PublicKey pk = dsaKeyValue.getPublicKey();
            return pk;
        } catch (XMLSecurityException ex) {
            if (log.isDebugEnabled()) {
                log.debug(ex);
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

