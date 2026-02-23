/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.HashSet;
import java.util.Set;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AndExpression;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.DifferenceEnumeration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.IntersectionEnumeration;
import net.sf.saxon.expr.ItemChecker;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OrExpression;
import net.sf.saxon.expr.SingletonIntersectExpression;
import net.sf.saxon.expr.SlashExpression;
import net.sf.saxon.expr.TailExpression;
import net.sf.saxon.expr.UnionEnumeration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.expr.sort.DocumentSorter;
import net.sf.saxon.expr.sort.GlobalOrderComparer;
import net.sf.saxon.functions.CurrentGroupCall;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.CombinedNodeTest;
import net.sf.saxon.pattern.ExceptPattern;
import net.sf.saxon.pattern.IntersectPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.pattern.UnionPattern;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

public class VennExpression
extends BinaryExpression {
    public VennExpression(Expression p1, int op, Expression p2) {
        super(p1, op, p2);
    }

    @Override
    public Expression simplify() throws XPathException {
        if (!(this.getLhsExpression() instanceof DocumentSorter)) {
            this.setLhsExpression(new DocumentSorter(this.getLhsExpression()));
        }
        if (!(this.getRhsExpression() instanceof DocumentSorter)) {
            this.setRhsExpression(new DocumentSorter(this.getRhsExpression()));
        }
        super.simplify();
        return this;
    }

    @Override
    public String getExpressionName() {
        switch (this.operator) {
            case 1: {
                return "union";
            }
            case 23: {
                return "intersect";
            }
            case 24: {
                return "except";
            }
        }
        return "unknown";
    }

    @Override
    public final ItemType getItemType() {
        ItemType t1 = this.getLhsExpression().getItemType();
        if (this.operator == 1) {
            ItemType t2 = this.getRhsExpression().getItemType();
            TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
            return Type.getCommonSuperType(t1, t2, th);
        }
        return t1;
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        switch (this.operator) {
            case 1: {
                return this.getLhsExpression().getStaticUType(contextItemType).union(this.getRhsExpression().getStaticUType(contextItemType));
            }
            case 23: {
                return this.getLhsExpression().getStaticUType(contextItemType).intersection(this.getRhsExpression().getStaticUType(contextItemType));
            }
        }
        return this.getLhsExpression().getStaticUType(contextItemType);
    }

    @Override
    public final int computeCardinality() {
        int c1 = this.getLhsExpression().getCardinality();
        int c2 = this.getRhsExpression().getCardinality();
        switch (this.operator) {
            case 1: {
                if (Literal.isEmptySequence(this.getLhsExpression())) {
                    return c2;
                }
                if (Literal.isEmptySequence(this.getRhsExpression())) {
                    return c1;
                }
                return c1 | c2 | 0x4000 | 0x8000;
            }
            case 23: {
                if (Literal.isEmptySequence(this.getLhsExpression())) {
                    return 8192;
                }
                if (Literal.isEmptySequence(this.getRhsExpression())) {
                    return 8192;
                }
                return c1 & c2 | 0x2000 | 0x4000;
            }
            case 24: {
                if (Literal.isEmptySequence(this.getLhsExpression())) {
                    return 8192;
                }
                if (Literal.isEmptySequence(this.getRhsExpression())) {
                    return c1;
                }
                return c1 | 0x2000 | 0x4000;
            }
        }
        return 57344;
    }

    @Override
    public int computeSpecialProperties() {
        int prop0 = this.getLhsExpression().getSpecialProperties();
        int prop1 = this.getRhsExpression().getSpecialProperties();
        int props = 131072;
        if (this.testContextDocumentNodeSet(prop0, prop1)) {
            props |= 0x10000;
        }
        if (this.testSubTree(prop0, prop1)) {
            props |= 0x100000;
        }
        if (this.createsNoNewNodes(prop0, prop1)) {
            props |= 0x800000;
        }
        return props;
    }

    private boolean testContextDocumentNodeSet(int prop0, int prop1) {
        switch (this.operator) {
            case 1: {
                return (prop0 & prop1 & 0x10000) != 0;
            }
            case 23: {
                return ((prop0 | prop1) & 0x10000) != 0;
            }
            case 24: {
                return (prop0 & 0x10000) != 0;
            }
        }
        return false;
    }

    public void gatherComponents(int operator, Set<Expression> set) {
        if (this.getLhsExpression() instanceof VennExpression && ((VennExpression)this.getLhsExpression()).operator == operator) {
            ((VennExpression)this.getLhsExpression()).gatherComponents(operator, set);
        } else {
            set.add(this.getLhsExpression());
        }
        if (this.getRhsExpression() instanceof VennExpression && ((VennExpression)this.getRhsExpression()).operator == operator) {
            ((VennExpression)this.getRhsExpression()).gatherComponents(operator, set);
        } else {
            set.add(this.getRhsExpression());
        }
    }

    private boolean testSubTree(int prop0, int prop1) {
        switch (this.operator) {
            case 1: {
                return (prop0 & prop1 & 0x100000) != 0;
            }
            case 23: {
                return ((prop0 | prop1) & 0x100000) != 0;
            }
            case 24: {
                return (prop0 & 0x100000) != 0;
            }
        }
        return false;
    }

    private boolean createsNoNewNodes(int prop0, int prop1) {
        return (prop0 & 0x800000) != 0 && (prop1 & 0x800000) != 0;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        ItemType t1;
        ItemType t0;
        TypeHierarchy th;
        Configuration config = visitor.getConfiguration();
        TypeChecker tc = config.getTypeChecker(false);
        this.getLhs().typeCheck(visitor, contextInfo);
        this.getRhs().typeCheck(visitor, contextInfo);
        if (!(this.getLhsExpression() instanceof Pattern)) {
            RoleDiagnostic role0 = new RoleDiagnostic(1, Token.tokens[this.operator], 0);
            this.setLhsExpression(tc.staticTypeCheck(this.getLhsExpression(), SequenceType.NODE_SEQUENCE, role0, visitor));
        }
        if (!(this.getRhsExpression() instanceof Pattern)) {
            RoleDiagnostic role1 = new RoleDiagnostic(1, Token.tokens[this.operator], 1);
            this.setRhsExpression(tc.staticTypeCheck(this.getRhsExpression(), SequenceType.NODE_SEQUENCE, role1, visitor));
        }
        if (this.operator != 1 && (th = config.getTypeHierarchy()).relationship(t0 = this.getLhsExpression().getItemType(), t1 = this.getRhsExpression().getItemType()) == Affinity.DISJOINT) {
            if (this.operator == 23) {
                return Literal.makeEmptySequence();
            }
            if (this.getLhsExpression().hasSpecialProperty(131072)) {
                return this.getLhsExpression();
            }
            return new DocumentSorter(this.getLhsExpression());
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression e = super.optimize(visitor, contextItemType);
        if (e != this) {
            return e;
        }
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        Expression lhs = this.getLhsExpression();
        Expression rhs = this.getRhsExpression();
        switch (this.operator) {
            case 1: {
                if (Literal.isEmptySequence(lhs) && (rhs.getSpecialProperties() & 0x20000) != 0) {
                    return rhs;
                }
                if (Literal.isEmptySequence(rhs) && (lhs.getSpecialProperties() & 0x20000) != 0) {
                    return lhs;
                }
                if (!(lhs instanceof CurrentGroupCall) || !(rhs instanceof ContextItemExpression)) break;
                return lhs;
            }
            case 23: {
                if (Literal.isEmptySequence(lhs)) {
                    return lhs;
                }
                if (Literal.isEmptySequence(rhs)) {
                    return rhs;
                }
                if (!(lhs instanceof CurrentGroupCall) || !(rhs instanceof ContextItemExpression)) break;
                return rhs;
            }
            case 24: {
                if (Literal.isEmptySequence(lhs)) {
                    return lhs;
                }
                if (Literal.isEmptySequence(rhs) && (lhs.getSpecialProperties() & 0x20000) != 0) {
                    return lhs;
                }
                if (!(lhs instanceof CurrentGroupCall) || !(rhs instanceof ContextItemExpression)) break;
                return new TailExpression(lhs, 2);
            }
        }
        if (lhs instanceof AxisExpression && rhs instanceof AxisExpression) {
            AxisExpression a1 = (AxisExpression)lhs;
            AxisExpression a2 = (AxisExpression)rhs;
            if (a1.getAxis() == a2.getAxis()) {
                if (a1.getNodeTest().equals(a2.getNodeTest())) {
                    return this.operator == 24 ? Literal.makeEmptySequence() : a1;
                }
                AxisExpression ax = new AxisExpression(a1.getAxis(), new CombinedNodeTest(a1.getNodeTest(), this.operator, a2.getNodeTest()));
                ExpressionTool.copyLocationInfo(this, ax);
                return ax;
            }
        }
        if (lhs instanceof SlashExpression && rhs instanceof SlashExpression && this.operator == 1) {
            SlashExpression path1 = (SlashExpression)lhs;
            SlashExpression path2 = (SlashExpression)rhs;
            if (path1.getFirstStep().isEqual(path2.getFirstStep())) {
                VennExpression venn = new VennExpression(path1.getRemainingSteps(), this.operator, path2.getRemainingSteps());
                ExpressionTool.copyLocationInfo(this, venn);
                Expression path = ExpressionTool.makePathExpression(path1.getFirstStep(), venn);
                ExpressionTool.copyLocationInfo(this, path);
                return path.optimize(visitor, contextItemType);
            }
        }
        if (lhs instanceof FilterExpression && rhs instanceof FilterExpression) {
            FilterExpression exp0 = (FilterExpression)lhs;
            FilterExpression exp1 = (FilterExpression)rhs;
            if (!exp0.isPositional(th) && !exp1.isPositional(th) && exp0.getSelectExpression().isEqual(exp1.getSelectExpression())) {
                BooleanExpression filter;
                switch (this.operator) {
                    case 1: {
                        filter = new OrExpression(exp0.getFilter(), exp1.getFilter());
                        break;
                    }
                    case 23: {
                        filter = new AndExpression(exp0.getFilter(), exp1.getFilter());
                        break;
                    }
                    case 24: {
                        Expression negate2 = SystemFunction.makeCall("not", this.getRetainedStaticContext(), exp1.getFilter());
                        filter = new AndExpression(exp0.getFilter(), negate2);
                        break;
                    }
                    default: {
                        throw new AssertionError((Object)("Unknown operator " + this.operator));
                    }
                }
                ExpressionTool.copyLocationInfo(this, filter);
                FilterExpression f = new FilterExpression(exp0.getSelectExpression(), filter);
                ExpressionTool.copyLocationInfo(this, f);
                return f.simplify().typeCheck(visitor, contextItemType).optimize(visitor, contextItemType);
            }
        }
        if (!visitor.isOptimizeForStreaming() && this.operator == 1 && lhs instanceof AxisExpression && rhs instanceof AxisExpression) {
            AxisExpression a0 = (AxisExpression)lhs;
            AxisExpression a1 = (AxisExpression)rhs;
            if (a0.getAxis() == 2 && a1.getAxis() == 3) {
                return new Block(new Expression[]{lhs, rhs});
            }
            if (a1.getAxis() == 2 && a0.getAxis() == 3) {
                return new Block(new Expression[]{rhs, lhs});
            }
        }
        if (this.operator == 23 && !Cardinality.allowsMany(lhs.getCardinality())) {
            return new SingletonIntersectExpression(lhs, this.operator, rhs.unordered(false, false));
        }
        if (this.operator == 23 && !Cardinality.allowsMany(rhs.getCardinality())) {
            return new SingletonIntersectExpression(rhs, this.operator, lhs.unordered(false, false));
        }
        if (this.operandsAreDisjoint(th)) {
            if (this.operator == 23) {
                return Literal.makeEmptySequence();
            }
            if (this.operator == 24) {
                if ((lhs.getSpecialProperties() & 0x20000) != 0) {
                    return lhs;
                }
                return new DocumentSorter(lhs);
            }
        }
        return this;
    }

    private boolean operandsAreDisjoint(TypeHierarchy th) {
        return th.relationship(this.getLhsExpression().getItemType(), this.getRhsExpression().getItemType()) == Affinity.DISJOINT;
    }

    @Override
    public Expression unordered(boolean retainAllNodes, boolean forStreaming) {
        if (this.operator == 1 && !forStreaming && this.operandsAreDisjoint(this.getConfiguration().getTypeHierarchy())) {
            Block block = new Block(new Expression[]{this.getLhsExpression(), this.getRhsExpression()});
            ExpressionTool.copyLocationInfo(this, block);
            return block;
        }
        return this;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        VennExpression exp = new VennExpression(this.getLhsExpression().copy(rebindings), this.operator, this.getRhsExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    protected OperandRole getOperandRole(int arg) {
        return OperandRole.SAME_FOCUS_ACTION;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof VennExpression) {
            VennExpression b = (VennExpression)other;
            if (this.operator != b.operator) {
                return false;
            }
            if (this.getLhsExpression().isEqual(b.getLhsExpression()) && this.getRhsExpression().isEqual(b.getRhsExpression())) {
                return true;
            }
            if (this.operator == 1 || this.operator == 23) {
                HashSet<Expression> s0 = new HashSet<Expression>(10);
                this.gatherComponents(this.operator, s0);
                HashSet<Expression> s1 = new HashSet<Expression>(10);
                ((VennExpression)other).gatherComponents(this.operator, s1);
                return s0.equals(s1);
            }
        }
        return false;
    }

    @Override
    public int computeHashCode() {
        return this.getLhsExpression().hashCode() ^ this.getRhsExpression().hashCode();
    }

    @Override
    public Pattern toPattern(Configuration config) throws XPathException {
        if (this.isPredicatePattern(this.getLhsExpression()) || this.isPredicatePattern(this.getRhsExpression())) {
            throw new XPathException("Cannot use a predicate pattern as an operand of a union, intersect, or except operator", "XTSE0340");
        }
        if (this.operator == 1) {
            return new UnionPattern(this.getLhsExpression().toPattern(config), this.getRhsExpression().toPattern(config));
        }
        if (this.operator == 24) {
            return new ExceptPattern(this.getLhsExpression().toPattern(config), this.getRhsExpression().toPattern(config));
        }
        return new IntersectPattern(this.getLhsExpression().toPattern(config), this.getRhsExpression().toPattern(config));
    }

    private boolean isPredicatePattern(Expression exp) {
        if (exp instanceof ItemChecker) {
            exp = ((ItemChecker)exp).getBaseExpression();
        }
        return exp instanceof FilterExpression && ((FilterExpression)exp).getSelectExpression() instanceof ContextItemExpression;
    }

    @Override
    protected String tag() {
        if (this.operator == 1) {
            return "union";
        }
        return Token.tokens[this.operator];
    }

    @Override
    public SequenceIterator iterate(XPathContext c) throws XPathException {
        SequenceIterator i1 = this.getLhsExpression().iterate(c);
        SequenceIterator i2 = this.getRhsExpression().iterate(c);
        switch (this.operator) {
            case 1: {
                return new UnionEnumeration(i1, i2, GlobalOrderComparer.getInstance());
            }
            case 23: {
                return new IntersectionEnumeration(i1, i2, GlobalOrderComparer.getInstance());
            }
            case 24: {
                return new DifferenceEnumeration(i1, i2, GlobalOrderComparer.getInstance());
            }
        }
        throw new UnsupportedOperationException("Unknown operator in Venn Expression");
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        if (this.operator == 1) {
            return this.getLhsExpression().effectiveBooleanValue(context) || this.getRhsExpression().effectiveBooleanValue(context);
        }
        return super.effectiveBooleanValue(context);
    }

    @Override
    public String getStreamerName() {
        return "VennExpression";
    }
}

