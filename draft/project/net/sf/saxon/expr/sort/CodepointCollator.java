/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.lib.SubstringMatcher;
import net.sf.saxon.regex.UnicodeString;

public class CodepointCollator
implements StringCollator,
SubstringMatcher {
    private static CodepointCollator theInstance = new CodepointCollator();

    public static CodepointCollator getInstance() {
        return theInstance;
    }

    @Override
    public String getCollationURI() {
        return "http://www.w3.org/2005/xpath-functions/collation/codepoint";
    }

    @Override
    public int compareStrings(CharSequence a, CharSequence b) {
        return CodepointCollator.compareCS(a, b);
    }

    public static int compareCS(CharSequence a, CharSequence b) {
        int nextb;
        int nexta;
        int c;
        if (a instanceof UnicodeString && b instanceof UnicodeString) {
            return ((UnicodeString)a).compareTo((UnicodeString)b);
        }
        int alen = a.length();
        int blen = b.length();
        int i = 0;
        int j = 0;
        do {
            if (i == alen) {
                if (j == blen) {
                    return 0;
                }
                return -1;
            }
            if (j == blen) {
                return 1;
            }
            if ((nexta = a.charAt(i++)) >= 55296 && nexta <= 56319) {
                nexta = (nexta - 55296) * 1024 + (a.charAt(i++) - 56320) + 65536;
            }
            if ((nextb = b.charAt(j++)) < 55296 || nextb > 56319) continue;
            nextb = (nextb - 55296) * 1024 + (b.charAt(j++) - 56320) + 65536;
        } while ((c = nexta - nextb) == 0);
        return c;
    }

    @Override
    public boolean comparesEqual(CharSequence s1, CharSequence s2) {
        if (s1 instanceof String) {
            return ((String)s1).contentEquals(s2);
        }
        if (s1 instanceof UnicodeString) {
            return s1.equals(UnicodeString.makeUnicodeString(s2));
        }
        return s1.length() == s2.length() && s1.toString().equals(s2.toString());
    }

    @Override
    public boolean contains(String s1, String s2) {
        return s1.contains(s2);
    }

    @Override
    public boolean endsWith(String s1, String s2) {
        return s1.endsWith(s2);
    }

    @Override
    public boolean startsWith(String s1, String s2) {
        return s1.startsWith(s2);
    }

    @Override
    public String substringAfter(String s1, String s2) {
        int i = s1.indexOf(s2);
        if (i < 0) {
            return "";
        }
        return s1.substring(i + s2.length());
    }

    @Override
    public String substringBefore(String s1, String s2) {
        int j = s1.indexOf(s2);
        if (j < 0) {
            return "";
        }
        return s1.substring(0, j);
    }

    @Override
    public AtomicMatchKey getCollationKey(CharSequence s) {
        return UnicodeString.makeUnicodeString(s);
    }
}

