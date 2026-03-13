/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.Expression;

public interface ContextSwitchingExpression
extends ContextOriginator {
    public Expression getSelectExpression();

    public Expression getActionExpression();
}

