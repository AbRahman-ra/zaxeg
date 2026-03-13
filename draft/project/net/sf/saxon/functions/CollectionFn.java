/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.IriToUri;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.CollectionFinder;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.lib.ResourceCollection;
import net.sf.saxon.om.DocumentKey;
import net.sf.saxon.om.DocumentPool;
import net.sf.saxon.om.GenericTreeInfo;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.LazySequence;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.resource.AbstractResourceCollection;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.tree.wrapper.SpaceStrippedDocument;
import net.sf.saxon.value.ExternalObject;
import net.sf.saxon.value.ObjectValue;

public class CollectionFn
extends SystemFunction
implements Callable {
    public static String EMPTY_COLLECTION_URI = "http://saxon.sf.net/collection/empty";
    public static final ResourceCollection EMPTY_COLLECTION = new EmptyCollection(EMPTY_COLLECTION_URI);

    @Override
    public int getSpecialProperties(Expression[] arguments) {
        return super.getSpecialProperties(arguments) & 0xFF7FFFFF | 0x80000;
    }

    private String getAbsoluteCollectionURI(String href, XPathContext context) throws XPathException {
        String absoluteURI;
        block10: {
            if (href == null) {
                absoluteURI = context.getConfiguration().getDefaultCollection();
            } else {
                URI uri;
                try {
                    uri = new URI(href);
                } catch (URISyntaxException e) {
                    href = IriToUri.iriToUri(href).toString();
                    try {
                        uri = new URI(href);
                    } catch (URISyntaxException e2) {
                        throw new XPathException(e2.getMessage(), "FODC0004");
                    }
                }
                try {
                    if (uri.isAbsolute()) {
                        absoluteURI = uri.toString();
                        break block10;
                    }
                    String base = this.getRetainedStaticContext().getStaticBaseUriString();
                    if (base != null) {
                        absoluteURI = ResolveURI.makeAbsolute(href, base).toString();
                        break block10;
                    }
                    throw new XPathException("Relative collection URI cannot be resolved: no base URI available", "FODC0002");
                } catch (URISyntaxException e) {
                    throw new XPathException(e.getMessage(), "FODC0004");
                }
            }
        }
        return absoluteURI;
    }

    private SequenceIterator getSequenceIterator(ResourceCollection collection, XPathContext context) throws XPathException {
        final Iterator<? extends Resource> sources = collection.getResources(context);
        return new SequenceIterator(){

            @Override
            public Item next() throws XPathException {
                try {
                    if (sources.hasNext()) {
                        return new ObjectValue(sources.next());
                    }
                    return null;
                } catch (Exception e) {
                    throw XPathException.makeXPathException(e);
                }
            }

            @Override
            public void close() {
                if (sources instanceof Closeable) {
                    try {
                        ((Closeable)((Object)sources)).close();
                    } catch (IOException e) {
                        throw new UncheckedXPathException(new XPathException(e));
                    }
                }
            }
        };
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        boolean alreadyStripped;
        GroundedValue cachedCollection;
        Item arg;
        String href = this.getArity() == 0 ? context.getController().getDefaultCollection() : ((arg = arguments[0].head()) == null ? context.getController().getDefaultCollection() : arg.getStringValue());
        if (href == null) {
            throw new XPathException("No default collection has been defined", "FODC0002");
        }
        String absoluteURI = this.getAbsoluteCollectionURI(href, context);
        PackageData packageData = this.getRetainedStaticContext().getPackageData();
        SpaceStrippingRule whitespaceRule = NoElementsSpaceStrippingRule.getInstance();
        String collectionKey = absoluteURI;
        if (packageData.isXSLT() && (whitespaceRule = ((StylesheetPackage)packageData).getSpaceStrippingRule()) != NoElementsSpaceStrippingRule.getInstance()) {
            collectionKey = ((StylesheetPackage)packageData).getPackageName() + ((StylesheetPackage)packageData).getPackageVersion() + " " + absoluteURI;
        }
        if ((cachedCollection = (GroundedValue)context.getController().getUserData("saxon:collections", collectionKey)) != null) {
            return cachedCollection;
        }
        CollectionFinder collectionFinder = context.getController().getCollectionFinder();
        ResourceCollection collection = collectionFinder.findCollection(context, absoluteURI);
        if (collection == null) {
            collection = new EmptyCollection(EMPTY_COLLECTION_URI);
        }
        if (packageData instanceof StylesheetPackage && whitespaceRule != NoElementsSpaceStrippingRule.getInstance() && collection instanceof AbstractResourceCollection && (alreadyStripped = collection.stripWhitespace(whitespaceRule))) {
            whitespaceRule = null;
        }
        SequenceIterator sourceSeq = this.getSequenceIterator(collection, context);
        SequenceIterator result = context.getConfiguration().getMultithreadedItemMappingIterator(sourceSeq, item1 -> ((Resource)((ExternalObject)item1).getObject()).getItem(context));
        if (whitespaceRule != null) {
            SpaceStrippingRule rule = whitespaceRule;
            ItemMappingFunction stripper = item -> {
                TreeInfo treeInfo;
                if (item instanceof NodeInfo && ((NodeInfo)item).getNodeKind() == 9 && (treeInfo = ((NodeInfo)item).getTreeInfo()).getSpaceStrippingRule() != rule) {
                    return new SpaceStrippedDocument(treeInfo, rule).getRootNode();
                }
                return item;
            };
            result = new ItemMappingIterator(result, stripper);
        }
        if (collection.isStable(context) || context.getConfiguration().getBooleanProperty(Feature.STABLE_COLLECTION_URI)) {
            Item item2;
            Controller controller = context.getController();
            DocumentPool docPool = controller.getDocumentPool();
            cachedCollection = result.materialize();
            UnfailingIterator iter = cachedCollection.iterate();
            while ((item2 = iter.next()) != null) {
                if (!(item2 instanceof NodeInfo) || ((NodeInfo)item2).getNodeKind() != 9) continue;
                String docUri = ((NodeInfo)item2).getSystemId();
                DocumentKey docKey = new DocumentKey(docUri);
                TreeInfo info = item2 instanceof TreeInfo ? (TreeInfo)((Object)item2) : new GenericTreeInfo(controller.getConfiguration(), (NodeInfo)item2);
                docPool.add(info, docKey);
            }
            context.getController().setUserData("saxon:collections", collectionKey, cachedCollection);
            return cachedCollection;
        }
        return new LazySequence(result);
    }

    private static class EmptyCollection
    implements ResourceCollection {
        private String collectionUri;

        EmptyCollection(String cUri) {
            this.collectionUri = cUri;
        }

        @Override
        public String getCollectionURI() {
            return this.collectionUri;
        }

        @Override
        public Iterator<String> getResourceURIs(XPathContext context) {
            return new ArrayList().iterator();
        }

        public Iterator<Resource> getResources(XPathContext context) {
            return new ArrayList().iterator();
        }

        @Override
        public boolean isStable(XPathContext context) {
            return true;
        }

        @Override
        public boolean stripWhitespace(SpaceStrippingRule rules) {
            return false;
        }
    }
}

