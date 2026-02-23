/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.accum;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.accum.AccumulatorRule;
import net.sf.saxon.expr.accum.IAccumulatorData;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.Evaluator;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.Rule;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.tree.util.Navigator;

public class AccumulatorData
implements IAccumulatorData {
    private Accumulator accumulator;
    private List<DataPoint> values = new ArrayList<DataPoint>();
    private boolean building = false;

    public AccumulatorData(Accumulator acc) {
        this.accumulator = acc;
    }

    @Override
    public Accumulator getAccumulator() {
        return this.accumulator;
    }

    public void buildIndex(NodeInfo doc, XPathContext context) throws XPathException {
        if (this.building) {
            throw new XPathException("Accumulator " + this.accumulator.getAccumulatorName().getDisplayName() + " requires access to its own value", "XTDE3400");
        }
        this.building = true;
        Expression initialValue = this.accumulator.getInitialValueExpression();
        XPathContextMajor c2 = context.newContext();
        SlotManager sf = this.accumulator.getSlotManagerForInitialValueExpression();
        Sequence[] slots = new Sequence[sf.getNumberOfVariables()];
        c2.setStackFrame(sf, slots);
        c2.setCurrentIterator(new ManualIterator(doc));
        Sequence val = initialValue.iterate(c2).materialize();
        this.values.add(new DataPoint(new Visit(doc, false), val));
        val = this.visit(doc, val, c2);
        this.values.add(new DataPoint(new Visit(doc, true), val));
        ((ArrayList)this.values).trimToSize();
        this.building = false;
    }

    private Sequence visit(NodeInfo node, Sequence value, XPathContext context) throws XPathException {
        try {
            ((ManualIterator)context.getCurrentIterator()).setContextItem(node);
            Rule rule = this.accumulator.getPreDescentRules().getRule(node, context);
            if (rule != null) {
                value = this.processRule(rule, node, false, value, context);
                this.logChange(node, value, context, " BEFORE ");
            }
            for (NodeInfo nodeInfo : node.children()) {
                value = this.visit(nodeInfo, value, context);
            }
            ((ManualIterator)context.getCurrentIterator()).setContextItem(node);
            rule = this.accumulator.getPostDescentRules().getRule(node, context);
            if (rule != null) {
                value = this.processRule(rule, node, true, value, context);
                this.logChange(node, value, context, " AFTER ");
            }
            return value;
        } catch (StackOverflowError e) {
            XPathException.StackOverflow err = new XPathException.StackOverflow("Too many nested accumulator evaluations. The accumulator definition may have cyclic dependencies", "XTDE3400", this.accumulator);
            err.setXPathContext(context);
            throw err;
        }
    }

    private void logChange(NodeInfo node, Sequence value, XPathContext context, String phase) {
        if (this.accumulator.isTracing()) {
            context.getConfiguration().getLogger().info(this.accumulator.getAccumulatorName().getDisplayName() + phase + Navigator.getPath(node) + ": " + Err.depictSequence(value));
        }
    }

    private Sequence processRule(Rule rule, NodeInfo node, boolean isPostDescent, Sequence value, XPathContext context) throws XPathException {
        AccumulatorRule target = (AccumulatorRule)rule.getAction();
        Expression delta = target.getNewValueExpression();
        XPathContextMajor c2 = context.newCleanContext();
        Controller controller = c2.getController();
        assert (controller != null);
        ManualIterator initialNode = new ManualIterator(node);
        c2.setCurrentIterator(initialNode);
        c2.openStackFrame(target.getStackFrameMap());
        c2.setLocalVariable(0, value);
        c2.setCurrentComponent(this.accumulator.getDeclaringComponent());
        c2.setTemporaryOutputState(130);
        value = Evaluator.EAGER_SEQUENCE.evaluate(delta, c2);
        if (node.getParent() == null && !isPostDescent && this.values.size() == 1) {
            this.values.clear();
        }
        this.values.add(new DataPoint(new Visit(node, isPostDescent), value));
        return value;
    }

    @Override
    public Sequence getValue(NodeInfo node, boolean postDescent) {
        Visit visit = new Visit(node, postDescent);
        return this.search(0, this.values.size(), visit);
    }

    private Sequence search(int start, int end, Visit sought) {
        if (start == end) {
            int rel = sought.compareTo(this.values.get((int)start).visit);
            if (rel < 0) {
                return this.values.get((int)(start - 1)).value;
            }
            return this.values.get((int)start).value;
        }
        int mid = (start + end) / 2;
        if (sought.compareTo(this.values.get((int)mid).visit) <= 0) {
            return this.search(start, mid, sought);
        }
        return this.search(mid + 1, end, sought);
    }

    private static class DataPoint {
        public Visit visit;
        public Sequence value;

        public DataPoint(Visit visit, Sequence value) {
            this.visit = visit;
            this.value = value;
        }
    }

    private static class Visit
    implements Comparable<Visit> {
        public NodeInfo node;
        public boolean isPostDescent;

        public Visit(NodeInfo node, boolean isPostDescent) {
            this.node = node;
            this.isPostDescent = isPostDescent;
        }

        @Override
        public int compareTo(Visit other) {
            int relation = Navigator.comparePosition(this.node, other.node);
            switch (relation) {
                case 12: {
                    if (this.isPostDescent == other.isPostDescent) {
                        return 0;
                    }
                    return this.isPostDescent ? 1 : -1;
                }
                case 10: {
                    return -1;
                }
                case 6: {
                    return 1;
                }
                case 0: {
                    return this.isPostDescent ? 1 : -1;
                }
                case 4: {
                    return other.isPostDescent ? -1 : 1;
                }
            }
            throw new IllegalStateException();
        }
    }
}

