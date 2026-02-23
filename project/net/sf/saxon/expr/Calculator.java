/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BigIntegerValue;
import net.sf.saxon.value.CalendarValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.z.IntHashMap;

public abstract class Calculator {
    public static final int PLUS = 0;
    public static final int MINUS = 1;
    public static final int TIMES = 2;
    public static final int DIV = 3;
    public static final int MOD = 4;
    public static final int IDIV = 5;
    private static final int[] tokens = new int[]{15, 16, 17, 18, 19, 56};
    public static final Calculator[] ANY_ANY = new Calculator[]{new AnyPlusAny(), new AnyMinusAny(), new AnyTimesAny(), new AnyDivAny(), new AnyModAny(), new AnyIdivAny()};
    public static final Calculator[] DOUBLE_DOUBLE = new Calculator[]{new DoublePlusDouble(), new DoubleMinusDouble(), new DoubleTimesDouble(), new DoubleDivDouble(), new DoubleModDouble(), new DoubleIdivDouble()};
    public static final Calculator[] DOUBLE_FLOAT = DOUBLE_DOUBLE;
    public static final Calculator[] DOUBLE_DECIMAL = DOUBLE_DOUBLE;
    public static final Calculator[] DOUBLE_INTEGER = DOUBLE_DOUBLE;
    public static final Calculator[] FLOAT_DOUBLE = DOUBLE_DOUBLE;
    public static final Calculator[] FLOAT_FLOAT = new Calculator[]{new FloatPlusFloat(), new FloatMinusFloat(), new FloatTimesFloat(), new FloatDivFloat(), new FloatModFloat(), new FloatIdivFloat()};
    public static final Calculator[] FLOAT_DECIMAL = FLOAT_FLOAT;
    public static final Calculator[] FLOAT_INTEGER = FLOAT_FLOAT;
    public static final Calculator[] DECIMAL_DOUBLE = DOUBLE_DOUBLE;
    public static final Calculator[] DECIMAL_FLOAT = FLOAT_FLOAT;
    public static final Calculator[] DECIMAL_DECIMAL = new Calculator[]{new DecimalPlusDecimal(), new DecimalMinusDecimal(), new DecimalTimesDecimal(), new DecimalDivDecimal(), new DecimalModDecimal(), new DecimalIdivDecimal()};
    public static final Calculator[] DECIMAL_INTEGER = DECIMAL_DECIMAL;
    public static final Calculator[] INTEGER_DOUBLE = DOUBLE_DOUBLE;
    public static final Calculator[] INTEGER_FLOAT = FLOAT_FLOAT;
    public static final Calculator[] INTEGER_DECIMAL = DECIMAL_DECIMAL;
    public static final Calculator[] INTEGER_INTEGER = new Calculator[]{new IntegerPlusInteger(), new IntegerMinusInteger(), new IntegerTimesInteger(), new IntegerDivInteger(), new IntegerModInteger(), new IntegerIdivInteger()};
    public static final Calculator[] DATETIME_DATETIME = new Calculator[]{null, new DateTimeMinusDateTime(), null, null, null, null};
    public static final Calculator[] DATETIME_DURATION = new Calculator[]{new DateTimePlusDuration(), new DateTimeMinusDuration(), null, null, null, null};
    public static final Calculator[] DURATION_DATETIME = new Calculator[]{new DurationPlusDateTime(), null, null, null, null, null};
    public static final Calculator[] DURATION_DURATION = new Calculator[]{new DurationPlusDuration(), new DurationMinusDuration(), null, new DurationDivDuration(), null, null};
    public static final Calculator[] DURATION_NUMERIC = new Calculator[]{null, null, new DurationTimesNumeric(), new DurationDivNumeric(), null, null};
    public static final Calculator[] NUMERIC_DURATION = new Calculator[]{null, null, new NumericTimesDuration(), null, null, null};
    private static IntHashMap<Calculator[]> table = new IntHashMap(100);
    private static IntHashMap<String> nameTable = new IntHashMap(100);

