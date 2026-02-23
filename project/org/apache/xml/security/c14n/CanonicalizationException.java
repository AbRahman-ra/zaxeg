/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.c14n;

import org.apache.xml.security.exceptions.XMLSecurityException;

public class CanonicalizationException
extends XMLSecurityException {
    private static final long serialVersionUID = 1L;

    public CanonicalizationException() {
    }

    public CanonicalizationException(String msgID) {
        super(msgID);
    }

    public CanonicalizationException(String msgID, Object[] exArgs) {
        super(msgID, exArgs);
    }

    public CanonicalizationException(String msgID, Exception originalException) {
        super(msgID, originalException);
    }

    public CanonicalizationException(String msgID, Object[] exArgs, Exception originalException) {
        super(msgID, exArgs, originalException);
    }
}

