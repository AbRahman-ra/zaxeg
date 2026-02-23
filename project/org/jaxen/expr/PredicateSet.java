/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.JaxenException;
import org.jaxen.expr.Predicate;
import org.jaxen.function.BooleanFunction;

public class PredicateSet
implements Serializable {
    private static final long serialVersionUID = -7166491740228977853L;
    private List predicates = Collections.EMPTY_LIST;

    public void addPredicate(Predicate predicate) {
        if (this.predicates == Collections.EMPTY_LIST) {
            this.predicates = new ArrayList();
        }
        this.predicates.add(predicate);
    }

    public List getPredicates() {
        return this.predicates;
    }

    public void simplify() {
        Iterator predIter = this.predicates.iterator();
        Predicate eachPred = null;
        while (predIter.hasNext()) {
            eachPred = (Predicate)predIter.next();
            eachPred.simplify();
        }
    }

    public String getText() {
        StringBuffer buf = new StringBuffer();
        Iterator predIter = this.predicates.iterator();
        Predicate eachPred = null;
        while (predIter.hasNext()) {
            eachPred = (Predicate)predIter.next();
            buf.append(eachPred.getText());
        }
        return buf.toString();
    }

    protected boolean evaluateAsBoolean(List contextNodeSet, ContextSupport support) throws JaxenException {
        return this.anyMatchingNode(contextNodeSet, support);
    }

    private boolean anyMatchingNode(List contextNodeSet, ContextSupport support) throws JaxenException {
        if (this.predicates.size() == 0) {
            return false;
        }
        Iterator predIter = this.predicates.iterator();
        List nodes2Filter = contextNodeSet;
        while (predIter.hasNext()) {
            int nodes2FilterSize = nodes2Filter.size();
            Context predContext = new Context(support);
            ArrayList tempList = new ArrayList(1);
            predContext.setNodeSet(tempList);
            for (int i = 0; i < nodes2FilterSize; ++i) {
                Boolean includes;
                int proximity;
                Object contextNode = nodes2Filter.get(i);
                tempList.clear();
                tempList.add(contextNode);
                predContext.setNodeSet(tempList);
                predContext.setPosition(i + 1);
                predContext.setSize(nodes2FilterSize);
                Object predResult = ((Predicate)predIter.next()).evaluate(predContext);
                if (!(predResult instanceof Number ? (proximity = ((Number)predResult).intValue()) == i + 1 : (includes = BooleanFunction.evaluate(predResult, predContext.getNavigator())) != false)) continue;
                return true;
            }
        }
        return false;
    }

    protected List evaluatePredicates(List contextNodeSet, ContextSupport support) throws JaxenException {
        if (this.predicates.size() == 0) {
            return contextNodeSet;
        }
        Iterator predIter = this.predicates.iterator();
        List nodes2Filter = contextNodeSet;
        while (predIter.hasNext()) {
            nodes2Filter = this.applyPredicate((Predicate)predIter.next(), nodes2Filter, support);
        }
        return nodes2Filter;
    }

    public List applyPredicate(Predicate predicate, List nodes2Filter, ContextSupport support) throws JaxenException {
        int nodes2FilterSize = nodes2Filter.size();
        ArrayList filteredNodes = new ArrayList(nodes2FilterSize);
        Context predContext = new Context(support);
        ArrayList tempList = new ArrayList(1);
        predContext.setNodeSet(tempList);
        for (int i = 0; i < nodes2FilterSize; ++i) {
            Object contextNode = nodes2Filter.get(i);
            tempList.clear();
            tempList.add(contextNode);
            predContext.setNodeSet(tempList);
            predContext.setPosition(i + 1);
            predContext.setSize(nodes2FilterSize);
            Object predResult = predicate.evaluate(predContext);
            if (predResult instanceof Number) {
                int proximity = ((Number)predResult).intValue();
                if (proximity != i + 1) continue;
                filteredNodes.add(contextNode);
                continue;
            }
            Boolean includes = BooleanFunction.evaluate(predResult, predContext.getNavigator());
            if (!includes.booleanValue()) continue;
            filteredNodes.add(contextNode);
        }
        return filteredNodes;
    }
}

