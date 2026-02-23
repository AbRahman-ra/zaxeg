/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.sapling;

import java.util.Objects;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.ma.trie.ImmutableHashTrieMap;
import net.sf.saxon.ma.trie.ImmutableList;
import net.sf.saxon.ma.trie.ImmutableMap;
import net.sf.saxon.ma.trie.Tuple2;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.sapling.SaplingNode;
import net.sf.saxon.sapling.SaplingText;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Untyped;

public class SaplingElement
extends SaplingNode {
    private StructuredQName nodeName;
    private ImmutableList<SaplingNode> reversedChildren = ImmutableList.empty();
    private ImmutableMap<StructuredQName, String> attributes = ImmutableHashTrieMap.empty();
    private NamespaceMap namespaces = NamespaceMap.emptyMap();

    public SaplingElement(String name) {
        Objects.requireNonNull(name);
        this.nodeName = StructuredQName.fromEQName(name);
    }

    public SaplingElement(QName name) {
        Objects.requireNonNull(name);
        this.nodeName = name.getStructuredQName();
        if (!this.nodeName.getPrefix().isEmpty() && this.nodeName.getURI().isEmpty()) {
            throw new IllegalArgumentException("No namespace URI for prefixed element name: " + name);
        }
        this.namespaces = NamespaceMap.of(this.nodeName.getPrefix(), this.nodeName.getURI());
    }

    private SaplingElement(StructuredQName name) {
        this.nodeName = name;
    }

    @Override
    public int getNodeKind() {
        return 1;
    }

    private SaplingElement copy() {
        SaplingElement e2 = new SaplingElement(this.nodeName);
        e2.reversedChildren = this.reversedChildren;
        e2.attributes = this.attributes;
        e2.namespaces = this.namespaces;
        return e2;
    }

    public SaplingElement withChild(SaplingNode ... children) {
        SaplingElement e2 = this.copy();
        for (SaplingNode child : children) {
            switch (child.getNodeKind()) {
                case 9: {
                    throw new IllegalArgumentException("Cannot add document node as a child of an element node");
                }
                case 1: 
                case 3: 
                case 7: 
                case 8: {
                    e2.reversedChildren = e2.reversedChildren.prepend(child);
                }
            }
        }
        return e2;
    }

    public SaplingElement withText(String value) {
        return this.withChild(new SaplingText(value));
    }

    private SaplingElement withAttribute(StructuredQName name, String value) {
        SaplingElement e2 = this.copy();
        e2.attributes = e2.attributes.put(name, value);
        return e2;
    }

    public SaplingElement withAttr(String name, String value) {
        return this.withAttribute(new StructuredQName("", "", name), value);
    }

    public SaplingElement withAttr(QName name, String value) {
        StructuredQName attName = name.getStructuredQName();
        if (attName.getPrefix().isEmpty() && !attName.getURI().isEmpty()) {
            throw new IllegalArgumentException("An attribute whose name is in a namespace must have a prefix");
        }
        this.withNamespace(attName.getPrefix(), attName.getURI());
        return this.withAttribute(attName, value);
    }

    public SaplingElement withNamespace(String prefix, String uri) {
        if (uri.isEmpty()) {
            if (prefix.isEmpty()) {
                return this;
            }
            throw new IllegalArgumentException("Cannot bind non-empty prefix to empty URI");
        }
        String existingURI = this.namespaces.getURI(prefix);
        if (existingURI != null) {
            if (existingURI.equals(uri)) {
                return this;
            }
            throw new IllegalStateException("Inconsistent namespace bindings for prefix '" + prefix + "'");
        }
        SaplingElement e2 = this.copy();
        e2.namespaces = this.namespaces.put(prefix, uri);
        return e2;
    }

    @Override
    protected void sendTo(Receiver receiver) throws XPathException {
        Configuration config = receiver.getPipelineConfiguration().getConfiguration();
        NamePool namePool = config.getNamePool();
        NamespaceMap ns = this.namespaces;
        if (!this.nodeName.getURI().isEmpty()) {
            ns = ns.put(this.nodeName.getPrefix(), this.nodeName.getURI());
        }
        AttributeMap atts = EmptyAttributeMap.getInstance();
        for (Tuple2<StructuredQName, String> tuple2 : this.attributes) {
            atts = atts.put(new AttributeInfo(new FingerprintedQName((StructuredQName)tuple2._1, namePool), BuiltInAtomicType.UNTYPED_ATOMIC, (String)tuple2._2, Loc.NONE, 0));
            if (((StructuredQName)tuple2._1).getURI().isEmpty()) continue;
            ns = ns.put(((StructuredQName)tuple2._1).getPrefix(), ((StructuredQName)tuple2._1).getURI());
        }
        receiver.startElement(new FingerprintedQName(this.nodeName, namePool), Untyped.getInstance(), atts, ns, Loc.NONE, 0);
        ImmutableList<SaplingNode> children = this.reversedChildren.reverse();
        for (SaplingNode node : children) {
            node.sendTo(receiver);
        }
        receiver.endElement();
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
}

