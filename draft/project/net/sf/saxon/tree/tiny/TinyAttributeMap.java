/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.util.Iterator;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.CodedName;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.tree.tiny.AttributeInfoIterator;
import net.sf.saxon.tree.tiny.TinyTree;

public class TinyAttributeMap
implements AttributeMap {
    private int element;
    private TinyTree tree;
    private int firstAttribute;

    public TinyAttributeMap(TinyTree tree, int element) {
        this.tree = tree;
        this.element = element;
        this.firstAttribute = tree.alpha[element];
    }

    @Override
    public int size() {
        int i;
        for (i = this.firstAttribute; i < this.tree.numberOfAttributes && this.tree.attParent[i] == this.element; ++i) {
        }
        return i - this.firstAttribute;
    }

    @Override
    public AttributeInfo get(NodeName name) {
        return null;
    }

    @Override
    public AttributeInfo get(String uri, String local) {
        return null;
    }

    @Override
    public AttributeInfo getByFingerprint(int fingerprint, NamePool namePool) {
        return null;
    }

    @Override
    public Iterator<AttributeInfo> iterator() {
        return new AttributeInfoIterator(this.tree, this.element);
    }

    @Override
    public AttributeInfo itemAt(int index) {
        int attNr = this.firstAttribute + index;
        int nc = this.tree.attCode[attNr];
        CodedName nodeName = new CodedName(nc & 0xFFFFF, this.tree.prefixPool.getPrefix(nc >> 20), this.tree.getNamePool());
        return new AttributeInfo(nodeName, this.tree.getAttributeType(attNr), this.tree.attValue[attNr].toString(), Loc.NONE, 0);
    }
}

