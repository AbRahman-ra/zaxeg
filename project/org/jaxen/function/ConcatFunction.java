/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.function;

import java.util.Iterator;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.Navigator;
import org.jaxen.function.StringFunction;

public class ConcatFunction
implements Function {
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() >= 2) {
            return ConcatFunction.evaluate(args, context.getNavigator());
        }
        throw new FunctionCallException("concat() requires at least two arguments");
    }

    public static String evaluate(List list, Navigator nav) {
        StringBuffer result = new StringBuffer();
        Iterator argIter = list.iterator();
        while (argIter.hasNext()) {
            result.append(StringFunction.evaluate(argIter.next(), nav));
        }
        return result.toString();
    }
}

