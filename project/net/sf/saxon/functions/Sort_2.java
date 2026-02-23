/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.List;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.Sort_1;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class Sort_2
extends Sort_1 {
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        List<Sort_1.ItemToBeSorted> inputList = this.getItemsToBeSorted(arguments[0]);
        return this.doSort(inputList, this.getCollation(context, arguments[1]), context);
    }

    protected StringCollator getCollation(XPathContext context, Sequence collationArg) throws XPathException {
        StringValue secondArg = (StringValue)collationArg.head();
        if (secondArg == null) {
            return context.getConfiguration().getCollation(this.getRetainedStaticContext().getDefaultCollationName());
        }
        return context.getConfiguration().getCollation(secondArg.getStringValue(), this.getStaticBaseUriString());
    }
}

