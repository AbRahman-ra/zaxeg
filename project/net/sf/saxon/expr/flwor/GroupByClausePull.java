/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.GroupByClause;
import net.sf.saxon.expr.flwor.GroupByClausePush;
import net.sf.saxon.expr.flwor.Tuple;
import net.sf.saxon.expr.flwor.TupleExpression;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.expr.sort.GenericAtomicComparer;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public class GroupByClausePull
extends TuplePull {
    private TuplePull base;
    private GroupByClause groupByClause;
    Iterator<List<GroupByClause.ObjectToBeGrouped>> groupIterator;
    private GenericAtomicComparer[] comparers;

    public GroupByClausePull(TuplePull base, GroupByClause groupBy, XPathContext context) {
        this.base = base;
        this.groupByClause = groupBy;
        this.comparers = new GenericAtomicComparer[groupBy.comparers.length];
        for (int i = 0; i < this.comparers.length; ++i) {
            this.comparers[i] = groupBy.comparers[i].provideContext(context);
        }
    }

    @Override
    public boolean nextTuple(XPathContext context) throws XPathException {
        if (this.groupIterator == null) {
            TupleExpression groupingTupleExpr = this.groupByClause.getGroupingTupleExpression();
            TupleExpression retainedTupleExpr = this.groupByClause.getRetainedTupleExpression();
            HashMap<Object, List<GroupByClause.ObjectToBeGrouped>> map = new HashMap<Object, List<GroupByClause.ObjectToBeGrouped>>();
            while (this.base.nextTuple(context)) {
                GroupByClause.ObjectToBeGrouped otbg = new GroupByClause.ObjectToBeGrouped();
                Sequence[] groupingValues = groupingTupleExpr.evaluateItem(context).getMembers();
                GroupByClausePush.checkGroupingValues(groupingValues);
                otbg.groupingValues = new Tuple(groupingValues);
                otbg.retainedValues = retainedTupleExpr.evaluateItem(context);
                GroupByClause.TupleComparisonKey key = this.groupByClause.getComparisonKey(otbg.groupingValues, this.comparers);
                List group = (List)map.get(key);
                GroupByClausePush.addToGroup(key, otbg, group, map);
            }
            this.groupIterator = map.values().iterator();
        }
        if (this.groupIterator.hasNext()) {
            List<GroupByClause.ObjectToBeGrouped> group = this.groupIterator.next();
            this.groupByClause.processGroup(group, context);
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        this.base.close();
        this.groupIterator = null;
    }
}

