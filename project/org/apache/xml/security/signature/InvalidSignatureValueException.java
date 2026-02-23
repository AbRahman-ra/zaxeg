/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.signature;

import org.apache.xml.security.signature.XMLSignatureException;

public class InvalidSignatureValueException
extends XMLSignatureException {
    private static final long serialVersionUID = 1L;

    public InvalidSignatureValueException() {
    }

    public InvalidSignatureValueException(String msgID) {
        super(msgID);
    }

    public InvalidSignatureValueException(String msgID, Object[] exArgs) {
        super(msgID, exArgs);
    }

    public InvalidSignatureValueException(String msgID, Exception originalException) {
        super(msgID, originalException);
    }

    public InvalidSignatureValueException(String msgID, Object[] exArgs, Exception originalException) {
        super(msgID, exArgs, originalException);
    }
}

