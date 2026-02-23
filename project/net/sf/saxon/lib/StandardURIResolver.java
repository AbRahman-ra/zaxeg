/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Predicate;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.Platform;
import net.sf.saxon.Version;
import net.sf.saxon.event.FilterFactory;
import net.sf.saxon.event.IDFilter;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.functions.EncodeForUri;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.functions.URIQueryParameters;
import net.sf.saxon.lib.AugmentedSource;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.resource.BinaryResource;
import net.sf.saxon.resource.DataURIScheme;
import net.sf.saxon.resource.UnparsedTextResource;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.Maker;
import net.sf.saxon.trans.NonDelegatingURIResolver;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class StandardURIResolver
implements NonDelegatingURIResolver {
    private Configuration config = null;
    private boolean recognizeQueryParameters = false;
    private Predicate<URI> allowedUriTest = null;

    public StandardURIResolver() {
        this(null);
    }

    public StandardURIResolver(Configuration config) {
        this.config = config;
    }

    public void setRecognizeQueryParameters(boolean recognize) {
        this.recognizeQueryParameters = recognize;
    }

    public boolean queryParametersAreRecognized() {
        return this.recognizeQueryParameters;
    }

    public void setAllowedUriTest(Predicate<URI> test) {
        this.allowedUriTest = test;
    }

    public Predicate<URI> getAllowedUriTest() {
        return this.allowedUriTest == null ? (this.config == null ? uri -> true : this.config.getAllowedUriTest()) : this.allowedUriTest;
    }

    protected Platform getPlatform() {
        return Version.platform;
    }

    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    @Override
    public Source resolve(String href, String base) throws XPathException {
        Boolean xinclude;
        Integer validation;
        Maker<XMLReader> parser;
        URI uri;
        URI relative;
        if (this.config != null && this.config.isTiming()) {
            assert (this.config != null);
            this.config.getLogger().info("URIResolver.resolve href=\"" + href + "\" base=\"" + base + "\"");
        }
        String relativeURI = href;
        String id = null;
        int hash = href.indexOf(35);
        if (hash >= 0) {
            relativeURI = href.substring(0, hash);
            id = href.substring(hash + 1);
        }
        URIQueryParameters params = null;
        try {
            relativeURI = ResolveURI.escapeSpaces(relativeURI);
            relative = new URI(relativeURI);
        } catch (URISyntaxException err) {
            throw new XPathException("Invalid relative URI " + Err.wrap(relativeURI), err);
        }
        String query = relative.getQuery();
        if (query != null && this.recognizeQueryParameters) {
            params = new URIQueryParameters(query, this.config);
            int q = relativeURI.indexOf(63);
            relativeURI = relativeURI.substring(0, q);
        }
        Source source = null;
        if (this.recognizeQueryParameters && relativeURI.endsWith(".ptree")) {
            throw new UnsupportedOperationException("PTree files are no longer supported (from Saxon 10.0)");
        }
        try {
            uri = ResolveURI.makeAbsolute(relativeURI, base);
        } catch (URISyntaxException err) {
            String expandedBase = ResolveURI.tryToExpand(base);
            if (!expandedBase.equals(base)) {
                return this.resolve(href, expandedBase);
            }
            throw new XPathException("Invalid URI " + Err.wrap(relativeURI) + " - base " + Err.wrap(base), err);
        }
        if (!this.getAllowedUriTest().test(uri)) {
            throw new XPathException("URI '" + uri.toString() + "' has been disallowed", "FODC0002");
        }
        String uriString = uri.toString();
        EncodeForUri.checkPercentEncoding(uriString);
        if ("data".equals(uri.getScheme())) {
            Resource resource;
            try {
                resource = DataURIScheme.decode(uri);
            } catch (IllegalArgumentException e) {
                throw new XPathException("Invalid URI using 'data' scheme: " + e.getMessage());
            }
            if (resource instanceof BinaryResource) {
                byte[] contents = ((BinaryResource)resource).getData();
                InputSource is = new InputSource(new ByteArrayInputStream(contents));
                source = new SAXSource(is);
                source.setSystemId(uriString);
            } else {
                assert (resource instanceof UnparsedTextResource);
                StringReader reader = new StringReader(((UnparsedTextResource)resource).getContent());
                source = new SAXSource(new InputSource(reader));
                source.setSystemId(uriString);
            }
        } else {
            source = new SAXSource();
            this.setSAXInputSource((SAXSource)source, uriString);
        }
        if (params != null && (parser = params.getXMLReaderMaker()) != null) {
            ((SAXSource)source).setXMLReader(parser.make());
        }
        if (((SAXSource)source).getXMLReader() == null && this.config == null) {
            try {
                ((SAXSource)source).setXMLReader(Version.platform.loadParser());
            } catch (Exception err) {
                throw new XPathException(err);
            }
        }
        if (params != null) {
            SpaceStrippingRule stripSpace = params.getSpaceStrippingRule();
            source = AugmentedSource.makeAugmentedSource(source);
            ((AugmentedSource)source).getParseOptions().setSpaceStrippingRule(stripSpace);
        }
        if (id != null) {
            final String idFinal = id;
            FilterFactory factory = new FilterFactory(){

                @Override
                public ProxyReceiver makeFilter(Receiver next) {
                    return new IDFilter(next, idFinal);
                }
            };
            source = AugmentedSource.makeAugmentedSource(source);
            ((AugmentedSource)source).addFilter(factory);
        }
        if (params != null && (validation = params.getValidationMode()) != null) {
            source = AugmentedSource.makeAugmentedSource(source);
            ((AugmentedSource)source).setSchemaValidationMode(validation);
        }
        if (params != null && (xinclude = params.getXInclude()) != null) {
            source = AugmentedSource.makeAugmentedSource(source);
            ((AugmentedSource)source).setXIncludeAware(xinclude);
        }
        return source;
    }

    protected Source getPTreeSource(String href, String base) throws XPathException {
        throw new XPathException("PTree files can only be read using a Saxon-EE configuration");
    }

    protected void setSAXInputSource(SAXSource source, String uriString) {
        InputStream is;
        if (uriString.startsWith("classpath:") && uriString.length() > 10 && (is = this.getConfiguration().getDynamicLoader().getResourceAsStream(uriString.substring(10))) != null) {
            source.setInputSource(new InputSource(is));
            source.setSystemId(uriString);
            return;
        }
        source.setInputSource(new InputSource(uriString));
        source.setSystemId(uriString);
    }
}

