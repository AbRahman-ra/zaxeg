/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.resource;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;

public class FailedResource
implements Resource {
    private String uri;
    private XPathException error;

    public FailedResource(String uri, XPathException error) {
        this.uri = uri;
        this.error = error;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getResourceURI() {
        return this.uri;
    }

    @Override
    public Item getItem(XPathContext context) throws XPathException {
        throw this.error;
    }

    public XPathException getError() {
        return this.error;
    }
}

