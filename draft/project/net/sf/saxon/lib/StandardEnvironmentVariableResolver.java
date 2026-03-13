/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.Map;
import java.util.Set;
import net.sf.saxon.lib.EnvironmentVariableResolver;

public class StandardEnvironmentVariableResolver
implements EnvironmentVariableResolver {
    @Override
    public Set<String> getAvailableEnvironmentVariables() {
        Map<String, String> vars = System.getenv();
        return vars.keySet();
    }

    @Override
    public String getEnvironmentVariable(String name) {
        return System.getenv(name);
    }
}

