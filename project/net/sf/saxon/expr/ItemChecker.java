/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.ArrayList;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.TypeCheckingFilter;
import net.sf.saxon.expr.CardinalityChecker;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.ItemTypeCheckingFunction;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.CombinedNodeTest;
import net.sf.saxon.pattern.DocumentNodeTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.IntegerValue;

public final class ItemChecker
extends UnaryExpression {
    private ItemType requiredItemType;
    private RoleDiagnostic role;

    public ItemChecker(Expression sequence, ItemType itemType, RoleDiagnostic role) {
        super(sequence);
        this.requiredItemType = itemType;
        this.role = role;
    }

    public ItemType getRequiredType() {
        return this.requiredItemType;
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.SAME_FOCUS_ACTION;
    }

    public RoleDiagnostic getRoleLocator() {
        return this.role;
    }

    @Override
    public Expression simplify() throws XPathException {
        Expression operand = this.getBaseExpression().simplify();
        if (this.requiredItemType instanceof AnyItemType) {
            return operand;
        }
        this.setBaseExpression(operand);
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().typeCheck(visitor, contextInfo);
        Expression operand = this.getBaseExpression();
        if (operand instanceof Block) {
            Block block = (Block)operand;
            ArrayList<ItemChecker> checkedOperands = new ArrayList<ItemChecker>();
            for (Operand o : block.operands()) {
                ItemChecker checkedOp = new ItemChecker(o.getChildExpression(), this.requiredItemType, this.role);
                checkedOperands.add(checkedOp);
            }
            Block newBlock = new Block(checkedOperands.toArray(new Expression[0]));
            ExpressionTool.copyLocationInfo(this, newBlock);
            return newBlock.typeCheck(visitor, contextInfo);
        }
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        int card = operand.getCardinality();
        if (card == 8192) {
            return operand;
        }
        ItemType supplied = operand.getItemType();
        Affinity relation = th.relationship(this.requiredItemType, supplied);
        if (relation == Affinity.SAME_TYPE || relation == Affinity.SUBSUMES) {
            return operand;
        }
        if (relation == Affinity.DISJOINT) {
            if (this.requiredItemType.equals(BuiltInAtomicType.STRING) && th.isSubType(supplied, BuiltInAtomicType.ANY_URI)) {
                return operand;
            }
            if (Cardinality.allowsZero(card)) {
                if (!(operand instanceof Literal)) {
                    String message = this.role.composeErrorMessage(this.requiredItemType, operand, th);
                    visitor.getStaticContext().issueWarning("The only value that can pass type-checking is an empty sequence. " + message, this.getLocation());
                }
            } else {
                String message = this.role.composeErrorMessage(this.requiredItemType, operand, th);
                XPathException err = new XPathException(message);
                err.setErrorCode(this.role.getErrorCode());
                err.setLocation(this.getLocation());
                err.setIsTypeError(this.role.isTypeError());
                throw err;
            }
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().optimize(visitor, contextInfo);
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        Affinity rel = th.relationship(this.requiredItemType, this.getBaseExpression().getItemType());
        if (rel == Affinity.SAME_TYPE || rel == Affinity.SUBSUMES) {
            return this.getBaseExpression();
        }
        return this;
    }

    @Override
    public int getImplementationMethod() {
        int m = 22;
        if (!Cardinality.allowsMany(this.getCardinality())) {
            m |= 1;
        }
        return m;
    }

    @Override
    public String getStreamerName() {
        return "ItemChecker";
    }

    @Override
    public IntegerValue[] getIntegerBounds() {
        return this.getBaseExpression().getIntegerBounds();
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        SequenceIterator base = this.getBaseExpression().iterate(context);
        return new ItemMappingIterator(base, this.getMappingFunction(context), true);
    }

    public ItemMappingFunction getMappingFunction(XPathContext context) {
        return new ItemTypeCheckingFunction(this.requiredItemType, this.role, this.getBaseExpression(), context.getConfiguration());
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        TypeHierarchy th = context.getConfiguration().getTypeHierarchy();
        Item item = this.getBaseExpression().evaluateItem(context);
        if (item == null) {
            return null;
        }
        if (this.requiredItemType.matches(item, th)) {
            return item;
        }
        if (this.requiredItemType.getUType().subsumes(UType.STRING) && BuiltInAtomicType.ANY_URI.matches(item, th)) {
            return item;
        }
        String message = this.role.composeErrorMessage(this.requiredItemType, item, th);
        String errorCode = this.role.getErrorCode();
        if ("XPDY0050".equals(errorCode)) {
            this.dynamicError(message, errorCode, context);
        } else {
            this.typeError(message, errorCode, context);
        }
        return null;
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        Expression next = this.getBaseExpression();
        int card = 57344;
        if (next instanceof CardinalityChecker) {
            card = ((CardinalityChecker)next).getRequiredCardinality();
            next = ((CardinalityChecker)next).getBaseExpression();
        }
        if ((next.getImplementationMethod() & 4) != 0 && !(this.requiredItemType instanceof DocumentNodeTest)) {
            TypeCheckingFilter filter = new TypeCheckingFilter(output);
            filter.setRequiredType(this.requiredItemType, card, this.role, this.getLocation());
            next.process(filter, context);
            filter.finalCheck();
        } else {
            super.process(output, context);
        }
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ItemChecker exp = new ItemChecker(this.getBaseExpression().copy(rebindings), this.requiredItemType, this.role);
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public ItemType getItemType() {
        ItemType operandType = this.getBaseExpression().getItemType();
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        Affinity relationship = th.relationship(this.requiredItemType, operandType);
        switch (relationship) {
            case OVERLAPS: {
                if (this.requiredItemType instanceof NodeTest && operandType instanceof NodeTest) {
                    return new CombinedNodeTest((NodeTest)this.requiredItemType, 23, (NodeTest)operandType);
                }
                return this.requiredItemType;
            }
            case SUBSUMES: 
            case SAME_TYPE: {
                return operandType;
            }
        }
        return this.requiredItemType;
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return UType.fromTypeCode(this.requiredItemType.getPrimitiveType());
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && this.requiredItemType == ((ItemChecker)other).requiredItemType;
    }

    @Override
    public int computeHashCode() {
        return super.computeHashCode() ^ this.requiredItemType.hashCode();
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("treat", this);
        out.emitAttribute("as", AlphaCode.fromItemType(this.requiredItemType));
        out.emitAttribute("diag", this.role.save());
        this.getBaseExpression().export(out);
        out.endElement();
    }

    @Override
    public String getExpressionName() {
        return "treatAs";
    }

    @Override
    public String toString() {
        String typeDesc = this.requiredItemType.toString();
        return "(" + this.getBaseExpression() + ") treat as " + typeDesc;
    }

    @Override
    public String toShortString() {
        return this.getBaseExpression().toShortString();
    }
}

