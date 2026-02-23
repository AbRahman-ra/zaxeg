/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.Visitor;
import org.dom4j.tree.AbstractNode;

public abstract class AbstractDocumentType
extends AbstractNode
implements DocumentType {
    public short getNodeType() {
        return 10;
    }

    public String getName() {
        return this.getElementName();
    }

    public void setName(String name) {
        this.setElementName(name);
    }

    public String getPath(Element context) {
        return "";
    }

    public String getUniquePath(Element context) {
        return "";
    }

    public String getText() {
        List list = this.getInternalDeclarations();
        if (list != null && list.size() > 0) {
            StringBuffer buffer = new StringBuffer();
            Iterator iter = list.iterator();
            if (iter.hasNext()) {
                Object decl = iter.next();
                buffer.append(decl.toString());
                while (iter.hasNext()) {
                    decl = iter.next();
                    buffer.append("\n");
                    buffer.append(decl.toString());
                }
            }
            return buffer.toString();
        }
        return "";
    }

    public String toString() {
        return super.toString() + " [DocumentType: " + this.asXML() + "]";
    }

    public String asXML() {
        String systemID;
        StringBuffer buffer = new StringBuffer("<!DOCTYPE ");
        buffer.append(this.getElementName());
        boolean hasPublicID = false;
        String publicID = this.getPublicID();
        if (publicID != null && publicID.length() > 0) {
            buffer.append(" PUBLIC \"");
            buffer.append(publicID);
            buffer.append("\"");
            hasPublicID = true;
        }
        if ((systemID = this.getSystemID()) != null && systemID.length() > 0) {
            if (!hasPublicID) {
                buffer.append(" SYSTEM");
            }
            buffer.append(" \"");
            buffer.append(systemID);
            buffer.append("\"");
        }
        buffer.append(">");
        return buffer.toString();
    }

    public void write(Writer writer) throws IOException {
        List list;
        String systemID;
        writer.write("<!DOCTYPE ");
        writer.write(this.getElementName());
        boolean hasPublicID = false;
        String publicID = this.getPublicID();
        if (publicID != null && publicID.length() > 0) {
            writer.write(" PUBLIC \"");
            writer.write(publicID);
            writer.write("\"");
            hasPublicID = true;
        }
        if ((systemID = this.getSystemID()) != null && systemID.length() > 0) {
            if (!hasPublicID) {
                writer.write(" SYSTEM");
            }
            writer.write(" \"");
            writer.write(systemID);
            writer.write("\"");
        }
        if ((list = this.getInternalDeclarations()) != null && list.size() > 0) {
            writer.write(" [");
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                Object decl = iter.next();
                writer.write("\n  ");
                writer.write(decl.toString());
            }
            writer.write("\n]");
        }
        writer.write(">");
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

