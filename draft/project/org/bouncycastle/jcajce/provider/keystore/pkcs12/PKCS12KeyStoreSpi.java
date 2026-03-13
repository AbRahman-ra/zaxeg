/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.keystore.pkcs12;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.BEROctetString;
import org.bouncycastle.asn1.BERSequence;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.GOST28147Parameters;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.AuthenticatedSafe;
import org.bouncycastle.asn1.pkcs.CertBag;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.EncryptedData;
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.MacData;
import org.bouncycastle.asn1.pkcs.PBES2Parameters;
import org.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.Pfx;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.SafeBag;
import org.bouncycastle.asn1.util.ASN1Dump;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.util.DigestFactory;
import org.bouncycastle.jcajce.BCLoadStoreParameter;
import org.bouncycastle.jcajce.PKCS12Key;
import org.bouncycastle.jcajce.PKCS12StoreParameter;
import org.bouncycastle.jcajce.provider.keystore.util.AdaptingKeyStoreSpi;
import org.bouncycastle.jcajce.provider.keystore.util.ParameterUtil;
import org.bouncycastle.jcajce.spec.GOST28147ParameterSpec;
import org.bouncycastle.jcajce.spec.PBKDF2KeySpec;
import org.bouncycastle.jcajce.util.BCJcaJceHelper;
import org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jce.interfaces.BCKeyStore;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.JDKPKCS12StoreParameter;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Integers;
import org.bouncycastle.util.Properties;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

