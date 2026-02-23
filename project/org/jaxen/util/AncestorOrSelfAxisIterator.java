/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jaxen.JaxenRuntimeException;
import org.jaxen.Navigator;
import org.jaxen.UnsupportedAxisException;

public class AncestorOrSelfAxisIterator
implements Iterator {
    private Object contextNode;
    private Navigator navigator;

    public AncestorOrSelfAxisIterator(Object contextNode, Navigator navigator) {
        this.contextNode = contextNode;
        this.navigator = navigator;
    }

    public boolean hasNext() {
        return this.contextNode != null;
    }

    public Object next() {
        try {
            if (this.hasNext()) {
                Object result = this.contextNode;
                this.contextNode = this.navigator.getParentNode(this.contextNode);
                return result;
            }
            throw new NoSuchElementException("Exhausted ancestor-or-self axis");
        } catch (UnsupportedAxisException e) {
            throw new JaxenRuntimeException(e);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}

