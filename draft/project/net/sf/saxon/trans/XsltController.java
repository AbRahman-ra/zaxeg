/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.function.Supplier;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.EventSource;
import net.sf.saxon.event.NamespaceDifferencer;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Sender;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.MappingFunction;
import net.sf.saxon.expr.MappingIterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.accum.AccumulatorManager;
import net.sf.saxon.expr.instruct.AttributeSet;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.lib.AugmentedSource;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.lib.OutputURIResolverWrapper;
import net.sf.saxon.lib.ResultDocumentResolver;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.DocumentKey;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GenericTreeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.serialize.Emitter;
import net.sf.saxon.serialize.MessageEmitter;
import net.sf.saxon.serialize.PrincipalOutputGatekeeper;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.QuitParsingException;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.tree.wrapper.SpaceStrippedDocument;
import net.sf.saxon.tree.wrapper.SpaceStrippedNode;
import net.sf.saxon.tree.wrapper.TypeStrippedDocument;

public class XsltController
extends Controller {
    private final Map<StructuredQName, Integer> messageCounters = new HashMap<StructuredQName, Integer>();
    private Receiver explicitMessageReceiver = null;
    private Supplier<Receiver> messageFactory = () -> new NamespaceDifferencer(new MessageEmitter(), new Properties());
    private boolean assertionsEnabled = true;
    private ResultDocumentResolver resultDocumentResolver;
    private HashSet<DocumentKey> allOutputDestinations;
    private Component.M initialMode = null;
    private Function initialFunction = null;
    private Map<StructuredQName, Sequence> initialTemplateParams;
    private Map<StructuredQName, Sequence> initialTemplateTunnelParams;
    private Map<Long, Stack<AttributeSet>> attributeSetEvaluationStacks = new HashMap<Long, Stack<AttributeSet>>();
    private AccumulatorManager accumulatorManager = new AccumulatorManager();
    private PrincipalOutputGatekeeper gatekeeper = null;
    private Destination principalDestination;

    public XsltController(Configuration config, PreparedStylesheet pss) {
        super(config, pss);
    }

    @Override
    public void reset() {
        TraceListener tracer;
        super.reset();
        Configuration config = this.getConfiguration();
        this.validationMode = config.getSchemaValidationMode();
        this.accumulatorManager = new AccumulatorManager();
        this.traceListener = null;
        try {
            tracer = config.makeTraceListener();
        } catch (XPathException err) {
            throw new IllegalStateException(err.getMessage());
        }
        if (tracer != null) {
            this.addTraceListener(tracer);
        }
        this.setModel(config.getParseOptions().getModel());
        this.globalContextItem = null;
        this.initialMode = null;
        this.clearPerTransformationData();
    }

    @Override
    protected synchronized void clearPerTransformationData() {
        super.clearPerTransformationData();
        this.principalResult = null;
        this.allOutputDestinations = null;
        if (this.messageCounters != null) {
            this.messageCounters.clear();
        }
    }

    public void setInitialMode(StructuredQName expandedModeName) throws XPathException {
        if (expandedModeName == null || expandedModeName.equals(Mode.UNNAMED_MODE_NAME)) {
            Mode initial = ((PreparedStylesheet)this.executable).getRuleManager().obtainMode(Mode.UNNAMED_MODE_NAME, true);
            this.initialMode = initial.getDeclaringComponent();
        } else {
            StylesheetPackage topLevelPackage = (StylesheetPackage)this.executable.getTopLevelPackage();
            if (expandedModeName.equals(Mode.DEFAULT_MODE_NAME)) {
                StructuredQName defaultModeName = topLevelPackage.getDefaultMode();
                if (!expandedModeName.equals(defaultModeName)) {
                    this.setInitialMode(defaultModeName);
                }
            } else {
                boolean declaredModes = topLevelPackage.isDeclaredModes();
                SymbolicName sn = new SymbolicName(174, expandedModeName);
                Component.M c = (Component.M)topLevelPackage.getComponent(sn);
                if (c == null) {
                    throw new XPathException("Requested initial mode " + expandedModeName + " is not defined in the stylesheet", "XTDE0045");
                }
                if (!((PreparedStylesheet)this.executable).isEligibleInitialMode(c)) {
                    throw new XPathException("Requested initial mode " + expandedModeName + " is private in the top-level package", "XTDE0045");
                }
                this.initialMode = c;
                if (!declaredModes && this.initialMode.getActor().isEmpty() && !expandedModeName.equals(topLevelPackage.getDefaultMode())) {
                    throw new XPathException("Requested initial mode " + expandedModeName + " contains no template rules", "XTDE0045");
                }
            }
        }
    }

    public StructuredQName getInitialModeName() {
        return this.initialMode == null ? null : this.initialMode.getActor().getModeName();
    }

    public Mode getInitialMode() {
        if (this.initialMode == null) {
            Component.M c;
            StylesheetPackage top = (StylesheetPackage)this.executable.getTopLevelPackage();
            StructuredQName defaultMode = top.getDefaultMode();
            if (defaultMode == null) {
                defaultMode = Mode.UNNAMED_MODE_NAME;
            }
            this.initialMode = c = (Component.M)top.getComponent(new SymbolicName(174, defaultMode));
            return c.getActor();
        }
        return this.initialMode.getActor();
    }

    public AccumulatorManager getAccumulatorManager() {
        return this.accumulatorManager;
    }

    public synchronized boolean checkUniqueOutputDestination(DocumentKey uri) {
        if (uri == null) {
            return true;
        }
        if (this.allOutputDestinations == null) {
            this.allOutputDestinations = new HashSet(20);
        }
        return !this.allOutputDestinations.contains(uri);
    }

    public void addUnavailableOutputDestination(DocumentKey uri) {
        if (this.allOutputDestinations == null) {
            this.allOutputDestinations = new HashSet(20);
        }
        this.allOutputDestinations.add(uri);
    }

    public void removeUnavailableOutputDestination(DocumentKey uri) {
        if (this.allOutputDestinations != null) {
            this.allOutputDestinations.remove(uri);
        }
    }

    public boolean isUnusedOutputDestination(DocumentKey uri) {
        return this.allOutputDestinations == null || !this.allOutputDestinations.contains(uri);
    }

    public void setInitialTemplateParameters(Map<StructuredQName, Sequence> params, boolean tunnel) {
        if (tunnel) {
            this.initialTemplateTunnelParams = params;
        } else {
            this.initialTemplateParams = params;
        }
    }

    public Map<StructuredQName, Sequence> getInitialTemplateParameters(boolean tunnel) {
        return tunnel ? this.initialTemplateTunnelParams : this.initialTemplateParams;
    }

    public void setMessageFactory(Supplier<Receiver> messageReceiverFactory) {
        this.messageFactory = messageReceiverFactory;
    }

    public void setMessageReceiverClassName(String name) {
        if (!name.equals(MessageEmitter.class.getName())) {
            this.messageFactory = () -> {
                try {
                    Object messageReceiver = this.getConfiguration().getInstance(name, null);
                    if (!(messageReceiver instanceof Receiver)) {
                        throw new XPathException(name + " is not a Receiver");
                    }
                    return (Receiver)messageReceiver;
                } catch (XPathException e) {
                    throw new UncheckedXPathException(e);
                }
            };
        }
    }

    public Receiver makeMessageReceiver() {
        return this.messageFactory.get();
    }

    public void setMessageEmitter(Receiver receiver) {
        if (this.getConfiguration().getBooleanProperty(Feature.ALLOW_MULTITHREADING)) {
            throw new IllegalStateException("XsltController#setMessageEmitter() is not supported for a configuration that allows multi-threading. Use setMessageFactory() instead");
        }
        Receiver messageReceiver = this.explicitMessageReceiver = receiver;
        receiver.setPipelineConfiguration(this.makePipelineConfiguration());
        if (receiver instanceof Emitter && ((Emitter)receiver).getOutputProperties() == null) {
            try {
                Properties props = new Properties();
                props.setProperty("method", "xml");
                props.setProperty("indent", "yes");
                props.setProperty("omit-xml-declaration", "yes");
                ((Emitter)receiver).setOutputProperties(props);
            } catch (XPathException xPathException) {
                // empty catch block
            }
        }
        this.setMessageFactory(() -> new ProxyReceiver(messageReceiver){

            @Override
            public void close() {
            }
        });
    }

    public Receiver getMessageEmitter() {
        return this.explicitMessageReceiver;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void incrementMessageCounter(StructuredQName code) {
        Map<StructuredQName, Integer> map = this.messageCounters;
        synchronized (map) {
            Integer c = this.messageCounters.get(code);
            int n = c == null ? 1 : c + 1;
            this.messageCounters.put(code, n);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Map<StructuredQName, Integer> getMessageCounters() {
        Map<StructuredQName, Integer> map = this.messageCounters;
        synchronized (map) {
            return new HashMap<StructuredQName, Integer>(this.messageCounters);
        }
    }

    public void setOutputURIResolver(OutputURIResolver resolver) {
        OutputURIResolver our = resolver == null ? this.getConfiguration().getOutputURIResolver() : resolver;
        this.setResultDocumentResolver(new OutputURIResolverWrapper(our));
    }

    public ResultDocumentResolver getResultDocumentResolver() {
        return this.resultDocumentResolver;
    }

    public void setResultDocumentResolver(ResultDocumentResolver resultDocumentResolver) {
        this.resultDocumentResolver = resultDocumentResolver;
    }

    public OutputURIResolver getOutputURIResolver() {
        if (this.resultDocumentResolver instanceof OutputURIResolverWrapper) {
            return ((OutputURIResolverWrapper)this.resultDocumentResolver).getOutputURIResolver();
        }
        return this.getConfiguration().getOutputURIResolver();
    }

    public void setPrincipalDestination(Destination destination) {
        this.principalDestination = destination;
    }

    public Destination getPrincipalDestination() {
        return this.principalDestination;
    }

    public boolean isAssertionsEnabled() {
        return this.assertionsEnabled;
    }

    public void setAssertionsEnabled(boolean enabled) {
        this.assertionsEnabled = enabled;
    }

    @Override
    public void preEvaluateGlobals(XPathContext context) throws XPathException {
        this.openMessageEmitter();
        super.preEvaluateGlobals(context);
        this.closeMessageEmitter();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void applyTemplates(Sequence source, Receiver out) throws XPathException {
        this.checkReadiness();
        this.openMessageEmitter();
        try {
            ComplexContentOutputter dest = this.prepareOutputReceiver(out);
            XPathContextMajor initialContext = this.newXPathContext();
            initialContext.createThreadManager();
            initialContext.setOrigin(this);
            boolean close = false;
            Mode mode = this.getInitialMode();
            if (mode == null) {
                throw new XPathException("Requested initial mode " + (this.initialMode == null ? "#unnamed" : this.initialMode.getActor().getModeName().getDisplayName()) + " does not exist", "XTDE0045");
            }
            if (!((PreparedStylesheet)this.executable).isEligibleInitialMode(this.initialMode)) {
                throw new XPathException("Requested initial mode " + mode.getModeName().getDisplayName() + " is not public or final", "XTDE0045");
            }
            this.warningIfStreamable(mode);
            boolean mustClose = false;
            ParameterSet ordinaryParams = null;
            if (this.initialTemplateParams != null) {
                ordinaryParams = new ParameterSet(this.initialTemplateParams);
            }
            ParameterSet tunnelParams = null;
            if (this.initialTemplateTunnelParams != null) {
                tunnelParams = new ParameterSet(this.initialTemplateTunnelParams);
            }
            SequenceIterator iter = source.iterate();
            MappingFunction preprocessor = this.getInputPreprocessor(mode);
            iter = new MappingIterator(iter, preprocessor);
            initialContext.trackFocus(iter);
            initialContext.setCurrentMode(this.initialMode);
            initialContext.setCurrentComponent(this.initialMode);
            for (TailCall tc = mode.applyTemplates(ordinaryParams, tunnelParams, null, dest, initialContext, Loc.NONE); tc != null; tc = tc.processLeavingTail()) {
            }
            initialContext.waitForChildThreads();
            dest.close();
        } catch (TerminationException err) {
            if (!err.hasBeenReported()) {
                this.reportFatalError(err);
            }
            throw err;
        } catch (UncheckedXPathException err) {
            this.handleXPathException(err.getXPathException());
        } catch (XPathException err) {
            this.handleXPathException(err);
        } finally {
            this.inUse = false;
            this.closeMessageEmitter();
            if (this.traceListener != null) {
                this.traceListener.close();
            }
            this.principalResultURI = null;
        }
    }

    private ComplexContentOutputter prepareOutputReceiver(Receiver out) throws XPathException {
        this.principalResult = out;
        if (this.principalResultURI == null) {
            this.principalResultURI = out.getSystemId();
        }
        if (this.getExecutable().createsSecondaryResult()) {
            this.gatekeeper = new PrincipalOutputGatekeeper(this, out);
            out = this.gatekeeper;
        }
        ComplexContentOutputter cco = new ComplexContentOutputter(out);
        cco.setSystemId(out.getSystemId());
        cco.open();
        return cco;
    }

    public PrincipalOutputGatekeeper getGatekeeper() {
        return this.gatekeeper;
    }

    private MappingFunction getInputPreprocessor(Mode finalMode) {
        return item -> {
            if (item instanceof NodeInfo) {
                GenericTreeInfo strippedDoc;
                TreeInfo docInfo;
                NodeInfo node = (NodeInfo)item;
                if (node.getConfiguration() == null) {
                    throw new XPathException("The supplied source document must be associated with a Configuration");
                }
                if (!node.getConfiguration().isCompatible(this.executable.getConfiguration())) {
                    throw new XPathException("Source document and stylesheet must use the same or compatible Configurations", "SXXP0004");
                }
                if (node.getTreeInfo().isTyped() && !this.executable.isSchemaAware()) {
                    throw new XPathException("Cannot use a schema-validated source document unless the stylesheet is schema-aware");
                }
                if (this.isStylesheetStrippingTypeAnnotations() && node != this.globalContextItem && (docInfo = node.getTreeInfo()).isTyped()) {
                    strippedDoc = new TypeStrippedDocument(docInfo);
                    node = ((TypeStrippedDocument)strippedDoc).wrap(node);
                }
                SpaceStrippingRule spaceStrippingRule = this.getSpaceStrippingRule();
                if (this.isStylesheetContainingStripSpace() && this.isStripSourceTree() && !(node instanceof SpaceStrippedNode) && node != this.globalContextItem && node.getTreeInfo().getSpaceStrippingRule() != spaceStrippingRule) {
                    strippedDoc = new SpaceStrippedDocument(node.getTreeInfo(), spaceStrippingRule);
                    if (!SpaceStrippedNode.isPreservedNode(node, (SpaceStrippedDocument)strippedDoc, node.getParent())) {
                        return EmptyIterator.emptyIterator();
                    }
                    node = ((SpaceStrippedDocument)strippedDoc).wrap(node);
                }
                if (this.getAccumulatorManager() != null) {
                    this.getAccumulatorManager().setApplicableAccumulators(node.getTreeInfo(), finalMode.getAccumulators());
                }
                return SingletonIterator.makeIterator(node);
            }
            return SingletonIterator.makeIterator(item);
        };
    }

    private void warningIfStreamable(Mode mode) {
        if (mode.isDeclaredStreamable()) {
            this.warning((this.initialMode == null ? "" : this.getInitialMode().getModeTitle()) + " is streamable, but the input is not supplied as a stream", "SXWN9000", Loc.NONE);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void callTemplate(StructuredQName initialTemplateName, Receiver out) throws XPathException {
        this.checkReadiness();
        this.openMessageEmitter();
        try {
            StylesheetPackage pack;
            Component initialComponent;
            ComplexContentOutputter dest = this.prepareOutputReceiver(out);
            XPathContextMajor initialContext = this.newXPathContext();
            initialContext.createThreadManager();
            initialContext.setOrigin(this);
            if (this.globalContextItem != null) {
                initialContext.setCurrentIterator(new ManualIterator(this.globalContextItem));
            }
            ParameterSet ordinaryParams = null;
            if (this.initialTemplateParams != null) {
                ordinaryParams = new ParameterSet(this.initialTemplateParams);
            }
            ParameterSet tunnelParams = null;
            if (this.initialTemplateTunnelParams != null) {
                tunnelParams = new ParameterSet(this.initialTemplateTunnelParams);
            }
            if ((initialComponent = (pack = (StylesheetPackage)this.executable.getTopLevelPackage()).getComponent(new SymbolicName(200, initialTemplateName))) == null) {
                throw new XPathException("Template " + initialTemplateName.getDisplayName() + " does not exist", "XTDE0040");
            }
            if (!pack.isImplicitPackage() && initialComponent.getVisibility() != Visibility.PUBLIC && initialComponent.getVisibility() != Visibility.FINAL) {
                throw new XPathException("Template " + initialTemplateName.getDisplayName() + " is " + initialComponent.getVisibility().show(), "XTDE0040");
            }
            NamedTemplate t = (NamedTemplate)initialComponent.getActor();
            XPathContextMajor c2 = initialContext.newContext();
            initialContext.setOrigin(this);
            c2.setCurrentComponent(initialComponent);
            c2.openStackFrame(t.getStackFrameMap());
            c2.setLocalParameters(ordinaryParams);
            c2.setTunnelParameters(tunnelParams);
            for (TailCall tc = t.expand(dest, c2); tc != null; tc = tc.processLeavingTail()) {
            }
            initialContext.waitForChildThreads();
            dest.close();
        } catch (UncheckedXPathException err) {
            this.handleXPathException(err.getXPathException());
        } catch (XPathException err) {
            this.handleXPathException(err);
        } finally {
            if (this.traceListener != null) {
                this.traceListener.close();
            }
            this.closeMessageEmitter();
            this.inUse = false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void applyStreamingTemplates(Source source, Receiver out) throws XPathException {
        this.checkReadiness();
        this.openMessageEmitter();
        ComplexContentOutputter dest = this.prepareOutputReceiver(out);
        boolean close = false;
        try {
            XPathContextMajor initialContext;
            block23: {
                Configuration config;
                Source s2;
                int validationMode = this.getSchemaValidationMode();
                Source underSource = source;
                if (source instanceof AugmentedSource) {
                    close = ((AugmentedSource)source).isPleaseCloseAfterUse();
                    int localValidate = ((AugmentedSource)source).getSchemaValidation();
                    if (localValidate != 0) {
                        validationMode = localValidate;
                    }
                    underSource = ((AugmentedSource)source).getContainedSource();
                }
                if ((s2 = (config = this.getConfiguration()).getSourceResolver().resolveSource(underSource, config)) != null) {
                    underSource = s2;
                }
                if (!(source instanceof SAXSource || source instanceof StreamSource || source instanceof EventSource)) {
                    throw new IllegalArgumentException("Streaming requires a SAXSource, StreamSource, or EventSource");
                }
                if (!this.initialMode.getActor().isDeclaredStreamable()) {
                    throw new IllegalArgumentException("Initial mode is not streamable");
                }
                if (source instanceof SAXSource && config.getBooleanProperty(Feature.IGNORE_SAX_SOURCE_PARSER)) {
                    ((SAXSource)source).setXMLReader(null);
                }
                initialContext = this.newXPathContext();
                initialContext.createThreadManager();
                initialContext.setOrigin(this);
                ParameterSet ordinaryParams = null;
                if (this.initialTemplateParams != null) {
                    ordinaryParams = new ParameterSet(this.initialTemplateParams);
                }
                ParameterSet tunnelParams = null;
                if (this.initialTemplateTunnelParams != null) {
                    tunnelParams = new ParameterSet(this.initialTemplateTunnelParams);
                }
                Receiver despatcher = config.makeStreamingTransformer(this.initialMode.getActor(), ordinaryParams, tunnelParams, dest, initialContext);
                if (config.isStripsAllWhiteSpace() || this.isStylesheetContainingStripSpace()) {
                    despatcher = this.makeStripper(despatcher);
                }
                PipelineConfiguration pipe = despatcher.getPipelineConfiguration();
                pipe.getParseOptions().setSchemaValidationMode(this.validationMode);
                boolean verbose = this.getConfiguration().isTiming();
                if (verbose) {
                    this.getConfiguration().getLogger().info("Streaming " + source.getSystemId());
                }
                try {
                    Sender.send(source, despatcher, null);
                } catch (QuitParsingException e) {
                    if (!verbose) break block23;
                    this.getConfiguration().getLogger().info("Streaming " + source.getSystemId() + " : early exit");
                }
            }
            initialContext.waitForChildThreads();
            dest.close();
        } catch (TerminationException err) {
            if (!err.hasBeenReported()) {
                this.reportFatalError(err);
            }
            throw err;
        } catch (UncheckedXPathException err) {
            this.handleXPathException(err.getXPathException());
        } catch (XPathException err) {
            this.handleXPathException(err);
        } finally {
            this.inUse = false;
            if (close && source instanceof AugmentedSource) {
                ((AugmentedSource)source).close();
            }
            if (this.traceListener != null) {
                this.traceListener.close();
            }
        }
    }

    public Receiver getStreamingReceiver(Mode mode, Receiver result) throws XPathException {
        this.checkReadiness();
        this.openMessageEmitter();
        ComplexContentOutputter dest = this.prepareOutputReceiver(result);
        XPathContextMajor initialContext = this.newXPathContext();
        initialContext.setOrigin(this);
        this.globalContextItem = null;
        if (!mode.isDeclaredStreamable()) {
            throw new XPathException("mode supplied to getStreamingReceiver() must be streamable");
        }
        Configuration config = this.getConfiguration();
        Receiver despatcher = config.makeStreamingTransformer(mode, null, null, dest, initialContext);
        if (despatcher == null) {
            throw new XPathException("Streaming requires Saxon-EE");
        }
        if (config.isStripsAllWhiteSpace() || this.isStylesheetContainingStripSpace()) {
            despatcher = this.makeStripper(despatcher);
        }
        despatcher.setPipelineConfiguration(this.makePipelineConfiguration());
        final ComplexContentOutputter finalResult = dest;
        return new ProxyReceiver(despatcher){

            @Override
            public void close() throws XPathException {
                if (XsltController.this.traceListener != null) {
                    XsltController.this.traceListener.close();
                }
                XsltController.this.closeMessageEmitter();
                finalResult.close();
                XsltController.this.inUse = false;
            }
        };
    }

    private void openMessageEmitter() throws XPathException {
        if (this.explicitMessageReceiver != null) {
            this.explicitMessageReceiver.open();
            if (this.explicitMessageReceiver instanceof Emitter && ((Emitter)this.explicitMessageReceiver).getWriter() == null) {
                ((Emitter)this.explicitMessageReceiver).setStreamResult(this.getConfiguration().getLogger().asStreamResult());
            }
        }
    }

    private void closeMessageEmitter() throws XPathException {
        if (this.explicitMessageReceiver != null) {
            this.explicitMessageReceiver.close();
        }
    }

    public synchronized Stack<AttributeSet> getAttributeSetEvaluationStack() {
        long thread = Thread.currentThread().getId();
        return this.attributeSetEvaluationStacks.computeIfAbsent(thread, k -> new Stack());
    }

    public synchronized void releaseAttributeSetEvaluationStack() {
        long thread = Thread.currentThread().getId();
        this.attributeSetEvaluationStacks.remove(thread);
    }
}

