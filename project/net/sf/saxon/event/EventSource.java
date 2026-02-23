/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import javax.xml.transform.Source;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.trans.XPathException;

public abstract class EventSource
implements Source {
    private String systemId;

    @Override
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    public abstract void send(Receiver var1) throws XPathException;
}

