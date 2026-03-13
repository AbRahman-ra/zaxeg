/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.newhope;

import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.Xof;
import org.bouncycastle.crypto.digests.SHAKEDigest;
import org.bouncycastle.pqc.crypto.ExchangePair;
import org.bouncycastle.pqc.crypto.newhope.NHAgreement;
import org.bouncycastle.pqc.crypto.newhope.NHExchangePairGenerator;
import org.bouncycastle.pqc.crypto.newhope.NHKeyPairGenerator;
import org.bouncycastle.pqc.crypto.newhope.NHPublicKeyParameters;
import org.bouncycastle.util.Arrays;

public class NHSecretKeyProcessor {
    private final Xof xof = new SHAKEDigest(256);

    private NHSecretKeyProcessor(byte[] byArray, byte[] byArray2) {
        this.xof.update(byArray, 0, byArray.length);
        if (byArray2 != null) {
            this.xof.update(byArray2, 0, byArray2.length);
        }
        Arrays.fill(byArray, (byte)0);
    }

    public byte[] processKey(byte[] byArray) {
        byte[] byArray2 = new byte[byArray.length];
        this.xof.doFinal(byArray2, 0, byArray2.length);
        NHSecretKeyProcessor.xor(byArray, byArray2);
        Arrays.fill(byArray2, (byte)0);
        return byArray;
    }

    private static void xor(byte[] byArray, byte[] byArray2) {
        for (int i = 0; i != byArray.length; ++i) {
            int n = i;
            byArray[n] = (byte)(byArray[n] ^ byArray2[i]);
        }
    }

    public static class PartyUBuilder {
        private final AsymmetricCipherKeyPair aKp;
        private final NHAgreement agreement = new NHAgreement();
        private byte[] sharedInfo = null;
        private boolean used = false;

        public PartyUBuilder(SecureRandom secureRandom) {
            NHKeyPairGenerator nHKeyPairGenerator = new NHKeyPairGenerator();
            nHKeyPairGenerator.init(new KeyGenerationParameters(secureRandom, 2048));
            this.aKp = nHKeyPairGenerator.generateKeyPair();
            this.agreement.init(this.aKp.getPrivate());
        }

        public PartyUBuilder withSharedInfo(byte[] byArray) {
            this.sharedInfo = Arrays.clone(byArray);
            return this;
        }

        public byte[] getPartA() {
            return ((NHPublicKeyParameters)this.aKp.getPublic()).getPubData();
        }

        public NHSecretKeyProcessor build(byte[] byArray) {
            if (this.used) {
                throw new IllegalStateException("builder already used");
            }
            this.used = true;
            return new NHSecretKeyProcessor(this.agreement.calculateAgreement(new NHPublicKeyParameters(byArray)), this.sharedInfo);
        }
    }

    public static class PartyVBuilder {
        protected final SecureRandom random;
        private byte[] sharedInfo = null;
        private byte[] sharedSecret = null;
        private boolean used = false;

        public PartyVBuilder(SecureRandom secureRandom) {
            this.random = secureRandom;
        }

        public PartyVBuilder withSharedInfo(byte[] byArray) {
            this.sharedInfo = Arrays.clone(byArray);
            return this;
        }

        public byte[] getPartB(byte[] byArray) {
            NHExchangePairGenerator nHExchangePairGenerator = new NHExchangePairGenerator(this.random);
            ExchangePair exchangePair = nHExchangePairGenerator.generateExchange(new NHPublicKeyParameters(byArray));
            this.sharedSecret = exchangePair.getSharedValue();
            return ((NHPublicKeyParameters)exchangePair.getPublicKey()).getPubData();
        }

        public NHSecretKeyProcessor build() {
            if (this.used) {
                throw new IllegalStateException("builder already used");
            }
            this.used = true;
            return new NHSecretKeyProcessor(this.sharedSecret, this.sharedInfo);
        }
    }
}

