/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import net.sf.saxon.z.IntIterator;

public interface IntToIntMap {
    public void setDefaultValue(int var1);

    public int getDefaultValue();

    public void clear();

    public boolean find(int var1);

    public int get(int var1);

    public int size();

    public boolean remove(int var1);

    public void put(int var1, int var2);

    public IntIterator keyIterator();
}

