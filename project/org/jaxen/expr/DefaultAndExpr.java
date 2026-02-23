/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.Navigator;
import org.jaxen.expr.DefaultLogicalExpr;
import org.jaxen.expr.Expr;
import org.jaxen.function.BooleanFunction;

class DefaultAndExpr
extends DefaultLogicalExpr {
    private static final long serialVersionUID = -5237984010263103742L;

    DefaultAndExpr(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    public String getOperator() {
        return "and";
    }

    public String toString() {
        return "[(DefaultAndExpr): " + this.getLHS() + ", " + this.getRHS() + "]";
    }

    public Object evaluate(Context context) throws JaxenException {
        Navigator nav = context.getNavigator();
        Boolean lhsValue = BooleanFunction.evaluate(this.getLHS().evaluate(context), nav);
        if (!lhsValue.booleanValue()) {
            return Boolean.FALSE;
        }
        Boolean rhsValue = BooleanFunction.evaluate(this.getRHS().evaluate(context), nav);
        if (!rhsValue.booleanValue()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}

