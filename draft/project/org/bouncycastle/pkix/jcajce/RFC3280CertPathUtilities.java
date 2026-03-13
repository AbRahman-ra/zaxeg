/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pkix.jcajce;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CRL;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.cert.X509Extension;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.IssuingDistributionPoint;
import org.bouncycastle.jcajce.PKIXCRLStore;
import org.bouncycastle.jcajce.PKIXCRLStoreSelector;
import org.bouncycastle.jcajce.PKIXCertStoreSelector;
import org.bouncycastle.jcajce.PKIXExtendedBuilderParameters;
import org.bouncycastle.jcajce.PKIXExtendedParameters;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.pkix.jcajce.AnnotatedException;
import org.bouncycastle.pkix.jcajce.CRLNotFoundException;
import org.bouncycastle.pkix.jcajce.CertStatus;
import org.bouncycastle.pkix.jcajce.PKIXCRLUtil;
import org.bouncycastle.pkix.jcajce.ReasonsMask;
import org.bouncycastle.pkix.jcajce.RevocationUtilities;
import org.bouncycastle.util.Arrays;

class RFC3280CertPathUtilities {
    public static final String ISSUING_DISTRIBUTION_POINT = Extension.issuingDistributionPoint.getId();
    public static final String FRESHEST_CRL = Extension.freshestCRL.getId();
    public static final String DELTA_CRL_INDICATOR = Extension.deltaCRLIndicator.getId();
    public static final String BASIC_CONSTRAINTS = Extension.basicConstraints.getId();
    public static final String AUTHORITY_KEY_IDENTIFIER = Extension.authorityKeyIdentifier.getId();
    protected static final int KEY_CERT_SIGN = 5;
    protected static final int CRL_SIGN = 6;

    RFC3280CertPathUtilities() {
    }

