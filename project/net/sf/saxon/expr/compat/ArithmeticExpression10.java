/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.compat;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.ArithmeticExpression;
import net.sf.saxon.expr.Calculator;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FirstItemExpression;
import net.sf.saxon.expr.InstanceOfExpression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.NegateExpression;
import net.sf.saxon.expr.OrExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.functions.Number_1;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.SequenceType;

public class ArithmeticExpression10
extends ArithmeticExpression
implements Callable {
    public ArithmeticExpression10(Expression p0, int operator, Expression p1) {
        super(p0, operator, p1);
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getLhs().typeCheck(visitor, contextInfo);
        this.getRhs().typeCheck(visitor, contextInfo);
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        if (Literal.isEmptySequence(this.getLhsExpression())) {
            return Literal.makeLiteral(DoubleValue.NaN, this);
        }
        if (Literal.isEmptySequence(this.getRhsExpression())) {
            return Literal.makeLiteral(DoubleValue.NaN, this);
        }
        Expression oldOp0 = this.getLhsExpression();
        Expression oldOp1 = this.getRhsExpression();
        SequenceType atomicType = SequenceType.OPTIONAL_ATOMIC;
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(true);
        RoleDiagnostic role0 = new RoleDiagnostic(1, Token.tokens[this.operator], 0);
        this.setLhsExpression(tc.staticTypeCheck(this.getLhsExpression(), atomicType, role0, visitor));
        RoleDiagnostic role1 = new RoleDiagnostic(1, Token.tokens[this.operator], 1);
        this.setRhsExpression(tc.staticTypeCheck(this.getRhsExpression(), atomicType, role1, visitor));
        ItemType itemType0 = this.getLhsExpression().getItemType();
        if (itemType0 instanceof ErrorType) {
            return Literal.makeLiteral(DoubleValue.NaN, this);
        }
        AtomicType type0 = (AtomicType)itemType0.getPrimitiveItemType();
        ItemType itemType1 = this.getRhsExpression().getItemType();
        if (itemType1 instanceof ErrorType) {
            return Literal.makeLiteral(DoubleValue.NaN, this);
        }
        AtomicType type1 = (AtomicType)itemType1.getPrimitiveItemType();
        if (th.isSubType(type0, BuiltInAtomicType.INTEGER) && th.isSubType(type1, BuiltInAtomicType.INTEGER) && (this.operator == 15 || this.operator == 16 || this.operator == 17)) {
            ArithmeticExpression arith = new ArithmeticExpression(this.getLhsExpression(), this.operator, this.getRhsExpression());
            Expression n = SystemFunction.makeCall("number", this.getRetainedStaticContext(), arith);
            return n.typeCheck(visitor, contextInfo);
        }
        if (this.calculator == null) {
            this.setLhsExpression(this.createConversionCode(this.getLhsExpression(), config, type0));
        }
        type0 = (AtomicType)this.getLhsExpression().getItemType().getPrimitiveItemType();
        if (this.calculator == null) {
            this.setRhsExpression(this.createConversionCode(this.getRhsExpression(), config, type1));
        }
        type1 = (AtomicType)this.getRhsExpression().getItemType().getPrimitiveItemType();
        if (this.getLhsExpression() != oldOp0) {
            this.adoptChildExpression(this.getLhsExpression());
        }
        if (this.getRhsExpression() != oldOp1) {
            this.adoptChildExpression(this.getRhsExpression());
        }
        if (this.operator == 299) {
            GroundedValue v;
            if (this.getRhsExpression() instanceof Literal && (v = ((Literal)this.getRhsExpression()).getValue()) instanceof NumericValue) {
                return Literal.makeLiteral(((NumericValue)v).negate(), this);
            }
            NegateExpression ne = new NegateExpression(this.getRhsExpression());
            ne.setBackwardsCompatible(true);
            return ne.typeCheck(visitor, contextInfo);
        }
        boolean mustResolve = !type0.equals(BuiltInAtomicType.ANY_ATOMIC) && !type1.equals(BuiltInAtomicType.ANY_ATOMIC) && !type0.equals(NumericType.getInstance()) && !type1.equals(NumericType.getInstance());
        this.calculator = this.assignCalculator(type0, type1, mustResolve);
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
    public void setCalculator(Calculator calc) {
        this.calculator = calc;
    }

    private Calculator assignCalculator(AtomicType type0, AtomicType type1, boolean mustResolve) throws XPathException {
        Calculator calculator = Calculator.getCalculator(type0.getFingerprint(), type1.getFingerprint(), ArithmeticExpression.mapOpCode(this.operator), mustResolve);
        if (calculator == null) {
            XPathException de = new XPathException("Arithmetic operator is not defined for arguments of types (" + type0.getDescription() + ", " + type1.getDescription() + ")");
            de.setLocation(this.getLocation());
            de.setErrorCode("XPTY0004");
            throw de;
        }
        return calculator;
    }

    private Expression createConversionCode(Expression operand, Configuration config, AtomicType type) {
        TypeHierarchy th = config.getTypeHierarchy();
        if (Cardinality.allowsMany(operand.getCardinality())) {
            Expression fie = FirstItemExpression.makeFirstItemExpression(operand);
            ExpressionTool.copyLocationInfo(this, fie);
            operand = fie;
        }
        if (th.isSubType(type, BuiltInAtomicType.DOUBLE) || th.isSubType(type, BuiltInAtomicType.DATE) || th.isSubType(type, BuiltInAtomicType.TIME) || th.isSubType(type, BuiltInAtomicType.DATE_TIME) || th.isSubType(type, BuiltInAtomicType.DURATION)) {
            return operand;
        }
        if (th.isSubType(type, BuiltInAtomicType.BOOLEAN) || th.isSubType(type, BuiltInAtomicType.STRING) || th.isSubType(type, BuiltInAtomicType.UNTYPED_ATOMIC) || th.isSubType(type, BuiltInAtomicType.FLOAT) || th.isSubType(type, BuiltInAtomicType.DECIMAL)) {
            if (operand instanceof Literal) {
                GroundedValue val = ((Literal)operand).getValue();
                return Literal.makeLiteral(Number_1.convert((AtomicValue)val, config), this);
            }
            return SystemFunction.makeCall("number", this.getRetainedStaticContext(), operand);
        }
        LetExpression let = new LetExpression();
        let.setRequiredType(SequenceType.OPTIONAL_ATOMIC);
        let.setVariableQName(new StructuredQName("nn", "http://saxon.sf.net/", "nn" + let.hashCode()));
        let.setSequence(operand);
        LocalVariableReference var = new LocalVariableReference(let);
        InstanceOfExpression isDouble = new InstanceOfExpression(var, BuiltInAtomicType.DOUBLE.zeroOrOne());
        var = new LocalVariableReference(let);
        InstanceOfExpression isDecimal = new InstanceOfExpression(var, BuiltInAtomicType.DECIMAL.zeroOrOne());
        var = new LocalVariableReference(let);
        InstanceOfExpression isFloat = new InstanceOfExpression(var, BuiltInAtomicType.FLOAT.zeroOrOne());
        var = new LocalVariableReference(let);
        InstanceOfExpression isString = new InstanceOfExpression(var, BuiltInAtomicType.STRING.zeroOrOne());
        var = new LocalVariableReference(let);
        InstanceOfExpression isUntypedAtomic = new InstanceOfExpression(var, BuiltInAtomicType.UNTYPED_ATOMIC.zeroOrOne());
        var = new LocalVariableReference(let);
        InstanceOfExpression isBoolean = new InstanceOfExpression(var, BuiltInAtomicType.BOOLEAN.zeroOrOne());
        OrExpression condition = new OrExpression(isDouble, isDecimal);
        condition = new OrExpression(condition, isFloat);
        condition = new OrExpression(condition, isString);
        condition = new OrExpression(condition, isUntypedAtomic);
        condition = new OrExpression(condition, isBoolean);
        var = new LocalVariableReference(let);
        Expression fn = SystemFunction.makeCall("number", this.getRetainedStaticContext(), var);
        var = new LocalVariableReference(let);
        var.setStaticType(SequenceType.SINGLE_ATOMIC, null, 0);
        Expression action = Choose.makeConditional(condition, fn, var);
        let.setAction(action);
        return let;
    }

    @Override
    public PlainType getItemType() {
        ItemType t2;
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
        return this.calculator.getResultType((AtomicType)t1.getPrimitiveItemType(), (AtomicType)t2.getPrimitiveItemType());
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ArithmeticExpression10 a2 = new ArithmeticExpression10(this.getLhsExpression().copy(rebindings), this.operator, this.getRhsExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, a2);
        a2.calculator = this.calculator;
        return a2;
    }

    @Override
    protected String tag() {
        return "arith10";
    }

    @Override
    protected void explainExtraAttributes(ExpressionPresenter out) {
        out.emitAttribute("calc", this.calculator.code());
    }

    @Override
    public AtomicValue evaluateItem(XPathContext context) throws XPathException {
        Calculator calc = this.calculator;
        AtomicValue v1 = (AtomicValue)this.getLhsExpression().evaluateItem(context);
        if (v1 == null) {
            return DoubleValue.NaN;
        }
        AtomicValue v2 = (AtomicValue)this.getRhsExpression().evaluateItem(context);
        if (v2 == null) {
            return DoubleValue.NaN;
        }
        if (calc == null) {
            calc = this.assignCalculator(v1.getPrimitiveType(), v2.getPrimitiveType(), true);
        }
        return calc.compute(v1, v2, context);
    }

    @Override
    public AtomicValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        Calculator calc = this.calculator;
        AtomicValue v1 = (AtomicValue)arguments[0].head();
        if (v1 == null) {
            return DoubleValue.NaN;
        }
        AtomicValue v2 = (AtomicValue)arguments[1].head();
        if (v2 == null) {
            return DoubleValue.NaN;
        }
        if (calc == null) {
            calc = this.assignCalculator(v1.getPrimitiveType(), v2.getPrimitiveType(), true);
        }
        return calc.compute(v1, v2, context);
    }
}

