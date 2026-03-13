/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.content.x509;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.x509.XMLX509DataContent;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLX509Certificate
extends SignatureElementProxy
implements XMLX509DataContent {
    public static final String JCA_CERT_ID = "X.509";

    public XMLX509Certificate(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
    }

    public XMLX509Certificate(Document doc, byte[] certificateBytes) {
        super(doc);
        this.addBase64Text(certificateBytes);
    }

    public XMLX509Certificate(Document doc, X509Certificate x509certificate) throws XMLSecurityException {
        super(doc);
        try {
            this.addBase64Text(x509certificate.getEncoded());
        } catch (CertificateEncodingException ex) {
            throw new XMLSecurityException("empty", ex);
        }
    }

    public byte[] getCertificateBytes() throws XMLSecurityException {
        return this.getBytesFromTextChild();
    }

    public X509Certificate getX509Certificate() throws XMLSecurityException {
        try {
            byte[] certbytes = this.getCertificateBytes();
            CertificateFactory certFact = CertificateFactory.getInstance(JCA_CERT_ID);
            X509Certificate cert = (X509Certificate)certFact.generateCertificate(new ByteArrayInputStream(certbytes));
            if (cert != null) {
                return cert;
            }
            return null;
        } catch (CertificateException ex) {
            throw new XMLSecurityException("empty", ex);
        }
    }

    public PublicKey getPublicKey() throws XMLSecurityException {
        X509Certificate cert = this.getX509Certificate();
        if (cert != null) {
            return cert.getPublicKey();
        }
        return null;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof XMLX509Certificate)) {
            return false;
        }
        XMLX509Certificate other = (XMLX509Certificate)obj;
        try {
            return Arrays.equals(other.getCertificateBytes(), this.getCertificateBytes());
        } catch (XMLSecurityException ex) {
            return false;
        }
    }

    public int hashCode() {
        int result;
        block3: {
            result = 17;
            try {
                byte[] bytes = this.getCertificateBytes();
                for (int i = 0; i < bytes.length; ++i) {
                    result = 31 * result + bytes[i];
                }
            } catch (XMLSecurityException e) {
                if (!log.isDebugEnabled()) break block3;
                log.debug(e);
            }
        }
        return result;
    }

    public String getBaseLocalName() {
        return "X509Certificate";
    }
}

