/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.util.Arrays;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.z.IntHashMap;

public class DecimalSymbols {
    public static final int DECIMAL_SEPARATOR = 0;
    public static final int GROUPING_SEPARATOR = 1;
    public static final int DIGIT = 2;
    public static final int MINUS_SIGN = 3;
    public static final int PERCENT = 4;
    public static final int PER_MILLE = 5;
    public static final int ZERO_DIGIT = 6;
    public static final int EXPONENT_SEPARATOR = 7;
    public static final int PATTERN_SEPARATOR = 8;
    public static final int INFINITY = 9;
    public static final int NAN = 10;
    private static final int ERR_NOT_SINGLE_CHAR = 0;
    private static final int ERR_NOT_UNICODE_DIGIT = 1;
    private static final int ERR_SAME_CHAR_IN_TWO_ROLES = 2;
    private static final int ERR_TWO_VALUES_FOR_SAME_PROPERTY = 3;
    private static String[] XSLT_CODES = new String[]{"XTSE0020", "XTSE1295", "XTSE1300", "XTSE1290"};
    private static String[] XQUERY_CODES = new String[]{"XQST0097", "XQST0097", "XQST0098", "XQST0114"};
    private String[] errorCodes = XSLT_CODES;
    private String infinityValue;
    private String NaNValue;
    public static final String[] propertyNames = new String[]{"decimal-separator", "grouping-separator", "digit", "minus-sign", "percent", "per-mille", "zero-digit", "exponent-separator", "pattern-separator", "infinity", "NaN"};
    private int[] intValues = new int[propertyNames.length - 2];
    private int[] precedences = new int[propertyNames.length];
    private boolean[] inconsistent = new boolean[propertyNames.length];
    static int[] zeroDigits = new int[]{48, 1632, 1776, 2406, 2534, 2662, 2790, 2918, 3046, 3174, 3302, 3430, 3664, 3792, 3872, 4160, 6112, 6160, 6470, 6608, 65296, 66720, 120782, 120792, 120802, 120812, 120822};

    public DecimalSymbols(HostLanguage language, int languageLevel) {
        this.intValues[0] = 46;
        this.intValues[1] = 44;
        this.intValues[2] = 35;
        this.intValues[3] = 45;
        this.intValues[4] = 37;
        this.intValues[5] = 8240;
        this.intValues[6] = 48;
        this.intValues[7] = 101;
        this.intValues[8] = 59;
        this.infinityValue = "Infinity";
        this.NaNValue = "NaN";
        Arrays.fill(this.precedences, Integer.MIN_VALUE);
        this.setHostLanguage(language, languageLevel);
    }

    public void setHostLanguage(HostLanguage language, int languageLevel) {
        this.errorCodes = language == HostLanguage.XQUERY ? XQUERY_CODES : XSLT_CODES;
    }

    public int getDecimalSeparator() {
        return this.intValues[0];
    }

    public int getGroupingSeparator() {
        return this.intValues[1];
    }

    public int getDigit() {
        return this.intValues[2];
    }

    public int getMinusSign() {
        return this.intValues[3];
    }

    public int getPercent() {
        return this.intValues[4];
    }

    public int getPerMille() {
        return this.intValues[5];
    }

    public int getZeroDigit() {
        return this.intValues[6];
    }

    public int getExponentSeparator() {
        return this.intValues[7];
    }

    public int getPatternSeparator() {
        return this.intValues[8];
    }

    public String getInfinity() {
        return this.infinityValue;
    }

    public String getNaN() {
        return this.NaNValue;
    }

    public void setDecimalSeparator(String value) throws XPathException {
        this.setProperty(0, value, 0);
    }

    public void setGroupingSeparator(String value) throws XPathException {
        this.setProperty(1, value, 0);
    }

    public void setDigit(String value) throws XPathException {
        this.setProperty(2, value, 0);
    }

    public void setMinusSign(String value) throws XPathException {
        this.setProperty(3, value, 0);
    }

    public void setPercent(String value) throws XPathException {
        this.setProperty(4, value, 0);
    }

    public void setPerMille(String value) throws XPathException {
        this.setProperty(5, value, 0);
    }

    public void setZeroDigit(String value) throws XPathException {
        this.setProperty(6, value, 0);
    }

    public void setExponentSeparator(String value) throws XPathException {
        this.setProperty(7, value, 0);
    }

    public void setPatternSeparator(String value) throws XPathException {
        this.setProperty(8, value, 0);
    }

    public void setInfinity(String value) throws XPathException {
        this.setProperty(9, value, 0);
    }

    public void setNaN(String value) throws XPathException {
        this.setProperty(10, value, 0);
    }

