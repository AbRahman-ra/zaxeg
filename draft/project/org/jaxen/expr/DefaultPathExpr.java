/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.expr.DefaultExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.PathExpr;

class DefaultPathExpr
extends DefaultExpr
implements PathExpr {
    private static final long serialVersionUID = -6593934674727004281L;
    private Expr filterExpr;
    private LocationPath locationPath;

    DefaultPathExpr(Expr filterExpr, LocationPath locationPath) {
        this.filterExpr = filterExpr;
        this.locationPath = locationPath;
    }

    public Expr getFilterExpr() {
        return this.filterExpr;
    }

    public void setFilterExpr(Expr filterExpr) {
        this.filterExpr = filterExpr;
    }

    public LocationPath getLocationPath() {
        return this.locationPath;
    }

    public String toString() {
        if (this.getLocationPath() != null) {
            return "[(DefaultPathExpr): " + this.getFilterExpr() + ", " + this.getLocationPath() + "]";
        }
        return "[(DefaultPathExpr): " + this.getFilterExpr() + "]";
    }

    public String getText() {
        StringBuffer buf = new StringBuffer();
        if (this.getFilterExpr() != null) {
            buf.append(this.getFilterExpr().getText());
        }
        if (this.getLocationPath() != null) {
            if (!this.getLocationPath().getSteps().isEmpty()) {
                buf.append("/");
            }
            buf.append(this.getLocationPath().getText());
        }
        return buf.toString();
    }

    public Expr simplify() {
        if (this.getFilterExpr() != null) {
            this.setFilterExpr(this.getFilterExpr().simplify());
        }
        if (this.getLocationPath() != null) {
            this.getLocationPath().simplify();
        }
        if (this.getFilterExpr() == null && this.getLocationPath() == null) {
            return null;
        }
        if (this.getLocationPath() == null) {
            return this.getFilterExpr();
        }
        if (this.getFilterExpr() == null) {
            return this.getLocationPath();
        }
        return this;
    }

    public Object evaluate(Context context) throws JaxenException {
        Object results = null;
        Context pathContext = null;
        if (this.getFilterExpr() != null) {
            results = this.getFilterExpr().evaluate(context);
            pathContext = new Context(context.getContextSupport());
            pathContext.setNodeSet(DefaultPathExpr.convertToList(results));
        }
        if (this.getLocationPath() != null) {
            return this.getLocationPath().evaluate(pathContext);
        }
        return results;
    }
}

