/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

public class Path_1
extends ScalarSystemFunction {
    @Override
    public AtomicValue evaluate(Item arg, XPathContext context) throws XPathException {
        return Path_1.makePath((NodeInfo)arg, context);
    }

    public static StringValue makePath(NodeInfo node, XPathContext context) {
        NodeInfo n;
        if (node.getNodeKind() == 9) {
            return StringValue.makeStringValue("/");
        }
        FastStringBuffer fsb = new FastStringBuffer(256);
        AxisIterator iter = node.iterateAxis(1);
        block9: while ((n = iter.next()) != null) {
            if (n.getParent() == null) {
                if (n.getNodeKind() == 9) {
                    return new StringValue(fsb);
                }
                fsb.prepend("Q{http://www.w3.org/2005/xpath-functions}root()");
                return new StringValue(fsb);
            }
            FastStringBuffer fsb2 = new FastStringBuffer(256);
            switch (n.getNodeKind()) {
                case 9: {
                    return new StringValue(fsb);
                }
                case 1: {
                    fsb2.append("/Q{");
                    fsb2.append(n.getURI());
                    fsb2.append("}");
                    fsb2.append(n.getLocalPart());
                    fsb2.append("[" + Navigator.getNumberSimple(n, context) + "]");
                    fsb2.append(fsb);
                    fsb = fsb2;
                    continue block9;
                }
                case 2: {
                    fsb2.append("/@");
                    String attURI = n.getURI();
                    if (!"".equals(attURI)) {
                        fsb2.append("Q{");
                        fsb2.append(attURI);
                        fsb2.append("}");
                    }
                    fsb2.append(n.getLocalPart());
                    fsb2.append(fsb);
                    fsb = fsb2;
                    continue block9;
                }
                case 3: {
                    fsb2.append("/text()[");
                    fsb2.append(Navigator.getNumberSimple(n, context) + "]");
                    fsb2.append(fsb);
                    fsb = fsb2;
                    continue block9;
                }
                case 8: {
                    fsb2.append("/comment()[");
                    fsb2.append(Navigator.getNumberSimple(n, context) + "]");
                    fsb2.append(fsb);
                    fsb = fsb2;
                    continue block9;
                }
                case 7: {
                    fsb2.append("/processing-instruction(");
                    fsb2.append(n.getLocalPart());
                    fsb2.append(")[");
                    fsb2.append(Navigator.getNumberSimple(n, context) + "]");
                    fsb2.append(fsb);
                    fsb = fsb2;
                    continue block9;
                }
                case 13: {
                    fsb2.append("/namespace::");
                    if (n.getLocalPart().isEmpty()) {
                        fsb2.append("*[Q{http://www.w3.org/2005/xpath-functions}local-name()=\"\"]");
                    } else {
                        fsb.append(n.getLocalPart());
                    }
                    fsb2.append(fsb);
                    fsb = fsb2;
                    continue block9;
                }
            }
            throw new AssertionError();
        }
        fsb.prepend("Q{http://www.w3.org/2005/xpath-functions}root()");
        return new StringValue(fsb);
    }
}

