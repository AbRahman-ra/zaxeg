/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.pqc.crypto.lms.Composer;
import org.bouncycastle.pqc.crypto.lms.DigestUtil;
import org.bouncycastle.pqc.crypto.lms.LMOtsParameters;
import org.bouncycastle.pqc.crypto.lms.LMOtsPrivateKey;
import org.bouncycastle.pqc.crypto.lms.LMOtsPublicKey;
import org.bouncycastle.pqc.crypto.lms.LMOtsSignature;
import org.bouncycastle.pqc.crypto.lms.LMSContext;
import org.bouncycastle.pqc.crypto.lms.LMSException;
import org.bouncycastle.pqc.crypto.lms.LMSSignature;
import org.bouncycastle.pqc.crypto.lms.LMSigParameters;
import org.bouncycastle.pqc.crypto.lms.LmsUtils;
import org.bouncycastle.pqc.crypto.lms.SeedDerive;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

class LM_OTS {
    private static final short D_PBLC = -32640;
    private static final int ITER_K = 20;
    private static final int ITER_PREV = 23;
    private static final int ITER_J = 22;
    static final int SEED_RANDOMISER_INDEX = -3;
    static final int SEED_LEN = 32;
    static final int MAX_HASH = 32;
    static final short D_MESG = -32383;

    LM_OTS() {
    }

    public static int coef(byte[] byArray, int n, int n2) {
        int n3 = n * n2 / 8;
        int n4 = 8 / n2;
        int n5 = n2 * (~n & n4 - 1);
        int n6 = (1 << n2) - 1;
        return byArray[n3] >>> n5 & n6;
    }

    public static int cksm(byte[] byArray, int n, LMOtsParameters lMOtsParameters) {
        int n2 = 0;
        int n3 = lMOtsParameters.getW();
        int n4 = (1 << n3) - 1;
        for (int i = 0; i < n * 8 / lMOtsParameters.getW(); ++i) {
            n2 = n2 + n4 - LM_OTS.coef(byArray, i, lMOtsParameters.getW());
        }
        return n2 << lMOtsParameters.getLs();
    }

    public static LMOtsPublicKey lms_ots_generatePublicKey(LMOtsPrivateKey lMOtsPrivateKey) {
        byte[] byArray = LM_OTS.lms_ots_generatePublicKey(lMOtsPrivateKey.getParameter(), lMOtsPrivateKey.getI(), lMOtsPrivateKey.getQ(), lMOtsPrivateKey.getMasterSecret());
        return new LMOtsPublicKey(lMOtsPrivateKey.getParameter(), lMOtsPrivateKey.getI(), lMOtsPrivateKey.getQ(), byArray);
    }

    static byte[] lms_ots_generatePublicKey(LMOtsParameters lMOtsParameters, byte[] byArray, int n, byte[] byArray2) {
        Digest digest = DigestUtil.getDigest(lMOtsParameters.getDigestOID());
        byte[] byArray3 = Composer.compose().bytes(byArray).u32str(n).u16str(-32640).padUntil(0, 22).build();
        digest.update(byArray3, 0, byArray3.length);
        Digest digest2 = DigestUtil.getDigest(lMOtsParameters.getDigestOID());
        byte[] byArray4 = Composer.compose().bytes(byArray).u32str(n).padUntil(0, 23 + digest2.getDigestSize()).build();
        SeedDerive seedDerive = new SeedDerive(byArray, byArray2, DigestUtil.getDigest(lMOtsParameters.getDigestOID()));
        seedDerive.setQ(n);
        seedDerive.setJ(0);
        int n2 = lMOtsParameters.getP();
        int n3 = lMOtsParameters.getN();
        int n4 = (1 << lMOtsParameters.getW()) - 1;
        for (int i = 0; i < n2; ++i) {
            seedDerive.deriveSeed(byArray4, i < n2 - 1, 23);
            Pack.shortToBigEndian((short)i, byArray4, 20);
            for (int j = 0; j < n4; ++j) {
                byArray4[22] = (byte)j;
                digest2.update(byArray4, 0, byArray4.length);
                digest2.doFinal(byArray4, 23);
            }
            digest.update(byArray4, 23, n3);
        }
        byte[] byArray5 = new byte[digest.getDigestSize()];
        digest.doFinal(byArray5, 0);
        return byArray5;
    }

    public static LMOtsSignature lm_ots_generate_signature(LMSigParameters lMSigParameters, LMOtsPrivateKey lMOtsPrivateKey, byte[][] byArray, byte[] byArray2, boolean bl) {
        byte[] byArray3;
        byte[] byArray4 = new byte[34];
        if (!bl) {
            LMSContext lMSContext = lMOtsPrivateKey.getSignatureContext(lMSigParameters, byArray);
            LmsUtils.byteArray(byArray2, 0, byArray2.length, lMSContext);
            byArray3 = lMSContext.getC();
            byArray4 = lMSContext.getQ();
        } else {
            byArray3 = new byte[32];
            System.arraycopy(byArray2, 0, byArray4, 0, lMOtsPrivateKey.getParameter().getN());
        }
        return LM_OTS.lm_ots_generate_signature(lMOtsPrivateKey, byArray4, byArray3);
    }

