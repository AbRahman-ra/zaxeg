/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.SlashExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.SequenceType;

public final class SimpleStepExpression
extends SlashExpression {
    private static OperandRole STEP_ROLE = new OperandRole(6, OperandUsage.TRANSMISSION, SequenceType.ANY_SEQUENCE);

    public SimpleStepExpression(Expression start, Expression step) {
        super(start, step);
        if (!(step instanceof AxisExpression)) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected OperandRole getOperandRole(int arg) {
        return arg == 0 ? OperandRole.FOCUS_CONTROLLING_SELECT : STEP_ROLE;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getLhs().typeCheck(visitor, contextInfo);
        ItemType selectType = this.getStart().getItemType();
        if (selectType == ErrorType.getInstance()) {
            return Literal.makeEmptySequence();
        }
        ContextItemStaticInfo cit = visitor.getConfiguration().makeContextItemStaticInfo(selectType, false);
        cit.setContextSettingExpression(this.getStart());
        this.getRhs().typeCheck(visitor, cit);
        if (!(this.getStep() instanceof AxisExpression)) {
            if (Literal.isEmptySequence(this.getStep())) {
                return this.getStep();
            }
            SlashExpression se = new SlashExpression(this.getStart(), this.getStep());
            ExpressionTool.copyLocationInfo(this, se);
            return se;
        }
        if (this.getStart() instanceof ContextItemExpression && AxisInfo.isForwards[((AxisExpression)this.getStep()).getAxis()]) {
            return this.getStep();
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        return this;
    }

    @Override
    public SimpleStepExpression copy(RebindingMap rebindings) {
        SimpleStepExpression exp = new SimpleStepExpression(this.getStart().copy(rebindings), this.getStep().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        NodeInfo origin = null;
        try {
            origin = (NodeInfo)this.getStart().evaluateItem(context);
        } catch (XPathException e) {
            if ("XPDY0002".equals(e.getErrorCodeLocalPart()) && !e.hasBeenReported()) {
                throw new XPathException("The context item for axis step " + this.toShortString() + " is absent", "XPDY0002", this.getLocation());
            }
            throw e;
        }
        if (origin == null) {
            return EmptyIterator.getInstance();
        }
        return ((AxisExpression)this.getStep()).iterate(origin);
    }

    @Override
    public String getExpressionName() {
        return "simpleStep";
    }
}

