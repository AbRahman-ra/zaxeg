/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.function;

import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.Navigator;
import org.jaxen.function.BooleanFunction;

public class NotFunction
implements Function {
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() == 1) {
            return NotFunction.evaluate(args.get(0), context.getNavigator());
        }
        throw new FunctionCallException("not() requires one argument.");
    }

    public static Boolean evaluate(Object obj, Navigator nav) {
        return BooleanFunction.evaluate(obj, nav) != false ? Boolean.FALSE : Boolean.TRUE;
    }
}

