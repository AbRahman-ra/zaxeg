/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.regex.BMPString;
import net.sf.saxon.regex.EmptyString;
import net.sf.saxon.regex.LatinString;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.util.CharSequenceConsumer;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.UntypedAtomicValue;

public class StringValue
extends AtomicValue {
    public static final StringValue EMPTY_STRING = new StringValue(EmptyString.THE_INSTANCE);
    public static final StringValue SINGLE_SPACE = new StringValue(LatinString.SINGLE_SPACE);
    public static final StringValue TRUE = new StringValue(new LatinString("true"));
    public static final StringValue FALSE = new StringValue(new LatinString("false"));
    protected CharSequence value;

    protected StringValue() {
        this.value = "";
        this.typeLabel = BuiltInAtomicType.STRING;
    }

    public StringValue(CharSequence value) {
        this.value = value == null ? "" : value;
        this.typeLabel = BuiltInAtomicType.STRING;
    }

    public StringValue(CharSequence value, AtomicType typeLabel) {
        this.value = value;
        this.typeLabel = typeLabel;
    }

    public synchronized void setContainsNoSurrogates() {
        if (!(this.value instanceof BMPString || this.value instanceof LatinString || this.value instanceof EmptyString)) {
            this.value = new BMPString(this.value);
        }
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        StringValue v = new StringValue(this.value);
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.STRING;
    }

    public static StringValue makeStringValue(CharSequence value) {
        if (value == null || value.length() == 0) {
            return EMPTY_STRING;
        }
        return new StringValue(value);
    }

    public static boolean isEmpty(CharSequence string) {
        if (string instanceof String) {
            return ((String)string).isEmpty();
        }
        if (string instanceof UnicodeString) {
            return ((UnicodeString)string).uLength() == 0;
        }
        return string.length() == 0;
    }

    @Override
    public final CharSequence getPrimitiveStringValue() {
        return this.value;
    }

    public final void setStringValueCS(CharSequence value) {
        this.value = value;
    }

    public synchronized int getStringLength() {
        if (!(this.value instanceof UnicodeString)) {
            this.makeUnicodeString();
        }
        return ((UnicodeString)this.value).uLength();
    }

    public synchronized int getStringLengthUpperBound() {
        if (this.value instanceof UnicodeString) {
            return ((UnicodeString)this.value).uLength();
        }
        return this.value.length();
    }

    public synchronized UnicodeString getUnicodeString() {
        if (!(this.value instanceof UnicodeString)) {
            this.makeUnicodeString();
        }
        return (UnicodeString)this.value;
    }

    private void makeUnicodeString() {
        this.value = UnicodeString.makeUnicodeString(this.value);
    }

    public static int getStringLength(CharSequence s) {
        if (s instanceof UnicodeString) {
            return ((UnicodeString)s).uLength();
        }
        int n = 0;
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c >= '\ud800' && c <= '\udbff') continue;
            ++n;
        }
        return n;
    }

    public boolean isZeroLength() {
        return this.value.length() == 0;
    }

    public boolean containsSurrogatePairs() {
        return UnicodeString.containsSurrogatePairs(this.value);
    }

    public boolean isKnownToContainNoSurrogates() {
        return this.value instanceof BMPString || this.value instanceof LatinString || this.value instanceof EmptyString;
    }

    public synchronized AtomicIterator<Int64Value> iterateCharacters() {
        if (this.value instanceof UnicodeString) {
            return new UnicodeCharacterIterator((UnicodeString)this.value);
        }
        return new CharacterIterator(this.value);
    }

    public static int[] expand(CharSequence s) {
        int[] array = new int[StringValue.getStringLength(s)];
        int o = 0;
        for (int i = 0; i < s.length(); ++i) {
            int charval;
            int c = s.charAt(i);
            if (c >= 55296 && c <= 56319) {
                charval = (c - 55296) * 1024 + (s.charAt(i + 1) - 56320) + 65536;
                ++i;
            } else {
                charval = c;
            }
            array[o++] = charval;
        }
        return array;
    }

    public static CharSequence contract(int[] codes, int used) {
        FastStringBuffer sb = new FastStringBuffer(codes.length);
        for (int i = 0; i < used; ++i) {
            sb.appendWideChar(codes[i]);
        }
        return sb;
    }

    @Override
    public AtomicMatchKey getXPathComparable(boolean ordered, StringCollator collator, int implicitTimezone) {
        return collator.getCollationKey(this.value);
    }

    @Override
    public boolean equals(Object other) {
        throw new ClassCastException("equals on StringValue is not allowed");
    }

    public int hashCode() {
        return this.value.hashCode();
    }

    public boolean codepointEquals(StringValue other) {
        if (this.value instanceof String) {
            return ((String)this.value).contentEquals(other.value);
        }
        if (other.value instanceof String) {
            return ((String)other.value).contentEquals(this.value);
        }
        if (this.value instanceof UnicodeString) {
            if (!(other.value instanceof UnicodeString)) {
                other.makeUnicodeString();
            }
            return this.value.equals(other.value);
        }
        return this.value.length() == other.value.length() && this.value.toString().equals(other.value.toString());
    }

    @Override
    public boolean effectiveBooleanValue() {
        return !this.isZeroLength();
    }

    @Override
    public String toString() {
        return "\"" + this.value + '\"';
    }

    @Override
    public String toShortString() {
        String s = this.value.toString();
        if (s.length() > 40) {
            s = s.substring(0, 35) + "...";
        }
        return "\"" + s + '\"';
    }

    @Override
    public Comparable getSchemaComparable() {
        return this.getStringValue();
    }

    @Override
    public boolean isIdentical(AtomicValue v) {
        return v instanceof StringValue && this instanceof AnyURIValue == v instanceof AnyURIValue && this instanceof UntypedAtomicValue == v instanceof UntypedAtomicValue && this.codepointEquals((StringValue)v);
    }

    public static String diagnosticDisplay(String s) {
        FastStringBuffer fsb = new FastStringBuffer(s.length());
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            if (c >= ' ' && c <= '~') {
                fsb.cat(c);
                continue;
            }
            fsb.append("\\u");
            for (int shift = 12; shift >= 0; shift -= 4) {
                fsb.cat("0123456789ABCDEF".charAt(c >> shift & 0xF));
            }
        }
        return fsb.toString();
    }

    public static final class Builder
    implements CharSequenceConsumer {
        FastStringBuffer buffer = new FastStringBuffer(256);

        @Override
        public CharSequenceConsumer cat(CharSequence chars) {
            return this.buffer.cat(chars);
        }

        @Override
        public CharSequenceConsumer cat(char c) {
            return this.buffer.cat(c);
        }

        public StringValue getStringValue() {
            return new StringValue(this.buffer.condense());
        }
    }

    public static final class UnicodeCharacterIterator
    implements AtomicIterator<Int64Value> {
        UnicodeString uValue;
        int inpos = 0;

        public UnicodeCharacterIterator(UnicodeString value) {
            this.uValue = value;
        }

        @Override
        public Int64Value next() {
            if (this.inpos < this.uValue.uLength()) {
                return new Int64Value(this.uValue.uCharAt(this.inpos++));
            }
            return null;
        }
    }

    public static final class CharacterIterator
    implements AtomicIterator<Int64Value> {
        int inpos = 0;
        private CharSequence value;

        public CharacterIterator(CharSequence value) {
            this.value = value;
        }

        @Override
        public Int64Value next() {
            if (this.inpos < this.value.length()) {
                int current;
                int c;
                if ((c = this.value.charAt(this.inpos++)) >= 55296 && c <= 56319) {
                    try {
                        current = (c - 55296) * 1024 + (this.value.charAt(this.inpos++) - 56320) + 65536;
                    } catch (StringIndexOutOfBoundsException e) {
                        System.err.println("Invalid surrogate at end of string");
                        System.err.println(StringValue.diagnosticDisplay(this.value.toString()));
                        e.printStackTrace();
                        throw e;
                    }
                } else {
                    current = c;
                }
                return new Int64Value(current);
            }
            return null;
        }
    }
}

