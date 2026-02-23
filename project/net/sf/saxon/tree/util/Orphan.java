/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.util;

import java.util.function.Predicate;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.GenericTreeInfo;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.SingleNodeIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.UntypedAtomicValue;

public final class Orphan
implements MutableNodeInfo {
    private short kind;
    private NodeName nodeName = null;
    private CharSequence stringValue;
    private SchemaType typeAnnotation = null;
    private int options = 0;
    private GenericTreeInfo treeInfo;

    public Orphan(Configuration config) {
        this.treeInfo = new GenericTreeInfo(config);
        this.treeInfo.setRootNode(this);
    }

    @Override
    public TreeInfo getTreeInfo() {
        return this.treeInfo;
    }

    @Override
    public String getSystemId() {
        return this.treeInfo.getSystemId();
    }

    @Override
    public String getPublicId() {
        return this.treeInfo.getPublicId();
    }

    @Override
    public void setSystemId(String systemId) {
        this.treeInfo.setSystemId(systemId);
    }

    @Override
    public boolean effectiveBooleanValue() {
        return true;
    }

    public void setNodeKind(short kind) {
        this.kind = kind;
    }

    public void setNodeName(NodeName nodeName) {
        this.nodeName = nodeName;
    }

    public void setStringValue(CharSequence stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public void setTypeAnnotation(SchemaType typeAnnotation) {
        this.typeAnnotation = typeAnnotation;
    }

    public void setIsId(boolean id) {
        this.setOption(2048, id);
    }

    private void setOption(int option, boolean on) {
        this.options = on ? (this.options |= option) : (this.options &= ~option);
    }

    private boolean isOption(int option) {
        return ReceiverOption.contains(this.options, option);
    }

    public void setIsIdref(boolean idref) {
        this.setOption(4096, idref);
    }

    public void setDisableOutputEscaping(boolean doe) {
        this.setOption(1, doe);
    }

    @Override
    public int getNodeKind() {
        return this.kind;
    }

    @Override
    public int getFingerprint() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasFingerprint() {
        return false;
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        switch (this.getNodeKind()) {
            case 7: 
            case 8: {
                return new StringValue(this.stringValue);
            }
            case 3: 
            case 9: 
            case 13: {
                return new UntypedAtomicValue(this.stringValue);
            }
        }
        if (this.typeAnnotation == null || this.typeAnnotation == Untyped.getInstance() || this.typeAnnotation == BuiltInAtomicType.UNTYPED_ATOMIC) {
            return new UntypedAtomicValue(this.stringValue);
        }
        return this.typeAnnotation.atomize(this);
    }

    @Override
    public SchemaType getSchemaType() {
        if (this.typeAnnotation == null) {
            if (this.kind == 1) {
                return Untyped.getInstance();
            }
            if (this.kind == 2) {
                return BuiltInAtomicType.UNTYPED_ATOMIC;
            }
        }
        return this.typeAnnotation;
    }

    public boolean equals(NodeInfo other) {
        return this == other;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String getBaseURI() {
        if (this.kind == 7) {
            return this.getSystemId();
        }
        return null;
    }

    @Override
    public Location saveLocation() {
        return this;
    }

    @Override
    public int compareOrder(NodeInfo other) {
        if (this.equals(other)) {
            return 0;
        }
        return this.hashCode() < other.hashCode() ? -1 : 1;
    }

    @Override
    public String getStringValue() {
        return this.stringValue.toString();
    }

    @Override
    public CharSequence getStringValueCS() {
        return this.stringValue;
    }

    @Override
    public String getLocalPart() {
        if (this.nodeName == null) {
            return "";
        }
        return this.nodeName.getLocalPart();
    }

    @Override
    public String getURI() {
        if (this.nodeName == null) {
            return "";
        }
        return this.nodeName.getURI();
    }

    @Override
    public String getPrefix() {
        if (this.nodeName == null) {
            return "";
        }
        return this.nodeName.getPrefix();
    }

    @Override
    public String getDisplayName() {
        if (this.nodeName == null) {
            return "";
        }
        return this.nodeName.getDisplayName();
    }

    @Override
    public NodeInfo getParent() {
        return null;
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber) {
        switch (axisNumber) {
            case 1: 
            case 5: 
            case 12: {
                return SingleNodeIterator.makeIterator(this);
            }
            case 0: 
            case 2: 
            case 3: 
            case 4: 
            case 6: 
            case 7: 
            case 8: 
            case 9: 
            case 10: 
            case 11: 
            case 13: {
                return EmptyIterator.ofNodes();
            }
        }
        throw new IllegalArgumentException("Unknown axis number " + axisNumber);
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber, Predicate<? super NodeInfo> nodeTest) {
        switch (axisNumber) {
            case 1: 
            case 5: 
            case 12: {
                return Navigator.filteredSingleton(this, nodeTest);
            }
            case 0: 
            case 2: 
            case 3: 
            case 4: 
            case 6: 
            case 7: 
            case 8: 
            case 9: 
            case 10: 
            case 11: 
            case 13: {
                return EmptyIterator.ofNodes();
            }
        }
        throw new IllegalArgumentException("Unknown axis number " + axisNumber);
    }

    @Override
    public String getAttributeValue(String uri, String local) {
        return null;
    }

    @Override
    public NodeInfo getRoot() {
        return this;
    }

    @Override
    public boolean hasChildNodes() {
        return false;
    }

    @Override
    public void generateId(FastStringBuffer buffer) {
        buffer.cat('Q');
        buffer.append(Integer.toString(this.hashCode()));
    }

    @Override
    public NamespaceBinding[] getDeclaredNamespaces(NamespaceBinding[] buffer) {
        return null;
    }

    @Override
    public NamespaceMap getAllNamespaces() {
        return null;
    }

    @Override
    public boolean isId() {
        return this.isOption(2048) || this.kind == 2 && this.nodeName.equals(StandardNames.XML_ID_NAME);
    }

    @Override
    public boolean isIdref() {
        return this.isOption(4096);
    }

    public boolean isDisableOutputEscaping() {
        return this.isOption(1);
    }

    @Override
    public void insertChildren(NodeInfo[] source, boolean atStart, boolean inherit) {
    }

    @Override
    public void insertSiblings(NodeInfo[] source, boolean before, boolean inherit) {
    }

    @Override
    public void setAttributes(AttributeMap attributes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttribute(NodeInfo attribute) {
    }

    @Override
    public void addAttribute(NodeName nameCode, SimpleType attType, CharSequence value, int properties) {
    }

    @Override
    public void delete() {
        this.kind = (short)-1;
    }

    @Override
    public boolean isDeleted() {
        return this.kind == -1;
    }

    @Override
    public void replace(NodeInfo[] replacement, boolean inherit) {
        throw new IllegalStateException("Cannot replace a parentless node");
    }

    @Override
    public void replaceStringValue(CharSequence stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public void rename(NodeName newNameCode) {
        if (this.kind == 2 || this.kind == 7) {
            this.nodeName = newNameCode;
        }
    }

    @Override
    public void addNamespace(NamespaceBinding nscode) {
    }

    @Override
    public void removeTypeAnnotation() {
        this.typeAnnotation = BuiltInAtomicType.UNTYPED_ATOMIC;
    }

    @Override
    public Builder newBuilder() {
        throw new UnsupportedOperationException("Cannot create children for an Orphan node");
    }
}

