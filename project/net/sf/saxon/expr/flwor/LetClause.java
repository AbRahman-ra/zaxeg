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
import net.sf.saxon.expr.flwor.LetClausePull;
import net.sf.saxon.expr.flwor.LetClausePush;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.expr.flwor.OperandProcessor;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.Evaluator;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.ItemType;

public class LetClause
extends Clause {
    private LocalVariableBinding rangeVariable;
    private Operand sequenceOp;
    private Evaluator evaluator;

    @Override
    public Clause.ClauseName getClauseKey() {
        return Clause.ClauseName.LET;
    }

    public Evaluator getEvaluator() {
        if (this.evaluator == null) {
            this.evaluator = ExpressionTool.lazyEvaluator(this.getSequence(), true);
        }
        return this.evaluator;
    }

    @Override
    public LetClause copy(FLWORExpression flwor, RebindingMap rebindings) {
        LetClause let2 = new LetClause();
        let2.setLocation(this.getLocation());
        let2.setPackageData(this.getPackageData());
        let2.rangeVariable = this.rangeVariable.copy();
        let2.initSequence(flwor, this.getSequence().copy(rebindings));
        return let2;
    }

    public void initSequence(FLWORExpression flwor, Expression sequence) {
        this.sequenceOp = new Operand(flwor, sequence, this.isRepeated() ? OperandRole.REPEAT_NAVIGATE : OperandRole.NAVIGATE);
    }

    public void setSequence(Expression sequence) {
        this.sequenceOp.setChildExpression(sequence);
    }

    public Expression getSequence() {
        return this.sequenceOp.getChildExpression();
    }

    public void setRangeVariable(LocalVariableBinding binding) {
        this.rangeVariable = binding;
    }

    public LocalVariableBinding getRangeVariable() {
        return this.rangeVariable;
    }

    @Override
    public LocalVariableBinding[] getRangeVariables() {
        return new LocalVariableBinding[]{this.rangeVariable};
    }

    @Override
    public TuplePull getPullStream(TuplePull base, XPathContext context) {
        return new LetClausePull(base, this);
    }

    @Override
    public TuplePush getPushStream(TuplePush destination, Outputter output, XPathContext context) {
        return new LetClausePush(output, destination, this);
    }

    @Override
    public void processOperands(OperandProcessor processor) throws XPathException {
        processor.processOperand(this.sequenceOp);
    }

    @Override
    public void typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        RoleDiagnostic role = new RoleDiagnostic(3, this.rangeVariable.getVariableQName().getDisplayName(), 0);
        this.setSequence(TypeChecker.strictTypeCheck(this.getSequence(), this.rangeVariable.getRequiredType(), role, visitor.getStaticContext()));
        this.evaluator = ExpressionTool.lazyEvaluator(this.getSequence(), true);
    }

    @Override
    public void gatherVariableReferences(ExpressionVisitor visitor, Binding binding, List<VariableReference> references) {
        ExpressionTool.gatherVariableReferences(this.getSequence(), binding, references);
    }

    @Override
    public void refineVariableType(ExpressionVisitor visitor, List<VariableReference> references, Expression returnExpr) {
        Expression seq = this.getSequence();
        ItemType actualItemType = seq.getItemType();
        for (VariableReference ref : references) {
            ref.refineVariableType(actualItemType, this.getSequence().getCardinality(), seq instanceof Literal ? ((Literal)seq).getValue() : null, seq.getSpecialProperties());
            ExpressionTool.resetStaticProperties(returnExpr);
        }
    }

    @Override
    public void addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        PathMap.PathMapNodeSet varPath = this.getSequence().addToPathMap(pathMap, pathMapNodeSet);
        pathMap.registerPathForVariable(this.rangeVariable, varPath);
    }

    @Override
    public void explain(ExpressionPresenter out) throws XPathException {
        out.startElement("let");
        out.emitAttribute("var", this.getRangeVariable().getVariableQName());
        out.emitAttribute("slot", this.getRangeVariable().getLocalSlotNumber() + "");
        this.getSequence().export(out);
        out.endElement();
    }

    @Override
    public String toShortString() {
        FastStringBuffer fsb = new FastStringBuffer(64);
        fsb.append("let $");
        fsb.append(this.rangeVariable.getVariableQName().getDisplayName());
        fsb.append(" := ");
        fsb.append(this.getSequence().toShortString());
        return fsb.toString();
    }

    public String toString() {
        FastStringBuffer fsb = new FastStringBuffer(64);
        fsb.append("let $");
        fsb.append(this.rangeVariable.getVariableQName().getDisplayName());
        fsb.append(" := ");
        fsb.append(this.getSequence().toString());
        return fsb.toString();
    }
}

