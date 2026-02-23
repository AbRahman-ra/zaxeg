/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.encryption;

import org.apache.xml.security.encryption.CipherData;
import org.apache.xml.security.encryption.EncryptionMethod;
import org.apache.xml.security.encryption.EncryptionProperties;
import org.apache.xml.security.keys.KeyInfo;

public interface EncryptedType {
    public String getId();

    public void setId(String var1);

    public String getType();

    public void setType(String var1);

    public String getMimeType();

    public void setMimeType(String var1);

    public String getEncoding();

    public void setEncoding(String var1);

    public EncryptionMethod getEncryptionMethod();

    public void setEncryptionMethod(EncryptionMethod var1);

    public KeyInfo getKeyInfo();

    public void setKeyInfo(KeyInfo var1);

    public CipherData getCipherData();

    public EncryptionProperties getEncryptionProperties();

    public void setEncryptionProperties(EncryptionProperties var1);
}