    protected static void processCRLB2(DistributionPoint distributionPoint, Object object, X509CRL x509CRL) throws AnnotatedException {
        IssuingDistributionPoint issuingDistributionPoint = null;
        try {
            issuingDistributionPoint = IssuingDistributionPoint.getInstance(RevocationUtilities.getExtensionValue(x509CRL, Extension.issuingDistributionPoint));
        } catch (Exception exception) {
            throw new AnnotatedException("Issuing distribution point extension could not be decoded.", exception);
        }
        if (issuingDistributionPoint != null) {
            ASN1Object aSN1Object;
            if (issuingDistributionPoint.getDistributionPoint() != null) {
                Object object2;
                aSN1Object = IssuingDistributionPoint.getInstance(issuingDistributionPoint).getDistributionPoint();
                ArrayList<GeneralName> arrayList = new ArrayList<GeneralName>();
                if (((DistributionPointName)aSN1Object).getType() == 0) {
                    object2 = GeneralNames.getInstance(((DistributionPointName)aSN1Object).getName()).getNames();
                    for (int i = 0; i < ((GeneralName[])object2).length; ++i) {
                        arrayList.add(object2[i]);
                    }
                }
                if (((DistributionPointName)aSN1Object).getType() == 1) {
                    object2 = new ASN1EncodableVector();
                    try {
                        Enumeration enumeration = ASN1Sequence.getInstance(x509CRL.getIssuerX500Principal().getEncoded()).getObjects();
                        while (enumeration.hasMoreElements()) {
                            ((ASN1EncodableVector)object2).add((ASN1Encodable)enumeration.nextElement());
                        }
                    } catch (Exception exception) {
                        throw new AnnotatedException("Could not read CRL issuer.", exception);
                    }
                    ((ASN1EncodableVector)object2).add(((DistributionPointName)aSN1Object).getName());
                    arrayList.add(new GeneralName(X500Name.getInstance(new DERSequence((ASN1EncodableVector)object2))));
                }
                boolean bl = false;
                if (distributionPoint.getDistributionPoint() != null) {
                    int n;
                    aSN1Object = distributionPoint.getDistributionPoint();
                    GeneralName[] generalNameArray = null;
                    if (((DistributionPointName)aSN1Object).getType() == 0) {
                        generalNameArray = GeneralNames.getInstance(((DistributionPointName)aSN1Object).getName()).getNames();
                    }
                    if (((DistributionPointName)aSN1Object).getType() == 1) {
                        if (distributionPoint.getCRLIssuer() != null) {
                            generalNameArray = distributionPoint.getCRLIssuer().getNames();
                        } else {
                            generalNameArray = new GeneralName[1];
                            try {
                                generalNameArray[0] = new GeneralName(X500Name.getInstance(((X509Certificate)object).getIssuerX500Principal().getEncoded()));
                            } catch (Exception exception) {
                                throw new AnnotatedException("Could not read certificate issuer.", exception);
                            }
                        }
                        for (n = 0; n < generalNameArray.length; ++n) {
                            Enumeration enumeration = ASN1Sequence.getInstance(generalNameArray[n].getName().toASN1Primitive()).getObjects();
                            ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
                            while (enumeration.hasMoreElements()) {
                                aSN1EncodableVector.add((ASN1Encodable)enumeration.nextElement());
                            }
                            aSN1EncodableVector.add(((DistributionPointName)aSN1Object).getName());
                            generalNameArray[n] = new GeneralName(X500Name.getInstance(new DERSequence(aSN1EncodableVector)));
                        }
                    }
                    if (generalNameArray != null) {
                        for (n = 0; n < generalNameArray.length; ++n) {
                            if (!arrayList.contains(generalNameArray[n])) continue;
                            bl = true;
                            break;
                        }
                    }
                    if (!bl) {
                        throw new AnnotatedException("No match for certificate CRL issuing distribution point name to cRLIssuer CRL distribution point.");
                    }
                } else {
                    if (distributionPoint.getCRLIssuer() == null) {
                        throw new AnnotatedException("Either the cRLIssuer or the distributionPoint field must be contained in DistributionPoint.");
                    }
                    GeneralName[] generalNameArray = distributionPoint.getCRLIssuer().getNames();
                    for (int i = 0; i < generalNameArray.length; ++i) {
                        if (!arrayList.contains(generalNameArray[i])) continue;
                        bl = true;
                        break;
                    }
                    if (!bl) {
                        throw new AnnotatedException("No match for certificate CRL issuing distribution point name to cRLIssuer CRL distribution point.");
                    }
                }
            }
            aSN1Object = null;
            try {
                aSN1Object = BasicConstraints.getInstance(RevocationUtilities.getExtensionValue((X509Extension)object, Extension.basicConstraints));
            } catch (Exception exception) {
                throw new AnnotatedException("Basic constraints extension could not be decoded.", exception);
            }
            if (object instanceof X509Certificate) {
                if (issuingDistributionPoint.onlyContainsUserCerts() && aSN1Object != null && ((BasicConstraints)aSN1Object).isCA()) {
                    throw new AnnotatedException("CA Cert CRL only contains user certificates.");
                }
                if (issuingDistributionPoint.onlyContainsCACerts() && (aSN1Object == null || !((BasicConstraints)aSN1Object).isCA())) {
                    throw new AnnotatedException("End CRL only contains CA certificates.");
                }
            }
            if (issuingDistributionPoint.onlyContainsAttributeCerts()) {
                throw new AnnotatedException("onlyContainsAttributeCerts boolean is asserted.");
            }
        }
    }

    protected static void processCRLB1(DistributionPoint distributionPoint, Object object, X509CRL x509CRL) throws AnnotatedException {
        ASN1Primitive aSN1Primitive = RevocationUtilities.getExtensionValue(x509CRL, Extension.issuingDistributionPoint);
        boolean bl = false;
        if (aSN1Primitive != null && IssuingDistributionPoint.getInstance(aSN1Primitive).isIndirectCRL()) {
            bl = true;
        }
        byte[] byArray = x509CRL.getIssuerX500Principal().getEncoded();
        boolean bl2 = false;
        if (distributionPoint.getCRLIssuer() != null) {
            GeneralName[] generalNameArray = distributionPoint.getCRLIssuer().getNames();
            for (int i = 0; i < generalNameArray.length; ++i) {
                if (generalNameArray[i].getTagNo() != 4) continue;
                try {
                    if (!Arrays.areEqual(generalNameArray[i].getName().toASN1Primitive().getEncoded(), byArray)) continue;
                    bl2 = true;
                    continue;
                } catch (IOException iOException) {
                    throw new AnnotatedException("CRL issuer information from distribution point cannot be decoded.", iOException);
                }
            }
            if (bl2 && !bl) {
                throw new AnnotatedException("Distribution point contains cRLIssuer field but CRL is not indirect.");
            }
            if (!bl2) {
                throw new AnnotatedException("CRL issuer of CRL does not match CRL issuer of distribution point.");
            }
        } else if (x509CRL.getIssuerX500Principal().equals(((X509Certificate)object).getIssuerX500Principal())) {
            bl2 = true;
        }
        if (!bl2) {
            throw new AnnotatedException("Cannot find matching CRL issuer for certificate.");
        }
    }

