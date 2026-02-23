/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec.rfc8032;

import java.security.SecureRandom;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.math.ec.rfc7748.X25519;
import org.bouncycastle.math.ec.rfc7748.X25519Field;
import org.bouncycastle.math.raw.Interleave;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.math.raw.Nat256;
import org.bouncycastle.util.Arrays;

public abstract class Ed25519 {
    private static final long M08L = 255L;
    private static final long M28L = 0xFFFFFFFL;
    private static final long M32L = 0xFFFFFFFFL;
    private static final int COORD_INTS = 8;
    private static final int POINT_BYTES = 32;
    private static final int SCALAR_INTS = 8;
    private static final int SCALAR_BYTES = 32;
    public static final int PREHASH_SIZE = 64;
    public static final int PUBLIC_KEY_SIZE = 32;
    public static final int SECRET_KEY_SIZE = 32;
    public static final int SIGNATURE_SIZE = 64;
    private static final byte[] DOM2_PREFIX = new byte[]{83, 105, 103, 69, 100, 50, 53, 53, 49, 57, 32, 110, 111, 32, 69, 100, 50, 53, 53, 49, 57, 32, 99, 111, 108, 108, 105, 115, 105, 111, 110, 115};
    private static final int[] P = new int[]{-19, -1, -1, -1, -1, -1, -1, Integer.MAX_VALUE};
    private static final int[] L = new int[]{1559614445, 1477600026, -1560830762, 350157278, 0, 0, 0, 0x10000000};
    private static final int L0 = -50998291;
    private static final int L1 = 19280294;
    private static final int L2 = 127719000;
    private static final int L3 = -6428113;
    private static final int L4 = 5343;
    private static final int[] B_x = new int[]{52811034, 25909283, 8072341, 50637101, 13785486, 30858332, 20483199, 20966410, 43936626, 4379245};
    private static final int[] B_y = new int[]{40265304, 0x1999999, 0x666666, 0x3333333, 0xCCCCCC, 0x2666666, 0x1999999, 0x666666, 0x3333333, 0xCCCCCC};
    private static final int[] C_d = new int[]{56195235, 47411844, 25868126, 40503822, 57364, 58321048, 30416477, 31930572, 57760639, 10749657};
    private static final int[] C_d2 = new int[]{45281625, 27714825, 18181821, 0xD4141D, 114729, 49533232, 60832955, 30306712, 48412415, 4722099};
    private static final int[] C_d4 = new int[]{23454386, 55429651, 2809210, 27797563, 229458, 31957600, 54557047, 27058993, 29715967, 9444199};
    private static final int WNAF_WIDTH_BASE = 7;
    private static final int PRECOMP_BLOCKS = 8;
    private static final int PRECOMP_TEETH = 4;
    private static final int PRECOMP_SPACING = 8;
    private static final int PRECOMP_POINTS = 8;
    private static final int PRECOMP_MASK = 7;
    private static final Object precompLock = new Object();
    private static PointExt[] precompBaseTable = null;
    private static int[] precompBase = null;

    private static byte[] calculateS(byte[] byArray, byte[] byArray2, byte[] byArray3) {
        int[] nArray = new int[16];
        Ed25519.decodeScalar(byArray, 0, nArray);
        int[] nArray2 = new int[8];
        Ed25519.decodeScalar(byArray2, 0, nArray2);
        int[] nArray3 = new int[8];
        Ed25519.decodeScalar(byArray3, 0, nArray3);
        Nat256.mulAddTo(nArray2, nArray3, nArray);
        byte[] byArray4 = new byte[64];
        for (int i = 0; i < nArray.length; ++i) {
            Ed25519.encode32(nArray[i], byArray4, i * 4);
        }
        return Ed25519.reduceScalar(byArray4);
    }

    private static boolean checkContextVar(byte[] byArray, byte by) {
        return byArray == null && by == 0 || byArray != null && byArray.length < 256;
    }

    private static int checkPoint(int[] nArray, int[] nArray2) {
        int[] nArray3 = F.create();
        int[] nArray4 = F.create();
        int[] nArray5 = F.create();
        F.sqr(nArray, nArray4);
        F.sqr(nArray2, nArray5);
        F.mul(nArray4, nArray5, nArray3);
        F.sub(nArray5, nArray4, nArray5);
        F.mul(nArray3, C_d, nArray3);
        F.addOne(nArray3);
        F.sub(nArray3, nArray5, nArray3);
        F.normalize(nArray3);
        return F.isZero(nArray3);
    }

    private static int checkPoint(int[] nArray, int[] nArray2, int[] nArray3) {
        int[] nArray4 = F.create();
        int[] nArray5 = F.create();
        int[] nArray6 = F.create();
        int[] nArray7 = F.create();
        F.sqr(nArray, nArray5);
        F.sqr(nArray2, nArray6);
        F.sqr(nArray3, nArray7);
        F.mul(nArray5, nArray6, nArray4);
        F.sub(nArray6, nArray5, nArray6);
        F.mul(nArray6, nArray7, nArray6);
        F.sqr(nArray7, nArray7);
        F.mul(nArray4, C_d, nArray4);
        F.add(nArray4, nArray7, nArray4);
        F.sub(nArray4, nArray6, nArray4);
        F.normalize(nArray4);
        return F.isZero(nArray4);
    }

    private static boolean checkPointVar(byte[] byArray) {
        int[] nArray = new int[8];
        Ed25519.decode32(byArray, 0, nArray, 0, 8);
        nArray[7] = nArray[7] & Integer.MAX_VALUE;
        return !Nat256.gte(nArray, P);
    }

    private static boolean checkScalarVar(byte[] byArray, int[] nArray) {
        Ed25519.decodeScalar(byArray, 0, nArray);
        return !Nat256.gte(nArray, L);
    }

    private static byte[] copy(byte[] byArray, int n, int n2) {
        byte[] byArray2 = new byte[n2];
        System.arraycopy(byArray, n, byArray2, 0, n2);
        return byArray2;
    }

    private static Digest createDigest() {
        return new SHA512Digest();
    }

    public static Digest createPrehash() {
        return Ed25519.createDigest();
    }

    private static int decode24(byte[] byArray, int n) {
        int n2 = byArray[n] & 0xFF;
        n2 |= (byArray[++n] & 0xFF) << 8;
        return n2 |= (byArray[++n] & 0xFF) << 16;
    }

    private static int decode32(byte[] byArray, int n) {
        int n2 = byArray[n] & 0xFF;
        n2 |= (byArray[++n] & 0xFF) << 8;
        n2 |= (byArray[++n] & 0xFF) << 16;
        return n2 |= byArray[++n] << 24;
    }

