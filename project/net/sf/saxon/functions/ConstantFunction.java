/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.IntegerValue;

public class ConstantFunction
extends SystemFunction {
    public GroundedValue value;

    public ConstantFunction(GroundedValue value) {
        this.value = value;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return this.value;
    }

    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        return Literal.makeLiteral(this.value);
    }

    @Override
    public IntegerValue[] getIntegerBounds() {
        if (this.value instanceof IntegerValue) {
            return new IntegerValue[]{(IntegerValue)this.value, (IntegerValue)this.value};
        }
        return null;
    }

    public static class False
    extends ConstantFunction {
        public False() {
            super(BooleanValue.FALSE);
        }
    }

    public static class True
    extends ConstantFunction {
        public True() {
            super(BooleanValue.TRUE);
        }
    }
}

