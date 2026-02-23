/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig;

import java.io.PrintStream;
import java.io.PrintWriter;

public class XMLSignatureException
extends Exception {
    private static final long serialVersionUID = -3438102491013869995L;
    private Throwable cause;

    public XMLSignatureException() {
    }

    public XMLSignatureException(String message) {
        super(message);
    }

    public XMLSignatureException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }

    public XMLSignatureException(Throwable cause) {
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

