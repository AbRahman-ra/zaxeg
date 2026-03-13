/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce;

import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import org.bouncycastle.jcajce.PKIXCertRevocationCheckerParameters;

public interface PKIXCertRevocationChecker {
    public void setParameter(String var1, Object var2);

    public void initialize(PKIXCertRevocationCheckerParameters var1) throws CertPathValidatorException;

    public void check(Certificate var1) throws CertPathValidatorException;
}

