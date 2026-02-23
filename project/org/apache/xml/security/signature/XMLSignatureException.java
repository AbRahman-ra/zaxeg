/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.signature;

import org.apache.xml.security.exceptions.XMLSecurityException;

public class XMLSignatureException
extends XMLSecurityException {
    private static final long serialVersionUID = 1L;

    public XMLSignatureException() {
    }

    public XMLSignatureException(String msgID) {
        super(msgID);
    }

    public XMLSignatureException(String msgID, Object[] exArgs) {
        super(msgID, exArgs);
    }

    public XMLSignatureException(String msgID, Exception originalException) {
        super(msgID, originalException);
    }

    public XMLSignatureException(String msgID, Object[] exArgs, Exception originalException) {
        super(msgID, exArgs, originalException);
    }
}

