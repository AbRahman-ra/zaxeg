/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.InstanceOfExpression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.pattern.PatternWithPredicate;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;

public class BooleanExpressionPattern
extends Pattern
implements PatternWithPredicate {
    private Operand expressionOp;

    public BooleanExpressionPattern(Expression expression) {
        this.expressionOp = new Operand(this, expression, OperandRole.SINGLE_ATOMIC);
        this.setPriority(1.0);
    }

    @Override
    public Expression getPredicate() {
        return this.expressionOp.getChildExpression();
    }

    @Override
    public Iterable<Operand> operands() {
        return this.expressionOp;
    }

    @Override
    public UType getUType() {
        if (this.getPredicate() instanceof InstanceOfExpression) {
            return ((InstanceOfExpression)this.getPredicate()).getRequiredItemType().getUType();
        }
        return UType.ANY;
    }

    @Override
    public int allocateSlots(SlotManager slotManager, int nextFree) {
        return ExpressionTool.allocateSlots(this.getPredicate(), nextFree, slotManager);
    }

    @Override
    public Pattern typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        ContextItemStaticInfo cit = visitor.getConfiguration().getDefaultContextItemStaticInfo();
        this.expressionOp.setChildExpression(this.getPredicate().typeCheck(visitor, cit));
        return this;
    }

    @Override
    public Pattern optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        ContextItemStaticInfo cit = visitor.getConfiguration().getDefaultContextItemStaticInfo();
        this.expressionOp.setChildExpression(this.getPredicate().optimize(visitor, cit));
        return this;
    }

    @Override
    public boolean matches(Item item, XPathContext context) {
        XPathContextMinor c2 = context.newMinorContext();
        ManualIterator iter = new ManualIterator(item);
        c2.setCurrentIterator(iter);
        c2.setCurrentOutputUri(null);
        try {
            return this.getPredicate().effectiveBooleanValue(c2);
        } catch (XPathException e) {
            return false;
        }
    }

    @Override
    public ItemType getItemType() {
        InstanceOfExpression ioe;
        if (this.getPredicate() instanceof InstanceOfExpression && (ioe = (InstanceOfExpression)this.getPredicate()).getBaseExpression() instanceof ContextItemExpression) {
            return ioe.getRequiredItemType();
        }
        return AnyItemType.getInstance();
    }

    @Override
    public int getFingerprint() {
        return -1;
    }

    @Override
    public String reconstruct() {
        return ".[" + this.getPredicate() + "]";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BooleanExpressionPattern && ((BooleanExpressionPattern)other).getPredicate().isEqual(this.getPredicate());
    }

    @Override
    public int computeHashCode() {
        return 0x7AEFFEA9 ^ this.getPredicate().hashCode();
    }

    @Override
    public Pattern copy(RebindingMap rebindings) {
        BooleanExpressionPattern n = new BooleanExpressionPattern(this.getPredicate().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, n);
        n.setOriginalText(this.getOriginalText());
        return n;
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("p.booleanExp");
        this.getPredicate().export(presenter);
        presenter.endElement();
    }
}