    public static int getTokenFromOperator(int operator) {
        return tokens[operator];
    }

    public String code() {
        String name = this.getClass().getSimpleName();
        return name.replaceAll("Any", "a").replaceAll("Double", "d").replaceAll("Float", "f").replaceAll("Decimal", "c").replaceAll("Integer", "i").replaceAll("Numeric", "n").replaceAll("DateTime", "t").replaceAll("Duration", "u").replaceAll("Plus", "+").replaceAll("Minus", "-").replaceAll("Times", "*").replaceAll("Div", "/").replaceAll("Idiv", "~").replaceAll("Mod", "%");
    }

    private static void def(int typeA, int typeB, Calculator[] calculatorSet, String setName) {
        int key = (typeA & 0xFFFF) << 16 | typeB & 0xFFFF;
        table.put(key, calculatorSet);
        nameTable.put(key, setName);
        if (typeA == 518) {
            Calculator.def(634, typeB, calculatorSet, setName);
            Calculator.def(633, typeB, calculatorSet, setName);
        }
        if (typeB == 518) {
            Calculator.def(typeA, 634, calculatorSet, setName);
            Calculator.def(typeA, 633, calculatorSet, setName);
        }
        if (typeA == 519) {
            Calculator.def(521, typeB, calculatorSet, setName);
            Calculator.def(520, typeB, calculatorSet, setName);
        }
        if (typeB == 519) {
            Calculator.def(typeA, 521, calculatorSet, setName);
            Calculator.def(typeA, 520, calculatorSet, setName);
        }
        if (typeA == 517) {
            Calculator.def(631, typeB, calculatorSet, setName);
        }
        if (typeB == 517) {
            Calculator.def(typeA, 631, calculatorSet, setName);
        }
    }

    public static Calculator getCalculator(int typeA, int typeB, int operator, boolean mustResolve) {
        int key = (typeA & 0xFFFF) << 16 | typeB & 0xFFFF;
        Calculator[] set = table.get(key);
        if (set == null) {
            if (mustResolve) {
                return null;
            }
            return ANY_ANY[operator];
        }
        return set[operator];
    }

    public static Calculator reconstructCalculator(String code) {
        int typeA = Calculator.typeFromCode(code.charAt(0));
        int typeB = Calculator.typeFromCode(code.charAt(2));
        int operator = Calculator.operatorFromCode(code.charAt(1));
        return Calculator.getCalculator(typeA, typeB, operator, false);
    }

    private static int typeFromCode(char code) {
        switch (code) {
            case 'a': {
                return 632;
            }
            case 'd': {
                return 517;
            }
            case 'i': {
                return 533;
            }
            case 'f': {
                return 516;
            }
            case 'c': {
                return 515;
            }
            case 'n': {
                return 635;
            }
            case 't': {
                return 519;
            }
            case 'u': {
                return 518;
            }
        }
        throw new AssertionError();
    }

    public static int operatorFromCode(char code) {
        switch (code) {
            case '+': {
                return 0;
            }
            case '-': {
                return 1;
            }
            case '*': {
                return 2;
            }
            case '/': {
                return 3;
            }
            case '~': {
                return 5;
            }
            case '%': {
                return 4;
            }
        }
        throw new AssertionError();
    }

    public static String getCalculatorSetName(int typeA, int typeB) {
        int key = (typeA & 0xFFFF) << 16 | typeB & 0xFFFF;
        return nameTable.get(key);
    }

    public abstract AtomicValue compute(AtomicValue var1, AtomicValue var2, XPathContext var3) throws XPathException;

    public abstract AtomicType getResultType(AtomicType var1, AtomicType var2);

