/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Sender;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.sort.DocumentOrderIterator;
import net.sf.saxon.expr.sort.GlobalOrderComparer;
import net.sf.saxon.functions.Doc;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.EmptySource;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.RelativeURIResolver;
import net.sf.saxon.lib.StandardErrorHandler;
import net.sf.saxon.om.DocumentKey;
import net.sf.saxon.om.DocumentPool;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.NonDelegatingURIResolver;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.value.Cardinality;

public class DocumentFn
extends SystemFunction
implements Callable {
    private Location location;

    @Override
    public int getCardinality(Expression[] arguments) {
        Expression expression = arguments[0];
        if (Cardinality.allowsMany(expression.getCardinality())) {
            return 57344;
        }
        return 24576;
    }

    @Override
    public int getSpecialProperties(Expression[] arguments) {
        return 0x8A0000;
    }

    @Override
    public Expression makeFunctionCall(Expression ... arguments) {
        this.location = arguments[0].getLocation();
        Expression expr = Doc.maybePreEvaluate(this, arguments);
        return expr == null ? super.makeFunctionCall(arguments) : expr;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        NodeInfo base;
        int numArgs = this.getArity();
        SequenceIterator hrefSequence = arguments[0].iterate();
        String baseURI = null;
        if (numArgs == 2 && (baseURI = (base = (NodeInfo)arguments[1].head()).getBaseURI()) == null) {
            throw new XPathException("The second argument to document() is a node with no base URI", "XTDE1162");
        }
        DocumentMappingFunction map = new DocumentMappingFunction(context);
        map.baseURI = baseURI;
        map.stylesheetURI = this.getStaticBaseUriString();
        map.packageData = this.getRetainedStaticContext().getPackageData();
        map.locator = this.location;
        ItemMappingIterator iter = new ItemMappingIterator(hrefSequence, map);
        return SequenceTool.toLazySequence(new DocumentOrderIterator(iter, GlobalOrderComparer.getInstance()));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NodeInfo makeDoc(String href, String baseURI, PackageData packageData, ParseOptions options, XPathContext c, Location locator, boolean silent) throws XPathException {
        Controller controller;
        Configuration config = c.getConfiguration();
        int hash = href.indexOf(35);
        String fragmentId = null;
        if (hash >= 0) {
            if (hash == href.length() - 1) {
                href = href.substring(0, hash);
            } else {
                fragmentId = href.substring(hash + 1);
                href = href.substring(0, hash);
                if (!NameChecker.isValidNCName(fragmentId)) {
                    XPathException de = new XPathException("The fragment identifier " + Err.wrap(fragmentId) + " is not a valid NCName");
                    de.setErrorCode("XTDE1160");
                    de.setXPathContext(c);
                    de.setLocator(locator);
                    throw de;
                }
            }
        }
        if ((controller = c.getController()) == null) {
            throw new XPathException("doc() function is not available in this environment");
        }
        DocumentKey documentKey = DocumentFn.computeDocumentKey(href, baseURI, packageData, c);
        TreeInfo doc = config.getGlobalDocumentPool().find(documentKey);
        if (doc != null) {
            return doc.getRootNode();
        }
        DocumentPool pool = controller.getDocumentPool();
        Controller controller2 = controller;
        synchronized (controller2) {
            doc = pool.find(documentKey);
            if (doc != null) {
                return DocumentFn.getFragment(doc, fragmentId, c, locator);
            }
            if (controller instanceof XsltController && !((XsltController)controller).checkUniqueOutputDestination(documentKey)) {
                pool.markUnavailable(documentKey);
                XPathException err = new XPathException("Cannot read a document that was written during the same transformation: " + documentKey);
                err.setXPathContext(c);
                err.setErrorCode("XTRE1500");
                err.setLocator(locator);
                throw err;
            }
            if (pool.isMarkedUnavailable(documentKey)) {
                XPathException err = new XPathException("Document has been marked not available: " + documentKey);
                err.setXPathContext(c);
                err.setErrorCode("FODC0002");
                err.setLocator(locator);
                throw err;
            }
        }
        try {
            Object b;
            TreeInfo newdoc;
            Source source = DocumentFn.resolveURI(href, baseURI, documentKey.toString(), c);
            if (source instanceof EmptySource) {
                return null;
            }
            source = config.getSourceResolver().resolveSource(source, config);
            if (source instanceof NodeInfo || source instanceof DOMSource) {
                NodeInfo startNode = controller.prepareInputTree(source);
                newdoc = startNode.getTreeInfo();
            } else {
                PathMap.PathMapRoot pathRoot;
                PathMap map;
                b = controller.makeBuilder();
                ((Builder)b).setUseEventLocation(true);
                if (b instanceof TinyBuilder) {
                    ((TinyBuilder)b).setStatistics(config.getTreeStatistics().SOURCE_DOCUMENT_STATISTICS);
                }
                Object s = b;
                if (options == null) {
                    SpaceStrippingRule rule;
                    options = new ParseOptions(((Builder)b).getPipelineConfiguration().getParseOptions());
                    if (packageData instanceof StylesheetPackage && (rule = ((StylesheetPackage)packageData).getSpaceStrippingRule()) != NoElementsSpaceStrippingRule.getInstance()) {
                        options.setSpaceStrippingRule(rule);
                    }
                    options.setSchemaValidationMode(controller.getSchemaValidationMode());
                }
                ((Builder)b).getPipelineConfiguration().setParseOptions(options);
                if (options.isLineNumbering()) {
                    ((Builder)b).setLineNumbering(true);
                }
                if (silent) {
                    StandardErrorHandler eh = new StandardErrorHandler(controller.getErrorReporter());
                    eh.setSilent(true);
                    options.setErrorHandler(eh);
                }
                if (packageData instanceof StylesheetPackage && ((StylesheetPackage)packageData).isStripsTypeAnnotations()) {
                    s = config.getAnnotationStripper((Receiver)s);
                }
                if ((map = controller.getPathMapForDocumentProjection()) != null && (pathRoot = map.getRootForDocument(documentKey.toString())) != null && !pathRoot.isReturnable() && !pathRoot.hasUnknownDependencies()) {
                    options.addFilter(config.makeDocumentProjector(pathRoot));
                }
                s.setPipelineConfiguration(((Builder)b).getPipelineConfiguration());
                try {
                    Sender.send(source, (Receiver)s, options);
                    newdoc = ((Builder)b).getCurrentRoot().getTreeInfo();
                    ((Builder)b).reset();
                } catch (XPathException err) {
                    if (err.getErrorCodeLocalPart() == null || err.getErrorCodeLocalPart().equals("SXXP0003")) {
                        err.setErrorCode("FODC0002");
                    }
                    throw err;
                } finally {
                    if (options.isPleaseCloseAfterUse()) {
                        ParseOptions.close(source);
                    }
                }
            }
            b = controller;
            synchronized (b) {
                doc = pool.find(documentKey);
                if (doc != null) {
                    return DocumentFn.getFragment(doc, fragmentId, c, locator);
                }
                controller.registerDocument(newdoc, documentKey);
                if (controller instanceof XsltController) {
                    ((XsltController)controller).addUnavailableOutputDestination(documentKey);
                }
            }
            return DocumentFn.getFragment(newdoc, fragmentId, c, locator);
        } catch (TransformerException err) {
            pool.markUnavailable(documentKey);
            XPathException xerr = XPathException.makeXPathException(err);
            xerr.maybeSetLocation(locator);
            String code = err.getException() instanceof URISyntaxException ? "FODC0005" : "FODC0002";
            xerr.maybeSetErrorCode(code);
            throw xerr;
        }
    }

    public static Source resolveURI(String href, String baseURI, String documentKey, XPathContext context) throws XPathException {
        Source source;
        Object uri;
        URIResolver resolver = context.getURIResolver();
        if (baseURI == null) {
            try {
                uri = new URI(href);
                if (!((URI)uri).isAbsolute()) {
                    throw new XPathException("Relative URI passed to document() function (" + href + "); but no base URI is available", "XTDE1162");
                }
            } catch (URISyntaxException e) {
                throw new XPathException("Invalid URI passed to document() function: " + href, "FODC0005");
            }
        }
        try {
            source = resolver instanceof RelativeURIResolver && documentKey != null ? ((RelativeURIResolver)resolver).dereference(documentKey) : resolver.resolve(href, baseURI);
        } catch (Exception ex) {
            XPathException de = new XPathException("Exception thrown by URIResolver resolving `" + href + "` against `" + baseURI + "'", ex);
            if (context.getConfiguration().getBooleanProperty(Feature.TRACE_EXTERNAL_FUNCTIONS)) {
                ex.printStackTrace();
            }
            throw de;
        }
        if (source instanceof StreamSource && ((StreamSource)source).getInputStream() == null && ((StreamSource)source).getReader() == null) {
            uri = source.getSystemId();
            if (uri != null) {
                resolver = context.getController().getStandardURIResolver();
                try {
                    source = resolver.resolve((String)uri, "");
                } catch (TransformerException ex) {
                    throw XPathException.makeXPathException(ex);
                }
            } else {
                source = null;
            }
        }
        if (source == null && !(resolver instanceof NonDelegatingURIResolver)) {
            resolver = context.getController().getStandardURIResolver();
            try {
                source = resolver instanceof RelativeURIResolver && documentKey != null ? ((RelativeURIResolver)resolver).dereference(documentKey) : resolver.resolve(href, baseURI);
            } catch (TransformerException ex) {
                throw XPathException.makeXPathException(ex);
            }
        }
        return source;
    }

    protected static DocumentKey computeDocumentKey(String href, String baseURI, PackageData packageData, XPathContext c) throws XPathException {
        Controller controller = c.getController();
        URIResolver resolver = controller.getURIResolver();
        if (resolver == null) {
            resolver = controller.getStandardURIResolver();
        }
        return DocumentFn.computeDocumentKey(href, baseURI, packageData, resolver, true);
    }

    public static DocumentKey computeDocumentKey(String href, String baseURI, PackageData packageData, URIResolver resolver, boolean strip) {
        String name;
        String absURI;
        if (resolver instanceof RelativeURIResolver) {
            try {
                absURI = ((RelativeURIResolver)resolver).makeAbsolute(href, baseURI);
            } catch (TransformerException e) {
                absURI = '/' + href;
            }
        } else {
            href = ResolveURI.escapeSpaces(href);
            if (baseURI == null) {
                try {
                    absURI = new URI(href).toString();
                } catch (URISyntaxException err) {
                    absURI = '/' + href;
                }
            } else if (href.isEmpty()) {
                absURI = baseURI;
            } else {
                try {
                    absURI = ResolveURI.makeAbsolute(href, baseURI).toString();
                } catch (IllegalArgumentException | URISyntaxException err) {
                    absURI = baseURI + "/../" + href;
                }
            }
        }
        if (strip && packageData instanceof StylesheetPackage && ((StylesheetPackage)packageData).getSpaceStrippingRule() != NoElementsSpaceStrippingRule.getInstance() && (name = ((StylesheetPackage)packageData).getPackageName()) != null) {
            return new DocumentKey(absURI, name, ((StylesheetPackage)packageData).getPackageVersion());
        }
        return new DocumentKey(absURI);
    }

    public static NodeInfo preLoadDoc(String href, String baseURI, Configuration config, SourceLocator locator) throws XPathException {
        String documentKey;
        int hash = href.indexOf(35);
        if (hash >= 0) {
            throw new XPathException("Fragment identifier not supported for preloaded documents");
        }
        URIResolver resolver = config.getURIResolver();
        if (resolver instanceof RelativeURIResolver) {
            try {
                documentKey = ((RelativeURIResolver)resolver).makeAbsolute(href, baseURI);
            } catch (TransformerException e) {
                documentKey = '/' + href;
                baseURI = "";
            }
        } else if (baseURI == null) {
            try {
                documentKey = new URI(href).toString();
            } catch (URISyntaxException err) {
                documentKey = '/' + href;
                baseURI = "";
            }
        } else if (href.isEmpty()) {
            documentKey = baseURI;
        } else {
            try {
                documentKey = ResolveURI.makeAbsolute(href, baseURI).toString();
            } catch (IllegalArgumentException | URISyntaxException err) {
                documentKey = baseURI + "/../" + href;
            }
        }
        TreeInfo doc = config.getGlobalDocumentPool().find(documentKey);
        if (doc != null) {
            return doc.getRootNode();
        }
        try {
            URIResolver r = resolver;
            Source source = null;
            if (r != null) {
                try {
                    source = r.resolve(href, baseURI);
                } catch (Exception ex) {
                    XPathException de = new XPathException("Exception thrown by URIResolver", ex);
                    if (config.getBooleanProperty(Feature.TRACE_EXTERNAL_FUNCTIONS)) {
                        ex.printStackTrace();
                    }
                    de.setLocator(locator);
                    throw de;
                }
            }
            if (source == null && !(r instanceof NonDelegatingURIResolver)) {
                r = config.getSystemURIResolver();
                source = r.resolve(href, baseURI);
            }
            source = config.getSourceResolver().resolveSource(source, config);
            TreeInfo newdoc = config.buildDocumentTree(source);
            config.getGlobalDocumentPool().add(newdoc, documentKey);
            return newdoc.getRootNode();
        } catch (TransformerException err) {
            XPathException xerr = XPathException.makeXPathException(err);
            xerr.setLocator(locator);
            xerr.setErrorCode("FODC0002");
            throw new XPathException(err);
        }
    }

    public static void sendDoc(String href, String baseURL, XPathContext c, Location locator, Receiver out, ParseOptions parseOptions) throws XPathException {
        String documentKey;
        PipelineConfiguration pipe = out.getPipelineConfiguration();
        if (pipe == null) {
            pipe = c.getController().makePipelineConfiguration();
            pipe.setXPathContext(c);
            out.setPipelineConfiguration(pipe);
        }
        if (baseURL == null) {
            try {
                documentKey = new URI(href).toString();
            } catch (URISyntaxException err) {
                documentKey = '/' + href;
                baseURL = "";
            }
        } else if (href.isEmpty()) {
            documentKey = baseURL;
        } else {
            try {
                documentKey = ResolveURI.makeAbsolute(href, baseURL).toString();
            } catch (IllegalArgumentException | URISyntaxException err) {
                documentKey = baseURL + "/../" + href;
            }
        }
        Controller controller = c.getController();
        TreeInfo doc = controller.getDocumentPool().find(documentKey);
        Source source = null;
        if (doc != null) {
            source = doc.getRootNode();
        } else {
            try {
                URIResolver r = controller.getURIResolver();
                if (r != null) {
                    source = r.resolve(href, baseURL);
                }
                if (source == null) {
                    r = controller.getStandardURIResolver();
                    source = r.resolve(href, baseURL);
                }
                if (source instanceof NodeInfo || source instanceof DOMSource) {
                    NodeInfo startNode = controller.prepareInputTree(source);
                    source = startNode.getRoot();
                }
            } catch (TransformerException err) {
                XPathException xerr = XPathException.makeXPathException(err);
                xerr.setLocator(locator);
                xerr.maybeSetErrorCode("FODC0005");
                throw xerr;
            }
        }
        if (controller.getConfiguration().isTiming()) {
            controller.getConfiguration().getLogger().info("Streaming input document " + source.getSystemId());
        }
        out.setPipelineConfiguration(pipe);
        try {
            Sender.send(source, out, parseOptions);
        } catch (XPathException e) {
            e.maybeSetLocation(locator);
            e.maybeSetErrorCode("FODC0002");
            throw e;
        }
    }

    private static NodeInfo getFragment(TreeInfo doc, String fragmentId, XPathContext context, Location locator) {
        if (fragmentId == null) {
            return doc.getRootNode();
        }
        if (!NameChecker.isValidNCName(fragmentId)) {
            context.getController().warning("Invalid fragment identifier in URI", "XTDE1160", locator);
            return doc.getRootNode();
        }
        return doc.selectID(fragmentId, false);
    }

    private static class DocumentMappingFunction
    implements ItemMappingFunction {
        public String baseURI;
        public String stylesheetURI;
        public Location locator;
        public PackageData packageData;
        public XPathContext context;

        public DocumentMappingFunction(XPathContext context) {
            this.context = context;
        }

        @Override
        public Item mapItem(Item item) throws XPathException {
            String b = this.baseURI;
            if (b == null) {
                b = item instanceof NodeInfo ? ((NodeInfo)item).getBaseURI() : this.stylesheetURI;
            }
            return DocumentFn.makeDoc(item.getStringValue(), b, this.packageData, null, this.context, this.locator, false);
        }
    }
}

