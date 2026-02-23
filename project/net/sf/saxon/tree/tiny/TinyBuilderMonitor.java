/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import net.sf.saxon.event.BuilderMonitor;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.type.SchemaType;

public class TinyBuilderMonitor
extends BuilderMonitor {
    private TinyBuilder builder;
    private int mark = -1;
    private int markedNodeNr = -1;
    private int markedAttribute = -1;
    private int markedNamespace = -1;

    public TinyBuilderMonitor(TinyBuilder builder) {
        super(builder);
        this.builder = builder;
    }

    @Override
    public void markNextNode(int nodeKind) {
        this.mark = nodeKind;
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        if (this.mark == 9) {
            this.markedNodeNr = this.builder.getTree().getNumberOfNodes();
        }
        this.mark = -1;
        super.startDocument(properties);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        if (this.mark == 1) {
            this.markedNodeNr = this.builder.getTree().getNumberOfNodes();
        }
        this.mark = -1;
        super.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.mark == 3) {
            this.markedNodeNr = this.builder.getTree().getNumberOfNodes();
        }
        this.mark = -1;
        super.characters(chars, locationId, properties);
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.mark == 8) {
            this.markedNodeNr = this.builder.getTree().getNumberOfNodes();
        }
        this.mark = -1;
        super.comment(chars, locationId, properties);
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        if (this.mark == 7) {
            this.markedNodeNr = this.builder.getTree().getNumberOfNodes();
        }
        this.mark = -1;
        super.processingInstruction(target, data, locationId, properties);
    }

    @Override
    public NodeInfo getMarkedNode() {
        if (this.markedNodeNr != -1) {
            return this.builder.getTree().getNode(this.markedNodeNr);
        }
        if (this.markedAttribute != -1) {
            return this.builder.getTree().getAttributeNode(this.markedAttribute);
        }
        return null;
    }
}