    public void setProperty(int key, String value, int precedence) throws XPathException {
        String name = propertyNames[key];
        if (key <= 8) {
            int intValue = this.singleChar(name, value);
            if (precedence > this.precedences[key]) {
                this.intValues[key] = intValue;
                this.precedences[key] = precedence;
                this.inconsistent[key] = false;
            } else if (precedence == this.precedences[key] && intValue != this.intValues[key]) {
                this.inconsistent[key] = true;
            }
            if (key == 6 && !DecimalSymbols.isValidZeroDigit(intValue)) {
                throw new XPathException("The value of the zero-digit attribute must be a Unicode digit with value zero", this.errorCodes[1]);
            }
        } else if (key == 9) {
            if (precedence > this.precedences[key]) {
                this.infinityValue = value;
                this.precedences[key] = precedence;
                this.inconsistent[key] = false;
            } else if (precedence == this.precedences[key] && !this.infinityValue.equals(value)) {
                this.inconsistent[key] = true;
            }
        } else if (key == 10) {
            if (precedence > this.precedences[key]) {
                this.NaNValue = value;
                this.precedences[key] = precedence;
                this.inconsistent[key] = false;
            } else if (precedence == this.precedences[key] && !this.NaNValue.equals(value)) {
                this.inconsistent[key] = false;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void setIntProperty(String name, int value) {
        for (int i = 0; i < propertyNames.length; ++i) {
            if (!propertyNames[i].equals(name)) continue;
            this.intValues[i] = value;
        }
    }

    public void export(StructuredQName name, ExpressionPresenter out) {
        DecimalSymbols defaultSymbols = new DecimalSymbols(HostLanguage.XSLT, 31);
        out.startElement("decimalFormat");
        if (name != null) {
            out.emitAttribute("name", name);
        }
        for (int i = 0; i < this.intValues.length; ++i) {
            int propValue = this.intValues[i];
            if (propValue == defaultSymbols.intValues[i]) continue;
            out.emitAttribute(propertyNames[i], propValue + "");
        }
        if (!"Infinity".equals(this.getInfinity())) {
            out.emitAttribute("infinity", this.getInfinity());
        }
        if (!"NaN".equals(this.getNaN())) {
            out.emitAttribute("NaN", this.getNaN());
        }
        out.endElement();
    }

    private int singleChar(String name, String value) throws XPathException {
        UnicodeString us = UnicodeString.makeUnicodeString(value);
        if (us.uLength() != 1) {
            XPathException err = new XPathException("Attribute " + name + " should be a single character", this.errorCodes[0]);
            err.setIsStaticError(true);
            throw err;
        }
        return us.uCharAt(0);
    }

    public void checkConsistency(StructuredQName name) throws XPathException {
        int zero;
        for (int i = 0; i < 10; ++i) {
            if (!this.inconsistent[i]) continue;
            XPathException err = new XPathException("Inconsistency in " + (name == null ? "unnamed decimal format. " : "decimal format " + name.getDisplayName() + ". ") + "There are two inconsistent values for decimal-format property " + propertyNames[i] + " at the same import precedence");
            err.setErrorCode(this.errorCodes[3]);
            err.setIsStaticError(true);
            throw err;
        }
        IntHashMap<String> map = new IntHashMap<String>(20);
        map.put(this.getDecimalSeparator(), "decimal-separator");
        if (map.get(this.getGroupingSeparator()) != null) {
            this.duplicate("grouping-separator", (String)map.get(this.getGroupingSeparator()), name);
        }
        map.put(this.getGroupingSeparator(), "grouping-separator");
        if (map.get(this.getPercent()) != null) {
            this.duplicate("percent", (String)map.get(this.getPercent()), name);
        }
        map.put(this.getPercent(), "percent");
        if (map.get(this.getPerMille()) != null) {
            this.duplicate("per-mille", (String)map.get(this.getPerMille()), name);
        }
        map.put(this.getPerMille(), "per-mille");
        if (map.get(this.getDigit()) != null) {
            this.duplicate("digit", (String)map.get(this.getDigit()), name);
        }
        map.put(this.getDigit(), "digit");
        if (map.get(this.getPatternSeparator()) != null) {
            this.duplicate("pattern-separator", (String)map.get(this.getPatternSeparator()), name);
        }
        map.put(this.getPatternSeparator(), "pattern-separator");
        if (map.get(this.getExponentSeparator()) != null) {
            this.duplicate("exponent-separator", (String)map.get(this.getExponentSeparator()), name);
        }
        map.put(this.getExponentSeparator(), "exponent-separator");
        for (int i = zero = this.getZeroDigit(); i < zero + 10; ++i) {
            if (map.get(i) == null) continue;
            XPathException err = new XPathException("Inconsistent properties in " + (name == null ? "unnamed decimal format. " : "decimal format " + name.getDisplayName() + ". ") + "The same character is used as digit " + (i - zero) + " in the chosen digit family, and as the " + (String)map.get(i));
            err.setErrorCode(this.errorCodes[2]);
            throw err;
        }
    }

    private void duplicate(String role1, String role2, StructuredQName name) throws XPathException {
        XPathException err = new XPathException("Inconsistent properties in " + (name == null ? "unnamed decimal format. " : "decimal format " + name.getDisplayName() + ". ") + "The same character is used as the " + role1 + " and as the " + role2);
        err.setErrorCode(this.errorCodes[2]);
        throw err;
    }

    public static boolean isValidZeroDigit(int zeroDigit) {
        return Arrays.binarySearch(zeroDigits, zeroDigit) >= 0;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof DecimalSymbols)) {
            return false;
        }
        DecimalSymbols o = (DecimalSymbols)obj;
        return this.getDecimalSeparator() == o.getDecimalSeparator() && this.getGroupingSeparator() == o.getGroupingSeparator() && this.getDigit() == o.getDigit() && this.getMinusSign() == o.getMinusSign() && this.getPercent() == o.getPercent() && this.getPerMille() == o.getPerMille() && this.getZeroDigit() == o.getZeroDigit() && this.getPatternSeparator() == o.getPatternSeparator() && this.getInfinity().equals(o.getInfinity()) && this.getNaN().equals(o.getNaN());
    }

    public int hashCode() {
        return this.getDecimalSeparator() + 37 * this.getGroupingSeparator() + 41 * this.getDigit();
    }
}

