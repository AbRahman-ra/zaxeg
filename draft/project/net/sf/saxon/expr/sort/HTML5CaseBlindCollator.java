/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.lib.SubstringMatcher;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.tree.util.FastStringBuffer;

public class HTML5CaseBlindCollator
implements StringCollator,
SubstringMatcher {
    private static HTML5CaseBlindCollator theInstance = new HTML5CaseBlindCollator();

    public static HTML5CaseBlindCollator getInstance() {
        return theInstance;
    }

    @Override
    public String getCollationURI() {
        return "http://www.w3.org/2005/xpath-functions/collation/html-ascii-case-insensitive";
    }

    @Override
    public int compareStrings(CharSequence a, CharSequence b) {
        return this.compareCS(a, b);
    }

    private int compareCS(CharSequence a, CharSequence b) {
        int nextb;
        int nexta;
        int c;
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
            nexta = a.charAt(i++);
            nextb = b.charAt(j++);
            if (nexta >= 97 && nexta <= 122) {
                nexta -= 32;
            }
            if (nextb < 97 || nextb > 122) continue;
            nextb -= 32;
        } while ((c = nexta - nextb) == 0);
        return c;
    }

    @Override
    public boolean comparesEqual(CharSequence s1, CharSequence s2) {
        return this.compareCS(s1, s2) == 0;
    }

    @Override
    public boolean contains(String s1, String s2) {
        return this.normalize(s1).contains(this.normalize(s2));
    }

    @Override
    public boolean endsWith(String s1, String s2) {
        return this.normalize(s1).endsWith(this.normalize(s2));
    }

    @Override
    public boolean startsWith(String s1, String s2) {
        return this.normalize(s1).startsWith(this.normalize(s2));
    }

    @Override
    public String substringAfter(String s1, String s2) {
        int i = this.normalize(s1).indexOf(this.normalize(s2));
        if (i < 0) {
            return "";
        }
        return s1.substring(i + s2.length());
    }

    @Override
    public String substringBefore(String s1, String s2) {
        int j = this.normalize(s1).indexOf(this.normalize(s2));
        if (j < 0) {
            return "";
        }
        return s1.substring(0, j);
    }

    @Override
    public AtomicMatchKey getCollationKey(CharSequence s) {
        return UnicodeString.makeUnicodeString(this.normalize(s));
    }

    private String normalize(CharSequence cs) {
        FastStringBuffer fsb = new FastStringBuffer(cs.length());
        for (int i = 0; i < cs.length(); ++i) {
            char c = cs.charAt(i);
            if ('a' <= c && c <= 'z') {
                fsb.cat((char)(c + 65 - 97));
                continue;
            }
            fsb.cat(c);
        }
        return fsb.toString();
    }
}

