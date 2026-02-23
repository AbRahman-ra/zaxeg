/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.type.TypeHierarchy;

public interface Negatable {
    public boolean isNegatable(TypeHierarchy var1);

    public Expression negate();
}

