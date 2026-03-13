/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.function;

import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

public class LastFunction
implements Function {
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() == 0) {
            return LastFunction.evaluate(context);
        }
        throw new FunctionCallException("last() requires no arguments.");
    }

    public static Double evaluate(Context context) {
        return new Double(context.getSize());
    }
}

