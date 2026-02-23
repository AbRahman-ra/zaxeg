/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import net.sf.saxon.expr.EarlyEvaluationContext;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.functions.Error;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.StandardDiagnostics;
import net.sf.saxon.lib.StandardLogger;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XmlProcessingError;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.tree.AttributeLocation;
import org.xml.sax.SAXParseException;

public class StandardErrorReporter
extends StandardDiagnostics
implements ErrorReporter {
    private int warningCount = 0;
    private int maximumNumberOfWarnings = 25;
    private int errorCount = 0;
    private int maximumNumberOfErrors = 1000;
    private int maxOrdinaryCharacter = 255;
    private int stackTraceDetail = 2;
    private Set<String> warningsIssued = new HashSet<String>();
    protected transient Logger logger = new StandardLogger();
    private XmlProcessingError latestError;
    private boolean outputErrorCodes = true;

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void setMaximumNumberOfWarnings(int max) {
        this.maximumNumberOfWarnings = max;
    }

    public int getMaximumNumberOfWarnings() {
        return this.maximumNumberOfWarnings;
    }

    public void setMaximumNumberOfErrors(int max) {
        this.maximumNumberOfErrors = max;
    }

    public int getMaximumNumberOfErrors() {
        return this.maximumNumberOfErrors;
    }

    public void setMaxOrdinaryCharacter(int max) {
        this.maxOrdinaryCharacter = max;
    }

    public int getMaxOrdinaryCharacter(int max) {
        return this.maxOrdinaryCharacter;
    }

    public void setStackTraceDetail(int level) {
        this.stackTraceDetail = level;
    }

    public int getStackTraceDetail() {
        return this.stackTraceDetail;
    }

    public void setOutputErrorCodes(boolean include) {
        this.outputErrorCodes = include;
    }

    @Override
    public void report(XmlProcessingError error) {
        if (error != this.latestError) {
            this.latestError = error;
            if (error.isWarning()) {
                this.warning(error);
            } else {
                this.error(error);
            }
        }
    }

    protected void warning(XmlProcessingError error) {
        String message;
        if (this.logger == null) {
            this.logger = new StandardLogger();
        }
        if (!this.warningsIssued.contains(message = this.constructMessage(error, "", "Warning "))) {
            this.logger.warning(message);
            ++this.warningCount;
            if (this.warningCount > this.getMaximumNumberOfWarnings()) {
                this.logger.info("No more warnings will be displayed");
            }
            this.warningsIssued.add(message);
        }
    }

    public boolean isReportingWarnings() {
        return this.warningCount < this.getMaximumNumberOfWarnings();
    }

    protected void error(XmlProcessingError error) {
        XPathException exception;
        XPathContext context;
        if (this.errorCount++ > this.maximumNumberOfErrors) {
            error.setFatal("Too many errors reported");
        }
        if (this.logger == null) {
            this.logger = new StandardLogger();
        }
        HostLanguage lang = error.getHostLanguage();
        String langText = "";
        if (lang != null) {
            switch (lang) {
                case XSLT: {
                    break;
                }
                case XQUERY: {
                    langText = "in query ";
                    break;
                }
                case XPATH: {
                    langText = "in expression ";
                    break;
                }
                case XML_SCHEMA: {
                    langText = "in schema ";
                    break;
                }
                case XSLT_PATTERN: {
                    langText = "in pattern ";
                }
            }
        }
        String kind = "Error ";
        if (error.isTypeError()) {
            kind = "Type error ";
        } else if (error.isStaticError()) {
            kind = "Static error ";
        }
        String message = this.constructMessage(error, langText, kind);
        this.logger.error(message);
        if (error instanceof XmlProcessingException && (context = (exception = ((XmlProcessingException)error).getXPathException()).getXPathContext()) != null && !(context instanceof EarlyEvaluationContext)) {
            this.outputStackTrace(this.logger, context);
        }
    }

    public String constructMessage(XmlProcessingError exception, String langText, String kind) {
        return this.constructFirstLine(exception, langText, kind) + "\n  " + this.constructSecondLine(exception);
    }

    public String constructFirstLine(XmlProcessingError error, String langText, String kind) {
        Location locator = error.getLocation();
        if (locator instanceof AttributeLocation) {
            return kind + langText + this.getLocationMessageText(locator);
        }
        if (locator instanceof XPathParser.NestedLocation) {
            String lineInfo;
            XPathParser.NestedLocation nestedLoc = (XPathParser.NestedLocation)locator;
            Location outerLoc = nestedLoc.getContainingLocation();
            int line = nestedLoc.getLocalLineNumber();
            int column = nestedLoc.getColumnNumber();
            String string = lineInfo = line <= 0 ? "" : "on line " + line + ' ';
            String columnInfo = column < 0 ? "" : "at " + (line <= 0 ? "char " : "column ") + column + ' ';
            String nearBy = nestedLoc.getNearbyText();
            Expression failingExpression = null;
            String extraContext = this.formatExtraContext(failingExpression, nearBy);
            if (outerLoc instanceof AttributeLocation) {
                String innerLoc = lineInfo + extraContext + columnInfo;
                return kind + innerLoc + langText + this.getLocationMessageText(outerLoc);
            }
            String innerLoc = lineInfo + columnInfo;
            if (outerLoc.getLineNumber() > 1) {
                innerLoc = innerLoc + "(" + langText + "on line " + outerLoc.getLineNumber() + ") ";
            }
            if (outerLoc.getSystemId() != null) {
                innerLoc = innerLoc + "of " + outerLoc.getSystemId() + " ";
            }
            return kind + extraContext + innerLoc;
        }
        return kind + this.getLocationMessage(error);
    }

    public String formatExtraContext(Expression failingExpression, String nearBy) {
        if (failingExpression != null) {
            if (failingExpression.isCallOn(Error.class)) {
                return "signaled by call to error() ";
            }
            return "evaluating (" + failingExpression.toShortString() + ") ";
        }
        if (nearBy != null && !nearBy.isEmpty()) {
            return (nearBy.startsWith("...") ? "near" : "in") + ' ' + Err.wrap(nearBy) + " ";
        }
        return "";
    }

    public String constructSecondLine(XmlProcessingError err) {
        return this.expandSpecialCharacters(this.wordWrap(this.getExpandedMessage(err))).toString();
    }

    protected String getLocationMessage(XmlProcessingError err) {
        Location loc = err.getLocation();
        return this.getLocationMessageText(loc);
    }

    public String getExpandedMessage(XmlProcessingError err) {
        String message = this.formatErrorCode(err) + " " + err.getMessage();
        message = this.formatNestedMessages(err, message);
        return message;
    }

    public String formatNestedMessages(XmlProcessingError err, String message) {
        if (err.getCause() == null) {
            return message;
        }
        StringBuilder sb = new StringBuilder(message);
        for (Throwable e = err.getCause(); e != null; e = e.getCause()) {
            String next;
            if (!(e instanceof SAXParseException)) {
                if (e instanceof RuntimeException) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    sb.append('\n').append(sw);
                } else {
                    sb.append(". Caused by ").append(e.getClass().getName());
                }
            }
            if ((next = e.getMessage()) == null) continue;
            sb.append(": ").append(next);
        }
        return sb.toString();
    }

    public String formatErrorCode(XmlProcessingError err) {
        QName qCode;
        if (this.outputErrorCodes && (qCode = err.getErrorCode()) != null) {
            if (qCode.getNamespaceURI().equals("http://www.w3.org/2005/xqt-errors")) {
                return qCode.getLocalName() + " ";
            }
            return qCode.toString() + " ";
        }
        return "";
    }

    public CharSequence expandSpecialCharacters(CharSequence in) {
        if (this.logger.isUnicodeAware()) {
            return in;
        }
        return this.expandSpecialCharacters(in, this.maxOrdinaryCharacter);
    }

    protected void outputStackTrace(Logger out, XPathContext context) {
        this.printStackTrace(context, out, this.stackTraceDetail);
    }
}

