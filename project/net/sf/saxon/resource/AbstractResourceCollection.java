/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.resource;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.URIQueryParameters;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.lib.ResourceCollection;
import net.sf.saxon.lib.ResourceFactory;
import net.sf.saxon.lib.StandardErrorReporter;
import net.sf.saxon.lib.StandardUnparsedTextResolver;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.resource.BinaryResource;
import net.sf.saxon.resource.CatalogCollection;
import net.sf.saxon.resource.UnparsedTextResource;
import net.sf.saxon.trans.Maker;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingIncident;
import org.xml.sax.XMLReader;

public abstract class AbstractResourceCollection
implements ResourceCollection {
    protected Configuration config;
    protected String collectionURI;
    protected URIQueryParameters params = null;

    public AbstractResourceCollection(Configuration config) {
        this.config = config;
    }

    @Override
    public String getCollectionURI() {
        return this.collectionURI;
    }

    @Override
    public boolean isStable(XPathContext context) {
        if (this.params == null) {
            return false;
        }
        Boolean stable = this.params.getStable();
        if (stable == null) {
            return context.getConfiguration().getBooleanProperty(Feature.STABLE_COLLECTION_URI);
        }
        return stable;
    }

    public void registerContentType(String contentType, ResourceFactory factory) {
        this.config.registerMediaType(contentType, factory);
    }

    protected ParseOptions optionsFromQueryParameters(URIQueryParameters params, XPathContext context) {
        ParseOptions options = new ParseOptions(context.getConfiguration().getParseOptions());
        if (params != null) {
            Controller controller;
            Maker<XMLReader> p;
            SpaceStrippingRule stripSpace;
            Boolean xInclude;
            Integer v = params.getValidationMode();
            if (v != null) {
                options.setSchemaValidationMode(v);
            }
            if ((xInclude = params.getXInclude()) != null) {
                options.setXIncludeAware(xInclude);
            }
            if ((stripSpace = params.getSpaceStrippingRule()) != null) {
                options.setSpaceStrippingRule(stripSpace);
            }
            if ((p = params.getXMLReaderMaker()) != null) {
                options.setXMLReaderMaker(p);
            }
            int onError = 1;
            if (params.getOnError() != null) {
                onError = params.getOnError();
            }
            ErrorReporter oldErrorListener = (controller = context.getController()) == null ? new StandardErrorReporter() : controller.getErrorReporter();
            AbstractResourceCollection.setupErrorHandlingForCollection(options, onError, oldErrorListener);
        }
        return options;
    }

    public static void setupErrorHandlingForCollection(ParseOptions options, int onError, ErrorReporter oldErrorListener) {
        if (onError == 3) {
            options.setErrorReporter(error -> {});
        } else if (onError == 2) {
            options.setErrorReporter(error -> {
                if (error.isWarning()) {
                    oldErrorListener.report(error);
                } else {
                    oldErrorListener.report(error.asWarning());
                    XmlProcessingIncident supp = new XmlProcessingIncident("The document will be excluded from the collection").asWarning();
                    supp.setLocation(error.getLocation());
                    oldErrorListener.report(supp);
                }
            });
        }
    }

    protected InputDetails getInputDetails(String resourceURI) throws XPathException {
        InputDetails inputDetails = new InputDetails();
        try {
            inputDetails.resourceUri = resourceURI;
            URI uri = new URI(resourceURI);
            if ("file".equals(uri.getScheme())) {
                inputDetails.contentType = this.params != null && this.params.getContentType() != null ? this.params.getContentType() : this.guessContentTypeFromName(resourceURI);
            } else {
                URL url = uri.toURL();
                URLConnection connection = url.openConnection();
                inputDetails.contentType = connection.getContentType();
                inputDetails.encoding = connection.getContentEncoding();
                for (String param : inputDetails.contentType.replace(" ", "").split(";")) {
                    if (param.startsWith("charset=")) {
                        inputDetails.encoding = param.split("=", 2)[1];
                        continue;
                    }
                    inputDetails.contentType = param;
                }
            }
            if (inputDetails.contentType == null || this.config.getResourceFactoryForMediaType(inputDetails.contentType) == null) {
                InputStream stream;
                if ("file".equals(uri.getScheme())) {
                    File file = new File(uri);
                    stream = new BufferedInputStream(new FileInputStream(file));
                    if (file.length() <= 1024L) {
                        inputDetails.binaryContent = BinaryResource.readBinaryFromStream(stream, resourceURI);
                        stream.close();
                        stream = new ByteArrayInputStream(inputDetails.binaryContent);
                    }
                } else {
                    URL url = uri.toURL();
                    URLConnection connection = url.openConnection();
                    stream = connection.getInputStream();
                }
                inputDetails.contentType = this.guessContentTypeFromContent(stream);
                stream.close();
            }
            if (this.params != null && this.params.getOnError() != null) {
                inputDetails.onError = this.params.getOnError();
            }
            return inputDetails;
        } catch (IOException | URISyntaxException e) {
            throw new XPathException(e);
        }
    }

    protected String guessContentTypeFromName(String resourceURI) {
        String contentTypeFromName = URLConnection.guessContentTypeFromName(resourceURI);
        String extension = null;
        if (contentTypeFromName == null && (extension = this.getFileExtension(resourceURI)) != null) {
            contentTypeFromName = this.config.getMediaTypeForFileExtension(extension);
        }
        return contentTypeFromName;
    }

    protected String guessContentTypeFromContent(InputStream stream) {
        try {
            if (!stream.markSupported()) {
                stream = new BufferedInputStream(stream);
            }
            return URLConnection.guessContentTypeFromStream(stream);
        } catch (IOException err) {
            return null;
        }
    }

    private String getFileExtension(String name) {
        int p;
        int i = name.lastIndexOf(46);
        if (i > (p = Math.max(name.lastIndexOf(47), name.lastIndexOf(92))) && i + 1 < name.length()) {
            return name.substring(i + 1);
        }
        return null;
    }

    public Resource makeResource(Configuration config, InputDetails details) throws XPathException {
        ResourceFactory factory = null;
        String contentType = details.contentType;
        if (contentType != null) {
            factory = config.getResourceFactoryForMediaType(contentType);
        }
        if (factory == null) {
            factory = BinaryResource.FACTORY;
        }
        return factory.makeResource(config, details);
    }

    public Resource makeTypedResource(Configuration config, Resource basicResource) throws XPathException {
        String mediaType = basicResource.getContentType();
        ResourceFactory factory = config.getResourceFactoryForMediaType(mediaType);
        if (factory == null) {
            return basicResource;
        }
        if (basicResource instanceof BinaryResource) {
            InputDetails details = new InputDetails();
            details.binaryContent = ((BinaryResource)basicResource).getData();
            details.contentType = mediaType;
            details.resourceUri = basicResource.getResourceURI();
            return factory.makeResource(config, details);
        }
        if (basicResource instanceof UnparsedTextResource) {
            InputDetails details = new InputDetails();
            details.characterContent = ((UnparsedTextResource)basicResource).getContent();
            details.contentType = mediaType;
            details.resourceUri = basicResource.getResourceURI();
            return factory.makeResource(config, details);
        }
        return basicResource;
    }

    public Resource makeResource(Configuration config, String resourceURI) throws XPathException {
        InputDetails details = this.getInputDetails(resourceURI);
        return this.makeResource(config, details);
    }

    @Override
    public boolean stripWhitespace(SpaceStrippingRule rules) {
        return false;
    }

    public static class InputDetails {
        public String resourceUri;
        public byte[] binaryContent;
        public String characterContent;
        public String contentType;
        public String encoding;
        public ParseOptions parseOptions;
        public int onError = 1;

        public InputStream getInputStream() throws IOException {
            URL url = new URL(this.resourceUri);
            URLConnection connection = url.openConnection();
            return connection.getInputStream();
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public byte[] obtainBinaryContent() throws XPathException {
            if (this.binaryContent != null) {
                return this.binaryContent;
            }
            if (this.characterContent != null) {
                String e = this.encoding != null ? this.encoding : "UTF-8";
                try {
                    return this.characterContent.getBytes(e);
                } catch (UnsupportedEncodingException ex) {
                    throw new XPathException(e);
                }
            }
            try (InputStream stream = this.getInputStream();){
                byte[] byArray = BinaryResource.readBinaryFromStream(stream, this.resourceUri);
                return byArray;
            } catch (IOException e) {
                throw new XPathException(e);
            }
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public String obtainCharacterContent() throws XPathException {
            if (this.characterContent != null) {
                return this.characterContent;
            }
            if (this.binaryContent != null && this.encoding != null) {
                try {
                    return new String(this.binaryContent, this.encoding);
                } catch (UnsupportedEncodingException e) {
                    throw new XPathException(e);
                }
            }
            try (InputStream stream = this.getInputStream();){
                StringBuilder builder = null;
                String enc = this.encoding;
                if (enc == null) {
                    enc = StandardUnparsedTextResolver.inferStreamEncoding(stream, null);
                }
                builder = CatalogCollection.makeStringBuilderFromStream(stream, enc);
                String string = this.characterContent = builder.toString();
                return string;
            } catch (IOException e) {
                throw new XPathException(e);
            }
        }
    }
}

