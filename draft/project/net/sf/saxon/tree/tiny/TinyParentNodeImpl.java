/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.tiny.TinyTextImpl;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.tree.tiny.WhitespaceTextImpl;
import net.sf.saxon.tree.util.FastStringBuffer;

public abstract class TinyParentNodeImpl
extends TinyNodeImpl {
    @Override
    public boolean hasChildNodes() {
        return this.nodeNr + 1 < this.tree.numberOfNodes && this.tree.depth[this.nodeNr + 1] > this.tree.depth[this.nodeNr];
    }

    @Override
    public String getStringValue() {
        return TinyParentNodeImpl.getStringValueCS(this.tree, this.nodeNr).toString();
    }

    @Override
    public CharSequence getStringValueCS() {
        return TinyParentNodeImpl.getStringValueCS(this.tree, this.nodeNr);
    }

    public static CharSequence getStringValueCS(TinyTree tree, int nodeNr) {
        short level = tree.depth[nodeNr];
        int next = nodeNr + 1;
        if (tree.nodeKind[nodeNr] == 17) {
            return TinyTextImpl.getStringValue(tree, nodeNr);
        }
        if (next < tree.numberOfNodes) {
            if (tree.depth[next] <= level) {
                return "";
            }
            if (tree.nodeKind[next] == 3 && (next + 1 >= tree.numberOfNodes || tree.depth[next + 1] <= level)) {
                return TinyTextImpl.getStringValue(tree, next);
            }
        }
        FastStringBuffer sb = null;
        while (next < tree.numberOfNodes && tree.depth[next] > level) {
            byte kind = tree.nodeKind[next];
            if (kind == 3 || kind == 17) {
                if (sb == null) {
                    sb = new FastStringBuffer(256);
                }
                sb.cat(TinyTextImpl.getStringValue(tree, next));
            } else if (kind == 4) {
                if (sb == null) {
                    sb = new FastStringBuffer(256);
                }
                WhitespaceTextImpl.appendStringValue(tree, next, sb);
            }
            ++next;
        }
        if (sb == null) {
            return "";
        }
        return sb.condense();
    }
}

