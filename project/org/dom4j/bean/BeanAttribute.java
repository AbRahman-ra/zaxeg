/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.bean;

import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.bean.BeanAttributeList;
import org.dom4j.tree.AbstractAttribute;

public class BeanAttribute
extends AbstractAttribute {
    private final BeanAttributeList beanList;
    private final int index;

    public BeanAttribute(BeanAttributeList beanList, int index) {
        this.beanList = beanList;
        this.index = index;
    }

    public QName getQName() {
        return this.beanList.getQName(this.index);
    }

    public Element getParent() {
        return this.beanList.getParent();
    }

    public String getValue() {
        Object data = this.getData();
        return data != null ? data.toString() : null;
    }

    public void setValue(String data) {
        this.beanList.setData(this.index, data);
    }

    public Object getData() {
        return this.beanList.getData(this.index);
    }

    public void setData(Object data) {
        this.beanList.setData(this.index, data);
    }
}

