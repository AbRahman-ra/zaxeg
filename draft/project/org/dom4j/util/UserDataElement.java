/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.util;

import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.tree.DefaultElement;

public class UserDataElement
extends DefaultElement {
    private Object data;

    public UserDataElement(String name) {
        super(name);
    }

    public UserDataElement(QName qname) {
        super(qname);
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String toString() {
        return super.toString() + " userData: " + this.data;
    }

    public Object clone() {
        UserDataElement answer = (UserDataElement)super.clone();
        if (answer != this) {
            answer.data = this.getCopyOfUserData();
        }
        return answer;
    }

    protected Object getCopyOfUserData() {
        return this.data;
    }

    protected Element createElement(String name) {
        Element answer = this.getDocumentFactory().createElement(name);
        answer.setData(this.getCopyOfUserData());
        return answer;
    }

    protected Element createElement(QName qName) {
        Element answer = this.getDocumentFactory().createElement(qName);
        answer.setData(this.getCopyOfUserData());
        return answer;
    }
}

