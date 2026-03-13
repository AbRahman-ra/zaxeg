/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.ArrayList;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.StringValue;

public class InScopePrefixes
extends SystemFunction {
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        NodeInfo element = (NodeInfo)arguments[0].head();
        String[] prefixes = element.getAllNamespaces().getPrefixArray();
        ArrayList<StringValue> result = new ArrayList<StringValue>();
        for (String s : prefixes) {
            result.add(new StringValue(s));
        }
        result.add(new StringValue("xml"));
        return SequenceExtent.makeSequenceExtent(result);
    }
}

