/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr.iter;

import java.util.Iterator;
import org.jaxen.ContextSupport;
import org.jaxen.NamedAccessNavigator;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.expr.iter.IterableAxis;

public class IterableAttributeAxis
extends IterableAxis {
    private static final long serialVersionUID = 1L;

    public IterableAttributeAxis(int value) {
        super(value);
    }

    public Iterator iterator(Object contextNode, ContextSupport support) throws UnsupportedAxisException {
        return support.getNavigator().getAttributeAxisIterator(contextNode);
    }

    public Iterator namedAccessIterator(Object contextNode, ContextSupport support, String localName, String namespacePrefix, String namespaceURI) throws UnsupportedAxisException {
        NamedAccessNavigator nav = (NamedAccessNavigator)support.getNavigator();
        return nav.getAttributeAxisIterator(contextNode, localName, namespacePrefix, namespaceURI);
    }

    public boolean supportsNamedAccess(ContextSupport support) {
        return support.getNavigator() instanceof NamedAccessNavigator;
    }
}

