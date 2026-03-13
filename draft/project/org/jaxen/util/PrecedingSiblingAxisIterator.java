/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import org.jaxen.JaxenConstants;
import org.jaxen.Navigator;
import org.jaxen.UnsupportedAxisException;

public class PrecedingSiblingAxisIterator
implements Iterator {
    private Object contextNode;
    private Navigator navigator;
    private Iterator siblingIter;
    private Object nextObj;

    public PrecedingSiblingAxisIterator(Object contextNode, Navigator navigator) throws UnsupportedAxisException {
        this.contextNode = contextNode;
        this.navigator = navigator;
        this.init();
        if (this.siblingIter.hasNext()) {
            this.nextObj = this.siblingIter.next();
        }
    }

    private void init() throws UnsupportedAxisException {
        Object parent = this.navigator.getParentNode(this.contextNode);
        if (parent != null) {
            Object eachChild;
            Iterator childIter = this.navigator.getChildAxisIterator(parent);
            LinkedList siblings = new LinkedList();
            while (childIter.hasNext() && !(eachChild = childIter.next()).equals(this.contextNode)) {
                siblings.addFirst(eachChild);
            }
            this.siblingIter = siblings.iterator();
        } else {
            this.siblingIter = JaxenConstants.EMPTY_ITERATOR;
        }
    }

    public boolean hasNext() {
        return this.nextObj != null;
    }

    public Object next() throws NoSuchElementException {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        Object obj = this.nextObj;
        this.nextObj = this.siblingIter.hasNext() ? this.siblingIter.next() : null;
        return obj;
    }

    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}

