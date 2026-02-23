/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.accum;

import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public interface IAccumulatorData {
    public Accumulator getAccumulator();

    public Sequence getValue(NodeInfo var1, boolean var2) throws XPathException;
}