    private static void decode32(byte[] byArray, int n, int[] nArray, int n2, int n3) {
        for (int i = 0; i < n3; ++i) {
            nArray[n2 + i] = Ed25519.decode32(byArray, n + i * 4);
        }
    }

    private static boolean decodePointVar(byte[] byArray, int n, boolean bl, PointAffine pointAffine) {
        byte[] byArray2 = Ed25519.copy(byArray, n, 32);
        if (!Ed25519.checkPointVar(byArray2)) {
            return false;
        }
        int n2 = (byArray2[31] & 0x80) >>> 7;
        byArray2[31] = (byte)(byArray2[31] & 0x7F);
        F.decode(byArray2, 0, pointAffine.y);
        int[] nArray = F.create();
        int[] nArray2 = F.create();
        F.sqr(pointAffine.y, nArray);
        F.mul(C_d, nArray, nArray2);
        F.subOne(nArray);
        F.addOne(nArray2);
        if (!F.sqrtRatioVar(nArray, nArray2, pointAffine.x)) {
            return false;
        }
        F.normalize(pointAffine.x);
        if (n2 == 1 && F.isZeroVar(pointAffine.x)) {
            return false;
        }
        if (bl ^ n2 != (pointAffine.x[0] & 1)) {
            F.negate(pointAffine.x, pointAffine.x);
        }
        return true;
    }

    private static void decodeScalar(byte[] byArray, int n, int[] nArray) {
        Ed25519.decode32(byArray, n, nArray, 0, 8);
    }

    private static void dom2(Digest digest, byte by, byte[] byArray) {
        if (byArray != null) {
            int n = DOM2_PREFIX.length;
            byte[] byArray2 = new byte[n + 2 + byArray.length];
            System.arraycopy(DOM2_PREFIX, 0, byArray2, 0, n);
            byArray2[n] = by;
            byArray2[n + 1] = (byte)byArray.length;
            System.arraycopy(byArray, 0, byArray2, n + 2, byArray.length);
            digest.update(byArray2, 0, byArray2.length);
        }
    }

    private static void encode24(int n, byte[] byArray, int n2) {
        byArray[n2] = (byte)n;
        byArray[++n2] = (byte)(n >>> 8);
        byArray[++n2] = (byte)(n >>> 16);
    }

    private static void encode32(int n, byte[] byArray, int n2) {
        byArray[n2] = (byte)n;
        byArray[++n2] = (byte)(n >>> 8);
        byArray[++n2] = (byte)(n >>> 16);
        byArray[++n2] = (byte)(n >>> 24);
    }

    private static void encode56(long l, byte[] byArray, int n) {
        Ed25519.encode32((int)l, byArray, n);
        Ed25519.encode24((int)(l >>> 32), byArray, n + 4);
    }

    private static int encodePoint(PointAccum pointAccum, byte[] byArray, int n) {
        int[] nArray = F.create();
        int[] nArray2 = F.create();
        F.inv(pointAccum.z, nArray2);
        F.mul(pointAccum.x, nArray2, nArray);
        F.mul(pointAccum.y, nArray2, nArray2);
        F.normalize(nArray);
        F.normalize(nArray2);
        int n2 = Ed25519.checkPoint(nArray, nArray2);
        F.encode(nArray2, byArray, n);
        int n3 = n + 32 - 1;
        byArray[n3] = (byte)(byArray[n3] | (nArray[0] & 1) << 7);
        return n2;
    }

    public static void generatePrivateKey(SecureRandom secureRandom, byte[] byArray) {
        secureRandom.nextBytes(byArray);
    }

    public static void generatePublicKey(byte[] byArray, int n, byte[] byArray2, int n2) {
        Digest digest = Ed25519.createDigest();
        byte[] byArray3 = new byte[digest.getDigestSize()];
        digest.update(byArray, n, 32);
        digest.doFinal(byArray3, 0);
        byte[] byArray4 = new byte[32];
        Ed25519.pruneScalar(byArray3, 0, byArray4);
        Ed25519.scalarMultBaseEncoded(byArray4, byArray2, n2);
    }

    private static int getWindow4(int[] nArray, int n) {
        int n2 = n >>> 3;
        int n3 = (n & 7) << 2;
        return nArray[n2] >>> n3 & 0xF;
    }

    private static byte[] getWnafVar(int[] nArray, int n) {
        int n2;
        int[] nArray2 = new int[16];
        int n3 = nArray2.length;
        int n4 = 0;
        int n5 = 8;
        while (--n5 >= 0) {
            n2 = nArray[n5];
            nArray2[--n3] = n2 >>> 16 | n4 << 16;
            nArray2[--n3] = n4 = n2;
        }
        byte[] byArray = new byte[253];
        n4 = 32 - n;
        n5 = 0;
        n2 = 0;
        int n6 = 0;
        while (n6 < nArray2.length) {
            int n7 = nArray2[n6];
            while (n5 < 16) {
                int n8 = n7 >>> n5;
                int n9 = n8 & 1;
                if (n9 == n2) {
                    ++n5;
                    continue;
                }
                int n10 = (n8 | 1) << n4;
                n2 = n10 >>> 31;
                byArray[(n6 << 4) + n5] = (byte)(n10 >> n4);
                n5 += n;
            }
            ++n6;
            n5 -= 16;
        }
        return byArray;
    }

    private static void implSign(Digest digest, byte[] byArray, byte[] byArray2, byte[] byArray3, int n, byte[] byArray4, byte by, byte[] byArray5, int n2, int n3, byte[] byArray6, int n4) {
        Ed25519.dom2(digest, by, byArray4);
        digest.update(byArray, 32, 32);
        digest.update(byArray5, n2, n3);
        digest.doFinal(byArray, 0);
        byte[] byArray7 = Ed25519.reduceScalar(byArray);
        byte[] byArray8 = new byte[32];
        Ed25519.scalarMultBaseEncoded(byArray7, byArray8, 0);
        Ed25519.dom2(digest, by, byArray4);
        digest.update(byArray8, 0, 32);
        digest.update(byArray3, n, 32);
        digest.update(byArray5, n2, n3);
        digest.doFinal(byArray, 0);
        byte[] byArray9 = Ed25519.reduceScalar(byArray);
        byte[] byArray10 = Ed25519.calculateS(byArray7, byArray9, byArray2);
        System.arraycopy(byArray8, 0, byArray6, n4, 32);
        System.arraycopy(byArray10, 0, byArray6, n4 + 32, 32);
    }

