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
import org.apache.xml.security.keys.content.x509.XMLX509Certificate;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import org.apache.xml.security.keys.storage.StorageResolver;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Element;

public class X509CertificateResolver
extends KeyResolverSpi {
    private static Log log = LogFactory.getLog(X509CertificateResolver.class);

    public PublicKey engineLookupAndResolvePublicKey(Element element, String BaseURI, StorageResolver storage) throws KeyResolverException {
        X509Certificate cert = this.engineLookupResolveX509Certificate(element, BaseURI, storage);
        if (cert != null) {
            return cert.getPublicKey();
        }
        return null;
    }

    public X509Certificate engineLookupResolveX509Certificate(Element element, String BaseURI, StorageResolver storage) throws KeyResolverException {
        try {
            Element[] els = XMLUtils.selectDsNodes(element.getFirstChild(), "X509Certificate");
            if (els == null || els.length == 0) {
                Element el = XMLUtils.selectDsNode(element.getFirstChild(), "X509Data", 0);
                if (el != null) {
                    return this.engineLookupResolveX509Certificate(el, BaseURI, storage);
                }
                return null;
            }
            for (int i = 0; i < els.length; ++i) {
                XMLX509Certificate xmlCert = new XMLX509Certificate(els[i], BaseURI);
                X509Certificate cert = xmlCert.getX509Certificate();
                if (cert == null) continue;
                return cert;
            }
            return null;
        } catch (XMLSecurityException ex) {
            if (log.isDebugEnabled()) {
                log.debug("XMLSecurityException", ex);
            }
            throw new KeyResolverException("generic.EmptyMessage", ex);
        }
    }

    public SecretKey engineLookupAndResolveSecretKey(Element element, String BaseURI, StorageResolver storage) {
        return null;
    }
}

