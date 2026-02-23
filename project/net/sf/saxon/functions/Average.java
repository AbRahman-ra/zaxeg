/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.ArithmeticExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.Fold;
import net.sf.saxon.functions.FoldingFunction;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.DayTimeDurationValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.value.YearMonthDurationValue;

public class Average
extends FoldingFunction {
    @Override
    public int getCardinality(Expression[] arguments) {
        if (!Cardinality.allowsZero(arguments[0].getCardinality())) {
            return 16384;
        }
        return super.getCardinality(arguments);
    }

    @Override
    public Fold getFold(XPathContext context, Sequence ... additionalArguments) {
        return new AverageFold(context);
    }

    private class AverageFold
    implements Fold {
        private XPathContext context;
        private AtomicValue data;
        private boolean atStart = true;
        private ConversionRules rules;
        private StringConverter toDouble;
        private int count = 0;

        public AverageFold(XPathContext context) {
            this.context = context;
            this.rules = context.getConfiguration().getConversionRules();
            this.toDouble = BuiltInAtomicType.DOUBLE.getStringConverter(this.rules);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public void processItem(Item item) throws XPathException {
            AtomicValue next = (AtomicValue)item;
            if (next instanceof UntypedAtomicValue) {
                next = this.toDouble.convert((UntypedAtomicValue)next).asAtomic();
            }
            ++this.count;
            if (this.atStart) {
                if (next instanceof NumericValue || next instanceof DayTimeDurationValue || next instanceof YearMonthDurationValue) {
                    this.data = next;
                    this.atStart = false;
                    return;
                }
                if (!(next instanceof DurationValue)) throw new XPathException("Input to avg() contains a value (" + Err.depict(next) + ") that is neither numeric, nor a duration", "FORG0006");
                throw new XPathException("Input to avg() contains a duration (" + Err.depict(next) + ") that is neither an xs:dayTimeDuration nor an xs:yearMonthDuration", "FORG0006");
            }
            if (this.data instanceof NumericValue) {
                if (!(next instanceof NumericValue)) {
                    throw new XPathException("Input to avg() contains a mix of numeric and non-numeric values", "FORG0006");
                }
                this.data = ArithmeticExpression.compute(this.data, 0, next, this.context);
                return;
            }
            if (!(this.data instanceof DurationValue)) throw new XPathException("Input to avg() contains a value (" + Err.depict(this.data) + ") that is neither numeric, nor a duration", "FORG0006");
            if (!(next instanceof DurationValue)) {
                throw new XPathException("Input to avg() contains a mix of duration and non-duration values", "FORG0006");
            }
            try {
                this.data = ((DurationValue)this.data).add((DurationValue)next);
                return;
            } catch (XPathException e) {
                if (!"XPTY0004".equals(e.getErrorCodeLocalPart())) throw e;
                e.setErrorCode("FORG0006");
                throw e;
            }
        }

        @Override
        public boolean isFinished() {
            return this.data instanceof DoubleValue && this.data.isNaN();
        }

        @Override
        public Sequence result() throws XPathException {
            if (this.atStart) {
                return EmptySequence.getInstance();
            }
            if (this.data instanceof NumericValue) {
                return ArithmeticExpression.compute(this.data, 3, new Int64Value(this.count), this.context);
            }
            return ((DurationValue)this.data).divide(this.count);
        }
    }
}

