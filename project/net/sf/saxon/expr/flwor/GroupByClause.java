/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.FLWORExpression;
import net.sf.saxon.expr.flwor.GroupByClausePull;
import net.sf.saxon.expr.flwor.GroupByClausePush;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.expr.flwor.OperandProcessor;
import net.sf.saxon.expr.flwor.Tuple;
import net.sf.saxon.expr.flwor.TupleExpression;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.sort.GenericAtomicComparer;
import net.sf.saxon.functions.DeepEqual;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceExtent;

public class GroupByClause
extends Clause {
    Configuration config;
    LocalVariableBinding[] bindings;
    GenericAtomicComparer[] comparers;
    Operand retainedTupleOp;
    Operand groupingTupleOp;

    public GroupByClause(Configuration config) {
        this.config = config;
    }

    @Override
    public Clause.ClauseName getClauseKey() {
        return Clause.ClauseName.GROUP_BY;
    }

    @Override
    public boolean containsNonInlineableVariableReference(Binding binding) {
        return this.getRetainedTupleExpression().includesBinding(binding) || this.getGroupingTupleExpression().includesBinding(binding);
    }

    @Override
    public GroupByClause copy(FLWORExpression flwor, RebindingMap rebindings) {
        GroupByClause g2 = new GroupByClause(this.config);
        g2.setLocation(this.getLocation());
        g2.setPackageData(this.getPackageData());
        g2.bindings = new LocalVariableBinding[this.bindings.length];
        for (int i = 0; i < this.bindings.length; ++i) {
            g2.bindings[i] = this.bindings[i].copy();
        }
        g2.comparers = this.comparers;
        g2.initRetainedTupleExpression(flwor, (TupleExpression)this.getRetainedTupleExpression().copy(rebindings));
        g2.initGroupingTupleExpression(flwor, (TupleExpression)this.getGroupingTupleExpression().copy(rebindings));
        return g2;
    }

    public void initRetainedTupleExpression(FLWORExpression flwor, TupleExpression expr) {
        this.retainedTupleOp = new Operand(flwor, expr, OperandRole.FLWOR_TUPLE_CONSTRAINED);
    }

    public void setRetainedTupleExpression(TupleExpression expr) {
        this.retainedTupleOp.setChildExpression(expr);
    }

    public TupleExpression getRetainedTupleExpression() {
        return (TupleExpression)this.retainedTupleOp.getChildExpression();
    }

    @Override
    public void optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        ArrayList<LocalVariableBinding> list = new ArrayList<LocalVariableBinding>(Arrays.asList(this.bindings));
        ArrayList<LocalVariableReference> retainingExpr = new ArrayList<LocalVariableReference>();
        for (Operand o : this.getRetainedTupleExpression().operands()) {
            retainingExpr.add((LocalVariableReference)o.getChildExpression());
        }
        int groupingSize = this.getGroupingTupleExpression().getSize();
        for (int i = list.size() - 1; i >= groupingSize; --i) {
            if (list.get(i).getNominalReferenceCount() != 0) continue;
            list.remove(i);
            retainingExpr.remove(i - groupingSize);
        }
        this.bindings = list.toArray(new LocalVariableBinding[0]);
        this.getRetainedTupleExpression().setVariables(retainingExpr);
    }

    public void initGroupingTupleExpression(FLWORExpression flwor, TupleExpression expr) {
        this.groupingTupleOp = new Operand(flwor, expr, OperandRole.FLWOR_TUPLE_CONSTRAINED);
    }

    public void setGroupingTupleExpression(TupleExpression expr) {
        this.groupingTupleOp.setChildExpression(expr);
    }

    public TupleExpression getGroupingTupleExpression() {
        return (TupleExpression)this.groupingTupleOp.getChildExpression();
    }

    public void setVariableBindings(LocalVariableBinding[] bindings) {
        this.bindings = bindings;
    }

    @Override
    public LocalVariableBinding[] getRangeVariables() {
        return this.bindings;
    }

    public void setComparers(GenericAtomicComparer[] comparers) {
        this.comparers = comparers;
    }

    @Override
    public TuplePull getPullStream(TuplePull base, XPathContext context) {
        return new GroupByClausePull(base, this, context);
    }

    @Override
    public TuplePush getPushStream(TuplePush destination, Outputter output, XPathContext context) {
        return new GroupByClausePush(output, destination, this, context);
    }

    @Override
    public void processOperands(OperandProcessor processor) throws XPathException {
        processor.processOperand(this.groupingTupleOp);
        processor.processOperand(this.retainedTupleOp);
    }

    @Override
    public void explain(ExpressionPresenter out) {
        out.startElement("group-by");
        for (Operand o : this.getRetainedTupleExpression().operands()) {
            LocalVariableReference ref = (LocalVariableReference)o.getChildExpression();
            out.startSubsidiaryElement("by");
            out.emitAttribute("var", ref.getDisplayName());
            out.emitAttribute("slot", ref.getBinding().getLocalSlotNumber() + "");
            out.endSubsidiaryElement();
        }
        out.endElement();
    }

    public String toString() {
        FastStringBuffer fsb = new FastStringBuffer(64);
        fsb.append("group by ... ");
        return fsb.toString();
    }

    public void processGroup(List<ObjectToBeGrouped> group, XPathContext context) throws XPathException {
        int j;
        LocalVariableBinding[] bindings = this.getRangeVariables();
        Sequence[] groupingValues = group.get((int)0).groupingValues.getMembers();
        for (j = 0; j < groupingValues.length; ++j) {
            Sequence v = groupingValues[j];
            context.setLocalVariable(bindings[j].getLocalSlotNumber(), v);
        }
        for (j = groupingValues.length; j < bindings.length; ++j) {
            ArrayList<Item> concatenatedValue = new ArrayList<Item>();
            for (ObjectToBeGrouped otbg : group) {
                Item it;
                Sequence val = otbg.retainedValues.getMembers()[j - groupingValues.length];
                SequenceIterator si = val.iterate();
                while ((it = si.next()) != null) {
                    concatenatedValue.add(it);
                }
            }
            SequenceExtent se = new SequenceExtent(concatenatedValue);
            context.setLocalVariable(bindings[j].getLocalSlotNumber(), se);
        }
    }

    public TupleComparisonKey getComparisonKey(Tuple t, GenericAtomicComparer[] comparers) {
        return new TupleComparisonKey(t.getMembers(), comparers);
    }

    @Override
    public void addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        throw new UnsupportedOperationException("Cannot use document projection with group-by");
    }

    public class TupleComparisonKey {
        private Sequence[] groupingValues;
        private GenericAtomicComparer[] comparers;

        public TupleComparisonKey(Sequence[] groupingValues, GenericAtomicComparer[] comparers) {
            this.groupingValues = groupingValues;
            this.comparers = comparers;
        }

        public int hashCode() {
            int h = 0x77557755 ^ this.groupingValues.length;
            for (int i = 0; i < this.groupingValues.length; ++i) {
                GenericAtomicComparer comparer = this.comparers[i];
                int implicitTimezone = comparer.getContext().getImplicitTimezone();
                try {
                    AtomicValue val;
                    SequenceIterator atoms = this.groupingValues[i].iterate();
                    while ((val = (AtomicValue)atoms.next()) != null) {
                        h ^= i + val.getXPathComparable(false, comparer.getCollator(), implicitTimezone).hashCode();
                    }
                    continue;
                } catch (XPathException xPathException) {
                    // empty catch block
                }
            }
            return h;
        }

        public boolean equals(Object other) {
            if (!(other instanceof TupleComparisonKey)) {
                return false;
            }
            if (this.groupingValues.length != ((TupleComparisonKey)other).groupingValues.length) {
                return false;
            }
            for (int i = 0; i < this.groupingValues.length; ++i) {
                try {
                    if (DeepEqual.deepEqual(this.groupingValues[i].iterate(), ((TupleComparisonKey)other).groupingValues[i].iterate(), this.comparers[i], this.comparers[i].getContext(), 0)) continue;
                    return false;
                } catch (XPathException e) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class ObjectToBeGrouped {
        public Tuple groupingValues;
        public Tuple retainedValues;
    }
}

