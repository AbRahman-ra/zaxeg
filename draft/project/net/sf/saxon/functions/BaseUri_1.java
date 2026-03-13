/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;

public class BaseUri_1
extends SystemFunction
implements Callable {
    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        NodeInfo node = (NodeInfo)arguments[0].head();
        if (node == null) {
            return ZeroOrOne.empty();
        }
        String s = node.getBaseURI();
        if (s == null) {
            return ZeroOrOne.empty();
        }
        return new ZeroOrOne<AnyURIValue>(new AnyURIValue(s));
    }

    @Override
    public String getCompilerName() {
        return "BaseURICompiler";
    }
}

