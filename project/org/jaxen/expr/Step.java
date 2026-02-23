/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import java.util.Iterator;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.JaxenException;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.expr.Predicated;

public interface Step
extends Predicated {
    public boolean matches(Object var1, ContextSupport var2) throws JaxenException;

    public String getText();

    public void simplify();

    public int getAxis();

    public Iterator axisIterator(Object var1, ContextSupport var2) throws UnsupportedAxisException;

    public List evaluate(Context var1) throws JaxenException;
}

