/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.resource;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.functions.URIQueryParameters;
import net.sf.saxon.lib.CollectionFinder;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.ResourceCollection;
import net.sf.saxon.regex.ARegularExpression;
import net.sf.saxon.resource.CatalogCollection;
import net.sf.saxon.resource.DirectoryCollection;
import net.sf.saxon.resource.JarCollection;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;

public class StandardCollectionFinder
implements CollectionFinder {
    private Map<String, ResourceCollection> registeredCollections = new HashMap<String, ResourceCollection>(2);

    public void registerCollection(String collectionURI, ResourceCollection collection) {
        this.registeredCollections.put(collectionURI, collection);
    }

    @Override
    public ResourceCollection findCollection(XPathContext context, String collectionURI) throws XPathException {
        String regex;
        URI resolvedURI;
        StandardCollectionFinder.checkNotNull(collectionURI, context);
        ResourceCollection registeredCollection = this.registeredCollections.get(collectionURI);
        if (registeredCollection != null) {
            return registeredCollection;
        }
        URIQueryParameters params = null;
        String query = null;
        try {
            URI relativeURI = new URI(ResolveURI.escapeSpaces(collectionURI));
            query = relativeURI.getQuery();
            if (query != null) {
                int q = collectionURI.indexOf(63);
                params = new URIQueryParameters(query, context.getConfiguration());
                collectionURI = ResolveURI.escapeSpaces(collectionURI.substring(0, q));
            }
        } catch (URISyntaxException e) {
            XPathException err = new XPathException("Invalid relative URI " + Err.wrap(collectionURI, 4) + " passed to collection() function");
            err.setErrorCode("FODC0004");
            err.setXPathContext(context);
            throw err;
        }
        try {
            resolvedURI = new URI(collectionURI);
        } catch (URISyntaxException e) {
            throw new XPathException(e);
        }
        if (!context.getConfiguration().getAllowedUriTest().test(resolvedURI)) {
            throw new XPathException("URI scheme '" + resolvedURI.getScheme() + "' has been disallowed");
        }
        if ("file".equals(resolvedURI.getScheme())) {
            File file = new File(resolvedURI);
            StandardCollectionFinder.checkFileExists(file, resolvedURI, context);
            if (file.isDirectory()) {
                return new DirectoryCollection(context.getConfiguration(), collectionURI, file, params);
            }
        }
        if ((regex = context.getConfiguration().getConfigurationProperty(Feature.ZIP_URI_PATTERN)) == null) {
            regex = "^jar:|\\.jar$|\\.zip$|\\.docx$";
        }
        if (this.isJarFileURI(collectionURI) || ARegularExpression.compile(regex, "").containsMatch(collectionURI)) {
            return new JarCollection(context, collectionURI, params);
        }
        return new CatalogCollection(context.getConfiguration(), collectionURI);
    }

    public static void checkNotNull(String collectionURI, XPathContext context) throws XPathException {
        if (collectionURI == null) {
            XPathException err = new XPathException("No default collection has been defined");
            err.setErrorCode("FODC0002");
            err.setXPathContext(context);
            throw err;
        }
    }

    protected boolean isJarFileURI(String collectionURI) {
        return collectionURI.endsWith(".jar") || collectionURI.endsWith(".zip") || collectionURI.endsWith(".docx") || collectionURI.startsWith("jar:");
    }

    public static void checkFileExists(File file, URI resolvedURI, XPathContext context) throws XPathException {
        if (!file.exists()) {
            XPathException err = new XPathException("The file or directory " + resolvedURI + " does not exist");
            err.setErrorCode("FODC0002");
            err.setXPathContext(context);
            throw err;
        }
    }
}

