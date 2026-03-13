/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.expr.DefaultMultiplicativeExpr;
import org.jaxen.expr.Expr;
import org.jaxen.function.NumberFunction;

class DefaultModExpr
extends DefaultMultiplicativeExpr {
    private static final long serialVersionUID = -5554964716492040687L;

    DefaultModExpr(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    public String getOperator() {
        return "mod";
    }

    public Object evaluate(Context context) throws JaxenException {
        Double lhsValue = NumberFunction.evaluate(this.getLHS().evaluate(context), context.getNavigator());
        Double rhsValue = NumberFunction.evaluate(this.getRHS().evaluate(context), context.getNavigator());
        double result = lhsValue % rhsValue;
        return new Double(result);
    }
}