public class PKCS12KeyStoreSpi
extends KeyStoreSpi
implements PKCSObjectIdentifiers,
X509ObjectIdentifiers,
BCKeyStore {
    static final String PKCS12_MAX_IT_COUNT_PROPERTY = "org.bouncycastle.pkcs12.max_it_count";
    private final JcaJceHelper helper = new BCJcaJceHelper();
    private static final int SALT_SIZE = 20;
    private static final int MIN_ITERATIONS = 51200;
    private static final DefaultSecretKeyProvider keySizeProvider = new DefaultSecretKeyProvider();
    private IgnoresCaseHashtable keys = new IgnoresCaseHashtable();
    private IgnoresCaseHashtable localIds = new IgnoresCaseHashtable();
    private IgnoresCaseHashtable certs = new IgnoresCaseHashtable();
    private Hashtable chainCerts = new Hashtable();
    private Hashtable keyCerts = new Hashtable();
    static final int NULL = 0;
    static final int CERTIFICATE = 1;
    static final int KEY = 2;
    static final int SECRET = 3;
    static final int SEALED = 4;
    static final int KEY_PRIVATE = 0;
    static final int KEY_PUBLIC = 1;
    static final int KEY_SECRET = 2;
    protected SecureRandom random = CryptoServicesRegistrar.getSecureRandom();
    private CertificateFactory certFact;
    private ASN1ObjectIdentifier keyAlgorithm;
    private ASN1ObjectIdentifier certAlgorithm;
    private AlgorithmIdentifier macAlgorithm = new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE);
    private int itCount = 102400;
    private int saltLength = 20;

    public PKCS12KeyStoreSpi(JcaJceHelper jcaJceHelper, ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1ObjectIdentifier aSN1ObjectIdentifier2) {
        this.keyAlgorithm = aSN1ObjectIdentifier;
        this.certAlgorithm = aSN1ObjectIdentifier2;
        try {
            this.certFact = jcaJceHelper.createCertificateFactory("X.509");
        } catch (Exception exception) {
            throw new IllegalArgumentException("can't create cert factory - " + exception.toString());
        }
    }

    private SubjectKeyIdentifier createSubjectKeyId(PublicKey publicKey) {
        try {
            SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
            return new SubjectKeyIdentifier(PKCS12KeyStoreSpi.getDigest(subjectPublicKeyInfo));
        } catch (Exception exception) {
            throw new RuntimeException("error creating key");
        }
    }

    private static byte[] getDigest(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        Digest digest = DigestFactory.createSHA1();
        byte[] byArray = new byte[digest.getDigestSize()];
        byte[] byArray2 = subjectPublicKeyInfo.getPublicKeyData().getBytes();
        digest.update(byArray2, 0, byArray2.length);
        digest.doFinal(byArray, 0);
        return byArray;
    }

    public void setRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
    }

    public boolean engineProbe(InputStream inputStream) throws IOException {
        return false;
    }

    public Enumeration engineAliases() {
        Hashtable<Object, String> hashtable = new Hashtable<Object, String>();
        Enumeration enumeration = this.certs.keys();
        while (enumeration.hasMoreElements()) {
            hashtable.put(enumeration.nextElement(), "cert");
        }
        enumeration = this.keys.keys();
        while (enumeration.hasMoreElements()) {
            String string = (String)enumeration.nextElement();
            if (hashtable.get(string) != null) continue;
            hashtable.put(string, "key");
        }
        return hashtable.keys();
    }

    public boolean engineContainsAlias(String string) {
        return this.certs.get(string) != null || this.keys.get(string) != null;
    }

    public void engineDeleteEntry(String string) throws KeyStoreException {
        Key key = (Key)this.keys.remove(string);
        Certificate certificate = (Certificate)this.certs.remove(string);
        if (certificate != null) {
            this.chainCerts.remove(new CertId(certificate.getPublicKey()));
        }
        if (key != null) {
            String string2 = (String)this.localIds.remove(string);
            if (string2 != null) {
                certificate = (Certificate)this.keyCerts.remove(string2);
            }
            if (certificate != null) {
                this.chainCerts.remove(new CertId(certificate.getPublicKey()));
            }
        }
    }

    public Certificate engineGetCertificate(String string) {
        if (string == null) {
            throw new IllegalArgumentException("null alias passed to getCertificate.");
        }
        Certificate certificate = (Certificate)this.certs.get(string);
        if (certificate == null) {
            String string2 = (String)this.localIds.get(string);
            certificate = string2 != null ? (Certificate)this.keyCerts.get(string2) : (Certificate)this.keyCerts.get(string);
        }
        return certificate;
    }

    public String engineGetCertificateAlias(Certificate certificate) {
        String string;
        Certificate certificate2;
        Enumeration enumeration = this.certs.elements();
        Enumeration enumeration2 = this.certs.keys();
        while (enumeration.hasMoreElements()) {
            certificate2 = (Certificate)enumeration.nextElement();
            string = (String)enumeration2.nextElement();
            if (!certificate2.equals(certificate)) continue;
            return string;
        }
        enumeration = this.keyCerts.elements();
        enumeration2 = this.keyCerts.keys();
        while (enumeration.hasMoreElements()) {
            certificate2 = (Certificate)enumeration.nextElement();
            string = (String)enumeration2.nextElement();
            if (!certificate2.equals(certificate)) continue;
            return string;
        }
        return null;
    }

    public Certificate[] engineGetCertificateChain(String string) {
        if (string == null) {
            throw new IllegalArgumentException("null alias passed to getCertificateChain.");
        }
        if (!this.engineIsKeyEntry(string)) {
            return null;
        }
        Certificate certificate = this.engineGetCertificate(string);
        if (certificate != null) {
            Certificate[] certificateArray;
            Vector<Certificate> vector = new Vector<Certificate>();
            while (certificate != null) {
                Object object;
                Object object2;
                Object object3;
                certificateArray = (Certificate[])certificate;
                Certificate certificate2 = null;
                byte[] byArray = certificateArray.getExtensionValue(Extension.authorityKeyIdentifier.getId());
                if (byArray != null && null != (object3 = ((AuthorityKeyIdentifier)(object2 = AuthorityKeyIdentifier.getInstance(((ASN1OctetString)(object = ASN1OctetString.getInstance(byArray))).getOctets()))).getKeyIdentifier())) {
                    certificate2 = (Certificate)this.chainCerts.get(new CertId((byte[])object3));
                }
                if (certificate2 == null && !(object = certificateArray.getIssuerDN()).equals(object2 = certificateArray.getSubjectDN())) {
                    object3 = this.chainCerts.keys();
                    while (object3.hasMoreElements()) {
                        X509Certificate x509Certificate = (X509Certificate)this.chainCerts.get(object3.nextElement());
                        Principal principal = x509Certificate.getSubjectDN();
                        if (!principal.equals(object)) continue;
                        try {
                            certificateArray.verify(x509Certificate.getPublicKey());
                            certificate2 = x509Certificate;
                            break;
                        } catch (Exception exception) {
                        }
                    }
                }
                if (vector.contains(certificate)) {
                    certificate = null;
                    continue;
                }
                vector.addElement(certificate);
                if (certificate2 != certificate) {
                    certificate = certificate2;
                    continue;
                }
                certificate = null;
            }
            certificateArray = new Certificate[vector.size()];
            for (int i = 0; i != certificateArray.length; ++i) {
                certificateArray[i] = (Certificate)vector.elementAt(i);
            }
            return certificateArray;
        }
        return null;
    }

    public Date engineGetCreationDate(String string) {
        if (string == null) {
            throw new NullPointerException("alias == null");
        }
        if (this.keys.get(string) == null && this.certs.get(string) == null) {
            return null;
        }
        return new Date();
    }

    public Key engineGetKey(String string, char[] cArray) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        if (string == null) {
            throw new IllegalArgumentException("null alias passed to getKey.");
        }
        return (Key)this.keys.get(string);
    }

    public boolean engineIsCertificateEntry(String string) {
        return this.certs.get(string) != null && this.keys.get(string) == null;
    }

    public boolean engineIsKeyEntry(String string) {
        return this.keys.get(string) != null;
    }

    public void engineSetCertificateEntry(String string, Certificate certificate) throws KeyStoreException {
        if (this.keys.get(string) != null) {
            throw new KeyStoreException("There is a key entry with the name " + string + ".");
        }
        this.certs.put(string, certificate);
        this.chainCerts.put(new CertId(certificate.getPublicKey()), certificate);
    }

    public void engineSetKeyEntry(String string, byte[] byArray, Certificate[] certificateArray) throws KeyStoreException {
        throw new RuntimeException("operation not supported");
    }

    public void engineSetKeyEntry(String string, Key key, char[] cArray, Certificate[] certificateArray) throws KeyStoreException {
        if (!(key instanceof PrivateKey)) {
            throw new KeyStoreException("PKCS12 does not support non-PrivateKeys");
        }
        if (key instanceof PrivateKey && certificateArray == null) {
            throw new KeyStoreException("no certificate chain for private key");
        }
        if (this.keys.get(string) != null) {
            this.engineDeleteEntry(string);
        }
        this.keys.put(string, key);
        if (certificateArray != null) {
            this.certs.put(string, certificateArray[0]);
            for (int i = 0; i != certificateArray.length; ++i) {
                this.chainCerts.put(new CertId(certificateArray[i].getPublicKey()), certificateArray[i]);
            }
        }
    }

    public int engineSize() {
        Hashtable<Object, String> hashtable = new Hashtable<Object, String>();
        Enumeration enumeration = this.certs.keys();
        while (enumeration.hasMoreElements()) {
            hashtable.put(enumeration.nextElement(), "cert");
        }
        enumeration = this.keys.keys();
        while (enumeration.hasMoreElements()) {
            String string = (String)enumeration.nextElement();
            if (hashtable.get(string) != null) continue;
            hashtable.put(string, "key");
        }
        return hashtable.size();
    }

    protected PrivateKey unwrapKey(AlgorithmIdentifier algorithmIdentifier, byte[] byArray, char[] cArray, boolean bl) throws IOException {
        ASN1ObjectIdentifier aSN1ObjectIdentifier = algorithmIdentifier.getAlgorithm();
        try {
            if (aSN1ObjectIdentifier.on(PKCSObjectIdentifiers.pkcs_12PbeIds)) {
                PKCS12PBEParams pKCS12PBEParams = PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters());
                PBEParameterSpec pBEParameterSpec = new PBEParameterSpec(pKCS12PBEParams.getIV(), this.validateIterationCount(pKCS12PBEParams.getIterations()));
                Cipher cipher = this.helper.createCipher(aSN1ObjectIdentifier.getId());
                PKCS12Key pKCS12Key = new PKCS12Key(cArray, bl);
                cipher.init(4, (Key)pKCS12Key, pBEParameterSpec);
                return (PrivateKey)cipher.unwrap(byArray, "", 2);
            }
            if (aSN1ObjectIdentifier.equals(PKCSObjectIdentifiers.id_PBES2)) {
                Cipher cipher = this.createCipher(4, cArray, algorithmIdentifier);
                return (PrivateKey)cipher.unwrap(byArray, "", 2);
            }
        } catch (Exception exception) {
            throw new IOException("exception unwrapping private key - " + exception.toString());
        }
        throw new IOException("exception unwrapping private key - cannot recognise: " + aSN1ObjectIdentifier);
    }

    protected byte[] wrapKey(String string, Key key, PKCS12PBEParams pKCS12PBEParams, char[] cArray) throws IOException {
        byte[] byArray;
        PBEKeySpec pBEKeySpec = new PBEKeySpec(cArray);
        try {
            SecretKeyFactory secretKeyFactory = this.helper.createSecretKeyFactory(string);
            PBEParameterSpec pBEParameterSpec = new PBEParameterSpec(pKCS12PBEParams.getIV(), pKCS12PBEParams.getIterations().intValue());
            Cipher cipher = this.helper.createCipher(string);
            cipher.init(3, (Key)secretKeyFactory.generateSecret(pBEKeySpec), pBEParameterSpec);
            byArray = cipher.wrap(key);
        } catch (Exception exception) {
            throw new IOException("exception encrypting data - " + exception.toString());
        }
        return byArray;
    }

    protected byte[] cryptData(boolean bl, AlgorithmIdentifier algorithmIdentifier, char[] cArray, boolean bl2, byte[] byArray) throws IOException {
        int n;
        ASN1ObjectIdentifier aSN1ObjectIdentifier = algorithmIdentifier.getAlgorithm();
        int n2 = n = bl ? 1 : 2;
        if (aSN1ObjectIdentifier.on(PKCSObjectIdentifiers.pkcs_12PbeIds)) {
            PKCS12PBEParams pKCS12PBEParams = PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters());
            try {
                PBEParameterSpec pBEParameterSpec = new PBEParameterSpec(pKCS12PBEParams.getIV(), pKCS12PBEParams.getIterations().intValue());
                PKCS12Key pKCS12Key = new PKCS12Key(cArray, bl2);
                Cipher cipher = this.helper.createCipher(aSN1ObjectIdentifier.getId());
                cipher.init(n, (Key)pKCS12Key, pBEParameterSpec);
                return cipher.doFinal(byArray);
            } catch (Exception exception) {
                throw new IOException("exception decrypting data - " + exception.toString());
            }
        }
        if (aSN1ObjectIdentifier.equals(PKCSObjectIdentifiers.id_PBES2)) {
            try {
                Cipher cipher = this.createCipher(n, cArray, algorithmIdentifier);
                return cipher.doFinal(byArray);
            } catch (Exception exception) {
                throw new IOException("exception decrypting data - " + exception.toString());
            }
        }
        throw new IOException("unknown PBE algorithm: " + aSN1ObjectIdentifier);
    }

    private Cipher createCipher(int n, char[] cArray, AlgorithmIdentifier algorithmIdentifier) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchProviderException {
        PBES2Parameters pBES2Parameters = PBES2Parameters.getInstance(algorithmIdentifier.getParameters());
        PBKDF2Params pBKDF2Params = PBKDF2Params.getInstance(pBES2Parameters.getKeyDerivationFunc().getParameters());
        AlgorithmIdentifier algorithmIdentifier2 = AlgorithmIdentifier.getInstance(pBES2Parameters.getEncryptionScheme());
        SecretKeyFactory secretKeyFactory = this.helper.createSecretKeyFactory(pBES2Parameters.getKeyDerivationFunc().getAlgorithm().getId());
        SecretKey secretKey = pBKDF2Params.isDefaultPrf() ? secretKeyFactory.generateSecret(new PBEKeySpec(cArray, pBKDF2Params.getSalt(), this.validateIterationCount(pBKDF2Params.getIterationCount()), keySizeProvider.getKeySize(algorithmIdentifier2))) : secretKeyFactory.generateSecret(new PBKDF2KeySpec(cArray, pBKDF2Params.getSalt(), this.validateIterationCount(pBKDF2Params.getIterationCount()), keySizeProvider.getKeySize(algorithmIdentifier2), pBKDF2Params.getPrf()));
        Cipher cipher = Cipher.getInstance(pBES2Parameters.getEncryptionScheme().getAlgorithm().getId());
        ASN1Encodable aSN1Encodable = pBES2Parameters.getEncryptionScheme().getParameters();
        if (aSN1Encodable instanceof ASN1OctetString) {
            cipher.init(n, (Key)secretKey, new IvParameterSpec(ASN1OctetString.getInstance(aSN1Encodable).getOctets()));
        } else {
            GOST28147Parameters gOST28147Parameters = GOST28147Parameters.getInstance(aSN1Encodable);
            cipher.init(n, (Key)secretKey, new GOST28147ParameterSpec(gOST28147Parameters.getEncryptionParamSet(), gOST28147Parameters.getIV()));
        }
        return cipher;
    }

    public void engineLoad(KeyStore.LoadStoreParameter loadStoreParameter) throws IOException, NoSuchAlgorithmException, CertificateException {
        if (loadStoreParameter == null) {
            this.engineLoad(null, null);
        } else if (loadStoreParameter instanceof BCLoadStoreParameter) {
            BCLoadStoreParameter bCLoadStoreParameter = (BCLoadStoreParameter)loadStoreParameter;
            this.engineLoad(bCLoadStoreParameter.getInputStream(), ParameterUtil.extractPassword(loadStoreParameter));
        } else {
            throw new IllegalArgumentException("no support for 'param' of type " + loadStoreParameter.getClass().getName());
        }
    }

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public void engineLoad(InputStream var1_1, char[] var2_2) throws IOException {
        if (var1_1 == null) {
            return;
        }
        var3_3 = new BufferedInputStream(var1_1);
        var3_3.mark(10);
        var4_4 = var3_3.read();
        if (var4_4 < 0) {
            throw new EOFException("no data in keystore stream");
        }
        if (var4_4 != 48) {
            throw new IOException("stream does not represent a PKCS12 key store");
        }
        var3_3.reset();
        var5_5 = new ASN1InputStream(var3_3);
        try {
            var6_6 = Pfx.getInstance(var5_5.readObject());
        } catch (Exception var7_7) {
            throw new IOException(var7_7.getMessage());
        }
        var7_8 = var6_6.getAuthSafe();
        var8_9 = new Vector<ASN1Object>();
        var9_10 = false;
        var10_11 = false;
        if (var6_6.getMacData() != null) {
            if (var2_2 == null) {
                throw new NullPointerException("no password supplied when one expected");
            }
            var11_12 = var6_6.getMacData();
            var12_14 = var11_12.getMac();
            this.macAlgorithm = var12_14.getAlgorithmId();
            var13_15 = var11_12.getSalt();
            this.itCount = this.validateIterationCount(var11_12.getIterationCount());
            this.saltLength = ((byte[])var13_15).length;
            var14_16 = ((ASN1OctetString)var7_8.getContent()).getOctets();
            try {
                var15_19 = this.calculatePbeMac(this.macAlgorithm.getAlgorithm(), (byte[])var13_15, this.itCount, var2_2, false, var14_16);
                var16_23 /* !! */  = var12_14.getDigest();
                if (Arrays.constantTimeAreEqual((byte[])var15_19, var16_23 /* !! */ )) ** GOTO lbl48
                if (var2_2.length > 0) {
                    throw new IOException("PKCS12 key store mac invalid - wrong password or corrupted file.");
                }
                var15_19 = this.calculatePbeMac(this.macAlgorithm.getAlgorithm(), (byte[])var13_15, this.itCount, var2_2, true, var14_16);
                if (!Arrays.constantTimeAreEqual((byte[])var15_19, var16_23 /* !! */ )) {
                    throw new IOException("PKCS12 key store mac invalid - wrong password or corrupted file.");
                }
                var10_11 = true;
            } catch (IOException var15_20) {
                throw var15_20;
            } catch (Exception var15_21) {
                throw new IOException("error constructing MAC: " + var15_21.toString());
            }
        } else if (var2_2 != null && var2_2.length != 0 && !Properties.isOverrideSet("org.bouncycastle.pkcs12.ignore_useless_passwd")) {
            throw new IOException("password supplied for keystore that does not require one");
        }
lbl48:
        // 4 sources

        this.keys = new IgnoresCaseHashtable();
        this.localIds = new IgnoresCaseHashtable();
        if (var7_8.getContentType().equals(PKCS12KeyStoreSpi.data)) {
            var11_12 = ASN1OctetString.getInstance(var7_8.getContent());
            var12_14 = AuthenticatedSafe.getInstance(var11_12.getOctets());
            var13_15 = var12_14.getContentInfo();
            for (var14_17 = 0; var14_17 != ((byte[])var13_15).length; ++var14_17) {
                if (var13_15[var14_17].getContentType().equals(PKCS12KeyStoreSpi.data)) {
                    var15_19 = ASN1OctetString.getInstance(var13_15[var14_17].getContent());
                    var16_23 /* !! */  = (byte[])ASN1Sequence.getInstance(var15_19.getOctets());
                    for (var17_24 = 0; var17_24 != var16_23 /* !! */ .size(); ++var17_24) {
                        var18_27 = SafeBag.getInstance(var16_23 /* !! */ .getObjectAt(var17_24));
                        if (var18_27.getBagId().equals(PKCS12KeyStoreSpi.pkcs8ShroudedKeyBag)) {
                            var19_30 = EncryptedPrivateKeyInfo.getInstance(var18_27.getBagValue());
                            var20_31 = this.unwrapKey(var19_30.getEncryptionAlgorithm(), var19_30.getEncryptedData(), var2_2, var10_11);
                            var21_32 = null;
                            var22_33 = null;
                            if (var18_27.getBagAttributes() != null) {
                                var23_34 = var18_27.getBagAttributes().getObjects();
                                while (var23_34.hasMoreElements()) {
                                    var24_35 /* !! */  = (ASN1Sequence)var23_34.nextElement();
                                    var25_36 = (ASN1ObjectIdentifier)var24_35 /* !! */ .getObjectAt(0);
                                    var26_37 = (ASN1Set)var24_35 /* !! */ .getObjectAt(1);
                                    var27_38 = null;
                                    if (var26_37.size() > 0) {
                                        var27_38 = (ASN1Primitive)var26_37.getObjectAt(0);
                                        if (var20_31 instanceof PKCS12BagAttributeCarrier) {
                                            var28_39 = (PKCS12BagAttributeCarrier)var20_31;
                                            var29_40 = var28_39.getBagAttribute((ASN1ObjectIdentifier)var25_36);
                                            if (var29_40 != null) {
                                                if (!var29_40.toASN1Primitive().equals(var27_38)) {
                                                    throw new IOException("attempt to add existing attribute with different value");
                                                }
                                            } else {
                                                var28_39.setBagAttribute((ASN1ObjectIdentifier)var25_36, var27_38);
                                            }
                                        }
                                    }
                                    if (var25_36.equals(PKCS12KeyStoreSpi.pkcs_9_at_friendlyName)) {
                                        var21_32 = ((DERBMPString)var27_38).getString();
                                        this.keys.put((String)var21_32, var20_31);
                                        continue;
                                    }
                                    if (!var25_36.equals(PKCS12KeyStoreSpi.pkcs_9_at_localKeyId)) continue;
                                    var22_33 = (ASN1OctetString)var27_38;
                                }
                            }
                            if (var22_33 != null) {
                                var23_34 = new String(Hex.encode(var22_33.getOctets()));
                                if (var21_32 == null) {
                                    this.keys.put((String)var23_34, var20_31);
                                    continue;
                                }
                                this.localIds.put((String)var21_32, var23_34);
                                continue;
                            }
                            var9_10 = true;
                            this.keys.put("unmarked", var20_31);
                            continue;
                        }
                        if (var18_27.getBagId().equals(PKCS12KeyStoreSpi.certBag)) {
                            var8_9.addElement(var18_27);
                            continue;
                        }
                        System.out.println("extra in data " + var18_27.getBagId());
                        System.out.println(ASN1Dump.dumpAsString(var18_27));
                    }
                    continue;
                }
                if (var13_15[var14_17].getContentType().equals(PKCS12KeyStoreSpi.encryptedData)) {
                    var15_19 = EncryptedData.getInstance(var13_15[var14_17].getContent());
                    var16_23 /* !! */  = this.cryptData(false, var15_19.getEncryptionAlgorithm(), var2_2, var10_11, var15_19.getContent().getOctets());
                    var17_25 = ASN1Sequence.getInstance(var16_23 /* !! */ );
                    for (var18_28 = 0; var18_28 != var17_25.size(); ++var18_28) {
                        var19_30 = SafeBag.getInstance(var17_25.getObjectAt(var18_28));
                        if (var19_30.getBagId().equals(PKCS12KeyStoreSpi.certBag)) {
                            var8_9.addElement(var19_30);
                            continue;
                        }
                        if (var19_30.getBagId().equals(PKCS12KeyStoreSpi.pkcs8ShroudedKeyBag)) {
                            var20_31 = EncryptedPrivateKeyInfo.getInstance(var19_30.getBagValue());
                            var21_32 = this.unwrapKey(var20_31.getEncryptionAlgorithm(), var20_31.getEncryptedData(), var2_2, var10_11);
                            var22_33 = (PKCS12BagAttributeCarrier)var21_32;
                            var23_34 = null;
                            var24_35 /* !! */  = null;
                            var25_36 = var19_30.getBagAttributes().getObjects();
                            while (var25_36.hasMoreElements()) {
                                var26_37 = (ASN1Sequence)var25_36.nextElement();
                                var27_38 = (ASN1ObjectIdentifier)var26_37.getObjectAt(0);
                                var28_39 = (ASN1Set)var26_37.getObjectAt(1);
                                var29_40 = null;
                                if (var28_39.size() > 0) {
                                    var29_40 = (ASN1Primitive)var28_39.getObjectAt(0);
                                    var30_41 = var22_33.getBagAttribute((ASN1ObjectIdentifier)var27_38);
                                    if (var30_41 != null) {
                                        if (!var30_41.toASN1Primitive().equals((ASN1Primitive)var29_40)) {
                                            throw new IOException("attempt to add existing attribute with different value");
                                        }
                                    } else {
                                        var22_33.setBagAttribute((ASN1ObjectIdentifier)var27_38, var29_40);
                                    }
                                }
                                if (var27_38.equals(PKCS12KeyStoreSpi.pkcs_9_at_friendlyName)) {
                                    var23_34 = ((DERBMPString)var29_40).getString();
                                    this.keys.put((String)var23_34, var21_32);
                                    continue;
                                }
                                if (!var27_38.equals(PKCS12KeyStoreSpi.pkcs_9_at_localKeyId)) continue;
                                var24_35 /* !! */  = (ASN1OctetString)var29_40;
                            }
                            var26_37 = new String(Hex.encode(var24_35 /* !! */ .getOctets()));
                            if (var23_34 == null) {
                                this.keys.put((String)var26_37, var21_32);
                                continue;
                            }
                            this.localIds.put((String)var23_34, var26_37);
                            continue;
                        }
                        if (var19_30.getBagId().equals(PKCS12KeyStoreSpi.keyBag)) {
                            var20_31 = PrivateKeyInfo.getInstance(var19_30.getBagValue());
                            var21_32 = BouncyCastleProvider.getPrivateKey((PrivateKeyInfo)var20_31);
                            var22_33 = (PKCS12BagAttributeCarrier)var21_32;
                            var23_34 = null;
                            var24_35 /* !! */  = null;
                            var25_36 = var19_30.getBagAttributes().getObjects();
                            while (var25_36.hasMoreElements()) {
                                var26_37 = ASN1Sequence.getInstance(var25_36.nextElement());
                                var27_38 = ASN1ObjectIdentifier.getInstance(var26_37.getObjectAt(0));
                                var28_39 = ASN1Set.getInstance(var26_37.getObjectAt(1));
                                var29_40 = null;
                                if (var28_39.size() <= 0) continue;
                                var29_40 = (ASN1Primitive)var28_39.getObjectAt(0);
                                var30_41 = var22_33.getBagAttribute((ASN1ObjectIdentifier)var27_38);
                                if (var30_41 != null) {
                                    if (!var30_41.toASN1Primitive().equals((ASN1Primitive)var29_40)) {
                                        throw new IOException("attempt to add existing attribute with different value");
                                    }
                                } else {
                                    var22_33.setBagAttribute((ASN1ObjectIdentifier)var27_38, var29_40);
                                }
                                if (var27_38.equals(PKCS12KeyStoreSpi.pkcs_9_at_friendlyName)) {
                                    var23_34 = ((DERBMPString)var29_40).getString();
                                    this.keys.put((String)var23_34, var21_32);
                                    continue;
                                }
                                if (!var27_38.equals(PKCS12KeyStoreSpi.pkcs_9_at_localKeyId)) continue;
                                var24_35 /* !! */  = (ASN1OctetString)var29_40;
                            }
                            var26_37 = new String(Hex.encode(var24_35 /* !! */ .getOctets()));
                            if (var23_34 == null) {
                                this.keys.put((String)var26_37, var21_32);
                                continue;
                            }
                            this.localIds.put((String)var23_34, var26_37);
                            continue;
                        }
                        System.out.println("extra in encryptedData " + var19_30.getBagId());
                        System.out.println(ASN1Dump.dumpAsString(var19_30));
                    }
                    continue;
                }
                System.out.println("extra " + var13_15[var14_17].getContentType().getId());
                System.out.println("extra " + ASN1Dump.dumpAsString(var13_15[var14_17].getContent()));
            }
        }
        this.certs = new IgnoresCaseHashtable();
        this.chainCerts = new Hashtable<K, V>();
        this.keyCerts = new Hashtable<K, V>();
        for (var11_13 = 0; var11_13 != var8_9.size(); ++var11_13) {
            var12_14 = (SafeBag)var8_9.elementAt(var11_13);
            var13_15 = CertBag.getInstance(var12_14.getBagValue());
            if (!var13_15.getCertId().equals(PKCS12KeyStoreSpi.x509Certificate)) {
                throw new RuntimeException("Unsupported certificate type: " + var13_15.getCertId());
            }
            try {
                var15_19 = new ByteArrayInputStream(((ASN1OctetString)var13_15.getCertValue()).getOctets());
                var14_18 = this.certFact.generateCertificate((InputStream)var15_19);
            } catch (Exception var15_22) {
                throw new RuntimeException(var15_22.toString());
            }
            var15_19 = null;
            var16_23 /* !! */  = null;
            if (var12_14.getBagAttributes() != null) {
                var17_26 = var12_14.getBagAttributes().getObjects();
                while (var17_26.hasMoreElements()) {
                    var18_29 = ASN1Sequence.getInstance(var17_26.nextElement());
                    var19_30 = ASN1ObjectIdentifier.getInstance(var18_29.getObjectAt(0));
                    var20_31 = ASN1Set.getInstance(var18_29.getObjectAt(1));
                    if (var20_31.size() <= 0) continue;
                    var21_32 = (ASN1Primitive)var20_31.getObjectAt(0);
                    var22_33 = null;
                    if (var14_18 instanceof PKCS12BagAttributeCarrier) {
                        var22_33 = (PKCS12BagAttributeCarrier)var14_18;
                        var23_34 = var22_33.getBagAttribute((ASN1ObjectIdentifier)var19_30);
                        if (var23_34 != null) {
                            if (!var23_34.toASN1Primitive().equals((ASN1Primitive)var21_32)) {
                                throw new IOException("attempt to add existing attribute with different value");
                            }
                        } else {
                            var22_33.setBagAttribute((ASN1ObjectIdentifier)var19_30, (ASN1Encodable)var21_32);
                        }
                    }
                    if (var19_30.equals(PKCS12KeyStoreSpi.pkcs_9_at_friendlyName)) {
                        var16_23 /* !! */  = (byte[])((DERBMPString)var21_32).getString();
                        continue;
                    }
                    if (!var19_30.equals(PKCS12KeyStoreSpi.pkcs_9_at_localKeyId)) continue;
                    var15_19 = (ASN1OctetString)var21_32;
                }
            }
            this.chainCerts.put(new CertId(var14_18.getPublicKey()), var14_18);
            if (var9_10) {
                if (!this.keyCerts.isEmpty()) continue;
                var17_26 = new String(Hex.encode(this.createSubjectKeyId(var14_18.getPublicKey()).getKeyIdentifier()));
                this.keyCerts.put(var17_26, var14_18);
                this.keys.put((String)var17_26, this.keys.remove("unmarked"));
                continue;
            }
            if (var15_19 != null) {
                var17_26 = new String(Hex.encode(var15_19.getOctets()));
                this.keyCerts.put(var17_26, var14_18);
            }
            if (var16_23 /* !! */  == null) continue;
            this.certs.put((String)var16_23 /* !! */ , var14_18);
        }
    }

    private int validateIterationCount(BigInteger bigInteger) {
        int n = bigInteger.intValue();
        if (n < 0) {
            throw new IllegalStateException("negative iteration count found");
        }
        BigInteger bigInteger2 = Properties.asBigInteger(PKCS12_MAX_IT_COUNT_PROPERTY);
        if (bigInteger2 != null && bigInteger2.intValue() < n) {
            throw new IllegalStateException("iteration count " + n + " greater than " + bigInteger2.intValue());
        }
        return n;
    }

    public void engineStore(KeyStore.LoadStoreParameter loadStoreParameter) throws IOException, NoSuchAlgorithmException, CertificateException {
        char[] cArray;
        if (loadStoreParameter == null) {
            throw new IllegalArgumentException("'param' arg cannot be null");
        }
        if (!(loadStoreParameter instanceof PKCS12StoreParameter) && !(loadStoreParameter instanceof JDKPKCS12StoreParameter)) {
            throw new IllegalArgumentException("No support for 'param' of type " + loadStoreParameter.getClass().getName());
        }
        PKCS12StoreParameter pKCS12StoreParameter = loadStoreParameter instanceof PKCS12StoreParameter ? (PKCS12StoreParameter)loadStoreParameter : new PKCS12StoreParameter(((JDKPKCS12StoreParameter)loadStoreParameter).getOutputStream(), loadStoreParameter.getProtectionParameter(), ((JDKPKCS12StoreParameter)loadStoreParameter).isUseDEREncoding());
        KeyStore.ProtectionParameter protectionParameter = loadStoreParameter.getProtectionParameter();
        if (protectionParameter == null) {
            cArray = null;
        } else if (protectionParameter instanceof KeyStore.PasswordProtection) {
            cArray = ((KeyStore.PasswordProtection)protectionParameter).getPassword();
        } else {
            throw new IllegalArgumentException("No support for protection parameter of type " + protectionParameter.getClass().getName());
        }
        this.doStore(pKCS12StoreParameter.getOutputStream(), cArray, pKCS12StoreParameter.isForDEREncoding());
    }

    public void engineStore(OutputStream outputStream, char[] cArray) throws IOException {
        this.doStore(outputStream, cArray, false);
    }

    private void doStore(OutputStream outputStream, char[] cArray, boolean bl) throws IOException {
        MacData macData;
        Object object;
        Object object2;
        Object object3;
        Object object4;
        Object object5;
        Object object6;
        ContentInfo[] contentInfoArray;
        Object object7;
        Object object8;
        Object object9;
        Object object10;
        AlgorithmIdentifier algorithmIdentifier;
        Object object11;
        Object object12;
        Object object13;
        Object object14;
        byte[] byArray;
        if (this.keys.size() == 0) {
            if (cArray == null) {
                Object object15;
                Object object16;
                Enumeration enumeration = this.certs.keys();
                ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
                while (enumeration.hasMoreElements()) {
                    try {
                        object16 = (String)enumeration.nextElement();
                        object15 = (Certificate)this.certs.get((String)object16);
                        SafeBag safeBag = this.createSafeBag((String)object16, (Certificate)object15);
                        aSN1EncodableVector.add(safeBag);
                    } catch (CertificateEncodingException certificateEncodingException) {
                        throw new IOException("Error encoding certificate: " + certificateEncodingException.toString());
                    }
                }
                if (bl) {
                    object16 = new ContentInfo(PKCSObjectIdentifiers.data, new DEROctetString(new DERSequence(aSN1EncodableVector).getEncoded()));
                    object15 = new Pfx(new ContentInfo(PKCSObjectIdentifiers.data, new DEROctetString(new DERSequence((ASN1Encodable)object16).getEncoded())), null);
                    ((ASN1Object)object15).encodeTo(outputStream, "DER");
                } else {
                    object16 = new ContentInfo(PKCSObjectIdentifiers.data, new BEROctetString(new BERSequence(aSN1EncodableVector).getEncoded()));
                    object15 = new Pfx(new ContentInfo(PKCSObjectIdentifiers.data, new BEROctetString(new BERSequence((ASN1Encodable)object16).getEncoded())), null);
                    ((ASN1Object)object15).encodeTo(outputStream, "BER");
                }
                return;
            }
        } else if (cArray == null) {
            throw new NullPointerException("no password supplied for PKCS#12 KeyStore");
        }
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        Enumeration enumeration = this.keys.keys();
        while (enumeration.hasMoreElements()) {
            Object object17;
            byArray = new byte[20];
            this.random.nextBytes(byArray);
            object14 = (String)enumeration.nextElement();
            object13 = (PrivateKey)this.keys.get((String)object14);
            object12 = new PKCS12PBEParams(byArray, 51200);
            object11 = this.wrapKey(this.keyAlgorithm.getId(), (Key)object13, (PKCS12PBEParams)object12, cArray);
            algorithmIdentifier = new AlgorithmIdentifier(this.keyAlgorithm, ((PKCS12PBEParams)object12).toASN1Primitive());
            object10 = new EncryptedPrivateKeyInfo(algorithmIdentifier, (byte[])object11);
            boolean bl2 = false;
            object9 = new ASN1EncodableVector();
            if (object13 instanceof PKCS12BagAttributeCarrier) {
                object8 = (PKCS12BagAttributeCarrier)object13;
                object17 = (DERBMPString)object8.getBagAttribute(pkcs_9_at_friendlyName);
                if (object17 == null || !((DERBMPString)object17).getString().equals(object14)) {
                    object8.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString((String)object14));
                }
                if (object8.getBagAttribute(pkcs_9_at_localKeyId) == null) {
                    object7 = this.engineGetCertificate((String)object14);
                    object8.setBagAttribute(pkcs_9_at_localKeyId, this.createSubjectKeyId(((Certificate)object7).getPublicKey()));
                }
                object7 = object8.getBagAttributeKeys();
                while (object7.hasMoreElements()) {
                    contentInfoArray = (ASN1ObjectIdentifier)object7.nextElement();
                    object6 = new ASN1EncodableVector();
                    ((ASN1EncodableVector)object6).add((ASN1Encodable)contentInfoArray);
                    ((ASN1EncodableVector)object6).add(new DERSet(object8.getBagAttribute((ASN1ObjectIdentifier)contentInfoArray)));
                    bl2 = true;
                    ((ASN1EncodableVector)object9).add(new DERSequence((ASN1EncodableVector)object6));
                }
            }
            if (!bl2) {
                object8 = new ASN1EncodableVector();
                object17 = this.engineGetCertificate((String)object14);
                ((ASN1EncodableVector)object8).add(pkcs_9_at_localKeyId);
                ((ASN1EncodableVector)object8).add(new DERSet(this.createSubjectKeyId(((Certificate)object17).getPublicKey())));
                ((ASN1EncodableVector)object9).add(new DERSequence((ASN1EncodableVector)object8));
                object8 = new ASN1EncodableVector();
                ((ASN1EncodableVector)object8).add(pkcs_9_at_friendlyName);
                ((ASN1EncodableVector)object8).add(new DERSet(new DERBMPString((String)object14)));
                ((ASN1EncodableVector)object9).add(new DERSequence((ASN1EncodableVector)object8));
            }
            object8 = new SafeBag(pkcs8ShroudedKeyBag, ((EncryptedPrivateKeyInfo)object10).toASN1Primitive(), new DERSet((ASN1EncodableVector)object9));
            aSN1EncodableVector.add((ASN1Encodable)object8);
        }
        byArray = new DERSequence(aSN1EncodableVector).getEncoded("DER");
        object14 = new BEROctetString(byArray);
        object13 = new byte[20];
        this.random.nextBytes((byte[])object13);
        object12 = new ASN1EncodableVector();
        object11 = new PKCS12PBEParams((byte[])object13, 51200);
        algorithmIdentifier = new AlgorithmIdentifier(this.certAlgorithm, ((PKCS12PBEParams)object11).toASN1Primitive());
        object10 = new Hashtable();
        Enumeration enumeration2 = this.keys.keys();
        while (enumeration2.hasMoreElements()) {
            try {
                object9 = (String)enumeration2.nextElement();
                object8 = this.engineGetCertificate((String)object9);
                boolean bl3 = false;
                object7 = new CertBag(x509Certificate, new DEROctetString(((Certificate)object8).getEncoded()));
                contentInfoArray = new ASN1EncodableVector();
                if (object8 instanceof PKCS12BagAttributeCarrier) {
                    object6 = (PKCS12BagAttributeCarrier)object8;
                    object5 = (DERBMPString)object6.getBagAttribute(pkcs_9_at_friendlyName);
                    if (object5 == null || !((DERBMPString)object5).getString().equals(object9)) {
                        object6.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString((String)object9));
                    }
                    if (object6.getBagAttribute(pkcs_9_at_localKeyId) == null) {
                        object6.setBagAttribute(pkcs_9_at_localKeyId, this.createSubjectKeyId(((Certificate)object8).getPublicKey()));
                    }
                    object4 = object6.getBagAttributeKeys();
                    while (object4.hasMoreElements()) {
                        object3 = (ASN1ObjectIdentifier)object4.nextElement();
                        object2 = new ASN1EncodableVector();
                        ((ASN1EncodableVector)object2).add((ASN1Encodable)object3);
                        ((ASN1EncodableVector)object2).add(new DERSet(object6.getBagAttribute((ASN1ObjectIdentifier)object3)));
                        contentInfoArray.add(new DERSequence((ASN1EncodableVector)object2));
                        bl3 = true;
                    }
                }
                if (!bl3) {
                    object6 = new ASN1EncodableVector();
                    ((ASN1EncodableVector)object6).add(pkcs_9_at_localKeyId);
                    ((ASN1EncodableVector)object6).add(new DERSet(this.createSubjectKeyId(((Certificate)object8).getPublicKey())));
                    contentInfoArray.add(new DERSequence((ASN1EncodableVector)object6));
                    object6 = new ASN1EncodableVector();
                    ((ASN1EncodableVector)object6).add(pkcs_9_at_friendlyName);
                    ((ASN1EncodableVector)object6).add(new DERSet(new DERBMPString((String)object9)));
                    contentInfoArray.add(new DERSequence((ASN1EncodableVector)object6));
                }
                object6 = new SafeBag(certBag, ((CertBag)object7).toASN1Primitive(), new DERSet((ASN1EncodableVector)contentInfoArray));
                ((ASN1EncodableVector)object12).add((ASN1Encodable)object6);
                ((Hashtable)object10).put(object8, object8);
            } catch (CertificateEncodingException certificateEncodingException) {
                throw new IOException("Error encoding certificate: " + certificateEncodingException.toString());
            }
        }
        enumeration2 = this.certs.keys();
        while (enumeration2.hasMoreElements()) {
            try {
                object9 = (String)enumeration2.nextElement();
                object8 = (Certificate)this.certs.get((String)object9);
                if (this.keys.get((String)object9) != null) continue;
                SafeBag safeBag = this.createSafeBag((String)object9, (Certificate)object8);
                ((ASN1EncodableVector)object12).add(safeBag);
                ((Hashtable)object10).put(object8, object8);
            } catch (CertificateEncodingException certificateEncodingException) {
                throw new IOException("Error encoding certificate: " + certificateEncodingException.toString());
            }
        }
        object9 = this.getUsedCertificateSet();
        enumeration2 = this.chainCerts.keys();
        while (enumeration2.hasMoreElements()) {
            try {
                object8 = (CertId)enumeration2.nextElement();
                Certificate certificate = (Certificate)this.chainCerts.get(object8);
                if (!object9.contains(certificate) || ((Hashtable)object10).get(certificate) != null) continue;
                object7 = new CertBag(x509Certificate, new DEROctetString(certificate.getEncoded()));
                contentInfoArray = new ASN1EncodableVector();
                if (certificate instanceof PKCS12BagAttributeCarrier) {
                    object6 = (PKCS12BagAttributeCarrier)((Object)certificate);
                    object5 = object6.getBagAttributeKeys();
                    while (object5.hasMoreElements()) {
                        object4 = (ASN1ObjectIdentifier)object5.nextElement();
                        if (((ASN1Primitive)object4).equals(PKCSObjectIdentifiers.pkcs_9_at_localKeyId)) continue;
                        object3 = new ASN1EncodableVector();
                        ((ASN1EncodableVector)object3).add((ASN1Encodable)object4);
                        ((ASN1EncodableVector)object3).add(new DERSet(object6.getBagAttribute((ASN1ObjectIdentifier)object4)));
                        contentInfoArray.add(new DERSequence((ASN1EncodableVector)object3));
                    }
                }
                object6 = new SafeBag(certBag, ((CertBag)object7).toASN1Primitive(), new DERSet((ASN1EncodableVector)contentInfoArray));
                ((ASN1EncodableVector)object12).add((ASN1Encodable)object6);
            } catch (CertificateEncodingException certificateEncodingException) {
                throw new IOException("Error encoding certificate: " + certificateEncodingException.toString());
            }
        }
        object8 = new DERSequence((ASN1EncodableVector)object12).getEncoded("DER");
        byte[] byArray2 = this.cryptData(true, algorithmIdentifier, cArray, false, (byte[])object8);
        object7 = new EncryptedData(data, algorithmIdentifier, new BEROctetString(byArray2));
        contentInfoArray = new ContentInfo[]{new ContentInfo(data, (ASN1Encodable)object14), new ContentInfo(encryptedData, ((EncryptedData)object7).toASN1Primitive())};
        object6 = new AuthenticatedSafe(contentInfoArray);
        object5 = ((ASN1Object)object6).getEncoded(bl ? "DER" : "BER");
        object4 = new ContentInfo(data, new BEROctetString((byte[])object5));
        object3 = new byte[this.saltLength];
        this.random.nextBytes((byte[])object3);
        object2 = ((ASN1OctetString)((ContentInfo)object4).getContent()).getOctets();
        try {
            object = this.calculatePbeMac(this.macAlgorithm.getAlgorithm(), (byte[])object3, this.itCount, cArray, false, (byte[])object2);
            DigestInfo digestInfo = new DigestInfo(this.macAlgorithm, (byte[])object);
            macData = new MacData(digestInfo, (byte[])object3, this.itCount);
        } catch (Exception exception) {
            throw new IOException("error constructing MAC: " + exception.toString());
        }
        object = new Pfx((ContentInfo)object4, macData);
        ((ASN1Object)object).encodeTo(outputStream, bl ? "DER" : "BER");
    }

    private SafeBag createSafeBag(String string, Certificate certificate) throws CertificateEncodingException {
        Object object;
        CertBag certBag = new CertBag(x509Certificate, new DEROctetString(certificate.getEncoded()));
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        boolean bl = false;
        if (certificate instanceof PKCS12BagAttributeCarrier) {
            object = (PKCS12BagAttributeCarrier)((Object)certificate);
            DERBMPString dERBMPString = (DERBMPString)object.getBagAttribute(pkcs_9_at_friendlyName);
            if (!(dERBMPString != null && dERBMPString.getString().equals(string) || string == null)) {
                object.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(string));
            }
            Enumeration enumeration = object.getBagAttributeKeys();
            while (enumeration.hasMoreElements()) {
                ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier)enumeration.nextElement();
                if (aSN1ObjectIdentifier.equals(PKCSObjectIdentifiers.pkcs_9_at_localKeyId)) continue;
                ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
                aSN1EncodableVector2.add(aSN1ObjectIdentifier);
                aSN1EncodableVector2.add(new DERSet(object.getBagAttribute(aSN1ObjectIdentifier)));
                aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
                bl = true;
            }
        }
        if (!bl) {
            object = new ASN1EncodableVector();
            ((ASN1EncodableVector)object).add(pkcs_9_at_friendlyName);
            ((ASN1EncodableVector)object).add(new DERSet(new DERBMPString(string)));
            aSN1EncodableVector.add(new DERSequence((ASN1EncodableVector)object));
        }
        return new SafeBag(PKCS12KeyStoreSpi.certBag, certBag.toASN1Primitive(), new DERSet(aSN1EncodableVector));
    }

    private Set getUsedCertificateSet() {
        Object object;
        String string;
        HashSet<Object> hashSet = new HashSet<Object>();
        Enumeration enumeration = this.keys.keys();
        while (enumeration.hasMoreElements()) {
            string = (String)enumeration.nextElement();
            object = this.engineGetCertificateChain(string);
            for (int i = 0; i != ((Certificate[])object).length; ++i) {
                hashSet.add(object[i]);
            }
        }
        enumeration = this.certs.keys();
        while (enumeration.hasMoreElements()) {
            string = (String)enumeration.nextElement();
            object = this.engineGetCertificate(string);
            hashSet.add(object);
        }
        return hashSet;
    }

    private byte[] calculatePbeMac(ASN1ObjectIdentifier aSN1ObjectIdentifier, byte[] byArray, int n, char[] cArray, boolean bl, byte[] byArray2) throws Exception {
        PBEParameterSpec pBEParameterSpec = new PBEParameterSpec(byArray, n);
        Mac mac = this.helper.createMac(aSN1ObjectIdentifier.getId());
        mac.init(new PKCS12Key(cArray, bl), pBEParameterSpec);
        mac.update(byArray2);
        return mac.doFinal();
    }

    public static class BCPKCS12KeyStore
    extends AdaptingKeyStoreSpi {
        public BCPKCS12KeyStore() {
            super(new BCJcaJceHelper(), new PKCS12KeyStoreSpi(new BCJcaJceHelper(), PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC2_CBC));
        }
    }

    public static class BCPKCS12KeyStore3DES
    extends AdaptingKeyStoreSpi {
        public BCPKCS12KeyStore3DES() {
            super(new BCJcaJceHelper(), new PKCS12KeyStoreSpi(new BCJcaJceHelper(), PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC));
        }
    }

    private class CertId {
        byte[] id;

        CertId(PublicKey publicKey) {
            this.id = PKCS12KeyStoreSpi.this.createSubjectKeyId(publicKey).getKeyIdentifier();
        }

        CertId(byte[] byArray) {
            this.id = byArray;
        }

        public int hashCode() {
            return Arrays.hashCode(this.id);
        }

        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof CertId)) {
                return false;
            }
            CertId certId = (CertId)object;
            return Arrays.areEqual(this.id, certId.id);
        }
    }

    public static class DefPKCS12KeyStore
    extends AdaptingKeyStoreSpi {
        public DefPKCS12KeyStore() {
            super(new DefaultJcaJceHelper(), new PKCS12KeyStoreSpi(new DefaultJcaJceHelper(), PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC2_CBC));
        }
    }

    public static class DefPKCS12KeyStore3DES
    extends AdaptingKeyStoreSpi {
        public DefPKCS12KeyStore3DES() {
            super(new DefaultJcaJceHelper(), new PKCS12KeyStoreSpi(new DefaultJcaJceHelper(), PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC));
        }
    }

    private static class DefaultSecretKeyProvider {
        private final Map KEY_SIZES;

        DefaultSecretKeyProvider() {
            HashMap<ASN1ObjectIdentifier, Integer> hashMap = new HashMap<ASN1ObjectIdentifier, Integer>();
            hashMap.put(new ASN1ObjectIdentifier("1.2.840.113533.7.66.10"), Integers.valueOf(128));
            hashMap.put(PKCSObjectIdentifiers.des_EDE3_CBC, Integers.valueOf(192));
            hashMap.put(NISTObjectIdentifiers.id_aes128_CBC, Integers.valueOf(128));
            hashMap.put(NISTObjectIdentifiers.id_aes192_CBC, Integers.valueOf(192));
            hashMap.put(NISTObjectIdentifiers.id_aes256_CBC, Integers.valueOf(256));
            hashMap.put(NTTObjectIdentifiers.id_camellia128_cbc, Integers.valueOf(128));
            hashMap.put(NTTObjectIdentifiers.id_camellia192_cbc, Integers.valueOf(192));
            hashMap.put(NTTObjectIdentifiers.id_camellia256_cbc, Integers.valueOf(256));
            hashMap.put(CryptoProObjectIdentifiers.gostR28147_gcfb, Integers.valueOf(256));
            this.KEY_SIZES = Collections.unmodifiableMap(hashMap);
        }

        public int getKeySize(AlgorithmIdentifier algorithmIdentifier) {
            Integer n = (Integer)this.KEY_SIZES.get(algorithmIdentifier.getAlgorithm());
            if (n != null) {
                return n;
            }
            return -1;
        }
    }

    private static class IgnoresCaseHashtable {
        private Hashtable orig = new Hashtable();
        private Hashtable keys = new Hashtable();

        private IgnoresCaseHashtable() {
        }

        public void put(String string, Object object) {
            String string2 = string == null ? null : Strings.toLowerCase(string);
            String string3 = (String)this.keys.get(string2);
            if (string3 != null) {
                this.orig.remove(string3);
            }
            this.keys.put(string2, string);
            this.orig.put(string, object);
        }

        public Enumeration keys() {
            return this.orig.keys();
        }

        public Object remove(String string) {
            String string2 = (String)this.keys.remove(string == null ? null : Strings.toLowerCase(string));
            if (string2 == null) {
                return null;
            }
            return this.orig.remove(string2);
        }

        public Object get(String string) {
            String string2 = (String)this.keys.get(string == null ? null : Strings.toLowerCase(string));
            if (string2 == null) {
                return null;
            }
            return this.orig.get(string2);
        }

        public Enumeration elements() {
            return this.orig.elements();
        }

        public int size() {
            return this.orig.size();
        }
    }
}

