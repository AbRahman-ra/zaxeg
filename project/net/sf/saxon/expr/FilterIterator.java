/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.StringValue;

public class FilterIterator
implements SequenceIterator {
    protected FocusIterator base;
    protected Expression filter;
    protected XPathContext filterContext;

    public FilterIterator(SequenceIterator base, Expression filter, XPathContext context) {
        this.filter = filter;
        this.filterContext = context.newMinorContext();
        this.base = this.filterContext.trackFocus(base);
    }

    public void setSequence(SequenceIterator base, XPathContext context) {
        this.filterContext = context.newMinorContext();
        this.base = this.filterContext.trackFocus(base);
    }

    @Override
    public Item next() throws XPathException {
        return this.getNextMatchingItem();
    }

    protected Item getNextMatchingItem() throws XPathException {
        Item next;
        while ((next = this.base.next()) != null) {
            if (!this.matches()) continue;
            return next;
        }
        return null;
    }

    protected boolean matches() throws XPathException {
        SequenceIterator iterator = this.filter.iterate(this.filterContext);
        return FilterIterator.testPredicateValue(iterator, this.base.position(), this.filter);
    }

    public static boolean testPredicateValue(SequenceIterator iterator, long position, Expression filter) throws XPathException {
        Item first = iterator.next();
        if (first == null) {
            return false;
        }
        if (first instanceof NodeInfo) {
            return true;
        }
        if (first instanceof BooleanValue) {
            if (iterator.next() != null) {
                ExpressionTool.ebvError("a sequence of two or more items starting with a boolean", filter);
            }
            return ((BooleanValue)first).getBooleanValue();
        }
        if (first instanceof StringValue) {
            if (iterator.next() != null) {
                ExpressionTool.ebvError("a sequence of two or more items starting with a string", filter);
            }
            return first.getStringValueCS().length() != 0;
        }
        if (first instanceof Int64Value) {
            if (iterator.next() != null) {
                ExpressionTool.ebvError("a sequence of two or more items starting with a numeric value", filter);
            }
            return ((Int64Value)first).longValue() == position;
        }
        if (first instanceof NumericValue) {
            if (iterator.next() != null) {
                ExpressionTool.ebvError("a sequence of two or more items starting with a numeric value", filter);
            }
            return ((NumericValue)first).compareTo(position) == 0;
        }
        if (first instanceof AtomicValue) {
            ExpressionTool.ebvError("a sequence starting with an atomic value of type " + ((AtomicValue)first).getPrimitiveType().getDisplayName() + " (" + first.toShortString() + ")", filter);
            return false;
        }
        ExpressionTool.ebvError("a sequence starting with " + first.getGenre().getDescription() + " (" + first.toShortString() + ")", filter);
        return false;
    }

    @Override
    public void close() {
        this.base.close();
    }

    public static final class NonNumeric
    extends FilterIterator {
        public NonNumeric(SequenceIterator base, Expression filter, XPathContext context) {
            super(base, filter, context);
        }

        @Override
        protected boolean matches() throws XPathException {
            return this.filter.effectiveBooleanValue(this.filterContext);
        }
    }
}

