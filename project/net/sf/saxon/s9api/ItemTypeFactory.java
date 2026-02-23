/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.util.Map;
import net.sf.saxon.Configuration;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.CombinedNodeTest;
import net.sf.saxon.pattern.ContentTypeTest;
import net.sf.saxon.pattern.DocumentNodeTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.SameNameTest;
import net.sf.saxon.s9api.ConstructedItemType;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmMap;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.ExternalObjectType;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SchemaDeclaration;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.ExternalObject;
import net.sf.saxon.value.ObjectValue;

public class ItemTypeFactory {
    private Processor processor;

    public ItemTypeFactory(Processor processor) {
        this.processor = processor;
    }

    public ItemType getAtomicType(QName name) throws SaxonApiException {
        return this.getAtomicType(name.getStructuredQName());
    }

    private ItemType getAtomicType(StructuredQName name) throws SaxonApiException {
        String uri = name.getURI();
        String local = name.getLocalPart();
        if ("http://www.w3.org/2001/XMLSchema".equals(uri)) {
            int fp = StandardNames.getFingerprint(uri, local);
            Configuration config = this.processor.getUnderlyingConfiguration();
            if (config.getXsdVersion() == 10 && config.getXMLVersion() == 10) {
                return this.getBuiltInAtomicType(fp);
            }
            return ItemType.BuiltInAtomicItemType.makeVariant((ItemType.BuiltInAtomicItemType)this.getBuiltInAtomicType(fp), config.getConversionRules());
        }
        Configuration config = this.processor.getUnderlyingConfiguration();
        SchemaType type = config.getSchemaType(new StructuredQName("", uri, local));
        if (type == null || !type.isAtomicType()) {
            throw new SaxonApiException("Unknown atomic type " + name.getClarkName());
        }
        return new ConstructedItemType((AtomicType)type, this.processor);
    }

    private ItemType getBuiltInAtomicType(int fp) throws SaxonApiException {
        switch (fp) {
            case 632: {
                return ItemType.ANY_ATOMIC_VALUE;
            }
            case 513: {
                return ItemType.STRING;
            }
            case 514: {
                return ItemType.BOOLEAN;
            }
            case 518: {
                return ItemType.DURATION;
            }
            case 519: {
                return ItemType.DATE_TIME;
            }
            case 521: {
                return ItemType.DATE;
            }
            case 520: {
                return ItemType.TIME;
            }
            case 522: {
                return ItemType.G_YEAR_MONTH;
            }
            case 526: {
                return ItemType.G_MONTH;
            }
            case 524: {
                return ItemType.G_MONTH_DAY;
            }
            case 523: {
                return ItemType.G_YEAR;
            }
            case 525: {
                return ItemType.G_DAY;
            }
            case 527: {
                return ItemType.HEX_BINARY;
            }
            case 528: {
                return ItemType.BASE64_BINARY;
            }
            case 529: {
                return ItemType.ANY_URI;
            }
            case 530: {
                return ItemType.QNAME;
            }
            case 531: {
                return ItemType.NOTATION;
            }
            case 631: {
                return ItemType.UNTYPED_ATOMIC;
            }
            case 515: {
                return ItemType.DECIMAL;
            }
            case 516: {
                return ItemType.FLOAT;
            }
            case 517: {
                return ItemType.DOUBLE;
            }
            case 533: {
                return ItemType.INTEGER;
            }
            case 534: {
                return ItemType.NON_POSITIVE_INTEGER;
            }
            case 535: {
                return ItemType.NEGATIVE_INTEGER;
            }
            case 536: {
                return ItemType.LONG;
            }
            case 537: {
                return ItemType.INT;
            }
            case 538: {
                return ItemType.SHORT;
            }
            case 539: {
                return ItemType.BYTE;
            }
            case 540: {
                return ItemType.NON_NEGATIVE_INTEGER;
            }
            case 541: {
                return ItemType.POSITIVE_INTEGER;
            }
            case 542: {
                return ItemType.UNSIGNED_LONG;
            }
            case 543: {
                return ItemType.UNSIGNED_INT;
            }
            case 544: {
                return ItemType.UNSIGNED_SHORT;
            }
            case 545: {
                return ItemType.UNSIGNED_BYTE;
            }
            case 633: {
                return ItemType.YEAR_MONTH_DURATION;
            }
            case 634: {
                return ItemType.DAY_TIME_DURATION;
            }
            case 553: {
                return ItemType.NORMALIZED_STRING;
            }
            case 554: {
                return ItemType.TOKEN;
            }
            case 555: {
                return ItemType.LANGUAGE;
            }
            case 558: {
                return ItemType.NAME;
            }
            case 556: {
                return ItemType.NMTOKEN;
            }
            case 559: {
                return ItemType.NCNAME;
            }
            case 560: {
                return ItemType.ID;
            }
            case 561: {
                return ItemType.IDREF;
            }
            case 563: {
                return ItemType.ENTITY;
            }
            case 565: {
                return ItemType.DATE_TIME_STAMP;
            }
        }
        throw new SaxonApiException("Unknown atomic type " + this.processor.getUnderlyingConfiguration().getNamePool().getClarkName(fp));
    }

