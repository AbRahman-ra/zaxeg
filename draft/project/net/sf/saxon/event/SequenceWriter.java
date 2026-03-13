/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.Builder;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.event.SequenceReceiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.LinkedTreeBuilder;
import net.sf.saxon.tree.util.Orphan;
import net.sf.saxon.type.SchemaType;

public abstract class SequenceWriter
extends SequenceReceiver {
    private TreeModel treeModel = null;
    private Builder builder = null;
    private int level = 0;

    public SequenceWriter(PipelineConfiguration pipe) {
        super(pipe);
    }

    public abstract void write(Item var1) throws XPathException;

    @Override
    public void startDocument(int properties) throws XPathException {
        if (this.builder == null) {
            this.createTree(ReceiverOption.contains(properties, 32768));
        }
        if (this.level++ == 0) {
            this.builder.startDocument(properties);
        }
    }

    @Override
    public void setUnparsedEntity(String name, String systemID, String publicID) throws XPathException {
        if (this.builder != null) {
            this.builder.setUnparsedEntity(name, systemID, publicID);
        }
    }

    private void createTree(boolean mutable) throws XPathException {
        PipelineConfiguration pipe = this.getPipelineConfiguration();
        if (this.treeModel != null) {
            this.builder = this.treeModel.makeBuilder(pipe);
        } else if (pipe.getController() != null) {
            TreeModel model;
            this.builder = mutable ? ((model = pipe.getController().getModel()).isMutable() ? pipe.getController().makeBuilder() : new LinkedTreeBuilder(pipe)) : pipe.getController().makeBuilder();
        } else {
            TreeModel model = this.getConfiguration().getParseOptions().getModel();
            this.builder = model.makeBuilder(pipe);
        }
        this.builder.setPipelineConfiguration(pipe);
        this.builder.setSystemId(this.systemId);
        this.builder.setBaseURI(this.systemId);
        this.builder.setTiming(false);
        this.builder.setUseEventLocation(false);
        this.builder.open();
    }

    public TreeModel getTreeModel() {
        return this.treeModel;
    }

    public void setTreeModel(TreeModel treeModel) {
        this.treeModel = treeModel;
    }

    @Override
    public void endDocument() throws XPathException {
        if (--this.level == 0) {
            this.builder.endDocument();
            NodeInfo doc = this.builder.getCurrentRoot();
            this.append(doc, Loc.NONE, 524288);
            this.builder = null;
            this.systemId = null;
        }
        this.previousAtomic = false;
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        if (this.builder == null) {
            this.createTree(ReceiverOption.contains(properties, 32768));
        }
        this.builder.startElement(elemName, type, attributes, namespaces, location, properties);
        ++this.level;
        this.previousAtomic = false;
    }

    @Override
    public void endElement() throws XPathException {
        this.builder.endElement();
        if (--this.level == 0) {
            this.builder.close();
            NodeInfo element = this.builder.getCurrentRoot();
            this.append(element, Loc.NONE, 524288);
            this.builder = null;
            this.systemId = null;
        }
        this.previousAtomic = false;
    }

    @Override
    public void characters(CharSequence s, Location locationId, int properties) throws XPathException {
        if (this.level == 0) {
            Orphan o = new Orphan(this.getConfiguration());
            o.setNodeKind((short)3);
            o.setStringValue(s.toString());
            this.write(o);
        } else if (s.length() > 0) {
            this.builder.characters(s, locationId, properties);
        }
        this.previousAtomic = false;
    }

    @Override
    public void comment(CharSequence comment, Location locationId, int properties) throws XPathException {
        if (this.level == 0) {
            Orphan o = new Orphan(this.getConfiguration());
            o.setNodeKind((short)8);
            o.setStringValue(comment);
            this.write(o);
        } else {
            this.builder.comment(comment, locationId, properties);
        }
        this.previousAtomic = false;
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        if (this.level == 0) {
            Orphan o = new Orphan(this.getConfiguration());
            o.setNodeName(new NoNamespaceName(target));
            o.setNodeKind((short)7);
            o.setStringValue(data);
            this.write(o);
        } else {
            this.builder.processingInstruction(target, data, locationId, properties);
        }
        this.previousAtomic = false;
    }

    @Override
    public void close() throws XPathException {
        this.previousAtomic = false;
        if (this.builder != null) {
            this.builder.close();
        }
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        if (item != null) {
            if (this.level == 0) {
                this.write(item);
                this.previousAtomic = false;
            } else {
                this.decompose(item, locationId, copyNamespaces);
            }
        }
    }

    @Override
    public boolean usesTypeAnnotations() {
        return this.builder == null || this.builder.usesTypeAnnotations();
    }
}

