/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.io.LineNumberReader;
import java.io.Reader;
import java.net.URI;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.TextLinesIterator;

public class UnparsedTextIterator
extends TextLinesIterator {
    XPathContext context;
    String encoding = null;

    public UnparsedTextIterator(URI absoluteURI, XPathContext context, String encoding, Location location) throws XPathException {
        Configuration config = context.getConfiguration();
        Reader reader = context.getController().getUnparsedTextURIResolver().resolve(absoluteURI, encoding, config);
        this.reader = new LineNumberReader(reader);
        this.uri = absoluteURI;
        this.context = context;
        this.checker = context.getConfiguration().getValidCharacterChecker();
        this.encoding = encoding;
        this.location = location;
    }

    public UnparsedTextIterator(LineNumberReader reader, URI absoluteURI, XPathContext context, String encoding) throws XPathException {
        this.reader = reader;
        this.uri = absoluteURI;
        this.context = context;
        this.checker = context.getConfiguration().getValidCharacterChecker();
        this.encoding = encoding;
        this.location = null;
    }
}

