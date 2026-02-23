/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.transforms;

import org.apache.xml.security.exceptions.XMLSecurityException;

public class InvalidTransformException
extends XMLSecurityException {
    private static final long serialVersionUID = 1L;

    public InvalidTransformException() {
    }

    public InvalidTransformException(String msgId) {
        super(msgId);
    }

    public InvalidTransformException(String msgId, Object[] exArgs) {
        super(msgId, exArgs);
    }

    public InvalidTransformException(String msgId, Exception originalException) {
        super(msgId, originalException);
    }

    public InvalidTransformException(String msgId, Object[] exArgs, Exception originalException) {
        super(msgId, exArgs, originalException);
    }
}

