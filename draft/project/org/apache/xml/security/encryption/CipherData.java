/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.encryption;

import org.apache.xml.security.encryption.CipherReference;
import org.apache.xml.security.encryption.CipherValue;
import org.apache.xml.security.encryption.XMLEncryptionException;

public interface CipherData {
    public static final int VALUE_TYPE = 1;
    public static final int REFERENCE_TYPE = 2;

    public int getDataType();

    public CipherValue getCipherValue();

    public void setCipherValue(CipherValue var1) throws XMLEncryptionException;

    public CipherReference getCipherReference();

    public void setCipherReference(CipherReference var1) throws XMLEncryptionException;
}

