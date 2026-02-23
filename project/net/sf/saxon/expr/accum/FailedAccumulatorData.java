/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.accum;

import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.accum.IAccumulatorData;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public class FailedAccumulatorData
implements IAccumulatorData {
    private Accumulator acc;
    private XPathException error;

    public FailedAccumulatorData(Accumulator acc, XPathException error) {
        this.acc = acc;
        this.error = error;
    }

    @Override
    public Accumulator getAccumulator() {
        return this.acc;
    }

    @Override
    public Sequence getValue(NodeInfo node, boolean postDescent) throws XPathException {
        throw this.error;
    }
}

