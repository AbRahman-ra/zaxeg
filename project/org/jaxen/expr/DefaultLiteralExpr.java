/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.Context;
import org.jaxen.expr.DefaultExpr;
import org.jaxen.expr.LiteralExpr;

class DefaultLiteralExpr
extends DefaultExpr
implements LiteralExpr {
    private static final long serialVersionUID = -953829179036273338L;
    private String literal;

    DefaultLiteralExpr(String literal) {
        this.literal = literal;
    }

    public String getLiteral() {
        return this.literal;
    }

    public String toString() {
        return "[(DefaultLiteralExpr): " + this.getLiteral() + "]";
    }

    public String getText() {
        if (this.literal.indexOf(34) == -1) {
            return "\"" + this.getLiteral() + "\"";
        }
        return "'" + this.getLiteral() + "'";
    }

    public Object evaluate(Context context) {
        return this.getLiteral();
    }
}

