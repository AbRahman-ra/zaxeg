/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.jaxen.JaxenConstants;
import org.jaxen.JaxenRuntimeException;
import org.jaxen.Navigator;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.util.PrecedingSiblingAxisIterator;

public class PrecedingAxisIterator
implements Iterator {
    private Iterator ancestorOrSelf;
    private Iterator precedingSibling;
    private ListIterator childrenOrSelf;
    private ArrayList stack;
    private Navigator navigator;

    public PrecedingAxisIterator(Object contextNode, Navigator navigator) throws UnsupportedAxisException {
        this.navigator = navigator;
        this.ancestorOrSelf = navigator.getAncestorOrSelfAxisIterator(contextNode);
        this.precedingSibling = JaxenConstants.EMPTY_ITERATOR;
        this.childrenOrSelf = JaxenConstants.EMPTY_LIST_ITERATOR;
        this.stack = new ArrayList();
    }

    public boolean hasNext() {
        try {
            while (!this.childrenOrSelf.hasPrevious()) {
                if (this.stack.isEmpty()) {
                    while (!this.precedingSibling.hasNext()) {
                        if (!this.ancestorOrSelf.hasNext()) {
                            return false;
                        }
                        Object contextNode = this.ancestorOrSelf.next();
                        this.precedingSibling = new PrecedingSiblingAxisIterator(contextNode, this.navigator);
                    }
                    Object node = this.precedingSibling.next();
                    this.childrenOrSelf = this.childrenOrSelf(node);
                    continue;
                }
                this.childrenOrSelf = (ListIterator)this.stack.remove(this.stack.size() - 1);
            }
            return true;
        } catch (UnsupportedAxisException e) {
            throw new JaxenRuntimeException(e);
        }
    }

    private ListIterator childrenOrSelf(Object node) {
        try {
            ArrayList<Object> reversed = new ArrayList<Object>();
            reversed.add(node);
            Iterator childAxisIterator = this.navigator.getChildAxisIterator(node);
            if (childAxisIterator != null) {
                while (childAxisIterator.hasNext()) {
                    reversed.add(childAxisIterator.next());
                }
            }
            return reversed.listIterator(reversed.size());
        } catch (UnsupportedAxisException e) {
            throw new JaxenRuntimeException(e);
        }
    }

    public Object next() throws NoSuchElementException {
        Object result;
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        while (true) {
            result = this.childrenOrSelf.previous();
            if (!this.childrenOrSelf.hasPrevious()) break;
            this.stack.add(this.childrenOrSelf);
            this.childrenOrSelf = this.childrenOrSelf(result);
        }
        return result;
    }

    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}

