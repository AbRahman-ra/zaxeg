/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j;

import org.dom4j.Attribute;
import org.dom4j.CDATA;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.Entity;
import org.dom4j.Namespace;
import org.dom4j.ProcessingInstruction;
import org.dom4j.Text;
import org.dom4j.Visitor;

public abstract class VisitorSupport
implements Visitor {
    public void visit(Document document) {
    }

    public void visit(DocumentType documentType) {
    }

    public void visit(Element node) {
    }

    public void visit(Attribute node) {
    }

    public void visit(CDATA node) {
    }

    public void visit(Comment node) {
    }

    public void visit(Entity node) {
    }

    public void visit(Namespace namespace) {
    }

    public void visit(ProcessingInstruction node) {
    }

    public void visit(Text node) {
    }
}

