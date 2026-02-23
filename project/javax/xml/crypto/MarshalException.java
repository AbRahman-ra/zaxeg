/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto;

import java.io.PrintStream;
import java.io.PrintWriter;

public class MarshalException
extends Exception {
    private static final long serialVersionUID = -863185580332643547L;
    private Throwable cause;

    public MarshalException() {
    }

    public MarshalException(String message) {
        super(message);
    }

    public MarshalException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }

    public MarshalException(Throwable cause) {
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

