/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api.streams;

import java.util.stream.Stream;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.streams.Step;

class AxisStep
extends Step<XdmNode> {
    private Axis axis;

    public AxisStep(Axis axis) {
        this.axis = axis;
    }

    @Override
    public Stream<? extends XdmNode> apply(XdmItem node) {
        return node instanceof XdmNode ? ((XdmNode)node).axisIterator(this.axis).stream() : Stream.empty();
    }
}

