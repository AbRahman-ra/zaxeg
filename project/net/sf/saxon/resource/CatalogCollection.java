/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.transform.Source;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.DocumentFn;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.resource.AbstractResourceCollection;
import net.sf.saxon.resource.DataURIScheme;
import net.sf.saxon.resource.FailedResource;
import net.sf.saxon.resource.StandardCollectionFinder;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.jiter.MappingJavaIterator;

public class CatalogCollection
extends AbstractResourceCollection {
    private boolean stable;
    private SpaceStrippingRule whitespaceRules;

    public CatalogCollection(Configuration config, String collectionURI) {
        super(config);
        this.collectionURI = collectionURI;
    }

    @Override
    public Iterator<String> getResourceURIs(XPathContext context) throws XPathException {
        StandardCollectionFinder.checkNotNull(this.collectionURI, context);
        return this.catalogContents(this.collectionURI, context);
    }

    public Iterator<Resource> getResources(XPathContext context) throws XPathException {
        StandardCollectionFinder.checkNotNull(this.collectionURI, context);
        Iterator<String> resourceURIs = this.getResourceURIs(context);
        return new MappingJavaIterator<String, Resource>(resourceURIs, in -> {
            try {
                if (in.startsWith("data:")) {
                    try {
                        Resource basicResource = DataURIScheme.decode(new URI((String)in));
                        return this.makeTypedResource(context.getConfiguration(), basicResource);
                    } catch (IllegalArgumentException | URISyntaxException e) {
                        throw new XPathException(e);
                    }
                }
                AbstractResourceCollection.InputDetails id = this.getInputDetails((String)in);
                id.parseOptions = new ParseOptions(context.getConfiguration().getParseOptions());
                id.parseOptions.setSpaceStrippingRule(this.whitespaceRules);
                id.resourceUri = in;
                return this.makeResource(context.getConfiguration(), id);
            } catch (XPathException e) {
                int onError;
                int n = onError = this.params == null ? 1 : this.params.getOnError();
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

    @Override
    public boolean isStable(XPathContext context) {
        return this.stable;
    }

    public static StringBuilder makeStringBuilderFromStream(InputStream in, String encoding) throws IOException {
        InputStreamReader is = new InputStreamReader(in, Charset.forName(encoding));
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(is);
        String read = br.readLine();
        while (read != null) {
            sb.append(read);
            read = br.readLine();
        }
        br.close();
        return sb;
    }

    protected Iterator<String> catalogContents(String href, XPathContext context) throws XPathException {
        NodeInfo item;
        Source source = DocumentFn.resolveURI(href, null, null, context);
        ParseOptions options = new ParseOptions();
        options.setSchemaValidationMode(4);
        options.setDTDValidationMode(4);
        TreeInfo catalog = context.getConfiguration().buildDocumentTree(source, options);
        if (catalog == null) {
            XPathException err = new XPathException("Failed to load collection catalog " + href);
            err.setErrorCode("FODC0004");
            err.setXPathContext(context);
            throw err;
        }
        AxisIterator iter = catalog.getRootNode().iterateAxis(3, NodeKindTest.ELEMENT);
        NodeInfo top = iter.next();
        if (top == null || !"collection".equals(top.getLocalPart()) || !top.getURI().isEmpty()) {
            String message = top == null ? "No outermost element found in collection catalog" : "Outermost element of collection catalog should be Q{}collection (found Q{" + top.getURI() + "}" + top.getLocalPart() + ")";
            XPathException err = new XPathException(message);
            err.setErrorCode("FODC0004");
            err.setXPathContext(context);
            throw err;
        }
        iter.close();
        String stableAtt = top.getAttributeValue("", "stable");
        if (stableAtt != null) {
            if ("true".equals(stableAtt)) {
                this.stable = true;
            } else if ("false".equals(stableAtt)) {
                this.stable = false;
            } else {
                XPathException err = new XPathException("The 'stable' attribute of element <collection> must be true or false");
                err.setErrorCode("FODC0004");
                err.setXPathContext(context);
                throw err;
            }
        }
        AxisIterator documents = top.iterateAxis(3, NodeKindTest.ELEMENT);
        ArrayList<String> result = new ArrayList<String>();
        while ((item = documents.next()) != null) {
            String uri;
            if (!"doc".equals(item.getLocalPart()) || !item.getURI().isEmpty()) {
                XPathException err = new XPathException("Children of <collection> element must be <doc> elements");
                err.setErrorCode("FODC0004");
                err.setXPathContext(context);
                throw err;
            }
            String hrefAtt = item.getAttributeValue("", "href");
            if (hrefAtt == null) {
                XPathException err = new XPathException("A <doc> element in the collection catalog has no @href attribute");
                err.setErrorCode("FODC0004");
                err.setXPathContext(context);
                throw err;
            }
            try {
                uri = ResolveURI.makeAbsolute(hrefAtt, item.getBaseURI()).toString();
            } catch (URISyntaxException e) {
                XPathException err = new XPathException("Invalid base URI or href URI in collection catalog: (" + item.getBaseURI() + ", " + hrefAtt + ")");
                err.setErrorCode("FODC0004");
                err.setXPathContext(context);
                throw err;
            }
            result.add(uri);
        }
        return result.iterator();
    }
}