    private static void implSign(byte[] byArray, int n, byte[] byArray2, byte by, byte[] byArray3, int n2, int n3, byte[] byArray4, int n4) {
        if (!Ed25519.checkContextVar(byArray2, by)) {
            throw new IllegalArgumentException("ctx");
        }
        Digest digest = Ed25519.createDigest();
        byte[] byArray5 = new byte[digest.getDigestSize()];
        digest.update(byArray, n, 32);
        digest.doFinal(byArray5, 0);
        byte[] byArray6 = new byte[32];
        Ed25519.pruneScalar(byArray5, 0, byArray6);
        byte[] byArray7 = new byte[32];
        Ed25519.scalarMultBaseEncoded(byArray6, byArray7, 0);
        Ed25519.implSign(digest, byArray5, byArray6, byArray7, 0, byArray2, by, byArray3, n2, n3, byArray4, n4);
    }

    private static void implSign(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, byte by, byte[] byArray4, int n3, int n4, byte[] byArray5, int n5) {
        if (!Ed25519.checkContextVar(byArray3, by)) {
            throw new IllegalArgumentException("ctx");
        }
        Digest digest = Ed25519.createDigest();
        byte[] byArray6 = new byte[digest.getDigestSize()];
        digest.update(byArray, n, 32);
        digest.doFinal(byArray6, 0);
        byte[] byArray7 = new byte[32];
        Ed25519.pruneScalar(byArray6, 0, byArray7);
        Ed25519.implSign(digest, byArray6, byArray7, byArray2, n2, byArray3, by, byArray4, n3, n4, byArray5, n5);
    }

    private static boolean implVerify(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, byte by, byte[] byArray4, int n3, int n4) {
        if (!Ed25519.checkContextVar(byArray3, by)) {
            throw new IllegalArgumentException("ctx");
        }
        byte[] byArray5 = Ed25519.copy(byArray, n, 32);
        byte[] byArray6 = Ed25519.copy(byArray, n + 32, 32);
        if (!Ed25519.checkPointVar(byArray5)) {
            return false;
        }
        int[] nArray = new int[8];
        if (!Ed25519.checkScalarVar(byArray6, nArray)) {
            return false;
        }
        PointAffine pointAffine = new PointAffine();
        if (!Ed25519.decodePointVar(byArray2, n2, true, pointAffine)) {
            return false;
        }
        Digest digest = Ed25519.createDigest();
        byte[] byArray7 = new byte[digest.getDigestSize()];
        Ed25519.dom2(digest, by, byArray3);
        digest.update(byArray5, 0, 32);
        digest.update(byArray2, n2, 32);
        digest.update(byArray4, n3, n4);
        digest.doFinal(byArray7, 0);
        byte[] byArray8 = Ed25519.reduceScalar(byArray7);
        int[] nArray2 = new int[8];
        Ed25519.decodeScalar(byArray8, 0, nArray2);
        PointAccum pointAccum = new PointAccum();
        Ed25519.scalarMultStrausVar(nArray, nArray2, pointAffine, pointAccum);
        byte[] byArray9 = new byte[32];
        return 0 != Ed25519.encodePoint(pointAccum, byArray9, 0) && Arrays.areEqual(byArray9, byArray5);
    }

    private static boolean isNeutralElementVar(int[] nArray, int[] nArray2) {
        return F.isZeroVar(nArray) && F.isOneVar(nArray2);
    }

    private static boolean isNeutralElementVar(int[] nArray, int[] nArray2, int[] nArray3) {
        return F.isZeroVar(nArray) && F.areEqualVar(nArray2, nArray3);
    }

    private static void pointAdd(PointExt pointExt, PointAccum pointAccum) {
        int[] nArray = F.create();
        int[] nArray2 = F.create();
        int[] nArray3 = F.create();
        int[] nArray4 = F.create();
        int[] nArray5 = pointAccum.u;
        int[] nArray6 = F.create();
        int[] nArray7 = F.create();
        int[] nArray8 = pointAccum.v;
        F.apm(pointAccum.y, pointAccum.x, nArray2, nArray);
        F.apm(pointExt.y, pointExt.x, nArray4, nArray3);
        F.mul(nArray, nArray3, nArray);
        F.mul(nArray2, nArray4, nArray2);
        F.mul(pointAccum.u, pointAccum.v, nArray3);
        F.mul(nArray3, pointExt.t, nArray3);
        F.mul(nArray3, C_d2, nArray3);
        F.mul(pointAccum.z, pointExt.z, nArray4);
        F.add(nArray4, nArray4, nArray4);
        F.apm(nArray2, nArray, nArray8, nArray5);
        F.apm(nArray4, nArray3, nArray7, nArray6);
        F.carry(nArray7);
        F.mul(nArray5, nArray6, pointAccum.x);
        F.mul(nArray7, nArray8, pointAccum.y);
        F.mul(nArray6, nArray7, pointAccum.z);
    }

    private static void pointAdd(PointExt pointExt, PointExt pointExt2) {
        int[] nArray = F.create();
        int[] nArray2 = F.create();
        int[] nArray3 = F.create();
        int[] nArray4 = F.create();
        int[] nArray5 = F.create();
        int[] nArray6 = F.create();
        int[] nArray7 = F.create();
        int[] nArray8 = F.create();
        F.apm(pointExt.y, pointExt.x, nArray2, nArray);
        F.apm(pointExt2.y, pointExt2.x, nArray4, nArray3);
        F.mul(nArray, nArray3, nArray);
        F.mul(nArray2, nArray4, nArray2);
        F.mul(pointExt.t, pointExt2.t, nArray3);
        F.mul(nArray3, C_d2, nArray3);
        F.mul(pointExt.z, pointExt2.z, nArray4);
        F.add(nArray4, nArray4, nArray4);
        F.apm(nArray2, nArray, nArray8, nArray5);
        F.apm(nArray4, nArray3, nArray7, nArray6);
        F.carry(nArray7);
        F.mul(nArray5, nArray6, pointExt2.x);
        F.mul(nArray7, nArray8, pointExt2.y);
        F.mul(nArray6, nArray7, pointExt2.z);
        F.mul(nArray5, nArray8, pointExt2.t);
    }

