/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.StringValue;

public class NamespaceForPrefix
extends SystemFunction
implements Callable {
    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        AnyURIValue result = NamespaceForPrefix.namespaceUriForPrefix((StringValue)arguments[0].head(), (NodeInfo)arguments[1].head());
        return new ZeroOrOne<AnyURIValue>(result);
    }

    private static AnyURIValue namespaceUriForPrefix(StringValue p, NodeInfo element) {
        String prefix = p == null ? "" : p.getStringValue();
        NamespaceMap resolver = element.getAllNamespaces();
        String uri = resolver.getURIForPrefix(prefix, true);
        if (uri == null || uri.isEmpty()) {
            return null;
        }
        return new AnyURIValue(uri);
    }
}

