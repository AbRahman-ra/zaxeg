/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.ComparisonException;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.value.Whitespace;

public class UntypedNumericComparer
implements AtomicComparer {
    private ConversionRules rules = ConversionRules.DEFAULT;
    private static double[][] bounds = new double[][]{{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, {1.0, 1.0, 10.0, 100.0, 1000.0, 10000.0, 100000.0, 1000000.0, 1.0E7, 1.0E8, 1.0E9, 1.0E10}, {1.0, 2.0, 20.0, 200.0, 2000.0, 20000.0, 200000.0, 2000000.0, 2.0E7, 2.0E8, 2.0E9, 2.0E10}, {1.0, 3.0, 30.0, 300.0, 3000.0, 30000.0, 300000.0, 3000000.0, 3.0E7, 3.0E8, 3.0E9, 3.0E10}, {1.0, 4.0, 40.0, 400.0, 4000.0, 40000.0, 400000.0, 4000000.0, 4.0E7, 4.0E8, 4.0E9, 4.0E10}, {1.0, 5.0, 50.0, 500.0, 5000.0, 50000.0, 500000.0, 5000000.0, 5.0E7, 5.0E8, 5.0E9, 5.0E10}, {1.0, 6.0, 60.0, 600.0, 6000.0, 60000.0, 600000.0, 6000000.0, 6.0E7, 6.0E8, 6.0E9, 6.0E10}, {1.0, 7.0, 70.0, 700.0, 7000.0, 70000.0, 700000.0, 7000000.0, 7.0E7, 7.0E8, 7.0E9, 7.0E10}, {1.0, 8.0, 80.0, 800.0, 8000.0, 80000.0, 800000.0, 8000000.0, 8.0E7, 8.0E8, 8.0E9, 8.0E10}, {1.0, 9.0, 90.0, 900.0, 9000.0, 90000.0, 900000.0, 9000000.0, 9.0E7, 9.0E8, 9.0E9, 9.0E10}, {1.0, 10.0, 100.0, 1000.0, 10000.0, 100000.0, 1000000.0, 1.0E7, 1.0E8, 1.0E9, 1.0E10, 1.0E11}};

    public static boolean quickCompare(UntypedAtomicValue a0, NumericValue a1, int operator, ConversionRules rules) throws XPathException {
        int comp = UntypedNumericComparer.quickComparison(a0, a1, rules);
        switch (operator) {
            case 50: {
                return comp == 0;
            }
            case 55: {
                return comp <= 0;
            }
            case 53: {
                return comp < 0;
            }
            case 54: {
                return comp >= 0;
            }
            case 52: {
                return comp > 0;
            }
        }
        return comp != 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static int quickComparison(UntypedAtomicValue a0, NumericValue a1, ConversionRules rules) throws XPathException {
        ConversionResult result;
        double d1 = a1.getDoubleValue();
        CharSequence cs = Whitespace.trimWhitespace(a0.getStringValueCS());
        boolean simple = true;
        int wholePartLength = 0;
        int firstDigit = -1;
        int decimalPoints = 0;
        int sign = 63;
        for (int i = 0; i < cs.length(); ++i) {
            char c = cs.charAt(i);
            if (c >= '0' && c <= '9') {
                if (firstDigit < 0) {
                    firstDigit = c - 48;
                }
                if (decimalPoints != 0) continue;
                ++wholePartLength;
                continue;
            }
            if (c == '-') {
                if (sign != 63 || wholePartLength > 0 || decimalPoints > 0) {
                    simple = false;
                    break;
                }
                sign = c;
                continue;
            }
            if (c == '.') {
                if (decimalPoints > 0) {
                    simple = false;
                    break;
                }
                decimalPoints = 1;
                continue;
            }
            simple = false;
            break;
        }
        if (firstDigit < 0) {
            simple = false;
        }
        if (simple && wholePartLength > 0 && wholePartLength <= 10) {
            double lowerBound = bounds[firstDigit][wholePartLength];
            double upperBound = bounds[firstDigit + 1][wholePartLength];
            if (sign == 45) {
                double temp = lowerBound;
                lowerBound = -upperBound;
                upperBound = -temp;
            }
            if (upperBound < d1) {
                return -1;
            }
            if (lowerBound > d1) {
                return 1;
            }
        }
        if (simple && decimalPoints == 0 && wholePartLength <= 15 && a1 instanceof Int64Value) {
            long l0 = Long.parseLong(cs.toString());
            return Long.compare(l0, a1.longValue());
        }
        UntypedAtomicValue c = a0;
        synchronized (c) {
            result = BuiltInAtomicType.DOUBLE.getStringConverter(rules).convertString(a0.getPrimitiveStringValue());
        }
        AtomicValue av = result.asAtomic();
        return Double.compare(((DoubleValue)av).getDoubleValue(), d1);
    }

    @Override
    public int compareAtomicValues(AtomicValue a, AtomicValue b) {
        try {
            return UntypedNumericComparer.quickComparison((UntypedAtomicValue)a, (NumericValue)b, this.rules);
        } catch (XPathException e) {
            throw new ComparisonException(e);
        }
    }

    @Override
    public StringCollator getCollator() {
        return null;
    }

    @Override
    public AtomicComparer provideContext(XPathContext context) {
        this.rules = context.getConfiguration().getConversionRules();
        return this;
    }

    @Override
    public boolean comparesEqual(AtomicValue a, AtomicValue b) {
        return this.compareAtomicValues(a, b) == 0;
    }

    @Override
    public String save() {
        return "QUNC";
    }
}

