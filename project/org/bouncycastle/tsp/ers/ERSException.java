/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.tsp.ers;

public class ERSException
extends Exception {
    private final Throwable cause;

    public ERSException(String string) {
        this(string, null);
    }

    public ERSException(String string, Throwable throwable) {
        super(string);
        this.cause = throwable;
    }

    public Throwable getCause() {
        return this.cause;
    }
}

