/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.expr.DefaultRelationalExpr;
import org.jaxen.expr.Expr;

class DefaultGreaterThanEqualExpr
extends DefaultRelationalExpr {
    private static final long serialVersionUID = -7848747981787197470L;

    DefaultGreaterThanEqualExpr(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    public String getOperator() {
        return ">=";
    }

    protected boolean evaluateDoubleDouble(Double lhs, Double rhs) {
        return lhs.compareTo(rhs) >= 0;
    }
}

