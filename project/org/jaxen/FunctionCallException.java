/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import org.jaxen.JaxenException;

public class FunctionCallException
extends JaxenException {
    private static final long serialVersionUID = 7908649612495640943L;

    public FunctionCallException(String message) {
        super(message);
    }

    public FunctionCallException(Throwable nestedException) {
        super(nestedException);
    }

    public FunctionCallException(String message, Exception nestedException) {
        super(message, nestedException);
    }

    public Throwable getNestedException() {
        return this.getCause();
    }
}

