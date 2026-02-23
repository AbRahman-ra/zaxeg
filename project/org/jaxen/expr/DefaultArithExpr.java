/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.expr.DefaultBinaryExpr;
import org.jaxen.expr.Expr;

abstract class DefaultArithExpr
extends DefaultBinaryExpr {
    DefaultArithExpr(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    public String toString() {
        return "[(DefaultArithExpr): " + this.getLHS() + ", " + this.getRHS() + "]";
    }
}