    protected static ReasonsMask processCRLD(X509CRL x509CRL, DistributionPoint distributionPoint) throws AnnotatedException {
        IssuingDistributionPoint issuingDistributionPoint = null;
        try {
            issuingDistributionPoint = IssuingDistributionPoint.getInstance(RevocationUtilities.getExtensionValue(x509CRL, Extension.issuingDistributionPoint));
        } catch (Exception exception) {
            throw new AnnotatedException("Issuing distribution point extension could not be decoded.", exception);
        }
        if (issuingDistributionPoint != null && issuingDistributionPoint.getOnlySomeReasons() != null && distributionPoint.getReasons() != null) {
            return new ReasonsMask(distributionPoint.getReasons()).intersect(new ReasonsMask(issuingDistributionPoint.getOnlySomeReasons()));
        }
        if ((issuingDistributionPoint == null || issuingDistributionPoint.getOnlySomeReasons() == null) && distributionPoint.getReasons() == null) {
            return ReasonsMask.allReasons;
        }
        return (distributionPoint.getReasons() == null ? ReasonsMask.allReasons : new ReasonsMask(distributionPoint.getReasons())).intersect(issuingDistributionPoint == null ? ReasonsMask.allReasons : new ReasonsMask(issuingDistributionPoint.getOnlySomeReasons()));
    }

    protected static Set processCRLF(X509CRL x509CRL, Object object, X509Certificate x509Certificate, PublicKey publicKey, PKIXExtendedParameters pKIXExtendedParameters, List list, JcaJceHelper jcaJceHelper) throws AnnotatedException {
        Object object2;
        Object object3;
        Object certPathBuilderException;
        Object object5;
        X509CertSelector x509CertSelector = new X509CertSelector();
        try {
            object5 = x509CRL.getIssuerX500Principal().getEncoded();
            x509CertSelector.setSubject((byte[])object5);
        } catch (IOException iOException) {
            throw new AnnotatedException("subject criteria for certificate selector to find issuer certificate for CRL could not be set", iOException);
        }
        object5 = new PKIXCertStoreSelector.Builder(x509CertSelector).build();
        LinkedHashSet<X509Certificate> linkedHashSet = new LinkedHashSet<X509Certificate>();
        try {
            RevocationUtilities.findCertificates(linkedHashSet, (PKIXCertStoreSelector)object5, pKIXExtendedParameters.getCertificateStores());
            RevocationUtilities.findCertificates(linkedHashSet, (PKIXCertStoreSelector)object5, pKIXExtendedParameters.getCertStores());
        } catch (AnnotatedException annotatedException) {
            throw new AnnotatedException("Issuer certificate for CRL cannot be searched.", annotatedException);
        }
        linkedHashSet.add(x509Certificate);
        ArrayList<X509Certificate> arrayList = new ArrayList<X509Certificate>();
        ArrayList<PublicKey> arrayList2 = new ArrayList<PublicKey>();
        for (X509Certificate serializable2 : linkedHashSet) {
            if (serializable2.equals(x509Certificate)) {
                arrayList.add(serializable2);
                arrayList2.add(publicKey);
                continue;
            }
            try {
                certPathBuilderException = jcaJceHelper.createCertPathBuilder("PKIX");
                X509CertSelector i = new X509CertSelector();
                i.setCertificate(serializable2);
                object3 = new PKIXExtendedParameters.Builder(pKIXExtendedParameters).setTargetConstraints(new PKIXCertStoreSelector.Builder(i).build());
                if (list.contains(serializable2)) {
                    ((PKIXExtendedParameters.Builder)object3).setRevocationEnabled(false);
                } else {
                    ((PKIXExtendedParameters.Builder)object3).setRevocationEnabled(true);
                }
                object2 = new PKIXExtendedBuilderParameters.Builder(((PKIXExtendedParameters.Builder)object3).build()).build();
                List<? extends Certificate> list2 = ((CertPathBuilder)certPathBuilderException).build((CertPathParameters)object2).getCertPath().getCertificates();
                arrayList.add(serializable2);
                arrayList2.add(RevocationUtilities.getNextWorkingKey(list2, 0, jcaJceHelper));
            } catch (CertPathBuilderException certPathValidatorException) {
                throw new AnnotatedException("CertPath for CRL signer failed to validate.", certPathValidatorException);
            } catch (CertPathValidatorException exception) {
                throw new AnnotatedException("Public key of issuer certificate of CRL could not be retrieved.", exception);
            } catch (Exception exception) {
                throw new AnnotatedException(exception.getMessage());
            }
        }
        HashSet hashSet = new HashSet();
        certPathBuilderException = null;
        for (int i = 0; i < arrayList.size(); ++i) {
            object3 = (X509Certificate)arrayList.get(i);
            object2 = ((X509Certificate)object3).getKeyUsage();
            if (!(object2 == null || ((boolean[])object2).length > 6 && object2[6])) {
                certPathBuilderException = new AnnotatedException("Issuer certificate key usage extension does not permit CRL signing.");
                continue;
            }
            hashSet.add(arrayList2.get(i));
        }
        if (hashSet.isEmpty() && certPathBuilderException == null) {
            throw new AnnotatedException("Cannot find a valid issuer certificate.");
        }
        if (hashSet.isEmpty() && certPathBuilderException != null) {
            throw certPathBuilderException;
        }
        return hashSet;
    }

