/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.keyresolver.implementations;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import javax.crypto.SecretKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.content.x509.XMLX509Certificate;
import org.apache.xml.security.keys.content.x509.XMLX509IssuerSerial;
import org.apache.xml.security.keys.content.x509.XMLX509SKI;
import org.apache.xml.security.keys.content.x509.XMLX509SubjectName;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import org.apache.xml.security.keys.storage.StorageResolver;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Element;

public class PrivateKeyResolver
extends KeyResolverSpi {
    private static Log log = LogFactory.getLog(PrivateKeyResolver.class.getName());
    private KeyStore keyStore;
    private char[] password;

    public PrivateKeyResolver(KeyStore keyStore, char[] password) {
        this.keyStore = keyStore;
        this.password = password;
    }

    public boolean engineCanResolve(Element element, String BaseURI, StorageResolver storage) {
        return XMLUtils.elementIsInSignatureSpace(element, "X509Data") || XMLUtils.elementIsInSignatureSpace(element, "KeyName");
    }

    public PublicKey engineLookupAndResolvePublicKey(Element element, String BaseURI, StorageResolver storage) throws KeyResolverException {
        return null;
    }

    public X509Certificate engineLookupResolveX509Certificate(Element element, String BaseURI, StorageResolver storage) throws KeyResolverException {
        return null;
    }

    public SecretKey engineResolveSecretKey(Element element, String BaseURI, StorageResolver storage) throws KeyResolverException {
        return null;
    }

    public PrivateKey engineLookupAndResolvePrivateKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        if (log.isDebugEnabled()) {
            log.debug("Can I resolve " + element.getTagName() + "?");
        }
        if (XMLUtils.elementIsInSignatureSpace(element, "X509Data")) {
            PrivateKey privKey = this.resolveX509Data(element, baseURI);
            if (privKey != null) {
                return privKey;
            }
        } else if (XMLUtils.elementIsInSignatureSpace(element, "KeyName")) {
            log.debug("Can I resolve KeyName?");
            String keyName = element.getFirstChild().getNodeValue();
            try {
                Key key = this.keyStore.getKey(keyName, this.password);
                if (key instanceof PrivateKey) {
                    return (PrivateKey)key;
                }
            } catch (Exception e) {
                log.debug("Cannot recover the key", e);
            }
        }
        log.debug("I can't");
        return null;
    }

    private PrivateKey resolveX509Data(Element element, String baseURI) {
        log.debug("Can I resolve X509Data?");
        try {
            PrivateKey privKey;
            int i;
            X509Data x509Data = new X509Data(element, baseURI);
            int len = x509Data.lengthSKI();
            for (i = 0; i < len; ++i) {
                XMLX509SKI x509SKI = x509Data.itemSKI(i);
                privKey = this.resolveX509SKI(x509SKI);
                if (privKey == null) continue;
                return privKey;
            }
            len = x509Data.lengthIssuerSerial();
            for (i = 0; i < len; ++i) {
                XMLX509IssuerSerial x509Serial = x509Data.itemIssuerSerial(i);
                privKey = this.resolveX509IssuerSerial(x509Serial);
                if (privKey == null) continue;
                return privKey;
            }
            len = x509Data.lengthSubjectName();
            for (i = 0; i < len; ++i) {
                XMLX509SubjectName x509SubjectName = x509Data.itemSubjectName(i);
                privKey = this.resolveX509SubjectName(x509SubjectName);
                if (privKey == null) continue;
                return privKey;
            }
            len = x509Data.lengthCertificate();
            for (i = 0; i < len; ++i) {
                XMLX509Certificate x509Cert = x509Data.itemCertificate(i);
                privKey = this.resolveX509Certificate(x509Cert);
                if (privKey == null) continue;
                return privKey;
            }
        } catch (XMLSecurityException e) {
            log.debug("XMLSecurityException", e);
        } catch (KeyStoreException e) {
            log.debug("KeyStoreException", e);
        }
        return null;
    }

    private PrivateKey resolveX509SKI(XMLX509SKI x509SKI) throws XMLSecurityException, KeyStoreException {
        log.debug("Can I resolve X509SKI?");
        Enumeration<String> aliases = this.keyStore.aliases();
        while (aliases.hasMoreElements()) {
            XMLX509SKI certSKI;
            Certificate cert;
            String alias = aliases.nextElement();
            if (!this.keyStore.isKeyEntry(alias) || !((cert = this.keyStore.getCertificate(alias)) instanceof X509Certificate) || !(certSKI = new XMLX509SKI(x509SKI.getDocument(), (X509Certificate)cert)).equals(x509SKI)) continue;
            log.debug("match !!! ");
            try {
                Key key = this.keyStore.getKey(alias, this.password);
                if (!(key instanceof PrivateKey)) continue;
                return (PrivateKey)key;
            } catch (Exception e) {
                log.debug("Cannot recover the key", e);
            }
        }
        return null;
    }

    private PrivateKey resolveX509IssuerSerial(XMLX509IssuerSerial x509Serial) throws KeyStoreException {
        log.debug("Can I resolve X509IssuerSerial?");
        Enumeration<String> aliases = this.keyStore.aliases();
        while (aliases.hasMoreElements()) {
            XMLX509IssuerSerial certSerial;
            Certificate cert;
            String alias = aliases.nextElement();
            if (!this.keyStore.isKeyEntry(alias) || !((cert = this.keyStore.getCertificate(alias)) instanceof X509Certificate) || !(certSerial = new XMLX509IssuerSerial(x509Serial.getDocument(), (X509Certificate)cert)).equals(x509Serial)) continue;
            log.debug("match !!! ");
            try {
                Key key = this.keyStore.getKey(alias, this.password);
                if (!(key instanceof PrivateKey)) continue;
                return (PrivateKey)key;
            } catch (Exception e) {
                log.debug("Cannot recover the key", e);
            }
        }
        return null;
    }

    private PrivateKey resolveX509SubjectName(XMLX509SubjectName x509SubjectName) throws KeyStoreException {
        log.debug("Can I resolve X509SubjectName?");
        Enumeration<String> aliases = this.keyStore.aliases();
        while (aliases.hasMoreElements()) {
            XMLX509SubjectName certSN;
            Certificate cert;
            String alias = aliases.nextElement();
            if (!this.keyStore.isKeyEntry(alias) || !((cert = this.keyStore.getCertificate(alias)) instanceof X509Certificate) || !(certSN = new XMLX509SubjectName(x509SubjectName.getDocument(), (X509Certificate)cert)).equals(x509SubjectName)) continue;
            log.debug("match !!! ");
            try {
                Key key = this.keyStore.getKey(alias, this.password);
                if (!(key instanceof PrivateKey)) continue;
                return (PrivateKey)key;
            } catch (Exception e) {
                log.debug("Cannot recover the key", e);
            }
        }
        return null;
    }

    private PrivateKey resolveX509Certificate(XMLX509Certificate x509Cert) throws XMLSecurityException, KeyStoreException {
        log.debug("Can I resolve X509Certificate?");
        byte[] x509CertBytes = x509Cert.getCertificateBytes();
        Enumeration<String> aliases = this.keyStore.aliases();
        while (aliases.hasMoreElements()) {
            Certificate cert;
            String alias = aliases.nextElement();
            if (!this.keyStore.isKeyEntry(alias) || !((cert = this.keyStore.getCertificate(alias)) instanceof X509Certificate)) continue;
            byte[] certBytes = null;
            try {
                certBytes = cert.getEncoded();
            } catch (CertificateEncodingException e1) {
                // empty catch block
            }
            if (certBytes == null || !Arrays.equals(certBytes, x509CertBytes)) continue;
            log.debug("match !!! ");
            try {
                Key key = this.keyStore.getKey(alias, this.password);
                if (!(key instanceof PrivateKey)) continue;
                return (PrivateKey)key;
            } catch (Exception e) {
                log.debug("Cannot recover the key", e);
            }
        }
        return null;
    }
}

