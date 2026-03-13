/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.accum;

import java.util.Stack;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.accum.IAccumulatorData;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.util.Navigator;

public class PathMappedAccumulatorData
implements IAccumulatorData {
    private IAccumulatorData originalData;
    private NodeInfo origin;

    PathMappedAccumulatorData(IAccumulatorData original, NodeInfo origin) {
        this.originalData = original;
        this.origin = origin;
    }

    @Override
    public Accumulator getAccumulator() {
        return null;
    }

    @Override
    public Sequence getValue(NodeInfo node, boolean postDescent) throws XPathException {
        return this.originalData.getValue(this.map(node), postDescent);
    }

    private NodeInfo map(NodeInfo node) {
        if (this.origin instanceof TinyNodeImpl && node instanceof TinyNodeImpl) {
            int nodeNrInSubtree = ((TinyNodeImpl)node).getNodeNumber();
            return ((TinyNodeImpl)this.origin).getTree().getNode(nodeNrInSubtree + ((TinyNodeImpl)this.origin).getNodeNumber());
        }
        Stack<Integer> path = new Stack<Integer>();
        for (NodeInfo ancestor = node; ancestor != null; ancestor = ancestor.getParent()) {
            path.push(Navigator.getSiblingPosition(ancestor, AnyNodeTest.getInstance(), Integer.MAX_VALUE));
        }
        NodeInfo target = this.origin;
        while (!path.isEmpty()) {
            int pos = (Integer)path.pop();
            AxisIterator kids = target.iterateAxis(3);
            while (pos-- > 0) {
                target = kids.next();
                assert (target != null);
            }
        }
        return target;
    }
}

