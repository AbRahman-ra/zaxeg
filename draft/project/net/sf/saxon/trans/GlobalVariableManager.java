/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.trans.XPathException;

public interface GlobalVariableManager {
    public GlobalVariable getEquivalentVariable(Expression var1);

    public void addGlobalVariable(GlobalVariable var1) throws XPathException;
}

