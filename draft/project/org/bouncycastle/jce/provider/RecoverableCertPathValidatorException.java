/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jce.provider;

import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;

class RecoverableCertPathValidatorException
extends CertPathValidatorException {
    public RecoverableCertPathValidatorException(String string, Throwable throwable, CertPath certPath, int n) {
        super(string, throwable, certPath, n);
    }
}

