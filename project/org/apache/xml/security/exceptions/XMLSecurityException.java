/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import org.apache.xml.security.utils.I18n;

public class XMLSecurityException
extends Exception {
    private static final long serialVersionUID = 1L;
    protected String msgID;

    public XMLSecurityException() {
        super("Missing message string");
        this.msgID = null;
    }

    public XMLSecurityException(String msgID) {
        super(I18n.getExceptionMessage(msgID));
        this.msgID = msgID;
    }

    public XMLSecurityException(String msgID, Object[] exArgs) {
        super(MessageFormat.format(I18n.getExceptionMessage(msgID), exArgs));
        this.msgID = msgID;
    }

    public XMLSecurityException(Exception originalException) {
        super("Missing message ID to locate message string in resource bundle \"org/apache/xml/security/resource/xmlsecurity\". Original Exception was a " + originalException.getClass().getName() + " and message " + originalException.getMessage(), originalException);
    }

    public XMLSecurityException(String msgID, Exception originalException) {
        super(I18n.getExceptionMessage(msgID, originalException), originalException);
        this.msgID = msgID;
    }

    public XMLSecurityException(String msgID, Object[] exArgs, Exception originalException) {
        super(MessageFormat.format(I18n.getExceptionMessage(msgID), exArgs), originalException);
        this.msgID = msgID;
    }

    public String getMsgID() {
        if (this.msgID == null) {
            return "Missing message ID";
        }
        return this.msgID;
    }

    public String toString() {
        String s = this.getClass().getName();
        String message = super.getLocalizedMessage();
        message = message != null ? s + ": " + message : s;
        if (super.getCause() != null) {
            message = message + "\nOriginal Exception was " + super.getCause().toString();
        }
        return message;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void printStackTrace() {
        PrintStream printStream = System.err;
        synchronized (printStream) {
            super.printStackTrace(System.err);
        }
    }

    public void printStackTrace(PrintWriter printwriter) {
        super.printStackTrace(printwriter);
    }

    public void printStackTrace(PrintStream printstream) {
        super.printStackTrace(printstream);
    }

    public Exception getOriginalException() {
        if (this.getCause() instanceof Exception) {
            return (Exception)this.getCause();
        }
        return null;
    }
}

