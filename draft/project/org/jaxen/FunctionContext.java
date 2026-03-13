/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import org.jaxen.Function;
import org.jaxen.UnresolvableException;

public interface FunctionContext {
    public Function getFunction(String var1, String var2, String var3) throws UnresolvableException;
}

