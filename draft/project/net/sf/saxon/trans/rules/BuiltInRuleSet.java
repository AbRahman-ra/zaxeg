/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.rules;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.om.Item;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;

public interface BuiltInRuleSet
extends ContextOriginator {
    public static final int DEEP_COPY = 1;
    public static final int DEEP_SKIP = 3;
    public static final int FAIL = 4;
    public static final int SHALLOW_COPY = 5;
    public static final int APPLY_TEMPLATES_TO_ATTRIBUTES = 6;
    public static final int APPLY_TEMPLATES_TO_CHILDREN = 7;

    public void process(Item var1, ParameterSet var2, ParameterSet var3, Outputter var4, XPathContext var5, Location var6) throws XPathException;

    public String getName();

    public int[] getActionForParentNodes(int var1);
}

