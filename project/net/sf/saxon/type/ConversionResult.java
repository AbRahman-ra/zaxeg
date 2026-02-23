/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.type.ValidationException;
import net.sf.saxon.value.AtomicValue;

public interface ConversionResult {
    public AtomicValue asAtomic() throws ValidationException;
}

