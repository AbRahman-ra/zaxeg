/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jce.provider;

public class PKIXNameConstraintValidatorException
extends Exception {
    private Throwable cause;

    public PKIXNameConstraintValidatorException(String string) {
        super(string);
    }

    public PKIXNameConstraintValidatorException(String string, Throwable throwable) {
        super(string);
        this.cause = throwable;
    }

    public Throwable getCause() {
        return this.cause;
    }
}

