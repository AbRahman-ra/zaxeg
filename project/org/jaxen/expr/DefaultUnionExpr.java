/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.XPathSyntaxException;
import org.jaxen.expr.DefaultBinaryExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.NodeComparator;
import org.jaxen.expr.UnionExpr;

public class DefaultUnionExpr
extends DefaultBinaryExpr
implements UnionExpr {
    private static final long serialVersionUID = 7629142718276852707L;

    public DefaultUnionExpr(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    public String getOperator() {
        return "|";
    }

    public String toString() {
        return "[(DefaultUnionExpr): " + this.getLHS() + ", " + this.getRHS() + "]";
    }

    public Object evaluate(Context context) throws JaxenException {
        ArrayList results = new ArrayList();
        try {
            List lhsResults = (List)this.getLHS().evaluate(context);
            List rhsResults = (List)this.getRHS().evaluate(context);
            HashSet unique = new HashSet();
            results.addAll(lhsResults);
            unique.addAll(lhsResults);
            Iterator rhsIter = rhsResults.iterator();
            while (rhsIter.hasNext()) {
                Object each = rhsIter.next();
                if (unique.contains(each)) continue;
                results.add(each);
                unique.add(each);
            }
            Collections.sort(results, new NodeComparator(context.getNavigator()));
            return results;
        } catch (ClassCastException e) {
            throw new XPathSyntaxException(this.getText(), context.getPosition(), "Unions are only allowed over node-sets");
        }
    }
}

