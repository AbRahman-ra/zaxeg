/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.function;

import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.Navigator;
import org.jaxen.function.StringFunction;

public class StringLengthFunction
implements Function {
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() == 0) {
            return StringLengthFunction.evaluate(context.getNodeSet(), context.getNavigator());
        }
        if (args.size() == 1) {
            return StringLengthFunction.evaluate(args.get(0), context.getNavigator());
        }
        throw new FunctionCallException("string-length() requires one argument.");
    }

    public static Double evaluate(Object obj, Navigator nav) throws FunctionCallException {
        String str = StringFunction.evaluate(obj, nav);
        char[] data = str.toCharArray();
        int length = 0;
        for (int i = 0; i < data.length; ++i) {
            char c = data[i];
            ++length;
            if (c < '\ud800' || c > '\udfff') continue;
            try {
                char low = data[i + 1];
                if (low < '\udc00' || low > '\udfff') {
                    throw new FunctionCallException("Bad surrogate pair in string " + str);
                }
                ++i;
                continue;
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new FunctionCallException("Bad surrogate pair in string " + str);
            }
        }
        return new Double(length);
    }
}