    public ItemType getNodeKindTest(XdmNodeKind kind) {
        switch (kind) {
            case DOCUMENT: {
                return ItemType.DOCUMENT_NODE;
            }
            case ELEMENT: {
                return ItemType.ELEMENT_NODE;
            }
            case ATTRIBUTE: {
                return ItemType.ATTRIBUTE_NODE;
            }
            case TEXT: {
                return ItemType.TEXT_NODE;
            }
            case COMMENT: {
                return ItemType.COMMENT_NODE;
            }
            case PROCESSING_INSTRUCTION: {
                return ItemType.PROCESSING_INSTRUCTION_NODE;
            }
            case NAMESPACE: {
                return ItemType.NAMESPACE_NODE;
            }
        }
        throw new IllegalArgumentException("XdmNodeKind");
    }

    public ItemType getItemType(XdmNodeKind kind, QName name) {
        int k = kind.getNumber();
        if (k == 1 || k == 2 || k == 7) {
            if (k == 7 && name.getNamespaceURI().isEmpty()) {
                throw new IllegalArgumentException("The name of a processing instruction must not be in a namespace");
            }
            NameTest type = new NameTest(k, name.getNamespaceURI(), name.getLocalName(), this.processor.getUnderlyingConfiguration().getNamePool());
            return new ConstructedItemType(type, this.processor);
        }
        throw new IllegalArgumentException("Node kind must be element, attribute, or processing-instruction");
    }

    public ItemType getSchemaElementTest(QName name) throws SaxonApiException {
        Configuration config = this.processor.getUnderlyingConfiguration();
        SchemaDeclaration decl = config.getElementDeclaration(name.getStructuredQName());
        if (decl == null) {
            throw new SaxonApiException("No global declaration found for element " + name.getClarkName());
        }
        try {
            NodeTest test = decl.makeSchemaNodeTest();
            return new ConstructedItemType(test, this.processor);
        } catch (MissingComponentException e) {
            throw new SaxonApiException(e);
        }
    }

    public ItemType getElementTest(QName name, QName schemaType, boolean nillable) throws SaxonApiException {
        Configuration config = this.processor.getUnderlyingConfiguration();
        NameTest nameTest = null;
        ContentTypeTest contentTest = null;
        if (name != null) {
            int elementFP = config.getNamePool().allocateFingerprint(name.getNamespaceURI(), name.getLocalName());
            nameTest = new NameTest(1, elementFP, config.getNamePool());
        }
        if (schemaType != null) {
            SchemaType type = config.getSchemaType(new StructuredQName("", schemaType.getNamespaceURI(), schemaType.getLocalName()));
            if (type == null) {
                throw new SaxonApiException("Unknown schema type " + schemaType.getClarkName());
            }
            contentTest = new ContentTypeTest(1, type, config, nillable);
        }
        if (contentTest == null) {
            if (nameTest == null) {
                return this.getNodeKindTest(XdmNodeKind.ELEMENT);
            }
            return new ConstructedItemType(nameTest, this.processor);
        }
        if (nameTest == null) {
            return new ConstructedItemType(contentTest, this.processor);
        }
        CombinedNodeTest combo = new CombinedNodeTest(nameTest, 23, contentTest);
        return new ConstructedItemType(combo, this.processor);
    }

    public ItemType getSchemaAttributeTest(QName name) throws SaxonApiException {
        StructuredQName nn;
        Configuration config = this.processor.getUnderlyingConfiguration();
        SchemaDeclaration decl = config.getAttributeDeclaration(nn = new StructuredQName("", name.getNamespaceURI(), name.getLocalName()));
        if (decl == null) {
            throw new SaxonApiException("No global declaration found for attribute " + name.getClarkName());
        }
        try {
            NodeTest test = decl.makeSchemaNodeTest();
            return new ConstructedItemType(test, this.processor);
        } catch (MissingComponentException e) {
            throw new SaxonApiException(e);
        }
    }

