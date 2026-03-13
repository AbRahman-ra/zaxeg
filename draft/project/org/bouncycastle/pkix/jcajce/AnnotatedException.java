/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pkix.jcajce;

class AnnotatedException
extends Exception {
    private Throwable _underlyingException;

    public AnnotatedException(String string, Throwable throwable) {
        super(string);
        this._underlyingException = throwable;
    }

    public AnnotatedException(String string) {
        this(string, null);
    }

    public Throwable getCause() {
        return this._underlyingException;
    }
}

