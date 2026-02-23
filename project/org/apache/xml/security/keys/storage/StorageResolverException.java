/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.storage;

import org.apache.xml.security.exceptions.XMLSecurityException;

public class StorageResolverException
extends XMLSecurityException {
    private static final long serialVersionUID = 1L;

    public StorageResolverException() {
    }

    public StorageResolverException(String msgID) {
        super(msgID);
    }

    public StorageResolverException(String msgID, Object[] exArgs) {
        super(msgID, exArgs);
    }

    public StorageResolverException(String msgID, Exception originalException) {
        super(msgID, originalException);
    }

    public StorageResolverException(String msgID, Object[] exArgs, Exception originalException) {
        super(msgID, exArgs, originalException);
    }
}

