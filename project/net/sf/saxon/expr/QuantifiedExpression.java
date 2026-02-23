/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Assignation;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.functions.BooleanFn;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;

public class QuantifiedExpression
extends Assignation {
    private int operator;

    @Override
    public String getExpressionName() {
        return Token.tokens[this.operator];
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    public int getOperator() {
        return this.operator;
    }

    @Override
    public int computeCardinality() {
        return 16384;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getSequenceOp().typeCheck(visitor, contextInfo);
        if (Literal.isEmptySequence(this.getSequence())) {
            return Literal.makeLiteral(BooleanValue.get(this.operator != 32), this);
        }
        this.setSequence(this.getSequence().unordered(false, false));
        SequenceType decl = this.getRequiredType();
        if (decl.getCardinality() == 8192) {
            XPathException err = new XPathException("Range variable will never satisfy the type empty-sequence()", "XPTY0004");
            err.setIsTypeError(true);
            err.setLocation(this.getLocation());
            throw err;
        }
        SequenceType sequenceType = SequenceType.makeSequenceType(decl.getPrimaryType(), 57344);
        RoleDiagnostic role = new RoleDiagnostic(3, this.getVariableQName().getDisplayName(), 0);
        this.setSequence(TypeChecker.strictTypeCheck(this.getSequence(), sequenceType, role, visitor.getStaticContext()));
        ItemType actualItemType = this.getSequence().getItemType();
        this.refineTypeInformation(actualItemType, 16384, null, this.getSequence().getSpecialProperties(), this);
        this.getActionOp().typeCheck(visitor, contextInfo);
        XPathException err = TypeChecker.ebvError(this.getAction(), visitor.getConfiguration().getTypeHierarchy());
        if (err != null) {
            err.setLocation(this.getLocation());
            throw err;
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression e3;
        this.getSequenceOp().optimize(visitor, contextItemType);
        this.getActionOp().optimize(visitor, contextItemType);
        Expression ebv = BooleanFn.rewriteEffectiveBooleanValue(this.getAction(), visitor, contextItemType);
        if (ebv != null) {
            this.setAction(ebv);
            this.adoptChildExpression(ebv);
        }
        if (Literal.hasEffectiveBooleanValue(ebv, true)) {
            if (this.getOperator() == 32) {
                return SystemFunction.makeCall("exists", this.getRetainedStaticContext(), this.getSequence());
            }
            Literal e2 = new Literal(BooleanValue.TRUE);
            ExpressionTool.copyLocationInfo(this, e2);
            return e2;
        }
        if (Literal.hasEffectiveBooleanValue(ebv, false)) {
            if (this.getOperator() == 32) {
                Literal e2 = new Literal(BooleanValue.FALSE);
                ExpressionTool.copyLocationInfo(this, e2);
                return e2;
            }
            return SystemFunction.makeCall("empty", this.getRetainedStaticContext(), this.getSequence());
        }
        if (this.getSequence() instanceof Literal) {
            GroundedValue seq = ((Literal)this.getSequence()).getValue();
            int len = seq.getLength();
            if (len == 0) {
                Literal e2 = new Literal(BooleanValue.get(this.getOperator() == 33));
                ExpressionTool.copyLocationInfo(this, e2);
                return e2;
            }
            if (len == 1) {
                if (this.getAction() instanceof VariableReference && ((VariableReference)this.getAction()).getBinding() == this) {
                    return SystemFunction.makeCall("boolean", this.getRetainedStaticContext(), this.getSequence());
                }
                this.replaceVariable(this.getSequence());
                return this.getAction();
            }
        }
        if (visitor.isOptimizeForStreaming() && (e3 = visitor.obtainOptimizer().optimizeQuantifiedExpressionForStreaming(this)) != null && e3 != this) {
            return e3.optimize(visitor, contextItemType);
        }
        return this;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public void checkForUpdatingSubexpressions() throws XPathException {
        this.getSequence().checkForUpdatingSubexpressions();
        this.getAction().checkForUpdatingSubexpressions();
    }

    @Override
    public boolean isUpdatingExpression() {
        return false;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        QuantifiedExpression qe = new QuantifiedExpression();
        ExpressionTool.copyLocationInfo(this, qe);
        qe.setOperator(this.operator);
        qe.setVariableQName(this.variableName);
        qe.setRequiredType(this.requiredType);
        qe.setSequence(this.getSequence().copy(rebindings));
        rebindings.put(this, qe);
        Expression newAction = this.getAction().copy(rebindings);
        qe.setAction(newAction);
        qe.variableName = this.variableName;
        qe.slotNumber = this.slotNumber;
        return qe;
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        return p | 0x800000;
    }

    @Override
    public BooleanValue evaluateItem(XPathContext context) throws XPathException {
        return BooleanValue.get(this.effectiveBooleanValue(context));
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        Item it;
        SequenceIterator base = this.getSequence().iterate(context);
        boolean some = this.operator == 32;
        int slot = this.getLocalSlotNumber();
        while ((it = base.next()) != null) {
            context.setLocalVariable(slot, it);
            if (some != this.getAction().effectiveBooleanValue(context)) continue;
            base.close();
            return some;
        }
        return !some;
    }

    @Override
    public ItemType getItemType() {
        return BuiltInAtomicType.BOOLEAN;
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return UType.BOOLEAN;
    }

    @Override
    public String toString() {
        return (this.operator == 32 ? "some" : "every") + " $" + this.getVariableEQName() + " in " + this.getSequence() + " satisfies " + ExpressionTool.parenthesize(this.getAction());
    }

    @Override
    public String toShortString() {
        return (this.operator == 32 ? "some" : "every") + " $" + this.getVariableName() + " in " + this.getSequence().toShortString() + " satisfies ...";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement(Token.tokens[this.operator], this);
        out.emitAttribute("var", this.getVariableQName());
        out.emitAttribute("slot", "" + this.slotNumber);
        this.getSequence().export(out);
        this.getAction().export(out);
        out.endElement();
    }
}

