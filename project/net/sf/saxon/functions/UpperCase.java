/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.One;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

public class UpperCase
extends ScalarSystemFunction {
    @Override
    public AtomicValue evaluate(Item arg, XPathContext context) {
        return StringValue.makeStringValue(arg.getStringValue().toUpperCase());
    }

    @Override
    public One resultWhenEmpty() {
        return ZERO_LENGTH_STRING;
    }

    @Override
    public String getCompilerName() {
        return "ForceCaseCompiler";
    }
}

