/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;

public final class TinyAttributeImpl
extends TinyNodeImpl {
    public TinyAttributeImpl(TinyTree tree, int nodeNr) {
        this.tree = tree;
        this.nodeNr = nodeNr;
    }

    @Override
    public void setSystemId(String uri) {
    }

    @Override
    public String getSystemId() {
        TinyNodeImpl parent = this.getParent();
        return parent == null ? null : this.getParent().getSystemId();
    }

    @Override
    public TinyNodeImpl getParent() {
        return this.tree.getNode(this.tree.attParent[this.nodeNr]);
    }

    @Override
    public NodeInfo getRoot() {
        TinyNodeImpl parent = this.getParent();
        if (parent == null) {
            return this;
        }
        return parent.getRoot();
    }

    @Override
    protected long getSequenceNumber() {
        return this.getParent().getSequenceNumber() + 32768L + (long)(this.nodeNr - this.tree.alpha[this.tree.attParent[this.nodeNr]]);
    }

    @Override
    public final int getNodeKind() {
        return 2;
    }

    @Override
    public CharSequence getStringValueCS() {
        return this.tree.attValue[this.nodeNr];
    }

    @Override
    public String getStringValue() {
        return this.tree.attValue[this.nodeNr].toString();
    }

    @Override
    public int getFingerprint() {
        return this.tree.attCode[this.nodeNr] & 0xFFFFF;
    }

    public int getNameCode() {
        return this.tree.attCode[this.nodeNr];
    }

    @Override
    public String getPrefix() {
        int code = this.tree.attCode[this.nodeNr];
        if (!NamePool.isPrefixed(code)) {
            return "";
        }
        return this.tree.prefixPool.getPrefix(code >> 20);
    }

    @Override
    public String getDisplayName() {
        int code = this.tree.attCode[this.nodeNr];
        if (code < 0) {
            return "";
        }
        if (NamePool.isPrefixed(code)) {
            return this.getPrefix() + ":" + this.getLocalPart();
        }
        return this.getLocalPart();
    }

    @Override
    public String getLocalPart() {
        return this.tree.getNamePool().getLocalName(this.tree.attCode[this.nodeNr]);
    }

    @Override
    public final String getURI() {
        int code = this.tree.attCode[this.nodeNr];
        if (!NamePool.isPrefixed(code)) {
            return "";
        }
        return this.tree.getNamePool().getURI(code);
    }

    @Override
    public SchemaType getSchemaType() {
        if (this.tree.attType == null) {
            return BuiltInAtomicType.UNTYPED_ATOMIC;
        }
        return this.tree.getAttributeType(this.nodeNr);
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        return this.tree.getTypedValueOfAttribute(this, this.nodeNr);
    }

    @Override
    public void generateId(FastStringBuffer buffer) {
        this.getParent().generateId(buffer);
        buffer.append("a");
        buffer.append(Integer.toString(this.tree.attCode[this.nodeNr]));
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) {
        throw new UnsupportedOperationException("copy() applied to attribute node");
    }

    @Override
    public int getLineNumber() {
        return this.getParent().getLineNumber();
    }

    @Override
    public int getColumnNumber() {
        return this.getParent().getColumnNumber();
    }

    @Override
    public boolean isId() {
        return this.tree.isIdAttribute(this.nodeNr);
    }

    @Override
    public boolean isIdref() {
        return this.tree.isIdrefAttribute(this.nodeNr);
    }

    public boolean isDefaultedAttribute() {
        return this.tree.isDefaultedAttribute(this.nodeNr);
    }

    @Override
    public int hashCode() {
        return (int)(this.tree.getDocumentNumber() & 0x3FFL) << 20 ^ this.nodeNr ^ 0xE0000;
    }
}

