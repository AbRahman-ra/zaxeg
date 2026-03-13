/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pkcs.jcajce;

import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.Provider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.cryptopro.GOST28147Parameters;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.ScryptParams;
import org.bouncycastle.asn1.pkcs.PBEParameter;
import org.bouncycastle.asn1.pkcs.PBES2Parameters;
import org.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.PasswordConverter;
import org.bouncycastle.jcajce.PBKDF1Key;
import org.bouncycastle.jcajce.PKCS12KeyWithParameters;
import org.bouncycastle.jcajce.io.CipherInputStream;
import org.bouncycastle.jcajce.spec.GOST28147ParameterSpec;
import org.bouncycastle.jcajce.spec.PBKDF2KeySpec;
import org.bouncycastle.jcajce.spec.ScryptKeySpec;
import org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jcajce.util.NamedJcaJceHelper;
import org.bouncycastle.jcajce.util.ProviderJcaJceHelper;
import org.bouncycastle.operator.DefaultSecretKeySizeProvider;
import org.bouncycastle.operator.InputDecryptor;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.SecretKeySizeProvider;

public class JcePKCSPBEInputDecryptorProviderBuilder {
    private JcaJceHelper helper = new DefaultJcaJceHelper();
    private boolean wrongPKCS12Zero = false;
    private SecretKeySizeProvider keySizeProvider = DefaultSecretKeySizeProvider.INSTANCE;

    public JcePKCSPBEInputDecryptorProviderBuilder setProvider(Provider provider) {
        this.helper = new ProviderJcaJceHelper(provider);
        return this;
    }

    public JcePKCSPBEInputDecryptorProviderBuilder setProvider(String string) {
        this.helper = new NamedJcaJceHelper(string);
        return this;
    }

    public JcePKCSPBEInputDecryptorProviderBuilder setTryWrongPKCS12Zero(boolean bl) {
        this.wrongPKCS12Zero = bl;
        return this;
    }

    public JcePKCSPBEInputDecryptorProviderBuilder setKeySizeProvider(SecretKeySizeProvider secretKeySizeProvider) {
        this.keySizeProvider = secretKeySizeProvider;
        return this;
    }

