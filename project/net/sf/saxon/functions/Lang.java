/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;

public class Lang
extends SystemFunction {
    public static boolean isLang(String arglang, NodeInfo target) {
        String doclang = null;
        NodeInfo node = target;
        while (node != null && (doclang = node.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang")) == null) {
            if ((node = node.getParent()) != null) continue;
            return false;
        }
        if (doclang == null) {
            return false;
        }
        while (!arglang.equalsIgnoreCase(doclang)) {
            int hyphen = doclang.lastIndexOf("-");
            if (hyphen < 0) {
                return false;
            }
            doclang = doclang.substring(0, hyphen);
        }
        return true;
    }

    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        NodeInfo target = arguments.length > 1 ? (NodeInfo)arguments[1].head() : this.getAndCheckContextItem(context);
        Item arg0Val = arguments[0].head();
        String testLang = arg0Val == null ? "" : arg0Val.getStringValue();
        return BooleanValue.get(Lang.isLang(testLang, target));
    }

    private NodeInfo getAndCheckContextItem(XPathContext context) throws XPathException {
        Item current = context.getContextItem();
        if (current == null) {
            XPathException err = new XPathException("The context item for lang() is absent");
            err.setErrorCode("XPDY0002");
            err.setXPathContext(context);
            throw err;
        }
        if (!(current instanceof NodeInfo)) {
            XPathException err = new XPathException("The context item for lang() is not a node");
            err.setErrorCode("XPTY0004");
            err.setXPathContext(context);
            throw err;
        }
        NodeInfo target = (NodeInfo)current;
        return target;
    }
}

