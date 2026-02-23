/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.URIQueryParameters;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.resource.AbstractResourceCollection;
import net.sf.saxon.resource.FailedResource;
import net.sf.saxon.resource.MetadataResource;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.StringValue;

public class JarCollection
extends AbstractResourceCollection {
    private XPathContext context;
    private String collectionURI;
    private SpaceStrippingRule whitespaceRules;

    public JarCollection(XPathContext context, String collectionURI, URIQueryParameters params) {
        super(context.getConfiguration());
        this.context = context;
        this.collectionURI = collectionURI;
        this.params = params;
    }

    @Override
    public boolean stripWhitespace(SpaceStrippingRule rules) {
        this.whitespaceRules = rules;
        return true;
    }

    @Override
    public String getCollectionURI() {
        return this.collectionURI;
    }

    @Override
    public Iterator<String> getResourceURIs(XPathContext context) throws XPathException {
        FilenameFilter filter = null;
        boolean recurse = false;
        if (this.params != null) {
            Boolean r;
            FilenameFilter f = this.params.getFilenameFilter();
            if (f != null) {
                filter = f;
            }
            if ((r = this.params.getRecurse()) != null) {
                recurse = r;
            }
        }
        ZipInputStream zipInputStream = this.getZipInputStream();
        ArrayList<String> result = new ArrayList<String>();
        try {
            ZipEntry entry;
            String dirStr = "";
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    dirStr = entry.getName();
                }
                if (!entry.isDirectory()) {
                    String entryName = entry.getName();
                    if (filter != null) {
                        if (dirStr.equals("") || !entryName.contains(dirStr)) {
                            dirStr = entryName.contains("/") ? entryName.substring(0, entryName.lastIndexOf("/")) : "";
                        }
                        if (filter.accept(new File(dirStr), entryName)) {
                            result.add(this.makeResourceURI(entryName));
                        }
                    } else {
                        result.add(this.makeResourceURI(entryName));
                    }
                }
                entry = zipInputStream.getNextEntry();
            }
        } catch (IOException e) {
            throw new XPathException("Unable to extract entry in JAR/ZIP file: " + this.collectionURI, e);
        }
        return result.iterator();
    }

    private ZipInputStream getZipInputStream() throws XPathException {
        InputStream stream;
        URLConnection connection;
        URL url;
        try {
            url = new URL(this.collectionURI);
        } catch (MalformedURLException e) {
            throw new XPathException("Malformed JAR/ZIP file URI: " + this.collectionURI, e);
        }
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            throw new XPathException("Unable to open connection to JAR/ZIP file URI: " + this.collectionURI, e);
        }
        try {
            stream = connection.getInputStream();
        } catch (IOException e) {
            throw new XPathException("Unable to get input stream for JAR/ZIP file connection: " + this.collectionURI, e);
        }
        return new ZipInputStream(stream);
    }

    public Iterator<Resource> getResources(XPathContext context) throws XPathException {
        FilenameFilter filter = null;
        boolean recurse = false;
        if (this.params != null) {
            Boolean r;
            FilenameFilter f = this.params.getFilenameFilter();
            if (f != null) {
                filter = f;
            }
            if ((r = this.params.getRecurse()) != null) {
                recurse = r;
            }
        }
        ZipInputStream zipInputStream = this.getZipInputStream();
        return new JarIterator(this.context, zipInputStream, filter);
    }

    private String makeResourceURI(String entryName) {
        return (this.collectionURI.startsWith("jar:") ? "" : "jar:") + this.collectionURI + "!/" + entryName;
    }

    protected Map<String, GroundedValue> makeProperties(ZipEntry entry) {
        HashMap<String, GroundedValue> map = new HashMap<String, GroundedValue>(10);
        map.put("comment", StringValue.makeStringValue(entry.getComment()));
        map.put("compressed-size", new Int64Value(entry.getCompressedSize()));
        map.put("crc", new Int64Value(entry.getCrc()));
        byte[] extra = entry.getExtra();
        if (extra != null) {
            map.put("extra", new Base64BinaryValue(extra));
        }
        map.put("compression-method", new Int64Value(entry.getMethod()));
        map.put("entry-name", StringValue.makeStringValue(entry.getName()));
        map.put("size", new Int64Value(entry.getSize()));
        try {
            map.put("last-modified", DateTimeValue.fromJavaTime(entry.getTime()));
        } catch (XPathException xPathException) {
            // empty catch block
        }
        return map;
    }

    private class JarIterator
    implements Iterator<Resource>,
    Closeable {
        private FilenameFilter filter;
        private Resource next = null;
        private XPathContext context;
        private ZipInputStream zipInputStream;
        private String dirStr = "";
        private ParseOptions options;
        private boolean metadata;

        public JarIterator(XPathContext context, ZipInputStream zipInputStream, FilenameFilter filter) {
            this.context = context;
            this.filter = filter;
            this.zipInputStream = zipInputStream;
            this.options = JarCollection.this.optionsFromQueryParameters(JarCollection.this.params, context);
            this.options.setSpaceStrippingRule(JarCollection.this.whitespaceRules);
            Boolean metadataParam = JarCollection.this.params == null ? null : JarCollection.this.params.getMetaData();
            this.metadata = metadataParam != null && metadataParam != false;
            this.advance();
        }

        @Override
        public boolean hasNext() {
            boolean more;
            boolean bl = more = this.next != null;
            if (!more) {
                try {
                    this.zipInputStream.close();
                } catch (IOException e) {
                    throw new UncheckedXPathException(new XPathException(e));
                }
            }
            return more;
        }

        @Override
        public Resource next() {
            Resource current = this.next;
            this.advance();
            return current;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void advance() {
            while (true) {
                ZipEntry entry;
                try {
                    entry = this.zipInputStream.getNextEntry();
                    if (entry == null) {
                        this.next = null;
                        return;
                    }
                } catch (IOException e) {
                    this.next = new FailedResource(null, new XPathException(e));
                    break;
                }
                if (entry.isDirectory()) {
                    this.dirStr = entry.getName();
                    continue;
                }
                String entryName = entry.getName();
                if (this.filter != null) {
                    if (this.dirStr.equals("") || !entryName.contains(this.dirStr)) {
                        this.dirStr = entryName.contains("/") ? entryName.substring(0, entryName.lastIndexOf("/")) : "";
                    }
                    if (!this.filter.accept(new File(this.dirStr), entryName)) continue;
                }
                String resourceURI = null;
                try {
                    ZipInputStream is = this.zipInputStream;
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    try {
                        byte[] buffer = new byte[4096];
                        int len = 0;
                        while ((len = ((InputStream)is).read(buffer)) > 0) {
                            output.write(buffer, 0, len);
                        }
                    } catch (IOException err) {
                        throw new UncheckedXPathException(new XPathException(err));
                    } finally {
                        try {
                            output.close();
                        } catch (IOException e) {
                            this.next = new FailedResource(null, new XPathException(e));
                        }
                    }
                    AbstractResourceCollection.InputDetails details = new AbstractResourceCollection.InputDetails();
                    details.binaryContent = output.toByteArray();
                    details.contentType = JarCollection.this.params != null && JarCollection.this.params.getContentType() != null ? JarCollection.this.params.getContentType() : JarCollection.this.guessContentTypeFromName(entry.getName());
                    if (details.contentType == null) {
                        ByteArrayInputStream bais = new ByteArrayInputStream(details.binaryContent);
                        details.contentType = JarCollection.this.guessContentTypeFromContent(bais);
                        try {
                            bais.close();
                        } catch (IOException e) {
                            details.contentType = null;
                        }
                    }
                    details.parseOptions = this.options;
                    details.resourceUri = resourceURI = JarCollection.this.makeResourceURI(entry.getName());
                    this.next = JarCollection.this.makeResource(this.context.getConfiguration(), details);
                    if (this.metadata) {
                        Map<String, GroundedValue> properties = JarCollection.this.makeProperties(entry);
                        this.next = new MetadataResource(resourceURI, this.next, properties);
                    }
                    return;
                } catch (XPathException e) {
                    this.next = new FailedResource(resourceURI, e);
                    continue;
                }
                break;
            }
        }

        @Override
        public void close() throws IOException {
            this.zipInputStream.close();
        }
    }
}

