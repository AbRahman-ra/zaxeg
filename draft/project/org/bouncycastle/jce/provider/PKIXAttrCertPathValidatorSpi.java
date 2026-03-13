/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jce.provider;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertPath;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertPathValidatorSpi;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.bouncycastle.jcajce.PKIXCertStoreSelector;
import org.bouncycastle.jcajce.PKIXExtendedParameters;
import org.bouncycastle.jcajce.util.BCJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jce.provider.CertPathValidatorUtilities;
import org.bouncycastle.jce.provider.RFC3281CertPathUtilities;
import org.bouncycastle.x509.ExtendedPKIXParameters;
import org.bouncycastle.x509.X509AttributeCertStoreSelector;
import org.bouncycastle.x509.X509AttributeCertificate;

public class PKIXAttrCertPathValidatorSpi
extends CertPathValidatorSpi {
    private final JcaJceHelper helper = new BCJcaJceHelper();

    public CertPathValidatorResult engineValidate(CertPath certPath, CertPathParameters certPathParameters) throws CertPathValidatorException, InvalidAlgorithmParameterException {
        PKIXExtendedParameters pKIXExtendedParameters;
        Cloneable cloneable;
        Object object;
        if (!(certPathParameters instanceof ExtendedPKIXParameters) && !(certPathParameters instanceof PKIXExtendedParameters)) {
            throw new InvalidAlgorithmParameterException("Parameters must be a " + ExtendedPKIXParameters.class.getName() + " instance.");
        }
        Set set = new HashSet();
        Set set2 = new HashSet();
        Set set3 = new HashSet();
        HashSet hashSet = new HashSet();
        if (certPathParameters instanceof PKIXParameters) {
            object = new PKIXExtendedParameters.Builder((PKIXParameters)certPathParameters);
            if (certPathParameters instanceof ExtendedPKIXParameters) {
                cloneable = (ExtendedPKIXParameters)certPathParameters;
                ((PKIXExtendedParameters.Builder)object).setUseDeltasEnabled(((ExtendedPKIXParameters)cloneable).isUseDeltasEnabled());
                ((PKIXExtendedParameters.Builder)object).setValidityModel(((ExtendedPKIXParameters)cloneable).getValidityModel());
                set = ((ExtendedPKIXParameters)cloneable).getAttrCertCheckers();
                set2 = ((ExtendedPKIXParameters)cloneable).getProhibitedACAttributes();
                set3 = ((ExtendedPKIXParameters)cloneable).getNecessaryACAttributes();
            }
            pKIXExtendedParameters = ((PKIXExtendedParameters.Builder)object).build();
        } else {
            pKIXExtendedParameters = (PKIXExtendedParameters)certPathParameters;
        }
        object = new Date();
        cloneable = CertPathValidatorUtilities.getValidityDate(pKIXExtendedParameters, (Date)object);
        PKIXCertStoreSelector pKIXCertStoreSelector = pKIXExtendedParameters.getTargetConstraints();
        if (!(pKIXCertStoreSelector instanceof X509AttributeCertStoreSelector)) {
            throw new InvalidAlgorithmParameterException("TargetConstraints must be an instance of " + X509AttributeCertStoreSelector.class.getName() + " for " + this.getClass().getName() + " class.");
        }
        X509AttributeCertificate x509AttributeCertificate = ((X509AttributeCertStoreSelector)((Object)pKIXCertStoreSelector)).getAttributeCert();
        CertPath certPath2 = RFC3281CertPathUtilities.processAttrCert1(x509AttributeCertificate, pKIXExtendedParameters);
        CertPathValidatorResult certPathValidatorResult = RFC3281CertPathUtilities.processAttrCert2(certPath, pKIXExtendedParameters);
        X509Certificate x509Certificate = (X509Certificate)certPath.getCertificates().get(0);
        RFC3281CertPathUtilities.processAttrCert3(x509Certificate, pKIXExtendedParameters);
        RFC3281CertPathUtilities.processAttrCert4(x509Certificate, hashSet);
        RFC3281CertPathUtilities.processAttrCert5(x509AttributeCertificate, (Date)cloneable);
        RFC3281CertPathUtilities.processAttrCert7(x509AttributeCertificate, certPath, certPath2, pKIXExtendedParameters, set);
        RFC3281CertPathUtilities.additionalChecks(x509AttributeCertificate, set2, set3);
        RFC3281CertPathUtilities.checkCRLs(x509AttributeCertificate, pKIXExtendedParameters, (Date)object, (Date)cloneable, x509Certificate, certPath.getCertificates(), this.helper);
        return certPathValidatorResult;
    }
}

