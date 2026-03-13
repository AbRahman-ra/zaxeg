/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.net.URI;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceNormalizer;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.s9api.AbstractDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.serialize.SerializationProperties;

public class XdmDestination
extends AbstractDestination {
    TreeModel treeModel = TreeModel.TINY_TREE;
    Builder builder;

    public void setBaseURI(URI baseURI) {
        if (!baseURI.isAbsolute()) {
            throw new IllegalArgumentException("Supplied base URI must be absolute");
        }
        this.setDestinationBaseURI(baseURI);
    }

    public URI getBaseURI() {
        return this.getDestinationBaseURI();
    }

    public void setTreeModel(TreeModel model) {
        this.treeModel = model;
    }

    public TreeModel getTreeModel() {
        return this.treeModel;
    }

    @Override
    public Receiver getReceiver(PipelineConfiguration pipe, SerializationProperties params) {
        String systemId;
        TreeModel model = this.treeModel;
        if (model == null) {
            int m = pipe.getParseOptions().getTreeModel();
            if (m != -1) {
                model = TreeModel.getTreeModel(m);
            }
            if (model == null) {
                model = TreeModel.TINY_TREE;
            }
        }
        this.builder = model.makeBuilder(pipe);
        String string = systemId = this.getBaseURI() == null ? null : this.getBaseURI().toASCIIString();
        if (systemId != null) {
            this.builder.setUseEventLocation(false);
            this.builder.setBaseURI(systemId);
        }
        SequenceNormalizer sn = params.makeSequenceNormalizer(this.builder);
        sn.setSystemId(systemId);
        sn.onClose(this.helper.getListeners());
        return sn;
    }

    @Override
    public void close() {
    }

    public XdmNode getXdmNode() {
        if (this.builder == null) {
            throw new IllegalStateException("The document has not yet been built");
        }
        NodeInfo node = this.builder.getCurrentRoot();
        return node == null ? null : (XdmNode)XdmValue.wrap(node);
    }

    public void reset() {
        this.builder = null;
    }
}

