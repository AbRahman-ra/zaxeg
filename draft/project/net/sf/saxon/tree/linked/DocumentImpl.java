/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.CopyOptions;
import net.sf.saxon.om.MutableDocumentInfo;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.linked.AttributeImpl;
import net.sf.saxon.tree.linked.ElementImpl;
import net.sf.saxon.tree.linked.LineNumberMap;
import net.sf.saxon.tree.linked.LinkedTreeBuilder;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.tree.linked.ParentNodeImpl;
import net.sf.saxon.tree.linked.SystemIdMap;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.Whitespace;
import net.sf.saxon.z.IntHashMap;

public final class DocumentImpl
extends ParentNodeImpl
implements TreeInfo,
MutableDocumentInfo {
    private ElementImpl documentElement;
    private HashMap<String, NodeInfo> idTable;
    private long documentNumber;
    private String baseURI;
    private HashMap<String, String[]> entityTable;
    private Set<ElementImpl> nilledElements;
    private Set<ElementImpl> topWithinEntityElements;
    private IntHashMap<List<NodeInfo>> elementList;
    private HashMap<String, Object> userData;
    private Configuration config;
    private LineNumberMap lineNumberMap;
    private SystemIdMap systemIdMap = new SystemIdMap();
    private boolean imaginary;
    private boolean mutable;
    private SpaceStrippingRule spaceStrippingRule = NoElementsSpaceStrippingRule.getInstance();

    public DocumentImpl() {
        this.setRawParent(null);
    }

    @Override
    public NodeInfo getRootNode() {
        return this;
    }

    public void setConfiguration(Configuration config) {
        this.config = config;
        this.documentNumber = config.getDocumentNumberAllocator().allocateDocumentNumber();
    }

    @Override
    public Configuration getConfiguration() {
        return this.config;
    }

    @Override
    public boolean isMutable() {
        return this.mutable;
    }

    public void setMutable(boolean mutable) {
        this.mutable = mutable;
    }

    @Override
    public NamePool getNamePool() {
        return this.config.getNamePool();
    }

    @Override
    public Builder newBuilder() {
        LinkedTreeBuilder builder = new LinkedTreeBuilder(this.config.makePipelineConfiguration());
        builder.setAllocateSequenceNumbers(false);
        return builder;
    }

    public void setImaginary(boolean imaginary) {
        this.imaginary = imaginary;
    }

    public boolean isImaginary() {
        return this.imaginary;
    }

    @Override
    public boolean isTyped() {
        return this.documentElement != null && this.documentElement.getSchemaType() != Untyped.getInstance();
    }

    @Override
    public long getDocumentNumber() {
        return this.documentNumber;
    }

    public void setDocumentElement(ElementImpl e) {
        this.documentElement = e;
    }

    public void graftLocationMap(DocumentImpl original) {
        this.systemIdMap = original.systemIdMap;
        this.lineNumberMap = original.lineNumberMap;
    }

    @Override
    public void setSystemId(String uri) {
        if (uri == null) {
            uri = "";
        }
        this.systemIdMap.setSystemId(this.getRawSequenceNumber(), uri);
    }

    @Override
    public String getSystemId() {
        return this.systemIdMap.getSystemId(this.getRawSequenceNumber());
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

    void setSystemId(int seq, String uri) {
        if (uri == null) {
            uri = "";
        }
        this.systemIdMap.setSystemId(seq, uri);
    }

    String getSystemId(int seq) {
        return this.systemIdMap.getSystemId(seq);
    }

    public void setLineNumbering() {
        this.lineNumberMap = new LineNumberMap();
        this.lineNumberMap.setLineAndColumn(this.getRawSequenceNumber(), 0, -1);
    }

    void setLineAndColumn(int sequence, int line, int column) {
        if (this.lineNumberMap != null && sequence >= 0) {
            this.lineNumberMap.setLineAndColumn(sequence, line, column);
        }
    }

    int getLineNumber(int sequence) {
        if (this.lineNumberMap != null && sequence >= 0) {
            return this.lineNumberMap.getLineNumber(sequence);
        }
        return -1;
    }

    int getColumnNumber(int sequence) {
        if (this.lineNumberMap != null && sequence >= 0) {
            return this.lineNumberMap.getColumnNumber(sequence);
        }
        return -1;
    }

    public void addNilledElement(ElementImpl element) {
        if (this.nilledElements == null) {
            this.nilledElements = new HashSet<ElementImpl>();
        }
        this.nilledElements.add(element);
    }

    boolean isNilledElement(ElementImpl element) {
        return this.nilledElements != null && this.nilledElements.contains(element);
    }

    public void markTopWithinEntity(ElementImpl element) {
        if (this.topWithinEntityElements == null) {
            this.topWithinEntityElements = new HashSet<ElementImpl>();
        }
        this.topWithinEntityElements.add(element);
    }

    public boolean isTopWithinEntity(ElementImpl element) {
        return this.topWithinEntityElements != null && this.topWithinEntityElements.contains(element);
    }

    @Override
    public int getLineNumber() {
        return 0;
    }

    @Override
    public final int getNodeKind() {
        return 9;
    }

    @Override
    public final NodeImpl getNextSibling() {
        return null;
    }

    @Override
    public final NodeImpl getPreviousSibling() {
        return null;
    }

    public ElementImpl getDocumentElement() {
        return this.documentElement;
    }

    @Override
    public NodeInfo getRoot() {
        return this;
    }

    @Override
    public DocumentImpl getPhysicalRoot() {
        return this;
    }

    @Override
    public void generateId(FastStringBuffer buffer) {
        buffer.cat('d');
        buffer.append(Long.toString(this.documentNumber));
    }

    AxisIterator getAllElements(int fingerprint) {
        IntHashMap<List<NodeInfo>> eList;
        List<NodeInfo> list;
        if (this.elementList == null) {
            this.elementList = new IntHashMap(500);
        }
        if ((list = (eList = this.elementList).get(fingerprint)) == null) {
            list = new ArrayList<NodeInfo>(500);
            for (NodeImpl next = this.getNextInDocument(this); next != null; next = next.getNextInDocument(this)) {
                if (next.getNodeKind() != 1 || next.getFingerprint() != fingerprint) continue;
                list.add(next);
            }
            eList.put(fingerprint, list);
        }
        return new ListIterator.OfNodes(list);
    }

    public void deIndex(NodeImpl node) {
        if (node instanceof ElementImpl) {
            IntHashMap<List<NodeInfo>> eList = this.elementList;
            if (eList != null) {
                List<NodeInfo> list = eList.get(node.getFingerprint());
                if (list == null) {
                    return;
                }
                list.remove(node);
            }
            if (node.isId()) {
                this.deregisterID(node.getStringValue());
            }
        } else if (node instanceof AttributeImpl && node.isId()) {
            this.deregisterID(node.getStringValue());
        }
    }

    private void indexIDs() {
        NodeImpl curr;
        if (this.idTable != null) {
            return;
        }
        this.idTable = new HashMap(256);
        DocumentImpl root = curr = this;
        while (curr != null) {
            if (curr.getNodeKind() == 1) {
                ElementImpl e = (ElementImpl)curr;
                if (e.isId()) {
                    this.registerID(e, Whitespace.trim(e.getStringValueCS()));
                }
                AttributeMap atts = e.attributes();
                for (AttributeInfo att : atts) {
                    if (!att.isId() || !NameChecker.isValidNCName(Whitespace.trim(att.getValue()))) continue;
                    this.registerID(e, Whitespace.trim(att.getValue()));
                }
            }
            curr = curr.getNextInDocument(root);
        }
    }

    protected void registerID(NodeInfo e, String id) {
        if (this.idTable == null) {
            this.idTable = new HashMap(256);
        }
        HashMap<String, NodeInfo> table = this.idTable;
        table.putIfAbsent(id, e);
    }

    @Override
    public NodeInfo selectID(String id, boolean getParent) {
        if (this.idTable == null) {
            this.indexIDs();
        }
        assert (this.idTable != null);
        NodeInfo node = this.idTable.get(id);
        if (node != null && getParent && node.isId() && node.getStringValue().equals(id)) {
            node = node.getParent();
        }
        return node;
    }

    protected void deregisterID(String id) {
        id = Whitespace.trim(id);
        if (this.idTable != null) {
            this.idTable.remove(id);
        }
    }

    public void setUnparsedEntity(String name, String uri, String publicId) {
        if (this.entityTable == null) {
            this.entityTable = new HashMap(10);
        }
        String[] ids = new String[]{uri, publicId};
        this.entityTable.put(name, ids);
    }

    @Override
    public Iterator<String> getUnparsedEntityNames() {
        if (this.entityTable == null) {
            List ls = Collections.emptyList();
            return ls.iterator();
        }
        return this.entityTable.keySet().iterator();
    }

    @Override
    public String[] getUnparsedEntity(String name) {
        if (this.entityTable == null) {
            return null;
        }
        return this.entityTable.get(name);
    }

    @Override
    public SchemaType getSchemaType() {
        if (this.documentElement == null || this.documentElement.getSchemaType() == Untyped.getInstance()) {
            return Untyped.getInstance();
        }
        return AnyType.getInstance();
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        out.startDocument(CopyOptions.getStartDocumentProperties(copyOptions));
        Iterator<String> names = this.getUnparsedEntityNames();
        while (names.hasNext()) {
            String name = names.next();
            String[] details = this.getUnparsedEntity(name);
            assert (details != null);
            out.setUnparsedEntity(name, details[0], details[1]);
        }
        for (NodeImpl next = this.getFirstChild(); next != null; next = next.getNextSibling()) {
            next.copy(out, copyOptions, locationId);
        }
        out.endDocument();
    }

    @Override
    public void replaceStringValue(CharSequence stringValue) {
        throw new UnsupportedOperationException("Cannot replace the value of a document node");
    }

    @Override
    public void resetIndexes() {
        this.idTable = null;
        this.elementList = null;
    }

    @Override
    public void setSpaceStrippingRule(SpaceStrippingRule rule) {
        this.spaceStrippingRule = rule;
    }

    @Override
    public SpaceStrippingRule getSpaceStrippingRule() {
        return this.spaceStrippingRule;
    }

    @Override
    public void setUserData(String key, Object value) {
        if (this.userData == null) {
            this.userData = new HashMap(4);
        }
        if (value == null) {
            this.userData.remove(key);
        } else {
            this.userData.put(key, value);
        }
    }

    @Override
    public Object getUserData(String key) {
        if (this.userData == null) {
            return null;
        }
        return this.userData.get(key);
    }
}

