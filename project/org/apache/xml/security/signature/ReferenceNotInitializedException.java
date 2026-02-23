/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.signature;

import org.apache.xml.security.signature.XMLSignatureException;

public class ReferenceNotInitializedException
extends XMLSignatureException {
    private static final long serialVersionUID = 1L;

    public ReferenceNotInitializedException() {
    }

    public ReferenceNotInitializedException(String msgID) {
        super(msgID);
    }

    public ReferenceNotInitializedException(String msgID, Object[] exArgs) {
        super(msgID, exArgs);
    }

    public ReferenceNotInitializedException(String msgID, Exception originalException) {
        super(msgID, originalException);
    }

    public ReferenceNotInitializedException(String msgID, Object[] exArgs, Exception originalException) {
        super(msgID, exArgs, originalException);
    }
}

