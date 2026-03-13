/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.pqc.crypto.lms.DigestUtil;
import org.bouncycastle.pqc.crypto.lms.LMOtsParameters;
import org.bouncycastle.pqc.crypto.lms.LMOtsSignature;
import org.bouncycastle.pqc.crypto.lms.LMSContext;
import org.bouncycastle.pqc.crypto.lms.LMSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSSignature;
import org.bouncycastle.pqc.crypto.lms.LMSigParameters;
import org.bouncycastle.pqc.crypto.lms.LM_OTS;
import org.bouncycastle.pqc.crypto.lms.LmsUtils;

class LMS {
    static final short D_LEAF = -32126;
    static final short D_INTR = -31869;

    LMS() {
    }

    public static LMSPrivateKeyParameters generateKeys(LMSigParameters lMSigParameters, LMOtsParameters lMOtsParameters, int n, byte[] byArray, byte[] byArray2) throws IllegalArgumentException {
        if (byArray2 == null || byArray2.length < lMSigParameters.getM()) {
            throw new IllegalArgumentException("root seed is less than " + lMSigParameters.getM());
        }
        int n2 = 1 << lMSigParameters.getH();
        return new LMSPrivateKeyParameters(lMSigParameters, lMOtsParameters, n, byArray, n2, byArray2);
    }

    public static LMSSignature generateSign(LMSPrivateKeyParameters lMSPrivateKeyParameters, byte[] byArray) {
        LMSContext lMSContext = lMSPrivateKeyParameters.generateLMSContext();
        lMSContext.update(byArray, 0, byArray.length);
        return LMS.generateSign(lMSContext);
    }

    public static LMSSignature generateSign(LMSContext lMSContext) {
        LMOtsSignature lMOtsSignature = LM_OTS.lm_ots_generate_signature(lMSContext.getPrivateKey(), lMSContext.getQ(), lMSContext.getC());
        return new LMSSignature(lMSContext.getPrivateKey().getQ(), lMOtsSignature, lMSContext.getSigParams(), lMSContext.getPath());
    }

    public static boolean verifySignature(LMSPublicKeyParameters lMSPublicKeyParameters, LMSSignature lMSSignature, byte[] byArray) {
        LMSContext lMSContext = lMSPublicKeyParameters.generateOtsContext(lMSSignature);
        LmsUtils.byteArray(byArray, lMSContext);
        return LMS.verifySignature(lMSPublicKeyParameters, lMSContext);
    }

    public static boolean verifySignature(LMSPublicKeyParameters lMSPublicKeyParameters, byte[] byArray, byte[] byArray2) {
        LMSContext lMSContext = lMSPublicKeyParameters.generateLMSContext(byArray);
        LmsUtils.byteArray(byArray2, lMSContext);
        return LMS.verifySignature(lMSPublicKeyParameters, lMSContext);
    }

    public static boolean verifySignature(LMSPublicKeyParameters lMSPublicKeyParameters, LMSContext lMSContext) {
        LMSSignature lMSSignature = (LMSSignature)lMSContext.getSignature();
        LMSigParameters lMSigParameters = lMSSignature.getParameter();
        int n = lMSigParameters.getH();
        byte[][] byArray = lMSSignature.getY();
        byte[] byArray2 = LM_OTS.lm_ots_validate_signature_calculate(lMSContext);
        int n2 = (1 << n) + lMSSignature.getQ();
        byte[] byArray3 = lMSPublicKeyParameters.getI();
        Digest digest = DigestUtil.getDigest(lMSigParameters.getDigestOID());
        byte[] byArray4 = new byte[digest.getDigestSize()];
        digest.update(byArray3, 0, byArray3.length);
        LmsUtils.u32str(n2, digest);
        LmsUtils.u16str((short)-32126, digest);
        digest.update(byArray2, 0, byArray2.length);
        digest.doFinal(byArray4, 0);
        int n3 = 0;
        while (n2 > 1) {
            if ((n2 & 1) == 1) {
                digest.update(byArray3, 0, byArray3.length);
                LmsUtils.u32str(n2 / 2, digest);
                LmsUtils.u16str((short)-31869, digest);
                digest.update(byArray[n3], 0, byArray[n3].length);
                digest.update(byArray4, 0, byArray4.length);
                digest.doFinal(byArray4, 0);
            } else {
                digest.update(byArray3, 0, byArray3.length);
                LmsUtils.u32str(n2 / 2, digest);
                LmsUtils.u16str((short)-31869, digest);
                digest.update(byArray4, 0, byArray4.length);
                digest.update(byArray[n3], 0, byArray[n3].length);
                digest.doFinal(byArray4, 0);
            }
            n2 /= 2;
            ++n3;
        }
        byte[] byArray5 = byArray4;
        return lMSPublicKeyParameters.matchesT1(byArray5);
    }
}

