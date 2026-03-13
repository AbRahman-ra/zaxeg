/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jce.provider;

import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.jcajce.PKIXCertRevocationChecker;
import org.bouncycastle.jcajce.PKIXCertRevocationCheckerParameters;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jce.provider.AnnotatedException;
import org.bouncycastle.jce.provider.RFC3280CertPathUtilities;

class ProvCrlRevocationChecker
implements PKIXCertRevocationChecker {
    private final JcaJceHelper helper;
    private PKIXCertRevocationCheckerParameters params;
    private Date currentDate = null;

    public ProvCrlRevocationChecker(JcaJceHelper jcaJceHelper) {
        this.helper = jcaJceHelper;
    }

    public void setParameter(String string, Object object) {
    }

    public void initialize(PKIXCertRevocationCheckerParameters pKIXCertRevocationCheckerParameters) {
        this.params = pKIXCertRevocationCheckerParameters;
        this.currentDate = new Date();
    }

    public void init(boolean bl) throws CertPathValidatorException {
        if (bl) {
            throw new CertPathValidatorException("forward checking not supported");
        }
        this.params = null;
        this.currentDate = new Date();
    }

    public void check(Certificate certificate) throws CertPathValidatorException {
        try {
            RFC3280CertPathUtilities.checkCRLs(this.params, this.params.getParamsPKIX(), this.currentDate, this.params.getValidDate(), (X509Certificate)certificate, this.params.getSigningCert(), this.params.getWorkingPublicKey(), this.params.getCertPath().getCertificates(), this.helper);
        } catch (AnnotatedException annotatedException) {
            Throwable throwable = annotatedException;
            if (null != annotatedException.getCause()) {
                throwable = annotatedException.getCause();
            }
            throw new CertPathValidatorException(annotatedException.getMessage(), throwable, this.params.getCertPath(), this.params.getIndex());
        }
    }
}

