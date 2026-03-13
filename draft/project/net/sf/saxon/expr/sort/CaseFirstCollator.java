/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.XPathException;

public class CaseFirstCollator
implements StringCollator {
    private StringCollator baseCollator;
    private boolean upperFirst;
    private String uri;

    public CaseFirstCollator(StringCollator base, boolean upperFirst, String collationURI) {
        this.baseCollator = base;
        this.upperFirst = upperFirst;
        this.uri = collationURI;
    }

    public static StringCollator makeCaseOrderedCollator(String uri, StringCollator stringCollator, String caseOrder) throws XPathException {
        switch (caseOrder) {
            case "lower-first": {
                stringCollator = new CaseFirstCollator(stringCollator, false, uri);
                break;
            }
            case "upper-first": {
                stringCollator = new CaseFirstCollator(stringCollator, true, uri);
                break;
            }
            default: {
                throw new XPathException("case-order must be lower-first, upper-first, or #default");
            }
        }
        return stringCollator;
    }

    @Override
    public String getCollationURI() {
        return this.uri;
    }

    @Override
    public int compareStrings(CharSequence a, CharSequence b) {
        int diff = this.baseCollator.compareStrings(a, b);
        if (diff != 0) {
            return diff;
        }
        int i = 0;
        int j = 0;
        while (true) {
            boolean bFirst;
            if (i < a.length() && j < b.length() && a.charAt(i) == b.charAt(j)) {
                ++i;
                ++j;
                continue;
            }
            while (i < a.length() && !Character.isLetter(a.charAt(i))) {
                ++i;
            }
            while (j < b.length() && !Character.isLetter(b.charAt(j))) {
                ++j;
            }
            if (i >= a.length()) {
                return 0;
            }
            if (j >= b.length()) {
                return 0;
            }
            boolean aFirst = this.upperFirst ? Character.isUpperCase(a.charAt(i++)) : Character.isLowerCase(a.charAt(i++));
            boolean bl = bFirst = this.upperFirst ? Character.isUpperCase(b.charAt(j++)) : Character.isLowerCase(b.charAt(j++));
            if (aFirst && !bFirst) {
                return -1;
            }
            if (bFirst && !aFirst) break;
        }
        return 1;
    }

    @Override
    public boolean comparesEqual(CharSequence s1, CharSequence s2) {
        return this.compareStrings(s1, s2) == 0;
    }

    @Override
    public AtomicMatchKey getCollationKey(CharSequence s) {
        return this.baseCollator.getCollationKey(s);
    }
}

