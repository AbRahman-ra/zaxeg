/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Version;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.ContentHandlerProxy;
import net.sf.saxon.event.EventSource;
import net.sf.saxon.event.FilterFactory;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Sender;
import net.sf.saxon.expr.EarlyEvaluationContext;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.PJConverter;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.PendingUpdateList;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.accum.AccumulatorRegistry;
import net.sf.saxon.expr.compat.TypeChecker10;
import net.sf.saxon.expr.instruct.Debugger;
import net.sf.saxon.expr.instruct.EvaluateInstr;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.MemoFunction;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.instruct.ResultDocument;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.SourceDocument;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.instruct.WithParam;
import net.sf.saxon.expr.number.Numberer_en;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ICompilerService;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.OptimizerOptions;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.expr.sort.AlphanumericCollator;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.expr.sort.HTML5CaseBlindCollator;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.IntegratedFunctionLibrary;
import net.sf.saxon.functions.MathFunctionSet;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.functions.registry.ConstructorFunctionLibrary;
import net.sf.saxon.functions.registry.ExsltCommonFunctionSet;
import net.sf.saxon.functions.registry.UseWhen30FunctionSet;
import net.sf.saxon.functions.registry.VendorFunctionSetHE;
import net.sf.saxon.functions.registry.XPath30FunctionSet;
import net.sf.saxon.functions.registry.XPath31FunctionSet;
import net.sf.saxon.functions.registry.XSLT30FunctionSet;
import net.sf.saxon.lib.AugmentedSource;
import net.sf.saxon.lib.CollationURIResolver;
import net.sf.saxon.lib.CollectionFinder;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.lib.EnvironmentVariableResolver;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.ExternalObjectModel;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.FunctionAnnotationHandler;
import net.sf.saxon.lib.InvalidityReportGenerator;
import net.sf.saxon.lib.LocalizerFactory;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.lib.Numberer;
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.ProtocolRestricter;
import net.sf.saxon.lib.ResourceCollection;
import net.sf.saxon.lib.ResourceFactory;
import net.sf.saxon.lib.SchemaURIResolver;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.lib.SourceResolver;
import net.sf.saxon.lib.StandardCollationURIResolver;
import net.sf.saxon.lib.StandardEntityResolver;
import net.sf.saxon.lib.StandardEnvironmentVariableResolver;
import net.sf.saxon.lib.StandardErrorReporter;
import net.sf.saxon.lib.StandardLogger;
import net.sf.saxon.lib.StandardURIChecker;
import net.sf.saxon.lib.StandardURIResolver;
import net.sf.saxon.lib.StandardUnparsedTextResolver;
import net.sf.saxon.lib.StaticQueryContextFactory;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.lib.XQueryFunctionAnnotationHandler;
import net.sf.saxon.ma.arrays.ArrayFunctionSet;
import net.sf.saxon.ma.map.MapFunctionSet;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.AllElementsSpaceStrippingRule;
import net.sf.saxon.om.DocumentPool;
import net.sf.saxon.om.FocusTrackingIterator;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.IgnorableSpaceStrippingRule;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NotationSet;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.pattern.PatternParser30;
import net.sf.saxon.pull.PullSource;
import net.sf.saxon.query.QueryModule;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.query.XQueryParser;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.resource.BinaryResource;
import net.sf.saxon.resource.JSONResource;
import net.sf.saxon.resource.StandardCollectionFinder;
import net.sf.saxon.resource.UnknownResource;
import net.sf.saxon.resource.UnparsedTextResource;
import net.sf.saxon.resource.XmlResource;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.sapling.SaplingDocument;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.serialize.charcode.CharacterSetFactory;
import net.sf.saxon.serialize.charcode.XMLCharacterData;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleNodeFactory;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.style.XSLEvaluate;
import net.sf.saxon.style.XSLTemplate;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trace.TraceCodeInjector;
import net.sf.saxon.trace.XSLTTraceCodeInjector;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.trans.ConfigurationReader;
import net.sf.saxon.trans.DynamicLoader;
import net.sf.saxon.trans.FunctionStreamability;
import net.sf.saxon.trans.LicenseException;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.PackageLoaderHE;
import net.sf.saxon.trans.SimpleMode;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.TypeAliasManager;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.trans.XmlProcessingIncident;
import net.sf.saxon.trans.packages.IPackageLoader;
import net.sf.saxon.tree.tiny.TreeStatistics;
import net.sf.saxon.tree.util.DocumentNumberAllocator;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.BuiltInType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SchemaDeclaration;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.StringToDouble;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.Closure;
import net.sf.saxon.value.MemoClosure;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringToDouble11;
import net.sf.saxon.z.IntHashSet;
import net.sf.saxon.z.IntSet;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.LexicalHandler;

