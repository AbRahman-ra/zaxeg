/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.keyresolver.implementations;

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import javax.crypto.SecretKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.content.x509.XMLX509IssuerSerial;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import org.apache.xml.security.keys.storage.StorageResolver;
import org.apache.xml.security.signature.XMLSignatureException;
import org.w3c.dom.Element;

public class X509IssuerSerialResolver
extends KeyResolverSpi {
    private static Log log = LogFactory.getLog(X509IssuerSerialResolver.class);

    public PublicKey engineLookupAndResolvePublicKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        X509Certificate cert = this.engineLookupResolveX509Certificate(element, baseURI, storage);
        if (cert != null) {
            return cert.getPublicKey();
        }
        return null;
    }

    public X509Certificate engineLookupResolveX509Certificate(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        if (log.isDebugEnabled()) {
            log.debug("Can I resolve " + element.getTagName() + "?");
        }
        X509Data x509data = null;
        try {
            x509data = new X509Data(element, baseURI);
        } catch (XMLSignatureException ex) {
            if (log.isDebugEnabled()) {
                log.debug("I can't");
            }
            return null;
        } catch (XMLSecurityException ex) {
            if (log.isDebugEnabled()) {
                log.debug("I can't");
            }
            return null;
        }
        if (!x509data.containsIssuerSerial()) {
            return null;
        }
        try {
            if (storage == null) {
                Object[] exArgs = new Object[]{"X509IssuerSerial"};
                KeyResolverException ex = new KeyResolverException("KeyResolver.needStorageResolver", exArgs);
                if (log.isDebugEnabled()) {
                    log.debug("", ex);
                }
                throw ex;
            }
            int noOfISS = x509data.lengthIssuerSerial();
            Iterator<Certificate> storageIterator = storage.getIterator();
            while (storageIterator.hasNext()) {
                X509Certificate cert = (X509Certificate)storageIterator.next();
                XMLX509IssuerSerial certSerial = new XMLX509IssuerSerial(element.getOwnerDocument(), cert);
                if (log.isDebugEnabled()) {
                    log.debug("Found Certificate Issuer: " + certSerial.getIssuerName());
                    log.debug("Found Certificate Serial: " + certSerial.getSerialNumber().toString());
                }
                for (int i = 0; i < noOfISS; ++i) {
                    XMLX509IssuerSerial xmliss = x509data.itemIssuerSerial(i);
                    if (log.isDebugEnabled()) {
                        log.debug("Found Element Issuer:     " + xmliss.getIssuerName());
                        log.debug("Found Element Serial:     " + xmliss.getSerialNumber().toString());
                    }
                    if (certSerial.equals(xmliss)) {
                        if (log.isDebugEnabled()) {
                            log.debug("match !!! ");
                        }
                        return cert;
                    }
                    if (!log.isDebugEnabled()) continue;
                    log.debug("no match...");
                }
            }
            return null;
        } catch (XMLSecurityException ex) {
            if (log.isDebugEnabled()) {
                log.debug("XMLSecurityException", ex);
            }
            throw new KeyResolverException("generic.EmptyMessage", ex);
        }
    }

    public SecretKey engineLookupAndResolveSecretKey(Element element, String baseURI, StorageResolver storage) {
        return null;
    }
}

