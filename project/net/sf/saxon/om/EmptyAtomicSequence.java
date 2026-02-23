/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.Collections;
import java.util.Iterator;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.value.AtomicValue;

public enum EmptyAtomicSequence implements AtomicSequence
{
    INSTANCE;


    public static EmptyAtomicSequence getInstance() {
        return INSTANCE;
    }

    @Override
    public AtomicValue head() {
        return null;
    }

    @Override
    public AtomicIterator<AtomicValue> iterate() {
        return EmptyIterator.ofAtomic();
    }

    @Override
    public AtomicValue itemAt(int n) {
        return null;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public CharSequence getCanonicalLexicalRepresentation() {
        return "";
    }

    public Comparable getSchemaComparable() {
        return null;
    }

    @Override
    public CharSequence getStringValueCS() {
        return "";
    }

    @Override
    public String getStringValue() {
        return "";
    }

    @Override
    public EmptyAtomicSequence subsequence(int start, int length) {
        return this;
    }

    @Override
    public boolean effectiveBooleanValue() {
        return false;
    }

    @Override
    public EmptyAtomicSequence reduce() {
        return this;
    }

    @Override
    public Iterator<AtomicValue> iterator() {
        return Collections.emptyList().iterator();
    }
}

