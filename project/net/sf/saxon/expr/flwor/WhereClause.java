/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import java.util.List;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.FLWORExpression;
import net.sf.saxon.expr.flwor.OperandProcessor;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.expr.flwor.WhereClausePull;
import net.sf.saxon.expr.flwor.WhereClausePush;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.ItemType;

public class WhereClause
extends Clause {
    private Operand predicateOp;

    public WhereClause(FLWORExpression flwor, Expression predicate) {
        this.predicateOp = new Operand(flwor, predicate, OperandRole.INSPECT);
    }

    @Override
    public void setRepeated(boolean repeated) {
        super.setRepeated(repeated);
        if (repeated) {
            this.predicateOp.setOperandRole(OperandRole.REPEAT_INSPECT);
        }
    }

    @Override
    public Clause.ClauseName getClauseKey() {
        return Clause.ClauseName.WHERE;
    }

    public Expression getPredicate() {
        return this.predicateOp.getChildExpression();
    }

    public void setPredicate(Expression predicate) {
        this.predicateOp.setChildExpression(predicate);
    }

    @Override
    public WhereClause copy(FLWORExpression flwor, RebindingMap rebindings) {
        WhereClause w2 = new WhereClause(flwor, this.getPredicate().copy(rebindings));
        w2.setLocation(this.getLocation());
        w2.setPackageData(this.getPackageData());
        return w2;
    }

    @Override
    public void typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        super.typeCheck(visitor, contextInfo);
    }

    @Override
    public TuplePull getPullStream(TuplePull base, XPathContext context) {
        return new WhereClausePull(base, this.getPredicate());
    }

    @Override
    public void gatherVariableReferences(ExpressionVisitor visitor, Binding binding, List<VariableReference> references) {
        ExpressionTool.gatherVariableReferences(this.getPredicate(), binding, references);
    }

    @Override
    public void refineVariableType(ExpressionVisitor visitor, List<VariableReference> references, Expression returnExpr) {
        ItemType actualItemType = this.getPredicate().getItemType();
        for (VariableReference ref : references) {
            ref.refineVariableType(actualItemType, this.getPredicate().getCardinality(), this.getPredicate() instanceof Literal ? ((Literal)this.getPredicate()).getValue() : null, this.getPredicate().getSpecialProperties());
            ExpressionTool.resetStaticProperties(returnExpr);
        }
    }

    @Override
    public TuplePush getPushStream(TuplePush destination, Outputter output, XPathContext context) {
        return new WhereClausePush(output, destination, this.getPredicate());
    }

    @Override
    public void processOperands(OperandProcessor processor) throws XPathException {
        processor.processOperand(this.predicateOp);
    }

    @Override
    public void addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        this.getPredicate().addToPathMap(pathMap, pathMapNodeSet);
    }

    @Override
    public void explain(ExpressionPresenter out) throws XPathException {
        out.startElement("where");
        this.getPredicate().export(out);
        out.endElement();
    }

    @Override
    public String toShortString() {
        FastStringBuffer fsb = new FastStringBuffer(64);
        fsb.append("where ");
        fsb.append(this.getPredicate().toShortString());
        return fsb.toString();
    }

    public String toString() {
        FastStringBuffer fsb = new FastStringBuffer(64);
        fsb.append("where ");
        fsb.append(this.getPredicate().toString());
        return fsb.toString();
    }
}

