/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.util;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.crypto.KeyAgreementSpi;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.gnu.GNUObjectIdentifiers;
import org.bouncycastle.asn1.kisa.KISAObjectIdentifiers;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.crypto.DerivationFunction;
import org.bouncycastle.crypto.agreement.kdf.DHKDFParameters;
import org.bouncycastle.crypto.agreement.kdf.DHKEKGenerator;
import org.bouncycastle.crypto.params.DESParameters;
import org.bouncycastle.crypto.params.KDFParameters;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Integers;
import org.bouncycastle.util.Strings;

public abstract class BaseAgreementSpi
extends KeyAgreementSpi {
    private static final Map<String, ASN1ObjectIdentifier> defaultOids = new HashMap<String, ASN1ObjectIdentifier>();
    private static final Map<String, Integer> keySizes = new HashMap<String, Integer>();
    private static final Map<String, String> nameTable = new HashMap<String, String>();
    private static final Hashtable oids = new Hashtable();
    private static final Hashtable des = new Hashtable();
    protected final String kaAlgorithm;
    protected final DerivationFunction kdf;
    protected byte[] ukmParameters;

    public BaseAgreementSpi(String string, DerivationFunction derivationFunction) {
        this.kaAlgorithm = string;
        this.kdf = derivationFunction;
    }

    protected static String getAlgorithm(String string) {
        if (string.indexOf(91) > 0) {
            return string.substring(0, string.indexOf(91));
        }
        if (string.startsWith(NISTObjectIdentifiers.aes.getId())) {
            return "AES";
        }
        if (string.startsWith(GNUObjectIdentifiers.Serpent.getId())) {
            return "Serpent";
        }
        String string2 = nameTable.get(Strings.toUpperCase(string));
        if (string2 != null) {
            return string2;
        }
        return string;
    }

    protected static int getKeySize(String string) {
        if (string.indexOf(91) > 0) {
            return Integer.parseInt(string.substring(string.indexOf(91) + 1, string.indexOf(93)));
        }
        String string2 = Strings.toUpperCase(string);
        if (!keySizes.containsKey(string2)) {
            return -1;
        }
        return keySizes.get(string2);
    }

    protected static byte[] trimZeroes(byte[] byArray) {
        int n;
        if (byArray[0] != 0) {
            return byArray;
        }
        for (n = 0; n < byArray.length && byArray[n] == 0; ++n) {
        }
        byte[] byArray2 = new byte[byArray.length - n];
        System.arraycopy(byArray, n, byArray2, 0, byArray2.length);
        return byArray2;
    }

    protected byte[] engineGenerateSecret() throws IllegalStateException {
        if (this.kdf != null) {
            byte[] byArray = this.calcSecret();
            try {
                return this.getSharedSecretBytes(byArray, null, byArray.length * 8);
            } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                throw new IllegalStateException(noSuchAlgorithmException.getMessage());
            }
        }
        return this.calcSecret();
    }

    protected int engineGenerateSecret(byte[] byArray, int n) throws IllegalStateException, ShortBufferException {
        byte[] byArray2 = this.engineGenerateSecret();
        if (byArray.length - n < byArray2.length) {
            throw new ShortBufferException(this.kaAlgorithm + " key agreement: need " + byArray2.length + " bytes");
        }
        System.arraycopy(byArray2, 0, byArray, n, byArray2.length);
        return byArray2.length;
    }

    protected SecretKey engineGenerateSecret(String string) throws NoSuchAlgorithmException {
        String string2 = Strings.toUpperCase(string);
        String string3 = string;
        if (oids.containsKey(string2)) {
            string3 = ((ASN1ObjectIdentifier)oids.get(string2)).getId();
        }
        int n = BaseAgreementSpi.getKeySize(string3);
        byte[] byArray = this.getSharedSecretBytes(this.calcSecret(), string3, n);
        String string4 = BaseAgreementSpi.getAlgorithm(string);
        if (des.containsKey(string4)) {
            DESParameters.setOddParity(byArray);
        }
        return new SecretKeySpec(byArray, string4);
    }

    private byte[] getSharedSecretBytes(byte[] byArray, String string, int n) throws NoSuchAlgorithmException {
        if (this.kdf != null) {
            if (n < 0) {
                throw new NoSuchAlgorithmException("unknown algorithm encountered: " + string);
            }
            byte[] byArray2 = new byte[n / 8];
            if (this.kdf instanceof DHKEKGenerator) {
                ASN1ObjectIdentifier aSN1ObjectIdentifier;
                if (string == null) {
                    throw new NoSuchAlgorithmException("algorithm OID is null");
                }
                try {
                    aSN1ObjectIdentifier = new ASN1ObjectIdentifier(string);
                } catch (IllegalArgumentException illegalArgumentException) {
                    throw new NoSuchAlgorithmException("no OID for algorithm: " + string);
                }
                DHKDFParameters dHKDFParameters = new DHKDFParameters(aSN1ObjectIdentifier, n, byArray, this.ukmParameters);
                this.kdf.init(dHKDFParameters);
            } else {
                KDFParameters kDFParameters = new KDFParameters(byArray, this.ukmParameters);
                this.kdf.init(kDFParameters);
            }
            this.kdf.generateBytes(byArray2, 0, byArray2.length);
            Arrays.clear(byArray);
            return byArray2;
        }
        if (n > 0) {
            byte[] byArray3 = new byte[n / 8];
            System.arraycopy(byArray, 0, byArray3, 0, byArray3.length);
            Arrays.clear(byArray);
            return byArray3;
        }
        return byArray;
    }

    protected abstract byte[] calcSecret();

    static {
        Integer n = Integers.valueOf(64);
        Integer n2 = Integers.valueOf(128);
        Integer n3 = Integers.valueOf(192);
        Integer n4 = Integers.valueOf(256);
        keySizes.put("DES", n);
        keySizes.put("DESEDE", n3);
        keySizes.put("BLOWFISH", n2);
        keySizes.put("AES", n4);
        keySizes.put(NISTObjectIdentifiers.id_aes128_ECB.getId(), n2);
        keySizes.put(NISTObjectIdentifiers.id_aes192_ECB.getId(), n3);
        keySizes.put(NISTObjectIdentifiers.id_aes256_ECB.getId(), n4);
        keySizes.put(NISTObjectIdentifiers.id_aes128_CBC.getId(), n2);
        keySizes.put(NISTObjectIdentifiers.id_aes192_CBC.getId(), n3);
        keySizes.put(NISTObjectIdentifiers.id_aes256_CBC.getId(), n4);
        keySizes.put(NISTObjectIdentifiers.id_aes128_CFB.getId(), n2);
        keySizes.put(NISTObjectIdentifiers.id_aes192_CFB.getId(), n3);
        keySizes.put(NISTObjectIdentifiers.id_aes256_CFB.getId(), n4);
        keySizes.put(NISTObjectIdentifiers.id_aes128_OFB.getId(), n2);
        keySizes.put(NISTObjectIdentifiers.id_aes192_OFB.getId(), n3);
        keySizes.put(NISTObjectIdentifiers.id_aes256_OFB.getId(), n4);
        keySizes.put(NISTObjectIdentifiers.id_aes128_wrap.getId(), n2);
        keySizes.put(NISTObjectIdentifiers.id_aes192_wrap.getId(), n3);
        keySizes.put(NISTObjectIdentifiers.id_aes256_wrap.getId(), n4);
        keySizes.put(NISTObjectIdentifiers.id_aes128_CCM.getId(), n2);
        keySizes.put(NISTObjectIdentifiers.id_aes192_CCM.getId(), n3);
        keySizes.put(NISTObjectIdentifiers.id_aes256_CCM.getId(), n4);
        keySizes.put(NISTObjectIdentifiers.id_aes128_GCM.getId(), n2);
        keySizes.put(NISTObjectIdentifiers.id_aes192_GCM.getId(), n3);
        keySizes.put(NISTObjectIdentifiers.id_aes256_GCM.getId(), n4);
        keySizes.put(NTTObjectIdentifiers.id_camellia128_wrap.getId(), n2);
        keySizes.put(NTTObjectIdentifiers.id_camellia192_wrap.getId(), n3);
        keySizes.put(NTTObjectIdentifiers.id_camellia256_wrap.getId(), n4);
        keySizes.put(KISAObjectIdentifiers.id_npki_app_cmsSeed_wrap.getId(), n2);
        keySizes.put(PKCSObjectIdentifiers.id_alg_CMS3DESwrap.getId(), n3);
        keySizes.put(PKCSObjectIdentifiers.des_EDE3_CBC.getId(), n3);
        keySizes.put(OIWObjectIdentifiers.desCBC.getId(), n);
        keySizes.put(CryptoProObjectIdentifiers.gostR28147_gcfb.getId(), n4);
        keySizes.put(CryptoProObjectIdentifiers.id_Gost28147_89_None_KeyWrap.getId(), n4);
        keySizes.put(CryptoProObjectIdentifiers.id_Gost28147_89_CryptoPro_KeyWrap.getId(), n4);
        keySizes.put(PKCSObjectIdentifiers.id_hmacWithSHA1.getId(), Integers.valueOf(160));
        keySizes.put(PKCSObjectIdentifiers.id_hmacWithSHA256.getId(), n4);
        keySizes.put(PKCSObjectIdentifiers.id_hmacWithSHA384.getId(), Integers.valueOf(384));
        keySizes.put(PKCSObjectIdentifiers.id_hmacWithSHA512.getId(), Integers.valueOf(512));
        defaultOids.put("DESEDE", PKCSObjectIdentifiers.des_EDE3_CBC);
        defaultOids.put("AES", NISTObjectIdentifiers.id_aes256_CBC);
        defaultOids.put("CAMELLIA", NTTObjectIdentifiers.id_camellia256_cbc);
        defaultOids.put("SEED", KISAObjectIdentifiers.id_seedCBC);
        defaultOids.put("DES", OIWObjectIdentifiers.desCBC);
        nameTable.put(MiscObjectIdentifiers.cast5CBC.getId(), "CAST5");
        nameTable.put(MiscObjectIdentifiers.as_sys_sec_alg_ideaCBC.getId(), "IDEA");
        nameTable.put(MiscObjectIdentifiers.cryptlib_algorithm_blowfish_ECB.getId(), "Blowfish");
        nameTable.put(MiscObjectIdentifiers.cryptlib_algorithm_blowfish_CBC.getId(), "Blowfish");
        nameTable.put(MiscObjectIdentifiers.cryptlib_algorithm_blowfish_CFB.getId(), "Blowfish");
        nameTable.put(MiscObjectIdentifiers.cryptlib_algorithm_blowfish_OFB.getId(), "Blowfish");
        nameTable.put(OIWObjectIdentifiers.desECB.getId(), "DES");
        nameTable.put(OIWObjectIdentifiers.desCBC.getId(), "DES");
        nameTable.put(OIWObjectIdentifiers.desCFB.getId(), "DES");
        nameTable.put(OIWObjectIdentifiers.desOFB.getId(), "DES");
        nameTable.put(OIWObjectIdentifiers.desEDE.getId(), "DESede");
        nameTable.put(PKCSObjectIdentifiers.des_EDE3_CBC.getId(), "DESede");
        nameTable.put(PKCSObjectIdentifiers.id_alg_CMS3DESwrap.getId(), "DESede");
        nameTable.put(PKCSObjectIdentifiers.id_alg_CMSRC2wrap.getId(), "RC2");
        nameTable.put(PKCSObjectIdentifiers.id_hmacWithSHA1.getId(), "HmacSHA1");
        nameTable.put(PKCSObjectIdentifiers.id_hmacWithSHA224.getId(), "HmacSHA224");
        nameTable.put(PKCSObjectIdentifiers.id_hmacWithSHA256.getId(), "HmacSHA256");
        nameTable.put(PKCSObjectIdentifiers.id_hmacWithSHA384.getId(), "HmacSHA384");
        nameTable.put(PKCSObjectIdentifiers.id_hmacWithSHA512.getId(), "HmacSHA512");
        nameTable.put(NTTObjectIdentifiers.id_camellia128_cbc.getId(), "Camellia");
        nameTable.put(NTTObjectIdentifiers.id_camellia192_cbc.getId(), "Camellia");
        nameTable.put(NTTObjectIdentifiers.id_camellia256_cbc.getId(), "Camellia");
        nameTable.put(NTTObjectIdentifiers.id_camellia128_wrap.getId(), "Camellia");
        nameTable.put(NTTObjectIdentifiers.id_camellia192_wrap.getId(), "Camellia");
        nameTable.put(NTTObjectIdentifiers.id_camellia256_wrap.getId(), "Camellia");
        nameTable.put(KISAObjectIdentifiers.id_npki_app_cmsSeed_wrap.getId(), "SEED");
        nameTable.put(KISAObjectIdentifiers.id_seedCBC.getId(), "SEED");
        nameTable.put(KISAObjectIdentifiers.id_seedMAC.getId(), "SEED");
        nameTable.put(CryptoProObjectIdentifiers.gostR28147_gcfb.getId(), "GOST28147");
        nameTable.put(NISTObjectIdentifiers.id_aes128_wrap.getId(), "AES");
        nameTable.put(NISTObjectIdentifiers.id_aes128_CCM.getId(), "AES");
        nameTable.put(NISTObjectIdentifiers.id_aes128_CCM.getId(), "AES");
        oids.put("DESEDE", PKCSObjectIdentifiers.des_EDE3_CBC);
        oids.put("AES", NISTObjectIdentifiers.id_aes256_CBC);
        oids.put("DES", OIWObjectIdentifiers.desCBC);
        des.put("DES", "DES");
        des.put("DESEDE", "DES");
        des.put(OIWObjectIdentifiers.desCBC.getId(), "DES");
        des.put(PKCSObjectIdentifiers.des_EDE3_CBC.getId(), "DES");
        des.put(PKCSObjectIdentifiers.id_alg_CMS3DESwrap.getId(), "DES");
    }
}

