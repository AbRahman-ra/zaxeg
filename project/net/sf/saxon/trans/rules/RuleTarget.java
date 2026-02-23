/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.rules;

import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.Rule;

public interface RuleTarget {
    public void export(ExpressionPresenter var1) throws XPathException;

    public void registerRule(Rule var1);
}

