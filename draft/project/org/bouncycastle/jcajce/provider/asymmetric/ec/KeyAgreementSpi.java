/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.ec;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.BasicAgreement;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DerivationFunction;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.agreement.ECDHCBasicAgreement;
import org.bouncycastle.crypto.agreement.ECDHCUnifiedAgreement;
import org.bouncycastle.crypto.agreement.ECMQVBasicAgreement;
import org.bouncycastle.crypto.agreement.kdf.ConcatenationKDFGenerator;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.crypto.params.ECDHUPrivateParameters;
import org.bouncycastle.crypto.params.ECDHUPublicParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.MQVPrivateParameters;
import org.bouncycastle.crypto.params.MQVPublicParameters;
import org.bouncycastle.crypto.util.DigestFactory;
import org.bouncycastle.jcajce.provider.asymmetric.ec.ECUtils;
import org.bouncycastle.jcajce.provider.asymmetric.util.BaseAgreementSpi;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jcajce.spec.DHUParameterSpec;
import org.bouncycastle.jcajce.spec.MQVParameterSpec;
import org.bouncycastle.jcajce.spec.UserKeyingMaterialSpec;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.interfaces.MQVPrivateKey;
import org.bouncycastle.jce.interfaces.MQVPublicKey;
import org.bouncycastle.util.Arrays;

