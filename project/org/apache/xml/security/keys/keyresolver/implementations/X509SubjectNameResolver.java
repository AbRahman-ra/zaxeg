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
import org.apache.xml.security.keys.content.x509.XMLX509SubjectName;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import org.apache.xml.security.keys.storage.StorageResolver;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Element;

public class X509SubjectNameResolver
extends KeyResolverSpi {
    private static Log log = LogFactory.getLog(X509SubjectNameResolver.class);

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
        Element[] x509childNodes = null;
        XMLX509SubjectName[] x509childObject = null;
        if (!XMLUtils.elementIsInSignatureSpace(element, "X509Data")) {
            if (log.isDebugEnabled()) {
                log.debug("I can't");
            }
            return null;
        }
        x509childNodes = XMLUtils.selectDsNodes(element.getFirstChild(), "X509SubjectName");
        if (x509childNodes == null || x509childNodes.length <= 0) {
            if (log.isDebugEnabled()) {
                log.debug("I can't");
            }
            return null;
        }
        try {
            if (storage == null) {
                Object[] exArgs = new Object[]{"X509SubjectName"};
                KeyResolverException ex = new KeyResolverException("KeyResolver.needStorageResolver", exArgs);
                if (log.isDebugEnabled()) {
                    log.debug("", ex);
                }
                throw ex;
            }
            x509childObject = new XMLX509SubjectName[x509childNodes.length];
            for (int i = 0; i < x509childNodes.length; ++i) {
                x509childObject[i] = new XMLX509SubjectName(x509childNodes[i], baseURI);
            }
            Iterator<Certificate> storageIterator = storage.getIterator();
            while (storageIterator.hasNext()) {
                X509Certificate cert = (X509Certificate)storageIterator.next();
                XMLX509SubjectName certSN = new XMLX509SubjectName(element.getOwnerDocument(), cert);
                if (log.isDebugEnabled()) {
                    log.debug("Found Certificate SN: " + certSN.getSubjectName());
                }
                for (int i = 0; i < x509childObject.length; ++i) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found Element SN:     " + x509childObject[i].getSubjectName());
                    }
                    if (certSN.equals(x509childObject[i])) {
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

