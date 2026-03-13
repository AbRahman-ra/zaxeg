/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.HashSet;
import java.util.Set;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import net.sf.saxon.expr.EarlyEvaluationContext;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.functions.Error;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.StandardDiagnostics;
import net.sf.saxon.lib.StandardLogger;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.AttributeLocation;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import org.xml.sax.SAXException;

public class StandardErrorListener
extends StandardDiagnostics
implements ErrorListener {
    private int warningCount = 0;
    private int maximumNumberOfWarnings = 25;
    private int maxOrdinaryCharacter = 255;
    private int stackTraceDetail = 2;
    private Set<String> warningsIssued = new HashSet<String>();
    protected transient Logger logger = new StandardLogger();

    public StandardErrorListener makeAnother(HostLanguage hostLanguage) {
        StandardErrorListener sel;
        try {
            sel = (StandardErrorListener)this.getClass().newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            sel = new StandardErrorListener();
        }
        sel.logger = this.logger;
        return sel;
    }

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

    public void setStackTraceDetail(int level) {
        this.stackTraceDetail = level;
    }

    public int getStackTraceDetail() {
        return this.stackTraceDetail;
    }

    public void setMaxOrdinaryCharacter(int max) {
        this.maxOrdinaryCharacter = max;
    }

    public int getMaxOrdinaryCharacter(int max) {
        return this.maxOrdinaryCharacter;
    }

    @Override
    public void warning(TransformerException exception) {
        XPathException xe;
        String message;
        if (this.logger == null) {
            this.logger = new StandardLogger();
        }
        if (!this.warningsIssued.contains(message = this.constructMessage(exception, xe = XPathException.makeXPathException(exception), "", "Warning "))) {
            if (exception instanceof ValidationException) {
                this.logger.error(message);
            } else {
                this.logger.warning(message);
                ++this.warningCount;
                if (this.warningCount > this.getMaximumNumberOfWarnings()) {
                    this.logger.info("No more warnings will be displayed");
                    this.warningCount = 0;
                }
            }
            this.warningsIssued.add(message);
        }
    }

    public boolean isReportingWarnings() {
        return true;
    }

    @Override
    public void error(TransformerException exception) {
        String message;
        if (this.logger == null) {
            this.logger = new StandardLogger();
        }
        if (exception instanceof ValidationException) {
            String explanation = this.getExpandedMessage(exception);
            ValidationFailure failure = ((ValidationException)exception).getValidationFailure();
            String constraintReference = failure.getConstraintReferenceMessage();
            String validationLocation = failure.getValidationLocationText();
            String contextLocation = failure.getContextLocationText();
            message = "Validation error " + this.getLocationMessage(exception) + "\n  " + this.wordWrap(explanation) + this.wordWrap(validationLocation.isEmpty() ? "" : "\n  " + validationLocation) + this.wordWrap(contextLocation.isEmpty() ? "" : "\n  " + contextLocation) + this.wordWrap(constraintReference == null ? "" : "\n  " + constraintReference) + this.formatListOfOffendingNodes(failure);
        } else {
            String prefix = "Error ";
            message = this.constructMessage(exception, XPathException.makeXPathException(exception), "", prefix);
        }
        if (exception instanceof ValidationException) {
            this.logger.error(message);
        } else {
            this.logger.error(message);
            this.logger.info("Processing terminated because error recovery is disabled");
        }
    }

    @Override
    public void fatalError(TransformerException exception) {
        XPathContext context;
        XPathException xe = XPathException.makeXPathException(exception);
        if (xe.hasBeenReported()) {
            return;
        }
        if (this.logger == null) {
            this.logger = new StandardLogger();
        }
        String lang = xe.getHostLanguage();
        String langText = "";
        if ("XPath".equals(lang)) {
            langText = "in expression ";
        } else if ("XQuery".equals(lang)) {
            langText = "in query ";
        } else if ("XSLT Pattern".equals(lang)) {
            langText = "in pattern ";
        }
        String kind = "Error ";
        if (xe.isSyntaxError()) {
            kind = "Syntax error ";
        } else if (xe.isStaticError()) {
            kind = "Static error ";
        } else if (xe.isTypeError()) {
            kind = "Type error ";
        }
        String message = this.constructMessage(exception, xe, langText, kind);
        this.logger.error(message);
        if (exception instanceof XPathException) {
            ((XPathException)exception).setHasBeenReported(true);
        }
        if (exception instanceof XPathException && (context = ((XPathException)exception).getXPathContext()) != null && !(context instanceof EarlyEvaluationContext)) {
            this.outputStackTrace(this.logger, context);
        }
    }

    public String constructMessage(TransformerException exception, XPathException xe, String langText, String kind) {
        return this.constructFirstLine(exception, xe, langText, kind) + "\n  " + this.constructSecondLine(exception, xe);
    }

    public String constructFirstLine(TransformerException exception, XPathException xe, String langText, String kind) {
        Expression failingExpression = null;
        if (exception instanceof XPathException) {
            failingExpression = ((XPathException)exception).getFailingExpression();
        }
        if (xe.getLocator() instanceof AttributeLocation) {
            return kind + langText + this.getLocationMessageText(xe.getLocator());
        }
        if (xe.getLocator() instanceof XPathParser.NestedLocation) {
            String lineInfo;
            XPathParser.NestedLocation nestedLoc = (XPathParser.NestedLocation)xe.getLocator();
            Location outerLoc = nestedLoc.getContainingLocation();
            int line = nestedLoc.getLocalLineNumber();
            int column = nestedLoc.getColumnNumber();
            String string = lineInfo = line <= 0 ? "" : "on line " + line + ' ';
            String columnInfo = column < 0 ? "" : "at " + (line <= 0 ? "char " : "column ") + column + ' ';
            String nearBy = nestedLoc.getNearbyText();
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
        if (xe instanceof ValidationException) {
            return "Validation error " + this.getLocationMessage(exception);
        }
        return kind + (failingExpression != null ? "evaluating (" + failingExpression.toShortString() + ") " : "") + this.getLocationMessage(exception);
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

    public String constructSecondLine(TransformerException err, XPathException xe) {
        if (xe instanceof ValidationException) {
            String explanation = this.getExpandedMessage(err);
            ValidationFailure failure = ((ValidationException)xe).getValidationFailure();
            String constraintReference = failure.getConstraintReferenceMessage();
            if (constraintReference != null) {
                explanation = explanation + " (" + constraintReference + ')';
            }
            return this.wordWrap(explanation + this.formatListOfOffendingNodes(failure));
        }
        return this.expandSpecialCharacters(this.wordWrap(this.getExpandedMessage(err))).toString();
    }

    protected void outputStackTrace(Logger out, XPathContext context) {
        this.printStackTrace(context, out, this.stackTraceDetail);
    }

    public String getLocationMessage(TransformerException err) {
        SourceLocator loc = err.getLocator();
        while (loc == null) {
            if (err.getException() instanceof TransformerException) {
                err = (TransformerException)err.getException();
                loc = err.getLocator();
                continue;
            }
            if (err.getCause() instanceof TransformerException) {
                err = (TransformerException)err.getCause();
                loc = err.getLocator();
                continue;
            }
            return "";
        }
        return this.getLocationMessageText(loc);
    }

    public String getExpandedMessage(TransformerException err) {
        String errorObjectDesc;
        Sequence errorObject;
        String message = this.formatErrorCode(err);
        if (err instanceof XPathException && (errorObject = ((XPathException)err).getErrorObject()) != null && (errorObjectDesc = this.formatErrorObject(errorObject)) != null) {
            message = message + " " + errorObjectDesc;
        }
        message = this.formatNestedMessages(err, message);
        return message;
    }

    public String formatNestedMessages(TransformerException err, String message) {
        Throwable e = err;
        while (e != null) {
            String next = e.getMessage();
            if (next == null) {
                next = "";
            }
            if (next.startsWith("net.sf.saxon.trans.XPathException: ")) {
                next = next.substring(next.indexOf(": ") + 2);
            }
            if (!"TRaX Transform Exception".equals(next) && !message.endsWith(next)) {
                if (!"".equals(message) && !message.trim().endsWith(":")) {
                    message = message + ": ";
                }
                message = message + next;
            }
            if (e instanceof TransformerException) {
                e = e.getException();
                continue;
            }
            if (!(e instanceof SAXException)) break;
            e = ((SAXException)e).getException();
        }
        return message;
    }

    public String formatErrorCode(TransformerException err) {
        StructuredQName qCode = null;
        if (err instanceof XPathException) {
            qCode = ((XPathException)err).getErrorCodeQName();
        }
        if (qCode == null && err.getException() instanceof XPathException) {
            qCode = ((XPathException)err.getException()).getErrorCodeQName();
        }
        String message = "";
        if (qCode != null) {
            message = qCode.hasURI("http://www.w3.org/2005/xqt-errors") ? qCode.getLocalPart() : qCode.getDisplayName();
        }
        return message;
    }

    public String formatErrorObject(Sequence errorObject) {
        return null;
    }

    public CharSequence expandSpecialCharacters(CharSequence in) {
        if (this.logger.isUnicodeAware()) {
            return in;
        }
        return this.expandSpecialCharacters(in, this.maxOrdinaryCharacter);
    }
}

