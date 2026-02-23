/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pkix.jcajce;

import java.security.cert.CertPathValidatorException;

class CRLNotFoundException
extends CertPathValidatorException {
    CRLNotFoundException(String string) {
        super(string);
    }

    public CRLNotFoundException(String string, Throwable throwable) {
        super(string, throwable);
    }
}

