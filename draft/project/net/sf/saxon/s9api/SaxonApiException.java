/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.trans.XPathException;

public class SaxonApiException
extends Exception {
    public SaxonApiException(Throwable cause) {
        super(cause);
    }

    public SaxonApiException(String message) {
        super(new XPathException(message));
    }

    public SaxonApiException(String message, Throwable cause) {
        super(new XPathException(message, cause));
    }

    @Override
    public String getMessage() {
        return this.getCause().getMessage();
    }

    public QName getErrorCode() {
        Throwable cause = this.getCause();
        if (cause instanceof XPathException) {
            StructuredQName code = ((XPathException)cause).getErrorCodeQName();
            return code == null ? null : new QName(code);
        }
        return null;
    }

    public int getLineNumber() {
        Throwable cause = this.getCause();
        if (cause instanceof XPathException) {
            Location loc = ((XPathException)cause).getLocator();
            return loc == null ? -1 : loc.getLineNumber();
        }
        return -1;
    }

    public String getSystemId() {
        Throwable cause = this.getCause();
        if (cause instanceof XPathException) {
            Location loc = ((XPathException)cause).getLocator();
            return loc == null ? null : loc.getSystemId();
        }
        return null;
    }
}

