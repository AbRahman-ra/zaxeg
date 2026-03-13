/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.xmss;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.pqc.crypto.xmss.DefaultXMSSMTOid;
import org.bouncycastle.pqc.crypto.xmss.DigestUtil;
import org.bouncycastle.pqc.crypto.xmss.WOTSPlus;
import org.bouncycastle.pqc.crypto.xmss.XMSSOid;
import org.bouncycastle.pqc.crypto.xmss.XMSSParameters;
import org.bouncycastle.util.Integers;

public final class XMSSMTParameters {
    private static final Map<Integer, XMSSMTParameters> paramsLookupTable;
    private final XMSSOid oid;
    private final XMSSParameters xmssParams;
    private final int height;
    private final int layers;

    public XMSSMTParameters(int n, int n2, Digest digest) {
        this(n, n2, DigestUtil.getDigestOID(digest.getAlgorithmName()));
    }

    public XMSSMTParameters(int n, int n2, ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.height = n;
        this.layers = n2;
        this.xmssParams = new XMSSParameters(XMSSMTParameters.xmssTreeHeight(n, n2), aSN1ObjectIdentifier);
        this.oid = DefaultXMSSMTOid.lookup(this.getTreeDigest(), this.getTreeDigestSize(), this.getWinternitzParameter(), this.getLen(), this.getHeight(), n2);
    }

    private static int xmssTreeHeight(int n, int n2) throws IllegalArgumentException {
        if (n < 2) {
            throw new IllegalArgumentException("totalHeight must be > 1");
        }
        if (n % n2 != 0) {
            throw new IllegalArgumentException("layers must divide totalHeight without remainder");
        }
        if (n / n2 == 1) {
            throw new IllegalArgumentException("height / layers must be greater than 1");
        }
        return n / n2;
    }

    public int getHeight() {
        return this.height;
    }

    public int getLayers() {
        return this.layers;
    }

    protected XMSSParameters getXMSSParameters() {
        return this.xmssParams;
    }

    protected WOTSPlus getWOTSPlus() {
        return this.xmssParams.getWOTSPlus();
    }

    protected String getTreeDigest() {
        return this.xmssParams.getTreeDigest();
    }

    public int getTreeDigestSize() {
        return this.xmssParams.getTreeDigestSize();
    }

    public ASN1ObjectIdentifier getTreeDigestOID() {
        return this.xmssParams.getTreeDigestOID();
    }

    int getWinternitzParameter() {
        return this.xmssParams.getWinternitzParameter();
    }

    protected int getLen() {
        return this.xmssParams.getLen();
    }

    protected XMSSOid getOid() {
        return this.oid;
    }

    public static XMSSMTParameters lookupByOID(int n) {
        return paramsLookupTable.get(Integers.valueOf(n));
    }

    static {
        HashMap<Integer, XMSSMTParameters> hashMap = new HashMap<Integer, XMSSMTParameters>();
        hashMap.put(Integers.valueOf(1), new XMSSMTParameters(20, 2, NISTObjectIdentifiers.id_sha256));
        hashMap.put(Integers.valueOf(2), new XMSSMTParameters(20, 4, NISTObjectIdentifiers.id_sha256));
        hashMap.put(Integers.valueOf(3), new XMSSMTParameters(40, 2, NISTObjectIdentifiers.id_sha256));
        hashMap.put(Integers.valueOf(4), new XMSSMTParameters(40, 4, NISTObjectIdentifiers.id_sha256));
        hashMap.put(Integers.valueOf(5), new XMSSMTParameters(40, 8, NISTObjectIdentifiers.id_sha256));
        hashMap.put(Integers.valueOf(6), new XMSSMTParameters(60, 3, NISTObjectIdentifiers.id_sha256));
        hashMap.put(Integers.valueOf(7), new XMSSMTParameters(60, 6, NISTObjectIdentifiers.id_sha256));
        hashMap.put(Integers.valueOf(8), new XMSSMTParameters(60, 12, NISTObjectIdentifiers.id_sha256));
        hashMap.put(Integers.valueOf(9), new XMSSMTParameters(20, 2, NISTObjectIdentifiers.id_sha512));
        hashMap.put(Integers.valueOf(10), new XMSSMTParameters(20, 4, NISTObjectIdentifiers.id_sha512));
        hashMap.put(Integers.valueOf(11), new XMSSMTParameters(40, 2, NISTObjectIdentifiers.id_sha512));
        hashMap.put(Integers.valueOf(12), new XMSSMTParameters(40, 4, NISTObjectIdentifiers.id_sha512));
        hashMap.put(Integers.valueOf(13), new XMSSMTParameters(40, 8, NISTObjectIdentifiers.id_sha512));
        hashMap.put(Integers.valueOf(14), new XMSSMTParameters(60, 3, NISTObjectIdentifiers.id_sha512));
        hashMap.put(Integers.valueOf(15), new XMSSMTParameters(60, 6, NISTObjectIdentifiers.id_sha512));
        hashMap.put(Integers.valueOf(16), new XMSSMTParameters(60, 12, NISTObjectIdentifiers.id_sha512));
        hashMap.put(Integers.valueOf(17), new XMSSMTParameters(20, 2, NISTObjectIdentifiers.id_shake128));
        hashMap.put(Integers.valueOf(18), new XMSSMTParameters(20, 4, NISTObjectIdentifiers.id_shake128));
        hashMap.put(Integers.valueOf(19), new XMSSMTParameters(40, 2, NISTObjectIdentifiers.id_shake128));
        hashMap.put(Integers.valueOf(20), new XMSSMTParameters(40, 4, NISTObjectIdentifiers.id_shake128));
        hashMap.put(Integers.valueOf(21), new XMSSMTParameters(40, 8, NISTObjectIdentifiers.id_shake128));
        hashMap.put(Integers.valueOf(22), new XMSSMTParameters(60, 3, NISTObjectIdentifiers.id_shake128));
        hashMap.put(Integers.valueOf(23), new XMSSMTParameters(60, 6, NISTObjectIdentifiers.id_shake128));
        hashMap.put(Integers.valueOf(24), new XMSSMTParameters(60, 12, NISTObjectIdentifiers.id_shake128));
        hashMap.put(Integers.valueOf(25), new XMSSMTParameters(20, 2, NISTObjectIdentifiers.id_shake256));
        hashMap.put(Integers.valueOf(26), new XMSSMTParameters(20, 4, NISTObjectIdentifiers.id_shake256));
        hashMap.put(Integers.valueOf(27), new XMSSMTParameters(40, 2, NISTObjectIdentifiers.id_shake256));
        hashMap.put(Integers.valueOf(28), new XMSSMTParameters(40, 4, NISTObjectIdentifiers.id_shake256));
        hashMap.put(Integers.valueOf(29), new XMSSMTParameters(40, 8, NISTObjectIdentifiers.id_shake256));
        hashMap.put(Integers.valueOf(30), new XMSSMTParameters(60, 3, NISTObjectIdentifiers.id_shake256));
        hashMap.put(Integers.valueOf(31), new XMSSMTParameters(60, 6, NISTObjectIdentifiers.id_shake256));
        hashMap.put(Integers.valueOf(32), new XMSSMTParameters(60, 12, NISTObjectIdentifiers.id_shake256));
        paramsLookupTable = Collections.unmodifiableMap(hashMap);
    }
}