public class Configuration
implements SourceResolver,
NotationSet {
    protected static Set<Feature> booleanFeatures = new HashSet<Feature>(40);
    private transient ApiProvider apiProcessor = null;
    private transient CharacterSetFactory characterSetFactory;
    private Map<String, StringCollator> collationMap = new HashMap<String, StringCollator>(10);
    private CollationURIResolver collationResolver = new StandardCollationURIResolver();
    private String defaultCollationName = "http://www.w3.org/2005/xpath-functions/collation/codepoint";
    private Predicate<URI> allowedUriTest = uri -> true;
    private final StandardCollectionFinder standardCollectionFinder = new StandardCollectionFinder();
    private CollectionFinder collectionFinder = this.standardCollectionFinder;
    private EnvironmentVariableResolver environmentVariableResolver = new StandardEnvironmentVariableResolver();
    private String defaultCollection = null;
    private ParseOptions defaultParseOptions = new ParseOptions();
    protected transient StaticQueryContext defaultStaticQueryContext;
    private StaticQueryContextFactory staticQueryContextFactory = new StaticQueryContextFactory();
    protected OptimizerOptions optimizerOptions = OptimizerOptions.FULL_HE_OPTIMIZATION;
    protected CompilerInfo defaultXsltCompilerInfo = this.makeCompilerInfo();
    private java.util.function.Function<Configuration, ? extends ErrorReporter> errorReporterFactory = config -> {
        StandardErrorReporter reporter = new StandardErrorReporter();
        reporter.setLogger(config.getLogger());
        return reporter;
    };
    private String label = null;
    private DocumentNumberAllocator documentNumberAllocator = new DocumentNumberAllocator();
    private transient Debugger debugger = null;
    private String defaultLanguage = Locale.getDefault().getLanguage();
    private String defaultCountry = Locale.getDefault().getCountry();
    private Properties defaultOutputProperties = new Properties();
    private transient DynamicLoader dynamicLoader = new DynamicLoader();
    private IntSet enabledProperties = new IntHashSet(64);
    private String zipUriPattern = null;
    private List<ExternalObjectModel> externalObjectModels = new ArrayList<ExternalObjectModel>(4);
    protected IndependentContext staticContextForSystemFunctions;
    private DocumentPool globalDocumentPool = new DocumentPool();
    private IntegratedFunctionLibrary integratedFunctionLibrary = new IntegratedFunctionLibrary();
    private transient LocalizerFactory localizerFactory;
    private NamePool namePool = new NamePool();
    protected Optimizer optimizer = null;
    private SerializerFactory serializerFactory = new SerializerFactory(this);
    private volatile ConcurrentLinkedQueue<XMLReader> sourceParserPool = new ConcurrentLinkedQueue();
    private volatile ConcurrentLinkedQueue<XMLReader> styleParserPool = new ConcurrentLinkedQueue();
    private String sourceParserClass;
    private transient SourceResolver sourceResolver = this;
    private transient Logger traceOutput = new StandardLogger();
    private ModuleURIResolver standardModuleURIResolver = Version.platform.makeStandardModuleURIResolver(this);
    private String styleParserClass;
    private final StandardURIResolver systemURIResolver = new StandardURIResolver(this);
    private UnparsedTextURIResolver unparsedTextURIResolver = new StandardUnparsedTextResolver();
    private transient XPathContext theConversionContext = null;
    private ConversionRules theConversionRules = null;
    private transient TraceListener traceListener = null;
    private String traceListenerClass = null;
    private String traceListenerOutput = null;
    private String defaultRegexEngine = "S";
    protected transient TypeHierarchy typeHierarchy;
    private TypeChecker typeChecker = new TypeChecker();
    private TypeChecker10 typeChecker10 = new TypeChecker10();
    private transient URIResolver uriResolver;
    protected FunctionLibraryList builtInExtensionLibraryList;
    protected int xsdVersion = 11;
    private int xmlVersion = 10;
    private int xpathVersionForXsd = 20;
    private int xpathVersionForXslt = 31;
    private Comparator<String> mediaQueryEvaluator = (o1, o2) -> 0;
    private Map<String, String> fileExtensions = new HashMap<String, String>();
    private Map<String, ResourceFactory> resourceFactoryMapping = new HashMap<String, ResourceFactory>();
    private Map<String, FunctionAnnotationHandler> functionAnnotationHandlers = new HashMap<String, FunctionAnnotationHandler>();
    protected int byteCodeThreshold = 100;
    private int regexBacktrackingLimit = 10000000;
    private TreeStatistics treeStatistics = new TreeStatistics();
    public static final int XML10 = 10;
    public static final int XML11 = 11;
    public static final int XSD10 = 10;
    public static final int XSD11 = 11;
    private static LexicalHandler dummyLexicalHandler = new DefaultHandler2();

    public Configuration() {
        this.init();
    }

    public static Configuration newConfiguration() {
        Class<Configuration> configurationClass = Configuration.class;
        try {
            return (Configuration)configurationClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot instantiate a Configuration", e);
        }
    }

    public static InputStream locateResource(String filename, List<String> messages, List<ClassLoader> loaders) {
        URL url;
        filename = "net/sf/saxon/data/" + filename;
        ClassLoader loader = null;
        try {
            loader = Thread.currentThread().getContextClassLoader();
        } catch (Exception err) {
            messages.add("Failed to getContextClassLoader() - continuing\n");
        }
        InputStream in = null;
        if (loader != null) {
            URL u = loader.getResource(filename);
            in = loader.getResourceAsStream(filename);
            if (in == null) {
                messages.add("Cannot read " + filename + " file located using ClassLoader " + loader + " - continuing\n");
            }
        }
        if (in == null && (loader = Configuration.class.getClassLoader()) != null && (in = loader.getResourceAsStream(filename)) == null) {
            messages.add("Cannot read " + filename + " file located using ClassLoader " + loader + " - continuing\n");
        }
        if (in == null && (url = ClassLoader.getSystemResource(filename)) != null) {
            try {
                in = url.openStream();
            } catch (IOException ioe) {
                messages.add("IO error " + ioe.getMessage() + " reading " + filename + " located using getSystemResource(): using defaults");
                in = null;
            }
        }
        loaders.add(loader);
        return in;
    }

    public static StreamSource locateResourceSource(String filename, List<String> messages, List<ClassLoader> loaders) {
        ClassLoader loader = null;
        try {
            loader = Thread.currentThread().getContextClassLoader();
        } catch (Exception err) {
            messages.add("Failed to getContextClassLoader() - continuing\n");
        }
        InputStream in = null;
        URL url = null;
        if (loader != null) {
            url = loader.getResource(filename);
            in = loader.getResourceAsStream(filename);
            if (in == null) {
                messages.add("Cannot read " + filename + " file located using ClassLoader " + loader + " - continuing\n");
            }
        }
        if (in == null && (loader = Configuration.class.getClassLoader()) != null && (in = loader.getResourceAsStream(filename)) == null) {
            messages.add("Cannot read " + filename + " file located using ClassLoader " + loader + " - continuing\n");
        }
        loaders.add(loader);
        return new StreamSource(in, url.toString());
    }

    public static Configuration readConfiguration(Source source) throws XPathException {
        Configuration tempConfig = Configuration.newConfiguration();
        return tempConfig.readConfigurationFile(source);
    }

    public static Configuration readConfiguration(Source source, Configuration baseConfiguration) throws XPathException {
        Configuration tempConfig = Configuration.newConfiguration();
        return tempConfig.readConfigurationFile(source, baseConfiguration);
    }

    public static Configuration instantiateConfiguration(String className, ClassLoader classLoader) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<?> theClass;
        ClassLoader loader = classLoader;
        if (loader == null) {
            try {
                loader = Thread.currentThread().getContextClassLoader();
            } catch (Exception err) {
                System.err.println("Failed to getContextClassLoader() - continuing");
            }
        }
        if (loader != null) {
            try {
                theClass = loader.loadClass(className);
            } catch (Exception ex) {
                theClass = Class.forName(className);
            }
        } else {
            theClass = Class.forName(className);
        }
        return (Configuration)theClass.newInstance();
    }

    public static boolean isAssertionsEnabled() {
        boolean assertsEnabled = false;
        if (!$assertionsDisabled) {
            assertsEnabled = true;
            if (!true) {
                throw new AssertionError();
            }
        }
        return assertsEnabled;
    }

    protected Configuration readConfigurationFile(Source source) throws XPathException {
        return new ConfigurationReader().makeConfiguration(source);
    }

    protected Configuration readConfigurationFile(Source source, Configuration baseConfiguration) throws XPathException {
        ConfigurationReader reader = this.makeConfigurationReader();
        reader.setBaseConfiguration(baseConfiguration);
        return reader.makeConfiguration(source);
    }

    protected ConfigurationReader makeConfigurationReader() {
        return new ConfigurationReader();
    }

    protected void init() {
        Version.platform.initialize(this);
        this.defaultXsltCompilerInfo.setURIResolver(this.getSystemURIResolver());
        StandardEntityResolver resolver = new StandardEntityResolver(this);
        this.defaultParseOptions.setEntityResolver(resolver);
        this.internalSetBooleanProperty(Feature.PREFER_JAXP_PARSER, true);
        this.internalSetBooleanProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, true);
        this.internalSetBooleanProperty(Feature.DISABLE_XSL_EVALUATE, false);
        this.registerFileExtension("xml", "application/xml");
        this.registerFileExtension("html", "application/html");
        this.registerFileExtension("atom", "application/atom");
        this.registerFileExtension("xsl", "application/xml+xslt");
        this.registerFileExtension("xslt", "application/xml+xslt");
        this.registerFileExtension("xsd", "application/xml+xsd");
        this.registerFileExtension("txt", "text/plain");
        this.registerFileExtension("MF", "text/plain");
        this.registerFileExtension("class", "application/java");
        this.registerFileExtension("json", "application/json");
        this.registerFileExtension("", "application/unknown");
        this.registerMediaType("application/xml", XmlResource.FACTORY);
        this.registerMediaType("text/xml", XmlResource.FACTORY);
        this.registerMediaType("application/html", XmlResource.FACTORY);
        this.registerMediaType("text/html", XmlResource.FACTORY);
        this.registerMediaType("application/atom", XmlResource.FACTORY);
        this.registerMediaType("application/xml+xslt", XmlResource.FACTORY);
        this.registerMediaType("application/xml+xsd", XmlResource.FACTORY);
        this.registerMediaType("application/rdf+xml", XmlResource.FACTORY);
        this.registerMediaType("text/plain", UnparsedTextResource.FACTORY);
        this.registerMediaType("application/java", BinaryResource.FACTORY);
        this.registerMediaType("application/binary", BinaryResource.FACTORY);
        this.registerMediaType("application/json", JSONResource.FACTORY);
        this.registerMediaType("application/unknown", UnknownResource.FACTORY);
        this.registerFunctionAnnotationHandler(new XQueryFunctionAnnotationHandler());
    }

    public static Configuration makeLicensedConfiguration(ClassLoader classLoader, String className) throws RuntimeException {
        if (className == null) {
            className = "com.saxonica.config.ProfessionalConfiguration";
        }
        try {
            return Configuration.instantiateConfiguration(className, classLoader);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public void importLicenseDetails(Configuration config) {
    }

    public String getEditionCode() {
        return "HE";
    }

    public void setProcessor(ApiProvider processor) {
        this.apiProcessor = processor;
    }

    public ApiProvider getProcessor() {
        return this.apiProcessor;
    }

    public String getProductTitle() {
        return "Saxon-" + this.getEditionCode() + " " + Version.getProductVersion() + Version.platform.getPlatformSuffix() + " from Saxonica";
    }

    public void checkLicensedFeature(int feature, String name, int localLicenseId) throws LicenseException {
        String require = feature == 8 ? "PE" : "EE";
        String message = "Requested feature (" + name + ") requires Saxon-" + require;
        if (!Version.softwareEdition.equals("HE")) {
            message = message + ". You are using Saxon-" + Version.softwareEdition + " software, but the Configuration is an instance of " + this.getClass().getName() + "; to use this feature you need to create an instance of " + (feature == 8 ? "com.saxonica.config.ProfessionalConfiguration" : "com.saxonica.config.EnterpriseConfiguration");
        }
        throw new LicenseException(message, 6);
    }

    public void disableLicensing() {
    }

    public boolean isFeatureAllowedBySecondaryLicense(int localLicenseId, int feature) {
        return false;
    }

    public boolean isLicensedFeature(int feature) {
        return false;
    }

    public String getLicenseFeature(String name) {
        return null;
    }

    public void displayLicenseMessage() {
    }

    public int registerLocalLicense(String dmk) {
        return -1;
    }

    public void setDynamicLoader(DynamicLoader dynamicLoader) {
        this.dynamicLoader = dynamicLoader;
    }

    public DynamicLoader getDynamicLoader() {
        return this.dynamicLoader;
    }

    public Class getClass(String className, boolean tracing, ClassLoader classLoader) throws XPathException {
        return this.dynamicLoader.getClass(className, tracing ? this.traceOutput : null, classLoader);
    }

    public Object getInstance(String className, ClassLoader classLoader) throws XPathException {
        return this.dynamicLoader.getInstance(className, this.isTiming() ? this.traceOutput : null, classLoader);
    }

    public void setAllowedUriTest(Predicate<URI> test) {
        this.allowedUriTest = test;
    }

    public Predicate<URI> getAllowedUriTest() {
        return this.allowedUriTest;
    }

    public URIResolver getURIResolver() {
        if (this.uriResolver == null) {
            return this.systemURIResolver;
        }
        return this.uriResolver;
    }

    public void setURIResolver(URIResolver resolver) {
        this.uriResolver = resolver;
        if (resolver instanceof StandardURIResolver) {
            ((StandardURIResolver)resolver).setConfiguration(this);
        }
        this.defaultXsltCompilerInfo.setURIResolver(resolver);
    }

    public void setParameterizedURIResolver() {
        this.getSystemURIResolver().setRecognizeQueryParameters(true);
    }

    public StandardURIResolver getSystemURIResolver() {
        return this.systemURIResolver;
    }

    public URIResolver makeURIResolver(String className) throws TransformerException {
        Object obj = this.dynamicLoader.getInstance(className, null);
        if (obj instanceof StandardURIResolver) {
            ((StandardURIResolver)obj).setConfiguration(this);
        }
        if (obj instanceof URIResolver) {
            return (URIResolver)obj;
        }
        throw new XPathException("Class " + className + " is not a URIResolver");
    }

    public void setErrorReporterFactory(java.util.function.Function<Configuration, ? extends ErrorReporter> factory) {
        this.errorReporterFactory = factory;
    }

    public ErrorReporter makeErrorReporter() {
        return this.errorReporterFactory.apply(this);
    }

    public Logger getLogger() {
        return this.traceOutput;
    }

    public void reportFatalError(XPathException err) {
        if (!err.hasBeenReported()) {
            this.makeErrorReporter().report(new XmlProcessingException(err));
            err.setHasBeenReported(true);
        }
    }

    public void setLogger(Logger logger) {
        this.traceOutput = logger;
    }

    public void setStandardErrorOutput(PrintStream out) {
        if (this.traceOutput instanceof StandardLogger) {
            ((StandardLogger)this.traceOutput).setPrintStream(out);
        }
    }

    public PrintStream getStandardErrorOutput() {
        if (this.traceOutput instanceof StandardLogger) {
            return ((StandardLogger)this.traceOutput).getPrintStream();
        }
        return System.err;
    }

    public void setXMLVersion(int version) {
        this.xmlVersion = version;
        this.theConversionRules = null;
    }

    public int getXMLVersion() {
        return this.xmlVersion;
    }

    public ParseOptions getParseOptions() {
        return this.defaultParseOptions;
    }

    public void setMediaQueryEvaluator(Comparator<String> comparator) {
        this.mediaQueryEvaluator = comparator;
    }

    public Comparator<String> getMediaQueryEvaluator() {
        return this.mediaQueryEvaluator;
    }

    public void setConversionRules(ConversionRules rules) {
        this.theConversionRules = rules;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ConversionRules getConversionRules() {
        if (this.theConversionRules == null) {
            Configuration configuration = this;
            synchronized (configuration) {
                ConversionRules cv = new ConversionRules();
                cv.setTypeHierarchy(this.getTypeHierarchy());
                cv.setNotationSet(this);
                if (this.xsdVersion == 10) {
                    cv.setStringToDoubleConverter(StringToDouble.getInstance());
                    cv.setURIChecker(StandardURIChecker.getInstance());
                } else {
                    cv.setStringToDoubleConverter(StringToDouble11.getInstance());
                }
                cv.setAllowYearZero(this.xsdVersion != 10);
                this.theConversionRules = cv;
                return this.theConversionRules;
            }
        }
        return this.theConversionRules;
    }

    public int getXsdVersion() {
        return this.xsdVersion;
    }

    public XPathContext getConversionContext() {
        if (this.theConversionContext == null) {
            this.theConversionContext = new EarlyEvaluationContext(this);
        }
        return this.theConversionContext;
    }

    public IntPredicate getValidCharacterChecker() {
        if (this.xmlVersion == 10) {
            return XMLCharacterData::isValid10;
        }
        return XMLCharacterData::isValid11;
    }

    public int getTreeModel() {
        return this.defaultParseOptions.getModel().getSymbolicValue();
    }

    public void setTreeModel(int treeModel) {
        this.defaultParseOptions.setModel(TreeModel.getTreeModel(treeModel));
    }

    public boolean isLineNumbering() {
        return this.defaultParseOptions.isLineNumbering();
    }

    public void setLineNumbering(boolean lineNumbering) {
        this.defaultParseOptions.setLineNumbering(lineNumbering);
    }

    public void setXIncludeAware(boolean state) {
        this.defaultParseOptions.setXIncludeAware(state);
    }

    public boolean isXIncludeAware() {
        return this.defaultParseOptions.isXIncludeAware();
    }

    public TraceListener getTraceListener() {
        return this.traceListener;
    }

    public TraceListener makeTraceListener() throws XPathException {
        if (this.traceListener != null) {
            return this.traceListener;
        }
        if (this.traceListenerClass != null) {
            try {
                return this.makeTraceListener(this.traceListenerClass);
            } catch (ClassCastException e) {
                throw new XPathException(e);
            }
        }
        return null;
    }

    public void setTraceListener(TraceListener traceListener) {
        this.traceListener = traceListener;
        this.setCompileWithTracing(traceListener != null);
        this.internalSetBooleanProperty(Feature.ALLOW_MULTITHREADING, false);
    }

    public void setTraceListenerClass(String className) {
        if (className == null) {
            this.traceListenerClass = null;
            this.setCompileWithTracing(false);
        } else {
            try {
                this.makeTraceListener(className);
            } catch (XPathException err) {
                throw new IllegalArgumentException(className + ": " + err.getMessage());
            }
            this.traceListenerClass = className;
            this.setCompileWithTracing(true);
        }
    }

    public String getTraceListenerClass() {
        return this.traceListenerClass;
    }

    public void setTraceListenerOutputFile(String filename) {
        this.traceListenerOutput = filename;
    }

    public String getTraceListenerOutputFile() {
        return this.traceListenerOutput;
    }

    public boolean isCompileWithTracing() {
        return this.getBooleanProperty(Feature.COMPILE_WITH_TRACING);
    }

    public void setCompileWithTracing(boolean trace) {
        this.internalSetBooleanProperty(Feature.COMPILE_WITH_TRACING, trace);
        if (this.defaultXsltCompilerInfo != null) {
            if (trace) {
                this.defaultXsltCompilerInfo.setCodeInjector(new XSLTTraceCodeInjector());
            } else {
                this.defaultXsltCompilerInfo.setCodeInjector(null);
            }
        }
        if (this.defaultStaticQueryContext != null) {
            if (trace) {
                this.defaultStaticQueryContext.setCodeInjector(new TraceCodeInjector());
            } else {
                this.defaultStaticQueryContext.setCodeInjector(null);
            }
        }
    }

    public TraceListener makeTraceListener(String className) throws XPathException {
        Object obj = this.dynamicLoader.getInstance(className, null);
        if (obj instanceof TraceListener) {
            String destination = this.getTraceListenerOutputFile();
            if (destination != null) {
                try {
                    ((TraceListener)obj).setOutputDestination(new StandardLogger(new PrintStream(destination)));
                } catch (FileNotFoundException e) {
                    throw new XPathException(e);
                }
            }
            return (TraceListener)obj;
        }
        throw new XPathException("Class " + className + " is not a TraceListener");
    }

    public BuiltInFunctionSet getXSLT30FunctionSet() {
        return XSLT30FunctionSet.getInstance();
    }

    public BuiltInFunctionSet getUseWhenFunctionSet() {
        return UseWhen30FunctionSet.getInstance();
    }

    public BuiltInFunctionSet getXPath30FunctionSet() {
        return XPath30FunctionSet.getInstance();
    }

    public BuiltInFunctionSet getXPath31FunctionSet() {
        return XPath31FunctionSet.getInstance();
    }

    public BuiltInFunctionSet getXQueryUpdateFunctionSet() {
        return null;
    }

    public SystemFunction makeSystemFunction(String localName, int arity) {
        try {
            return this.getXSLT30FunctionSet().makeFunction(localName, arity);
        } catch (XPathException e) {
            return null;
        }
    }

    public void registerExtensionFunction(ExtensionFunctionDefinition function) {
        this.integratedFunctionLibrary.registerFunction(function);
    }

    public IntegratedFunctionLibrary getIntegratedFunctionLibrary() {
        return this.integratedFunctionLibrary;
    }

    public FunctionLibraryList getBuiltInExtensionLibraryList() {
        if (this.builtInExtensionLibraryList == null) {
            this.builtInExtensionLibraryList = new FunctionLibraryList();
            this.builtInExtensionLibraryList.addFunctionLibrary(VendorFunctionSetHE.getInstance());
            this.builtInExtensionLibraryList.addFunctionLibrary(MathFunctionSet.getInstance());
            this.builtInExtensionLibraryList.addFunctionLibrary(MapFunctionSet.getInstance());
            this.builtInExtensionLibraryList.addFunctionLibrary(ArrayFunctionSet.getInstance());
            this.builtInExtensionLibraryList.addFunctionLibrary(ExsltCommonFunctionSet.getInstance());
        }
        return this.builtInExtensionLibraryList;
    }

    public SystemFunction bindSaxonExtensionFunction(String localName, int arity) throws XPathException {
        throw new UnsupportedOperationException("The extension function saxon:" + localName + "#" + arity + " requires Saxon-PE or higher");
    }

    public void addExtensionBinders(FunctionLibraryList list) {
    }

    public Function getSystemFunction(StructuredQName name, int arity) {
        try {
            if (this.staticContextForSystemFunctions == null) {
                this.staticContextForSystemFunctions = new IndependentContext(this);
            }
            FunctionLibraryList lib = new FunctionLibraryList();
            lib.addFunctionLibrary(XPath31FunctionSet.getInstance());
            lib.addFunctionLibrary(this.getBuiltInExtensionLibraryList());
            lib.addFunctionLibrary(new ConstructorFunctionLibrary(this));
            lib.addFunctionLibrary(this.getIntegratedFunctionLibrary());
            SymbolicName.F symbolicName = new SymbolicName.F(name, arity);
            return lib.getFunctionItem(symbolicName, this.staticContextForSystemFunctions);
        } catch (XPathException e) {
            return null;
        }
    }

    public UserFunction newUserFunction(boolean memoFunction, FunctionStreamability streamability) {
        if (memoFunction) {
            return new MemoFunction();
        }
        return new UserFunction();
    }

    public void registerCollation(String collationURI, StringCollator collator) {
        this.collationMap.put(collationURI, collator);
    }

    public void setCollationURIResolver(CollationURIResolver resolver) {
        this.collationResolver = resolver;
    }

    public CollationURIResolver getCollationURIResolver() {
        return this.collationResolver;
    }

    public StringCollator getCollation(String collationName) throws XPathException {
        if (collationName == null || collationName.equals("http://www.w3.org/2005/xpath-functions/collation/codepoint")) {
            return CodepointCollator.getInstance();
        }
        if (collationName.equals("http://www.w3.org/2005/xpath-functions/collation/html-ascii-case-insensitive")) {
            return HTML5CaseBlindCollator.getInstance();
        }
        if (collationName.startsWith("http://saxon.sf.net/collation/alphaNumeric?base=")) {
            return new AlphanumericCollator(this.getCollation(collationName.substring("http://saxon.sf.net/collation/alphaNumeric?base=".length())));
        }
        StringCollator collator = this.collationMap.get(collationName);
        if (collator == null) {
            collator = this.getCollationURIResolver().resolve(collationName, this);
        }
        return collator;
    }

    public StringCollator getCollation(String collationURI, String baseURI) throws XPathException {
        if (collationURI.equals("http://www.w3.org/2005/xpath-functions/collation/codepoint")) {
            return CodepointCollator.getInstance();
        }
        try {
            String absoluteURI = ResolveURI.makeAbsolute(collationURI, baseURI).toString();
            return this.getCollation(absoluteURI);
        } catch (URISyntaxException e) {
            throw new XPathException("Collation name is not a valid URI: " + collationURI + " (base = " + baseURI + ")", "FOCH0002");
        }
    }

    public StringCollator getCollation(String collationURI, String baseURI, String errorCode) throws XPathException {
        if (collationURI.equals("http://www.w3.org/2005/xpath-functions/collation/codepoint")) {
            return CodepointCollator.getInstance();
        }
        try {
            StringCollator collator;
            String absoluteURI = collationURI;
            if (baseURI != null) {
                absoluteURI = ResolveURI.makeAbsolute(collationURI, baseURI).toString();
            }
            if ((collator = this.getCollation(absoluteURI)) == null) {
                throw new XPathException("Unknown collation " + absoluteURI, errorCode);
            }
            return collator;
        } catch (URISyntaxException e) {
            throw new XPathException("Collation name is not a valid URI: " + collationURI + " (base = " + baseURI + ")", errorCode);
        }
    }

    public String getDefaultCollationName() {
        return this.defaultCollationName;
    }

    public void setDefaultCollection(String uri) {
        this.defaultCollection = uri;
    }

    public String getDefaultCollection() {
        return this.defaultCollection;
    }

    public void setCollectionFinder(CollectionFinder cf) {
        this.collectionFinder = cf;
    }

    public CollectionFinder getCollectionFinder() {
        return this.collectionFinder;
    }

    public StandardCollectionFinder getStandardCollectionFinder() {
        return this.standardCollectionFinder;
    }

    public void registerCollection(String collectionURI, ResourceCollection collection) {
        this.standardCollectionFinder.registerCollection(collectionURI, collection);
        if (this.collectionFinder instanceof StandardCollectionFinder && this.collectionFinder != this.standardCollectionFinder) {
            ((StandardCollectionFinder)this.collectionFinder).registerCollection(collectionURI, collection);
        }
    }

    public void registerFileExtension(String extension, String mediaType) {
        this.fileExtensions.put(extension, mediaType);
    }

    public void registerMediaType(String contentType, ResourceFactory factory) {
        this.resourceFactoryMapping.put(contentType, factory);
    }

    public String getMediaTypeForFileExtension(String extension) {
        String mediaType = this.fileExtensions.get(extension);
        if (mediaType == null) {
            mediaType = this.fileExtensions.get("");
        }
        return mediaType;
    }

    public ResourceFactory getResourceFactoryForMediaType(String mediaType) {
        return this.resourceFactoryMapping.get(mediaType);
    }

    public void setLocalizerFactory(LocalizerFactory factory) {
        this.localizerFactory = factory;
    }

    public LocalizerFactory getLocalizerFactory() {
        return this.localizerFactory;
    }

    public void setDefaultLanguage(String language) {
        ValidationFailure vf = StringConverter.StringToLanguage.INSTANCE.validate(language);
        if (vf != null) {
            throw new IllegalArgumentException("The default language must be a valid language code");
        }
        this.defaultLanguage = language;
    }

    public String getDefaultLanguage() {
        return this.defaultLanguage;
    }

    public void setDefaultCountry(String country) {
        this.defaultCountry = country;
    }

    public String getDefaultCountry() {
        return this.defaultCountry;
    }

    public void setDefaultRegexEngine(String engine) {
        if (!("J".equals(engine) || "N".equals(engine) || "S".equals(engine))) {
            throw new IllegalArgumentException("Regex engine must be S|J|N");
        }
        this.defaultRegexEngine = engine;
    }

    public String getDefaultRegexEngine() {
        return this.defaultRegexEngine;
    }

    public RegularExpression compileRegularExpression(CharSequence regex, String flags, String hostLanguage, List<String> warnings) throws XPathException {
        return Version.platform.compileRegularExpression(this, regex, flags, hostLanguage, warnings);
    }

    public Numberer makeNumberer(String language, String country) {
        if (this.localizerFactory == null) {
            Numberer_en numberer = new Numberer_en();
            if (language != null) {
                numberer.setLanguage(language);
            }
            if (country != null) {
                numberer.setCountry(country);
            }
            return numberer;
        }
        Numberer numberer = this.localizerFactory.getNumberer(language, country);
        if (numberer == null) {
            numberer = new Numberer_en();
        }
        return numberer;
    }

    public void setModuleURIResolver(ModuleURIResolver resolver) {
        this.getDefaultStaticQueryContext().setModuleURIResolver(resolver);
    }

    public void setModuleURIResolver(String className) throws TransformerException {
        Object obj = this.dynamicLoader.getInstance(className, null);
        if (!(obj instanceof ModuleURIResolver)) {
            throw new XPathException("Class " + className + " is not a ModuleURIResolver");
        }
        this.setModuleURIResolver((ModuleURIResolver)obj);
    }

    public ModuleURIResolver getModuleURIResolver() {
        return this.getDefaultStaticQueryContext().getModuleURIResolver();
    }

    public ModuleURIResolver getStandardModuleURIResolver() {
        return this.standardModuleURIResolver;
    }

    public UnparsedTextURIResolver getUnparsedTextURIResolver() {
        return this.unparsedTextURIResolver;
    }

    public void setUnparsedTextURIResolver(UnparsedTextURIResolver resolver) {
        this.unparsedTextURIResolver = resolver;
    }

    public CompilerInfo getDefaultXsltCompilerInfo() {
        return this.defaultXsltCompilerInfo;
    }

    public StaticQueryContext getDefaultStaticQueryContext() {
        if (this.defaultStaticQueryContext == null) {
            this.defaultStaticQueryContext = this.makeStaticQueryContext(false);
        }
        return this.defaultStaticQueryContext;
    }

    protected StaticQueryContext makeStaticQueryContext(boolean copyFromDefault) {
        return this.staticQueryContextFactory.newStaticQueryContext(this, copyFromDefault);
    }

    public void registerFunctionAnnotationHandler(FunctionAnnotationHandler handler) {
        this.functionAnnotationHandlers.put(handler.getAssertionNamespace(), handler);
    }

    public FunctionAnnotationHandler getFunctionAnnotationHandler(String namespace) {
        return this.functionAnnotationHandlers.get(namespace);
    }

    public boolean isStreamabilityEnabled() {
        return false;
    }

    public String getMessageEmitterClass() {
        return this.defaultXsltCompilerInfo.getMessageReceiverClassName();
    }

    public void setMessageEmitterClass(String messageReceiverClassName) {
        this.defaultXsltCompilerInfo.setMessageReceiverClassName(messageReceiverClassName);
    }

    public String getSourceParserClass() {
        return this.sourceParserClass;
    }

    public void setSourceParserClass(String sourceParserClass) {
        this.sourceParserClass = sourceParserClass;
    }

    public String getStyleParserClass() {
        return this.styleParserClass;
    }

    public void setStyleParserClass(String parser) {
        this.styleParserClass = parser;
    }

    public OutputURIResolver getOutputURIResolver() {
        return this.defaultXsltCompilerInfo.getOutputURIResolver();
    }

    public void setOutputURIResolver(OutputURIResolver outputURIResolver) {
        this.defaultXsltCompilerInfo.setOutputURIResolver(outputURIResolver);
    }

    public void setSerializerFactory(SerializerFactory factory) {
        this.serializerFactory = factory;
    }

    public SerializerFactory getSerializerFactory() {
        return this.serializerFactory;
    }

    public CharacterSetFactory getCharacterSetFactory() {
        if (this.characterSetFactory == null) {
            this.characterSetFactory = new CharacterSetFactory();
        }
        return this.characterSetFactory;
    }

    public void setDefaultSerializationProperties(Properties props) {
        this.defaultOutputProperties = props;
    }

    public Properties getDefaultSerializationProperties() {
        return this.defaultOutputProperties;
    }

    public SerializationProperties obtainDefaultSerializationProperties() {
        return new SerializationProperties(this.defaultOutputProperties);
    }

    public void processResultDocument(ResultDocument instruction, Expression content, XPathContext context) throws XPathException {
        instruction.processInstruction(content, context);
    }

    public SequenceIterator getMultithreadedItemMappingIterator(SequenceIterator base, ItemMappingFunction action) throws XPathException {
        return new ItemMappingIterator(base, action);
    }

    public boolean isTiming() {
        return this.enabledProperties.contains(77);
    }

    public void setTiming(boolean timing) {
        if (timing) {
            this.enabledProperties.add(77);
        } else {
            this.enabledProperties.remove(77);
        }
    }

    public boolean isVersionWarning() {
        return false;
    }

    public void setVersionWarning(boolean warn) {
    }

    public boolean isValidation() {
        return this.defaultParseOptions.getDTDValidationMode() == 1 || this.defaultParseOptions.getDTDValidationMode() == 2;
    }

    public void setValidation(boolean validation) {
        this.defaultParseOptions.setDTDValidationMode(validation ? 1 : 4);
    }

    public FilterFactory makeDocumentProjector(PathMap.PathMapRoot map) {
        throw new UnsupportedOperationException("Document projection requires Saxon-EE");
    }

    public FilterFactory makeDocumentProjector(XQueryExpression exp) {
        throw new UnsupportedOperationException("Document projection requires Saxon-EE");
    }

    public int getSchemaValidationMode() {
        return this.defaultParseOptions.getSchemaValidationMode();
    }

    public void setSchemaValidationMode(int validationMode) {
        this.defaultParseOptions.setSchemaValidationMode(validationMode);
    }

    public void setValidationWarnings(boolean warn) {
        this.defaultParseOptions.setContinueAfterValidationErrors(warn);
    }

    public boolean isValidationWarnings() {
        return this.defaultParseOptions.isContinueAfterValidationErrors();
    }

    public void setExpandAttributeDefaults(boolean expand) {
        this.defaultParseOptions.setExpandAttributeDefaults(expand);
    }

    public boolean isExpandAttributeDefaults() {
        return this.defaultParseOptions.isExpandAttributeDefaults();
    }

    public NamePool getNamePool() {
        return this.namePool;
    }

    public void setNamePool(NamePool targetNamePool) {
        this.namePool = targetNamePool;
    }

    public TypeHierarchy getTypeHierarchy() {
        if (this.typeHierarchy == null) {
            this.typeHierarchy = new TypeHierarchy(this);
        }
        return this.typeHierarchy;
    }

    public TypeChecker getTypeChecker(boolean backwardsCompatible) {
        if (backwardsCompatible) {
            return this.typeChecker10;
        }
        return this.typeChecker;
    }

    public TypeAliasManager makeTypeAliasManager() {
        return new TypeAliasManager();
    }

    public DocumentNumberAllocator getDocumentNumberAllocator() {
        return this.documentNumberAllocator;
    }

    public void setDocumentNumberAllocator(DocumentNumberAllocator allocator) {
        this.documentNumberAllocator = allocator;
    }

    public boolean isCompatible(Configuration other) {
        return this.namePool == other.namePool && this.documentNumberAllocator == other.documentNumberAllocator;
    }

    public DocumentPool getGlobalDocumentPool() {
        return this.globalDocumentPool;
    }

    public boolean isStripsAllWhiteSpace() {
        return this.defaultParseOptions.getSpaceStrippingRule() == AllElementsSpaceStrippingRule.getInstance();
    }

    public XMLReader createXMLParser() {
        XMLReader parser = this.getSourceParserClass() != null ? this.makeParser(this.getSourceParserClass()) : Configuration.loadParser();
        return parser;
    }

    public XMLReader getSourceParser() throws TransformerFactoryConfigurationError {
        XMLReader parser;
        if (this.sourceParserPool == null) {
            this.sourceParserPool = new ConcurrentLinkedQueue();
        }
        if ((parser = this.sourceParserPool.poll()) != null) {
            return parser;
        }
        parser = this.getSourceParserClass() != null ? this.makeParser(this.getSourceParserClass()) : Configuration.loadParser();
        if (this.isTiming()) {
            this.reportParserDetails(parser);
        }
        try {
            Sender.configureParser(parser);
        } catch (XPathException err) {
            throw new TransformerFactoryConfigurationError(err);
        }
        if (this.isValidation()) {
            try {
                parser.setFeature("http://xml.org/sax/features/validation", true);
            } catch (SAXException err) {
                throw new TransformerFactoryConfigurationError("The XML parser does not support validation");
            }
        }
        return parser;
    }

    private void reportParserDetails(XMLReader reader) {
        String name = reader.getClass().getName();
        this.traceOutput.info("Using parser " + name);
    }

    public synchronized void reuseSourceParser(XMLReader parser) {
        if (this.sourceParserPool == null) {
            this.sourceParserPool = new ConcurrentLinkedQueue();
        }
        try {
            try {
                parser.setContentHandler(null);
                if (parser.getEntityResolver() == this.defaultParseOptions.getEntityResolver()) {
                    parser.setEntityResolver(null);
                }
                parser.setDTDHandler(null);
                parser.setErrorHandler(null);
                parser.setProperty("http://xml.org/sax/properties/lexical-handler", dummyLexicalHandler);
            } catch (SAXNotRecognizedException | SAXNotSupportedException sAXException) {
                // empty catch block
            }
            this.sourceParserPool.offer(parser);
        } catch (Exception exception) {
            // empty catch block
        }
    }

    private static XMLReader loadParser() {
        return Version.platform.loadParser();
    }

    public synchronized XMLReader getStyleParser() throws TransformerFactoryConfigurationError {
        XMLReader parser;
        if (this.styleParserPool == null) {
            this.styleParserPool = new ConcurrentLinkedQueue();
        }
        if ((parser = this.styleParserPool.poll()) != null) {
            return parser;
        }
        if (this.getStyleParserClass() != null) {
            parser = this.makeParser(this.getStyleParserClass());
        } else {
            parser = Configuration.loadParser();
            StandardEntityResolver resolver = new StandardEntityResolver(this);
            parser.setEntityResolver(resolver);
        }
        try {
            parser.setFeature("http://xml.org/sax/features/namespaces", true);
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            throw new TransformerFactoryConfigurationError(e);
        }
        return parser;
    }

    public synchronized void reuseStyleParser(XMLReader parser) {
        if (this.styleParserPool == null) {
            this.styleParserPool = new ConcurrentLinkedQueue();
        }
        try {
            try {
                parser.setContentHandler(null);
                parser.setDTDHandler(null);
                parser.setErrorHandler(null);
                parser.setProperty("http://xml.org/sax/properties/lexical-handler", dummyLexicalHandler);
            } catch (SAXNotRecognizedException | SAXNotSupportedException sAXException) {
                // empty catch block
            }
            this.styleParserPool.offer(parser);
        } catch (Exception exception) {
            // empty catch block
        }
    }

    public void loadSchema(String absoluteURI) throws SchemaException {
        this.readSchema(this.makePipelineConfiguration(), "", absoluteURI, null);
    }

    public String readSchema(PipelineConfiguration pipe, String baseURI, String schemaLocation, String expected) throws SchemaException {
        this.needEnterpriseEdition();
        return null;
    }

    public void readMultipleSchemas(PipelineConfiguration pipe, String baseURI, Collection<String> schemaLocations, String expected) throws SchemaException {
        this.needEnterpriseEdition();
    }

    public String readInlineSchema(NodeInfo root, String expected, ErrorReporter errorReporter) throws SchemaException {
        this.needEnterpriseEdition();
        return null;
    }

    protected void needEnterpriseEdition() {
        throw new UnsupportedOperationException("You need the Enterprise Edition of Saxon (with an EnterpriseConfiguration) for this operation");
    }

    public void addSchemaSource(Source schemaSource) throws SchemaException {
        this.addSchemaSource(schemaSource, this.makeErrorReporter());
    }

    public void addSchemaSource(Source schemaSource, ErrorReporter errorReporter) throws SchemaException {
        this.needEnterpriseEdition();
    }

    public void addSchemaForBuiltInNamespace(String namespace) {
    }

    public boolean isSchemaAvailable(String targetNamespace) {
        return false;
    }

    public void clearSchemaCache() {
    }

    public Set<String> getImportedNamespaces() {
        return Collections.emptySet();
    }

    public void sealNamespace(String namespace) {
    }

    public Collection<GlobalParam> getDeclaredSchemaParameters() {
        return null;
    }

    public Iterator<? extends SchemaType> getExtensionsOfType(SchemaType type) {
        return Collections.emptyIterator();
    }

    public void importComponents(Source source) throws XPathException {
        this.needEnterpriseEdition();
    }

    public void exportComponents(Receiver out) throws XPathException {
        this.needEnterpriseEdition();
    }

    public Function getSchemaAsFunctionItem() {
        return null;
    }

    public Function getSchemaComponentAsFunctionItem(String kind, QNameValue name) throws XPathException {
        return null;
    }

    public SchemaDeclaration getElementDeclaration(int fingerprint) {
        return null;
    }

    public SchemaDeclaration getElementDeclaration(StructuredQName qName) {
        return null;
    }

    public SchemaDeclaration getAttributeDeclaration(int fingerprint) {
        return null;
    }

    public SchemaDeclaration getAttributeDeclaration(StructuredQName attributeName) {
        return null;
    }

    public SchemaType getSchemaType(StructuredQName name) {
        if (name.hasURI("http://www.w3.org/2001/XMLSchema")) {
            return BuiltInType.getSchemaTypeByLocalName(name.getLocalPart());
        }
        return null;
    }

    public ItemType makeUserUnionType(List<AtomicType> memberTypes) {
        return null;
    }

    @Override
    public boolean isDeclaredNotation(String uri, String local) {
        return false;
    }

    public void checkTypeDerivationIsOK(SchemaType derived, SchemaType base, int block) throws SchemaException {
    }

    public void prepareValidationReporting(XPathContext context, ParseOptions options) {
    }

    public Receiver getDocumentValidator(Receiver receiver, String systemId, ParseOptions validationOptions, Location initiatingLocation) {
        return receiver;
    }

    public Receiver getElementValidator(Receiver receiver, ParseOptions validationOptions, Location locationId) throws XPathException {
        return receiver;
    }

    public SimpleType validateAttribute(StructuredQName nodeName, CharSequence value, int validation) throws ValidationException, MissingComponentException {
        return BuiltInAtomicType.UNTYPED_ATOMIC;
    }

    public Receiver getAnnotationStripper(Receiver destination) {
        return destination;
    }

    public XMLReader makeParser(String className) throws TransformerFactoryConfigurationError {
        try {
            Object obj = this.dynamicLoader.getInstance(className, null);
            if (obj instanceof XMLReader) {
                return (XMLReader)obj;
            }
            if (obj instanceof SAXParserFactory) {
                try {
                    SAXParser saxParser = ((SAXParserFactory)obj).newSAXParser();
                    return saxParser.getXMLReader();
                } catch (ParserConfigurationException | SAXException e) {
                    throw new XPathException(e);
                }
            }
        } catch (XPathException err) {
            throw new TransformerFactoryConfigurationError(err);
        }
        throw new TransformerFactoryConfigurationError("Class " + className + " is not a SAX2 XMLReader or SAXParserFactory");
    }

    public XPathParser newExpressionParser(String language, boolean updating, int languageVersion) throws XPathException {
        if ("XQ".equals(language)) {
            if (updating) {
                throw new XPathException("XQuery Update is supported only in Saxon-EE");
            }
            if (languageVersion == 31 || languageVersion == 30 || languageVersion == 10) {
                XQueryParser parser = new XQueryParser();
                parser.setLanguage(XPathParser.ParsedLanguage.XQUERY, 31);
                return parser;
            }
            throw new XPathException("Unknown XQuery version " + languageVersion);
        }
        if ("XP".equals(language)) {
            if (languageVersion == 31 || languageVersion == 30 || languageVersion == 305 || languageVersion == 20) {
                XPathParser parser = new XPathParser();
                parser.setLanguage(XPathParser.ParsedLanguage.XPATH, languageVersion);
                return parser;
            }
            throw new XPathException("Unknown XPath version " + languageVersion);
        }
        if ("PATTERN".equals(language)) {
            if (languageVersion == 30 || languageVersion == 20 || languageVersion == 305 || languageVersion == 31) {
                return new PatternParser30();
            }
            throw new XPathException("Unknown XPath version " + languageVersion);
        }
        throw new XPathException("Unknown expression language " + language);
    }

    public ExpressionPresenter newExpressionExporter(String target, OutputStream destination, StylesheetPackage rootPackage) throws XPathException {
        throw new XPathException("Exporting a stylesheet requires Saxon-EE");
    }

    public void setDebugger(Debugger debugger) {
        this.debugger = debugger;
    }

    public Debugger getDebugger() {
        return this.debugger;
    }

    public SlotManager makeSlotManager() {
        if (this.debugger == null) {
            return new SlotManager();
        }
        return this.debugger.makeSlotManager();
    }

    public Receiver makeStreamingTransformer(Mode mode, ParameterSet ordinaryParams, ParameterSet tunnelParams, Outputter output, XPathContext context) throws XPathException {
        throw new XPathException("Streaming is only available in Saxon-EE");
    }

    public Expression makeStreamInstruction(Expression hrefExp, Expression body, boolean streaming, ParseOptions options, PackageData packageData, Location location, RetainedStaticContext rsc) throws XPathException {
        SourceDocument si = new SourceDocument(hrefExp, body, options);
        si.setLocation(location);
        si.setRetainedStaticContext(rsc);
        return si;
    }

    public java.util.function.Function<SequenceIterator, FocusTrackingIterator> getFocusTrackerFactory(Executable exec, boolean multithreaded) {
        return FocusTrackingIterator::new;
    }

    public void checkStrictStreamability(XSLTemplate template, Expression body) throws XPathException {
    }

    public boolean isStreamedNode(NodeInfo node) {
        return false;
    }

    public OptimizerOptions getOptimizerOptions() {
        return this.optimizerOptions.intersect(OptimizerOptions.FULL_HE_OPTIMIZATION);
    }

    public Optimizer obtainOptimizer() {
        if (this.optimizer == null) {
            this.optimizer = new Optimizer(this);
            this.optimizer.setOptimizerOptions(this.optimizerOptions.intersect(OptimizerOptions.FULL_HE_OPTIMIZATION));
            return this.optimizer;
        }
        return this.optimizer;
    }

    public Optimizer obtainOptimizer(OptimizerOptions options) {
        Optimizer optimizer = new Optimizer(this);
        optimizer.setOptimizerOptions(options.intersect(OptimizerOptions.FULL_HE_OPTIMIZATION));
        return optimizer;
    }

    public ContextItemStaticInfo makeContextItemStaticInfo(ItemType itemType, boolean maybeUndefined) {
        return new ContextItemStaticInfo(itemType, maybeUndefined);
    }

    public ContextItemStaticInfo getDefaultContextItemStaticInfo() {
        return ContextItemStaticInfo.DEFAULT;
    }

    public XQueryExpression makeXQueryExpression(Expression exp, QueryModule mainModule, boolean streaming) throws XPathException {
        XQueryExpression xqe = new XQueryExpression(exp, mainModule, false);
        if (mainModule.getCodeInjector() != null) {
            mainModule.getCodeInjector().process(xqe);
        }
        return xqe;
    }

    public Sequence makeClosure(Expression expression, int ref, XPathContext context) throws XPathException {
        if (this.getBooleanProperty(Feature.EAGER_EVALUATION)) {
            SequenceIterator iter = expression.iterate(context);
            return iter.materialize();
        }
        Closure closure = ref > 1 ? new MemoClosure() : new Closure();
        closure.setExpression(expression);
        closure.setSavedXPathContext(context.newContext());
        closure.saveContext(expression, context);
        return closure;
    }

    public GroundedValue makeSequenceExtent(Expression expression, int ref, XPathContext context) throws XPathException {
        return expression.iterate(context).materialize();
    }

    public StyleNodeFactory makeStyleNodeFactory(Compilation compilation) {
        return new StyleNodeFactory(this, compilation);
    }

    public Expression makeEvaluateInstruction(XSLEvaluate source, ComponentDeclaration decl) throws XPathException {
        Expression xpath = source.getTargetExpression();
        SequenceType requiredType = source.getRequiredType();
        Expression contextItem = source.getContextItemExpression();
        Expression baseUri = source.getBaseUriExpression();
        Expression namespaceContext = source.getNamespaceContextExpression();
        Expression schemaAware = source.getSchemaAwareExpression();
        Expression withParams = source.getWithParamsExpression();
        EvaluateInstr inst = new EvaluateInstr(xpath, requiredType, contextItem, baseUri, namespaceContext, schemaAware);
        WithParam[] params = source.getWithParamInstructions(inst, source.getCompilation(), decl, false);
        inst.setActualParams(params);
        inst.setDynamicParams(withParams);
        inst.setDefaultXPathNamespace(source.getDefaultXPathNamespace());
        inst.setOptionsExpression(source.getOptionsExpression());
        return inst;
    }

    public StylesheetPackage makeStylesheetPackage() {
        return new StylesheetPackage(this);
    }

    public AccumulatorRegistry makeAccumulatorRegistry() {
        return new AccumulatorRegistry();
    }

    public void registerExternalObjectModel(ExternalObjectModel model) {
        try {
            this.getClass(model.getDocumentClassName(), false, null);
        } catch (XPathException e) {
            return;
        }
        if (this.externalObjectModels == null) {
            this.externalObjectModels = new ArrayList<ExternalObjectModel>(4);
        }
        if (!this.externalObjectModels.contains(model)) {
            this.externalObjectModels.add(model);
        }
    }

    public ExternalObjectModel getExternalObjectModel(String uri) {
        for (ExternalObjectModel model : this.externalObjectModels) {
            if (!model.getIdentifyingURI().equals(uri)) continue;
            return model;
        }
        return null;
    }

    public ExternalObjectModel getExternalObjectModel(Class<?> nodeClass) {
        for (ExternalObjectModel model : this.externalObjectModels) {
            PJConverter converter = model.getPJConverter(nodeClass);
            if (converter == null) continue;
            return model;
        }
        return null;
    }

    public List<ExternalObjectModel> getExternalObjectModels() {
        return this.externalObjectModels;
    }

    public synchronized JavaExternalObjectType getJavaExternalObjectType(Class<?> theClass) {
        return new JavaExternalObjectType(this, theClass);
    }

    public Map<String, Function> makeMethodMap(Class javaClass, String required) {
        throw new UnsupportedOperationException();
    }

    public MapItem externalObjectAsMap(ObjectValue<?> value, String required) {
        throw new UnsupportedOperationException();
    }

    public Expression makeObjectLookupExpression(Expression lhs, Expression rhs) throws XPathException {
        throw new UnsupportedOperationException();
    }

    public NodeInfo unravel(Source source) {
        List<ExternalObjectModel> externalObjectModels = this.getExternalObjectModels();
        if (!(source instanceof NodeInfo)) {
            for (ExternalObjectModel model : externalObjectModels) {
                NodeInfo node = model.unravel(source, this);
                if (node == null) continue;
                if (!node.getConfiguration().isCompatible(this)) {
                    throw new IllegalArgumentException("Externally supplied Node belongs to the wrong Configuration");
                }
                return node;
            }
        }
        if (source instanceof NodeInfo) {
            if (!((NodeInfo)source).getConfiguration().isCompatible(this)) {
                throw new IllegalArgumentException("Externally supplied NodeInfo belongs to the wrong Configuration");
            }
            return (NodeInfo)source;
        }
        throw new IllegalArgumentException("A source of class " + source.getClass() + " is not recognized by any registered object model");
    }

    public boolean isExtensionElementAvailable(StructuredQName qName) {
        return false;
    }

    public void setStaticQueryContextFactory(StaticQueryContextFactory factory) {
        this.staticQueryContextFactory = factory;
    }

    public StaticQueryContext newStaticQueryContext() {
        return this.makeStaticQueryContext(true);
    }

    public PendingUpdateList newPendingUpdateList() {
        throw new UnsupportedOperationException("XQuery update is supported only in Saxon-EE");
    }

    public PipelineConfiguration makePipelineConfiguration() {
        PipelineConfiguration pipe = new PipelineConfiguration(this);
        pipe.setURIResolver(this.getURIResolver());
        pipe.setParseOptions(new ParseOptions(this.defaultParseOptions));
        pipe.setErrorReporter(this.makeErrorReporter());
        return pipe;
    }

    public SchemaURIResolver makeSchemaURIResolver(URIResolver resolver) {
        return null;
    }

    public static Configuration getConfiguration(XPathContext context) {
        return context.getConfiguration();
    }

    public void setSourceResolver(SourceResolver resolver) {
        this.sourceResolver = resolver;
    }

    public SourceResolver getSourceResolver() {
        return this.sourceResolver;
    }

    @Override
    public Source resolveSource(Source source, Configuration config) throws XPathException {
        if (source instanceof AugmentedSource) {
            return source;
        }
        if (source instanceof StreamSource) {
            return source;
        }
        if (source instanceof SAXSource) {
            return source;
        }
        if (source instanceof DOMSource) {
            return source;
        }
        if (source instanceof NodeInfo) {
            return source;
        }
        if (source instanceof PullSource) {
            return source;
        }
        if (source instanceof StAXSource) {
            return source;
        }
        if (source instanceof EventSource) {
            return source;
        }
        if (source instanceof SaplingDocument) {
            return source;
        }
        return null;
    }

    public TreeInfo buildDocumentTree(Source source) throws XPathException {
        if (source == null) {
            throw new NullPointerException("source");
        }
        if (source instanceof AugmentedSource) {
            return this.buildDocumentTree(((AugmentedSource)source).getContainedSource(), ((AugmentedSource)source).getParseOptions());
        }
        return this.buildDocumentTree(source, new ParseOptions(this.defaultParseOptions));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public TreeInfo buildDocumentTree(Source source, ParseOptions parseOptions) throws XPathException {
        if (source == null) {
            throw new NullPointerException("source");
        }
        boolean finallyClose = false;
        try {
            ParseOptions options = new ParseOptions(parseOptions);
            Source src2 = this.resolveSource(source, this);
            if (src2 == null) {
                throw new XPathException("Unknown source class " + source.getClass().getName());
            }
            source = src2;
            if (source instanceof AugmentedSource) {
                options.merge(((AugmentedSource)source).getParseOptions());
            }
            options.applyDefaults(this);
            finallyClose = options.isPleaseCloseAfterUse();
            TreeModel treeModel = options.getModel();
            boolean lineNumbering = options.isLineNumbering();
            PipelineConfiguration pipe = this.makePipelineConfiguration();
            pipe.setParseOptions(options);
            Builder builder = treeModel.makeBuilder(pipe);
            builder.setTiming(this.isTiming());
            builder.setLineNumbering(lineNumbering);
            builder.setPipelineConfiguration(pipe);
            builder.setSystemId(source.getSystemId());
            Sender.send(source, builder, options);
            NodeInfo newdoc = builder.getCurrentRoot();
            if (newdoc.getNodeKind() != 9) {
                throw new XPathException("Source object represents a node other than a document node");
            }
            builder.reset();
            TreeInfo treeInfo = newdoc.getTreeInfo();
            return treeInfo;
        } finally {
            if (finallyClose) {
                ParseOptions.close(source);
            }
        }
    }

    public TreeStatistics getTreeStatistics() {
        return this.treeStatistics;
    }

    public Receiver makeEmitter(String clarkName, Properties props) throws XPathException {
        Object handler;
        int brace = clarkName.indexOf(125);
        String localName = clarkName.substring(brace + 1);
        int colon = localName.indexOf(58);
        String className = localName.substring(colon + 1);
        try {
            handler = this.dynamicLoader.getInstance(className, null);
        } catch (XPathException e) {
            throw new XPathException("Cannot create user-supplied output method. " + e.getMessage(), "SXCH0004");
        }
        if (handler instanceof Receiver) {
            return (Receiver)handler;
        }
        if (handler instanceof ContentHandler) {
            ContentHandlerProxy emitter = new ContentHandlerProxy();
            emitter.setUnderlyingContentHandler((ContentHandler)handler);
            emitter.setOutputProperties(props);
            return emitter;
        }
        throw new XPathException("Output method " + className + " is neither a Receiver nor a SAX2 ContentHandler");
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void setConfigurationProperty(String name, Object value) {
        Feature<?> feature = Feature.byName(name);
        if (feature == null) {
            if (name.startsWith("http://saxon.sf.net/feature/parserFeature?uri=")) {
                String uri = name.substring("http://saxon.sf.net/feature/parserFeature?uri=".length());
                try {
                    uri = URLDecoder.decode(uri, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException(e);
                }
                this.defaultParseOptions.addParserFeature(uri, Configuration.requireBoolean(name, value));
                return;
            }
            if (!name.startsWith("http://saxon.sf.net/feature/parserProperty?uri=")) throw new IllegalArgumentException("Unrecognized configuration feature: " + name);
            String uri = name.substring("http://saxon.sf.net/feature/parserProperty?uri=".length());
            try {
                uri = URLDecoder.decode(uri, "utf-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
            this.defaultParseOptions.addParserProperties(uri, value);
            return;
        }
        this.setConfigurationProperty(feature, value);
    }

    public <T> void setConfigurationProperty(Feature<T> feature, T value) {
        String name = feature.name;
        if (booleanFeatures.contains(feature)) {
            if (feature == Feature.COMPILE_WITH_TRACING) {
                boolean b = Configuration.requireBoolean(name, value);
                this.setCompileWithTracing(b);
            } else if (feature == Feature.DTD_VALIDATION) {
                boolean b = Configuration.requireBoolean(name, value);
                this.setValidation(b);
            } else if (feature == Feature.EXPAND_ATTRIBUTE_DEFAULTS) {
                boolean b = Configuration.requireBoolean(name, value);
                this.setExpandAttributeDefaults(b);
            }
            this.internalSetBooleanProperty(feature, value);
        } else {
            block2 : switch (feature.code) {
                case 124: {
                    this.allowedUriTest = ProtocolRestricter.make((String)value);
                    break;
                }
                case 6: {
                    if (!(value instanceof CollationURIResolver)) {
                        throw new IllegalArgumentException("COLLATION_URI_RESOLVER value must be an instance of net.sf.saxon.lib.CollationURIResolver");
                    }
                    this.setCollationURIResolver((CollationURIResolver)value);
                    break;
                }
                case 7: {
                    this.setCollationURIResolver((CollationURIResolver)this.instantiateClassName(name, value, CollationURIResolver.class));
                    break;
                }
                case 8: {
                    if (!(value instanceof CollectionFinder)) {
                        throw new IllegalArgumentException("COLLECTION_FINDER value must be an instance of net.sf.saxon.lib.ICollectionFinder");
                    }
                    this.setCollectionFinder((CollectionFinder)value);
                    break;
                }
                case 9: {
                    this.setCollectionFinder((CollectionFinder)this.instantiateClassName(name, value, CollectionFinder.class));
                    break;
                }
                case 17: {
                    this.defaultCollationName = value.toString();
                    break;
                }
                case 18: {
                    this.setDefaultCollection(value.toString());
                    break;
                }
                case 19: {
                    this.setDefaultCountry(value.toString());
                    break;
                }
                case 20: {
                    this.setDefaultLanguage(value.toString());
                    break;
                }
                case 21: {
                    this.setDefaultRegexEngine(value.toString());
                    break;
                }
                case 25: {
                    boolean b = Configuration.requireBoolean(name, value);
                    if (b) {
                        this.defaultParseOptions.setDTDValidationMode(2);
                    } else {
                        this.defaultParseOptions.setDTDValidationMode(this.isValidation() ? 1 : 4);
                    }
                    this.internalSetBooleanProperty(Feature.DTD_VALIDATION_RECOVERABLE, b);
                    break;
                }
                case 27: {
                    if ("".equals(value)) {
                        this.defaultParseOptions.setEntityResolver(null);
                        break;
                    }
                    this.defaultParseOptions.setEntityResolver((EntityResolver)this.instantiateClassName(name, value, EntityResolver.class));
                    break;
                }
                case 28: {
                    if (!(value instanceof EnvironmentVariableResolver)) {
                        throw new IllegalArgumentException("ENVIRONMENT_VARIABLE_RESOLVER value must be an instance of net.sf.saxon.lib.EnvironmentVariableResolver");
                    }
                    this.environmentVariableResolver = (EnvironmentVariableResolver)value;
                    break;
                }
                case 29: {
                    this.environmentVariableResolver = (EnvironmentVariableResolver)this.instantiateClassName(name, value, EnvironmentVariableResolver.class);
                    break;
                }
                case 30: {
                    break;
                }
                case 38: {
                    boolean b = Configuration.requireBoolean(name, value);
                    this.setLineNumbering(b);
                    break;
                }
                case 41: {
                    if (!(value instanceof String)) {
                        throw new IllegalArgumentException("MESSAGE_EMITTER_CLASS class must be a String");
                    }
                    this.setMessageEmitterClass((String)value);
                    break;
                }
                case 42: {
                    if (!(value instanceof ModuleURIResolver)) {
                        throw new IllegalArgumentException("MODULE_URI_RESOLVER value must be an instance of net.sf.saxon.lib.ModuleURIResolver");
                    }
                    this.setModuleURIResolver((ModuleURIResolver)value);
                    break;
                }
                case 43: {
                    this.setModuleURIResolver((ModuleURIResolver)this.instantiateClassName(name, value, ModuleURIResolver.class));
                    break;
                }
                case 46: {
                    if (!(value instanceof NamePool)) {
                        throw new IllegalArgumentException("NAME_POOL value must be an instance of net.sf.saxon.om.NamePool");
                    }
                    this.setNamePool((NamePool)value);
                    break;
                }
                case 48: {
                    String s;
                    int v;
                    this.optimizerOptions = value instanceof Integer ? ((v = ((Integer)value).intValue()) == 0 ? new OptimizerOptions(0) : OptimizerOptions.FULL_EE_OPTIMIZATION) : ((s = this.requireString(name, value)).matches("[0-9]+") ? ("0".equals(s) ? new OptimizerOptions(0) : OptimizerOptions.FULL_EE_OPTIMIZATION) : new OptimizerOptions(s));
                    if (this.optimizer != null) {
                        this.optimizer.setOptimizerOptions(this.optimizerOptions);
                    }
                    this.internalSetBooleanProperty(Feature.GENERATE_BYTE_CODE, this.optimizerOptions.isSet(64));
                    this.defaultXsltCompilerInfo.setOptimizerOptions(this.optimizerOptions);
                    break;
                }
                case 49: {
                    if (!(value instanceof OutputURIResolver)) {
                        throw new IllegalArgumentException("OUTPUT_URI_RESOLVER value must be an instance of net.sf.saxon.lib.OutputURIResolver");
                    }
                    this.setOutputURIResolver((OutputURIResolver)value);
                    break;
                }
                case 50: {
                    this.setOutputURIResolver((OutputURIResolver)this.instantiateClassName(name, value, OutputURIResolver.class));
                    break;
                }
                case 53: {
                    boolean b = Configuration.requireBoolean(name, value);
                    this.getSystemURIResolver().setRecognizeQueryParameters(b);
                    break;
                }
                case 54: {
                    break;
                }
                case 55: {
                    break;
                }
                case 119: {
                    this.regexBacktrackingLimit = this.requireInteger(name, value);
                    break;
                }
                case 62: {
                    this.setSerializerFactory((SerializerFactory)this.instantiateClassName(name, value, SerializerFactory.class));
                    break;
                }
                case 60: {
                    this.setSchemaValidationMode(this.requireInteger(feature.name, value));
                    break;
                }
                case 61: {
                    String mode = this.requireString(feature.name, value);
                    this.setSchemaValidationMode(Validation.getCode(mode));
                    break;
                }
                case 63: {
                    this.setSourceParserClass(this.requireString(feature.name, value));
                    break;
                }
                case 64: {
                    this.setSourceResolver((SourceResolver)this.instantiateClassName(name, value, SourceResolver.class));
                    break;
                }
                case 67: {
                    try {
                        boolean append = true;
                        boolean autoFlush = true;
                        this.setStandardErrorOutput(new PrintStream(new FileOutputStream((String)value, append), autoFlush));
                        break;
                    } catch (FileNotFoundException fnf) {
                        throw new IllegalArgumentException(fnf);
                    }
                }
                case 71: {
                    String s;
                    switch (s = this.requireString(name, value)) {
                        case "all": {
                            this.defaultParseOptions.setSpaceStrippingRule(AllElementsSpaceStrippingRule.getInstance());
                            break block2;
                        }
                        case "none": {
                            this.defaultParseOptions.setSpaceStrippingRule(NoElementsSpaceStrippingRule.getInstance());
                            break block2;
                        }
                        case "ignorable": {
                            this.defaultParseOptions.setSpaceStrippingRule(IgnorableSpaceStrippingRule.getInstance());
                            break block2;
                        }
                    }
                    throw new IllegalArgumentException("Unrecognized value STRIP_WHITESPACE = '" + value + "': must be 'all', 'none', or 'ignorable'");
                }
                case 72: {
                    this.setStyleParserClass(this.requireString(name, value));
                    break;
                }
                case 77: {
                    this.setTiming(Configuration.requireBoolean(name, value));
                    break;
                }
                case 79: {
                    if (!(value instanceof TraceListener)) {
                        throw new IllegalArgumentException("TRACE_LISTENER is of wrong class");
                    }
                    this.setTraceListener((TraceListener)value);
                    break;
                }
                case 80: {
                    this.setTraceListenerClass(this.requireString(name, value));
                    break;
                }
                case 81: {
                    this.setTraceListenerOutputFile(this.requireString(name, value));
                    break;
                }
                case 83: {
                    this.setTreeModel(this.requireInteger(name, value));
                    break;
                }
                case 84: {
                    String s;
                    switch (s = this.requireString(name, value)) {
                        case "tinyTree": {
                            this.setTreeModel(1);
                            break block2;
                        }
                        case "tinyTreeCondensed": {
                            this.setTreeModel(2);
                            break block2;
                        }
                        case "linkedTree": {
                            this.setTreeModel(0);
                            break block2;
                        }
                        case "jdom": {
                            this.setTreeModel(3);
                            break block2;
                        }
                        case "jdom2": {
                            this.setTreeModel(4);
                            break block2;
                        }
                    }
                    throw new IllegalArgumentException("Unrecognized value TREE_MODEL_NAME = '" + value + "': must be linkedTree|tinyTree|tinyTreeCondensed");
                }
                case 85: {
                    this.setUnparsedTextURIResolver((UnparsedTextURIResolver)value);
                    break;
                }
                case 86: {
                    this.setUnparsedTextURIResolver((UnparsedTextURIResolver)this.instantiateClassName(name, value, UnparsedTextURIResolver.class));
                    break;
                }
                case 87: {
                    this.setURIResolver((URIResolver)this.instantiateClassName(name, value, URIResolver.class));
                    break;
                }
                case 90: {
                    this.defaultParseOptions.setUseXsiSchemaLocation(Configuration.requireBoolean(name, value));
                    break;
                }
                case 91: {
                    this.defaultParseOptions.setAddCommentsAfterValidationErrors(Configuration.requireBoolean(name, value));
                    break;
                }
                case 92: {
                    this.setValidationWarnings(Configuration.requireBoolean(name, value));
                    break;
                }
                case 93: {
                    break;
                }
                case 94: {
                    this.setXIncludeAware(Configuration.requireBoolean(name, value));
                    break;
                }
                case 120: {
                    int val = this.requireInteger(name, value);
                    if (val != 20 && val != 30 && val != 31) {
                        throw new IllegalArgumentException("XPath version for XSD must be 20 (XPath 2.0), 30 (XPath 3.0), or 31 (XPath 3.1)");
                    }
                    this.xpathVersionForXsd = val;
                    break;
                }
                case 121: {
                    int val = this.requireInteger(name, value);
                    if (val != 20 && val != 30 && val != 305 && val != 31) {
                        throw new IllegalArgumentException("XPath version for XSLT must be 20 (XPath 2.0), 30 (XPath 3.0), 31 (XPath 3.1), or 305 (XPath 3.0 with XSLT-defined extensions)");
                    }
                    this.xpathVersionForXslt = val;
                    break;
                }
                case 98: {
                    this.getDefaultStaticQueryContext().setUpdatingEnabled(Configuration.requireBoolean(name, value));
                    break;
                }
                case 99: {
                    this.getDefaultStaticQueryContext().setConstructionMode(Validation.getCode(value.toString()));
                    break;
                }
                case 100: {
                    this.getDefaultStaticQueryContext().setDefaultElementNamespace(value.toString());
                    break;
                }
                case 101: {
                    this.getDefaultStaticQueryContext().setDefaultFunctionNamespace(value.toString());
                    break;
                }
                case 102: {
                    this.getDefaultStaticQueryContext().setEmptyLeast(Configuration.requireBoolean(name, value));
                    break;
                }
                case 103: {
                    this.getDefaultStaticQueryContext().setInheritNamespaces(Configuration.requireBoolean(name, value));
                    break;
                }
                case 105: {
                    this.getDefaultStaticQueryContext().setPreserveBoundarySpace(Configuration.requireBoolean(name, value));
                    break;
                }
                case 106: {
                    this.getDefaultStaticQueryContext().setPreserveNamespaces(Configuration.requireBoolean(name, value));
                    break;
                }
                case 107: {
                    XPathParser parser = new XPathParser();
                    parser.setLanguage(XPathParser.ParsedLanguage.SEQUENCE_TYPE, 31);
                    try {
                        SequenceType type = parser.parseSequenceType(value.toString(), new IndependentContext(this));
                        if (type.getCardinality() != 16384) {
                            throw new IllegalArgumentException("Context item type must have no occurrence indicator");
                        }
                        this.getDefaultStaticQueryContext().setRequiredContextItemType(type.getPrimaryType());
                        break;
                    } catch (XPathException err) {
                        throw new IllegalArgumentException(err);
                    }
                }
                case 108: {
                    this.getDefaultStaticQueryContext().setSchemaAware(Configuration.requireBoolean(name, value));
                    break;
                }
                case 109: {
                    this.getDefaultStaticQueryContext().setErrorListener((ErrorListener)this.instantiateClassName(name, value, ErrorListener.class));
                    break;
                }
                case 110: {
                    if ("3.1".equals(value)) break;
                    this.makeErrorReporter().report(new XmlProcessingIncident("XQuery version ignored: only \"3.1\" is recognized").asWarning());
                    break;
                }
                case 95: {
                    String xv = this.requireString(name, value);
                    if (!xv.equals("1.0") && !xv.equals("1.1")) {
                        throw new IllegalArgumentException("XML_VERSION value must be \"1.0\" or \"1.1\" as a String");
                    }
                    this.setXMLVersion(xv.equals("1.0") ? 10 : 11);
                    break;
                }
                case 111: {
                    String vn = this.requireString(name, value);
                    if (!vn.equals("1.0") && !vn.equals("1.1")) {
                        throw new IllegalArgumentException("XSD_VERSION value must be \"1.0\" or \"1.1\" as a String");
                    }
                    this.xsdVersion = value.equals("1.0") ? 10 : 11;
                    this.theConversionRules = null;
                    break;
                }
                case 112: {
                    this.getDefaultXsltCompilerInfo().setAssertionsEnabled(Configuration.requireBoolean(name, value));
                    break;
                }
                case 113: {
                    String s = this.requireString(name, value);
                    this.getDefaultXsltCompilerInfo().setDefaultInitialMode(StructuredQName.fromClarkName(s));
                    break;
                }
                case 114: {
                    String s = this.requireString(name, value);
                    this.getDefaultXsltCompilerInfo().setDefaultInitialTemplate(StructuredQName.fromClarkName(s));
                    break;
                }
                case 115: {
                    this.getDefaultXsltCompilerInfo().setSchemaAware(Configuration.requireBoolean(name, value));
                    break;
                }
                case 116: {
                    this.getDefaultXsltCompilerInfo().setErrorListener((ErrorListener)this.instantiateClassName(name, value, ErrorListener.class));
                    break;
                }
                case 117: {
                    this.getDefaultXsltCompilerInfo().setURIResolver((URIResolver)this.instantiateClassName(name, value, URIResolver.class));
                    break;
                }
                case 118: {
                    if ("3.0".equals(value)) break;
                    this.makeErrorReporter().report(new XmlProcessingIncident("XSLT version ignored: only \"3.0\" is recognized").asWarning());
                    break;
                }
                case 127: {
                    this.zipUriPattern = (String)value;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unknown configuration property " + name);
                }
            }
        }
    }

    public static boolean requireBoolean(String propertyName, Object value) {
        if (value instanceof Boolean) {
            return (Boolean)value;
        }
        if (value instanceof String) {
            if ("true".equals(value = ((String)value).trim()) || "on".equals(value) || "yes".equals(value) || "1".equals(value)) {
                return true;
            }
            if ("false".equals(value) || "off".equals(value) || "no".equals(value) || "0".equals(value)) {
                return false;
            }
            throw new IllegalArgumentException(propertyName + " must be 'true' or 'false' (or on|off, yes|no, 1|0)");
        }
        throw new IllegalArgumentException(propertyName + " must be a boolean (or a string representing a boolean)");
    }

    protected int requireInteger(String propertyName, Object value) {
        if (value instanceof Integer) {
            return (Integer)value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String)value);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(propertyName + " must be an integer");
            }
        }
        throw new IllegalArgumentException(propertyName + " must be an integer (or a string representing an integer)");
    }

    protected void internalSetBooleanProperty(Feature property, Object value) {
        boolean b = Configuration.requireBoolean(property.name, value);
        if (b) {
            this.enabledProperties.add(property.code);
        } else {
            this.enabledProperties.remove(property.code);
        }
    }

    public boolean getBooleanProperty(Feature<?> feature) {
        return this.enabledProperties.contains(feature.code);
    }

    public void setBooleanProperty(String propertyName, boolean value) {
        this.setConfigurationProperty(propertyName, (Object)value);
    }

    public void setBooleanProperty(Feature<Boolean> feature, boolean value) {
        this.setConfigurationProperty(feature, value);
    }

    protected String requireString(String propertyName, Object value) {
        if (value instanceof String) {
            return (String)value;
        }
        throw new IllegalArgumentException("The value of " + propertyName + " must be a string");
    }

    protected Object instantiateClassName(String propertyName, Object value, Class<?> requiredClass) {
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(propertyName + " must be a String");
        }
        try {
            Object obj = this.getInstance((String)value, null);
            if (!requiredClass.isAssignableFrom(obj.getClass())) {
                throw new IllegalArgumentException("Error in " + propertyName + ": Class " + value + " does not implement " + requiredClass.getName());
            }
            return obj;
        } catch (XPathException err) {
            throw new IllegalArgumentException("Cannot use " + value + " as the value of " + propertyName + ". " + err.getMessage());
        }
    }

    public Object getConfigurationProperty(String name) {
        Feature<?> feature = Feature.byName(name);
        if (feature == null) {
            throw new IllegalArgumentException("Unknown configuration property " + name);
        }
        return this.getConfigurationProperty(feature);
    }

    public <T> T getConfigurationProperty(Feature<T> feature) {
        if (booleanFeatures.contains(feature)) {
            return (T)Boolean.valueOf(this.getBooleanProperty(feature));
        }
        switch (feature.code) {
            case 124: {
                if (this.allowedUriTest instanceof ProtocolRestricter) {
                    return (T)this.allowedUriTest.toString();
                }
                return (T)"all";
            }
            case 6: {
                return (T)this.getCollationURIResolver();
            }
            case 7: {
                return (T)this.getCollationURIResolver().getClass().getName();
            }
            case 13: {
                return (T)this;
            }
            case 17: {
                return (T)this.defaultCollationName;
            }
            case 18: {
                return (T)this.getDefaultCollection();
            }
            case 19: {
                return (T)this.getDefaultCountry();
            }
            case 20: {
                return (T)this.getDefaultLanguage();
            }
            case 24: {
                return (T)Boolean.valueOf(this.isValidation());
            }
            case 25: {
                return (T)Boolean.valueOf(this.defaultParseOptions.getDTDValidationMode() == 2);
            }
            case 30: {
                return null;
            }
            case 27: {
                EntityResolver er = this.defaultParseOptions.getEntityResolver();
                if (er == null) {
                    return (T)"";
                }
                return (T)er.getClass().getName();
            }
            case 28: {
                return (T)this.environmentVariableResolver;
            }
            case 29: {
                return (T)this.environmentVariableResolver.getClass().getName();
            }
            case 31: {
                return (T)Boolean.valueOf(this.isExpandAttributeDefaults());
            }
            case 38: {
                return (T)Boolean.valueOf(this.isLineNumbering());
            }
            case 41: {
                return (T)this.getMessageEmitterClass();
            }
            case 42: {
                return (T)this.getModuleURIResolver();
            }
            case 43: {
                return (T)this.getModuleURIResolver().getClass().getName();
            }
            case 46: {
                return (T)this.getNamePool();
            }
            case 48: {
                return (T)this.optimizerOptions.toString();
            }
            case 49: {
                return (T)this.getOutputURIResolver();
            }
            case 50: {
                return (T)this.getOutputURIResolver().getClass().getName();
            }
            case 53: {
                return (T)Boolean.valueOf(this.getSystemURIResolver().queryParametersAreRecognized());
            }
            case 54: {
                return (T)Integer.valueOf(0);
            }
            case 55: {
                return (T)"recoverWithWarnings";
            }
            case 119: {
                return (T)Integer.valueOf(this.regexBacktrackingLimit);
            }
            case 60: {
                return (T)Integer.valueOf(this.getSchemaValidationMode());
            }
            case 61: {
                return (T)Validation.toString(this.getSchemaValidationMode());
            }
            case 62: {
                return (T)this.getSerializerFactory().getClass().getName();
            }
            case 63: {
                return (T)this.getSourceParserClass();
            }
            case 64: {
                return (T)this.getSourceResolver().getClass().getName();
            }
            case 71: {
                SpaceStrippingRule rule = this.getParseOptions().getSpaceStrippingRule();
                if (rule == AllElementsSpaceStrippingRule.getInstance()) {
                    return (T)"all";
                }
                if (rule == null || rule == IgnorableSpaceStrippingRule.getInstance()) {
                    return (T)"ignorable";
                }
                return (T)"none";
            }
            case 72: {
                return (T)this.getStyleParserClass();
            }
            case 77: {
                return (T)Boolean.valueOf(this.isTiming());
            }
            case 79: {
                return (T)this.traceListener;
            }
            case 80: {
                return (T)this.traceListenerClass;
            }
            case 81: {
                return (T)this.traceListenerOutput;
            }
            case 83: {
                return (T)Integer.valueOf(this.getTreeModel());
            }
            case 84: {
                switch (this.getTreeModel()) {
                    default: {
                        return (T)"tinyTree";
                    }
                    case 2: {
                        return (T)"tinyTreeCondensed";
                    }
                    case 0: 
                }
                return (T)"linkedTree";
            }
            case 85: {
                return (T)this.getUnparsedTextURIResolver();
            }
            case 86: {
                return (T)this.getUnparsedTextURIResolver().getClass().getName();
            }
            case 87: {
                return (T)this.getURIResolver().getClass().getName();
            }
            case 90: {
                return (T)Boolean.valueOf(this.defaultParseOptions.isUseXsiSchemaLocation());
            }
            case 91: {
                return (T)Boolean.valueOf(this.defaultParseOptions.isAddCommentsAfterValidationErrors());
            }
            case 92: {
                return (T)Boolean.valueOf(this.isValidationWarnings());
            }
            case 93: {
                return (T)Boolean.valueOf(false);
            }
            case 94: {
                return (T)Boolean.valueOf(this.isXIncludeAware());
            }
            case 95: {
                return (T)(this.getXMLVersion() == 10 ? "1.0" : "1.1");
            }
            case 98: {
                return (T)Boolean.valueOf(this.getDefaultStaticQueryContext().isUpdatingEnabled());
            }
            case 99: {
                return (T)Integer.valueOf(this.getDefaultStaticQueryContext().getConstructionMode());
            }
            case 100: {
                return (T)this.getDefaultStaticQueryContext().getDefaultElementNamespace();
            }
            case 101: {
                return (T)this.getDefaultStaticQueryContext().getDefaultFunctionNamespace();
            }
            case 102: {
                return (T)Boolean.valueOf(this.getDefaultStaticQueryContext().isEmptyLeast());
            }
            case 103: {
                return (T)Boolean.valueOf(this.getDefaultStaticQueryContext().isInheritNamespaces());
            }
            case 105: {
                return (T)Boolean.valueOf(this.getDefaultStaticQueryContext().isPreserveBoundarySpace());
            }
            case 106: {
                return (T)Boolean.valueOf(this.getDefaultStaticQueryContext().isPreserveNamespaces());
            }
            case 107: {
                return (T)this.getDefaultStaticQueryContext().getRequiredContextItemType();
            }
            case 108: {
                return (T)Boolean.valueOf(this.getDefaultStaticQueryContext().isSchemaAware());
            }
            case 109: {
                return (T)this.getDefaultStaticQueryContext().getErrorListener().getClass().getName();
            }
            case 110: {
                return (T)"3.1";
            }
            case 120: {
                return (T)Integer.valueOf(this.xpathVersionForXsd);
            }
            case 121: {
                return (T)Integer.valueOf(this.xpathVersionForXslt);
            }
            case 111: {
                return (T)(this.xsdVersion == 10 ? "1.0" : "1.1");
            }
            case 112: {
                return (T)Boolean.valueOf(this.getDefaultXsltCompilerInfo().isAssertionsEnabled());
            }
            case 113: {
                return (T)this.getDefaultXsltCompilerInfo().getDefaultInitialMode().getClarkName();
            }
            case 114: {
                return (T)this.getDefaultXsltCompilerInfo().getDefaultInitialTemplate().getClarkName();
            }
            case 115: {
                return (T)Boolean.valueOf(this.getDefaultXsltCompilerInfo().isSchemaAware());
            }
            case 116: {
                return (T)this.getDefaultXsltCompilerInfo().getErrorListener().getClass().getName();
            }
            case 117: {
                return (T)this.getDefaultXsltCompilerInfo().getURIResolver().getClass().getName();
            }
            case 118: {
                return (T)Integer.valueOf(30);
            }
            case 127: {
                return (T)(this.zipUriPattern == null ? Feature.ZIP_URI_PATTERN.defaultValue : this.zipUriPattern);
            }
        }
        throw new IllegalArgumentException("Unknown configuration property " + feature.name);
    }

    public boolean isGenerateByteCode(HostLanguage hostLanguage) {
        return false;
    }

    public boolean isDeferredByteCode(HostLanguage hostLanguage) {
        return false;
    }

    public boolean isJITEnabled() {
        return false;
    }

    public void close() {
        if (this.traceOutput != null) {
            this.traceOutput.close();
        }
    }

    public IPackageLoader makePackageLoader() {
        return new PackageLoaderHE(this);
    }

    public InvalidityReportGenerator createValidityReporter() {
        throw new UnsupportedOperationException("Schema validation requires Saxon-EE");
    }

    public int getCountDown() {
        return this.byteCodeThreshold;
    }

    public SimpleMode makeMode(StructuredQName modeName, CompilerInfo compilerInfo) {
        return new SimpleMode(modeName);
    }

    public TemplateRule makeTemplateRule() {
        return new TemplateRule();
    }

    public XPathContextMajor.ThreadManager makeThreadManager() {
        return null;
    }

    public CompilerInfo makeCompilerInfo() {
        return new CompilerInfo(this);
    }

    public ICompilerService makeCompilerService(HostLanguage hostLanguage) {
        return null;
    }

    public void createByteCodeReport(String fileName) {
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    static {
        booleanFeatures.add(Feature.ALLOW_EXTERNAL_FUNCTIONS);
        booleanFeatures.add(Feature.ALLOW_MULTITHREADING);
        booleanFeatures.add(Feature.ALLOW_SYNTAX_EXTENSIONS);
        booleanFeatures.add(Feature.ASSERTIONS_CAN_SEE_COMMENTS);
        booleanFeatures.add(Feature.COMPILE_WITH_TRACING);
        booleanFeatures.add(Feature.DEBUG_BYTE_CODE);
        booleanFeatures.add(Feature.DISABLE_XSL_EVALUATE);
        booleanFeatures.add(Feature.DISPLAY_BYTE_CODE);
        booleanFeatures.add(Feature.DTD_VALIDATION);
        booleanFeatures.add(Feature.EAGER_EVALUATION);
        booleanFeatures.add(Feature.EXPAND_ATTRIBUTE_DEFAULTS);
        booleanFeatures.add(Feature.EXPATH_FILE_DELETE_TEMPORARY_FILES);
        booleanFeatures.add(Feature.GENERATE_BYTE_CODE);
        booleanFeatures.add(Feature.IGNORE_SAX_SOURCE_PARSER);
        booleanFeatures.add(Feature.IMPLICIT_SCHEMA_IMPORTS);
        booleanFeatures.add(Feature.MARK_DEFAULTED_ATTRIBUTES);
        booleanFeatures.add(Feature.MONITOR_HOT_SPOT_BYTE_CODE);
        booleanFeatures.add(Feature.MULTIPLE_SCHEMA_IMPORTS);
        booleanFeatures.add(Feature.PRE_EVALUATE_DOC_FUNCTION);
        booleanFeatures.add(Feature.PREFER_JAXP_PARSER);
        booleanFeatures.add(Feature.RETAIN_DTD_ATTRIBUTE_TYPES);
        booleanFeatures.add(Feature.STABLE_COLLECTION_URI);
        booleanFeatures.add(Feature.STABLE_UNPARSED_TEXT);
        booleanFeatures.add(Feature.STREAMING_FALLBACK);
        booleanFeatures.add(Feature.STRICT_STREAMABILITY);
        booleanFeatures.add(Feature.SUPPRESS_EVALUATION_EXPIRY_WARNING);
        booleanFeatures.add(Feature.SUPPRESS_XPATH_WARNINGS);
        booleanFeatures.add(Feature.SUPPRESS_XSLT_NAMESPACE_CHECK);
        booleanFeatures.add(Feature.TRACE_EXTERNAL_FUNCTIONS);
        booleanFeatures.add(Feature.TRACE_OPTIMIZER_DECISIONS);
        booleanFeatures.add(Feature.USE_PI_DISABLE_OUTPUT_ESCAPING);
        booleanFeatures.add(Feature.USE_TYPED_VALUE_CACHE);
        booleanFeatures.add(Feature.XQUERY_MULTIPLE_MODULE_IMPORTS);
        booleanFeatures.add(Feature.RETAIN_NODE_FOR_DIAGNOSTICS);
        booleanFeatures.add(Feature.ALLOW_UNRESOLVED_SCHEMA_COMPONENTS);
    }

    public static class LicenseFeature {
        public static final int SCHEMA_VALIDATION = 1;
        public static final int ENTERPRISE_XSLT = 2;
        public static final int ENTERPRISE_XQUERY = 4;
        public static final int PROFESSIONAL_EDITION = 8;
    }

    public static interface ApiProvider {
    }
}

