/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.MappingFunction;
import net.sf.saxon.expr.MappingIterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SpecificFunctionType;

public class ForEachFn
extends SystemFunction {
    @Override
    public ItemType getResultItemType(Expression[] args) {
        ItemType fnType = args[1].getItemType();
        if (fnType instanceof SpecificFunctionType) {
            return ((SpecificFunctionType)fnType).getResultType().getPrimaryType();
        }
        return AnyItemType.getInstance();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return SequenceTool.toLazySequence(this.evalMap((Function)arguments[1].head(), arguments[0].iterate(), context));
    }

    private SequenceIterator evalMap(final Function function, SequenceIterator base, final XPathContext context) {
        MappingFunction map = new MappingFunction(){
            private final Sequence[] args = new Sequence[1];

            @Override
            public SequenceIterator map(Item item) throws XPathException {
                this.args[0] = item;
                return SystemFunction.dynamicCall(function, context, this.args).iterate();
            }
        };
        return new MappingIterator(base, map);
    }
}

