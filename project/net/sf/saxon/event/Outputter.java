/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceBindingSet;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.CharSequenceConsumer;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.value.StringValue;

public abstract class Outputter
implements Receiver {
    protected PipelineConfiguration pipelineConfiguration;
    protected String systemId = null;

    @Override
    public void setPipelineConfiguration(PipelineConfiguration pipe) {
        this.pipelineConfiguration = pipe;
    }

    @Override
    public PipelineConfiguration getPipelineConfiguration() {
        return this.pipelineConfiguration;
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
    public void open() throws XPathException {
    }

    @Override
    public abstract void startDocument(int var1) throws XPathException;

    @Override
    public abstract void endDocument() throws XPathException;

    @Override
    public void setUnparsedEntity(String name, String systemID, String publicID) throws XPathException {
    }

    public abstract void startElement(NodeName var1, SchemaType var2, Location var3, int var4) throws XPathException;

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.startElement(elemName, type, location, properties);
        for (NamespaceBinding ns : namespaces) {
            this.namespace(ns.getPrefix(), ns.getURI(), properties);
        }
        for (AttributeInfo att : attributes) {
            this.attribute(att.getNodeName(), att.getType(), att.getValue(), att.getLocation(), att.getProperties());
        }
        this.startContent();
    }

    public abstract void namespace(String var1, String var2, int var3) throws XPathException;

    public void namespaces(NamespaceBindingSet bindings, int properties) throws XPathException {
        for (NamespaceBinding nb : bindings) {
            this.namespace(nb.getPrefix(), nb.getURI(), properties);
        }
    }

    public abstract void attribute(NodeName var1, SimpleType var2, CharSequence var3, Location var4, int var5) throws XPathException;

    public void startContent() throws XPathException {
    }

    @Override
    public abstract void endElement() throws XPathException;

    @Override
    public abstract void characters(CharSequence var1, Location var2, int var3) throws XPathException;

    @Override
    public abstract void processingInstruction(String var1, CharSequence var2, Location var3, int var4) throws XPathException;

    @Override
    public abstract void comment(CharSequence var1, Location var2, int var3) throws XPathException;

    @Override
    public void append(Item item, Location locationId, int properties) throws XPathException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void append(Item item) throws XPathException {
        this.append(item, Loc.NONE, 524288);
    }

    public CharSequenceConsumer getStringReceiver(final boolean asTextNode) {
        return new CharSequenceConsumer(){
            FastStringBuffer buffer = new FastStringBuffer(256);

            @Override
            public CharSequenceConsumer cat(CharSequence chars) {
                return this.buffer.cat(chars);
            }

            @Override
            public CharSequenceConsumer cat(char c) {
                return this.buffer.cat(c);
            }

            @Override
            public void close() throws XPathException {
                if (asTextNode) {
                    Outputter.this.characters(this.buffer, Loc.NONE, 0);
                } else {
                    Outputter.this.append(new StringValue(this.buffer.condense()));
                }
            }
        };
    }

    @Override
    public void close() throws XPathException {
    }

    @Override
    public boolean usesTypeAnnotations() {
        return false;
    }
}

