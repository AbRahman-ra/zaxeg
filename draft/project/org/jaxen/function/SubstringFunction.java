/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.function;

import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.Navigator;
import org.jaxen.function.NumberFunction;
import org.jaxen.function.RoundFunction;
import org.jaxen.function.StringFunction;
import org.jaxen.function.StringLengthFunction;

public class SubstringFunction
implements Function {
    public Object call(Context context, List args) throws FunctionCallException {
        int argc = args.size();
        if (argc < 2 || argc > 3) {
            throw new FunctionCallException("substring() requires two or three arguments.");
        }
        Navigator nav = context.getNavigator();
        String str = StringFunction.evaluate(args.get(0), nav);
        if (str == null) {
            return "";
        }
        int stringLength = StringLengthFunction.evaluate(args.get(0), nav).intValue();
        if (stringLength == 0) {
            return "";
        }
        Double d1 = NumberFunction.evaluate(args.get(1), nav);
        if (d1.isNaN()) {
            return "";
        }
        int start = RoundFunction.evaluate(d1, nav).intValue() - 1;
        int substringLength = stringLength;
        if (argc == 3) {
            Double d2 = NumberFunction.evaluate(args.get(2), nav);
            substringLength = !d2.isNaN() ? RoundFunction.evaluate(d2, nav).intValue() : 0;
        }
        if (substringLength < 0) {
            return "";
        }
        int end = start + substringLength;
        if (argc == 2) {
            end = stringLength;
        }
        if (start < 0) {
            start = 0;
        } else if (start > stringLength) {
            return "";
        }
        if (end > stringLength) {
            end = stringLength;
        } else if (end < start) {
            return "";
        }
        if (stringLength == str.length()) {
            return str.substring(start, end);
        }
        return SubstringFunction.unicodeSubstring(str, start, end);
    }

    private static String unicodeSubstring(String s, int start, int end) {
        StringBuffer result = new StringBuffer(s.length());
        int jChar = 0;
        for (int uChar = 0; uChar < end; ++uChar) {
            char c = s.charAt(jChar);
            if (uChar >= start) {
                result.append(c);
            }
            if (c >= '\ud800') {
                ++jChar;
                if (uChar >= start) {
                    result.append(s.charAt(jChar));
                }
            }
            ++jChar;
        }
        return result.toString();
    }
}

