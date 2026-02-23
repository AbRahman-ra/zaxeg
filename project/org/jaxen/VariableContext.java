/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import org.jaxen.UnresolvableException;

public interface VariableContext {
    public Object getVariableValue(String var1, String var2, String var3) throws UnresolvableException;
}

