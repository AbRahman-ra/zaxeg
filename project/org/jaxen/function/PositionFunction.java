/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.function;

import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

public class PositionFunction
implements Function {
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() == 0) {
            return PositionFunction.evaluate(context);
        }
        throw new FunctionCallException("position() does not take any arguments.");
    }

    public static Double evaluate(Context context) {
        return new Double(context.getPosition());
    }
}

