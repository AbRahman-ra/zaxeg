/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.TailIterator;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;

public class TailExpression
extends UnaryExpression {
    int start;

    public TailExpression(Expression base, int start) {
        super(base);
        this.start = start;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().optimize(visitor, contextInfo);
        if (this.getBaseExpression() instanceof Literal) {
            GroundedValue value = this.iterate(visitor.getStaticContext().makeEarlyEvaluationContext()).materialize();
            return Literal.makeLiteral(value, this);
        }
        return this;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        TailExpression exp = new TailExpression(this.getBaseExpression().copy(rebindings), this.start);
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public ItemType getItemType() {
        return this.getBaseExpression().getItemType();
    }

    @Override
    public int computeCardinality() {
        return this.getBaseExpression().getCardinality() | 0x2000;
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.SAME_FOCUS_ACTION;
    }

    public int getStart() {
        return this.start;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TailExpression && this.getBaseExpression().isEqual(((TailExpression)other).getBaseExpression()) && this.start == ((TailExpression)other).start;
    }

    @Override
    public int computeHashCode() {
        return super.computeHashCode() ^ this.start;
    }

    @Override
    public String getStreamerName() {
        return "TailExpression";
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        SequenceIterator baseIter = this.getBaseExpression().iterate(context);
        return TailIterator.make(baseIter, this.start);
    }

    @Override
    public String getExpressionName() {
        return "tail";
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("tail", this);
        destination.emitAttribute("start", this.start + "");
        this.getBaseExpression().export(destination);
        destination.endElement();
    }

    @Override
    public String toString() {
        if (this.start == 2) {
            return "tail(" + this.getBaseExpression() + ")";
        }
        return ExpressionTool.parenthesize(this.getBaseExpression()) + "[position() ge " + this.start + "]";
    }

    @Override
    public String toShortString() {
        if (this.start == 2) {
            return "tail(" + this.getBaseExpression().toShortString() + ")";
        }
        return this.getBaseExpression().toShortString() + "[position() ge " + this.start + "]";
    }
}

