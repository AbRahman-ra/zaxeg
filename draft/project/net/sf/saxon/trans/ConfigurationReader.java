/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.Version;
import net.sf.saxon.event.ContentHandlerProxy;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.ResultDocument;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.ExternalObjectModel;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.LocalizerFactory;
import net.sf.saxon.lib.StandardErrorReporter;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.QNameParser;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.XmlProcessingError;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.trans.XmlProcessingIncident;
import net.sf.saxon.trans.packages.PackageDetails;
import net.sf.saxon.trans.packages.PackageLibrary;
import net.sf.saxon.trans.packages.VersionedPackageName;
import net.sf.saxon.tree.util.FastStringBuffer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

public class ConfigurationReader
implements ContentHandler,
NamespaceResolver {
    private int level = 0;
    private String section = null;
    private String subsection = null;
    private FastStringBuffer buffer = new FastStringBuffer(100);
    protected Configuration config;
    private ClassLoader classLoader = null;
    private List<XmlProcessingError> errors = new ArrayList<XmlProcessingError>();
    private Locator locator;
    private Stack<List<String[]>> namespaceStack = new Stack();
    private PackageLibrary packageLibrary;
    private PackageDetails currentPackage;
    private Configuration baseConfiguration;

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setBaseConfiguration(Configuration base) {
        this.baseConfiguration = base;
    }

    public Configuration makeConfiguration(Source source) throws XPathException {
        if (source instanceof NodeInfo) {
            ContentHandlerProxy proxy = new ContentHandlerProxy(){

                @Override
                public void startDocument(int properties) throws XPathException {
                    try {
                        this.getUnderlyingContentHandler().startDocument();
                    } catch (SAXException e) {
                        throw XPathException.makeXPathException(e);
                    }
                }

                @Override
                public void endDocument() throws XPathException {
                    try {
                        this.getUnderlyingContentHandler().endDocument();
                    } catch (SAXException e) {
                        throw XPathException.makeXPathException(e);
                    }
                }
            };
            proxy.setUnderlyingContentHandler(this);
            proxy.setPipelineConfiguration(((NodeInfo)source).getConfiguration().makePipelineConfiguration());
            proxy.open();
            this.setDocumentLocator(new Loc(source.getSystemId(), -1, -1));
            proxy.startDocument(0);
            ((NodeInfo)source).copy(proxy, 2, Loc.NONE);
            proxy.endDocument();
            proxy.close();
        } else {
            InputSource is;
            XMLReader parser = null;
            if (source instanceof SAXSource) {
                parser = ((SAXSource)source).getXMLReader();
                is = ((SAXSource)source).getInputSource();
            } else if (source instanceof StreamSource) {
                is = new InputSource(source.getSystemId());
                is.setCharacterStream(((StreamSource)source).getReader());
                is.setByteStream(((StreamSource)source).getInputStream());
            } else {
                throw new XPathException("Source for configuration file must be a StreamSource or SAXSource or NodeInfo");
            }
            if (parser == null) {
                parser = Version.platform.loadParser();
                try {
                    parser.setFeature("http://xml.org/sax/features/namespaces", true);
                    parser.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
                } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
                    throw new TransformerFactoryConfigurationError(e);
                }
            }
            try {
                parser.setContentHandler(this);
                parser.parse(is);
            } catch (IOException e) {
                throw new XPathException("Failed to read config file", e);
            } catch (SAXException e) {
                throw new XPathException("Failed to parse config file", e);
            }
        }
        if (!this.errors.isEmpty()) {
            ErrorReporter reporter = this.config == null ? new StandardErrorReporter() : this.config.makeErrorReporter();
            for (XmlProcessingError err : this.errors) {
                reporter.report(err.asWarning());
            }
            throw XPathException.fromXmlProcessingError(this.errors.get(0));
        }
        if (this.baseConfiguration != null) {
            this.config.importLicenseDetails(this.baseConfiguration);
        }
        return this.config;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    public void startDocument() {
        this.namespaceStack.push(new ArrayList());
    }

    @Override
    public void endDocument() {
        this.namespaceStack.pop();
        if (this.config != null) {
            this.config.getDefaultXsltCompilerInfo().setPackageLibrary(this.packageLibrary);
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) {
        this.namespaceStack.peek().add(new String[]{prefix, uri});
    }

    @Override
    public void endPrefixMapping(String prefix) {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) {
        this.buffer.setLength(0);
        if ("http://saxon.sf.net/ns/configuration".equals(uri)) {
            if (this.level == 0) {
                String label;
                String targetEdition;
                String edition;
                if (!"configuration".equals(localName)) {
                    this.error(localName, null, null, "configuration");
                }
                if ((edition = atts.getValue("edition")) == null) {
                    edition = "HE";
                }
                switch (edition) {
                    case "HE": {
                        this.config = new Configuration();
                        break;
                    }
                    case "PE": {
                        this.config = Configuration.makeLicensedConfiguration(this.classLoader, "com.saxonica.config.ProfessionalConfiguration");
                        break;
                    }
                    case "EE": {
                        this.config = Configuration.makeLicensedConfiguration(this.classLoader, "com.saxonica.config.EnterpriseConfiguration");
                        break;
                    }
                    default: {
                        this.error("configuration", "edition", edition, "HE|PE|EE");
                        this.config = new Configuration();
                    }
                }
                if (this.baseConfiguration != null) {
                    this.config.setNamePool(this.baseConfiguration.getNamePool());
                    this.config.setDocumentNumberAllocator(this.baseConfiguration.getDocumentNumberAllocator());
                }
                this.packageLibrary = new PackageLibrary(this.config.getDefaultXsltCompilerInfo());
                String licenseLoc = atts.getValue("licenseFileLocation");
                if (licenseLoc != null && !edition.equals("HE") && !this.config.isLicensedFeature(8)) {
                    String base = this.locator.getSystemId();
                    try {
                        URI absoluteLoc = ResolveURI.makeAbsolute(licenseLoc, base);
                        this.config.setConfigurationProperty("http://saxon.sf.net/feature/licenseFileLocation", absoluteLoc.toString());
                    } catch (Exception err) {
                        XmlProcessingIncident incident = new XmlProcessingIncident("Failed to process license at " + licenseLoc);
                        incident.setCause(err);
                        this.errors.add(incident);
                    }
                }
                if ((targetEdition = atts.getValue("targetEdition")) != null) {
                    this.packageLibrary.getCompilerInfo().setTargetEdition(targetEdition);
                }
                if ((label = atts.getValue("label")) != null) {
                    this.config.setLabel(label);
                }
                this.config.getDynamicLoader().setClassLoader(this.classLoader);
            }
            if (this.level == 1) {
                this.section = localName;
                if ("global".equals(localName)) {
                    this.readGlobalElement(atts);
                } else if ("serialization".equals(localName)) {
                    this.readSerializationElement(atts);
                } else if ("xquery".equals(localName)) {
                    this.readXQueryElement(atts);
                } else if ("xslt".equals(localName)) {
                    this.readXsltElement(atts);
                } else if (!"xsltPackages".equals(localName)) {
                    if ("xsd".equals(localName)) {
                        this.readXsdElement(atts);
                    } else if (!"resources".equals(localName) && !"collations".equals(localName)) {
                        if ("localizations".equals(localName)) {
                            this.readLocalizationsElement(atts);
                        } else {
                            this.error(localName, null, null, null);
                        }
                    }
                }
            } else if (this.level == 2) {
                this.subsection = localName;
                switch (this.section) {
                    case "resources": {
                        if (!"fileExtension".equals(localName)) break;
                        this.readFileExtension(atts);
                        break;
                    }
                    case "collations": {
                        if (!"collation".equals(localName)) {
                            this.error(localName, null, null, "collation");
                            break;
                        }
                        this.readCollation(atts);
                        break;
                    }
                    case "localizations": {
                        if (!"localization".equals(localName)) {
                            this.error(localName, null, null, "localization");
                            break;
                        }
                        this.readLocalization(atts);
                        break;
                    }
                    case "xslt": {
                        if ("extensionElement".equals(localName)) {
                            this.readExtensionElement(atts);
                            break;
                        }
                        this.error(localName, null, null, null);
                        break;
                    }
                    case "xsltPackages": {
                        if (!"package".equals(localName)) break;
                        this.readXsltPackage(atts);
                    }
                }
            } else if (this.level == 3 && "package".equals(this.subsection)) {
                if ("withParam".equals(localName)) {
                    this.readWithParam(atts);
                } else {
                    this.error(localName, null, null, null);
                }
            }
        } else {
            XmlProcessingIncident incident = new XmlProcessingIncident("Configuration elements must be in namespace http://saxon.sf.net/ns/configuration");
            this.errors.add(incident);
        }
        ++this.level;
        this.namespaceStack.push(new ArrayList());
    }

    private void readGlobalElement(Attributes atts) {
        Properties props = new Properties();
        for (int i = 0; i < atts.getLength(); ++i) {
            String name = atts.getLocalName(i);
            String value = atts.getValue(i);
            if (value.isEmpty() || !atts.getURI(i).isEmpty()) continue;
            props.put(name, value);
        }
        props.put("#element", "global");
        this.applyProperty(props, "allowedProtocols", Feature.ALLOWED_PROTOCOLS);
        this.applyProperty(props, "allowExternalFunctions", Feature.ALLOW_EXTERNAL_FUNCTIONS);
        this.applyProperty(props, "allowMultiThreading", Feature.ALLOW_MULTITHREADING);
        this.applyProperty(props, "allowOldJavaUriFormat", Feature.ALLOW_OLD_JAVA_URI_FORMAT);
        this.applyProperty(props, "allowSyntaxExtensions", Feature.ALLOW_SYNTAX_EXTENSIONS);
        this.applyProperty(props, "collationUriResolver", Feature.COLLATION_URI_RESOLVER_CLASS);
        this.applyProperty(props, "collectionFinder", Feature.COLLECTION_FINDER_CLASS);
        this.applyProperty(props, "compileWithTracing", Feature.COMPILE_WITH_TRACING);
        this.applyProperty(props, "debugByteCode", Feature.DEBUG_BYTE_CODE);
        this.applyProperty(props, "debugByteCodeDirectory", Feature.DEBUG_BYTE_CODE_DIR);
        this.applyProperty(props, "defaultCollation", Feature.DEFAULT_COLLATION);
        this.applyProperty(props, "defaultCollection", Feature.DEFAULT_COLLECTION);
        this.applyProperty(props, "defaultRegexEngine", Feature.DEFAULT_REGEX_ENGINE);
        this.applyProperty(props, "displayByteCode", Feature.DISPLAY_BYTE_CODE);
        this.applyProperty(props, "dtdValidation", Feature.DTD_VALIDATION);
        this.applyProperty(props, "dtdValidationRecoverable", Feature.DTD_VALIDATION_RECOVERABLE);
        this.applyProperty(props, "eagerEvaluation", Feature.EAGER_EVALUATION);
        this.applyProperty(props, "entityResolver", Feature.ENTITY_RESOLVER_CLASS);
        this.applyProperty(props, "errorListener", Feature.ERROR_LISTENER_CLASS);
        this.applyProperty(props, "environmentVariableResolver", Feature.ENVIRONMENT_VARIABLE_RESOLVER_CLASS);
        this.applyProperty(props, "expandAttributeDefaults", Feature.EXPAND_ATTRIBUTE_DEFAULTS);
        this.applyProperty(props, "generateByteCode", Feature.GENERATE_BYTE_CODE);
        this.applyProperty(props, "ignoreSAXSourceParser", Feature.IGNORE_SAX_SOURCE_PARSER);
        this.applyProperty(props, "lineNumbering", Feature.LINE_NUMBERING);
        this.applyProperty(props, "markDefaultedAttributes", Feature.MARK_DEFAULTED_ATTRIBUTES);
        this.applyProperty(props, "maxCompiledClasses", Feature.MAX_COMPILED_CLASSES);
        this.applyProperty(props, "monitorHotSpotByteCode", Feature.MONITOR_HOT_SPOT_BYTE_CODE);
        this.applyProperty(props, "optimizationLevel", Feature.OPTIMIZATION_LEVEL);
        this.applyProperty(props, "parser", Feature.SOURCE_PARSER_CLASS);
        this.applyProperty(props, "preEvaluateDoc", Feature.PRE_EVALUATE_DOC_FUNCTION);
        this.applyProperty(props, "preferJaxpParser", Feature.PREFER_JAXP_PARSER);
        this.applyProperty(props, "recognizeUriQueryParameters", Feature.RECOGNIZE_URI_QUERY_PARAMETERS);
        this.applyProperty(props, "retainNodeForDiagnostics", Feature.RETAIN_NODE_FOR_DIAGNOSTICS);
        this.applyProperty(props, "schemaValidation", Feature.SCHEMA_VALIDATION_MODE);
        this.applyProperty(props, "serializerFactory", Feature.SERIALIZER_FACTORY_CLASS);
        this.applyProperty(props, "sourceResolver", Feature.SOURCE_RESOLVER_CLASS);
        this.applyProperty(props, "stableCollectionUri", Feature.STABLE_COLLECTION_URI);
        this.applyProperty(props, "stableUnparsedText", Feature.STABLE_UNPARSED_TEXT);
        this.applyProperty(props, "standardErrorOutputFile", Feature.STANDARD_ERROR_OUTPUT_FILE);
        this.applyProperty(props, "streamability", Feature.STREAMABILITY);
        this.applyProperty(props, "streamingFallback", Feature.STREAMING_FALLBACK);
        this.applyProperty(props, "stripSpace", Feature.STRIP_WHITESPACE);
        this.applyProperty(props, "styleParser", Feature.STYLE_PARSER_CLASS);
        this.applyProperty(props, "suppressEvaluationExpiryWarning", Feature.SUPPRESS_EVALUATION_EXPIRY_WARNING);
        this.applyProperty(props, "suppressXPathWarnings", Feature.SUPPRESS_XPATH_WARNINGS);
        this.applyProperty(props, "suppressXsltNamespaceCheck", Feature.SUPPRESS_XSLT_NAMESPACE_CHECK);
        this.applyProperty(props, "thresholdForHotspotByteCode", Feature.THRESHOLD_FOR_HOTSPOT_BYTE_CODE);
        this.applyProperty(props, "timing", Feature.TIMING);
        this.applyProperty(props, "traceExternalFunctions", Feature.TRACE_EXTERNAL_FUNCTIONS);
        this.applyProperty(props, "traceListener", Feature.TRACE_LISTENER_CLASS);
        this.applyProperty(props, "traceListenerOutputFile", Feature.TRACE_LISTENER_OUTPUT_FILE);
        this.applyProperty(props, "traceOptimizerDecisions", Feature.TRACE_OPTIMIZER_DECISIONS);
        this.applyProperty(props, "treeModel", Feature.TREE_MODEL_NAME);
        this.applyProperty(props, "unparsedTextUriResolver", Feature.UNPARSED_TEXT_URI_RESOLVER_CLASS);
        this.applyProperty(props, "uriResolver", Feature.URI_RESOLVER_CLASS);
        this.applyProperty(props, "usePiDisableOutputEscaping", Feature.USE_PI_DISABLE_OUTPUT_ESCAPING);
        this.applyProperty(props, "useTypedValueCache", Feature.USE_TYPED_VALUE_CACHE);
        this.applyProperty(props, "validationComments", Feature.VALIDATION_COMMENTS);
        this.applyProperty(props, "validationWarnings", Feature.VALIDATION_WARNINGS);
        this.applyProperty(props, "versionOfXml", Feature.XML_VERSION);
        this.applyProperty(props, "xInclude", Feature.XINCLUDE);
        this.applyProperty(props, "zipUriPattern", Feature.ZIP_URI_PATTERN);
    }

    private void applyProperty(Properties props, String attributeName, Feature feature) {
        String value = props.getProperty(attributeName);
        if (value != null) {
            try {
                this.config.setConfigurationProperty(feature.name, value);
            } catch (IllegalArgumentException e) {
                String message = e.getMessage();
                if (message.startsWith(attributeName)) {
                    message = message.replace(attributeName, "Value");
                }
                if (message.startsWith("Unknown configuration property")) {
                    message = "Property not available in Saxon-" + this.config.getEditionCode();
                }
                this.error(props.getProperty("#element"), attributeName, value, message);
            }
        }
    }

    private void readSerializationElement(Attributes atts) {
        Properties props = new Properties();
        for (int i = 0; i < atts.getLength(); ++i) {
            String uri = atts.getURI(i);
            String name = atts.getLocalName(i);
            String value = atts.getValue(i);
            if (value.isEmpty()) continue;
            try {
                ResultDocument.setSerializationProperty(props, uri, name, value, this, false, this.config);
                continue;
            } catch (XPathException e) {
                this.errors.add(new XmlProcessingException(e));
            }
        }
        this.config.setDefaultSerializationProperties(props);
    }

    private void readCollation(Attributes atts) {
        Properties props = new Properties();
        String collationUri = null;
        for (int i = 0; i < atts.getLength(); ++i) {
            if (!atts.getURI(i).isEmpty()) continue;
            String name = atts.getLocalName(i);
            String value = atts.getValue(i);
            if (value.isEmpty()) continue;
            if ("uri".equals(name)) {
                collationUri = value;
                continue;
            }
            props.put(name, value);
        }
        if (collationUri == null) {
            this.errors.add(new XmlProcessingIncident("collation specified with no uri"));
        }
        StringCollator collator = null;
        try {
            collator = Version.platform.makeCollation(this.config, props, collationUri);
        } catch (XPathException e) {
            this.errors.add(new XmlProcessingIncident(e.getMessage()));
        }
        this.config.registerCollation(collationUri, collator);
    }

    private void readLocalizationsElement(Attributes atts) {
        for (int i = 0; i < atts.getLength(); ++i) {
            if (!atts.getURI(i).isEmpty()) continue;
            String name = atts.getLocalName(i);
            String value = atts.getValue(i);
            if ("defaultLanguage".equals(name) && !value.isEmpty()) {
                this.config.setConfigurationProperty("http://saxon.sf.net/feature/defaultLanguage", value);
            }
            if (!"defaultCountry".equals(name) || value.isEmpty()) continue;
            this.config.setConfigurationProperty("http://saxon.sf.net/feature/defaultCountry", value);
        }
    }

    private void readLocalization(Attributes atts) {
        LocalizerFactory factory;
        String lang = null;
        Properties properties = new Properties();
        for (int i = 0; i < atts.getLength(); ++i) {
            if (!atts.getURI(i).isEmpty()) continue;
            String name = atts.getLocalName(i);
            String value = atts.getValue(i);
            if ("lang".equals(name) && !value.isEmpty()) {
                lang = value;
                continue;
            }
            if (value.isEmpty()) continue;
            properties.setProperty(name, value);
        }
        if (lang != null && (factory = this.config.getLocalizerFactory()) != null) {
            factory.setLanguageProperties(lang, properties);
        }
    }

    private void readFileExtension(Attributes atts) {
        String extension = atts.getValue("", "extension");
        String mediaType = atts.getValue("", "mediaType");
        if (extension == null) {
            this.error("fileExtension", "extension", null, null);
        }
        if (mediaType == null) {
            this.error("fileExtension", "mediaType", null, null);
        }
        this.config.registerFileExtension(extension, mediaType);
    }

    protected void readExtensionElement(Attributes atts) {
        XmlProcessingIncident err = new XmlProcessingIncident("Extension elements are not available in Saxon-HE");
        err.setLocation(Loc.makeFromSax(this.locator));
        this.errors.add(err);
    }

    protected void readXsltPackage(Attributes atts) {
        String name = atts.getValue("name");
        if (name == null) {
            String attName = "exportLocation";
            String location = atts.getValue("exportLocation");
            URI uri = null;
            if (location == null) {
                attName = "sourceLocation";
                location = atts.getValue("sourceLocation");
            }
            if (location == null) {
                this.error("package", attName, null, null);
            }
            try {
                uri = ResolveURI.makeAbsolute(location, this.locator.getSystemId());
            } catch (URISyntaxException e) {
                this.error("package", attName, location, "Requires a valid URI.");
            }
            File file = new File(uri);
            try {
                this.packageLibrary.addPackage(file);
            } catch (XPathException e) {
                this.error(e);
            }
        } else {
            String priority;
            String exportLoc;
            String version = atts.getValue("version");
            if (version == null) {
                version = "1";
            }
            VersionedPackageName vpn = null;
            PackageDetails details = new PackageDetails();
            try {
                vpn = new VersionedPackageName(name, version);
            } catch (XPathException err) {
                this.error("package", "version", version, null);
            }
            details.nameAndVersion = vpn;
            this.currentPackage = details;
            String sourceLoc = atts.getValue("sourceLocation");
            StreamSource source = null;
            if (sourceLoc != null) {
                try {
                    source = new StreamSource(ResolveURI.makeAbsolute(sourceLoc, this.locator.getSystemId()).toString());
                } catch (URISyntaxException e) {
                    this.error("package", "sourceLocation", sourceLoc, "Requires a valid URI.");
                }
                details.sourceLocation = source;
            }
            if ((exportLoc = atts.getValue("exportLocation")) != null) {
                try {
                    source = new StreamSource(ResolveURI.makeAbsolute(exportLoc, this.locator.getSystemId()).toString());
                } catch (URISyntaxException e) {
                    this.error("package", "exportLocation", exportLoc, "Requires a valid URI.");
                }
                details.exportLocation = source;
            }
            if ((priority = atts.getValue("priority")) != null) {
                try {
                    details.priority = Integer.parseInt(priority);
                } catch (NumberFormatException err) {
                    this.error("package", "priority", priority, "Requires an integer.");
                }
            }
            details.baseName = atts.getValue("base");
            details.shortName = atts.getValue("shortName");
            this.packageLibrary.addPackage(details);
        }
    }

    protected void readWithParam(Attributes atts) {
        String name;
        if (this.currentPackage.exportLocation != null) {
            this.error("withParam", null, null, "Not allowed when @exportLocation exists");
        }
        if ((name = atts.getValue("name")) == null) {
            this.error("withParam", "name", null, null);
        }
        QNameParser qp = new QNameParser(this).withAcceptEQName(true);
        StructuredQName qName = null;
        try {
            qName = qp.parse(name, "");
        } catch (XPathException e) {
            this.error("withParam", "name", name, "Requires valid QName");
        }
        String select = atts.getValue("select");
        if (select == null) {
            this.error("withParam", "select", null, null);
        }
        IndependentContext env = new IndependentContext(this.config);
        env.setNamespaceResolver(this);
        XPathParser parser = new XPathParser();
        GroundedValue value = null;
        try {
            Expression exp = parser.parse(select, 0, 0, env);
            value = exp.iterate(env.makeEarlyEvaluationContext()).materialize();
        } catch (XPathException e) {
            this.error(e);
        }
        if (this.currentPackage.staticParams == null) {
            this.currentPackage.staticParams = new HashMap<StructuredQName, GroundedValue>();
        }
        this.currentPackage.staticParams.put(qName, value);
    }

    private void readXQueryElement(Attributes atts) {
        Properties props = new Properties();
        for (int i = 0; i < atts.getLength(); ++i) {
            String name = atts.getLocalName(i);
            String value = atts.getValue(i);
            if (value.isEmpty() || !atts.getURI(i).isEmpty()) continue;
            props.put(name, value);
        }
        props.put("#element", "xquery");
        this.applyProperty(props, "allowUpdate", Feature.XQUERY_ALLOW_UPDATE);
        this.applyProperty(props, "constructionMode", Feature.XQUERY_CONSTRUCTION_MODE);
        this.applyProperty(props, "defaultElementNamespace", Feature.XQUERY_DEFAULT_ELEMENT_NAMESPACE);
        this.applyProperty(props, "defaultFunctionNamespace", Feature.XQUERY_DEFAULT_FUNCTION_NAMESPACE);
        this.applyProperty(props, "emptyLeast", Feature.XQUERY_EMPTY_LEAST);
        this.applyProperty(props, "inheritNamespaces", Feature.XQUERY_INHERIT_NAMESPACES);
        this.applyProperty(props, "moduleUriResolver", Feature.MODULE_URI_RESOLVER_CLASS);
        this.applyProperty(props, "preserveBoundarySpace", Feature.XQUERY_PRESERVE_BOUNDARY_SPACE);
        this.applyProperty(props, "preserveNamespaces", Feature.XQUERY_PRESERVE_NAMESPACES);
        this.applyProperty(props, "requiredContextItemType", Feature.XQUERY_REQUIRED_CONTEXT_ITEM_TYPE);
        this.applyProperty(props, "schemaAware", Feature.XQUERY_SCHEMA_AWARE);
        this.applyProperty(props, "staticErrorListener", Feature.XQUERY_STATIC_ERROR_LISTENER_CLASS);
        this.applyProperty(props, "version", Feature.XQUERY_VERSION);
    }

    private void readXsltElement(Attributes atts) {
        Properties props = new Properties();
        for (int i = 0; i < atts.getLength(); ++i) {
            String name = atts.getLocalName(i);
            String value = atts.getValue(i);
            if (value.isEmpty() || !atts.getURI(i).isEmpty()) continue;
            props.put(name, value);
        }
        props.put("#element", "xslt");
        this.applyProperty(props, "disableXslEvaluate", Feature.DISABLE_XSL_EVALUATE);
        this.applyProperty(props, "enableAssertions", Feature.XSLT_ENABLE_ASSERTIONS);
        this.applyProperty(props, "initialMode", Feature.XSLT_INITIAL_MODE);
        this.applyProperty(props, "initialTemplate", Feature.XSLT_INITIAL_TEMPLATE);
        this.applyProperty(props, "messageEmitter", Feature.MESSAGE_EMITTER_CLASS);
        this.applyProperty(props, "outputUriResolver", Feature.OUTPUT_URI_RESOLVER_CLASS);
        this.applyProperty(props, "recoveryPolicy", Feature.RECOVERY_POLICY_NAME);
        this.applyProperty(props, "resultDocumentThreads", Feature.RESULT_DOCUMENT_THREADS);
        this.applyProperty(props, "schemaAware", Feature.XSLT_SCHEMA_AWARE);
        this.applyProperty(props, "staticErrorListener", Feature.XSLT_STATIC_ERROR_LISTENER_CLASS);
        this.applyProperty(props, "staticUriResolver", Feature.XSLT_STATIC_URI_RESOLVER_CLASS);
        this.applyProperty(props, "strictStreamability", Feature.STRICT_STREAMABILITY);
        this.applyProperty(props, "styleParser", Feature.STYLE_PARSER_CLASS);
        this.applyProperty(props, "version", Feature.XSLT_VERSION);
    }

    private void readXsdElement(Attributes atts) {
        Properties props = new Properties();
        for (int i = 0; i < atts.getLength(); ++i) {
            String name = atts.getLocalName(i);
            String value = atts.getValue(i);
            if (value.isEmpty() || !atts.getURI(i).isEmpty()) continue;
            props.put(name, value);
        }
        props.put("#element", "xsd");
        this.applyProperty(props, "assertionsCanSeeComments", Feature.ASSERTIONS_CAN_SEE_COMMENTS);
        this.applyProperty(props, "implicitSchemaImports", Feature.IMPLICIT_SCHEMA_IMPORTS);
        this.applyProperty(props, "multipleSchemaImports", Feature.MULTIPLE_SCHEMA_IMPORTS);
        this.applyProperty(props, "occurrenceLimits", Feature.OCCURRENCE_LIMITS);
        this.applyProperty(props, "schemaUriResolver", Feature.SCHEMA_URI_RESOLVER_CLASS);
        this.applyProperty(props, "thresholdForCompilingTypes", Feature.THRESHOLD_FOR_COMPILING_TYPES);
        this.applyProperty(props, "useXsiSchemaLocation", Feature.USE_XSI_SCHEMA_LOCATION);
        this.applyProperty(props, "version", Feature.XSD_VERSION);
    }

    private void error(String element, String attribute, String actual, String required) {
        XmlProcessingIncident err = attribute == null ? new XmlProcessingIncident("Invalid configuration element " + element) : (actual == null ? new XmlProcessingIncident("Missing configuration property " + element + "/@" + attribute) : new XmlProcessingIncident("Invalid configuration property " + element + "/@" + attribute + ". Supplied value '" + actual + "'. " + required));
        err.setLocation(Loc.makeFromSax(this.locator));
        this.errors.add(err);
    }

    protected void error(XPathException err) {
        err.setLocator(Loc.makeFromSax(this.locator));
        this.errors.add(new XmlProcessingException(err));
    }

    protected void errorClass(String element, String attribute, String actual, Class required, Exception cause) {
        XmlProcessingIncident err = new XmlProcessingIncident("Invalid configuration property " + element + (attribute == null ? "" : "/@" + attribute) + ". Supplied value '" + actual + "', required value is the name of a class that implements '" + required.getName() + "'");
        err.setCause(cause);
        err.setLocation(Loc.makeFromSax(this.locator));
        this.errors.add(err);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        String content;
        if (this.level == 3 && "resources".equals(this.section) && !(content = this.buffer.toString()).isEmpty()) {
            if ("externalObjectModel".equals(localName)) {
                try {
                    ExternalObjectModel model = (ExternalObjectModel)this.config.getInstance(content, null);
                    this.config.registerExternalObjectModel(model);
                } catch (ClassCastException | XPathException e) {
                    this.errorClass("externalObjectModel", null, content, ExternalObjectModel.class, e);
                }
            } else if ("extensionFunction".equals(localName)) {
                try {
                    ExtensionFunctionDefinition model = (ExtensionFunctionDefinition)this.config.getInstance(content, null);
                    this.config.registerExtensionFunction(model);
                } catch (ClassCastException | IllegalArgumentException | XPathException e) {
                    this.errorClass("extensionFunction", null, content, ExtensionFunctionDefinition.class, e);
                }
            } else if ("schemaDocument".equals(localName)) {
                try {
                    Source source = this.getInputSource(content);
                    this.config.addSchemaSource(source);
                } catch (XPathException e) {
                    this.errors.add(new XmlProcessingException(e));
                }
            } else if ("schemaComponentModel".equals(localName)) {
                try {
                    Source source = this.getInputSource(content);
                    this.config.importComponents(source);
                } catch (XPathException e) {
                    this.errors.add(new XmlProcessingException(e));
                }
            } else if (!"fileExtension".equals(localName)) {
                this.error(localName, null, null, null);
            }
        }
        --this.level;
        this.buffer.setLength(0);
        this.namespaceStack.pop();
    }

    private Source getInputSource(String href) throws XPathException {
        try {
            String base = this.locator.getSystemId();
            URI abs = ResolveURI.makeAbsolute(href, base);
            return new StreamSource(abs.toString());
        } catch (URISyntaxException e) {
            throw new XPathException(e);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        this.buffer.append(ch, start, length);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) {
    }

    @Override
    public void processingInstruction(String target, String data) {
    }

    @Override
    public void skippedEntity(String name) {
    }

    @Override
    public String getURIForPrefix(String prefix, boolean useDefault) {
        for (int i = this.namespaceStack.size() - 1; i >= 0; --i) {
            List list = (List)this.namespaceStack.get(i);
            for (String[] pair : list) {
                if (!pair[0].equals(prefix)) continue;
                return pair[1];
            }
        }
        return null;
    }

    @Override
    public Iterator<String> iteratePrefixes() {
        HashSet<String> prefixes = new HashSet<String>();
        for (List list : this.namespaceStack) {
            for (String[] pair : list) {
                prefixes.add(pair[0]);
            }
        }
        return prefixes.iterator();
    }
}

