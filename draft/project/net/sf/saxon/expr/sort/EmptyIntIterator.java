/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.z.IntIterator;

public class EmptyIntIterator
implements IntIterator {
    private static EmptyIntIterator THE_INSTANCE = new EmptyIntIterator();

    public static EmptyIntIterator getInstance() {
        return THE_INSTANCE;
    }

    private EmptyIntIterator() {
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public int next() {
        return 0;
    }
}

