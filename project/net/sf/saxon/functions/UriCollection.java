/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.ResourceCollection;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.LazySequence;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;

public class UriCollection
extends SystemFunction {
    private SequenceIterator getUris(String href, XPathContext context) throws XPathException {
        ResourceCollection rCollection = context.getConfiguration().getCollectionFinder().findCollection(context, href);
        if (rCollection == null) {
            XPathException err = new XPathException("No collection has been defined for href: " + (href == null ? "" : href));
            err.setErrorCode("FODC0002");
            err.setXPathContext(context);
            throw err;
        }
        final Iterator<String> sources = rCollection.getResourceURIs(context);
        return new SequenceIterator(){

            @Override
            public AnyURIValue next() {
                if (sources.hasNext()) {
                    return new AnyURIValue((CharSequence)sources.next());
                }
                return null;
            }

            @Override
            public void close() {
                if (sources instanceof Closeable) {
                    try {
                        ((Closeable)((Object)sources)).close();
                    } catch (IOException e) {
                        throw new UncheckedXPathException(new XPathException(e));
                    }
                }
            }
        };
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        URI hrefURI;
        if (arguments.length == 0) {
            return this.getDefaultUriCollection(context);
        }
        Item arg = arguments[0].head();
        if (arg == null) {
            return this.getDefaultUriCollection(context);
        }
        String href = arg.getStringValue();
        try {
            hrefURI = new URI(href);
        } catch (URISyntaxException e) {
            throw new XPathException("Invalid URI passed to uri-collection: " + href, "FODC0004");
        }
        if (!hrefURI.isAbsolute()) {
            URI staticBaseUri = this.getRetainedStaticContext().getStaticBaseUri();
            if (staticBaseUri == null) {
                throw new XPathException("No base URI available for uri-collection", "FODC0002");
            }
            hrefURI = staticBaseUri.resolve(hrefURI);
        }
        return new LazySequence(this.getUris(hrefURI.toString(), context));
    }

    private Sequence getDefaultUriCollection(XPathContext context) throws XPathException {
        String href = context.getConfiguration().getDefaultCollection();
        if (href == null) {
            throw new XPathException("No default collection has been defined", "FODC0002");
        }
        return new LazySequence(this.getUris(href, context));
    }
}

