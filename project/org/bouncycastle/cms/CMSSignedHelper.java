/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cms;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.cms.OtherRevocationInfoFormat;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.eac.EACObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AttributeCertificate;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.CertificateList;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.Store;

class CMSSignedHelper {
    static final CMSSignedHelper INSTANCE = new CMSSignedHelper();
    private static final Map encryptionAlgs = new HashMap();

    CMSSignedHelper() {
    }

    private static void addEntries(ASN1ObjectIdentifier aSN1ObjectIdentifier, String string) {
        encryptionAlgs.put(aSN1ObjectIdentifier.getId(), string);
    }

    String getEncryptionAlgName(String string) {
        String string2 = (String)encryptionAlgs.get(string);
        if (string2 != null) {
            return string2;
        }
        return string;
    }

    AlgorithmIdentifier fixDigestAlgID(AlgorithmIdentifier algorithmIdentifier, DigestAlgorithmIdentifierFinder digestAlgorithmIdentifierFinder) {
        ASN1Encodable aSN1Encodable = algorithmIdentifier.getParameters();
        if (aSN1Encodable == null || DERNull.INSTANCE.equals(aSN1Encodable)) {
            return digestAlgorithmIdentifierFinder.find(algorithmIdentifier.getAlgorithm());
        }
        return algorithmIdentifier;
    }

    void setSigningEncryptionAlgorithmMapping(ASN1ObjectIdentifier aSN1ObjectIdentifier, String string) {
        CMSSignedHelper.addEntries(aSN1ObjectIdentifier, string);
    }

    Store getCertificates(ASN1Set aSN1Set) {
        if (aSN1Set != null) {
            ArrayList<X509CertificateHolder> arrayList = new ArrayList<X509CertificateHolder>(aSN1Set.size());
            Enumeration enumeration = aSN1Set.getObjects();
            while (enumeration.hasMoreElements()) {
                ASN1Primitive aSN1Primitive = ((ASN1Encodable)enumeration.nextElement()).toASN1Primitive();
                if (!(aSN1Primitive instanceof ASN1Sequence)) continue;
                arrayList.add(new X509CertificateHolder(Certificate.getInstance(aSN1Primitive)));
            }
            return new CollectionStore(arrayList);
        }
        return new CollectionStore(new ArrayList());
    }

    Store getAttributeCertificates(ASN1Set aSN1Set) {
        if (aSN1Set != null) {
            ArrayList<X509AttributeCertificateHolder> arrayList = new ArrayList<X509AttributeCertificateHolder>(aSN1Set.size());
            Enumeration enumeration = aSN1Set.getObjects();
            while (enumeration.hasMoreElements()) {
                ASN1Primitive aSN1Primitive = ((ASN1Encodable)enumeration.nextElement()).toASN1Primitive();
                if (!(aSN1Primitive instanceof ASN1TaggedObject)) continue;
                arrayList.add(new X509AttributeCertificateHolder(AttributeCertificate.getInstance(((ASN1TaggedObject)aSN1Primitive).getObject())));
            }
            return new CollectionStore(arrayList);
        }
        return new CollectionStore(new ArrayList());
    }

    Store getCRLs(ASN1Set aSN1Set) {
        if (aSN1Set != null) {
            ArrayList<X509CRLHolder> arrayList = new ArrayList<X509CRLHolder>(aSN1Set.size());
            Enumeration enumeration = aSN1Set.getObjects();
            while (enumeration.hasMoreElements()) {
                ASN1Primitive aSN1Primitive = ((ASN1Encodable)enumeration.nextElement()).toASN1Primitive();
                if (!(aSN1Primitive instanceof ASN1Sequence)) continue;
                arrayList.add(new X509CRLHolder(CertificateList.getInstance(aSN1Primitive)));
            }
            return new CollectionStore(arrayList);
        }
        return new CollectionStore(new ArrayList());
    }

    Store getOtherRevocationInfo(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Set aSN1Set) {
        if (aSN1Set != null) {
            ArrayList<ASN1Encodable> arrayList = new ArrayList<ASN1Encodable>(aSN1Set.size());
            Enumeration enumeration = aSN1Set.getObjects();
            while (enumeration.hasMoreElements()) {
                OtherRevocationInfoFormat otherRevocationInfoFormat;
                ASN1TaggedObject aSN1TaggedObject;
                ASN1Primitive aSN1Primitive = ((ASN1Encodable)enumeration.nextElement()).toASN1Primitive();
                if (!(aSN1Primitive instanceof ASN1TaggedObject) || (aSN1TaggedObject = ASN1TaggedObject.getInstance(aSN1Primitive)).getTagNo() != 1 || !aSN1ObjectIdentifier.equals((otherRevocationInfoFormat = OtherRevocationInfoFormat.getInstance(aSN1TaggedObject, false)).getInfoFormat())) continue;
                arrayList.add(otherRevocationInfoFormat.getInfo());
            }
            return new CollectionStore(arrayList);
        }
        return new CollectionStore(new ArrayList());
    }

