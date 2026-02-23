/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.value.StringValue;

public class LowerCase
extends ScalarSystemFunction {
    @Override
    public StringValue evaluate(Item arg, XPathContext context) {
        return StringValue.makeStringValue(arg.getStringValue().toLowerCase());
    }

    @Override
    public ZeroOrOne resultWhenEmpty() {
        return ZERO_LENGTH_STRING;
    }

    @Override
    public String getCompilerName() {
        return "ForceCaseCompiler";
    }
}

