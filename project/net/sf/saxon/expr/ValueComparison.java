/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.ComparisonExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Negatable;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.expr.sort.ComparisonException;
import net.sf.saxon.expr.sort.GenericAtomicComparer;
import net.sf.saxon.expr.sort.UntypedNumericComparer;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

public final class ValueComparison
extends BinaryExpression
implements ComparisonExpression,
Negatable {
    private AtomicComparer comparer;
    private BooleanValue resultWhenEmpty = null;
    private boolean needsRuntimeCheck;

    public ValueComparison(Expression p1, int op, Expression p2) {
        super(p1, op, p2);
    }

    @Override
    public String getExpressionName() {
        return "ValueComparison";
    }

    public void setAtomicComparer(AtomicComparer comparer) {
        this.comparer = comparer;
    }

    @Override
    public AtomicComparer getAtomicComparer() {
        return this.comparer;
    }

    @Override
    public int getSingletonOperator() {
        return this.operator;
    }

    @Override
    public boolean convertsUntypedToOther() {
        return this.comparer instanceof UntypedNumericComparer;
    }

    public void setResultWhenEmpty(BooleanValue value) {
        this.resultWhenEmpty = value;
    }

    public BooleanValue getResultWhenEmpty() {
        return this.resultWhenEmpty;
    }

    public boolean needsRuntimeComparabilityCheck() {
        return this.needsRuntimeCheck;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        BuiltInAtomicType p1;
        this.resetLocalStaticProperties();
        this.getLhs().typeCheck(visitor, contextInfo);
        this.getRhs().typeCheck(visitor, contextInfo);
        Configuration config = visitor.getConfiguration();
        StaticContext env = visitor.getStaticContext();
        if (Literal.isEmptySequence(this.getLhsExpression())) {
            return this.resultWhenEmpty == null ? this.getLhsExpression() : Literal.makeLiteral(this.resultWhenEmpty, this);
        }
        if (Literal.isEmptySequence(this.getRhsExpression())) {
            return this.resultWhenEmpty == null ? this.getRhsExpression() : Literal.makeLiteral(this.resultWhenEmpty, this);
        }
        if (this.comparer instanceof UntypedNumericComparer) {
            return this;
        }
        SequenceType optionalAtomic = SequenceType.OPTIONAL_ATOMIC;
        TypeChecker tc = config.getTypeChecker(false);
        RoleDiagnostic role0 = new RoleDiagnostic(1, Token.tokens[this.operator], 0);
        this.setLhsExpression(tc.staticTypeCheck(this.getLhsExpression(), optionalAtomic, role0, visitor));
        RoleDiagnostic role1 = new RoleDiagnostic(1, Token.tokens[this.operator], 1);
        this.setRhsExpression(tc.staticTypeCheck(this.getRhsExpression(), optionalAtomic, role1, visitor));
        PlainType t0 = this.getLhsExpression().getItemType().getAtomizedItemType();
        PlainType t1 = this.getRhsExpression().getItemType().getAtomizedItemType();
        if (t0.getUType().union(t1.getUType()).overlaps(UType.EXTENSION)) {
            XPathException err = new XPathException("Cannot perform comparisons involving external objects");
            err.setIsTypeError(true);
            err.setErrorCode("XPTY0004");
            err.setLocation(this.getLocation());
            throw err;
        }
        BuiltInAtomicType p0 = (BuiltInAtomicType)t0.getPrimitiveItemType();
        if (p0.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
            p0 = BuiltInAtomicType.STRING;
        }
        if ((p1 = (BuiltInAtomicType)t1.getPrimitiveItemType()).equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
            p1 = BuiltInAtomicType.STRING;
        }
        boolean bl = this.needsRuntimeCheck = p0.equals(BuiltInAtomicType.ANY_ATOMIC) || p1.equals(BuiltInAtomicType.ANY_ATOMIC);
        if (!this.needsRuntimeCheck && !Type.isPossiblyComparable(p0, p1, Token.isOrderedOperator(this.operator))) {
            boolean opt0 = Cardinality.allowsZero(this.getLhsExpression().getCardinality());
            boolean opt1 = Cardinality.allowsZero(this.getRhsExpression().getCardinality());
            if (opt0 || opt1) {
                String which = null;
                if (opt0) {
                    which = "the first operand is";
                }
                if (opt1) {
                    which = "the second operand is";
                }
                if (opt0 && opt1) {
                    which = "one or both operands are";
                }
                visitor.getStaticContext().issueWarning("Comparison of " + t0.toString() + (opt0 ? "?" : "") + " to " + t1.toString() + (opt1 ? "?" : "") + " will fail unless " + which + " empty", this.getLocation());
                this.needsRuntimeCheck = true;
            } else {
                String message = "In {" + this.toShortString() + "}: cannot compare " + t0.toString() + " to " + t1.toString();
                XPathException err = new XPathException(message);
                err.setIsTypeError(true);
                err.setErrorCode("XPTY0004");
                err.setLocation(this.getLocation());
                throw err;
            }
        }
        if (this.operator != 50 && this.operator != 51) {
            this.mustBeOrdered(t0, p0);
            this.mustBeOrdered(t1, p1);
        }
        if (this.comparer == null) {
            String defaultCollationName = env.getDefaultCollationName();
            StringCollator comp = config.getCollation(defaultCollationName);
            if (comp == null) {
                comp = CodepointCollator.getInstance();
            }
            this.comparer = GenericAtomicComparer.makeAtomicComparer(p0, p1, comp, env.getConfiguration().getConversionContext());
        }
        return this;
    }

    private void mustBeOrdered(PlainType t1, BuiltInAtomicType p1) throws XPathException {
        if (!p1.isOrdered(true)) {
            XPathException err = new XPathException("Type " + t1.toString() + " is not an ordered type");
            err.setErrorCode("XPTY0004");
            err.setIsTypeError(true);
            err.setLocation(this.getLocation());
            throw err;
        }
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getLhs().optimize(visitor, contextInfo);
        this.getRhs().optimize(visitor, contextInfo);
        return visitor.obtainOptimizer().optimizeValueComparison(this, visitor, contextInfo);
    }

    @Override
    public boolean isNegatable(TypeHierarchy th) {
        return this.isNeverNaN(this.getLhsExpression(), th) && this.isNeverNaN(this.getRhsExpression(), th);
    }

    private boolean isNeverNaN(Expression exp, TypeHierarchy th) {
        return th.relationship(exp.getItemType(), BuiltInAtomicType.DOUBLE) == Affinity.DISJOINT && th.relationship(exp.getItemType(), BuiltInAtomicType.FLOAT) == Affinity.DISJOINT;
    }

    @Override
    public Expression negate() {
        ValueComparison vc = new ValueComparison(this.getLhsExpression(), Token.negate(this.operator), this.getRhsExpression());
        vc.comparer = this.comparer;
        vc.resultWhenEmpty = this.resultWhenEmpty == null || this.resultWhenEmpty == BooleanValue.FALSE ? BooleanValue.TRUE : BooleanValue.FALSE;
        ExpressionTool.copyLocationInfo(this, vc);
        return vc;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ValueComparison && super.equals(other) && this.comparer.equals(((ValueComparison)other).comparer);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ValueComparison vc = new ValueComparison(this.getLhsExpression().copy(rebindings), this.operator, this.getRhsExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, vc);
        vc.comparer = this.comparer;
        vc.resultWhenEmpty = this.resultWhenEmpty;
        vc.needsRuntimeCheck = this.needsRuntimeCheck;
        return vc;
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        try {
            AtomicValue v0 = (AtomicValue)this.getLhsExpression().evaluateItem(context);
            if (v0 == null) {
                return this.resultWhenEmpty == BooleanValue.TRUE;
            }
            AtomicValue v1 = (AtomicValue)this.getRhsExpression().evaluateItem(context);
            if (v1 == null) {
                return this.resultWhenEmpty == BooleanValue.TRUE;
            }
            return ValueComparison.compare(v0, this.operator, v1, this.comparer.provideContext(context), this.needsRuntimeCheck);
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            e.maybeSetContext(context);
            throw e;
        }
    }

    public static boolean compare(AtomicValue v0, int op, AtomicValue v1, AtomicComparer comparer, boolean checkTypes) throws XPathException {
        if (checkTypes && !Type.isGuaranteedComparable(v0.getPrimitiveType(), v1.getPrimitiveType(), Token.isOrderedOperator(op))) {
            XPathException e2 = new XPathException("Cannot compare " + Type.displayTypeName(v0) + " to " + Type.displayTypeName(v1));
            e2.setErrorCode("XPTY0004");
            e2.setIsTypeError(true);
            throw e2;
        }
        if (v0.isNaN() || v1.isNaN()) {
            return op == 51;
        }
        try {
            switch (op) {
                case 50: {
                    return comparer.comparesEqual(v0, v1);
                }
                case 51: {
                    return !comparer.comparesEqual(v0, v1);
                }
                case 52: {
                    return comparer.compareAtomicValues(v0, v1) > 0;
                }
                case 53: {
                    return comparer.compareAtomicValues(v0, v1) < 0;
                }
                case 54: {
                    return comparer.compareAtomicValues(v0, v1) >= 0;
                }
                case 55: {
                    return comparer.compareAtomicValues(v0, v1) <= 0;
                }
            }
            throw new UnsupportedOperationException("Unknown operator " + op);
        } catch (ComparisonException err) {
            throw err.getCause();
        } catch (ClassCastException err) {
            err.printStackTrace();
            XPathException e2 = new XPathException("Cannot compare " + Type.displayTypeName(v0) + " to " + Type.displayTypeName(v1));
            e2.setErrorCode("XPTY0004");
            e2.setIsTypeError(true);
            throw e2;
        }
    }

    @Override
    public BooleanValue evaluateItem(XPathContext context) throws XPathException {
        try {
            AtomicValue v0 = (AtomicValue)this.getLhsExpression().evaluateItem(context);
            if (v0 == null) {
                return this.resultWhenEmpty;
            }
            AtomicValue v1 = (AtomicValue)this.getRhsExpression().evaluateItem(context);
            if (v1 == null) {
                return this.resultWhenEmpty;
            }
            return BooleanValue.get(ValueComparison.compare(v0, this.operator, v1, this.comparer.provideContext(context), this.needsRuntimeCheck));
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            e.maybeSetContext(context);
            throw e;
        }
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
        if (this.resultWhenEmpty != null) {
            return 16384;
        }
        return super.computeCardinality();
    }

    @Override
    public String tag() {
        return "vc";
    }

    @Override
    protected void explainExtraAttributes(ExpressionPresenter out) {
        if (this.resultWhenEmpty != null) {
            out.emitAttribute("onEmpty", this.resultWhenEmpty.getBooleanValue() ? "1" : "0");
        }
        out.emitAttribute("comp", this.comparer.save());
    }
}

