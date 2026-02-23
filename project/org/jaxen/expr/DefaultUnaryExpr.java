/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.expr.DefaultExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.UnaryExpr;
import org.jaxen.function.NumberFunction;

class DefaultUnaryExpr
extends DefaultExpr
implements UnaryExpr {
    private static final long serialVersionUID = 2303714238683092334L;
    private Expr expr;

    DefaultUnaryExpr(Expr expr) {
        this.expr = expr;
    }

    public Expr getExpr() {
        return this.expr;
    }

    public String toString() {
        return "[(DefaultUnaryExpr): " + this.getExpr() + "]";
    }

    public String getText() {
        return "-(" + this.getExpr().getText() + ")";
    }

    public Expr simplify() {
        this.expr = this.expr.simplify();
        return this;
    }

    public Object evaluate(Context context) throws JaxenException {
        Double number = NumberFunction.evaluate(this.getExpr().evaluate(context), context.getNavigator());
        return new Double(number * -1.0);
    }
}

