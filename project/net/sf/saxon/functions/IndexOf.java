/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.functions.CollatingFunctionFixed;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;

public class IndexOf
extends CollatingFunctionFixed {
    @Override
    public IntegerValue[] getIntegerBounds() {
        return new IntegerValue[]{Int64Value.PLUS_ONE, Expression.MAX_SEQUENCE_LENGTH};
    }

    @Override
    public void supplyTypeInformation(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType, Expression[] arguments) {
        ItemType type0 = arguments[0].getItemType();
        ItemType type1 = arguments[1].getItemType();
        if (type0 instanceof AtomicType && type1 instanceof AtomicType) {
            this.preAllocateComparer((AtomicType)type0, (AtomicType)type1, visitor.getStaticContext());
        }
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        AtomicComparer comparer = this.getAtomicComparer(context);
        SequenceIterator seq = arguments[0].iterate();
        AtomicValue val = (AtomicValue)arguments[1].head();
        BuiltInAtomicType searchType = val.getPrimitiveType();
        return SequenceTool.toLazySequence(new IndexIterator(seq, searchType, val, comparer));
    }

    @Override
    public String getStreamerName() {
        return "IndexOf";
    }

    private static class IndexIterator
    implements SequenceIterator {
        private int index = 0;
        private SequenceIterator base;
        private BuiltInAtomicType searchType;
        private AtomicComparer comparer;
        private AtomicValue key;

        public IndexIterator(SequenceIterator base, BuiltInAtomicType searchType, AtomicValue key, AtomicComparer comparer) {
            this.base = base;
            this.searchType = searchType;
            this.key = key;
            this.comparer = comparer;
        }

        @Override
        public void close() {
            this.base.close();
        }

        @Override
        public Int64Value next() throws XPathException {
            AtomicValue baseItem;
            while ((baseItem = (AtomicValue)this.base.next()) != null) {
                ++this.index;
                if (!Type.isGuaranteedComparable(this.searchType, baseItem.getPrimitiveType(), false) || !this.comparer.comparesEqual(baseItem, this.key)) continue;
                return new Int64Value(this.index);
            }
            return null;
        }
    }
}

