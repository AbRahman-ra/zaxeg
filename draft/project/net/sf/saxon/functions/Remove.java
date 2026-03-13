/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.EnumSet;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.TailExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.EnumSetTool;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;

public class Remove
extends SystemFunction {
    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        GroundedValue index;
        if (Literal.isAtomic(arguments[1]) && (index = ((Literal)arguments[1]).getValue()) instanceof IntegerValue) {
            try {
                long value = ((IntegerValue)index).longValue();
                if (value <= 0L) {
                    return arguments[0];
                }
                if (value == 1L) {
                    return new TailExpression(arguments[0], 2);
                }
            } catch (XPathException xPathException) {
                // empty catch block
            }
        }
        return super.makeFunctionCall(arguments);
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        NumericValue n = (NumericValue)arguments[1].head();
        int pos = (int)n.longValue();
        if (pos < 1) {
            return arguments[0];
        }
        return SequenceTool.toLazySequence2(new RemoveIterator(arguments[0].iterate(), pos));
    }

    @Override
    public String getStreamerName() {
        return "Remove";
    }

    public static class RemoveIterator
    implements SequenceIterator,
    LastPositionFinder {
        SequenceIterator base;
        int removePosition;
        int basePosition = 0;
        Item current = null;

        public RemoveIterator(SequenceIterator base, int removePosition) {
            this.base = base;
            this.removePosition = removePosition;
        }

        @Override
        public Item next() throws XPathException {
            this.current = this.base.next();
            ++this.basePosition;
            if (this.current != null && this.basePosition == this.removePosition) {
                this.current = this.base.next();
                ++this.basePosition;
            }
            return this.current;
        }

        @Override
        public void close() {
            this.base.close();
        }

        @Override
        public int getLength() throws XPathException {
            if (this.base instanceof LastPositionFinder) {
                int x = ((LastPositionFinder)((Object)this.base)).getLength();
                if (this.removePosition >= 1 && this.removePosition <= x) {
                    return x - 1;
                }
                return x;
            }
            throw new AssertionError((Object)"base of removeIterator is not a LastPositionFinder");
        }

        @Override
        public EnumSet<SequenceIterator.Property> getProperties() {
            return EnumSetTool.intersect(this.base.getProperties(), EnumSet.of(SequenceIterator.Property.LAST_POSITION_FINDER));
        }
    }
}

