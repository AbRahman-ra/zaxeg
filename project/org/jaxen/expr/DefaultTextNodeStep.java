/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.ContextSupport;
import org.jaxen.Navigator;
import org.jaxen.expr.DefaultStep;
import org.jaxen.expr.PredicateSet;
import org.jaxen.expr.TextNodeStep;
import org.jaxen.expr.iter.IterableAxis;

public class DefaultTextNodeStep
extends DefaultStep
implements TextNodeStep {
    private static final long serialVersionUID = -3821960984972022948L;

    public DefaultTextNodeStep(IterableAxis axis, PredicateSet predicateSet) {
        super(axis, predicateSet);
    }

    public boolean matches(Object node, ContextSupport support) {
        Navigator nav = support.getNavigator();
        return nav.isText(node);
    }

    public String getText() {
        return this.getAxisName() + "::text()" + super.getText();
    }
}

