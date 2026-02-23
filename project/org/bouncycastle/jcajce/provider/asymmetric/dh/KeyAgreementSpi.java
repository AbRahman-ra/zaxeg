/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.dh;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.BasicAgreement;
import org.bouncycastle.crypto.DerivationFunction;
import org.bouncycastle.crypto.agreement.DHUnifiedAgreement;
import org.bouncycastle.crypto.agreement.MQVBasicAgreement;
import org.bouncycastle.crypto.agreement.kdf.ConcatenationKDFGenerator;
import org.bouncycastle.crypto.agreement.kdf.DHKEKGenerator;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.crypto.params.DHMQVPrivateParameters;
import org.bouncycastle.crypto.params.DHMQVPublicParameters;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;
import org.bouncycastle.crypto.params.DHUPrivateParameters;
import org.bouncycastle.crypto.params.DHUPublicParameters;
import org.bouncycastle.crypto.util.DigestFactory;
import org.bouncycastle.jcajce.provider.asymmetric.dh.BCDHPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.dh.BCDHPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.BaseAgreementSpi;
import org.bouncycastle.jcajce.spec.DHDomainParameterSpec;
import org.bouncycastle.jcajce.spec.DHUParameterSpec;
import org.bouncycastle.jcajce.spec.MQVParameterSpec;
import org.bouncycastle.jcajce.spec.UserKeyingMaterialSpec;

