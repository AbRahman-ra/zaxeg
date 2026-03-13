/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.CastingExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;

public final class CastableExpression
extends CastingExpression {
    public CastableExpression(Expression source, AtomicType target, boolean allowEmpty) {
        super(source, target, allowEmpty);
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().typeCheck(visitor, contextInfo);
        SequenceType atomicType = SequenceType.ATOMIC_SEQUENCE;
        Configuration config = visitor.getConfiguration();
        RoleDiagnostic role = new RoleDiagnostic(2, "castable as", 0);
        TypeChecker tc = config.getTypeChecker(false);
        Expression operand = tc.staticTypeCheck(this.getBaseExpression(), atomicType, role, visitor);
        this.setBaseExpression(operand);
        if (operand instanceof Literal) {
            return this.preEvaluate();
        }
        return this;
    }

    protected Expression preEvaluate() throws XPathException {
        GroundedValue literalOperand = ((Literal)this.getBaseExpression()).getValue();
        if (literalOperand instanceof AtomicValue && this.converter != null) {
            ConversionResult result = this.converter.convert((AtomicValue)literalOperand);
            return Literal.makeLiteral(BooleanValue.get(!(result instanceof ValidationFailure)), this);
        }
        int length = literalOperand.getLength();
        if (length == 0) {
            return Literal.makeLiteral(BooleanValue.get(this.allowsEmpty()), this);
        }
        if (length > 1) {
            return Literal.makeLiteral(BooleanValue.FALSE, this);
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.optimizeChildren(visitor, contextInfo);
        if (this.getBaseExpression() instanceof Literal) {
            return this.preEvaluate();
        }
        return this;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CastableExpression && this.getBaseExpression().isEqual(((CastableExpression)other).getBaseExpression()) && this.getTargetType() == ((CastableExpression)other).getTargetType() && this.allowsEmpty() == ((CastableExpression)other).allowsEmpty();
    }

    @Override
    public int computeHashCode() {
        return super.computeHashCode() ^ 0x5555;
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
    public int computeCardinality() {
        return 16384;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        CastableExpression ce = new CastableExpression(this.getBaseExpression().copy(rebindings), this.getTargetType(), this.allowsEmpty());
        ExpressionTool.copyLocationInfo(this, ce);
        ce.setRetainedStaticContext(this.getRetainedStaticContext());
        ce.converter = this.converter;
        return ce;
    }

    @Override
    public BooleanValue evaluateItem(XPathContext context) throws XPathException {
        return BooleanValue.get(this.effectiveBooleanValue(context));
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        Item item;
        int count = 0;
        SequenceIterator iter = this.getBaseExpression().iterate(context);
        while ((item = iter.next()) != null) {
            if (item instanceof NodeInfo) {
                AtomicValue av;
                AtomicSequence atomizedValue = item.atomize();
                int length = SequenceTool.getLength(atomizedValue);
                if ((count += length) > 1) {
                    return false;
                }
                if (length == 0 || this.isCastable(av = atomizedValue.head(), this.getTargetType(), context)) continue;
                return false;
            }
            if (item instanceof AtomicValue) {
                AtomicValue av = (AtomicValue)item;
                if (++count > 1) {
                    return false;
                }
                if (this.isCastable(av, this.getTargetType(), context)) continue;
                return false;
            }
            if (item instanceof ArrayItem) {
                return false;
            }
            throw new XPathException("Input to cast cannot be atomized", "XPTY0004");
        }
        return count != 0 || this.allowsEmpty();
    }

    private boolean isCastable(AtomicValue value, AtomicType targetType, XPathContext context) {
        Converter converter = this.converter;
        if (converter == null) {
            converter = context.getConfiguration().getConversionRules().getConverter(value.getPrimitiveType(), targetType);
            if (converter == null) {
                return false;
            }
            if (converter.isAlwaysSuccessful()) {
                return true;
            }
            if (this.getTargetType().isNamespaceSensitive()) {
                converter = converter.setNamespaceResolver(this.getRetainedStaticContext());
            }
        }
        return !(converter.convert(value) instanceof ValidationFailure);
    }

    @Override
    public String getExpressionName() {
        return "castable";
    }

    @Override
    public String toString() {
        return this.getBaseExpression().toString() + " castable as " + this.getTargetType().getEQName();
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        this.export(out, "castable");
    }
}

