/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import javax.xml.transform.Source;

public class EmptySource
implements Source {
    private static final EmptySource THE_INSTANCE = new EmptySource();

    private EmptySource() {
    }

    public static EmptySource getInstance() {
        return THE_INSTANCE;
    }

    @Override
    public void setSystemId(String systemId) {
    }

    @Override
    public String getSystemId() {
        return null;
    }
}

