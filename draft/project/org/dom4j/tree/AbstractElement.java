/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Attribute;
import org.dom4j.CDATA;
import org.dom4j.CharacterData;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Entity;
import org.dom4j.IllegalAddException;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;
import org.dom4j.QName;
import org.dom4j.Text;
import org.dom4j.Visitor;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.AbstractBranch;
import org.dom4j.tree.BackedList;
import org.dom4j.tree.ContentListFacade;
import org.dom4j.tree.NamespaceStack;
import org.dom4j.tree.SingleIterator;
import org.xml.sax.Attributes;

public abstract class AbstractElement
extends AbstractBranch
implements Element {
    private static final DocumentFactory DOCUMENT_FACTORY = DocumentFactory.getInstance();
    protected static final List EMPTY_LIST = Collections.EMPTY_LIST;
    protected static final Iterator EMPTY_ITERATOR = EMPTY_LIST.iterator();
    protected static final boolean VERBOSE_TOSTRING = false;
    protected static final boolean USE_STRINGVALUE_SEPARATOR = false;

    public short getNodeType() {
        return 1;
    }

    public boolean isRootElement() {
        Element root;
        Document document = this.getDocument();
        return document != null && (root = document.getRootElement()) == this;
    }

    public void setName(String name) {
        this.setQName(this.getDocumentFactory().createQName(name));
    }

    public void setNamespace(Namespace namespace) {
        this.setQName(this.getDocumentFactory().createQName(this.getName(), namespace));
    }

    public String getXPathNameStep() {
        String uri = this.getNamespaceURI();
        if (uri == null || uri.length() == 0) {
            return this.getName();
        }
        String prefix = this.getNamespacePrefix();
        if (prefix == null || prefix.length() == 0) {
            return "*[name()='" + this.getName() + "']";
        }
        return this.getQualifiedName();
    }

    public String getPath(Element context) {
        if (this == context) {
            return ".";
        }
        Element parent = this.getParent();
        if (parent == null) {
            return "/" + this.getXPathNameStep();
        }
        if (parent == context) {
            return this.getXPathNameStep();
        }
        return parent.getPath(context) + "/" + this.getXPathNameStep();
    }

    public String getUniquePath(Element context) {
        int idx;
        Element parent = this.getParent();
        if (parent == null) {
            return "/" + this.getXPathNameStep();
        }
        StringBuffer buffer = new StringBuffer();
        if (parent != context) {
            buffer.append(parent.getUniquePath(context));
            buffer.append("/");
        }
        buffer.append(this.getXPathNameStep());
        List mySiblings = parent.elements(this.getQName());
        if (mySiblings.size() > 1 && (idx = mySiblings.indexOf(this)) >= 0) {
            buffer.append("[");
            buffer.append(Integer.toString(++idx));
            buffer.append("]");
        }
        return buffer.toString();
    }

    public String asXML() {
        try {
            StringWriter out = new StringWriter();
            XMLWriter writer = new XMLWriter(out, new OutputFormat());
            writer.write(this);
            writer.flush();
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException("IOException while generating textual representation: " + e.getMessage());
        }
    }

    public void write(Writer out) throws IOException {
        XMLWriter writer = new XMLWriter(out, new OutputFormat());
        writer.write(this);
    }

    public void accept(Visitor visitor) {
        int i;
        visitor.visit(this);
        int size = this.attributeCount();
        for (i = 0; i < size; ++i) {
            Attribute attribute = this.attribute(i);
            visitor.visit(attribute);
        }
        size = this.nodeCount();
        for (i = 0; i < size; ++i) {
            Node node = this.node(i);
            node.accept(visitor);
        }
    }

    public String toString() {
        String uri = this.getNamespaceURI();
        if (uri != null && uri.length() > 0) {
            return super.toString() + " [Element: <" + this.getQualifiedName() + " uri: " + uri + " attributes: " + this.attributeList() + "/>]";
        }
        return super.toString() + " [Element: <" + this.getQualifiedName() + " attributes: " + this.attributeList() + "/>]";
    }

    public Namespace getNamespace() {
        return this.getQName().getNamespace();
    }

    public String getName() {
        return this.getQName().getName();
    }

    public String getNamespacePrefix() {
        return this.getQName().getNamespacePrefix();
    }

    public String getNamespaceURI() {
        return this.getQName().getNamespaceURI();
    }

    public String getQualifiedName() {
        return this.getQName().getQualifiedName();
    }

    public Object getData() {
        return this.getText();
    }

    public void setData(Object data) {
    }

    public Node node(int index) {
        if (index >= 0) {
            List list = this.contentList();
            if (index >= list.size()) {
                return null;
            }
            Object node = list.get(index);
            if (node != null) {
                if (node instanceof Node) {
                    return (Node)node;
                }
                return this.getDocumentFactory().createText(node.toString());
            }
        }
        return null;
    }

    public int indexOf(Node node) {
        return this.contentList().indexOf(node);
    }

    public int nodeCount() {
        return this.contentList().size();
    }

    public Iterator nodeIterator() {
        return this.contentList().iterator();
    }

    public Element element(String name) {
        List list = this.contentList();
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            Element element;
            Object object = list.get(i);
            if (!(object instanceof Element) || !name.equals((element = (Element)object).getName())) continue;
            return element;
        }
        return null;
    }

    public Element element(QName qName) {
        List list = this.contentList();
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            Element element;
            Object object = list.get(i);
            if (!(object instanceof Element) || !qName.equals((element = (Element)object).getQName())) continue;
            return element;
        }
        return null;
    }

    public Element element(String name, Namespace namespace) {
        return this.element(this.getDocumentFactory().createQName(name, namespace));
    }

    public List elements() {
        List list = this.contentList();
        BackedList answer = this.createResultList();
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            Object object = list.get(i);
            if (!(object instanceof Element)) continue;
            answer.addLocal(object);
        }
        return answer;
    }

    public List elements(String name) {
        List list = this.contentList();
        BackedList answer = this.createResultList();
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            Element element;
            Object object = list.get(i);
            if (!(object instanceof Element) || !name.equals((element = (Element)object).getName())) continue;
            answer.addLocal(element);
        }
        return answer;
    }

    public List elements(QName qName) {
        List list = this.contentList();
        BackedList answer = this.createResultList();
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            Element element;
            Object object = list.get(i);
            if (!(object instanceof Element) || !qName.equals((element = (Element)object).getQName())) continue;
            answer.addLocal(element);
        }
        return answer;
    }

    public List elements(String name, Namespace namespace) {
        return this.elements(this.getDocumentFactory().createQName(name, namespace));
    }

    public Iterator elementIterator() {
        List list = this.elements();
        return list.iterator();
    }

    public Iterator elementIterator(String name) {
        List list = this.elements(name);
        return list.iterator();
    }

    public Iterator elementIterator(QName qName) {
        List list = this.elements(qName);
        return list.iterator();
    }

    public Iterator elementIterator(String name, Namespace ns) {
        return this.elementIterator(this.getDocumentFactory().createQName(name, ns));
    }

    public List attributes() {
        return new ContentListFacade(this, this.attributeList());
    }

    public Iterator attributeIterator() {
        return this.attributeList().iterator();
    }

    public Attribute attribute(int index) {
        return (Attribute)this.attributeList().get(index);
    }

    public int attributeCount() {
        return this.attributeList().size();
    }

    public Attribute attribute(String name) {
        List list = this.attributeList();
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            Attribute attribute = (Attribute)list.get(i);
            if (!name.equals(attribute.getName())) continue;
            return attribute;
        }
        return null;
    }

    public Attribute attribute(QName qName) {
        List list = this.attributeList();
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            Attribute attribute = (Attribute)list.get(i);
            if (!qName.equals(attribute.getQName())) continue;
            return attribute;
        }
        return null;
    }

    public Attribute attribute(String name, Namespace namespace) {
        return this.attribute(this.getDocumentFactory().createQName(name, namespace));
    }

    public void setAttributes(Attributes attributes, NamespaceStack namespaceStack, boolean noNamespaceAttributes) {
        int size = attributes.getLength();
        if (size > 0) {
            DocumentFactory factory = this.getDocumentFactory();
            if (size == 1) {
                String name = attributes.getQName(0);
                if (noNamespaceAttributes || !name.startsWith("xmlns")) {
                    String attributeURI = attributes.getURI(0);
                    String attributeLocalName = attributes.getLocalName(0);
                    String attributeValue = attributes.getValue(0);
                    QName attributeQName = namespaceStack.getAttributeQName(attributeURI, attributeLocalName, name);
                    this.add(factory.createAttribute((Element)this, attributeQName, attributeValue));
                }
            } else {
                List list = this.attributeList(size);
                list.clear();
                for (int i = 0; i < size; ++i) {
                    String attributeName = attributes.getQName(i);
                    if (!noNamespaceAttributes && attributeName.startsWith("xmlns")) continue;
                    String attributeURI = attributes.getURI(i);
                    String attributeLocalName = attributes.getLocalName(i);
                    String attributeValue = attributes.getValue(i);
                    QName attributeQName = namespaceStack.getAttributeQName(attributeURI, attributeLocalName, attributeName);
                    Attribute attribute = factory.createAttribute((Element)this, attributeQName, attributeValue);
                    list.add(attribute);
                    this.childAdded(attribute);
                }
            }
        }
    }

    public String attributeValue(String name) {
        Attribute attrib = this.attribute(name);
        if (attrib == null) {
            return null;
        }
        return attrib.getValue();
    }

    public String attributeValue(QName qName) {
        Attribute attrib = this.attribute(qName);
        if (attrib == null) {
            return null;
        }
        return attrib.getValue();
    }

    public String attributeValue(String name, String defaultValue) {
        String answer = this.attributeValue(name);
        return answer != null ? answer : defaultValue;
    }

    public String attributeValue(QName qName, String defaultValue) {
        String answer = this.attributeValue(qName);
        return answer != null ? answer : defaultValue;
    }

    public void setAttributeValue(String name, String value) {
        this.addAttribute(name, value);
    }

    public void setAttributeValue(QName qName, String value) {
        this.addAttribute(qName, value);
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
            this.attributeList().add(attribute);
            this.childAdded(attribute);
        }
    }

    public boolean remove(Attribute attribute) {
        List list = this.attributeList();
        boolean answer = list.remove(attribute);
        if (answer) {
            this.childRemoved(attribute);
        } else {
            Attribute copy = this.attribute(attribute.getQName());
            if (copy != null) {
                list.remove(copy);
                answer = true;
            }
        }
        return answer;
    }

    public List processingInstructions() {
        List list = this.contentList();
        BackedList answer = this.createResultList();
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            Object object = list.get(i);
            if (!(object instanceof ProcessingInstruction)) continue;
            answer.addLocal(object);
        }
        return answer;
    }

    public List processingInstructions(String target) {
        List list = this.contentList();
        BackedList answer = this.createResultList();
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            ProcessingInstruction pi;
            Object object = list.get(i);
            if (!(object instanceof ProcessingInstruction) || !target.equals((pi = (ProcessingInstruction)object).getName())) continue;
            answer.addLocal(pi);
        }
        return answer;
    }

    public ProcessingInstruction processingInstruction(String target) {
        List list = this.contentList();
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            ProcessingInstruction pi;
            Object object = list.get(i);
            if (!(object instanceof ProcessingInstruction) || !target.equals((pi = (ProcessingInstruction)object).getName())) continue;
            return pi;
        }
        return null;
    }

    public boolean removeProcessingInstruction(String target) {
        List list = this.contentList();
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            ProcessingInstruction pi;
            Object object = iter.next();
            if (!(object instanceof ProcessingInstruction) || !target.equals((pi = (ProcessingInstruction)object).getName())) continue;
            iter.remove();
            return true;
        }
        return false;
    }

    public Node getXPathResult(int index) {
        Node answer = this.node(index);
        if (answer != null && !answer.supportsParent()) {
            return answer.asXPathResult(this);
        }
        return answer;
    }

    public Element addAttribute(String name, String value) {
        Attribute attribute = this.attribute(name);
        if (value != null) {
            if (attribute == null) {
                this.add(this.getDocumentFactory().createAttribute((Element)this, name, value));
            } else if (attribute.isReadOnly()) {
                this.remove(attribute);
                this.add(this.getDocumentFactory().createAttribute((Element)this, name, value));
            } else {
                attribute.setValue(value);
            }
        } else if (attribute != null) {
            this.remove(attribute);
        }
        return this;
    }

    public Element addAttribute(QName qName, String value) {
        Attribute attribute = this.attribute(qName);
        if (value != null) {
            if (attribute == null) {
                this.add(this.getDocumentFactory().createAttribute((Element)this, qName, value));
            } else if (attribute.isReadOnly()) {
                this.remove(attribute);
                this.add(this.getDocumentFactory().createAttribute((Element)this, qName, value));
            } else {
                attribute.setValue(value);
            }
        } else if (attribute != null) {
            this.remove(attribute);
        }
        return this;
    }

    public Element addCDATA(String cdata) {
        CDATA node = this.getDocumentFactory().createCDATA(cdata);
        this.addNewNode(node);
        return this;
    }

    public Element addComment(String comment) {
        Comment node = this.getDocumentFactory().createComment(comment);
        this.addNewNode(node);
        return this;
    }

    public Element addElement(String name) {
        Element node;
        DocumentFactory factory = this.getDocumentFactory();
        int index = name.indexOf(":");
        String prefix = "";
        String localName = name;
        Namespace namespace = null;
        if (index > 0) {
            prefix = name.substring(0, index);
            localName = name.substring(index + 1);
            namespace = this.getNamespaceForPrefix(prefix);
            if (namespace == null) {
                throw new IllegalAddException("No such namespace prefix: " + prefix + " is in scope on: " + this + " so cannot add element: " + name);
            }
        } else {
            namespace = this.getNamespaceForPrefix("");
        }
        if (namespace != null) {
            QName qname = factory.createQName(localName, namespace);
            node = factory.createElement(qname);
        } else {
            node = factory.createElement(name);
        }
        this.addNewNode(node);
        return node;
    }

    public Element addEntity(String name, String text) {
        Entity node = this.getDocumentFactory().createEntity(name, text);
        this.addNewNode(node);
        return this;
    }

    public Element addNamespace(String prefix, String uri) {
        Namespace node = this.getDocumentFactory().createNamespace(prefix, uri);
        this.addNewNode(node);
        return this;
    }

    public Element addProcessingInstruction(String target, String data) {
        ProcessingInstruction node = this.getDocumentFactory().createProcessingInstruction(target, data);
        this.addNewNode(node);
        return this;
    }

    public Element addProcessingInstruction(String target, Map data) {
        ProcessingInstruction node = this.getDocumentFactory().createProcessingInstruction(target, data);
        this.addNewNode(node);
        return this;
    }

    public Element addText(String text) {
        Text node = this.getDocumentFactory().createText(text);
        this.addNewNode(node);
        return this;
    }

    public void add(Node node) {
        switch (node.getNodeType()) {
            case 1: {
                this.add((Element)node);
                break;
            }
            case 2: {
                this.add((Attribute)node);
                break;
            }
            case 3: {
                this.add((Text)node);
                break;
            }
            case 4: {
                this.add((CDATA)node);
                break;
            }
            case 5: {
                this.add((Entity)node);
                break;
            }
            case 7: {
                this.add((ProcessingInstruction)node);
                break;
            }
            case 8: {
                this.add((Comment)node);
                break;
            }
            case 13: {
                this.add((Namespace)node);
                break;
            }
            default: {
                this.invalidNodeTypeAddException(node);
            }
        }
    }

    public boolean remove(Node node) {
        switch (node.getNodeType()) {
            case 1: {
                return this.remove((Element)node);
            }
            case 2: {
                return this.remove((Attribute)node);
            }
            case 3: {
                return this.remove((Text)node);
            }
            case 4: {
                return this.remove((CDATA)node);
            }
            case 5: {
                return this.remove((Entity)node);
            }
            case 7: {
                return this.remove((ProcessingInstruction)node);
            }
            case 8: {
                return this.remove((Comment)node);
            }
            case 13: {
                return this.remove((Namespace)node);
            }
        }
        return false;
    }

    public void add(CDATA cdata) {
        this.addNode(cdata);
    }

    public void add(Comment comment) {
        this.addNode(comment);
    }

    public void add(Element element) {
        this.addNode(element);
    }

    public void add(Entity entity) {
        this.addNode(entity);
    }

    public void add(Namespace namespace) {
        this.addNode(namespace);
    }

    public void add(ProcessingInstruction pi) {
        this.addNode(pi);
    }

    public void add(Text text) {
        this.addNode(text);
    }

    public boolean remove(CDATA cdata) {
        return this.removeNode(cdata);
    }

    public boolean remove(Comment comment) {
        return this.removeNode(comment);
    }

    public boolean remove(Element element) {
        return this.removeNode(element);
    }

    public boolean remove(Entity entity) {
        return this.removeNode(entity);
    }

    public boolean remove(Namespace namespace) {
        return this.removeNode(namespace);
    }

    public boolean remove(ProcessingInstruction pi) {
        return this.removeNode(pi);
    }

    public boolean remove(Text text) {
        return this.removeNode(text);
    }

    public boolean hasMixedContent() {
        List content = this.contentList();
        if (content == null || content.isEmpty() || content.size() < 2) {
            return false;
        }
        Class<?> prevClass = null;
        Iterator iter = content.iterator();
        while (iter.hasNext()) {
            Object object = iter.next();
            Class<?> newClass = object.getClass();
            if (newClass == prevClass) continue;
            if (prevClass != null) {
                return true;
            }
            prevClass = newClass;
        }
        return false;
    }

    public boolean isTextOnly() {
        List content = this.contentList();
        if (content == null || content.isEmpty()) {
            return true;
        }
        Iterator iter = content.iterator();
        while (iter.hasNext()) {
            Object object = iter.next();
            if (object instanceof CharacterData || object instanceof String) continue;
            return false;
        }
        return true;
    }

    public void setText(String text) {
        List allContent = this.contentList();
        if (allContent != null) {
            Iterator it = allContent.iterator();
            while (it.hasNext()) {
                Node node = (Node)it.next();
                switch (node.getNodeType()) {
                    case 3: 
                    case 4: 
                    case 5: {
                        it.remove();
                    }
                }
            }
        }
        this.addText(text);
    }

    public String getStringValue() {
        List list = this.contentList();
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
        return "";
    }

    public void normalize() {
        List content = this.contentList();
        CharacterData previousText = null;
        int i = 0;
        while (i < content.size()) {
            Node node = (Node)content.get(i);
            if (node instanceof Text) {
                Text text = (Text)node;
                if (previousText != null) {
                    previousText.appendText(text.getText());
                    this.remove(text);
                    continue;
                }
                String value = text.getText();
                if (value == null || value.length() <= 0) {
                    this.remove(text);
                    continue;
                }
                previousText = text;
                ++i;
                continue;
            }
            if (node instanceof Element) {
                Element element = (Element)node;
                element.normalize();
            }
            previousText = null;
            ++i;
        }
    }

    public String elementText(String name) {
        Element element = this.element(name);
        return element != null ? element.getText() : null;
    }

    public String elementText(QName qName) {
        Element element = this.element(qName);
        return element != null ? element.getText() : null;
    }

    public String elementTextTrim(String name) {
        Element element = this.element(name);
        return element != null ? element.getTextTrim() : null;
    }

    public String elementTextTrim(QName qName) {
        Element element = this.element(qName);
        return element != null ? element.getTextTrim() : null;
    }

    public void appendAttributes(Element element) {
        int size = element.attributeCount();
        for (int i = 0; i < size; ++i) {
            Attribute attribute = element.attribute(i);
            if (attribute.supportsParent()) {
                this.addAttribute(attribute.getQName(), attribute.getValue());
                continue;
            }
            this.add(attribute);
        }
    }

    public Element createCopy() {
        Element clone = this.createElement(this.getQName());
        clone.appendAttributes(this);
        clone.appendContent(this);
        return clone;
    }

    public Element createCopy(String name) {
        Element clone = this.createElement(name);
        clone.appendAttributes(this);
        clone.appendContent(this);
        return clone;
    }

    public Element createCopy(QName qName) {
        Element clone = this.createElement(qName);
        clone.appendAttributes(this);
        clone.appendContent(this);
        return clone;
    }

    public QName getQName(String qualifiedName) {
        Namespace namespace;
        String prefix = "";
        String localName = qualifiedName;
        int index = qualifiedName.indexOf(":");
        if (index > 0) {
            prefix = qualifiedName.substring(0, index);
            localName = qualifiedName.substring(index + 1);
        }
        if ((namespace = this.getNamespaceForPrefix(prefix)) != null) {
            return this.getDocumentFactory().createQName(localName, namespace);
        }
        return this.getDocumentFactory().createQName(localName);
    }

    public Namespace getNamespaceForPrefix(String prefix) {
        Namespace answer;
        if (prefix == null) {
            prefix = "";
        }
        if (prefix.equals(this.getNamespacePrefix())) {
            return this.getNamespace();
        }
        if (prefix.equals("xml")) {
            return Namespace.XML_NAMESPACE;
        }
        List list = this.contentList();
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            Namespace namespace;
            Object object = list.get(i);
            if (!(object instanceof Namespace) || !prefix.equals((namespace = (Namespace)object).getPrefix())) continue;
            return namespace;
        }
        Element parent = this.getParent();
        if (parent != null && (answer = parent.getNamespaceForPrefix(prefix)) != null) {
            return answer;
        }
        if (prefix == null || prefix.length() <= 0) {
            return Namespace.NO_NAMESPACE;
        }
        return null;
    }

    public Namespace getNamespaceForURI(String uri) {
        if (uri == null || uri.length() <= 0) {
            return Namespace.NO_NAMESPACE;
        }
        if (uri.equals(this.getNamespaceURI())) {
            return this.getNamespace();
        }
        List list = this.contentList();
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            Namespace namespace;
            Object object = list.get(i);
            if (!(object instanceof Namespace) || !uri.equals((namespace = (Namespace)object).getURI())) continue;
            return namespace;
        }
        return null;
    }

    public List getNamespacesForURI(String uri) {
        BackedList answer = this.createResultList();
        List list = this.contentList();
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            Object object = list.get(i);
            if (!(object instanceof Namespace) || !((Namespace)object).getURI().equals(uri)) continue;
            answer.addLocal(object);
        }
        return answer;
    }

    public List declaredNamespaces() {
        BackedList answer = this.createResultList();
        List list = this.contentList();
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            Object object = list.get(i);
            if (!(object instanceof Namespace)) continue;
            answer.addLocal(object);
        }
        return answer;
    }

    public List additionalNamespaces() {
        List list = this.contentList();
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

    public List additionalNamespaces(String defaultNamespaceURI) {
        List list = this.contentList();
        BackedList answer = this.createResultList();
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            Namespace namespace;
            Object object = list.get(i);
            if (!(object instanceof Namespace) || defaultNamespaceURI.equals((namespace = (Namespace)object).getURI())) continue;
            answer.addLocal(namespace);
        }
        return answer;
    }

    public void ensureAttributesCapacity(int minCapacity) {
        List list;
        if (minCapacity > 1 && (list = this.attributeList()) instanceof ArrayList) {
            ArrayList arrayList = (ArrayList)list;
            arrayList.ensureCapacity(minCapacity);
        }
    }

    protected Element createElement(String name) {
        return this.getDocumentFactory().createElement(name);
    }

    protected Element createElement(QName qName) {
        return this.getDocumentFactory().createElement(qName);
    }

    protected void addNode(Node node) {
        if (node.getParent() != null) {
            String message = "The Node already has an existing parent of \"" + node.getParent().getQualifiedName() + "\"";
            throw new IllegalAddException(this, node, message);
        }
        this.addNewNode(node);
    }

    protected void addNode(int index, Node node) {
        if (node.getParent() != null) {
            String message = "The Node already has an existing parent of \"" + node.getParent().getQualifiedName() + "\"";
            throw new IllegalAddException(this, node, message);
        }
        this.addNewNode(index, node);
    }

    protected void addNewNode(Node node) {
        this.contentList().add(node);
        this.childAdded(node);
    }

    protected void addNewNode(int index, Node node) {
        this.contentList().add(index, node);
        this.childAdded(node);
    }

    protected boolean removeNode(Node node) {
        boolean answer = this.contentList().remove(node);
        if (answer) {
            this.childRemoved(node);
        }
        return answer;
    }

    protected void childAdded(Node node) {
        if (node != null) {
            node.setParent(this);
        }
    }

    protected void childRemoved(Node node) {
        if (node != null) {
            node.setParent(null);
            node.setDocument(null);
        }
    }

    protected abstract List attributeList();

    protected abstract List attributeList(int var1);

    protected DocumentFactory getDocumentFactory() {
        DocumentFactory factory;
        QName qName = this.getQName();
        if (qName != null && (factory = qName.getDocumentFactory()) != null) {
            return factory;
        }
        return DOCUMENT_FACTORY;
    }

    protected List createAttributeList() {
        return this.createAttributeList(5);
    }

    protected List createAttributeList(int size) {
        return new ArrayList(size);
    }

    protected Iterator createSingleIterator(Object result) {
        return new SingleIterator(result);
    }
}

