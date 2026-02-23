/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.math.BigInteger;
import net.sf.saxon.tree.util.FastStringBuffer;

public class FloatingPointConverter {
    public static FloatingPointConverter THE_INSTANCE = new FloatingPointConverter();
    private static final char[] NEGATIVE_INFINITY = new char[]{'-', 'I', 'N', 'F'};
    private static final char[] POSITIVE_INFINITY = new char[]{'I', 'N', 'F'};
    private static final char[] NaN = new char[]{'N', 'a', 'N'};
    private static final char[] charForDigit = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    public static final long DOUBLE_SIGN_MASK = Long.MIN_VALUE;
    private static final long doubleExpMask = 0x7FF0000000000000L;
    private static final int doubleExpShift = 52;
    private static final int doubleExpBias = 1023;
    private static final long doubleFractMask = 0xFFFFFFFFFFFFFL;
    public static final int FLOAT_SIGN_MASK = Integer.MIN_VALUE;
    private static final int floatExpMask = 2139095040;
    private static final int floatExpShift = 23;
    private static final int floatExpBias = 127;
    private static final int floatFractMask = 0x7FFFFF;
    private static final BigInteger TEN = BigInteger.valueOf(10L);
    private static final BigInteger NINE = BigInteger.valueOf(9L);

    private FloatingPointConverter() {
    }

    public static FastStringBuffer appendInt(FastStringBuffer s, int i) {
        if (i < 0) {
            if (i == Integer.MIN_VALUE) {
                s.append("-2147483648");
                return s;
            }
            s.cat('-');
            i = -i;
        }
        if (i < 10) {
            s.cat(charForDigit[i]);
            return s;
        }
        if (i < 100) {
            s.cat(charForDigit[i / 10]);
            s.cat(charForDigit[i % 10]);
            return s;
        }
        if (i < 1000) {
            s.cat(charForDigit[i / 100]);
            int c = i % 100;
            s.cat(charForDigit[c / 10]);
            s.cat(charForDigit[c % 10]);
            return s;
        }
        if (i < 10000) {
            s.cat(charForDigit[i / 1000]);
            int c = i % 1000;
            s.cat(charForDigit[c / 100]);
            s.cat(charForDigit[(c %= 100) / 10]);
            s.cat(charForDigit[c % 10]);
            return s;
        }
        if (i < 100000) {
            s.cat(charForDigit[i / 10000]);
            int c = i % 10000;
            s.cat(charForDigit[c / 1000]);
            s.cat(charForDigit[(c %= 1000) / 100]);
            s.cat(charForDigit[(c %= 100) / 10]);
            s.cat(charForDigit[c % 10]);
            return s;
        }
        if (i < 1000000) {
            s.cat(charForDigit[i / 100000]);
            int c = i % 100000;
            s.cat(charForDigit[c / 10000]);
            s.cat(charForDigit[(c %= 10000) / 1000]);
            s.cat(charForDigit[(c %= 1000) / 100]);
            s.cat(charForDigit[(c %= 100) / 10]);
            s.cat(charForDigit[c % 10]);
            return s;
        }
        if (i < 10000000) {
            s.cat(charForDigit[i / 1000000]);
            int c = i % 1000000;
            s.cat(charForDigit[c / 100000]);
            s.cat(charForDigit[(c %= 100000) / 10000]);
            s.cat(charForDigit[(c %= 10000) / 1000]);
            s.cat(charForDigit[(c %= 1000) / 100]);
            s.cat(charForDigit[(c %= 100) / 10]);
            s.cat(charForDigit[c % 10]);
            return s;
        }
        if (i < 100000000) {
            s.cat(charForDigit[i / 10000000]);
            int c = i % 10000000;
            s.cat(charForDigit[c / 1000000]);
            s.cat(charForDigit[(c %= 1000000) / 100000]);
            s.cat(charForDigit[(c %= 100000) / 10000]);
            s.cat(charForDigit[(c %= 10000) / 1000]);
            s.cat(charForDigit[(c %= 1000) / 100]);
            s.cat(charForDigit[(c %= 100) / 10]);
            s.cat(charForDigit[c % 10]);
            return s;
        }
        if (i < 1000000000) {
            s.cat(charForDigit[i / 100000000]);
            int c = i % 100000000;
            s.cat(charForDigit[c / 10000000]);
            s.cat(charForDigit[(c %= 10000000) / 1000000]);
            s.cat(charForDigit[(c %= 1000000) / 100000]);
            s.cat(charForDigit[(c %= 100000) / 10000]);
            s.cat(charForDigit[(c %= 10000) / 1000]);
            s.cat(charForDigit[(c %= 1000) / 100]);
            s.cat(charForDigit[(c %= 100) / 10]);
            s.cat(charForDigit[c % 10]);
            return s;
        }
        s.cat(charForDigit[i / 1000000000]);
        int c = i % 1000000000;
        s.cat(charForDigit[c / 100000000]);
        s.cat(charForDigit[(c %= 100000000) / 10000000]);
        s.cat(charForDigit[(c %= 10000000) / 1000000]);
        s.cat(charForDigit[(c %= 1000000) / 100000]);
        s.cat(charForDigit[(c %= 100000) / 10000]);
        s.cat(charForDigit[(c %= 10000) / 1000]);
        s.cat(charForDigit[(c %= 1000) / 100]);
        s.cat(charForDigit[(c %= 100) / 10]);
        s.cat(charForDigit[c % 10]);
        return s;
    }

