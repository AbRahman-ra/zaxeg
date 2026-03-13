/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import java.util.function.IntPredicate;

public class IntExceptPredicate
implements IntPredicate {
    private IntPredicate p1;
    private IntPredicate p2;

    public IntExceptPredicate(IntPredicate p1, IntPredicate p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public boolean test(int value) {
        return this.p1.test(value) && !this.p2.test(value);
    }

    public IntPredicate[] getOperands() {
        return new IntPredicate[]{this.p1, this.p2};
    }
}

