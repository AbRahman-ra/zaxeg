/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.Objects;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.s9api.XmlProcessingError;
import net.sf.saxon.trans.XPathException;

public class ErrorReporterToListener
implements ErrorReporter {
    private ErrorListener listener;

    public ErrorReporterToListener(ErrorListener listener) {
        this.listener = Objects.requireNonNull(listener);
    }

    public ErrorListener getErrorListener() {
        return this.listener;
    }

    @Override
    public void report(XmlProcessingError error) {
        if (!error.isAlreadyReported()) {
            try {
                XPathException err = XPathException.fromXmlProcessingError(error);
                if (error.isWarning()) {
                    this.listener.warning(err);
                } else {
                    this.listener.fatalError(err);
                }
                error.setAlreadyReported(true);
            } catch (TransformerException e) {
                error.setFatal(e.getMessage());
            }
        }
    }
}

