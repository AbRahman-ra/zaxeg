/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.lib.ResourceFactory;
import net.sf.saxon.lib.StandardUnparsedTextResolver;
import net.sf.saxon.om.Item;
import net.sf.saxon.resource.AbstractResourceCollection;
import net.sf.saxon.resource.CatalogCollection;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class UnparsedTextResource
implements Resource {
    private String contentType;
    private String encoding;
    private String href;
    private String unparsedText = null;
    public static final ResourceFactory FACTORY = new ResourceFactory(){

        @Override
        public Resource makeResource(Configuration config, AbstractResourceCollection.InputDetails details) throws XPathException {
            return new UnparsedTextResource(details);
        }
    };

    public UnparsedTextResource(AbstractResourceCollection.InputDetails details) throws XPathException {
        this.href = details.resourceUri;
        this.contentType = details.contentType;
        this.encoding = details.encoding;
        if (details.characterContent != null) {
            this.unparsedText = details.characterContent;
        } else if (details.binaryContent != null) {
            if (details.encoding == null) {
                try {
                    ByteArrayInputStream is = new ByteArrayInputStream(details.binaryContent);
                    details.encoding = StandardUnparsedTextResolver.inferStreamEncoding(is, null);
                    ((InputStream)is).close();
                } catch (IOException e) {
                    throw new XPathException(e);
                }
            }
            try {
                this.unparsedText = new String(details.binaryContent, details.encoding);
            } catch (UnsupportedEncodingException e) {
                throw new XPathException(e);
            }
        }
    }

    @Override
    public String getResourceURI() {
        return this.href;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public String getContent() throws XPathException {
        if (this.unparsedText == null) {
            try {
                URL url = new URL(this.href);
                URLConnection connection = url.openConnection();
                InputStream stream = connection.getInputStream();
                StringBuilder builder = null;
                String enc = this.encoding;
                if (enc == null) {
                    enc = StandardUnparsedTextResolver.inferStreamEncoding(stream, null);
                }
                builder = CatalogCollection.makeStringBuilderFromStream(stream, enc);
                this.unparsedText = builder.toString();
            } catch (IOException e) {
                throw new XPathException(e);
            }
        }
        return this.unparsedText;
    }

    @Override
    public Item getItem(XPathContext context) throws XPathException {
        return new StringValue(this.getContent());
    }

    @Override
    public String getContentType() {
        return this.contentType == null ? "text/plain" : this.contentType;
    }
}

