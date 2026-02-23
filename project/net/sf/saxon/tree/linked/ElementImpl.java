/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;
import net.sf.saxon.event.CopyInformee;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.CopyOptions;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.linked.AttributeAxisIterator;
import net.sf.saxon.tree.linked.AttributeImpl;
import net.sf.saxon.tree.linked.AttributeMapWithIdentity;
import net.sf.saxon.tree.linked.DocumentImpl;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.tree.linked.ParentNodeImpl;
import net.sf.saxon.tree.linked.TextImpl;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.BuiltInListType;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Whitespace;

public class ElementImpl
extends ParentNodeImpl
implements NamespaceResolver {
    private NodeName nodeName;
    private SchemaType type = Untyped.getInstance();
    private AttributeMap attributeMap;
    private NamespaceMap namespaceMap = NamespaceMap.emptyMap();

    public ElementImpl() {
        this.attributeMap = EmptyAttributeMap.getInstance();
    }

    @Override
    public void setAttributes(AttributeMap atts) {
        this.attributeMap = atts;
    }

    public void setNodeName(NodeName name) {
        this.nodeName = name;
    }

    public void initialise(NodeName elemName, SchemaType elementType, AttributeMap atts, NodeInfo parent, int sequenceNumber) {
        this.nodeName = elemName;
        this.type = elementType;
        this.setRawParent((ParentNodeImpl)parent);
        this.setRawSequenceNumber(sequenceNumber);
        this.attributeMap = atts;
    }

    @Override
    public NodeName getNodeName() {
        return this.nodeName;
    }

    public void setLocation(String systemId, int line, int column) {
        DocumentImpl root = this.getRawParent().getPhysicalRoot();
        root.setLineAndColumn(this.getRawSequenceNumber(), line, column);
        root.setSystemId(this.getRawSequenceNumber(), systemId);
    }

    @Override
    public void setSystemId(String uri) {
        this.getPhysicalRoot().setSystemId(this.getRawSequenceNumber(), uri);
    }

    @Override
    public NodeInfo getRoot() {
        ParentNodeImpl up = this.getRawParent();
        if (up == null || up instanceof DocumentImpl && ((DocumentImpl)up).isImaginary()) {
            return this;
        }
        return up.getRoot();
    }

    @Override
    public final String getSystemId() {
        DocumentImpl root = this.getPhysicalRoot();
        return root == null ? null : root.getSystemId(this.getRawSequenceNumber());
    }

    @Override
    public String getBaseURI() {
        return Navigator.getBaseURI(this, n -> this.getPhysicalRoot().isTopWithinEntity((ElementImpl)n));
    }

    @Override
    public boolean isNilled() {
        return this.getPhysicalRoot().isNilledElement(this);
    }

    @Override
    public void setTypeAnnotation(SchemaType type) {
        this.type = type;
    }

    public void setNilled() {
        this.getPhysicalRoot().addNilledElement(this);
    }

    @Override
    public SchemaType getSchemaType() {
        return this.type;
    }

    @Override
    public int getLineNumber() {
        DocumentImpl root = this.getPhysicalRoot();
        if (root == null) {
            return -1;
        }
        return root.getLineNumber(this.getRawSequenceNumber());
    }

    @Override
    public int getColumnNumber() {
        DocumentImpl root = this.getPhysicalRoot();
        if (root == null) {
            return -1;
        }
        return root.getColumnNumber(this.getRawSequenceNumber());
    }

    @Override
    public void generateId(FastStringBuffer buffer) {
        int sequence = this.getRawSequenceNumber();
        if (sequence >= 0) {
            this.getPhysicalRoot().generateId(buffer);
            buffer.append("e");
            buffer.append(Integer.toString(sequence));
        } else {
            this.getRawParent().generateId(buffer);
            buffer.append("f");
            buffer.append(Integer.toString(this.getSiblingPosition()));
        }
    }

    @Override
    public final int getNodeKind() {
        return 1;
    }

    @Override
    public AttributeMap attributes() {
        return this.attributeMap;
    }

    AxisIterator iterateAttributes(Predicate<? super NodeInfo> test) {
        if (this.attributeMap instanceof AttributeMapWithIdentity) {
            return new Navigator.AxisFilter(((AttributeMapWithIdentity)this.attributeMap).iterateAttributes(this), test);
        }
        return new AttributeAxisIterator(this, test);
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location location) throws XPathException {
        Object o;
        SchemaType typeCode = CopyOptions.includes(copyOptions, 4) ? this.getSchemaType() : Untyped.getInstance();
        CopyInformee informee = (CopyInformee)out.getPipelineConfiguration().getComponent(CopyInformee.class.getName());
        if (informee != null && (o = informee.notifyElementNode(this)) instanceof Location) {
            location = (Location)o;
        }
        NamespaceMap ns = CopyOptions.includes(copyOptions, 2) ? this.getAllNamespaces() : NamespaceMap.emptyMap();
        ArrayList<AttributeInfo> atts = new ArrayList<AttributeInfo>(this.attributes().size());
        for (AttributeInfo att : this.attributes()) {
            atts.add(new AttributeInfo(att.getNodeName(), BuiltInAtomicType.UNTYPED_ATOMIC, att.getValue(), att.getLocation(), 0));
        }
        out.startElement(NameOfNode.makeName(this), typeCode, AttributeMap.fromList(atts), ns, location, 131136);
        for (NodeImpl next = this.getFirstChild(); next != null; next = next.getNextSibling()) {
            next.copy(out, copyOptions, location);
        }
        out.endElement();
    }

    @Override
    public void delete() {
        DocumentImpl root = this.getPhysicalRoot();
        super.delete();
        if (root != null) {
            AxisIterator iter = this.iterateAxis(5, NodeKindTest.ELEMENT);
            while (true) {
                ElementImpl n = (ElementImpl)iter.next();
                for (AttributeInfo att : this.attributeMap) {
                    if (!att.isId()) continue;
                    root.deregisterID(att.getValue());
                }
                if (n == null) break;
                root.deIndex(n);
            }
        }
    }

    @Override
    public void rename(NodeName newName) {
        String prefix = newName.getPrefix();
        String uri = newName.getURI();
        NamespaceBinding ns = new NamespaceBinding(prefix, uri);
        String uc = this.getURIForPrefix(prefix, true);
        if (uc == null) {
            uc = "";
        }
        if (!uc.equals(uri)) {
            if (uc.isEmpty()) {
                this.addNamespace(ns);
            } else {
                throw new IllegalArgumentException("Namespace binding of new name conflicts with existing namespace binding");
            }
        }
        this.nodeName = newName;
    }

    @Override
    public void addNamespace(NamespaceBinding binding) {
        if (binding.getURI().isEmpty()) {
            throw new IllegalArgumentException("Cannot add a namespace undeclaration");
        }
        String existing = this.namespaceMap.getURI(binding.getPrefix());
        if (existing != null) {
            if (!existing.equals(binding.getURI())) {
                throw new IllegalArgumentException("New namespace conflicts with existing namespace binding");
            }
        } else {
            this.namespaceMap = this.namespaceMap.put(binding.getPrefix(), binding.getURI());
        }
    }

    @Override
    public void replaceStringValue(CharSequence stringValue) {
        if (stringValue.length() == 0) {
            this.setChildren(null);
        } else {
            TextImpl text = new TextImpl(stringValue.toString());
            text.setRawParent(this);
            this.setChildren(text);
        }
    }

    public void setAttributeInfo(int index, AttributeInfo attInfo) {
        AttributeMapWithIdentity attMap = this.prepareAttributesForUpdate();
        attMap = attMap.set(index, attInfo);
        this.setAttributes(attMap);
    }

    private AttributeMapWithIdentity prepareAttributesForUpdate() {
        if (this.attributes() instanceof AttributeMapWithIdentity) {
            return (AttributeMapWithIdentity)this.attributes();
        }
        AttributeMapWithIdentity newAtts = new AttributeMapWithIdentity(this.attributes().asList());
        this.setAttributes(newAtts);
        return newAtts;
    }

    @Override
    public void addAttribute(NodeName nodeName, SimpleType attType, CharSequence value, int properties) {
        DocumentImpl root;
        AttributeMapWithIdentity atts = this.prepareAttributesForUpdate();
        atts = atts.add(new AttributeInfo(nodeName, attType, value.toString(), Loc.NONE, 0));
        this.setAttributes(atts);
        if (!nodeName.hasURI("")) {
            NamespaceBinding binding = nodeName.getNamespaceBinding();
            String prefix = binding.getPrefix();
            String uc = this.getURIForPrefix(prefix, false);
            if (uc == null) {
                this.addNamespace(binding);
            } else if (!uc.equals(binding.getURI())) {
                throw new IllegalStateException("Namespace binding of new name conflicts with existing namespace binding");
            }
        }
        if (ReceiverOption.contains(properties, 2048) && (root = this.getPhysicalRoot()) != null) {
            root.registerID(this, Whitespace.trim(value));
        }
    }

    @Override
    public void removeAttribute(NodeInfo attribute) {
        if (!(attribute instanceof AttributeImpl)) {
            return;
        }
        int index = ((AttributeImpl)attribute).getSiblingPosition();
        AttributeInfo info = this.attributes().itemAt(index);
        AttributeMapWithIdentity atts = this.prepareAttributesForUpdate();
        atts = atts.remove(index);
        this.setAttributes(atts);
        if (index >= 0 && info.isId()) {
            DocumentImpl root = this.getPhysicalRoot();
            root.deregisterID(info.getValue());
        }
        ((AttributeImpl)attribute).setRawParent(null);
    }

    @Override
    public void removeNamespace(String prefix) {
        Objects.requireNonNull(prefix);
        if (prefix.equals(this.getPrefix())) {
            throw new IllegalStateException("Cannot remove binding of namespace prefix used on the element name");
        }
        for (AttributeInfo att : this.attributeMap) {
            if (!att.getNodeName().getPrefix().equals(prefix)) continue;
            throw new IllegalStateException("Cannot remove binding of namespace prefix used on an existing attribute name");
        }
        this.namespaceMap = this.namespaceMap.remove(prefix);
    }

    @Override
    public void addNamespace(String prefix, String uri) {
        String existingURI = this.namespaceMap.getURI(prefix);
        if (existingURI == null) {
            this.namespaceMap = this.namespaceMap.put(prefix, uri);
        } else if (!existingURI.equals(uri)) {
            throw new IllegalStateException("New namespace binding conflicts with existing namespace binding");
        }
    }

    @Override
    public void removeTypeAnnotation() {
        if (this.getSchemaType() != Untyped.getInstance()) {
            this.type = AnyType.getInstance();
            this.getRawParent().removeTypeAnnotation();
        }
    }

    public void setNamespaceMap(NamespaceMap map) {
        this.namespaceMap = map;
    }

    @Override
    public String getURIForPrefix(String prefix, boolean useDefault) {
        if (prefix.isEmpty()) {
            if (useDefault) {
                return this.namespaceMap.getDefaultNamespace();
            }
            return "";
        }
        return this.namespaceMap.getURI(prefix);
    }

    @Override
    public Iterator<String> iteratePrefixes() {
        return this.namespaceMap.iteratePrefixes();
    }

    public boolean isInScopeNamespace(String uri) {
        for (NamespaceBinding b : this.namespaceMap) {
            if (!b.getURI().equals(uri)) continue;
            return true;
        }
        return false;
    }

    @Override
    public NamespaceBinding[] getDeclaredNamespaces(NamespaceBinding[] buffer) {
        ArrayList<NamespaceBinding> bindings = new ArrayList<NamespaceBinding>();
        for (NamespaceBinding nb : this.namespaceMap) {
            bindings.add(nb);
        }
        return bindings.toArray(NamespaceBinding.EMPTY_ARRAY);
    }

    protected void fixupInsertedNamespaces(boolean inherit) {
        if (this.getRawParent().getNodeKind() == 9) {
            return;
        }
        ElementImpl parent = (ElementImpl)this.getRawParent();
        NamespaceMap parentNamespaces = parent.namespaceMap;
        if (inherit) {
            this.deepAddNamespaces(parentNamespaces);
        }
    }

    private void deepAddNamespaces(NamespaceMap inheritedNamespaces) {
        NamespaceMap childNamespaces = this.namespaceMap;
        for (NamespaceBinding namespaceBinding : inheritedNamespaces) {
            if (childNamespaces.getURI(namespaceBinding.getPrefix()) == null) {
                childNamespaces = childNamespaces.put(namespaceBinding.getPrefix(), namespaceBinding.getURI());
                continue;
            }
            inheritedNamespaces = inheritedNamespaces.remove(namespaceBinding.getPrefix());
        }
        this.namespaceMap = childNamespaces;
        for (NodeInfo nodeInfo : this.children(ElementImpl.class::isInstance)) {
            ((ElementImpl)nodeInfo).deepAddNamespaces(inheritedNamespaces);
        }
    }

    @Override
    public NamespaceMap getAllNamespaces() {
        return this.namespaceMap;
    }

    @Override
    public String getAttributeValue(String uri, String localName) {
        return this.attributeMap == null ? null : this.attributeMap.getValue(uri, localName);
    }

    @Override
    public boolean isId() {
        try {
            SchemaType type = this.getSchemaType();
            return type.getFingerprint() == 560 || type.isIdType() && NameChecker.isValidNCName(this.getStringValueCS());
        } catch (MissingComponentException e) {
            return false;
        }
    }

    @Override
    public boolean isIdref() {
        return ElementImpl.isIdRefNode(this);
    }

    static boolean isIdRefNode(NodeImpl node) {
        SchemaType type = node.getSchemaType();
        try {
            if (type.isIdRefType()) {
                if (type == BuiltInAtomicType.IDREF || type == BuiltInListType.IDREFS) {
                    return true;
                }
                try {
                    for (AtomicValue av : node.atomize()) {
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
}

