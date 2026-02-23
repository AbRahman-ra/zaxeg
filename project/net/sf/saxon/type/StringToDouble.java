/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.trans.Err;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.Whitespace;

public class StringToDouble
extends StringConverter {
    private static StringToDouble THE_INSTANCE = new StringToDouble();
    private static double[] powers = new double[]{1.0, 10.0, 100.0, 1000.0, 10000.0, 100000.0, 1000000.0, 1.0E7, 1.0E8};

    public static StringToDouble getInstance() {
        return THE_INSTANCE;
    }

    protected StringToDouble() {
    }

    public double stringToNumber(CharSequence s) throws NumberFormatException {
        String n;
        int len = s.length();
        boolean containsDisallowedChars = false;
        boolean containsWhitespace = false;
        if (len < 9) {
            boolean useJava = false;
            long num = 0L;
            int dot = -1;
            int lastDigit = -1;
            boolean onlySpaceAllowed = false;
            block10: for (int i = 0; i < len; ++i) {
                char c = s.charAt(i);
                switch (c) {
                    case '\t': 
                    case '\n': 
                    case '\r': 
                    case ' ': {
                        containsWhitespace = true;
                        if (lastDigit == -1) continue block10;
                        onlySpaceAllowed = true;
                        continue block10;
                    }
                    case '0': 
                    case '1': 
                    case '2': 
                    case '3': 
                    case '4': 
                    case '5': 
                    case '6': 
                    case '7': 
                    case '8': 
                    case '9': {
                        if (onlySpaceAllowed) {
                            throw new NumberFormatException("Numeric value contains embedded whitespace");
                        }
                        lastDigit = i;
                        num = num * 10L + (long)(c - 48);
                        continue block10;
                    }
                    case '.': {
                        if (onlySpaceAllowed) {
                            throw new NumberFormatException("Numeric value contains embedded whitespace");
                        }
                        if (dot != -1) {
                            throw new NumberFormatException("Only one decimal point allowed");
                        }
                        dot = i;
                        continue block10;
                    }
                    case 'D': 
                    case 'F': 
                    case 'N': 
                    case 'X': 
                    case 'd': 
                    case 'f': 
                    case 'n': 
                    case 'x': {
                        containsDisallowedChars = true;
                        useJava = true;
                        break block10;
                    }
                    default: {
                        useJava = true;
                    }
                }
            }
            if (!useJava) {
                if (lastDigit == -1) {
                    throw new NumberFormatException("String to double conversion: no digits found");
                }
                if (dot == -1 || dot > lastDigit) {
                    return num;
                }
                int afterPoint = lastDigit - dot;
                return (double)num / powers[afterPoint];
            }
        } else {
            block11: for (int i = 0; i < len; ++i) {
                char c = s.charAt(i);
                switch (c) {
                    case '\t': 
                    case '\n': 
                    case '\r': 
                    case ' ': {
                        containsWhitespace = true;
                        continue block11;
                    }
                    case '+': 
                    case '-': 
                    case '.': 
                    case '0': 
                    case '1': 
                    case '2': 
                    case '3': 
                    case '4': 
                    case '5': 
                    case '6': 
                    case '7': 
                    case '8': 
                    case '9': 
                    case 'E': 
                    case 'e': {
                        continue block11;
                    }
                    default: {
                        containsDisallowedChars = true;
                        break block11;
                    }
                }
            }
        }
        String string = n = containsWhitespace ? Whitespace.trimWhitespace(s).toString() : s.toString();
        if ("INF".equals(n)) {
            return Double.POSITIVE_INFINITY;
        }
        if ("+INF".equals(n)) {
            return this.signedPositiveInfinity();
        }
        if ("-INF".equals(n)) {
            return Double.NEGATIVE_INFINITY;
        }
        if ("NaN".equals(n)) {
            return Double.NaN;
        }
        if (containsDisallowedChars) {
            throw new NumberFormatException("invalid floating point value: " + s);
        }
        return Double.parseDouble(n);
    }

    protected double signedPositiveInfinity() {
        throw new NumberFormatException("the float/double value '+INF' is not allowed under XSD 1.0");
    }

    @Override
    public ConversionResult convertString(CharSequence input) {
        try {
            double d = this.stringToNumber(input);
            return new DoubleValue(d);
        } catch (NumberFormatException e) {
            return new ValidationFailure("Cannot convert string " + Err.wrap(input, 4) + " to double");
        }
    }
}

