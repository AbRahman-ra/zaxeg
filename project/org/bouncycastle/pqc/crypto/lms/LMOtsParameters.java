/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;

public class LMOtsParameters {
    public static final int reserved = 0;
    public static final LMOtsParameters sha256_n32_w1 = new LMOtsParameters(1, 32, 1, 265, 7, 8516, NISTObjectIdentifiers.id_sha256);
    public static final LMOtsParameters sha256_n32_w2 = new LMOtsParameters(2, 32, 2, 133, 6, 4292, NISTObjectIdentifiers.id_sha256);
    public static final LMOtsParameters sha256_n32_w4 = new LMOtsParameters(3, 32, 4, 67, 4, 2180, NISTObjectIdentifiers.id_sha256);
    public static final LMOtsParameters sha256_n32_w8 = new LMOtsParameters(4, 32, 8, 34, 0, 1124, NISTObjectIdentifiers.id_sha256);
    private static final Map<Object, LMOtsParameters> suppliers = new HashMap<Object, LMOtsParameters>(){
        {
            this.put(sha256_n32_w1.type, sha256_n32_w1);
            this.put(sha256_n32_w2.type, sha256_n32_w2);
            this.put(sha256_n32_w4.type, sha256_n32_w4);
            this.put(sha256_n32_w8.type, sha256_n32_w8);
        }
    };
    private final int type;
    private final int n;
    private final int w;
    private final int p;
    private final int ls;
    private final int sigLen;
    private final ASN1ObjectIdentifier digestOID;

    protected LMOtsParameters(int n, int n2, int n3, int n4, int n5, int n6, ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.type = n;
        this.n = n2;
        this.w = n3;
        this.p = n4;
        this.ls = n5;
        this.sigLen = n6;
        this.digestOID = aSN1ObjectIdentifier;
    }

    public int getType() {
        return this.type;
    }

    public int getN() {
        return this.n;
    }

    public int getW() {
        return this.w;
    }

    public int getP() {
        return this.p;
    }

    public int getLs() {
        return this.ls;
    }

    public int getSigLen() {
        return this.sigLen;
    }

    public ASN1ObjectIdentifier getDigestOID() {
        return this.digestOID;
    }

    public static LMOtsParameters getParametersForType(int n) {
        return suppliers.get(n);
    }
}

