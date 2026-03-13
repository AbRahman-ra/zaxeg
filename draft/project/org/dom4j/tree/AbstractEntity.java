/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.io.IOException;
import java.io.Writer;
import org.dom4j.Element;
import org.dom4j.Entity;
import org.dom4j.Visitor;
import org.dom4j.tree.AbstractNode;

public abstract class AbstractEntity
extends AbstractNode
implements Entity {
    public short getNodeType() {
        return 5;
    }

    public String getPath(Element context) {
        Element parent = this.getParent();
        return parent != null && parent != context ? parent.getPath(context) + "/text()" : "text()";
    }

    public String getUniquePath(Element context) {
        Element parent = this.getParent();
        return parent != null && parent != context ? parent.getUniquePath(context) + "/text()" : "text()";
    }

    public String toString() {
        return super.toString() + " [Entity: &" + this.getName() + ";]";
    }

    public String getStringValue() {
        return "&" + this.getName() + ";";
    }

    public String asXML() {
        return "&" + this.getName() + ";";
    }

    public void write(Writer writer) throws IOException {
        writer.write("&");
        writer.write(this.getName());
        writer.write(";");
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

