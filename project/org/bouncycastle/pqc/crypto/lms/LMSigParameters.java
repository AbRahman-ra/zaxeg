/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;

public class LMSigParameters {
    public static final LMSigParameters lms_sha256_n32_h5 = new LMSigParameters(5, 32, 5, NISTObjectIdentifiers.id_sha256);
    public static final LMSigParameters lms_sha256_n32_h10 = new LMSigParameters(6, 32, 10, NISTObjectIdentifiers.id_sha256);
    public static final LMSigParameters lms_sha256_n32_h15 = new LMSigParameters(7, 32, 15, NISTObjectIdentifiers.id_sha256);
    public static final LMSigParameters lms_sha256_n32_h20 = new LMSigParameters(8, 32, 20, NISTObjectIdentifiers.id_sha256);
    public static final LMSigParameters lms_sha256_n32_h25 = new LMSigParameters(9, 32, 25, NISTObjectIdentifiers.id_sha256);
    private static Map<Object, LMSigParameters> paramBuilders = new HashMap<Object, LMSigParameters>(){
        {
            this.put(lms_sha256_n32_h5.type, lms_sha256_n32_h5);
            this.put(lms_sha256_n32_h10.type, lms_sha256_n32_h10);
            this.put(lms_sha256_n32_h15.type, lms_sha256_n32_h15);
            this.put(lms_sha256_n32_h20.type, lms_sha256_n32_h20);
            this.put(lms_sha256_n32_h25.type, lms_sha256_n32_h25);
        }
    };
    private final int type;
    private final int m;
    private final int h;
    private final ASN1ObjectIdentifier digestOid;

    protected LMSigParameters(int n, int n2, int n3, ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.type = n;
        this.m = n2;
        this.h = n3;
        this.digestOid = aSN1ObjectIdentifier;
    }

    public int getType() {
        return this.type;
    }

    public int getH() {
        return this.h;
    }

    public int getM() {
        return this.m;
    }

    public ASN1ObjectIdentifier getDigestOID() {
        return this.digestOid;
    }

    static LMSigParameters getParametersForType(int n) {
        return paramBuilders.get(n);
    }
}

