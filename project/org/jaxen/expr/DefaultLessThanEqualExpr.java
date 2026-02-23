/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.expr.DefaultRelationalExpr;
import org.jaxen.expr.Expr;

class DefaultLessThanEqualExpr
extends DefaultRelationalExpr {
    private static final long serialVersionUID = 7980276649555334242L;

    DefaultLessThanEqualExpr(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    public String getOperator() {
        return "<=";
    }

    protected boolean evaluateDoubleDouble(Double lhs, Double rhs) {
        return lhs <= rhs;
    }
}

