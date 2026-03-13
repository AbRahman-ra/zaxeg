/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.c14n;

import org.apache.xml.security.exceptions.XMLSecurityException;

public class InvalidCanonicalizerException
extends XMLSecurityException {
    private static final long serialVersionUID = 1L;

    public InvalidCanonicalizerException() {
    }

    public InvalidCanonicalizerException(String msgID) {
        super(msgID);
    }

    public InvalidCanonicalizerException(String msgID, Object[] exArgs) {
        super(msgID, exArgs);
    }

    public InvalidCanonicalizerException(String msgID, Exception originalException) {
        super(msgID, originalException);
    }

    public InvalidCanonicalizerException(String msgID, Object[] exArgs, Exception originalException) {
        super(msgID, exArgs, originalException);
    }
}

