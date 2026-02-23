/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.expr.DefaultEqualityExpr;
import org.jaxen.expr.Expr;

class DefaultNotEqualsExpr
extends DefaultEqualityExpr {
    private static final long serialVersionUID = -8001267398136979152L;

    DefaultNotEqualsExpr(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    public String getOperator() {
        return "!=";
    }

    public String toString() {
        return "[(DefaultNotEqualsExpr): " + this.getLHS() + ", " + this.getRHS() + "]";
    }

    protected boolean evaluateObjectObject(Object lhs, Object rhs) {
        if (this.eitherIsNumber(lhs, rhs)) {
            Double left = (Double)lhs;
            Double right = (Double)rhs;
            return left.doubleValue() != right.doubleValue();
        }
        return !lhs.equals(rhs);
    }
}

