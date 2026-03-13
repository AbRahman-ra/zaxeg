/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.gmss.util;

import org.bouncycastle.crypto.Digest;

public class WinternitzOTSVerify {
    private Digest messDigestOTS;
    private int mdsize;
    private int w;

    public WinternitzOTSVerify(Digest digest, int n) {
        this.w = n;
        this.messDigestOTS = digest;
        this.mdsize = this.messDigestOTS.getDigestSize();
    }

    public int getSignatureLength() {
        int n = this.messDigestOTS.getDigestSize();
        int n2 = ((n << 3) + (this.w - 1)) / this.w;
        int n3 = this.getLog((n2 << this.w) + 1);
        return n * (n2 += (n3 + this.w - 1) / this.w);
    }

    public byte[] Verify(byte[] byArray, byte[] byArray2) {
        int n;
        byte[] byArray3 = new byte[this.mdsize];
        this.messDigestOTS.update(byArray, 0, byArray.length);
        this.messDigestOTS.doFinal(byArray3, 0);
        int n2 = ((this.mdsize << 3) + (this.w - 1)) / this.w;
        int n3 = this.getLog((n2 << this.w) + 1);
        int n4 = n2 + (n3 + this.w - 1) / this.w;
        int n5 = this.mdsize * n4;
        if (n5 != byArray2.length) {
            return null;
        }
        byte[] byArray4 = new byte[n5];
        int n6 = 0;
        int n7 = 0;
        if (8 % this.w == 0) {
            int n8;
            int n9;
            n = 8 / this.w;
            int n10 = (1 << this.w) - 1;
            for (n9 = 0; n9 < byArray3.length; ++n9) {
                for (int i = 0; i < n; ++i) {
                    n8 = byArray3[n9] & n10;
                    n6 += n8;
                    this.hashSignatureBlock(byArray2, n7 * this.mdsize, n10 - n8, byArray4, n7 * this.mdsize);
                    byArray3[n9] = (byte)(byArray3[n9] >>> this.w);
                    ++n7;
                }
            }
            n6 = (n2 << this.w) - n6;
            for (n9 = 0; n9 < n3; n9 += this.w) {
                n8 = n6 & n10;
                this.hashSignatureBlock(byArray2, n7 * this.mdsize, n10 - n8, byArray4, n7 * this.mdsize);
                n6 >>>= this.w;
                ++n7;
            }
        } else if (this.w < 8) {
            int n11;
            long l;
            int n12;
            n = this.mdsize / this.w;
            int n13 = (1 << this.w) - 1;
            int n14 = 0;
            for (n12 = 0; n12 < n; ++n12) {
                int n15;
                l = 0L;
                for (n15 = 0; n15 < this.w; ++n15) {
                    l ^= (long)((byArray3[n14] & 0xFF) << (n15 << 3));
                    ++n14;
                }
                for (n15 = 0; n15 < 8; ++n15) {
                    n11 = (int)(l & (long)n13);
                    n6 += n11;
                    this.hashSignatureBlock(byArray2, n7 * this.mdsize, n13 - n11, byArray4, n7 * this.mdsize);
                    l >>>= this.w;
                    ++n7;
                }
            }
            n = this.mdsize % this.w;
            l = 0L;
            for (n12 = 0; n12 < n; ++n12) {
                l ^= (long)((byArray3[n14] & 0xFF) << (n12 << 3));
                ++n14;
            }
            n <<= 3;
            for (n12 = 0; n12 < n; n12 += this.w) {
                n11 = (int)(l & (long)n13);
                n6 += n11;
                this.hashSignatureBlock(byArray2, n7 * this.mdsize, n13 - n11, byArray4, n7 * this.mdsize);
                l >>>= this.w;
                ++n7;
            }
            n6 = (n2 << this.w) - n6;
            for (n12 = 0; n12 < n3; n12 += this.w) {
                n11 = n6 & n13;
                this.hashSignatureBlock(byArray2, n7 * this.mdsize, n13 - n11, byArray4, n7 * this.mdsize);
                n6 >>>= this.w;
                ++n7;
            }
        } else if (this.w < 57) {
            long l;
            int n16;
            int n17;
            long l2;
            int n18;
            int n19;
            n = (this.mdsize << 3) - this.w;
            int n20 = (1 << this.w) - 1;
            byte[] byArray5 = new byte[this.mdsize];
            int n21 = 0;
            while (n21 <= n) {
                n19 = n21 >>> 3;
                n18 = n21 % 8;
                int n22 = (n21 += this.w) + 7 >>> 3;
                l2 = 0L;
                n17 = 0;
                for (n16 = n19; n16 < n22; ++n16) {
                    l2 ^= (long)((byArray3[n16] & 0xFF) << (n17 << 3));
                    ++n17;
                }
                n6 = (int)((long)n6 + l);
                System.arraycopy(byArray2, n7 * this.mdsize, byArray5, 0, this.mdsize);
                for (l = (l2 >>>= n18) & (long)n20; l < (long)n20; ++l) {
                    this.messDigestOTS.update(byArray5, 0, byArray5.length);
                    this.messDigestOTS.doFinal(byArray5, 0);
                }
                System.arraycopy(byArray5, 0, byArray4, n7 * this.mdsize, this.mdsize);
                ++n7;
            }
            n19 = n21 >>> 3;
            if (n19 < this.mdsize) {
                n18 = n21 % 8;
                l2 = 0L;
                n17 = 0;
                for (n16 = n19; n16 < this.mdsize; ++n16) {
                    l2 ^= (long)((byArray3[n16] & 0xFF) << (n17 << 3));
                    ++n17;
                }
                n6 = (int)((long)n6 + l);
                System.arraycopy(byArray2, n7 * this.mdsize, byArray5, 0, this.mdsize);
                for (l = (l2 >>>= n18) & (long)n20; l < (long)n20; ++l) {
                    this.messDigestOTS.update(byArray5, 0, byArray5.length);
                    this.messDigestOTS.doFinal(byArray5, 0);
                }
                System.arraycopy(byArray5, 0, byArray4, n7 * this.mdsize, this.mdsize);
                ++n7;
            }
            n6 = (n2 << this.w) - n6;
            for (n16 = 0; n16 < n3; n16 += this.w) {
                System.arraycopy(byArray2, n7 * this.mdsize, byArray5, 0, this.mdsize);
                for (l = (long)(n6 & n20); l < (long)n20; ++l) {
                    this.messDigestOTS.update(byArray5, 0, byArray5.length);
                    this.messDigestOTS.doFinal(byArray5, 0);
                }
                System.arraycopy(byArray5, 0, byArray4, n7 * this.mdsize, this.mdsize);
                n6 >>>= this.w;
                ++n7;
            }
        }
        this.messDigestOTS.update(byArray4, 0, byArray4.length);
        byte[] byArray6 = new byte[this.mdsize];
        this.messDigestOTS.doFinal(byArray6, 0);
        return byArray6;
    }

    public int getLog(int n) {
        int n2 = 1;
        int n3 = 2;
        while (n3 < n) {
            n3 <<= 1;
            ++n2;
        }
        return n2;
    }

    private void hashSignatureBlock(byte[] byArray, int n, int n2, byte[] byArray2, int n3) {
        if (n2 < 1) {
            System.arraycopy(byArray, n, byArray2, n3, this.mdsize);
        } else {
            this.messDigestOTS.update(byArray, n, this.mdsize);
            this.messDigestOTS.doFinal(byArray2, n3);
            while (--n2 > 0) {
                this.messDigestOTS.update(byArray2, n3, this.mdsize);
                this.messDigestOTS.doFinal(byArray2, n3);
            }
        }
    }
}

