/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pull;

import javax.xml.transform.Source;
import net.sf.saxon.pull.PullProvider;

public class PullSource
implements Source {
    private String systemId;
    private PullProvider provider;

    public PullSource(PullProvider provider) {
        this.provider = provider;
        if (provider.getSourceLocator() != null) {
            this.systemId = provider.getSourceLocator().getSystemId();
        }
    }

    public PullProvider getPullProvider() {
        return this.provider;
    }

    @Override
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }
}

