/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import java.util.Iterator;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.Navigator;
import org.jaxen.expr.DefaultTruthExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.RelationalExpr;
import org.jaxen.function.NumberFunction;

abstract class DefaultRelationalExpr
extends DefaultTruthExpr
implements RelationalExpr {
    DefaultRelationalExpr(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    public String toString() {
        return "[(DefaultRelationalExpr): " + this.getLHS() + ", " + this.getRHS() + "]";
    }

    public Object evaluate(Context context) throws JaxenException {
        Object lhsValue = this.getLHS().evaluate(context);
        Object rhsValue = this.getRHS().evaluate(context);
        Navigator nav = context.getNavigator();
        if (this.bothAreSets(lhsValue, rhsValue)) {
            return this.evaluateSetSet((List)lhsValue, (List)rhsValue, nav);
        }
        if (this.eitherIsSet(lhsValue, rhsValue)) {
            if (this.isSet(lhsValue)) {
                return this.evaluateSetSet((List)lhsValue, DefaultRelationalExpr.convertToList(rhsValue), nav);
            }
            return this.evaluateSetSet(DefaultRelationalExpr.convertToList(lhsValue), (List)rhsValue, nav);
        }
        return this.evaluateObjectObject(lhsValue, rhsValue, nav) ? Boolean.TRUE : Boolean.FALSE;
    }

    private Object evaluateSetSet(List lhsSet, List rhsSet, Navigator nav) {
        if (this.setIsEmpty(lhsSet) || this.setIsEmpty(rhsSet)) {
            return Boolean.FALSE;
        }
        Iterator lhsIterator = lhsSet.iterator();
        while (lhsIterator.hasNext()) {
            Object lhs = lhsIterator.next();
            Iterator rhsIterator = rhsSet.iterator();
            while (rhsIterator.hasNext()) {
                Object rhs = rhsIterator.next();
                if (!this.evaluateObjectObject(lhs, rhs, nav)) continue;
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    private boolean evaluateObjectObject(Object lhs, Object rhs, Navigator nav) {
        if (lhs == null || rhs == null) {
            return false;
        }
        Double lhsNum = NumberFunction.evaluate(lhs, nav);
        Double rhsNum = NumberFunction.evaluate(rhs, nav);
        if (NumberFunction.isNaN(lhsNum) || NumberFunction.isNaN(rhsNum)) {
            return false;
        }
        return this.evaluateDoubleDouble(lhsNum, rhsNum);
    }

    protected abstract boolean evaluateDoubleDouble(Double var1, Double var2);
}

