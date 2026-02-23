/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.DocumentPool;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;

public class DocumentUri_1
extends ScalarSystemFunction {
    @Override
    public AtomicValue evaluate(Item item, XPathContext context) throws XPathException {
        return DocumentUri_1.getDocumentURI((NodeInfo)item, context);
    }

    public static AnyURIValue getDocumentURI(NodeInfo node, XPathContext c) {
        if (node.getNodeKind() == 9) {
            Object o = node.getTreeInfo().getUserData("saxon:document-uri");
            if (o instanceof String) {
                return o.toString().isEmpty() ? null : new AnyURIValue(o.toString());
            }
            Controller controller = c.getController();
            assert (controller != null);
            DocumentPool pool = controller.getDocumentPool();
            String docURI = pool.getDocumentURI(node);
            if (docURI == null) {
                docURI = node.getSystemId();
            }
            if (docURI == null) {
                return null;
            }
            if ("".equals(docURI)) {
                return null;
            }
            return new AnyURIValue(docURI);
        }
        return null;
    }
}

