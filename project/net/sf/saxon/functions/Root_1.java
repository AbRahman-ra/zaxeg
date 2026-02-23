/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;

public class Root_1
extends SystemFunction {
    @Override
    public int getSpecialProperties(Expression[] arguments) {
        int prop = 25296896;
        if (this.getArity() == 0 || (arguments[0].getSpecialProperties() & 0x10000) != 0) {
            prop |= 0x10000;
        }
        return prop;
    }

    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        NodeInfo node = (NodeInfo)arguments[0].head();
        if (node == null) {
            return ZeroOrOne.empty();
        }
        return new ZeroOrOne<NodeInfo>(node.getRoot());
    }

    @Override
    public String getStreamerName() {
        return "Root";
    }

    @Override
    public String getCompilerName() {
        return "RootFunctionCompiler";
    }
}

