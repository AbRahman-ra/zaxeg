/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.function.ext;

import java.util.List;
import java.util.Locale;
import org.jaxen.Context;
import org.jaxen.FunctionCallException;
import org.jaxen.Navigator;
import org.jaxen.function.StringFunction;
import org.jaxen.function.ext.LocaleFunctionSupport;

public class UpperFunction
extends LocaleFunctionSupport {
    public Object call(Context context, List args) throws FunctionCallException {
        Navigator navigator = context.getNavigator();
        int size = args.size();
        if (size > 0) {
            Object text = args.get(0);
            Locale locale = null;
            if (size > 1) {
                locale = this.getLocale(args.get(1), navigator);
            }
            return UpperFunction.evaluate(text, locale, navigator);
        }
        throw new FunctionCallException("upper-case() requires at least one argument.");
    }

    public static String evaluate(Object strArg, Locale locale, Navigator nav) {
        String str = StringFunction.evaluate(strArg, nav);
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        return str.toUpperCase(locale);
    }
}

