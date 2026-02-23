/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime.smime;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.util.Strings;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class SMimeUtils {
    private static final Map RFC5751_MICALGS;
    private static final Map RFC3851_MICALGS;
    private static final Map STANDARD_MICALGS;
    private static final Map forMic;
    private static final byte[] nl;

    SMimeUtils() {
    }

    static String lessQuotes(String string) {
        if (string == null || string.length() <= 1) {
            return string;
        }
        if (string.charAt(0) == '\"' && string.charAt(string.length() - 1) == '\"') {
            return string.substring(1, string.length() - 1);
        }
        return string;
    }

    static String getParameter(String string, List<String> list) {
        for (String string2 : list) {
            if (!string2.startsWith(string)) continue;
            return string2;
        }
        return null;
    }

    static ASN1ObjectIdentifier getDigestOID(String string) {
        ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier)forMic.get(Strings.toLowerCase(string));
        if (aSN1ObjectIdentifier == null) {
            throw new IllegalArgumentException("unknown micalg passed: " + string);
        }
        return aSN1ObjectIdentifier;
    }

    static InputStream autoBuffer(InputStream inputStream) {
        if (inputStream instanceof FileInputStream) {
            return new BufferedInputStream(inputStream);
        }
        return inputStream;
    }

    static OutputStream autoBuffer(OutputStream outputStream) {
        if (outputStream instanceof FileOutputStream) {
            return new BufferedOutputStream(outputStream);
        }
        return outputStream;
    }

    static OutputStream createUnclosable(OutputStream outputStream) {
        return new FilterOutputStream(outputStream){

            public void write(byte[] byArray, int n, int n2) throws IOException {
                if (byArray == null) {
                    throw new NullPointerException();
                }
                if ((n | n2 | byArray.length - (n2 + n) | n + n2) < 0) {
                    throw new IndexOutOfBoundsException();
                }
                this.out.write(byArray, n, n2);
            }

            public void close() throws IOException {
            }
        };
    }

    static {
        nl = new byte[2];
        SMimeUtils.nl[0] = 13;
        SMimeUtils.nl[1] = 10;
        HashMap<ASN1ObjectIdentifier, String> hashMap = new HashMap<ASN1ObjectIdentifier, String>();
        hashMap.put(CMSAlgorithm.MD5, "md5");
        hashMap.put(CMSAlgorithm.SHA1, "sha-1");
        hashMap.put(CMSAlgorithm.SHA224, "sha-224");
        hashMap.put(CMSAlgorithm.SHA256, "sha-256");
        hashMap.put(CMSAlgorithm.SHA384, "sha-384");
        hashMap.put(CMSAlgorithm.SHA512, "sha-512");
        hashMap.put(CMSAlgorithm.GOST3411, "gostr3411-94");
        hashMap.put(CMSAlgorithm.GOST3411_2012_256, "gostr3411-2012-256");
        hashMap.put(CMSAlgorithm.GOST3411_2012_512, "gostr3411-2012-512");
        RFC5751_MICALGS = Collections.unmodifiableMap(hashMap);
        HashMap<ASN1ObjectIdentifier, String> hashMap2 = new HashMap<ASN1ObjectIdentifier, String>();
        hashMap2.put(CMSAlgorithm.MD5, "md5");
        hashMap2.put(CMSAlgorithm.SHA1, "sha1");
        hashMap2.put(CMSAlgorithm.SHA224, "sha224");
        hashMap2.put(CMSAlgorithm.SHA256, "sha256");
        hashMap2.put(CMSAlgorithm.SHA384, "sha384");
        hashMap2.put(CMSAlgorithm.SHA512, "sha512");
        hashMap2.put(CMSAlgorithm.GOST3411, "gostr3411-94");
        hashMap2.put(CMSAlgorithm.GOST3411_2012_256, "gostr3411-2012-256");
        hashMap2.put(CMSAlgorithm.GOST3411_2012_512, "gostr3411-2012-512");
        RFC3851_MICALGS = Collections.unmodifiableMap(hashMap2);
        STANDARD_MICALGS = RFC5751_MICALGS;
        TreeMap<String, ASN1ObjectIdentifier> treeMap = new TreeMap<String, ASN1ObjectIdentifier>(String.CASE_INSENSITIVE_ORDER);
        for (Object k : STANDARD_MICALGS.keySet()) {
            treeMap.put(STANDARD_MICALGS.get(k).toString(), (ASN1ObjectIdentifier)k);
        }
        for (Object k : RFC3851_MICALGS.keySet()) {
            treeMap.put(RFC3851_MICALGS.get(k).toString(), (ASN1ObjectIdentifier)k);
        }
        forMic = Collections.unmodifiableMap(treeMap);
    }
}

