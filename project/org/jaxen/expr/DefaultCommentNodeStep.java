/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.ContextSupport;
import org.jaxen.Navigator;
import org.jaxen.expr.CommentNodeStep;
import org.jaxen.expr.DefaultStep;
import org.jaxen.expr.PredicateSet;
import org.jaxen.expr.iter.IterableAxis;

public class DefaultCommentNodeStep
extends DefaultStep
implements CommentNodeStep {
    private static final long serialVersionUID = 4340788283861875606L;

    public DefaultCommentNodeStep(IterableAxis axis, PredicateSet predicateSet) {
        super(axis, predicateSet);
    }

    public String toString() {
        return "[(DefaultCommentNodeStep): " + this.getAxis() + "]";
    }

    public String getText() {
        return this.getAxisName() + "::comment()";
    }

    public boolean matches(Object node, ContextSupport contextSupport) {
        Navigator nav = contextSupport.getNavigator();
        return nav.isComment(node);
    }
}

