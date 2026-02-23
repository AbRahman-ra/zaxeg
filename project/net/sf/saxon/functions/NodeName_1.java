/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.QNameValue;

public class NodeName_1
extends ScalarSystemFunction {
    @Override
    public AtomicValue evaluate(Item item, XPathContext context) throws XPathException {
        return NodeName_1.nodeName((NodeInfo)item);
    }

    public static QNameValue nodeName(NodeInfo node) {
        if (node.getLocalPart().isEmpty()) {
            return null;
        }
        return new QNameValue(node.getPrefix(), node.getURI(), node.getLocalPart(), BuiltInAtomicType.QNAME);
    }

    @Override
    public String getCompilerName() {
        return "NodeNameFnCompiler";
    }
}

