/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.One;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

public class GenerateId_1
extends ScalarSystemFunction {
    @Override
    public int getSpecialProperties(Expression[] arguments) {
        int p = super.getSpecialProperties(arguments);
        return p & 0xFF7FFFFF;
    }

    @Override
    public ZeroOrOne resultWhenEmpty() {
        return One.string("");
    }

    @Override
    public AtomicValue evaluate(Item arg, XPathContext context) throws XPathException {
        return GenerateId_1.generateId((NodeInfo)arg);
    }

    public static StringValue generateId(NodeInfo node) {
        FastStringBuffer buffer = new FastStringBuffer(16);
        node.generateId(buffer);
        buffer.condense();
        return new StringValue(buffer);
    }

    @Override
    public String getCompilerName() {
        return "GenerateIdCompiler";
    }
}

