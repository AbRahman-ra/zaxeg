/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.configuration.loader;

import com.zatca.configuration.enums.Configuration;

public interface ConfigurationLoader {
    public Configuration loadConfiguration() throws IllegalAccessException;

    public Configuration loadConfiguration(String[] var1) throws IllegalAccessException;
}

