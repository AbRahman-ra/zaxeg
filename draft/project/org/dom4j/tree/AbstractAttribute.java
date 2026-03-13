/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.io.IOException;
import java.io.Writer;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.Visitor;
import org.dom4j.tree.AbstractNode;
import org.dom4j.tree.DefaultAttribute;

public abstract class AbstractAttribute
extends AbstractNode
implements Attribute {
    public short getNodeType() {
        return 2;
    }

    public void setNamespace(Namespace namespace) {
        String msg = "This Attribute is read only and cannot be changed";
        throw new UnsupportedOperationException(msg);
    }

    public String getText() {
        return this.getValue();
    }

    public void setText(String text) {
        this.setValue(text);
    }

    public void setValue(String value) {
        String msg = "This Attribute is read only and cannot be changed";
        throw new UnsupportedOperationException(msg);
    }

    public Object getData() {
        return this.getValue();
    }

    public void setData(Object data) {
        this.setValue(data == null ? null : data.toString());
    }

    public String toString() {
        return super.toString() + " [Attribute: name " + this.getQualifiedName() + " value \"" + this.getValue() + "\"]";
    }

    public String asXML() {
        return this.getQualifiedName() + "=\"" + this.getValue() + "\"";
    }

    public void write(Writer writer) throws IOException {
        writer.write(this.getQualifiedName());
        writer.write("=\"");
        writer.write(this.getValue());
        writer.write("\"");
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
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

    public String getPath(Element context) {
        StringBuffer result = new StringBuffer();
        Element parent = this.getParent();
        if (parent != null && parent != context) {
            result.append(parent.getPath(context));
            result.append("/");
        }
        result.append("@");
        String uri = this.getNamespaceURI();
        String prefix = this.getNamespacePrefix();
        if (uri == null || uri.length() == 0 || prefix == null || prefix.length() == 0) {
            result.append(this.getName());
        } else {
            result.append(this.getQualifiedName());
        }
        return result.toString();
    }

    public String getUniquePath(Element context) {
        StringBuffer result = new StringBuffer();
        Element parent = this.getParent();
        if (parent != null && parent != context) {
            result.append(parent.getUniquePath(context));
            result.append("/");
        }
        result.append("@");
        String uri = this.getNamespaceURI();
        String prefix = this.getNamespacePrefix();
        if (uri == null || uri.length() == 0 || prefix == null || prefix.length() == 0) {
            result.append(this.getName());
        } else {
            result.append(this.getQualifiedName());
        }
        return result.toString();
    }

    protected Node createXPathResult(Element parent) {
        return new DefaultAttribute(parent, this.getQName(), this.getValue());
    }
}

