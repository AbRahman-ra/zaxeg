/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.encryption;

import java.util.Iterator;
import org.apache.xml.security.encryption.EncryptionProperty;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface EncryptionProperties {
    public String getId();

    public void setId(String var1);

    public Iterator<EncryptionProperty> getEncryptionProperties();

    public void addEncryptionProperty(EncryptionProperty var1);

    public void removeEncryptionProperty(EncryptionProperty var1);
}

