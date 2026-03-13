/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.One;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.NumericValue;

public final class RoundHalfToEven
extends SystemFunction {
    @Override
    public int getCardinality(Expression[] arguments) {
        return arguments[0].getCardinality();
    }

    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        NumericValue val0 = (NumericValue)arguments[0].head();
        if (val0 == null) {
            return ZeroOrOne.empty();
        }
        int scale = 0;
        if (arguments.length == 2) {
            NumericValue scaleVal = (NumericValue)arguments[1].head();
            if (scaleVal.compareTo(Integer.MAX_VALUE) > 0) {
                return new ZeroOrOne<NumericValue>(val0);
            }
            scale = scaleVal.compareTo(Integer.MIN_VALUE) < 0 ? Integer.MIN_VALUE : (int)scaleVal.longValue();
        }
        return new One<NumericValue>(val0.roundHalfToEven(scale));
    }

    @Override
    public String getCompilerName() {
        return "RoundingCompiler";
    }
}

