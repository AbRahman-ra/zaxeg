/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.io.StringWriter;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.XMLEmitter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingIncident;

public class MessageWarner
extends XMLEmitter {
    private boolean abort = false;
    private String errorCode = null;

    @Override
    public void startDocument(int properties) throws XPathException {
        this.setWriter(new StringWriter());
        this.abort = ReceiverOption.contains(properties, 16384);
        super.startDocument(properties);
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        if (target.equals("error-code")) {
            this.errorCode = data.toString();
        } else {
            super.processingInstruction(target, data, locationId, properties);
        }
    }

    @Override
    public void endDocument() throws XPathException {
        ErrorReporter reporter = this.getPipelineConfiguration().getErrorReporter();
        XmlProcessingIncident de = new XmlProcessingIncident(this.getWriter().toString(), this.errorCode == null ? "XTMM9000" : this.errorCode);
        if (!this.abort) {
            de = de.asWarning();
        }
        reporter.report(de);
    }

    @Override
    public void close() {
    }
}

