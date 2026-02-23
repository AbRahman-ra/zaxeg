/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import net.sf.saxon.type.StringToDouble;

public class StringToDouble11
extends StringToDouble {
    private static StringToDouble11 THE_INSTANCE = new StringToDouble11();

    public static StringToDouble11 getInstance() {
        return THE_INSTANCE;
    }

    protected StringToDouble11() {
    }

    @Override
    protected double signedPositiveInfinity() {
        return Double.POSITIVE_INFINITY;
    }
}

