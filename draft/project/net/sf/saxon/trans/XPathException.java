/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XmlProcessingError;
import net.sf.saxon.trans.XmlProcessingException;

public class XPathException
extends TransformerException {
    private boolean isTypeError = false;
    private boolean isSyntaxError = false;
    private boolean isStaticError = false;
    private boolean isGlobalError = false;
    private String hostLanguage = null;
    private StructuredQName errorCode;
    private Sequence errorObject;
    private Expression failingExpression;
    private boolean hasBeenReported = false;
    transient XPathContext context;

    public XPathException(String message) {
        super(message);
        XPathException.breakPoint();
    }

    public XPathException(Throwable err) {
        super(err);
        XPathException.breakPoint();
    }

    public XPathException(String message, Throwable err) {
        super(message, err);
        XPathException.breakPoint();
    }

    public XPathException(String message, String errorCode, Location loc) {
        this(message, errorCode);
        this.setLocator(loc);
        XPathException.breakPoint();
    }

    public XPathException(String message, Location loc, Throwable err) {
        super(message, loc, err);
        XPathException.breakPoint();
    }

    public XPathException(String message, String errorCode) {
        super(message);
        this.setErrorCode(errorCode);
        XPathException.breakPoint();
    }

    public XPathException(String message, String errorCode, XPathContext context) {
        super(message);
        this.setErrorCode(errorCode);
        this.setXPathContext(context);
        XPathException.breakPoint();
    }

    private static void breakPoint() {
    }

    public static XPathException makeXPathException(Exception err) {
        if (err instanceof XPathException) {
            return (XPathException)err;
        }
        if (err.getCause() instanceof XPathException) {
            return (XPathException)err.getCause();
        }
        if (err instanceof TransformerException) {
            XPathException xe = new XPathException(err.getMessage(), err);
            xe.setLocator(((TransformerException)err).getLocator());
            return xe;
        }
        return new XPathException(err);
    }

    public static XPathException fromXmlProcessingError(XmlProcessingError error) {
        if (error instanceof XmlProcessingException) {
            return ((XmlProcessingException)error).getXPathException();
        }
        XPathException e = new XPathException(error.getMessage());
        e.setLocation(error.getLocation());
        e.setHostLanguage(error.getHostLanguage());
        e.setIsStaticError(error.isStaticError());
        e.setIsTypeError(error.isTypeError());
        QName code = error.getErrorCode();
        if (code != null) {
            e.setErrorCodeQName(code.getStructuredQName());
        }
        return e;
    }

    public void setXPathContext(XPathContext context) {
        this.context = context;
    }

    public void setLocation(Location loc) {
        if (loc != null) {
            this.setLocator(loc.saveLocation());
        }
    }

    public Expression getFailingExpression() {
        return this.failingExpression;
    }

    public void setFailingExpression(Expression failingExpression) {
        this.failingExpression = failingExpression;
    }

    public void maybeSetFailingExpression(Expression failingExpression) {
        if (this.failingExpression == null) {
            this.failingExpression = failingExpression;
        }
        this.maybeSetLocation(failingExpression.getLocation());
    }

    @Override
    public Location getLocator() {
        SourceLocator locator = super.getLocator();
        if (locator == null) {
            return null;
        }
        if (locator instanceof Location) {
            return (Location)locator;
        }
        return new Loc(locator);
    }

    public XPathContext getXPathContext() {
        return this.context;
    }

    public void setIsStaticError(boolean is) {
        this.isStaticError = is;
    }

    public boolean isStaticError() {
        return this.isStaticError;
    }

    public void setIsSyntaxError(boolean is) {
        if (is) {
            this.isStaticError = true;
        }
        this.isSyntaxError = is;
    }

    public boolean isSyntaxError() {
        return this.isSyntaxError;
    }

    public void setIsTypeError(boolean is) {
        this.isTypeError = is;
    }

    public boolean isTypeError() {
        return this.isTypeError;
    }

    public void setIsGlobalError(boolean is) {
        this.isGlobalError = is;
    }

    public boolean isGlobalError() {
        return this.isGlobalError;
    }

    public void setHostLanguage(String language) {
        this.hostLanguage = language;
    }

    public void setHostLanguage(HostLanguage language) {
        this.hostLanguage = language == null ? null : language.toString();
    }

    public String getHostLanguage() {
        return this.hostLanguage;
    }

    public void setErrorCode(String code) {
        if (code != null) {
            this.errorCode = new StructuredQName("err", "http://www.w3.org/2005/xqt-errors", code);
        }
    }

    public void maybeSetErrorCode(String code) {
        if (this.errorCode == null && code != null) {
            this.errorCode = new StructuredQName("err", "http://www.w3.org/2005/xqt-errors", code);
        }
    }

    public void setErrorCodeQName(StructuredQName code) {
        this.errorCode = code;
    }

    public StructuredQName getErrorCodeQName() {
        return this.errorCode;
    }

    public String getErrorCodeLocalPart() {
        return this.errorCode == null ? null : this.errorCode.getLocalPart();
    }

    public String getErrorCodeNamespace() {
        return this.errorCode == null ? null : this.errorCode.getURI();
    }

    public void setErrorObject(Sequence value) {
        this.errorObject = value;
    }

    public Sequence getErrorObject() {
        return this.errorObject;
    }

    public void setHasBeenReported(boolean reported) {
        this.hasBeenReported = reported;
    }

    public boolean hasBeenReported() {
        return this.hasBeenReported;
    }

    public void maybeSetLocation(Location here) {
        if (here != null) {
            if (this.getLocator() == null) {
                this.setLocator(here.saveLocation());
            } else if (this.getLocator().getLineNumber() == -1 && (this.getLocator().getSystemId() == null || here.getSystemId() == null || this.getLocator().getSystemId().equals(here.getSystemId()))) {
                this.setLocator(here.saveLocation());
            }
        }
    }

    public void maybeSetContext(XPathContext context) {
        if (this.getXPathContext() == null) {
            this.setXPathContext(context);
        }
    }

    public boolean isReportableStatically() {
        if (this.isStaticError() || this.isTypeError()) {
            return true;
        }
        StructuredQName err = this.errorCode;
        if (err != null && err.hasURI("http://www.w3.org/2005/xqt-errors")) {
            String local = err.getLocalPart();
            return local.equals("XTDE1260") || local.equals("XTDE1280") || local.equals("XTDE1390") || local.equals("XTDE1400") || local.equals("XTDE1428") || local.equals("XTDE1440") || local.equals("XTDE1460");
        }
        return false;
    }

    public static class StackOverflow
    extends XPathException {
        public StackOverflow(String message, String errorCode, Location location) {
            super(message, errorCode, location);
        }
    }

    public static class Circularity
    extends XPathException {
        public Circularity(String message) {
            super(message);
        }
    }
}

