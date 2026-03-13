/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import java.util.HashSet;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.pattern.VennPattern;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;

public class IntersectPattern
extends VennPattern {
    public IntersectPattern(Pattern p1, Pattern p2) {
        super(p1, p2);
    }

    @Override
    public ItemType getItemType() {
        return this.p1.getItemType();
    }

    @Override
    public UType getUType() {
        return this.p1.getUType().intersection(this.p2.getUType());
    }

    @Override
    public double getDefaultPriority() {
        return this.p1.getDefaultPriority();
    }

    @Override
    public boolean matches(Item item, XPathContext context) throws XPathException {
        return this.p1.matches(item, context) && this.p2.matches(item, context);
    }

    @Override
    public boolean matchesBeneathAnchor(NodeInfo node, NodeInfo anchor, XPathContext context) throws XPathException {
        return this.p1.matchesBeneathAnchor(node, anchor, context) && this.p2.matchesBeneathAnchor(node, anchor, context);
    }

    @Override
    public Pattern convertToTypedPattern(String val) throws XPathException {
        Pattern np1 = this.p1.convertToTypedPattern(val);
        Pattern np2 = this.p2.convertToTypedPattern(val);
        if (this.p1 == np1 && this.p2 == np2) {
            return this;
        }
        return new IntersectPattern(np1, np2);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof IntersectPattern) {
            HashSet<Pattern> s0 = new HashSet<Pattern>(10);
            this.gatherComponentPatterns(s0);
            HashSet<Pattern> s1 = new HashSet<Pattern>(10);
            ((IntersectPattern)other).gatherComponentPatterns(s1);
            return s0.equals(s1);
        }
        return false;
    }

    @Override
    public int computeHashCode() {
        return 0x13D7DFA6 ^ this.p1.hashCode() ^ this.p2.hashCode();
    }

    @Override
    protected String getOperatorName() {
        return "intersect";
    }

    @Override
    public Pattern copy(RebindingMap rebindings) {
        IntersectPattern n = new IntersectPattern(this.p1.copy(rebindings), this.p2.copy(rebindings));
        ExpressionTool.copyLocationInfo(this, n);
        n.setOriginalText(this.getOriginalText());
        return n;
    }
}

