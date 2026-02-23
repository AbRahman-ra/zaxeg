/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.net.URI;
import java.net.URISyntaxException;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.PendingUpdateList;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;

public class Put
extends SystemFunction {
    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        return new SystemFunctionCall(this, arguments){

            @Override
            public boolean isUpdatingExpression() {
                return true;
            }

            @Override
            public void evaluatePendingUpdates(XPathContext context, PendingUpdateList pul) throws XPathException {
                String abs;
                NodeInfo node = (NodeInfo)this.getArg(0).evaluateItem(context);
                int kind = node.getNodeKind();
                if (kind != 1 && kind != 9) {
                    throw new XPathException("Node in put() must be a document or element node", "FOUP0001", context);
                }
                String relative = this.getArg(1).evaluateItem(context).getStringValue();
                try {
                    URI resolved = ResolveURI.makeAbsolute(relative, Put.this.getStaticBaseUriString());
                    abs = resolved.toString();
                } catch (URISyntaxException err) {
                    throw new XPathException("Base URI " + Err.wrap(Put.this.getStaticBaseUriString()) + " is invalid: " + err.getMessage(), "FOUP0002", context);
                }
                pul.addPutAction(node, abs, this);
            }
        };
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        throw new XPathException("Dynamic evaluation of fn:put() is not supported");
    }
}

