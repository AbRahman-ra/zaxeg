/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.xmss;

import java.io.IOException;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSStoreableObjectInterface;
import org.bouncycastle.pqc.crypto.xmss.XMSSUtil;
import org.bouncycastle.util.Encodable;
import org.bouncycastle.util.Pack;

public final class XMSSMTPublicKeyParameters
extends XMSSMTKeyParameters
implements XMSSStoreableObjectInterface,
Encodable {
    private final XMSSMTParameters params;
    private final int oid;
    private final byte[] root;
    private final byte[] publicSeed;

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private XMSSMTPublicKeyParameters(Builder builder) {
        super(false, builder.params.getTreeDigest());
        this.params = builder.params;
        if (this.params == null) {
            throw new NullPointerException("params == null");
        }
        int n = this.params.getTreeDigestSize();
        byte[] byArray = builder.publicKey;
        if (byArray != null) {
            int n2 = 4;
            int n3 = n;
            int n4 = n;
            int n5 = 0;
            if (byArray.length == n3 + n4) {
                this.oid = 0;
                this.root = XMSSUtil.extractBytesAtOffset(byArray, n5, n3);
                this.publicSeed = XMSSUtil.extractBytesAtOffset(byArray, n5 += n3, n4);
                return;
            } else {
                if (byArray.length != n2 + n3 + n4) throw new IllegalArgumentException("public key has wrong size");
                this.oid = Pack.bigEndianToInt(byArray, 0);
                this.root = XMSSUtil.extractBytesAtOffset(byArray, n5 += n2, n3);
                this.publicSeed = XMSSUtil.extractBytesAtOffset(byArray, n5 += n3, n4);
            }
            return;
        } else {
            this.oid = this.params.getOid() != null ? this.params.getOid().getOid() : 0;
            byte[] byArray2 = builder.root;
            if (byArray2 != null) {
                if (byArray2.length != n) {
                    throw new IllegalArgumentException("length of root must be equal to length of digest");
                }
                this.root = byArray2;
            } else {
                this.root = new byte[n];
            }
            byte[] byArray3 = builder.publicSeed;
            if (byArray3 != null) {
                if (byArray3.length != n) {
                    throw new IllegalArgumentException("length of publicSeed must be equal to length of digest");
                }
                this.publicSeed = byArray3;
                return;
            } else {
                this.publicSeed = new byte[n];
            }
        }
    }

    public byte[] getEncoded() throws IOException {
        return this.toByteArray();
    }

    public byte[] toByteArray() {
        byte[] byArray;
        int n = this.params.getTreeDigestSize();
        int n2 = 4;
        int n3 = n;
        int n4 = n;
        int n5 = 0;
        if (this.oid != 0) {
            byArray = new byte[n2 + n3 + n4];
            Pack.intToBigEndian(this.oid, byArray, n5);
            n5 += n2;
        } else {
            byArray = new byte[n3 + n4];
        }
        XMSSUtil.copyBytesAtOffset(byArray, this.root, n5);
        XMSSUtil.copyBytesAtOffset(byArray, this.publicSeed, n5 += n3);
        return byArray;
    }

    public byte[] getRoot() {
        return XMSSUtil.cloneArray(this.root);
    }

    public byte[] getPublicSeed() {
        return XMSSUtil.cloneArray(this.publicSeed);
    }

    public XMSSMTParameters getParameters() {
        return this.params;
    }

    public static class Builder {
        private final XMSSMTParameters params;
        private byte[] root = null;
        private byte[] publicSeed = null;
        private byte[] publicKey = null;

        public Builder(XMSSMTParameters xMSSMTParameters) {
            this.params = xMSSMTParameters;
        }

        public Builder withRoot(byte[] byArray) {
            this.root = XMSSUtil.cloneArray(byArray);
            return this;
        }

        public Builder withPublicSeed(byte[] byArray) {
            this.publicSeed = XMSSUtil.cloneArray(byArray);
            return this;
        }

        public Builder withPublicKey(byte[] byArray) {
            this.publicKey = XMSSUtil.cloneArray(byArray);
            return this;
        }

        public XMSSMTPublicKeyParameters build() {
            return new XMSSMTPublicKeyParameters(this);
        }
    }
}

