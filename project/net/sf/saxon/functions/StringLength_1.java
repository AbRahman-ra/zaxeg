/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.One;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.StringValue;

public class StringLength_1
extends ScalarSystemFunction {
    @Override
    public IntegerValue[] getIntegerBounds() {
        return new IntegerValue[]{Int64Value.ZERO, Expression.MAX_STRING_LENGTH};
    }

    @Override
    public ZeroOrOne resultWhenEmpty() {
        return One.integer(0L);
    }

    @Override
    public AtomicValue evaluate(Item arg, XPathContext context) throws XPathException {
        CharSequence s;
        if (arg instanceof StringValue) {
            return Int64Value.makeIntegerValue(((StringValue)arg).getStringLength());
        }
        try {
            s = arg.getStringValueCS();
        } catch (UnsupportedOperationException e) {
            throw new XPathException("Cannot get the string value of a function item", "FOTY0013");
        }
        return Int64Value.makeIntegerValue(StringValue.getStringLength(s));
    }

    @Override
    public String getCompilerName() {
        return "StringLengthCompiler";
    }
}

