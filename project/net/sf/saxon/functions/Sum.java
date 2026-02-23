/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.ArithmeticExpression;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.Fold;
import net.sf.saxon.functions.FoldingFunction;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
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

public class Sum
extends FoldingFunction {
    @Override
    public ItemType getResultItemType(Expression[] args) {
        TypeHierarchy th = this.getRetainedStaticContext().getConfiguration().getTypeHierarchy();
        ItemType base = Atomizer.getAtomizedItemType(args[0], false, th);
        if (base.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
            base = BuiltInAtomicType.DOUBLE;
        }
        if (Cardinality.allowsZero(args[0].getCardinality())) {
            if (this.getArity() == 1) {
                return Type.getCommonSuperType(base, BuiltInAtomicType.INTEGER, th);
            }
            return Type.getCommonSuperType(base, args[1].getItemType(), th);
        }
        return base.getPrimitiveItemType();
    }

    @Override
    public int getCardinality(Expression[] arguments) {
        if (this.getArity() == 1 || arguments[1].getCardinality() == 1) {
            return 16384;
        }
        return 24576;
    }

    @Override
    public Fold getFold(XPathContext context, Sequence ... additionalArguments) throws XPathException {
        if (additionalArguments.length > 0) {
            AtomicValue z = (AtomicValue)additionalArguments[0].head();
            return new SumFold(context, z);
        }
        return new SumFold(context, Int64Value.ZERO);
    }

    public static AtomicValue total(SequenceIterator in, XPathContext context, Location locator) throws XPathException {
        try {
            SumFold fold = new SumFold(context, null);
            in.forEachOrFail(fold::processItem);
            return (AtomicValue)fold.result().head();
        } catch (XPathException e) {
            e.maybeSetLocation(locator);
            throw e;
        }
    }

    @Override
    public String getCompilerName() {
        return "SumCompiler";
    }

    private static class SumFold
    implements Fold {
        private XPathContext context;
        private AtomicValue zeroValue;
        private AtomicValue data;
        private boolean atStart = true;
        private ConversionRules rules;
        private StringConverter toDouble;

        public SumFold(XPathContext context, AtomicValue zeroValue) {
            this.context = context;
            this.zeroValue = zeroValue;
            this.rules = context.getConfiguration().getConversionRules();
            this.toDouble = BuiltInAtomicType.DOUBLE.getStringConverter(this.rules);
        }

        @Override
        public void processItem(Item item) throws XPathException {
            AtomicValue next = (AtomicValue)item;
            if (this.atStart) {
                this.atStart = false;
                if (next instanceof UntypedAtomicValue) {
                    this.data = this.toDouble.convert(next).asAtomic();
                    return;
                }
                if (next instanceof NumericValue || next instanceof DayTimeDurationValue || next instanceof YearMonthDurationValue) {
                    this.data = next;
                    return;
                }
                XPathException err = new XPathException("Input to sum() contains a value of type " + next.getPrimitiveType().getDisplayName() + " which is neither numeric, nor a duration");
                err.setXPathContext(this.context);
                err.setErrorCode("FORG0006");
                throw err;
            }
            if (this.data instanceof NumericValue) {
                if (next instanceof UntypedAtomicValue) {
                    next = this.toDouble.convert(next).asAtomic();
                } else if (!(next instanceof NumericValue)) {
                    XPathException err = new XPathException("Input to sum() contains a mix of numeric and non-numeric values");
                    err.setXPathContext(this.context);
                    err.setErrorCode("FORG0006");
                    throw err;
                }
                this.data = ArithmeticExpression.compute(this.data, 0, next, this.context);
            } else if (this.data instanceof DurationValue) {
                if (!(this.data instanceof DayTimeDurationValue) && !(this.data instanceof YearMonthDurationValue)) {
                    XPathException err = new XPathException("Input to sum() contains a duration that is neither a dayTimeDuration nor a yearMonthDuration");
                    err.setXPathContext(this.context);
                    err.setErrorCode("FORG0006");
                    throw err;
                }
                if (!(next instanceof DurationValue)) {
                    XPathException err = new XPathException("Input to sum() contains a mix of duration and non-duration values");
                    err.setXPathContext(this.context);
                    err.setErrorCode("FORG0006");
                    throw err;
                }
                this.data = ((DurationValue)this.data).add((DurationValue)next);
            } else {
                XPathException err = new XPathException("Input to sum() contains a value of type " + this.data.getPrimitiveType().getDisplayName() + " which is neither numeric, nor a duration");
                err.setXPathContext(this.context);
                err.setErrorCode("FORG0006");
                throw err;
            }
        }

        @Override
        public boolean isFinished() {
            return this.data instanceof DoubleValue && this.data.isNaN();
        }

        @Override
        public Sequence result() {
            if (this.atStart) {
                return this.zeroValue == null ? EmptySequence.getInstance() : this.zeroValue;
            }
            return this.data;
        }
    }
}

