/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.Calculator;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.NegateExpression;
import net.sf.saxon.expr.UntypedSequenceConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.CastingTarget;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.SequenceType;

public class ArithmeticExpression
extends BinaryExpression {
    protected Calculator calculator;
    private PlainType itemType;

    public ArithmeticExpression(Expression p0, int operator, Expression p1) {
        super(p0, operator, p1);
    }

    @Override
    public String getExpressionName() {
        return "arithmetic";
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        return p | 0x4000000;
    }

    public void setCalculator(Calculator calculator) {
        this.calculator = calculator;
    }

    public Calculator getCalculator() {
        return this.calculator;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.resetLocalStaticProperties();
        this.getLhs().typeCheck(visitor, contextInfo);
        this.getRhs().typeCheck(visitor, contextInfo);
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        TypeChecker tc = config.getTypeChecker(false);
        Expression oldOp0 = this.getLhsExpression();
        Expression oldOp1 = this.getRhsExpression();
        SequenceType atomicType = SequenceType.OPTIONAL_ATOMIC;
        RoleDiagnostic role0 = new RoleDiagnostic(1, Token.tokens[this.operator], 0);
        this.setLhsExpression(tc.staticTypeCheck(this.getLhsExpression(), atomicType, role0, visitor));
        ItemType itemType0 = this.getLhsExpression().getItemType();
        if (itemType0 instanceof ErrorType) {
            return Literal.makeEmptySequence();
        }
        AtomicType type0 = (AtomicType)itemType0.getPrimitiveItemType();
        if (type0.getFingerprint() == 631) {
            this.setLhsExpression(UntypedSequenceConverter.makeUntypedSequenceConverter(config, this.getLhsExpression(), BuiltInAtomicType.DOUBLE));
            type0 = BuiltInAtomicType.DOUBLE;
        } else if ((this.getLhsExpression().getSpecialProperties() & 0x4000000) == 0 && th.relationship(type0, BuiltInAtomicType.UNTYPED_ATOMIC) != Affinity.DISJOINT) {
            this.setLhsExpression(UntypedSequenceConverter.makeUntypedSequenceConverter(config, this.getLhsExpression(), BuiltInAtomicType.DOUBLE));
            type0 = (AtomicType)this.getLhsExpression().getItemType().getPrimitiveItemType();
        }
        RoleDiagnostic role1 = new RoleDiagnostic(1, Token.tokens[this.operator], 1);
        this.setRhsExpression(tc.staticTypeCheck(this.getRhsExpression(), atomicType, role1, visitor));
        ItemType itemType1 = this.getRhsExpression().getItemType();
        if (itemType1 instanceof ErrorType) {
            return Literal.makeEmptySequence();
        }
        AtomicType type1 = (AtomicType)itemType1.getPrimitiveItemType();
        if (type1.getFingerprint() == 631) {
            this.setRhsExpression(UntypedSequenceConverter.makeUntypedSequenceConverter(config, this.getRhsExpression(), BuiltInAtomicType.DOUBLE));
            type1 = BuiltInAtomicType.DOUBLE;
        } else if ((this.getRhsExpression().getSpecialProperties() & 0x4000000) == 0 && th.relationship(type1, BuiltInAtomicType.UNTYPED_ATOMIC) != Affinity.DISJOINT) {
            this.setRhsExpression(UntypedSequenceConverter.makeUntypedSequenceConverter(config, this.getRhsExpression(), BuiltInAtomicType.DOUBLE));
            type1 = (AtomicType)this.getRhsExpression().getItemType().getPrimitiveItemType();
        }
        if (itemType0.getUType().union(itemType1.getUType()).overlaps(UType.EXTENSION)) {
            XPathException de = new XPathException("Arithmetic operators are not defined for external objects");
            de.setLocation(this.getLocation());
            de.setErrorCode("XPTY0004");
            throw de;
        }
        if (this.getLhsExpression() != oldOp0) {
            this.adoptChildExpression(this.getLhsExpression());
        }
        if (this.getRhsExpression() != oldOp1) {
            this.adoptChildExpression(this.getRhsExpression());
        }
        if (Literal.isEmptySequence(this.getLhsExpression()) || Literal.isEmptySequence(this.getRhsExpression())) {
            return Literal.makeEmptySequence();
        }
        if (this.operator == 299) {
            if (this.getRhsExpression() instanceof Literal && ((Literal)this.getRhsExpression()).getValue() instanceof NumericValue) {
                NumericValue nv = (NumericValue)((Literal)this.getRhsExpression()).getValue();
                return Literal.makeLiteral(nv.negate(), this);
            }
            NegateExpression ne = new NegateExpression(this.getRhsExpression());
            ne.setBackwardsCompatible(false);
            return ne.typeCheck(visitor, contextInfo);
        }
        boolean mustResolve = !type0.equals(BuiltInAtomicType.ANY_ATOMIC) && !type1.equals(BuiltInAtomicType.ANY_ATOMIC) && !type0.equals(NumericType.getInstance()) && !type1.equals(NumericType.getInstance());
        this.calculator = Calculator.getCalculator(type0.getFingerprint(), type1.getFingerprint(), ArithmeticExpression.mapOpCode(this.operator), mustResolve);
        if (this.calculator == null) {
            XPathException de = new XPathException("Arithmetic operator is not defined for arguments of types (" + type0.getDescription() + ", " + type1.getDescription() + ")");
            de.setLocation(this.getLocation());
            de.setIsTypeError(true);
            de.setErrorCode("XPTY0004");
            throw de;
        }
        if (this.calculator.code().matches("d.d")) {
            GroundedValue value;
            if (this.getLhsExpression() instanceof Literal && !type0.equals(BuiltInAtomicType.DOUBLE) && (value = ((Literal)this.getLhsExpression()).getValue()) instanceof NumericValue) {
                this.setLhsExpression(Literal.makeLiteral(new DoubleValue(((NumericValue)value).getDoubleValue()), this));
            }
            if (this.getRhsExpression() instanceof Literal && !type1.equals(BuiltInAtomicType.DOUBLE) && (value = ((Literal)this.getRhsExpression()).getValue()) instanceof NumericValue) {
                this.setRhsExpression(Literal.makeLiteral(new DoubleValue(((NumericValue)value).getDoubleValue()), this));
            }
        }
        try {
            if (this.getLhsExpression() instanceof Literal && this.getRhsExpression() instanceof Literal) {
                return Literal.makeLiteral(this.evaluateItem(visitor.getStaticContext().makeEarlyEvaluationContext()).materialize(), this);
            }
        } catch (XPathException xPathException) {
            // empty catch block
        }
        return this;
    }

    @Override
    public IntegerValue[] getIntegerBounds() {
        IntegerValue[] bounds0 = this.getLhsExpression().getIntegerBounds();
        IntegerValue[] bounds1 = this.getRhsExpression().getIntegerBounds();
        if (bounds0 == null || bounds1 == null) {
            return null;
        }
        switch (this.operator) {
            case 15: {
                return new IntegerValue[]{bounds0[0].plus(bounds1[0]), bounds0[1].plus(bounds1[1])};
            }
            case 16: {
                return new IntegerValue[]{bounds0[0].minus(bounds1[1]), bounds0[1].minus(bounds1[0])};
            }
            case 17: {
                if (this.getRhsExpression() instanceof Literal) {
                    IntegerValue val1 = bounds1[0];
                    if (val1.signum() > 0) {
                        return new IntegerValue[]{bounds0[0].times(val1), bounds0[1].times(val1)};
                    }
                    return null;
                }
                if (this.getLhsExpression() instanceof Literal) {
                    IntegerValue val0 = bounds1[0];
                    if (val0.signum() > 0) {
                        return new IntegerValue[]{bounds1[0].times(val0), bounds1[1].times(val0)};
                    }
                    return null;
                }
            }
            case 18: 
            case 56: {
                IntegerValue val1;
                if (this.getRhsExpression() instanceof Literal && (val1 = bounds1[0]).signum() > 0) {
                    try {
                        return new IntegerValue[]{bounds0[0].idiv(val1), bounds0[1].idiv(val1)};
                    } catch (XPathException e) {
                        return null;
                    }
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ArithmeticExpression ae = new ArithmeticExpression(this.getLhsExpression().copy(rebindings), this.operator, this.getRhsExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, ae);
        ae.calculator = this.calculator;
        return ae;
    }

    public static AtomicValue compute(AtomicValue value0, int operator, AtomicValue value1, XPathContext context) throws XPathException {
        int p0 = value0.getPrimitiveType().getFingerprint();
        int p1 = value1.getPrimitiveType().getFingerprint();
        Calculator calculator = Calculator.getCalculator(p0, p1, operator, false);
        return calculator.compute(value0, value1, context);
    }

    public static int mapOpCode(int op) {
        switch (op) {
            case 15: {
                return 0;
            }
            case 16: 
            case 299: {
                return 1;
            }
            case 17: {
                return 2;
            }
            case 18: {
                return 3;
            }
            case 56: {
                return 5;
            }
            case 19: {
                return 4;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public PlainType getItemType() {
        CastingTarget resultType;
        ItemType t2;
        if (this.itemType != null) {
            return this.itemType;
        }
        if (this.calculator == null) {
            return BuiltInAtomicType.ANY_ATOMIC;
        }
        ItemType t1 = this.getLhsExpression().getItemType();
        if (!(t1 instanceof AtomicType)) {
            t1 = t1.getAtomizedItemType();
        }
        if (!((t2 = this.getRhsExpression().getItemType()) instanceof AtomicType)) {
            t2 = t2.getAtomizedItemType();
        }
        if ((resultType = this.calculator.getResultType((AtomicType)t1.getPrimitiveItemType(), (AtomicType)t2.getPrimitiveItemType())).equals(BuiltInAtomicType.ANY_ATOMIC)) {
            TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
            if ((this.operator == 15 || this.operator == 16) && (NumericType.isNumericType(t2) || NumericType.isNumericType(t1))) {
                resultType = NumericType.getInstance();
            }
        }
        this.itemType = resultType;
        return this.itemType;
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        if (this.getParentExpression() instanceof FilterExpression && ((FilterExpression)this.getParentExpression()).getRhsExpression() == this) {
            return UType.NUMERIC;
        }
        if (this.operator == 299) {
            return UType.NUMERIC;
        }
        return UType.ANY_ATOMIC;
    }

    @Override
    public void resetLocalStaticProperties() {
        super.resetLocalStaticProperties();
        this.itemType = null;
    }

    @Override
    public AtomicValue evaluateItem(XPathContext context) throws XPathException {
        AtomicValue v0 = (AtomicValue)this.getLhsExpression().evaluateItem(context);
        if (v0 == null) {
            return null;
        }
        AtomicValue v1 = (AtomicValue)this.getRhsExpression().evaluateItem(context);
        if (v1 == null) {
            return null;
        }
        try {
            return this.calculator.compute(v0, v1, context);
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            e.maybeSetFailingExpression(this);
            e.maybeSetContext(context);
            throw e;
        }
    }

    @Override
    protected String tag() {
        return "arith";
    }

    @Override
    protected void explainExtraAttributes(ExpressionPresenter out) {
        if (this.calculator != null) {
            out.emitAttribute("calc", this.calculator.code());
        }
    }
}

