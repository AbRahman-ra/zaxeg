/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;

public class FunctionArity
extends SystemFunction {
    @Override
    public IntegerValue[] getIntegerBounds() {
        return new IntegerValue[]{Int64Value.ZERO, Int64Value.makeIntegerValue(65535L)};
    }

    @Override
    public IntegerValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        Function f = (Function)arguments[0].head();
        return Int64Value.makeIntegerValue(f.getArity());
    }
}

