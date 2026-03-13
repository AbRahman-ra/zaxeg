/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.expr.BinaryExpr;
import org.jaxen.expr.DefaultExpr;
import org.jaxen.expr.Expr;

abstract class DefaultBinaryExpr
extends DefaultExpr
implements BinaryExpr {
    private Expr lhs;
    private Expr rhs;

    DefaultBinaryExpr(Expr lhs, Expr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Expr getLHS() {
        return this.lhs;
    }

    public Expr getRHS() {
        return this.rhs;
    }

    public void setLHS(Expr lhs) {
        this.lhs = lhs;
    }

    public void setRHS(Expr rhs) {
        this.rhs = rhs;
    }

    public abstract String getOperator();

    public String getText() {
        return "(" + this.getLHS().getText() + " " + this.getOperator() + " " + this.getRHS().getText() + ")";
    }

    public String toString() {
        return "[" + this.getClass().getName() + ": " + this.getLHS() + ", " + this.getRHS() + "]";
    }

    public Expr simplify() {
        this.setLHS(this.getLHS().simplify());
        this.setRHS(this.getRHS().simplify());
        return this;
    }
}

