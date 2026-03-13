/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.GenericTreeInfo;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.PrefixPool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.SystemIdMap;
import net.sf.saxon.tree.tiny.AppendableCharSequence;
import net.sf.saxon.tree.tiny.CharSlice;
import net.sf.saxon.tree.tiny.LargeStringBuffer;
import net.sf.saxon.tree.tiny.NodeVectorTree;
import net.sf.saxon.tree.tiny.Statistics;
import net.sf.saxon.tree.tiny.TinyAttributeImpl;
import net.sf.saxon.tree.tiny.TinyCommentImpl;
import net.sf.saxon.tree.tiny.TinyDocumentImpl;
import net.sf.saxon.tree.tiny.TinyElementImpl;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.tiny.TinyParentNodeImpl;
import net.sf.saxon.tree.tiny.TinyProcInstImpl;
import net.sf.saxon.tree.tiny.TinyTextImpl;
import net.sf.saxon.tree.tiny.TinyTextualElement;
import net.sf.saxon.tree.tiny.WhitespaceTextImpl;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.BuiltInListType;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.value.Whitespace;
import net.sf.saxon.z.IntArraySet;
import net.sf.saxon.z.IntHashMap;
import net.sf.saxon.z.IntHashSet;
import net.sf.saxon.z.IntSet;
import net.sf.saxon.z.IntSingletonSet;

