/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Assignation;
import net.sf.saxon.expr.EvaluationMode;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.PendingUpdateList;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.DocumentInstr;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.TailCallReturner;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.Evaluator;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public class LetExpression
extends Assignation
implements TailCallReturner {
    private Evaluator evaluator = null;
    private boolean needsEagerEvaluation = false;
    private boolean needsLazyEvaluation = false;
    private boolean isInstruction;

    public void setInstruction(boolean inst) {
        this.isInstruction = inst;
    }

    @Override
    public boolean isInstruction() {
        return this.isInstruction;
    }

    @Override
    public String getExpressionName() {
        return "let";
    }

    public void setNeedsEagerEvaluation(boolean req) {
        if (!req || this.needsLazyEvaluation) {
            // empty if block
        }
        this.needsEagerEvaluation = req;
    }

    public void setNeedsLazyEvaluation(boolean req) {
        if (req && this.needsEagerEvaluation) {
            this.needsEagerEvaluation = false;
        }
        this.needsLazyEvaluation = req;
    }

    public boolean isNeedsLazyEvaluation() {
        return this.needsLazyEvaluation;
    }

    @Override
    public boolean isLiftable(boolean forStreaming) {
        return super.isLiftable(forStreaming) && !this.needsEagerEvaluation;
    }

    @Override
    public void resetLocalStaticProperties() {
        super.resetLocalStaticProperties();
        this.references = new ArrayList();
        if (this.evaluator == Evaluator.VARIABLE && !(this.getSequence() instanceof VariableReference)) {
            this.evaluator = null;
            this.setEvaluator();
        }
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getSequenceOp().typeCheck(visitor, contextInfo);
        RoleDiagnostic role = new RoleDiagnostic(3, this.getVariableQName().getDisplayName(), 0);
        this.setSequence(TypeChecker.strictTypeCheck(this.getSequence(), this.requiredType, role, visitor.getStaticContext()));
        ItemType actualItemType = this.getSequence().getItemType();
        this.refineTypeInformation(actualItemType, this.getSequence().getCardinality(), this.getSequence() instanceof Literal ? ((Literal)this.getSequence()).getValue() : null, this.getSequence().getSpecialProperties(), this);
        this.getActionOp().typeCheck(visitor, contextInfo);
        return this;
    }

    @Override
    public boolean implementsStaticTypeCheck() {
        return true;
    }

    @Override
    public Expression staticTypeCheck(SequenceType req, boolean backwardsCompatible, RoleDiagnostic role, ExpressionVisitor visitor) throws XPathException {
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(backwardsCompatible);
        this.setAction(tc.staticTypeCheck(this.getAction(), req, role, visitor));
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Optimizer opt = visitor.obtainOptimizer();
        if (this.getAction() instanceof VariableReference && ((VariableReference)this.getAction()).getBinding() == this && !ExpressionTool.changesXsltContext(this.getSequence())) {
            this.getSequenceOp().optimize(visitor, contextItemType);
            opt.trace("Eliminated trivial variable " + this.getVariableName(), this.getSequence());
            return this.getSequence();
        }
        if (this.getSequence() instanceof Literal && opt.isOptionSet(4)) {
            opt.trace("Inlined constant variable " + this.getVariableName(), this.getSequence());
            this.replaceVariable(this.getSequence());
            return this.getAction().optimize(visitor, contextItemType);
        }
        if (this.getSequence() instanceof DocumentInstr && ((DocumentInstr)this.getSequence()).isTextOnly()) {
            this.verifyReferences();
            if (this.allReferencesAreFlattened()) {
                Expression stringValueExpression = ((DocumentInstr)this.getSequence()).getStringValueExpression();
                stringValueExpression = stringValueExpression.typeCheck(visitor, contextItemType);
                this.setSequence(stringValueExpression);
                this.requiredType = SequenceType.SINGLE_UNTYPED_ATOMIC;
                this.adoptChildExpression(this.getSequence());
                this.refineTypeInformation(this.requiredType.getPrimaryType(), this.requiredType.getCardinality(), null, 0, this);
            }
        }
        if (this.getSequence().hasSpecialProperty(0x2000000)) {
            this.needsEagerEvaluation = true;
        }
        this.hasLoopingReference |= this.removeDeadReferences();
        if (!this.needsEagerEvaluation) {
            boolean considerRemoval;
            boolean bl = considerRemoval = (this.references != null && this.references.size() < 2 || this.getSequence() instanceof VariableReference) && !this.isIndexedVariable && !this.hasLoopingReference && !this.needsEagerEvaluation;
            if (considerRemoval) {
                this.verifyReferences();
                boolean bl2 = considerRemoval = this.references != null;
            }
            if (considerRemoval && this.references.isEmpty()) {
                this.getActionOp().optimize(visitor, contextItemType);
                opt.trace("Eliminated unused variable " + this.getVariableName(), this.getAction());
                return this.getAction();
            }
            if (considerRemoval && this.references.size() == 1 && ExpressionTool.dependsOnFocus(this.getSequence())) {
                if (visitor.isOptimizeForStreaming()) {
                    considerRemoval = false;
                }
                Expression child = (Expression)this.references.get(0);
                Expression parent = child.getParentExpression();
                while (parent != null && parent != this) {
                    Operand operand = ExpressionTool.findOperand(parent, child);
                    assert (operand != null);
                    if (!operand.hasSameFocus()) {
                        considerRemoval = false;
                        break;
                    }
                    child = parent;
                    parent = child.getParentExpression();
                }
            }
            if (considerRemoval && this.references.size() == 1) {
                if (ExpressionTool.changesXsltContext(this.getSequence())) {
                    considerRemoval = false;
                } else if ((this.getSequence().getDependencies() & 0x20) != 0) {
                    considerRemoval = false;
                } else if (((VariableReference)this.references.get(0)).isInLoop()) {
                    considerRemoval = false;
                }
            }
            if (considerRemoval && (this.references.size() == 1 || this.getSequence() instanceof Literal || this.getSequence() instanceof VariableReference) && opt.isOptionSet(4)) {
                this.inlineReferences();
                opt.trace("Inlined references to $" + this.getVariableName(), this.getAction());
                this.references = null;
                return this.getAction().optimize(visitor, contextItemType);
            }
        }
        int tries = 0;
        while (tries++ < 5) {
            Expression seq0 = this.getSequence();
            this.getSequenceOp().optimize(visitor, contextItemType);
            if (this.getSequence() instanceof Literal && !this.isIndexedVariable) {
                return this.optimize(visitor, contextItemType);
            }
            if (seq0 != this.getSequence()) continue;
            break;
        }
        tries = 0;
        while (tries++ < 5) {
            Expression act0 = this.getAction();
            this.getActionOp().optimize(visitor, contextItemType);
            if (act0 == this.getAction()) break;
            if (this.isIndexedVariable || this.needsEagerEvaluation) continue;
            this.verifyReferences();
            if (this.references == null || this.references.size() >= 2) continue;
            if (this.references.isEmpty()) {
                this.hasLoopingReference = false;
                return this.optimize(visitor, contextItemType);
            }
            if (((VariableReference)this.references.get(0)).isInLoop()) continue;
            return this.optimize(visitor, contextItemType);
        }
        this.setEvaluator();
        return this;
    }

    public void setEvaluator() {
        if (this.needsEagerEvaluation) {
            this.setEvaluator(ExpressionTool.eagerEvaluator(this.getSequence()));
        } else if (this.isIndexedVariable()) {
            this.setEvaluator(Evaluator.MAKE_INDEXED_VARIABLE);
        } else if (this.evaluator == null) {
            this.setEvaluator(ExpressionTool.lazyEvaluator(this.getSequence(), this.getNominalReferenceCount() > 1));
        }
    }

    private void inlineReferences() {
        for (VariableReference ref : this.references) {
            Expression parent = ref.getParentExpression();
            if (parent == null) continue;
            Operand o = ExpressionTool.findOperand(parent, ref);
            if (o != null) {
                o.setChildExpression(this.getSequence().copy(new RebindingMap()));
            }
            ExpressionTool.resetStaticProperties(parent);
        }
    }

    @Override
    public double getCost() {
        return this.getSequence().getCost() + this.getAction().getCost();
    }

    private boolean allReferencesAreFlattened() {
        return this.references != null && this.references.stream().allMatch(VariableReference::isFlattened);
    }

    @Override
    public boolean isVacuousExpression() {
        return this.getAction().isVacuousExpression();
    }

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        this.getAction().checkPermittedContents(parentType, whole);
    }

    @Override
    public IntegerValue[] getIntegerBounds() {
        return this.getAction().getIntegerBounds();
    }

    @Override
    public int getImplementationMethod() {
        return this.getAction().getImplementationMethod();
    }

    @Override
    public void gatherProperties(BiConsumer<String, Object> consumer) {
        consumer.accept("name", this.getVariableQName());
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        LetExpression let = this;
        while (true) {
            Sequence val = let.eval(context);
            context.setLocalVariable(let.getLocalSlotNumber(), val);
            if (!(let.getAction() instanceof LetExpression)) break;
            let = (LetExpression)let.getAction();
        }
        return let.getAction().iterate(context);
    }

    public Sequence eval(XPathContext context) throws XPathException {
        if (this.evaluator == null) {
            this.setEvaluator(ExpressionTool.lazyEvaluator(this.getSequence(), this.getNominalReferenceCount() > 1));
        }
        try {
            int savedOutputState = context.getTemporaryOutputState();
            context.setTemporaryOutputState(206);
            Sequence result = this.evaluator.evaluate(this.getSequence(), context);
            context.setTemporaryOutputState(savedOutputState);
            return result;
        } catch (ClassCastException e) {
            assert (false);
            int savedOutputState = context.getTemporaryOutputState();
            context.setTemporaryOutputState(206);
            Sequence result = Evaluator.EAGER_SEQUENCE.evaluate(this.getSequence(), context);
            context.setTemporaryOutputState(savedOutputState);
            return result;
        }
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        LetExpression let = this;
        while (true) {
            Sequence val = let.eval(context);
            context.setLocalVariable(let.getLocalSlotNumber(), val);
            if (!(let.getAction() instanceof LetExpression)) break;
            let = (LetExpression)let.getAction();
        }
        return let.getAction().evaluateItem(context);
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        LetExpression let = this;
        while (true) {
            Sequence val = let.eval(context);
            context.setLocalVariable(let.getLocalSlotNumber(), val);
            if (!(let.getAction() instanceof LetExpression)) break;
            let = (LetExpression)let.getAction();
        }
        return let.getAction().effectiveBooleanValue(context);
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        LetExpression let = this;
        while (true) {
            Sequence val = let.eval(context);
            context.setLocalVariable(let.getLocalSlotNumber(), val);
            if (!(let.getAction() instanceof LetExpression)) break;
            let = (LetExpression)let.getAction();
        }
        let.getAction().process(output, context);
    }

    @Override
    public ItemType getItemType() {
        return this.getAction().getItemType();
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        if (this.isInstruction()) {
            return UType.ANY;
        }
        return this.getAction().getStaticUType(contextItemType);
    }

    @Override
    public int computeCardinality() {
        return this.getAction().getCardinality();
    }

    @Override
    public int computeSpecialProperties() {
        int props = this.getAction().getSpecialProperties();
        int seqProps = this.getSequence().getSpecialProperties();
        if ((seqProps & 0x800000) == 0) {
            props &= 0xFF7FFFFF;
        }
        return props;
    }

    @Override
    public int markTailFunctionCalls(StructuredQName qName, int arity) {
        return ExpressionTool.markTailFunctionCalls(this.getAction(), qName, arity);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        LetExpression let = new LetExpression();
        rebindings.put(this, let);
        let.isIndexedVariable = this.isIndexedVariable;
        let.hasLoopingReference = this.hasLoopingReference;
        let.setNeedsEagerEvaluation(this.needsEagerEvaluation);
        let.setNeedsLazyEvaluation(this.needsLazyEvaluation);
        let.setVariableQName(this.variableName);
        let.setRequiredType(this.requiredType);
        let.setSequence(this.getSequence().copy(rebindings));
        let.setInstruction(this.isInstruction());
        ExpressionTool.copyLocationInfo(this, let);
        Expression newAction = this.getAction().copy(rebindings);
        let.setAction(newAction);
        return let;
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        LetExpression let = this;
        while (true) {
            Sequence val = let.eval(context);
            context.setLocalVariable(let.getLocalSlotNumber(), val);
            if (!(let.getAction() instanceof LetExpression)) break;
            let = (LetExpression)let.getAction();
        }
        if (let.getAction() instanceof TailCallReturner) {
            return ((TailCallReturner)((Object)let.getAction())).processLeavingTail(output, context);
        }
        let.getAction().process(output, context);
        return null;
    }

    @Override
    public void evaluatePendingUpdates(XPathContext context, PendingUpdateList pul) throws XPathException {
        LetExpression let = this;
        while (true) {
            Sequence val = let.eval(context);
            context.setLocalVariable(let.getLocalSlotNumber(), val);
            if (!(let.getAction() instanceof LetExpression)) break;
            let = (LetExpression)let.getAction();
        }
        let.getAction().evaluatePendingUpdates(context, pul);
    }

    @Override
    public String toString() {
        return "let $" + this.getVariableEQName() + " := " + this.getSequence() + " return " + ExpressionTool.parenthesize(this.getAction());
    }

    @Override
    public String toShortString() {
        return "let $" + this.getVariableName() + " := ...";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("let", this);
        out.emitAttribute("var", this.variableName);
        if (this.getRequiredType() != SequenceType.ANY_SEQUENCE) {
            out.emitAttribute("as", this.getRequiredType().toAlphaCode());
        }
        if (this.isIndexedVariable()) {
            out.emitAttribute("indexable", "true");
        }
        out.emitAttribute("slot", this.getLocalSlotNumber() + "");
        if (this.evaluator == null) {
            this.setEvaluator(ExpressionTool.lazyEvaluator(this.getSequence(), this.getNominalReferenceCount() > 1));
        }
        out.emitAttribute("eval", this.getEvaluator().getEvaluationMode().getCode() + "");
        this.getSequence().export(out);
        this.getAction().export(out);
        out.endElement();
    }

    public void setEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    public void setEvaluationMode(EvaluationMode mode) {
        this.evaluator = mode.getEvaluator();
    }

    public Evaluator getEvaluator() {
        return this.evaluator;
    }
}