    public static BigDecimalValue decimalDivide(NumericValue a, NumericValue b) throws XPathException {
        BigDecimal A = a.getDecimalValue();
        BigDecimal B = b.getDecimalValue();
        int scale = Math.max(18, A.scale() - B.scale() + 18);
        try {
            BigDecimal result = A.divide(B, scale, RoundingMode.HALF_DOWN);
            return new BigDecimalValue(result);
        } catch (ArithmeticException err) {
            if (b.compareTo(0L) == 0) {
                throw new XPathException("Decimal divide by zero", "FOAR0001");
            }
            throw err;
        }
    }

    static {
        Calculator.def(517, 517, DOUBLE_DOUBLE, "DOUBLE_DOUBLE");
        Calculator.def(517, 516, DOUBLE_FLOAT, "DOUBLE_FLOAT");
        Calculator.def(517, 515, DOUBLE_DECIMAL, "DOUBLE_DECIMAL");
        Calculator.def(517, 533, DOUBLE_INTEGER, "DOUBLE_INTEGER");
        Calculator.def(516, 517, FLOAT_DOUBLE, "FLOAT_DOUBLE");
        Calculator.def(516, 516, FLOAT_FLOAT, "FLOAT_FLOAT");
        Calculator.def(516, 515, FLOAT_DECIMAL, "FLOAT_DECIMAL");
        Calculator.def(516, 533, FLOAT_INTEGER, "FLOAT_INTEGER");
        Calculator.def(515, 517, DECIMAL_DOUBLE, "DECIMAL_DOUBLE");
        Calculator.def(515, 516, DECIMAL_FLOAT, "DECIMAL_FLOAT");
        Calculator.def(515, 515, DECIMAL_DECIMAL, "DECIMAL_DECIMAL");
        Calculator.def(515, 533, DECIMAL_INTEGER, "DECIMAL_INTEGER");
        Calculator.def(533, 517, INTEGER_DOUBLE, "INTEGER_DOUBLE");
        Calculator.def(533, 516, INTEGER_FLOAT, "INTEGER_FLOAT");
        Calculator.def(533, 515, INTEGER_DECIMAL, "INTEGER_DECIMAL");
        Calculator.def(533, 533, INTEGER_INTEGER, "INTEGER_INTEGER");
        Calculator.def(519, 519, DATETIME_DATETIME, "DATETIME_DATETIME");
        Calculator.def(519, 518, DATETIME_DURATION, "DATETIME_DURATION");
        Calculator.def(518, 519, DURATION_DATETIME, "DURATION_DATETIME");
        Calculator.def(518, 518, DURATION_DURATION, "DURATION_DURATION");
        Calculator.def(518, 517, DURATION_NUMERIC, "DURATION_NUMERIC");
        Calculator.def(518, 516, DURATION_NUMERIC, "DURATION_NUMERIC");
        Calculator.def(518, 515, DURATION_NUMERIC, "DURATION_NUMERIC");
        Calculator.def(518, 533, DURATION_NUMERIC, "DURATION_NUMERIC");
        Calculator.def(517, 518, NUMERIC_DURATION, "NUMERIC_DURATION");
        Calculator.def(516, 518, NUMERIC_DURATION, "NUMERIC_DURATION");
        Calculator.def(515, 518, NUMERIC_DURATION, "NUMERIC_DURATION");
        Calculator.def(533, 518, NUMERIC_DURATION, "NUMERIC_DURATION");
    }

