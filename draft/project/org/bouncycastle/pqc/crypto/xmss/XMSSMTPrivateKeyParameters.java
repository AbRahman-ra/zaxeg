/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.xmss;

import java.io.IOException;
import org.bouncycastle.pqc.crypto.xmss.BDSStateMap;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSStoreableObjectInterface;
import org.bouncycastle.pqc.crypto.xmss.XMSSUtil;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Encodable;

public final class XMSSMTPrivateKeyParameters
extends XMSSMTKeyParameters
implements XMSSStoreableObjectInterface,
Encodable {
    private final XMSSMTParameters params;
    private final byte[] secretKeySeed;
    private final byte[] secretKeyPRF;
    private final byte[] publicSeed;
    private final byte[] root;
    private volatile long index;
    private volatile BDSStateMap bdsState;
    private volatile boolean used;

    private XMSSMTPrivateKeyParameters(Builder builder) {
        super(true, builder.params.getTreeDigest());
        this.params = builder.params;
        if (this.params == null) {
            throw new NullPointerException("params == null");
        }
        int n = this.params.getTreeDigestSize();
        byte[] byArray = builder.privateKey;
        if (byArray != null) {
            if (builder.xmss == null) {
                throw new NullPointerException("xmss == null");
            }
            int n2 = this.params.getHeight();
            int n3 = (n2 + 7) / 8;
            int n4 = n;
            int n5 = n;
            int n6 = n;
            int n7 = n;
            int n8 = 0;
            this.index = XMSSUtil.bytesToXBigEndian(byArray, n8, n3);
            if (!XMSSUtil.isIndexValid(n2, this.index)) {
                throw new IllegalArgumentException("index out of bounds");
            }
            this.secretKeySeed = XMSSUtil.extractBytesAtOffset(byArray, n8 += n3, n4);
            this.secretKeyPRF = XMSSUtil.extractBytesAtOffset(byArray, n8 += n4, n5);
            this.publicSeed = XMSSUtil.extractBytesAtOffset(byArray, n8 += n5, n6);
            this.root = XMSSUtil.extractBytesAtOffset(byArray, n8 += n6, n7);
            byte[] byArray2 = XMSSUtil.extractBytesAtOffset(byArray, n8 += n7, byArray.length - n8);
            try {
                BDSStateMap bDSStateMap = (BDSStateMap)XMSSUtil.deserialize(byArray2, BDSStateMap.class);
                this.bdsState = bDSStateMap.withWOTSDigest(builder.xmss.getTreeDigestOID());
            } catch (IOException iOException) {
                throw new IllegalArgumentException(iOException.getMessage(), iOException);
            } catch (ClassNotFoundException classNotFoundException) {
                throw new IllegalArgumentException(classNotFoundException.getMessage(), classNotFoundException);
            }
        } else {
            this.index = builder.index;
            byte[] byArray3 = builder.secretKeySeed;
            if (byArray3 != null) {
                if (byArray3.length != n) {
                    throw new IllegalArgumentException("size of secretKeySeed needs to be equal size of digest");
                }
                this.secretKeySeed = byArray3;
            } else {
                this.secretKeySeed = new byte[n];
            }
            byte[] byArray4 = builder.secretKeyPRF;
            if (byArray4 != null) {
                if (byArray4.length != n) {
                    throw new IllegalArgumentException("size of secretKeyPRF needs to be equal size of digest");
                }
                this.secretKeyPRF = byArray4;
            } else {
                this.secretKeyPRF = new byte[n];
            }
            byte[] byArray5 = builder.publicSeed;
            if (byArray5 != null) {
                if (byArray5.length != n) {
                    throw new IllegalArgumentException("size of publicSeed needs to be equal size of digest");
                }
                this.publicSeed = byArray5;
            } else {
                this.publicSeed = new byte[n];
            }
            byte[] byArray6 = builder.root;
            if (byArray6 != null) {
                if (byArray6.length != n) {
                    throw new IllegalArgumentException("size of root needs to be equal size of digest");
                }
                this.root = byArray6;
            } else {
                this.root = new byte[n];
            }
            BDSStateMap bDSStateMap = builder.bdsState;
            if (bDSStateMap != null) {
                this.bdsState = bDSStateMap;
            } else {
                long l = builder.index;
                int n9 = this.params.getHeight();
                this.bdsState = XMSSUtil.isIndexValid(n9, l) && byArray5 != null && byArray3 != null ? new BDSStateMap(this.params, builder.index, byArray5, byArray3) : new BDSStateMap(builder.maxIndex + 1L);
            }
            if (builder.maxIndex >= 0L && builder.maxIndex != this.bdsState.getMaxIndex()) {
                throw new IllegalArgumentException("maxIndex set but not reflected in state");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public byte[] getEncoded() throws IOException {
        XMSSMTPrivateKeyParameters xMSSMTPrivateKeyParameters = this;
        synchronized (xMSSMTPrivateKeyParameters) {
            return this.toByteArray();
        }
    }

    public byte[] toByteArray() {
        XMSSMTPrivateKeyParameters xMSSMTPrivateKeyParameters = this;
        synchronized (xMSSMTPrivateKeyParameters) {
            int n = this.params.getTreeDigestSize();
            int n2 = (this.params.getHeight() + 7) / 8;
            int n3 = n;
            int n4 = n;
            int n5 = n;
            int n6 = n;
            int n7 = n2 + n3 + n4 + n5 + n6;
            byte[] byArray = new byte[n7];
            int n8 = 0;
            byte[] byArray2 = XMSSUtil.toBytesBigEndian(this.index, n2);
            XMSSUtil.copyBytesAtOffset(byArray, byArray2, n8);
            XMSSUtil.copyBytesAtOffset(byArray, this.secretKeySeed, n8 += n2);
            XMSSUtil.copyBytesAtOffset(byArray, this.secretKeyPRF, n8 += n3);
            XMSSUtil.copyBytesAtOffset(byArray, this.publicSeed, n8 += n4);
            XMSSUtil.copyBytesAtOffset(byArray, this.root, n8 += n5);
            try {
                return Arrays.concatenate(byArray, XMSSUtil.serialize(this.bdsState));
            } catch (IOException iOException) {
                throw new IllegalStateException("error serializing bds state: " + iOException.getMessage(), iOException);
            }
        }
    }

    public long getIndex() {
        return this.index;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long getUsagesRemaining() {
        XMSSMTPrivateKeyParameters xMSSMTPrivateKeyParameters = this;
        synchronized (xMSSMTPrivateKeyParameters) {
            return this.bdsState.getMaxIndex() - this.getIndex() + 1L;
        }
    }

    public byte[] getSecretKeySeed() {
        return XMSSUtil.cloneArray(this.secretKeySeed);
    }

    public byte[] getSecretKeyPRF() {
        return XMSSUtil.cloneArray(this.secretKeyPRF);
    }

    public byte[] getPublicSeed() {
        return XMSSUtil.cloneArray(this.publicSeed);
    }

    public byte[] getRoot() {
        return XMSSUtil.cloneArray(this.root);
    }

    BDSStateMap getBDSState() {
        return this.bdsState;
    }

    public XMSSMTParameters getParameters() {
        return this.params;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public XMSSMTPrivateKeyParameters getNextKey() {
        XMSSMTPrivateKeyParameters xMSSMTPrivateKeyParameters = this;
        synchronized (xMSSMTPrivateKeyParameters) {
            return this.extractKeyShard(1);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    XMSSMTPrivateKeyParameters rollKey() {
        XMSSMTPrivateKeyParameters xMSSMTPrivateKeyParameters = this;
        synchronized (xMSSMTPrivateKeyParameters) {
            if (this.getIndex() < this.bdsState.getMaxIndex()) {
                this.bdsState.updateState(this.params, this.index, this.publicSeed, this.secretKeySeed);
                ++this.index;
                this.used = false;
            } else {
                this.index = this.bdsState.getMaxIndex() + 1L;
                this.bdsState = new BDSStateMap(this.bdsState.getMaxIndex());
                this.used = false;
            }
            return this;
        }
    }

    public XMSSMTPrivateKeyParameters extractKeyShard(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("cannot ask for a shard with 0 keys");
        }
        XMSSMTPrivateKeyParameters xMSSMTPrivateKeyParameters = this;
        synchronized (xMSSMTPrivateKeyParameters) {
            if ((long)n <= this.getUsagesRemaining()) {
                XMSSMTPrivateKeyParameters xMSSMTPrivateKeyParameters2 = new Builder(this.params).withSecretKeySeed(this.secretKeySeed).withSecretKeyPRF(this.secretKeyPRF).withPublicSeed(this.publicSeed).withRoot(this.root).withIndex(this.getIndex()).withBDSState(new BDSStateMap(this.bdsState, this.getIndex() + (long)n - 1L)).build();
                for (int i = 0; i != n; ++i) {
                    this.rollKey();
                }
                return xMSSMTPrivateKeyParameters2;
            }
            throw new IllegalArgumentException("usageCount exceeds usages remaining");
        }
    }

    public static class Builder {
        private final XMSSMTParameters params;
        private long index = 0L;
        private long maxIndex = -1L;
        private byte[] secretKeySeed = null;
        private byte[] secretKeyPRF = null;
        private byte[] publicSeed = null;
        private byte[] root = null;
        private BDSStateMap bdsState = null;
        private byte[] privateKey = null;
        private XMSSParameters xmss = null;

        public Builder(XMSSMTParameters xMSSMTParameters) {
            this.params = xMSSMTParameters;
        }

        public Builder withIndex(long l) {
            this.index = l;
            return this;
        }

        public Builder withMaxIndex(long l) {
            this.maxIndex = l;
            return this;
        }

        public Builder withSecretKeySeed(byte[] byArray) {
            this.secretKeySeed = XMSSUtil.cloneArray(byArray);
            return this;
        }

        public Builder withSecretKeyPRF(byte[] byArray) {
            this.secretKeyPRF = XMSSUtil.cloneArray(byArray);
            return this;
        }

        public Builder withPublicSeed(byte[] byArray) {
            this.publicSeed = XMSSUtil.cloneArray(byArray);
            return this;
        }

        public Builder withRoot(byte[] byArray) {
            this.root = XMSSUtil.cloneArray(byArray);
            return this;
        }

        public Builder withBDSState(BDSStateMap bDSStateMap) {
            this.bdsState = bDSStateMap.getMaxIndex() == 0L ? new BDSStateMap(bDSStateMap, (1L << this.params.getHeight()) - 1L) : bDSStateMap;
            return this;
        }

        public Builder withPrivateKey(byte[] byArray) {
            this.privateKey = XMSSUtil.cloneArray(byArray);
            this.xmss = this.params.getXMSSParameters();
            return this;
        }

        public XMSSMTPrivateKeyParameters build() {
            return new XMSSMTPrivateKeyParameters(this);
        }
    }
}

