/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.encryption;

import org.apache.xml.security.exceptions.XMLSecurityException;

public class XMLEncryptionException
extends XMLSecurityException {
    private static final long serialVersionUID = 1L;

    public XMLEncryptionException() {
    }

    public XMLEncryptionException(String msgID) {
        super(msgID);
    }

    public XMLEncryptionException(String msgID, Object[] exArgs) {
        super(msgID, exArgs);
    }

    public XMLEncryptionException(String msgID, Exception originalException) {
        super(msgID, originalException);
    }

    public XMLEncryptionException(String msgID, Object[] exArgs, Exception originalException) {
        super(msgID, exArgs, originalException);
    }
}