    protected static PublicKey processCRLG(X509CRL x509CRL, Set set) throws AnnotatedException {
        Exception exception = null;
        for (PublicKey publicKey : set) {
            try {
                x509CRL.verify(publicKey);
                return publicKey;
            } catch (Exception exception2) {
                exception = exception2;
            }
        }
        throw new AnnotatedException("Cannot verify CRL.", exception);
    }

    protected static X509CRL processCRLH(Set set, PublicKey publicKey) throws AnnotatedException {
        Exception exception = null;
        for (X509CRL x509CRL : set) {
            try {
                x509CRL.verify(publicKey);
                return x509CRL;
            } catch (Exception exception2) {
                exception = exception2;
            }
        }
        if (exception != null) {
            throw new AnnotatedException("Cannot verify delta CRL.", exception);
        }
        return null;
    }

    protected static Set processCRLA1i(PKIXExtendedParameters pKIXExtendedParameters, Date date, X509Certificate x509Certificate, X509CRL x509CRL) throws AnnotatedException {
        HashSet hashSet = new HashSet();
        if (pKIXExtendedParameters.isUseDeltasEnabled()) {
            CRLDistPoint cRLDistPoint = null;
            try {
                cRLDistPoint = CRLDistPoint.getInstance(RevocationUtilities.getExtensionValue(x509Certificate, Extension.freshestCRL));
            } catch (AnnotatedException annotatedException) {
                throw new AnnotatedException("Freshest CRL extension could not be decoded from certificate.", annotatedException);
            }
            if (cRLDistPoint == null) {
                try {
                    cRLDistPoint = CRLDistPoint.getInstance(RevocationUtilities.getExtensionValue(x509CRL, Extension.freshestCRL));
                } catch (AnnotatedException annotatedException) {
                    throw new AnnotatedException("Freshest CRL extension could not be decoded from CRL.", annotatedException);
                }
            }
            if (cRLDistPoint != null) {
                ArrayList<PKIXCRLStore> arrayList = new ArrayList<PKIXCRLStore>();
                arrayList.addAll(pKIXExtendedParameters.getCRLStores());
                try {
                    arrayList.addAll(RevocationUtilities.getAdditionalStoresFromCRLDistributionPoint(cRLDistPoint, pKIXExtendedParameters.getNamedCRLStoreMap()));
                } catch (AnnotatedException annotatedException) {
                    throw new AnnotatedException("No new delta CRL locations could be added from Freshest CRL extension.", annotatedException);
                }
                try {
                    hashSet.addAll(RevocationUtilities.getDeltaCRLs(date, x509CRL, pKIXExtendedParameters.getCertStores(), arrayList));
                } catch (AnnotatedException annotatedException) {
                    throw new AnnotatedException("Exception obtaining delta CRLs.", annotatedException);
                }
            }
        }
        return hashSet;
    }

