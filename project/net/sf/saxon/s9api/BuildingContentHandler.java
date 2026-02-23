/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.xml.sax.ContentHandler;

public interface BuildingContentHandler
extends ContentHandler {
    public XdmNode getDocumentNode() throws SaxonApiException;
}

