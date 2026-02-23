/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.encryption;

import org.apache.xml.security.encryption.EncryptedType;
import org.apache.xml.security.encryption.ReferenceList;

public interface EncryptedKey
extends EncryptedType {
    public String getRecipient();

    public void setRecipient(String var1);

    public ReferenceList getReferenceList();

    public void setReferenceList(ReferenceList var1);

    public String getCarriedName();

    public void setCarriedName(String var1);
}

