/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.serialize.charcode.UTF8CharacterSet;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

public class EncodeForUri
extends ScalarSystemFunction {
    private static final String hex = "0123456789ABCDEF";
    private static int[] UTF8RepresentationLength = new int[]{1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 2, 2, 3, 4};

    @Override
    public AtomicValue evaluate(Item arg, XPathContext context) throws XPathException {
        CharSequence s = arg.getStringValueCS();
        return StringValue.makeStringValue(EncodeForUri.escape(s, "-_.~"));
    }

    @Override
    public ZeroOrOne resultWhenEmpty() {
        return ZERO_LENGTH_STRING;
    }

    public static CharSequence escape(CharSequence s, String allowedPunctuation) {
        FastStringBuffer sb = new FastStringBuffer(s.length());
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9') {
                sb.cat(c);
                continue;
            }
            if (c <= ' ' || c >= '\u007f') {
                EncodeForUri.escapeChar(c, i + 1 < s.length() ? s.charAt(i + 1) : (char)' ', sb);
                continue;
            }
            if (allowedPunctuation.indexOf(c) >= 0) {
                sb.cat(c);
                continue;
            }
            EncodeForUri.escapeChar(c, ' ', sb);
        }
        return sb;
    }

    public static void escapeChar(char c, char c2, FastStringBuffer sb) {
        byte[] array = new byte[4];
        int used = UTF8CharacterSet.getUTF8Encoding(c, c2, array);
        for (int b = 0; b < used; ++b) {
            int v = array[b] & 0xFF;
            sb.cat('%');
            sb.cat(hex.charAt(v / 16));
            sb.cat(hex.charAt(v % 16));
        }
    }

    public static void checkPercentEncoding(String uri) throws XPathException {
        String hexDigits = "0123456789abcdefABCDEF";
        int i = 0;
        while (i < uri.length()) {
            char c = uri.charAt(i);
            if (c == '%') {
                int h2;
                if (i + 2 >= uri.length()) {
                    throw new XPathException("% sign in URI must be followed by two hex digits" + Err.wrap(uri));
                }
                int h1 = "0123456789abcdefABCDEF".indexOf(uri.charAt(i + 1));
                if (h1 > 15) {
                    h1 -= 6;
                }
                if ((h2 = "0123456789abcdefABCDEF".indexOf(uri.charAt(i + 2))) > 15) {
                    h2 -= 6;
                }
                if (h1 >= 0 && h2 >= 0) {
                    int b = h1 << 4 | h2;
                    int expectedOctets = UTF8RepresentationLength[h1];
                    if (expectedOctets == -1) {
                        throw new XPathException("First %-encoded octet in URI is not valid as the start of a UTF-8 character: first two bits must not be '10'" + Err.wrap(uri));
                    }
                    byte[] bytes = new byte[expectedOctets];
                    bytes[0] = (byte)b;
                    i += 3;
                    for (int q = 1; q < expectedOctets; ++q) {
                        if (i + 2 > uri.length() || uri.charAt(i) != '%') {
                            throw new XPathException("Incomplete %-encoded UTF-8 octet sequence in URI " + Err.wrap(uri));
                        }
                        h1 = "0123456789abcdefABCDEF".indexOf(uri.charAt(i + 1));
                        if (h1 > 15) {
                            h1 -= 6;
                        }
                        if ((h2 = "0123456789abcdefABCDEF".indexOf(uri.charAt(i + 2))) > 15) {
                            h2 -= 6;
                        }
                        if (h1 < 0 || h2 < 0) {
                            throw new XPathException("Invalid %-encoded UTF-8 octet sequence in URI" + Err.wrap(uri));
                        }
                        if (UTF8RepresentationLength[h1] != -1) {
                            throw new XPathException("In a URI, a %-encoded UTF-8 octet after the first must have '10' as the first two bits" + Err.wrap(uri));
                        }
                        b = h1 << 4 | h2;
                        bytes[q] = (byte)b;
                        i += 3;
                    }
                    continue;
                }
                throw new XPathException("% sign in URI must be followed by two hex digits" + Err.wrap(uri));
            }
            ++i;
        }
    }
}

