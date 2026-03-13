/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.FocusTrackingIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.pattern.PatternMaker;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.NumericValue;

public class GeneralPositionalPattern
extends Pattern {
    private NodeTest nodeTest;
    private Expression positionExpr;
    private boolean usesPosition = true;

    public GeneralPositionalPattern(NodeTest base, Expression positionExpr) {
        this.nodeTest = base;
        this.positionExpr = positionExpr;
    }

    @Override
    public Iterable<Operand> operands() {
        return new Operand(this, this.positionExpr, OperandRole.FOCUS_CONTROLLED_ACTION);
    }

    public Expression getPositionExpr() {
        return this.positionExpr;
    }

    public NodeTest getNodeTest() {
        return this.nodeTest;
    }

    public void setUsesPosition(boolean usesPosition) {
        this.usesPosition = usesPosition;
    }

    @Override
    public Pattern simplify() throws XPathException {
        this.positionExpr = this.positionExpr.simplify();
        return this;
    }

    @Override
    public Pattern typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        ContextItemStaticInfo cit = visitor.getConfiguration().makeContextItemStaticInfo(this.getItemType(), false);
        this.positionExpr = this.positionExpr.typeCheck(visitor, cit);
        this.positionExpr = ExpressionTool.unsortedIfHomogeneous(this.positionExpr, false);
        return this;
    }

    @Override
    public Pattern optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Configuration config = visitor.getConfiguration();
        ContextItemStaticInfo cit = config.makeContextItemStaticInfo(this.getItemType(), false);
        this.positionExpr = this.positionExpr.optimize(visitor, cit);
        if (Literal.isConstantBoolean(this.positionExpr, true)) {
            return new NodeTestPattern(this.nodeTest);
        }
        if (Literal.isConstantBoolean(this.positionExpr, false)) {
            return new NodeTestPattern(ErrorType.getInstance());
        }
        if ((this.positionExpr.getDependencies() & 4) == 0) {
            this.usesPosition = false;
        }
        if (!FilterExpression.isPositionalFilter(this.positionExpr, config.getTypeHierarchy())) {
            int axis = 3;
            if (this.nodeTest.getPrimitiveType() == 2) {
                axis = 2;
            } else if (this.nodeTest.getPrimitiveType() == 13) {
                axis = 8;
            }
            AxisExpression ae = new AxisExpression(axis, this.nodeTest);
            FilterExpression fe = new FilterExpression(ae, this.positionExpr);
            return PatternMaker.fromExpression(fe, config, true).typeCheck(visitor, contextInfo);
        }
        return this;
    }

    @Override
    public int getDependencies() {
        return this.positionExpr.getDependencies() & 0x180;
    }

    @Override
    public int allocateSlots(SlotManager slotManager, int nextFree) {
        return ExpressionTool.allocateSlots(this.positionExpr, nextFree, slotManager);
    }

    @Override
    public boolean matches(Item item, XPathContext context) throws XPathException {
        return item instanceof NodeInfo && this.matchesBeneathAnchor((NodeInfo)item, null, context);
    }

    @Override
    public boolean matchesBeneathAnchor(NodeInfo node, NodeInfo anchor, XPathContext context) throws XPathException {
        return this.internalMatches(node, anchor, context);
    }

    private boolean internalMatches(NodeInfo node, NodeInfo anchor, XPathContext context) throws XPathException {
        if (!this.nodeTest.test(node)) {
            return false;
        }
        XPathContextMinor c2 = context.newMinorContext();
        ManualIterator iter = new ManualIterator(node);
        c2.setCurrentIterator(iter);
        try {
            Item predicate;
            XPathContextMinor c = c2;
            int actualPosition = -1;
            if (this.usesPosition) {
                actualPosition = this.getActualPosition(node, Integer.MAX_VALUE, context.getCurrentIterator());
                ManualIterator man = new ManualIterator(node, actualPosition);
                XPathContextMinor c3 = c2.newMinorContext();
                c3.setCurrentIterator(man);
                c = c3;
            }
            if ((predicate = this.positionExpr.evaluateItem(c)) instanceof NumericValue) {
                NumericValue position = (NumericValue)this.positionExpr.evaluateItem(context);
                int requiredPos = position.asSubscript();
                if (actualPosition < 0 && requiredPos != -1) {
                    actualPosition = this.getActualPosition(node, requiredPos, context.getCurrentIterator());
                }
                return requiredPos != -1 && actualPosition == requiredPos;
            }
            return ExpressionTool.effectiveBooleanValue(predicate);
        } catch (XPathException.Circularity | XPathException.StackOverflow e) {
            throw e;
        } catch (XPathException e) {
            this.handleDynamicError(e, c2);
            return false;
        }
    }

    private int getActualPosition(NodeInfo node, int max, FocusIterator iterator) {
        if (iterator instanceof FocusTrackingIterator) {
            return ((FocusTrackingIterator)iterator).getSiblingPosition(node, this.nodeTest, max);
        }
        return Navigator.getSiblingPosition(node, this.nodeTest, max);
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
        return this.nodeTest;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof GeneralPositionalPattern) {
            GeneralPositionalPattern fp = (GeneralPositionalPattern)other;
            return this.nodeTest.equals(fp.nodeTest) && this.positionExpr.isEqual(fp.positionExpr);
        }
        return false;
    }

    @Override
    public int computeHashCode() {
        return this.nodeTest.hashCode() ^ this.positionExpr.hashCode();
    }

    @Override
    public Pattern copy(RebindingMap rebindings) {
        GeneralPositionalPattern n = new GeneralPositionalPattern(this.nodeTest.copy(), this.positionExpr.copy(rebindings));
        ExpressionTool.copyLocationInfo(this, n);
        n.setOriginalText(this.getOriginalText());
        return n;
    }

    @Override
    public String reconstruct() {
        return this.nodeTest + "[" + this.positionExpr + "]";
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("p.genPos");
        presenter.emitAttribute("test", AlphaCode.fromItemType(this.nodeTest));
        if (!this.usesPosition) {
            presenter.emitAttribute("flags", "P");
        }
        this.positionExpr.export(presenter);
        presenter.endElement();
    }
}

