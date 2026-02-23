/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.expr.Expr;

public interface VariableReferenceExpr
extends Expr {
    public String getPrefix();

    public String getVariableName();
}

