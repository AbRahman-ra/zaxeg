/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jce.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.PolicyQualifierInfo;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.cert.X509Extension;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.internal.asn1.isismtt.ISISMTTObjectIdentifiers;
import org.bouncycastle.jcajce.PKIXCRLStore;
import org.bouncycastle.jcajce.PKIXCRLStoreSelector;
import org.bouncycastle.jcajce.PKIXCertRevocationCheckerParameters;
import org.bouncycastle.jcajce.PKIXCertStore;
import org.bouncycastle.jcajce.PKIXCertStoreSelector;
import org.bouncycastle.jcajce.PKIXExtendedBuilderParameters;
import org.bouncycastle.jcajce.PKIXExtendedParameters;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jce.exception.ExtCertPathBuilderException;
import org.bouncycastle.jce.exception.ExtCertPathValidatorException;
import org.bouncycastle.jce.provider.AnnotatedException;
import org.bouncycastle.jce.provider.CertStatus;
import org.bouncycastle.jce.provider.CrlCache;
import org.bouncycastle.jce.provider.PKIXCRLUtil;
import org.bouncycastle.jce.provider.PKIXPolicyNode;
import org.bouncycastle.jce.provider.PrincipalUtils;
import org.bouncycastle.jce.provider.RFC3280CertPathUtilities;
import org.bouncycastle.jce.provider.RecoverableCertPathValidatorException;
import org.bouncycastle.jce.provider.X509CRLObject;
import org.bouncycastle.util.Properties;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.StoreException;
import org.bouncycastle.x509.X509AttributeCertificate;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class CertPathValidatorUtilities {
    protected static final String CERTIFICATE_POLICIES = Extension.certificatePolicies.getId();
    protected static final String BASIC_CONSTRAINTS = Extension.basicConstraints.getId();
    protected static final String POLICY_MAPPINGS = Extension.policyMappings.getId();
    protected static final String SUBJECT_ALTERNATIVE_NAME = Extension.subjectAlternativeName.getId();
    protected static final String NAME_CONSTRAINTS = Extension.nameConstraints.getId();
    protected static final String KEY_USAGE = Extension.keyUsage.getId();
    protected static final String INHIBIT_ANY_POLICY = Extension.inhibitAnyPolicy.getId();
    protected static final String ISSUING_DISTRIBUTION_POINT = Extension.issuingDistributionPoint.getId();
    protected static final String DELTA_CRL_INDICATOR = Extension.deltaCRLIndicator.getId();
    protected static final String POLICY_CONSTRAINTS = Extension.policyConstraints.getId();
    protected static final String FRESHEST_CRL = Extension.freshestCRL.getId();
    protected static final String CRL_DISTRIBUTION_POINTS = Extension.cRLDistributionPoints.getId();
    protected static final String AUTHORITY_KEY_IDENTIFIER = Extension.authorityKeyIdentifier.getId();
    protected static final String ANY_POLICY = "2.5.29.32.0";
    protected static final String CRL_NUMBER = Extension.cRLNumber.getId();
    protected static final int KEY_CERT_SIGN = 5;
    protected static final int CRL_SIGN = 6;
    protected static final String[] crlReasons = new String[]{"unspecified", "keyCompromise", "cACompromise", "affiliationChanged", "superseded", "cessationOfOperation", "certificateHold", "unknown", "removeFromCRL", "privilegeWithdrawn", "aACompromise"};

    CertPathValidatorUtilities() {
    }

    static Collection findTargets(PKIXExtendedBuilderParameters pKIXExtendedBuilderParameters) throws CertPathBuilderException {
        PKIXExtendedParameters pKIXExtendedParameters = pKIXExtendedBuilderParameters.getBaseParameters();
        PKIXCertStoreSelector pKIXCertStoreSelector = pKIXExtendedParameters.getTargetConstraints();
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        try {
            CertPathValidatorUtilities.findCertificates(linkedHashSet, pKIXCertStoreSelector, pKIXExtendedParameters.getCertificateStores());
            CertPathValidatorUtilities.findCertificates(linkedHashSet, pKIXCertStoreSelector, pKIXExtendedParameters.getCertStores());
        } catch (AnnotatedException annotatedException) {
            throw new ExtCertPathBuilderException("Error finding target certificate.", annotatedException);
        }
        if (!linkedHashSet.isEmpty()) {
            return linkedHashSet;
        }
        Certificate certificate = pKIXCertStoreSelector.getCertificate();
        if (null == certificate) {
            throw new CertPathBuilderException("No certificate found matching targetConstraints.");
        }
        return Collections.singleton(certificate);
    }

    protected static TrustAnchor findTrustAnchor(X509Certificate x509Certificate, Set set) throws AnnotatedException {
        return CertPathValidatorUtilities.findTrustAnchor(x509Certificate, set, null);
    }

    protected static TrustAnchor findTrustAnchor(X509Certificate x509Certificate, Set set, String string) throws AnnotatedException {
        TrustAnchor trustAnchor = null;
        PublicKey publicKey = null;
        Exception exception = null;
        X509CertSelector x509CertSelector = new X509CertSelector();
        X500Principal x500Principal = x509Certificate.getIssuerX500Principal();
        x509CertSelector.setSubject(x500Principal);
        X500Name x500Name = null;
        Iterator iterator = set.iterator();
        while (iterator.hasNext() && trustAnchor == null) {
            block14: {
                trustAnchor = (TrustAnchor)iterator.next();
                if (trustAnchor.getTrustedCert() != null) {
                    if (x509CertSelector.match(trustAnchor.getTrustedCert())) {
                        publicKey = trustAnchor.getTrustedCert().getPublicKey();
                    } else {
                        trustAnchor = null;
                    }
                } else if (trustAnchor.getCA() != null && trustAnchor.getCAName() != null && trustAnchor.getCAPublicKey() != null) {
                    if (x500Name == null) {
                        x500Name = X500Name.getInstance(x500Principal.getEncoded());
                    }
                    try {
                        X500Name x500Name2 = X500Name.getInstance(trustAnchor.getCA().getEncoded());
                        if (x500Name.equals(x500Name2)) {
                            publicKey = trustAnchor.getCAPublicKey();
                            break block14;
                        }
                        trustAnchor = null;
                    } catch (IllegalArgumentException illegalArgumentException) {
                        trustAnchor = null;
                    }
                } else {
                    trustAnchor = null;
                }
            }
            if (publicKey == null) continue;
            try {
                CertPathValidatorUtilities.verifyX509Certificate(x509Certificate, publicKey, string);
            } catch (Exception exception2) {
                exception = exception2;
                trustAnchor = null;
                publicKey = null;
            }
        }
        if (trustAnchor == null && exception != null) {
            throw new AnnotatedException("TrustAnchor found but certificate validation failed.", exception);
        }
        return trustAnchor;
    }

    static boolean isIssuerTrustAnchor(X509Certificate x509Certificate, Set set, String string) throws AnnotatedException {
        try {
            return CertPathValidatorUtilities.findTrustAnchor(x509Certificate, set, string) != null;
        } catch (Exception exception) {
            return false;
        }
    }

    static List<PKIXCertStore> getAdditionalStoresFromAltNames(byte[] byArray, Map<GeneralName, PKIXCertStore> map) throws CertificateParsingException {
        if (byArray == null) {
            return Collections.EMPTY_LIST;
        }
        GeneralNames generalNames = GeneralNames.getInstance(ASN1OctetString.getInstance(byArray).getOctets());
        GeneralName[] generalNameArray = generalNames.getNames();
        ArrayList<PKIXCertStore> arrayList = new ArrayList<PKIXCertStore>();
        for (int i = 0; i != generalNameArray.length; ++i) {
            GeneralName generalName = generalNameArray[i];
            PKIXCertStore pKIXCertStore = map.get(generalName);
            if (pKIXCertStore == null) continue;
            arrayList.add(pKIXCertStore);
        }
        return arrayList;
    }

    protected static Date getValidityDate(PKIXExtendedParameters pKIXExtendedParameters, Date date) {
        Date date2 = pKIXExtendedParameters.getValidityDate();
        return null == date2 ? date : date2;
    }

    protected static boolean isSelfIssued(X509Certificate x509Certificate) {
        return x509Certificate.getSubjectDN().equals(x509Certificate.getIssuerDN());
    }

    protected static ASN1Primitive getExtensionValue(X509Extension x509Extension, String string) throws AnnotatedException {
        byte[] byArray = x509Extension.getExtensionValue(string);
        return null == byArray ? null : CertPathValidatorUtilities.getObject(string, byArray);
    }

    private static ASN1Primitive getObject(String string, byte[] byArray) throws AnnotatedException {
        try {
            ASN1OctetString aSN1OctetString = ASN1OctetString.getInstance(byArray);
            return ASN1Primitive.fromByteArray(aSN1OctetString.getOctets());
        } catch (Exception exception) {
            throw new AnnotatedException("exception processing extension " + string, exception);
        }
    }

    protected static AlgorithmIdentifier getAlgorithmIdentifier(PublicKey publicKey) throws CertPathValidatorException {
        try {
            return SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()).getAlgorithm();
        } catch (Exception exception) {
            throw new ExtCertPathValidatorException("Subject public key cannot be decoded.", exception);
        }
    }

    protected static final Set getQualifierSet(ASN1Sequence aSN1Sequence) throws CertPathValidatorException {
        HashSet<PolicyQualifierInfo> hashSet = new HashSet<PolicyQualifierInfo>();
        if (aSN1Sequence == null) {
            return hashSet;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ASN1OutputStream aSN1OutputStream = ASN1OutputStream.create(byteArrayOutputStream);
        Enumeration enumeration = aSN1Sequence.getObjects();
        while (enumeration.hasMoreElements()) {
            try {
                aSN1OutputStream.writeObject((ASN1Encodable)enumeration.nextElement());
                hashSet.add(new PolicyQualifierInfo(byteArrayOutputStream.toByteArray()));
            } catch (IOException iOException) {
                throw new ExtCertPathValidatorException("Policy qualifier info cannot be decoded.", iOException);
            }
            byteArrayOutputStream.reset();
        }
        return hashSet;
    }

    protected static PKIXPolicyNode removePolicyNode(PKIXPolicyNode pKIXPolicyNode, List[] listArray, PKIXPolicyNode pKIXPolicyNode2) {
        PKIXPolicyNode pKIXPolicyNode3 = (PKIXPolicyNode)pKIXPolicyNode2.getParent();
        if (pKIXPolicyNode == null) {
            return null;
        }
        if (pKIXPolicyNode3 == null) {
            for (int i = 0; i < listArray.length; ++i) {
                listArray[i] = new ArrayList();
            }
            return null;
        }
        pKIXPolicyNode3.removeChild(pKIXPolicyNode2);
        CertPathValidatorUtilities.removePolicyNodeRecurse(listArray, pKIXPolicyNode2);
        return pKIXPolicyNode;
    }

    private static void removePolicyNodeRecurse(List[] listArray, PKIXPolicyNode pKIXPolicyNode) {
        listArray[pKIXPolicyNode.getDepth()].remove(pKIXPolicyNode);
        if (pKIXPolicyNode.hasChildren()) {
            Iterator iterator = pKIXPolicyNode.getChildren();
            while (iterator.hasNext()) {
                PKIXPolicyNode pKIXPolicyNode2 = (PKIXPolicyNode)iterator.next();
                CertPathValidatorUtilities.removePolicyNodeRecurse(listArray, pKIXPolicyNode2);
            }
        }
    }

    protected static boolean processCertD1i(int n, List[] listArray, ASN1ObjectIdentifier aSN1ObjectIdentifier, Set set) {
        List list = listArray[n - 1];
        for (int i = 0; i < list.size(); ++i) {
            PKIXPolicyNode pKIXPolicyNode = (PKIXPolicyNode)list.get(i);
            Set set2 = pKIXPolicyNode.getExpectedPolicies();
            if (!set2.contains(aSN1ObjectIdentifier.getId())) continue;
            HashSet<String> hashSet = new HashSet<String>();
            hashSet.add(aSN1ObjectIdentifier.getId());
            PKIXPolicyNode pKIXPolicyNode2 = new PKIXPolicyNode(new ArrayList(), n, hashSet, pKIXPolicyNode, set, aSN1ObjectIdentifier.getId(), false);
            pKIXPolicyNode.addChild(pKIXPolicyNode2);
            listArray[n].add(pKIXPolicyNode2);
            return true;
        }
        return false;
    }

    protected static void processCertD1ii(int n, List[] listArray, ASN1ObjectIdentifier aSN1ObjectIdentifier, Set set) {
        List list = listArray[n - 1];
        for (int i = 0; i < list.size(); ++i) {
            PKIXPolicyNode pKIXPolicyNode = (PKIXPolicyNode)list.get(i);
            if (!ANY_POLICY.equals(pKIXPolicyNode.getValidPolicy())) continue;
            HashSet<String> hashSet = new HashSet<String>();
            hashSet.add(aSN1ObjectIdentifier.getId());
            PKIXPolicyNode pKIXPolicyNode2 = new PKIXPolicyNode(new ArrayList(), n, hashSet, pKIXPolicyNode, set, aSN1ObjectIdentifier.getId(), false);
            pKIXPolicyNode.addChild(pKIXPolicyNode2);
            listArray[n].add(pKIXPolicyNode2);
            return;
        }
    }

    protected static void prepareNextCertB1(int n, List[] listArray, String string, Map map, X509Certificate x509Certificate) throws AnnotatedException, CertPathValidatorException {
        boolean bl = false;
        for (PKIXPolicyNode pKIXPolicyNode : listArray[n]) {
            if (!pKIXPolicyNode.getValidPolicy().equals(string)) continue;
            bl = true;
            pKIXPolicyNode.expectedPolicies = (Set)map.get(string);
            break;
        }
        if (!bl) {
            for (PKIXPolicyNode pKIXPolicyNode : listArray[n]) {
                PKIXPolicyNode pKIXPolicyNode2;
                if (!ANY_POLICY.equals(pKIXPolicyNode.getValidPolicy())) continue;
                Set set = null;
                ASN1Sequence aSN1Sequence = null;
                try {
                    aSN1Sequence = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue(x509Certificate, CERTIFICATE_POLICIES));
                } catch (Exception exception) {
                    throw new AnnotatedException("Certificate policies cannot be decoded.", exception);
                }
                Enumeration enumeration = aSN1Sequence.getObjects();
                while (enumeration.hasMoreElements()) {
                    PolicyInformation policyInformation = null;
                    try {
                        policyInformation = PolicyInformation.getInstance(enumeration.nextElement());
                    } catch (Exception exception) {
                        throw new AnnotatedException("Policy information cannot be decoded.", exception);
                    }
                    if (!ANY_POLICY.equals(policyInformation.getPolicyIdentifier().getId())) continue;
                    try {
                        set = CertPathValidatorUtilities.getQualifierSet(policyInformation.getPolicyQualifiers());
                        break;
                    } catch (CertPathValidatorException certPathValidatorException) {
                        throw new ExtCertPathValidatorException("Policy qualifier info set could not be built.", certPathValidatorException);
                    }
                }
                boolean bl2 = false;
                if (x509Certificate.getCriticalExtensionOIDs() != null) {
                    bl2 = x509Certificate.getCriticalExtensionOIDs().contains(CERTIFICATE_POLICIES);
                }
                if (!ANY_POLICY.equals((pKIXPolicyNode2 = (PKIXPolicyNode)pKIXPolicyNode.getParent()).getValidPolicy())) break;
                PKIXPolicyNode pKIXPolicyNode3 = new PKIXPolicyNode(new ArrayList(), n, (Set)map.get(string), pKIXPolicyNode2, set, string, bl2);
                pKIXPolicyNode2.addChild(pKIXPolicyNode3);
                listArray[n].add(pKIXPolicyNode3);
                break;
            }
        }
    }

    protected static PKIXPolicyNode prepareNextCertB2(int n, List[] listArray, String string, PKIXPolicyNode pKIXPolicyNode) {
        Iterator iterator = listArray[n].iterator();
        while (iterator.hasNext()) {
            PKIXPolicyNode pKIXPolicyNode2 = (PKIXPolicyNode)iterator.next();
            if (!pKIXPolicyNode2.getValidPolicy().equals(string)) continue;
            PKIXPolicyNode pKIXPolicyNode3 = (PKIXPolicyNode)pKIXPolicyNode2.getParent();
            pKIXPolicyNode3.removeChild(pKIXPolicyNode2);
            iterator.remove();
            for (int i = n - 1; i >= 0; --i) {
                PKIXPolicyNode pKIXPolicyNode4;
                List list = listArray[i];
                for (int j = 0; j < list.size() && ((pKIXPolicyNode4 = (PKIXPolicyNode)list.get(j)).hasChildren() || (pKIXPolicyNode = CertPathValidatorUtilities.removePolicyNode(pKIXPolicyNode, listArray, pKIXPolicyNode4)) != null); ++j) {
                }
            }
        }
        return pKIXPolicyNode;
    }

    protected static boolean isAnyPolicy(Set set) {
        return set == null || set.contains(ANY_POLICY) || set.isEmpty();
    }

    protected static void findCertificates(LinkedHashSet linkedHashSet, PKIXCertStoreSelector pKIXCertStoreSelector, List list) throws AnnotatedException {
        for (Object e : list) {
            Object object;
            if (e instanceof Store) {
                object = (Store)e;
                try {
                    linkedHashSet.addAll(object.getMatches(pKIXCertStoreSelector));
                    continue;
                } catch (StoreException storeException) {
                    throw new AnnotatedException("Problem while picking certificates from X.509 store.", storeException);
                }
            }
            object = (CertStore)e;
            try {
                linkedHashSet.addAll(PKIXCertStoreSelector.getCertificates(pKIXCertStoreSelector, (CertStore)object));
            } catch (CertStoreException certStoreException) {
                throw new AnnotatedException("Problem while picking certificates from certificate store.", certStoreException);
            }
        }
    }

    static List<PKIXCRLStore> getAdditionalStoresFromCRLDistributionPoint(CRLDistPoint cRLDistPoint, Map<GeneralName, PKIXCRLStore> map, Date date, JcaJceHelper jcaJceHelper) throws AnnotatedException {
        Object object;
        DistributionPoint[] distributionPointArray;
        if (null == cRLDistPoint) {
            return Collections.EMPTY_LIST;
        }
        try {
            distributionPointArray = cRLDistPoint.getDistributionPoints();
        } catch (Exception exception) {
            throw new AnnotatedException("Distribution points could not be read.", exception);
        }
        ArrayList<PKIXCRLStore> arrayList = new ArrayList<PKIXCRLStore>();
        for (int i = 0; i < distributionPointArray.length; ++i) {
            DistributionPointName distributionPointName = distributionPointArray[i].getDistributionPoint();
            if (distributionPointName == null || distributionPointName.getType() != 0) continue;
            object = GeneralNames.getInstance(distributionPointName.getName()).getNames();
            for (int j = 0; j < ((GeneralName[])object).length; ++j) {
                PKIXCRLStore pKIXCRLStore = map.get(object[j]);
                if (pKIXCRLStore == null) continue;
                arrayList.add(pKIXCRLStore);
            }
        }
        if (arrayList.isEmpty() && Properties.isOverrideSet("org.bouncycastle.x509.enableCRLDP")) {
            CertificateFactory certificateFactory;
            try {
                certificateFactory = jcaJceHelper.createCertificateFactory("X.509");
            } catch (Exception exception) {
                throw new AnnotatedException("cannot create certificate factory: " + exception.getMessage(), exception);
            }
            block8: for (int i = 0; i < distributionPointArray.length; ++i) {
                object = distributionPointArray[i].getDistributionPoint();
                if (object == null || ((DistributionPointName)object).getType() != 0) continue;
                GeneralName[] generalNameArray = GeneralNames.getInstance(((DistributionPointName)object).getName()).getNames();
                for (int j = 0; j < generalNameArray.length; ++j) {
                    GeneralName generalName = generalNameArray[i];
                    if (generalName.getTagNo() != 6) continue;
                    try {
                        URI uRI = new URI(((ASN1String)((Object)generalName.getName())).getString());
                        PKIXCRLStore pKIXCRLStore = CrlCache.getCrl(certificateFactory, date, uRI);
                        if (pKIXCRLStore == null) continue block8;
                        arrayList.add(pKIXCRLStore);
                        continue block8;
                    } catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }
        return arrayList;
    }

    protected static void getCRLIssuersFromDistributionPoint(DistributionPoint distributionPoint, Collection collection, X509CRLSelector x509CRLSelector) throws AnnotatedException {
        Object object;
        ArrayList<X500Name> arrayList = new ArrayList<X500Name>();
        if (distributionPoint.getCRLIssuer() != null) {
            object = distributionPoint.getCRLIssuer().getNames();
            for (int i = 0; i < ((Object)object).length; ++i) {
                if (((GeneralName)object[i]).getTagNo() != 4) continue;
                try {
                    arrayList.add(X500Name.getInstance(((GeneralName)object[i]).getName().toASN1Primitive().getEncoded()));
                    continue;
                } catch (IOException iOException) {
                    throw new AnnotatedException("CRL issuer information from distribution point cannot be decoded.", iOException);
                }
            }
        } else {
            if (distributionPoint.getDistributionPoint() == null) {
                throw new AnnotatedException("CRL issuer is omitted from distribution point but no distributionPoint field present.");
            }
            object = collection.iterator();
            while (object.hasNext()) {
                arrayList.add((X500Name)object.next());
            }
        }
        object = arrayList.iterator();
        while (object.hasNext()) {
            try {
                x509CRLSelector.addIssuerName(((X500Name)object.next()).getEncoded());
            } catch (IOException iOException) {
                throw new AnnotatedException("Cannot decode CRL issuer information.", iOException);
            }
        }
    }

    private static BigInteger getSerialNumber(Object object) {
        return ((X509Certificate)object).getSerialNumber();
    }

    protected static void getCertStatus(Date date, X509CRL x509CRL, Object object, CertStatus certStatus) throws AnnotatedException {
        int n;
        Object object2;
        X509CRLEntry x509CRLEntry;
        boolean bl;
        try {
            bl = X509CRLObject.isIndirectCRL(x509CRL);
        } catch (CRLException cRLException) {
            throw new AnnotatedException("Failed check for indirect CRL.", cRLException);
        }
        if (bl) {
            x509CRLEntry = x509CRL.getRevokedCertificate(CertPathValidatorUtilities.getSerialNumber(object));
            if (x509CRLEntry == null) {
                return;
            }
            object2 = x509CRLEntry.getCertificateIssuer();
            X500Name x500Name = object2 == null ? PrincipalUtils.getIssuerPrincipal(x509CRL) : PrincipalUtils.getX500Name((X500Principal)object2);
            if (!PrincipalUtils.getEncodedIssuerPrincipal(object).equals(x500Name)) {
                return;
            }
        } else {
            if (!PrincipalUtils.getEncodedIssuerPrincipal(object).equals(PrincipalUtils.getIssuerPrincipal(x509CRL))) {
                return;
            }
            x509CRLEntry = x509CRL.getRevokedCertificate(CertPathValidatorUtilities.getSerialNumber(object));
            if (x509CRLEntry == null) {
                return;
            }
        }
        object2 = null;
        if (x509CRLEntry.hasExtensions()) {
            if (x509CRLEntry.hasUnsupportedCriticalExtension()) {
                throw new AnnotatedException("CRL entry has unsupported critical extensions.");
            }
            try {
                object2 = ASN1Enumerated.getInstance(CertPathValidatorUtilities.getExtensionValue(x509CRLEntry, Extension.reasonCode.getId()));
            } catch (Exception exception) {
                throw new AnnotatedException("Reason code CRL entry extension could not be decoded.", exception);
            }
        }
        int n2 = n = null == object2 ? 0 : ((ASN1Enumerated)object2).intValueExact();
        if (date.getTime() >= x509CRLEntry.getRevocationDate().getTime() || n == 0 || n == 1 || n == 2 || n == 10) {
            certStatus.setCertStatus(n);
            certStatus.setRevocationDate(x509CRLEntry.getRevocationDate());
        }
    }

    protected static Set getDeltaCRLs(Date date, X509CRL x509CRL, List<CertStore> list, List<PKIXCRLStore> list2, JcaJceHelper jcaJceHelper) throws AnnotatedException {
        Object object;
        Object object2;
        X509CRLSelector x509CRLSelector = new X509CRLSelector();
        try {
            x509CRLSelector.addIssuerName(PrincipalUtils.getIssuerPrincipal(x509CRL).getEncoded());
        } catch (IOException iOException) {
            throw new AnnotatedException("Cannot extract issuer from CRL.", iOException);
        }
        BigInteger bigInteger = null;
        try {
            object2 = CertPathValidatorUtilities.getExtensionValue(x509CRL, CRL_NUMBER);
            if (object2 != null) {
                bigInteger = ASN1Integer.getInstance(object2).getPositiveValue();
            }
        } catch (Exception exception) {
            throw new AnnotatedException("CRL number extension could not be extracted from CRL.", exception);
        }
        try {
            object2 = x509CRL.getExtensionValue(ISSUING_DISTRIBUTION_POINT);
        } catch (Exception exception) {
            throw new AnnotatedException("Issuing distribution point extension value could not be read.", exception);
        }
        x509CRLSelector.setMinCRLNumber(bigInteger == null ? null : bigInteger.add(BigInteger.valueOf(1L)));
        PKIXCRLStoreSelector.Builder builder = new PKIXCRLStoreSelector.Builder(x509CRLSelector);
        builder.setIssuingDistributionPoint((byte[])object2);
        builder.setIssuingDistributionPointEnabled(true);
        builder.setMaxBaseCRLNumber(bigInteger);
        PKIXCRLStoreSelector<? extends CRL> pKIXCRLStoreSelector = builder.build();
        Set set = PKIXCRLUtil.findCRLs(pKIXCRLStoreSelector, date, list, list2);
        if (set.isEmpty() && Properties.isOverrideSet("org.bouncycastle.x509.enableCRLDP")) {
            try {
                object = jcaJceHelper.createCertificateFactory("X.509");
            } catch (Exception exception) {
                throw new AnnotatedException("cannot create certificate factory: " + exception.getMessage(), exception);
            }
            CRLDistPoint cRLDistPoint = CRLDistPoint.getInstance(object2);
            DistributionPoint[] distributionPointArray = cRLDistPoint.getDistributionPoints();
            block10: for (int i = 0; i < distributionPointArray.length; ++i) {
                DistributionPointName distributionPointName = distributionPointArray[i].getDistributionPoint();
                if (distributionPointName == null || distributionPointName.getType() != 0) continue;
                GeneralName[] generalNameArray = GeneralNames.getInstance(distributionPointName.getName()).getNames();
                for (int j = 0; j < generalNameArray.length; ++j) {
                    GeneralName generalName = generalNameArray[i];
                    if (generalName.getTagNo() != 6) continue;
                    try {
                        PKIXCRLStore pKIXCRLStore = CrlCache.getCrl((CertificateFactory)object, date, new URI(((ASN1String)((Object)generalName.getName())).getString()));
                        if (pKIXCRLStore == null) continue block10;
                        set = PKIXCRLUtil.findCRLs(pKIXCRLStoreSelector, date, Collections.EMPTY_LIST, Collections.singletonList(pKIXCRLStore));
                        continue block10;
                    } catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }
        object = new HashSet();
        for (DistributionPoint[] distributionPointArray : set) {
            if (!CertPathValidatorUtilities.isDeltaCRL((X509CRL)distributionPointArray)) continue;
            object.add(distributionPointArray);
        }
        return object;
    }

    private static boolean isDeltaCRL(X509CRL x509CRL) {
        Set<String> set = x509CRL.getCriticalExtensionOIDs();
        if (set == null) {
            return false;
        }
        return set.contains(RFC3280CertPathUtilities.DELTA_CRL_INDICATOR);
    }

    protected static Set getCompleteCRLs(PKIXCertRevocationCheckerParameters pKIXCertRevocationCheckerParameters, DistributionPoint distributionPoint, Object object, PKIXExtendedParameters pKIXExtendedParameters, Date date) throws AnnotatedException, RecoverableCertPathValidatorException {
        Cloneable cloneable;
        X509CRLSelector x509CRLSelector = new X509CRLSelector();
        try {
            cloneable = new HashSet<X500Name>();
            cloneable.add(PrincipalUtils.getEncodedIssuerPrincipal(object));
            CertPathValidatorUtilities.getCRLIssuersFromDistributionPoint(distributionPoint, cloneable, x509CRLSelector);
        } catch (AnnotatedException annotatedException) {
            throw new AnnotatedException("Could not get issuer information from distribution point.", annotatedException);
        }
        if (object instanceof X509Certificate) {
            x509CRLSelector.setCertificateChecking((X509Certificate)object);
        }
        cloneable = new PKIXCRLStoreSelector.Builder(x509CRLSelector).setCompleteCRLEnabled(true).build();
        Set set = PKIXCRLUtil.findCRLs(cloneable, date, pKIXExtendedParameters.getCertStores(), pKIXExtendedParameters.getCRLStores());
        CertPathValidatorUtilities.checkCRLsNotEmpty(pKIXCertRevocationCheckerParameters, set, object);
        return set;
    }

    protected static Date getValidCertDateFromValidityModel(Date date, int n, CertPath certPath, int n2) throws AnnotatedException {
        if (1 != n || n2 <= 0) {
            return date;
        }
        X509Certificate x509Certificate = (X509Certificate)certPath.getCertificates().get(n2 - 1);
        if (n2 - 1 == 0) {
            ASN1GeneralizedTime aSN1GeneralizedTime = null;
            try {
                byte[] byArray = ((X509Certificate)certPath.getCertificates().get(n2 - 1)).getExtensionValue(ISISMTTObjectIdentifiers.id_isismtt_at_dateOfCertGen.getId());
                if (byArray != null) {
                    aSN1GeneralizedTime = ASN1GeneralizedTime.getInstance(ASN1Primitive.fromByteArray(byArray));
                }
            } catch (IOException iOException) {
                throw new AnnotatedException("Date of cert gen extension could not be read.");
            } catch (IllegalArgumentException illegalArgumentException) {
                throw new AnnotatedException("Date of cert gen extension could not be read.");
            }
            if (aSN1GeneralizedTime != null) {
                try {
                    return aSN1GeneralizedTime.getDate();
                } catch (ParseException parseException) {
                    throw new AnnotatedException("Date from date of cert gen extension could not be parsed.", parseException);
                }
            }
        }
        return x509Certificate.getNotBefore();
    }

    protected static PublicKey getNextWorkingKey(List list, int n, JcaJceHelper jcaJceHelper) throws CertPathValidatorException {
        Certificate certificate = (Certificate)list.get(n);
        PublicKey publicKey = certificate.getPublicKey();
        if (!(publicKey instanceof DSAPublicKey)) {
            return publicKey;
        }
        DSAPublicKey dSAPublicKey = (DSAPublicKey)publicKey;
        if (dSAPublicKey.getParams() != null) {
            return dSAPublicKey;
        }
        for (int i = n + 1; i < list.size(); ++i) {
            X509Certificate x509Certificate = (X509Certificate)list.get(i);
            publicKey = x509Certificate.getPublicKey();
            if (!(publicKey instanceof DSAPublicKey)) {
                throw new CertPathValidatorException("DSA parameters cannot be inherited from previous certificate.");
            }
            DSAPublicKey dSAPublicKey2 = (DSAPublicKey)publicKey;
            if (dSAPublicKey2.getParams() == null) continue;
            DSAParams dSAParams = dSAPublicKey2.getParams();
            DSAPublicKeySpec dSAPublicKeySpec = new DSAPublicKeySpec(dSAPublicKey.getY(), dSAParams.getP(), dSAParams.getQ(), dSAParams.getG());
            try {
                KeyFactory keyFactory = jcaJceHelper.createKeyFactory("DSA");
                return keyFactory.generatePublic(dSAPublicKeySpec);
            } catch (Exception exception) {
                throw new RuntimeException(exception.getMessage());
            }
        }
        throw new CertPathValidatorException("DSA parameters cannot be inherited from previous certificate.");
    }

    static Collection findIssuerCerts(X509Certificate x509Certificate, List<CertStore> list, List<PKIXCertStore> list2) throws AnnotatedException {
        Object object;
        Object object2;
        X509CertSelector x509CertSelector = new X509CertSelector();
        try {
            x509CertSelector.setSubject(PrincipalUtils.getIssuerPrincipal(x509Certificate).getEncoded());
        } catch (Exception exception) {
            throw new AnnotatedException("Subject criteria for certificate selector to find issuer certificate could not be set.", exception);
        }
        try {
            byte[] byArray;
            object2 = x509Certificate.getExtensionValue(AUTHORITY_KEY_IDENTIFIER);
            if (object2 != null && (byArray = AuthorityKeyIdentifier.getInstance(((ASN1OctetString)(object = ASN1OctetString.getInstance(object2))).getOctets()).getKeyIdentifier()) != null) {
                x509CertSelector.setSubjectKeyIdentifier(new DEROctetString(byArray).getEncoded());
            }
        } catch (Exception exception) {
            // empty catch block
        }
        object2 = new PKIXCertStoreSelector.Builder(x509CertSelector).build();
        object = new LinkedHashSet();
        try {
            CertPathValidatorUtilities.findCertificates((LinkedHashSet)object, (PKIXCertStoreSelector)object2, list);
            CertPathValidatorUtilities.findCertificates((LinkedHashSet)object, (PKIXCertStoreSelector)object2, list2);
        } catch (AnnotatedException annotatedException) {
            throw new AnnotatedException("Issuer certificate cannot be searched.", annotatedException);
        }
        return object;
    }

    protected static void verifyX509Certificate(X509Certificate x509Certificate, PublicKey publicKey, String string) throws GeneralSecurityException {
        if (string == null) {
            x509Certificate.verify(publicKey);
        } else {
            x509Certificate.verify(publicKey, string);
        }
    }

    static void checkCRLsNotEmpty(PKIXCertRevocationCheckerParameters pKIXCertRevocationCheckerParameters, Set set, Object object) throws RecoverableCertPathValidatorException {
        if (set.isEmpty()) {
            if (object instanceof X509AttributeCertificate) {
                X509AttributeCertificate x509AttributeCertificate = (X509AttributeCertificate)object;
                throw new RecoverableCertPathValidatorException("No CRLs found for issuer \"" + x509AttributeCertificate.getIssuer().getPrincipals()[0] + "\"", null, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
            }
            X509Certificate x509Certificate = (X509Certificate)object;
            throw new RecoverableCertPathValidatorException("No CRLs found for issuer \"" + RFC4519Style.INSTANCE.toString(PrincipalUtils.getIssuerPrincipal(x509Certificate)) + "\"", null, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
        }
    }
}

