/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.dom4j.Branch;
import org.dom4j.Comment;
import org.dom4j.Element;
import org.dom4j.IllegalAddException;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;
import org.dom4j.QName;
import org.dom4j.tree.AbstractNode;
import org.dom4j.tree.BackedList;
import org.dom4j.tree.ContentListFacade;

public abstract class AbstractBranch
extends AbstractNode
implements Branch {
    protected static final int DEFAULT_CONTENT_LIST_SIZE = 5;

    public boolean isReadOnly() {
        return false;
    }

    public boolean hasContent() {
        return this.nodeCount() > 0;
    }

    public List content() {
        List backingList = this.contentList();
        return new ContentListFacade(this, backingList);
    }

    public String getText() {
        int size;
        List content = this.contentList();
        if (content != null && (size = content.size()) >= 1) {
            Object first = content.get(0);
            String firstText = this.getContentAsText(first);
            if (size == 1) {
                return firstText;
            }
            StringBuffer buffer = new StringBuffer(firstText);
            for (int i = 1; i < size; ++i) {
                Object node = content.get(i);
                buffer.append(this.getContentAsText(node));
            }
            return buffer.toString();
        }
        return "";
    }

    protected String getContentAsText(Object content) {
        if (content instanceof Node) {
            Node node = (Node)content;
            switch (node.getNodeType()) {
                case 3: 
                case 4: 
                case 5: {
                    return node.getText();
                }
            }
        } else if (content instanceof String) {
            return (String)content;
        }
        return "";
    }

    protected String getContentAsStringValue(Object content) {
        if (content instanceof Node) {
            Node node = (Node)content;
            switch (node.getNodeType()) {
                case 1: 
                case 3: 
                case 4: 
                case 5: {
                    return node.getStringValue();
                }
            }
        } else if (content instanceof String) {
            return (String)content;
        }
        return "";
    }

    public String getTextTrim() {
        String text = this.getText();
        StringBuffer textContent = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(text);
        while (tokenizer.hasMoreTokens()) {
            String str = tokenizer.nextToken();
            textContent.append(str);
            if (!tokenizer.hasMoreTokens()) continue;
            textContent.append(" ");
        }
        return textContent.toString();
    }

    public void setProcessingInstructions(List listOfPIs) {
        Iterator iter = listOfPIs.iterator();
        while (iter.hasNext()) {
            ProcessingInstruction pi = (ProcessingInstruction)iter.next();
            this.addNode(pi);
        }
    }

    public Element addElement(String name) {
        Element node = this.getDocumentFactory().createElement(name);
        this.add(node);
        return node;
    }

    public Element addElement(String qualifiedName, String namespaceURI) {
        Element node = this.getDocumentFactory().createElement(qualifiedName, namespaceURI);
        this.add(node);
        return node;
    }

    public Element addElement(QName qname) {
        Element node = this.getDocumentFactory().createElement(qname);
        this.add(node);
        return node;
    }

    public Element addElement(String name, String prefix, String uri) {
        Namespace namespace = Namespace.get(prefix, uri);
        QName qName = this.getDocumentFactory().createQName(name, namespace);
        return this.addElement(qName);
    }

    public void add(Node node) {
        switch (node.getNodeType()) {
            case 1: {
                this.add((Element)node);
                break;
            }
            case 8: {
                this.add((Comment)node);
                break;
            }
            case 7: {
                this.add((ProcessingInstruction)node);
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
            case 8: {
                return this.remove((Comment)node);
            }
            case 7: {
                return this.remove((ProcessingInstruction)node);
            }
        }
        this.invalidNodeTypeAddException(node);
        return false;
    }

    public void add(Comment comment) {
        this.addNode(comment);
    }

    public void add(Element element) {
        this.addNode(element);
    }

    public void add(ProcessingInstruction pi) {
        this.addNode(pi);
    }

    public boolean remove(Comment comment) {
        return this.removeNode(comment);
    }

    public boolean remove(Element element) {
        return this.removeNode(element);
    }

    public boolean remove(ProcessingInstruction pi) {
        return this.removeNode(pi);
    }

    public Element elementByID(String elementID) {
        int size = this.nodeCount();
        for (int i = 0; i < size; ++i) {
            Node node = this.node(i);
            if (!(node instanceof Element)) continue;
            Element element = (Element)node;
            String id = this.elementID(element);
            if (id != null && id.equals(elementID)) {
                return element;
            }
            if ((element = element.elementByID(elementID)) == null) continue;
            return element;
        }
        return null;
    }

    public void appendContent(Branch branch) {
        int size = branch.nodeCount();
        for (int i = 0; i < size; ++i) {
            Node node = branch.node(i);
            this.add((Node)node.clone());
        }
    }

    public Node node(int index) {
        Object object = this.contentList().get(index);
        if (object instanceof Node) {
            return (Node)object;
        }
        if (object instanceof String) {
            return this.getDocumentFactory().createText(object.toString());
        }
        return null;
    }

    public int nodeCount() {
        return this.contentList().size();
    }

    public int indexOf(Node node) {
        return this.contentList().indexOf(node);
    }

    public Iterator nodeIterator() {
        return this.contentList().iterator();
    }

    protected String elementID(Element element) {
        return element.attributeValue("ID");
    }

    protected abstract List contentList();

    protected List createContentList() {
        return new ArrayList(5);
    }

    protected List createContentList(int size) {
        return new ArrayList(size);
    }

    protected BackedList createResultList() {
        return new BackedList(this, this.contentList());
    }

    protected List createSingleResultList(Object result) {
        BackedList list = new BackedList(this, this.contentList(), 1);
        list.addLocal(result);
        return list;
    }

    protected List createEmptyList() {
        return new BackedList(this, this.contentList(), 0);
    }

    protected abstract void addNode(Node var1);

    protected abstract void addNode(int var1, Node var2);

    protected abstract boolean removeNode(Node var1);

    protected abstract void childAdded(Node var1);

    protected abstract void childRemoved(Node var1);

    protected void contentRemoved() {
        List content = this.contentList();
        int size = content.size();
        for (int i = 0; i < size; ++i) {
            Object object = content.get(i);
            if (!(object instanceof Node)) continue;
            this.childRemoved((Node)object);
        }
    }

    protected void invalidNodeTypeAddException(Node node) {
        throw new IllegalAddException("Invalid node type. Cannot add node: " + node + " to this branch: " + this);
    }
}

