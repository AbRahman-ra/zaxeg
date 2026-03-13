/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;

public class FilterFn
extends SystemFunction {
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return SequenceTool.toLazySequence(this.evalFilter((Function)arguments[1].head(), arguments[0].iterate(), context));
    }

    private SequenceIterator evalFilter(final Function function, SequenceIterator base, final XPathContext context) {
        ItemMappingFunction map = new ItemMappingFunction(){
            private final Sequence[] args = new Sequence[1];

            @Override
            public Item mapItem(Item item) throws XPathException {
                this.args[0] = item;
                BooleanValue result = (BooleanValue)SystemFunction.dynamicCall(function, context, this.args).head();
                return result.getBooleanValue() ? item : null;
            }
        };
        return new ItemMappingIterator(base, map);
    }

    @Override
    public String getStreamerName() {
        return "FilterFn";
    }
}

