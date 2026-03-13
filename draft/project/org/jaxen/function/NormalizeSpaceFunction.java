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

public class NormalizeSpaceFunction
implements Function {
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() == 0) {
            return NormalizeSpaceFunction.evaluate(context.getNodeSet(), context.getNavigator());
        }
        if (args.size() == 1) {
            return NormalizeSpaceFunction.evaluate(args.get(0), context.getNavigator());
        }
        throw new FunctionCallException("normalize-space() cannot have more than one argument");
    }

    public static String evaluate(Object strArg, Navigator nav) {
        String str = StringFunction.evaluate(strArg, nav);
        char[] buffer = str.toCharArray();
        int write = 0;
        int lastWrite = 0;
        boolean wroteOne = false;
        int read = 0;
        while (read < buffer.length) {
            if (NormalizeSpaceFunction.isXMLSpace(buffer[read])) {
                if (wroteOne) {
                    buffer[write++] = 32;
                }
                while (++read < buffer.length && NormalizeSpaceFunction.isXMLSpace(buffer[read])) {
                }
                continue;
            }
            buffer[write++] = buffer[read++];
            wroteOne = true;
            lastWrite = write;
        }
        return new String(buffer, 0, lastWrite);
    }

    private static boolean isXMLSpace(char c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t';
    }
}

