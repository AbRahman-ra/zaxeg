/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.sapling;

import javax.xml.transform.Source;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.ma.trie.ImmutableList;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.sapling.SaplingNode;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;

public class SaplingDocument
extends SaplingNode
implements Source {
    private String baseUri;
    private ImmutableList<SaplingNode> reversedChildren = ImmutableList.empty();

    public SaplingDocument() {
    }

    public SaplingDocument(String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public void setSystemId(String systemId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSystemId() {
        return this.baseUri;
    }

    @Override
    public int getNodeKind() {
        return 9;
    }

    private SaplingDocument copy() {
        SaplingDocument d2 = new SaplingDocument(this.baseUri);
        d2.reversedChildren = this.reversedChildren;
        return d2;
    }

    public SaplingDocument withChild(SaplingNode ... children) {
        SaplingDocument e2 = this.copy();
        for (SaplingNode node : children) {
            switch (node.getNodeKind()) {
                case 9: {
                    throw new IllegalArgumentException("Cannot add document child to a document node");
                }
                case 1: 
                case 3: 
                case 7: 
                case 8: {
                    e2.reversedChildren = e2.reversedChildren.prepend(node);
                }
            }
        }
        return e2;
    }

    @Override
    public void sendTo(Receiver receiver) throws XPathException {
        receiver = new NamespaceReducer(receiver);
        receiver.open();
        receiver.setSystemId(this.baseUri);
        receiver.startDocument(0);
        ImmutableList<SaplingNode> children = this.reversedChildren.reverse();
        for (SaplingNode node : children) {
            node.sendTo(receiver);
        }
        receiver.endDocument();
        receiver.close();
    }

    public NodeInfo toNodeInfo(Configuration config) throws XPathException {
        PipelineConfiguration pipe = config.makePipelineConfiguration();
        TreeModel treeModel = config.getParseOptions().getModel();
        Builder builder = treeModel.makeBuilder(pipe);
        builder.open();
        this.sendTo(builder);
        builder.close();
        return builder.getCurrentRoot();
    }

    public XdmNode toXdmNode(Processor processor) throws SaxonApiException {
        try {
            return (XdmNode)XdmValue.wrap(this.toNodeInfo(processor.getUnderlyingConfiguration()));
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public void serialize(Serializer serializer) throws SaxonApiException {
        Processor proc = serializer.getProcessor();
        this.send(proc, serializer);
    }

    public void send(Processor processor, Destination destination) throws SaxonApiException {
        try {
            PipelineConfiguration pipe = processor.getUnderlyingConfiguration().makePipelineConfiguration();
            this.sendTo(destination.getReceiver(pipe, new SerializationProperties()));
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }
}

