/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import java.util.List;
import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.expr.DefaultExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.XPathExpr;

public class DefaultXPathExpr
implements XPathExpr {
    private static final long serialVersionUID = 3007613096320896040L;
    private Expr rootExpr;

    public DefaultXPathExpr(Expr rootExpr) {
        this.rootExpr = rootExpr;
    }

    public Expr getRootExpr() {
        return this.rootExpr;
    }

    public void setRootExpr(Expr rootExpr) {
        this.rootExpr = rootExpr;
    }

    public String toString() {
        return "[(DefaultXPath): " + this.getRootExpr() + "]";
    }

    public String getText() {
        return this.getRootExpr().getText();
    }

    public void simplify() {
        this.setRootExpr(this.getRootExpr().simplify());
    }

    public List asList(Context context) throws JaxenException {
        Expr expr = this.getRootExpr();
        Object value = expr.evaluate(context);
        List result = DefaultExpr.convertToList(value);
        return result;
    }
}

