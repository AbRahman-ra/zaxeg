/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto;

import java.io.PrintStream;
import java.io.PrintWriter;

public class NoSuchMechanismException
extends RuntimeException {
    private static final long serialVersionUID = 4189669069570660166L;
    private Throwable cause;

    public NoSuchMechanismException() {
    }

    public NoSuchMechanismException(String message) {
        super(message);
    }

    public NoSuchMechanismException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }

    public NoSuchMechanismException(Throwable cause) {
        super(cause == null ? null : cause.toString());
        this.cause = cause;
    }

    public Throwable getCause() {
        return this.cause;
    }

    public void printStackTrace() {
        super.printStackTrace();
        this.cause.printStackTrace();
    }

    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        this.cause.printStackTrace(s);
    }

    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        this.cause.printStackTrace(s);
    }
}

