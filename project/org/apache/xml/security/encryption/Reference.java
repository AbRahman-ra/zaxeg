/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.encryption;

import java.util.Iterator;
import org.w3c.dom.Element;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface Reference {
    public String getType();

    public String getURI();

    public void setURI(String var1);

    public Iterator<Element> getElementRetrievalInformation();

    public void addElementRetrievalInformation(Element var1);

    public void removeElementRetrievalInformation(Element var1);
}

