/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.instruct.WithParam;

public interface ITemplateCall
extends ContextOriginator {
    public WithParam[] getActualParams();

    public WithParam[] getTunnelParams();
}

