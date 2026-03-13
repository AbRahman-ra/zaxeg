/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.FLWORExpression;
import net.sf.saxon.trace.TraceableComponent;

public interface CodeInjector {
    default public Expression inject(Expression exp) {
        return exp;
    }

    default public void process(TraceableComponent component) {
    }

    default public Clause injectClause(FLWORExpression expression, Clause clause) {
        return clause;
    }
}

