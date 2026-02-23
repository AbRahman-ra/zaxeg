/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import java.io.Serializable;
import java.util.List;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.PredicateSet;

public interface Predicated
extends Serializable {
    public void addPredicate(Predicate var1);

    public List getPredicates();

    public PredicateSet getPredicateSet();
}

