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

public class NumberFunction
implements Function {
    private static final Double NaN = new Double(Double.NaN);

    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() == 1) {
            return NumberFunction.evaluate(args.get(0), context.getNavigator());
        }
        if (args.size() == 0) {
            return NumberFunction.evaluate(context.getNodeSet(), context.getNavigator());
        }
        throw new FunctionCallException("number() takes at most one argument.");
    }

    public static Double evaluate(Object obj, Navigator nav) {
        if (obj instanceof Double) {
            return (Double)obj;
        }
        if (obj instanceof String) {
            String str = (String)obj;
            try {
                Double doubleValue = new Double(str);
                return doubleValue;
            } catch (NumberFormatException e) {
                return NaN;
            }
        }
        if (obj instanceof List || obj instanceof Iterator) {
            return NumberFunction.evaluate(StringFunction.evaluate(obj, nav), nav);
        }
        if (nav.isElement(obj) || nav.isAttribute(obj) || nav.isText(obj) || nav.isComment(obj) || nav.isProcessingInstruction(obj) || nav.isDocument(obj) || nav.isNamespace(obj)) {
            return NumberFunction.evaluate(StringFunction.evaluate(obj, nav), nav);
        }
        if (obj instanceof Boolean) {
            if (Boolean.TRUE.equals(obj)) {
                return new Double(1.0);
            }
            return new Double(0.0);
        }
        return NaN;
    }

    public static boolean isNaN(double val) {
        return Double.isNaN(val);
    }

    public static boolean isNaN(Double val) {
        return val.equals(NaN);
    }
}