    public InputDecryptorProvider build(final char[] cArray) {
        return new InputDecryptorProvider(){
            private Cipher cipher;
            private AlgorithmIdentifier encryptionAlg;

            public InputDecryptor get(AlgorithmIdentifier algorithmIdentifier) throws OperatorCreationException {
                block13: {
                    ASN1ObjectIdentifier aSN1ObjectIdentifier = algorithmIdentifier.getAlgorithm();
                    try {
                        if (aSN1ObjectIdentifier.on(PKCSObjectIdentifiers.pkcs_12PbeIds)) {
                            PKCS12PBEParams pKCS12PBEParams = PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters());
                            this.cipher = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createCipher(aSN1ObjectIdentifier.getId());
                            this.cipher.init(2, new PKCS12KeyWithParameters(cArray, JcePKCSPBEInputDecryptorProviderBuilder.this.wrongPKCS12Zero, pKCS12PBEParams.getIV(), pKCS12PBEParams.getIterations().intValue()));
                            this.encryptionAlg = algorithmIdentifier;
                            break block13;
                        }
                        if (aSN1ObjectIdentifier.equals(PKCSObjectIdentifiers.id_PBES2)) {
                            SecretKey secretKey;
                            Object object;
                            Object object2;
                            PBES2Parameters pBES2Parameters = PBES2Parameters.getInstance(algorithmIdentifier.getParameters());
                            if (MiscObjectIdentifiers.id_scrypt.equals(pBES2Parameters.getKeyDerivationFunc().getAlgorithm())) {
                                object2 = ScryptParams.getInstance(pBES2Parameters.getKeyDerivationFunc().getParameters());
                                object = AlgorithmIdentifier.getInstance(pBES2Parameters.getEncryptionScheme());
                                SecretKeyFactory secretKeyFactory = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createSecretKeyFactory("SCRYPT");
                                secretKey = secretKeyFactory.generateSecret(new ScryptKeySpec(cArray, ((ScryptParams)object2).getSalt(), ((ScryptParams)object2).getCostParameter().intValue(), ((ScryptParams)object2).getBlockSize().intValue(), ((ScryptParams)object2).getParallelizationParameter().intValue(), JcePKCSPBEInputDecryptorProviderBuilder.this.keySizeProvider.getKeySize((AlgorithmIdentifier)object)));
                            } else {
                                object2 = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createSecretKeyFactory(pBES2Parameters.getKeyDerivationFunc().getAlgorithm().getId());
                                object = PBKDF2Params.getInstance(pBES2Parameters.getKeyDerivationFunc().getParameters());
                                AlgorithmIdentifier algorithmIdentifier2 = AlgorithmIdentifier.getInstance(pBES2Parameters.getEncryptionScheme());
                                secretKey = ((PBKDF2Params)object).isDefaultPrf() ? ((SecretKeyFactory)object2).generateSecret(new PBEKeySpec(cArray, ((PBKDF2Params)object).getSalt(), ((PBKDF2Params)object).getIterationCount().intValue(), JcePKCSPBEInputDecryptorProviderBuilder.this.keySizeProvider.getKeySize(algorithmIdentifier2))) : ((SecretKeyFactory)object2).generateSecret(new PBKDF2KeySpec(cArray, ((PBKDF2Params)object).getSalt(), ((PBKDF2Params)object).getIterationCount().intValue(), JcePKCSPBEInputDecryptorProviderBuilder.this.keySizeProvider.getKeySize(algorithmIdentifier2), ((PBKDF2Params)object).getPrf()));
                            }
                            this.cipher = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createCipher(pBES2Parameters.getEncryptionScheme().getAlgorithm().getId());
                            this.encryptionAlg = AlgorithmIdentifier.getInstance(pBES2Parameters.getEncryptionScheme());
                            object2 = pBES2Parameters.getEncryptionScheme().getParameters();
                            if (object2 instanceof ASN1OctetString) {
                                this.cipher.init(2, (Key)secretKey, new IvParameterSpec(ASN1OctetString.getInstance(object2).getOctets()));
                            } else if (object2 instanceof ASN1Sequence && JcePKCSPBEInputDecryptorProviderBuilder.this.isCCMorGCM(pBES2Parameters.getEncryptionScheme())) {
                                object = AlgorithmParameters.getInstance(pBES2Parameters.getEncryptionScheme().getAlgorithm().getId());
                                ((AlgorithmParameters)object).init(((ASN1Sequence)object2).getEncoded());
                                this.cipher.init(2, (Key)secretKey, (AlgorithmParameters)object);
                            } else if (object2 == null) {
                                this.cipher.init(2, secretKey);
                            } else {
                                object = GOST28147Parameters.getInstance(object2);
                                this.cipher.init(2, (Key)secretKey, new GOST28147ParameterSpec(((GOST28147Parameters)object).getEncryptionParamSet(), ((GOST28147Parameters)object).getIV()));
                            }
                            break block13;
                        }
                        if (aSN1ObjectIdentifier.equals(PKCSObjectIdentifiers.pbeWithMD5AndDES_CBC) || aSN1ObjectIdentifier.equals(PKCSObjectIdentifiers.pbeWithSHA1AndDES_CBC)) {
                            PBEParameter pBEParameter = PBEParameter.getInstance(algorithmIdentifier.getParameters());
                            this.cipher = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createCipher(aSN1ObjectIdentifier.getId());
                            this.cipher.init(2, (Key)new PBKDF1Key(cArray, PasswordConverter.ASCII), new PBEParameterSpec(pBEParameter.getSalt(), pBEParameter.getIterationCount().intValue()));
                            break block13;
                        }
                        throw new OperatorCreationException("unable to create InputDecryptor: algorithm " + aSN1ObjectIdentifier + " unknown.");
                    } catch (Exception exception) {
                        throw new OperatorCreationException("unable to create InputDecryptor: " + exception.getMessage(), exception);
                    }
                }
                return new InputDecryptor(){

                    public AlgorithmIdentifier getAlgorithmIdentifier() {
                        return encryptionAlg;
                    }

                    public InputStream getInputStream(InputStream inputStream) {
                        return new CipherInputStream(inputStream, cipher);
                    }
                };
            }
        };
    }

    private boolean isCCMorGCM(ASN1Encodable aSN1Encodable) {
        ASN1Sequence aSN1Sequence;
        AlgorithmIdentifier algorithmIdentifier = AlgorithmIdentifier.getInstance(aSN1Encodable);
        ASN1Encodable aSN1Encodable2 = algorithmIdentifier.getParameters();
        if (aSN1Encodable2 instanceof ASN1Sequence && (aSN1Sequence = ASN1Sequence.getInstance(aSN1Encodable2)).size() == 2) {
            return aSN1Sequence.getObjectAt(1) instanceof ASN1Integer;
        }
        return false;
    }
}