    private static void fppfpp(FastStringBuffer sb, int e, long f, int p) {
        boolean high;
        boolean low;
        int U;
        long Mminus;
        long R = f << Math.max(e - p, 0);
        long S = 1L << Math.max(0, -(e - p));
        long Mplus = Mminus = 1L << Math.max(e - p, 0);
        boolean initial = true;
        if (f == 1L << p - 1) {
            Mplus <<= 1;
            R <<= 1;
            S <<= 1;
        }
        int k = 0;
        while (R < (S + 9L) / 10L) {
            --k;
            R *= 10L;
            Mminus *= 10L;
            Mplus *= 10L;
        }
        while (2L * R + Mplus >= 2L * S) {
            S *= 10L;
            ++k;
        }
        for (int z = k; z < 0; ++z) {
            if (initial) {
                sb.append("0.");
            }
            initial = false;
            sb.cat('0');
        }
        while (true) {
            --k;
            long R10 = R * 10L;
            U = (int)(R10 / S);
            R = R10 - (long)U * S;
            low = 2L * R < (Mminus *= 10L);
            boolean bl = high = 2L * R > 2L * S - (Mplus *= 10L);
            if (low || high) break;
            if (k == -1) {
                if (initial) {
                    sb.cat('0');
                }
                sb.cat('.');
            }
            sb.cat(charForDigit[U]);
            initial = false;
        }
        if (high && (!low || 2L * R > S)) {
            ++U;
        }
        if (k == -1) {
            if (initial) {
                sb.cat('0');
            }
            sb.cat('.');
        }
        sb.cat(charForDigit[U]);
        for (int z = 0; z < k; ++z) {
            sb.cat('0');
        }
    }

    private static void fppfppBig(FastStringBuffer sb, int e, long f, int p) {
        boolean high;
        boolean low;
        int U;
        BigInteger Mminus;
        BigInteger R = BigInteger.valueOf(f).shiftLeft(Math.max(e - p, 0));
        BigInteger S = BigInteger.ONE.shiftLeft(Math.max(0, -(e - p)));
        BigInteger Mplus = Mminus = BigInteger.ONE.shiftLeft(Math.max(e - p, 0));
        boolean initial = true;
        if (f == 1L << p - 1) {
            Mplus = Mplus.shiftLeft(1);
            R = R.shiftLeft(1);
            S = S.shiftLeft(1);
        }
        int k = 0;
        while (R.compareTo(S.add(NINE).divide(TEN)) < 0) {
            --k;
            R = R.multiply(TEN);
            Mminus = Mminus.multiply(TEN);
            Mplus = Mplus.multiply(TEN);
        }
        while (R.shiftLeft(1).add(Mplus).compareTo(S.shiftLeft(1)) >= 0) {
            S = S.multiply(TEN);
            ++k;
        }
        for (int z = k; z < 0; ++z) {
            if (initial) {
                sb.append("0.");
            }
            initial = false;
            sb.cat('0');
        }
        while (true) {
            --k;
            BigInteger R10 = R.multiply(TEN);
            U = R10.divide(S).intValue();
            R = R10.mod(S);
            Mminus = Mminus.multiply(TEN);
            Mplus = Mplus.multiply(TEN);
            BigInteger R2 = R.shiftLeft(1);
            low = R2.compareTo(Mminus) < 0;
            boolean bl = high = R2.compareTo(S.shiftLeft(1).subtract(Mplus)) > 0;
            if (low || high) break;
            if (k == -1) {
                if (initial) {
                    sb.cat('0');
                }
                sb.cat('.');
            }
            sb.cat(charForDigit[U]);
            initial = false;
        }
        if (high && (!low || R.shiftLeft(1).compareTo(S) > 0)) {
            ++U;
        }
        if (k == -1) {
            if (initial) {
                sb.cat('0');
            }
            sb.cat('.');
        }
        sb.cat(charForDigit[U]);
        for (int z = 0; z < k; ++z) {
            sb.cat('0');
        }
    }

