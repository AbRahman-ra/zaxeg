/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.ContextSupport;
import org.jaxen.expr.AllNodeStep;
import org.jaxen.expr.DefaultStep;
import org.jaxen.expr.PredicateSet;
import org.jaxen.expr.iter.IterableAxis;

public class DefaultAllNodeStep
extends DefaultStep
implements AllNodeStep {
    private static final long serialVersionUID = 292886316770123856L;

    public DefaultAllNodeStep(IterableAxis axis, PredicateSet predicateSet) {
        super(axis, predicateSet);
    }

    public String toString() {
        return "[(DefaultAllNodeStep): " + this.getAxisName() + "]";
    }

    public String getText() {
        return this.getAxisName() + "::node()" + super.getText();
    }

    public boolean matches(Object node, ContextSupport contextSupport) {
        return true;
    }
}

