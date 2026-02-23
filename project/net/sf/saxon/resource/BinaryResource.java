/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.resource;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.lib.ResourceFactory;
import net.sf.saxon.resource.AbstractResourceCollection;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Base64BinaryValue;

public class BinaryResource
implements Resource {
    private final String href;
    private final String contentType;
    private byte[] data;
    private URLConnection connection = null;
    public static final ResourceFactory FACTORY = (config, details) -> new BinaryResource(details);

    public BinaryResource(AbstractResourceCollection.InputDetails in) {
        this.contentType = in.contentType;
        this.href = in.resourceUri;
        this.data = in.binaryContent;
    }

    public BinaryResource(String href, String contentType, byte[] content) {
        this.contentType = contentType;
        this.href = href;
        this.data = content;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return this.data;
    }

    @Override
    public String getResourceURI() {
        return this.href;
    }

    private byte[] readBinaryFromConn(URLConnection con) throws XPathException {
        InputStream raw = null;
        this.connection = con;
        try {
            int offset;
            raw = this.connection.getInputStream();
            int contentLength = this.connection.getContentLength();
            BufferedInputStream in = new BufferedInputStream(raw);
            if (contentLength < 0) {
                byte[] result = BinaryResource.readBinaryFromStream(in, this.connection.getURL().getPath());
                ((InputStream)in).close();
                return result;
            }
            byte[] data = new byte[contentLength];
            int bytesRead = 0;
            for (offset = 0; offset < contentLength && (bytesRead = ((InputStream)in).read(data, offset, data.length - offset)) != -1; offset += bytesRead) {
            }
            ((InputStream)in).close();
            if (offset != contentLength) {
                throw new XPathException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
            }
            return data;
        } catch (IOException e) {
            throw new XPathException(e);
        }
    }

    public static byte[] readBinaryFromStream(InputStream in, String path) throws XPathException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[16384];
        try {
            int nRead;
            while ((nRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new XPathException("Failed to read: " + path + " " + e);
        }
    }

    @Override
    public Base64BinaryValue getItem(XPathContext context) throws XPathException {
        if (this.data != null) {
            return new Base64BinaryValue(this.data);
        }
        if (this.connection != null) {
            this.data = this.readBinaryFromConn(this.connection);
            return new Base64BinaryValue(this.data);
        }
        try {
            URL url = new URI(this.href).toURL();
            this.connection = url.openConnection();
            this.data = this.readBinaryFromConn(this.connection);
            return new Base64BinaryValue(this.data);
        } catch (IOException | URISyntaxException e) {
            throw new XPathException(e);
        }
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }
}

