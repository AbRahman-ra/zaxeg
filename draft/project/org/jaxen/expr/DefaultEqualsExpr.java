/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.expr.DefaultEqualityExpr;
import org.jaxen.expr.Expr;

class DefaultEqualsExpr
extends DefaultEqualityExpr {
    private static final long serialVersionUID = -8327599812627931648L;

    DefaultEqualsExpr(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    public String getOperator() {
        return "=";
    }

    public String toString() {
        return "[(DefaultEqualsExpr): " + this.getLHS() + ", " + this.getRHS() + "]";
    }

    protected boolean evaluateObjectObject(Object lhs, Object rhs) {
        if (this.eitherIsNumber(lhs, rhs)) {
            Double left = (Double)lhs;
            Double right = (Double)rhs;
            return left.doubleValue() == right.doubleValue();
        }
        return lhs.equals(rhs);
    }
}

