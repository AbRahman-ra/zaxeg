/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.tree.BackedList;
import org.dom4j.tree.DefaultElement;

public class IndexedElement
extends DefaultElement {
    private Map elementIndex;
    private Map attributeIndex;

    public IndexedElement(String name) {
        super(name);
    }

    public IndexedElement(QName qname) {
        super(qname);
    }

    public IndexedElement(QName qname, int attributeCount) {
        super(qname, attributeCount);
    }

    public Attribute attribute(String name) {
        return (Attribute)this.attributeIndex().get(name);
    }

    public Attribute attribute(QName qName) {
        return (Attribute)this.attributeIndex().get(qName);
    }

    public Element element(String name) {
        return this.asElement(this.elementIndex().get(name));
    }

    public Element element(QName qName) {
        return this.asElement(this.elementIndex().get(qName));
    }

    public List elements(String name) {
        return this.asElementList(this.elementIndex().get(name));
    }

    public List elements(QName qName) {
        return this.asElementList(this.elementIndex().get(qName));
    }

    protected Element asElement(Object object) {
        List list;
        if (object instanceof Element) {
            return (Element)object;
        }
        if (object != null && (list = (List)object).size() >= 1) {
            return (Element)list.get(0);
        }
        return null;
    }

    protected List asElementList(Object object) {
        if (object instanceof Element) {
            return this.createSingleResultList(object);
        }
        if (object != null) {
            List list = (List)object;
            BackedList answer = this.createResultList();
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                answer.addLocal(list.get(i));
            }
            return answer;
        }
        return this.createEmptyList();
    }

    protected Iterator asElementIterator(Object object) {
        return this.asElementList(object).iterator();
    }

    protected void addNode(Node node) {
        super.addNode(node);
        if (this.elementIndex != null && node instanceof Element) {
            this.addToElementIndex((Element)node);
        } else if (this.attributeIndex != null && node instanceof Attribute) {
            this.addToAttributeIndex((Attribute)node);
        }
    }

    protected boolean removeNode(Node node) {
        if (super.removeNode(node)) {
            if (this.elementIndex != null && node instanceof Element) {
                this.removeFromElementIndex((Element)node);
            } else if (this.attributeIndex != null && node instanceof Attribute) {
                this.removeFromAttributeIndex((Attribute)node);
            }
            return true;
        }
        return false;
    }

    protected Map attributeIndex() {
        if (this.attributeIndex == null) {
            this.attributeIndex = this.createAttributeIndex();
            Iterator iter = this.attributeIterator();
            while (iter.hasNext()) {
                this.addToAttributeIndex((Attribute)iter.next());
            }
        }
        return this.attributeIndex;
    }

    protected Map elementIndex() {
        if (this.elementIndex == null) {
            this.elementIndex = this.createElementIndex();
            Iterator iter = this.elementIterator();
            while (iter.hasNext()) {
                this.addToElementIndex((Element)iter.next());
            }
        }
        return this.elementIndex;
    }

    protected Map createAttributeIndex() {
        Map answer = this.createIndex();
        return answer;
    }

    protected Map createElementIndex() {
        Map answer = this.createIndex();
        return answer;
    }

    protected void addToElementIndex(Element element) {
        QName qName = element.getQName();
        String name = qName.getName();
        this.addToElementIndex(qName, element);
        this.addToElementIndex(name, element);
    }

    protected void addToElementIndex(Object key, Element value) {
        Object oldValue = this.elementIndex.get(key);
        if (oldValue == null) {
            this.elementIndex.put(key, value);
        } else if (oldValue instanceof List) {
            List list = (List)oldValue;
            list.add(value);
        } else {
            List list = this.createList();
            list.add(oldValue);
            list.add(value);
            this.elementIndex.put(key, list);
        }
    }

    protected void removeFromElementIndex(Element element) {
        QName qName = element.getQName();
        String name = qName.getName();
        this.removeFromElementIndex(qName, element);
        this.removeFromElementIndex(name, element);
    }

    protected void removeFromElementIndex(Object key, Element value) {
        Object oldValue = this.elementIndex.get(key);
        if (oldValue instanceof List) {
            List list = (List)oldValue;
            list.remove(value);
        } else {
            this.elementIndex.remove(key);
        }
    }

    protected void addToAttributeIndex(Attribute attribute) {
        QName qName = attribute.getQName();
        String name = qName.getName();
        this.addToAttributeIndex(qName, attribute);
        this.addToAttributeIndex(name, attribute);
    }

    protected void addToAttributeIndex(Object key, Attribute value) {
        Object oldValue = this.attributeIndex.get(key);
        if (oldValue != null) {
            this.attributeIndex.put(key, value);
        }
    }

    protected void removeFromAttributeIndex(Attribute attribute) {
        QName qName = attribute.getQName();
        String name = qName.getName();
        this.removeFromAttributeIndex(qName, attribute);
        this.removeFromAttributeIndex(name, attribute);
    }

    protected void removeFromAttributeIndex(Object key, Attribute value) {
        Object oldValue = this.attributeIndex.get(key);
        if (oldValue != null && oldValue.equals(value)) {
            this.attributeIndex.remove(key);
        }
    }

    protected Map createIndex() {
        return new HashMap();
    }

    protected List createList() {
        return new ArrayList();
    }
}

