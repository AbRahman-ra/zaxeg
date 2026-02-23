/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.resource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.URIQueryParameters;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.resource.AbstractResourceCollection;
import net.sf.saxon.resource.FailedResource;
import net.sf.saxon.resource.MetadataResource;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.jiter.MappingJavaIterator;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.StringValue;

public class DirectoryCollection
extends AbstractResourceCollection {
    private File dirFile;
    private SpaceStrippingRule whitespaceRules;

    public DirectoryCollection(Configuration config, String collectionURI, File file, URIQueryParameters params) throws XPathException {
        super(config);
        if (collectionURI == null) {
            throw new NullPointerException();
        }
        this.collectionURI = collectionURI;
        this.dirFile = file;
        this.params = params == null ? new URIQueryParameters("", config) : params;
    }

    @Override
    public boolean stripWhitespace(SpaceStrippingRule rules) {
        this.whitespaceRules = rules;
        return true;
    }

    @Override
    public Iterator<String> getResourceURIs(XPathContext context) {
        return this.directoryContents(this.dirFile, this.params);
    }

    public Iterator<Resource> getResources(XPathContext context) {
        ParseOptions options = this.optionsFromQueryParameters(this.params, context);
        options.setSpaceStrippingRule(this.whitespaceRules);
        Boolean metadataParam = this.params.getMetaData();
        boolean metadata = metadataParam != null && metadataParam != false;
        Iterator<String> resourceURIs = this.getResourceURIs(context);
        return new MappingJavaIterator<String, Resource>(resourceURIs, in -> {
            try {
                Resource resource;
                AbstractResourceCollection.InputDetails details = this.getInputDetails((String)in);
                details.resourceUri = in;
                details.parseOptions = options;
                if (this.params.getContentType() != null) {
                    details.contentType = this.params.getContentType();
                }
                if ((resource = this.makeResource(context.getConfiguration(), details)) != null) {
                    if (metadata) {
                        return this.makeMetadataResource(resource, details);
                    }
                    return resource;
                }
                return null;
            } catch (XPathException e) {
                int onError = this.params.getOnError();
                if (onError == 1) {
                    return new FailedResource((String)in, e);
                }
                if (onError == 2) {
                    context.getController().warning("collection(): failed to parse " + in + ": " + e.getMessage(), e.getErrorCodeLocalPart(), null);
                    return null;
                }
                return null;
            }
        });
    }

    private MetadataResource makeMetadataResource(Resource resource, AbstractResourceCollection.InputDetails details) {
        HashMap<String, GroundedValue> properties;
        block6: {
            properties = new HashMap<String, GroundedValue>();
            try {
                URI uri = new URI(resource.getResourceURI());
                if (details.contentType != null) {
                    properties.put("content-type", StringValue.makeStringValue(details.contentType));
                }
                if (details.encoding != null) {
                    properties.put("encoding", StringValue.makeStringValue(details.encoding));
                }
                if (!"file".equals(uri.getScheme())) break block6;
                File file = new File(uri);
                properties.put("path", StringValue.makeStringValue(file.getPath()));
                properties.put("absolute-path", StringValue.makeStringValue(file.getAbsolutePath()));
                properties.put("canonical-path", StringValue.makeStringValue(file.getCanonicalPath()));
                properties.put("can-read", BooleanValue.get(file.canRead()));
                properties.put("can-write", BooleanValue.get(file.canWrite()));
                properties.put("can-execute", BooleanValue.get(file.canExecute()));
                properties.put("is-hidden", BooleanValue.get(file.isHidden()));
                try {
                    properties.put("last-modified", DateTimeValue.fromJavaTime(file.lastModified()));
                } catch (XPathException xPathException) {
                    // empty catch block
                }
                properties.put("length", new Int64Value(file.length()));
            } catch (IOException | URISyntaxException exception) {
                // empty catch block
            }
        }
        return new MetadataResource(resource.getResourceURI(), resource, properties);
    }

    protected Iterator<String> directoryContents(File directory, URIQueryParameters params) {
        FilenameFilter filter = null;
        boolean recurse = false;
        if (params != null) {
            Boolean r;
            FilenameFilter f = params.getFilenameFilter();
            if (f != null) {
                filter = f;
            }
            if ((r = params.getRecurse()) != null) {
                recurse = r;
            }
        }
        Stack<Iterator<File>> directories = new Stack<Iterator<File>>();
        directories.push(Arrays.asList(directory.listFiles(filter)).iterator());
        return new DirectoryIterator(directories, recurse, filter);
    }

    private static class DirectoryIterator
    implements Iterator<String> {
        private Stack<Iterator<File>> directories;
        private FilenameFilter filter;
        private boolean recurse;
        private String next = null;

        public DirectoryIterator(Stack<Iterator<File>> directories, boolean recurse, FilenameFilter filter) {
            this.directories = directories;
            this.recurse = recurse;
            this.filter = filter;
            this.advance();
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public String next() {
            String s = this.next;
            this.advance();
            return s;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void advance() {
            if (this.directories.isEmpty()) {
                this.next = null;
            } else {
                Iterator<File> files = this.directories.peek();
                while (!files.hasNext()) {
                    this.directories.pop();
                    if (this.directories.isEmpty()) {
                        this.next = null;
                        return;
                    }
                    files = this.directories.peek();
                }
                File nextFile = files.next();
                if (nextFile.isDirectory()) {
                    if (this.recurse) {
                        this.directories.push(Arrays.asList(nextFile.listFiles(this.filter)).iterator());
                    }
                    this.advance();
                } else {
                    this.next = nextFile.toURI().toString();
                }
            }
        }
    }
}

