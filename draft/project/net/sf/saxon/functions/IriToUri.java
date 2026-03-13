/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.Arrays;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.EncodeForUri;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

public class IriToUri
extends ScalarSystemFunction {
    public static boolean[] allowedASCII = new boolean[128];
    private static final String hex = "0123456789ABCDEF";

    @Override
    public AtomicValue evaluate(Item arg, XPathContext context) throws XPathException {
        CharSequence s = arg.getStringValueCS();
        return StringValue.makeStringValue(IriToUri.iriToUri(s));
    }

    @Override
    public ZeroOrOne resultWhenEmpty() {
        return ZERO_LENGTH_STRING;
    }

    public static CharSequence iriToUri(CharSequence s) {
        if (IriToUri.allAllowedAscii(s)) {
            return s;
        }
        FastStringBuffer sb = new FastStringBuffer(s.length() + 20);
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c >= '\u007f' || !allowedASCII[c]) {
                EncodeForUri.escapeChar(c, i + 1 < s.length() ? s.charAt(i + 1) : (char)' ', sb);
                continue;
            }
            sb.cat(c);
        }
        return sb;
    }

    private static boolean allAllowedAscii(CharSequence s) {
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c < '\u007f' && allowedASCII[c]) continue;
            return false;
        }
        return true;
    }

    static {
        Arrays.fill(allowedASCII, 0, 32, false);
        Arrays.fill(allowedASCII, 33, 127, true);
        IriToUri.allowedASCII[34] = false;
        IriToUri.allowedASCII[60] = false;
        IriToUri.allowedASCII[62] = false;
        IriToUri.allowedASCII[92] = false;
        IriToUri.allowedASCII[94] = false;
        IriToUri.allowedASCII[96] = false;
        IriToUri.allowedASCII[123] = false;
        IriToUri.allowedASCII[124] = false;
        IriToUri.allowedASCII[125] = false;
    }
}

