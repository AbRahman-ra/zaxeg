/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.util.Arrays;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Whitespace;

public class Base64BinaryValue
extends AtomicValue
implements AtomicMatchKey,
Comparable {
    private byte[] binaryValue;
    private static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    private static int[] encoding = new int[64];
    private static int[] decoding = new int[128];

    public Base64BinaryValue(CharSequence s) throws XPathException {
        this.binaryValue = Base64BinaryValue.decode(s);
        this.typeLabel = BuiltInAtomicType.BASE64_BINARY;
    }

    public Base64BinaryValue(byte[] value) {
        this.binaryValue = value;
        this.typeLabel = BuiltInAtomicType.BASE64_BINARY;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        Base64BinaryValue v = new Base64BinaryValue(this.binaryValue);
        v.typeLabel = typeLabel;
        return v;
    }

    public byte[] getBinaryValue() {
        return this.binaryValue;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.BASE64_BINARY;
    }

    @Override
    public String getPrimitiveStringValue() {
        return Base64BinaryValue.encode(this.binaryValue).toString();
    }

    public int getLengthInOctets() {
        return this.binaryValue.length;
    }

    @Override
    public Comparable getSchemaComparable() {
        return new Base64BinaryComparable();
    }

    @Override
    public AtomicMatchKey getXPathComparable(boolean ordered, StringCollator collator, int implicitTimezone) {
        return this;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Base64BinaryValue && Arrays.equals(this.binaryValue, ((Base64BinaryValue)other).binaryValue);
    }

    public int hashCode() {
        return Base64BinaryValue.byteArrayHashCode(this.binaryValue);
    }

    protected static int byteArrayHashCode(byte[] value) {
        long h = 0L;
        for (int i = 0; i < Math.min(value.length, 64); ++i) {
            h = h << 1 ^ (long)value[i];
        }
        return (int)(h >> 32 ^ h);
    }

    public static CharSequence encode(byte[] value) {
        int val;
        FastStringBuffer buff = new FastStringBuffer(value.length);
        int whole = value.length - value.length % 3;
        for (int i = 0; i < whole; i += 3) {
            val = ((value[i] & 0xFF) << 16) + ((value[i + 1] & 0xFF) << 8) + (value[i + 2] & 0xFF);
            buff.cat((char)encoding[val >> 18 & 0x3F]);
            buff.cat((char)encoding[val >> 12 & 0x3F]);
            buff.cat((char)encoding[val >> 6 & 0x3F]);
            buff.cat((char)encoding[val & 0x3F]);
        }
        int remainder = value.length % 3;
        switch (remainder) {
            default: {
                break;
            }
            case 1: {
                val = (value[whole] & 0xFF) << 4;
                buff.cat((char)encoding[val >> 6 & 0x3F]);
                buff.cat((char)encoding[val & 0x3F]);
                buff.append("==");
                break;
            }
            case 2: {
                val = ((value[whole] & 0xFF) << 10) + ((value[whole + 1] & 0xFF) << 2);
                buff.cat((char)encoding[val >> 12 & 0x3F]);
                buff.cat((char)encoding[val >> 6 & 0x3F]);
                buff.cat((char)encoding[val & 0x3F]);
                buff.append("=");
                break;
            }
        }
        return buff.condense();
    }

    public static byte[] decode(CharSequence in) throws XPathException {
        char[] unit = new char[4];
        byte[] result = new byte[in.length()];
        int bytesUsed = 0;
        int i = 0;
        int u = 0;
        int pad = 0;
        int chars = 0;
        char last = '\u0000';
        while (i < in.length()) {
            char c;
            if (!Whitespace.isWhite(c = in.charAt(i++))) {
                ++chars;
                if (c == '=') {
                    pad = 1;
                    for (int k = i; k < in.length(); ++k) {
                        char ch = in.charAt(k);
                        if (ch == '=') {
                            ++pad;
                            ++chars;
                            continue;
                        }
                        if (Whitespace.isWhite(ch)) continue;
                        throw new XPathException("Base64 padding character '=' is followed by non-padding characters", "FORG0001");
                    }
                    if (pad == 1 && "AEIMQUYcgkosw048".indexOf(last) < 0) {
                        throw new XPathException("In base64, if the value ends with a single '=' character, then the preceding character must be one of [AEIMQUYcgkosw048]", "FORG0001");
                    }
                    if (pad == 2 && "AQgw".indexOf(last) < 0) {
                        throw new XPathException("In base64, if the value ends with '==', then the preceding character must be one of [AQgw]", "FORG0001");
                    }
                    if (pad > 2) {
                        throw new XPathException("Found " + pad + " '=' characters at end of base64 value; max is 2", "FORG0001");
                    }
                    if (pad != (4 - u) % 4) {
                        throw new XPathException("Required " + (4 - u) % 4 + " '=' characters at end of base64 value; found " + pad, "FORG0001");
                    }
                    for (int p = 0; p < pad; ++p) {
                        unit[u++] = 65;
                    }
                    i = in.length();
                } else {
                    last = c;
                    unit[u++] = c;
                }
                if (u == 4) {
                    int t = (Base64BinaryValue.decodeChar(unit[0]) << 18) + (Base64BinaryValue.decodeChar(unit[1]) << 12) + (Base64BinaryValue.decodeChar(unit[2]) << 6) + Base64BinaryValue.decodeChar(unit[3]);
                    if (bytesUsed + 3 > result.length) {
                        byte[] r2 = new byte[bytesUsed * 2];
                        System.arraycopy(result, 0, r2, 0, bytesUsed);
                        result = r2;
                    }
                    result[bytesUsed++] = (byte)(t >> 16 & 0xFF);
                    result[bytesUsed++] = (byte)(t >> 8 & 0xFF);
                    result[bytesUsed++] = (byte)(t & 0xFF);
                    u = 0;
                }
            }
            if (i < in.length()) continue;
            bytesUsed -= pad;
            break;
        }
        if (chars % 4 != 0) {
            throw new XPathException("Length of base64 value must be a multiple of four", "FORG0001");
        }
        byte[] r3 = new byte[bytesUsed];
        System.arraycopy(result, 0, r3, 0, bytesUsed);
        return r3;
    }

    private static int decodeChar(char c) throws XPathException {
        int d;
        int n = d = c < '\u0080' ? decoding[c] : -1;
        if (d == -1) {
            if (UTF16CharacterSet.isSurrogate(c)) {
                throw new XPathException("Invalid character (surrogate pair) in base64 value", "FORG0001");
            }
            throw new XPathException("Invalid character '" + c + "' in base64 value", "FORG0001");
        }
        return d;
    }

    public int compareTo(Object o) {
        byte[] other = ((Base64BinaryValue)o).binaryValue;
        int len0 = this.binaryValue.length;
        int len1 = other.length;
        int shorter = Math.min(len0, len1);
        for (int i = 0; i < shorter; ++i) {
            int a = this.binaryValue[i] & 0xFF;
            int b = other[i] & 0xFF;
            if (a == b) continue;
            return a < b ? -1 : 1;
        }
        return Integer.signum(len0 - len1);
    }

    static {
        Arrays.fill(decoding, -1);
        int i = 0;
        while (i < alphabet.length()) {
            char c = alphabet.charAt(i);
            Base64BinaryValue.encoding[i] = c;
            Base64BinaryValue.decoding[c] = i++;
        }
    }

    private class Base64BinaryComparable
    implements Comparable {
        private Base64BinaryComparable() {
        }

        public Base64BinaryValue getBase64BinaryValue() {
            return Base64BinaryValue.this;
        }

        public int compareTo(Object o) {
            if (o instanceof Base64BinaryComparable && Arrays.equals(this.getBase64BinaryValue().binaryValue, ((Base64BinaryComparable)o).getBase64BinaryValue().binaryValue)) {
                return 0;
            }
            return Integer.MIN_VALUE;
        }

        public boolean equals(Object o) {
            return this.compareTo(o) == 0;
        }

        public int hashCode() {
            return Base64BinaryValue.this.hashCode();
        }
    }
}

