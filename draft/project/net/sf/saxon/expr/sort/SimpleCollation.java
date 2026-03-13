/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.text.RuleBasedCollator;
import java.util.Comparator;
import net.sf.saxon.Platform;
import net.sf.saxon.Version;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.expr.sort.RuleBasedSubstringMatcher;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.lib.SubstringMatcher;

public class SimpleCollation
implements StringCollator {
    private Comparator comparator;
    private String uri;
    private static Platform platform = Version.platform;

    public SimpleCollation(String uri, Comparator comparator) {
        this.uri = uri;
        this.comparator = comparator;
    }

    @Override
    public String getCollationURI() {
        return this.uri;
    }

    @Override
    public int compareStrings(CharSequence o1, CharSequence o2) {
        return this.comparator.compare(o1, o2);
    }

    @Override
    public boolean comparesEqual(CharSequence s1, CharSequence s2) {
        return this.comparator.compare(s1, s2) == 0;
    }

    public Comparator getComparator() {
        return this.comparator;
    }

    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }

    @Override
    public AtomicMatchKey getCollationKey(CharSequence s) {
        return platform.getCollationKey(this, s.toString());
    }

    public SubstringMatcher getSubstringMatcher() {
        if (this.comparator instanceof SubstringMatcher) {
            return (SubstringMatcher)((Object)this.comparator);
        }
        if (this.comparator instanceof RuleBasedCollator) {
            return new RuleBasedSubstringMatcher(this.uri, (RuleBasedCollator)this.comparator);
        }
        return null;
    }
}

