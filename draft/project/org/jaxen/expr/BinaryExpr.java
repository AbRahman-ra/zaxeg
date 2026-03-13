/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.expr.Expr;

public interface BinaryExpr
extends Expr {
    public Expr getLHS();

    public Expr getRHS();

    public String getOperator();
}

