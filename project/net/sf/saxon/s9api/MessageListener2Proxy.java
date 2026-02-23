/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.event.SequenceWriter;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.MessageListener2;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

class MessageListener2Proxy
extends SequenceWriter {
    private MessageListener2 listener;
    private boolean terminate;
    private Location locationId;
    private StructuredQName errorCode;

    protected MessageListener2Proxy(MessageListener2 listener, PipelineConfiguration pipe) {
        super(pipe);
        this.setTreeModel(TreeModel.LINKED_TREE);
        this.listener = listener;
    }

    public MessageListener2 getMessageListener() {
        return this.listener;
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        this.terminate = ReceiverOption.contains(properties, 16384);
        this.locationId = null;
        this.errorCode = null;
        super.startDocument(properties);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        if (this.locationId == null) {
            this.locationId = location;
        }
        super.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public void characters(CharSequence s, Location locationId, int properties) throws XPathException {
        if (this.locationId == null) {
            this.locationId = locationId;
        }
        super.characters(s, locationId, properties);
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        if (target.equals("error-code") && this.errorCode == null) {
            this.errorCode = StructuredQName.fromEQName(data);
        } else {
            super.processingInstruction(target, data, locationId, properties);
        }
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        if (this.locationId == null) {
            this.locationId = locationId;
        }
        super.append(item, locationId, copyNamespaces);
    }

    @Override
    public void write(Item item) throws XPathException {
        Location loc = this.locationId == null ? Loc.NONE : this.locationId.saveLocation();
        this.listener.message(new XdmNode((NodeInfo)item), new QName(this.errorCode), this.terminate, loc);
    }
}

