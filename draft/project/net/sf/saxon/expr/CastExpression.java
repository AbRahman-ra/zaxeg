/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AtomicSequenceConverter;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.CastingExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.functions.String_1;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public class CastExpression
extends CastingExpression
implements Callable {
    public CastExpression(Expression source, AtomicType target, boolean allowEmpty) {
        super(source, target, allowEmpty);
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().typeCheck(visitor, contextInfo);
        SequenceType atomicType = SequenceType.makeSequenceType(BuiltInAtomicType.ANY_ATOMIC, this.getCardinality());
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        RoleDiagnostic role = new RoleDiagnostic(2, "cast as", 0);
        TypeChecker tc = config.getTypeChecker(false);
        Expression operand = tc.staticTypeCheck(this.getBaseExpression(), atomicType, role, visitor);
        this.setBaseExpression(operand);
        ItemType sourceItemType = operand.getItemType();
        if (sourceItemType instanceof ErrorType) {
            if (this.allowsEmpty()) {
                return Literal.makeEmptySequence();
            }
            XPathException err = new XPathException("Cast does not allow an empty sequence as input");
            err.setErrorCode("XPTY0004");
            err.setLocation(this.getLocation());
            err.setIsTypeError(true);
            throw err;
        }
        PlainType sourceType = (PlainType)sourceItemType;
        Affinity r = th.relationship(sourceType, this.getTargetType());
        if (r == Affinity.SAME_TYPE) {
            return operand;
        }
        if (r == Affinity.SUBSUMED_BY) {
            this.converter = new Converter.UpCastingConverter(this.getTargetType());
        } else {
            ConversionRules rules = visitor.getConfiguration().getConversionRules();
            if (sourceType.isAtomicType() && sourceType != BuiltInAtomicType.ANY_ATOMIC) {
                this.converter = rules.getConverter((AtomicType)sourceType, this.getTargetType());
                if (this.converter == null) {
                    XPathException err = new XPathException("Casting from " + sourceType + " to " + this.getTargetType() + " can never succeed");
                    err.setErrorCode("XPTY0004");
                    err.setLocation(this.getLocation());
                    err.setIsTypeError(true);
                    throw err;
                }
                if (this.getTargetType().isNamespaceSensitive()) {
                    this.converter = this.converter.setNamespaceResolver(this.getRetainedStaticContext());
                }
            }
        }
        if (operand instanceof Literal) {
            return this.preEvaluate();
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression e;
        ItemType et;
        ItemType it;
        ItemType et2;
        Expression e2;
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        Expression e22 = super.optimize(visitor, contextInfo);
        if (e22 != this) {
            return e22;
        }
        Expression operand = this.getBaseExpression();
        if (this.getTargetType() == BuiltInAtomicType.UNTYPED_ATOMIC) {
            if (operand.isCallOn(String_1.class)) {
                e2 = ((SystemFunctionCall)operand).getArg(0);
                if (e2.getItemType() instanceof AtomicType && e2.getCardinality() == 16384) {
                    operand = e2;
                }
            } else if (operand instanceof CastExpression) {
                if (((CastExpression)operand).getTargetType() == BuiltInAtomicType.UNTYPED_ATOMIC) {
                    return operand;
                }
                if (((CastExpression)operand).getTargetType() == BuiltInAtomicType.STRING) {
                    ((CastExpression)operand).setTargetType(BuiltInAtomicType.UNTYPED_ATOMIC);
                    return operand;
                }
            } else if (operand instanceof AtomicSequenceConverter) {
                if (operand.getItemType() == BuiltInAtomicType.UNTYPED_ATOMIC) {
                    return operand;
                }
                if (operand.getItemType() == BuiltInAtomicType.STRING) {
                    AtomicSequenceConverter old = (AtomicSequenceConverter)operand;
                    AtomicSequenceConverter asc = new AtomicSequenceConverter(old.getBaseExpression(), BuiltInAtomicType.UNTYPED_ATOMIC);
                    return asc.typeCheck(visitor, contextInfo).optimize(visitor, contextInfo);
                }
            }
        }
        if (operand.isCallOn(String_1.class) && (et2 = (e2 = ((SystemFunctionCall)operand).getArg(0)).getItemType()) instanceof AtomicType && e2.getCardinality() == 16384 && th.isSubType(et2, this.getTargetType())) {
            return e2;
        }
        if (operand instanceof CastExpression && (th.isSubType(it = ((CastExpression)operand).getTargetType(), BuiltInAtomicType.STRING) || th.isSubType(it, BuiltInAtomicType.UNTYPED_ATOMIC)) && (et = (e = ((CastExpression)operand).getBaseExpression()).getItemType()) instanceof AtomicType && e.getCardinality() == 16384 && th.isSubType(et, this.getTargetType())) {
            return e;
        }
        if (operand instanceof AtomicSequenceConverter && (th.isSubType(it = operand.getItemType(), BuiltInAtomicType.STRING) || th.isSubType(it, BuiltInAtomicType.UNTYPED_ATOMIC)) && (et = (e = ((AtomicSequenceConverter)operand).getBaseExpression()).getItemType()) instanceof AtomicType && e.getCardinality() == 16384 && th.isSubType(et, this.getTargetType())) {
            return e;
        }
        if (!Cardinality.allowsZero(operand.getCardinality())) {
            this.setAllowEmpty(false);
            this.resetLocalStaticProperties();
        }
        if (operand instanceof Literal) {
            return this.preEvaluate();
        }
        return this;
    }

    protected Expression preEvaluate() throws XPathException {
        GroundedValue literalOperand = ((Literal)this.getBaseExpression()).getValue();
        if (literalOperand instanceof AtomicValue && this.converter != null) {
            ConversionResult result = this.converter.convert((AtomicValue)literalOperand);
            if (result instanceof ValidationFailure) {
                ValidationFailure err = (ValidationFailure)result;
                String code = err.getErrorCode();
                if (code == null) {
                    code = "FORG0001";
                }
                throw new XPathException(err.getMessage(), code, this.getLocation());
            }
            return Literal.makeLiteral((AtomicValue)result, this);
        }
        if (literalOperand.getLength() == 0) {
            if (this.allowsEmpty()) {
                return this.getBaseExpression();
            }
            XPathException err = new XPathException("Cast can never succeed: the operand must not be an empty sequence", "XPTY0004", this.getLocation());
            err.setIsTypeError(true);
            throw err;
        }
        return this;
    }

    @Override
    public int computeCardinality() {
        return this.allowsEmpty() && Cardinality.allowsZero(this.getBaseExpression().getCardinality()) ? 24576 : 16384;
    }

    @Override
    public ItemType getItemType() {
        return this.getTargetType();
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return this.getTargetType().getUType();
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        if (this.getTargetType() == BuiltInAtomicType.UNTYPED_ATOMIC) {
            p &= 0xFBFFFFFF;
        }
        return p;
    }

    @Override
    public IntegerValue[] getIntegerBounds() {
        if (this.converter == Converter.BooleanToInteger.INSTANCE) {
            return new IntegerValue[]{Int64Value.ZERO, Int64Value.PLUS_ONE};
        }
        return null;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        CastExpression c2 = new CastExpression(this.getBaseExpression().copy(rebindings), this.getTargetType(), this.allowsEmpty());
        ExpressionTool.copyLocationInfo(this, c2);
        c2.converter = this.converter;
        c2.setRetainedStaticContext(this.getRetainedStaticContext());
        c2.setOperandIsStringLiteral(this.isOperandIsStringLiteral());
        return c2;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        AtomicValue result = this.doCast((AtomicValue)arguments[0].head(), context);
        return result == null ? EmptySequence.getInstance() : result;
    }

    private AtomicValue doCast(AtomicValue value, XPathContext context) throws XPathException {
        ConversionResult result;
        if (value == null) {
            if (this.allowsEmpty()) {
                return null;
            }
            XPathException e = new XPathException("Cast does not allow an empty sequence");
            e.setXPathContext(context);
            e.setLocation(this.getLocation());
            e.setErrorCode("XPTY0004");
            throw e;
        }
        Converter converter = this.converter;
        if (converter == null) {
            ConversionRules rules = context.getConfiguration().getConversionRules();
            converter = rules.getConverter(value.getPrimitiveType(), this.getTargetType());
            if (converter == null) {
                XPathException e = new XPathException("Casting from " + value.getPrimitiveType() + " to " + this.getTargetType() + " is not permitted");
                e.setXPathContext(context);
                e.setLocation(this.getLocation());
                e.setErrorCode("XPTY0004");
                throw e;
            }
            if (this.getTargetType().isNamespaceSensitive()) {
                converter = converter.setNamespaceResolver(this.getRetainedStaticContext());
            }
        }
        if ((result = converter.convert(value)) instanceof ValidationFailure) {
            ValidationFailure err = (ValidationFailure)result;
            ValidationException xe = err.makeException();
            xe.maybeSetErrorCode("FORG0001");
            xe.maybeSetLocation(this.getLocation());
            throw xe;
        }
        return (AtomicValue)result;
    }

    @Override
    public AtomicValue evaluateItem(XPathContext context) throws XPathException {
        try {
            AtomicValue value = (AtomicValue)this.getBaseExpression().evaluateItem(context);
            return this.doCast(value, context);
        } catch (ClassCastException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CastExpression && this.getBaseExpression().isEqual(((CastExpression)other).getBaseExpression()) && this.getTargetType() == ((CastExpression)other).getTargetType() && this.allowsEmpty() == ((CastExpression)other).allowsEmpty();
    }

    @Override
    public int computeHashCode() {
        return super.computeHashCode() ^ this.getTargetType().hashCode();
    }

    @Override
    public String toString() {
        return this.getTargetType().getEQName() + "(" + this.getBaseExpression().toString() + ")";
    }

    @Override
    public String toShortString() {
        return this.getTargetType().getDisplayName() + "(" + this.getBaseExpression().toShortString() + ")";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        this.export(out, "cast");
    }

    @Override
    public String getExpressionName() {
        return "cast";
    }
}

