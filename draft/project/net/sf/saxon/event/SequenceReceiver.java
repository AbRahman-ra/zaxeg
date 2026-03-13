/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.Orphan;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.ExternalObject;

public abstract class SequenceReceiver
implements Receiver {
    protected boolean previousAtomic = false;
    protected PipelineConfiguration pipelineConfiguration;
    protected String systemId = null;

    public SequenceReceiver(PipelineConfiguration pipe) {
        this.pipelineConfiguration = pipe;
    }

    @Override
    public final PipelineConfiguration getPipelineConfiguration() {
        return this.pipelineConfiguration;
    }

    @Override
    public void setPipelineConfiguration(PipelineConfiguration pipelineConfiguration) {
        this.pipelineConfiguration = pipelineConfiguration;
    }

    public final Configuration getConfiguration() {
        return this.pipelineConfiguration.getConfiguration();
    }

    @Override
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    @Override
    public void setUnparsedEntity(String name, String systemID, String publicID) throws XPathException {
    }

    @Override
    public void open() throws XPathException {
        this.previousAtomic = false;
    }

    @Override
    public abstract void append(Item var1, Location var2, int var3) throws XPathException;

    @Override
    public void append(Item item) throws XPathException {
        this.append(item, Loc.NONE, 524288);
    }

    public NamePool getNamePool() {
        return this.pipelineConfiguration.getConfiguration().getNamePool();
    }

    protected void flatten(ArrayItem array, Location locationId, int copyNamespaces) throws XPathException {
        for (Sequence sequence : array.members()) {
            sequence.iterate().forEachOrFail(it -> this.append(it, locationId, copyNamespaces));
        }
    }

    protected void decompose(Item item, Location locationId, int copyNamespaces) throws XPathException {
        if (item != null) {
            if (item instanceof AtomicValue || item instanceof ExternalObject) {
                if (this.previousAtomic) {
                    this.characters(" ", locationId, 0);
                }
                this.characters(item.getStringValueCS(), locationId, 0);
                this.previousAtomic = true;
            } else if (item instanceof ArrayItem) {
                this.flatten((ArrayItem)item, locationId, copyNamespaces);
            } else {
                if (item instanceof Function) {
                    String thing = item instanceof MapItem ? "map" : "function item";
                    String errorCode = this.getErrorCodeForDecomposingFunctionItems();
                    if (errorCode.startsWith("SENR")) {
                        throw new XPathException("Cannot serialize a " + thing + " using this output method", errorCode, locationId);
                    }
                    throw new XPathException("Cannot add a " + thing + " to an XDM node tree", errorCode, locationId);
                }
                NodeInfo node = (NodeInfo)item;
                int kind = node.getNodeKind();
                if (node instanceof Orphan && ((Orphan)node).isDisableOutputEscaping()) {
                    this.characters(item.getStringValueCS(), locationId, 1);
                    this.previousAtomic = false;
                } else if (kind == 9) {
                    this.startDocument(0);
                    for (NodeInfo nodeInfo : node.children()) {
                        this.append(nodeInfo, locationId, copyNamespaces);
                    }
                    this.previousAtomic = false;
                    this.endDocument();
                } else {
                    if (kind == 2 || kind == 13) {
                        String thing = kind == 2 ? "an attribute" : "a namespace";
                        throw new XPathException("Sequence normalization: Cannot process " + thing + " node", "SENR0001", locationId);
                    }
                    int copyOptions = 4;
                    if (ReceiverOption.contains(copyNamespaces, 524288)) {
                        copyOptions |= 2;
                    }
                    ((NodeInfo)item).copy(this, copyOptions, locationId);
                    this.previousAtomic = false;
                }
            }
        }
    }

    protected String getErrorCodeForDecomposingFunctionItems() {
        return this.getPipelineConfiguration().isXSLT() ? "XTDE0450" : "XQTY0105";
    }

    @Override
    public boolean handlesAppend() {
        return true;
    }
}