    private static class DurationDivNumeric
    extends Calculator {
        private DurationDivNumeric() {
        }

        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            double d = 1.0 / ((NumericValue)b).getDoubleValue();
            return ((DurationValue)a).multiply(d);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return typeA;
        }
    }

    private static class NumericTimesDuration
    extends Calculator {
        private NumericTimesDuration() {
        }

        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            if (a instanceof Int64Value) {
                return ((DurationValue)b).multiply(((Int64Value)a).longValue());
            }
            return ((DurationValue)b).multiply(((NumericValue)a).getDoubleValue());
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return typeB;
        }
    }

    private static class DurationTimesNumeric
    extends Calculator {
        private DurationTimesNumeric() {
        }

        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            if (b instanceof Int64Value) {
                return ((DurationValue)a).multiply(((Int64Value)b).longValue());
            }
            return ((DurationValue)a).multiply(((NumericValue)b).getDoubleValue());
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return typeA;
        }
    }

    private static class DurationDivDuration
    extends Calculator {
        private DurationDivDuration() {
        }

        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return ((DurationValue)a).divide((DurationValue)b);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.DECIMAL;
        }
    }

    private static class DurationMinusDuration
    extends Calculator {
        private DurationMinusDuration() {
        }

        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return ((DurationValue)a).subtract((DurationValue)b);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return typeA;
        }
    }

    private static class DurationPlusDuration
    extends Calculator {
        private DurationPlusDuration() {
        }

        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return ((DurationValue)a).add((DurationValue)b);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return typeA;
        }
    }

    private static class DurationPlusDateTime
    extends Calculator {
        private DurationPlusDateTime() {
        }

        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return ((CalendarValue)b).add((DurationValue)a);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return typeB;
        }
    }

    private static class DateTimeMinusDuration
    extends Calculator {
        private DateTimeMinusDuration() {
        }

        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return ((CalendarValue)a).add(((DurationValue)b).negate());
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return typeA;
        }
    }

    private static class DateTimePlusDuration
    extends Calculator {
        private DateTimePlusDuration() {
        }

        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return ((CalendarValue)a).add((DurationValue)b);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return typeA;
        }
    }

    private static class DateTimeMinusDateTime
    extends Calculator {
        private DateTimeMinusDateTime() {
        }

        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return ((CalendarValue)a).subtract((CalendarValue)b, c);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.DAY_TIME_DURATION;
        }
    }

    public static class IntegerIdivInteger
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return ((IntegerValue)a).idiv((IntegerValue)b);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.INTEGER;
        }
    }

    public static class IntegerModInteger
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return ((IntegerValue)a).mod((IntegerValue)b);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.INTEGER;
        }
    }

    public static class IntegerDivInteger
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return ((IntegerValue)a).div((IntegerValue)b);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.DECIMAL;
        }
    }

    public static class IntegerTimesInteger
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return ((IntegerValue)a).times((IntegerValue)b);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.INTEGER;
        }
    }

    public static class IntegerMinusInteger
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return ((IntegerValue)a).minus((IntegerValue)b);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.INTEGER;
        }
    }

    public static class IntegerPlusInteger
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return ((IntegerValue)a).plus((IntegerValue)b);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.INTEGER;
        }
    }

    public static class DecimalIdivDecimal
    extends Calculator {
        @Override
        public IntegerValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            if (a instanceof IntegerValue && b instanceof IntegerValue) {
                return ((IntegerValue)a).idiv((IntegerValue)b);
            }
            BigDecimal A = ((NumericValue)a).getDecimalValue();
            BigDecimal B = ((NumericValue)b).getDecimalValue();
            if (B.signum() == 0) {
                throw new XPathException("Integer division by zero", "FOAR0001", c);
            }
            BigInteger quot = A.divideToIntegralValue(B).toBigInteger();
            return BigIntegerValue.makeIntegerValue(quot);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.INTEGER;
        }
    }

    public static class DecimalModDecimal
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            if (a instanceof IntegerValue && b instanceof IntegerValue) {
                return ((IntegerValue)a).mod((IntegerValue)b);
            }
            BigDecimal A = ((NumericValue)a).getDecimalValue();
            BigDecimal B = ((NumericValue)b).getDecimalValue();
            try {
                return new BigDecimalValue(A.remainder(B));
            } catch (ArithmeticException err) {
                if (((NumericValue)b).compareTo(0L) == 0) {
                    throw new XPathException("Decimal modulo zero", "FOAR0001", c);
                }
                throw err;
            }
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.DECIMAL;
        }
    }

    public static class DecimalDivDecimal
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return DecimalDivDecimal.decimalDivide((NumericValue)a, (NumericValue)b);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.DECIMAL;
        }
    }

    public static class DecimalTimesDecimal
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            if (a instanceof IntegerValue && b instanceof IntegerValue) {
                return ((IntegerValue)a).times((IntegerValue)b);
            }
            return new BigDecimalValue(((NumericValue)a).getDecimalValue().multiply(((NumericValue)b).getDecimalValue()));
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.DECIMAL;
        }
    }

    public static class DecimalMinusDecimal
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            if (a instanceof IntegerValue && b instanceof IntegerValue) {
                return ((IntegerValue)a).minus((IntegerValue)b);
            }
            return new BigDecimalValue(((NumericValue)a).getDecimalValue().subtract(((NumericValue)b).getDecimalValue()));
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.DECIMAL;
        }
    }

    public static class DecimalPlusDecimal
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            if (a instanceof IntegerValue && b instanceof IntegerValue) {
                return ((IntegerValue)a).plus((IntegerValue)b);
            }
            return new BigDecimalValue(((NumericValue)a).getDecimalValue().add(((NumericValue)b).getDecimalValue()));
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.DECIMAL;
        }
    }

    public static class FloatIdivFloat
    extends Calculator {
        @Override
        public IntegerValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            float A = ((NumericValue)a).getFloatValue();
            float B = ((NumericValue)b).getFloatValue();
            if ((double)B == 0.0) {
                throw new XPathException("Integer division by zero", "FOAR0001", c);
            }
            if (Float.isNaN(A) || Float.isInfinite(A)) {
                throw new XPathException("First operand of idiv is NaN or infinity", "FOAR0002", c);
            }
            if (Float.isNaN(B)) {
                throw new XPathException("Second operand of idiv is NaN", "FOAR0002", c);
            }
            float quotient = A / B;
            if (Float.isInfinite(quotient)) {
                return new DecimalIdivDecimal().compute(new BigDecimalValue(((NumericValue)a).getDecimalValue()), new BigDecimalValue(((NumericValue)b).getDecimalValue()), c);
            }
            return (IntegerValue)Converter.FloatToInteger.INSTANCE.convert(new FloatValue(quotient)).asAtomic();
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.INTEGER;
        }
    }

    public static class FloatModFloat
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return new FloatValue(((NumericValue)a).getFloatValue() % ((NumericValue)b).getFloatValue());
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.FLOAT;
        }
    }

    public static class FloatDivFloat
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return new FloatValue(((NumericValue)a).getFloatValue() / ((NumericValue)b).getFloatValue());
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.FLOAT;
        }
    }

    public static class FloatTimesFloat
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return new FloatValue(((NumericValue)a).getFloatValue() * ((NumericValue)b).getFloatValue());
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.FLOAT;
        }
    }

    public static class FloatMinusFloat
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return new FloatValue(((NumericValue)a).getFloatValue() - ((NumericValue)b).getFloatValue());
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.FLOAT;
        }
    }

    public static class FloatPlusFloat
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return new FloatValue(((NumericValue)a).getFloatValue() + ((NumericValue)b).getFloatValue());
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.FLOAT;
        }
    }

    private static class DoubleIdivDouble
    extends Calculator {
        private DoubleIdivDouble() {
        }

        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            double A = ((NumericValue)a).getDoubleValue();
            double B = ((NumericValue)b).getDoubleValue();
            if (B == 0.0) {
                throw new XPathException("Integer division by zero", "FOAR0001", c);
            }
            if (Double.isNaN(A) || Double.isInfinite(A)) {
                throw new XPathException("First operand of idiv is NaN or infinity", "FOAR0002", c);
            }
            if (Double.isNaN(B)) {
                throw new XPathException("Second operand of idiv is NaN", "FOAR0002", c);
            }
            return IntegerValue.makeIntegerValue(new DoubleValue(A / B)).asAtomic();
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.INTEGER;
        }
    }

    public static class DoubleModDouble
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return new DoubleValue(((NumericValue)a).getDoubleValue() % ((NumericValue)b).getDoubleValue());
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.DOUBLE;
        }
    }

    public static class DoubleDivDouble
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return new DoubleValue(((NumericValue)a).getDoubleValue() / ((NumericValue)b).getDoubleValue());
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.DOUBLE;
        }
    }

    public static class DoubleTimesDouble
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return new DoubleValue(((NumericValue)a).getDoubleValue() * ((NumericValue)b).getDoubleValue());
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.DOUBLE;
        }
    }

    public static class DoubleMinusDouble
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return new DoubleValue(((NumericValue)a).getDoubleValue() - ((NumericValue)b).getDoubleValue());
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.DOUBLE;
        }
    }

    public static class DoublePlusDouble
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            return new DoubleValue(((NumericValue)a).getDoubleValue() + ((NumericValue)b).getDoubleValue());
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.DOUBLE;
        }
    }

    public static class AnyIdivAny
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            Calculator calc = AnyIdivAny.getCalculator(a.getItemType().getPrimitiveType(), b.getItemType().getPrimitiveType(), 5, true);
            if (calc == null) {
                throw new XPathException("Unsuitable types for idiv operation (" + Type.displayTypeName(a) + ", " + Type.displayTypeName(b) + ")", "XPTY0004", c);
            }
            return calc.compute(a, b, c);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.ANY_ATOMIC;
        }
    }

    public static class AnyModAny
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            Calculator calc = AnyModAny.getCalculator(a.getItemType().getPrimitiveType(), b.getItemType().getPrimitiveType(), 4, true);
            if (calc == null) {
                throw new XPathException("Unsuitable types for mod operation (" + Type.displayTypeName(a) + ", " + Type.displayTypeName(b) + ")", "XPTY0004", c);
            }
            return calc.compute(a, b, c);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.ANY_ATOMIC;
        }
    }

    public static class AnyDivAny
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            Calculator calc = AnyDivAny.getCalculator(a.getItemType().getPrimitiveType(), b.getItemType().getPrimitiveType(), 3, true);
            if (calc == null) {
                throw new XPathException("Unsuitable types for div operation (" + Type.displayTypeName(a) + ", " + Type.displayTypeName(b) + ")", "XPTY0004", c);
            }
            return calc.compute(a, b, c);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.ANY_ATOMIC;
        }
    }

    public static class AnyTimesAny
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            Calculator calc = AnyTimesAny.getCalculator(a.getItemType().getPrimitiveType(), b.getItemType().getPrimitiveType(), 2, true);
            if (calc == null) {
                throw new XPathException("Unsuitable types for * operation (" + Type.displayTypeName(a) + ", " + Type.displayTypeName(b) + ")", "XPTY0004", c);
            }
            return calc.compute(a, b, c);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.ANY_ATOMIC;
        }
    }

    public static class AnyMinusAny
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            Calculator calc = AnyMinusAny.getCalculator(a.getItemType().getPrimitiveType(), b.getItemType().getPrimitiveType(), 1, true);
            if (calc == null) {
                throw new XPathException("Unsuitable types for - operation (" + Type.displayTypeName(a) + ", " + Type.displayTypeName(b) + ")", "XPTY0004", c);
            }
            return calc.compute(a, b, c);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.ANY_ATOMIC;
        }
    }

    public static class AnyPlusAny
    extends Calculator {
        @Override
        public AtomicValue compute(AtomicValue a, AtomicValue b, XPathContext c) throws XPathException {
            Calculator calc = AnyPlusAny.getCalculator(a.getItemType().getPrimitiveType(), b.getItemType().getPrimitiveType(), 0, true);
            if (calc == null) {
                throw new XPathException("Unsuitable types for + operation (" + Type.displayTypeName(a) + ", " + Type.displayTypeName(b) + ")", "XPTY0004", c);
            }
            return calc.compute(a, b, c);
        }

        @Override
        public AtomicType getResultType(AtomicType typeA, AtomicType typeB) {
            return BuiltInAtomicType.ANY_ATOMIC;
        }
    }
}

