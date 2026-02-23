/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.Invalidity;
import net.sf.saxon.lib.InvalidityHandler;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.type.ValidationFailure;

public class InvalidityHandlerWrappingErrorReporter
implements InvalidityHandler {
    private ErrorReporter errorReporter;

    public InvalidityHandlerWrappingErrorReporter(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    @Override
    public void startReporting(String systemId) throws XPathException {
    }

    @Override
    public void reportInvalidity(Invalidity failure) throws XPathException {
        this.errorReporter.report(new XmlProcessingException(((ValidationFailure)failure).makeException()));
    }

    public ErrorReporter getErrorReporter() {
        return this.errorReporter;
    }

    @Override
    public Sequence endReporting() {
        return null;
    }
}

