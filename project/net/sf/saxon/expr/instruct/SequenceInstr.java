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

public class SequenceInstr
extends UnaryExpression {
    public SequenceInstr(Expression base) {
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
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().typeCheck(visitor, contextInfo);
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().optimize(visitor, contextInfo);
        return this;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        return new SequenceInstr(this.getBaseExpression().copy(rebindings));
    }

    @Override
    public int getImplementationMethod() {
        return this.getBaseExpression().getImplementationMethod();
    }

    @Override
    public String getExpressionName() {
        return "sequence";
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
    public void export(ExpressionPresenter out) throws XPathException {
        this.getBaseExpression().export(out);
    }

    @Override
    public String getStreamerName() {
        return "SequenceInstr";
    }
}

