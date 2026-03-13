/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.resource;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.lib.ResourceFactory;
import net.sf.saxon.om.Item;
import net.sf.saxon.resource.AbstractResourceCollection;
import net.sf.saxon.resource.BinaryResource;
import net.sf.saxon.trans.XPathException;

public class UnknownResource
implements Resource {
    private Configuration config;
    private AbstractResourceCollection.InputDetails details;
    public static final ResourceFactory FACTORY = UnknownResource::new;

    public UnknownResource(Configuration config, AbstractResourceCollection.InputDetails details) {
        this.config = config;
        this.details = details;
    }

    @Override
    public String getResourceURI() {
        return this.details.resourceUri;
    }

    @Override
    public Item getItem(XPathContext context) throws XPathException {
        String mediaType;
        InputStream stream;
        if (this.details.binaryContent != null) {
            stream = new ByteArrayInputStream(this.details.binaryContent);
        } else {
            try {
                stream = this.details.getInputStream();
            } catch (IOException e) {
                throw new XPathException(e);
            }
        }
        if (stream == null) {
            throw new XPathException("Unable to dereference resource URI " + this.details.resourceUri);
        }
        try {
            if (!stream.markSupported()) {
                stream = new BufferedInputStream(stream);
            }
            mediaType = URLConnection.guessContentTypeFromStream(stream);
        } catch (IOException e) {
            mediaType = null;
        }
        if (mediaType == null) {
            mediaType = this.config.getMediaTypeForFileExtension("");
        }
        if (mediaType == null || mediaType.equals("application/unknown")) {
            mediaType = "application/binary";
        }
        this.details.contentType = mediaType;
        this.details.binaryContent = BinaryResource.readBinaryFromStream(stream, this.details.resourceUri);
        ResourceFactory delegee = this.config.getResourceFactoryForMediaType(mediaType);
        Resource actual = delegee.makeResource(this.config, this.details);
        return actual.getItem(context);
    }

    @Override
    public String getContentType() {
        return "application/xml";
    }
}

