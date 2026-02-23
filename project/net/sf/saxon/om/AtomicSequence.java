/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.value.AtomicValue;

public interface AtomicSequence
extends GroundedValue,
Iterable<AtomicValue> {
    @Override
    public AtomicValue head();

    @Override
    public AtomicIterator iterate();

    @Override
    public AtomicValue itemAt(int var1);

    @Override
    public int getLength();

    public CharSequence getCanonicalLexicalRepresentation();

    public Comparable<?> getSchemaComparable();

    @Override
    public CharSequence getStringValueCS();

    @Override
    public String getStringValue();
}

