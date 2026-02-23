/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.PrependSequenceIterator;

public class PrependAxisIterator
extends PrependSequenceIterator
implements AxisIterator {
    public PrependAxisIterator(NodeInfo start, AxisIterator base) {
        super(start, base);
    }

    @Override
    public NodeInfo next() {
        try {
            return (NodeInfo)super.next();
        } catch (XPathException e) {
            throw new AssertionError();
        }
    }
}

