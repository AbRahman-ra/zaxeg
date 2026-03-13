/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public final class AnyURIValue
extends StringValue {
    public static final AnyURIValue EMPTY_URI = new AnyURIValue("");

    public AnyURIValue(CharSequence value) {
        this.value = value == null ? "" : Whitespace.collapseWhitespace(value).toString();
        this.typeLabel = BuiltInAtomicType.ANY_URI;
    }

    public AnyURIValue(CharSequence value, AtomicType type) {
        this.value = value == null ? "" : Whitespace.collapseWhitespace(value).toString();
        this.typeLabel = type;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        AnyURIValue v = new AnyURIValue(this.value);
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.ANY_URI;
    }

    public static String decode(String s) {
        if (s == null) {
            return s;
        }
        int n = s.length();
        if (n == 0) {
            return s;
        }
        if (s.indexOf(37) < 0) {
            return s;
        }
        FastStringBuffer sb = new FastStringBuffer(n);
        ByteBuffer bb = ByteBuffer.allocate(n);
        Charset utf8 = Charset.forName("UTF-8");
        char c = s.charAt(0);
        boolean betweenBrackets = false;
        int i = 0;
        while (i < n) {
            assert (c == s.charAt(i));
            if (c == '[') {
                betweenBrackets = true;
            } else if (betweenBrackets && c == ']') {
                betweenBrackets = false;
            }
            if (c != '%' || betweenBrackets) {
                sb.cat(c);
                if (++i >= n) break;
                c = s.charAt(i);
                continue;
            }
            bb.clear();
            do {
                assert (n - i >= 2);
                bb.put(AnyURIValue.hex(s.charAt(++i), s.charAt(++i)));
            } while (++i < n && (c = s.charAt(i)) == '%');
            bb.flip();
            sb.cat(utf8.decode(bb));
        }
        return sb.toString();
    }

    private static byte hex(char high, char low) {
        return (byte)(AnyURIValue.hexToDec(high) << 4 | AnyURIValue.hexToDec(low));
    }

    private static int hexToDec(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'a' && c <= 'f') {
            return c - 97 + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 65 + 10;
        }
        return 0;
    }
}

