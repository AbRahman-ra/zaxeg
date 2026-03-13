/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.keystore.bcfks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAKey;
import java.security.interfaces.RSAKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.bc.EncryptedObjectStoreData;
import org.bouncycastle.asn1.bc.EncryptedPrivateKeyData;
import org.bouncycastle.asn1.bc.EncryptedSecretKeyData;
import org.bouncycastle.asn1.bc.ObjectData;
import org.bouncycastle.asn1.bc.ObjectDataSequence;
import org.bouncycastle.asn1.bc.ObjectStore;
import org.bouncycastle.asn1.bc.ObjectStoreData;
import org.bouncycastle.asn1.bc.ObjectStoreIntegrityCheck;
import org.bouncycastle.asn1.bc.PbkdMacIntegrityCheck;
import org.bouncycastle.asn1.bc.SecretKeyData;
import org.bouncycastle.asn1.bc.SignatureCheck;
import org.bouncycastle.asn1.kisa.KISAObjectIdentifiers;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.ScryptParams;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.nsri.NSRIObjectIdentifiers;
import org.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.EncryptionScheme;
import org.bouncycastle.asn1.pkcs.KeyDerivationFunc;
import org.bouncycastle.asn1.pkcs.PBES2Parameters;
import org.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.util.PBKDF2Config;
import org.bouncycastle.crypto.util.PBKDFConfig;
import org.bouncycastle.crypto.util.ScryptConfig;
import org.bouncycastle.internal.asn1.cms.CCMParameters;
import org.bouncycastle.jcajce.BCFKSLoadStoreParameter;
import org.bouncycastle.jcajce.BCFKSStoreParameter;
import org.bouncycastle.jcajce.BCLoadStoreParameter;
import org.bouncycastle.jcajce.provider.keystore.util.AdaptingKeyStoreSpi;
import org.bouncycastle.jcajce.provider.keystore.util.ParameterUtil;
import org.bouncycastle.jcajce.util.BCJcaJceHelper;
import org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jce.interfaces.ECKey;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class BcFKSKeyStoreSpi
extends KeyStoreSpi {
    private static final Map<String, ASN1ObjectIdentifier> oidMap = new HashMap<String, ASN1ObjectIdentifier>();
    private static final Map<ASN1ObjectIdentifier, String> publicAlgMap = new HashMap<ASN1ObjectIdentifier, String>();
    private PublicKey verificationKey;
    private BCFKSLoadStoreParameter.CertChainValidator validator;
    private static final BigInteger CERTIFICATE;
    private static final BigInteger PRIVATE_KEY;
    private static final BigInteger SECRET_KEY;
    private static final BigInteger PROTECTED_PRIVATE_KEY;
    private static final BigInteger PROTECTED_SECRET_KEY;
    private final JcaJceHelper helper;
    private final Map<String, ObjectData> entries = new HashMap<String, ObjectData>();
    private final Map<String, PrivateKey> privateKeyCache = new HashMap<String, PrivateKey>();
    private AlgorithmIdentifier hmacAlgorithm;
    private KeyDerivationFunc hmacPkbdAlgorithm;
    private AlgorithmIdentifier signatureAlgorithm;
    private Date creationDate;
    private Date lastModifiedDate;
    private ASN1ObjectIdentifier storeEncryptionAlgorithm = NISTObjectIdentifiers.id_aes256_CCM;

    private static String getPublicKeyAlg(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        String string = publicAlgMap.get(aSN1ObjectIdentifier);
        if (string != null) {
            return string;
        }
        return aSN1ObjectIdentifier.getId();
    }

    BcFKSKeyStoreSpi(JcaJceHelper jcaJceHelper) {
        this.helper = jcaJceHelper;
    }

    @Override
    public Key engineGetKey(String string, char[] cArray) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        ObjectData objectData = this.entries.get(string);
        if (objectData != null) {
            if (objectData.getType().equals(PRIVATE_KEY) || objectData.getType().equals(PROTECTED_PRIVATE_KEY)) {
                PrivateKey privateKey = this.privateKeyCache.get(string);
                if (privateKey != null) {
                    return privateKey;
                }
                EncryptedPrivateKeyData encryptedPrivateKeyData = EncryptedPrivateKeyData.getInstance(objectData.getData());
                EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = EncryptedPrivateKeyInfo.getInstance(encryptedPrivateKeyData.getEncryptedPrivateKeyInfo());
                try {
                    PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(this.decryptData("PRIVATE_KEY_ENCRYPTION", encryptedPrivateKeyInfo.getEncryptionAlgorithm(), cArray, encryptedPrivateKeyInfo.getEncryptedData()));
                    KeyFactory keyFactory = this.helper.createKeyFactory(BcFKSKeyStoreSpi.getPublicKeyAlg(privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm()));
                    PrivateKey privateKey2 = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyInfo.getEncoded()));
                    this.privateKeyCache.put(string, privateKey2);
                    return privateKey2;
                } catch (Exception exception) {
                    throw new UnrecoverableKeyException("BCFKS KeyStore unable to recover private key (" + string + "): " + exception.getMessage());
                }
            }
            if (objectData.getType().equals(SECRET_KEY) || objectData.getType().equals(PROTECTED_SECRET_KEY)) {
                EncryptedSecretKeyData encryptedSecretKeyData = EncryptedSecretKeyData.getInstance(objectData.getData());
                try {
                    SecretKeyData secretKeyData = SecretKeyData.getInstance(this.decryptData("SECRET_KEY_ENCRYPTION", encryptedSecretKeyData.getKeyEncryptionAlgorithm(), cArray, encryptedSecretKeyData.getEncryptedKeyData()));
                    SecretKeyFactory secretKeyFactory = this.helper.createSecretKeyFactory(secretKeyData.getKeyAlgorithm().getId());
                    return secretKeyFactory.generateSecret(new SecretKeySpec(secretKeyData.getKeyBytes(), secretKeyData.getKeyAlgorithm().getId()));
                } catch (Exception exception) {
                    throw new UnrecoverableKeyException("BCFKS KeyStore unable to recover secret key (" + string + "): " + exception.getMessage());
                }
            }
            throw new UnrecoverableKeyException("BCFKS KeyStore unable to recover secret key (" + string + "): type not recognized");
        }
        return null;
    }

    @Override
    public java.security.cert.Certificate[] engineGetCertificateChain(String string) {
        ObjectData objectData = this.entries.get(string);
        if (objectData != null && (objectData.getType().equals(PRIVATE_KEY) || objectData.getType().equals(PROTECTED_PRIVATE_KEY))) {
            EncryptedPrivateKeyData encryptedPrivateKeyData = EncryptedPrivateKeyData.getInstance(objectData.getData());
            Certificate[] certificateArray = encryptedPrivateKeyData.getCertificateChain();
            java.security.cert.Certificate[] certificateArray2 = new X509Certificate[certificateArray.length];
            for (int i = 0; i != certificateArray2.length; ++i) {
                certificateArray2[i] = this.decodeCertificate(certificateArray[i]);
            }
            return certificateArray2;
        }
        return null;
    }

    @Override
    public java.security.cert.Certificate engineGetCertificate(String string) {
        ObjectData objectData = this.entries.get(string);
        if (objectData != null) {
            if (objectData.getType().equals(PRIVATE_KEY) || objectData.getType().equals(PROTECTED_PRIVATE_KEY)) {
                EncryptedPrivateKeyData encryptedPrivateKeyData = EncryptedPrivateKeyData.getInstance(objectData.getData());
                Certificate[] certificateArray = encryptedPrivateKeyData.getCertificateChain();
                return this.decodeCertificate(certificateArray[0]);
            }
            if (objectData.getType().equals(CERTIFICATE)) {
                return this.decodeCertificate(objectData.getData());
            }
        }
        return null;
    }

    private java.security.cert.Certificate decodeCertificate(Object object) {
        if (this.helper != null) {
            try {
                CertificateFactory certificateFactory = this.helper.createCertificateFactory("X.509");
                return certificateFactory.generateCertificate(new ByteArrayInputStream(Certificate.getInstance(object).getEncoded()));
            } catch (Exception exception) {
                return null;
            }
        }
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return certificateFactory.generateCertificate(new ByteArrayInputStream(Certificate.getInstance(object).getEncoded()));
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    public Date engineGetCreationDate(String string) {
        ObjectData objectData = this.entries.get(string);
        if (objectData != null) {
            try {
                return objectData.getLastModifiedDate().getDate();
            } catch (ParseException parseException) {
                return new Date();
            }
        }
        return null;
    }

    @Override
    public void engineSetKeyEntry(String string, Key key, char[] cArray, java.security.cert.Certificate[] certificateArray) throws KeyStoreException {
        Date date;
        Date date2 = date = new Date();
        ObjectData objectData = this.entries.get(string);
        if (objectData != null) {
            date = this.extractCreationDate(objectData, date);
        }
        this.privateKeyCache.remove(string);
        if (key instanceof PrivateKey) {
            if (certificateArray == null) {
                throw new KeyStoreException("BCFKS KeyStore requires a certificate chain for private key storage.");
            }
            try {
                EncryptedPrivateKeyInfo encryptedPrivateKeyInfo;
                Object object;
                byte[] byArray = key.getEncoded();
                KeyDerivationFunc keyDerivationFunc = this.generatePkbdAlgorithmIdentifier(PKCSObjectIdentifiers.id_PBKDF2, 32);
                byte[] byArray2 = this.generateKey(keyDerivationFunc, "PRIVATE_KEY_ENCRYPTION", cArray != null ? cArray : new char[]{}, 32);
                if (this.storeEncryptionAlgorithm.equals(NISTObjectIdentifiers.id_aes256_CCM)) {
                    object = this.createCipher("AES/CCM/NoPadding", byArray2);
                    byte[] byArray3 = ((Cipher)object).doFinal(byArray);
                    AlgorithmParameters algorithmParameters = ((Cipher)object).getParameters();
                    PBES2Parameters pBES2Parameters = new PBES2Parameters(keyDerivationFunc, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_CCM, CCMParameters.getInstance(algorithmParameters.getEncoded())));
                    encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBES2, pBES2Parameters), byArray3);
                } else {
                    object = this.createCipher("AESKWP", byArray2);
                    byte[] byArray4 = ((Cipher)object).doFinal(byArray);
                    PBES2Parameters pBES2Parameters = new PBES2Parameters(keyDerivationFunc, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_wrap_pad));
                    encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBES2, pBES2Parameters), byArray4);
                }
                object = this.createPrivateKeySequence(encryptedPrivateKeyInfo, certificateArray);
                this.entries.put(string, new ObjectData(PRIVATE_KEY, string, date, date2, ((ASN1Object)object).getEncoded(), null));
            } catch (Exception exception) {
                throw new ExtKeyStoreException("BCFKS KeyStore exception storing private key: " + exception.toString(), exception);
            }
        } else if (key instanceof SecretKey) {
            if (certificateArray != null) {
                throw new KeyStoreException("BCFKS KeyStore cannot store certificate chain with secret key.");
            }
            try {
                ASN1Object aSN1Object;
                SecretKeyData secretKeyData;
                byte[] byArray = key.getEncoded();
                KeyDerivationFunc keyDerivationFunc = this.generatePkbdAlgorithmIdentifier(PKCSObjectIdentifiers.id_PBKDF2, 32);
                byte[] byArray5 = this.generateKey(keyDerivationFunc, "SECRET_KEY_ENCRYPTION", cArray != null ? cArray : new char[]{}, 32);
                String string2 = Strings.toUpperCase(key.getAlgorithm());
                if (string2.indexOf("AES") > -1) {
                    secretKeyData = new SecretKeyData(NISTObjectIdentifiers.aes, byArray);
                } else {
                    aSN1Object = oidMap.get(string2);
                    if (aSN1Object != null) {
                        secretKeyData = new SecretKeyData((ASN1ObjectIdentifier)aSN1Object, byArray);
                    } else {
                        aSN1Object = oidMap.get(string2 + "." + byArray.length * 8);
                        if (aSN1Object != null) {
                            secretKeyData = new SecretKeyData((ASN1ObjectIdentifier)aSN1Object, byArray);
                        } else {
                            throw new KeyStoreException("BCFKS KeyStore cannot recognize secret key (" + string2 + ") for storage.");
                        }
                    }
                }
                if (this.storeEncryptionAlgorithm.equals(NISTObjectIdentifiers.id_aes256_CCM)) {
                    Cipher cipher = this.createCipher("AES/CCM/NoPadding", byArray5);
                    byte[] byArray6 = cipher.doFinal(secretKeyData.getEncoded());
                    AlgorithmParameters algorithmParameters = cipher.getParameters();
                    PBES2Parameters pBES2Parameters = new PBES2Parameters(keyDerivationFunc, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_CCM, CCMParameters.getInstance(algorithmParameters.getEncoded())));
                    aSN1Object = new EncryptedSecretKeyData(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBES2, pBES2Parameters), byArray6);
                } else {
                    Cipher cipher = this.createCipher("AESKWP", byArray5);
                    byte[] byArray7 = cipher.doFinal(secretKeyData.getEncoded());
                    PBES2Parameters pBES2Parameters = new PBES2Parameters(keyDerivationFunc, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_wrap_pad));
                    aSN1Object = new EncryptedSecretKeyData(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBES2, pBES2Parameters), byArray7);
                }
                this.entries.put(string, new ObjectData(SECRET_KEY, string, date, date2, aSN1Object.getEncoded(), null));
            } catch (Exception exception) {
                throw new ExtKeyStoreException("BCFKS KeyStore exception storing private key: " + exception.toString(), exception);
            }
        } else {
            throw new KeyStoreException("BCFKS KeyStore unable to recognize key.");
        }
        this.lastModifiedDate = date2;
    }

    private Cipher createCipher(String string, byte[] byArray) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException {
        Cipher cipher = this.helper.createCipher(string);
        cipher.init(1, new SecretKeySpec(byArray, "AES"));
        return cipher;
    }

    private SecureRandom getDefaultSecureRandom() {
        return CryptoServicesRegistrar.getSecureRandom();
    }

    private EncryptedPrivateKeyData createPrivateKeySequence(EncryptedPrivateKeyInfo encryptedPrivateKeyInfo, java.security.cert.Certificate[] certificateArray) throws CertificateEncodingException {
        Certificate[] certificateArray2 = new Certificate[certificateArray.length];
        for (int i = 0; i != certificateArray.length; ++i) {
            certificateArray2[i] = Certificate.getInstance(certificateArray[i].getEncoded());
        }
        return new EncryptedPrivateKeyData(encryptedPrivateKeyInfo, certificateArray2);
    }

    @Override
    public void engineSetKeyEntry(String string, byte[] byArray, java.security.cert.Certificate[] certificateArray) throws KeyStoreException {
        Date date;
        Date date2 = date = new Date();
        ObjectData objectData = this.entries.get(string);
        if (objectData != null) {
            date = this.extractCreationDate(objectData, date);
        }
        if (certificateArray != null) {
            EncryptedPrivateKeyInfo encryptedPrivateKeyInfo;
            try {
                encryptedPrivateKeyInfo = EncryptedPrivateKeyInfo.getInstance(byArray);
            } catch (Exception exception) {
                throw new ExtKeyStoreException("BCFKS KeyStore private key encoding must be an EncryptedPrivateKeyInfo.", exception);
            }
            try {
                this.privateKeyCache.remove(string);
                this.entries.put(string, new ObjectData(PROTECTED_PRIVATE_KEY, string, date, date2, this.createPrivateKeySequence(encryptedPrivateKeyInfo, certificateArray).getEncoded(), null));
            } catch (Exception exception) {
                throw new ExtKeyStoreException("BCFKS KeyStore exception storing protected private key: " + exception.toString(), exception);
            }
        }
        try {
            this.entries.put(string, new ObjectData(PROTECTED_SECRET_KEY, string, date, date2, byArray, null));
        } catch (Exception exception) {
            throw new ExtKeyStoreException("BCFKS KeyStore exception storing protected private key: " + exception.toString(), exception);
        }
        this.lastModifiedDate = date2;
    }

    @Override
    public void engineSetCertificateEntry(String string, java.security.cert.Certificate certificate) throws KeyStoreException {
        Date date;
        ObjectData objectData = this.entries.get(string);
        Date date2 = date = new Date();
        if (objectData != null) {
            if (!objectData.getType().equals(CERTIFICATE)) {
                throw new KeyStoreException("BCFKS KeyStore already has a key entry with alias " + string);
            }
            date = this.extractCreationDate(objectData, date);
        }
        try {
            this.entries.put(string, new ObjectData(CERTIFICATE, string, date, date2, certificate.getEncoded(), null));
        } catch (CertificateEncodingException certificateEncodingException) {
            throw new ExtKeyStoreException("BCFKS KeyStore unable to handle certificate: " + certificateEncodingException.getMessage(), certificateEncodingException);
        }
        this.lastModifiedDate = date2;
    }

    private Date extractCreationDate(ObjectData objectData, Date date) {
        try {
            date = objectData.getCreationDate().getDate();
        } catch (ParseException parseException) {
            // empty catch block
        }
        return date;
    }

    @Override
    public void engineDeleteEntry(String string) throws KeyStoreException {
        ObjectData objectData = this.entries.get(string);
        if (objectData == null) {
            return;
        }
        this.privateKeyCache.remove(string);
        this.entries.remove(string);
        this.lastModifiedDate = new Date();
    }

    @Override
    public Enumeration<String> engineAliases() {
        final Iterator<String> iterator = new HashSet<String>(this.entries.keySet()).iterator();
        return new Enumeration(){

            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            public Object nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public boolean engineContainsAlias(String string) {
        if (string == null) {
            throw new NullPointerException("alias value is null");
        }
        return this.entries.containsKey(string);
    }

    @Override
    public int engineSize() {
        return this.entries.size();
    }

    @Override
    public boolean engineIsKeyEntry(String string) {
        ObjectData objectData = this.entries.get(string);
        if (objectData != null) {
            BigInteger bigInteger = objectData.getType();
            return bigInteger.equals(PRIVATE_KEY) || bigInteger.equals(SECRET_KEY) || bigInteger.equals(PROTECTED_PRIVATE_KEY) || bigInteger.equals(PROTECTED_SECRET_KEY);
        }
        return false;
    }

    @Override
    public boolean engineIsCertificateEntry(String string) {
        ObjectData objectData = this.entries.get(string);
        if (objectData != null) {
            return objectData.getType().equals(CERTIFICATE);
        }
        return false;
    }

    @Override
    public String engineGetCertificateAlias(java.security.cert.Certificate certificate) {
        byte[] byArray;
        if (certificate == null) {
            return null;
        }
        try {
            byArray = certificate.getEncoded();
        } catch (CertificateEncodingException certificateEncodingException) {
            return null;
        }
        for (String string : this.entries.keySet()) {
            ObjectData objectData = this.entries.get(string);
            if (objectData.getType().equals(CERTIFICATE)) {
                if (!Arrays.areEqual(objectData.getData(), byArray)) continue;
                return string;
            }
            if (!objectData.getType().equals(PRIVATE_KEY) && !objectData.getType().equals(PROTECTED_PRIVATE_KEY)) continue;
            try {
                EncryptedPrivateKeyData encryptedPrivateKeyData = EncryptedPrivateKeyData.getInstance(objectData.getData());
                if (!Arrays.areEqual(encryptedPrivateKeyData.getCertificateChain()[0].toASN1Primitive().getEncoded(), byArray)) continue;
                return string;
            } catch (IOException iOException) {
            }
        }
        return null;
    }

    private byte[] generateKey(KeyDerivationFunc keyDerivationFunc, String string, char[] cArray, int n) throws IOException {
        byte[] byArray = PBEParametersGenerator.PKCS12PasswordToBytes(cArray);
        byte[] byArray2 = PBEParametersGenerator.PKCS12PasswordToBytes(string.toCharArray());
        int n2 = n;
        if (MiscObjectIdentifiers.id_scrypt.equals(keyDerivationFunc.getAlgorithm())) {
            ScryptParams scryptParams = ScryptParams.getInstance(keyDerivationFunc.getParameters());
            if (scryptParams.getKeyLength() != null) {
                n2 = scryptParams.getKeyLength().intValue();
            } else if (n2 == -1) {
                throw new IOException("no keyLength found in ScryptParams");
            }
            return SCrypt.generate(Arrays.concatenate(byArray, byArray2), scryptParams.getSalt(), scryptParams.getCostParameter().intValue(), scryptParams.getBlockSize().intValue(), scryptParams.getBlockSize().intValue(), n2);
        }
        if (keyDerivationFunc.getAlgorithm().equals(PKCSObjectIdentifiers.id_PBKDF2)) {
            PBKDF2Params pBKDF2Params = PBKDF2Params.getInstance(keyDerivationFunc.getParameters());
            if (pBKDF2Params.getKeyLength() != null) {
                n2 = pBKDF2Params.getKeyLength().intValue();
            } else if (n2 == -1) {
                throw new IOException("no keyLength found in PBKDF2Params");
            }
            if (pBKDF2Params.getPrf().getAlgorithm().equals(PKCSObjectIdentifiers.id_hmacWithSHA512)) {
                PKCS5S2ParametersGenerator pKCS5S2ParametersGenerator = new PKCS5S2ParametersGenerator(new SHA512Digest());
                pKCS5S2ParametersGenerator.init(Arrays.concatenate(byArray, byArray2), pBKDF2Params.getSalt(), pBKDF2Params.getIterationCount().intValue());
                return ((KeyParameter)pKCS5S2ParametersGenerator.generateDerivedParameters(n2 * 8)).getKey();
            }
            if (pBKDF2Params.getPrf().getAlgorithm().equals(NISTObjectIdentifiers.id_hmacWithSHA3_512)) {
                PKCS5S2ParametersGenerator pKCS5S2ParametersGenerator = new PKCS5S2ParametersGenerator(new SHA3Digest(512));
                pKCS5S2ParametersGenerator.init(Arrays.concatenate(byArray, byArray2), pBKDF2Params.getSalt(), pBKDF2Params.getIterationCount().intValue());
                return ((KeyParameter)pKCS5S2ParametersGenerator.generateDerivedParameters(n2 * 8)).getKey();
            }
            throw new IOException("BCFKS KeyStore: unrecognized MAC PBKD PRF: " + pBKDF2Params.getPrf().getAlgorithm());
        }
        throw new IOException("BCFKS KeyStore: unrecognized MAC PBKD.");
    }

    private void verifySig(ASN1Encodable aSN1Encodable, SignatureCheck signatureCheck, PublicKey publicKey) throws GeneralSecurityException, IOException {
        Signature signature = this.helper.createSignature(signatureCheck.getSignatureAlgorithm().getAlgorithm().getId());
        signature.initVerify(publicKey);
        signature.update(aSN1Encodable.toASN1Primitive().getEncoded("DER"));
        if (!signature.verify(signatureCheck.getSignature().getOctets())) {
            throw new IOException("BCFKS KeyStore corrupted: signature calculation failed");
        }
    }

    private void verifyMac(byte[] byArray, PbkdMacIntegrityCheck pbkdMacIntegrityCheck, char[] cArray) throws NoSuchAlgorithmException, IOException, NoSuchProviderException {
        byte[] byArray2 = this.calculateMac(byArray, pbkdMacIntegrityCheck.getMacAlgorithm(), pbkdMacIntegrityCheck.getPbkdAlgorithm(), cArray);
        if (!Arrays.constantTimeAreEqual(byArray2, pbkdMacIntegrityCheck.getMac())) {
            throw new IOException("BCFKS KeyStore corrupted: MAC calculation failed");
        }
    }

    private byte[] calculateMac(byte[] byArray, AlgorithmIdentifier algorithmIdentifier, KeyDerivationFunc keyDerivationFunc, char[] cArray) throws NoSuchAlgorithmException, IOException, NoSuchProviderException {
        String string = algorithmIdentifier.getAlgorithm().getId();
        Mac mac = this.helper.createMac(string);
        try {
            mac.init(new SecretKeySpec(this.generateKey(keyDerivationFunc, "INTEGRITY_CHECK", cArray != null ? cArray : new char[]{}, -1), string));
        } catch (InvalidKeyException invalidKeyException) {
            throw new IOException("Cannot set up MAC calculation: " + invalidKeyException.getMessage());
        }
        return mac.doFinal(byArray);
    }

    @Override
    public void engineStore(KeyStore.LoadStoreParameter loadStoreParameter) throws CertificateException, NoSuchAlgorithmException, IOException {
        if (loadStoreParameter == null) {
            throw new IllegalArgumentException("'parameter' arg cannot be null");
        }
        if (loadStoreParameter instanceof BCFKSStoreParameter) {
            BCFKSStoreParameter bCFKSStoreParameter = (BCFKSStoreParameter)loadStoreParameter;
            char[] cArray = ParameterUtil.extractPassword(loadStoreParameter);
            this.hmacPkbdAlgorithm = this.generatePkbdAlgorithmIdentifier(bCFKSStoreParameter.getStorePBKDFConfig(), 64);
            this.engineStore(bCFKSStoreParameter.getOutputStream(), cArray);
        } else if (loadStoreParameter instanceof BCFKSLoadStoreParameter) {
            BCFKSLoadStoreParameter bCFKSLoadStoreParameter = (BCFKSLoadStoreParameter)loadStoreParameter;
            if (bCFKSLoadStoreParameter.getStoreSignatureKey() != null) {
                this.signatureAlgorithm = this.generateSignatureAlgId(bCFKSLoadStoreParameter.getStoreSignatureKey(), bCFKSLoadStoreParameter.getStoreSignatureAlgorithm());
                this.hmacPkbdAlgorithm = this.generatePkbdAlgorithmIdentifier(bCFKSLoadStoreParameter.getStorePBKDFConfig(), 64);
                this.storeEncryptionAlgorithm = bCFKSLoadStoreParameter.getStoreEncryptionAlgorithm() == BCFKSLoadStoreParameter.EncryptionAlgorithm.AES256_CCM ? NISTObjectIdentifiers.id_aes256_CCM : NISTObjectIdentifiers.id_aes256_wrap_pad;
                this.hmacAlgorithm = bCFKSLoadStoreParameter.getStoreMacAlgorithm() == BCFKSLoadStoreParameter.MacAlgorithm.HmacSHA512 ? new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA512, DERNull.INSTANCE) : new AlgorithmIdentifier(NISTObjectIdentifiers.id_hmacWithSHA3_512, DERNull.INSTANCE);
                char[] cArray = ParameterUtil.extractPassword(bCFKSLoadStoreParameter);
                EncryptedObjectStoreData encryptedObjectStoreData = this.getEncryptedObjectStoreData(this.signatureAlgorithm, cArray);
                try {
                    SignatureCheck signatureCheck;
                    Object object;
                    Signature signature = this.helper.createSignature(this.signatureAlgorithm.getAlgorithm().getId());
                    signature.initSign((PrivateKey)bCFKSLoadStoreParameter.getStoreSignatureKey());
                    signature.update(encryptedObjectStoreData.getEncoded());
                    X509Certificate[] x509CertificateArray = bCFKSLoadStoreParameter.getStoreCertificates();
                    if (x509CertificateArray != null) {
                        object = new Certificate[x509CertificateArray.length];
                        for (int i = 0; i != ((Certificate[])object).length; ++i) {
                            object[i] = Certificate.getInstance(x509CertificateArray[i].getEncoded());
                        }
                        signatureCheck = new SignatureCheck(this.signatureAlgorithm, (Certificate[])object, signature.sign());
                    } else {
                        signatureCheck = new SignatureCheck(this.signatureAlgorithm, signature.sign());
                    }
                    object = new ObjectStore(encryptedObjectStoreData, new ObjectStoreIntegrityCheck(signatureCheck));
                    bCFKSLoadStoreParameter.getOutputStream().write(((ASN1Object)object).getEncoded());
                    bCFKSLoadStoreParameter.getOutputStream().flush();
                } catch (GeneralSecurityException generalSecurityException) {
                    throw new IOException("error creating signature: " + generalSecurityException.getMessage(), generalSecurityException);
                }
            } else {
                char[] cArray = ParameterUtil.extractPassword(bCFKSLoadStoreParameter);
                this.hmacPkbdAlgorithm = this.generatePkbdAlgorithmIdentifier(bCFKSLoadStoreParameter.getStorePBKDFConfig(), 64);
                this.storeEncryptionAlgorithm = bCFKSLoadStoreParameter.getStoreEncryptionAlgorithm() == BCFKSLoadStoreParameter.EncryptionAlgorithm.AES256_CCM ? NISTObjectIdentifiers.id_aes256_CCM : NISTObjectIdentifiers.id_aes256_wrap_pad;
                this.hmacAlgorithm = bCFKSLoadStoreParameter.getStoreMacAlgorithm() == BCFKSLoadStoreParameter.MacAlgorithm.HmacSHA512 ? new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA512, DERNull.INSTANCE) : new AlgorithmIdentifier(NISTObjectIdentifiers.id_hmacWithSHA3_512, DERNull.INSTANCE);
                this.engineStore(bCFKSLoadStoreParameter.getOutputStream(), cArray);
            }
        } else if (loadStoreParameter instanceof BCLoadStoreParameter) {
            BCLoadStoreParameter bCLoadStoreParameter = (BCLoadStoreParameter)loadStoreParameter;
            this.engineStore(bCLoadStoreParameter.getOutputStream(), ParameterUtil.extractPassword(loadStoreParameter));
        } else {
            throw new IllegalArgumentException("no support for 'parameter' of type " + loadStoreParameter.getClass().getName());
        }
    }

    @Override
    public void engineStore(OutputStream outputStream, char[] cArray) throws IOException, NoSuchAlgorithmException, CertificateException {
        Object object;
        if (this.creationDate == null) {
            throw new IOException("KeyStore not initialized");
        }
        EncryptedObjectStoreData encryptedObjectStoreData = this.getEncryptedObjectStoreData(this.hmacAlgorithm, cArray);
        if (MiscObjectIdentifiers.id_scrypt.equals(this.hmacPkbdAlgorithm.getAlgorithm())) {
            object = ScryptParams.getInstance(this.hmacPkbdAlgorithm.getParameters());
            this.hmacPkbdAlgorithm = this.generatePkbdAlgorithmIdentifier(this.hmacPkbdAlgorithm, ((ScryptParams)object).getKeyLength().intValue());
        } else {
            object = PBKDF2Params.getInstance(this.hmacPkbdAlgorithm.getParameters());
            this.hmacPkbdAlgorithm = this.generatePkbdAlgorithmIdentifier(this.hmacPkbdAlgorithm, ((PBKDF2Params)object).getKeyLength().intValue());
        }
        try {
            object = this.calculateMac(encryptedObjectStoreData.getEncoded(), this.hmacAlgorithm, this.hmacPkbdAlgorithm, cArray);
        } catch (NoSuchProviderException noSuchProviderException) {
            throw new IOException("cannot calculate mac: " + noSuchProviderException.getMessage());
        }
        ObjectStore objectStore = new ObjectStore(encryptedObjectStoreData, new ObjectStoreIntegrityCheck(new PbkdMacIntegrityCheck(this.hmacAlgorithm, this.hmacPkbdAlgorithm, (byte[])object)));
        outputStream.write(objectStore.getEncoded());
        outputStream.flush();
    }

    private EncryptedObjectStoreData getEncryptedObjectStoreData(AlgorithmIdentifier algorithmIdentifier, char[] cArray) throws IOException, NoSuchAlgorithmException {
        EncryptedObjectStoreData encryptedObjectStoreData;
        ObjectData[] objectDataArray = this.entries.values().toArray(new ObjectData[this.entries.size()]);
        KeyDerivationFunc keyDerivationFunc = this.generatePkbdAlgorithmIdentifier(this.hmacPkbdAlgorithm, 32);
        byte[] byArray = this.generateKey(keyDerivationFunc, "STORE_ENCRYPTION", cArray != null ? cArray : new char[]{}, 32);
        ObjectStoreData objectStoreData = new ObjectStoreData(algorithmIdentifier, this.creationDate, this.lastModifiedDate, new ObjectDataSequence(objectDataArray), null);
        try {
            if (this.storeEncryptionAlgorithm.equals(NISTObjectIdentifiers.id_aes256_CCM)) {
                Cipher cipher = this.createCipher("AES/CCM/NoPadding", byArray);
                byte[] byArray2 = cipher.doFinal(objectStoreData.getEncoded());
                AlgorithmParameters algorithmParameters = cipher.getParameters();
                PBES2Parameters pBES2Parameters = new PBES2Parameters(keyDerivationFunc, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_CCM, CCMParameters.getInstance(algorithmParameters.getEncoded())));
                encryptedObjectStoreData = new EncryptedObjectStoreData(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBES2, pBES2Parameters), byArray2);
            } else {
                Cipher cipher = this.createCipher("AESKWP", byArray);
                byte[] byArray3 = cipher.doFinal(objectStoreData.getEncoded());
                PBES2Parameters pBES2Parameters = new PBES2Parameters(keyDerivationFunc, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_wrap_pad));
                encryptedObjectStoreData = new EncryptedObjectStoreData(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBES2, pBES2Parameters), byArray3);
            }
        } catch (NoSuchPaddingException noSuchPaddingException) {
            throw new NoSuchAlgorithmException(noSuchPaddingException.toString());
        } catch (BadPaddingException badPaddingException) {
            throw new IOException(badPaddingException.toString());
        } catch (IllegalBlockSizeException illegalBlockSizeException) {
            throw new IOException(illegalBlockSizeException.toString());
        } catch (InvalidKeyException invalidKeyException) {
            throw new IOException(invalidKeyException.toString());
        } catch (NoSuchProviderException noSuchProviderException) {
            throw new IOException(noSuchProviderException.toString());
        }
        return encryptedObjectStoreData;
    }

    @Override
    public void engineLoad(KeyStore.LoadStoreParameter loadStoreParameter) throws CertificateException, NoSuchAlgorithmException, IOException {
        if (loadStoreParameter == null) {
            this.engineLoad(null, null);
        } else if (loadStoreParameter instanceof BCFKSLoadStoreParameter) {
            BCFKSLoadStoreParameter bCFKSLoadStoreParameter = (BCFKSLoadStoreParameter)loadStoreParameter;
            char[] cArray = ParameterUtil.extractPassword(bCFKSLoadStoreParameter);
            this.hmacPkbdAlgorithm = this.generatePkbdAlgorithmIdentifier(bCFKSLoadStoreParameter.getStorePBKDFConfig(), 64);
            this.storeEncryptionAlgorithm = bCFKSLoadStoreParameter.getStoreEncryptionAlgorithm() == BCFKSLoadStoreParameter.EncryptionAlgorithm.AES256_CCM ? NISTObjectIdentifiers.id_aes256_CCM : NISTObjectIdentifiers.id_aes256_wrap_pad;
            this.hmacAlgorithm = bCFKSLoadStoreParameter.getStoreMacAlgorithm() == BCFKSLoadStoreParameter.MacAlgorithm.HmacSHA512 ? new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA512, DERNull.INSTANCE) : new AlgorithmIdentifier(NISTObjectIdentifiers.id_hmacWithSHA3_512, DERNull.INSTANCE);
            this.verificationKey = (PublicKey)bCFKSLoadStoreParameter.getStoreSignatureKey();
            this.validator = bCFKSLoadStoreParameter.getCertChainValidator();
            this.signatureAlgorithm = this.generateSignatureAlgId(this.verificationKey, bCFKSLoadStoreParameter.getStoreSignatureAlgorithm());
            AlgorithmIdentifier algorithmIdentifier = this.hmacAlgorithm;
            ASN1ObjectIdentifier aSN1ObjectIdentifier = this.storeEncryptionAlgorithm;
            InputStream inputStream = bCFKSLoadStoreParameter.getInputStream();
            this.engineLoad(inputStream, cArray);
            if (!(inputStream == null || this.isSimilarHmacPbkd(bCFKSLoadStoreParameter.getStorePBKDFConfig(), this.hmacPkbdAlgorithm) && aSN1ObjectIdentifier.equals(this.storeEncryptionAlgorithm))) {
                throw new IOException("configuration parameters do not match existing store");
            }
        } else if (loadStoreParameter instanceof BCLoadStoreParameter) {
            BCLoadStoreParameter bCLoadStoreParameter = (BCLoadStoreParameter)loadStoreParameter;
            this.engineLoad(bCLoadStoreParameter.getInputStream(), ParameterUtil.extractPassword(loadStoreParameter));
        } else {
            throw new IllegalArgumentException("no support for 'parameter' of type " + loadStoreParameter.getClass().getName());
        }
    }

    private boolean isSimilarHmacPbkd(PBKDFConfig pBKDFConfig, KeyDerivationFunc keyDerivationFunc) {
        if (!pBKDFConfig.getAlgorithm().equals(keyDerivationFunc.getAlgorithm())) {
            return false;
        }
        if (MiscObjectIdentifiers.id_scrypt.equals(keyDerivationFunc.getAlgorithm())) {
            if (!(pBKDFConfig instanceof ScryptConfig)) {
                return false;
            }
            ScryptConfig scryptConfig = (ScryptConfig)pBKDFConfig;
            ScryptParams scryptParams = ScryptParams.getInstance(keyDerivationFunc.getParameters());
            if (scryptConfig.getSaltLength() != scryptParams.getSalt().length || scryptConfig.getBlockSize() != scryptParams.getBlockSize().intValue() || scryptConfig.getCostParameter() != scryptParams.getCostParameter().intValue() || scryptConfig.getParallelizationParameter() != scryptParams.getParallelizationParameter().intValue()) {
                return false;
            }
        } else {
            if (!(pBKDFConfig instanceof PBKDF2Config)) {
                return false;
            }
            PBKDF2Config pBKDF2Config = (PBKDF2Config)pBKDFConfig;
            PBKDF2Params pBKDF2Params = PBKDF2Params.getInstance(keyDerivationFunc.getParameters());
            if (pBKDF2Config.getSaltLength() != pBKDF2Params.getSalt().length || pBKDF2Config.getIterationCount() != pBKDF2Params.getIterationCount().intValue()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void engineLoad(InputStream inputStream, char[] cArray) throws IOException, NoSuchAlgorithmException, CertificateException {
        Object object;
        Iterator<ASN1Encodable> iterator;
        Object object2;
        AlgorithmIdentifier algorithmIdentifier;
        ASN1Encodable aSN1Encodable;
        ObjectStore objectStore;
        this.entries.clear();
        this.privateKeyCache.clear();
        this.creationDate = null;
        this.lastModifiedDate = null;
        this.hmacAlgorithm = null;
        if (inputStream == null) {
            this.lastModifiedDate = this.creationDate = new Date();
            this.verificationKey = null;
            this.validator = null;
            this.hmacAlgorithm = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA512, DERNull.INSTANCE);
            this.hmacPkbdAlgorithm = this.generatePkbdAlgorithmIdentifier(PKCSObjectIdentifiers.id_PBKDF2, 64);
            return;
        }
        ASN1InputStream aSN1InputStream = new ASN1InputStream(inputStream);
        try {
            objectStore = ObjectStore.getInstance(aSN1InputStream.readObject());
        } catch (Exception exception) {
            throw new IOException(exception.getMessage());
        }
        ObjectStoreIntegrityCheck objectStoreIntegrityCheck = objectStore.getIntegrityCheck();
        if (objectStoreIntegrityCheck.getType() == 0) {
            aSN1Encodable = PbkdMacIntegrityCheck.getInstance(objectStoreIntegrityCheck.getIntegrityCheck());
            this.hmacAlgorithm = aSN1Encodable.getMacAlgorithm();
            this.hmacPkbdAlgorithm = aSN1Encodable.getPbkdAlgorithm();
            algorithmIdentifier = this.hmacAlgorithm;
            try {
                this.verifyMac(objectStore.getStoreData().toASN1Primitive().getEncoded(), (PbkdMacIntegrityCheck)aSN1Encodable, cArray);
            } catch (NoSuchProviderException noSuchProviderException) {
                throw new IOException(noSuchProviderException.getMessage());
            }
        } else if (objectStoreIntegrityCheck.getType() == 1) {
            aSN1Encodable = SignatureCheck.getInstance(objectStoreIntegrityCheck.getIntegrityCheck());
            algorithmIdentifier = ((SignatureCheck)aSN1Encodable).getSignatureAlgorithm();
            try {
                object2 = ((SignatureCheck)aSN1Encodable).getCertificates();
                if (this.validator != null) {
                    if (object2 == null) {
                        throw new IOException("validator specified but no certifcates in store");
                    }
                    iterator = this.helper.createCertificateFactory("X.509");
                    object = new X509Certificate[((Certificate[])object2).length];
                    for (int i = 0; i != ((X509Certificate[])object).length; ++i) {
                        object[i] = (X509Certificate)((CertificateFactory)((Object)iterator)).generateCertificate(new ByteArrayInputStream(((ASN1Object)object2[i]).getEncoded()));
                    }
                    if (this.validator.isValid((X509Certificate[])object)) {
                        this.verifySig(objectStore.getStoreData(), (SignatureCheck)aSN1Encodable, object[0].getPublicKey());
                    }
                    throw new IOException("certificate chain in key store signature not valid");
                }
                this.verifySig(objectStore.getStoreData(), (SignatureCheck)aSN1Encodable, this.verificationKey);
            } catch (GeneralSecurityException generalSecurityException) {
                throw new IOException("error verifying signature: " + generalSecurityException.getMessage(), generalSecurityException);
            }
        } else {
            throw new IOException("BCFKS KeyStore unable to recognize integrity check.");
        }
        aSN1Encodable = objectStore.getStoreData();
        if (aSN1Encodable instanceof EncryptedObjectStoreData) {
            iterator = (EncryptedObjectStoreData)aSN1Encodable;
            object = ((EncryptedObjectStoreData)((Object)iterator)).getEncryptionAlgorithm();
            object2 = ObjectStoreData.getInstance(this.decryptData("STORE_ENCRYPTION", (AlgorithmIdentifier)object, cArray, ((EncryptedObjectStoreData)((Object)iterator)).getEncryptedContent().getOctets()));
        } else {
            object2 = ObjectStoreData.getInstance(aSN1Encodable);
        }
        try {
            this.creationDate = ((ObjectStoreData)object2).getCreationDate().getDate();
            this.lastModifiedDate = ((ObjectStoreData)object2).getLastModifiedDate().getDate();
        } catch (ParseException parseException) {
            throw new IOException("BCFKS KeyStore unable to parse store data information.");
        }
        if (!((ObjectStoreData)object2).getIntegrityAlgorithm().equals(algorithmIdentifier)) {
            throw new IOException("BCFKS KeyStore storeData integrity algorithm does not match store integrity algorithm.");
        }
        iterator = ((ObjectStoreData)object2).getObjectDataSequence().iterator();
        while (iterator.hasNext()) {
            object = ObjectData.getInstance(iterator.next());
            this.entries.put(((ObjectData)object).getIdentifier(), (ObjectData)object);
        }
    }

    private byte[] decryptData(String string, AlgorithmIdentifier algorithmIdentifier, char[] cArray, byte[] byArray) throws IOException {
        if (!algorithmIdentifier.getAlgorithm().equals(PKCSObjectIdentifiers.id_PBES2)) {
            throw new IOException("BCFKS KeyStore cannot recognize protection algorithm.");
        }
        PBES2Parameters pBES2Parameters = PBES2Parameters.getInstance(algorithmIdentifier.getParameters());
        EncryptionScheme encryptionScheme = pBES2Parameters.getEncryptionScheme();
        try {
            Object object;
            AlgorithmParameters algorithmParameters;
            Cipher cipher;
            if (encryptionScheme.getAlgorithm().equals(NISTObjectIdentifiers.id_aes256_CCM)) {
                cipher = this.helper.createCipher("AES/CCM/NoPadding");
                algorithmParameters = this.helper.createAlgorithmParameters("CCM");
                object = CCMParameters.getInstance(encryptionScheme.getParameters());
                algorithmParameters.init(((ASN1Object)object).getEncoded());
            } else if (encryptionScheme.getAlgorithm().equals(NISTObjectIdentifiers.id_aes256_wrap_pad)) {
                cipher = this.helper.createCipher("AESKWP");
                algorithmParameters = null;
            } else {
                throw new IOException("BCFKS KeyStore cannot recognize protection encryption algorithm.");
            }
            object = this.generateKey(pBES2Parameters.getKeyDerivationFunc(), string, cArray != null ? cArray : new char[]{}, 32);
            cipher.init(2, (Key)new SecretKeySpec((byte[])object, "AES"), algorithmParameters);
            byte[] byArray2 = cipher.doFinal(byArray);
            return byArray2;
        } catch (IOException iOException) {
            throw iOException;
        } catch (Exception exception) {
            throw new IOException(exception.toString());
        }
    }

    private AlgorithmIdentifier generateSignatureAlgId(Key key, BCFKSLoadStoreParameter.SignatureAlgorithm signatureAlgorithm) throws IOException {
        if (key == null) {
            return null;
        }
        if (key instanceof ECKey) {
            if (signatureAlgorithm == BCFKSLoadStoreParameter.SignatureAlgorithm.SHA512withECDSA) {
                return new AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA512);
            }
            if (signatureAlgorithm == BCFKSLoadStoreParameter.SignatureAlgorithm.SHA3_512withECDSA) {
                return new AlgorithmIdentifier(NISTObjectIdentifiers.id_ecdsa_with_sha3_512);
            }
        }
        if (key instanceof DSAKey) {
            if (signatureAlgorithm == BCFKSLoadStoreParameter.SignatureAlgorithm.SHA512withDSA) {
                return new AlgorithmIdentifier(NISTObjectIdentifiers.dsa_with_sha512);
            }
            if (signatureAlgorithm == BCFKSLoadStoreParameter.SignatureAlgorithm.SHA3_512withDSA) {
                return new AlgorithmIdentifier(NISTObjectIdentifiers.id_dsa_with_sha3_512);
            }
        }
        if (key instanceof RSAKey) {
            if (signatureAlgorithm == BCFKSLoadStoreParameter.SignatureAlgorithm.SHA512withRSA) {
                return new AlgorithmIdentifier(PKCSObjectIdentifiers.sha512WithRSAEncryption, DERNull.INSTANCE);
            }
            if (signatureAlgorithm == BCFKSLoadStoreParameter.SignatureAlgorithm.SHA3_512withRSA) {
                return new AlgorithmIdentifier(NISTObjectIdentifiers.id_rsassa_pkcs1_v1_5_with_sha3_512, DERNull.INSTANCE);
            }
        }
        throw new IOException("unknown signature algorithm");
    }

    private KeyDerivationFunc generatePkbdAlgorithmIdentifier(PBKDFConfig pBKDFConfig, int n) {
        if (MiscObjectIdentifiers.id_scrypt.equals(pBKDFConfig.getAlgorithm())) {
            ScryptConfig scryptConfig = (ScryptConfig)pBKDFConfig;
            byte[] byArray = new byte[scryptConfig.getSaltLength()];
            this.getDefaultSecureRandom().nextBytes(byArray);
            ScryptParams scryptParams = new ScryptParams(byArray, scryptConfig.getCostParameter(), scryptConfig.getBlockSize(), scryptConfig.getParallelizationParameter(), n);
            return new KeyDerivationFunc(MiscObjectIdentifiers.id_scrypt, scryptParams);
        }
        PBKDF2Config pBKDF2Config = (PBKDF2Config)pBKDFConfig;
        byte[] byArray = new byte[pBKDF2Config.getSaltLength()];
        this.getDefaultSecureRandom().nextBytes(byArray);
        return new KeyDerivationFunc(PKCSObjectIdentifiers.id_PBKDF2, new PBKDF2Params(byArray, pBKDF2Config.getIterationCount(), n, pBKDF2Config.getPRF()));
    }

    private KeyDerivationFunc generatePkbdAlgorithmIdentifier(KeyDerivationFunc keyDerivationFunc, int n) {
        if (MiscObjectIdentifiers.id_scrypt.equals(keyDerivationFunc.getAlgorithm())) {
            ScryptParams scryptParams = ScryptParams.getInstance(keyDerivationFunc.getParameters());
            byte[] byArray = new byte[scryptParams.getSalt().length];
            this.getDefaultSecureRandom().nextBytes(byArray);
            ScryptParams scryptParams2 = new ScryptParams(byArray, scryptParams.getCostParameter(), scryptParams.getBlockSize(), scryptParams.getParallelizationParameter(), BigInteger.valueOf(n));
            return new KeyDerivationFunc(MiscObjectIdentifiers.id_scrypt, scryptParams2);
        }
        PBKDF2Params pBKDF2Params = PBKDF2Params.getInstance(keyDerivationFunc.getParameters());
        byte[] byArray = new byte[pBKDF2Params.getSalt().length];
        this.getDefaultSecureRandom().nextBytes(byArray);
        PBKDF2Params pBKDF2Params2 = new PBKDF2Params(byArray, pBKDF2Params.getIterationCount().intValue(), n, pBKDF2Params.getPrf());
        return new KeyDerivationFunc(PKCSObjectIdentifiers.id_PBKDF2, pBKDF2Params2);
    }

    private KeyDerivationFunc generatePkbdAlgorithmIdentifier(ASN1ObjectIdentifier aSN1ObjectIdentifier, int n) {
        byte[] byArray = new byte[64];
        this.getDefaultSecureRandom().nextBytes(byArray);
        if (PKCSObjectIdentifiers.id_PBKDF2.equals(aSN1ObjectIdentifier)) {
            return new KeyDerivationFunc(PKCSObjectIdentifiers.id_PBKDF2, new PBKDF2Params(byArray, 51200, n, new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA512, DERNull.INSTANCE)));
        }
        throw new IllegalStateException("unknown derivation algorithm: " + aSN1ObjectIdentifier);
    }

    static {
        oidMap.put("DESEDE", OIWObjectIdentifiers.desEDE);
        oidMap.put("TRIPLEDES", OIWObjectIdentifiers.desEDE);
        oidMap.put("TDEA", OIWObjectIdentifiers.desEDE);
        oidMap.put("HMACSHA1", PKCSObjectIdentifiers.id_hmacWithSHA1);
        oidMap.put("HMACSHA224", PKCSObjectIdentifiers.id_hmacWithSHA224);
        oidMap.put("HMACSHA256", PKCSObjectIdentifiers.id_hmacWithSHA256);
        oidMap.put("HMACSHA384", PKCSObjectIdentifiers.id_hmacWithSHA384);
        oidMap.put("HMACSHA512", PKCSObjectIdentifiers.id_hmacWithSHA512);
        oidMap.put("SEED", KISAObjectIdentifiers.id_seedCBC);
        oidMap.put("CAMELLIA.128", NTTObjectIdentifiers.id_camellia128_cbc);
        oidMap.put("CAMELLIA.192", NTTObjectIdentifiers.id_camellia192_cbc);
        oidMap.put("CAMELLIA.256", NTTObjectIdentifiers.id_camellia256_cbc);
        oidMap.put("ARIA.128", NSRIObjectIdentifiers.id_aria128_cbc);
        oidMap.put("ARIA.192", NSRIObjectIdentifiers.id_aria192_cbc);
        oidMap.put("ARIA.256", NSRIObjectIdentifiers.id_aria256_cbc);
        publicAlgMap.put(PKCSObjectIdentifiers.rsaEncryption, "RSA");
        publicAlgMap.put(X9ObjectIdentifiers.id_ecPublicKey, "EC");
        publicAlgMap.put(OIWObjectIdentifiers.elGamalAlgorithm, "DH");
        publicAlgMap.put(PKCSObjectIdentifiers.dhKeyAgreement, "DH");
        publicAlgMap.put(X9ObjectIdentifiers.id_dsa, "DSA");
        CERTIFICATE = BigInteger.valueOf(0L);
        PRIVATE_KEY = BigInteger.valueOf(1L);
        SECRET_KEY = BigInteger.valueOf(2L);
        PROTECTED_PRIVATE_KEY = BigInteger.valueOf(3L);
        PROTECTED_SECRET_KEY = BigInteger.valueOf(4L);
    }

    public static class Def
    extends BcFKSKeyStoreSpi {
        public Def() {
            super(new DefaultJcaJceHelper());
        }
    }

    public static class DefCompat
    extends AdaptingKeyStoreSpi {
        public DefCompat() {
            super(new DefaultJcaJceHelper(), new BcFKSKeyStoreSpi(new DefaultJcaJceHelper()));
        }
    }

    public static class DefShared
    extends SharedKeyStoreSpi {
        public DefShared() {
            super(new DefaultJcaJceHelper());
        }
    }

    public static class DefSharedCompat
    extends AdaptingKeyStoreSpi {
        public DefSharedCompat() {
            super(new DefaultJcaJceHelper(), new BcFKSKeyStoreSpi(new DefaultJcaJceHelper()));
        }
    }

    private static class ExtKeyStoreException
    extends KeyStoreException {
        private final Throwable cause;

        ExtKeyStoreException(String string, Throwable throwable) {
            super(string);
            this.cause = throwable;
        }

        public Throwable getCause() {
            return this.cause;
        }
    }

    private static class SharedKeyStoreSpi
    extends BcFKSKeyStoreSpi
    implements PKCSObjectIdentifiers,
    X509ObjectIdentifiers {
        private final Map<String, byte[]> cache;
        private final byte[] seedKey;

        public SharedKeyStoreSpi(JcaJceHelper jcaJceHelper) {
            super(jcaJceHelper);
            try {
                this.seedKey = new byte[32];
                jcaJceHelper.createSecureRandom("DEFAULT").nextBytes(this.seedKey);
            } catch (GeneralSecurityException generalSecurityException) {
                throw new IllegalArgumentException("can't create random - " + generalSecurityException.toString());
            }
            this.cache = new HashMap<String, byte[]>();
        }

        public void engineDeleteEntry(String string) throws KeyStoreException {
            throw new KeyStoreException("delete operation not supported in shared mode");
        }

        public void engineSetKeyEntry(String string, Key key, char[] cArray, java.security.cert.Certificate[] certificateArray) throws KeyStoreException {
            throw new KeyStoreException("set operation not supported in shared mode");
        }

        public void engineSetKeyEntry(String string, byte[] byArray, java.security.cert.Certificate[] certificateArray) throws KeyStoreException {
            throw new KeyStoreException("set operation not supported in shared mode");
        }

        public void engineSetCertificateEntry(String string, java.security.cert.Certificate certificate) throws KeyStoreException {
            throw new KeyStoreException("set operation not supported in shared mode");
        }

        public Key engineGetKey(String string, char[] cArray) throws NoSuchAlgorithmException, UnrecoverableKeyException {
            Object object;
            byte[] byArray;
            try {
                byArray = this.calculateMac(string, cArray);
            } catch (InvalidKeyException invalidKeyException) {
                throw new UnrecoverableKeyException("unable to recover key (" + string + "): " + invalidKeyException.getMessage());
            }
            if (this.cache.containsKey(string) && !Arrays.constantTimeAreEqual((byte[])(object = (Object)this.cache.get(string)), byArray)) {
                throw new UnrecoverableKeyException("unable to recover key (" + string + ")");
            }
            object = super.engineGetKey(string, cArray);
            if (object != null && !this.cache.containsKey(string)) {
                this.cache.put(string, byArray);
            }
            return object;
        }

        private byte[] calculateMac(String string, char[] cArray) throws NoSuchAlgorithmException, InvalidKeyException {
            byte[] byArray = cArray != null ? Arrays.concatenate(Strings.toUTF8ByteArray(cArray), Strings.toUTF8ByteArray(string)) : Arrays.concatenate(this.seedKey, Strings.toUTF8ByteArray(string));
            return SCrypt.generate(byArray, this.seedKey, 16384, 8, 1, 32);
        }
    }

    public static class Std
    extends BcFKSKeyStoreSpi {
        public Std() {
            super(new BCJcaJceHelper());
        }
    }

    public static class StdCompat
    extends AdaptingKeyStoreSpi {
        public StdCompat() {
            super(new DefaultJcaJceHelper(), new BcFKSKeyStoreSpi(new BCJcaJceHelper()));
        }
    }

    public static class StdShared
    extends SharedKeyStoreSpi {
        public StdShared() {
            super(new BCJcaJceHelper());
        }
    }

    public static class StdSharedCompat
    extends AdaptingKeyStoreSpi {
        public StdSharedCompat() {
            super(new BCJcaJceHelper(), new BcFKSKeyStoreSpi(new BCJcaJceHelper()));
        }
    }
}

