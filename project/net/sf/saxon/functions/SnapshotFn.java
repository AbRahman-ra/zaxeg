/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.event.BuilderMonitor;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.LazySequence;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.tree.wrapper.SnapshotNode;
import net.sf.saxon.tree.wrapper.VirtualCopy;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;

public class SnapshotFn
extends SystemFunction {
    @Override
    public int getCardinality(Expression[] arguments) {
        return arguments[0].getCardinality();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        Sequence in = arguments.length == 0 ? context.getContextItem() : arguments[0];
        SequenceIterator iter = SnapshotFn.snapshotSequence(in.iterate(), context);
        return new LazySequence(iter);
    }

    public static SequenceIterator snapshotSequence(SequenceIterator nodes, XPathContext context) {
        return new ItemMappingIterator(nodes, SnapshotFn.getMappingFunction());
    }

    public static ItemMappingFunction getMappingFunction() {
        return SnapshotFn::snapshotSingle;
    }

    public static Item snapshotSingle(Item origin) {
        if (origin instanceof NodeInfo) {
            if (((NodeInfo)origin).getParent() == null) {
                VirtualCopy vc = VirtualCopy.makeVirtualCopy((NodeInfo)origin);
                vc.getTreeInfo().setCopyAccumulators(true);
                return vc;
            }
            return SnapshotNode.makeSnapshot((NodeInfo)origin);
        }
        return origin;
    }

    public static List<NodeInfo> makeAncestorList(NodeInfo origin) {
        ArrayList<NodeInfo> ancestors = new ArrayList<NodeInfo>(20);
        origin.iterateAxis(0).forEachNode(ancestors::add);
        return ancestors;
    }

    public static BuilderMonitor openAncestors(NodeInfo origin, List<NodeInfo> ancestors, XPathContext context) throws XPathException {
        NodeInfo root = origin.getRoot();
        TinyBuilder builder = new TinyBuilder(context.getController().makePipelineConfiguration());
        builder.setStatistics(context.getConfiguration().getTreeStatistics().TEMPORARY_TREE_STATISTICS);
        builder.setSystemId(root.getSystemId());
        builder.setTiming(false);
        BuilderMonitor bm = builder.getBuilderMonitor();
        bm.open();
        TreeInfo source = root.getTreeInfo();
        Iterator<String> unparsedEntities = source.getUnparsedEntityNames();
        while (unparsedEntities.hasNext()) {
            String name = unparsedEntities.next();
            String[] properties = source.getUnparsedEntity(name);
            builder.setUnparsedEntity(name, properties[0], properties[1]);
        }
        Enum ancestorType = context.getController().getExecutable().isSchemaAware() ? AnyType.getInstance() : Untyped.getInstance();
        block5: for (int i = ancestors.size() - 1; i >= 0; --i) {
            NodeInfo anc = ancestors.get(i);
            int kind = anc.getNodeKind();
            switch (kind) {
                case 1: {
                    bm.startElement(NameOfNode.makeName(anc), (SchemaType)((Object)ancestorType), anc.attributes(), anc.getAllNamespaces(), Loc.NONE, 0);
                    continue block5;
                }
                case 9: {
                    bm.startDocument(0);
                    continue block5;
                }
                default: {
                    throw new IllegalStateException("Unknown ancestor node kind " + anc.getNodeKind());
                }
            }
        }
        return bm;
    }

    public static void closeAncestors(List<NodeInfo> ancestors, Receiver bm) throws XPathException {
        block4: for (NodeInfo anc : ancestors) {
            switch (anc.getNodeKind()) {
                case 1: {
                    bm.endElement();
                    continue block4;
                }
                case 9: {
                    bm.endDocument();
                    continue block4;
                }
            }
            throw new IllegalStateException("Unknown ancestor node kind " + anc.getNodeKind());
        }
        bm.close();
    }

    @Override
    public String getStreamerName() {
        return "SnapshotFn";
    }
}

