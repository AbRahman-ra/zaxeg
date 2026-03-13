/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.math.BigInteger;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.ArithmeticExpression;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.Calculator;
import net.sf.saxon.expr.CastExpression;
import net.sf.saxon.expr.CompareToStringConstant;
import net.sf.saxon.expr.ComparisonExpression;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.ContextSwitchingExpression;
import net.sf.saxon.expr.EarlyEvaluationContext;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterIterator;
import net.sf.saxon.expr.FirstItemExpression;
import net.sf.saxon.expr.InstanceOfExpression;
import net.sf.saxon.expr.IntegerRangeTest;
import net.sf.saxon.expr.IsLastExpression;
import net.sf.saxon.expr.LastItemExpression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.SubscriptExpression;
import net.sf.saxon.expr.SubsequenceIterator;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.functions.LocalName_1;
import net.sf.saxon.functions.PositionAndLast;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.registry.VendorFunctionSetHE;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.BasePatternWithPredicate;
import net.sf.saxon.pattern.GeneralNodePattern;
import net.sf.saxon.pattern.GeneralPositionalPattern;
import net.sf.saxon.pattern.LocalNameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.pattern.SimplePositionalPattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.MemoClosure;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public final class FilterExpression
extends BinaryExpression
implements ContextSwitchingExpression {
    private boolean filterIsPositional;
    private boolean filterIsSingletonBoolean;
    private boolean filterIsIndependent;
    public boolean doneReorderingPredicates = false;
    public static final int FILTERED = 10000;
    public static final OperandRole FILTER_PREDICATE = new OperandRole(6, OperandUsage.INSPECTION, SequenceType.ANY_SEQUENCE);

    public FilterExpression(Expression base, Expression filter) {
        super(base, 4, filter);
        base.setFiltered(true);
    }

    @Override
    protected OperandRole getOperandRole(int arg) {
        return arg == 0 ? OperandRole.SAME_FOCUS_ACTION : FILTER_PREDICATE;
    }

    public Expression getBase() {
        return this.getLhsExpression();
    }

    public void setBase(Expression base) {
        this.setLhsExpression(base);
    }

    public Expression getFilter() {
        return this.getRhsExpression();
    }

    public void setFilter(Expression filter) {
        this.setRhsExpression(filter);
    }

    @Override
    public String getExpressionName() {
        return "filter";
    }

    @Override
    public ItemType getItemType() {
        if (this.getFilter() instanceof InstanceOfExpression && ((InstanceOfExpression)this.getFilter()).getBaseExpression() instanceof ContextItemExpression) {
            return ((InstanceOfExpression)this.getFilter()).getRequiredItemType();
        }
        return this.getBase().getItemType();
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return this.getBase().getStaticUType(contextItemType);
    }

    @Override
    public Expression getSelectExpression() {
        return this.getBase();
    }

    public boolean isFilterIsPositional() {
        return this.filterIsPositional;
    }

    @Override
    public Expression getActionExpression() {
        return this.getFilter();
    }

    public boolean isPositional(TypeHierarchy th) {
        return FilterExpression.isPositionalFilter(this.getFilter(), th);
    }

    public boolean isSimpleBooleanFilter() {
        return this.filterIsSingletonBoolean;
    }

    public boolean isIndependentFilter() {
        return this.filterIsIndependent;
    }

    @Override
    public Expression simplify() throws XPathException {
        this.setBase(this.getBase().simplify());
        this.setFilter(this.getFilter().simplify());
        if (Literal.isEmptySequence(this.getBase())) {
            return this.getBase();
        }
        if (this.getFilter() instanceof Literal && !(((Literal)this.getFilter()).getValue() instanceof NumericValue)) {
            try {
                if (this.getFilter().effectiveBooleanValue(new EarlyEvaluationContext(this.getConfiguration()))) {
                    return this.getBase();
                }
                return Literal.makeEmptySequence();
            } catch (XPathException e) {
                e.maybeSetLocation(this.getLocation());
                throw e;
            }
        }
        if (this.getFilter().isCallOn(PositionAndLast.Last.class)) {
            this.setFilter(new IsLastExpression(true));
            this.adoptChildExpression(this.getFilter());
        }
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        this.getLhs().typeCheck(visitor, contextInfo);
        this.getBase().setFiltered(true);
        if (Literal.isEmptySequence(this.getBase())) {
            return this.getBase();
        }
        ContextItemStaticInfo baseItemType = config.makeContextItemStaticInfo(this.getSelectExpression().getItemType(), false);
        baseItemType.setContextSettingExpression(this.getBase());
        this.getRhs().typeCheck(visitor, baseItemType);
        Expression filter2 = ExpressionTool.unsortedIfHomogeneous(this.getFilter(), visitor.isOptimizeForStreaming());
        if (filter2 != this.getFilter()) {
            this.setFilter(filter2);
        }
        if (Literal.isConstantOne(this.getFilter())) {
            Expression fie = FirstItemExpression.makeFirstItemExpression(this.getBase());
            ExpressionTool.copyLocationInfo(this, fie);
            return fie;
        }
        this.filterIsPositional = FilterExpression.isPositionalFilter(this.getFilter(), th);
        this.filterIsSingletonBoolean = this.getFilter().getCardinality() == 16384 && this.getFilter().getItemType().equals(BuiltInAtomicType.BOOLEAN);
        this.filterIsIndependent = (this.getFilter().getDependencies() & 0x1E) == 0;
        ExpressionTool.resetStaticProperties(this);
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        FilterExpression f2;
        Expression subsequence;
        boolean contextIsDoc;
        Expression f;
        int isIndexable;
        Configuration config = visitor.getConfiguration();
        Optimizer opt = visitor.obtainOptimizer();
        boolean tracing = config.getBooleanProperty(Feature.TRACE_OPTIMIZER_DECISIONS);
        TypeHierarchy th = config.getTypeHierarchy();
        this.getLhs().optimize(visitor, contextItemType);
        this.getBase().setFiltered(true);
        ContextItemStaticInfo baseItemType = config.makeContextItemStaticInfo(this.getSelectExpression().getItemType(), false);
        baseItemType.setContextSettingExpression(this.getBase());
        this.getRhs().optimize(visitor, baseItemType);
        Expression filter2 = ExpressionTool.unsortedIfHomogeneous(this.getFilter(), visitor.isOptimizeForStreaming());
        if (filter2 != this.getFilter()) {
            this.setFilter(filter2);
        }
        if (this.getFilter() instanceof IsLastExpression && ((IsLastExpression)this.getFilter()).getCondition() && this.getBase() instanceof AxisExpression && ((AxisExpression)this.getBase()).getAxis() == 3) {
            NodeTest test = ((AxisExpression)this.getBase()).getNodeTest();
            AxisExpression fs = new AxisExpression(7, test);
            this.setFilter(SystemFunction.makeCall("empty", this.getRetainedStaticContext(), fs));
            if (tracing) {
                Optimizer.trace(config, "Replaced [last()] predicate by test for following-sibling", this);
            }
        }
        if (this.getBase() instanceof AxisExpression && ((AxisExpression)this.getBase()).getNodeTest() == NodeKindTest.ELEMENT && this.getFilter() instanceof CompareToStringConstant && ((CompareToStringConstant)this.getFilter()).getSingletonOperator() == 50 && ((CompareToStringConstant)this.getFilter()).getLhsExpression().isCallOn(LocalName_1.class) && ((SystemFunctionCall)((CompareToStringConstant)this.getFilter()).getLhsExpression()).getArg(0) instanceof ContextItemExpression) {
            AxisExpression ax2 = new AxisExpression(((AxisExpression)this.getBase()).getAxis(), new LocalNameTest(config.getNamePool(), 1, ((CompareToStringConstant)this.getFilter()).getComparand()));
            ExpressionTool.copyLocationInfo(this, ax2);
            return ax2;
        }
        ItemType filterType = this.getFilter().getItemType();
        if (!th.isSubType(filterType, BuiltInAtomicType.BOOLEAN) && th.relationship(filterType, NumericType.getInstance()) == Affinity.DISJOINT) {
            Expression f3 = SystemFunction.makeCall("boolean", this.getRetainedStaticContext(), this.getFilter());
            this.setFilter(f3.optimize(visitor, baseItemType));
        }
        if (this.getFilter() instanceof Literal && ((Literal)this.getFilter()).getValue() instanceof BooleanValue) {
            if (((BooleanValue)((Literal)this.getFilter()).getValue()).getBooleanValue()) {
                if (tracing) {
                    opt.trace("Redundant filter removed", this.getBase());
                }
                return this.getBase();
            }
            Literal result = Literal.makeEmptySequence();
            ExpressionTool.copyLocationInfo(this, result);
            if (tracing) {
                opt.trace("Filter expression eliminated because predicate is always false", result);
            }
            return result;
        }
        this.filterIsPositional = FilterExpression.isPositionalFilter(this.getFilter(), th);
        boolean bl = this.filterIsSingletonBoolean = this.getFilter().getCardinality() == 16384 && this.getFilter().getItemType().equals(BuiltInAtomicType.BOOLEAN);
        if (!this.filterIsPositional && !visitor.isOptimizeForStreaming() && (isIndexable = opt.isIndexableFilter(this.getFilter())) != 0 && (f = opt.tryIndexedFilter(this, visitor, isIndexable > 0, contextIsDoc = contextItemType != null && contextItemType.getItemType() != ErrorType.getInstance() && th.isSubType(contextItemType.getItemType(), NodeKindTest.DOCUMENT))) != this) {
            return f.typeCheck(visitor, contextItemType).optimize(visitor, contextItemType);
        }
        if (this.filterIsPositional && this.getFilter() instanceof BooleanExpression && ((BooleanExpression)this.getFilter()).operator == 10) {
            Expression p1;
            BooleanExpression bf = (BooleanExpression)this.getFilter();
            if (FilterExpression.isExplicitlyPositional(bf.getLhsExpression()) && !FilterExpression.isExplicitlyPositional(bf.getRhsExpression())) {
                Expression p0 = FilterExpression.forceToBoolean(bf.getLhsExpression());
                p1 = FilterExpression.forceToBoolean(bf.getRhsExpression());
                FilterExpression f1 = new FilterExpression(this.getBase(), p0);
                ExpressionTool.copyLocationInfo(this, f1);
                FilterExpression f22 = new FilterExpression(f1, p1);
                ExpressionTool.copyLocationInfo(this, f22);
                if (tracing) {
                    opt.trace("Composite filter replaced by nested filter expressions", f22);
                }
                return f22.optimize(visitor, contextItemType);
            }
            if (FilterExpression.isExplicitlyPositional(bf.getRhsExpression()) && !FilterExpression.isExplicitlyPositional(bf.getLhsExpression())) {
                Expression p0 = FilterExpression.forceToBoolean(bf.getLhsExpression());
                p1 = FilterExpression.forceToBoolean(bf.getRhsExpression());
                FilterExpression f1 = new FilterExpression(this.getBase(), p1);
                ExpressionTool.copyLocationInfo(this, f1);
                FilterExpression f23 = new FilterExpression(f1, p0);
                ExpressionTool.copyLocationInfo(this, f23);
                if (tracing) {
                    opt.trace("Composite filter replaced by nested filter expressions", f23);
                }
                return f23.optimize(visitor, contextItemType);
            }
        }
        if (this.getFilter() instanceof IsLastExpression && ((IsLastExpression)this.getFilter()).getCondition()) {
            if (this.getBase() instanceof Literal) {
                this.setFilter(Literal.makeLiteral(new Int64Value(((Literal)this.getBase()).getValue().getLength()), this));
            } else {
                return new LastItemExpression(this.getBase());
            }
        }
        if ((subsequence = this.tryToRewritePositionalFilter(visitor, tracing)) != null) {
            if (tracing) {
                subsequence.setRetainedStaticContext(this.getRetainedStaticContext());
                opt.trace("Rewrote Filter Expression as:", subsequence);
            }
            ExpressionTool.copyLocationInfo(this, subsequence);
            return subsequence.simplify().typeCheck(visitor, contextItemType).optimize(visitor, contextItemType);
        }
        if (!(this.filterIsPositional || this.doneReorderingPredicates || this.getParentExpression() instanceof FilterExpression || (f2 = opt.reorderPredicates(this, visitor, contextItemType)) == this)) {
            f2.doneReorderingPredicates = true;
            return f2;
        }
        Sequence sequence = this.tryEarlyEvaluation(visitor);
        if (sequence != null) {
            GroundedValue value = sequence.materialize();
            return Literal.makeLiteral(value, this);
        }
        return this;
    }

    @Override
    public double getCost() {
        return Math.max(this.getLhsExpression().getCost() + 5.0 * this.getRhsExpression().getCost(), 1.0E9);
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public IntegerValue[] getIntegerBounds() {
        return this.getBase().getIntegerBounds();
    }

    private Sequence tryEarlyEvaluation(ExpressionVisitor visitor) {
        try {
            if (this.getBase() instanceof Literal && !ExpressionTool.refersToVariableOrFunction(this.getFilter()) && (this.getFilter().getDependencies() & 0xFFFFFFE1) == 0) {
                XPathContext context = visitor.getStaticContext().makeEarlyEvaluationContext();
                return this.iterate(context).materialize();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        PathMap.PathMapNodeSet target = this.getBase().addToPathMap(pathMap, pathMapNodeSet);
        this.getFilter().addToPathMap(pathMap, target);
        return target;
    }

    private static Expression forceToBoolean(Expression in) {
        if (in.getItemType().getPrimitiveType() == 514) {
            return in;
        }
        return SystemFunction.makeCall("boolean", in.getRetainedStaticContext(), in);
    }

    private Expression tryToRewritePositionalFilter(ExpressionVisitor visitor, boolean tracing) throws XPathException {
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        if (this.getFilter() instanceof Literal) {
            Expression result;
            GroundedValue val = ((Literal)this.getFilter()).getValue();
            if (val instanceof NumericValue) {
                int lvalue = ((NumericValue)val).asSubscript();
                Expression result2 = lvalue != -1 ? (lvalue == 1 ? FirstItemExpression.makeFirstItemExpression(this.getBase()) : new SubscriptExpression(this.getBase(), this.getFilter())) : Literal.makeEmptySequence();
                if (tracing) {
                    Optimizer.trace(config, "Rewriting numeric filter expression with constant subscript", result2);
                }
                return result2;
            }
            Expression expression = result = ExpressionTool.effectiveBooleanValue(val.iterate()) ? this.getBase() : Literal.makeEmptySequence();
            if (tracing) {
                Optimizer.trace(config, "Rewriting boolean filter expression with constant subscript", result);
            }
            return result;
        }
        if (NumericType.isNumericType(this.getFilter().getItemType()) && !Cardinality.allowsMany(this.getFilter().getCardinality()) && (this.getFilter().getDependencies() & 0x1E) == 0) {
            SubscriptExpression result = new SubscriptExpression(this.getBase(), this.getFilter());
            if (tracing) {
                Optimizer.trace(config, "Rewriting numeric filter expression with focus-independent subscript", result);
            }
            return result;
        }
        if (this.getFilter() instanceof ComparisonExpression) {
            Expression comparand;
            Expression lhs = ((ComparisonExpression)((Object)this.getFilter())).getLhsExpression();
            Expression rhs = ((ComparisonExpression)((Object)this.getFilter())).getRhsExpression();
            int operator = ((ComparisonExpression)((Object)this.getFilter())).getSingletonOperator();
            if (lhs.isCallOn(PositionAndLast.Position.class) && NumericType.isNumericType(rhs.getItemType())) {
                comparand = rhs;
            } else if (rhs.isCallOn(PositionAndLast.Position.class) && NumericType.isNumericType(lhs.getItemType())) {
                comparand = lhs;
                operator = Token.inverse(operator);
            } else {
                return null;
            }
            if (ExpressionTool.dependsOnFocus(comparand)) {
                return null;
            }
            int card = comparand.getCardinality();
            if (Cardinality.allowsMany(card)) {
                return null;
            }
            if (Cardinality.allowsZero(card)) {
                LetExpression let = new LetExpression();
                let.setRequiredType(SequenceType.makeSequenceType(comparand.getItemType(), card));
                let.setVariableQName(new StructuredQName("pp", "http://saxon.sf.net/", "pp" + let.hashCode()));
                let.setSequence(comparand);
                comparand = new LocalVariableReference(let);
                LocalVariableReference existsArg = new LocalVariableReference(let);
                Expression exists = SystemFunction.makeCall("exists", this.getRetainedStaticContext(), existsArg);
                Expression rewrite = FilterExpression.tryToRewritePositionalFilterSupport(this.getBase(), comparand, operator, th);
                if (rewrite == null) {
                    return this;
                }
                Expression choice = Choose.makeConditional(exists, rewrite);
                let.setAction(choice);
                return let;
            }
            return FilterExpression.tryToRewritePositionalFilterSupport(this.getBase(), comparand, operator, th);
        }
        if (this.getFilter() instanceof IntegerRangeTest) {
            Expression val = ((IntegerRangeTest)this.getFilter()).getValue();
            if (!val.isCallOn(PositionAndLast.class)) {
                return null;
            }
            Expression min = ((IntegerRangeTest)this.getFilter()).getMin();
            Expression max = ((IntegerRangeTest)this.getFilter()).getMax();
            if (ExpressionTool.dependsOnFocus(min)) {
                return null;
            }
            if (ExpressionTool.dependsOnFocus(max)) {
                if (max.isCallOn(PositionAndLast.Last.class)) {
                    Expression result = SystemFunction.makeCall("subsequence", this.getRetainedStaticContext(), this.getBase(), min);
                    if (tracing) {
                        Optimizer.trace(config, "Rewriting numeric range filter expression using subsequence()", result);
                    }
                    return result;
                }
                return null;
            }
            LetExpression let = new LetExpression();
            let.setRequiredType(SequenceType.SINGLE_INTEGER);
            let.setVariableQName(new StructuredQName("nn", "http://saxon.sf.net/", "nn" + let.hashCode()));
            let.setSequence(min);
            min = new LocalVariableReference(let);
            LocalVariableReference min2 = new LocalVariableReference(let);
            ArithmeticExpression minMinusOne = new ArithmeticExpression(min2, 16, Literal.makeLiteral(Int64Value.makeIntegerValue(1L), this));
            ArithmeticExpression length = new ArithmeticExpression(max, 16, minMinusOne);
            Expression subs = SystemFunction.makeCall("subsequence", this.getRetainedStaticContext(), this.getBase(), min, length);
            let.setAction(subs);
            if (tracing) {
                Optimizer.trace(config, "Rewriting numeric range filter expression using subsequence()", subs);
            }
            return let;
        }
        return null;
    }

    private static Expression tryToRewritePositionalFilterSupport(Expression start, Expression comparand, int operator, TypeHierarchy th) throws XPathException {
        if (th.isSubType(comparand.getItemType(), BuiltInAtomicType.INTEGER)) {
            switch (operator) {
                case 50: {
                    if (Literal.isConstantOne(comparand)) {
                        return FirstItemExpression.makeFirstItemExpression(start);
                    }
                    if (comparand instanceof Literal && ((IntegerValue)((Literal)comparand).getValue()).asBigInteger().compareTo(BigInteger.ZERO) <= 0) {
                        return Literal.makeEmptySequence();
                    }
                    return new SubscriptExpression(start, comparand);
                }
                case 53: {
                    Expression[] args = new Expression[3];
                    args[0] = start;
                    args[1] = Literal.makeLiteral(Int64Value.makeIntegerValue(1L), start);
                    if (Literal.isAtomic(comparand)) {
                        long n = ((NumericValue)((Literal)comparand).getValue()).longValue();
                        args[2] = Literal.makeLiteral(Int64Value.makeIntegerValue(n - 1L), start);
                    } else {
                        ArithmeticExpression decrement = new ArithmeticExpression(comparand, 16, Literal.makeLiteral(Int64Value.makeIntegerValue(1L), start));
                        decrement.setCalculator(Calculator.getCalculator(533, 533, 1, true));
                        args[2] = decrement;
                    }
                    return SystemFunction.makeCall("subsequence", start.getRetainedStaticContext(), args);
                }
                case 55: {
                    Expression[] args = new Expression[]{start, Literal.makeLiteral(Int64Value.makeIntegerValue(1L), start), comparand};
                    return SystemFunction.makeCall("subsequence", start.getRetainedStaticContext(), args);
                }
                case 51: {
                    return SystemFunction.makeCall("remove", start.getRetainedStaticContext(), start, comparand);
                }
                case 52: {
                    Expression[] args = new Expression[2];
                    args[0] = start;
                    if (Literal.isAtomic(comparand)) {
                        long n = ((NumericValue)((Literal)comparand).getValue()).longValue();
                        args[1] = Literal.makeLiteral(Int64Value.makeIntegerValue(n + 1L), start);
                    } else {
                        args[1] = new ArithmeticExpression(comparand, 15, Literal.makeLiteral(Int64Value.makeIntegerValue(1L), start));
                    }
                    return SystemFunction.makeCall("subsequence", start.getRetainedStaticContext(), args);
                }
                case 54: {
                    return SystemFunction.makeCall("subsequence", start.getRetainedStaticContext(), start, comparand);
                }
            }
            throw new IllegalArgumentException("operator");
        }
        switch (operator) {
            case 50: {
                return new SubscriptExpression(start, comparand);
            }
            case 53: {
                LetExpression let = new LetExpression();
                let.setRequiredType(SequenceType.makeSequenceType(comparand.getItemType(), 16384));
                let.setVariableQName(new StructuredQName("pp", "http://saxon.sf.net/", "pp" + let.hashCode()));
                let.setSequence(comparand);
                LocalVariableReference isWholeArg = new LocalVariableReference(let);
                LocalVariableReference arithArg = new LocalVariableReference(let);
                LocalVariableReference floorArg = new LocalVariableReference(let);
                Expression isWhole = VendorFunctionSetHE.getInstance().makeFunction("is-whole-number", 1).makeFunctionCall(isWholeArg);
                ArithmeticExpression minusOne = new ArithmeticExpression(arithArg, 16, Literal.makeLiteral(Int64Value.makeIntegerValue(1L), start));
                Expression floor = SystemFunction.makeCall("floor", start.getRetainedStaticContext(), floorArg);
                Expression choice = Choose.makeConditional(isWhole, minusOne, floor);
                Expression subs = SystemFunction.makeCall("subsequence", start.getRetainedStaticContext(), start, Literal.makeLiteral(Int64Value.makeIntegerValue(1L), start), choice);
                let.setAction(subs);
                return let;
            }
            case 55: {
                Expression floor = SystemFunction.makeCall("floor", start.getRetainedStaticContext(), comparand);
                return SystemFunction.makeCall("subsequence", start.getRetainedStaticContext(), start, Literal.makeLiteral(Int64Value.makeIntegerValue(1L), start), floor);
            }
            case 51: {
                LetExpression let = new LetExpression();
                ExpressionTool.copyLocationInfo(start, let);
                let.setRequiredType(SequenceType.makeSequenceType(comparand.getItemType(), 16384));
                let.setVariableQName(new StructuredQName("pp", "http://saxon.sf.net/", "pp" + let.hashCode()));
                let.setSequence(comparand);
                LocalVariableReference isWholeArg = new LocalVariableReference(let);
                LocalVariableReference castArg = new LocalVariableReference(let);
                Expression isWhole = VendorFunctionSetHE.getInstance().makeFunction("is-whole-number", 1).makeFunctionCall(isWholeArg);
                ExpressionTool.copyLocationInfo(start, isWhole);
                CastExpression cast = new CastExpression(castArg, BuiltInAtomicType.INTEGER, false);
                ExpressionTool.copyLocationInfo(start, cast);
                Expression choice = Choose.makeConditional(isWhole, cast, Literal.makeLiteral(Int64Value.makeIntegerValue(0L), start));
                Expression rem = SystemFunction.makeCall("remove", start.getRetainedStaticContext(), start, choice);
                let.setAction(rem);
                return let;
            }
            case 52: {
                LetExpression let = new LetExpression();
                let.setRequiredType(SequenceType.makeSequenceType(comparand.getItemType(), 16384));
                let.setVariableQName(new StructuredQName("pp", "http://saxon.sf.net/", "pp" + let.hashCode()));
                let.setSequence(comparand);
                LocalVariableReference isWholeArg = new LocalVariableReference(let);
                LocalVariableReference arithArg = new LocalVariableReference(let);
                LocalVariableReference ceilingArg = new LocalVariableReference(let);
                Expression isWhole = VendorFunctionSetHE.getInstance().makeFunction("is-whole-number", 1).makeFunctionCall(isWholeArg);
                ArithmeticExpression plusOne = new ArithmeticExpression(arithArg, 15, Literal.makeLiteral(Int64Value.makeIntegerValue(1L), start));
                Expression ceiling = SystemFunction.makeCall("ceiling", start.getRetainedStaticContext(), ceilingArg);
                Expression choice = Choose.makeConditional(isWhole, plusOne, ceiling);
                Expression subs = SystemFunction.makeCall("subsequence", start.getRetainedStaticContext(), start, choice);
                let.setAction(subs);
                return let;
            }
            case 54: {
                Expression ceiling = SystemFunction.makeCall("ceiling", start.getRetainedStaticContext(), comparand);
                return SystemFunction.makeCall("subsequence", start.getRetainedStaticContext(), start, ceiling);
            }
        }
        throw new IllegalArgumentException("operator");
    }

    @Override
    public Expression unordered(boolean retainAllNodes, boolean forStreaming) throws XPathException {
        if (!this.filterIsPositional) {
            this.setBase(this.getBase().unordered(retainAllNodes, forStreaming));
        }
        return this;
    }

    private FilterExpression promoteIndependentPredicates(Binding[] bindings, Optimizer opt, TypeHierarchy th) {
        if (!ExpressionTool.dependsOnVariable(this.getBase(), bindings)) {
            return this;
        }
        if (this.isPositional(th)) {
            return this;
        }
        if (this.getBase() instanceof FilterExpression) {
            FilterExpression fe = (FilterExpression)this.getBase();
            if (fe.isPositional(th)) {
                return this;
            }
            if (!ExpressionTool.dependsOnVariable(fe.getFilter(), bindings)) {
                return this;
            }
            if (!ExpressionTool.dependsOnVariable(this.getFilter(), bindings)) {
                FilterExpression result = new FilterExpression(new FilterExpression(fe.getBase(), this.getFilter()).promoteIndependentPredicates(bindings, opt, th), fe.getFilter());
                opt.trace("Reordered filter predicates:", result);
                return result;
            }
        }
        return this;
    }

    public static boolean isPositionalFilter(Expression exp, TypeHierarchy th) {
        ItemType type = exp.getItemType();
        if (type.equals(BuiltInAtomicType.BOOLEAN)) {
            return FilterExpression.isExplicitlyPositional(exp);
        }
        return type.equals(BuiltInAtomicType.ANY_ATOMIC) || type instanceof AnyItemType || type.equals(BuiltInAtomicType.INTEGER) || type.equals(NumericType.getInstance()) || NumericType.isNumericType(type) || FilterExpression.isExplicitlyPositional(exp);
    }

    private static boolean isExplicitlyPositional(Expression exp) {
        return (exp.getDependencies() & 0xC) != 0;
    }

    @Override
    public int computeCardinality() {
        if (this.getFilter() instanceof Literal && ((Literal)this.getFilter()).getValue() instanceof NumericValue) {
            if (((NumericValue)((Literal)this.getFilter()).getValue()).compareTo(1L) == 0 && !Cardinality.allowsZero(this.getBase().getCardinality())) {
                return 16384;
            }
            return 24576;
        }
        if (this.filterIsIndependent) {
            ItemType filterType = this.getFilter().getItemType().getPrimitiveItemType();
            if (filterType == BuiltInAtomicType.INTEGER || filterType == BuiltInAtomicType.DOUBLE || filterType == BuiltInAtomicType.DECIMAL || filterType == BuiltInAtomicType.FLOAT) {
                return 24576;
            }
            if (this.getFilter() instanceof ArithmeticExpression) {
                return 24576;
            }
        }
        if (this.getFilter() instanceof IsLastExpression && ((IsLastExpression)this.getFilter()).getCondition()) {
            return 24576;
        }
        if (!Cardinality.allowsMany(this.getBase().getCardinality())) {
            return 24576;
        }
        return 57344;
    }

    @Override
    public int computeSpecialProperties() {
        return this.getBase().getSpecialProperties();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FilterExpression) {
            FilterExpression f = (FilterExpression)other;
            return this.getBase().isEqual(f.getBase()) && this.getFilter().isEqual(f.getFilter());
        }
        return false;
    }

    @Override
    public int computeHashCode() {
        return "FilterExpression".hashCode() + this.getBase().hashCode() + this.getFilter().hashCode();
    }

    @Override
    public Pattern toPattern(Configuration config) throws XPathException {
        Expression base = this.getSelectExpression();
        Expression filter = this.getFilter();
        TypeHierarchy th = config.getTypeHierarchy();
        Pattern basePattern = base.toPattern(config);
        if (!this.isPositional(th)) {
            return new BasePatternWithPredicate(basePattern, filter);
        }
        if (basePattern instanceof NodeTestPattern && basePattern.getItemType() instanceof NodeTest && this.filterIsPositional && base instanceof AxisExpression && ((AxisExpression)base).getAxis() == 3 && (filter.getDependencies() & 8) == 0) {
            if (filter instanceof Literal && ((Literal)filter).getValue() instanceof IntegerValue) {
                return new SimplePositionalPattern((NodeTest)basePattern.getItemType(), (int)((IntegerValue)((Literal)filter).getValue()).longValue());
            }
            return new GeneralPositionalPattern((NodeTest)basePattern.getItemType(), filter);
        }
        if (base.getItemType() instanceof NodeTest) {
            return new GeneralNodePattern(this, (NodeTest)base.getItemType());
        }
        throw new XPathException("The filtered expression in an XSLT 2.0 pattern must be a simple step");
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        SequenceIterator baseIter;
        if (this.filterIsIndependent) {
            try {
                SequenceIterator it = this.getFilter().iterate(context);
                Item first = it.next();
                if (first == null) {
                    return EmptyIterator.emptyIterator();
                }
                if (first instanceof NumericValue) {
                    if (it.next() == null) {
                        int pos = ((NumericValue)first).asSubscript();
                        if (pos != -1) {
                            if (this.getBase() instanceof VariableReference) {
                                Sequence baseVal = ((VariableReference)this.getBase()).evaluateVariable(context);
                                if (baseVal instanceof MemoClosure) {
                                    Item m = ((MemoClosure)baseVal).itemAt(pos - 1);
                                    return m == null ? EmptyIterator.emptyIterator() : m.iterate();
                                }
                                Item m = baseVal.materialize().itemAt(pos - 1);
                                return m == null ? EmptyIterator.emptyIterator() : m.iterate();
                            }
                            if (this.getBase() instanceof Literal) {
                                Item i = ((Literal)this.getBase()).getValue().itemAt(pos - 1);
                                return i == null ? EmptyIterator.emptyIterator() : i.iterate();
                            }
                            SequenceIterator baseIter2 = this.getBase().iterate(context);
                            return SubsequenceIterator.make(baseIter2, pos, pos);
                        }
                        return EmptyIterator.emptyIterator();
                    }
                } else {
                    boolean ebv = false;
                    if (first instanceof NodeInfo) {
                        ebv = true;
                    } else if (first instanceof BooleanValue) {
                        ebv = ((BooleanValue)first).getBooleanValue();
                        if (it.next() != null) {
                            ExpressionTool.ebvError("sequence of two or more items starting with a boolean value", this.getFilter());
                        }
                    } else if (first instanceof StringValue) {
                        boolean bl = ebv = !((StringValue)first).isZeroLength();
                        if (it.next() != null) {
                            ExpressionTool.ebvError("sequence of two or more items starting with a boolean value", this.getFilter());
                        }
                    } else {
                        ExpressionTool.ebvError("sequence starting with an atomic value other than a boolean, number, or string", this.getFilter());
                    }
                    if (ebv) {
                        return this.getBase().iterate(context);
                    }
                    return EmptyIterator.emptyIterator();
                }
                ExpressionTool.ebvError("sequence of two or more items starting with a numeric value", this.getFilter());
            } catch (XPathException e) {
                e.maybeSetLocation(this.getLocation());
                throw e;
            }
        }
        if ((baseIter = this.getBase().iterate(context)) instanceof EmptyIterator) {
            return baseIter;
        }
        if (this.filterIsPositional && !this.filterIsSingletonBoolean) {
            return new FilterIterator(baseIter, this.getFilter(), context);
        }
        return new FilterIterator.NonNumeric(baseIter, this.getFilter(), context);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        FilterExpression fe = new FilterExpression(this.getBase().copy(rebindings), this.getFilter().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, fe);
        fe.filterIsIndependent = this.filterIsIndependent;
        fe.filterIsPositional = this.filterIsPositional;
        fe.filterIsSingletonBoolean = this.filterIsSingletonBoolean;
        return fe;
    }

    @Override
    public String getStreamerName() {
        return "FilterExpression";
    }

    @Override
    public String toString() {
        return ExpressionTool.parenthesize(this.getBase()) + "[" + this.getFilter() + "]";
    }

    @Override
    public String toShortString() {
        return this.getBase().toShortString() + "[" + this.getFilter().toShortString() + "]";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("filter", this);
        String flags = "";
        if (this.filterIsIndependent) {
            flags = flags + "i";
        }
        if (this.filterIsPositional) {
            flags = flags + "p";
        }
        if (this.filterIsSingletonBoolean) {
            flags = flags + "b";
        }
        out.emitAttribute("flags", flags);
        this.getBase().export(out);
        this.getFilter().export(out);
        out.endElement();
    }

    public void setFlags(String flags) {
        this.filterIsIndependent = flags.contains("i");
        this.filterIsPositional = flags.contains("p");
        this.filterIsSingletonBoolean = flags.contains("b");
    }
}

