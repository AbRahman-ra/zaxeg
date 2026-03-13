/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.expr.DefaultArithExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.MultiplicativeExpr;

abstract class DefaultMultiplicativeExpr
extends DefaultArithExpr
implements MultiplicativeExpr {
    DefaultMultiplicativeExpr(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    public String toString() {
        return "[(DefaultMultiplicativeExpr): " + this.getLHS() + ", " + this.getRHS() + "]";
    }
}

