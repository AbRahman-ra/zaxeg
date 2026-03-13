/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.compat;

import java.util.ArrayList;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.GeneralComparison;
import net.sf.saxon.expr.GeneralComparison20;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.expr.sort.GenericAtomicComparer;
import net.sf.saxon.functions.Number_1;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.PrependSequenceIterator;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.StringValue;

public class GeneralComparison10
extends BinaryExpression
implements Callable {
    protected int singletonOperator;
    protected AtomicComparer comparer;
    private boolean atomize0 = true;
    private boolean atomize1 = true;
    private boolean maybeBoolean0 = true;
    private boolean maybeBoolean1 = true;

    public GeneralComparison10(Expression p0, int op, Expression p1) {
        super(p0, op, p1);
        this.singletonOperator = GeneralComparison.getCorrespondingSingletonOperator(op);
    }

    @Override
    public int computeCardinality() {
        return 16384;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getLhs().typeCheck(visitor, contextInfo);
        this.getRhs().typeCheck(visitor, contextInfo);
        StaticContext env = visitor.getStaticContext();
        StringCollator comp = visitor.getConfiguration().getCollation(env.getDefaultCollationName());
        if (comp == null) {
            comp = CodepointCollator.getInstance();
        }
        XPathContext context = env.makeEarlyEvaluationContext();
        this.comparer = new GenericAtomicComparer(comp, context);
        if (this.getLhsExpression() instanceof Literal && this.getRhsExpression() instanceof Literal) {
            return Literal.makeLiteral(this.evaluateItem(context), this);
        }
        return this;
    }

    public void setAtomicComparer(AtomicComparer comparer) {
        this.comparer = comparer;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Configuration config = visitor.getConfiguration();
        StaticContext env = visitor.getStaticContext();
        this.getLhs().optimize(visitor, contextInfo);
        this.getRhs().optimize(visitor, contextInfo);
        this.setLhsExpression(this.getLhsExpression().unordered(false, false));
        this.setRhsExpression(this.getRhsExpression().unordered(false, false));
        if (this.getLhsExpression() instanceof Literal && this.getRhsExpression() instanceof Literal) {
            return Literal.makeLiteral(this.evaluateItem(env.makeEarlyEvaluationContext()), this);
        }
        TypeHierarchy th = config.getTypeHierarchy();
        ItemType type0 = this.getLhsExpression().getItemType();
        ItemType type1 = this.getRhsExpression().getItemType();
        if (type0.isPlainType()) {
            this.atomize0 = false;
        }
        if (type1.isPlainType()) {
            this.atomize1 = false;
        }
        if (th.relationship(type0, BuiltInAtomicType.BOOLEAN) == Affinity.DISJOINT) {
            this.maybeBoolean0 = false;
        }
        if (th.relationship(type1, BuiltInAtomicType.BOOLEAN) == Affinity.DISJOINT) {
            this.maybeBoolean1 = false;
        }
        if (!this.maybeBoolean0 && !this.maybeBoolean1) {
            boolean numeric1;
            if (!(type0 instanceof AtomicType)) {
                this.setLhsExpression(Atomizer.makeAtomizer(this.getLhsExpression(), null).simplify());
                type0 = this.getLhsExpression().getItemType();
            }
            if (!(type1 instanceof AtomicType)) {
                this.setRhsExpression(Atomizer.makeAtomizer(this.getRhsExpression(), null).simplify());
                type1 = this.getRhsExpression().getItemType();
            }
            Affinity n0 = th.relationship(type0, NumericType.getInstance());
            Affinity n1 = th.relationship(type1, NumericType.getInstance());
            boolean maybeNumeric0 = n0 != Affinity.DISJOINT;
            boolean maybeNumeric1 = n1 != Affinity.DISJOINT;
            boolean numeric0 = n0 == Affinity.SUBSUMED_BY || n0 == Affinity.SAME_TYPE;
            boolean bl = numeric1 = n1 == Affinity.SUBSUMED_BY || n1 == Affinity.SAME_TYPE;
            if (this.operator == 6 || this.operator == 22) {
                if (!maybeNumeric0 && !maybeNumeric1 || numeric0 && numeric1) {
                    GeneralComparison20 gc = new GeneralComparison20(this.getLhsExpression(), this.operator, this.getRhsExpression());
                    gc.setRetainedStaticContext(this.getRetainedStaticContext());
                    gc.setAtomicComparer(this.comparer);
                    Expression binExp = visitor.obtainOptimizer().optimizeGeneralComparison(visitor, gc, false, contextInfo);
                    ExpressionTool.copyLocationInfo(this, binExp);
                    return binExp.typeCheck(visitor, contextInfo).optimize(visitor, contextInfo);
                }
            } else if (numeric0 && numeric1) {
                GeneralComparison20 gc = new GeneralComparison20(this.getLhsExpression(), this.operator, this.getRhsExpression());
                gc.setRetainedStaticContext(this.getRetainedStaticContext());
                Expression binExp = visitor.obtainOptimizer().optimizeGeneralComparison(visitor, gc, false, contextInfo);
                ExpressionTool.copyLocationInfo(this, binExp);
                return binExp.typeCheck(visitor, contextInfo).optimize(visitor, contextInfo);
            }
        }
        return this;
    }

    @Override
    public BooleanValue evaluateItem(XPathContext context) throws XPathException {
        return BooleanValue.get(this.effectiveBooleanValue(context));
    }

    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        return BooleanValue.get(this.effectiveBooleanValue(arguments[0].iterate(), arguments[1].iterate(), context));
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        return this.effectiveBooleanValue(this.getLhsExpression().iterate(context), this.getRhsExpression().iterate(context), context);
    }

    private boolean effectiveBooleanValue(SequenceIterator iter0, SequenceIterator iter1, XPathContext context) throws XPathException {
        AtomicValue item0;
        boolean iter1used = false;
        if (this.maybeBoolean0) {
            Item i02;
            Item i01 = iter0.next();
            Item item = i02 = i01 == null ? null : iter0.next();
            if (i01 instanceof BooleanValue && i02 == null) {
                iter0.close();
                boolean b = ExpressionTool.effectiveBooleanValue(iter1);
                return GeneralComparison10.compare((BooleanValue)i01, this.singletonOperator, BooleanValue.get(b), this.comparer, context);
            }
            if (i01 == null && !this.maybeBoolean1) {
                iter0.close();
                return false;
            }
            if (i02 != null) {
                iter0 = new PrependSequenceIterator(i02, iter0);
            }
            if (i01 != null) {
                iter0 = new PrependSequenceIterator(i01, iter0);
            }
        }
        if (this.maybeBoolean1) {
            Item i12;
            Item i11 = iter1.next();
            Item item = i12 = i11 == null ? null : iter1.next();
            if (i11 instanceof BooleanValue && i12 == null) {
                iter1.close();
                boolean b = ExpressionTool.effectiveBooleanValue(iter0);
                return GeneralComparison10.compare(BooleanValue.get(b), this.singletonOperator, (BooleanValue)i11, this.comparer, context);
            }
            if (i11 == null && !this.maybeBoolean0) {
                iter1.close();
                return false;
            }
            if (i12 != null) {
                iter1 = new PrependSequenceIterator(i12, iter1);
            }
            if (i11 != null) {
                iter1 = new PrependSequenceIterator(i11, iter1);
            }
        }
        if (this.atomize0) {
            iter0 = Atomizer.getAtomizingIterator(iter0, false);
        }
        if (this.atomize1) {
            iter1 = Atomizer.getAtomizingIterator(iter1, false);
        }
        if (iter0 instanceof EmptyIterator || iter1 instanceof EmptyIterator) {
            return false;
        }
        if (this.operator == 12 || this.operator == 14 || this.operator == 11 || this.operator == 13) {
            final Configuration config = context.getConfiguration();
            ItemMappingFunction map = new ItemMappingFunction(){

                @Override
                public DoubleValue mapItem(Item item) throws XPathException {
                    return Number_1.convert((AtomicValue)item, config);
                }
            };
            iter0 = new ItemMappingIterator(iter0, map, true);
            iter1 = new ItemMappingIterator(iter1, map, true);
        }
        ArrayList<AtomicValue> seq1 = null;
        block2: while ((item0 = (AtomicValue)iter0.next()) != null) {
            if (iter1 != null) {
                while (true) {
                    AtomicValue item1;
                    if ((item1 = (AtomicValue)iter1.next()) == null) {
                        iter1 = null;
                        if (seq1 != null) continue block2;
                        return false;
                    }
                    try {
                        if (GeneralComparison10.compare(item0, this.singletonOperator, item1, this.comparer, context)) {
                            return true;
                        }
                        if (seq1 == null) {
                            seq1 = new ArrayList<AtomicValue>(40);
                        }
                        seq1.add(item1);
                    } catch (XPathException e) {
                        e.maybeSetLocation(this.getLocation());
                        e.maybeSetContext(context);
                        throw e;
                    }
                }
            }
            for (AtomicValue item1 : seq1) {
                if (!GeneralComparison10.compare(item0, this.singletonOperator, item1, this.comparer, context)) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        GeneralComparison10 gc = new GeneralComparison10(this.getLhsExpression().copy(rebindings), this.operator, this.getRhsExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, gc);
        gc.setRetainedStaticContext(this.getRetainedStaticContext());
        gc.comparer = this.comparer;
        gc.atomize0 = this.atomize0;
        gc.atomize1 = this.atomize1;
        gc.maybeBoolean0 = this.maybeBoolean0;
        gc.maybeBoolean1 = this.maybeBoolean1;
        return gc;
    }

    private static boolean compare(AtomicValue a0, int operator, AtomicValue a1, AtomicComparer comparer, XPathContext context) throws XPathException {
        comparer = comparer.provideContext(context);
        ConversionRules rules = context.getConfiguration().getConversionRules();
        BuiltInAtomicType t0 = a0.getPrimitiveType();
        BuiltInAtomicType t1 = a1.getPrimitiveType();
        if (t0.isPrimitiveNumeric() || t1.isPrimitiveNumeric()) {
            DoubleValue v0 = Number_1.convert(a0, context.getConfiguration());
            DoubleValue v1 = Number_1.convert(a1, context.getConfiguration());
            return ValueComparison.compare(v0, operator, v1, comparer, false);
        }
        if (t0.equals(BuiltInAtomicType.STRING) || t1.equals(BuiltInAtomicType.STRING) || t0.equals(BuiltInAtomicType.UNTYPED_ATOMIC) && t1.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
            StringValue s0 = StringValue.makeStringValue(a0.getStringValueCS());
            StringValue s1 = StringValue.makeStringValue(a1.getStringValueCS());
            return ValueComparison.compare(s0, operator, s1, comparer, false);
        }
        if (t0.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
            a0 = t1.getStringConverter(rules).convert((StringValue)a0).asAtomic();
        }
        if (t1.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
            a1 = t0.getStringConverter(rules).convert((StringValue)a1).asAtomic();
        }
        return ValueComparison.compare(a0, operator, a1, comparer, false);
    }

    @Override
    public ItemType getItemType() {
        return BuiltInAtomicType.BOOLEAN;
    }

    @Override
    protected void explainExtraAttributes(ExpressionPresenter out) {
        out.emitAttribute("cardinality", "many-to-many (1.0)");
        out.emitAttribute("comp", this.comparer.save());
    }

    @Override
    public String tag() {
        return "gc10";
    }
}

