/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.lib.ResourceFactory;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.resource.AbstractResourceCollection;
import net.sf.saxon.trans.XPathException;

public class XmlResource
implements Resource {
    private NodeInfo doc;
    private Configuration config;
    private AbstractResourceCollection.InputDetails details;
    public static final ResourceFactory FACTORY = new ResourceFactory(){

        @Override
        public Resource makeResource(Configuration config, AbstractResourceCollection.InputDetails details) throws XPathException {
            return new XmlResource(config, details);
        }
    };

    public XmlResource(NodeInfo doc) {
        this.config = doc.getConfiguration();
        this.doc = doc;
    }

    public XmlResource(Configuration config, NodeInfo doc) {
        this.config = config;
        this.doc = doc;
        if (config != doc.getConfiguration()) {
            throw new IllegalArgumentException("Supplied node belongs to wrong configuration");
        }
    }

    public XmlResource(Configuration config, AbstractResourceCollection.InputDetails details) {
        this.config = config;
        this.details = details;
    }

    @Override
    public String getResourceURI() {
        if (this.doc == null) {
            return this.details.resourceUri;
        }
        return this.doc.getSystemId();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Item getItem(XPathContext context) throws XPathException {
        if (this.doc == null) {
            StreamSource source;
            String resourceURI = this.details.resourceUri;
            ParseOptions options = this.details.parseOptions;
            if (options == null) {
                options = this.config.getParseOptions();
            }
            if (this.details.characterContent != null) {
                source = new StreamSource(new StringReader(this.details.characterContent), resourceURI);
            } else if (this.details.binaryContent != null) {
                source = new StreamSource(new ByteArrayInputStream(this.details.binaryContent), resourceURI);
            } else {
                try {
                    InputStream stream = this.details.getInputStream();
                    source = new StreamSource(stream, resourceURI);
                } catch (IOException e) {
                    throw new XPathException(e);
                }
            }
            try {
                this.doc = this.config.buildDocumentTree(source, options).getRootNode();
            } catch (XPathException e) {
                if (this.details.onError == 1) {
                    XPathException e2 = new XPathException("collection(): failed to parse XML file " + source.getSystemId() + ": " + e.getMessage(), e.getErrorCodeLocalPart());
                    throw e2;
                }
                if (this.details.onError == 2) {
                    context.getController().warning("collection(): failed to parse XML file " + source.getSystemId() + ": " + e.getMessage(), e.getErrorCodeLocalPart(), null);
                }
                this.doc = null;
            } finally {
                if (source != null && source.getInputStream() != null) {
                    try {
                        source.getInputStream().close();
                    } catch (IOException iOException) {}
                }
            }
        }
        return this.doc;
    }

    @Override
    public String getContentType() {
        return "application/xml";
    }
}

