/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.function;

import java.util.HashMap;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.Navigator;
import org.jaxen.function.StringFunction;

public class TranslateFunction
implements Function {
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() == 3) {
            return TranslateFunction.evaluate(args.get(0), args.get(1), args.get(2), context.getNavigator());
        }
        throw new FunctionCallException("translate() requires three arguments.");
    }

    public static String evaluate(Object strArg, Object fromArg, Object toArg, Navigator nav) throws FunctionCallException {
        String inStr = StringFunction.evaluate(strArg, nav);
        String fromStr = StringFunction.evaluate(fromArg, nav);
        String toStr = StringFunction.evaluate(toArg, nav);
        HashMap<String, String> characterMap = new HashMap<String, String>();
        String[] fromCharacters = TranslateFunction.toUnicodeCharacters(fromStr);
        String[] toCharacters = TranslateFunction.toUnicodeCharacters(toStr);
        int fromLen = fromCharacters.length;
        int toLen = toCharacters.length;
        for (int i = 0; i < fromLen; ++i) {
            String cFrom = fromCharacters[i];
            if (characterMap.containsKey(cFrom)) continue;
            if (i < toLen) {
                characterMap.put(cFrom, toCharacters[i]);
                continue;
            }
            characterMap.put(cFrom, null);
        }
        StringBuffer outStr = new StringBuffer(inStr.length());
        String[] inCharacters = TranslateFunction.toUnicodeCharacters(inStr);
        int inLen = inCharacters.length;
        for (int i = 0; i < inLen; ++i) {
            String cIn = inCharacters[i];
            if (characterMap.containsKey(cIn)) {
                String cTo = (String)characterMap.get(cIn);
                if (cTo == null) continue;
                outStr.append(cTo);
                continue;
            }
            outStr.append(cIn);
        }
        return outStr.toString();
    }

    private static String[] toUnicodeCharacters(String s) throws FunctionCallException {
        String[] result = new String[s.length()];
        int stringLength = 0;
        for (int i = 0; i < s.length(); ++i) {
            block6: {
                char c1 = s.charAt(i);
                if (TranslateFunction.isHighSurrogate(c1)) {
                    try {
                        char c2 = s.charAt(i + 1);
                        if (TranslateFunction.isLowSurrogate(c2)) {
                            result[stringLength] = (c1 + "" + c2).intern();
                            ++i;
                            break block6;
                        }
                        throw new FunctionCallException("Mismatched surrogate pair in translate function");
                    } catch (StringIndexOutOfBoundsException ex) {
                        throw new FunctionCallException("High surrogate without low surrogate at end of string passed to translate function");
                    }
                }
                result[stringLength] = String.valueOf(c1).intern();
            }
            ++stringLength;
        }
        if (stringLength == result.length) {
            return result;
        }
        String[] trimmed = new String[stringLength];
        System.arraycopy(result, 0, trimmed, 0, stringLength);
        return trimmed;
    }

    private static boolean isHighSurrogate(char c) {
        return c >= '\ud800' && c <= '\udbff';
    }

    private static boolean isLowSurrogate(char c) {
        return c >= '\udc00' && c <= '\udfff';
    }
}

