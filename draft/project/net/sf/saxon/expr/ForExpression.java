/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.ArrayList;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.AndExpression;
import net.sf.saxon.expr.Assignation;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.MappingFunction;
import net.sf.saxon.expr.MappingIterator;
import net.sf.saxon.expr.PendingUpdateList;
import net.sf.saxon.expr.SlashExpression;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.OuterForExpression;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public class ForExpression
extends Assignation {
    private int actionCardinality = 32768;

    @Override
    public String getExpressionName() {
        return "for";
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getSequenceOp().typeCheck(visitor, contextInfo);
        if (Literal.isEmptySequence(this.getSequence()) && !(this instanceof OuterForExpression)) {
            return this.getSequence();
        }
        if (this.requiredType != null) {
            SequenceType decl = this.requiredType;
            SequenceType sequenceType = SequenceType.makeSequenceType(decl.getPrimaryType(), 57344);
            RoleDiagnostic role = new RoleDiagnostic(3, this.variableName.getDisplayName(), 0);
            this.setSequence(TypeChecker.strictTypeCheck(this.getSequence(), sequenceType, role, visitor.getStaticContext()));
            ItemType actualItemType = this.getSequence().getItemType();
            this.refineTypeInformation(actualItemType, this.getRangeVariableCardinality(), null, this.getSequence().getSpecialProperties(), this);
        }
        if (Literal.isEmptySequence(this.getAction())) {
            return this.getAction();
        }
        this.getActionOp().typeCheck(visitor, contextInfo);
        this.actionCardinality = this.getAction().getCardinality();
        return this;
    }

    protected int getRangeVariableCardinality() {
        return 16384;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression p;
        Configuration config = visitor.getConfiguration();
        Optimizer opt = visitor.obtainOptimizer();
        boolean debug = config.getBooleanProperty(Feature.TRACE_OPTIMIZER_DECISIONS);
        if (Choose.isSingleBranchChoice(this.getAction())) {
            this.getActionOp().optimize(visitor, contextItemType);
        }
        if ((p = this.promoteWhereClause()) != null) {
            if (debug) {
                opt.trace("Promoted where clause in for $" + this.getVariableName(), p);
            }
            return p.optimize(visitor, contextItemType);
        }
        Expression seq0 = this.getSequence();
        this.getSequenceOp().optimize(visitor, contextItemType);
        if (seq0 != this.getSequence()) {
            return this.optimize(visitor, contextItemType);
        }
        if (Literal.isEmptySequence(this.getSequence()) && !(this instanceof OuterForExpression)) {
            return this.getSequence();
        }
        Expression act0 = this.getAction();
        this.getActionOp().optimize(visitor, contextItemType);
        if (act0 != this.getAction()) {
            return this.optimize(visitor, contextItemType);
        }
        if (Literal.isEmptySequence(this.getAction())) {
            return this.getAction();
        }
        if (this.getSequence() instanceof SlashExpression && this.getAction() instanceof SlashExpression) {
            SlashExpression path2 = (SlashExpression)this.getAction();
            Expression start2 = path2.getSelectExpression();
            Expression step2 = path2.getActionExpression();
            if (start2 instanceof VariableReference && ((VariableReference)start2).getBinding() == this && ExpressionTool.getReferenceCount(this.getAction(), this, false) == 1 && (step2.getDependencies() & 0xC) == 0) {
                Expression newPath = new SlashExpression(this.getSequence(), path2.getActionExpression());
                ExpressionTool.copyLocationInfo(this, newPath);
                newPath = newPath.simplify().typeCheck(visitor, contextItemType);
                if (newPath instanceof SlashExpression) {
                    if (debug) {
                        opt.trace("Collapsed return clause of for $" + this.getVariableName() + " into path expression", newPath);
                    }
                    return newPath.optimize(visitor, contextItemType);
                }
            }
        }
        if (this.getAction() instanceof VariableReference && ((VariableReference)this.getAction()).getBinding() == this) {
            if (debug) {
                opt.trace("Collapsed redundant for expression $" + this.getVariableName(), this.getSequence());
            }
            return this.getSequence();
        }
        if (this.getSequence().getCardinality() == 16384) {
            LetExpression let = new LetExpression();
            let.setVariableQName(this.variableName);
            let.setRequiredType(SequenceType.makeSequenceType(this.getSequence().getItemType(), 16384));
            let.setSequence(this.getSequence());
            let.setAction(this.getAction());
            let.setSlotNumber(this.slotNumber);
            let.setRetainedStaticContextLocally(this.getRetainedStaticContext());
            ExpressionTool.rebindVariableReferences(this.getAction(), this, let);
            return let.typeCheck(visitor, contextItemType).optimize(visitor, contextItemType);
        }
        return this;
    }

    @Override
    public Expression unordered(boolean retainAllNodes, boolean forStreaming) throws XPathException {
        this.setSequence(this.getSequence().unordered(retainAllNodes, forStreaming));
        this.setAction(this.getAction().unordered(retainAllNodes, forStreaming));
        return this;
    }

    @Override
    public IntegerValue[] getIntegerBounds() {
        return this.getAction().getIntegerBounds();
    }

    private Expression promoteWhereClause() {
        if (Choose.isSingleBranchChoice(this.getAction())) {
            Expression condition = ((Choose)this.getAction()).getCondition(0);
            Binding[] bindingList = new Binding[]{this};
            ArrayList<Expression> list = new ArrayList<Expression>(5);
            Expression promotedCondition = null;
            BooleanExpression.listAndComponents(condition, list);
            for (int i = list.size() - 1; i >= 0; --i) {
                Expression term = (Expression)list.get(i);
                if (ExpressionTool.dependsOnVariable(term, bindingList)) continue;
                promotedCondition = promotedCondition == null ? term : new AndExpression(term, promotedCondition);
                list.remove(i);
            }
            if (promotedCondition != null) {
                if (list.isEmpty()) {
                    Expression oldThen = ((Choose)this.getAction()).getAction(0);
                    this.setAction(oldThen);
                    return Choose.makeConditional(promotedCondition, this);
                }
                Expression retainedCondition = (Expression)list.get(0);
                for (int i = 1; i < list.size(); ++i) {
                    retainedCondition = new AndExpression(retainedCondition, (Expression)list.get(i));
                }
                ((Choose)this.getAction()).setCondition(0, retainedCondition);
                Expression newIf = Choose.makeConditional(promotedCondition, this, Literal.makeEmptySequence());
                ExpressionTool.copyLocationInfo(this, newIf);
                return newIf;
            }
        }
        return null;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ForExpression forExp = new ForExpression();
        ExpressionTool.copyLocationInfo(this, forExp);
        forExp.setRequiredType(this.requiredType);
        forExp.setVariableQName(this.variableName);
        forExp.setSequence(this.getSequence().copy(rebindings));
        rebindings.put(this, forExp);
        Expression newAction = this.getAction().copy(rebindings);
        forExp.setAction(newAction);
        forExp.variableName = this.variableName;
        forExp.slotNumber = this.slotNumber;
        return forExp;
    }

    @Override
    public int markTailFunctionCalls(StructuredQName qName, int arity) {
        if (!Cardinality.allowsMany(this.getSequence().getCardinality())) {
            return ExpressionTool.markTailFunctionCalls(this.getAction(), qName, arity);
        }
        return 0;
    }

    @Override
    public boolean isVacuousExpression() {
        return this.getAction().isVacuousExpression();
    }

    @Override
    public int getImplementationMethod() {
        return 6;
    }

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        this.getAction().checkPermittedContents(parentType, false);
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        SequenceIterator base = this.getSequence().iterate(context);
        MappingAction map = new MappingAction(context, this.getLocalSlotNumber(), this.getAction());
        switch (this.actionCardinality) {
            case 16384: {
                return new ItemMappingIterator(base, map, true);
            }
            case 24576: {
                return new ItemMappingIterator(base, map, false);
            }
        }
        return new MappingIterator(base, map);
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        int slot = this.getLocalSlotNumber();
        this.getSequence().iterate(context).forEachOrFail(item -> {
            context.setLocalVariable(slot, item);
            this.getAction().process(output, context);
        });
    }

    @Override
    public void evaluatePendingUpdates(XPathContext context, PendingUpdateList pul) throws XPathException {
        int slot = this.getLocalSlotNumber();
        this.getSequence().iterate(context).forEachOrFail(item -> {
            context.setLocalVariable(slot, item);
            this.getAction().evaluatePendingUpdates(context, pul);
        });
    }

    @Override
    public ItemType getItemType() {
        return this.getAction().getItemType();
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return this.getAction().getStaticUType(contextItemType);
    }

    @Override
    public int computeCardinality() {
        int c1 = this.getSequence().getCardinality();
        int c2 = this.getAction().getCardinality();
        return Cardinality.multiply(c1, c2);
    }

    @Override
    public String toString() {
        return "for $" + this.getVariableEQName() + " in " + (this.getSequence() == null ? "(...)" : this.getSequence().toString()) + " return " + (this.getAction() == null ? "(...)" : ExpressionTool.parenthesize(this.getAction()));
    }

    @Override
    public String toShortString() {
        return "for $" + this.getVariableQName().getDisplayName() + " in " + (this.getSequence() == null ? "(...)" : this.getSequence().toShortString()) + " return " + (this.getAction() == null ? "(...)" : this.getAction().toShortString());
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("for", this);
        this.explainSpecializedAttributes(out);
        out.emitAttribute("var", this.getVariableQName());
        ItemType varType = this.getSequence().getItemType();
        if (varType != AnyItemType.getInstance()) {
            out.emitAttribute("as", AlphaCode.fromItemType(varType));
        }
        out.emitAttribute("slot", "" + this.getLocalSlotNumber());
        out.setChildRole("in");
        this.getSequence().export(out);
        out.setChildRole("return");
        this.getAction().export(out);
        out.endElement();
    }

    protected void explainSpecializedAttributes(ExpressionPresenter out) {
    }

    @Override
    public String getStreamerName() {
        return "ForExpression";
    }

    public static class MappingAction
    implements MappingFunction,
    ItemMappingFunction {
        protected XPathContext context;
        private int slotNumber;
        private Expression action;

        public MappingAction(XPathContext context, int slotNumber, Expression action) {
            this.context = context;
            this.slotNumber = slotNumber;
            this.action = action;
        }

        @Override
        public SequenceIterator map(Item item) throws XPathException {
            this.context.setLocalVariable(this.slotNumber, item);
            return this.action.iterate(this.context);
        }

        @Override
        public Item mapItem(Item item) throws XPathException {
            this.context.setLocalVariable(this.slotNumber, item);
            return this.action.evaluateItem(this.context);
        }
    }
}