public final class TinyTree
extends GenericTreeInfo
implements NodeVectorTree {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    protected AppendableCharSequence charBuffer;
    protected FastStringBuffer commentBuffer = null;
    protected int numberOfNodes = 0;
    public byte[] nodeKind;
    protected short[] depth;
    protected int[] next;
    protected int[] alpha;
    protected int[] beta;
    protected int[] nameCode;
    protected int[] prior = null;
    protected SchemaType[] typeArray = null;
    protected AtomicSequence[] typedValueArray = null;
    protected IntSet idRefElements = null;
    protected IntSet idRefAttributes = null;
    protected IntSet nilledElements = null;
    protected IntSet defaultedAttributes = null;
    protected IntSet topWithinEntity = null;
    private boolean allowTypedValueCache = true;
    private Map<String, IntSet> localNameIndex = null;
    public static final int TYPECODE_IDREF = 0x20000000;
    protected int numberOfAttributes = 0;
    protected int[] attParent;
    protected int[] attCode;
    protected CharSequence[] attValue;
    protected AtomicSequence[] attTypedValue;
    protected SimpleType[] attType;
    protected int numberOfNamespaces = 0;
    protected NamespaceMap[] namespaceMaps;
    private int[] lineNumbers = null;
    private int[] columnNumbers = null;
    private SystemIdMap systemIdMap = null;
    protected boolean usesNamespaces = false;
    protected PrefixPool prefixPool = new PrefixPool();
    private HashMap<String, NodeInfo> idTable;
    protected HashMap<String, String[]> entityTable;
    private NodeInfo copiedFrom;
    protected IntHashMap<String> knownBaseUris;
    private String uniformBaseUri = null;

    public TinyTree(Configuration config, Statistics statistics) {
        super(config);
        int nodes = (int)statistics.getAverageNodes() + 1;
        int attributes = (int)statistics.getAverageAttributes() + 1;
        int namespaces = (int)statistics.getAverageNamespaces() + 1;
        int characters = (int)statistics.getAverageCharacters() + 1;
        this.nodeKind = new byte[nodes];
        this.depth = new short[nodes];
        this.next = new int[nodes];
        this.alpha = new int[nodes];
        this.beta = new int[nodes];
        this.nameCode = new int[nodes];
        this.numberOfAttributes = 0;
        this.attParent = new int[attributes];
        this.attCode = new int[attributes];
        this.attValue = new String[attributes];
        this.numberOfNamespaces = 0;
        this.namespaceMaps = new NamespaceMap[namespaces];
        this.charBuffer = characters > 65000 ? new LargeStringBuffer() : new FastStringBuffer(characters);
        this.setConfiguration(config);
    }

    @Override
    public void setConfiguration(Configuration config) {
        super.setConfiguration(config);
        this.allowTypedValueCache = config.isLicensedFeature(1) && config.getBooleanProperty(Feature.USE_TYPED_VALUE_CACHE);
    }

    private void ensureNodeCapacity(short kind, int needed) {
        if (this.nodeKind.length < this.numberOfNodes + needed) {
            int k = kind == 11 ? this.numberOfNodes + 1 : Math.max(this.numberOfNodes * 2, this.numberOfNodes + needed);
            this.nodeKind = Arrays.copyOf(this.nodeKind, k);
            this.next = Arrays.copyOf(this.next, k);
            this.depth = Arrays.copyOf(this.depth, k);
            this.alpha = Arrays.copyOf(this.alpha, k);
            this.beta = Arrays.copyOf(this.beta, k);
            this.nameCode = Arrays.copyOf(this.nameCode, k);
            if (this.typeArray != null) {
                this.typeArray = Arrays.copyOf(this.typeArray, k);
            }
            if (this.typedValueArray != null) {
                this.typedValueArray = Arrays.copyOf(this.typedValueArray, k);
            }
            if (this.lineNumbers != null) {
                this.lineNumbers = Arrays.copyOf(this.lineNumbers, k);
                this.columnNumbers = Arrays.copyOf(this.columnNumbers, k);
            }
        }
    }

    private void ensureAttributeCapacity(int needed) {
        if (this.attParent.length < this.numberOfAttributes + needed) {
            int k = Math.max(this.numberOfAttributes + needed, this.numberOfAttributes * 2);
            if (k == 0) {
                k = 10 + needed;
            }
            this.attParent = Arrays.copyOf(this.attParent, k);
            this.attCode = Arrays.copyOf(this.attCode, k);
            this.attValue = Arrays.copyOf(this.attValue, k);
            if (this.attType != null) {
                this.attType = Arrays.copyOf(this.attType, k);
            }
            if (this.attTypedValue != null) {
                this.attTypedValue = Arrays.copyOf(this.attTypedValue, k);
            }
        }
    }

    private void ensureNamespaceCapacity(int needed) {
        if (this.namespaceMaps.length < this.numberOfNamespaces + needed) {
            int k = Math.max(this.numberOfNamespaces * 2, this.numberOfNamespaces + needed);
            if (k == 0) {
                k = 10;
            }
            this.namespaceMaps = Arrays.copyOf(this.namespaceMaps, k);
        }
    }

    public PrefixPool getPrefixPool() {
        return this.prefixPool;
    }

    public void setCopiedFrom(NodeInfo copiedFrom) {
        this.copiedFrom = copiedFrom;
    }

    public NodeInfo getCopiedFrom() {
        return this.copiedFrom;
    }

    int addDocumentNode(TinyDocumentImpl doc) {
        this.setRootNode(doc);
        return this.addNode((short)9, 0, 0, 0, -1);
    }

    int addNode(short kind, int depth, int alpha, int beta, int nameCode) {
        this.ensureNodeCapacity(kind, 1);
        this.nodeKind[this.numberOfNodes] = (byte)kind;
        this.depth[this.numberOfNodes] = (short)depth;
        this.alpha[this.numberOfNodes] = alpha;
        this.beta[this.numberOfNodes] = beta;
        this.nameCode[this.numberOfNodes] = nameCode;
        this.next[this.numberOfNodes] = -1;
        if (this.typeArray != null) {
            this.typeArray[this.numberOfNodes] = Untyped.getInstance();
        }
        if (this.numberOfNodes == 0) {
            this.setDocumentNumber(this.getConfiguration().getDocumentNumberAllocator().allocateDocumentNumber());
        }
        return this.numberOfNodes++;
    }

    void appendChars(CharSequence chars) {
        if (this.charBuffer instanceof FastStringBuffer && this.charBuffer.length() > 65000) {
            LargeStringBuffer lsb = new LargeStringBuffer();
            this.charBuffer = lsb.cat(this.charBuffer);
        }
        this.charBuffer.cat(chars);
    }

    public int addTextNodeCopy(int depth, int existingNodeNr) {
        return this.addNode((short)3, depth, this.alpha[existingNodeNr], this.beta[existingNodeNr], -1);
    }

    void condense(Statistics statistics) {
        if (this.numberOfNodes * 3 < this.nodeKind.length || this.nodeKind.length - this.numberOfNodes > 20000) {
            this.nodeKind = Arrays.copyOf(this.nodeKind, this.numberOfNodes);
            this.next = Arrays.copyOf(this.next, this.numberOfNodes);
            this.depth = Arrays.copyOf(this.depth, this.numberOfNodes);
            this.alpha = Arrays.copyOf(this.alpha, this.numberOfNodes);
            this.beta = Arrays.copyOf(this.beta, this.numberOfNodes);
            this.nameCode = Arrays.copyOf(this.nameCode, this.numberOfNodes);
            if (this.typeArray != null) {
                this.typeArray = Arrays.copyOf(this.typeArray, this.numberOfNodes);
            }
            if (this.lineNumbers != null) {
                this.lineNumbers = Arrays.copyOf(this.lineNumbers, this.numberOfNodes);
                this.columnNumbers = Arrays.copyOf(this.columnNumbers, this.numberOfNodes);
            }
        }
        if (this.numberOfAttributes * 3 < this.attParent.length || this.attParent.length - this.numberOfAttributes > 1000) {
            int k = this.numberOfAttributes;
            if (k == 0) {
                this.attParent = IntArraySet.EMPTY_INT_ARRAY;
                this.attCode = IntArraySet.EMPTY_INT_ARRAY;
                this.attValue = EMPTY_STRING_ARRAY;
                this.attType = null;
            } else {
                this.attParent = Arrays.copyOf(this.attParent, this.numberOfAttributes);
                this.attCode = Arrays.copyOf(this.attCode, this.numberOfAttributes);
                this.attValue = Arrays.copyOf(this.attValue, this.numberOfAttributes);
            }
            if (this.attType != null) {
                this.attType = Arrays.copyOf(this.attType, this.numberOfAttributes);
            }
        }
        if (this.numberOfNamespaces * 3 < this.namespaceMaps.length) {
            this.namespaceMaps = Arrays.copyOf(this.namespaceMaps, this.numberOfNamespaces);
        }
        this.prefixPool.condense();
        statistics.updateStatistics(this.numberOfNodes, this.numberOfAttributes, this.numberOfNamespaces, this.charBuffer.length());
    }

    void setElementAnnotation(int nodeNr, SchemaType type) {
        if (!type.equals(Untyped.getInstance())) {
            if (this.typeArray == null) {
                this.typeArray = new SchemaType[this.nodeKind.length];
                Arrays.fill(this.typeArray, 0, this.nodeKind.length, Untyped.getInstance());
            }
            assert (this.typeArray != null);
            this.typeArray[nodeNr] = type;
        }
    }

    public int getTypeAnnotation(int nodeNr) {
        if (this.typeArray == null) {
            return 630;
        }
        return this.typeArray[nodeNr].getFingerprint();
    }

    public SchemaType getSchemaType(int nodeNr) {
        if (this.typeArray == null) {
            return Untyped.getInstance();
        }
        return this.typeArray[nodeNr];
    }

    public AtomicSequence getTypedValueOfElement(TinyElementImpl element) throws XPathException {
        int nodeNr = element.nodeNr;
        if (this.typedValueArray == null || this.typedValueArray[nodeNr] == null) {
            SchemaType stype = this.getSchemaType(nodeNr);
            int annotation = stype.getFingerprint();
            if (annotation == 630 || annotation == 631 || annotation == 572) {
                CharSequence stringValue = TinyParentNodeImpl.getStringValueCS(this, nodeNr);
                return new UntypedAtomicValue(stringValue);
            }
            if (annotation == 513) {
                CharSequence stringValue = TinyParentNodeImpl.getStringValueCS(this, nodeNr);
                return new StringValue(stringValue);
            }
            if (annotation == 529) {
                CharSequence stringValue = TinyParentNodeImpl.getStringValueCS(this, nodeNr);
                return new AnyURIValue(stringValue);
            }
            AtomicSequence value = stype.atomize(element);
            if (this.allowTypedValueCache) {
                if (this.typedValueArray == null) {
                    this.typedValueArray = new AtomicSequence[this.nodeKind.length];
                }
                this.typedValueArray[nodeNr] = value;
            }
            return value;
        }
        return this.typedValueArray[nodeNr];
    }

    public AtomicSequence getTypedValueOfElement(int nodeNr) throws XPathException {
        if (this.typedValueArray == null || this.typedValueArray[nodeNr] == null) {
            SchemaType stype = this.getSchemaType(nodeNr);
            int annotation = stype.getFingerprint();
            if (annotation == 631 || annotation == 630) {
                CharSequence stringValue = TinyParentNodeImpl.getStringValueCS(this, nodeNr);
                return new UntypedAtomicValue(stringValue);
            }
            if (annotation == 513) {
                CharSequence stringValue = TinyParentNodeImpl.getStringValueCS(this, nodeNr);
                return new StringValue(stringValue);
            }
            if (annotation == 529) {
                CharSequence stringValue = TinyParentNodeImpl.getStringValueCS(this, nodeNr);
                return new AnyURIValue(stringValue);
            }
            if (annotation == 560) {
                CharSequence stringValue = TinyParentNodeImpl.getStringValueCS(this, nodeNr);
                return new StringValue(stringValue, BuiltInAtomicType.ID);
            }
            TinyNodeImpl element = this.getNode(nodeNr);
            AtomicSequence value = stype.atomize(element);
            if (this.allowTypedValueCache) {
                if (this.typedValueArray == null) {
                    this.typedValueArray = new AtomicSequence[this.nodeKind.length];
                }
                this.typedValueArray[nodeNr] = value;
            }
            return value;
        }
        return this.typedValueArray[nodeNr];
    }

    public AtomicSequence getTypedValueOfAttribute(TinyAttributeImpl att, int nodeNr) throws XPathException {
        if (this.attType == null) {
            return new UntypedAtomicValue(this.attValue[nodeNr]);
        }
        if (this.attTypedValue == null || this.attTypedValue[nodeNr] == null) {
            SimpleType type = this.getAttributeType(nodeNr);
            if (type.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
                return new UntypedAtomicValue(this.attValue[nodeNr]);
            }
            if (type.equals(BuiltInAtomicType.STRING)) {
                return new StringValue(this.attValue[nodeNr]);
            }
            if (type.equals(BuiltInAtomicType.ANY_URI)) {
                return new AnyURIValue(this.attValue[nodeNr]);
            }
            if (att == null) {
                att = new TinyAttributeImpl(this, nodeNr);
            }
            AtomicSequence value = type.atomize(att);
            if (this.allowTypedValueCache) {
                if (this.attTypedValue == null) {
                    this.attTypedValue = new AtomicSequence[this.attParent.length];
                }
                this.attTypedValue[nodeNr] = value;
            }
            return value;
        }
        return this.attTypedValue[nodeNr];
    }

    @Override
    public int getNodeKind(int nodeNr) {
        int kind = this.nodeKind[nodeNr];
        return kind == 4 ? 3 : kind;
    }

    public int getNameCode(int nodeNr) {
        return this.nameCode[nodeNr];
    }

    @Override
    public int getFingerprint(int nodeNr) {
        int nc = this.nameCode[nodeNr];
        return nc == -1 ? -1 : nc & 0xFFFFF;
    }

    public String getPrefix(int nodeNr) {
        int code = this.nameCode[nodeNr] >> 20;
        if (code <= 0) {
            return code == 0 ? "" : null;
        }
        return this.prefixPool.getPrefix(code);
    }

    void ensurePriorIndex() {
        if (this.prior == null || this.prior.length < this.numberOfNodes) {
            this.makePriorIndex();
        }
    }

    private synchronized void makePriorIndex() {
        int[] p = new int[this.numberOfNodes];
        Arrays.fill(p, 0, this.numberOfNodes, -1);
        for (int i = 0; i < this.numberOfNodes; ++i) {
            int nextNode = this.next[i];
            if (nextNode <= i) continue;
            p[nextNode] = i;
        }
        this.prior = p;
    }

    void addAttribute(NodeInfo root, int parent, int nameCode, SimpleType type, CharSequence attValue, int properties) {
        this.ensureAttributeCapacity(1);
        this.attParent[this.numberOfAttributes] = parent;
        this.attCode[this.numberOfAttributes] = nameCode;
        this.attValue[this.numberOfAttributes] = attValue.toString();
        if (!type.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
            this.initializeAttributeTypeCodes();
        }
        if (this.attType != null) {
            this.attType[this.numberOfAttributes] = type;
        }
        if (this.alpha[parent] == -1) {
            this.alpha[parent] = this.numberOfAttributes;
        }
        if (root instanceof TinyDocumentImpl) {
            boolean isIDREF;
            block24: {
                boolean isID = false;
                try {
                    if (ReceiverOption.contains(properties, 2048)) {
                        isID = true;
                    } else if ((nameCode & 0xFFFFF) == 388) {
                        isID = true;
                    } else if (type.isIdType()) {
                        isID = true;
                    }
                } catch (MissingComponentException missingComponentException) {
                    // empty catch block
                }
                if (isID) {
                    String id = Whitespace.trim(attValue);
                    this.attValue[this.numberOfAttributes] = id;
                    if (NameChecker.isValidNCName(id)) {
                        TinyNodeImpl e = this.getNode(parent);
                        this.registerID(e, id);
                    } else if (this.attType != null) {
                        this.attType[this.numberOfAttributes] = BuiltInAtomicType.UNTYPED_ATOMIC;
                    }
                }
                isIDREF = false;
                try {
                    if (ReceiverOption.contains(properties, 4096)) {
                        isIDREF = true;
                        break block24;
                    }
                    if (type == BuiltInAtomicType.IDREF || type == BuiltInListType.IDREFS) {
                        isIDREF = true;
                        break block24;
                    }
                    if (!type.isIdRefType()) break block24;
                    try {
                        AtomicSequence as = type.getTypedValue(attValue, null, this.getConfiguration().getConversionRules());
                        for (AtomicValue v : as) {
                            if (!v.getItemType().isIdRefType()) continue;
                            isIDREF = true;
                        }
                    } catch (ValidationException validationException) {}
                } catch (MissingComponentException missingComponentException) {
                    // empty catch block
                }
            }
            if (isIDREF) {
                if (this.idRefAttributes == null) {
                    this.idRefAttributes = new IntHashSet();
                }
                this.idRefAttributes.add(this.numberOfAttributes);
            }
        }
        ++this.numberOfAttributes;
    }

    private void initializeAttributeTypeCodes() {
        if (this.attType == null) {
            this.attType = new SimpleType[this.attParent.length];
            Arrays.fill(this.attType, 0, this.numberOfAttributes, BuiltInAtomicType.UNTYPED_ATOMIC);
        }
    }

    public void markDefaultedAttribute(int attNr) {
        if (this.defaultedAttributes == null) {
            this.defaultedAttributes = new IntHashSet();
        }
        this.defaultedAttributes.add(attNr);
    }

    public boolean isDefaultedAttribute(int attNr) {
        return this.defaultedAttributes != null && this.defaultedAttributes.contains(attNr);
    }

    public void indexIDElement(NodeInfo root, int nodeNr) {
        String id = Whitespace.trim(TinyParentNodeImpl.getStringValueCS(this, nodeNr));
        if (root.getNodeKind() == 9 && NameChecker.isValidNCName(id)) {
            TinyNodeImpl e = this.getNode(nodeNr);
            this.registerID(e, id);
        }
    }

    public boolean hasXmlSpacePreserveAttribute() {
        for (int i = 0; i < this.numberOfAttributes; ++i) {
            if ((this.attCode[i] & 0xFFFFF) != 386 || !"preserve".equals(this.attValue[i].toString())) continue;
            return true;
        }
        return false;
    }

    void addNamespaces(int parent, NamespaceMap nsMap) {
        this.usesNamespaces = true;
        for (int i = 0; i < this.numberOfNamespaces; ++i) {
            if (!this.namespaceMaps[i].equals(nsMap)) continue;
            this.beta[parent] = i;
            return;
        }
        this.ensureNamespaceCapacity(1);
        this.namespaceMaps[this.numberOfNamespaces] = nsMap;
        this.beta[parent] = this.numberOfNamespaces++;
    }

    @Override
    public final TinyNodeImpl getNode(int nr) {
        switch (this.nodeKind[nr]) {
            case 9: {
                return (TinyDocumentImpl)this.getRootNode();
            }
            case 1: {
                return new TinyElementImpl(this, nr);
            }
            case 17: {
                return new TinyTextualElement(this, nr);
            }
            case 3: {
                return new TinyTextImpl(this, nr);
            }
            case 4: {
                return new WhitespaceTextImpl(this, nr);
            }
            case 8: {
                return new TinyCommentImpl(this, nr);
            }
            case 7: {
                return new TinyProcInstImpl(this, nr);
            }
            case 12: {
                throw new IllegalArgumentException("Attempting to treat a parent pointer as a node");
            }
            case 11: {
                throw new IllegalArgumentException("Attempting to treat a stopper entry as a node");
            }
        }
        throw new IllegalStateException("Unknown node kind " + this.nodeKind[nr]);
    }

    AtomicValue getAtomizedValueOfUntypedNode(int nodeNr) {
        switch (this.nodeKind[nodeNr]) {
            case 1: 
            case 9: {
                int next;
                short level = this.depth[nodeNr];
                if (this.depth[next] <= level) {
                    return UntypedAtomicValue.ZERO_LENGTH_UNTYPED;
                }
                if (this.nodeKind[next] == 3 && this.depth[next + 1] <= level) {
                    int length = this.beta[next];
                    int start = this.alpha[next];
                    return new UntypedAtomicValue(this.charBuffer.subSequence(start, start + length));
                }
                if (this.nodeKind[next] == 4 && this.depth[next + 1] <= level) {
                    return new UntypedAtomicValue(WhitespaceTextImpl.getStringValueCS(this, next));
                }
                FastStringBuffer sb = null;
                for (next = nodeNr + 1; next < this.numberOfNodes && this.depth[next] > level; ++next) {
                    if (this.nodeKind[next] == 3) {
                        if (sb == null) {
                            sb = new FastStringBuffer(256);
                        }
                        sb.cat(TinyTextImpl.getStringValue(this, next));
                        continue;
                    }
                    if (this.nodeKind[next] != 4) continue;
                    if (sb == null) {
                        sb = new FastStringBuffer(256);
                    }
                    WhitespaceTextImpl.appendStringValue(this, next, sb);
                }
                if (sb == null) {
                    return UntypedAtomicValue.ZERO_LENGTH_UNTYPED;
                }
                return new UntypedAtomicValue(sb.condense());
            }
            case 3: {
                return new UntypedAtomicValue(TinyTextImpl.getStringValue(this, nodeNr));
            }
            case 4: {
                return new UntypedAtomicValue(WhitespaceTextImpl.getStringValueCS(this, nodeNr));
            }
            case 7: 
            case 8: {
                int start2 = this.alpha[nodeNr];
                int len2 = this.beta[nodeNr];
                if (len2 == 0) {
                    return UntypedAtomicValue.ZERO_LENGTH_UNTYPED;
                }
                char[] dest = new char[len2];
                assert (this.commentBuffer != null);
                this.commentBuffer.getChars(start2, start2 + len2, dest, 0);
                return new StringValue(new CharSlice(dest, 0, len2));
            }
        }
        throw new IllegalStateException("Unknown node kind");
    }

    TinyAttributeImpl getAttributeNode(int nr) {
        return new TinyAttributeImpl(this, nr);
    }

    int getAttributeAnnotation(int nr) {
        if (this.attType == null) {
            return 631;
        }
        return this.attType[nr].getFingerprint();
    }

    SimpleType getAttributeType(int nr) {
        if (this.attType == null) {
            return BuiltInAtomicType.UNTYPED_ATOMIC;
        }
        return this.attType[nr];
    }

    public boolean isIdAttribute(int nr) {
        try {
            return this.attType != null && this.getAttributeType(nr).isIdType();
        } catch (MissingComponentException e) {
            return false;
        }
    }

    public boolean isIdrefAttribute(int nr) {
        return this.idRefAttributes != null && this.idRefAttributes.contains(nr);
    }

    public boolean isIdElement(int nr) {
        try {
            return this.getSchemaType(nr).isIdType() && this.getTypedValueOfElement(nr).getLength() == 1;
        } catch (XPathException e) {
            return false;
        }
    }

    public boolean isIdrefElement(int nr) {
        SchemaType type = this.getSchemaType(nr);
        try {
            if (type.isIdRefType()) {
                if (type == BuiltInAtomicType.IDREF || type == BuiltInListType.IDREFS) {
                    return true;
                }
                try {
                    for (AtomicValue av : this.getTypedValueOfElement(nr)) {
                        if (!av.getItemType().isIdRefType()) continue;
                        return true;
                    }
                } catch (XPathException xPathException) {}
            }
        } catch (MissingComponentException e) {
            return false;
        }
        return false;
    }

    void setSystemId(int seq, String uri) {
        if (uri == null) {
            uri = "";
        }
        if (this.systemIdMap == null) {
            this.systemIdMap = new SystemIdMap();
        }
        this.systemIdMap.setSystemId(seq, uri);
    }

    void setUniformBaseUri(String base) {
        this.uniformBaseUri = base;
    }

    String getUniformBaseUri() {
        return this.uniformBaseUri;
    }

    public String getSystemId(int seq) {
        if (this.systemIdMap == null) {
            return null;
        }
        return this.systemIdMap.getSystemId(seq);
    }

    @Override
    public NodeInfo getRootNode() {
        if (this.getNodeKind(0) == 9) {
            if (this.root != null) {
                return this.root;
            }
            this.root = new TinyDocumentImpl(this);
            return this.root;
        }
        return this.getNode(0);
    }

    public void setLineNumbering() {
        this.lineNumbers = new int[this.nodeKind.length];
        Arrays.fill(this.lineNumbers, -1);
        this.columnNumbers = new int[this.nodeKind.length];
        Arrays.fill(this.columnNumbers, -1);
    }

    void setLineNumber(int sequence, int line, int column) {
        if (this.lineNumbers != null) {
            assert (this.columnNumbers != null);
            this.lineNumbers[sequence] = line;
            this.columnNumbers[sequence] = column;
        }
    }

    public int getLineNumber(int sequence) {
        if (this.lineNumbers != null) {
            for (int i = sequence; i >= 0; --i) {
                int c = this.lineNumbers[i];
                if (c <= 0) continue;
                return c;
            }
        }
        return -1;
    }

    public int getColumnNumber(int sequence) {
        if (this.columnNumbers != null) {
            for (int i = sequence; i >= 0; --i) {
                int c = this.columnNumbers[i];
                if (c <= 0) continue;
                return c;
            }
        }
        return -1;
    }

    public void setNilled(int nodeNr) {
        if (this.nilledElements == null) {
            this.nilledElements = new IntHashSet();
        }
        this.nilledElements.add(nodeNr);
    }

    public boolean isNilled(int nodeNr) {
        return this.nilledElements != null && this.nilledElements.contains(nodeNr);
    }

    void registerID(NodeInfo e, String id) {
        if (this.idTable == null) {
            this.idTable = new HashMap(256);
        }
        this.idTable.putIfAbsent(id, e);
    }

    @Override
    public NodeInfo selectID(String id, boolean getParent) {
        if (this.idTable == null) {
            return null;
        }
        NodeInfo node = this.idTable.get(id);
        if (node != null && getParent && node.isId() && node.getStringValue().equals(id)) {
            node = node.getParent();
        }
        return node;
    }

    void setUnparsedEntity(String name, String uri, String publicId) {
        if (this.entityTable == null) {
            this.entityTable = new HashMap(20);
        }
        String[] ids = new String[]{uri, publicId};
        this.entityTable.put(name, ids);
    }

    @Override
    public Iterator<String> getUnparsedEntityNames() {
        if (this.entityTable == null) {
            List emptyList = Collections.emptyList();
            return emptyList.iterator();
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

    public NamePool getNamePool() {
        return this.getConfiguration().getNamePool();
    }

    public void markTopWithinEntity(int nodeNr) {
        if (this.topWithinEntity == null) {
            this.topWithinEntity = new IntHashSet();
        }
        this.topWithinEntity.add(nodeNr);
    }

    public boolean isTopWithinEntity(int nodeNr) {
        return this.topWithinEntity != null && this.topWithinEntity.contains(nodeNr);
    }

    public void diagnosticDump() {
        int i;
        NamePool pool = this.getNamePool();
        System.err.println("    node    kind   depth    next   alpha    beta    name    type");
        for (i = 0; i < this.numberOfNodes; ++i) {
            String eqName = "";
            if (this.nameCode[i] != -1) {
                try {
                    eqName = pool.getEQName(this.nameCode[i]);
                } catch (Exception err) {
                    eqName = "#" + this.nameCode[1];
                }
            }
            System.err.println(TinyTree.n8(i) + TinyTree.n8(this.nodeKind[i]) + TinyTree.n8(this.depth[i]) + TinyTree.n8(this.next[i]) + TinyTree.n8(this.alpha[i]) + TinyTree.n8(this.beta[i]) + TinyTree.n8(this.nameCode[i]) + TinyTree.n8(this.getTypeAnnotation(i)) + " " + eqName);
        }
        System.err.println("    attr  parent    name    value");
        for (i = 0; i < this.numberOfAttributes; ++i) {
            System.err.println(TinyTree.n8(i) + TinyTree.n8(this.attParent[i]) + TinyTree.n8(this.attCode[i]) + "    " + this.attValue[i]);
        }
        System.err.println("      ns  parent  prefix     uri");
        for (i = 0; i < this.numberOfNamespaces; ++i) {
            System.err.println(TinyTree.n8(i) + "  " + this.namespaceMaps[i]);
        }
    }

    public static synchronized void diagnosticDump(NodeInfo node) {
        if (node instanceof TinyNodeImpl) {
            TinyTree tree = ((TinyNodeImpl)node).tree;
            System.err.println("Tree containing node " + ((TinyNodeImpl)node).nodeNr);
            tree.diagnosticDump();
        } else {
            System.err.println("Node is not in a TinyTree");
        }
    }

    private static String n8(int val) {
        String s = "        " + val;
        return s.substring(s.length() - 8);
    }

    public void showSize() {
        System.err.println("Tree size: " + this.numberOfNodes + " nodes, " + this.charBuffer.length() + " characters, " + this.numberOfAttributes + " attributes");
    }

    @Override
    public boolean isTyped() {
        return this.typeArray != null;
    }

    public int getNumberOfNodes() {
        return this.numberOfNodes;
    }

    public int getNumberOfAttributes() {
        return this.numberOfAttributes;
    }

    public int getNumberOfNamespaces() {
        return this.numberOfNamespaces;
    }

    @Override
    public byte[] getNodeKindArray() {
        return this.nodeKind;
    }

    public short[] getNodeDepthArray() {
        return this.depth;
    }

    @Override
    public int[] getNameCodeArray() {
        return this.nameCode;
    }

    public SchemaType[] getTypeArray() {
        return this.typeArray;
    }

    public int[] getNextPointerArray() {
        return this.next;
    }

    public int[] getAlphaArray() {
        return this.alpha;
    }

    public int[] getBetaArray() {
        return this.beta;
    }

    public AppendableCharSequence getCharacterBuffer() {
        return this.charBuffer;
    }

    public CharSequence getCommentBuffer() {
        return this.commentBuffer;
    }

    public int[] getAttributeNameCodeArray() {
        return this.attCode;
    }

    public SimpleType[] getAttributeTypeArray() {
        return this.attType;
    }

    public int[] getAttributeParentArray() {
        return this.attParent;
    }

    public CharSequence[] getAttributeValueArray() {
        return this.attValue;
    }

    public NamespaceBinding[] getNamespaceBindings() {
        throw new UnsupportedOperationException();
    }

    public NamespaceMap[] getNamespaceMaps() {
        return this.namespaceMaps;
    }

    public int[] getNamespaceParentArray() {
        throw new UnsupportedOperationException();
    }

    public boolean isUsesNamespaces() {
        return this.usesNamespaces;
    }

    public void bulkCopy(TinyTree source, int nodeNr, int currentDepth, int parentNodeNr) {
        int end = source.next[nodeNr];
        while (end < nodeNr && end >= 0) {
            end = source.next[end];
        }
        if (end == -1 && (end = source.numberOfNodes) - 1 < source.nodeKind.length && source.nodeKind[end - 1] == 11) {
            --end;
        }
        int length = end - nodeNr;
        assert (length > 0);
        this.ensureNodeCapacity((short)1, length);
        System.arraycopy(source.nodeKind, nodeNr, this.nodeKind, this.numberOfNodes, length);
        int depthDiff = currentDepth - source.depth[nodeNr];
        NamespaceMap subtreeRoot = source.namespaceMaps[source.beta[nodeNr]];
        NamespaceMap inherited = this.namespaceMaps[this.beta[parentNodeNr]];
        boolean sameNamespaces = subtreeRoot == inherited || inherited.isEmpty();
        block10: for (int i = 0; i < length; ++i) {
            int from = nodeNr + i;
            int to = this.numberOfNodes + i;
            this.depth[to] = (short)(source.depth[from] + depthDiff);
            this.next[to] = source.next[from] + (to - from);
            switch (source.nodeKind[from]) {
                case 1: {
                    NamespaceMap out;
                    this.nameCode[to] = source.nameCode[from] & 0xFFFFF | this.prefixPool.obtainPrefixCode(source.getPrefix(from)) << 20;
                    int firstAtt = source.alpha[from];
                    if (firstAtt >= 0) {
                        int aTo;
                        int lastAtt;
                        for (lastAtt = firstAtt; lastAtt < source.numberOfAttributes && source.attParent[lastAtt] == from; ++lastAtt) {
                        }
                        int atts = lastAtt - firstAtt;
                        this.ensureAttributeCapacity(atts);
                        int aFrom = firstAtt;
                        this.alpha[to] = aTo = this.numberOfAttributes;
                        System.arraycopy(source.attValue, firstAtt, this.attValue, aTo, atts);
                        Arrays.fill(this.attParent, aTo, aTo + atts, to);
                        int a = 0;
                        while (a < atts) {
                            this.attCode[aTo] = source.attCode[aFrom];
                            int attNameCode = this.attCode[aTo];
                            if (NamePool.isPrefixed(attNameCode)) {
                                String prefix = source.prefixPool.getPrefix(attNameCode >> 20);
                                this.attCode[aTo] = attNameCode & 0xFFFFF | this.prefixPool.obtainPrefixCode(prefix) << 20;
                            } else {
                                this.attCode[aTo] = attNameCode;
                            }
                            if (source.isIdAttribute(aFrom)) {
                                this.registerID(this.getNode(to), source.attValue[aFrom].toString());
                            }
                            if (source.isIdrefAttribute(aFrom)) {
                                if (this.idRefAttributes == null) {
                                    this.idRefAttributes = new IntHashSet();
                                }
                                this.idRefAttributes.add(aTo);
                            }
                            ++a;
                            ++aFrom;
                            ++aTo;
                        }
                        this.numberOfAttributes += atts;
                    } else {
                        this.alpha[to] = -1;
                    }
                    if (sameNamespaces) {
                        if (source.beta[from] == source.beta[nodeNr]) {
                            this.beta[to] = this.beta[parentNodeNr];
                            continue block10;
                        }
                        this.ensureNamespaceCapacity(1);
                        this.namespaceMaps[this.numberOfNamespaces] = source.namespaceMaps[source.beta[nodeNr]];
                        ++this.numberOfNamespaces;
                        continue block10;
                    }
                    if (i > 0 && source.beta[from] == source.beta[nodeNr]) {
                        this.beta[to] = this.beta[parentNodeNr];
                        continue block10;
                    }
                    this.ensureNamespaceCapacity(1);
                    NamespaceMap in = source.namespaceMaps[source.beta[from]];
                    this.namespaceMaps[this.numberOfNamespaces] = out = inherited.putAll(in);
                    ++this.numberOfNamespaces;
                    continue block10;
                }
                case 17: {
                    int start = source.alpha[from];
                    int len = source.beta[from];
                    this.nameCode[to] = source.nameCode[from] & 0xFFFFF | this.prefixPool.obtainPrefixCode(source.getPrefix(from)) << 20;
                    this.alpha[to] = this.charBuffer.length();
                    this.appendChars(source.charBuffer.subSequence(start, start + len));
                    this.beta[to] = len;
                    continue block10;
                }
                case 3: {
                    int start = source.alpha[from];
                    int len = source.beta[from];
                    this.nameCode[to] = -1;
                    this.alpha[to] = this.charBuffer.length();
                    this.appendChars(source.charBuffer.subSequence(start, start + len));
                    this.beta[to] = len;
                    continue block10;
                }
                case 4: {
                    this.nameCode[to] = -1;
                    this.alpha[to] = source.alpha[from];
                    this.beta[to] = source.beta[from];
                    continue block10;
                }
                case 8: {
                    int start = source.alpha[from];
                    int len = source.beta[from];
                    this.nameCode[to] = -1;
                    CharSequence text = source.commentBuffer.subSequence(start, start + len);
                    if (this.commentBuffer == null) {
                        this.commentBuffer = new FastStringBuffer(256);
                    }
                    this.alpha[to] = this.commentBuffer.length();
                    this.commentBuffer.cat(text);
                    this.beta[to] = len;
                    continue block10;
                }
                case 7: {
                    int start = source.alpha[from];
                    int len = source.beta[from];
                    this.nameCode[to] = source.nameCode[from];
                    CharSequence text = source.commentBuffer.subSequence(start, start + len);
                    if (this.commentBuffer == null) {
                        this.commentBuffer = new FastStringBuffer(256);
                    }
                    this.alpha[to] = this.commentBuffer.length();
                    this.commentBuffer.cat(text);
                    this.beta[to] = len;
                    continue block10;
                }
                case 12: {
                    this.nameCode[to] = -1;
                    this.alpha[to] = source.alpha[from] + (to - from);
                    this.beta[to] = -1;
                    continue block10;
                }
            }
        }
        this.numberOfNodes += length;
    }

    public synchronized Map<String, IntSet> getLocalNameIndex() {
        if (this.localNameIndex == null) {
            this.localNameIndex = new HashMap<String, IntSet>();
            IntHashSet indexed = new IntHashSet();
            for (int i = 0; i < this.numberOfNodes; ++i) {
                int fp;
                if ((this.nodeKind[i] & 0xF) != 1 || indexed.contains(fp = this.nameCode[i] & 0xFFFFF)) continue;
                String local = this.getNamePool().getLocalName(fp);
                indexed.add(fp);
                IntSet existing = this.localNameIndex.get(local);
                if (existing == null) {
                    this.localNameIndex.put(local, new IntSingletonSet(fp));
                    continue;
                }
                IntSet copy = existing.isMutable() ? existing : existing.mutableCopy();
                copy.add(fp);
                this.localNameIndex.put(local, copy);
            }
        }
        return this.localNameIndex;
    }
}

