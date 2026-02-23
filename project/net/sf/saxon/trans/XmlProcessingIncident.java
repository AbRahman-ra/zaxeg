/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.util.Objects;
import javax.xml.transform.TransformerException;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XmlProcessingError;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.tree.util.Navigator;

public class XmlProcessingIncident
implements XmlProcessingError {
    private String message;
    private String errorCode;
    private Throwable cause;
    private Location locator = null;
    private boolean isWarning;
    private boolean isTypeError;
    private String fatalErrorMessage;
    private boolean hasBeenReported = false;
    private HostLanguage hostLanguage;
    private boolean isStaticError;

    public XmlProcessingIncident(String message, String errorCode, Location location) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(errorCode);
        Objects.requireNonNull(location);
        this.message = message;
        this.setErrorCodeAsEQName(errorCode);
        this.locator = location;
        this.isWarning = false;
    }

    public XmlProcessingIncident(String message) {
        this.message = message;
    }

    public XmlProcessingIncident(String message, String errorCode) {
        this.message = message;
        this.setErrorCodeAsEQName(errorCode);
    }

    public XmlProcessingIncident(TransformerException err, boolean isWarning) {
        XPathException exception = XPathException.makeXPathException(err);
        this.message = exception.getMessage();
        this.errorCode = exception.getErrorCodeQName().getEQName();
        this.locator = exception.getLocator();
        this.isWarning = isWarning;
    }

    public void setWarning(boolean warning) {
        this.isWarning = warning;
    }

    @Override
    public XmlProcessingIncident asWarning() {
        this.isWarning = true;
        return this;
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
        return this.hasBeenReported;
    }

    @Override
    public void setAlreadyReported(boolean reported) {
        this.hasBeenReported = reported;
    }

    @Override
    public HostLanguage getHostLanguage() {
        return this.hostLanguage;
    }

    public void setHostLanguage(HostLanguage language) {
        this.hostLanguage = language;
    }

    @Override
    public boolean isTypeError() {
        return this.isTypeError;
    }

    public void setTypeError(boolean isTypeError) {
        this.isTypeError = isTypeError;
    }

    @Override
    public boolean isStaticError() {
        return this.isStaticError;
    }

    public void setStaticError(boolean isStaticError) {
        this.isStaticError = isStaticError;
    }

    @Override
    public QName getErrorCode() {
        if (this.errorCode == null) {
            return null;
        }
        return new QName(StructuredQName.fromEQName(this.errorCode));
    }

    public void setErrorCodeAsEQName(String code) {
        this.errorCode = code.startsWith("Q{") ? code : (NameChecker.isValidNCName(code) ? "Q{http://www.w3.org/2005/xqt-errors}" + code : "Q{http://saxon.sf.net/}invalid-error-code");
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String getModuleUri() {
        return this.getLocation().getSystemId();
    }

    @Override
    public Location getLocation() {
        return this.locator;
    }

    public void setLocation(Location loc) {
        this.locator = loc;
    }

    @Override
    public int getColumnNumber() {
        Location locator = this.getLocation();
        if (locator != null) {
            return locator.getColumnNumber();
        }
        return -1;
    }

    @Override
    public int getLineNumber() {
        Location locator = this.getLocation();
        if (locator != null) {
            return locator.getLineNumber();
        }
        return -1;
    }

    @Override
    public String getInstructionName() {
        return ((NodeInfo)this.locator).getDisplayName();
    }

    @Override
    public boolean isWarning() {
        return this.isWarning;
    }

    @Override
    public String getPath() {
        if (this.locator instanceof NodeInfo) {
            return Navigator.getPath((NodeInfo)this.locator);
        }
        return null;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public static void maybeSetHostLanguage(XmlProcessingError error, HostLanguage lang) {
        if (error.getHostLanguage() == null) {
            if (error instanceof XmlProcessingIncident) {
                ((XmlProcessingIncident)error).setHostLanguage(lang);
            } else if (error instanceof XmlProcessingException) {
                ((XmlProcessingException)error).getXPathException().setHostLanguage(lang);
            }
        }
    }

    public static void maybeSetLocation(XmlProcessingError error, Location loc) {
        if (error.getLocation() == null) {
            if (error instanceof XmlProcessingIncident) {
                ((XmlProcessingIncident)error).setLocation(loc);
            } else if (error instanceof XmlProcessingException) {
                ((XmlProcessingException)error).getXPathException().setLocation(loc);
            }
        }
    }
}

