/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.CollatingFunctionFixed;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.lib.SubstringMatcher;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class SubstringBefore
extends CollatingFunctionFixed {
    @Override
    public boolean isSubstringMatchingFunction() {
        return true;
    }

    private static StringValue substringBefore(StringValue arg0, StringValue arg1, SubstringMatcher collator) {
        String s0 = arg0.getStringValue();
        String s1 = arg1.getStringValue();
        StringValue result = new StringValue(collator.substringBefore(s0, s1));
        if (arg0.isKnownToContainNoSurrogates()) {
            result.setContainsNoSurrogates();
        }
        return result;
    }

    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue arg1 = (StringValue)arguments[1].head();
        if (arg1 == null || arg1.isZeroLength()) {
            return StringValue.EMPTY_STRING;
        }
        StringValue arg0 = (StringValue)arguments[0].head();
        if (arg0 == null || arg0.isZeroLength()) {
            return StringValue.EMPTY_STRING;
        }
        StringCollator collator = this.getStringCollator();
        return SubstringBefore.substringBefore(arg0, arg1, (SubstringMatcher)collator);
    }

    @Override
    public String getCompilerName() {
        return "SubstringBeforeCompiler";
    }
}

