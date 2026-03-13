/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;

public final class SimplePositionalPattern
extends Pattern {
    private NodeTest nodeTest;
    private int position;

    public SimplePositionalPattern(NodeTest nodeTest, int position) {
        this.nodeTest = nodeTest;
        this.position = position;
    }

    public int getPosition() {
        return this.position;
    }

    public NodeTest getNodeTest() {
        return this.nodeTest;
    }

    @Override
    public boolean matches(Item item, XPathContext context) {
        return item instanceof NodeInfo && this.matchesBeneathAnchor((NodeInfo)item, null, context);
    }

    @Override
    public UType getUType() {
        return this.nodeTest.getUType();
    }

    @Override
    public int getFingerprint() {
        return this.nodeTest.getFingerprint();
    }

    @Override
    public ItemType getItemType() {
        return this.nodeTest.getPrimitiveItemType();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SimplePositionalPattern) {
            SimplePositionalPattern fp = (SimplePositionalPattern)other;
            return this.nodeTest.equals(fp.nodeTest) && this.position == fp.position;
        }
        return false;
    }

    @Override
    public int computeHashCode() {
        return this.nodeTest.hashCode() ^ this.position << 3;
    }

    @Override
    public boolean isMotionless() {
        return false;
    }

    @Override
    public boolean matchesBeneathAnchor(NodeInfo node, NodeInfo anchor, XPathContext context) {
        if (!this.nodeTest.test(node)) {
            return false;
        }
        if (anchor != null && node.getParent() != anchor) {
            return false;
        }
        return this.position == Navigator.getSiblingPosition(node, this.nodeTest, this.position);
    }

    @Override
    public Pattern copy(RebindingMap rebindings) {
        SimplePositionalPattern n = new SimplePositionalPattern(this.nodeTest.copy(), this.position);
        ExpressionTool.copyLocationInfo(this, n);
        n.setOriginalText(this.getOriginalText());
        return n;
    }

    @Override
    public String reconstruct() {
        return this.nodeTest + "[" + this.position + "]";
    }

    @Override
    public void export(ExpressionPresenter presenter) {
        presenter.startElement("p.simPos");
        presenter.emitAttribute("test", AlphaCode.fromItemType(this.nodeTest));
        presenter.emitAttribute("pos", this.position + "");
        presenter.endElement();
    }
}

