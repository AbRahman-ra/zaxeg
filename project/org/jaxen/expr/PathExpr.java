/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;

public interface PathExpr
extends Expr {
    public Expr getFilterExpr();

    public void setFilterExpr(Expr var1);

    public LocationPath getLocationPath();
}

