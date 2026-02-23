/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.One;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;

public class Nilled_1
extends SystemFunction
implements Callable {
    private static BooleanValue getNilledProperty(NodeInfo node) {
        if (node == null || node.getNodeKind() != 1) {
            return null;
        }
        return BooleanValue.get(node.isNilled());
    }

    public static boolean isNilled(NodeInfo node) {
        BooleanValue b = Nilled_1.getNilledProperty(node);
        return b != null && b.getBooleanValue();
    }

    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        NodeInfo node = (NodeInfo)arguments[0].head();
        if (node == null || node.getNodeKind() != 1) {
            return ZeroOrOne.empty();
        }
        return One.bool(Nilled_1.isNilled(node));
    }
}

