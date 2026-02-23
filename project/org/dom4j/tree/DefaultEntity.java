/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import org.dom4j.Element;
import org.dom4j.tree.FlyweightEntity;

public class DefaultEntity
extends FlyweightEntity {
    private Element parent;

    public DefaultEntity(String name) {
        super(name);
    }

    public DefaultEntity(String name, String text) {
        super(name, text);
    }

    public DefaultEntity(Element parent, String name, String text) {
        super(name, text);
        this.parent = parent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Element getParent() {
        return this.parent;
    }

    public void setParent(Element parent) {
        this.parent = parent;
    }

    public boolean supportsParent() {
        return true;
    }

    public boolean isReadOnly() {
        return false;
    }
}

