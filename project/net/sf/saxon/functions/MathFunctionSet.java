/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.om.One;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.NumericValue;

public class MathFunctionSet
extends BuiltInFunctionSet {
    private static MathFunctionSet THE_INSTANCE = new MathFunctionSet();

    public static MathFunctionSet getInstance() {
        return THE_INSTANCE;
    }

    private MathFunctionSet() {
        this.init();
    }

    private void reg1(String name, Class<? extends SystemFunction> implementation) {
        this.register(name, 1, implementation, BuiltInAtomicType.DOUBLE, 24576, 32768).arg(0, BuiltInAtomicType.DOUBLE, 24576, EMPTY);
    }

    private void init() {
        this.register("pi", 0, PiFn.class, BuiltInAtomicType.DOUBLE, 16384, 0);
        this.reg1("sin", SinFn.class);
        this.reg1("cos", CosFn.class);
        this.reg1("tan", TanFn.class);
        this.reg1("asin", AsinFn.class);
        this.reg1("acos", AcosFn.class);
        this.reg1("atan", AtanFn.class);
        this.reg1("sqrt", SqrtFn.class);
        this.reg1("log", LogFn.class);
        this.reg1("log10", Log10Fn.class);
        this.reg1("exp", ExpFn.class);
        this.reg1("exp10", Exp10Fn.class);
        this.register("pow", 2, PowFn.class, BuiltInAtomicType.DOUBLE, 24576, 32768).arg(0, BuiltInAtomicType.DOUBLE, 24576, EMPTY).arg(1, BuiltInAtomicType.DOUBLE, 16384, null);
        this.register("atan2", 2, Atan2Fn.class, BuiltInAtomicType.DOUBLE, 16384, 0).arg(0, BuiltInAtomicType.DOUBLE, 16384, null).arg(1, BuiltInAtomicType.DOUBLE, 16384, null);
    }

    @Override
    public String getNamespace() {
        return "http://www.w3.org/2005/xpath-functions/math";
    }

    @Override
    public String getConventionalPrefix() {
        return "math";
    }

    public static class Atan2Fn
    extends SystemFunction {
        @Override
        public DoubleValue call(XPathContext context, Sequence[] arguments) throws XPathException {
            DoubleValue y = (DoubleValue)arguments[0].head();
            assert (y != null);
            DoubleValue x = (DoubleValue)arguments[1].head();
            assert (x != null);
            double result = Math.atan2(y.getDoubleValue(), x.getDoubleValue());
            return new DoubleValue(result);
        }
    }

    public static class PowFn
    extends SystemFunction {
        @Override
        public ZeroOrOne call(XPathContext context, Sequence[] args) throws XPathException {
            DoubleValue result;
            DoubleValue x = (DoubleValue)args[0].head();
            if (x == null) {
                result = null;
            } else {
                double dx = x.getDoubleValue();
                if (dx == 1.0) {
                    result = x;
                } else {
                    NumericValue y = (NumericValue)args[1].head();
                    assert (y != null);
                    double dy = y.getDoubleValue();
                    result = dx == -1.0 && Double.isInfinite(dy) ? new DoubleValue(1.0) : new DoubleValue(Math.pow(dx, dy));
                }
            }
            return new ZeroOrOne<Object>(result);
        }
    }

    public static class Exp10Fn
    extends TrigFn1 {
        @Override
        protected double compute(double input) {
            return Math.pow(10.0, input);
        }
    }

    public static class ExpFn
    extends TrigFn1 {
        @Override
        protected double compute(double input) {
            return Math.exp(input);
        }
    }

    public static class Log10Fn
    extends TrigFn1 {
        @Override
        protected double compute(double input) {
            return Math.log10(input);
        }
    }

    public static class LogFn
    extends TrigFn1 {
        @Override
        protected double compute(double input) {
            return Math.log(input);
        }
    }

    public static class SqrtFn
    extends TrigFn1 {
        @Override
        protected double compute(double input) {
            return Math.sqrt(input);
        }
    }

    public static class AtanFn
    extends TrigFn1 {
        @Override
        protected double compute(double input) {
            return Math.atan(input);
        }
    }

    public static class AcosFn
    extends TrigFn1 {
        @Override
        protected double compute(double input) {
            return Math.acos(input);
        }
    }

    public static class AsinFn
    extends TrigFn1 {
        @Override
        protected double compute(double input) {
            return Math.asin(input);
        }
    }

    public static class TanFn
    extends TrigFn1 {
        @Override
        protected double compute(double input) {
            return Math.tan(input);
        }
    }

    public static class CosFn
    extends TrigFn1 {
        @Override
        protected double compute(double input) {
            return Math.cos(input);
        }
    }

    public static class SinFn
    extends TrigFn1 {
        @Override
        protected double compute(double input) {
            return Math.sin(input);
        }
    }

    private static abstract class TrigFn1
    extends SystemFunction {
        private TrigFn1() {
        }

        protected abstract double compute(double var1);

        @Override
        public ZeroOrOne call(XPathContext context, Sequence[] args) throws XPathException {
            DoubleValue in = (DoubleValue)args[0].head();
            if (in == null) {
                return ZeroOrOne.empty();
            }
            return One.dbl(this.compute(in.getDoubleValue()));
        }
    }

    public static class PiFn
    extends SystemFunction {
        @Override
        public Expression makeFunctionCall(Expression ... arguments) {
            return Literal.makeLiteral(new DoubleValue(Math.PI));
        }

        @Override
        public DoubleValue call(XPathContext context, Sequence[] arguments) throws XPathException {
            return new DoubleValue(Math.PI);
        }
    }
}

