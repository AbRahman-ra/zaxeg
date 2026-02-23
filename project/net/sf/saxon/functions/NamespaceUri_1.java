/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.One;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;

public class NamespaceUri_1
extends ScalarSystemFunction {
    @Override
    public AtomicValue evaluate(Item item, XPathContext context) throws XPathException {
        String uri = ((NodeInfo)item).getURI();
        return new AnyURIValue(uri);
    }

    @Override
    public ZeroOrOne resultWhenEmpty() {
        return new One<AnyURIValue>(new AnyURIValue(""));
    }

    @Override
    public String getCompilerName() {
        return "NamespaceUriFnCompiler";
    }
}

