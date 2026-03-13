/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.util.Arrays;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.tree.tiny.TinyTextImpl;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.z.IntHashMap;

public class TinyBuilderCondensed
extends TinyBuilder {
    public IntHashMap<int[]> textValues = new IntHashMap(100);

    public TinyBuilderCondensed(PipelineConfiguration pipe) {
        super(pipe);
    }

    @Override
    public void endElement() throws XPathException {
        boolean sameDepth;
        TinyTree tree = this.getTree();
        super.endElement();
        int last = tree.numberOfNodes - 1;
        boolean bl = sameDepth = tree.depth[last] == this.getCurrentDepth();
        if (sameDepth) {
            boolean hasFinalTextNode;
            boolean isTextualElement = tree.nodeKind[last] == 17;
            boolean bl2 = hasFinalTextNode = tree.nodeKind[last] == 3;
            if ((isTextualElement || hasFinalTextNode) && tree.beta[last] <= 256) {
                CharSequence chars = TinyTextImpl.getStringValue(tree, last);
                int hash = chars.hashCode();
                int[] nodes = this.textValues.get(hash);
                if (nodes != null) {
                    int nodeNr;
                    int used = nodes[0];
                    for (int i = 1; i < used && (nodeNr = nodes[i]) != 0; ++i) {
                        if (!TinyBuilderCondensed.isEqual(chars, TinyTextImpl.getStringValue(tree, nodeNr))) continue;
                        int length = tree.alpha[last];
                        tree.alpha[last] = tree.alpha[nodeNr];
                        tree.beta[last] = tree.beta[nodeNr];
                        tree.getCharacterBuffer().setLength(length);
                        return;
                    }
                } else {
                    nodes = new int[4];
                    nodes[0] = 1;
                    this.textValues.put(hash, nodes);
                }
                if (nodes[0] + 1 > nodes.length) {
                    int[] n2 = Arrays.copyOf(nodes, nodes.length * 2);
                    this.textValues.put(hash, n2);
                    nodes = n2;
                }
                int n = nodes[0];
                nodes[0] = n + 1;
                nodes[n] = last;
            }
        }
    }

    @Override
    protected String getAttValue(AttributeInfo att) {
        return super.getAttValue(att).intern();
    }

    private static boolean isEqual(CharSequence a, CharSequence b) {
        if (a.getClass() == b.getClass()) {
            return a.equals(b);
        }
        return a.toString().equals(b.toString());
    }
}

