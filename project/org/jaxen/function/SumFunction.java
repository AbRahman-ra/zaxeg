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
import org.jaxen.function.NumberFunction;

public class SumFunction
implements Function {
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() == 1) {
            return SumFunction.evaluate(args.get(0), context.getNavigator());
        }
        throw new FunctionCallException("sum() requires one argument.");
    }

    public static Double evaluate(Object obj, Navigator nav) throws FunctionCallException {
        double sum = 0.0;
        if (obj instanceof List) {
            Iterator nodeIter = ((List)obj).iterator();
            while (nodeIter.hasNext()) {
                double term = NumberFunction.evaluate(nodeIter.next(), nav);
                sum += term;
            }
        } else {
            throw new FunctionCallException("The argument to the sum function must be a node-set");
        }
        return new Double(sum);
    }
}

