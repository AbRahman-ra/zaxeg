/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

public class UntypedAtomicValue
extends StringValue {
    public static final UntypedAtomicValue ZERO_LENGTH_UNTYPED = new UntypedAtomicValue("");

    public UntypedAtomicValue(CharSequence value) {
        this.value = value;
        this.typeLabel = BuiltInAtomicType.UNTYPED_ATOMIC;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        if (!typeLabel.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
            throw new UnsupportedOperationException();
        }
        return this;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.UNTYPED_ATOMIC;
    }

    @Override
    public final CharSequence getStringValueCS() {
        return this.value;
    }

    @Override
    public String toShortString() {
        return "u" + super.toShortString();
    }
}

