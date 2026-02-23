/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.exceptions;

import org.apache.xml.security.exceptions.XMLSecurityException;

public class AlgorithmAlreadyRegisteredException
extends XMLSecurityException {
    private static final long serialVersionUID = 1L;

    public AlgorithmAlreadyRegisteredException() {
    }

    public AlgorithmAlreadyRegisteredException(String msgID) {
        super(msgID);
    }

    public AlgorithmAlreadyRegisteredException(String msgID, Object[] exArgs) {
        super(msgID, exArgs);
    }

    public AlgorithmAlreadyRegisteredException(String msgID, Exception originalException) {
        super(msgID, originalException);
    }

    public AlgorithmAlreadyRegisteredException(String msgID, Object[] exArgs, Exception originalException) {
        super(msgID, exArgs, originalException);
    }
}

