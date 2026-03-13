/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.transforms;

import org.apache.xml.security.exceptions.XMLSecurityException;

public class TransformationException
extends XMLSecurityException {
    private static final long serialVersionUID = 1L;

    public TransformationException() {
    }

    public TransformationException(String msgID) {
        super(msgID);
    }

    public TransformationException(String msgID, Object[] exArgs) {
        super(msgID, exArgs);
    }

    public TransformationException(String msgID, Exception originalException) {
        super(msgID, originalException);
    }

    public TransformationException(String msgID, Object[] exArgs, Exception originalException) {
        super(msgID, exArgs, originalException);
    }
}