    public static LMOtsSignature lm_ots_generate_signature(LMOtsPrivateKey lMOtsPrivateKey, byte[] byArray, byte[] byArray2) {
        LMOtsParameters lMOtsParameters = lMOtsPrivateKey.getParameter();
        int n = lMOtsParameters.getN();
        int n2 = lMOtsParameters.getP();
        int n3 = lMOtsParameters.getW();
        byte[] byArray3 = new byte[n2 * n];
        Digest digest = DigestUtil.getDigest(lMOtsParameters.getDigestOID());
        SeedDerive seedDerive = lMOtsPrivateKey.getDerivationFunction();
        int n4 = LM_OTS.cksm(byArray, n, lMOtsParameters);
        byArray[n] = (byte)(n4 >>> 8 & 0xFF);
        byArray[n + 1] = (byte)n4;
        byte[] byArray4 = Composer.compose().bytes(lMOtsPrivateKey.getI()).u32str(lMOtsPrivateKey.getQ()).padUntil(0, 23 + n).build();
        seedDerive.setJ(0);
        for (int i = 0; i < n2; ++i) {
            Pack.shortToBigEndian((short)i, byArray4, 20);
            seedDerive.deriveSeed(byArray4, i < n2 - 1, 23);
            int n5 = LM_OTS.coef(byArray, i, n3);
            for (int j = 0; j < n5; ++j) {
                byArray4[22] = (byte)j;
                digest.update(byArray4, 0, 23 + n);
                digest.doFinal(byArray4, 23);
            }
            System.arraycopy(byArray4, 23, byArray3, n * i, n);
        }
        return new LMOtsSignature(lMOtsParameters, byArray2, byArray3);
    }

    public static boolean lm_ots_validate_signature(LMOtsPublicKey lMOtsPublicKey, LMOtsSignature lMOtsSignature, byte[] byArray, boolean bl) throws LMSException {
        if (!lMOtsSignature.getType().equals(lMOtsPublicKey.getParameter())) {
            throw new LMSException("public key and signature ots types do not match");
        }
        return Arrays.areEqual(LM_OTS.lm_ots_validate_signature_calculate(lMOtsPublicKey, lMOtsSignature, byArray), lMOtsPublicKey.getK());
    }

    public static byte[] lm_ots_validate_signature_calculate(LMOtsPublicKey lMOtsPublicKey, LMOtsSignature lMOtsSignature, byte[] byArray) {
        LMSContext lMSContext = lMOtsPublicKey.createOtsContext(lMOtsSignature);
        LmsUtils.byteArray(byArray, lMSContext);
        return LM_OTS.lm_ots_validate_signature_calculate(lMSContext);
    }

    public static byte[] lm_ots_validate_signature_calculate(LMSContext lMSContext) {
        LMOtsPublicKey lMOtsPublicKey = lMSContext.getPublicKey();
        LMOtsParameters lMOtsParameters = lMOtsPublicKey.getParameter();
        Object object = lMSContext.getSignature();
        LMOtsSignature lMOtsSignature = object instanceof LMSSignature ? ((LMSSignature)object).getOtsSignature() : (LMOtsSignature)object;
        int n = lMOtsParameters.getN();
        int n2 = lMOtsParameters.getW();
        int n3 = lMOtsParameters.getP();
        byte[] byArray = lMSContext.getQ();
        int n4 = LM_OTS.cksm(byArray, n, lMOtsParameters);
        byArray[n] = (byte)(n4 >>> 8 & 0xFF);
        byArray[n + 1] = (byte)n4;
        byte[] byArray2 = lMOtsPublicKey.getI();
        int n5 = lMOtsPublicKey.getQ();
        Digest digest = DigestUtil.getDigest(lMOtsParameters.getDigestOID());
        LmsUtils.byteArray(byArray2, digest);
        LmsUtils.u32str(n5, digest);
        LmsUtils.u16str((short)-32640, digest);
        byte[] byArray3 = Composer.compose().bytes(byArray2).u32str(n5).padUntil(0, 23 + n).build();
        int n6 = (1 << n2) - 1;
        byte[] byArray4 = lMOtsSignature.getY();
        Digest digest2 = DigestUtil.getDigest(lMOtsParameters.getDigestOID());
        for (int i = 0; i < n3; ++i) {
            int n7;
            Pack.shortToBigEndian((short)i, byArray3, 20);
            System.arraycopy(byArray4, i * n, byArray3, 23, n);
            for (int j = n7 = LM_OTS.coef(byArray, i, n2); j < n6; ++j) {
                byArray3[22] = (byte)j;
                digest2.update(byArray3, 0, 23 + n);
                digest2.doFinal(byArray3, 23);
            }
            digest.update(byArray3, 23, n);
        }
        byte[] byArray5 = new byte[n];
        digest.doFinal(byArray5, 0);
        return byArray5;
    }
}

