/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;

public class HasChildren_1
extends SystemFunction {
    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        NodeInfo arg = (NodeInfo)arguments[0].head();
        if (arg == null) {
            return BooleanValue.FALSE;
        }
        return BooleanValue.get(arg.hasChildNodes());
    }
}

