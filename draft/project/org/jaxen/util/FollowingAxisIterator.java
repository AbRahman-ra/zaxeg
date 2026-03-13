/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jaxen.JaxenConstants;
import org.jaxen.JaxenRuntimeException;
import org.jaxen.Navigator;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.util.DescendantOrSelfAxisIterator;

public class FollowingAxisIterator
implements Iterator {
    private Object contextNode;
    private Navigator navigator;
    private Iterator siblings;
    private Iterator currentSibling;

    public FollowingAxisIterator(Object contextNode, Navigator navigator) throws UnsupportedAxisException {
        this.contextNode = contextNode;
        this.navigator = navigator;
        this.siblings = navigator.getFollowingSiblingAxisIterator(contextNode);
        this.currentSibling = JaxenConstants.EMPTY_ITERATOR;
    }

    private boolean goForward() {
        while (!this.siblings.hasNext()) {
            if (this.goUp()) continue;
            return false;
        }
        Object nextSibling = this.siblings.next();
        this.currentSibling = new DescendantOrSelfAxisIterator(nextSibling, this.navigator);
        return true;
    }

    private boolean goUp() {
        if (this.contextNode == null || this.navigator.isDocument(this.contextNode)) {
            return false;
        }
        try {
            this.contextNode = this.navigator.getParentNode(this.contextNode);
            if (this.contextNode != null && !this.navigator.isDocument(this.contextNode)) {
                this.siblings = this.navigator.getFollowingSiblingAxisIterator(this.contextNode);
                return true;
            }
            return false;
        } catch (UnsupportedAxisException e) {
            throw new JaxenRuntimeException(e);
        }
    }

    public boolean hasNext() {
        while (!this.currentSibling.hasNext()) {
            if (this.goForward()) continue;
            return false;
        }
        return true;
    }

    public Object next() throws NoSuchElementException {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        return this.currentSibling.next();
    }

    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}

