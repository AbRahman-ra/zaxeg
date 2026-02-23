/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.One;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.StringToDouble11;
import net.sf.saxon.value.StringValue;

public class Number_1
extends ScalarSystemFunction {
    @Override
    public AtomicValue evaluate(Item arg, XPathContext context) throws XPathException {
        return Number_1.toNumber((AtomicValue)arg);
    }

    @Override
    public ZeroOrOne resultWhenEmpty() {
        return new One<DoubleValue>(DoubleValue.NaN);
    }

    @Override
    public Expression typeCheckCaller(FunctionCall caller, ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) {
        if (caller.getArg(0).isCallOn(Number_1.class)) {
            caller.setArg(0, ((FunctionCall)caller.getArg(0)).getArg(0));
        }
        return caller;
    }

    public static DoubleValue toNumber(AtomicValue arg0) {
        if (arg0 instanceof BooleanValue) {
            return Converter.BooleanToDouble.INSTANCE.convert((BooleanValue)arg0);
        }
        if (arg0 instanceof NumericValue) {
            return (DoubleValue)Converter.NumericToDouble.INSTANCE.convert((NumericValue)arg0).asAtomic();
        }
        if (arg0 instanceof StringValue && !(arg0 instanceof AnyURIValue)) {
            ConversionResult cr = StringToDouble11.getInstance().convert((StringValue)arg0);
            if (cr instanceof ValidationFailure) {
                return DoubleValue.NaN;
            }
            return (DoubleValue)cr;
        }
        return DoubleValue.NaN;
    }

    public static DoubleValue convert(AtomicValue value, Configuration config) {
        try {
            if (value == null) {
                return DoubleValue.NaN;
            }
            if (value instanceof BooleanValue) {
                return new DoubleValue(((BooleanValue)value).getBooleanValue() ? 1.0 : 0.0);
            }
            if (value instanceof DoubleValue) {
                return (DoubleValue)value;
            }
            if (value instanceof NumericValue) {
                return new DoubleValue(((NumericValue)value).getDoubleValue());
            }
            if (value instanceof StringValue && !(value instanceof AnyURIValue)) {
                double d = config.getConversionRules().getStringToDoubleConverter().stringToNumber(value.getStringValueCS());
                return new DoubleValue(d);
            }
            return DoubleValue.NaN;
        } catch (NumberFormatException e) {
            return DoubleValue.NaN;
        }
    }

    @Override
    public String getCompilerName() {
        return "NumberFnCompiler";
    }
}

