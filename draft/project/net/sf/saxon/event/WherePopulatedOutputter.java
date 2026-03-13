/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.ProxyOutputter;
import net.sf.saxon.expr.instruct.WherePopulated;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;

public class WherePopulatedOutputter
extends ProxyOutputter {
    private int level = 0;
    private boolean pendingStartTag = false;
    private NodeName pendingElemName;
    private SchemaType pendingSchemaType;
    private Location pendingLocationId;
    private int pendingProperties;
    private AttributeMap pendingAttributes;
    private NamespaceMap pendingNamespaces;

    public WherePopulatedOutputter(Outputter next) {
        super(next);
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        if (this.level++ == 0) {
            this.pendingStartTag = true;
            this.pendingElemName = null;
            this.pendingProperties = properties;
        } else {
            super.startDocument(properties);
        }
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, Location location, int properties) throws XPathException {
        this.releaseStartTag();
        if (this.level++ == 0) {
            this.pendingStartTag = true;
            this.pendingElemName = elemName;
            this.pendingSchemaType = type;
            this.pendingLocationId = location.saveLocation();
            this.pendingProperties = properties;
            this.pendingAttributes = EmptyAttributeMap.getInstance();
            this.pendingNamespaces = NamespaceMap.emptyMap();
        } else {
            super.startElement(elemName, type, location, properties);
        }
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.releaseStartTag();
        if (this.level++ == 0) {
            this.pendingStartTag = true;
            this.pendingElemName = elemName;
            this.pendingSchemaType = type;
            this.pendingLocationId = location.saveLocation();
            this.pendingProperties = properties;
            this.pendingAttributes = attributes;
            this.pendingNamespaces = namespaces;
        } else {
            super.startElement(elemName, type, attributes, namespaces, location, properties);
        }
    }

    @Override
    public void namespace(String prefix, String namespaceUri, int properties) throws XPathException {
        if (this.level == 1) {
            this.pendingNamespaces = this.pendingNamespaces.put(prefix, namespaceUri);
        } else {
            super.namespace(prefix, namespaceUri, properties);
        }
    }

    @Override
    public void attribute(NodeName attName, SimpleType typeCode, CharSequence value, Location location, int properties) throws XPathException {
        if (this.level == 1) {
            this.pendingAttributes = this.pendingAttributes.put(new AttributeInfo(attName, typeCode, value.toString(), location, properties));
        } else if (this.level != 0 || value.length() != 0) {
            super.attribute(attName, typeCode, value, location, properties);
        }
    }

    @Override
    public void endDocument() throws XPathException {
        if (--this.level == 0) {
            if (!this.pendingStartTag) {
                super.endDocument();
            }
        } else {
            super.endDocument();
        }
    }

    @Override
    public void endElement() throws XPathException {
        if (--this.level == 0) {
            if (!this.pendingStartTag) {
                super.endElement();
            }
        } else {
            super.endElement();
        }
        this.pendingStartTag = false;
    }

    public void releaseStartTag() throws XPathException {
        if (this.level >= 1 && this.pendingStartTag) {
            if (this.pendingElemName == null) {
                this.getNextOutputter().startDocument(this.pendingProperties);
            } else {
                this.getNextOutputter().startElement(this.pendingElemName, this.pendingSchemaType, this.pendingAttributes, this.pendingNamespaces, this.pendingLocationId, this.pendingProperties);
            }
            this.pendingStartTag = false;
        }
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.level == 0) {
            if (chars.length() > 0) {
                super.characters(chars, locationId, properties);
            }
        } else if (this.level == 1) {
            if (chars.length() > 0) {
                this.releaseStartTag();
                super.characters(chars, locationId, properties);
            }
        } else {
            super.characters(chars, locationId, properties);
        }
    }

    @Override
    public void processingInstruction(String name, CharSequence data, Location location, int properties) throws XPathException {
        if (this.level == 0) {
            if (data.length() > 0) {
                super.processingInstruction(name, data, location, properties);
            }
        } else if (this.level == 1) {
            if (data.length() > 0) {
                this.releaseStartTag();
                super.processingInstruction(name, data, location, properties);
            }
        } else {
            super.processingInstruction(name, data, location, properties);
        }
    }

    @Override
    public void comment(CharSequence content, Location location, int properties) throws XPathException {
        if (this.level == 0) {
            if (content.length() > 0) {
                super.comment(content, location, properties);
            }
        } else if (this.level == 1) {
            if (content.length() > 0) {
                this.releaseStartTag();
                super.comment(content, location, properties);
            }
        } else {
            super.comment(content, location, properties);
        }
    }

    @Override
    public void append(Item item) throws XPathException {
        if (this.level == 0) {
            if (!WherePopulated.isDeemedEmpty(item)) {
                this.getNextOutputter().append(item);
            }
        } else if (this.level == 1 && this.pendingStartTag) {
            if (item instanceof NodeInfo) {
                NodeInfo node = (NodeInfo)item;
                switch (node.getNodeKind()) {
                    case 3: {
                        if (node.getNodeKind() != 3 || node.getStringValueCS().length() != 0) break;
                        return;
                    }
                    case 9: {
                        if (node.getNodeKind() != 9 || node.hasChildNodes()) break;
                        return;
                    }
                    case 2: {
                        this.attribute(NameOfNode.makeName(node), (SimpleType)node.getSchemaType(), node.getStringValue(), Loc.NONE, 0);
                        return;
                    }
                    case 13: {
                        this.namespace(node.getLocalPart(), node.getStringValue(), 0);
                        return;
                    }
                }
            }
            this.releaseStartTag();
            this.getNextOutputter().append(item);
        } else {
            super.append(item);
        }
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        if (this.level == 0) {
            if (!WherePopulated.isDeemedEmpty(item)) {
                this.getNextOutputter().append(item, locationId, copyNamespaces);
            }
        } else if (this.level == 1 && this.pendingStartTag) {
            if (item instanceof NodeInfo) {
                NodeInfo node = (NodeInfo)item;
                switch (node.getNodeKind()) {
                    case 3: {
                        if (node.getNodeKind() != 3 || node.getStringValueCS().length() != 0) break;
                        return;
                    }
                    case 9: {
                        if (node.getNodeKind() != 9 || node.hasChildNodes()) break;
                        return;
                    }
                    case 2: {
                        this.attribute(NameOfNode.makeName(node), (SimpleType)node.getSchemaType(), node.getStringValue(), locationId, 0);
                        return;
                    }
                    case 13: {
                        this.namespace(node.getLocalPart(), node.getStringValue(), 0);
                        return;
                    }
                }
            }
            this.releaseStartTag();
            this.getNextOutputter().append(item, locationId, copyNamespaces);
        } else {
            super.append(item, locationId, copyNamespaces);
        }
    }
}

