/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree;

import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.tree.util.Navigator;

public class AttributeLocation
implements Location {
    private String systemId;
    private int lineNumber;
    private int columnNumber;
    private StructuredQName elementName;
    private StructuredQName attributeName;
    private NodeInfo elementNode;

    public AttributeLocation(NodeInfo element, StructuredQName attributeName) {
        this.systemId = element.getSystemId();
        this.lineNumber = element.getLineNumber();
        this.columnNumber = element.getColumnNumber();
        this.elementName = Navigator.getNodeName(element);
        this.attributeName = attributeName;
        if (element.getConfiguration().getBooleanProperty(Feature.RETAIN_NODE_FOR_DIAGNOSTICS)) {
            this.elementNode = element;
        }
    }

    public AttributeLocation(StructuredQName elementName, StructuredQName attributeName, Location location) {
        this.systemId = location.getSystemId();
        this.lineNumber = location.getLineNumber();
        this.columnNumber = location.getColumnNumber();
        this.elementName = elementName;
        this.attributeName = attributeName;
    }

    public void setElementNode(NodeInfo node) {
        this.elementNode = node;
    }

    public NodeInfo getElementNode() {
        return this.elementNode;
    }

    public StructuredQName getElementName() {
        return this.elementName;
    }

    public StructuredQName getAttributeName() {
        return this.attributeName;
    }

    @Override
    public int getColumnNumber() {
        return this.columnNumber;
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    @Override
    public String getPublicId() {
        return null;
    }

    @Override
    public int getLineNumber() {
        return this.lineNumber;
    }

    @Override
    public Location saveLocation() {
        return this;
    }
}

