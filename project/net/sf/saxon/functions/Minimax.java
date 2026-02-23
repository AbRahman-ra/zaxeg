/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.Properties;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FirstItemExpression;
import net.sf.saxon.expr.LastItemExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.RangeExpression;
import net.sf.saxon.expr.UntypedSequenceConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.DescendingComparer;
import net.sf.saxon.expr.sort.GenericAtomicComparer;
import net.sf.saxon.functions.CollatingFunctionFixed;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.StringToDouble;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.UntypedAtomicValue;

public abstract class Minimax
extends CollatingFunctionFixed {
    private PlainType argumentType = BuiltInAtomicType.ANY_ATOMIC;
    private boolean ignoreNaN = false;

    public abstract boolean isMaxFunction();

    public void setIgnoreNaN(boolean ignore) {
        this.ignoreNaN = ignore;
    }

    public boolean isIgnoreNaN() {
        return this.ignoreNaN;
    }

    public AtomicComparer getComparer() {
        return this.getPreAllocatedAtomicComparer();
    }

    public PlainType getArgumentType() {
        return this.argumentType;
    }

    @Override
    public void supplyTypeInformation(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType, Expression[] arguments) {
        ItemType type = arguments[0].getItemType();
        this.argumentType = type.getAtomizedItemType();
        if (this.argumentType instanceof AtomicType) {
            if (this.argumentType == BuiltInAtomicType.UNTYPED_ATOMIC) {
                this.argumentType = BuiltInAtomicType.DOUBLE;
            }
            this.preAllocateComparer((AtomicType)this.argumentType, (AtomicType)this.argumentType, visitor.getStaticContext());
        }
    }

    @Override
    public ItemType getResultItemType(Expression[] args) {
        TypeHierarchy th = this.getRetainedStaticContext().getConfiguration().getTypeHierarchy();
        ItemType base = Atomizer.getAtomizedItemType(args[0], false, th);
        if (base.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
            base = BuiltInAtomicType.DOUBLE;
        }
        return base.getPrimitiveItemType();
    }

    @Override
    public int getCardinality(Expression[] arguments) {
        if (!Cardinality.allowsZero(arguments[0].getCardinality())) {
            return 16384;
        }
        return 24576;
    }

    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        ItemType it;
        int card = arguments[0].getCardinality();
        if (!Cardinality.allowsMany(card) && (it = arguments[0].getItemType().getPrimitiveItemType()) instanceof BuiltInAtomicType && ((BuiltInAtomicType)it).isOrdered(false)) {
            TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
            if (th.relationship(it, BuiltInAtomicType.UNTYPED_ATOMIC) != Affinity.DISJOINT) {
                return UntypedSequenceConverter.makeUntypedSequenceConverter(visitor.getConfiguration(), arguments[0], BuiltInAtomicType.DOUBLE).typeCheck(visitor, contextInfo);
            }
            return arguments[0];
        }
        if (arguments[0] instanceof RangeExpression) {
            if (this.isMaxFunction()) {
                Expression start = ((RangeExpression)arguments[0]).getLhsExpression();
                Expression end = ((RangeExpression)arguments[0]).getRhsExpression();
                if (start instanceof Literal && end instanceof Literal) {
                    return end;
                }
                return new LastItemExpression(arguments[0]);
            }
            return FirstItemExpression.makeFirstItemExpression(arguments[0]);
        }
        return null;
    }

    @Override
    public AtomicComparer getAtomicComparer(XPathContext context) {
        AtomicComparer comparer = this.getPreAllocatedAtomicComparer();
        if (comparer != null) {
            return comparer;
        }
        AtomicType type = this.argumentType.getPrimitiveItemType();
        if (type.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
            type = BuiltInAtomicType.DOUBLE;
        }
        BuiltInAtomicType prim = (BuiltInAtomicType)type;
        return GenericAtomicComparer.makeAtomicComparer(prim, prim, this.getStringCollator(), context);
    }

    public static AtomicValue minimax(SequenceIterator iter, boolean isMaxFunction, AtomicComparer atomicComparer, boolean ignoreNaN, XPathContext context) throws XPathException {
        AtomicValue test;
        AtomicValue prim;
        AtomicValue min;
        boolean foundString;
        boolean foundNaN;
        boolean foundFloat;
        boolean foundDouble;
        StringToDouble converter;
        ConversionRules rules;
        block40: {
            block39: {
                rules = context.getConfiguration().getConversionRules();
                converter = context.getConfiguration().getConversionRules().getStringToDoubleConverter();
                foundDouble = false;
                foundFloat = false;
                foundNaN = false;
                foundString = false;
                if (isMaxFunction) {
                    atomicComparer = new DescendingComparer(atomicComparer);
                }
                atomicComparer = atomicComparer.provideContext(context);
                do {
                    if ((min = (AtomicValue)iter.next()) == null) {
                        return null;
                    }
                    prim = min;
                    if (min instanceof UntypedAtomicValue) {
                        try {
                            prim = min = new DoubleValue(converter.stringToNumber(min.getStringValueCS()));
                            foundDouble = true;
                        } catch (NumberFormatException e) {
                            XPathException de = new XPathException("Failure converting " + Err.wrap(min.getStringValueCS()) + " to a number");
                            de.setErrorCode("FORG0001");
                            de.setXPathContext(context);
                            throw de;
                        }
                    } else if (prim instanceof DoubleValue) {
                        foundDouble = true;
                    } else if (prim instanceof FloatValue) {
                        foundFloat = true;
                    } else if (prim instanceof StringValue && !(prim instanceof AnyURIValue)) {
                        foundString = true;
                    }
                    if (!prim.isNaN()) break block39;
                } while (ignoreNaN);
                if (prim instanceof DoubleValue) {
                    return min;
                }
                foundNaN = true;
                min = FloatValue.NaN;
                break block40;
            }
            if (!prim.getPrimitiveType().isOrdered(false)) {
                XPathException de = new XPathException("Type " + prim.getPrimitiveType() + " is not an ordered type");
                de.setErrorCode("FORG0006");
                de.setIsTypeError(true);
                de.setXPathContext(context);
                throw de;
            }
        }
        while ((test = (AtomicValue)iter.next()) != null) {
            AtomicValue test2;
            prim = test2 = test;
            if (test instanceof UntypedAtomicValue) {
                try {
                    test2 = new DoubleValue(converter.stringToNumber(test.getStringValueCS()));
                    if (foundNaN) {
                        return DoubleValue.NaN;
                    }
                    prim = test2;
                    foundDouble = true;
                } catch (NumberFormatException e) {
                    XPathException de = new XPathException("Failure converting " + Err.wrap(test.getStringValueCS()) + " to a number");
                    de.setErrorCode("FORG0001");
                    de.setXPathContext(context);
                    throw de;
                }
            } else if (prim instanceof DoubleValue) {
                if (foundNaN) {
                    return DoubleValue.NaN;
                }
                foundDouble = true;
            } else if (prim instanceof FloatValue) {
                foundFloat = true;
            } else if (prim instanceof StringValue && !(prim instanceof AnyURIValue)) {
                foundString = true;
            }
            if (prim.isNaN()) {
                if (ignoreNaN) continue;
                if (foundDouble) {
                    return DoubleValue.NaN;
                }
                foundNaN = true;
                continue;
            }
            try {
                if (atomicComparer.compareAtomicValues(prim, min) >= 0) continue;
                min = test2;
            } catch (ClassCastException err) {
                if (min.getItemType() == test2.getItemType()) {
                    throw err;
                }
                XPathException de = new XPathException("Cannot compare " + min.getItemType() + " with " + test2.getItemType());
                de.setErrorCode("FORG0006");
                de.setIsTypeError(true);
                de.setXPathContext(context);
                throw de;
            }
        }
        if (foundNaN) {
            return FloatValue.NaN;
        }
        if (foundDouble) {
            if (!(min instanceof DoubleValue)) {
                min = Converter.convert(min, BuiltInAtomicType.DOUBLE, rules);
            }
        } else if (foundFloat) {
            if (!(min instanceof FloatValue)) {
                min = Converter.convert(min, BuiltInAtomicType.FLOAT, rules);
            }
        } else if (min instanceof AnyURIValue && foundString) {
            min = Converter.convert(min, BuiltInAtomicType.STRING, rules);
        }
        return min;
    }

    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        return new ZeroOrOne<AtomicValue>(Minimax.minimax(arguments[0].iterate(), this.isMaxFunction(), this.getAtomicComparer(context), this.ignoreNaN, context));
    }

    @Override
    public void exportAttributes(ExpressionPresenter out) {
        super.exportAttributes(out);
        if (this.ignoreNaN) {
            out.emitAttribute("flags", "i");
        }
    }

    @Override
    public void importAttributes(Properties attributes) throws XPathException {
        super.importAttributes(attributes);
        String flags = attributes.getProperty("flags");
        if (flags != null && flags.contains("i")) {
            this.setIgnoreNaN(true);
        }
    }

    @Override
    public String getStreamerName() {
        return "Minimax";
    }

    public static class Max
    extends Minimax {
        @Override
        public boolean isMaxFunction() {
            return true;
        }
    }

    public static class Min
    extends Minimax {
        @Override
        public boolean isMaxFunction() {
            return false;
        }
    }
}

