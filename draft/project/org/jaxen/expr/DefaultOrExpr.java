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

class DefaultOrExpr
extends DefaultLogicalExpr {
    private static final long serialVersionUID = 4894552680753026730L;

    DefaultOrExpr(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    public String getOperator() {
        return "or";
    }

    public String toString() {
        return "[(DefaultOrExpr): " + this.getLHS() + ", " + this.getRHS() + "]";
    }

    public Object evaluate(Context context) throws JaxenException {
        Navigator nav = context.getNavigator();
        Boolean lhsValue = BooleanFunction.evaluate(this.getLHS().evaluate(context), nav);
        if (lhsValue.booleanValue()) {
            return Boolean.TRUE;
        }
        Boolean rhsValue = BooleanFunction.evaluate(this.getRHS().evaluate(context), nav);
        if (rhsValue.booleanValue()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}

