/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XmlProcessingError;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.AttributeLocation;

public class XmlProcessingException
implements XmlProcessingError {
    private XPathException exception;
    private boolean isWarning;
    private String fatalErrorMessage;

    public XmlProcessingException(XPathException exception) {
        this.exception = exception;
    }

    public XPathException getXPathException() {
        return this.exception;
    }

    @Override
    public HostLanguage getHostLanguage() {
        Location loc = this.getLocation();
        if (loc instanceof Instruction || loc instanceof AttributeLocation) {
            return HostLanguage.XSLT;
        }
        return HostLanguage.XPATH;
    }

    @Override
    public boolean isStaticError() {
        return this.exception.isStaticError();
    }

    @Override
    public boolean isTypeError() {
        return this.exception.isTypeError();
    }

    @Override
    public QName getErrorCode() {
        StructuredQName errorCodeQName = this.exception.getErrorCodeQName();
        return errorCodeQName == null ? null : new QName(errorCodeQName);
    }

    @Override
    public String getMessage() {
        return this.exception.getMessage();
    }

    @Override
    public Location getLocation() {
        return this.exception.getLocator();
    }

    @Override
    public boolean isWarning() {
        return this.isWarning;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public Throwable getCause() {
        return this.exception.getCause();
    }

    public void setWarning(boolean warning) {
        this.isWarning = warning;
    }

    @Override
    public XmlProcessingException asWarning() {
        XmlProcessingException e2 = new XmlProcessingException(this.exception);
        e2.setWarning(true);
        return e2;
    }

    @Override
    public void setFatal(String message) {
        this.fatalErrorMessage = message;
    }

    @Override
    public String getFatalErrorMessage() {
        return this.fatalErrorMessage;
    }

    @Override
    public boolean isAlreadyReported() {
        return this.exception.hasBeenReported();
    }

    @Override
    public void setAlreadyReported(boolean reported) {
        this.exception.setHasBeenReported(reported);
    }
}

