/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.accum;

import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.accum.IAccumulatorData;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.wrapper.VirtualCopy;

public class VirtualAccumulatorData
implements IAccumulatorData {
    private IAccumulatorData realData;

    public VirtualAccumulatorData(IAccumulatorData realData) {
        this.realData = realData;
    }

    @Override
    public Accumulator getAccumulator() {
        return this.realData.getAccumulator();
    }

    @Override
    public Sequence getValue(NodeInfo node, boolean postDescent) throws XPathException {
        NodeInfo original = ((VirtualCopy)node).getOriginalNode();
        return this.realData.getValue(original, postDescent);
    }
}

