/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cert.jcajce;

import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameStyle;
import org.bouncycastle.jcajce.interfaces.BCX509Certificate;

public class JcaX500NameUtil {
    public static X500Name getIssuer(X509Certificate x509Certificate) {
        if (x509Certificate instanceof BCX509Certificate) {
            return JcaX500NameUtil.notNull(((BCX509Certificate)((Object)x509Certificate)).getIssuerX500Name());
        }
        return JcaX500NameUtil.getX500Name(x509Certificate.getIssuerX500Principal());
    }

    public static X500Name getIssuer(X500NameStyle x500NameStyle, X509Certificate x509Certificate) {
        if (x509Certificate instanceof BCX509Certificate) {
            return X500Name.getInstance(x500NameStyle, JcaX500NameUtil.notNull(((BCX509Certificate)((Object)x509Certificate)).getIssuerX500Name()));
        }
        return JcaX500NameUtil.getX500Name(x500NameStyle, x509Certificate.getIssuerX500Principal());
    }

    public static X500Name getSubject(X509Certificate x509Certificate) {
        if (x509Certificate instanceof BCX509Certificate) {
            return JcaX500NameUtil.notNull(((BCX509Certificate)((Object)x509Certificate)).getSubjectX500Name());
        }
        return JcaX500NameUtil.getX500Name(x509Certificate.getSubjectX500Principal());
    }

    public static X500Name getSubject(X500NameStyle x500NameStyle, X509Certificate x509Certificate) {
        if (x509Certificate instanceof BCX509Certificate) {
            return X500Name.getInstance(x500NameStyle, JcaX500NameUtil.notNull(((BCX509Certificate)((Object)x509Certificate)).getSubjectX500Name()));
        }
        return JcaX500NameUtil.getX500Name(x500NameStyle, x509Certificate.getSubjectX500Principal());
    }

    public static X500Name getX500Name(X500Principal x500Principal) {
        return X500Name.getInstance(JcaX500NameUtil.getEncoded(x500Principal));
    }

    public static X500Name getX500Name(X500NameStyle x500NameStyle, X500Principal x500Principal) {
        return X500Name.getInstance(x500NameStyle, JcaX500NameUtil.getEncoded(x500Principal));
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

    private static byte[] getEncoded(X500Principal x500Principal) {
        return JcaX500NameUtil.notNull(x500Principal).getEncoded();
    }
}