    private static void pointAddVar(boolean bl, PointExt pointExt, PointAccum pointAccum) {
        int[] nArray;
        int[] nArray2;
        int[] nArray3;
        int[] nArray4;
        int[] nArray5 = F.create();
        int[] nArray6 = F.create();
        int[] nArray7 = F.create();
        int[] nArray8 = F.create();
        int[] nArray9 = pointAccum.u;
        int[] nArray10 = F.create();
        int[] nArray11 = F.create();
        int[] nArray12 = pointAccum.v;
        if (bl) {
            nArray4 = nArray8;
            nArray3 = nArray7;
            nArray2 = nArray11;
            nArray = nArray10;
        } else {
            nArray4 = nArray7;
            nArray3 = nArray8;
            nArray2 = nArray10;
            nArray = nArray11;
        }
        F.apm(pointAccum.y, pointAccum.x, nArray6, nArray5);
        F.apm(pointExt.y, pointExt.x, nArray3, nArray4);
        F.mul(nArray5, nArray7, nArray5);
        F.mul(nArray6, nArray8, nArray6);
        F.mul(pointAccum.u, pointAccum.v, nArray7);
        F.mul(nArray7, pointExt.t, nArray7);
        F.mul(nArray7, C_d2, nArray7);
        F.mul(pointAccum.z, pointExt.z, nArray8);
        F.add(nArray8, nArray8, nArray8);
        F.apm(nArray6, nArray5, nArray12, nArray9);
        F.apm(nArray8, nArray7, nArray, nArray2);
        F.carry(nArray);
        F.mul(nArray9, nArray10, pointAccum.x);
        F.mul(nArray11, nArray12, pointAccum.y);
        F.mul(nArray10, nArray11, pointAccum.z);
    }

    private static void pointAddVar(boolean bl, PointExt pointExt, PointExt pointExt2, PointExt pointExt3) {
        int[] nArray;
        int[] nArray2;
        int[] nArray3;
        int[] nArray4;
        int[] nArray5 = F.create();
        int[] nArray6 = F.create();
        int[] nArray7 = F.create();
        int[] nArray8 = F.create();
        int[] nArray9 = F.create();
        int[] nArray10 = F.create();
        int[] nArray11 = F.create();
        int[] nArray12 = F.create();
        if (bl) {
            nArray4 = nArray8;
            nArray3 = nArray7;
            nArray2 = nArray11;
            nArray = nArray10;
        } else {
            nArray4 = nArray7;
            nArray3 = nArray8;
            nArray2 = nArray10;
            nArray = nArray11;
        }
        F.apm(pointExt.y, pointExt.x, nArray6, nArray5);
        F.apm(pointExt2.y, pointExt2.x, nArray3, nArray4);
        F.mul(nArray5, nArray7, nArray5);
        F.mul(nArray6, nArray8, nArray6);
        F.mul(pointExt.t, pointExt2.t, nArray7);
        F.mul(nArray7, C_d2, nArray7);
        F.mul(pointExt.z, pointExt2.z, nArray8);
        F.add(nArray8, nArray8, nArray8);
        F.apm(nArray6, nArray5, nArray12, nArray9);
        F.apm(nArray8, nArray7, nArray, nArray2);
        F.carry(nArray);
        F.mul(nArray9, nArray10, pointExt3.x);
        F.mul(nArray11, nArray12, pointExt3.y);
        F.mul(nArray10, nArray11, pointExt3.z);
        F.mul(nArray9, nArray12, pointExt3.t);
    }

    private static void pointAddPrecomp(PointPrecomp pointPrecomp, PointAccum pointAccum) {
        int[] nArray = F.create();
        int[] nArray2 = F.create();
        int[] nArray3 = F.create();
        int[] nArray4 = pointAccum.u;
        int[] nArray5 = F.create();
        int[] nArray6 = F.create();
        int[] nArray7 = pointAccum.v;
        F.apm(pointAccum.y, pointAccum.x, nArray2, nArray);
        F.mul(nArray, pointPrecomp.ymx_h, nArray);
        F.mul(nArray2, pointPrecomp.ypx_h, nArray2);
        F.mul(pointAccum.u, pointAccum.v, nArray3);
        F.mul(nArray3, pointPrecomp.xyd, nArray3);
        F.apm(nArray2, nArray, nArray7, nArray4);
        F.apm(pointAccum.z, nArray3, nArray6, nArray5);
        F.carry(nArray6);
        F.mul(nArray4, nArray5, pointAccum.x);
        F.mul(nArray6, nArray7, pointAccum.y);
        F.mul(nArray5, nArray6, pointAccum.z);
    }

    private static PointExt pointCopy(PointAccum pointAccum) {
        PointExt pointExt = new PointExt();
        F.copy(pointAccum.x, 0, pointExt.x, 0);
        F.copy(pointAccum.y, 0, pointExt.y, 0);
        F.copy(pointAccum.z, 0, pointExt.z, 0);
        F.mul(pointAccum.u, pointAccum.v, pointExt.t);
        return pointExt;
    }

    private static PointExt pointCopy(PointAffine pointAffine) {
        PointExt pointExt = new PointExt();
        F.copy(pointAffine.x, 0, pointExt.x, 0);
        F.copy(pointAffine.y, 0, pointExt.y, 0);
        Ed25519.pointExtendXY(pointExt);
        return pointExt;
    }

    private static PointExt pointCopy(PointExt pointExt) {
        PointExt pointExt2 = new PointExt();
        Ed25519.pointCopy(pointExt, pointExt2);
        return pointExt2;
    }

    private static void pointCopy(PointAffine pointAffine, PointAccum pointAccum) {
        F.copy(pointAffine.x, 0, pointAccum.x, 0);
        F.copy(pointAffine.y, 0, pointAccum.y, 0);
        Ed25519.pointExtendXY(pointAccum);
    }

    private static void pointCopy(PointExt pointExt, PointExt pointExt2) {
        F.copy(pointExt.x, 0, pointExt2.x, 0);
        F.copy(pointExt.y, 0, pointExt2.y, 0);
        F.copy(pointExt.z, 0, pointExt2.z, 0);
        F.copy(pointExt.t, 0, pointExt2.t, 0);
    }

    private static void pointDouble(PointAccum pointAccum) {
        int[] nArray = F.create();
        int[] nArray2 = F.create();
        int[] nArray3 = F.create();
        int[] nArray4 = pointAccum.u;
        int[] nArray5 = F.create();
        int[] nArray6 = F.create();
        int[] nArray7 = pointAccum.v;
        F.sqr(pointAccum.x, nArray);
        F.sqr(pointAccum.y, nArray2);
        F.sqr(pointAccum.z, nArray3);
        F.add(nArray3, nArray3, nArray3);
        F.apm(nArray, nArray2, nArray7, nArray6);
        F.add(pointAccum.x, pointAccum.y, nArray4);
        F.sqr(nArray4, nArray4);
        F.sub(nArray7, nArray4, nArray4);
        F.add(nArray3, nArray6, nArray5);
        F.carry(nArray5);
        F.mul(nArray4, nArray5, pointAccum.x);
        F.mul(nArray6, nArray7, pointAccum.y);
        F.mul(nArray5, nArray6, pointAccum.z);
    }