    public ItemType getAttributeTest(QName name, QName schemaType) throws SaxonApiException {
        NameTest nameTest = null;
        ContentTypeTest contentTest = null;
        Configuration config = this.processor.getUnderlyingConfiguration();
        if (name != null) {
            int attributeFP = config.getNamePool().allocateFingerprint(name.getNamespaceURI(), name.getLocalName());
            nameTest = new NameTest(2, attributeFP, config.getNamePool());
        }
        if (schemaType != null) {
            SchemaType type = config.getSchemaType(new StructuredQName("", schemaType.getNamespaceURI(), schemaType.getLocalName()));
            if (type == null) {
                throw new SaxonApiException("Unknown schema type " + schemaType.getClarkName());
            }
            contentTest = new ContentTypeTest(2, type, config, false);
        }
        if (contentTest == null) {
            if (nameTest == null) {
                return this.getNodeKindTest(XdmNodeKind.ATTRIBUTE);
            }
            return new ConstructedItemType(nameTest, this.processor);
        }
        if (nameTest == null) {
            return new ConstructedItemType(contentTest, this.processor);
        }
        CombinedNodeTest combo = new CombinedNodeTest(nameTest, 23, contentTest);
        return new ConstructedItemType(combo, this.processor);
    }

    public ItemType getDocumentTest(ItemType elementTest) {
        net.sf.saxon.type.ItemType test = elementTest.getUnderlyingItemType();
        if (test.getPrimitiveType() != 1) {
            throw new IllegalArgumentException("Supplied itemType is not an element test");
        }
        DocumentNodeTest docTest = new DocumentNodeTest((NodeTest)test);
        return new ConstructedItemType(docTest, this.processor);
    }

    public ItemType getExternalObjectType(Class externalClass) {
        JavaExternalObjectType type = this.processor.getUnderlyingConfiguration().getJavaExternalObjectType(externalClass);
        return new ConstructedItemType(type, this.processor);
    }

    public XdmItem getExternalObject(Object object) {
        return (XdmItem)XdmItem.wrap(new ObjectValue<Object>(object));
    }

    public ItemType getMapType(ItemType keyType, SequenceType valueType) {
        if (!(keyType.getUnderlyingItemType() instanceof AtomicType)) {
            throw new IllegalArgumentException("Map key must be atomic");
        }
        return new ConstructedItemType(new MapType((AtomicType)keyType.getUnderlyingItemType(), valueType.getUnderlyingSequenceType()), this.processor);
    }

    public ItemType getArrayType(SequenceType memberType) {
        return new ConstructedItemType(new ArrayItemType(memberType.getUnderlyingSequenceType()), this.processor);
    }

    public XdmMap newMap(Map<?, ?> map) throws SaxonApiException {
        try {
            return XdmMap.makeMap(map);
        } catch (IllegalArgumentException e) {
            throw new SaxonApiException(e);
        }
    }

    public ItemType getItemType(XdmItem item) {
        if (item.isAtomicValue()) {
            AtomicValue value = (AtomicValue)item.getUnderlyingValue();
            AtomicType type = value.getItemType();
            return new ConstructedItemType(type, this.processor);
        }
        if (item.isNode()) {
            NodeInfo node = (NodeInfo)item.getUnderlyingValue();
            int kind = node.getNodeKind();
            if (node.getLocalPart().isEmpty()) {
                return new ConstructedItemType(NodeKindTest.makeNodeKindTest(kind), this.processor);
            }
            return new ConstructedItemType(new SameNameTest(node), this.processor);
        }
        Item it = item.getUnderlyingValue();
        if (it instanceof MapItem) {
            return ItemType.ANY_MAP;
        }
        if (it instanceof ArrayItem) {
            return ItemType.ANY_ARRAY;
        }
        if (it instanceof ExternalObject) {
            return new ConstructedItemType(ExternalObjectType.THE_INSTANCE, this.processor);
        }
        return ItemType.ANY_FUNCTION;
    }

    public ItemType exposeItemType(net.sf.saxon.type.ItemType it) {
        return new ConstructedItemType(it, this.processor);
    }
}

