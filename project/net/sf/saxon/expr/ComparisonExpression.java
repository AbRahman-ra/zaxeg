/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.sort.AtomicComparer;

public interface ComparisonExpression {
    public AtomicComparer getAtomicComparer();

    public int getSingletonOperator();

    public Operand getLhs();

    public Operand getRhs();

    public Expression getLhsExpression();

    public Expression getRhsExpression();

    public boolean convertsUntypedToOther();
}

