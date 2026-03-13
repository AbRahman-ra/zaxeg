/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.GroupByClause;
import net.sf.saxon.expr.flwor.Tuple;
import net.sf.saxon.expr.flwor.TupleExpression;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.expr.sort.GenericAtomicComparer;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;

public class GroupByClausePush
extends TuplePush {
    private TuplePush destination;
    private GroupByClause groupByClause;
    private HashMap<Object, List<GroupByClause.ObjectToBeGrouped>> map = new HashMap();
    private XPathContext context;
    private GenericAtomicComparer[] comparers;

    public GroupByClausePush(Outputter outputter, TuplePush destination, GroupByClause groupBy, XPathContext context) {
        super(outputter);
        this.destination = destination;
        this.groupByClause = groupBy;
        this.context = context;
        this.comparers = new GenericAtomicComparer[groupBy.comparers.length];
        for (int i = 0; i < this.comparers.length; ++i) {
            this.comparers[i] = groupBy.comparers[i].provideContext(context);
        }
    }

    @Override
    public void processTuple(XPathContext context) throws XPathException {
        TupleExpression groupingTupleExpr = this.groupByClause.getGroupingTupleExpression();
        TupleExpression retainedTupleExpr = this.groupByClause.getRetainedTupleExpression();
        GroupByClause.ObjectToBeGrouped otbg = new GroupByClause.ObjectToBeGrouped();
        Sequence[] groupingValues = groupingTupleExpr.evaluateItem(context).getMembers();
        GroupByClausePush.checkGroupingValues(groupingValues);
        otbg.groupingValues = new Tuple(groupingValues);
        otbg.retainedValues = retainedTupleExpr.evaluateItem(context);
        GroupByClause.TupleComparisonKey key = this.groupByClause.getComparisonKey(otbg.groupingValues, this.comparers);
        List<GroupByClause.ObjectToBeGrouped> group = this.map.get(key);
        GroupByClausePush.addToGroup(key, otbg, group, this.map);
    }

    protected static void addToGroup(Object key, GroupByClause.ObjectToBeGrouped objectToBeGrouped, List<GroupByClause.ObjectToBeGrouped> group, HashMap<Object, List<GroupByClause.ObjectToBeGrouped>> map) {
        if (group != null) {
            group.add(objectToBeGrouped);
            map.put(key, group);
        } else {
            ArrayList<GroupByClause.ObjectToBeGrouped> list = new ArrayList<GroupByClause.ObjectToBeGrouped>();
            list.add(objectToBeGrouped);
            map.put(key, list);
        }
    }

    protected static void checkGroupingValues(Sequence[] groupingValues) throws XPathException {
        for (int i = 0; i < groupingValues.length; ++i) {
            Sequence v = groupingValues[i];
            if (v instanceof EmptySequence || v instanceof AtomicValue) continue;
            if (SequenceTool.getLength(v = Atomizer.getAtomizingIterator(v.iterate(), false).materialize()) > 1) {
                throw new XPathException("Grouping key value cannot be a sequence of more than one item", "XPTY0004");
            }
            groupingValues[i] = v;
        }
    }

    @Override
    public void close() throws XPathException {
        for (List<GroupByClause.ObjectToBeGrouped> group : this.map.values()) {
            this.groupByClause.processGroup(group, this.context);
            this.destination.processTuple(this.context);
        }
        this.destination.close();
    }
}

