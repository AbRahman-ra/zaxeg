/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.newhope;

import java.io.IOException;
import java.security.SecureRandom;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.util.DEROtherInfo;
import org.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import org.bouncycastle.pqc.crypto.ExchangePair;
import org.bouncycastle.pqc.crypto.newhope.NHAgreement;
import org.bouncycastle.pqc.crypto.newhope.NHExchangePairGenerator;
import org.bouncycastle.pqc.crypto.newhope.NHKeyPairGenerator;
import org.bouncycastle.pqc.crypto.newhope.NHPublicKeyParameters;

public class NHOtherInfoGenerator {
    protected final DEROtherInfo.Builder otherInfoBuilder;
    protected final SecureRandom random;
    protected boolean used = false;

    public NHOtherInfoGenerator(AlgorithmIdentifier algorithmIdentifier, byte[] byArray, byte[] byArray2, SecureRandom secureRandom) {
        this.otherInfoBuilder = new DEROtherInfo.Builder(algorithmIdentifier, byArray, byArray2);
        this.random = secureRandom;
    }

    private static byte[] getEncoded(NHPublicKeyParameters nHPublicKeyParameters) {
        try {
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PQCObjectIdentifiers.newHope);
            SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(algorithmIdentifier, nHPublicKeyParameters.getPubData());
            return subjectPublicKeyInfo.getEncoded();
        } catch (IOException iOException) {
            return null;
        }
    }

    private static NHPublicKeyParameters getPublicKey(byte[] byArray) {
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(byArray);
        return new NHPublicKeyParameters(subjectPublicKeyInfo.getPublicKeyData().getOctets());
    }

    public static class PartyU
    extends NHOtherInfoGenerator {
        private AsymmetricCipherKeyPair aKp;
        private NHAgreement agreement = new NHAgreement();

        public PartyU(AlgorithmIdentifier algorithmIdentifier, byte[] byArray, byte[] byArray2, SecureRandom secureRandom) {
            super(algorithmIdentifier, byArray, byArray2, secureRandom);
            NHKeyPairGenerator nHKeyPairGenerator = new NHKeyPairGenerator();
            nHKeyPairGenerator.init(new KeyGenerationParameters(secureRandom, 2048));
            this.aKp = nHKeyPairGenerator.generateKeyPair();
            this.agreement.init(this.aKp.getPrivate());
        }

        public NHOtherInfoGenerator withSuppPubInfo(byte[] byArray) {
            this.otherInfoBuilder.withSuppPubInfo(byArray);
            return this;
        }

        public byte[] getSuppPrivInfoPartA() {
            return NHOtherInfoGenerator.getEncoded((NHPublicKeyParameters)this.aKp.getPublic());
        }

        public DEROtherInfo generate(byte[] byArray) {
            if (this.used) {
                throw new IllegalStateException("builder already used");
            }
            this.used = true;
            this.otherInfoBuilder.withSuppPrivInfo(this.agreement.calculateAgreement(NHOtherInfoGenerator.getPublicKey(byArray)));
            return this.otherInfoBuilder.build();
        }
    }

    public static class PartyV
    extends NHOtherInfoGenerator {
        public PartyV(AlgorithmIdentifier algorithmIdentifier, byte[] byArray, byte[] byArray2, SecureRandom secureRandom) {
            super(algorithmIdentifier, byArray, byArray2, secureRandom);
        }

        public NHOtherInfoGenerator withSuppPubInfo(byte[] byArray) {
            this.otherInfoBuilder.withSuppPubInfo(byArray);
            return this;
        }

        public byte[] getSuppPrivInfoPartB(byte[] byArray) {
            NHExchangePairGenerator nHExchangePairGenerator = new NHExchangePairGenerator(this.random);
            ExchangePair exchangePair = nHExchangePairGenerator.generateExchange(NHOtherInfoGenerator.getPublicKey(byArray));
            this.otherInfoBuilder.withSuppPrivInfo(exchangePair.getSharedValue());
            return NHOtherInfoGenerator.getEncoded((NHPublicKeyParameters)exchangePair.getPublicKey());
        }

        public DEROtherInfo generate() {
            if (this.used) {
                throw new IllegalStateException("builder already used");
            }
            this.used = true;
            return this.otherInfoBuilder.build();
        }
    }
}

