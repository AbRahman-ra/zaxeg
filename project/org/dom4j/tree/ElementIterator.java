/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.util.Iterator;
import org.dom4j.Element;
import org.dom4j.tree.FilterIterator;

public class ElementIterator
extends FilterIterator {
    public ElementIterator(Iterator proxy) {
        super(proxy);
    }

    protected boolean matches(Object element) {
        return element instanceof Element;
    }
}

