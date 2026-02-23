/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;

public class AnchorPattern
extends Pattern {
    private static AnchorPattern THE_INSTANCE = new AnchorPattern();

    public static AnchorPattern getInstance() {
        return THE_INSTANCE;
    }

    protected AnchorPattern() {
    }

    @Override
    public UType getUType() {
        return UType.PARENT_NODE_KINDS;
    }

    @Override
    public Pattern typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        return this;
    }

    @Override
    public boolean matchesBeneathAnchor(NodeInfo node, NodeInfo anchor, XPathContext context) throws XPathException {
        return anchor == null || node == anchor;
    }

    @Override
    public boolean matches(Item item, XPathContext context) throws XPathException {
        throw new AssertionError();
    }

    @Override
    public ItemType getItemType() {
        return AnyNodeTest.getInstance();
    }

    @Override
    public String reconstruct() {
        return ".";
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("p.anchor");
        presenter.endElement();
    }

    @Override
    public Pattern copy(RebindingMap rebindings) {
        return this;
    }
}

