/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.util.EnumSet;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.AtomizedValueIterator;
import net.sf.saxon.om.CodedName;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.type.SimpleType;

final class AttributeIterator
implements AxisIterator,
AtomizedValueIterator {
    private final TinyTree tree;
    private final int element;
    private final NodeTest nodeTest;
    private int index;
    private int currentNodeNr;

    AttributeIterator(TinyTree tree, int element, NodeTest nodeTest) {
        this.nodeTest = nodeTest;
        this.tree = tree;
        this.element = element;
        this.index = tree.alpha[element];
        this.currentNodeNr = -1;
    }

    private boolean moveToNext() {
        while (true) {
            if (this.index >= this.tree.numberOfAttributes || this.tree.attParent[this.index] != this.element) {
                this.index = Integer.MAX_VALUE;
                this.currentNodeNr = -1;
                return false;
            }
            SimpleType typeCode = this.tree.getAttributeType(this.index);
            if (this.nodeTest.matches(2, new CodedName(this.tree.attCode[this.index] & 0xFFFFF, "", this.tree.getNamePool()), typeCode)) {
                this.currentNodeNr = this.index++;
                if (this.nodeTest instanceof NameTest) {
                    this.index = Integer.MAX_VALUE;
                }
                return true;
            }
            ++this.index;
        }
    }

    @Override
    public NodeInfo next() {
        if (this.moveToNext()) {
            return this.tree.getAttributeNode(this.currentNodeNr);
        }
        return null;
    }

    @Override
    public AtomicSequence nextAtomizedValue() throws XPathException {
        if (this.moveToNext()) {
            return this.tree.getTypedValueOfAttribute(null, this.currentNodeNr);
        }
        return null;
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.ATOMIZING);
    }
}

