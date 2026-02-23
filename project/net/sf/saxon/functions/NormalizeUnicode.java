/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.serialize.codenorm.Normalizer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.CompressedWhitespace;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class NormalizeUnicode
extends SystemFunction {
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue sv = (StringValue)arguments[0].head();
        if (sv == null) {
            return StringValue.EMPTY_STRING;
        }
        String nf = arguments.length == 1 ? "NFC" : Whitespace.trim(arguments[1].head().getStringValue());
        return NormalizeUnicode.normalize(sv, nf, context);
    }

    public static StringValue normalize(StringValue sv, String form, XPathContext c) throws XPathException {
        int fb;
        if (form.equalsIgnoreCase("NFC")) {
            fb = 2;
        } else if (form.equalsIgnoreCase("NFD")) {
            fb = 0;
        } else if (form.equalsIgnoreCase("NFKC")) {
            fb = 3;
        } else if (form.equalsIgnoreCase("NFKD")) {
            fb = 1;
        } else {
            if (form.isEmpty()) {
                return sv;
            }
            String msg = "Normalization form " + form + " is not supported";
            XPathException err = new XPathException(msg);
            err.setErrorCode("FOCH0003");
            err.setXPathContext(c);
            throw err;
        }
        boolean allASCII = true;
        CharSequence chars = sv.getStringValueCS();
        if (chars instanceof CompressedWhitespace) {
            return sv;
        }
        for (int i = chars.length() - 1; i >= 0; --i) {
            if (chars.charAt(i) <= '\u007f') continue;
            allASCII = false;
            break;
        }
        if (allASCII) {
            return sv;
        }
        Normalizer norm = Normalizer.make(fb, c.getConfiguration());
        CharSequence result = norm.normalize(sv.getStringValueCS());
        return StringValue.makeStringValue(result);
    }
}

