/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.util.Iterator;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.tree.FilterIterator;

public class ElementQNameIterator
extends FilterIterator {
    private QName qName;

    public ElementQNameIterator(Iterator proxy, QName qName) {
        super(proxy);
        this.qName = qName;
    }

    protected boolean matches(Object object) {
        if (object instanceof Element) {
            Element element = (Element)object;
            return this.qName.equals(element.getQName());
        }
        return false;
    }
}