public class KeyAgreementSpi
extends BaseAgreementSpi {
    private static final X9IntegerConverter converter = new X9IntegerConverter();
    private String kaAlgorithm;
    private ECDomainParameters parameters;
    private Object agreement;
    private MQVParameterSpec mqvParameters;
    private DHUParameterSpec dheParameters;
    private byte[] result;

    protected KeyAgreementSpi(String string, BasicAgreement basicAgreement, DerivationFunction derivationFunction) {
        super(string, derivationFunction);
        this.kaAlgorithm = string;
        this.agreement = basicAgreement;
    }

    protected KeyAgreementSpi(String string, ECDHCUnifiedAgreement eCDHCUnifiedAgreement, DerivationFunction derivationFunction) {
        super(string, derivationFunction);
        this.kaAlgorithm = string;
        this.agreement = eCDHCUnifiedAgreement;
    }

    protected byte[] bigIntToBytes(BigInteger bigInteger) {
        return converter.integerToBytes(bigInteger, converter.getByteLength(this.parameters.getCurve()));
    }

    protected Key engineDoPhase(Key key, boolean bl) throws InvalidKeyException, IllegalStateException {
        CipherParameters cipherParameters;
        Object object;
        if (this.parameters == null) {
            throw new IllegalStateException(this.kaAlgorithm + " not initialised.");
        }
        if (!bl) {
            throw new IllegalStateException(this.kaAlgorithm + " can only be between two parties.");
        }
        if (this.agreement instanceof ECMQVBasicAgreement) {
            if (!(key instanceof MQVPublicKey)) {
                object = (ECPublicKeyParameters)ECUtils.generatePublicKeyParameter((PublicKey)key);
                ECPublicKeyParameters eCPublicKeyParameters = (ECPublicKeyParameters)ECUtils.generatePublicKeyParameter(this.mqvParameters.getOtherPartyEphemeralKey());
                cipherParameters = new MQVPublicParameters((ECPublicKeyParameters)object, eCPublicKeyParameters);
            } else {
                object = (MQVPublicKey)key;
                ECPublicKeyParameters eCPublicKeyParameters = (ECPublicKeyParameters)ECUtils.generatePublicKeyParameter(object.getStaticKey());
                ECPublicKeyParameters eCPublicKeyParameters2 = (ECPublicKeyParameters)ECUtils.generatePublicKeyParameter(object.getEphemeralKey());
                cipherParameters = new MQVPublicParameters(eCPublicKeyParameters, eCPublicKeyParameters2);
            }
        } else if (this.agreement instanceof ECDHCUnifiedAgreement) {
            object = (ECPublicKeyParameters)ECUtils.generatePublicKeyParameter((PublicKey)key);
            ECPublicKeyParameters eCPublicKeyParameters = (ECPublicKeyParameters)ECUtils.generatePublicKeyParameter(this.dheParameters.getOtherPartyEphemeralKey());
            cipherParameters = new ECDHUPublicParameters((ECPublicKeyParameters)object, eCPublicKeyParameters);
        } else {
            if (!(key instanceof PublicKey)) {
                throw new InvalidKeyException(this.kaAlgorithm + " key agreement requires " + KeyAgreementSpi.getSimpleName(ECPublicKey.class) + " for doPhase");
            }
            cipherParameters = ECUtils.generatePublicKeyParameter((PublicKey)key);
        }
        try {
            this.result = this.agreement instanceof BasicAgreement ? this.bigIntToBytes(((BasicAgreement)this.agreement).calculateAgreement(cipherParameters)) : ((ECDHCUnifiedAgreement)this.agreement).calculateAgreement(cipherParameters);
        } catch (Exception exception) {
            throw new InvalidKeyException("calculation failed: " + exception.getMessage()){

                public Throwable getCause() {
                    return exception;
                }
            };
        }
        return null;
    }

    protected void engineInit(Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (!(algorithmParameterSpec == null || algorithmParameterSpec instanceof MQVParameterSpec || algorithmParameterSpec instanceof UserKeyingMaterialSpec || algorithmParameterSpec instanceof DHUParameterSpec)) {
            throw new InvalidAlgorithmParameterException("No algorithm parameters supported");
        }
        this.initFromKey(key, algorithmParameterSpec);
    }

    protected void engineInit(Key key, SecureRandom secureRandom) throws InvalidKeyException {
        try {
            this.initFromKey(key, null);
        } catch (InvalidAlgorithmParameterException invalidAlgorithmParameterException) {
            throw new InvalidKeyException(invalidAlgorithmParameterException.getMessage());
        }
    }

    private void initFromKey(Key key, AlgorithmParameterSpec algorithmParameterSpec) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (this.agreement instanceof ECMQVBasicAgreement) {
            ECPublicKeyParameters eCPublicKeyParameters;
            ECPrivateKeyParameters eCPrivateKeyParameters;
            ECPrivateKeyParameters eCPrivateKeyParameters2;
            Object object;
            this.mqvParameters = null;
            if (!(key instanceof MQVPrivateKey) && !(algorithmParameterSpec instanceof MQVParameterSpec)) {
                throw new InvalidAlgorithmParameterException(this.kaAlgorithm + " key agreement requires " + KeyAgreementSpi.getSimpleName(MQVParameterSpec.class) + " for initialisation");
            }
            if (key instanceof MQVPrivateKey) {
                object = (MQVPrivateKey)key;
                eCPrivateKeyParameters2 = (ECPrivateKeyParameters)ECUtil.generatePrivateKeyParameter(object.getStaticPrivateKey());
                eCPrivateKeyParameters = (ECPrivateKeyParameters)ECUtil.generatePrivateKeyParameter(object.getEphemeralPrivateKey());
                eCPublicKeyParameters = null;
                if (object.getEphemeralPublicKey() != null) {
                    eCPublicKeyParameters = (ECPublicKeyParameters)ECUtils.generatePublicKeyParameter(object.getEphemeralPublicKey());
                }
            } else {
                object = (MQVParameterSpec)algorithmParameterSpec;
                eCPrivateKeyParameters2 = (ECPrivateKeyParameters)ECUtil.generatePrivateKeyParameter((PrivateKey)key);
                eCPrivateKeyParameters = (ECPrivateKeyParameters)ECUtil.generatePrivateKeyParameter(((MQVParameterSpec)object).getEphemeralPrivateKey());
                eCPublicKeyParameters = null;
                if (((MQVParameterSpec)object).getEphemeralPublicKey() != null) {
                    eCPublicKeyParameters = (ECPublicKeyParameters)ECUtils.generatePublicKeyParameter(((MQVParameterSpec)object).getEphemeralPublicKey());
                }
                this.mqvParameters = object;
                this.ukmParameters = ((MQVParameterSpec)object).getUserKeyingMaterial();
            }
            object = new MQVPrivateParameters(eCPrivateKeyParameters2, eCPrivateKeyParameters, eCPublicKeyParameters);
            this.parameters = eCPrivateKeyParameters2.getParameters();
            ((ECMQVBasicAgreement)this.agreement).init((CipherParameters)object);
        } else if (algorithmParameterSpec instanceof DHUParameterSpec) {
            if (!(this.agreement instanceof ECDHCUnifiedAgreement)) {
                throw new InvalidAlgorithmParameterException(this.kaAlgorithm + " key agreement cannot be used with " + KeyAgreementSpi.getSimpleName(DHUParameterSpec.class));
            }
            DHUParameterSpec dHUParameterSpec = (DHUParameterSpec)algorithmParameterSpec;
            ECPrivateKeyParameters eCPrivateKeyParameters = (ECPrivateKeyParameters)ECUtil.generatePrivateKeyParameter((PrivateKey)key);
            ECPrivateKeyParameters eCPrivateKeyParameters3 = (ECPrivateKeyParameters)ECUtil.generatePrivateKeyParameter(dHUParameterSpec.getEphemeralPrivateKey());
            ECPublicKeyParameters eCPublicKeyParameters = null;
            if (dHUParameterSpec.getEphemeralPublicKey() != null) {
                eCPublicKeyParameters = (ECPublicKeyParameters)ECUtils.generatePublicKeyParameter(dHUParameterSpec.getEphemeralPublicKey());
            }
            this.dheParameters = dHUParameterSpec;
            this.ukmParameters = dHUParameterSpec.getUserKeyingMaterial();
            ECDHUPrivateParameters eCDHUPrivateParameters = new ECDHUPrivateParameters(eCPrivateKeyParameters, eCPrivateKeyParameters3, eCPublicKeyParameters);
            this.parameters = eCPrivateKeyParameters.getParameters();
            ((ECDHCUnifiedAgreement)this.agreement).init(eCDHUPrivateParameters);
        } else {
            if (!(key instanceof PrivateKey)) {
                throw new InvalidKeyException(this.kaAlgorithm + " key agreement requires " + KeyAgreementSpi.getSimpleName(ECPrivateKey.class) + " for initialisation");
            }
            if (this.kdf == null && algorithmParameterSpec instanceof UserKeyingMaterialSpec) {
                throw new InvalidAlgorithmParameterException("no KDF specified for UserKeyingMaterialSpec");
            }
            ECPrivateKeyParameters eCPrivateKeyParameters = (ECPrivateKeyParameters)ECUtil.generatePrivateKeyParameter((PrivateKey)key);
            this.parameters = eCPrivateKeyParameters.getParameters();
            this.ukmParameters = algorithmParameterSpec instanceof UserKeyingMaterialSpec ? ((UserKeyingMaterialSpec)algorithmParameterSpec).getUserKeyingMaterial() : null;
            ((BasicAgreement)this.agreement).init(eCPrivateKeyParameters);
        }
    }

    private static String getSimpleName(Class clazz) {
        String string = clazz.getName();
        return string.substring(string.lastIndexOf(46) + 1);
    }

    protected byte[] calcSecret() {
        return Arrays.clone(this.result);
    }

    public static class CDHwithSHA1KDFAndSharedInfo
    extends KeyAgreementSpi {
        public CDHwithSHA1KDFAndSharedInfo() {
            super("ECCDHwithSHA1KDF", new ECDHCBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class CDHwithSHA224KDFAndSharedInfo
    extends KeyAgreementSpi {
        public CDHwithSHA224KDFAndSharedInfo() {
            super("ECCDHwithSHA224KDF", new ECDHCBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class CDHwithSHA256KDFAndSharedInfo
    extends KeyAgreementSpi {
        public CDHwithSHA256KDFAndSharedInfo() {
            super("ECCDHwithSHA256KDF", new ECDHCBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class CDHwithSHA384KDFAndSharedInfo
    extends KeyAgreementSpi {
        public CDHwithSHA384KDFAndSharedInfo() {
            super("ECCDHwithSHA384KDF", new ECDHCBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class CDHwithSHA512KDFAndSharedInfo
    extends KeyAgreementSpi {
        public CDHwithSHA512KDFAndSharedInfo() {
            super("ECCDHwithSHA512KDF", new ECDHCBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class DH
    extends KeyAgreementSpi {
        public DH() {
            super("ECDH", new ECDHBasicAgreement(), null);
        }
    }

    public static class DHC
    extends KeyAgreementSpi {
        public DHC() {
            super("ECDHC", new ECDHCBasicAgreement(), null);
        }
    }

    public static class DHUC
    extends KeyAgreementSpi {
        public DHUC() {
            super("ECCDHU", new ECDHCUnifiedAgreement(), null);
        }
    }

    public static class DHUwithSHA1CKDF
    extends KeyAgreementSpi {
        public DHUwithSHA1CKDF() {
            super("ECCDHUwithSHA1CKDF", new ECDHCUnifiedAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class DHUwithSHA1KDF
    extends KeyAgreementSpi {
        public DHUwithSHA1KDF() {
            super("ECCDHUwithSHA1KDF", new ECDHCUnifiedAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class DHUwithSHA224CKDF
    extends KeyAgreementSpi {
        public DHUwithSHA224CKDF() {
            super("ECCDHUwithSHA224CKDF", new ECDHCUnifiedAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class DHUwithSHA224KDF
    extends KeyAgreementSpi {
        public DHUwithSHA224KDF() {
            super("ECCDHUwithSHA224KDF", new ECDHCUnifiedAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class DHUwithSHA256CKDF
    extends KeyAgreementSpi {
        public DHUwithSHA256CKDF() {
            super("ECCDHUwithSHA256CKDF", new ECDHCUnifiedAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class DHUwithSHA256KDF
    extends KeyAgreementSpi {
        public DHUwithSHA256KDF() {
            super("ECCDHUwithSHA256KDF", new ECDHCUnifiedAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class DHUwithSHA384CKDF
    extends KeyAgreementSpi {
        public DHUwithSHA384CKDF() {
            super("ECCDHUwithSHA384CKDF", new ECDHCUnifiedAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class DHUwithSHA384KDF
    extends KeyAgreementSpi {
        public DHUwithSHA384KDF() {
            super("ECCDHUwithSHA384KDF", new ECDHCUnifiedAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class DHUwithSHA512CKDF
    extends KeyAgreementSpi {
        public DHUwithSHA512CKDF() {
            super("ECCDHUwithSHA512CKDF", new ECDHCUnifiedAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class DHUwithSHA512KDF
    extends KeyAgreementSpi {
        public DHUwithSHA512KDF() {
            super("ECCDHUwithSHA512KDF", new ECDHCUnifiedAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class DHwithSHA1CKDF
    extends KeyAgreementSpi {
        public DHwithSHA1CKDF() {
            super("ECDHwithSHA1CKDF", new ECDHCBasicAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class DHwithSHA1KDF
    extends KeyAgreementSpi {
        public DHwithSHA1KDF() {
            super("ECDHwithSHA1KDF", new ECDHBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class DHwithSHA1KDFAndSharedInfo
    extends KeyAgreementSpi {
        public DHwithSHA1KDFAndSharedInfo() {
            super("ECDHwithSHA1KDF", new ECDHBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class DHwithSHA224KDFAndSharedInfo
    extends KeyAgreementSpi {
        public DHwithSHA224KDFAndSharedInfo() {
            super("ECDHwithSHA224KDF", new ECDHBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class DHwithSHA256CKDF
    extends KeyAgreementSpi {
        public DHwithSHA256CKDF() {
            super("ECDHwithSHA256CKDF", new ECDHCBasicAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class DHwithSHA256KDFAndSharedInfo
    extends KeyAgreementSpi {
        public DHwithSHA256KDFAndSharedInfo() {
            super("ECDHwithSHA256KDF", new ECDHBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class DHwithSHA384CKDF
    extends KeyAgreementSpi {
        public DHwithSHA384CKDF() {
            super("ECDHwithSHA384CKDF", new ECDHCBasicAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class DHwithSHA384KDFAndSharedInfo
    extends KeyAgreementSpi {
        public DHwithSHA384KDFAndSharedInfo() {
            super("ECDHwithSHA384KDF", new ECDHBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class DHwithSHA512CKDF
    extends KeyAgreementSpi {
        public DHwithSHA512CKDF() {
            super("ECDHwithSHA512CKDF", new ECDHCBasicAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class DHwithSHA512KDFAndSharedInfo
    extends KeyAgreementSpi {
        public DHwithSHA512KDFAndSharedInfo() {
            super("ECDHwithSHA512KDF", new ECDHBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class ECKAEGwithRIPEMD160KDF
    extends KeyAgreementSpi {
        public ECKAEGwithRIPEMD160KDF() {
            super("ECKAEGwithRIPEMD160KDF", new ECDHBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(new RIPEMD160Digest()));
        }
    }

    public static class ECKAEGwithSHA1KDF
    extends KeyAgreementSpi {
        public ECKAEGwithSHA1KDF() {
            super("ECKAEGwithSHA1KDF", new ECDHBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class ECKAEGwithSHA224KDF
    extends KeyAgreementSpi {
        public ECKAEGwithSHA224KDF() {
            super("ECKAEGwithSHA224KDF", new ECDHBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class ECKAEGwithSHA256KDF
    extends KeyAgreementSpi {
        public ECKAEGwithSHA256KDF() {
            super("ECKAEGwithSHA256KDF", new ECDHBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class ECKAEGwithSHA384KDF
    extends KeyAgreementSpi {
        public ECKAEGwithSHA384KDF() {
            super("ECKAEGwithSHA384KDF", new ECDHBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class ECKAEGwithSHA512KDF
    extends KeyAgreementSpi {
        public ECKAEGwithSHA512KDF() {
            super("ECKAEGwithSHA512KDF", new ECDHBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class MQV
    extends KeyAgreementSpi {
        public MQV() {
            super("ECMQV", new ECMQVBasicAgreement(), null);
        }
    }

    public static class MQVwithSHA1CKDF
    extends KeyAgreementSpi {
        public MQVwithSHA1CKDF() {
            super("ECMQVwithSHA1CKDF", new ECMQVBasicAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class MQVwithSHA1KDF
    extends KeyAgreementSpi {
        public MQVwithSHA1KDF() {
            super("ECMQVwithSHA1KDF", new ECMQVBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class MQVwithSHA1KDFAndSharedInfo
    extends KeyAgreementSpi {
        public MQVwithSHA1KDFAndSharedInfo() {
            super("ECMQVwithSHA1KDF", new ECMQVBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA1()));
        }
    }

    public static class MQVwithSHA224CKDF
    extends KeyAgreementSpi {
        public MQVwithSHA224CKDF() {
            super("ECMQVwithSHA224CKDF", new ECMQVBasicAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class MQVwithSHA224KDF
    extends KeyAgreementSpi {
        public MQVwithSHA224KDF() {
            super("ECMQVwithSHA224KDF", new ECMQVBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class MQVwithSHA224KDFAndSharedInfo
    extends KeyAgreementSpi {
        public MQVwithSHA224KDFAndSharedInfo() {
            super("ECMQVwithSHA224KDF", new ECMQVBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA224()));
        }
    }

    public static class MQVwithSHA256CKDF
    extends KeyAgreementSpi {
        public MQVwithSHA256CKDF() {
            super("ECMQVwithSHA256CKDF", new ECMQVBasicAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class MQVwithSHA256KDF
    extends KeyAgreementSpi {
        public MQVwithSHA256KDF() {
            super("ECMQVwithSHA256KDF", new ECMQVBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class MQVwithSHA256KDFAndSharedInfo
    extends KeyAgreementSpi {
        public MQVwithSHA256KDFAndSharedInfo() {
            super("ECMQVwithSHA256KDF", new ECMQVBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA256()));
        }
    }

    public static class MQVwithSHA384CKDF
    extends KeyAgreementSpi {
        public MQVwithSHA384CKDF() {
            super("ECMQVwithSHA384CKDF", new ECMQVBasicAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class MQVwithSHA384KDF
    extends KeyAgreementSpi {
        public MQVwithSHA384KDF() {
            super("ECMQVwithSHA384KDF", new ECMQVBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class MQVwithSHA384KDFAndSharedInfo
    extends KeyAgreementSpi {
        public MQVwithSHA384KDFAndSharedInfo() {
            super("ECMQVwithSHA384KDF", new ECMQVBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA384()));
        }
    }

    public static class MQVwithSHA512CKDF
    extends KeyAgreementSpi {
        public MQVwithSHA512CKDF() {
            super("ECMQVwithSHA512CKDF", new ECMQVBasicAgreement(), (DerivationFunction)new ConcatenationKDFGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class MQVwithSHA512KDF
    extends KeyAgreementSpi {
        public MQVwithSHA512KDF() {
            super("ECMQVwithSHA512KDF", new ECMQVBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA512()));
        }
    }

    public static class MQVwithSHA512KDFAndSharedInfo
    extends KeyAgreementSpi {
        public MQVwithSHA512KDFAndSharedInfo() {
            super("ECMQVwithSHA512KDF", new ECMQVBasicAgreement(), (DerivationFunction)new KDF2BytesGenerator(DigestFactory.createSHA512()));
        }
    }
}

