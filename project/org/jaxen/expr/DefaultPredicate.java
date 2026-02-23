/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.expr.Expr;
import org.jaxen.expr.Predicate;

class DefaultPredicate
implements Predicate {
    private static final long serialVersionUID = -4140068594075364971L;
    private Expr expr;

    DefaultPredicate(Expr expr) {
        this.setExpr(expr);
    }

    public Expr getExpr() {
        return this.expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    public String getText() {
        return "[" + this.getExpr().getText() + "]";
    }

    public String toString() {
        return "[(DefaultPredicate): " + this.getExpr() + "]";
    }

    public void simplify() {
        this.setExpr(this.getExpr().simplify());
    }

    public Object evaluate(Context context) throws JaxenException {
        return this.getExpr().evaluate(context);
    }
}

