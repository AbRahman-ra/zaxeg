/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.VariableReference;

public final class Cardinality {
    private Cardinality() {
    }

    public static boolean allowsMany(int cardinality) {
        return (cardinality & 0x8000) != 0;
    }

    public static boolean expectsMany(Expression expression) {
        Binding b;
        if (expression instanceof VariableReference && (b = ((VariableReference)expression).getBinding()) instanceof LetExpression) {
            return Cardinality.expectsMany(((LetExpression)b).getSequence());
        }
        if (expression instanceof Atomizer) {
            return Cardinality.expectsMany(((Atomizer)expression).getBaseExpression());
        }
        if (expression instanceof FilterExpression) {
            return Cardinality.expectsMany(((FilterExpression)expression).getSelectExpression());
        }
        return Cardinality.allowsMany(expression.getCardinality());
    }

    public static boolean allowsZero(int cardinality) {
        return (cardinality & 0x2000) != 0;
    }

    public static int union(int c1, int c2) {
        int r = c1 | c2;
        if (r == 40960) {
            r = 57344;
        }
        return r;
    }

    public static int sum(int c1, int c2) {
        int min = Cardinality.min(c1) + Cardinality.min(c2);
        int max = Cardinality.max(c1) + Cardinality.max(c2);
        return Cardinality.fromMinAndMax(min, max);
    }

    static int min(int cardinality) {
        if (Cardinality.allowsZero(cardinality)) {
            return 0;
        }
        if (cardinality == 32768) {
            return 2;
        }
        return 1;
    }

    static int max(int cardinality) {
        if (Cardinality.allowsMany(cardinality)) {
            return 2;
        }
        if (cardinality == 8192) {
            return 0;
        }
        return 1;
    }

    static int fromMinAndMax(int min, int max) {
        boolean zero = min == 0;
        boolean one = min <= 1 || max <= 1;
        boolean many = max > 1;
        return (zero ? 8192 : 0) + (one ? 16384 : 0) + (many ? 32768 : 0);
    }

    public static boolean subsumes(int c1, int c2) {
        return (c1 | c2) == c1;
    }

    public static int multiply(int c1, int c2) {
        if (c1 == 8192 || c2 == 8192) {
            return 8192;
        }
        if (c2 == 16384) {
            return c1;
        }
        if (c1 == 16384) {
            return c2;
        }
        if (c1 == 24576 && c2 == 24576) {
            return 24576;
        }
        if (c1 == 49152 && c2 == 49152) {
            return 49152;
        }
        return 57344;
    }

    public static String toString(int cardinality) {
        switch (cardinality) {
            case 24576: {
                return "zero or one";
            }
            case 16384: {
                return "exactly one";
            }
            case 57344: {
                return "zero or more";
            }
            case 49152: {
                return "one or more";
            }
            case 8192: {
                return "exactly zero";
            }
            case 32768: {
                return "more than one";
            }
        }
        return "code " + cardinality;
    }

    public static String getOccurrenceIndicator(int cardinality) {
        switch (cardinality) {
            case 24576: {
                return "?";
            }
            case 16384: {
                return "";
            }
            case 57344: {
                return "*";
            }
            case 49152: {
                return "+";
            }
            case 32768: {
                return "+";
            }
            case 8192: {
                return "0";
            }
        }
        return "*";
    }

    public static int fromOccurrenceIndicator(String indicator) {
        switch (indicator) {
            case "?": {
                return 24576;
            }
            case "*": {
                return 57344;
            }
            case "+": {
                return 49152;
            }
            case "1": {
                return 16384;
            }
            case "": {
                return 16384;
            }
        }
        return 8192;
    }

    public static String generateJavaScriptChecker(int card) {
        if (Cardinality.allowsZero(card) && Cardinality.allowsMany(card)) {
            return "function c() {return true;};";
        }
        if (card == 16384) {
            return "function c(n) {return n==1;};";
        }
        if (card == 8192) {
            return "function c(n) {return n==0;};";
        }
        if (!Cardinality.allowsZero(card)) {
            return "function c(n) {return n>=1;};";
        }
        return "function c(n) {return n<=1;};";
    }
}

