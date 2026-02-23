/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pkix.jcajce;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509Certificate;
import java.security.cert.X509Extension;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
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
import org.bouncycastle.jcajce.PKIXExtendedParameters;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.pkix.jcajce.AnnotatedException;
import org.bouncycastle.pkix.jcajce.CRLNotFoundException;
import org.bouncycastle.pkix.jcajce.CertStatus;
import org.bouncycastle.pkix.jcajce.PKIXCRLUtil;
import org.bouncycastle.pkix.jcajce.RFC3280CertPathUtilities;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.StoreException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class RevocationUtilities {
    protected static final String ISSUING_DISTRIBUTION_POINT = Extension.issuingDistributionPoint.getId();

    RevocationUtilities() {
    }

    protected static Date getValidityDate(PKIXExtendedParameters pKIXExtendedParameters, Date date) {
        Date date2 = pKIXExtendedParameters.getValidityDate();
        return null == date2 ? date : date2;
    }

    protected static ASN1Primitive getExtensionValue(X509Extension x509Extension, ASN1ObjectIdentifier aSN1ObjectIdentifier) throws AnnotatedException {
        byte[] byArray = x509Extension.getExtensionValue(aSN1ObjectIdentifier.getId());
        return null == byArray ? null : RevocationUtilities.getObject(aSN1ObjectIdentifier, byArray);
    }

    private static ASN1Primitive getObject(ASN1ObjectIdentifier aSN1ObjectIdentifier, byte[] byArray) throws AnnotatedException {
        try {
            return ASN1Primitive.fromByteArray(ASN1OctetString.getInstance(byArray).getOctets());
        } catch (Exception exception) {
            throw new AnnotatedException("exception processing extension " + aSN1ObjectIdentifier, exception);
        }
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

    static List<PKIXCRLStore> getAdditionalStoresFromCRLDistributionPoint(CRLDistPoint cRLDistPoint, Map<GeneralName, PKIXCRLStore> map) throws AnnotatedException {
        DistributionPoint[] distributionPointArray;
        if (cRLDistPoint == null) {
            return Collections.emptyList();
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
            GeneralName[] generalNameArray = GeneralNames.getInstance(distributionPointName.getName()).getNames();
            for (int j = 0; j < generalNameArray.length; ++j) {
                PKIXCRLStore pKIXCRLStore = map.get(generalNameArray[j]);
                if (pKIXCRLStore == null) continue;
                arrayList.add(pKIXCRLStore);
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
                    arrayList.add(X500Name.getInstance(((GeneralName)object[i]).getName()));
                    continue;
                } catch (IllegalArgumentException illegalArgumentException) {
                    throw new AnnotatedException("CRL issuer information from distribution point cannot be decoded.", illegalArgumentException);
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

    protected static void getCertStatus(Date date, X509CRL x509CRL, Object object, CertStatus certStatus) throws AnnotatedException {
        X500Principal x500Principal;
        Object object2;
        Object object3;
        boolean bl;
        try {
            bl = RevocationUtilities.isIndirectCRL(x509CRL);
        } catch (CRLException cRLException) {
            throw new AnnotatedException("Failed check for indirect CRL.", cRLException);
        }
        X509Certificate x509Certificate = (X509Certificate)object;
        X500Name x500Name = RevocationUtilities.getIssuer(x509Certificate);
        if (!bl && !x500Name.equals(object3 = RevocationUtilities.getIssuer(x509CRL))) {
            return;
        }
        object3 = x509CRL.getRevokedCertificate(x509Certificate.getSerialNumber());
        if (null == object3) {
            return;
        }
        if (bl && !x500Name.equals(object2 = null == (x500Principal = ((X509CRLEntry)object3).getCertificateIssuer()) ? RevocationUtilities.getIssuer(x509CRL) : RevocationUtilities.getX500Name(x500Principal))) {
            return;
        }
        int n = 0;
        if (((X509CRLEntry)object3).hasExtensions()) {
            try {
                object2 = RevocationUtilities.getExtensionValue((X509Extension)object3, Extension.reasonCode);
                ASN1Enumerated aSN1Enumerated = ASN1Enumerated.getInstance(object2);
                if (null != aSN1Enumerated) {
                    n = aSN1Enumerated.intValueExact();
                }
            } catch (Exception exception) {
                throw new AnnotatedException("Reason code CRL entry extension could not be decoded.", exception);
            }
        }
        if (date.before((Date)(object2 = ((X509CRLEntry)object3).getRevocationDate()))) {
            switch (n) {
                case 0: 
                case 1: 
                case 2: 
                case 10: {
                    break;
                }
                default: {
                    return;
                }
            }
        }
        certStatus.setCertStatus(n);
        certStatus.setRevocationDate((Date)object2);
    }

    protected static Set getDeltaCRLs(Date date, X509CRL x509CRL, List<CertStore> list, List<PKIXCRLStore> list2) throws AnnotatedException {
        Object object;
        X509CRLSelector x509CRLSelector = new X509CRLSelector();
        try {
            x509CRLSelector.addIssuerName(x509CRL.getIssuerX500Principal().getEncoded());
        } catch (IOException iOException) {
            throw new AnnotatedException("cannot extract issuer from CRL.", iOException);
        }
        BigInteger bigInteger = null;
        try {
            object = RevocationUtilities.getExtensionValue(x509CRL, Extension.cRLNumber);
            if (object != null) {
                bigInteger = ASN1Integer.getInstance(object).getPositiveValue();
            }
        } catch (Exception exception) {
            throw new AnnotatedException("cannot extract CRL number extension from CRL", exception);
        }
        try {
            object = x509CRL.getExtensionValue(ISSUING_DISTRIBUTION_POINT);
        } catch (Exception exception) {
            throw new AnnotatedException("issuing distribution point extension value could not be read", exception);
        }
        x509CRLSelector.setMinCRLNumber(bigInteger == null ? null : bigInteger.add(BigInteger.valueOf(1L)));
        PKIXCRLStoreSelector.Builder builder = new PKIXCRLStoreSelector.Builder(x509CRLSelector);
        builder.setIssuingDistributionPoint((byte[])object);
        builder.setIssuingDistributionPointEnabled(true);
        builder.setMaxBaseCRLNumber(bigInteger);
        PKIXCRLStoreSelector<? extends CRL> pKIXCRLStoreSelector = builder.build();
        Set set = PKIXCRLUtil.findCRLs(pKIXCRLStoreSelector, date, list, list2);
        HashSet<X509CRL> hashSet = new HashSet<X509CRL>();
        for (X509CRL x509CRL2 : set) {
            if (!RevocationUtilities.isDeltaCRL(x509CRL2)) continue;
            hashSet.add(x509CRL2);
        }
        return hashSet;
    }

    private static boolean isDeltaCRL(X509CRL x509CRL) {
        Set<String> set = x509CRL.getCriticalExtensionOIDs();
        return null == set ? false : set.contains(RFC3280CertPathUtilities.DELTA_CRL_INDICATOR);
    }

    protected static Set getCompleteCRLs(DistributionPoint distributionPoint, Object object, Date date, List list, List list2) throws AnnotatedException, CRLNotFoundException {
        Cloneable cloneable;
        X509CRLSelector x509CRLSelector = new X509CRLSelector();
        try {
            cloneable = new HashSet<X500Name>();
            cloneable.add(RevocationUtilities.getIssuer((X509Certificate)object));
            RevocationUtilities.getCRLIssuersFromDistributionPoint(distributionPoint, cloneable, x509CRLSelector);
        } catch (AnnotatedException annotatedException) {
            throw new AnnotatedException("Could not get issuer information from distribution point.", annotatedException);
        }
        if (object instanceof X509Certificate) {
            x509CRLSelector.setCertificateChecking((X509Certificate)object);
        }
        cloneable = new PKIXCRLStoreSelector.Builder(x509CRLSelector).setCompleteCRLEnabled(true).build();
        Set set = PKIXCRLUtil.findCRLs(cloneable, date, list, list2);
        RevocationUtilities.checkCRLsNotEmpty(set, object);
        return set;
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

    static void checkCRLsNotEmpty(Set set, Object object) throws CRLNotFoundException {
        if (set.isEmpty()) {
            X500Name x500Name = RevocationUtilities.getIssuer((X509Certificate)object);
            throw new CRLNotFoundException("No CRLs found for issuer \"" + RFC4519Style.INSTANCE.toString(x500Name) + "\"");
        }
    }

    public static boolean isIndirectCRL(X509CRL x509CRL) throws CRLException {
        try {
            byte[] byArray = x509CRL.getExtensionValue(Extension.issuingDistributionPoint.getId());
            return byArray != null && IssuingDistributionPoint.getInstance(ASN1OctetString.getInstance(byArray).getOctets()).isIndirectCRL();
        } catch (Exception exception) {
            throw new CRLException("exception reading IssuingDistributionPoint", exception);
        }
    }

    private static X500Name getIssuer(X509Certificate x509Certificate) {
        return RevocationUtilities.getX500Name(x509Certificate.getIssuerX500Principal());
    }

    private static X500Name getIssuer(X509CRL x509CRL) {
        return RevocationUtilities.getX500Name(x509CRL.getIssuerX500Principal());
    }

    private static X500Name getX500Name(X500Principal x500Principal) {
        return X500Name.getInstance(x500Principal.getEncoded());
    }
}

