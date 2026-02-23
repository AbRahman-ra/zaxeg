/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.wrapper.VirtualCopy;
import net.sf.saxon.tree.wrapper.VirtualTreeInfo;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.UntypedAtomicValue;

public class VirtualUntypedCopy
extends VirtualCopy {
    public static VirtualCopy makeVirtualUntypedTree(NodeInfo original, NodeInfo root) {
        VirtualTreeInfo doc;
        while (original instanceof VirtualUntypedCopy && original.getParent() == null) {
            original = ((VirtualUntypedCopy)original).original;
            root = ((VirtualUntypedCopy)root).original;
        }
        VirtualUntypedCopy vc = new VirtualUntypedCopy(original, root);
        Configuration config = original.getConfiguration();
        vc.tree = doc = new VirtualTreeInfo(config, vc);
        return vc;
    }

    protected VirtualUntypedCopy(NodeInfo base, NodeInfo root) {
        super(base, root);
    }

    @Override
    public SchemaType getSchemaType() {
        switch (this.getNodeKind()) {
            case 1: {
                return Untyped.getInstance();
            }
            case 2: {
                return BuiltInAtomicType.UNTYPED_ATOMIC;
            }
        }
        return super.getSchemaType();
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        switch (this.getNodeKind()) {
            case 1: 
            case 2: {
                return new UntypedAtomicValue(this.getStringValueCS());
            }
        }
        return super.atomize();
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        super.copy(out, copyOptions & 0xFFFFFFFB, locationId);
    }

    @Override
    protected VirtualCopy wrap(NodeInfo node) {
        VirtualUntypedCopy vc = new VirtualUntypedCopy(node, this.root);
        vc.tree = this.tree;
        return vc;
    }

    @Override
    public boolean isNilled() {
        return false;
    }
}

