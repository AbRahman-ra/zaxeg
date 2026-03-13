/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.encryption;

import java.util.Iterator;
import org.w3c.dom.Element;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface EncryptionProperty {
    public String getTarget();

    public void setTarget(String var1);

    public String getId();

    public void setId(String var1);

    public String getAttribute(String var1);

    public void setAttribute(String var1, String var2);

    public Iterator<Element> getEncryptionInformation();

    public void addEncryptionInformation(Element var1);

    public void removeEncryptionInformation(Element var1);
}

