/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.JsonLocation;
import java.io.IOException;

public abstract class JacksonException
extends IOException {
    private static final long serialVersionUID = 123L;

    protected JacksonException(String msg) {
        super(msg);
    }

    protected JacksonException(Throwable t) {
        super(t);
    }

    protected JacksonException(String msg, Throwable rootCause) {
        super(msg, rootCause);
    }

    public abstract JsonLocation getLocation();

    public abstract String getOriginalMessage();

    public abstract Object getProcessor();
}

