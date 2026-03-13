/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;

public class OnNonEmptyExpr
extends UnaryExpression {
    public OnNonEmptyExpr(Expression base) {
        super(base);
    }

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    protected OperandRole getOperandRole() {
        return new OperandRole(0, OperandUsage.TRANSMISSION);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        return new OnNonEmptyExpr(this.getBaseExpression().copy(rebindings));
    }

    @Override
    public int getIntrinsicDependencies() {
        return 0x2000000;
    }

    @Override
    public boolean allowExtractingCommonSubexpressions() {
        return false;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().typeCheck(visitor, contextInfo);
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().optimize(visitor, contextInfo);
        if (visitor.isOptimizeForStreaming()) {
            visitor.obtainOptimizer().makeCopyOperationsExplicit(this, this.getOperand());
        }
        return this;
    }

    @Override
    public int getImplementationMethod() {
        return this.getBaseExpression().getImplementationMethod();
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        this.getBaseExpression().process(output, context);
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        return this.getBaseExpression().iterate(context);
    }

    @Override
    public String getExpressionName() {
        return "onNonEmpty";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("onNonEmpty", this);
        this.getBaseExpression().export(out);
        out.endElement();
    }

    @Override
    public String getStreamerName() {
        return "OnNonEmpty";
    }
}

