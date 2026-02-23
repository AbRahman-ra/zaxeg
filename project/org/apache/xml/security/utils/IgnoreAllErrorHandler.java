/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class IgnoreAllErrorHandler
implements ErrorHandler {
    private static Log log = LogFactory.getLog(IgnoreAllErrorHandler.class);
    private static final boolean warnOnExceptions = System.getProperty("org.apache.xml.security.test.warn.on.exceptions", "false").equals("true");
    private static final boolean throwExceptions = System.getProperty("org.apache.xml.security.test.throw.exceptions", "false").equals("true");

    public void warning(SAXParseException ex) throws SAXException {
        if (warnOnExceptions) {
            log.warn("", ex);
        }
        if (throwExceptions) {
            throw ex;
        }
    }

    public void error(SAXParseException ex) throws SAXException {
        if (warnOnExceptions) {
            log.error("", ex);
        }
        if (throwExceptions) {
            throw ex;
        }
    }

    public void fatalError(SAXParseException ex) throws SAXException {
        if (warnOnExceptions) {
            log.warn("", ex);
        }
        if (throwExceptions) {
            throw ex;
        }
    }
}

