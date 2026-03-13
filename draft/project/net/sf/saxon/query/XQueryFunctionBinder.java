/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.XQueryFunction;

public interface XQueryFunctionBinder
extends FunctionLibrary {
    public XQueryFunction getDeclaration(StructuredQName var1, int var2);
}

