/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.CollatingFunctionFixed;
import net.sf.saxon.lib.SubstringMatcher;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class SubstringAfter
extends CollatingFunctionFixed {
    @Override
    public boolean isSubstringMatchingFunction() {
        return true;
    }

    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue arg1 = (StringValue)arguments[0].head();
        StringValue arg2 = (StringValue)arguments[1].head();
        return SubstringAfter.substringAfter(arg1, arg2, (SubstringMatcher)this.getStringCollator());
    }

    private static StringValue substringAfter(StringValue arg1, StringValue arg2, SubstringMatcher collator) {
        if (arg1 == null) {
            arg1 = StringValue.EMPTY_STRING;
        }
        if (arg2 == null) {
            arg2 = StringValue.EMPTY_STRING;
        }
        if (arg2.isZeroLength()) {
            return arg1;
        }
        if (arg1.isZeroLength()) {
            return StringValue.EMPTY_STRING;
        }
        String s1 = arg1.getStringValue();
        String s2 = arg2.getStringValue();
        String result = collator.substringAfter(s1, s2);
        StringValue s = StringValue.makeStringValue(result);
        if (arg1.isKnownToContainNoSurrogates()) {
            s.setContainsNoSurrogates();
        }
        return s;
    }

    @Override
    public String getCompilerName() {
        return "SubstringAfterCompiler";
    }
}

