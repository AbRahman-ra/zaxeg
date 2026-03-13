/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto;

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.URIDereferencer;

public interface XMLCryptoContext {
    public String getBaseURI();

    public void setBaseURI(String var1);

    public KeySelector getKeySelector();

    public void setKeySelector(KeySelector var1);

    public URIDereferencer getURIDereferencer();

    public void setURIDereferencer(URIDereferencer var1);

    public String getNamespacePrefix(String var1, String var2);

    public String putNamespacePrefix(String var1, String var2);

    public String getDefaultNamespacePrefix();

    public void setDefaultNamespacePrefix(String var1);

    public Object setProperty(String var1, Object var2);

    public Object getProperty(String var1);

    public Object get(Object var1);

    public Object put(Object var1, Object var2);
}