    private static void fppfppExponential(FastStringBuffer sb, int e, long f, int p) {
        boolean high;
        boolean low;
        int U;
        BigInteger Mminus;
        BigInteger R = BigInteger.valueOf(f).shiftLeft(Math.max(e - p, 0));
        BigInteger S = BigInteger.ONE.shiftLeft(Math.max(0, -(e - p)));
        BigInteger Mplus = Mminus = BigInteger.ONE.shiftLeft(Math.max(e - p, 0));
        boolean initial = true;
        boolean doneDot = false;
        if (f == 1L << p - 1) {
            Mplus = Mplus.shiftLeft(1);
            R = R.shiftLeft(1);
            S = S.shiftLeft(1);
        }
        int k = 0;
        while (R.compareTo(S.add(NINE).divide(TEN)) < 0) {
            --k;
            R = R.multiply(TEN);
            Mminus = Mminus.multiply(TEN);
            Mplus = Mplus.multiply(TEN);
        }
        while (R.shiftLeft(1).add(Mplus).compareTo(S.shiftLeft(1)) >= 0) {
            S = S.multiply(TEN);
            ++k;
        }
        int H = k - 1;
        while (true) {
            --k;
            BigInteger R10 = R.multiply(TEN);
            U = R10.divide(S).intValue();
            R = R10.mod(S);
            Mminus = Mminus.multiply(TEN);
            Mplus = Mplus.multiply(TEN);
            BigInteger R2 = R.shiftLeft(1);
            low = R2.compareTo(Mminus) < 0;
            boolean bl = high = R2.compareTo(S.shiftLeft(1).subtract(Mplus)) > 0;
            if (low || high) break;
            sb.cat(charForDigit[U]);
            if (initial) {
                sb.cat('.');
                doneDot = true;
            }
            initial = false;
        }
        if (high && (!low || R.shiftLeft(1).compareTo(S) > 0)) {
            ++U;
        }
        sb.cat(charForDigit[U]);
        if (!doneDot) {
            sb.append(".0");
        }
        sb.cat('E');
        FloatingPointConverter.appendInt(sb, H);
    }

    public static FastStringBuffer appendDouble(FastStringBuffer s, double d, boolean forceExponential) {
        if (d == Double.NEGATIVE_INFINITY) {
            s.append(NEGATIVE_INFINITY);
        } else if (d == Double.POSITIVE_INFINITY) {
            s.append(POSITIVE_INFINITY);
        } else if (d != d) {
            s.append(NaN);
        } else if (d == 0.0) {
            if ((Double.doubleToLongBits(d) & Long.MIN_VALUE) != 0L) {
                s.cat('-');
            }
            s.cat('0');
            if (forceExponential) {
                s.append(".0E0");
            }
        } else if (d == Double.MAX_VALUE) {
            s.append("1.7976931348623157E308");
        } else if (d == -1.7976931348623157E308) {
            s.append("-1.7976931348623157E308");
        } else if (d == Double.MIN_VALUE) {
            s.append("4.9E-324");
        } else if (d == -4.9E-324) {
            s.append("-4.9E-324");
        } else {
            if (d < 0.0) {
                s.cat('-');
                d = -d;
            }
            long bits = Double.doubleToLongBits(d);
            long fraction = 0x10000000000000L | bits & 0xFFFFFFFFFFFFFL;
            long rawExp = (bits & 0x7FF0000000000000L) >> 52;
            int exp = (int)rawExp - 1023;
            if (rawExp == 0L) {
                s.append(Double.toString(d));
                return s;
            }
            if (forceExponential || d >= 1000000.0 || d < 1.0E-6) {
                FloatingPointConverter.fppfppExponential(s, exp, fraction, 52);
            } else if (d <= 0.01) {
                FloatingPointConverter.fppfppBig(s, exp, fraction, 52);
            } else {
                FloatingPointConverter.fppfpp(s, exp, fraction, 52);
            }
        }
        return s;
    }

    public static FastStringBuffer appendFloat(FastStringBuffer s, float f, boolean forceExponential) {
        if (f == Float.NEGATIVE_INFINITY) {
            s.append(NEGATIVE_INFINITY);
        } else if (f == Float.POSITIVE_INFINITY) {
            s.append(POSITIVE_INFINITY);
        } else if (f != f) {
            s.append(NaN);
        } else if ((double)f == 0.0) {
            if ((Float.floatToIntBits(f) & Integer.MIN_VALUE) != 0) {
                s.cat('-');
            }
            s.cat('0');
        } else if (f == Float.MAX_VALUE) {
            s.append("3.4028235E38");
        } else if (f == -3.4028235E38f) {
            s.append("-3.4028235E38");
        } else if (f == Float.MIN_VALUE) {
            s.append("1.4E-45");
        } else if (f == -1.4E-45f) {
            s.append("-1.4E-45");
        } else {
            if (f < 0.0f) {
                s.cat('-');
                f = -f;
            }
            int bits = Float.floatToIntBits(f);
            int fraction = 0x800000 | bits & 0x7FFFFF;
            int rawExp = (bits & 0x7F800000) >> 23;
            int exp = rawExp - 127;
            int precision = 23;
            if (rawExp == 0) {
                s.append(Float.toString(f));
                return s;
            }
            if (forceExponential || f >= 1000000.0f || f < 1.0E-6f) {
                FloatingPointConverter.fppfppExponential(s, exp, fraction, precision);
            } else {
                FloatingPointConverter.fppfpp(s, exp, fraction, precision);
            }
        }
        return s;
    }
}

