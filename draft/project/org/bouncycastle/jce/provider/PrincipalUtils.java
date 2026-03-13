/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jce.provider;

import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameStyle;
import org.bouncycastle.jcajce.interfaces.BCX509Certificate;
import org.bouncycastle.x509.X509AttributeCertificate;

class PrincipalUtils {
    PrincipalUtils() {
    }

    static X500Name getCA(TrustAnchor trustAnchor) {
        return PrincipalUtils.getX500Name(PrincipalUtils.notNull(trustAnchor).getCA());
    }

    static X500Name getEncodedIssuerPrincipal(Object object) {
        if (object instanceof X509Certificate) {
            return PrincipalUtils.getIssuerPrincipal((X509Certificate)object);
        }
        return PrincipalUtils.getX500Name((X500Principal)((X509AttributeCertificate)object).getIssuer().getPrincipals()[0]);
    }

    static X500Name getIssuerPrincipal(X509Certificate x509Certificate) {
        if (x509Certificate instanceof BCX509Certificate) {
            return PrincipalUtils.notNull(((BCX509Certificate)((Object)x509Certificate)).getIssuerX500Name());
        }
        return PrincipalUtils.getX500Name(PrincipalUtils.notNull(x509Certificate).getIssuerX500Principal());
    }

    static X500Name getIssuerPrincipal(X509CRL x509CRL) {
        return PrincipalUtils.getX500Name(PrincipalUtils.notNull(x509CRL).getIssuerX500Principal());
    }

    static X500Name getSubjectPrincipal(X509Certificate x509Certificate) {
        if (x509Certificate instanceof BCX509Certificate) {
            return PrincipalUtils.notNull(((BCX509Certificate)((Object)x509Certificate)).getSubjectX500Name());
        }
        return PrincipalUtils.getX500Name(PrincipalUtils.notNull(x509Certificate).getSubjectX500Principal());
    }

    static X500Name getX500Name(X500Principal x500Principal) {
        X500Name x500Name = X500Name.getInstance(PrincipalUtils.getEncoded(x500Principal));
        return PrincipalUtils.notNull(x500Name);
    }

    static X500Name getX500Name(X500NameStyle x500NameStyle, X500Principal x500Principal) {
        X500Name x500Name = X500Name.getInstance(x500NameStyle, PrincipalUtils.getEncoded(x500Principal));
        return PrincipalUtils.notNull(x500Name);
    }

    private static byte[] getEncoded(X500Principal x500Principal) {
        byte[] byArray = PrincipalUtils.notNull(x500Principal).getEncoded();
        return PrincipalUtils.notNull(byArray);
    }

    private static byte[] notNull(byte[] byArray) {
        if (null == byArray) {
            throw new IllegalStateException();
        }
        return byArray;
    }

    private static TrustAnchor notNull(TrustAnchor trustAnchor) {
        if (null == trustAnchor) {
            throw new IllegalStateException();
        }
        return trustAnchor;
    }

    private static X509Certificate notNull(X509Certificate x509Certificate) {
        if (null == x509Certificate) {
            throw new IllegalStateException();
        }
        return x509Certificate;
    }

    private static X509CRL notNull(X509CRL x509CRL) {
        if (null == x509CRL) {
            throw new IllegalStateException();
        }
        return x509CRL;
    }

    private static X500Name notNull(X500Name x500Name) {
        if (null == x500Name) {
            throw new IllegalStateException();
        }
        return x500Name;
    }

    private static X500Principal notNull(X500Principal x500Principal) {
        if (null == x500Principal) {
            throw new IllegalStateException();
        }
        return x500Principal;
    }
}

