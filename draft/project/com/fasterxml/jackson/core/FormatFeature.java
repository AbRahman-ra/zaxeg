/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.util.JacksonFeature;

public interface FormatFeature
extends JacksonFeature {
    @Override
    public boolean enabledByDefault();

    @Override
    public int getMask();

    @Override
    public boolean enabledIn(int var1);
}

