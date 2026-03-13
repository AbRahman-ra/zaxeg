/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import net.sf.saxon.event.BuilderMonitor;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.LinkedTreeBuilder;
import net.sf.saxon.type.SchemaType;

public class LinkedBuilderMonitor
extends BuilderMonitor {
    private LinkedTreeBuilder builder;
    private int mark = -1;
    private NodeInfo markedNode;

    public LinkedBuilderMonitor(LinkedTreeBuilder builder) {
        super(builder);
        this.builder = builder;
    }

    @Override
    public void markNextNode(int nodeKind) {
        this.mark = nodeKind;
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        super.startDocument(properties);
        if (this.mark == 9) {
            this.markedNode = this.builder.getCurrentParentNode();
        }
        this.mark = -1;
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        super.startElement(elemName, type, attributes, namespaces, location, properties);
        if (this.mark == 1) {
            this.markedNode = this.builder.getCurrentParentNode();
        }
        this.mark = -1;
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        super.characters(chars, locationId, properties);
        if (this.mark == 3) {
            this.markedNode = this.builder.getCurrentLeafNode();
        }
        this.mark = -1;
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        super.comment(chars, locationId, properties);
        if (this.mark == 8) {
            this.markedNode = this.builder.getCurrentLeafNode();
        }
        this.mark = -1;
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        super.processingInstruction(target, data, locationId, properties);
        if (this.mark == 7) {
            this.markedNode = this.builder.getCurrentLeafNode();
        }
        this.mark = -1;
    }

    @Override
    public NodeInfo getMarkedNode() {
        return this.markedNode;
    }
}

