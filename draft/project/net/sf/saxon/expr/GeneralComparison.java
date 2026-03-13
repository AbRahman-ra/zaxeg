/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.ArrayList;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.CastExpression;
import net.sf.saxon.expr.ComparisonExpression;
import net.sf.saxon.expr.ContextSwitchingExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.GeneralComparison20;
import net.sf.saxon.expr.IntegerRangeTest;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.RangeExpression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.expr.sort.GenericAtomicComparer;
import net.sf.saxon.expr.sort.UntypedNumericComparer;
import net.sf.saxon.functions.Minimax;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerRange;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.QualifiedNameValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.UntypedAtomicValue;

public abstract class GeneralComparison
extends BinaryExpression
implements ComparisonExpression,
Callable {
    protected int singletonOperator;
    protected AtomicComparer comparer;
    protected boolean needsRuntimeCheck = true;
    protected ComparisonCardinality comparisonCardinality = ComparisonCardinality.MANY_TO_MANY;
    protected boolean doneWarnings = false;

    public GeneralComparison(Expression p0, int op, Expression p1) {
        super(p0, op, p1);
        this.singletonOperator = GeneralComparison.getCorrespondingSingletonOperator(op);
    }

    public boolean needsRuntimeCheck() {
        return this.needsRuntimeCheck;
    }

    public void setNeedsRuntimeCheck(boolean needsCheck) {
        this.needsRuntimeCheck = needsCheck;
    }

    public ComparisonCardinality getComparisonCardinality() {
        return this.comparisonCardinality;
    }

    public void setComparisonCardinality(ComparisonCardinality card) {
        this.comparisonCardinality = card;
    }

    public void setAtomicComparer(AtomicComparer comparer) {
        this.comparer = comparer;
    }

    @Override
    public String getExpressionName() {
        return "GeneralComparison";
    }

    public NamespaceResolver getNamespaceResolver() {
        return this.getRetainedStaticContext();
    }

    @Override
    public AtomicComparer getAtomicComparer() {
        return this.comparer;
    }

    @Override
    public int getSingletonOperator() {
        return this.singletonOperator;
    }

    @Override
    public boolean convertsUntypedToOther() {
        return true;
    }

    @Override
    public int computeCardinality() {
        return 16384;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        Expression oldOp0 = this.getLhsExpression();
        Expression oldOp1 = this.getRhsExpression();
        this.getLhs().typeCheck(visitor, contextInfo);
        this.getRhs().typeCheck(visitor, contextInfo);
        if (Literal.isEmptySequence(this.getLhsExpression()) || Literal.isEmptySequence(this.getRhsExpression())) {
            return Literal.makeLiteral(BooleanValue.FALSE, this);
        }
        this.setLhsExpression(this.getLhsExpression().unordered(false, false));
        this.setRhsExpression(this.getRhsExpression().unordered(false, false));
        SequenceType atomicType = SequenceType.ATOMIC_SEQUENCE;
        TypeChecker tc = config.getTypeChecker(false);
        RoleDiagnostic role0 = new RoleDiagnostic(1, Token.tokens[this.operator], 0);
        this.setLhsExpression(tc.staticTypeCheck(this.getLhsExpression(), atomicType, role0, visitor));
        RoleDiagnostic role1 = new RoleDiagnostic(1, Token.tokens[this.operator], 1);
        this.setRhsExpression(tc.staticTypeCheck(this.getRhsExpression(), atomicType, role1, visitor));
        if (this.getLhsExpression() != oldOp0) {
            this.adoptChildExpression(this.getLhsExpression());
        }
        if (this.getRhsExpression() != oldOp1) {
            this.adoptChildExpression(this.getRhsExpression());
        }
        ItemType t0 = this.getLhsExpression().getItemType();
        ItemType t1 = this.getRhsExpression().getItemType();
        if (t0 instanceof ErrorType || t1 instanceof ErrorType) {
            return Literal.makeLiteral(BooleanValue.FALSE, this);
        }
        if (t0.getUType().union(t1.getUType()).overlaps(UType.EXTENSION)) {
            XPathException err = new XPathException("Cannot perform comparisons involving external objects");
            err.setIsTypeError(true);
            err.setErrorCode("XPTY0004");
            err.setLocator(this.getLocation());
            throw err;
        }
        BuiltInAtomicType pt0 = (BuiltInAtomicType)t0.getPrimitiveItemType();
        BuiltInAtomicType pt1 = (BuiltInAtomicType)t1.getPrimitiveItemType();
        int c0 = this.getLhsExpression().getCardinality();
        int c1 = this.getRhsExpression().getCardinality();
        if (c0 == 8192 || c1 == 8192) {
            return Literal.makeLiteral(BooleanValue.FALSE, this);
        }
        if (!(t0.equals(BuiltInAtomicType.ANY_ATOMIC) || t0.equals(BuiltInAtomicType.UNTYPED_ATOMIC) || t1.equals(BuiltInAtomicType.ANY_ATOMIC) || t1.equals(BuiltInAtomicType.UNTYPED_ATOMIC) || Type.isPossiblyComparable(pt0, pt1, Token.isOrderedOperator(this.singletonOperator)))) {
            String message = "In {" + this.toShortString() + "}: cannot compare " + t0 + " to " + t1;
            if (Cardinality.allowsZero(c0) || Cardinality.allowsZero(c1)) {
                if (!this.doneWarnings) {
                    this.doneWarnings = true;
                    String which = "one";
                    if (Cardinality.allowsZero(c0) && !Cardinality.allowsZero(c1)) {
                        which = "the first";
                    } else if (Cardinality.allowsZero(c1) && !Cardinality.allowsZero(c0)) {
                        which = "the second";
                    }
                    visitor.getStaticContext().issueWarning(message + ". The comparison can succeed only if " + which + " operand is empty, and in that case will always be false", this.getLocation());
                }
            } else {
                XPathException err = new XPathException(message);
                err.setErrorCode("XPTY0004");
                err.setIsTypeError(true);
                err.setLocator(this.getLocation());
                throw err;
            }
        }
        boolean bl = this.needsRuntimeCheck = !Type.isGuaranteedGenerallyComparable(pt0, pt1, Token.isOrderedOperator(this.singletonOperator));
        if (!(Cardinality.allowsMany(c0) || Cardinality.allowsMany(c1) || t0.equals(BuiltInAtomicType.ANY_ATOMIC) || t1.equals(BuiltInAtomicType.ANY_ATOMIC))) {
            Expression e0 = this.getLhsExpression();
            Expression e1 = this.getRhsExpression();
            if (t0.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
                if (t1.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
                    e0 = new CastExpression(this.getLhsExpression(), BuiltInAtomicType.STRING, Cardinality.allowsZero(c0));
                    this.adoptChildExpression(e0);
                    e1 = new CastExpression(this.getRhsExpression(), BuiltInAtomicType.STRING, Cardinality.allowsZero(c1));
                    this.adoptChildExpression(e1);
                } else {
                    if (NumericType.isNumericType(t1)) {
                        Expression vun = this.makeCompareUntypedToNumeric(this.getLhsExpression(), this.getRhsExpression(), this.singletonOperator);
                        return vun.typeCheck(visitor, contextInfo);
                    }
                    e0 = new CastExpression(this.getLhsExpression(), pt1, Cardinality.allowsZero(c0));
                    this.adoptChildExpression(e0);
                }
            } else if (t1.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
                if (NumericType.isNumericType(t0)) {
                    Expression vun = this.makeCompareUntypedToNumeric(this.getRhsExpression(), this.getLhsExpression(), Token.inverse(this.singletonOperator));
                    return vun.typeCheck(visitor, contextInfo);
                }
                e1 = new CastExpression(this.getRhsExpression(), pt0, Cardinality.allowsZero(c1));
                this.adoptChildExpression(e1);
            }
            ValueComparison vc = new ValueComparison(e0, this.singletonOperator, e1);
            vc.setAtomicComparer(this.comparer);
            vc.setResultWhenEmpty(BooleanValue.FALSE);
            ExpressionTool.copyLocationInfo(this, vc);
            Optimizer.trace(config, "Replaced general comparison by value comparison", vc);
            return vc.typeCheck(visitor, contextInfo);
        }
        StaticContext env = visitor.getStaticContext();
        if (this.comparer == null) {
            String defaultCollationName = env.getDefaultCollationName();
            StringCollator collation = config.getCollation(defaultCollationName);
            if (collation == null) {
                collation = CodepointCollator.getInstance();
            }
            this.comparer = GenericAtomicComparer.makeAtomicComparer(pt0, pt1, collation, config.getConversionContext());
        }
        if (this.getLhsExpression() instanceof Literal && this.getRhsExpression() instanceof Literal) {
            return Literal.makeLiteral(this.evaluateItem(env.makeEarlyEvaluationContext()), this);
        }
        return this;
    }

    private Expression makeCompareUntypedToNumeric(Expression lhs, Expression rhs, int operator) {
        ValueComparison vc = new ValueComparison(lhs, operator, rhs);
        vc.setAtomicComparer(new UntypedNumericComparer());
        ExpressionTool.copyLocationInfo(this, vc);
        Optimizer.trace(this.getConfiguration(), "Replaced general comparison by untyped-numeric value comparison", vc);
        return vc;
    }

    private static Expression makeMinOrMax(Expression exp, String function) {
        if (Cardinality.allowsMany(exp.getCardinality())) {
            Expression fn = SystemFunction.makeCall(function, exp.getRetainedStaticContext(), exp);
            assert (fn != null);
            ((Minimax)((SystemFunctionCall)fn).getTargetFunction()).setIgnoreNaN(true);
            return fn;
        }
        return exp;
    }

    @Override
    public int getIntrinsicDependencies() {
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        if (this.mayInvolveCastToQName(th, this.getLhsExpression(), this.getRhsExpression()) || this.mayInvolveCastToQName(th, this.getRhsExpression(), this.getLhsExpression())) {
            return 2048;
        }
        return 0;
    }

    private boolean mayInvolveCastToQName(TypeHierarchy th, Expression e1, Expression e2) {
        SimpleType s1 = (SimpleType)((Object)e1.getItemType().getAtomizedItemType());
        return (s1 == BuiltInAtomicType.ANY_ATOMIC || s1.isNamespaceSensitive()) && th.relationship(e2.getItemType().getAtomizedItemType(), BuiltInAtomicType.UNTYPED_ATOMIC) != Affinity.DISJOINT && (e2.getSpecialProperties() & 0x4000000) == 0;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GeneralComparison && super.equals(other) && this.comparer.equals(((GeneralComparison)other).comparer);
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        StaticContext env = visitor.getStaticContext();
        this.getLhs().optimize(visitor, contextInfo);
        this.getRhs().optimize(visitor, contextInfo);
        if (Literal.isEmptySequence(this.getLhsExpression()) || Literal.isEmptySequence(this.getRhsExpression())) {
            return Literal.makeLiteral(BooleanValue.FALSE, this);
        }
        this.setLhsExpression(this.getLhsExpression().unordered(false, false));
        this.setRhsExpression(this.getRhsExpression().unordered(false, false));
        if (this.getLhsExpression() instanceof Literal && this.getRhsExpression() instanceof Literal) {
            return Literal.makeLiteral(this.evaluateItem(visitor.getStaticContext().makeEarlyEvaluationContext()).materialize(), this);
        }
        ItemType t0 = this.getLhsExpression().getItemType();
        ItemType t1 = this.getRhsExpression().getItemType();
        int c0 = this.getLhsExpression().getCardinality();
        int c1 = this.getRhsExpression().getCardinality();
        boolean many0 = Cardinality.allowsMany(c0);
        boolean many1 = Cardinality.allowsMany(c1);
        if (many0) {
            this.comparisonCardinality = many1 ? ComparisonCardinality.MANY_TO_MANY : ComparisonCardinality.MANY_TO_ONE;
        } else {
            if (many1) {
                GeneralComparison mc = this.getInverseComparison();
                mc.comparisonCardinality = ComparisonCardinality.MANY_TO_ONE;
                ExpressionTool.copyLocationInfo(this, mc);
                mc.comparer = this.comparer;
                mc.needsRuntimeCheck = this.needsRuntimeCheck;
                return mc.optimize(visitor, contextInfo);
            }
            this.comparisonCardinality = ComparisonCardinality.ONE_TO_ONE;
        }
        if (this.operator == 6) {
            GroundedValue value1;
            GroundedValue value0;
            if (this.getLhsExpression() instanceof RangeExpression) {
                Expression min = ((RangeExpression)this.getLhsExpression()).getLhsExpression();
                Expression max = ((RangeExpression)this.getLhsExpression()).getRhsExpression();
                IntegerRangeTest ir = new IntegerRangeTest(this.getRhsExpression(), min, max);
                ExpressionTool.copyLocationInfo(this, ir);
                return ir;
            }
            if (this.getRhsExpression() instanceof RangeExpression) {
                Expression min = ((RangeExpression)this.getRhsExpression()).getLhsExpression();
                Expression max = ((RangeExpression)this.getRhsExpression()).getRhsExpression();
                IntegerRangeTest ir = new IntegerRangeTest(this.getLhsExpression(), min, max);
                ExpressionTool.copyLocationInfo(this, ir);
                return ir;
            }
            if (this.getLhsExpression() instanceof Literal && (value0 = ((Literal)this.getLhsExpression()).getValue()) instanceof IntegerRange) {
                long min = ((IntegerRange)value0).getStart();
                long max = ((IntegerRange)value0).getEnd();
                IntegerRangeTest ir = new IntegerRangeTest(this.getRhsExpression(), Literal.makeLiteral(Int64Value.makeIntegerValue(min), this), Literal.makeLiteral(Int64Value.makeIntegerValue(max), this));
                ExpressionTool.copyLocationInfo(this, ir);
                return ir;
            }
            if (this.getRhsExpression() instanceof Literal && (value1 = ((Literal)this.getRhsExpression()).getValue()) instanceof IntegerRange) {
                long min = ((IntegerRange)value1).getStart();
                long max = ((IntegerRange)value1).getEnd();
                IntegerRangeTest ir = new IntegerRangeTest(this.getLhsExpression(), Literal.makeLiteral(Int64Value.makeIntegerValue(min), this), Literal.makeLiteral(Int64Value.makeIntegerValue(max), this));
                ExpressionTool.copyLocationInfo(this, ir);
                return ir;
            }
        }
        if (this.operator != 6 && this.operator != 22 && (this.comparisonCardinality == ComparisonCardinality.MANY_TO_MANY || this.comparisonCardinality == ComparisonCardinality.MANY_TO_ONE && (this.manyOperandIsLiftable() || this.manyOperandIsRangeExpression())) && (NumericType.isNumericType(t0) || NumericType.isNumericType(t1))) {
            ValueComparison vc;
            switch (this.operator) {
                case 12: 
                case 14: {
                    vc = new ValueComparison(GeneralComparison.makeMinOrMax(this.getLhsExpression(), "min"), this.singletonOperator, GeneralComparison.makeMinOrMax(this.getRhsExpression(), "max"));
                    vc.setResultWhenEmpty(BooleanValue.FALSE);
                    vc.setAtomicComparer(this.comparer);
                    break;
                }
                case 11: 
                case 13: {
                    vc = new ValueComparison(GeneralComparison.makeMinOrMax(this.getLhsExpression(), "max"), this.singletonOperator, GeneralComparison.makeMinOrMax(this.getRhsExpression(), "min"));
                    vc.setResultWhenEmpty(BooleanValue.FALSE);
                    vc.setAtomicComparer(this.comparer);
                    break;
                }
                default: {
                    throw new UnsupportedOperationException("Unknown operator " + this.operator);
                }
            }
            ExpressionTool.copyLocationInfo(this, vc);
            vc.setRetainedStaticContext(this.getRetainedStaticContext());
            return vc.typeCheck(visitor, contextInfo).optimize(visitor, contextInfo);
        }
        if (this.getLhsExpression() instanceof Literal && this.getRhsExpression() instanceof Literal) {
            return Literal.makeLiteral(this.evaluateItem(env.makeEarlyEvaluationContext()), this);
        }
        return visitor.obtainOptimizer().optimizeGeneralComparison(visitor, this, false, contextInfo);
    }

    private boolean manyOperandIsLiftable() {
        if (this.getParentExpression() instanceof ContextSwitchingExpression && ((ContextSwitchingExpression)((Object)this.getParentExpression())).getActionExpression() == this) {
            for (Operand o : this.operands()) {
                if (!Cardinality.allowsMany(o.getChildExpression().getCardinality()) || !ExpressionTool.dependsOnFocus(o.getChildExpression())) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean manyOperandIsRangeExpression() {
        for (Operand o : this.operands()) {
            Expression e = o.getChildExpression();
            if (!Cardinality.allowsMany(e.getCardinality())) continue;
            return e instanceof RangeExpression || e instanceof Literal && ((Literal)e).getValue() instanceof IntegerRange;
        }
        return false;
    }

    @Override
    public BooleanValue evaluateItem(XPathContext context) throws XPathException {
        switch (this.comparisonCardinality) {
            case ONE_TO_ONE: {
                AtomicValue value0 = (AtomicValue)this.getLhsExpression().evaluateItem(context);
                AtomicValue value1 = (AtomicValue)this.getRhsExpression().evaluateItem(context);
                return BooleanValue.get(this.evaluateOneToOne(value0, value1, context));
            }
            case MANY_TO_ONE: {
                SequenceIterator iter0 = this.getLhsExpression().iterate(context);
                AtomicValue value1 = (AtomicValue)this.getRhsExpression().evaluateItem(context);
                return BooleanValue.get(this.evaluateManyToOne(iter0, value1, context));
            }
            case MANY_TO_MANY: {
                SequenceIterator iter1 = this.getLhsExpression().iterate(context);
                SequenceIterator iter2 = this.getRhsExpression().iterate(context);
                return BooleanValue.get(this.evaluateManyToMany(iter1, iter2, context));
            }
        }
        return null;
    }

    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        switch (this.comparisonCardinality) {
            case ONE_TO_ONE: {
                AtomicValue value0 = (AtomicValue)arguments[0].head();
                AtomicValue value1 = (AtomicValue)arguments[1].head();
                return BooleanValue.get(this.evaluateOneToOne(value0, value1, context));
            }
            case MANY_TO_ONE: {
                SequenceIterator iter0 = arguments[0].iterate();
                AtomicValue value1 = (AtomicValue)arguments[1].head();
                return BooleanValue.get(this.evaluateManyToOne(iter0, value1, context));
            }
            case MANY_TO_MANY: {
                SequenceIterator iter1 = arguments[0].iterate();
                SequenceIterator iter2 = arguments[1].iterate();
                return BooleanValue.get(this.evaluateManyToMany(iter1, iter2, context));
            }
        }
        return null;
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        switch (this.comparisonCardinality) {
            case ONE_TO_ONE: {
                AtomicValue value0 = (AtomicValue)this.getLhsExpression().evaluateItem(context);
                AtomicValue value1 = (AtomicValue)this.getRhsExpression().evaluateItem(context);
                return this.evaluateOneToOne(value0, value1, context);
            }
            case MANY_TO_ONE: {
                SequenceIterator iter0 = this.getLhsExpression().iterate(context);
                AtomicValue value1 = (AtomicValue)this.getRhsExpression().evaluateItem(context);
                return this.evaluateManyToOne(iter0, value1, context);
            }
            case MANY_TO_MANY: {
                SequenceIterator iter1 = this.getLhsExpression().iterate(context);
                SequenceIterator iter2 = this.getRhsExpression().iterate(context);
                return this.evaluateManyToMany(iter1, iter2, context);
            }
        }
        return false;
    }

    private boolean evaluateOneToOne(AtomicValue value0, AtomicValue value1, XPathContext context) throws XPathException {
        try {
            return value0 != null && value1 != null && GeneralComparison.compare(value0, this.singletonOperator, value1, this.comparer.provideContext(context), this.needsRuntimeCheck, context, this.getRetainedStaticContext());
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            e.maybeSetContext(context);
            throw e;
        }
    }

    private boolean evaluateManyToOne(SequenceIterator iter0, AtomicValue value1, XPathContext context) throws XPathException {
        try {
            AtomicValue item0;
            if (value1 == null) {
                return false;
            }
            AtomicComparer boundComparer = this.comparer.provideContext(context);
            while ((item0 = (AtomicValue)iter0.next()) != null) {
                if (!GeneralComparison.compare(item0, this.singletonOperator, value1, boundComparer, this.needsRuntimeCheck, context, this.getRetainedStaticContext())) continue;
                iter0.close();
                return true;
            }
            return false;
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            e.maybeSetContext(context);
            throw e;
        }
    }

    public boolean evaluateManyToMany(SequenceIterator iter0, SequenceIterator iter1, XPathContext context) throws XPathException {
        try {
            boolean exhausted0 = false;
            boolean exhausted1 = false;
            ArrayList<AtomicValue> value0 = new ArrayList<AtomicValue>();
            ArrayList<AtomicValue> value1 = new ArrayList<AtomicValue>();
            AtomicComparer boundComparer = this.comparer.provideContext(context);
            while (true) {
                if (!exhausted0) {
                    AtomicValue item0 = (AtomicValue)iter0.next();
                    if (item0 == null) {
                        if (exhausted1) {
                            return false;
                        }
                        exhausted0 = true;
                    } else {
                        for (AtomicValue item1 : value1) {
                            if (!GeneralComparison.compare(item0, this.singletonOperator, item1, boundComparer, this.needsRuntimeCheck, context, this.getRetainedStaticContext())) continue;
                            iter0.close();
                            iter1.close();
                            return true;
                        }
                        if (!exhausted1) {
                            value0.add(item0);
                        }
                    }
                }
                if (exhausted1) continue;
                AtomicValue item1 = (AtomicValue)iter1.next();
                if (item1 == null) {
                    if (exhausted0) {
                        return false;
                    }
                    exhausted1 = true;
                    continue;
                }
                for (AtomicValue item0 : value0) {
                    if (!GeneralComparison.compare(item0, this.singletonOperator, item1, boundComparer, this.needsRuntimeCheck, context, this.getRetainedStaticContext())) continue;
                    iter0.close();
                    iter1.close();
                    return true;
                }
                if (exhausted0) continue;
                value1.add(item1);
            }
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            e.maybeSetContext(context);
            throw e;
        }
    }

    public static boolean compare(AtomicValue a0, int operator, AtomicValue a1, AtomicComparer comparer, boolean checkTypes, XPathContext context, NamespaceResolver nsResolver) throws XPathException {
        boolean u0 = a0 instanceof UntypedAtomicValue;
        boolean u1 = a1 instanceof UntypedAtomicValue;
        if (u0 != u1) {
            ConversionRules rules = context.getConfiguration().getConversionRules();
            if (u0) {
                if (a1 instanceof NumericValue) {
                    return UntypedNumericComparer.quickCompare((UntypedAtomicValue)a0, (NumericValue)a1, operator, rules);
                }
                if (!(a1 instanceof StringValue)) {
                    StringConverter sc = a1.getItemType().getPrimitiveItemType().getStringConverter(rules);
                    if (a1 instanceof QualifiedNameValue) {
                        sc = (StringConverter)sc.setNamespaceResolver(nsResolver);
                    }
                    a0 = sc.convertString(a0.getStringValueCS()).asAtomic();
                }
            } else {
                if (a0 instanceof NumericValue) {
                    return UntypedNumericComparer.quickCompare((UntypedAtomicValue)a1, (NumericValue)a0, Token.inverse(operator), rules);
                }
                if (!(a0 instanceof StringValue)) {
                    StringConverter sc = a0.getItemType().getPrimitiveItemType().getStringConverter(rules);
                    if (a0 instanceof QualifiedNameValue) {
                        sc = (StringConverter)sc.setNamespaceResolver(nsResolver);
                    }
                    a1 = sc.convertString(a1.getStringValueCS()).asAtomic();
                }
            }
            checkTypes = false;
        }
        return ValueComparison.compare(a0, operator, a1, comparer, checkTypes);
    }

    @Override
    public ItemType getItemType() {
        return BuiltInAtomicType.BOOLEAN;
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return UType.BOOLEAN;
    }

    public static int getCorrespondingSingletonOperator(int op) {
        switch (op) {
            case 6: {
                return 50;
            }
            case 13: {
                return 54;
            }
            case 22: {
                return 51;
            }
            case 12: {
                return 53;
            }
            case 11: {
                return 52;
            }
            case 14: {
                return 55;
            }
        }
        return op;
    }

    protected GeneralComparison getInverseComparison() {
        GeneralComparison20 gc2 = new GeneralComparison20(this.getRhsExpression(), Token.inverse(this.operator), this.getLhsExpression());
        gc2.setRetainedStaticContext(this.getRetainedStaticContext());
        return gc2;
    }

    @Override
    public String getStreamerName() {
        return "GeneralComparison";
    }

    @Override
    public String tag() {
        return "gc";
    }

    @Override
    protected void explainExtraAttributes(ExpressionPresenter out) {
        String cc = "";
        switch (this.comparisonCardinality) {
            case ONE_TO_ONE: {
                cc = "1:1";
                break;
            }
            case MANY_TO_ONE: {
                cc = "N:1";
                break;
            }
            case MANY_TO_MANY: {
                cc = "M:N";
            }
        }
        out.emitAttribute("card", cc);
        out.emitAttribute("comp", this.comparer.save());
    }

    public static enum ComparisonCardinality {
        ONE_TO_ONE,
        MANY_TO_ONE,
        MANY_TO_MANY;

    }
}

