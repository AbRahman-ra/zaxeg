/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import java.util.List;
import org.jaxen.Context;
import org.jaxen.FunctionCallException;

public interface Function {
    public Object call(Context var1, List var2) throws FunctionCallException;
}

