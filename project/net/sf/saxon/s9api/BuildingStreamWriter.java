/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import javax.xml.stream.XMLStreamWriter;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public interface BuildingStreamWriter
extends XMLStreamWriter {
    public XdmNode getDocumentNode() throws SaxonApiException;

    public void setCheckValues(boolean var1);

    public boolean isCheckValues();
}

