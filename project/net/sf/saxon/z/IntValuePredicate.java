/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import java.util.function.IntPredicate;

public class IntValuePredicate
implements IntPredicate {
    private int target;

    public IntValuePredicate(int target) {
        this.target = target;
    }

    @Override
    public boolean test(int value) {
        return value == this.target;
    }

    public int getTarget() {
        return this.target;
    }
}

