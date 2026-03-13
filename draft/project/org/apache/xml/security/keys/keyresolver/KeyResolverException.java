/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.keyresolver;

import org.apache.xml.security.exceptions.XMLSecurityException;

public class KeyResolverException
extends XMLSecurityException {
    private static final long serialVersionUID = 1L;

    public KeyResolverException() {
    }

    public KeyResolverException(String msgID) {
        super(msgID);
    }

    public KeyResolverException(String msgID, Object[] exArgs) {
        super(msgID, exArgs);
    }

    public KeyResolverException(String msgID, Exception originalException) {
        super(msgID, originalException);
    }

    public KeyResolverException(String msgID, Object[] exArgs, Exception originalException) {
        super(msgID, exArgs, originalException);
    }
}

