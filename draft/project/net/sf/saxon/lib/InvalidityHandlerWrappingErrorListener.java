/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import net.sf.saxon.lib.Invalidity;
import net.sf.saxon.lib.InvalidityHandler;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ValidationFailure;

public class InvalidityHandlerWrappingErrorListener
implements InvalidityHandler {
    private ErrorListener errorListener;

    public InvalidityHandlerWrappingErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    @Override
    public void startReporting(String systemId) throws XPathException {
    }

    @Override
    public void reportInvalidity(Invalidity failure) throws XPathException {
        try {
            this.errorListener.error(((ValidationFailure)failure).makeException());
        } catch (TransformerException e) {
            throw XPathException.makeXPathException(e);
        }
    }

    public ErrorListener getErrorListener() {
        return this.errorListener;
    }

    @Override
    public Sequence endReporting() {
        return null;
    }
}