    private static void pointExtendXY(PointAccum pointAccum) {
        F.one(pointAccum.z);
        F.copy(pointAccum.x, 0, pointAccum.u, 0);
        F.copy(pointAccum.y, 0, pointAccum.v, 0);
    }

    private static void pointExtendXY(PointExt pointExt) {
        F.one(pointExt.z);
        F.mul(pointExt.x, pointExt.y, pointExt.t);
    }

    private static void pointLookup(int n, int n2, PointPrecomp pointPrecomp) {
        int n3 = n * 8 * 3 * 10;
        for (int i = 0; i < 8; ++i) {
            int n4 = (i ^ n2) - 1 >> 31;
            F.cmov(n4, precompBase, n3, pointPrecomp.ypx_h, 0);
            F.cmov(n4, precompBase, n3 += 10, pointPrecomp.ymx_h, 0);
            F.cmov(n4, precompBase, n3 += 10, pointPrecomp.xyd, 0);
            n3 += 10;
        }
    }

    private static void pointLookup(int[] nArray, int n, int[] nArray2, PointExt pointExt) {
        int n2 = Ed25519.getWindow4(nArray, n);
        int n3 = n2 >>> 3 ^ 1;
        int n4 = (n2 ^ -n3) & 7;
        int n5 = 0;
        for (int i = 0; i < 8; ++i) {
            int n6 = (i ^ n4) - 1 >> 31;
            F.cmov(n6, nArray2, n5, pointExt.x, 0);
            F.cmov(n6, nArray2, n5 += 10, pointExt.y, 0);
            F.cmov(n6, nArray2, n5 += 10, pointExt.z, 0);
            F.cmov(n6, nArray2, n5 += 10, pointExt.t, 0);
            n5 += 10;
        }
        F.cnegate(n3, pointExt.x);
        F.cnegate(n3, pointExt.t);
    }

    private static void pointLookup(int[] nArray, int n, PointExt pointExt) {
        int n2 = 40 * n;
        F.copy(nArray, n2, pointExt.x, 0);
        F.copy(nArray, n2 += 10, pointExt.y, 0);
        F.copy(nArray, n2 += 10, pointExt.z, 0);
        F.copy(nArray, n2 += 10, pointExt.t, 0);
    }

    private static int[] pointPrecompute(PointAffine pointAffine, int n) {
        PointExt pointExt = Ed25519.pointCopy(pointAffine);
        PointExt pointExt2 = Ed25519.pointCopy(pointExt);
        Ed25519.pointAdd(pointExt, pointExt2);
        int[] nArray = F.createTable(n * 4);
        int n2 = 0;
        int n3 = 0;
        while (true) {
            F.copy(pointExt.x, 0, nArray, n2);
            F.copy(pointExt.y, 0, nArray, n2 += 10);
            F.copy(pointExt.z, 0, nArray, n2 += 10);
            F.copy(pointExt.t, 0, nArray, n2 += 10);
            n2 += 10;
            if (++n3 == n) break;
            Ed25519.pointAdd(pointExt2, pointExt);
        }
        return nArray;
    }

    private static PointExt[] pointPrecomputeVar(PointExt pointExt, int n) {
        PointExt pointExt2 = new PointExt();
        Ed25519.pointAddVar(false, pointExt, pointExt, pointExt2);
        PointExt[] pointExtArray = new PointExt[n];
        pointExtArray[0] = Ed25519.pointCopy(pointExt);
        for (int i = 1; i < n; ++i) {
            pointExtArray[i] = new PointExt();
            Ed25519.pointAddVar(false, pointExtArray[i - 1], pointExt2, pointExtArray[i]);
        }
        return pointExtArray;
    }

    private static void pointSetNeutral(PointAccum pointAccum) {
        F.zero(pointAccum.x);
        F.one(pointAccum.y);
        F.one(pointAccum.z);
        F.zero(pointAccum.u);
        F.one(pointAccum.v);
    }

