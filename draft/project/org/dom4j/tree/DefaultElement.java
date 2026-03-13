/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.IllegalAddException;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;
import org.dom4j.QName;
import org.dom4j.Text;
import org.dom4j.tree.AbstractElement;
import org.dom4j.tree.BackedList;
import org.dom4j.tree.ContentListFacade;
import org.dom4j.tree.DefaultText;

public class DefaultElement
extends AbstractElement {
    private static final transient DocumentFactory DOCUMENT_FACTORY = DocumentFactory.getInstance();
    private QName qname;
    private Branch parentBranch;
    private Object content;
    private Object attributes;

    public DefaultElement(String name) {
        this.qname = DOCUMENT_FACTORY.createQName(name);
    }

    public DefaultElement(QName qname) {
        this.qname = qname;
    }

    public DefaultElement(QName qname, int attributeCount) {
        this.qname = qname;
        if (attributeCount > 1) {
            this.attributes = new ArrayList(attributeCount);
        }
    }

    public DefaultElement(String name, Namespace namespace) {
        this.qname = DOCUMENT_FACTORY.createQName(name, namespace);
    }

    public Element getParent() {
        Element result = null;
        if (this.parentBranch instanceof Element) {
            result = (Element)this.parentBranch;
        }
        return result;
    }

    public void setParent(Element parent) {
        if (this.parentBranch instanceof Element || parent != null) {
            this.parentBranch = parent;
        }
    }

    public Document getDocument() {
        if (this.parentBranch instanceof Document) {
            return (Document)this.parentBranch;
        }
        if (this.parentBranch instanceof Element) {
            Element parent = (Element)this.parentBranch;
            return parent.getDocument();
        }
        return null;
    }

    public void setDocument(Document document) {
        if (this.parentBranch instanceof Document || document != null) {
            this.parentBranch = document;
        }
    }

    public boolean supportsParent() {
        return true;
    }

    public QName getQName() {
        return this.qname;
    }

    public void setQName(QName name) {
        this.qname = name;
    }

    public String getText() {
        Object contentShadow = this.content;
        if (contentShadow instanceof List) {
            return super.getText();
        }
        if (contentShadow != null) {
            return this.getContentAsText(contentShadow);
        }
        return "";
    }

    public String getStringValue() {
        Object contentShadow = this.content;
        if (contentShadow instanceof List) {
            List list = (List)contentShadow;
            int size = list.size();
            if (size > 0) {
                if (size == 1) {
                    return this.getContentAsStringValue(list.get(0));
                }
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < size; ++i) {
                    Object node = list.get(i);
                    String string = this.getContentAsStringValue(node);
                    if (string.length() <= 0) continue;
                    buffer.append(string);
                }
                return buffer.toString();
            }
        } else if (contentShadow != null) {
            return this.getContentAsStringValue(contentShadow);
        }
        return "";
    }

    public Object clone() {
        DefaultElement answer = (DefaultElement)super.clone();
        if (answer != this) {
            answer.content = null;
            answer.attributes = null;
            answer.appendAttributes(this);
            answer.appendContent(this);
        }
        return answer;
    }

    public Namespace getNamespaceForPrefix(String prefix) {
        Namespace answer;
        Element parent;
        Namespace namespace;
        if (prefix == null) {
            prefix = "";
        }
        if (prefix.equals(this.getNamespacePrefix())) {
            return this.getNamespace();
        }
        if (prefix.equals("xml")) {
            return Namespace.XML_NAMESPACE;
        }
        Object contentShadow = this.content;
        if (contentShadow instanceof List) {
            List list = (List)contentShadow;
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                Namespace namespace2;
                Object object = list.get(i);
                if (!(object instanceof Namespace) || !prefix.equals((namespace2 = (Namespace)object).getPrefix())) continue;
                return namespace2;
            }
        } else if (contentShadow instanceof Namespace && prefix.equals((namespace = (Namespace)contentShadow).getPrefix())) {
            return namespace;
        }
        if ((parent = this.getParent()) != null && (answer = parent.getNamespaceForPrefix(prefix)) != null) {
            return answer;
        }
        if (prefix == null || prefix.length() <= 0) {
            return Namespace.NO_NAMESPACE;
        }
        return null;
    }

    public Namespace getNamespaceForURI(String uri) {
        Element parent;
        Namespace namespace;
        if (uri == null || uri.length() <= 0) {
            return Namespace.NO_NAMESPACE;
        }
        if (uri.equals(this.getNamespaceURI())) {
            return this.getNamespace();
        }
        Object contentShadow = this.content;
        if (contentShadow instanceof List) {
            List list = (List)contentShadow;
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                Namespace namespace2;
                Object object = list.get(i);
                if (!(object instanceof Namespace) || !uri.equals((namespace2 = (Namespace)object).getURI())) continue;
                return namespace2;
            }
        } else if (contentShadow instanceof Namespace && uri.equals((namespace = (Namespace)contentShadow).getURI())) {
            return namespace;
        }
        if ((parent = this.getParent()) != null) {
            return parent.getNamespaceForURI(uri);
        }
        return null;
    }

    public List declaredNamespaces() {
        BackedList answer = this.createResultList();
        Object contentShadow = this.content;
        if (contentShadow instanceof List) {
            List list = (List)contentShadow;
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                Object object = list.get(i);
                if (!(object instanceof Namespace)) continue;
                answer.addLocal(object);
            }
        } else if (contentShadow instanceof Namespace) {
            answer.addLocal(contentShadow);
        }
        return answer;
    }

    public List additionalNamespaces() {
        Object contentShadow = this.content;
        if (contentShadow instanceof List) {
            List list = (List)contentShadow;
            int size = list.size();
            BackedList answer = this.createResultList();
            for (int i = 0; i < size; ++i) {
                Namespace namespace;
                Object object = list.get(i);
                if (!(object instanceof Namespace) || (namespace = (Namespace)object).equals(this.getNamespace())) continue;
                answer.addLocal(namespace);
            }
            return answer;
        }
        if (contentShadow instanceof Namespace) {
            Namespace namespace = (Namespace)contentShadow;
            if (namespace.equals(this.getNamespace())) {
                return this.createEmptyList();
            }
            return this.createSingleResultList(namespace);
        }
        return this.createEmptyList();
    }

    public List additionalNamespaces(String defaultNamespaceURI) {
        Namespace namespace;
        Object contentShadow = this.content;
        if (contentShadow instanceof List) {
            List list = (List)contentShadow;
            BackedList answer = this.createResultList();
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                Namespace namespace2;
                Object object = list.get(i);
                if (!(object instanceof Namespace) || defaultNamespaceURI.equals((namespace2 = (Namespace)object).getURI())) continue;
                answer.addLocal(namespace2);
            }
            return answer;
        }
        if (contentShadow instanceof Namespace && !defaultNamespaceURI.equals((namespace = (Namespace)contentShadow).getURI())) {
            return this.createSingleResultList(namespace);
        }
        return this.createEmptyList();
    }

    public List processingInstructions() {
        Object contentShadow = this.content;
        if (contentShadow instanceof List) {
            List list = (List)contentShadow;
            BackedList answer = this.createResultList();
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                Object object = list.get(i);
                if (!(object instanceof ProcessingInstruction)) continue;
                answer.addLocal(object);
            }
            return answer;
        }
        if (contentShadow instanceof ProcessingInstruction) {
            return this.createSingleResultList(contentShadow);
        }
        return this.createEmptyList();
    }

    public List processingInstructions(String target) {
        ProcessingInstruction pi;
        Object shadow = this.content;
        if (shadow instanceof List) {
            List list = (List)shadow;
            BackedList answer = this.createResultList();
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                ProcessingInstruction pi2;
                Object object = list.get(i);
                if (!(object instanceof ProcessingInstruction) || !target.equals((pi2 = (ProcessingInstruction)object).getName())) continue;
                answer.addLocal(pi2);
            }
            return answer;
        }
        if (shadow instanceof ProcessingInstruction && target.equals((pi = (ProcessingInstruction)shadow).getName())) {
            return this.createSingleResultList(pi);
        }
        return this.createEmptyList();
    }

    public ProcessingInstruction processingInstruction(String target) {
        ProcessingInstruction pi;
        Object shadow = this.content;
        if (shadow instanceof List) {
            List list = (List)shadow;
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                ProcessingInstruction pi2;
                Object object = list.get(i);
                if (!(object instanceof ProcessingInstruction) || !target.equals((pi2 = (ProcessingInstruction)object).getName())) continue;
                return pi2;
            }
        } else if (shadow instanceof ProcessingInstruction && target.equals((pi = (ProcessingInstruction)shadow).getName())) {
            return pi;
        }
        return null;
    }

    public boolean removeProcessingInstruction(String target) {
        ProcessingInstruction pi;
        Object shadow = this.content;
        if (shadow instanceof List) {
            List list = (List)shadow;
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                ProcessingInstruction pi2;
                Object object = iter.next();
                if (!(object instanceof ProcessingInstruction) || !target.equals((pi2 = (ProcessingInstruction)object).getName())) continue;
                iter.remove();
                return true;
            }
        } else if (shadow instanceof ProcessingInstruction && target.equals((pi = (ProcessingInstruction)shadow).getName())) {
            this.content = null;
            return true;
        }
        return false;
    }

    public Element element(String name) {
        Element element;
        Object contentShadow = this.content;
        if (contentShadow instanceof List) {
            List list = (List)contentShadow;
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                Element element2;
                Object object = list.get(i);
                if (!(object instanceof Element) || !name.equals((element2 = (Element)object).getName())) continue;
                return element2;
            }
        } else if (contentShadow instanceof Element && name.equals((element = (Element)contentShadow).getName())) {
            return element;
        }
        return null;
    }

    public Element element(QName qName) {
        Element element;
        Object contentShadow = this.content;
        if (contentShadow instanceof List) {
            List list = (List)contentShadow;
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                Element element2;
                Object object = list.get(i);
                if (!(object instanceof Element) || !qName.equals((element2 = (Element)object).getQName())) continue;
                return element2;
            }
        } else if (contentShadow instanceof Element && qName.equals((element = (Element)contentShadow).getQName())) {
            return element;
        }
        return null;
    }

    public Element element(String name, Namespace namespace) {
        return this.element(this.getDocumentFactory().createQName(name, namespace));
    }

    public void setContent(List content) {
        this.contentRemoved();
        if (content instanceof ContentListFacade) {
            content = ((ContentListFacade)content).getBackingList();
        }
        if (content == null) {
            this.content = null;
        } else {
            int size = content.size();
            List newContent = this.createContentList(size);
            for (int i = 0; i < size; ++i) {
                Object object = content.get(i);
                if (object instanceof Node) {
                    Node node = (Node)object;
                    Element parent = node.getParent();
                    if (parent != null && parent != this) {
                        node = (Node)node.clone();
                    }
                    newContent.add(node);
                    this.childAdded(node);
                    continue;
                }
                if (object == null) continue;
                String text = object.toString();
                Text node = this.getDocumentFactory().createText(text);
                newContent.add(node);
                this.childAdded(node);
            }
            this.content = newContent;
        }
    }

    public void clearContent() {
        if (this.content != null) {
            this.contentRemoved();
            this.content = null;
        }
    }

    public Node node(int index) {
        if (index >= 0) {
            Object node;
            Object contentShadow = this.content;
            if (contentShadow instanceof List) {
                List list = (List)contentShadow;
                if (index >= list.size()) {
                    return null;
                }
                node = list.get(index);
            } else {
                Object object = node = index == 0 ? contentShadow : null;
            }
            if (node != null) {
                if (node instanceof Node) {
                    return (Node)node;
                }
                return new DefaultText(node.toString());
            }
        }
        return null;
    }

    public int indexOf(Node node) {
        Object contentShadow = this.content;
        if (contentShadow instanceof List) {
            List list = (List)contentShadow;
            return list.indexOf(node);
        }
        if (contentShadow != null && contentShadow.equals(node)) {
            return 0;
        }
        return -1;
    }

    public int nodeCount() {
        Object contentShadow = this.content;
        if (contentShadow instanceof List) {
            List list = (List)contentShadow;
            return list.size();
        }
        return contentShadow != null ? 1 : 0;
    }

    public Iterator nodeIterator() {
        Object contentShadow = this.content;
        if (contentShadow instanceof List) {
            List list = (List)contentShadow;
            return list.iterator();
        }
        if (contentShadow != null) {
            return this.createSingleIterator(contentShadow);
        }
        return EMPTY_ITERATOR;
    }

    public List attributes() {
        return new ContentListFacade(this, this.attributeList());
    }

    public void setAttributes(List attributes) {
        if (attributes instanceof ContentListFacade) {
            attributes = ((ContentListFacade)attributes).getBackingList();
        }
        this.attributes = attributes;
    }

    public Iterator attributeIterator() {
        Object attributesShadow = this.attributes;
        if (attributesShadow instanceof List) {
            List list = (List)attributesShadow;
            return list.iterator();
        }
        if (attributesShadow != null) {
            return this.createSingleIterator(attributesShadow);
        }
        return EMPTY_ITERATOR;
    }

    public Attribute attribute(int index) {
        Object attributesShadow = this.attributes;
        if (attributesShadow instanceof List) {
            List list = (List)attributesShadow;
            return (Attribute)list.get(index);
        }
        if (attributesShadow != null && index == 0) {
            return (Attribute)attributesShadow;
        }
        return null;
    }

    public int attributeCount() {
        Object attributesShadow = this.attributes;
        if (attributesShadow instanceof List) {
            List list = (List)attributesShadow;
            return list.size();
        }
        return attributesShadow != null ? 1 : 0;
    }

    public Attribute attribute(String name) {
        Attribute attribute;
        Object attributesShadow = this.attributes;
        if (attributesShadow instanceof List) {
            List list = (List)attributesShadow;
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                Attribute attribute2 = (Attribute)list.get(i);
                if (!name.equals(attribute2.getName())) continue;
                return attribute2;
            }
        } else if (attributesShadow != null && name.equals((attribute = (Attribute)attributesShadow).getName())) {
            return attribute;
        }
        return null;
    }

    public Attribute attribute(QName qName) {
        Attribute attribute;
        Object attributesShadow = this.attributes;
        if (attributesShadow instanceof List) {
            List list = (List)attributesShadow;
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                Attribute attribute2 = (Attribute)list.get(i);
                if (!qName.equals(attribute2.getQName())) continue;
                return attribute2;
            }
        } else if (attributesShadow != null && qName.equals((attribute = (Attribute)attributesShadow).getQName())) {
            return attribute;
        }
        return null;
    }

    public Attribute attribute(String name, Namespace namespace) {
        return this.attribute(this.getDocumentFactory().createQName(name, namespace));
    }

    public void add(Attribute attribute) {
        if (attribute.getParent() != null) {
            String message = "The Attribute already has an existing parent \"" + attribute.getParent().getQualifiedName() + "\"";
            throw new IllegalAddException(this, (Node)attribute, message);
        }
        if (attribute.getValue() == null) {
            Attribute oldAttribute = this.attribute(attribute.getQName());
            if (oldAttribute != null) {
                this.remove(oldAttribute);
            }
        } else {
            if (this.attributes == null) {
                this.attributes = attribute;
            } else {
                this.attributeList().add(attribute);
            }
            this.childAdded(attribute);
        }
    }

    public boolean remove(Attribute attribute) {
        boolean answer = false;
        Object attributesShadow = this.attributes;
        if (attributesShadow instanceof List) {
            Attribute copy;
            List list = (List)attributesShadow;
            answer = list.remove(attribute);
            if (!answer && (copy = this.attribute(attribute.getQName())) != null) {
                list.remove(copy);
                answer = true;
            }
        } else if (attributesShadow != null) {
            if (attribute.equals(attributesShadow)) {
                this.attributes = null;
                answer = true;
            } else {
                Attribute other = (Attribute)attributesShadow;
                if (attribute.getQName().equals(other.getQName())) {
                    this.attributes = null;
                    answer = true;
                }
            }
        }
        if (answer) {
            this.childRemoved(attribute);
        }
        return answer;
    }

    protected void addNewNode(Node node) {
        Object contentShadow = this.content;
        if (contentShadow == null) {
            this.content = node;
        } else if (contentShadow instanceof List) {
            List list = (List)contentShadow;
            list.add(node);
        } else {
            List list = this.createContentList();
            list.add(contentShadow);
            list.add(node);
            this.content = list;
        }
        this.childAdded(node);
    }

    protected boolean removeNode(Node node) {
        boolean answer = false;
        Object contentShadow = this.content;
        if (contentShadow != null) {
            if (contentShadow == node) {
                this.content = null;
                answer = true;
            } else if (contentShadow instanceof List) {
                List list = (List)contentShadow;
                answer = list.remove(node);
            }
        }
        if (answer) {
            this.childRemoved(node);
        }
        return answer;
    }

    protected List contentList() {
        Object contentShadow = this.content;
        if (contentShadow instanceof List) {
            return (List)contentShadow;
        }
        List list = this.createContentList();
        if (contentShadow != null) {
            list.add(contentShadow);
        }
        this.content = list;
        return list;
    }

    protected List attributeList() {
        Object attributesShadow = this.attributes;
        if (attributesShadow instanceof List) {
            return (List)attributesShadow;
        }
        if (attributesShadow != null) {
            List list = this.createAttributeList();
            list.add(attributesShadow);
            this.attributes = list;
            return list;
        }
        List list = this.createAttributeList();
        this.attributes = list;
        return list;
    }

    protected List attributeList(int size) {
        Object attributesShadow = this.attributes;
        if (attributesShadow instanceof List) {
            return (List)attributesShadow;
        }
        if (attributesShadow != null) {
            List list = this.createAttributeList(size);
            list.add(attributesShadow);
            this.attributes = list;
            return list;
        }
        List list = this.createAttributeList(size);
        this.attributes = list;
        return list;
    }

    protected void setAttributeList(List attributeList) {
        this.attributes = attributeList;
    }

    protected DocumentFactory getDocumentFactory() {
        DocumentFactory factory = this.qname.getDocumentFactory();
        return factory != null ? factory : DOCUMENT_FACTORY;
    }
}

