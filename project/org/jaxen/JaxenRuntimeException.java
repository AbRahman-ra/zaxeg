/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import java.io.PrintStream;
import java.io.PrintWriter;
import org.jaxen.JaxenException;

public class JaxenRuntimeException
extends RuntimeException {
    private static final long serialVersionUID = -930309761511911193L;
    private Throwable cause;
    private boolean causeSet = false;

    public JaxenRuntimeException(Throwable cause) {
        super(cause.getMessage());
        this.initCause(cause);
    }

    public JaxenRuntimeException(String message) {
        super(message);
    }

    public Throwable getCause() {
        return this.cause;
    }

    public Throwable initCause(Throwable cause) {
        if (this.causeSet) {
            throw new IllegalStateException("Cause cannot be reset");
        }
        if (cause == this) {
            throw new IllegalArgumentException("Exception cannot be its own cause");
        }
        this.causeSet = true;
        this.cause = cause;
        return this;
    }

    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        if (JaxenException.javaVersion < 1.4 && this.getCause() != null) {
            s.print("Caused by: ");
            this.getCause().printStackTrace(s);
        }
    }

    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        if (JaxenException.javaVersion < 1.4 && this.getCause() != null) {
            s.print("Caused by: ");
            this.getCause().printStackTrace(s);
        }
    }
}