    static {
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.dsa_with_sha224, "DSA");
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.dsa_with_sha256, "DSA");
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.dsa_with_sha384, "DSA");
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.dsa_with_sha512, "DSA");
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.id_dsa_with_sha3_224, "DSA");
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.id_dsa_with_sha3_256, "DSA");
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.id_dsa_with_sha3_384, "DSA");
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.id_dsa_with_sha3_512, "DSA");
        CMSSignedHelper.addEntries(OIWObjectIdentifiers.dsaWithSHA1, "DSA");
        CMSSignedHelper.addEntries(OIWObjectIdentifiers.md4WithRSA, "RSA");
        CMSSignedHelper.addEntries(OIWObjectIdentifiers.md4WithRSAEncryption, "RSA");
        CMSSignedHelper.addEntries(OIWObjectIdentifiers.md5WithRSA, "RSA");
        CMSSignedHelper.addEntries(OIWObjectIdentifiers.sha1WithRSA, "RSA");
        CMSSignedHelper.addEntries(PKCSObjectIdentifiers.md2WithRSAEncryption, "RSA");
        CMSSignedHelper.addEntries(PKCSObjectIdentifiers.md4WithRSAEncryption, "RSA");
        CMSSignedHelper.addEntries(PKCSObjectIdentifiers.md5WithRSAEncryption, "RSA");
        CMSSignedHelper.addEntries(PKCSObjectIdentifiers.sha1WithRSAEncryption, "RSA");
        CMSSignedHelper.addEntries(PKCSObjectIdentifiers.sha224WithRSAEncryption, "RSA");
        CMSSignedHelper.addEntries(PKCSObjectIdentifiers.sha256WithRSAEncryption, "RSA");
        CMSSignedHelper.addEntries(PKCSObjectIdentifiers.sha384WithRSAEncryption, "RSA");
        CMSSignedHelper.addEntries(PKCSObjectIdentifiers.sha512WithRSAEncryption, "RSA");
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.id_rsassa_pkcs1_v1_5_with_sha3_224, "RSA");
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.id_rsassa_pkcs1_v1_5_with_sha3_256, "RSA");
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.id_rsassa_pkcs1_v1_5_with_sha3_384, "RSA");
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.id_rsassa_pkcs1_v1_5_with_sha3_512, "RSA");
        CMSSignedHelper.addEntries(X9ObjectIdentifiers.ecdsa_with_SHA1, "ECDSA");
        CMSSignedHelper.addEntries(X9ObjectIdentifiers.ecdsa_with_SHA224, "ECDSA");
        CMSSignedHelper.addEntries(X9ObjectIdentifiers.ecdsa_with_SHA256, "ECDSA");
        CMSSignedHelper.addEntries(X9ObjectIdentifiers.ecdsa_with_SHA384, "ECDSA");
        CMSSignedHelper.addEntries(X9ObjectIdentifiers.ecdsa_with_SHA512, "ECDSA");
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.id_ecdsa_with_sha3_224, "ECDSA");
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.id_ecdsa_with_sha3_256, "ECDSA");
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.id_ecdsa_with_sha3_384, "ECDSA");
        CMSSignedHelper.addEntries(NISTObjectIdentifiers.id_ecdsa_with_sha3_512, "ECDSA");
        CMSSignedHelper.addEntries(X9ObjectIdentifiers.id_dsa_with_sha1, "DSA");
        CMSSignedHelper.addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_1, "ECDSA");
        CMSSignedHelper.addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_224, "ECDSA");
        CMSSignedHelper.addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_256, "ECDSA");
        CMSSignedHelper.addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_384, "ECDSA");
        CMSSignedHelper.addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_512, "ECDSA");
        CMSSignedHelper.addEntries(EACObjectIdentifiers.id_TA_RSA_v1_5_SHA_1, "RSA");
        CMSSignedHelper.addEntries(EACObjectIdentifiers.id_TA_RSA_v1_5_SHA_256, "RSA");
        CMSSignedHelper.addEntries(EACObjectIdentifiers.id_TA_RSA_PSS_SHA_1, "RSAandMGF1");
        CMSSignedHelper.addEntries(EACObjectIdentifiers.id_TA_RSA_PSS_SHA_256, "RSAandMGF1");
        CMSSignedHelper.addEntries(X9ObjectIdentifiers.id_dsa, "DSA");
        CMSSignedHelper.addEntries(PKCSObjectIdentifiers.rsaEncryption, "RSA");
        CMSSignedHelper.addEntries(TeleTrusTObjectIdentifiers.teleTrusTRSAsignatureAlgorithm, "RSA");
        CMSSignedHelper.addEntries(X509ObjectIdentifiers.id_ea_rsa, "RSA");
        CMSSignedHelper.addEntries(PKCSObjectIdentifiers.id_RSASSA_PSS, "RSAandMGF1");
        CMSSignedHelper.addEntries(CryptoProObjectIdentifiers.gostR3410_94, "GOST3410");
        CMSSignedHelper.addEntries(CryptoProObjectIdentifiers.gostR3410_2001, "ECGOST3410");
        CMSSignedHelper.addEntries(new ASN1ObjectIdentifier("1.3.6.1.4.1.5849.1.6.2"), "ECGOST3410");
        CMSSignedHelper.addEntries(new ASN1ObjectIdentifier("1.3.6.1.4.1.5849.1.1.5"), "GOST3410");
        CMSSignedHelper.addEntries(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256, "ECGOST3410-2012-256");
        CMSSignedHelper.addEntries(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_512, "ECGOST3410-2012-512");
        CMSSignedHelper.addEntries(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001, "ECGOST3410");
        CMSSignedHelper.addEntries(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94, "GOST3410");
        CMSSignedHelper.addEntries(RosstandartObjectIdentifiers.id_tc26_signwithdigest_gost_3410_12_256, "ECGOST3410-2012-256");
        CMSSignedHelper.addEntries(RosstandartObjectIdentifiers.id_tc26_signwithdigest_gost_3410_12_512, "ECGOST3410-2012-512");
    }
}

