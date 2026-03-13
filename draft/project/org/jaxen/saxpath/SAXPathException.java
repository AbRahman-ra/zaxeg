/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.saxpath;

import java.io.PrintStream;
import java.io.PrintWriter;

public class SAXPathException
extends Exception {
    private static final long serialVersionUID = 4826444568928720706L;
    private static double javaVersion = 1.4;
    private Throwable cause;
    private boolean causeSet = false;

    public SAXPathException(String message) {
        super(message);
    }

    public SAXPathException(Throwable cause) {
        super(cause.getMessage());
        this.initCause(cause);
    }

    public SAXPathException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
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
        if (javaVersion < 1.4 && this.getCause() != null) {
            s.print("Caused by: ");
            this.getCause().printStackTrace(s);
        }
    }

    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        if (javaVersion < 1.4 && this.getCause() != null) {
            s.print("Caused by: ");
            this.getCause().printStackTrace(s);
        }
    }

    static {
        try {
            String versionString = System.getProperty("java.version");
            versionString = versionString.substring(0, 3);
            javaVersion = Double.valueOf(versionString);
        } catch (Exception exception) {
            // empty catch block
        }
    }
}

