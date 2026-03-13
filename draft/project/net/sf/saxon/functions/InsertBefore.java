/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.NumericValue;

public class InsertBefore
extends SystemFunction {
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        NumericValue n = (NumericValue)arguments[1].head();
        int pos = (int)n.longValue();
        return SequenceTool.toLazySequence(new InsertIterator(arguments[0].iterate(), arguments[2].iterate(), pos));
    }

    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        return new SystemFunctionCall(this, arguments){

            @Override
            public ItemType getItemType() {
                return Type.getCommonSuperType(this.getArg(0).getItemType(), this.getArg(2).getItemType());
            }
        };
    }

    @Override
    public String getStreamerName() {
        return "InsertBefore";
    }

    public static class InsertIterator
    implements SequenceIterator {
        private SequenceIterator base;
        private SequenceIterator insert;
        private int insertPosition;
        private int position = 0;
        private boolean inserting = false;

        public InsertIterator(SequenceIterator base, SequenceIterator insert, int insertPosition) {
            this.base = base;
            this.insert = insert;
            this.insertPosition = Math.max(insertPosition, 1);
            this.inserting = insertPosition == 1;
        }

        @Override
        public Item next() throws XPathException {
            Item nextItem;
            if (this.inserting) {
                nextItem = this.insert.next();
                if (nextItem == null) {
                    this.inserting = false;
                    nextItem = this.base.next();
                }
            } else if (this.position == this.insertPosition - 1) {
                nextItem = this.insert.next();
                if (nextItem == null) {
                    nextItem = this.base.next();
                } else {
                    this.inserting = true;
                }
            } else {
                nextItem = this.base.next();
                if (nextItem == null && this.position < this.insertPosition - 1) {
                    this.inserting = true;
                    nextItem = this.insert.next();
                }
            }
            if (nextItem == null) {
                this.position = -1;
                return null;
            }
            ++this.position;
            return nextItem;
        }

        @Override
        public void close() {
            this.base.close();
            this.insert.close();
        }
    }
}

