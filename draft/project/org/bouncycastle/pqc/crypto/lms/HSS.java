/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import java.util.Arrays;
import java.util.List;
import org.bouncycastle.pqc.crypto.ExhaustedPrivateKeyException;
import org.bouncycastle.pqc.crypto.lms.HSSKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.lms.HSSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.lms.HSSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.HSSSignature;
import org.bouncycastle.pqc.crypto.lms.LMOtsParameters;
import org.bouncycastle.pqc.crypto.lms.LMOtsPrivateKey;
import org.bouncycastle.pqc.crypto.lms.LMS;
import org.bouncycastle.pqc.crypto.lms.LMSContext;
import org.bouncycastle.pqc.crypto.lms.LMSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSSignature;
import org.bouncycastle.pqc.crypto.lms.LMSSignedPubKey;
import org.bouncycastle.pqc.crypto.lms.LMSigParameters;

class HSS {
    HSS() {
    }

    public static HSSPrivateKeyParameters generateHSSKeyPair(HSSKeyGenerationParameters hSSKeyGenerationParameters) {
        LMSPrivateKeyParameters[] lMSPrivateKeyParametersArray = new LMSPrivateKeyParameters[hSSKeyGenerationParameters.getDepth()];
        LMSSignature[] lMSSignatureArray = new LMSSignature[hSSKeyGenerationParameters.getDepth() - 1];
        byte[] byArray = new byte[32];
        hSSKeyGenerationParameters.getRandom().nextBytes(byArray);
        byte[] byArray2 = new byte[16];
        hSSKeyGenerationParameters.getRandom().nextBytes(byArray2);
        byte[] byArray3 = new byte[]{};
        long l = 1L;
        for (int i = 0; i < lMSPrivateKeyParametersArray.length; ++i) {
            lMSPrivateKeyParametersArray[i] = i == 0 ? new LMSPrivateKeyParameters(hSSKeyGenerationParameters.getLmsParameters()[i].getLMSigParam(), hSSKeyGenerationParameters.getLmsParameters()[i].getLMOTSParam(), 0, byArray2, 1 << hSSKeyGenerationParameters.getLmsParameters()[i].getLMSigParam().getH(), byArray) : new PlaceholderLMSPrivateKey(hSSKeyGenerationParameters.getLmsParameters()[i].getLMSigParam(), hSSKeyGenerationParameters.getLmsParameters()[i].getLMOTSParam(), -1, byArray3, 1 << hSSKeyGenerationParameters.getLmsParameters()[i].getLMSigParam().getH(), byArray3);
            l *= (long)(1 << hSSKeyGenerationParameters.getLmsParameters()[i].getLMSigParam().getH());
        }
        if (l == 0L) {
            l = Long.MAX_VALUE;
        }
        return new HSSPrivateKeyParameters(hSSKeyGenerationParameters.getDepth(), Arrays.asList(lMSPrivateKeyParametersArray), Arrays.asList(lMSSignatureArray), 0L, l);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void incrementIndex(HSSPrivateKeyParameters hSSPrivateKeyParameters) {
        HSSPrivateKeyParameters hSSPrivateKeyParameters2 = hSSPrivateKeyParameters;
        synchronized (hSSPrivateKeyParameters2) {
            HSS.rangeTestKeys(hSSPrivateKeyParameters);
            hSSPrivateKeyParameters.incIndex();
            hSSPrivateKeyParameters.getKeys().get(hSSPrivateKeyParameters.getL() - 1).incIndex();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void rangeTestKeys(HSSPrivateKeyParameters hSSPrivateKeyParameters) {
        HSSPrivateKeyParameters hSSPrivateKeyParameters2 = hSSPrivateKeyParameters;
        synchronized (hSSPrivateKeyParameters2) {
            int n;
            if (hSSPrivateKeyParameters.getIndex() >= hSSPrivateKeyParameters.getIndexLimit()) {
                throw new ExhaustedPrivateKeyException("hss private key" + (hSSPrivateKeyParameters.isShard() ? " shard" : "") + " is exhausted");
            }
            int n2 = n = hSSPrivateKeyParameters.getL();
            List<LMSPrivateKeyParameters> list = hSSPrivateKeyParameters.getKeys();
            while (list.get(n2 - 1).getIndex() == 1 << list.get(n2 - 1).getSigParameters().getH()) {
                if (--n2 != 0) continue;
                throw new ExhaustedPrivateKeyException("hss private key" + (hSSPrivateKeyParameters.isShard() ? " shard" : "") + " is exhausted the maximum limit for this HSS private key");
            }
            while (n2 < n) {
                hSSPrivateKeyParameters.replaceConsumedKey(n2);
                ++n2;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static HSSSignature generateSignature(HSSPrivateKeyParameters hSSPrivateKeyParameters, byte[] byArray) {
        LMSSignedPubKey[] lMSSignedPubKeyArray;
        LMSPrivateKeyParameters lMSPrivateKeyParameters;
        int n = hSSPrivateKeyParameters.getL();
        Object object = hSSPrivateKeyParameters;
        synchronized (object) {
            HSS.rangeTestKeys(hSSPrivateKeyParameters);
            List<LMSPrivateKeyParameters> list = hSSPrivateKeyParameters.getKeys();
            List<LMSSignature> list2 = hSSPrivateKeyParameters.getSig();
            lMSPrivateKeyParameters = hSSPrivateKeyParameters.getKeys().get(n - 1);
            lMSSignedPubKeyArray = new LMSSignedPubKey[n - 1];
            for (int i = 0; i < n - 1; ++i) {
                lMSSignedPubKeyArray[i] = new LMSSignedPubKey(list2.get(i), list.get(i + 1).getPublicKey());
            }
            hSSPrivateKeyParameters.incIndex();
        }
        object = lMSPrivateKeyParameters.generateLMSContext().withSignedPublicKeys(lMSSignedPubKeyArray);
        ((LMSContext)object).update(byArray, 0, byArray.length);
        return HSS.generateSignature(n, (LMSContext)object);
    }

    public static HSSSignature generateSignature(int n, LMSContext lMSContext) {
        return new HSSSignature(n - 1, lMSContext.getSignedPubKeys(), LMS.generateSign(lMSContext));
    }

    public static boolean verifySignature(HSSPublicKeyParameters hSSPublicKeyParameters, HSSSignature hSSSignature, byte[] byArray) {
        int n = hSSSignature.getlMinus1();
        if (n + 1 != hSSPublicKeyParameters.getL()) {
            return false;
        }
        LMSSignature[] lMSSignatureArray = new LMSSignature[n + 1];
        LMSPublicKeyParameters[] lMSPublicKeyParametersArray = new LMSPublicKeyParameters[n];
        for (int i = 0; i < n; ++i) {
            lMSSignatureArray[i] = hSSSignature.getSignedPubKey()[i].getSignature();
            lMSPublicKeyParametersArray[i] = hSSSignature.getSignedPubKey()[i].getPublicKey();
        }
        lMSSignatureArray[n] = hSSSignature.getSignature();
        LMSPublicKeyParameters lMSPublicKeyParameters = hSSPublicKeyParameters.getLMSPublicKey();
        for (int i = 0; i < n; ++i) {
            LMSSignature lMSSignature = lMSSignatureArray[i];
            byte[] byArray2 = lMSPublicKeyParametersArray[i].toByteArray();
            if (!LMS.verifySignature(lMSPublicKeyParameters, lMSSignature, byArray2)) {
                return false;
            }
            try {
                lMSPublicKeyParameters = lMSPublicKeyParametersArray[i];
                continue;
            } catch (Exception exception) {
                throw new IllegalStateException(exception.getMessage(), exception);
            }
        }
        return LMS.verifySignature(lMSPublicKeyParameters, lMSSignatureArray[n], byArray);
    }

    static class PlaceholderLMSPrivateKey
    extends LMSPrivateKeyParameters {
        public PlaceholderLMSPrivateKey(LMSigParameters lMSigParameters, LMOtsParameters lMOtsParameters, int n, byte[] byArray, int n2, byte[] byArray2) {
            super(lMSigParameters, lMOtsParameters, n, byArray, n2, byArray2);
        }

        LMOtsPrivateKey getNextOtsPrivateKey() {
            throw new RuntimeException("placeholder only");
        }

        public LMSPublicKeyParameters getPublicKey() {
            throw new RuntimeException("placeholder only");
        }
    }
}

