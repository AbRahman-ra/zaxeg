/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.sort.NumericComparer;
import net.sf.saxon.value.StringToDouble11;

public class NumericComparer11
extends NumericComparer {
    private static NumericComparer11 THE_INSTANCE = new NumericComparer11();

    public static NumericComparer getInstance() {
        return THE_INSTANCE;
    }

    protected NumericComparer11() {
        this.converter = StringToDouble11.getInstance();
    }

    @Override
    public String save() {
        return "NC11";
    }
}

