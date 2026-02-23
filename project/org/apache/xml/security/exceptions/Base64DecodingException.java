/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.exceptions;

import org.apache.xml.security.exceptions.XMLSecurityException;

public class Base64DecodingException
extends XMLSecurityException {
    private static final long serialVersionUID = 1L;

    public Base64DecodingException() {
    }

    public Base64DecodingException(String msgID) {
        super(msgID);
    }

    public Base64DecodingException(String msgID, Object[] exArgs) {
        super(msgID, exArgs);
    }

    public Base64DecodingException(String msgID, Exception originalException) {
        super(msgID, originalException);
    }

    public Base64DecodingException(String msgID, Object[] exArgs, Exception originalException) {
        super(msgID, exArgs, originalException);
    }
}

