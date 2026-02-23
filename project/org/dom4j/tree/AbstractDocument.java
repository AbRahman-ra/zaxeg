/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.IllegalAddException;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;
import org.dom4j.QName;
import org.dom4j.Text;
import org.dom4j.Visitor;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.AbstractBranch;

public abstract class AbstractDocument
extends AbstractBranch
implements Document {
    protected String encoding;

    public short getNodeType() {
        return 9;
    }

    public String getPath(Element context) {
        return "/";
    }

    public String getUniquePath(Element context) {
        return "/";
    }

    public Document getDocument() {
        return this;
    }

    public String getXMLEncoding() {
        return null;
    }

    public String getStringValue() {
        Element root = this.getRootElement();
        return root != null ? root.getStringValue() : "";
    }

    public String asXML() {
        OutputFormat format = new OutputFormat();
        format.setEncoding(this.encoding);
        try {
            StringWriter out = new StringWriter();
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(this);
            writer.flush();
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException("IOException while generating textual representation: " + e.getMessage());
        }
    }

    public void write(Writer out) throws IOException {
        OutputFormat format = new OutputFormat();
        format.setEncoding(this.encoding);
        XMLWriter writer = new XMLWriter(out, format);
        writer.write(this);
    }

    public void accept(Visitor visitor) {
        List content;
        visitor.visit(this);
        DocumentType docType = this.getDocType();
        if (docType != null) {
            visitor.visit(docType);
        }
        if ((content = this.content()) != null) {
            Iterator iter = content.iterator();
            while (iter.hasNext()) {
                Object object = iter.next();
                if (object instanceof String) {
                    Text text = this.getDocumentFactory().createText((String)object);
                    visitor.visit(text);
                    continue;
                }
                Node node = (Node)object;
                node.accept(visitor);
            }
        }
    }

    public String toString() {
        return super.toString() + " [Document: name " + this.getName() + "]";
    }

    public void normalize() {
        Element element = this.getRootElement();
        if (element != null) {
            element.normalize();
        }
    }

    public Document addComment(String comment) {
        Comment node = this.getDocumentFactory().createComment(comment);
        this.add(node);
        return this;
    }

    public Document addProcessingInstruction(String target, String data) {
        ProcessingInstruction node = this.getDocumentFactory().createProcessingInstruction(target, data);
        this.add(node);
        return this;
    }

    public Document addProcessingInstruction(String target, Map data) {
        ProcessingInstruction node = this.getDocumentFactory().createProcessingInstruction(target, data);
        this.add(node);
        return this;
    }

    public Element addElement(String name) {
        Element element = this.getDocumentFactory().createElement(name);
        this.add(element);
        return element;
    }

    public Element addElement(String qualifiedName, String namespaceURI) {
        Element element = this.getDocumentFactory().createElement(qualifiedName, namespaceURI);
        this.add(element);
        return element;
    }

    public Element addElement(QName qName) {
        Element element = this.getDocumentFactory().createElement(qName);
        this.add(element);
        return element;
    }

    public void setRootElement(Element rootElement) {
        this.clearContent();
        if (rootElement != null) {
            super.add(rootElement);
            this.rootElementAdded(rootElement);
        }
    }

    public void add(Element element) {
        this.checkAddElementAllowed(element);
        super.add(element);
        this.rootElementAdded(element);
    }

    public boolean remove(Element element) {
        boolean answer = super.remove(element);
        Element root = this.getRootElement();
        if (root != null && answer) {
            this.setRootElement(null);
        }
        element.setDocument(null);
        return answer;
    }

    public Node asXPathResult(Element parent) {
        return this;
    }

    protected void childAdded(Node node) {
        if (node != null) {
            node.setDocument(this);
        }
    }

    protected void childRemoved(Node node) {
        if (node != null) {
            node.setDocument(null);
        }
    }

    protected void checkAddElementAllowed(Element element) {
        Element root = this.getRootElement();
        if (root != null) {
            throw new IllegalAddException(this, (Node)element, "Cannot add another element to this Document as it already has a root element of: " + root.getQualifiedName());
        }
    }

    protected abstract void rootElementAdded(Element var1);

    public void setXMLEncoding(String enc) {
        this.encoding = enc;
    }
}

