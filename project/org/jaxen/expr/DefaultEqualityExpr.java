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
import org.jaxen.expr.EqualityExpr;
import org.jaxen.expr.Expr;
import org.jaxen.function.BooleanFunction;
import org.jaxen.function.NumberFunction;
import org.jaxen.function.StringFunction;

abstract class DefaultEqualityExpr
extends DefaultTruthExpr
implements EqualityExpr {
    DefaultEqualityExpr(Expr lhs, Expr rhs) {
        super(lhs, rhs);
    }

    public String toString() {
        return "[(DefaultEqualityExpr): " + this.getLHS() + ", " + this.getRHS() + "]";
    }

    public Object evaluate(Context context) throws JaxenException {
        Object lhsValue = this.getLHS().evaluate(context);
        Object rhsValue = this.getRHS().evaluate(context);
        if (lhsValue == null || rhsValue == null) {
            return Boolean.FALSE;
        }
        Navigator nav = context.getNavigator();
        if (this.bothAreSets(lhsValue, rhsValue)) {
            return this.evaluateSetSet((List)lhsValue, (List)rhsValue, nav);
        }
        if (this.isSet(lhsValue) && this.isBoolean(rhsValue)) {
            Boolean lhsBoolean = ((List)lhsValue).isEmpty() ? Boolean.FALSE : Boolean.TRUE;
            Boolean rhsBoolean = (Boolean)rhsValue;
            return this.evaluateObjectObject(lhsBoolean, rhsBoolean, nav);
        }
        if (this.isBoolean(lhsValue) && this.isSet(rhsValue)) {
            Boolean lhsBoolean = (Boolean)lhsValue;
            Boolean rhsBoolean = ((List)rhsValue).isEmpty() ? Boolean.FALSE : Boolean.TRUE;
            return this.evaluateObjectObject(lhsBoolean, rhsBoolean, nav);
        }
        if (this.eitherIsSet(lhsValue, rhsValue)) {
            if (this.isSet(lhsValue)) {
                return this.evaluateSetSet((List)lhsValue, DefaultEqualityExpr.convertToList(rhsValue), nav);
            }
            return this.evaluateSetSet(DefaultEqualityExpr.convertToList(lhsValue), (List)rhsValue, nav);
        }
        return this.evaluateObjectObject(lhsValue, rhsValue, nav);
    }

    private Boolean evaluateSetSet(List lhsSet, List rhsSet, Navigator nav) {
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
        if (this.eitherIsBoolean(lhs, rhs)) {
            return this.evaluateObjectObject(BooleanFunction.evaluate(lhs, nav), BooleanFunction.evaluate(rhs, nav));
        }
        if (this.eitherIsNumber(lhs, rhs)) {
            return this.evaluateObjectObject(NumberFunction.evaluate(lhs, nav), NumberFunction.evaluate(rhs, nav));
        }
        return this.evaluateObjectObject(StringFunction.evaluate(lhs, nav), StringFunction.evaluate(rhs, nav));
    }

    protected abstract boolean evaluateObjectObject(Object var1, Object var2);
}

