/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.keyresolver;

import org.apache.xml.security.exceptions.XMLSecurityException;

public class InvalidKeyResolverException
extends XMLSecurityException {
    private static final long serialVersionUID = 1L;

    public InvalidKeyResolverException() {
    }

    public InvalidKeyResolverException(String msgID) {
        super(msgID);
    }

    public InvalidKeyResolverException(String msgID, Object[] exArgs) {
        super(msgID, exArgs);
    }

    public InvalidKeyResolverException(String msgID, Exception originalException) {
        super(msgID, originalException);
    }

    public InvalidKeyResolverException(String msgID, Object[] exArgs, Exception originalException) {
        super(msgID, exArgs, originalException);
    }
}

