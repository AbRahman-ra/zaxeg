/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import net.sf.saxon.query.QueryModule;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.trans.XPathException;

public abstract class QueryLibrary
extends QueryModule {
    public QueryLibrary(StaticQueryContext sqc) throws XPathException {
        super(sqc);
    }

    public abstract void link(QueryModule var1) throws XPathException;
}

