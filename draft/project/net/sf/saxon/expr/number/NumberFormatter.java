/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.number;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import net.sf.saxon.expr.number.Alphanumeric;
import net.sf.saxon.expr.number.RegularGroupFormatter;
import net.sf.saxon.lib.Numberer;
import net.sf.saxon.regex.EmptyString;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.regex.charclass.Categories;
import net.sf.saxon.tree.util.FastStringBuffer;

public class NumberFormatter {
    private ArrayList<UnicodeString> formatTokens;
    private ArrayList<UnicodeString> punctuationTokens;
    private boolean startsWithPunctuation;
    private static IntPredicate alphanumeric = Categories.getCategory("N").or(Categories.getCategory("L"));

    public void prepare(String format) {
        if (format.isEmpty()) {
            format = "1";
        }
        this.formatTokens = new ArrayList(10);
        this.punctuationTokens = new ArrayList(10);
        UnicodeString uFormat = UnicodeString.makeUnicodeString(format);
        int len = uFormat.uLength();
        int i = 0;
        boolean first = true;
        this.startsWithPunctuation = true;
        while (i < len) {
            int c = uFormat.uCharAt(i);
            int t = i;
            while (NumberFormatter.isLetterOrDigit(c) && ++i != len) {
                c = uFormat.uCharAt(i);
            }
            if (i > t) {
                UnicodeString tok = uFormat.uSubstring(t, i);
                this.formatTokens.add(tok);
                if (first) {
                    this.punctuationTokens.add(UnicodeString.makeUnicodeString("."));
                    this.startsWithPunctuation = false;
                    first = false;
                }
            }
            if (i == len) break;
            t = i;
            c = uFormat.uCharAt(i);
            while (!NumberFormatter.isLetterOrDigit(c)) {
                first = false;
                if (++i == len) break;
                c = uFormat.uCharAt(i);
            }
            if (i <= t) continue;
            UnicodeString sep = uFormat.uSubstring(t, i);
            this.punctuationTokens.add(sep);
        }
        if (this.formatTokens.isEmpty()) {
            this.formatTokens.add(UnicodeString.makeUnicodeString("1"));
            if (this.punctuationTokens.size() == 1) {
                this.punctuationTokens.add(this.punctuationTokens.get(0));
            }
        }
    }

    public static boolean isLetterOrDigit(int c) {
        if (c <= 127) {
            return c >= 48 && c <= 57 || c >= 65 && c <= 90 || c >= 97 && c <= 122;
        }
        return alphanumeric.test(c);
    }

    public CharSequence format(List numbers, int groupSize, String groupSeparator, String letterValue, String ordinal, Numberer numberer) {
        FastStringBuffer sb = new FastStringBuffer(16);
        int num = 0;
        int tok = 0;
        if (this.startsWithPunctuation) {
            sb.append(this.punctuationTokens.get(tok));
        }
        while (num < numbers.size()) {
            String s;
            Object o;
            if (num > 0) {
                if (tok == 0 && this.startsWithPunctuation) {
                    sb.append(".");
                } else {
                    sb.append(this.punctuationTokens.get(tok));
                }
            }
            if ((o = numbers.get(num++)) instanceof Long) {
                long nr = (Long)o;
                RegularGroupFormatter rgf = new RegularGroupFormatter(groupSize, groupSeparator, EmptyString.THE_INSTANCE);
                s = numberer.format(nr, this.formatTokens.get(tok), rgf, letterValue, ordinal);
            } else if (o instanceof BigInteger) {
                FastStringBuffer fsb = new FastStringBuffer(64);
                fsb.append(o.toString());
                RegularGroupFormatter rgf = new RegularGroupFormatter(groupSize, groupSeparator, EmptyString.THE_INSTANCE);
                s = rgf.format(fsb);
                s = this.translateDigits(s, this.formatTokens.get(tok));
            } else {
                s = o.toString();
            }
            sb.append(s);
            if (++tok != this.formatTokens.size()) continue;
            --tok;
        }
        if (this.punctuationTokens.size() > this.formatTokens.size()) {
            sb.append(this.punctuationTokens.get(this.punctuationTokens.size() - 1));
        }
        return sb.condense();
    }

    private String translateDigits(String in, UnicodeString picture) {
        if (picture.length() == 0) {
            return in;
        }
        int formchar = picture.uCharAt(0);
        int digitValue = Alphanumeric.getDigitValue(formchar);
        if (digitValue >= 0) {
            int zero = formchar - digitValue;
            if (zero == 48) {
                return in;
            }
            int[] digits = new int[10];
            for (int z = 0; z <= 9; ++z) {
                digits[z] = zero + z;
            }
            FastStringBuffer sb = new FastStringBuffer(128);
            for (int i = 0; i < in.length(); ++i) {
                char c = in.charAt(i);
                if (c >= '0' && c <= '9') {
                    sb.appendWideChar(digits[c - 48]);
                    continue;
                }
                sb.cat(c);
            }
            return sb.toString();
        }
        return in;
    }
}

