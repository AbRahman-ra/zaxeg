/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.signature;

import org.apache.xml.security.signature.Reference;
import org.apache.xml.security.signature.XMLSignatureException;

public class MissingResourceFailureException
extends XMLSignatureException {
    private static final long serialVersionUID = 1L;
    private Reference uninitializedReference = null;

    public MissingResourceFailureException(String msgID, Reference reference) {
        super(msgID);
        this.uninitializedReference = reference;
    }

    public MissingResourceFailureException(String msgID, Object[] exArgs, Reference reference) {
        super(msgID, exArgs);
        this.uninitializedReference = reference;
    }

    public MissingResourceFailureException(String msgID, Exception originalException, Reference reference) {
        super(msgID, originalException);
        this.uninitializedReference = reference;
    }

    public MissingResourceFailureException(String msgID, Object[] exArgs, Exception originalException, Reference reference) {
        super(msgID, exArgs, originalException);
        this.uninitializedReference = reference;
    }

    public void setReference(Reference reference) {
        this.uninitializedReference = reference;
    }

    public Reference getReference() {
        return this.uninitializedReference;
    }
}

