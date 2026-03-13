/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import javax.xml.transform.SourceLocator;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

public interface MessageListener2 {
    public void message(XdmNode var1, QName var2, boolean var3, SourceLocator var4);
}

