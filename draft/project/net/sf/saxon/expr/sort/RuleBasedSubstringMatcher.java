/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.text.CollationElementIterator;
import java.text.RuleBasedCollator;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.expr.sort.CollationMatchKey;
import net.sf.saxon.expr.sort.SimpleCollation;
import net.sf.saxon.lib.SubstringMatcher;
import net.sf.saxon.tree.util.FastStringBuffer;

public class RuleBasedSubstringMatcher
extends SimpleCollation
implements SubstringMatcher {
    public RuleBasedSubstringMatcher(String uri, RuleBasedCollator collator) {
        super(uri, collator);
    }

    private RuleBasedCollator getRuleBasedCollator() {
        return (RuleBasedCollator)this.getComparator();
    }

    @Override
    public boolean contains(String s1, String s2) {
        RuleBasedCollator collator = this.getRuleBasedCollator();
        CollationElementIterator iter1 = collator.getCollationElementIterator(s1);
        CollationElementIterator iter2 = collator.getCollationElementIterator(s2);
        return this.collationContains(iter1, iter2, null, false);
    }

    @Override
    public boolean endsWith(String s1, String s2) {
        RuleBasedCollator collator = this.getRuleBasedCollator();
        CollationElementIterator iter1 = collator.getCollationElementIterator(s1);
        CollationElementIterator iter2 = collator.getCollationElementIterator(s2);
        return this.collationContains(iter1, iter2, null, true);
    }

    @Override
    public boolean startsWith(String s1, String s2) {
        RuleBasedCollator collator = this.getRuleBasedCollator();
        CollationElementIterator iter1 = collator.getCollationElementIterator(s1);
        CollationElementIterator iter2 = collator.getCollationElementIterator(s2);
        return this.collationStartsWith(iter1, iter2);
    }

    @Override
    public String substringAfter(String s1, String s2) {
        int[] ia;
        CollationElementIterator iter2;
        RuleBasedCollator collator = this.getRuleBasedCollator();
        CollationElementIterator iter1 = collator.getCollationElementIterator(s1);
        boolean ba = this.collationContains(iter1, iter2 = collator.getCollationElementIterator(s2), ia = new int[2], false);
        if (ba) {
            return s1.substring(ia[1]);
        }
        return "";
    }

    @Override
    public String substringBefore(String s1, String s2) {
        int[] ib;
        CollationElementIterator iter2;
        RuleBasedCollator collator = this.getRuleBasedCollator();
        CollationElementIterator iter1 = collator.getCollationElementIterator(s1);
        boolean bb = this.collationContains(iter1, iter2 = collator.getCollationElementIterator(s2), ib = new int[2], false);
        if (bb) {
            return s1.substring(0, ib[0]);
        }
        return "";
    }

    private boolean collationStartsWith(CollationElementIterator s0, CollationElementIterator s1) {
        while (true) {
            int e0;
            int e1;
            if ((e1 = s1.next()) == 0) {
                continue;
            }
            if (e1 == -1) {
                return true;
            }
            while ((e0 = s0.next()) == 0) {
            }
            if (e0 != e1) break;
        }
        return false;
    }

    private boolean collationContains(CollationElementIterator s0, CollationElementIterator s1, int[] offsets, boolean matchAtEnd) {
        int e1;
        while ((e1 = s1.next()) == 0) {
        }
        if (e1 == -1) {
            return true;
        }
        int e0 = -1;
        while (true) {
            if (e0 != e1) {
                while ((e0 = s0.next()) == 0) {
                }
                if (e0 != -1) continue;
                return false;
            }
            int start = s0.getOffset();
            if (this.collationStartsWith(s0, s1)) {
                if (matchAtEnd) {
                    while ((e0 = s0.next()) == 0) {
                    }
                    if (e0 == -1) {
                        return true;
                    }
                } else {
                    if (offsets != null) {
                        offsets[0] = start - 1;
                        offsets[1] = s0.getOffset();
                    }
                    return true;
                }
            }
            s0.setOffset(start);
            if (s0.getOffset() != start) {
                s0.next();
            }
            s1.reset();
            e0 = -1;
            while ((e1 = s1.next()) == 0) {
            }
        }
    }

    @Override
    public AtomicMatchKey getCollationKey(CharSequence s) {
        return new CollationMatchKey(this.getRuleBasedCollator().getCollationKey(s.toString()));
    }

    public static void main(String[] args) throws Exception {
        String rules = " ='-'='*'< a < b < c < d < e < f < g < h < i < j < k < l < m < n < o < p < q < r < s < t < u < v < w < x < y < z";
        RuleBasedCollator collator = new RuleBasedCollator(rules);
        for (int i = 0; i < args.length; ++i) {
            int e;
            System.err.println(args[i]);
            FastStringBuffer sb = new FastStringBuffer(256);
            CollationElementIterator iter = collator.getCollationElementIterator(args[i]);
            while ((e = iter.next()) != -1) {
                sb.append(e + " ");
            }
            System.err.println(sb.toString());
        }
    }
}

