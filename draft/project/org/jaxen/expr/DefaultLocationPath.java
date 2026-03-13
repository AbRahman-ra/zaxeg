/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.JaxenException;
import org.jaxen.expr.DefaultExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NodeComparator;
import org.jaxen.expr.Step;

abstract class DefaultLocationPath
extends DefaultExpr
implements LocationPath {
    private List steps = new LinkedList();

    DefaultLocationPath() {
    }

    public void addStep(Step step) {
        this.getSteps().add(step);
    }

    public List getSteps() {
        return this.steps;
    }

    public Expr simplify() {
        Iterator stepIter = this.getSteps().iterator();
        Step eachStep = null;
        while (stepIter.hasNext()) {
            eachStep = (Step)stepIter.next();
            eachStep.simplify();
        }
        return this;
    }

    public String getText() {
        StringBuffer buf = new StringBuffer();
        Iterator stepIter = this.getSteps().iterator();
        while (stepIter.hasNext()) {
            buf.append(((Step)stepIter.next()).getText());
            if (!stepIter.hasNext()) continue;
            buf.append("/");
        }
        return buf.toString();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        Iterator stepIter = this.getSteps().iterator();
        while (stepIter.hasNext()) {
            buf.append(stepIter.next().toString());
            if (!stepIter.hasNext()) continue;
            buf.append("/");
        }
        return buf.toString();
    }

    public boolean isAbsolute() {
        return false;
    }

    public Object evaluate(Context context) throws JaxenException {
        List nodeSet = context.getNodeSet();
        List contextNodeSet = new ArrayList(nodeSet);
        ContextSupport support = context.getContextSupport();
        Context stepContext = new Context(support);
        Iterator stepIter = this.getSteps().iterator();
        while (stepIter.hasNext()) {
            Step eachStep = (Step)stepIter.next();
            stepContext.setNodeSet(contextNodeSet);
            contextNodeSet = eachStep.evaluate(stepContext);
            if (!this.isReverseAxis(eachStep)) continue;
            Collections.reverse(contextNodeSet);
        }
        if (this.getSteps().size() > 1 || nodeSet.size() > 1) {
            Collections.sort(contextNodeSet, new NodeComparator(support.getNavigator()));
        }
        return contextNodeSet;
    }

    private boolean isReverseAxis(Step step) {
        int axis = step.getAxis();
        return axis == 8 || axis == 6 || axis == 4 || axis == 13;
    }
}

