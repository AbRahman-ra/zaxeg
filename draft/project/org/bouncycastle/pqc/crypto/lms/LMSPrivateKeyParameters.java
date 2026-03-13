/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.pqc.crypto.ExhaustedPrivateKeyException;
import org.bouncycastle.pqc.crypto.lms.Composer;
import org.bouncycastle.pqc.crypto.lms.DigestUtil;
import org.bouncycastle.pqc.crypto.lms.LMOtsParameters;
import org.bouncycastle.pqc.crypto.lms.LMOtsPrivateKey;
import org.bouncycastle.pqc.crypto.lms.LMS;
import org.bouncycastle.pqc.crypto.lms.LMSContext;
import org.bouncycastle.pqc.crypto.lms.LMSContextBasedSigner;
import org.bouncycastle.pqc.crypto.lms.LMSKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSigParameters;
import org.bouncycastle.pqc.crypto.lms.LM_OTS;
import org.bouncycastle.pqc.crypto.lms.LmsUtils;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public class LMSPrivateKeyParameters
extends LMSKeyParameters
implements LMSContextBasedSigner {
    private static CacheKey T1 = new CacheKey(1);
    private static CacheKey[] internedKeys = new CacheKey[129];
    private final byte[] I;
    private final LMSigParameters parameters;
    private final LMOtsParameters otsParameters;
    private final int maxQ;
    private final byte[] masterSecret;
    private final Map<CacheKey, byte[]> tCache;
    private final int maxCacheR;
    private final Digest tDigest;
    private int q;
    private LMSPublicKeyParameters publicKey;

    public LMSPrivateKeyParameters(LMSigParameters lMSigParameters, LMOtsParameters lMOtsParameters, int n, byte[] byArray, int n2, byte[] byArray2) {
        super(true);
        this.parameters = lMSigParameters;
        this.otsParameters = lMOtsParameters;
        this.q = n;
        this.I = Arrays.clone(byArray);
        this.maxQ = n2;
        this.masterSecret = Arrays.clone(byArray2);
        this.maxCacheR = 1 << this.parameters.getH() + 1;
        this.tCache = new WeakHashMap<CacheKey, byte[]>();
        this.tDigest = DigestUtil.getDigest(lMSigParameters.getDigestOID());
    }

    private LMSPrivateKeyParameters(LMSPrivateKeyParameters lMSPrivateKeyParameters, int n, int n2) {
        super(true);
        this.parameters = lMSPrivateKeyParameters.parameters;
        this.otsParameters = lMSPrivateKeyParameters.otsParameters;
        this.q = n;
        this.I = lMSPrivateKeyParameters.I;
        this.maxQ = n2;
        this.masterSecret = lMSPrivateKeyParameters.masterSecret;
        this.maxCacheR = 1 << this.parameters.getH();
        this.tCache = lMSPrivateKeyParameters.tCache;
        this.tDigest = DigestUtil.getDigest(this.parameters.getDigestOID());
        this.publicKey = lMSPrivateKeyParameters.publicKey;
    }

    public static LMSPrivateKeyParameters getInstance(byte[] byArray, byte[] byArray2) throws IOException {
        LMSPrivateKeyParameters lMSPrivateKeyParameters = LMSPrivateKeyParameters.getInstance(byArray);
        lMSPrivateKeyParameters.publicKey = LMSPublicKeyParameters.getInstance(byArray2);
        return lMSPrivateKeyParameters;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static LMSPrivateKeyParameters getInstance(Object object) throws IOException {
        if (object instanceof LMSPrivateKeyParameters) {
            return (LMSPrivateKeyParameters)object;
        }
        if (object instanceof DataInputStream) {
            DataInputStream dataInputStream = (DataInputStream)object;
            if (dataInputStream.readInt() != 0) {
                throw new IllegalStateException("expected version 0 lms private key");
            }
            LMSigParameters lMSigParameters = LMSigParameters.getParametersForType(dataInputStream.readInt());
            LMOtsParameters lMOtsParameters = LMOtsParameters.getParametersForType(dataInputStream.readInt());
            byte[] byArray = new byte[16];
            dataInputStream.readFully(byArray);
            int n = dataInputStream.readInt();
            int n2 = dataInputStream.readInt();
            int n3 = dataInputStream.readInt();
            if (n3 < 0) {
                throw new IllegalStateException("secret length less than zero");
            }
            if (n3 > dataInputStream.available()) {
                throw new IOException("secret length exceeded " + dataInputStream.available());
            }
            byte[] byArray2 = new byte[n3];
            dataInputStream.readFully(byArray2);
            return new LMSPrivateKeyParameters(lMSigParameters, lMOtsParameters, n, byArray, n2, byArray2);
        }
        if (object instanceof byte[]) {
            InputStream inputStream = null;
            try {
                inputStream = new DataInputStream(new ByteArrayInputStream((byte[])object));
                LMSPrivateKeyParameters lMSPrivateKeyParameters = LMSPrivateKeyParameters.getInstance(inputStream);
                return lMSPrivateKeyParameters;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
        if (object instanceof InputStream) {
            return LMSPrivateKeyParameters.getInstance(Streams.readAll((InputStream)object));
        }
        throw new IllegalArgumentException("cannot parse " + object);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    LMOtsPrivateKey getCurrentOTSKey() {
        LMSPrivateKeyParameters lMSPrivateKeyParameters = this;
        synchronized (lMSPrivateKeyParameters) {
            if (this.q >= this.maxQ) {
                throw new ExhaustedPrivateKeyException("ots private keys expired");
            }
            return new LMOtsPrivateKey(this.otsParameters, this.I, this.q, this.masterSecret);
        }
    }

    public synchronized int getIndex() {
        return this.q;
    }

    synchronized void incIndex() {
        ++this.q;
    }

    public LMSContext generateLMSContext() {
        LMSigParameters lMSigParameters = this.getSigParameters();
        int n = lMSigParameters.getH();
        int n2 = this.getIndex();
        LMOtsPrivateKey lMOtsPrivateKey = this.getNextOtsPrivateKey();
        int n3 = (1 << n) + n2;
        byte[][] byArrayArray = new byte[n][];
        for (int i = 0; i < n; ++i) {
            int n4 = n3 / (1 << i) ^ 1;
            byArrayArray[i] = this.findT(n4);
        }
        return lMOtsPrivateKey.getSignatureContext(this.getSigParameters(), byArrayArray);
    }

    public byte[] generateSignature(LMSContext lMSContext) {
        try {
            return LMS.generateSign(lMSContext).getEncoded();
        } catch (IOException iOException) {
            throw new IllegalStateException("unable to encode signature: " + iOException.getMessage(), iOException);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    LMOtsPrivateKey getNextOtsPrivateKey() {
        LMSPrivateKeyParameters lMSPrivateKeyParameters = this;
        synchronized (lMSPrivateKeyParameters) {
            if (this.q >= this.maxQ) {
                throw new ExhaustedPrivateKeyException("ots private key exhausted");
            }
            LMOtsPrivateKey lMOtsPrivateKey = new LMOtsPrivateKey(this.otsParameters, this.I, this.q, this.masterSecret);
            this.incIndex();
            return lMOtsPrivateKey;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public LMSPrivateKeyParameters extractKeyShard(int n) {
        LMSPrivateKeyParameters lMSPrivateKeyParameters = this;
        synchronized (lMSPrivateKeyParameters) {
            if (this.q + n >= this.maxQ) {
                throw new IllegalArgumentException("usageCount exceeds usages remaining");
            }
            LMSPrivateKeyParameters lMSPrivateKeyParameters2 = new LMSPrivateKeyParameters(this, this.q, this.q + n);
            this.q += n;
            return lMSPrivateKeyParameters2;
        }
    }

    public LMSigParameters getSigParameters() {
        return this.parameters;
    }

    public LMOtsParameters getOtsParameters() {
        return this.otsParameters;
    }

    public byte[] getI() {
        return Arrays.clone(this.I);
    }

    public byte[] getMasterSecret() {
        return Arrays.clone(this.masterSecret);
    }

    public long getUsagesRemaining() {
        return this.maxQ - this.q;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public LMSPublicKeyParameters getPublicKey() {
        LMSPrivateKeyParameters lMSPrivateKeyParameters = this;
        synchronized (lMSPrivateKeyParameters) {
            if (this.publicKey == null) {
                this.publicKey = new LMSPublicKeyParameters(this.parameters, this.otsParameters, this.findT(T1), this.I);
            }
            return this.publicKey;
        }
    }

    byte[] findT(int n) {
        if (n < this.maxCacheR) {
            return this.findT(n < internedKeys.length ? internedKeys[n] : new CacheKey(n));
        }
        return this.calcT(n);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private byte[] findT(CacheKey cacheKey) {
        Map<CacheKey, byte[]> map = this.tCache;
        synchronized (map) {
            byte[] byArray = this.tCache.get(cacheKey);
            if (byArray != null) {
                return byArray;
            }
            byArray = this.calcT(cacheKey.index);
            this.tCache.put(cacheKey, byArray);
            return byArray;
        }
    }

    private byte[] calcT(int n) {
        int n2 = this.getSigParameters().getH();
        int n3 = 1 << n2;
        if (n >= n3) {
            LmsUtils.byteArray(this.getI(), this.tDigest);
            LmsUtils.u32str(n, this.tDigest);
            LmsUtils.u16str((short)-32126, this.tDigest);
            byte[] byArray = LM_OTS.lms_ots_generatePublicKey(this.getOtsParameters(), this.getI(), n - n3, this.getMasterSecret());
            LmsUtils.byteArray(byArray, this.tDigest);
            byte[] byArray2 = new byte[this.tDigest.getDigestSize()];
            this.tDigest.doFinal(byArray2, 0);
            return byArray2;
        }
        byte[] byArray = this.findT(2 * n);
        byte[] byArray3 = this.findT(2 * n + 1);
        LmsUtils.byteArray(this.getI(), this.tDigest);
        LmsUtils.u32str(n, this.tDigest);
        LmsUtils.u16str((short)-31869, this.tDigest);
        LmsUtils.byteArray(byArray, this.tDigest);
        LmsUtils.byteArray(byArray3, this.tDigest);
        byte[] byArray4 = new byte[this.tDigest.getDigestSize()];
        this.tDigest.doFinal(byArray4, 0);
        return byArray4;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        LMSPrivateKeyParameters lMSPrivateKeyParameters = (LMSPrivateKeyParameters)object;
        if (this.q != lMSPrivateKeyParameters.q) {
            return false;
        }
        if (this.maxQ != lMSPrivateKeyParameters.maxQ) {
            return false;
        }
        if (!Arrays.areEqual(this.I, lMSPrivateKeyParameters.I)) {
            return false;
        }
        if (this.parameters != null ? !this.parameters.equals(lMSPrivateKeyParameters.parameters) : lMSPrivateKeyParameters.parameters != null) {
            return false;
        }
        if (this.otsParameters != null ? !this.otsParameters.equals(lMSPrivateKeyParameters.otsParameters) : lMSPrivateKeyParameters.otsParameters != null) {
            return false;
        }
        if (!Arrays.areEqual(this.masterSecret, lMSPrivateKeyParameters.masterSecret)) {
            return false;
        }
        if (this.publicKey != null && lMSPrivateKeyParameters.publicKey != null) {
            return this.publicKey.equals(lMSPrivateKeyParameters.publicKey);
        }
        return true;
    }

    public int hashCode() {
        int n = this.q;
        n = 31 * n + Arrays.hashCode(this.I);
        n = 31 * n + (this.parameters != null ? this.parameters.hashCode() : 0);
        n = 31 * n + (this.otsParameters != null ? this.otsParameters.hashCode() : 0);
        n = 31 * n + this.maxQ;
        n = 31 * n + Arrays.hashCode(this.masterSecret);
        n = 31 * n + (this.publicKey != null ? this.publicKey.hashCode() : 0);
        return n;
    }

    public byte[] getEncoded() throws IOException {
        return Composer.compose().u32str(0).u32str(this.parameters.getType()).u32str(this.otsParameters.getType()).bytes(this.I).u32str(this.q).u32str(this.maxQ).u32str(this.masterSecret.length).bytes(this.masterSecret).build();
    }

    static {
        LMSPrivateKeyParameters.internedKeys[1] = T1;
        for (int i = 2; i < internedKeys.length; ++i) {
            LMSPrivateKeyParameters.internedKeys[i] = new CacheKey(i);
        }
    }

    private static class CacheKey {
        private final int index;

        CacheKey(int n) {
            this.index = n;
        }

        public int hashCode() {
            return this.index;
        }

        public boolean equals(Object object) {
            if (object instanceof CacheKey) {
                return ((CacheKey)object).index == this.index;
            }
            return false;
        }
    }
}

