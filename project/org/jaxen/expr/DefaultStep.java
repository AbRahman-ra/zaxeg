/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.JaxenException;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.expr.IdentitySet;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.PredicateSet;
import org.jaxen.expr.Step;
import org.jaxen.expr.iter.IterableAxis;
import org.jaxen.saxpath.Axis;

public abstract class DefaultStep
implements Step {
    private IterableAxis axis;
    private PredicateSet predicates;

    public DefaultStep(IterableAxis axis, PredicateSet predicates) {
        this.axis = axis;
        this.predicates = predicates;
    }

    public void addPredicate(Predicate predicate) {
        this.predicates.addPredicate(predicate);
    }

    public List getPredicates() {
        return this.predicates.getPredicates();
    }

    public PredicateSet getPredicateSet() {
        return this.predicates;
    }

    public int getAxis() {
        return this.axis.value();
    }

    public IterableAxis getIterableAxis() {
        return this.axis;
    }

    public String getAxisName() {
        return Axis.lookup(this.getAxis());
    }

    public String getText() {
        return this.predicates.getText();
    }

    public String toString() {
        return this.getIterableAxis() + " " + super.toString();
    }

    public void simplify() {
        this.predicates.simplify();
    }

    public Iterator axisIterator(Object contextNode, ContextSupport support) throws UnsupportedAxisException {
        return this.getIterableAxis().iterator(contextNode, support);
    }

    public List evaluate(Context context) throws JaxenException {
        List contextNodeSet = context.getNodeSet();
        IdentitySet unique = new IdentitySet();
        int contextSize = contextNodeSet.size();
        ArrayList interimSet = new ArrayList();
        ArrayList newNodeSet = new ArrayList();
        ContextSupport support = context.getContextSupport();
        for (int i = 0; i < contextSize; ++i) {
            Object eachContextNode = contextNodeSet.get(i);
            Iterator axisNodeIter = this.axis.iterator(eachContextNode, support);
            while (axisNodeIter.hasNext()) {
                Object eachAxisNode = axisNodeIter.next();
                if (unique.contains(eachAxisNode) || !this.matches(eachAxisNode, support)) continue;
                unique.add(eachAxisNode);
                interimSet.add(eachAxisNode);
            }
            newNodeSet.addAll(this.getPredicateSet().evaluatePredicates(interimSet, support));
            interimSet.clear();
        }
        return newNodeSet;
    }
}

