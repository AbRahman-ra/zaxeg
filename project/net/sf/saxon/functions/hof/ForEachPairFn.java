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
import net.sf.saxon.value.ExternalObject;
import net.sf.saxon.value.ObjectValue;

public class ForEachPairFn
extends SystemFunction {
    @Override
    public ItemType getResultItemType(Expression[] args) {
        ItemType fnType = args[2].getItemType();
        if (fnType instanceof SpecificFunctionType) {
            return ((SpecificFunctionType)fnType).getResultType().getPrimaryType();
        }
        return AnyItemType.getInstance();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return SequenceTool.toLazySequence(this.evalMapPairs((Function)arguments[2].head(), arguments[0].iterate(), arguments[1].iterate(), context));
    }

    private SequenceIterator evalMapPairs(Function function, SequenceIterator seq0, SequenceIterator seq1, XPathContext context) {
        PairedSequenceIterator pairs = new PairedSequenceIterator(seq0, seq1);
        MappingFunction map = item -> {
            Sequence[] pair = (Sequence[])((ExternalObject)item).getObject();
            return ForEachPairFn.dynamicCall(function, context, pair).iterate();
        };
        return new MappingIterator(pairs, map);
    }

    private static class PairedSequenceIterator
    implements SequenceIterator {
        private SequenceIterator seq0;
        private SequenceIterator seq1;
        private Sequence[] args = new Sequence[2];

        public PairedSequenceIterator(SequenceIterator seq0, SequenceIterator seq1) {
            this.seq0 = seq0;
            this.seq1 = seq1;
        }

        @Override
        public ObjectValue<Sequence[]> next() throws XPathException {
            Item i0 = this.seq0.next();
            if (i0 == null) {
                this.close();
                return null;
            }
            Item i1 = this.seq1.next();
            if (i1 == null) {
                this.close();
                return null;
            }
            this.args[0] = i0;
            this.args[1] = i1;
            return new ObjectValue<Sequence[]>(this.args);
        }

        @Override
        public void close() {
            this.seq0.close();
            this.seq1.close();
        }
    }
}

