/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jce.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateFactory;
import java.security.cert.Extension;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.isara.IsaraObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import org.bouncycastle.asn1.ocsp.CertID;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.ocsp.OCSPResponse;
import org.bouncycastle.asn1.ocsp.ResponderID;
import org.bouncycastle.asn1.ocsp.ResponseBytes;
import org.bouncycastle.asn1.ocsp.ResponseData;
import org.bouncycastle.asn1.ocsp.RevokedInfo;
import org.bouncycastle.asn1.ocsp.SingleResponse;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStrictStyle;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.internal.asn1.bsi.BSIObjectIdentifiers;
import org.bouncycastle.internal.asn1.eac.EACObjectIdentifiers;
import org.bouncycastle.jcajce.PKIXCertRevocationChecker;
import org.bouncycastle.jcajce.PKIXCertRevocationCheckerParameters;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jcajce.util.MessageDigestUtils;
import org.bouncycastle.jce.exception.ExtCertPathValidatorException;
import org.bouncycastle.jce.provider.OcspCache;
import org.bouncycastle.jce.provider.ProvRevocationChecker;
import org.bouncycastle.jce.provider.RecoverableCertPathValidatorException;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Properties;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class ProvOcspRevocationChecker
implements PKIXCertRevocationChecker {
    private static final int DEFAULT_OCSP_TIMEOUT = 15000;
    private static final int DEFAULT_OCSP_MAX_RESPONSE_SIZE = 32768;
    private static final Map oids = new HashMap();
    private final ProvRevocationChecker parent;
    private final JcaJceHelper helper;
    private PKIXCertRevocationCheckerParameters parameters;
    private boolean isEnabledOCSP;
    private String ocspURL;

    public ProvOcspRevocationChecker(ProvRevocationChecker provRevocationChecker, JcaJceHelper jcaJceHelper) {
        this.parent = provRevocationChecker;
        this.helper = jcaJceHelper;
    }

    @Override
    public void setParameter(String string, Object object) {
    }

    @Override
    public void initialize(PKIXCertRevocationCheckerParameters pKIXCertRevocationCheckerParameters) {
        this.parameters = pKIXCertRevocationCheckerParameters;
        this.isEnabledOCSP = Properties.isOverrideSet("ocsp.enable");
        this.ocspURL = Properties.getPropertyValue("ocsp.responderURL");
    }

    public List<CertPathValidatorException> getSoftFailExceptions() {
        return null;
    }

    public void init(boolean bl) throws CertPathValidatorException {
        if (bl) {
            throw new CertPathValidatorException("forward checking not supported");
        }
        this.parameters = null;
        this.isEnabledOCSP = Properties.isOverrideSet("ocsp.enable");
        this.ocspURL = Properties.getPropertyValue("ocsp.responderURL");
    }

    public boolean isForwardCheckingSupported() {
        return false;
    }

    public Set<String> getSupportedExtensions() {
        return null;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void check(java.security.cert.Certificate certificate) throws CertPathValidatorException {
        Object object;
        Object object2;
        Object object3;
        X509Certificate x509Certificate = (X509Certificate)certificate;
        Map<X509Certificate, byte[]> map = this.parent.getOcspResponses();
        URI uRI = this.parent.getOcspResponder();
        if (uRI == null) {
            if (this.ocspURL != null) {
                try {
                    uRI = new URI(this.ocspURL);
                } catch (URISyntaxException uRISyntaxException) {
                    throw new CertPathValidatorException("configuration error: " + uRISyntaxException.getMessage(), (Throwable)uRISyntaxException, this.parameters.getCertPath(), this.parameters.getIndex());
                }
            } else {
                uRI = ProvOcspRevocationChecker.getOcspResponderURI(x509Certificate);
            }
        }
        byte[] byArray = null;
        boolean bl = false;
        if (map.get(x509Certificate) == null && uRI != null) {
            if (this.ocspURL == null && this.parent.getOcspResponder() == null && !this.isEnabledOCSP) {
                throw new RecoverableCertPathValidatorException("OCSP disabled by \"ocsp.enable\" setting", null, this.parameters.getCertPath(), this.parameters.getIndex());
            }
            object3 = this.extractCert();
            CertID certID = this.createCertID(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1), (Certificate)object3, new ASN1Integer(x509Certificate.getSerialNumber()));
            object2 = OcspCache.getOcspResponse(certID, this.parameters, uRI, this.parent.getOcspResponderCert(), this.parent.getOcspExtensions(), this.helper);
            try {
                map.put(x509Certificate, ((ASN1Object)object2).getEncoded());
                bl = true;
            } catch (IOException iOException) {
                throw new CertPathValidatorException("unable to encode OCSP response", (Throwable)iOException, this.parameters.getCertPath(), this.parameters.getIndex());
            }
        } else {
            object3 = this.parent.getOcspExtensions();
            for (int i = 0; i != object3.size(); ++i) {
                object2 = (Extension)object3.get(i);
                object = object2.getValue();
                if (!OCSPObjectIdentifiers.id_pkix_ocsp_nonce.getId().equals(object2.getId())) continue;
                byArray = object;
            }
        }
        if (map.isEmpty()) throw new RecoverableCertPathValidatorException("no OCSP response found for any certificate", null, this.parameters.getCertPath(), this.parameters.getIndex());
        object3 = OCSPResponse.getInstance(map.get(x509Certificate));
        ASN1Integer aSN1Integer = new ASN1Integer(x509Certificate.getSerialNumber());
        if (object3 == null) throw new RecoverableCertPathValidatorException("no OCSP response found for certificate", null, this.parameters.getCertPath(), this.parameters.getIndex());
        if (0 != ((OCSPResponse)object3).getResponseStatus().getIntValue()) throw new CertPathValidatorException("OCSP response failed: " + ((OCSPResponse)object3).getResponseStatus().getValue(), null, this.parameters.getCertPath(), this.parameters.getIndex());
        object2 = ResponseBytes.getInstance(((OCSPResponse)object3).getResponseBytes());
        if (!((ResponseBytes)object2).getResponseType().equals(OCSPObjectIdentifiers.id_pkix_ocsp_basic)) return;
        try {
            object = BasicOCSPResponse.getInstance(((ResponseBytes)object2).getResponse().getOctets());
            if (!bl && !ProvOcspRevocationChecker.validatedOcspResponse((BasicOCSPResponse)object, this.parameters, byArray, this.parent.getOcspResponderCert(), this.helper)) return;
            ResponseData responseData = ResponseData.getInstance(((BasicOCSPResponse)object).getTbsResponseData());
            ASN1Sequence aSN1Sequence = responseData.getResponses();
            CertID certID = null;
            for (int i = 0; i != aSN1Sequence.size(); ++i) {
                ASN1Object aSN1Object;
                SingleResponse singleResponse = SingleResponse.getInstance(aSN1Sequence.getObjectAt(i));
                if (!aSN1Integer.equals(singleResponse.getCertID().getSerialNumber())) continue;
                ASN1GeneralizedTime aSN1GeneralizedTime = singleResponse.getNextUpdate();
                if (aSN1GeneralizedTime != null && this.parameters.getValidDate().after(aSN1GeneralizedTime.getDate())) {
                    throw new ExtCertPathValidatorException("OCSP response expired");
                }
                if (certID == null || !certID.getHashAlgorithm().equals(singleResponse.getCertID().getHashAlgorithm())) {
                    aSN1Object = this.extractCert();
                    certID = this.createCertID(singleResponse.getCertID(), (Certificate)aSN1Object, aSN1Integer);
                }
                if (!certID.equals(singleResponse.getCertID())) continue;
                if (singleResponse.getCertStatus().getTagNo() == 0) {
                    return;
                }
                if (singleResponse.getCertStatus().getTagNo() != 1) throw new CertPathValidatorException("certificate revoked, details unknown", null, this.parameters.getCertPath(), this.parameters.getIndex());
                aSN1Object = RevokedInfo.getInstance(singleResponse.getCertStatus().getStatus());
                CRLReason cRLReason = ((RevokedInfo)aSN1Object).getRevocationReason();
                throw new CertPathValidatorException("certificate revoked, reason=(" + cRLReason + "), date=" + ((RevokedInfo)aSN1Object).getRevocationTime().getDate(), null, this.parameters.getCertPath(), this.parameters.getIndex());
            }
            return;
        } catch (CertPathValidatorException certPathValidatorException) {
            throw certPathValidatorException;
        } catch (Exception exception) {
            throw new CertPathValidatorException("unable to process OCSP response", (Throwable)exception, this.parameters.getCertPath(), this.parameters.getIndex());
        }
    }

    static URI getOcspResponderURI(X509Certificate x509Certificate) {
        byte[] byArray = x509Certificate.getExtensionValue(org.bouncycastle.asn1.x509.Extension.authorityInfoAccess.getId());
        if (byArray == null) {
            return null;
        }
        AuthorityInformationAccess authorityInformationAccess = AuthorityInformationAccess.getInstance(ASN1OctetString.getInstance(byArray).getOctets());
        AccessDescription[] accessDescriptionArray = authorityInformationAccess.getAccessDescriptions();
        for (int i = 0; i != accessDescriptionArray.length; ++i) {
            GeneralName generalName;
            AccessDescription accessDescription = accessDescriptionArray[i];
            if (!AccessDescription.id_ad_ocsp.equals(accessDescription.getAccessMethod()) || (generalName = accessDescription.getAccessLocation()).getTagNo() != 6) continue;
            try {
                return new URI(((ASN1String)((Object)generalName.getName())).getString());
            } catch (URISyntaxException uRISyntaxException) {
                // empty catch block
            }
        }
        return null;
    }

    static boolean validatedOcspResponse(BasicOCSPResponse basicOCSPResponse, PKIXCertRevocationCheckerParameters pKIXCertRevocationCheckerParameters, byte[] byArray, X509Certificate x509Certificate, JcaJceHelper jcaJceHelper) throws CertPathValidatorException {
        try {
            Object object;
            Object object2;
            ASN1Sequence aSN1Sequence = basicOCSPResponse.getCerts();
            Signature signature = jcaJceHelper.createSignature(ProvOcspRevocationChecker.getSignatureName(basicOCSPResponse.getSignatureAlgorithm()));
            X509Certificate x509Certificate2 = ProvOcspRevocationChecker.getSignerCert(basicOCSPResponse, pKIXCertRevocationCheckerParameters.getSigningCert(), x509Certificate, jcaJceHelper);
            if (x509Certificate2 == null && aSN1Sequence == null) {
                throw new CertPathValidatorException("OCSP responder certificate not found");
            }
            if (x509Certificate2 != null) {
                signature.initVerify(x509Certificate2.getPublicKey());
            } else {
                object2 = jcaJceHelper.createCertificateFactory("X.509");
                object = (X509Certificate)((CertificateFactory)object2).generateCertificate(new ByteArrayInputStream(aSN1Sequence.getObjectAt(0).toASN1Primitive().getEncoded()));
                ((java.security.cert.Certificate)object).verify(pKIXCertRevocationCheckerParameters.getSigningCert().getPublicKey());
                ((X509Certificate)object).checkValidity(pKIXCertRevocationCheckerParameters.getValidDate());
                if (!ProvOcspRevocationChecker.responderMatches(basicOCSPResponse.getTbsResponseData().getResponderID(), (X509Certificate)object, jcaJceHelper)) {
                    throw new CertPathValidatorException("responder certificate does not match responderID", null, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
                }
                List<String> list = ((X509Certificate)object).getExtendedKeyUsage();
                if (list == null || !list.contains(KeyPurposeId.id_kp_OCSPSigning.getId())) {
                    throw new CertPathValidatorException("responder certificate not valid for signing OCSP responses", null, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
                }
                signature.initVerify((java.security.cert.Certificate)object);
            }
            signature.update(basicOCSPResponse.getTbsResponseData().getEncoded("DER"));
            if (signature.verify(basicOCSPResponse.getSignature().getBytes())) {
                if (byArray != null && !Arrays.areEqual(byArray, ((org.bouncycastle.asn1.x509.Extension)(object = ((Extensions)(object2 = basicOCSPResponse.getTbsResponseData().getResponseExtensions())).getExtension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce))).getExtnValue().getOctets())) {
                    throw new CertPathValidatorException("nonce mismatch in OCSP response", null, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
                }
                return true;
            }
            return false;
        } catch (CertPathValidatorException certPathValidatorException) {
            throw certPathValidatorException;
        } catch (GeneralSecurityException generalSecurityException) {
            throw new CertPathValidatorException("OCSP response failure: " + generalSecurityException.getMessage(), (Throwable)generalSecurityException, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
        } catch (IOException iOException) {
            throw new CertPathValidatorException("OCSP response failure: " + iOException.getMessage(), (Throwable)iOException, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
        }
    }

    private static X509Certificate getSignerCert(BasicOCSPResponse basicOCSPResponse, X509Certificate x509Certificate, X509Certificate x509Certificate2, JcaJceHelper jcaJceHelper) throws NoSuchProviderException, NoSuchAlgorithmException {
        ResponderID responderID = basicOCSPResponse.getTbsResponseData().getResponderID();
        byte[] byArray = responderID.getKeyHash();
        if (byArray != null) {
            MessageDigest messageDigest = jcaJceHelper.createMessageDigest("SHA1");
            X509Certificate x509Certificate3 = x509Certificate2;
            if (x509Certificate3 != null && Arrays.areEqual(byArray, ProvOcspRevocationChecker.calcKeyHash(messageDigest, x509Certificate3.getPublicKey()))) {
                return x509Certificate3;
            }
            x509Certificate3 = x509Certificate;
            if (x509Certificate3 != null && Arrays.areEqual(byArray, ProvOcspRevocationChecker.calcKeyHash(messageDigest, x509Certificate3.getPublicKey()))) {
                return x509Certificate3;
            }
        } else {
            X500Name x500Name = X500Name.getInstance(BCStrictStyle.INSTANCE, responderID.getName());
            X509Certificate x509Certificate4 = x509Certificate2;
            if (x509Certificate4 != null && x500Name.equals(X500Name.getInstance(BCStrictStyle.INSTANCE, x509Certificate4.getSubjectX500Principal().getEncoded()))) {
                return x509Certificate4;
            }
            x509Certificate4 = x509Certificate;
            if (x509Certificate4 != null && x500Name.equals(X500Name.getInstance(BCStrictStyle.INSTANCE, x509Certificate4.getSubjectX500Principal().getEncoded()))) {
                return x509Certificate4;
            }
        }
        return null;
    }

    private static boolean responderMatches(ResponderID responderID, X509Certificate x509Certificate, JcaJceHelper jcaJceHelper) throws NoSuchProviderException, NoSuchAlgorithmException {
        byte[] byArray = responderID.getKeyHash();
        if (byArray != null) {
            MessageDigest messageDigest = jcaJceHelper.createMessageDigest("SHA1");
            return Arrays.areEqual(byArray, ProvOcspRevocationChecker.calcKeyHash(messageDigest, x509Certificate.getPublicKey()));
        }
        X500Name x500Name = X500Name.getInstance(BCStrictStyle.INSTANCE, responderID.getName());
        return x500Name.equals(X500Name.getInstance(BCStrictStyle.INSTANCE, x509Certificate.getSubjectX500Principal().getEncoded()));
    }

    private static byte[] calcKeyHash(MessageDigest messageDigest, PublicKey publicKey) {
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
        return messageDigest.digest(subjectPublicKeyInfo.getPublicKeyData().getBytes());
    }

    private Certificate extractCert() throws CertPathValidatorException {
        try {
            return Certificate.getInstance(this.parameters.getSigningCert().getEncoded());
        } catch (Exception exception) {
            throw new CertPathValidatorException("cannot process signing cert: " + exception.getMessage(), (Throwable)exception, this.parameters.getCertPath(), this.parameters.getIndex());
        }
    }

    private CertID createCertID(CertID certID, Certificate certificate, ASN1Integer aSN1Integer) throws CertPathValidatorException {
        return this.createCertID(certID.getHashAlgorithm(), certificate, aSN1Integer);
    }

    private CertID createCertID(AlgorithmIdentifier algorithmIdentifier, Certificate certificate, ASN1Integer aSN1Integer) throws CertPathValidatorException {
        try {
            MessageDigest messageDigest = this.helper.createMessageDigest(MessageDigestUtils.getDigestName(algorithmIdentifier.getAlgorithm()));
            DEROctetString dEROctetString = new DEROctetString(messageDigest.digest(certificate.getSubject().getEncoded("DER")));
            DEROctetString dEROctetString2 = new DEROctetString(messageDigest.digest(certificate.getSubjectPublicKeyInfo().getPublicKeyData().getBytes()));
            return new CertID(algorithmIdentifier, dEROctetString, dEROctetString2, aSN1Integer);
        } catch (Exception exception) {
            throw new CertPathValidatorException("problem creating ID: " + exception, exception);
        }
    }

    private static String getDigestName(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        String string = MessageDigestUtils.getDigestName(aSN1ObjectIdentifier);
        int n = string.indexOf(45);
        if (n > 0 && !string.startsWith("SHA3")) {
            return string.substring(0, n) + string.substring(n + 1);
        }
        return string;
    }

    private static String getSignatureName(AlgorithmIdentifier algorithmIdentifier) {
        ASN1Encodable aSN1Encodable = algorithmIdentifier.getParameters();
        if (aSN1Encodable != null && !DERNull.INSTANCE.equals(aSN1Encodable) && algorithmIdentifier.getAlgorithm().equals(PKCSObjectIdentifiers.id_RSASSA_PSS)) {
            RSASSAPSSparams rSASSAPSSparams = RSASSAPSSparams.getInstance(aSN1Encodable);
            return ProvOcspRevocationChecker.getDigestName(rSASSAPSSparams.getHashAlgorithm().getAlgorithm()) + "WITHRSAANDMGF1";
        }
        if (oids.containsKey(algorithmIdentifier.getAlgorithm())) {
            return (String)oids.get(algorithmIdentifier.getAlgorithm());
        }
        return algorithmIdentifier.getAlgorithm().getId();
    }

    static {
        oids.put(new ASN1ObjectIdentifier("1.2.840.113549.1.1.5"), "SHA1WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha224WithRSAEncryption, "SHA224WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha256WithRSAEncryption, "SHA256WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha384WithRSAEncryption, "SHA384WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha512WithRSAEncryption, "SHA512WITHRSA");
        oids.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94, "GOST3411WITHGOST3410");
        oids.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001, "GOST3411WITHECGOST3410");
        oids.put(RosstandartObjectIdentifiers.id_tc26_signwithdigest_gost_3410_12_256, "GOST3411-2012-256WITHECGOST3410-2012-256");
        oids.put(RosstandartObjectIdentifiers.id_tc26_signwithdigest_gost_3410_12_512, "GOST3411-2012-512WITHECGOST3410-2012-512");
        oids.put(BSIObjectIdentifiers.ecdsa_plain_SHA1, "SHA1WITHPLAIN-ECDSA");
        oids.put(BSIObjectIdentifiers.ecdsa_plain_SHA224, "SHA224WITHPLAIN-ECDSA");
        oids.put(BSIObjectIdentifiers.ecdsa_plain_SHA256, "SHA256WITHPLAIN-ECDSA");
        oids.put(BSIObjectIdentifiers.ecdsa_plain_SHA384, "SHA384WITHPLAIN-ECDSA");
        oids.put(BSIObjectIdentifiers.ecdsa_plain_SHA512, "SHA512WITHPLAIN-ECDSA");
        oids.put(BSIObjectIdentifiers.ecdsa_plain_RIPEMD160, "RIPEMD160WITHPLAIN-ECDSA");
        oids.put(EACObjectIdentifiers.id_TA_ECDSA_SHA_1, "SHA1WITHCVC-ECDSA");
        oids.put(EACObjectIdentifiers.id_TA_ECDSA_SHA_224, "SHA224WITHCVC-ECDSA");
        oids.put(EACObjectIdentifiers.id_TA_ECDSA_SHA_256, "SHA256WITHCVC-ECDSA");
        oids.put(EACObjectIdentifiers.id_TA_ECDSA_SHA_384, "SHA384WITHCVC-ECDSA");
        oids.put(EACObjectIdentifiers.id_TA_ECDSA_SHA_512, "SHA512WITHCVC-ECDSA");
        oids.put(IsaraObjectIdentifiers.id_alg_xmss, "XMSS");
        oids.put(IsaraObjectIdentifiers.id_alg_xmssmt, "XMSSMT");
        oids.put(new ASN1ObjectIdentifier("1.2.840.113549.1.1.4"), "MD5WITHRSA");
        oids.put(new ASN1ObjectIdentifier("1.2.840.113549.1.1.2"), "MD2WITHRSA");
        oids.put(new ASN1ObjectIdentifier("1.2.840.10040.4.3"), "SHA1WITHDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA1, "SHA1WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA224, "SHA224WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA256, "SHA256WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA384, "SHA384WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA512, "SHA512WITHECDSA");
        oids.put(OIWObjectIdentifiers.sha1WithRSA, "SHA1WITHRSA");
        oids.put(OIWObjectIdentifiers.dsaWithSHA1, "SHA1WITHDSA");
        oids.put(NISTObjectIdentifiers.dsa_with_sha224, "SHA224WITHDSA");
        oids.put(NISTObjectIdentifiers.dsa_with_sha256, "SHA256WITHDSA");
    }
}

