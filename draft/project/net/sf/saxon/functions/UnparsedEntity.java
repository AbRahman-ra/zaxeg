/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.StringValue;

public abstract class UnparsedEntity
extends SystemFunction
implements Callable {
    public static int URI = 0;
    public static int PUBLIC_ID = 1;

    public abstract int getOp();

    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        String[] ids;
        int operation = this.getOp();
        String arg0 = arguments[0].head().getStringValue();
        NodeInfo doc = null;
        if (this.getArity() == 1) {
            Item it = context.getContextItem();
            if (it instanceof NodeInfo) {
                doc = ((NodeInfo)it).getRoot();
            }
            if (doc == null || doc.getNodeKind() != 9) {
                String code = operation == URI ? "XTDE1370" : "XTDE1380";
                throw new XPathException("In function " + this.getFunctionName().getDisplayName() + ", the context item must be a node in a tree whose root is a document node", code, context);
            }
        } else {
            doc = (NodeInfo)arguments[1].head();
            if (doc != null) {
                doc = doc.getRoot();
            }
            if (doc == null || doc.getNodeKind() != 9) {
                String code = operation == URI ? "XTDE1370" : "XTDE1380";
                throw new XPathException("In function " + this.getFunctionName().getDisplayName() + ", the second argument must be a document node", code, context);
            }
        }
        String result = (ids = doc.getTreeInfo().getUnparsedEntity(arg0)) == null ? "" : ids[operation];
        return operation == URI ? new AnyURIValue(result) : new StringValue(result);
    }

    public static class UnparsedEntityPublicId
    extends UnparsedEntity {
        @Override
        public int getOp() {
            return PUBLIC_ID;
        }
    }

    public static class UnparsedEntityUri
    extends UnparsedEntity {
        @Override
        public int getOp() {
            return URI;
        }
    }
}

