/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Sender;
import net.sf.saxon.event.SequenceCollector;
import net.sf.saxon.event.Sink;
import net.sf.saxon.event.Stripper;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.Bindery;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.functions.AccessorFn;
import net.sf.saxon.lib.AugmentedSource;
import net.sf.saxon.lib.CollectionFinder;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.StandardErrorReporter;
import net.sf.saxon.lib.StandardURIResolver;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.om.AllElementsSpaceStrippingRule;
import net.sf.saxon.om.DocumentKey;
import net.sf.saxon.om.DocumentPool;
import net.sf.saxon.om.FocusTrackingIterator;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.MemoSequence;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.regex.RegexIterator;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.TraceEventMulticaster;
import net.sf.saxon.trans.KeyIndex;
import net.sf.saxon.trans.StylesheetCache;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.trans.XmlProcessingIncident;
import net.sf.saxon.trans.rules.RuleManager;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.tree.wrapper.SpaceStrippedDocument;
import net.sf.saxon.tree.wrapper.SpaceStrippedNode;
import net.sf.saxon.tree.wrapper.TypeStrippedDocument;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.z.IntHashMap;
import org.xml.sax.SAXParseException;

public class Controller
implements ContextOriginator {
    private Configuration config;
    protected Executable executable;
    protected Item globalContextItem;
    private boolean globalContextItemPreset;
    private Map<PackageData, Bindery> binderies;
    private GlobalParameterSet globalParameters;
    private boolean convertParameters = true;
    private Map<GlobalVariable, Set<GlobalVariable>> globalVariableDependencies = new HashMap<GlobalVariable, Set<GlobalVariable>>();
    protected TraceListener traceListener;
    private boolean tracingPaused;
    private Logger traceFunctionDestination;
    private URIResolver standardURIResolver;
    private URIResolver userURIResolver;
    protected Receiver principalResult;
    protected String principalResultURI;
    private UnparsedTextURIResolver unparsedTextResolver;
    private String defaultCollectionURI;
    private ErrorReporter errorReporter = new StandardErrorReporter();
    private TreeModel treeModel = TreeModel.TINY_TREE;
    private DocumentPool sourceDocumentPool;
    private IntHashMap<Map<Long, KeyIndex>> localIndexes;
    private HashMap<String, Object> userDataTable;
    private NodeInfo lastRememberedNode = null;
    private int lastRememberedNumber = -1;
    private DateTimeValue currentDateTime;
    private boolean dateTimePreset = false;
    private PathMap pathMap = null;
    protected int validationMode = 0;
    protected boolean inUse = false;
    private boolean stripSourceTrees = true;
    private CollectionFinder collectionFinder = null;
    public static final String ANONYMOUS_PRINCIPAL_OUTPUT_URI = "dummy:/anonymous/principal/result";
    private StylesheetCache stylesheetCache = null;
    private Function<SequenceIterator, FocusTrackingIterator> focusTrackerFactory = FocusTrackingIterator::new;
    private Function<SequenceIterator, FocusTrackingIterator> multiThreadedFocusTrackerFactory;

    public Controller(Configuration config) {
        this.config = config;
        this.executable = new Executable(config);
        this.sourceDocumentPool = new DocumentPool();
        this.reset();
    }

    public Controller(Configuration config, Executable executable) {
        this.config = config;
        this.executable = executable;
        this.sourceDocumentPool = new DocumentPool();
        this.reset();
    }

    public void reset() {
        TraceListener tracer;
        this.globalParameters = new GlobalParameterSet();
        this.focusTrackerFactory = this.config.getFocusTrackerFactory(this.executable, false);
        this.multiThreadedFocusTrackerFactory = this.config.getFocusTrackerFactory(this.executable, true);
        this.standardURIResolver = this.config.getSystemURIResolver();
        this.userURIResolver = this.config.getURIResolver();
        this.unparsedTextResolver = this.config.getUnparsedTextURIResolver();
        this.validationMode = this.config.getSchemaValidationMode();
        this.errorReporter = new StandardErrorReporter();
        this.traceListener = null;
        this.traceFunctionDestination = this.config.getLogger();
        try {
            tracer = this.config.makeTraceListener();
        } catch (XPathException err) {
            throw new IllegalStateException(err.getMessage());
        }
        if (tracer != null) {
            this.addTraceListener(tracer);
        }
        this.setModel(this.config.getParseOptions().getModel());
        this.globalContextItem = null;
        this.currentDateTime = null;
        this.dateTimePreset = false;
        this.clearPerTransformationData();
    }

    protected synchronized void clearPerTransformationData() {
        this.userDataTable = new HashMap(20);
        this.principalResult = null;
        this.tracingPaused = false;
        this.lastRememberedNode = null;
        this.lastRememberedNumber = -1;
        this.stylesheetCache = null;
        this.localIndexes = null;
        if (!this.globalContextItemPreset) {
            this.globalContextItem = null;
        }
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public Sequence getParameter(StructuredQName name) {
        return this.globalParameters.get(name);
    }

    public GroundedValue getConvertedParameter(StructuredQName name, SequenceType requiredType, XPathContext context) throws XPathException {
        GroundedValue val = this.globalParameters.convertParameterValue(name, requiredType, this.convertParameters, context);
        if (val != null) {
            Item next;
            Configuration config = this.getConfiguration();
            UnfailingIterator iter = val.iterate();
            while ((next = iter.next()) != null) {
                if (!(next instanceof NodeInfo) || config.isCompatible(((NodeInfo)next).getConfiguration())) continue;
                throw new XPathException("A node supplied in a global parameter must be built using the same Configuration that was used to compile the stylesheet or query", "SXXP0004");
            }
            if (val instanceof NodeInfo && ((NodeInfo)val).getNodeKind() == 9) {
                String systemId = ((NodeInfo)val).getRoot().getSystemId();
                try {
                    DocumentPool pool;
                    if (systemId != null && new URI(systemId).isAbsolute() && (pool = this.getDocumentPool()).find(systemId) == null) {
                        pool.add(((NodeInfo)val).getTreeInfo(), systemId);
                    }
                } catch (URISyntaxException uRISyntaxException) {
                    // empty catch block
                }
            }
            val = val.materialize();
        }
        return val;
    }

    public void setBaseOutputURI(String uri) {
        this.principalResultURI = uri;
    }

    public String getBaseOutputURI() {
        return this.principalResultURI;
    }

    public Receiver getPrincipalResult() {
        return this.principalResult;
    }

    public SequenceCollector allocateSequenceOutputter() {
        PipelineConfiguration pipe = this.makePipelineConfiguration();
        return new SequenceCollector(pipe, 20);
    }

    public SequenceCollector allocateSequenceOutputter(int size) {
        PipelineConfiguration pipe = this.makePipelineConfiguration();
        return new SequenceCollector(pipe, size);
    }

    public PipelineConfiguration makePipelineConfiguration() {
        PipelineConfiguration pipe = this.config.makePipelineConfiguration();
        pipe.setURIResolver(this.userURIResolver == null ? this.standardURIResolver : this.userURIResolver);
        pipe.getParseOptions().setSchemaValidationMode(this.validationMode);
        pipe.getParseOptions().setErrorReporter(this.errorReporter);
        pipe.setController(this);
        Executable executable = this.getExecutable();
        if (executable != null) {
            pipe.setHostLanguage(executable.getHostLanguage());
        }
        return pipe;
    }

    public void setErrorReporter(ErrorReporter reporter) {
        this.errorReporter = reporter;
    }

    public ErrorReporter getErrorReporter() {
        return this.errorReporter;
    }

    public void reportFatalError(XPathException err) {
        if (!err.hasBeenReported()) {
            if (err.getHostLanguage() == null) {
                if (this.executable.getHostLanguage() == HostLanguage.XSLT) {
                    err.setHostLanguage("XSLT");
                } else if (this.executable.getHostLanguage() == HostLanguage.XQUERY) {
                    err.setHostLanguage("XQuery");
                }
            }
            this.getErrorReporter().report(new XmlProcessingException(err));
            err.setHasBeenReported(true);
        }
    }

    public void warning(String message, String errorCode, Location locator) {
        XmlProcessingIncident warning = new XmlProcessingIncident(message, errorCode, locator).asWarning();
        this.errorReporter.report(warning);
    }

    protected void handleXPathException(XPathException err) throws XPathException {
        Throwable cause = err.getException();
        if (cause instanceof SAXParseException) {
            SAXParseException spe = (SAXParseException)cause;
            if ((cause = spe.getException()) instanceof RuntimeException) {
                this.reportFatalError(err);
            }
        } else {
            this.reportFatalError(err);
        }
        throw err;
    }

    public Executable getExecutable() {
        return this.executable;
    }

    public DocumentPool getDocumentPool() {
        return this.sourceDocumentPool;
    }

    public void clearDocumentPool() {
        for (PackageData pack : this.getExecutable().getPackages()) {
            this.sourceDocumentPool.discardIndexes(pack.getKeyManager());
        }
        this.sourceDocumentPool = new DocumentPool();
    }

    public synchronized Bindery getBindery(PackageData packageData) {
        Bindery b = this.binderies.get(packageData);
        if (b == null) {
            b = new Bindery(packageData);
            this.binderies.put(packageData, b);
        }
        return b;
    }

    public void setGlobalContextItem(Item contextItem) throws XPathException {
        this.setGlobalContextItem(contextItem, false);
    }

    public void setGlobalContextItem(Item contextItem, boolean alreadyStripped) throws XPathException {
        if (!alreadyStripped) {
            if (this.globalContextItem instanceof SpaceStrippedNode && ((SpaceStrippedNode)this.globalContextItem).getUnderlyingNode() == contextItem) {
                return;
            }
            if (contextItem instanceof NodeInfo) {
                NodeInfo node = (NodeInfo)contextItem;
                contextItem = this.prepareInputTree(node);
                if (node.getNodeKind() == 9 && node.getSystemId() != null) {
                    this.getDocumentPool().add(node.getTreeInfo(), node.getSystemId());
                }
            }
        }
        if (contextItem instanceof NodeInfo) {
            NodeInfo startNode = (NodeInfo)contextItem;
            if (startNode.getConfiguration() == null) {
                throw new XPathException("The supplied source document must be associated with a Configuration");
            }
            if (!startNode.getConfiguration().isCompatible(this.executable.getConfiguration())) {
                throw new XPathException("Source document and stylesheet must use the same or compatible Configurations", "SXXP0004");
            }
            if (startNode.getTreeInfo().isTyped() && !this.executable.isSchemaAware()) {
                throw new XPathException("Cannot use a schema-validated source document unless the stylesheet is schema-aware");
            }
        }
        this.globalContextItem = contextItem;
        this.globalContextItemPreset = true;
    }

    public void clearGlobalContextItem() {
        this.globalContextItem = null;
        this.globalContextItemPreset = false;
    }

    public Item getGlobalContextItem() {
        return this.globalContextItem;
    }

    public void setURIResolver(URIResolver resolver) {
        this.userURIResolver = resolver;
        if (resolver instanceof StandardURIResolver) {
            ((StandardURIResolver)resolver).setConfiguration(this.getConfiguration());
        }
    }

    public URIResolver getURIResolver() {
        return this.userURIResolver;
    }

    public URIResolver getStandardURIResolver() {
        return this.standardURIResolver;
    }

    public void setUnparsedTextURIResolver(UnparsedTextURIResolver resolver) {
        this.unparsedTextResolver = resolver;
    }

    public UnparsedTextURIResolver getUnparsedTextURIResolver() {
        return this.unparsedTextResolver;
    }

    public CollectionFinder getCollectionFinder() {
        if (this.collectionFinder == null) {
            this.collectionFinder = this.config.getCollectionFinder();
        }
        return this.collectionFinder;
    }

    public void setCollectionFinder(CollectionFinder cf) {
        this.collectionFinder = cf;
    }

    public void setDefaultCollection(String uri) {
        this.defaultCollectionURI = uri;
    }

    public String getDefaultCollection() {
        return this.defaultCollectionURI == null ? this.getConfiguration().getDefaultCollection() : this.defaultCollectionURI;
    }

    public int getSchemaValidationMode() {
        return this.validationMode;
    }

    public void setSchemaValidationMode(int validationMode) {
        this.validationMode = validationMode;
    }

    public void setModel(TreeModel model) {
        this.treeModel = model;
    }

    public TreeModel getModel() {
        return this.treeModel;
    }

    public Builder makeBuilder() {
        Builder b = this.treeModel.makeBuilder(this.makePipelineConfiguration());
        b.setTiming(this.config.isTiming());
        b.setLineNumbering(this.config.isLineNumbering());
        return b;
    }

    public void setStripSourceTrees(boolean strip) {
        this.stripSourceTrees = strip;
    }

    public boolean isStripSourceTree() {
        return this.stripSourceTrees;
    }

    protected boolean isStylesheetContainingStripSpace() {
        SpaceStrippingRule rule;
        return this.executable instanceof PreparedStylesheet && (rule = ((PreparedStylesheet)this.executable).getTopLevelPackage().getSpaceStrippingRule()) != null && rule != NoElementsSpaceStrippingRule.getInstance();
    }

    public boolean isStylesheetStrippingTypeAnnotations() {
        return this.executable instanceof PreparedStylesheet && ((PreparedStylesheet)this.executable).getTopLevelPackage().isStripsTypeAnnotations();
    }

    public Stripper makeStripper(Receiver next) {
        if (next == null) {
            next = new Sink(this.makePipelineConfiguration());
        }
        return new Stripper(this.getSpaceStrippingRule(), next);
    }

    public SpaceStrippingRule getSpaceStrippingRule() {
        SpaceStrippingRule rule;
        if (this.config.getParseOptions().getSpaceStrippingRule() == AllElementsSpaceStrippingRule.getInstance()) {
            return AllElementsSpaceStrippingRule.getInstance();
        }
        if (this.executable instanceof PreparedStylesheet && (rule = ((PreparedStylesheet)this.executable).getTopLevelPackage().getSpaceStrippingRule()) != null) {
            return rule;
        }
        return NoElementsSpaceStrippingRule.getInstance();
    }

    public void registerDocument(TreeInfo doc, DocumentKey uri) throws XPathException {
        if (!this.getExecutable().isSchemaAware() && !Untyped.getInstance().equals(doc.getRootNode().getSchemaType())) {
            boolean isXSLT = this.getExecutable().getHostLanguage() == HostLanguage.XSLT;
            String message = isXSLT ? "The source document has been schema-validated, but the stylesheet is not schema-aware. A stylesheet is schema-aware if either (a) it contains an xsl:import-schema declaration, or (b) the stylesheet compiler was configured to be schema-aware." : "The source document has been schema-validated, but the query is not schema-aware. A query is schema-aware if either (a) it contains an 'import schema' declaration, or (b) the query compiler was configured to be schema-aware.";
            throw new XPathException(message);
        }
        if (uri != null) {
            this.sourceDocumentPool.add(doc, uri);
        }
    }

    public RuleManager getRuleManager() {
        Executable exec = this.getExecutable();
        return exec instanceof PreparedStylesheet ? ((PreparedStylesheet)this.getExecutable()).getRuleManager() : null;
    }

    public void setTraceListener(TraceListener listener) {
        this.traceListener = listener;
    }

    public TraceListener getTraceListener() {
        return this.traceListener;
    }

    public final boolean isTracing() {
        return this.traceListener != null && !this.tracingPaused;
    }

    public final void pauseTracing(boolean pause) {
        this.tracingPaused = pause;
    }

    public void addTraceListener(TraceListener trace) {
        if (trace != null) {
            this.traceListener = TraceEventMulticaster.add(this.traceListener, trace);
        }
    }

    public void removeTraceListener(TraceListener trace) {
        this.traceListener = TraceEventMulticaster.remove(this.traceListener, trace);
    }

    public void setTraceFunctionDestination(Logger stream) {
        this.traceFunctionDestination = stream;
    }

    public Logger getTraceFunctionDestination() {
        return this.traceFunctionDestination;
    }

    public void initializeController(GlobalParameterSet params) throws XPathException {
        block3: {
            this.binderies = new HashMap<PackageData, Bindery>();
            try {
                this.executable.checkSuppliedParameters(params);
            } catch (XPathException e) {
                if (e.hasBeenReported()) break block3;
                this.getErrorReporter().report(new XmlProcessingException(e));
                throw e;
            }
        }
        this.globalParameters = params;
        this.globalContextItem = this.executable.checkInitialContextItem(this.globalContextItem, this.newXPathContext());
        if (this.traceListener != null) {
            this.traceListener.open(this);
            this.preEvaluateGlobals(this.newXPathContext());
        }
    }

    public void setApplyFunctionConversionRulesToExternalVariables(boolean applyConversionRules) {
        this.convertParameters = applyConversionRules;
    }

    public synchronized Object getUserData(Object key, String name) {
        String keyValue = key.hashCode() + " " + name;
        return this.userDataTable.get(keyValue);
    }

    public synchronized void setUserData(Object key, String name, Object data) {
        String keyVal = key.hashCode() + " " + name;
        if (data == null) {
            this.userDataTable.remove(keyVal);
        } else {
            this.userDataTable.put(keyVal, data);
        }
    }

    public synchronized IntHashMap<Map<Long, KeyIndex>> getLocalIndexes() {
        if (this.localIndexes == null) {
            this.localIndexes = new IntHashMap();
        }
        return this.localIndexes;
    }

    public synchronized void setRememberedNumber(NodeInfo node, int number) {
        this.lastRememberedNode = node;
        this.lastRememberedNumber = number;
    }

    public synchronized int getRememberedNumber(NodeInfo node) {
        if (this.lastRememberedNode == node) {
            return this.lastRememberedNumber;
        }
        return -1;
    }

    protected void checkReadiness() throws XPathException {
        if (this.inUse) {
            throw new IllegalStateException("The Controller is being used recursively or concurrently. This is not permitted.");
        }
        if (this.binderies == null) {
            throw new IllegalStateException("The Controller has not been initialized");
        }
        this.inUse = true;
        this.clearPerTransformationData();
        if (this.executable == null) {
            throw new XPathException("Stylesheet has not been prepared");
        }
        if (!this.dateTimePreset) {
            this.currentDateTime = null;
        }
    }

    public NodeInfo makeSourceTree(Source source, int validationMode) throws XPathException {
        if (source instanceof SAXSource && this.config.getBooleanProperty(Feature.IGNORE_SAX_SOURCE_PARSER)) {
            ((SAXSource)source).setXMLReader(null);
        }
        Builder sourceBuilder = this.makeBuilder();
        sourceBuilder.setUseEventLocation(true);
        if (sourceBuilder instanceof TinyBuilder) {
            ((TinyBuilder)sourceBuilder).setStatistics(this.config.getTreeStatistics().SOURCE_DOCUMENT_STATISTICS);
        }
        Receiver r = sourceBuilder;
        SpaceStrippingRule spaceStrippingRule = NoElementsSpaceStrippingRule.getInstance();
        if (this.config.isStripsAllWhiteSpace() || this.isStylesheetContainingStripSpace() || validationMode == 1 || validationMode == 2) {
            r = this.makeStripper(sourceBuilder);
            spaceStrippingRule = this.getSpaceStrippingRule();
        }
        if (this.isStylesheetStrippingTypeAnnotations()) {
            r = this.config.getAnnotationStripper(r);
        }
        PipelineConfiguration pipe = sourceBuilder.getPipelineConfiguration();
        pipe.getParseOptions().setSchemaValidationMode(validationMode);
        r.setPipelineConfiguration(pipe);
        Sender.send(source, r, null);
        if (source instanceof AugmentedSource && ((AugmentedSource)source).isPleaseCloseAfterUse()) {
            ((AugmentedSource)source).close();
        }
        NodeInfo doc = sourceBuilder.getCurrentRoot();
        sourceBuilder.reset();
        if (source.getSystemId() != null) {
            this.registerDocument(doc.getTreeInfo(), new DocumentKey(source.getSystemId()));
        }
        doc.getTreeInfo().setSpaceStrippingRule(spaceStrippingRule);
        return doc;
    }

    public NodeInfo prepareInputTree(Source source) {
        TreeInfo docInfo;
        NodeInfo start = this.getConfiguration().unravel(source);
        if (this.isStylesheetStrippingTypeAnnotations() && (docInfo = start.getTreeInfo()).isTyped()) {
            TypeStrippedDocument strippedDoc = new TypeStrippedDocument(docInfo);
            start = strippedDoc.wrap(start);
        }
        if (this.stripSourceTrees && this.isStylesheetContainingStripSpace()) {
            docInfo = start.getTreeInfo();
            SpaceStrippingRule spaceStrippingRule = this.getSpaceStrippingRule();
            if (docInfo.getSpaceStrippingRule() != spaceStrippingRule) {
                SpaceStrippedDocument strippedDoc = new SpaceStrippedDocument(docInfo, spaceStrippingRule);
                if (!SpaceStrippedNode.isPreservedNode(start, strippedDoc, start.getParent())) {
                    return null;
                }
                start = strippedDoc.wrap(start);
            }
        }
        return start;
    }

    public void preEvaluateGlobals(XPathContext context) throws XPathException {
        for (PackageData pack : this.getExecutable().getPackages()) {
            for (GlobalVariable var : pack.getGlobalVariableList()) {
                if (var.isUnused()) continue;
                try {
                    var.evaluateVariable(context, var.getDeclaringComponent());
                } catch (XPathException err) {
                    this.getBindery(var.getPackageData()).setGlobalVariable(var, new Bindery.FailureValue(err));
                }
            }
        }
    }

    public synchronized void registerGlobalVariableDependency(GlobalVariable one, GlobalVariable two) throws XPathException {
        if (one == two) {
            throw new XPathException.Circularity("Circular dependency among global variables: " + one.getVariableQName().getDisplayName() + " depends on its own value");
        }
        Set<GlobalVariable> transitiveDependencies = this.globalVariableDependencies.get(two);
        if (transitiveDependencies != null) {
            if (transitiveDependencies.contains(one)) {
                throw new XPathException.Circularity("Circular dependency among variables: " + one.getVariableQName().getDisplayName() + " depends on the value of " + two.getVariableQName().getDisplayName() + ", which depends directly or indirectly on the value of " + one.getVariableQName().getDisplayName());
            }
            for (GlobalVariable var : transitiveDependencies) {
                this.registerGlobalVariableDependency(one, var);
            }
        }
        Set existingDependencies = this.globalVariableDependencies.computeIfAbsent(one, k -> new HashSet());
        existingDependencies.add(two);
    }

    public void setCurrentDateTime(DateTimeValue dateTime) throws XPathException {
        if (this.currentDateTime == null) {
            if (dateTime.getComponent(AccessorFn.Component.TIMEZONE) == null) {
                throw new XPathException("No timezone is present in supplied value of current date/time");
            }
        } else {
            throw new IllegalStateException("Current date and time can only be set once, and cannot subsequently be changed");
        }
        this.currentDateTime = dateTime;
        this.dateTimePreset = true;
    }

    public DateTimeValue getCurrentDateTime() {
        if (this.currentDateTime == null) {
            this.currentDateTime = DateTimeValue.now();
        }
        return this.currentDateTime;
    }

    public int getImplicitTimezone() {
        return this.getCurrentDateTime().getTimezoneInMinutes();
    }

    public XPathContextMajor newXPathContext() {
        XPathContextMajor c = new XPathContextMajor(this);
        c.setCurrentOutputUri(this.principalResultURI);
        return c;
    }

    public void setUseDocumentProjection(PathMap pathMap) {
        this.pathMap = pathMap;
    }

    public PathMap getPathMapForDocumentProjection() {
        return this.pathMap;
    }

    public synchronized StylesheetCache getStylesheetCache() {
        if (this.stylesheetCache == null) {
            this.stylesheetCache = new StylesheetCache();
        }
        return this.stylesheetCache;
    }

    public Function<SequenceIterator, FocusTrackingIterator> getFocusTrackerFactory(boolean multithreaded) {
        return multithreaded && this.multiThreadedFocusTrackerFactory != null ? this.multiThreadedFocusTrackerFactory : this.focusTrackerFactory;
    }

    public void setFocusTrackerFactory(Function<SequenceIterator, FocusTrackingIterator> focusTrackerFactory) {
        this.focusTrackerFactory = focusTrackerFactory;
    }

    public void setMultithreadedFocusTrackerFactory(Function<SequenceIterator, FocusTrackingIterator> focusTrackerFactory) {
        this.multiThreadedFocusTrackerFactory = focusTrackerFactory;
    }

    public void setMemoizingFocusTrackerFactory() {
        this.setFocusTrackerFactory(base -> {
            FocusTrackingIterator fti;
            if (!(base.getProperties().contains((Object)SequenceIterator.Property.GROUNDED) || base instanceof GroupIterator || base instanceof RegexIterator)) {
                try {
                    MemoSequence ms = new MemoSequence((SequenceIterator)base);
                    fti = FocusTrackingIterator.track(ms.iterate());
                } catch (XPathException e) {
                    fti = FocusTrackingIterator.track(base);
                }
            } else {
                fti = FocusTrackingIterator.track(base);
            }
            return fti;
        });
    }
}

