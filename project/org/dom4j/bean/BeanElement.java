/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.bean;

import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.bean.BeanAttributeList;
import org.dom4j.bean.BeanDocumentFactory;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.NamespaceStack;
import org.xml.sax.Attributes;

public class BeanElement
extends DefaultElement {
    private static final DocumentFactory DOCUMENT_FACTORY = BeanDocumentFactory.getInstance();
    private Object bean;
    static /* synthetic */ Class class$org$dom4j$bean$BeanElement;

    public BeanElement(String name, Object bean) {
        this(DOCUMENT_FACTORY.createQName(name), bean);
    }

    public BeanElement(String name, Namespace namespace, Object bean) {
        this(DOCUMENT_FACTORY.createQName(name, namespace), bean);
    }

    public BeanElement(QName qname, Object bean) {
        super(qname);
        this.bean = bean;
    }

    public BeanElement(QName qname) {
        super(qname);
    }

    public Object getData() {
        return this.bean;
    }

    public void setData(Object data) {
        this.bean = data;
        this.setAttributeList(null);
    }

    public Attribute attribute(String name) {
        return this.getBeanAttributeList().attribute(name);
    }

    public Attribute attribute(QName qname) {
        return this.getBeanAttributeList().attribute(qname);
    }

    public Element addAttribute(String name, String value) {
        Attribute attribute = this.attribute(name);
        if (attribute != null) {
            attribute.setValue(value);
        }
        return this;
    }

    public Element addAttribute(QName qName, String value) {
        Attribute attribute = this.attribute(qName);
        if (attribute != null) {
            attribute.setValue(value);
        }
        return this;
    }

    public void setAttributes(List attributes) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void setAttributes(Attributes attributes, NamespaceStack namespaceStack, boolean noNamespaceAttributes) {
        String className = attributes.getValue("class");
        if (className != null) {
            try {
                Class<?> beanClass = Class.forName(className, true, (class$org$dom4j$bean$BeanElement == null ? (class$org$dom4j$bean$BeanElement = BeanElement.class$("org.dom4j.bean.BeanElement")) : class$org$dom4j$bean$BeanElement).getClassLoader());
                this.setData(beanClass.newInstance());
                for (int i = 0; i < attributes.getLength(); ++i) {
                    String attributeName = attributes.getLocalName(i);
                    if ("class".equalsIgnoreCase(attributeName)) continue;
                    this.addAttribute(attributeName, attributes.getValue(i));
                }
            } catch (Exception ex) {
                ((BeanDocumentFactory)this.getDocumentFactory()).handleException(ex);
            }
        } else {
            super.setAttributes(attributes, namespaceStack, noNamespaceAttributes);
        }
    }

    protected DocumentFactory getDocumentFactory() {
        return DOCUMENT_FACTORY;
    }

    protected BeanAttributeList getBeanAttributeList() {
        return (BeanAttributeList)this.attributeList();
    }

    protected List createAttributeList() {
        return new BeanAttributeList(this);
    }

    protected List createAttributeList(int size) {
        return new BeanAttributeList(this);
    }

    static /* synthetic */ Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }
}

