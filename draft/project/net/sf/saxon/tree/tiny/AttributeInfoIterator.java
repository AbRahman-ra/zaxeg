/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.util.Iterator;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.CodedName;
import net.sf.saxon.tree.tiny.TinyTree;

final class AttributeInfoIterator
implements Iterator<AttributeInfo> {
    private TinyTree tree;
    private int element;
    private int index;

    AttributeInfoIterator(TinyTree tree, int element) {
        this.tree = tree;
        this.element = element;
        this.index = tree.alpha[element];
    }

    @Override
    public boolean hasNext() {
        return this.index < this.tree.numberOfAttributes && this.tree.attParent[this.index] == this.element;
    }

    @Override
    public AttributeInfo next() {
        int nc = this.tree.attCode[this.index];
        CodedName nodeName = new CodedName(nc & 0xFFFFF, this.tree.prefixPool.getPrefix(nc >> 20), this.tree.getNamePool());
        AttributeInfo info = new AttributeInfo(nodeName, this.tree.getAttributeType(this.index), this.tree.attValue[this.index].toString(), Loc.NONE, 0);
        ++this.index;
        return info;
    }
}

