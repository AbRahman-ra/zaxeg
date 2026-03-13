/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;

public final class AncestorQualifiedPattern
extends Pattern {
    private Pattern basePattern;
    private Pattern upperPattern;
    private int upwardsAxis = 9;
    private ItemType refinedItemType;
    private boolean testUpperPatternFirst = false;

    public AncestorQualifiedPattern(Pattern base, Pattern upper, int axis) {
        this.basePattern = base;
        this.upperPattern = upper;
        this.upwardsAxis = axis;
        this.adoptChildExpression(base);
        this.adoptChildExpression(upper);
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandList(new Operand(this, this.upperPattern, OperandRole.SAME_FOCUS_ACTION), new Operand(this, this.basePattern, OperandRole.SAME_FOCUS_ACTION));
    }

    @Override
    public void bindCurrent(LocalBinding binding) {
        this.basePattern.bindCurrent(binding);
        this.upperPattern.bindCurrent(binding);
    }

    public Pattern getBasePattern() {
        return this.basePattern;
    }

    public Pattern getUpperPattern() {
        return this.upperPattern;
    }

    public int getUpwardsAxis() {
        return this.upwardsAxis;
    }

    @Override
    public boolean isMotionless() {
        return this.basePattern.isMotionless() && this.upperPattern.isMotionless();
    }

    @Override
    public boolean matchesCurrentGroup() {
        return this.upperPattern.matchesCurrentGroup();
    }

    @Override
    public Pattern simplify() throws XPathException {
        this.upperPattern = this.upperPattern.simplify();
        this.basePattern = this.basePattern.simplify();
        return this;
    }

    @Override
    public Pattern typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        ItemType type;
        this.basePattern = this.basePattern.typeCheck(visitor, contextItemType);
        this.upperPattern = this.upperPattern.typeCheck(visitor, contextItemType);
        if (this.upwardsAxis == 9 && (type = this.basePattern.getItemType()) instanceof NodeTest) {
            AxisExpression step = type.getPrimitiveType() == 2 ? new AxisExpression(2, (NodeTest)type) : new AxisExpression(3, (NodeTest)type);
            ExpressionTool.copyLocationInfo(this, step);
            Expression exp = step.typeCheck(visitor, visitor.getConfiguration().makeContextItemStaticInfo(this.upperPattern.getItemType(), false));
            this.refinedItemType = exp.getItemType();
        }
        this.testUpperPatternFirst = this.upperPattern.getCost() < this.basePattern.getCost();
        return this;
    }

    @Override
    public int getDependencies() {
        return this.basePattern.getDependencies() | this.upperPattern.getDependencies();
    }

    @Override
    public int allocateSlots(SlotManager slotManager, int nextFree) {
        nextFree = this.upperPattern.allocateSlots(slotManager, nextFree);
        nextFree = this.basePattern.allocateSlots(slotManager, nextFree);
        return nextFree;
    }

    @Override
    public boolean matches(Item item, XPathContext context) throws XPathException {
        return item instanceof NodeInfo && this.matchesBeneathAnchor((NodeInfo)item, null, context);
    }

    @Override
    public boolean matchesBeneathAnchor(NodeInfo node, NodeInfo anchor, XPathContext context) throws XPathException {
        if (this.testUpperPatternFirst) {
            return this.matchesUpperPattern(node, anchor, context) && this.basePattern.matches(node, context);
        }
        return this.basePattern.matchesBeneathAnchor(node, anchor, context) && this.matchesUpperPattern(node, anchor, context);
    }

    private boolean matchesUpperPattern(NodeInfo node, NodeInfo anchor, XPathContext context) throws XPathException {
        switch (this.upwardsAxis) {
            case 12: {
                return this.upperPattern.matchesBeneathAnchor(node, anchor, context);
            }
            case 9: {
                NodeInfo par = node.getParent();
                return par != null && this.upperPattern.matchesBeneathAnchor(par, anchor, context);
            }
            case 0: {
                NodeInfo anc = node.getParent();
                return this.hasMatchingAncestor(anchor, anc, context);
            }
            case 1: {
                return this.hasMatchingAncestor(anchor, node, context);
            }
        }
        throw new XPathException("Unsupported axis " + AxisInfo.axisName[this.upwardsAxis] + " in pattern");
    }

    private boolean hasMatchingAncestor(NodeInfo anchor, NodeInfo anc, XPathContext context) throws XPathException {
        while (anc != null) {
            if (this.upperPattern.matchesBeneathAnchor(anc, anchor, context)) {
                return true;
            }
            if (anc.equals(anchor)) {
                return false;
            }
            anc = anc.getParent();
        }
        return false;
    }

    @Override
    public UType getUType() {
        return this.basePattern.getUType();
    }

    @Override
    public int getFingerprint() {
        return this.basePattern.getFingerprint();
    }

    @Override
    public ItemType getItemType() {
        if (this.refinedItemType != null) {
            return this.refinedItemType;
        }
        return this.basePattern.getItemType();
    }

    @Override
    public Pattern convertToTypedPattern(String val) throws XPathException {
        if (this.upperPattern.getUType().equals(UType.DOCUMENT)) {
            Pattern b2 = this.basePattern.convertToTypedPattern(val);
            if (b2 == this.basePattern) {
                return this;
            }
            return new AncestorQualifiedPattern(b2, this.upperPattern, this.upwardsAxis);
        }
        Pattern u2 = this.upperPattern.convertToTypedPattern(val);
        if (u2 == this.upperPattern) {
            return this;
        }
        return new AncestorQualifiedPattern(this.basePattern, u2, this.upwardsAxis);
    }

    @Override
    public String reconstruct() {
        return this.upperPattern + (this.upwardsAxis == 9 ? "/" : "//") + this.basePattern;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AncestorQualifiedPattern) {
            AncestorQualifiedPattern aqp = (AncestorQualifiedPattern)other;
            return this.basePattern.isEqual(aqp.basePattern) && this.upperPattern.isEqual(aqp.upperPattern) && this.upwardsAxis == aqp.upwardsAxis;
        }
        return false;
    }

    @Override
    public int computeHashCode() {
        return 0x158CB ^ this.basePattern.hashCode() ^ this.upperPattern.hashCode() ^ this.upwardsAxis << 22;
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("p.withUpper");
        presenter.emitAttribute("axis", AxisInfo.axisName[this.getUpwardsAxis()]);
        presenter.emitAttribute("upFirst", "" + this.testUpperPatternFirst);
        this.basePattern.export(presenter);
        this.upperPattern.export(presenter);
        presenter.endElement();
    }

    @Override
    public Pattern copy(RebindingMap rebindings) {
        AncestorQualifiedPattern n = new AncestorQualifiedPattern(this.basePattern.copy(rebindings), this.upperPattern.copy(rebindings), this.upwardsAxis);
        ExpressionTool.copyLocationInfo(this, n);
        n.setOriginalText(this.getOriginalText());
        return n;
    }
}

