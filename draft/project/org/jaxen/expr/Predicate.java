/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import java.io.Serializable;
import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.expr.Expr;

public interface Predicate
extends Serializable {
    public Expr getExpr();

    public void setExpr(Expr var1);

    public void simplify();

    public String getText();

    public Object evaluate(Context var1) throws JaxenException;
}

