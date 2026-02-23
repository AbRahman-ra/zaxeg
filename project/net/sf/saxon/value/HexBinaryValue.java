/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.util.Arrays;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.Whitespace;

public class HexBinaryValue
extends AtomicValue
implements AtomicMatchKey,
Comparable {
    private byte[] binaryValue;

    public HexBinaryValue(CharSequence in) throws XPathException {
        CharSequence s = Whitespace.trimWhitespace(in);
        if ((s.length() & 1) != 0) {
            XPathException err = new XPathException("A hexBinary value must contain an even number of characters");
            err.setErrorCode("FORG0001");
            throw err;
        }
        this.binaryValue = new byte[s.length() / 2];
        for (int i = 0; i < this.binaryValue.length; ++i) {
            this.binaryValue[i] = (byte)((this.fromHex(s.charAt(2 * i)) << 4) + this.fromHex(s.charAt(2 * i + 1)));
        }
        this.typeLabel = BuiltInAtomicType.HEX_BINARY;
    }

    public HexBinaryValue(CharSequence s, AtomicType type) {
        if ((s.length() & 1) != 0) {
            throw new IllegalArgumentException("A hexBinary value must contain an even number of characters");
        }
        this.binaryValue = new byte[s.length() / 2];
        try {
            for (int i = 0; i < this.binaryValue.length; ++i) {
                this.binaryValue[i] = (byte)((this.fromHex(s.charAt(2 * i)) << 4) + this.fromHex(s.charAt(2 * i + 1)));
            }
        } catch (XPathException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        this.typeLabel = type;
    }

    public HexBinaryValue(byte[] value) {
        this.binaryValue = value;
        this.typeLabel = BuiltInAtomicType.HEX_BINARY;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        HexBinaryValue v = new HexBinaryValue(this.binaryValue);
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.HEX_BINARY;
    }

    public byte[] getBinaryValue() {
        return this.binaryValue;
    }

    private int fromHex(char c) throws XPathException {
        int d = "0123456789ABCDEFabcdef".indexOf(c);
        if (d > 15) {
            d -= 6;
        }
        if (d < 0) {
            XPathException err = new XPathException("Invalid hexadecimal digit '" + c + "'");
            err.setErrorCode("FORG0001");
            throw err;
        }
        return d;
    }

    @Override
    public CharSequence getPrimitiveStringValue() {
        String digits = "0123456789ABCDEF";
        FastStringBuffer sb = new FastStringBuffer(this.binaryValue.length * 2);
        for (byte aBinaryValue : this.binaryValue) {
            sb.cat(digits.charAt(aBinaryValue >> 4 & 0xF));
            sb.cat(digits.charAt(aBinaryValue & 0xF));
        }
        return sb;
    }

    public int getLengthInOctets() {
        return this.binaryValue.length;
    }

    @Override
    public Comparable<HexBinaryComparable> getSchemaComparable() {
        return new HexBinaryComparable();
    }

    @Override
    public AtomicMatchKey getXPathComparable(boolean ordered, StringCollator collator, int implicitTimezone) {
        return this;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof HexBinaryValue && Arrays.equals(this.binaryValue, ((HexBinaryValue)other).binaryValue);
    }

    public int hashCode() {
        return Base64BinaryValue.byteArrayHashCode(this.binaryValue);
    }

    public int compareTo(Object o) {
        byte[] other = ((HexBinaryValue)o).binaryValue;
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

    private class HexBinaryComparable
    implements Comparable<HexBinaryComparable> {
        private HexBinaryComparable() {
        }

        public HexBinaryValue getHexBinaryValue() {
            return HexBinaryValue.this;
        }

        @Override
        public int compareTo(HexBinaryComparable o) {
            if (Arrays.equals(this.getHexBinaryValue().binaryValue, o.getHexBinaryValue().binaryValue)) {
                return 0;
            }
            return Integer.MIN_VALUE;
        }

        public boolean equals(Object o) {
            return o instanceof HexBinaryComparable && this.compareTo((HexBinaryComparable)o) == 0;
        }

        public int hashCode() {
            return HexBinaryValue.this.hashCode();
        }
    }
}