    protected static Set[] processCRLA1ii(PKIXExtendedParameters pKIXExtendedParameters, Date date, Date date2, X509Certificate x509Certificate, X509CRL x509CRL) throws AnnotatedException {
        X509CRLSelector x509CRLSelector = new X509CRLSelector();
        x509CRLSelector.setCertificateChecking(x509Certificate);
        try {
            x509CRLSelector.addIssuerName(x509CRL.getIssuerX500Principal().getEncoded());
        } catch (IOException iOException) {
            throw new AnnotatedException("Cannot extract issuer from CRL." + iOException, iOException);
        }
        PKIXCRLStoreSelector<? extends CRL> pKIXCRLStoreSelector = new PKIXCRLStoreSelector.Builder(x509CRLSelector).setCompleteCRLEnabled(true).build();
        Set set = PKIXCRLUtil.findCRLs(pKIXCRLStoreSelector, date2, pKIXExtendedParameters.getCertStores(), pKIXExtendedParameters.getCRLStores());
        HashSet hashSet = new HashSet();
        if (pKIXExtendedParameters.isUseDeltasEnabled()) {
            try {
                hashSet.addAll(RevocationUtilities.getDeltaCRLs(date2, x509CRL, pKIXExtendedParameters.getCertStores(), pKIXExtendedParameters.getCRLStores()));
            } catch (AnnotatedException annotatedException) {
                throw new AnnotatedException("Exception obtaining delta CRLs.", annotatedException);
            }
        }
        return new Set[]{set, hashSet};
    }

    protected static void processCRLC(X509CRL x509CRL, X509CRL x509CRL2, PKIXExtendedParameters pKIXExtendedParameters) throws AnnotatedException {
        if (x509CRL == null) {
            return;
        }
        IssuingDistributionPoint issuingDistributionPoint = null;
        try {
            issuingDistributionPoint = IssuingDistributionPoint.getInstance(RevocationUtilities.getExtensionValue(x509CRL2, Extension.issuingDistributionPoint));
        } catch (Exception exception) {
            throw new AnnotatedException("issuing distribution point extension could not be decoded.", exception);
        }
        if (pKIXExtendedParameters.isUseDeltasEnabled()) {
            if (!x509CRL.getIssuerX500Principal().equals(x509CRL2.getIssuerX500Principal())) {
                throw new AnnotatedException("complete CRL issuer does not match delta CRL issuer");
            }
            IssuingDistributionPoint issuingDistributionPoint2 = null;
            try {
                issuingDistributionPoint2 = IssuingDistributionPoint.getInstance(RevocationUtilities.getExtensionValue(x509CRL, Extension.issuingDistributionPoint));
            } catch (Exception exception) {
                throw new AnnotatedException("Issuing distribution point extension from delta CRL could not be decoded.", exception);
            }
            boolean bl = false;
            if (issuingDistributionPoint == null) {
                if (issuingDistributionPoint2 == null) {
                    bl = true;
                }
            } else if (issuingDistributionPoint.equals(issuingDistributionPoint2)) {
                bl = true;
            }
            if (!bl) {
                throw new AnnotatedException("Issuing distribution point extension from delta CRL and complete CRL does not match.");
            }
            ASN1Primitive aSN1Primitive = null;
            try {
                aSN1Primitive = RevocationUtilities.getExtensionValue(x509CRL2, Extension.authorityKeyIdentifier);
            } catch (AnnotatedException annotatedException) {
                throw new AnnotatedException("Authority key identifier extension could not be extracted from complete CRL.", annotatedException);
            }
            ASN1Primitive aSN1Primitive2 = null;
            try {
                aSN1Primitive2 = RevocationUtilities.getExtensionValue(x509CRL, Extension.authorityKeyIdentifier);
            } catch (AnnotatedException annotatedException) {
                throw new AnnotatedException("Authority key identifier extension could not be extracted from delta CRL.", annotatedException);
            }
            if (aSN1Primitive == null) {
                throw new AnnotatedException("CRL authority key identifier is null.");
            }
            if (aSN1Primitive2 == null) {
                throw new AnnotatedException("Delta CRL authority key identifier is null.");
            }
            if (!aSN1Primitive.equals(aSN1Primitive2)) {
                throw new AnnotatedException("Delta CRL authority key identifier does not match complete CRL authority key identifier.");
            }
        }
    }

    protected static void processCRLI(Date date, X509CRL x509CRL, Object object, CertStatus certStatus, PKIXExtendedParameters pKIXExtendedParameters) throws AnnotatedException {
        if (pKIXExtendedParameters.isUseDeltasEnabled() && x509CRL != null) {
            RevocationUtilities.getCertStatus(date, x509CRL, object, certStatus);
        }
    }

