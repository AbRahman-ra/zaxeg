/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.CardinalityChecker;
import net.sf.saxon.expr.CompareToIntegerConstant;
import net.sf.saxon.expr.ComparisonExpression;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.GeneralComparison;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.RootExpression;
import net.sf.saxon.expr.SlashExpression;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.FLWORExpression;
import net.sf.saxon.expr.flwor.ForClauseOuterPull;
import net.sf.saxon.expr.flwor.ForClauseOuterPush;
import net.sf.saxon.expr.flwor.ForClausePull;
import net.sf.saxon.expr.flwor.ForClausePush;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.expr.flwor.OperandProcessor;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.functions.KeyFn;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

public class ForClause
extends Clause {
    private LocalVariableBinding rangeVariable;
    private LocalVariableBinding positionVariable;
    private Operand sequenceOp;
    private boolean allowsEmpty;

    @Override
    public Clause.ClauseName getClauseKey() {
        return Clause.ClauseName.FOR;
    }

    @Override
    public ForClause copy(FLWORExpression flwor, RebindingMap rebindings) {
        ForClause f2 = new ForClause();
        f2.setLocation(this.getLocation());
        f2.setPackageData(this.getPackageData());
        f2.rangeVariable = this.rangeVariable.copy();
        if (this.positionVariable != null) {
            f2.positionVariable = this.positionVariable.copy();
        }
        f2.initSequence(flwor, this.getSequence().copy(rebindings));
        f2.allowsEmpty = this.allowsEmpty;
        return f2;
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

    public void setPositionVariable(LocalVariableBinding binding) {
        this.positionVariable = binding;
    }

    public LocalVariableBinding getPositionVariable() {
        return this.positionVariable;
    }

    @Override
    public LocalVariableBinding[] getRangeVariables() {
        if (this.positionVariable == null) {
            return new LocalVariableBinding[]{this.rangeVariable};
        }
        return new LocalVariableBinding[]{this.rangeVariable, this.positionVariable};
    }

    public void setAllowingEmpty(boolean option) {
        this.allowsEmpty = option;
    }

    public boolean isAllowingEmpty() {
        return this.allowsEmpty;
    }

    @Override
    public void typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        SequenceType decl = this.rangeVariable.getRequiredType();
        if (this.allowsEmpty && !Cardinality.allowsZero(decl.getCardinality())) {
            RoleDiagnostic role = new RoleDiagnostic(3, this.rangeVariable.getVariableQName().getDisplayName(), 0);
            Expression checker = CardinalityChecker.makeCardinalityChecker(this.getSequence(), 49152, role);
            this.setSequence(checker);
        }
        SequenceType sequenceType = SequenceType.makeSequenceType(decl.getPrimaryType(), 57344);
        RoleDiagnostic role = new RoleDiagnostic(3, this.rangeVariable.getVariableQName().getDisplayName(), 0);
        this.setSequence(TypeChecker.strictTypeCheck(this.getSequence(), sequenceType, role, visitor.getStaticContext()));
    }

    @Override
    public TuplePull getPullStream(TuplePull base, XPathContext context) {
        if (this.allowsEmpty) {
            return new ForClauseOuterPull(base, this);
        }
        return new ForClausePull(base, this);
    }

    @Override
    public TuplePush getPushStream(TuplePush destination, Outputter output, XPathContext context) {
        if (this.allowsEmpty) {
            return new ForClauseOuterPush(output, destination, this);
        }
        return new ForClausePush(output, destination, this);
    }

    public boolean addPredicate(FLWORExpression flwor, ExpressionVisitor visitor, ContextItemStaticInfo contextItemType, Expression condition) throws XPathException {
        Binding[] thisVar;
        ItemType selectionContextItemType;
        Configuration config = this.getConfiguration();
        Optimizer opt = visitor.obtainOptimizer();
        boolean debug = config.getBooleanProperty(Feature.TRACE_OPTIMIZER_DECISIONS);
        TypeHierarchy th = config.getTypeHierarchy();
        Expression head = null;
        Expression selection = this.getSequence();
        ItemType itemType = selectionContextItemType = contextItemType == null ? null : contextItemType.getItemType();
        if (this.getSequence() instanceof SlashExpression) {
            if (((SlashExpression)this.getSequence()).isAbsolute()) {
                head = ((SlashExpression)this.getSequence()).getFirstStep();
                selection = ((SlashExpression)this.getSequence()).getRemainingSteps();
                selectionContextItemType = head.getItemType();
            } else {
                SlashExpression p = ((SlashExpression)this.getSequence()).tryToMakeAbsolute();
                if (p != null) {
                    this.setSequence(p);
                    head = p.getFirstStep();
                    selection = p.getRemainingSteps();
                    selectionContextItemType = head.getItemType();
                }
            }
        }
        boolean changed = false;
        if (this.positionVariable != null && this.positionVariable.getNominalReferenceCount() == 0) {
            this.positionVariable = null;
        }
        if (this.positionVariable != null && (condition instanceof ValueComparison || condition instanceof GeneralComparison || condition instanceof CompareToIntegerConstant) && ExpressionTool.dependsOnVariable(condition, new Binding[]{this.positionVariable})) {
            ComparisonExpression comp = (ComparisonExpression)((Object)condition);
            Expression[] operands = new Expression[]{comp.getLhsExpression(), comp.getRhsExpression()};
            if (ExpressionTool.dependsOnVariable(flwor, new Binding[]{this.positionVariable})) {
                return false;
            }
            for (int op = 0; op < 2; ++op) {
                Binding[] thisVar2 = new Binding[]{this.getRangeVariable()};
                if (this.positionVariable == null || !(operands[op] instanceof VariableReference) || changed) continue;
                ArrayList<VariableReference> varRefs = new ArrayList<VariableReference>();
                ExpressionTool.gatherVariableReferences(condition, this.positionVariable, varRefs);
                if (varRefs.size() != 1 || varRefs.get(0) != operands[op] || ExpressionTool.dependsOnFocus(operands[1 - op]) || ExpressionTool.dependsOnVariable(operands[1 - op], thisVar2)) continue;
                RetainedStaticContext rsc = new RetainedStaticContext(visitor.getStaticContext());
                Expression position = SystemFunction.makeCall("position", rsc, new Expression[0]);
                Expression predicate = condition.copy(new RebindingMap());
                Operand child = op == 0 ? ((ComparisonExpression)((Object)predicate)).getLhs() : ((ComparisonExpression)((Object)predicate)).getRhs();
                child.setChildExpression(position);
                if (debug) {
                    opt.trace("Replaced positional variable in predicate by position()", predicate);
                }
                selection = new FilterExpression(selection, predicate);
                ExpressionTool.copyLocationInfo(predicate, selection);
                ContextItemStaticInfo cit = config.makeContextItemStaticInfo(selectionContextItemType, true);
                selection = selection.typeCheck(visitor, cit);
                if (!ExpressionTool.dependsOnVariable(flwor, new Binding[]{this.positionVariable})) {
                    this.positionVariable = null;
                }
                changed = true;
                break;
            }
        }
        if (this.positionVariable == null && opt.isVariableReplaceableByDot(condition, thisVar = new Binding[]{this.getRangeVariable()})) {
            ContextItemExpression replacement = new ContextItemExpression();
            boolean found = ExpressionTool.inlineVariableReferences(condition, this.getRangeVariable(), replacement);
            if (found) {
                ContextItemStaticInfo cit = config.makeContextItemStaticInfo(this.getSequence().getItemType(), true);
                Expression predicate = condition.typeCheck(visitor, cit);
                Affinity rel = th.relationship(predicate.getItemType(), BuiltInAtomicType.INTEGER);
                if (rel != Affinity.DISJOINT) {
                    RetainedStaticContext rsc = new RetainedStaticContext(visitor.getStaticContext());
                    predicate = SystemFunction.makeCall("boolean", rsc, predicate);
                    assert (predicate != null);
                }
                selection = new FilterExpression(selection, predicate);
                ExpressionTool.copyLocationInfo(predicate, selection);
                cit = config.makeContextItemStaticInfo(selectionContextItemType, true);
                selection = selection.typeCheck(visitor, cit);
                changed = true;
            }
        }
        if (changed) {
            if (head == null) {
                this.setSequence(selection);
            } else if (head instanceof RootExpression && selection.isCallOn(KeyFn.class)) {
                this.setSequence(selection);
            } else {
                Expression path = ExpressionTool.makePathExpression(head, selection);
                if (path instanceof SlashExpression) {
                    ExpressionTool.copyLocationInfo(condition, path);
                    Expression k = visitor.obtainOptimizer().convertPathExpressionToKey((SlashExpression)path, visitor);
                    if (k == null) {
                        this.setSequence(path);
                    } else {
                        this.setSequence(k);
                    }
                    this.sequenceOp.typeCheck(visitor, contextItemType);
                    this.sequenceOp.optimize(visitor, contextItemType);
                }
            }
        }
        return changed;
    }

    @Override
    public void processOperands(OperandProcessor processor) throws XPathException {
        processor.processOperand(this.sequenceOp);
    }

    @Override
    public void gatherVariableReferences(ExpressionVisitor visitor, Binding binding, List<VariableReference> references) {
        ExpressionTool.gatherVariableReferences(this.getSequence(), binding, references);
    }

    @Override
    public void refineVariableType(ExpressionVisitor visitor, List<VariableReference> references, Expression returnExpr) {
        ItemType actualItemType = this.getSequence().getItemType();
        if (actualItemType instanceof ErrorType) {
            actualItemType = AnyItemType.getInstance();
        }
        for (VariableReference ref : references) {
            ref.refineVariableType(actualItemType, this.allowsEmpty ? 24576 : 16384, null, this.getSequence().getSpecialProperties());
        }
    }

    @Override
    public void addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        PathMap.PathMapNodeSet varPath = this.getSequence().addToPathMap(pathMap, pathMapNodeSet);
        pathMap.registerPathForVariable(this.rangeVariable, varPath);
    }

    @Override
    public void explain(ExpressionPresenter out) throws XPathException {
        out.startElement("for");
        out.emitAttribute("var", this.getRangeVariable().getVariableQName());
        out.emitAttribute("slot", this.getRangeVariable().getLocalSlotNumber() + "");
        LocalVariableBinding posVar = this.getPositionVariable();
        if (posVar != null) {
            out.emitAttribute("at", posVar.getVariableQName());
            out.emitAttribute("at-slot", posVar.getLocalSlotNumber() + "");
        }
        this.getSequence().export(out);
        out.endElement();
    }

    @Override
    public String toShortString() {
        FastStringBuffer fsb = new FastStringBuffer(64);
        fsb.append("for $");
        fsb.append(this.rangeVariable.getVariableQName().getDisplayName());
        fsb.cat(' ');
        LocalVariableBinding posVar = this.getPositionVariable();
        if (posVar != null) {
            fsb.append("at $");
            fsb.append(posVar.getVariableQName().getDisplayName());
            fsb.cat(' ');
        }
        fsb.append("in ");
        fsb.append(this.getSequence().toShortString());
        return fsb.toString();
    }

    public String toString() {
        FastStringBuffer fsb = new FastStringBuffer(64);
        fsb.append("for $");
        fsb.append(this.rangeVariable.getVariableQName().getDisplayName());
        fsb.cat(' ');
        LocalVariableBinding posVar = this.getPositionVariable();
        if (posVar != null) {
            fsb.append("at $");
            fsb.append(posVar.getVariableQName().getDisplayName());
            fsb.cat(' ');
        }
        fsb.append("in ");
        fsb.append(this.getSequence().toString());
        return fsb.toString();
    }
}

