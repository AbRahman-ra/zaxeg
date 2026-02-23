/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.ComparisonExpression;
import net.sf.saxon.expr.EquivalenceComparer;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;

public class EquivalenceComparison
extends BinaryExpression
implements ComparisonExpression {
    private AtomicComparer comparer;
    private boolean knownToBeComparable = false;

    public EquivalenceComparison(Expression p1, int operator, Expression p2) {
        super(p1, operator, p2);
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        StaticContext env = visitor.getStaticContext();
        String defaultCollationName = env.getDefaultCollationName();
        Configuration config = visitor.getConfiguration();
        StringCollator collation = config.getCollation(defaultCollationName);
        if (collation == null) {
            collation = CodepointCollator.getInstance();
        }
        this.comparer = new EquivalenceComparer(collation, config.getConversionContext());
        Expression oldOp0 = this.getLhsExpression();
        Expression oldOp1 = this.getRhsExpression();
        this.getLhs().typeCheck(visitor, contextInfo);
        this.getRhs().typeCheck(visitor, contextInfo);
        this.setLhsExpression(this.getLhsExpression().unordered(false, false));
        this.setRhsExpression(this.getRhsExpression().unordered(false, false));
        SequenceType atomicType = SequenceType.OPTIONAL_ATOMIC;
        TypeChecker tc = config.getTypeChecker(false);
        RoleDiagnostic role0 = new RoleDiagnostic(1, "eq", 0);
        this.setLhsExpression(tc.staticTypeCheck(this.getLhsExpression(), atomicType, role0, visitor));
        RoleDiagnostic role1 = new RoleDiagnostic(1, "eq", 1);
        this.setRhsExpression(tc.staticTypeCheck(this.getRhsExpression(), atomicType, role1, visitor));
        if (this.getLhsExpression() != oldOp0) {
            this.adoptChildExpression(this.getLhsExpression());
        }
        if (this.getRhsExpression() != oldOp1) {
            this.adoptChildExpression(this.getRhsExpression());
        }
        ItemType t0 = this.getLhsExpression().getItemType();
        ItemType t1 = this.getRhsExpression().getItemType();
        if (t0 instanceof ErrorType) {
            t0 = BuiltInAtomicType.ANY_ATOMIC;
        }
        if (t1 instanceof ErrorType) {
            t1 = BuiltInAtomicType.ANY_ATOMIC;
        }
        if (t0.getUType().union(t1.getUType()).overlaps(UType.EXTENSION)) {
            XPathException err = new XPathException("Cannot perform comparisons involving external objects");
            err.setIsTypeError(true);
            err.setErrorCode("XPTY0004");
            err.setLocation(this.getLocation());
            throw err;
        }
        BuiltInAtomicType pt0 = (BuiltInAtomicType)t0.getPrimitiveItemType();
        BuiltInAtomicType pt1 = (BuiltInAtomicType)t1.getPrimitiveItemType();
        if (!(t0.equals(BuiltInAtomicType.ANY_ATOMIC) || t0.equals(BuiltInAtomicType.UNTYPED_ATOMIC) || t1.equals(BuiltInAtomicType.ANY_ATOMIC) || t1.equals(BuiltInAtomicType.UNTYPED_ATOMIC))) {
            if (Type.isGuaranteedComparable(pt0, pt1, false)) {
                this.knownToBeComparable = true;
            } else if (!Type.isPossiblyComparable(pt0, pt1, false)) {
                env.issueWarning("Cannot compare " + t0.toString() + " to " + t1.toString(), this.getLocation());
            }
        }
        try {
            if (this.getLhsExpression() instanceof Literal && this.getRhsExpression() instanceof Literal) {
                GroundedValue v = this.evaluateItem(visitor.getStaticContext().makeEarlyEvaluationContext()).materialize();
                return Literal.makeLiteral(v, this);
            }
        } catch (XPathException xPathException) {
            // empty catch block
        }
        return this;
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
        return false;
    }

    @Override
    public int computeCardinality() {
        return 16384;
    }

    @Override
    public ItemType getItemType() {
        return BuiltInAtomicType.BOOLEAN;
    }

    public boolean isKnownToBeComparable() {
        return this.knownToBeComparable;
    }

    public AtomicComparer getComparer() {
        return this.comparer;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        EquivalenceComparison sc = new EquivalenceComparison(this.getLhsExpression().copy(rebindings), this.operator, this.getRhsExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, sc);
        sc.comparer = this.comparer;
        sc.knownToBeComparable = this.knownToBeComparable;
        return sc;
    }

    @Override
    public BooleanValue evaluateItem(XPathContext context) throws XPathException {
        return BooleanValue.get(this.effectiveBooleanValue(context));
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        AtomicValue v0 = (AtomicValue)this.getLhsExpression().evaluateItem(context);
        AtomicValue v1 = (AtomicValue)this.getRhsExpression().evaluateItem(context);
        if (v0 == null || v1 == null) {
            return v0 == v1;
        }
        AtomicComparer comp2 = this.comparer.provideContext(context);
        return (this.knownToBeComparable || Type.isGuaranteedComparable(v0.getPrimitiveType(), v1.getPrimitiveType(), false)) && comp2.comparesEqual(v0, v1);
    }

    @Override
    public String getExpressionName() {
        return "equivalent";
    }

    @Override
    protected void explainExtraAttributes(ExpressionPresenter out) {
        out.emitAttribute("cardinality", "singleton");
    }
}

