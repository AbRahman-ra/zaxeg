/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.One;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class NormalizeSpace_1
extends ScalarSystemFunction {
    @Override
    public ZeroOrOne resultWhenEmpty() {
        return One.string("");
    }

    @Override
    public AtomicValue evaluate(Item arg, XPathContext context) throws XPathException {
        return NormalizeSpace_1.normalizeSpace((StringValue)arg);
    }

    public static StringValue normalizeSpace(StringValue sv) {
        if (sv == null) {
            return StringValue.EMPTY_STRING;
        }
        return StringValue.makeStringValue(Whitespace.collapseWhitespace(sv.getStringValueCS()));
    }

    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        return new SystemFunctionCall(this, arguments){

            @Override
            public boolean effectiveBooleanValue(XPathContext c) throws XPathException {
                AtomicValue sv = (AtomicValue)this.getArg(0).evaluateItem(c);
                if (sv == null) {
                    return false;
                }
                CharSequence cs = sv.getStringValueCS();
                return !Whitespace.isWhite(cs);
            }
        };
    }

    @Override
    public String getCompilerName() {
        return "NormalizeSpaceCompiler";
    }
}

