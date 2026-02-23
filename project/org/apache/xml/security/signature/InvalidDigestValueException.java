/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.signature;

import org.apache.xml.security.signature.XMLSignatureException;

public class InvalidDigestValueException
extends XMLSignatureException {
    private static final long serialVersionUID = 1L;

    public InvalidDigestValueException() {
    }

    public InvalidDigestValueException(String msgID) {
        super(msgID);
    }

    public InvalidDigestValueException(String msgID, Object[] exArgs) {
        super(msgID, exArgs);
    }

    public InvalidDigestValueException(String msgID, Exception originalException) {
        super(msgID, originalException);
    }

    public InvalidDigestValueException(String msgID, Object[] exArgs, Exception originalException) {
        super(msgID, exArgs, originalException);
    }
}

