/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import java.util.ArrayList;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.expr.DefaultExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.FilterExpr;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.PredicateSet;
import org.jaxen.expr.Predicated;

public class DefaultFilterExpr
extends DefaultExpr
implements FilterExpr,
Predicated {
    private static final long serialVersionUID = -549640659288005735L;
    private Expr expr;
    private PredicateSet predicates;

    public DefaultFilterExpr(PredicateSet predicateSet) {
        this.predicates = predicateSet;
    }

    public DefaultFilterExpr(Expr expr, PredicateSet predicateSet) {
        this.expr = expr;
        this.predicates = predicateSet;
    }

    public void addPredicate(Predicate predicate) {
        this.predicates.addPredicate(predicate);
    }

    public List getPredicates() {
        return this.predicates.getPredicates();
    }

    public PredicateSet getPredicateSet() {
        return this.predicates;
    }

    public Expr getExpr() {
        return this.expr;
    }

    public String toString() {
        return "[(DefaultFilterExpr): expr: " + this.expr + " predicates: " + this.predicates + " ]";
    }

    public String getText() {
        String text = "";
        if (this.expr != null) {
            text = this.expr.getText();
        }
        text = text + this.predicates.getText();
        return text;
    }

    public Expr simplify() {
        this.predicates.simplify();
        if (this.expr != null) {
            this.expr = this.expr.simplify();
        }
        if (this.predicates.getPredicates().size() == 0) {
            return this.getExpr();
        }
        return this;
    }

    public boolean asBoolean(Context context) throws JaxenException {
        ArrayList results = null;
        if (this.expr != null) {
            results = this.expr.evaluate(context);
        } else {
            List nodeSet = context.getNodeSet();
            ArrayList list = new ArrayList(nodeSet.size());
            list.addAll(nodeSet);
            results = list;
        }
        if (results instanceof Boolean) {
            Boolean b = (Boolean)((Object)results);
            return b;
        }
        if (results instanceof List) {
            return this.getPredicateSet().evaluateAsBoolean(results, context.getContextSupport());
        }
        return false;
    }

    public Object evaluate(Context context) throws JaxenException {
        Object results = this.getExpr().evaluate(context);
        if (results instanceof List) {
            List newresults = this.getPredicateSet().evaluatePredicates((List)results, context.getContextSupport());
            results = newresults;
        }
        return results;
    }
}

