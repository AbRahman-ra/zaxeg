/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.expr.sort.ComparisonException;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Whitespace;

public final class BooleanValue
extends AtomicValue
implements Comparable,
AtomicMatchKey {
    private boolean value;
    public static final BooleanValue TRUE = new BooleanValue(true);
    public static final BooleanValue FALSE = new BooleanValue(false);

    private BooleanValue(boolean value) {
        this.value = value;
        this.typeLabel = BuiltInAtomicType.BOOLEAN;
    }

    public static BooleanValue get(boolean value) {
        return value ? TRUE : FALSE;
    }

    public BooleanValue(boolean value, AtomicType typeLabel) {
        this.value = value;
        this.typeLabel = typeLabel;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        BooleanValue v = new BooleanValue(this.value);
        v.typeLabel = typeLabel;
        return v;
    }

    public static ConversionResult fromString(CharSequence s) {
        int len = (s = Whitespace.trimWhitespace(s)).length();
        if (len == 1) {
            char c = s.charAt(0);
            if (c == '1') {
                return TRUE;
            }
            if (c == '0') {
                return FALSE;
            }
        } else if (len == 4) {
            if (s.charAt(0) == 't' && s.charAt(1) == 'r' && s.charAt(2) == 'u' && s.charAt(3) == 'e') {
                return TRUE;
            }
        } else if (len == 5 && s.charAt(0) == 'f' && s.charAt(1) == 'a' && s.charAt(2) == 'l' && s.charAt(3) == 's' && s.charAt(4) == 'e') {
            return FALSE;
        }
        ValidationFailure err = new ValidationFailure("The string " + Err.wrap(s, 4) + " cannot be cast to a boolean");
        err.setErrorCode("FORG0001");
        return err;
    }

    public boolean getBooleanValue() {
        return this.value;
    }

    @Override
    public boolean effectiveBooleanValue() {
        return this.value;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.BOOLEAN;
    }

    @Override
    public String getPrimitiveStringValue() {
        return this.value ? "true" : "false";
    }

    @Override
    public Comparable getSchemaComparable() {
        return new BooleanComparable();
    }

    @Override
    public AtomicMatchKey getXPathComparable(boolean ordered, StringCollator collator, int implicitTimezone) {
        return this;
    }

    public int compareTo(Object other) {
        if (!(other instanceof BooleanValue)) {
            XPathException e = new XPathException("Boolean values are not comparable to " + other.getClass(), "XPTY0004");
            throw new ComparisonException(e);
        }
        if (this.value == ((BooleanValue)other).value) {
            return 0;
        }
        if (this.value) {
            return 1;
        }
        return -1;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BooleanValue && this.value == ((BooleanValue)other).value;
    }

    public int hashCode() {
        return this.value ? 0 : 1;
    }

    @Override
    public String toString() {
        return this.getStringValue() + "()";
    }

    private class BooleanComparable
    implements Comparable {
        private BooleanComparable() {
        }

        public boolean asBoolean() {
            return BooleanValue.this.getBooleanValue();
        }

        public int compareTo(Object o) {
            return this.equals(o) ? 0 : Integer.MIN_VALUE;
        }

        public boolean equals(Object o) {
            return o instanceof BooleanComparable && this.asBoolean() == ((BooleanComparable)o).asBoolean();
        }

        public int hashCode() {
            return this.asBoolean() ? 9999999 : 8888888;
        }
    }
}

