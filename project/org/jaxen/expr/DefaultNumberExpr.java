/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.Context;
import org.jaxen.expr.DefaultExpr;
import org.jaxen.expr.NumberExpr;

class DefaultNumberExpr
extends DefaultExpr
implements NumberExpr {
    private static final long serialVersionUID = -6021898973386269611L;
    private Double number;

    DefaultNumberExpr(Double number) {
        this.number = number;
    }

    public Number getNumber() {
        return this.number;
    }

    public String toString() {
        return "[(DefaultNumberExpr): " + this.getNumber() + "]";
    }

    public String getText() {
        return this.getNumber().toString();
    }

    public Object evaluate(Context context) {
        return this.getNumber();
    }
}

