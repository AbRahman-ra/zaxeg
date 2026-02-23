/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public abstract class Assignation
extends Expression
implements LocalBinding {
    private Operand sequenceOp = new Operand(this, null, OperandRole.NAVIGATE);
    private Operand actionOp = new Operand(this, null, this instanceof LetExpression ? OperandRole.SAME_FOCUS_ACTION : REPEATED_ACTION_ROLE);
    protected int slotNumber = -999;
    protected StructuredQName variableName;
    protected SequenceType requiredType;
    protected boolean isIndexedVariable = false;
    protected boolean hasLoopingReference = false;
    protected List<VariableReference> references = null;
    private static final OperandRole REPEATED_ACTION_ROLE = new OperandRole(4, OperandUsage.TRANSMISSION);

    public Operand getSequenceOp() {
        return this.sequenceOp;
    }

    public Operand getActionOp() {
        return this.actionOp;
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandList(this.sequenceOp, this.actionOp);
    }

    public void setRequiredType(SequenceType requiredType) {
        this.requiredType = requiredType;
    }

    public void setVariableQName(StructuredQName variableName) {
        this.variableName = variableName;
    }

    @Override
    public StructuredQName getVariableQName() {
        return this.variableName;
    }

    @Override
    public StructuredQName getObjectName() {
        return this.variableName;
    }

    @Override
    public SequenceType getRequiredType() {
        return this.requiredType;
    }

    @Override
    public IntegerValue[] getIntegerBoundsForVariable() {
        return this.getSequence().getIntegerBounds();
    }

    @Override
    public int getLocalSlotNumber() {
        return this.slotNumber;
    }

    @Override
    public int computeDependencies() {
        int d = super.computeDependencies();
        if (!ExpressionTool.containsLocalVariableReference(this)) {
            d &= 0xFFFFFF7F;
        }
        return d;
    }

    @Override
    public Sequence evaluateVariable(XPathContext context) throws XPathException {
        Sequence actual = context.evaluateLocalVariable(this.slotNumber);
        if (!(actual instanceof GroundedValue) && !(actual instanceof NodeInfo)) {
            actual = actual.materialize();
            context.setLocalVariable(this.slotNumber, actual);
        }
        return actual;
    }

    public void setAction(Expression action) {
        this.actionOp.setChildExpression(action);
    }

    @Override
    public final boolean isGlobal() {
        return false;
    }

    @Override
    public final boolean isAssignable() {
        return false;
    }

    @Override
    public void checkForUpdatingSubexpressions() throws XPathException {
        this.getSequence().checkForUpdatingSubexpressions();
        if (this.getSequence().isUpdatingExpression()) {
            XPathException err = new XPathException("An updating expression cannot be used to initialize a variable", "XUST0001");
            err.setLocator(this.getSequence().getLocation());
            throw err;
        }
        this.getAction().checkForUpdatingSubexpressions();
    }

    @Override
    public boolean isUpdatingExpression() {
        return this.getAction().isUpdatingExpression();
    }

    public Expression getAction() {
        return this.actionOp.getChildExpression();
    }

    public void setSequence(Expression sequence) {
        this.sequenceOp.setChildExpression(sequence);
    }

    public Expression getSequence() {
        return this.sequenceOp.getChildExpression();
    }

    public void setSlotNumber(int nr) {
        this.slotNumber = nr;
    }

    public int getRequiredSlots() {
        return 1;
    }

    @Override
    public boolean hasVariableBinding(Binding binding) {
        return this == binding;
    }

    @Override
    public Expression unordered(boolean retainAllNodes, boolean forStreaming) throws XPathException {
        this.setAction(this.getAction().unordered(retainAllNodes, forStreaming));
        return this;
    }

    @Override
    public double getCost() {
        return this.getSequence().getCost() + 5.0 * this.getAction().getCost();
    }

    @Override
    public void suppressValidation(int validationMode) {
        this.getAction().suppressValidation(validationMode);
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        PathMap.PathMapNodeSet varPath = this.getSequence().addToPathMap(pathMap, pathMapNodeSet);
        pathMap.registerPathForVariable(this, varPath);
        return this.getAction().addToPathMap(pathMap, pathMapNodeSet);
    }

    public String getVariableName() {
        if (this.variableName == null) {
            return "zz:var" + this.computeHashCode();
        }
        return this.variableName.getDisplayName();
    }

    public String getVariableEQName() {
        if (this.variableName == null) {
            return "Q{http://ns.saxonica.com/anonymous-var}var" + this.computeHashCode();
        }
        if (this.variableName.hasURI("")) {
            return this.variableName.getLocalPart();
        }
        return this.variableName.getEQName();
    }

    public void refineTypeInformation(ItemType type, int cardinality, GroundedValue constantValue, int properties, Assignation currentExpression) throws XPathException {
        ExpressionTool.processExpressionTree(currentExpression.getAction(), null, (exp, result) -> {
            if (exp instanceof VariableReference && ((VariableReference)exp).getBinding() == currentExpression) {
                ((VariableReference)exp).refineVariableType(type, cardinality, constantValue, properties);
            }
            return false;
        });
    }

    @Override
    public void addReference(VariableReference ref, boolean isLoopingReference) {
        this.hasLoopingReference |= isLoopingReference;
        if (this.references == null) {
            this.references = new ArrayList<VariableReference>();
        }
        for (VariableReference vr : this.references) {
            if (vr != ref) continue;
            return;
        }
        this.references.add(ref);
    }

    public int getNominalReferenceCount() {
        if (this.isIndexedVariable) {
            return 10000;
        }
        if (this.references == null || this.hasLoopingReference) {
            return 10;
        }
        return this.references.size();
    }

    protected boolean removeDeadReferences() {
        boolean inLoop = false;
        if (this.references != null) {
            for (int i = this.references.size() - 1; i >= 0; --i) {
                boolean found = false;
                inLoop |= this.references.get(i).isInLoop();
                for (Expression parent = this.references.get(i).getParentExpression(); parent != null; parent = parent.getParentExpression()) {
                    if (parent != this) continue;
                    found = true;
                    break;
                }
                if (found) continue;
                this.references.remove(i);
            }
        }
        return inLoop;
    }

    protected void verifyReferences() {
        this.rebuildReferenceList(false);
    }

    public void rebuildReferenceList(boolean force) {
        int[] results = new int[]{0, force ? Integer.MAX_VALUE : 500};
        ArrayList<VariableReference> references = new ArrayList<VariableReference>();
        Assignation.countReferences(this, this.getAction(), references, results);
        this.references = results[1] <= 0 ? null : references;
    }

    private static void countReferences(Binding binding, Expression exp, List<VariableReference> references, int[] results) {
        if (exp instanceof LocalVariableReference) {
            LocalVariableReference ref = (LocalVariableReference)exp;
            if (ref.getBinding() == binding) {
                ref.recomputeInLoop();
                results[0] = results[0] + (ref.isInLoop() ? 10 : 1);
                references.add((LocalVariableReference)exp);
            }
        } else if ((exp.getDependencies() & 0x80) != 0) {
            results[1] = results[1] - 1;
            if (results[1] <= 0) {
                results[0] = 100;
                results[1] = 0;
            } else {
                for (Operand o : exp.operands()) {
                    Assignation.countReferences(binding, o.getChildExpression(), references, results);
                }
            }
        }
    }

    @Override
    public boolean isIndexedVariable() {
        return this.isIndexedVariable;
    }

    public boolean replaceVariable(Expression seq) {
        Binding newBinding;
        boolean done = ExpressionTool.inlineVariableReferences(this.getAction(), this, seq);
        if (done && this.isIndexedVariable() && seq instanceof VariableReference && (newBinding = ((VariableReference)seq).getBinding()) instanceof Assignation) {
            ((Assignation)newBinding).setIndexedVariable();
        }
        return done;
    }

    @Override
    public void setIndexedVariable() {
        this.isIndexedVariable = true;
    }
}

