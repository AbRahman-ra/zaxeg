/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.HashSet;
import java.util.function.Supplier;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.ProxyOutputter;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pattern.CombinedNodeTest;
import net.sf.saxon.pattern.ContentTypeTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.Orphan;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.Cardinality;

public class TypeCheckingFilter
extends ProxyOutputter {
    private ItemType itemType;
    private int cardinality;
    private RoleDiagnostic role;
    private Location locator;
    private int count = 0;
    private int level = 0;
    private HashSet<Long> checkedElements = new HashSet(10);
    private TypeHierarchy typeHierarchy = this.getConfiguration().getTypeHierarchy();

    public TypeCheckingFilter(Outputter next) {
        super(next);
    }

    public void setRequiredType(ItemType type, int cardinality, RoleDiagnostic role, Location locator) {
        this.itemType = type;
        this.cardinality = cardinality;
        this.role = role;
        this.locator = locator;
    }

    @Override
    public void namespace(String prefix, String namespaceUri, int properties) throws XPathException {
        if (this.level == 0) {
            if (++this.count == 2) {
                this.checkAllowsMany(Loc.NONE);
            }
            this.checkItemType(NodeKindTest.NAMESPACE, null, Loc.NONE);
        }
        this.getNextOutputter().namespace(prefix, namespaceUri, properties);
    }

    @Override
    public void attribute(NodeName attName, SimpleType typeCode, CharSequence value, Location location, int properties) throws XPathException {
        if (this.level == 0) {
            if (++this.count == 2) {
                this.checkAllowsMany(location);
            }
            CombinedNodeTest type = new CombinedNodeTest(new NameTest(2, attName, this.getConfiguration().getNamePool()), 23, new ContentTypeTest(2, typeCode, this.getConfiguration(), false));
            this.checkItemType(type, this.nodeSupplier((short)2, attName, typeCode, value), location);
        }
        this.getNextOutputter().attribute(attName, typeCode, value, location, properties);
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.level == 0) {
            if (++this.count == 2) {
                this.checkAllowsMany(locationId);
            }
            this.checkItemType(NodeKindTest.TEXT, this.nodeSupplier((short)3, null, null, chars), locationId);
        }
        this.getNextOutputter().characters(chars, locationId, properties);
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.level == 0) {
            if (++this.count == 2) {
                this.checkAllowsMany(locationId);
            }
            this.checkItemType(NodeKindTest.COMMENT, this.nodeSupplier((short)8, null, null, chars), locationId);
        }
        this.getNextOutputter().comment(chars, locationId, properties);
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        if (this.level == 0) {
            if (++this.count == 2) {
                this.checkAllowsMany(locationId);
            }
            this.checkItemType(NodeKindTest.PROCESSING_INSTRUCTION, this.nodeSupplier((short)7, new NoNamespaceName(target), null, data), locationId);
        }
        this.getNextOutputter().processingInstruction(target, data, locationId, properties);
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        if (this.level == 0) {
            if (++this.count == 2) {
                this.checkAllowsMany(Loc.NONE);
            }
            this.checkItemType(NodeKindTest.DOCUMENT, this.nodeSupplier((short)9, null, null, ""), Loc.NONE);
        }
        ++this.level;
        this.getNextOutputter().startDocument(properties);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType elemType, Location location, int properties) throws XPathException {
        this.checkElementStart(elemName, elemType, location);
        this.getNextOutputter().startElement(elemName, elemType, location, properties);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.checkElementStart(elemName, type, location);
        this.getNextOutputter().startElement(elemName, type, attributes, namespaces, location, properties);
    }

    private void checkElementStart(NodeName elemName, SchemaType elemType, Location location) throws XPathException {
        Configuration config = this.getConfiguration();
        NamePool namePool = config.getNamePool();
        if (this.level == 0) {
            if (++this.count == 1) {
                CombinedNodeTest type = new CombinedNodeTest(new NameTest(1, elemName, namePool), 23, new ContentTypeTest(1, elemType, config, false));
                this.checkItemType(type, this.nodeSupplier((short)1, elemName, elemType, ""), location);
            } else {
                long key;
                if (this.count == 2) {
                    this.checkAllowsMany(location);
                }
                if (!this.checkedElements.contains(key = (long)elemName.obtainFingerprint(namePool) << 32 | (long)elemType.getFingerprint())) {
                    CombinedNodeTest type = new CombinedNodeTest(new NameTest(1, elemName, namePool), 23, new ContentTypeTest(1, elemType, config, false));
                    this.checkItemType(type, this.nodeSupplier((short)1, elemName, elemType, ""), location);
                    this.checkedElements.add(key);
                }
            }
        }
        ++this.level;
    }

    @Override
    public void endDocument() throws XPathException {
        --this.level;
        this.getNextOutputter().endDocument();
    }

    @Override
    public void endElement() throws XPathException {
        --this.level;
        this.getNextOutputter().endElement();
    }

    @Override
    public void close() throws XPathException {
        this.finalCheck();
        super.close();
    }

    public void finalCheck() throws XPathException {
        if (this.count == 0 && !Cardinality.allowsZero(this.cardinality)) {
            XPathException err = new XPathException("An empty sequence is not allowed as the " + this.role.getMessage());
            String errorCode = this.role.getErrorCode();
            err.setErrorCode(errorCode);
            if (!"XPDY0050".equals(errorCode)) {
                err.setIsTypeError(true);
            }
            throw err;
        }
    }

    private Supplier<NodeInfo> nodeSupplier(short nodeKind, NodeName name, SchemaType type, CharSequence value) {
        return () -> {
            Orphan o = new Orphan(this.getPipelineConfiguration().getConfiguration());
            o.setNodeKind(nodeKind);
            if (name != null) {
                o.setNodeName(name);
            }
            o.setTypeAnnotation(type);
            o.setStringValue(value);
            return o;
        };
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        if (this.level == 0) {
            if (++this.count == 2) {
                this.checkAllowsMany(locationId);
            }
            this.checkItem(item, locationId);
        }
        this.getNextOutputter().append(item, locationId, copyNamespaces);
    }

    @Override
    public void append(Item item) throws XPathException {
        if (this.level == 0) {
            if (++this.count == 2) {
                this.checkAllowsMany(Loc.NONE);
            }
            this.checkItem(item, Loc.NONE);
        }
        this.getNextOutputter().append(item);
    }

    @Override
    public boolean usesTypeAnnotations() {
        return true;
    }

    private void checkItemType(ItemType type, Supplier<? extends Item> itemSupplier, Location locationId) throws XPathException {
        if (!this.typeHierarchy.isSubType(type, this.itemType)) {
            this.throwTypeError(type, itemSupplier == null ? null : itemSupplier.get(), locationId);
        }
    }

    private void checkItem(Item item, Location locationId) throws XPathException {
        if (!this.itemType.matches(item, this.typeHierarchy)) {
            this.throwTypeError(null, item, locationId);
        }
    }

    private void throwTypeError(ItemType suppliedType, Item item, Location locationId) throws XPathException {
        String message = item == null ? this.role.composeErrorMessage(this.itemType, suppliedType) : this.role.composeErrorMessage(this.itemType, item, this.typeHierarchy);
        String errorCode = this.role.getErrorCode();
        XPathException err = new XPathException(message);
        err.setErrorCode(errorCode);
        if (!"XPDY0050".equals(errorCode)) {
            err.setIsTypeError(true);
        }
        if (locationId == null) {
            err.setLocation(this.locator);
        } else {
            err.setLocation(locationId.saveLocation());
        }
        throw err;
    }

    private void checkAllowsMany(Location locationId) throws XPathException {
        if (!Cardinality.allowsMany(this.cardinality)) {
            XPathException err = new XPathException("A sequence of more than one item is not allowed as the " + this.role.getMessage());
            String errorCode = this.role.getErrorCode();
            err.setErrorCode(errorCode);
            if (!"XPDY0050".equals(errorCode)) {
                err.setIsTypeError(true);
            }
            if (locationId == null || locationId == Loc.NONE) {
                err.setLocator(this.locator);
            } else {
                err.setLocator(locationId);
            }
            throw err;
        }
    }
}

