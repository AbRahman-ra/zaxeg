/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.Properties;
import javax.xml.transform.Result;
import net.sf.saxon.event.Receiver;

public interface StAXResultHandler {
    public Receiver getReceiver(Result var1, Properties var2);
}

