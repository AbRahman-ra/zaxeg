/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.sort.DocumentSorter;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.SequenceType;

public class ConditionalSorter
extends Expression {
    private Operand conditionOp;
    private Operand sorterOp;
    private static final OperandRole DOC_SORTER_ROLE = new OperandRole(16, OperandUsage.TRANSMISSION, SequenceType.ANY_SEQUENCE, expr -> expr instanceof DocumentSorter);

    public ConditionalSorter(Expression condition, DocumentSorter sorter) {
        this.conditionOp = new Operand(this, condition, OperandRole.SINGLE_ATOMIC);
        this.sorterOp = new Operand(this, sorter, DOC_SORTER_ROLE);
        this.adoptChildExpression(condition);
        this.adoptChildExpression(sorter);
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandList(this.conditionOp, this.sorterOp);
    }

    public void setCondition(Expression condition) {
        this.conditionOp.setChildExpression(condition);
    }

    public void setDocumentSorter(DocumentSorter documentSorter) {
        this.sorterOp.setChildExpression(documentSorter);
    }

    public Expression getCondition() {
        return this.conditionOp.getChildExpression();
    }

    public DocumentSorter getDocumentSorter() {
        return (DocumentSorter)this.sorterOp.getChildExpression();
    }

    @Override
    public Expression simplify() throws XPathException {
        return this.rewrite(Expression::simplify);
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        return this.rewrite(exp -> exp.typeCheck(visitor, contextInfo));
    }

    @Override
    public int getCardinality() {
        return this.getDocumentSorter().getCardinality();
    }

    @Override
    protected int computeSpecialProperties() {
        return this.getCondition().getSpecialProperties() | 0x20000;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        return this.rewrite(exp -> exp.optimize(visitor, contextInfo));
    }

    private Expression rewrite(RewriteAction rewriter) throws XPathException {
        Expression base = rewriter.rewrite(this.getDocumentSorter());
        if (!(base instanceof DocumentSorter)) {
            return base;
        }
        this.sorterOp.setChildExpression(base);
        Expression cond = rewriter.rewrite(this.getCondition());
        if (cond instanceof Literal) {
            boolean b = ((Literal)cond).getValue().effectiveBooleanValue();
            if (b) {
                return base;
            }
            return ((DocumentSorter)base).getBaseExpression();
        }
        this.conditionOp.setChildExpression(cond);
        return this;
    }

    @Override
    public Expression unordered(boolean retainAllNodes, boolean forStreaming) throws XPathException {
        Expression base = this.getDocumentSorter().unordered(retainAllNodes, forStreaming);
        if (base instanceof DocumentSorter) {
            return this;
        }
        return base;
    }

    @Override
    protected int computeCardinality() {
        return 57344;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ConditionalSorter cs = new ConditionalSorter(this.getCondition().copy(rebindings), (DocumentSorter)this.getDocumentSorter().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, cs);
        return cs;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("conditionalSort", this);
        this.getCondition().export(out);
        this.getDocumentSorter().export(out);
        out.endElement();
    }

    @Override
    public ItemType getItemType() {
        return this.getDocumentSorter().getItemType();
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        boolean b = this.getCondition().effectiveBooleanValue(context);
        if (b) {
            return this.getDocumentSorter().iterate(context);
        }
        return this.getDocumentSorter().getBaseExpression().iterate(context);
    }

    @Override
    public String getExpressionName() {
        return "conditionalSort";
    }

    @FunctionalInterface
    private static interface RewriteAction {
        public Expression rewrite(Expression var1) throws XPathException;
    }
}

