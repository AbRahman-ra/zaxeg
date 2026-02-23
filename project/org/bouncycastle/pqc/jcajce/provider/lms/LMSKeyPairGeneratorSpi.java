/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.jcajce.provider.lms;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.pqc.crypto.lms.HSSKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.lms.HSSKeyPairGenerator;
import org.bouncycastle.pqc.crypto.lms.HSSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.lms.HSSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMOtsParameters;
import org.bouncycastle.pqc.crypto.lms.LMSKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.lms.LMSKeyPairGenerator;
import org.bouncycastle.pqc.crypto.lms.LMSParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSigParameters;
import org.bouncycastle.pqc.jcajce.provider.lms.BCLMSPrivateKey;
import org.bouncycastle.pqc.jcajce.provider.lms.BCLMSPublicKey;
import org.bouncycastle.pqc.jcajce.spec.LMSHSSKeyGenParameterSpec;
import org.bouncycastle.pqc.jcajce.spec.LMSHSSParameterSpec;
import org.bouncycastle.pqc.jcajce.spec.LMSKeyGenParameterSpec;
import org.bouncycastle.pqc.jcajce.spec.LMSParameterSpec;

public class LMSKeyPairGeneratorSpi
extends KeyPairGenerator {
    private KeyGenerationParameters param;
    private ASN1ObjectIdentifier treeDigest;
    private AsymmetricCipherKeyPairGenerator engine = new LMSKeyPairGenerator();
    private SecureRandom random = CryptoServicesRegistrar.getSecureRandom();
    private boolean initialised = false;

    public LMSKeyPairGeneratorSpi() {
        super("LMS");
    }

    public void initialize(int n, SecureRandom secureRandom) {
        throw new IllegalArgumentException("use AlgorithmParameterSpec");
    }

    public void initialize(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException {
        if (algorithmParameterSpec instanceof LMSKeyGenParameterSpec) {
            LMSKeyGenParameterSpec lMSKeyGenParameterSpec = (LMSKeyGenParameterSpec)algorithmParameterSpec;
            this.param = new LMSKeyGenerationParameters(new LMSParameters(lMSKeyGenParameterSpec.getSigParams(), lMSKeyGenParameterSpec.getOtsParams()), secureRandom);
            this.engine = new LMSKeyPairGenerator();
            this.engine.init(this.param);
        } else if (algorithmParameterSpec instanceof LMSHSSKeyGenParameterSpec) {
            LMSKeyGenParameterSpec[] lMSKeyGenParameterSpecArray = ((LMSHSSKeyGenParameterSpec)algorithmParameterSpec).getLMSSpecs();
            LMSParameters[] lMSParametersArray = new LMSParameters[lMSKeyGenParameterSpecArray.length];
            for (int i = 0; i != lMSKeyGenParameterSpecArray.length; ++i) {
                lMSParametersArray[i] = new LMSParameters(lMSKeyGenParameterSpecArray[i].getSigParams(), lMSKeyGenParameterSpecArray[i].getOtsParams());
            }
            this.param = new HSSKeyGenerationParameters(lMSParametersArray, secureRandom);
            this.engine = new HSSKeyPairGenerator();
            this.engine.init(this.param);
        } else if (algorithmParameterSpec instanceof LMSParameterSpec) {
            LMSParameterSpec lMSParameterSpec = (LMSParameterSpec)algorithmParameterSpec;
            this.param = new LMSKeyGenerationParameters(new LMSParameters(lMSParameterSpec.getSigParams(), lMSParameterSpec.getOtsParams()), secureRandom);
            this.engine = new LMSKeyPairGenerator();
            this.engine.init(this.param);
        } else if (algorithmParameterSpec instanceof LMSHSSParameterSpec) {
            LMSParameterSpec[] lMSParameterSpecArray = ((LMSHSSParameterSpec)algorithmParameterSpec).getLMSSpecs();
            LMSParameters[] lMSParametersArray = new LMSParameters[lMSParameterSpecArray.length];
            for (int i = 0; i != lMSParameterSpecArray.length; ++i) {
                lMSParametersArray[i] = new LMSParameters(lMSParameterSpecArray[i].getSigParams(), lMSParameterSpecArray[i].getOtsParams());
            }
            this.param = new HSSKeyGenerationParameters(lMSParametersArray, secureRandom);
            this.engine = new HSSKeyPairGenerator();
            this.engine.init(this.param);
        } else {
            throw new InvalidAlgorithmParameterException("parameter object not a LMSParameterSpec/LMSHSSParameterSpec");
        }
        this.initialised = true;
    }

    public KeyPair generateKeyPair() {
        if (!this.initialised) {
            this.param = new LMSKeyGenerationParameters(new LMSParameters(LMSigParameters.lms_sha256_n32_h10, LMOtsParameters.sha256_n32_w2), this.random);
            this.engine.init(this.param);
            this.initialised = true;
        }
        AsymmetricCipherKeyPair asymmetricCipherKeyPair = this.engine.generateKeyPair();
        if (this.engine instanceof LMSKeyPairGenerator) {
            LMSPublicKeyParameters lMSPublicKeyParameters = (LMSPublicKeyParameters)asymmetricCipherKeyPair.getPublic();
            LMSPrivateKeyParameters lMSPrivateKeyParameters = (LMSPrivateKeyParameters)asymmetricCipherKeyPair.getPrivate();
            return new KeyPair(new BCLMSPublicKey(lMSPublicKeyParameters), new BCLMSPrivateKey(lMSPrivateKeyParameters));
        }
        HSSPublicKeyParameters hSSPublicKeyParameters = (HSSPublicKeyParameters)asymmetricCipherKeyPair.getPublic();
        HSSPrivateKeyParameters hSSPrivateKeyParameters = (HSSPrivateKeyParameters)asymmetricCipherKeyPair.getPrivate();
        return new KeyPair(new BCLMSPublicKey(hSSPublicKeyParameters), new BCLMSPrivateKey(hSSPrivateKeyParameters));
    }
}

