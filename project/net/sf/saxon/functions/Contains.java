/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.functions.CollatingFunctionFixed;
import net.sf.saxon.lib.SubstringMatcher;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.StringValue;

public class Contains
extends CollatingFunctionFixed {
    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        if (this.getStringCollator() == CodepointCollator.getInstance()) {
            return new SystemFunctionCall.Optimized(this, arguments){

                @Override
                public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
                    String s0 = this.getArg(0).evaluateAsString(context).toString();
                    CharSequence s1 = this.getArg(1).evaluateAsString(context);
                    return s0.contains(s1);
                }

                @Override
                public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) {
                    return this;
                }
            };
        }
        return super.makeOptimizedFunctionCall(visitor, contextInfo, arguments);
    }

    @Override
    public boolean isSubstringMatchingFunction() {
        return true;
    }

    private static boolean contains(StringValue arg0, StringValue arg1, SubstringMatcher collator) {
        if (arg1 == null || arg1.isZeroLength() || collator.comparesEqual(arg1.getPrimitiveStringValue(), "")) {
            return true;
        }
        if (arg0 == null || arg0.isZeroLength()) {
            return false;
        }
        String s0 = arg0.getStringValue();
        String s1 = arg1.getStringValue();
        return collator.contains(s0, s1);
    }

    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue s0 = (StringValue)arguments[0].head();
        StringValue s1 = (StringValue)arguments[1].head();
        return BooleanValue.get(Contains.contains(s0, s1, (SubstringMatcher)this.getStringCollator()));
    }

    @Override
    public String getCompilerName() {
        return "ContainsCompiler";
    }
}

