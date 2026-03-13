/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.expr.DefaultMultiplicativeExpr;
import org.jaxen.expr.Expr;
import org.jaxen.function.NumberFunction;

class DefaultDivExpr
extends DefaultMultiplicativeExpr {
    private static final long serialVersionUID = 6318739386201615441L;

    DefaultDivExpr(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    public String getOperator() {
        return "div";
    }

    public Object evaluate(Context context) throws JaxenException {
        Double lhsValue = NumberFunction.evaluate(this.getLHS().evaluate(context), context.getNavigator());
        Double rhsValue = NumberFunction.evaluate(this.getRHS().evaluate(context), context.getNavigator());
        double result = lhsValue / rhsValue;
        return new Double(result);
    }
}

