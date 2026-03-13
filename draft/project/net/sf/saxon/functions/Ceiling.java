/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.value.NumericValue;

public final class Ceiling
extends ScalarSystemFunction {
    @Override
    public NumericValue evaluate(Item arg, XPathContext context) {
        return ((NumericValue)arg).ceiling();
    }

    @Override
    public String getCompilerName() {
        return "RoundingCompiler";
    }
}

