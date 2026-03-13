/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import javax.xml.transform.Result;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public interface Receiver
extends Result {
    public void setPipelineConfiguration(PipelineConfiguration var1);

    public PipelineConfiguration getPipelineConfiguration();

    @Override
    public void setSystemId(String var1);

    public void open() throws XPathException;

    public void startDocument(int var1) throws XPathException;

    public void endDocument() throws XPathException;

    public void setUnparsedEntity(String var1, String var2, String var3) throws XPathException;

    public void startElement(NodeName var1, SchemaType var2, AttributeMap var3, NamespaceMap var4, Location var5, int var6) throws XPathException;

    public void endElement() throws XPathException;

    public void characters(CharSequence var1, Location var2, int var3) throws XPathException;

    public void processingInstruction(String var1, CharSequence var2, Location var3, int var4) throws XPathException;

    public void comment(CharSequence var1, Location var2, int var3) throws XPathException;

    default public void append(Item item, Location locationId, int properties) throws XPathException {
        throw new UnsupportedOperationException();
    }

    default public void append(Item item) throws XPathException {
        this.append(item, Loc.NONE, 524288);
    }

    public void close() throws XPathException;

    default public boolean usesTypeAnnotations() {
        return false;
    }

    default public boolean handlesAppend() {
        return false;
    }
}

