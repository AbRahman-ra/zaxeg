/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

public class CodepointCollatingComparer
implements AtomicComparer {
    private static CodepointCollator collator = CodepointCollator.getInstance();
    private static CodepointCollatingComparer THE_INSTANCE = new CodepointCollatingComparer();

    public static CodepointCollatingComparer getInstance() {
        return THE_INSTANCE;
    }

    private CodepointCollatingComparer() {
    }

    @Override
    public StringCollator getCollator() {
        return collator;
    }

    @Override
    public AtomicComparer provideContext(XPathContext context) {
        return this;
    }

    @Override
    public int compareAtomicValues(AtomicValue a, AtomicValue b) {
        if (a == null) {
            return b == null ? 0 : -1;
        }
        if (b == null) {
            return 1;
        }
        StringValue as = (StringValue)a;
        StringValue bs = (StringValue)b;
        return CodepointCollator.compareCS(as.getStringValueCS(), bs.getStringValueCS());
    }

    @Override
    public boolean comparesEqual(AtomicValue a, AtomicValue b) {
        return ((StringValue)a).codepointEquals((StringValue)b);
    }

    @Override
    public String save() {
        return "CCC";
    }
}