public class KeyAgreementSpi
extends BaseAgreementSpi {
    private static final BigInteger ONE = BigInteger.valueOf(1L);
    private static final BigInteger TWO = BigInteger.valueOf(2L);
    private final DHUnifiedAgreement unifiedAgreement;
    private final BasicAgreement mqvAgreement;
    private DHUParameterSpec dheParameters;
    private MQVParameterSpec mqvParameters;
    private BigInteger x;
    private BigInteger p;
    private BigInteger g;
    private byte[] result;

    public KeyAgreementSpi() {
        this("Diffie-Hellman", null);
    }

    public KeyAgreementSpi(String string, DerivationFunction derivationFunction) {
        super(string, derivationFunction);
        this.unifiedAgreement = null;
        this.mqvAgreement = null;
    }

    public KeyAgreementSpi(String string, DHUnifiedAgreement dHUnifiedAgreement, DerivationFunction derivationFunction) {
        super(string, derivationFunction);
        this.unifiedAgreement = dHUnifiedAgreement;
        this.mqvAgreement = null;
    }

    public KeyAgreementSpi(String string, BasicAgreement basicAgreement, DerivationFunction derivationFunction) {
        super(string, derivationFunction);
        this.unifiedAgreement = null;
        this.mqvAgreement = basicAgreement;
    }

    protected byte[] bigIntToBytes(BigInteger bigInteger) {
        int n = (this.p.bitLength() + 7) / 8;
        byte[] byArray = bigInteger.toByteArray();
        if (byArray.length == n) {
            return byArray;
        }
        if (byArray[0] == 0 && byArray.length == n + 1) {
            byte[] byArray2 = new byte[byArray.length - 1];
            System.arraycopy(byArray, 1, byArray2, 0, byArray2.length);
            return byArray2;
        }
        byte[] byArray3 = new byte[n];
        System.arraycopy(byArray, 0, byArray3, byArray3.length - byArray.length, byArray.length);
        return byArray3;
    }

    protected Key engineDoPhase(Key key, boolean bl) throws InvalidKeyException, IllegalStateException {
        if (this.x == null) {
            throw new IllegalStateException("Diffie-Hellman not initialised.");
        }
        if (!(key instanceof DHPublicKey)) {
            throw new InvalidKeyException("DHKeyAgreement doPhase requires DHPublicKey");
        }
        DHPublicKey dHPublicKey = (DHPublicKey)key;
        if (!dHPublicKey.getParams().getG().equals(this.g) || !dHPublicKey.getParams().getP().equals(this.p)) {
            throw new InvalidKeyException("DHPublicKey not for this KeyAgreement!");
        }
        BigInteger bigInteger = ((DHPublicKey)key).getY();
        if (bigInteger == null || bigInteger.compareTo(TWO) < 0 || bigInteger.compareTo(this.p.subtract(ONE)) >= 0) {
            throw new InvalidKeyException("Invalid DH PublicKey");
        }
        if (this.unifiedAgreement != null) {
            if (!bl) {
                throw new IllegalStateException("unified Diffie-Hellman can use only two key pairs");
            }
            DHPublicKeyParameters dHPublicKeyParameters = this.generatePublicKeyParameter((PublicKey)key);
            DHPublicKeyParameters dHPublicKeyParameters2 = this.generatePublicKeyParameter(this.dheParameters.getOtherPartyEphemeralKey());
            DHUPublicParameters dHUPublicParameters = new DHUPublicParameters(dHPublicKeyParameters, dHPublicKeyParameters2);
            this.result = this.unifiedAgreement.calculateAgreement(dHUPublicParameters);
            return null;
        }
        if (this.mqvAgreement != null) {
            if (!bl) {
                throw new IllegalStateException("MQV Diffie-Hellman can use only two key pairs");
            }
            DHPublicKeyParameters dHPublicKeyParameters = this.generatePublicKeyParameter((PublicKey)key);
            DHPublicKeyParameters dHPublicKeyParameters3 = this.generatePublicKeyParameter(this.mqvParameters.getOtherPartyEphemeralKey());
            DHMQVPublicParameters dHMQVPublicParameters = new DHMQVPublicParameters(dHPublicKeyParameters, dHPublicKeyParameters3);
            this.result = this.bigIntToBytes(this.mqvAgreement.calculateAgreement(dHMQVPublicParameters));
            return null;
        }
        BigInteger bigInteger2 = bigInteger.modPow(this.x, this.p);
        if (bigInteger2.compareTo(ONE) == 0) {
            throw new InvalidKeyException("Shared key can't be 1");
        }
        this.result = this.bigIntToBytes(bigInteger2);
        if (bl) {
            return null;
        }
        return new BCDHPublicKey(bigInteger2, dHPublicKey.getParams());
    }

    protected byte[] engineGenerateSecret() throws IllegalStateException {
        if (this.x == null) {
            throw new IllegalStateException("Diffie-Hellman not initialised.");
        }
        return super.engineGenerateSecret();
    }

    protected int engineGenerateSecret(byte[] byArray, int n) throws IllegalStateException, ShortBufferException {
        if (this.x == null) {
            throw new IllegalStateException("Diffie-Hellman not initialised.");
        }
        return super.engineGenerateSecret(byArray, n);
    }

    protected SecretKey engineGenerateSecret(String string) throws NoSuchAlgorithmException {
        if (this.x == null) {
            throw new IllegalStateException("Diffie-Hellman not initialised.");
        }
        if (string.equals("TlsPremasterSecret")) {
            return new SecretKeySpec(KeyAgreementSpi.trimZeroes(this.result), string);
        }
        return super.engineGenerateSecret(string);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected void engineInit(Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (!(key instanceof DHPrivateKey)) {
            throw new InvalidKeyException("DHKeyAgreement requires DHPrivateKey for initialisation");
        }
        DHPrivateKey dHPrivateKey = (DHPrivateKey)key;
        if (algorithmParameterSpec != null) {
            if (algorithmParameterSpec instanceof DHParameterSpec) {
                DHParameterSpec dHParameterSpec = (DHParameterSpec)algorithmParameterSpec;
                this.p = dHParameterSpec.getP();
                this.g = dHParameterSpec.getG();
                this.dheParameters = null;
                this.ukmParameters = null;
            } else if (algorithmParameterSpec instanceof DHUParameterSpec) {
                if (this.unifiedAgreement == null) {
                    throw new InvalidAlgorithmParameterException("agreement algorithm not DHU based");
                }
                this.p = dHPrivateKey.getParams().getP();
                this.g = dHPrivateKey.getParams().getG();
                this.dheParameters = (DHUParameterSpec)algorithmParameterSpec;
                this.ukmParameters = ((DHUParameterSpec)algorithmParameterSpec).getUserKeyingMaterial();
                if (this.dheParameters.getEphemeralPublicKey() != null) {
                    this.unifiedAgreement.init(new DHUPrivateParameters(this.generatePrivateKeyParameter(dHPrivateKey), this.generatePrivateKeyParameter(this.dheParameters.getEphemeralPrivateKey()), this.generatePublicKeyParameter(this.dheParameters.getEphemeralPublicKey())));
                } else {
                    this.unifiedAgreement.init(new DHUPrivateParameters(this.generatePrivateKeyParameter(dHPrivateKey), this.generatePrivateKeyParameter(this.dheParameters.getEphemeralPrivateKey())));
                }
            } else if (algorithmParameterSpec instanceof MQVParameterSpec) {
                if (this.mqvAgreement == null) {
                    throw new InvalidAlgorithmParameterException("agreement algorithm not MQV based");
                }
                this.p = dHPrivateKey.getParams().getP();
                this.g = dHPrivateKey.getParams().getG();
                this.mqvParameters = (MQVParameterSpec)algorithmParameterSpec;
                this.ukmParameters = ((MQVParameterSpec)algorithmParameterSpec).getUserKeyingMaterial();
                if (this.mqvParameters.getEphemeralPublicKey() != null) {
                    this.mqvAgreement.init(new DHMQVPrivateParameters(this.generatePrivateKeyParameter(dHPrivateKey), this.generatePrivateKeyParameter(this.mqvParameters.getEphemeralPrivateKey()), this.generatePublicKeyParameter(this.mqvParameters.getEphemeralPublicKey())));
                } else {
                    this.mqvAgreement.init(new DHMQVPrivateParameters(this.generatePrivateKeyParameter(dHPrivateKey), this.generatePrivateKeyParameter(this.mqvParameters.getEphemeralPrivateKey())));
                }
            } else {
                if (!(algorithmParameterSpec instanceof UserKeyingMaterialSpec)) throw new InvalidAlgorithmParameterException("DHKeyAgreement only accepts DHParameterSpec");
                if (this.kdf == null) {
                    throw new InvalidAlgorithmParameterException("no KDF specified for UserKeyingMaterialSpec");
                }
                this.p = dHPrivateKey.getParams().getP();
                this.g = dHPrivateKey.getParams().getG();
                this.dheParameters = null;
                this.ukmParameters = ((UserKeyingMaterialSpec)algorithmParameterSpec).getUserKeyingMaterial();
            }
        } else {
            this.p = dHPrivateKey.getParams().getP();
            this.g = dHPrivateKey.getParams().getG();
        }
        this.x = dHPrivateKey.getX();
        this.result = this.bigIntToBytes(this.x);
    }

    protected void engineInit(Key key, SecureRandom secureRandom) throws InvalidKeyException {
        if (!(key instanceof DHPrivateKey)) {
            throw new InvalidKeyException("DHKeyAgreement requires DHPrivateKey");
        }
        DHPrivateKey dHPrivateKey = (DHPrivateKey)key;
        this.p = dHPrivateKey.getParams().getP();
        this.g = dHPrivateKey.getParams().getG();
        this.x = dHPrivateKey.getX();
        this.result = this.bigIntToBytes(this.x);
    }

    protected byte[] calcSecret() {
        return this.result;
    }

    private DHPrivateKeyParameters generatePrivateKeyParameter(PrivateKey privateKey) throws InvalidKeyException {
        if (privateKey instanceof DHPrivateKey) {
            if (privateKey instanceof BCDHPrivateKey) {
                return ((BCDHPrivateKey)privateKey).engineGetKeyParameters();
            }
            DHPrivateKey dHPrivateKey = (DHPrivateKey)privateKey;
            DHParameterSpec dHParameterSpec = dHPrivateKey.getParams();
            return new DHPrivateKeyParameters(dHPrivateKey.getX(), new DHParameters(dHParameterSpec.getP(), dHParameterSpec.getG(), null, dHParameterSpec.getL()));
        }
        throw new InvalidKeyException("private key not a DHPrivateKey");
    }

    private DHPublicKeyParameters generatePublicKeyParameter(PublicKey publicKey) throws InvalidKeyException {
        if (publicKey instanceof DHPublicKey) {
            if (publicKey instanceof BCDHPublicKey) {
                return ((BCDHPublicKey)publicKey).engineGetKeyParameters();
            }
            DHPublicKey dHPublicKey = (DHPublicKey)publicKey;
            DHParameterSpec dHParameterSpec = dHPublicKey.getParams();
            if (dHParameterSpec instanceof DHDomainParameterSpec) {
                return new DHPublicKeyParameters(dHPublicKey.getY(), ((DHDomainParameterSpec)dHParameterSpec).getDomainParameters());
            }
            return new DHPublicKeyParameters(dHPublicKey.getY(), new DHParameters(dHParameterSpec.getP(), dHParameterSpec.getG(), null, dHParameterSpec.getL()));
        }
        throw new InvalidKeyException("public key not a DHPublicKey");
    }

    public static class DHUwithSHA1CKDF
    extends KeyAgreementSpi {
        public DHUwithSHA1CKDF() {
            super("DHUwithSHA1CKDF", new DHUnifiedAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class DHUwithSHA1KDF
    extends KeyAgreementSpi {
        public DHUwithSHA1KDF() {
            super("DHUwithSHA1KDF", new DHUnifiedAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class DHUwithSHA224CKDF
    extends KeyAgreementSpi {
        public DHUwithSHA224CKDF() {
            super("DHUwithSHA224CKDF", new DHUnifiedAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class DHUwithSHA224KDF
    extends KeyAgreementSpi {
        public DHUwithSHA224KDF() {
            super("DHUwithSHA224KDF", new DHUnifiedAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class DHUwithSHA256CKDF
    extends KeyAgreementSpi {
        public DHUwithSHA256CKDF() {
            super("DHUwithSHA256CKDF", new DHUnifiedAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class DHUwithSHA256KDF
    extends KeyAgreementSpi {
        public DHUwithSHA256KDF() {
            super("DHUwithSHA256KDF", new DHUnifiedAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class DHUwithSHA384CKDF
    extends KeyAgreementSpi {
        public DHUwithSHA384CKDF() {
            super("DHUwithSHA384CKDF", new DHUnifiedAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class DHUwithSHA384KDF
    extends KeyAgreementSpi {
        public DHUwithSHA384KDF() {
            super("DHUwithSHA384KDF", new DHUnifiedAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class DHUwithSHA512CKDF
    extends KeyAgreementSpi {
        public DHUwithSHA512CKDF() {
            super("DHUwithSHA512CKDF", new DHUnifiedAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class DHUwithSHA512KDF
    extends KeyAgreementSpi {
        public DHUwithSHA512KDF() {
            super("DHUwithSHA512KDF", new DHUnifiedAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class DHwithRFC2631KDF
    extends KeyAgreementSpi {
        public DHwithRFC2631KDF() {
            super("DHwithRFC2631KDF", new DHKEKGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class DHwithSHA1CKDF
    extends KeyAgreementSpi {
        public DHwithSHA1CKDF() {
            super("DHwithSHA1CKDF", new ConcatenationKDFGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class DHwithSHA1KDF
    extends KeyAgreementSpi {
        public DHwithSHA1KDF() {
            super("DHwithSHA1CKDF", new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class DHwithSHA224CKDF
    extends KeyAgreementSpi {
        public DHwithSHA224CKDF() {
            super("DHwithSHA224CKDF", new ConcatenationKDFGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class DHwithSHA224KDF
    extends KeyAgreementSpi {
        public DHwithSHA224KDF() {
            super("DHwithSHA224CKDF", new KDF2BytesGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class DHwithSHA256CKDF
    extends KeyAgreementSpi {
        public DHwithSHA256CKDF() {
            super("DHwithSHA256CKDF", new ConcatenationKDFGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class DHwithSHA256KDF
    extends KeyAgreementSpi {
        public DHwithSHA256KDF() {
            super("DHwithSHA256CKDF", new KDF2BytesGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class DHwithSHA384CKDF
    extends KeyAgreementSpi {
        public DHwithSHA384CKDF() {
            super("DHwithSHA384CKDF", new ConcatenationKDFGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class DHwithSHA384KDF
    extends KeyAgreementSpi {
        public DHwithSHA384KDF() {
            super("DHwithSHA384KDF", new KDF2BytesGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class DHwithSHA512CKDF
    extends KeyAgreementSpi {
        public DHwithSHA512CKDF() {
            super("DHwithSHA512CKDF", new ConcatenationKDFGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class DHwithSHA512KDF
    extends KeyAgreementSpi {
        public DHwithSHA512KDF() {
            super("DHwithSHA512KDF", new KDF2BytesGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class MQVwithSHA1CKDF
    extends KeyAgreementSpi {
        public MQVwithSHA1CKDF() {
            super("MQVwithSHA1CKDF", new MQVBasicAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class MQVwithSHA1KDF
    extends KeyAgreementSpi {
        public MQVwithSHA1KDF() {
            super("MQVwithSHA1KDF", new MQVBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class MQVwithSHA224CKDF
    extends KeyAgreementSpi {
        public MQVwithSHA224CKDF() {
            super("MQVwithSHA224CKDF", new MQVBasicAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class MQVwithSHA224KDF
    extends KeyAgreementSpi {
        public MQVwithSHA224KDF() {
            super("MQVwithSHA224KDF", new MQVBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class MQVwithSHA256CKDF
    extends KeyAgreementSpi {
        public MQVwithSHA256CKDF() {
            super("MQVwithSHA256CKDF", new MQVBasicAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class MQVwithSHA256KDF
    extends KeyAgreementSpi {
        public MQVwithSHA256KDF() {
            super("MQVwithSHA256KDF", new MQVBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class MQVwithSHA384CKDF
    extends KeyAgreementSpi {
        public MQVwithSHA384CKDF() {
            super("MQVwithSHA384CKDF", new MQVBasicAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class MQVwithSHA384KDF
    extends KeyAgreementSpi {
        public MQVwithSHA384KDF() {
            super("MQVwithSHA384KDF", new MQVBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class MQVwithSHA512CKDF
    extends KeyAgreementSpi {
        public MQVwithSHA512CKDF() {
            super("MQVwithSHA512CKDF", new MQVBasicAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class MQVwithSHA512KDF
    extends KeyAgreementSpi {
        public MQVwithSHA512KDF() {
            super("MQVwithSHA512KDF", new MQVBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA512()));
        }
    }
}

