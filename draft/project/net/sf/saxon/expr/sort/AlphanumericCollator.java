/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.tree.util.FastStringBuffer;

public class AlphanumericCollator
implements StringCollator {
    private StringCollator baseCollator;
    private static Pattern pattern = Pattern.compile("\\d+");
    public static final String PREFIX = "http://saxon.sf.net/collation/alphaNumeric?base=";

    public AlphanumericCollator(StringCollator base) {
        this.baseCollator = base;
    }

    @Override
    public String getCollationURI() {
        return PREFIX + this.baseCollator.getCollationURI();
    }

    @Override
    public int compareStrings(CharSequence cs1, CharSequence cs2) {
        String s1 = cs1.toString();
        String s2 = cs2.toString();
        int pos1 = 0;
        int pos2 = 0;
        Matcher m1 = pattern.matcher(s1);
        Matcher m2 = pattern.matcher(s2);
        while (true) {
            BigInteger n2;
            boolean b1 = m1.find(pos1);
            boolean b2 = m2.find(pos2);
            int m1start = b1 ? m1.start() : s1.length();
            int m2start = b2 ? m2.start() : s2.length();
            int c = this.baseCollator.compareStrings(s1.substring(pos1, m1start), s2.substring(pos2, m2start));
            if (c != 0) {
                return c;
            }
            if (b1 && !b2) {
                return 1;
            }
            if (b2 && !b1) {
                return -1;
            }
            if (!b1) {
                return 0;
            }
            BigInteger n1 = new BigInteger(s1.substring(m1start, m1.end()));
            c = n1.compareTo(n2 = new BigInteger(s2.substring(m2start, m2.end())));
            if (c != 0) {
                return c;
            }
            pos1 = m1.end();
            pos2 = m2.end();
        }
    }

    @Override
    public boolean comparesEqual(CharSequence s1, CharSequence s2) {
        return this.compareStrings(s1, s2) == 0;
    }

    @Override
    public AtomicMatchKey getCollationKey(CharSequence cs) {
        String s = cs.toString();
        FastStringBuffer sb = new FastStringBuffer(s.length() * 2);
        int pos1 = 0;
        Matcher m1 = pattern.matcher(s);
        while (true) {
            boolean b1;
            int m1start = (b1 = m1.find(pos1)) ? m1.start() : s.length();
            sb.append(this.baseCollator.getCollationKey(s.substring(pos1, m1start)).toString());
            if (!b1) {
                return UnicodeString.makeUnicodeString(sb);
            }
            int n1 = Integer.parseInt(s.substring(m1start, m1.end()));
            sb.append(n1 + "");
            pos1 = m1.end();
        }
    }
}

