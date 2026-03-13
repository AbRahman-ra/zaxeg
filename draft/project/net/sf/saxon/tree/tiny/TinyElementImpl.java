/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.CopyInformee;
import net.sf.saxon.event.CopyNamespaceSensitiveException;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.CodedName;
import net.sf.saxon.om.CopyOptions;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.tiny.TinyParentNodeImpl;
import net.sf.saxon.tree.tiny.TinyTextImpl;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.tree.tiny.WhitespaceTextImpl;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.z.IntHashMap;

public class TinyElementImpl
extends TinyParentNodeImpl {
    public TinyElementImpl(TinyTree tree, int nodeNr) {
        this.tree = tree;
        this.nodeNr = nodeNr;
    }

    @Override
    public final int getNodeKind() {
        return 1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getBaseURI() {
        if (this.tree.getUniformBaseUri() != null) {
            return this.tree.getUniformBaseUri();
        }
        TinyTree tinyTree = this.tree;
        synchronized (tinyTree) {
            String uri;
            if (this.tree.knownBaseUris == null) {
                this.tree.knownBaseUris = new IntHashMap();
            }
            if ((uri = this.tree.knownBaseUris.get(this.nodeNr)) == null) {
                uri = Navigator.getBaseURI(this, n -> this.tree.isTopWithinEntity(((TinyElementImpl)n).getNodeNumber()));
                this.tree.knownBaseUris.put(this.nodeNr, uri);
            }
            return uri;
        }
    }

    @Override
    public SchemaType getSchemaType() {
        return this.tree.getSchemaType(this.nodeNr);
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        return this.tree.getTypedValueOfElement(this);
    }

    @Override
    public NamespaceBinding[] getDeclaredNamespaces(NamespaceBinding[] buffer) {
        TinyNodeImpl parent = this.getParent();
        if (parent != null && parent.getNodeKind() == 1) {
            return this.getAllNamespaces().getDifferences(parent.getAllNamespaces(), false);
        }
        return this.getAllNamespaces().getNamespaceBindings();
    }

    @Override
    public NamespaceMap getAllNamespaces() {
        return this.tree.namespaceMaps[this.tree.beta[this.nodeNr]];
    }

    public boolean hasUniformNamespaces() {
        return false;
    }

    @Override
    public String getAttributeValue(String uri, String local) {
        int a = this.tree.alpha[this.nodeNr];
        if (a < 0) {
            return null;
        }
        NamePool pool = this.getNamePool();
        while (a < this.tree.numberOfAttributes && this.tree.attParent[a] == this.nodeNr) {
            int nc = this.tree.attCode[a];
            StructuredQName name = pool.getUnprefixedQName(nc);
            if (name.getLocalPart().equals(local) && name.hasURI(uri)) {
                return this.tree.attValue[a].toString();
            }
            ++a;
        }
        return null;
    }

    public String getAttributeValue(int fp) {
        int a = this.tree.alpha[this.nodeNr];
        if (a < 0) {
            return null;
        }
        while (a < this.tree.numberOfAttributes && this.tree.attParent[a] == this.nodeNr) {
            if (fp == (this.tree.attCode[a] & 0xFFFFF)) {
                return this.tree.attValue[a].toString();
            }
            ++a;
        }
        return null;
    }

    private int subtreeSize() {
        int next = this.tree.next[this.nodeNr];
        while (next < this.nodeNr) {
            if (next < 0) {
                return this.tree.numberOfNodes - this.nodeNr;
            }
            next = this.tree.next[next];
        }
        return this.nodeNr - next;
    }

    @Override
    public void copy(Receiver receiver, int copyOptions, Location location) throws XPathException {
        boolean copyTypes = CopyOptions.includes(copyOptions, 4);
        short level = -1;
        boolean closePending = false;
        short startLevel = this.tree.depth[this.nodeNr];
        boolean disallowNamespaceSensitiveContent = (copyOptions & 4) != 0 && (copyOptions & 2) == 0;
        Configuration config = this.tree.getConfiguration();
        NamePool pool = config.getNamePool();
        int next = this.nodeNr;
        CopyInformee informee = (CopyInformee)receiver.getPipelineConfiguration().getComponent(CopyInformee.class.getName());
        SchemaType elementType = Untyped.getInstance();
        SimpleType attributeType = BuiltInAtomicType.UNTYPED_ATOMIC;
        do {
            short nodeLevel = this.tree.depth[next];
            if (closePending) {
                level = (short)(level + 1);
            }
            while (level > nodeLevel) {
                receiver.endElement();
                level = (short)(level - 1);
            }
            level = nodeLevel;
            byte kind = this.tree.nodeKind[next];
            switch (kind) {
                case 1: 
                case 17: {
                    Location loc;
                    if (copyTypes) {
                        elementType = this.tree.getSchemaType(next);
                        if (disallowNamespaceSensitiveContent) {
                            try {
                                this.checkNotNamespaceSensitiveElement(elementType, next);
                            } catch (CopyNamespaceSensitiveException e) {
                                e.setErrorCode(receiver.getPipelineConfiguration().isXSLT() ? "XTTE0950" : "XQTY0086");
                                throw e;
                            }
                        }
                    }
                    if (informee != null && (loc = (Location)informee.notifyElementNode(this.tree.getNode(next))) != null) {
                        location = loc;
                    }
                    int nameCode = this.tree.nameCode[next];
                    int fp = nameCode & 0xFFFFF;
                    String prefix = this.tree.getPrefix(next);
                    if (location.getLineNumber() < this.tree.getLineNumber(next)) {
                        String systemId = location.getSystemId() == null ? this.getSystemId() : location.getSystemId();
                        location = new Loc(systemId, this.tree.getLineNumber(next), this.getColumnNumber());
                    }
                    NamespaceMap namespaces = NamespaceMap.emptyMap();
                    boolean addAttributeNamespaces = false;
                    if (this.tree.usesNamespaces) {
                        if ((copyOptions & 2) != 0) {
                            if (kind == 17) {
                                int parent = TinyElementImpl.getParentNodeNr(this.tree, next);
                                namespaces = this.tree.namespaceMaps[this.tree.beta[parent]];
                            } else {
                                namespaces = this.tree.namespaceMaps[this.tree.beta[next]];
                            }
                        } else {
                            addAttributeNamespaces = true;
                            String uri = pool.getURI(nameCode);
                            if (!uri.isEmpty()) {
                                namespaces = NamespaceMap.of(prefix, uri);
                            }
                        }
                    }
                    if (kind == 17) {
                        closePending = false;
                        receiver.startElement(new CodedName(fp, prefix, pool), elementType, EmptyAttributeMap.getInstance(), namespaces, location, 131072);
                        CharSequence value = TinyTextImpl.getStringValue(this.tree, next);
                        receiver.characters(value, location, 1024);
                        receiver.endElement();
                        break;
                    }
                    closePending = true;
                    AttributeMap attributes = EmptyAttributeMap.getInstance();
                    int att = this.tree.alpha[next];
                    if (att >= 0) {
                        while (att < this.tree.numberOfAttributes && this.tree.attParent[att] == next) {
                            int attCode = this.tree.attCode[att];
                            int attfp = attCode & 0xFFFFF;
                            if (copyTypes) {
                                attributeType = this.tree.getAttributeType(att);
                                if (disallowNamespaceSensitiveContent) {
                                    try {
                                        this.checkNotNamespaceSensitiveAttribute(attributeType, att);
                                    } catch (CopyNamespaceSensitiveException e) {
                                        e.setErrorCode(receiver.getPipelineConfiguration().isXSLT() ? "XTTE0950" : "XQTY0086");
                                        throw e;
                                    }
                                }
                            }
                            String attPrefix = this.tree.prefixPool.getPrefix(attCode >> 20);
                            int attProps = 0x100000;
                            if (this.tree.isIdAttribute(att)) {
                                attProps |= 0x800;
                            }
                            if (this.tree.isIdrefAttribute(att)) {
                                attProps |= 0x1000;
                            }
                            attributes = attributes.put(new AttributeInfo(new CodedName(attfp, attPrefix, pool), attributeType, this.tree.attValue[att].toString(), location, attProps));
                            if (addAttributeNamespaces && !attPrefix.isEmpty()) {
                                namespaces = namespaces.put(attPrefix, pool.getURI(attCode));
                            }
                            ++att;
                        }
                    }
                    receiver.startElement(new CodedName(fp, prefix, pool), elementType, attributes, namespaces, location, 131072);
                    break;
                }
                case 3: {
                    closePending = false;
                    CharSequence value = TinyTextImpl.getStringValue(this.tree, next);
                    receiver.characters(value, location, 1024);
                    break;
                }
                case 4: {
                    closePending = false;
                    CharSequence value = WhitespaceTextImpl.getStringValueCS(this.tree, next);
                    receiver.characters(value, location, 1024);
                    break;
                }
                case 8: {
                    closePending = false;
                    int start = this.tree.alpha[next];
                    int len = this.tree.beta[next];
                    if (len > 0) {
                        receiver.comment(this.tree.commentBuffer.subSequence(start, start + len), location, 0);
                        break;
                    }
                    receiver.comment("", Loc.NONE, 0);
                    break;
                }
                case 7: {
                    closePending = false;
                    TinyNodeImpl pi = this.tree.getNode(next);
                    receiver.processingInstruction(pi.getLocalPart(), pi.getStringValue(), location, 0);
                    break;
                }
                case 12: {
                    closePending = false;
                }
            }
        } while (++next < this.tree.numberOfNodes && this.tree.depth[next] > startLevel);
        if (closePending) {
            level = (short)(level + 1);
        }
        while (level > startLevel) {
            receiver.endElement();
            level = (short)(level - 1);
        }
    }

    protected void checkNotNamespaceSensitiveElement(SchemaType type, int nodeNr) throws XPathException {
        if (type instanceof SimpleType && ((SimpleType)type).isNamespaceSensitive()) {
            if (type.isAtomicType()) {
                throw new CopyNamespaceSensitiveException("Cannot copy QName or NOTATION values without copying namespaces");
            }
            AtomicSequence value = this.tree.getTypedValueOfElement(nodeNr);
            for (AtomicValue val : value) {
                if (!val.getPrimitiveType().isNamespaceSensitive()) continue;
                throw new CopyNamespaceSensitiveException("Cannot copy QName or NOTATION values without copying namespaces");
            }
        }
    }

    private void checkNotNamespaceSensitiveAttribute(SimpleType type, int nodeNr) throws XPathException {
        if (type.isNamespaceSensitive()) {
            if (type.isAtomicType()) {
                throw new CopyNamespaceSensitiveException("Cannot copy QName or NOTATION values without copying namespaces");
            }
            AtomicSequence value = this.tree.getTypedValueOfAttribute(null, nodeNr);
            for (AtomicValue val : value) {
                if (!val.getPrimitiveType().isNamespaceSensitive()) continue;
                throw new CopyNamespaceSensitiveException("Cannot copy QName or NOTATION values without copying namespaces");
            }
        }
    }

    public String getURIForPrefix(String prefix, boolean useDefault) {
        if (!useDefault && (prefix == null || prefix.isEmpty())) {
            return "";
        }
        int ns = this.tree.beta[this.nodeNr];
        NamespaceMap map = this.tree.namespaceMaps[ns];
        return map.getURIForPrefix(prefix, useDefault);
    }

    @Override
    public boolean isId() {
        return this.tree.isIdElement(this.nodeNr);
    }

    @Override
    public boolean isIdref() {
        return this.tree.isIdrefElement(this.nodeNr);
    }

    private boolean isSkipValidator(Receiver r) {
        return false;
    }
}

