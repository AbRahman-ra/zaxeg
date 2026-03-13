/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto;

import java.io.PrintStream;
import java.io.PrintWriter;
import javax.xml.crypto.URIReference;

public class URIReferenceException
extends Exception {
    private static final long serialVersionUID = 7173469703932561419L;
    private Throwable cause;
    private URIReference uriReference;

    public URIReferenceException() {
    }

    public URIReferenceException(String message) {
        super(message);
    }

    public URIReferenceException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }

    public URIReferenceException(String message, Throwable cause, URIReference uriReference) {
        this(message, cause);
        if (uriReference == null) {
            throw new NullPointerException("uriReference cannot be null");
        }
        this.uriReference = uriReference;
    }

    public URIReferenceException(Throwable cause) {
        super(cause == null ? null : cause.toString());
        this.cause = cause;
    }

    public URIReference getURIReference() {
        return this.uriReference;
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

