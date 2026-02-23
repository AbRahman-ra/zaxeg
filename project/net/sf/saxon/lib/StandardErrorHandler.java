/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.Objects;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.trans.XmlProcessingIncident;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class StandardErrorHandler
implements ErrorHandler {
    private ErrorReporter errorReporter;
    private int warningCount = 0;
    private int errorCount = 0;
    private int fatalErrorCount = 0;
    private boolean silent = false;

    public StandardErrorHandler(ErrorReporter reporter) {
        this.errorReporter = Objects.requireNonNull(reporter);
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    @Override
    public void warning(SAXParseException e) {
        try {
            ++this.warningCount;
            if (!this.silent) {
                this.errorReporter.report(new XmlProcessingException(XPathException.makeXPathException(e)).asWarning());
            }
        } catch (Exception exception) {
            // empty catch block
        }
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        ++this.errorCount;
        if (!this.silent) {
            this.reportError(e, false);
        }
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        ++this.fatalErrorCount;
        if (!this.silent) {
            this.reportError(e, true);
        }
        throw e;
    }

    protected void reportError(SAXParseException e, boolean isFatal) {
        try {
            Loc loc = new Loc(e.getSystemId(), e.getLineNumber(), e.getColumnNumber());
            if (this.errorReporter != null) {
                XmlProcessingIncident err = new XmlProcessingIncident(" Error reported by XML parser: " + e.getMessage(), "SXXP0003", loc);
                err.setCause(e);
                this.errorReporter.report(err);
            }
        } catch (Exception unexpected) {
            throw new AssertionError((Object)unexpected);
        }
    }

    public int getWarningCount() {
        return this.warningCount;
    }

    public int getErrorCount() {
        return this.errorCount;
    }

    public int getFatalErrorCount() {
        return this.fatalErrorCount;
    }
}

