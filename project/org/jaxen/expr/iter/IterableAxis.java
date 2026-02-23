/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr.iter;

import java.io.Serializable;
import java.util.Iterator;
import org.jaxen.ContextSupport;
import org.jaxen.UnsupportedAxisException;

public abstract class IterableAxis
implements Serializable {
    private int value;

    public IterableAxis(int axisValue) {
        this.value = axisValue;
    }

    public int value() {
        return this.value;
    }

    public abstract Iterator iterator(Object var1, ContextSupport var2) throws UnsupportedAxisException;

    public Iterator namedAccessIterator(Object contextNode, ContextSupport support, String localName, String namespacePrefix, String namespaceURI) throws UnsupportedAxisException {
        throw new UnsupportedOperationException("Named access unsupported");
    }

    public boolean supportsNamedAccess(ContextSupport support) {
        return false;
    }
}

