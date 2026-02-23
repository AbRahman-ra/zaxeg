/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto;

import java.io.PrintStream;
import java.io.PrintWriter;

public class KeySelectorException
extends Exception {
    private static final long serialVersionUID = -7480033639322531109L;
    private Throwable cause;

    public KeySelectorException() {
    }

    public KeySelectorException(String message) {
        super(message);
    }

    public KeySelectorException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }

    public KeySelectorException(Throwable cause) {
        super(cause == null ? null : cause.toString());
        this.cause = cause;
    }

    public Throwable getCause() {
        return this.cause;
    }

    public void printStackTrace() {
        super.printStackTrace();
        if (this.cause != null) {
            this.cause.printStackTrace();
        }
    }

    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        if (this.cause != null) {
            this.cause.printStackTrace(s);
        }
    }

    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        if (this.cause != null) {
            this.cause.printStackTrace(s);
        }
    }
}

