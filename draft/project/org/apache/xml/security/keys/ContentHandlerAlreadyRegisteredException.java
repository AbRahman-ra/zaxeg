/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys;

import org.apache.xml.security.exceptions.XMLSecurityException;

public class ContentHandlerAlreadyRegisteredException
extends XMLSecurityException {
    private static final long serialVersionUID = 1L;

    public ContentHandlerAlreadyRegisteredException() {
    }

    public ContentHandlerAlreadyRegisteredException(String msgID) {
        super(msgID);
    }

    public ContentHandlerAlreadyRegisteredException(String msgID, Object[] exArgs) {
        super(msgID, exArgs);
    }

    public ContentHandlerAlreadyRegisteredException(String msgID, Exception originalException) {
        super(msgID, originalException);
    }

    public ContentHandlerAlreadyRegisteredException(String msgID, Object[] exArgs, Exception originalException) {
        super(msgID, exArgs, originalException);
    }
}

