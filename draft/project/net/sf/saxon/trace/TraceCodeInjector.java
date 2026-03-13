/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.FLWORExpression;
import net.sf.saxon.expr.flwor.TraceClause;
import net.sf.saxon.expr.instruct.ComponentTracer;
import net.sf.saxon.expr.instruct.TraceExpression;
import net.sf.saxon.expr.parser.CodeInjector;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.trace.TraceableComponent;

public class TraceCodeInjector
implements CodeInjector {
    @Override
    public Expression inject(Expression exp) {
        if (!(exp instanceof TraceExpression) && this.isApplicable(exp)) {
            return new TraceExpression(exp);
        }
        return exp;
    }

    protected boolean isApplicable(Expression exp) {
        return false;
    }

    @Override
    public void process(TraceableComponent component) {
        Expression newBody = ExpressionTool.injectCode(component.getBody(), this);
        component.setBody(newBody);
        ComponentTracer trace = new ComponentTracer(component);
        component.setBody(trace);
    }

    @Override
    public Clause injectClause(FLWORExpression expression, Clause clause) {
        return new TraceClause(expression, clause);
    }
}

