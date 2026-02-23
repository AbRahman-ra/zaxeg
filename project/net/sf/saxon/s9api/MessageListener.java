/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import javax.xml.transform.SourceLocator;
import net.sf.saxon.s9api.XdmNode;

public interface MessageListener {
    public void message(XdmNode var1, boolean var2, SourceLocator var3);
}

