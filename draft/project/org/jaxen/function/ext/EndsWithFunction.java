/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.function.ext;

import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.Navigator;
import org.jaxen.function.StringFunction;

public class EndsWithFunction
implements Function {
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() == 2) {
            return EndsWithFunction.evaluate(args.get(0), args.get(1), context.getNavigator());
        }
        throw new FunctionCallException("ends-with() requires two arguments.");
    }

    public static Boolean evaluate(Object strArg, Object matchArg, Navigator nav) {
        String match;
        String str = StringFunction.evaluate(strArg, nav);
        return str.endsWith(match = StringFunction.evaluate(matchArg, nav)) ? Boolean.TRUE : Boolean.FALSE;
    }
}

