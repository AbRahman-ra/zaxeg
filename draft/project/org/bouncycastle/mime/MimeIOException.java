/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime;

import java.io.IOException;

public class MimeIOException
extends IOException {
    private Throwable cause;

    public MimeIOException(String string, Throwable throwable) {
        super(string);
        this.cause = throwable;
    }

    public MimeIOException(String string) {
        super(string);
    }

    public Throwable getCause() {
        return this.cause;
    }
}