    private static void pointSetNeutral(PointExt pointExt) {
        F.zero(pointExt.x);
        F.one(pointExt.y);
        F.one(pointExt.z);
        F.zero(pointExt.t);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void precompute() {
        Object object = precompLock;
        synchronized (object) {
            if (precompBase != null) {
                return;
            }
            Object object2 = new PointExt();
            F.copy(B_x, 0, ((PointExt)object2).x, 0);
            F.copy(B_y, 0, ((PointExt)object2).y, 0);
            Ed25519.pointExtendXY((PointExt)object2);
            precompBaseTable = Ed25519.pointPrecomputeVar((PointExt)object2, 32);
            object2 = new PointAccum();
            F.copy(B_x, 0, ((PointAccum)object2).x, 0);
            F.copy(B_y, 0, ((PointAccum)object2).y, 0);
            Ed25519.pointExtendXY((PointAccum)object2);
            precompBase = F.createTable(192);
            int n = 0;
            for (int i = 0; i < 8; ++i) {
                int n2;
                int n3;
                int n4;
                PointExt[] pointExtArray = new PointExt[4];
                PointExt pointExt = new PointExt();
                Ed25519.pointSetNeutral(pointExt);
                for (int j = 0; j < 4; ++j) {
                    PointExt pointExt2 = Ed25519.pointCopy((PointAccum)object2);
                    Ed25519.pointAddVar(true, pointExt, pointExt2, pointExt);
                    Ed25519.pointDouble((PointAccum)object2);
                    pointExtArray[j] = Ed25519.pointCopy((PointAccum)object2);
                    if (i + j == 10) continue;
                    for (n4 = 1; n4 < 8; ++n4) {
                        Ed25519.pointDouble((PointAccum)object2);
                    }
                }
                PointExt[] pointExtArray2 = new PointExt[8];
                int n5 = 0;
                pointExtArray2[n5++] = pointExt;
                for (n4 = 0; n4 < 3; ++n4) {
                    n3 = 1 << n4;
                    n2 = 0;
                    while (n2 < n3) {
                        pointExtArray2[n5] = new PointExt();
                        Ed25519.pointAddVar(false, pointExtArray2[n5 - n3], pointExtArray[n4], pointExtArray2[n5]);
                        ++n2;
                        ++n5;
                    }
                }
                int[] nArray = F.createTable(8);
                int[] nArray2 = F.create();
                F.copy(pointExtArray2[0].z, 0, nArray2, 0);
                F.copy(nArray2, 0, nArray, 0);
                n2 = 0;
                while (++n2 < 8) {
                    F.mul(nArray2, pointExtArray2[n2].z, nArray2);
                    F.copy(nArray2, 0, nArray, n2 * 10);
                }
                F.add(nArray2, nArray2, nArray2);
                F.invVar(nArray2, nArray2);
                --n2;
                int[] nArray3 = F.create();
                while (n2 > 0) {
                    int n6 = n2--;
                    F.copy(nArray, n2 * 10, nArray3, 0);
                    F.mul(nArray3, nArray2, nArray3);
                    F.copy(nArray3, 0, nArray, n6 * 10);
                    F.mul(nArray2, pointExtArray2[n6].z, nArray2);
                }
                F.copy(nArray2, 0, nArray, 0);
                for (n3 = 0; n3 < 8; ++n3) {
                    PointExt pointExt3 = pointExtArray2[n3];
                    nArray3 = F.create();
                    int[] nArray4 = F.create();
                    F.copy(nArray, n3 * 10, nArray4, 0);
                    F.mul(pointExt3.x, nArray4, nArray3);
                    F.mul(pointExt3.y, nArray4, nArray4);
                    PointPrecomp pointPrecomp = new PointPrecomp();
                    F.apm(nArray4, nArray3, pointPrecomp.ypx_h, pointPrecomp.ymx_h);
                    F.mul(nArray3, nArray4, pointPrecomp.xyd);
                    F.mul(pointPrecomp.xyd, C_d4, pointPrecomp.xyd);
                    F.normalize(pointPrecomp.ypx_h);
                    F.normalize(pointPrecomp.ymx_h);
                    F.copy(pointPrecomp.ypx_h, 0, precompBase, n);
                    F.copy(pointPrecomp.ymx_h, 0, precompBase, n += 10);
                    F.copy(pointPrecomp.xyd, 0, precompBase, n += 10);
                    n += 10;
                }
            }
        }
    }

    private static void pruneScalar(byte[] byArray, int n, byte[] byArray2) {
        System.arraycopy(byArray, n, byArray2, 0, 32);
        byArray2[0] = (byte)(byArray2[0] & 0xF8);
        byArray2[31] = (byte)(byArray2[31] & 0x7F);
        byArray2[31] = (byte)(byArray2[31] | 0x40);
    }

    private static byte[] reduceScalar(byte[] byArray) {
        long l = (long)Ed25519.decode32(byArray, 0) & 0xFFFFFFFFL;
        long l2 = (long)(Ed25519.decode24(byArray, 4) << 4) & 0xFFFFFFFFL;
        long l3 = (long)Ed25519.decode32(byArray, 7) & 0xFFFFFFFFL;
        long l4 = (long)(Ed25519.decode24(byArray, 11) << 4) & 0xFFFFFFFFL;
        long l5 = (long)Ed25519.decode32(byArray, 14) & 0xFFFFFFFFL;
        long l6 = (long)(Ed25519.decode24(byArray, 18) << 4) & 0xFFFFFFFFL;
        long l7 = (long)Ed25519.decode32(byArray, 21) & 0xFFFFFFFFL;
        long l8 = (long)(Ed25519.decode24(byArray, 25) << 4) & 0xFFFFFFFFL;
        long l9 = (long)Ed25519.decode32(byArray, 28) & 0xFFFFFFFFL;
        long l10 = (long)(Ed25519.decode24(byArray, 32) << 4) & 0xFFFFFFFFL;
        long l11 = (long)Ed25519.decode32(byArray, 35) & 0xFFFFFFFFL;
        long l12 = (long)(Ed25519.decode24(byArray, 39) << 4) & 0xFFFFFFFFL;
        long l13 = (long)Ed25519.decode32(byArray, 42) & 0xFFFFFFFFL;
        long l14 = (long)(Ed25519.decode24(byArray, 46) << 4) & 0xFFFFFFFFL;
        long l15 = (long)Ed25519.decode32(byArray, 49) & 0xFFFFFFFFL;
        long l16 = (long)(Ed25519.decode24(byArray, 53) << 4) & 0xFFFFFFFFL;
        long l17 = (long)Ed25519.decode32(byArray, 56) & 0xFFFFFFFFL;
        long l18 = (long)(Ed25519.decode24(byArray, 60) << 4) & 0xFFFFFFFFL;
        long l19 = (long)byArray[63] & 0xFFL;
        l10 -= l19 * -50998291L;
        l11 -= l19 * 19280294L;
        l12 -= l19 * 127719000L;
        l13 -= l19 * -6428113L;
        l14 -= l19 * 5343L;
        l18 += l17 >> 28;
        l17 &= 0xFFFFFFFL;
        l9 -= l18 * -50998291L;
        l10 -= l18 * 19280294L;
        l11 -= l18 * 127719000L;
        l12 -= l18 * -6428113L;
        l13 -= l18 * 5343L;
        l8 -= l17 * -50998291L;
        l9 -= l17 * 19280294L;
        l10 -= l17 * 127719000L;
        l11 -= l17 * -6428113L;
        l12 -= l17 * 5343L;
        l16 += l15 >> 28;
        l15 &= 0xFFFFFFFL;
        l7 -= l16 * -50998291L;
        l8 -= l16 * 19280294L;
        l9 -= l16 * 127719000L;
        l10 -= l16 * -6428113L;
        l11 -= l16 * 5343L;
        l6 -= l15 * -50998291L;
        l7 -= l15 * 19280294L;
        l8 -= l15 * 127719000L;
        l9 -= l15 * -6428113L;
        l10 -= l15 * 5343L;
        l14 += l13 >> 28;
        l13 &= 0xFFFFFFFL;
        l5 -= l14 * -50998291L;
        l6 -= l14 * 19280294L;
        l7 -= l14 * 127719000L;
        l8 -= l14 * -6428113L;
        l9 -= l14 * 5343L;
        l13 += l12 >> 28;
        l12 &= 0xFFFFFFFL;
        l4 -= l13 * -50998291L;
        l5 -= l13 * 19280294L;
        l6 -= l13 * 127719000L;
        l7 -= l13 * -6428113L;
        l8 -= l13 * 5343L;
        l12 += l11 >> 28;
        l11 &= 0xFFFFFFFL;
        l3 -= l12 * -50998291L;
        l4 -= l12 * 19280294L;
        l5 -= l12 * 127719000L;
        l6 -= l12 * -6428113L;
        l7 -= l12 * 5343L;
        l11 += l10 >> 28;
        l10 &= 0xFFFFFFFL;
        l2 -= l11 * -50998291L;
        l3 -= l11 * 19280294L;
        l4 -= l11 * 127719000L;
        l5 -= l11 * -6428113L;
        l6 -= l11 * 5343L;
        l9 += l8 >> 28;
        l8 &= 0xFFFFFFFL;
        l10 += l9 >> 28;
        long l20 = (l9 &= 0xFFFFFFFL) >>> 27;
        l2 -= l10 * 19280294L;
        l3 -= l10 * 127719000L;
        l4 -= l10 * -6428113L;
        l5 -= l10 * 5343L;
        l &= 0xFFFFFFFL;
        l2 &= 0xFFFFFFFL;
        l3 &= 0xFFFFFFFL;
        l4 &= 0xFFFFFFFL;
        l5 &= 0xFFFFFFFL;
        l6 &= 0xFFFFFFFL;
        l7 &= 0xFFFFFFFL;
        l8 &= 0xFFFFFFFL;
        l10 = (l9 += (l8 += (l7 += (l6 += (l5 += (l4 += (l3 += (l2 += (l -= (l10 += l20) * -50998291L) >> 28) >> 28) >> 28) >> 28) >> 28) >> 28) >> 28) >> 28) >> 28;
        l9 &= 0xFFFFFFFL;
        l2 += l10 & 0x12631A6L;
        l3 += l10 & 0x79CD658L;
        l4 += l10 & 0xFFFFFFFFFF9DEA2FL;
        l5 += l10 & 0x14DFL;
        l &= 0xFFFFFFFL;
        l2 &= 0xFFFFFFFL;
        l3 &= 0xFFFFFFFL;
        l4 &= 0xFFFFFFFL;
        l5 &= 0xFFFFFFFL;
        l6 &= 0xFFFFFFFL;
        l7 &= 0xFFFFFFFL;
        l9 += (l8 += (l7 += (l6 += (l5 += (l4 += (l3 += (l2 += (l += (l10 -= l20) & 0xFFFFFFFFFCF5D3EDL) >> 28) >> 28) >> 28) >> 28) >> 28) >> 28) >> 28) >> 28;
        byte[] byArray2 = new byte[32];
        Ed25519.encode56(l | l2 << 28, byArray2, 0);
        Ed25519.encode56(l3 | l4 << 28, byArray2, 7);
        Ed25519.encode56(l5 | l6 << 28, byArray2, 14);
        Ed25519.encode56(l7 | (l8 &= 0xFFFFFFFL) << 28, byArray2, 21);
        Ed25519.encode32((int)l9, byArray2, 28);
        return byArray2;
    }

    private static void scalarMult(byte[] byArray, PointAffine pointAffine, PointAccum pointAccum) {
        int[] nArray = new int[8];
        Ed25519.decodeScalar(byArray, 0, nArray);
        Nat.shiftDownBits(8, nArray, 3, 1);
        Nat.cadd(8, ~nArray[0] & 1, nArray, L, nArray);
        Nat.shiftDownBit(8, nArray, 0);
        int[] nArray2 = Ed25519.pointPrecompute(pointAffine, 8);
        PointExt pointExt = new PointExt();
        Ed25519.pointCopy(pointAffine, pointAccum);
        Ed25519.pointLookup(nArray2, 7, pointExt);
        Ed25519.pointAdd(pointExt, pointAccum);
        int n = 62;
        while (true) {
            Ed25519.pointLookup(nArray, n, nArray2, pointExt);
            Ed25519.pointAdd(pointExt, pointAccum);
            Ed25519.pointDouble(pointAccum);
            Ed25519.pointDouble(pointAccum);
            Ed25519.pointDouble(pointAccum);
            if (--n < 0) break;
            Ed25519.pointDouble(pointAccum);
        }
    }

    private static void scalarMultBase(byte[] byArray, PointAccum pointAccum) {
        Ed25519.precompute();
        int[] nArray = new int[8];
        Ed25519.decodeScalar(byArray, 0, nArray);
        Nat.cadd(8, ~nArray[0] & 1, nArray, L, nArray);
        Nat.shiftDownBit(8, nArray, 1);
        for (int i = 0; i < 8; ++i) {
            nArray[i] = Interleave.shuffle2(nArray[i]);
        }
        PointPrecomp pointPrecomp = new PointPrecomp();
        Ed25519.pointSetNeutral(pointAccum);
        int n = 28;
        while (true) {
            for (int i = 0; i < 8; ++i) {
                int n2 = nArray[i] >>> n;
                int n3 = n2 >>> 3 & 1;
                int n4 = (n2 ^ -n3) & 7;
                Ed25519.pointLookup(i, n4, pointPrecomp);
                F.cswap(n3, pointPrecomp.ypx_h, pointPrecomp.ymx_h);
                F.cnegate(n3, pointPrecomp.xyd);
                Ed25519.pointAddPrecomp(pointPrecomp, pointAccum);
            }
            if ((n -= 4) < 0) break;
            Ed25519.pointDouble(pointAccum);
        }
    }

    private static void scalarMultBaseEncoded(byte[] byArray, byte[] byArray2, int n) {
        PointAccum pointAccum = new PointAccum();
        Ed25519.scalarMultBase(byArray, pointAccum);
        if (0 == Ed25519.encodePoint(pointAccum, byArray2, n)) {
            throw new IllegalStateException();
        }
    }

    public static void scalarMultBaseYZ(X25519.Friend friend, byte[] byArray, int n, int[] nArray, int[] nArray2) {
        if (null == friend) {
            throw new NullPointerException("This method is only for use by X25519");
        }
        byte[] byArray2 = new byte[32];
        Ed25519.pruneScalar(byArray, n, byArray2);
        PointAccum pointAccum = new PointAccum();
        Ed25519.scalarMultBase(byArray2, pointAccum);
        if (0 == Ed25519.checkPoint(pointAccum.x, pointAccum.y, pointAccum.z)) {
            throw new IllegalStateException();
        }
        F.copy(pointAccum.y, 0, nArray, 0);
        F.copy(pointAccum.z, 0, nArray2, 0);
    }

    private static void scalarMultOrderVar(PointAffine pointAffine, PointAccum pointAccum) {
        byte[] byArray = Ed25519.getWnafVar(L, 5);
        PointExt[] pointExtArray = Ed25519.pointPrecomputeVar(Ed25519.pointCopy(pointAffine), 8);
        Ed25519.pointSetNeutral(pointAccum);
        int n = 252;
        while (true) {
            byte by;
            if ((by = byArray[n]) != 0) {
                int n2 = by >> 31;
                int n3 = (by ^ n2) >>> 1;
                Ed25519.pointAddVar(n2 != 0, pointExtArray[n3], pointAccum);
            }
            if (--n < 0) break;
            Ed25519.pointDouble(pointAccum);
        }
    }

    private static void scalarMultStrausVar(int[] nArray, int[] nArray2, PointAffine pointAffine, PointAccum pointAccum) {
        Ed25519.precompute();
        byte[] byArray = Ed25519.getWnafVar(nArray, 7);
        byte[] byArray2 = Ed25519.getWnafVar(nArray2, 5);
        PointExt[] pointExtArray = Ed25519.pointPrecomputeVar(Ed25519.pointCopy(pointAffine), 8);
        Ed25519.pointSetNeutral(pointAccum);
        int n = 252;
        while (true) {
            int n2;
            int n3;
            byte by;
            if ((by = byArray[n]) != 0) {
                n3 = by >> 31;
                n2 = (by ^ n3) >>> 1;
                Ed25519.pointAddVar(n3 != 0, precompBaseTable[n2], pointAccum);
            }
            if ((n3 = byArray2[n]) != 0) {
                n2 = n3 >> 31;
                int n4 = (n3 ^ n2) >>> 1;
                Ed25519.pointAddVar(n2 != 0, pointExtArray[n4], pointAccum);
            }
            if (--n < 0) break;
            Ed25519.pointDouble(pointAccum);
        }
    }

    public static void sign(byte[] byArray, int n, byte[] byArray2, int n2, int n3, byte[] byArray3, int n4) {
        byte[] byArray4 = null;
        byte by = 0;
        Ed25519.implSign(byArray, n, byArray4, by, byArray2, n2, n3, byArray3, n4);
    }

    public static void sign(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, int n3, int n4, byte[] byArray4, int n5) {
        byte[] byArray5 = null;
        byte by = 0;
        Ed25519.implSign(byArray, n, byArray2, n2, byArray5, by, byArray3, n3, n4, byArray4, n5);
    }

    public static void sign(byte[] byArray, int n, byte[] byArray2, byte[] byArray3, int n2, int n3, byte[] byArray4, int n4) {
        byte by = 0;
        Ed25519.implSign(byArray, n, byArray2, by, byArray3, n2, n3, byArray4, n4);
    }

    public static void sign(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, byte[] byArray4, int n3, int n4, byte[] byArray5, int n5) {
        byte by = 0;
        Ed25519.implSign(byArray, n, byArray2, n2, byArray3, by, byArray4, n3, n4, byArray5, n5);
    }

    public static void signPrehash(byte[] byArray, int n, byte[] byArray2, byte[] byArray3, int n2, byte[] byArray4, int n3) {
        byte by = 1;
        Ed25519.implSign(byArray, n, byArray2, by, byArray3, n2, 64, byArray4, n3);
    }

    public static void signPrehash(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, byte[] byArray4, int n3, byte[] byArray5, int n4) {
        byte by = 1;
        Ed25519.implSign(byArray, n, byArray2, n2, byArray3, by, byArray4, n3, 64, byArray5, n4);
    }

    public static void signPrehash(byte[] byArray, int n, byte[] byArray2, Digest digest, byte[] byArray3, int n2) {
        byte[] byArray4 = new byte[64];
        if (64 != digest.doFinal(byArray4, 0)) {
            throw new IllegalArgumentException("ph");
        }
        byte by = 1;
        Ed25519.implSign(byArray, n, byArray2, by, byArray4, 0, byArray4.length, byArray3, n2);
    }

    public static void signPrehash(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, Digest digest, byte[] byArray4, int n3) {
        byte[] byArray5 = new byte[64];
        if (64 != digest.doFinal(byArray5, 0)) {
            throw new IllegalArgumentException("ph");
        }
        byte by = 1;
        Ed25519.implSign(byArray, n, byArray2, n2, byArray3, by, byArray5, 0, byArray5.length, byArray4, n3);
    }

    public static boolean validatePublicKeyFull(byte[] byArray, int n) {
        PointAffine pointAffine = new PointAffine();
        if (!Ed25519.decodePointVar(byArray, n, false, pointAffine)) {
            return false;
        }
        F.normalize(pointAffine.x);
        F.normalize(pointAffine.y);
        if (Ed25519.isNeutralElementVar(pointAffine.x, pointAffine.y)) {
            return false;
        }
        PointAccum pointAccum = new PointAccum();
        Ed25519.scalarMultOrderVar(pointAffine, pointAccum);
        F.normalize(pointAccum.x);
        F.normalize(pointAccum.y);
        F.normalize(pointAccum.z);
        return Ed25519.isNeutralElementVar(pointAccum.x, pointAccum.y, pointAccum.z);
    }

    public static boolean validatePublicKeyPartial(byte[] byArray, int n) {
        PointAffine pointAffine = new PointAffine();
        return Ed25519.decodePointVar(byArray, n, false, pointAffine);
    }

    public static boolean verify(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, int n3, int n4) {
        byte[] byArray4 = null;
        byte by = 0;
        return Ed25519.implVerify(byArray, n, byArray2, n2, byArray4, by, byArray3, n3, n4);
    }

    public static boolean verify(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, byte[] byArray4, int n3, int n4) {
        byte by = 0;
        return Ed25519.implVerify(byArray, n, byArray2, n2, byArray3, by, byArray4, n3, n4);
    }

    public static boolean verifyPrehash(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, byte[] byArray4, int n3) {
        byte by = 1;
        return Ed25519.implVerify(byArray, n, byArray2, n2, byArray3, by, byArray4, n3, 64);
    }

    public static boolean verifyPrehash(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, Digest digest) {
        byte[] byArray4 = new byte[64];
        if (64 != digest.doFinal(byArray4, 0)) {
            throw new IllegalArgumentException("ph");
        }
        byte by = 1;
        return Ed25519.implVerify(byArray, n, byArray2, n2, byArray3, by, byArray4, 0, byArray4.length);
    }

    public static final class Algorithm {
        public static final int Ed25519 = 0;
        public static final int Ed25519ctx = 1;
        public static final int Ed25519ph = 2;
    }

    private static class F
    extends X25519Field {
        private F() {
        }
    }

    private static class PointAccum {
        int[] x = F.create();
        int[] y = F.create();
        int[] z = F.create();
        int[] u = F.create();
        int[] v = F.create();

        private PointAccum() {
        }
    }

    private static class PointAffine {
        int[] x = F.create();
        int[] y = F.create();

        private PointAffine() {
        }
    }

    private static class PointExt {
        int[] x = F.create();
        int[] y = F.create();
        int[] z = F.create();
        int[] t = F.create();

        private PointExt() {
        }
    }

    private static class PointPrecomp {
        int[] ypx_h = F.create();
        int[] ymx_h = F.create();
        int[] xyd = F.create();

        private PointPrecomp() {
        }
    }
}

