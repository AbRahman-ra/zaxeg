/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.List;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.om.Function;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;

public interface FunctionLibrary {
    default public void setConfiguration(Configuration config) {
    }

    public boolean isAvailable(SymbolicName.F var1);

    public Expression bind(SymbolicName.F var1, Expression[] var2, StaticContext var3, List<String> var4);

    public FunctionLibrary copy();

    public Function getFunctionItem(SymbolicName.F var1, StaticContext var2) throws XPathException;
}

