/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.Set;

public interface EnvironmentVariableResolver {
    public Set<String> getAvailableEnvironmentVariables();

    public String getEnvironmentVariable(String var1);
}