    protected static void processCRLJ(Date date, X509CRL x509CRL, Object object, CertStatus certStatus) throws AnnotatedException {
        if (certStatus.getCertStatus() == 11) {
            RevocationUtilities.getCertStatus(date, x509CRL, object, certStatus);
        }
    }

    static void checkCRL(DistributionPoint distributionPoint, PKIXExtendedParameters pKIXExtendedParameters, Date date, Date date2, X509Certificate x509Certificate, X509Certificate x509Certificate2, PublicKey publicKey, CertStatus certStatus, ReasonsMask reasonsMask, List list, JcaJceHelper jcaJceHelper) throws AnnotatedException, CRLNotFoundException {
        if (date2.getTime() > date.getTime()) {
            throw new AnnotatedException("Validation time is in future.");
        }
        Set set = RevocationUtilities.getCompleteCRLs(distributionPoint, x509Certificate, date2, pKIXExtendedParameters.getCertStores(), pKIXExtendedParameters.getCRLStores());
        boolean bl = false;
        AnnotatedException annotatedException = null;
        Iterator iterator = set.iterator();
        while (iterator.hasNext() && certStatus.getCertStatus() == 11 && !reasonsMask.isAllReasons()) {
            try {
                Set<String> set2;
                X509CRL x509CRL = (X509CRL)iterator.next();
                ReasonsMask reasonsMask2 = RFC3280CertPathUtilities.processCRLD(x509CRL, distributionPoint);
                if (!reasonsMask2.hasNewReasons(reasonsMask)) continue;
                Set set3 = RFC3280CertPathUtilities.processCRLF(x509CRL, x509Certificate, x509Certificate2, publicKey, pKIXExtendedParameters, list, jcaJceHelper);
                PublicKey publicKey2 = RFC3280CertPathUtilities.processCRLG(x509CRL, set3);
                X509CRL x509CRL2 = null;
                if (pKIXExtendedParameters.isUseDeltasEnabled()) {
                    set2 = RevocationUtilities.getDeltaCRLs(date2, x509CRL, pKIXExtendedParameters.getCertStores(), pKIXExtendedParameters.getCRLStores());
                    x509CRL2 = RFC3280CertPathUtilities.processCRLH(set2, publicKey2);
                }
                if (pKIXExtendedParameters.getValidityModel() != 1 && x509Certificate.getNotAfter().getTime() < x509CRL.getThisUpdate().getTime()) {
                    throw new AnnotatedException("No valid CRL for current time found.");
                }
                RFC3280CertPathUtilities.processCRLB1(distributionPoint, x509Certificate, x509CRL);
                RFC3280CertPathUtilities.processCRLB2(distributionPoint, x509Certificate, x509CRL);
                RFC3280CertPathUtilities.processCRLC(x509CRL2, x509CRL, pKIXExtendedParameters);
                RFC3280CertPathUtilities.processCRLI(date2, x509CRL2, x509Certificate, certStatus, pKIXExtendedParameters);
                RFC3280CertPathUtilities.processCRLJ(date2, x509CRL, x509Certificate, certStatus);
                if (certStatus.getCertStatus() == 8) {
                    certStatus.setCertStatus(11);
                }
                reasonsMask.addReasons(reasonsMask2);
                set2 = x509CRL.getCriticalExtensionOIDs();
                if (set2 != null) {
                    set2 = new HashSet<String>(set2);
                    set2.remove(Extension.issuingDistributionPoint.getId());
                    set2.remove(Extension.deltaCRLIndicator.getId());
                    if (!set2.isEmpty()) {
                        throw new AnnotatedException("CRL contains unsupported critical extensions.");
                    }
                }
                if (x509CRL2 != null && (set2 = x509CRL2.getCriticalExtensionOIDs()) != null) {
                    set2 = new HashSet<String>(set2);
                    set2.remove(Extension.issuingDistributionPoint.getId());
                    set2.remove(Extension.deltaCRLIndicator.getId());
                    if (!set2.isEmpty()) {
                        throw new AnnotatedException("Delta CRL contains unsupported critical extension.");
                    }
                }
                bl = true;
            } catch (AnnotatedException annotatedException2) {
                annotatedException = annotatedException2;
            }
        }
        if (!bl) {
            throw annotatedException;
        }
    }
}

