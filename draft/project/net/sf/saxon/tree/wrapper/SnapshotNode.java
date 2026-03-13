/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import java.util.function.Predicate;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.tree.wrapper.VirtualCopy;
import net.sf.saxon.tree.wrapper.VirtualTreeInfo;
import net.sf.saxon.value.UntypedAtomicValue;

public class SnapshotNode
extends VirtualCopy
implements NodeInfo {
    protected NodeInfo pivot;

    protected SnapshotNode(NodeInfo base, NodeInfo pivot) {
        super(base, pivot.getRoot());
        this.pivot = pivot;
    }

    public static SnapshotNode makeSnapshot(NodeInfo original) {
        SnapshotNode vc = new SnapshotNode(original, original);
        Configuration config = original.getConfiguration();
        VirtualTreeInfo doc = new VirtualTreeInfo(config);
        long docNr = config.getDocumentNumberAllocator().allocateDocumentNumber();
        doc.setDocumentNumber(docNr);
        doc.setCopyAccumulators(true);
        vc.tree = doc;
        doc.setRootNode(vc.getRoot());
        return vc;
    }

    @Override
    protected SnapshotNode wrap(NodeInfo node) {
        SnapshotNode vc = new SnapshotNode(node, this.pivot);
        vc.tree = this.tree;
        return vc;
    }

    @Override
    public CharSequence getStringValueCS() {
        if (Navigator.isAncestorOrSelf(this.original, this.pivot)) {
            return this.pivot.getStringValueCS();
        }
        return this.original.getStringValueCS();
    }

    @Override
    public NodeInfo getParent() {
        if (this.parent == null) {
            NodeInfo basep = this.original.getParent();
            if (basep == null) {
                return null;
            }
            this.parent = this.wrap(basep);
        }
        return this.parent;
    }

    @Override
    public NodeInfo getRoot() {
        return super.getRoot();
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        Navigator.copy((NodeInfo)this, out, copyOptions, locationId);
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        switch (this.getNodeKind()) {
            case 2: 
            case 3: 
            case 7: 
            case 8: 
            case 13: {
                return this.original.atomize();
            }
        }
        if (Navigator.isAncestorOrSelf(this.pivot, this.original)) {
            return this.original.atomize();
        }
        return new UntypedAtomicValue(this.pivot.getStringValueCS());
    }

    @Override
    public boolean isId() {
        return this.original.isId();
    }

    @Override
    public boolean isIdref() {
        return this.original.isIdref();
    }

    @Override
    public boolean isNilled() {
        return this.original.isNilled();
    }

    @Override
    public String getPublicId() {
        return this.original != null ? this.original.getPublicId() : null;
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber, Predicate<? super NodeInfo> nodeTest) {
        switch (this.getNodeKind()) {
            case 2: 
            case 3: 
            case 7: 
            case 8: 
            case 13: {
                return super.iterateAxis(axisNumber, nodeTest);
            }
        }
        if (!this.original.isSameNodeInfo(this.pivot) && Navigator.isAncestorOrSelf(this.original, this.pivot)) {
            switch (axisNumber) {
                case 3: {
                    return Navigator.filteredSingleton(this.getChildOfAncestorNode(), nodeTest);
                }
                case 4: 
                case 5: {
                    AxisIterator iter = new Navigator.DescendantEnumeration(this, axisNumber == 5, true);
                    if (!(nodeTest instanceof AnyNodeTest)) {
                        iter = new Navigator.AxisFilter(iter, nodeTest);
                    }
                    return iter;
                }
            }
            return super.iterateAxis(axisNumber, nodeTest);
        }
        return super.iterateAxis(axisNumber, nodeTest);
    }

    private NodeInfo getChildOfAncestorNode() {
        int pivotKind = this.pivot.getNodeKind();
        SnapshotNode p = this.wrap(this.pivot);
        if ((pivotKind == 2 || pivotKind == 13) && p.getParent().isSameNodeInfo(this)) {
            return null;
        }
        while (true) {
            SnapshotNode q;
            if ((q = (SnapshotNode)p.getParent()) == null) {
                throw new AssertionError();
            }
            if (q.isSameNodeInfo(this)) {
                return p;
            }
            p = q;
        }
    }

    @Override
    protected boolean isIncludedInCopy(NodeInfo sourceNode) {
        switch (sourceNode.getNodeKind()) {
            case 2: 
            case 13: {
                return this.isIncludedInCopy(sourceNode.getParent());
            }
        }
        return Navigator.isAncestorOrSelf(this.pivot, sourceNode) || Navigator.isAncestorOrSelf(sourceNode, this.pivot);
    }
}

