/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.CopyOptions;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.tiny.TinyParentNodeImpl;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.z.IntHashMap;

public final class TinyDocumentImpl
extends TinyParentNodeImpl {
    private IntHashMap<List<NodeInfo>> elementList;
    private String baseURI;

    public TinyDocumentImpl(TinyTree tree) {
        this.tree = tree;
        this.nodeNr = 0;
    }

    @Override
    public TinyTree getTree() {
        return this.tree;
    }

    public NodeInfo getRootNode() {
        return this;
    }

    @Override
    public Configuration getConfiguration() {
        return this.tree.getConfiguration();
    }

    @Override
    public void setSystemId(String uri) {
        this.tree.setSystemId(this.nodeNr, uri);
    }

    @Override
    public String getSystemId() {
        return this.tree.getSystemId(this.nodeNr);
    }

    public void setBaseURI(String uri) {
        this.baseURI = uri;
    }

    @Override
    public String getBaseURI() {
        if (this.baseURI != null) {
            return this.baseURI;
        }
        return this.getSystemId();
    }

    @Override
    public int getLineNumber() {
        return 0;
    }

    public boolean isTyped() {
        return this.tree.getTypeArray() != null;
    }

    @Override
    public final int getNodeKind() {
        return 9;
    }

    @Override
    public TinyNodeImpl getParent() {
        return null;
    }

    @Override
    public NodeInfo getRoot() {
        return this;
    }

    @Override
    public void generateId(FastStringBuffer buffer) {
        buffer.cat('d');
        buffer.append(Long.toString(this.getTreeInfo().getDocumentNumber()));
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        return new UntypedAtomicValue(this.getStringValueCS());
    }

    AxisIterator getAllElements(int fingerprint) {
        List<NodeInfo> list;
        if (this.elementList == null) {
            this.elementList = new IntHashMap(20);
        }
        if ((list = this.elementList.get(fingerprint)) == null) {
            list = this.makeElementList(fingerprint);
            this.elementList.put(fingerprint, list);
        }
        return new ListIterator.OfNodes(list);
    }

    List<NodeInfo> makeElementList(int fingerprint) {
        int size = this.tree.getNumberOfNodes() / 20;
        if (size > 100) {
            size = 100;
        }
        if (size < 20) {
            size = 20;
        }
        ArrayList<NodeInfo> list = new ArrayList<NodeInfo>(size);
        int i = this.nodeNr + 1;
        try {
            while (this.tree.depth[i] != 0) {
                byte kind = this.tree.nodeKind[i];
                if ((kind & 0xF) == 1 && (this.tree.nameCode[i] & 0xFFFFF) == fingerprint) {
                    list.add(this.tree.getNode(i));
                }
                ++i;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return list;
        }
        list.trimToSize();
        return list;
    }

    @Override
    public SchemaType getSchemaType() {
        AxisIterator children = this.iterateAxis(3, NodeKindTest.ELEMENT);
        NodeInfo node = children.next();
        if (node == null || node.getSchemaType() == Untyped.getInstance()) {
            return Untyped.getInstance();
        }
        return AnyType.getInstance();
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        out.startDocument(CopyOptions.getStartDocumentProperties(copyOptions));
        if (this.tree.entityTable != null) {
            for (Map.Entry entry : this.tree.entityTable.entrySet()) {
                String name = (String)entry.getKey();
                String[] details = (String[])entry.getValue();
                String systemId = details[0];
                String publicId = details[1];
                out.setUnparsedEntity(name, systemId, publicId);
            }
        }
        for (NodeInfo nodeInfo : this.children()) {
            nodeInfo.copy(out, copyOptions, locationId);
        }
        out.endDocument();
    }

    public void showSize() {
        this.tree.showSize();
    }

    @Override
    public int hashCode() {
        return (int)this.tree.getDocumentNumber();
    }
}

