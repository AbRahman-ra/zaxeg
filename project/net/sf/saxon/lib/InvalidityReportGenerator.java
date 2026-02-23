/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import javax.xml.stream.XMLStreamWriter;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.lib.Invalidity;
import net.sf.saxon.lib.StandardInvalidityHandler;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public class InvalidityReportGenerator
extends StandardInvalidityHandler {
    public static final String REPORT_NS = "http://saxon.sf.net/ns/validation";

    public InvalidityReportGenerator(Configuration config) {
        super(config);
    }

    public InvalidityReportGenerator(Configuration config, Outputter receiver) throws XPathException {
        super(config);
    }

    public void setReceiver(Outputter receiver) {
    }

    public void setSystemId(String id) {
    }

    public void setSchemaName(String name) {
    }

    public int getErrorCount() {
        return 0;
    }

    public int getWarningCount() {
        return 0;
    }

    public void setXsdVersion(String version) {
    }

    public XMLStreamWriter getWriter() {
        return null;
    }

    @Override
    public void reportInvalidity(Invalidity failure) throws XPathException {
    }

    @Override
    public void startReporting(String systemId) throws XPathException {
    }

    @Override
    public Sequence endReporting() throws XPathException {
        return null;
    }

    public void createMetaData() throws XPathException {
    }
}

