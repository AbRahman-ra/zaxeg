/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import java.util.ArrayList;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.OrderByClause;
import net.sf.saxon.expr.flwor.Tuple;
import net.sf.saxon.expr.flwor.TupleExpression;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.ItemToBeSorted;
import net.sf.saxon.expr.sort.SortKeyDefinitionList;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.trans.XPathException;

public class OrderByClausePush
extends TuplePush {
    private TuplePush destination;
    private OrderByClause orderByClause;
    private TupleExpression tupleExpr;
    private AtomicComparer[] comparers;
    private XPathContext context;
    private int position = 0;
    private ArrayList<ItemToBeSorted> tupleArray = new ArrayList(100);

    public OrderByClausePush(Outputter outputter, TuplePush destination, TupleExpression tupleExpr, OrderByClause orderBy, XPathContext context) {
        super(outputter);
        this.destination = destination;
        this.tupleExpr = tupleExpr;
        this.orderByClause = orderBy;
        this.context = context;
        AtomicComparer[] suppliedComparers = orderBy.getAtomicComparers();
        this.comparers = new AtomicComparer[suppliedComparers.length];
        for (int n = 0; n < this.comparers.length; ++n) {
            this.comparers[n] = suppliedComparers[n].provideContext(context);
        }
    }

    @Override
    public void processTuple(XPathContext context) throws XPathException {
        Tuple tuple = this.tupleExpr.evaluateItem(context);
        SortKeyDefinitionList sortKeyDefinitions = this.orderByClause.getSortKeyDefinitions();
        ItemToBeSorted itbs = new ItemToBeSorted(sortKeyDefinitions.size());
        itbs.value = tuple;
        for (int i = 0; i < sortKeyDefinitions.size(); ++i) {
            itbs.sortKeyValues[i] = this.orderByClause.evaluateSortKey(i, context);
        }
        itbs.originalPosition = ++this.position;
        this.tupleArray.add(itbs);
    }

    @Override
    public void close() throws XPathException {
        try {
            this.tupleArray.sort((a, b) -> {
                try {
                    for (int i = 0; i < this.comparers.length; ++i) {
                        int comp = this.comparers[i].compareAtomicValues(a.sortKeyValues[i], b.sortKeyValues[i]);
                        if (comp == 0) continue;
                        return comp;
                    }
                } catch (NoDynamicContextException e) {
                    throw new AssertionError((Object)("Sorting without dynamic context: " + e.getMessage()));
                }
                return a.originalPosition - b.originalPosition;
            });
        } catch (ClassCastException e) {
            XPathException err = new XPathException("Non-comparable types found while sorting: " + e.getMessage());
            err.setErrorCode("XPTY0004");
            throw err;
        }
        for (ItemToBeSorted itbs : this.tupleArray) {
            this.tupleExpr.setCurrentTuple(this.context, (Tuple)itbs.value);
            this.destination.processTuple(this.context);
        }
        this.destination.close();
    }
}

