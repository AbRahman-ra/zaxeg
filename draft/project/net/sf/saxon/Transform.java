/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.Version;
import net.sf.saxon.expr.instruct.GlobalContextRequirement;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.lib.AugmentedSource;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.StandardLogger;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SchemaValidator;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.UnprefixedElementMatchingPolicy;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltPackage;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trace.AbstractTraceListener;
import net.sf.saxon.trace.TimingCodeInjector;
import net.sf.saxon.trace.TimingTraceListener;
import net.sf.saxon.trans.CommandLineOptions;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.trans.LicenseException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.packages.PackageDetails;
import net.sf.saxon.trans.packages.PackageLibrary;
import net.sf.saxon.value.DateTimeValue;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class Transform {
    protected Processor processor;
    protected XsltCompiler compiler;
    protected boolean useURLs = false;
    protected boolean showTime = false;
    protected int repeat = 1;
    protected String sourceParserName = null;
    protected boolean schemaAware = false;
    protected boolean allowExit = true;
    protected boolean run = true;
    private Logger traceDestination = new StandardLogger();
    private boolean closeTraceDestination = false;

    public static void main(String[] args) {
        new Transform().doTransform(args, "java net.sf.saxon.Transform");
    }

    public void setPermittedOptions(CommandLineOptions options) {
        options.addRecognizedOption("a", 1, "Use <?xml-stylesheet?> processing instruction to identify stylesheet");
        options.addRecognizedOption("catalog", 258, "Use specified catalog file to resolve URIs");
        options.addRecognizedOption("config", 258, "Use specified configuration file");
        options.addRecognizedOption("cr", 259, "Use specified collection URI resolver class");
        options.addRecognizedOption("diag", 2, "Display runtime diagnostics");
        options.addRecognizedOption("dtd", 4, "Validate using DTD");
        options.setPermittedValues("dtd", new String[]{"on", "off", "recover"}, "on");
        options.addRecognizedOption("ea", 1, "Enable assertions");
        options.addRecognizedOption("expand", 1, "Expand attribute defaults from DTD or Schema");
        options.addRecognizedOption("explain", 2, "Display compiled expression tree and optimization decisions in human-readable form");
        options.addRecognizedOption("export", 2, "Display compiled expression tree and optimization decisions for exportation");
        options.addRecognizedOption("ext", 1, "Allow calls to Java extension functions and xsl:result-document");
        options.addRecognizedOption("im", 262, "Name of initial mode");
        options.addRecognizedOption("init", 3, "User-supplied net.sf.saxon.lib.Initializer class to initialize the Saxon Configuration");
        options.addRecognizedOption("it", 6, "Name of initial template");
        options.addRecognizedOption("jit", 1, "Just-in-time compilation");
        options.addRecognizedOption("l", 1, "Maintain line numbers for source documents");
        options.addRecognizedOption("lib", 263, "List of file names of library packages used by the stylesheet");
        options.addRecognizedOption("license", 1, "Check for local license file");
        options.addRecognizedOption("m", 3, "Use named class to handle xsl:message output");
        options.addRecognizedOption("nogo", 1, "Compile only, no evaluation");
        options.addRecognizedOption("now", 264, "Run with specified current date/time");
        options.addRecognizedOption("ns", 265, "Default namespace for element names (URI, or ##any, or ##html5)");
        options.addRecognizedOption("o", 258, "Use specified file for primary output");
        options.addRecognizedOption("opt", 265, "Enable/disable optimization options [-]cfgjklmnrsvwx");
        options.addRecognizedOption("or", 259, "Use named OutputURIResolver class");
        options.addRecognizedOption("outval", 260, "Action when validation of output file fails");
        options.setPermittedValues("outval", new String[]{"recover", "fatal"}, null);
        options.addRecognizedOption("p", 1, "Recognize query parameters in URI passed to doc()");
        options.addRecognizedOption("quit", 257, "Quit JVM if transformation fails");
        options.addRecognizedOption("r", 259, "Use named URIResolver class");
        options.addRecognizedOption("relocate", 1, "Produce relocatable packages");
        options.addRecognizedOption("repeat", 261, "Run N times for performance measurement");
        options.addRecognizedOption("s", 258, "Source file for primary input");
        options.addRecognizedOption("sa", 1, "Run in schema-aware mode");
        options.addRecognizedOption("scmin", 2, "Pre-load schema in SCM format");
        options.addRecognizedOption("strip", 260, "Handling of whitespace text nodes in source documents");
        options.setPermittedValues("strip", new String[]{"none", "all", "ignorable"}, null);
        options.addRecognizedOption("t", 1, "Display version and timing information, and names of output files");
        options.addRecognizedOption("target", 260, "Target Saxon edition for execution via -export");
        options.setPermittedValues("target", new String[]{"EE", "PE", "HE", "JS"}, null);
        options.addRecognizedOption("T", 3, "Use named TraceListener class, or standard TraceListener");
        options.addRecognizedOption("TB", 2, "Trace hotspot bytecode generation to specified XML file");
        options.addRecognizedOption("TJ", 1, "Debug binding and execution of extension functions");
        options.setPermittedValues("TJ", new String[]{"on", "off"}, "on");
        options.addRecognizedOption("Tlevel", 9, "Level of detail for trace listener output");
        options.setPermittedValues("Tlevel", new String[]{"none", "low", "normal", "high"}, "normal");
        options.addRecognizedOption("Tout", 2, "File for trace listener output");
        options.addRecognizedOption("TP", 2, "Use profiling trace listener, with specified output file");
        options.addRecognizedOption("threads", 261, "Run stylesheet on directory of files divided in N threads");
        options.addRecognizedOption("tree", 260, "Use specified tree model for source documents");
        options.setPermittedValues("tree", new String[]{"linked", "tiny", "tinyc"}, null);
        options.addRecognizedOption("traceout", 258, "File for output of trace() and -T output");
        options.addRecognizedOption("u", 1, "Interpret filename arguments as URIs");
        options.setPermittedValues("u", new String[]{"on", "off"}, "on");
        options.addRecognizedOption("val", 4, "Apply validation to source documents");
        options.setPermittedValues("val", new String[]{"strict", "lax"}, "strict");
        options.addRecognizedOption("versionmsg", 1, "No longer used");
        options.addRecognizedOption("warnings", 260, "No longer used");
        options.setPermittedValues("warnings", new String[]{"silent", "recover", "fatal"}, null);
        options.addRecognizedOption("x", 259, "Use named XMLReader class for parsing source documents");
        options.addRecognizedOption("xi", 1, "Expand XInclude directives in source documents");
        options.addRecognizedOption("xmlversion", 260, "Indicate whether XML 1.1 is supported");
        options.setPermittedValues("xmlversion", new String[]{"1.0", "1.1"}, null);
        options.addRecognizedOption("xsd", 263, "List of schema documents to be preloaded");
        options.addRecognizedOption("xsdversion", 260, "Indicate whether XSD 1.1 is supported");
        options.setPermittedValues("xsdversion", new String[]{"1.0", "1.1"}, null);
        options.addRecognizedOption("xsiloc", 1, "Load schemas named in xsi:schemaLocation (default on)");
        options.addRecognizedOption("xsl", 258, "Main stylesheet file");
        options.addRecognizedOption("y", 259, "Use named XMLReader class for parsing stylesheet and schema documents");
        options.addRecognizedOption("?", 512, "Display command line help text");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void doTransform(String[] args, String command) {
        block157: {
            Configuration config;
            String sourceFileName = null;
            String styleFileName = null;
            File outputFile = null;
            String outputFileName = null;
            boolean wholeDirectory = false;
            boolean dtdValidation = false;
            String styleParserName = null;
            boolean explain = false;
            boolean export = false;
            String explainOutputFileName = null;
            String exportOutputFileName = null;
            String additionalSchemas = null;
            TraceListener traceListener = null;
            TransformThread[] th = null;
            int threadCount = 0;
            boolean jit = true;
            CommandLineOptions options = new CommandLineOptions();
            this.setPermittedOptions(options);
            try {
                options.setActualOptions(args);
            } catch (XPathException err) {
                this.quit(err.getMessage(), 2);
            }
            this.schemaAware = false;
            String configFile = options.getOptionValue("config");
            if (configFile != null) {
                try {
                    config = Configuration.readConfiguration(new StreamSource(configFile));
                    this.initializeConfiguration(config);
                    this.processor = new Processor(config);
                    this.schemaAware = config.isLicensedFeature(2);
                } catch (XPathException e) {
                    this.quit(e.getMessage(), 2);
                }
            }
            if (this.processor == null && !this.schemaAware) {
                this.schemaAware = options.testIfSchemaAware();
            }
            if (this.processor == null) {
                this.processor = new Processor(true);
                config = this.processor.getUnderlyingConfiguration();
                this.initializeConfiguration(config);
                try {
                    this.setFactoryConfiguration(this.schemaAware, null);
                    CompilerInfo defaultCompilerInfo = config.getDefaultXsltCompilerInfo();
                    if (this.schemaAware) {
                        defaultCompilerInfo.setSchemaAware(true);
                    } else {
                        defaultCompilerInfo.setSchemaAware(false);
                    }
                } catch (Exception err) {
                    err.printStackTrace();
                    this.quit(err.getMessage(), 2);
                }
            }
            config = this.processor.getUnderlyingConfiguration();
            try {
                XMLReader styleParser;
                options.applyToConfiguration(this.processor);
                this.compiler = this.processor.newXsltCompiler();
                this.allowExit = !"off".equals(options.getOptionValue("quit"));
                boolean useAssociatedStylesheet = "on".equals(options.getOptionValue("a"));
                String value = options.getOptionValue("explain");
                if (value != null) {
                    explain = true;
                    this.processor.setConfigurationProperty(Feature.TRACE_OPTIMIZER_DECISIONS, true);
                    jit = false;
                    this.compiler.setJustInTimeCompilation(jit);
                    if (!"".equals(value)) {
                        explainOutputFileName = value;
                    }
                }
                if ((value = options.getOptionValue("export")) != null) {
                    export = true;
                    jit = false;
                    this.compiler.setJustInTimeCompilation(jit);
                    if (!"".equals(value)) {
                        exportOutputFileName = value;
                    }
                }
                if ((value = options.getOptionValue("target")) != null) {
                    this.compiler.setTargetEdition(value);
                }
                if ((value = options.getOptionValue("relocate")) != null && !"off".equals(value)) {
                    this.compiler.setRelocatable(true);
                }
                if ((value = options.getOptionValue("jit")) != null) {
                    if ("on".equals(value) && exportOutputFileName == null && this.run) {
                        if (export) {
                            jit = false;
                            System.err.println("Warning: -jit:on is ignored when -export:on is set");
                        } else {
                            jit = true;
                        }
                    } else if ("off".equals(value)) {
                        jit = false;
                    }
                    this.compiler.setJustInTimeCompilation(jit);
                }
                if ((value = options.getOptionValue("lib")) != null) {
                    StringTokenizer st = new StringTokenizer(value, File.pathSeparator);
                    Object packs = new HashSet<File>();
                    while (st.hasMoreTokens()) {
                        String n = st.nextToken();
                        packs.add(new File(n));
                    }
                    PackageLibrary lib = null;
                    try {
                        lib = new PackageLibrary(this.compiler.getUnderlyingCompilerInfo(), (Set<File>)packs);
                    } catch (XPathException e) {
                        this.quit(e.getMessage(), 2);
                    }
                    this.compiler.getUnderlyingCompilerInfo().setPackageLibrary(lib);
                }
                if ((value = options.getOptionValue("ns")) != null) {
                    if (value.equals("##any")) {
                        this.compiler.setUnprefixedElementMatchingPolicy(UnprefixedElementMatchingPolicy.ANY_NAMESPACE);
                    } else if (value.equals("##html5")) {
                        this.compiler.setDefaultElementNamespace("http://www.w3.org/1999/xhtml");
                        this.compiler.setUnprefixedElementMatchingPolicy(UnprefixedElementMatchingPolicy.DEFAULT_NAMESPACE_OR_NONE);
                    } else {
                        this.compiler.setDefaultElementNamespace(value);
                    }
                }
                if ((value = options.getOptionValue("o")) != null) {
                    outputFileName = value;
                }
                if ((value = options.getOptionValue("nogo")) != null) {
                    if ("on".equals(options.getOptionValue("jit"))) {
                        System.err.println("Warning: -jit:on is ignored when -nogo is set");
                    }
                    this.run = false;
                    this.compiler.setJustInTimeCompilation(false);
                }
                if ("on".equals(value = options.getOptionValue("p"))) {
                    config.setParameterizedURIResolver();
                    this.useURLs = true;
                }
                if ((value = options.getOptionValue("repeat")) != null) {
                    try {
                        this.repeat = Integer.parseInt(value);
                    } catch (NumberFormatException err) {
                        this.badUsage("Bad number after -repeat");
                    }
                }
                if ((value = options.getOptionValue("s")) != null) {
                    sourceFileName = value;
                }
                if ((value = options.getOptionValue("threads")) != null) {
                    threadCount = Integer.parseInt(value);
                }
                if ((value = options.getOptionValue("t")) != null) {
                    System.err.println(config.getProductTitle());
                    System.err.println(Version.platform.getPlatformVersion());
                    this.processor.setConfigurationProperty(Feature.TIMING, true);
                    this.showTime = true;
                }
                if ((value = options.getOptionValue("T")) != null) {
                    String out = options.getOptionValue("Tout");
                    if (out != null) {
                        config.setTraceListenerOutputFile(out);
                    }
                    if ("".equals(value)) {
                        value = "net.sf.saxon.trace.XSLTTraceListener";
                    }
                    traceListener = config.makeTraceListener(value);
                    this.processor.setConfigurationProperty(Feature.TRACE_LISTENER, traceListener);
                    this.processor.setConfigurationProperty(Feature.LINE_NUMBERING, true);
                    value = options.getOptionValue("Tlevel");
                    if (value != null && traceListener instanceof AbstractTraceListener) {
                        switch (value) {
                            case "none": {
                                ((AbstractTraceListener)traceListener).setLevelOfDetail(0);
                                break;
                            }
                            case "low": {
                                ((AbstractTraceListener)traceListener).setLevelOfDetail(1);
                                break;
                            }
                            case "normal": {
                                ((AbstractTraceListener)traceListener).setLevelOfDetail(2);
                                break;
                            }
                            case "high": {
                                ((AbstractTraceListener)traceListener).setLevelOfDetail(3);
                            }
                        }
                    }
                }
                if ((value = options.getOptionValue("TB")) != null) {
                    config.setBooleanProperty(Feature.MONITOR_HOT_SPOT_BYTE_CODE, true);
                }
                if ((value = options.getOptionValue("TP")) != null) {
                    traceListener = new TimingTraceListener();
                    this.processor.setConfigurationProperty(Feature.TRACE_LISTENER, traceListener);
                    this.processor.setConfigurationProperty(Feature.LINE_NUMBERING, true);
                    this.compiler.getUnderlyingCompilerInfo().setCodeInjector(new TimingCodeInjector());
                    if (!value.isEmpty()) {
                        traceListener.setOutputDestination(new StandardLogger(new File(value)));
                    }
                }
                if ((value = options.getOptionValue("traceout")) == null) {
                    if (this.traceDestination == null) {
                        this.traceDestination = config.getLogger();
                    }
                } else {
                    switch (value) {
                        case "#err": {
                            this.traceDestination = new StandardLogger();
                            break;
                        }
                        case "#out": {
                            this.traceDestination = new StandardLogger(System.out);
                            break;
                        }
                        case "#null": {
                            this.traceDestination = null;
                            break;
                        }
                        default: {
                            this.traceDestination = new StandardLogger(new File(value));
                            this.closeTraceDestination = true;
                        }
                    }
                }
                if ((value = options.getOptionValue("u")) != null) {
                    this.useURLs = "on".equals(value);
                }
                if ((value = options.getOptionValue("x")) != null) {
                    this.sourceParserName = value;
                    this.processor.setConfigurationProperty(Feature.SOURCE_PARSER_CLASS, this.sourceParserName);
                }
                if ((value = options.getOptionValue("xsd")) != null) {
                    additionalSchemas = value;
                }
                if ((value = options.getOptionValue("xsdversion")) != null) {
                    this.processor.setConfigurationProperty(Feature.XSD_VERSION, value);
                }
                if ((value = options.getOptionValue("xsl")) != null) {
                    styleFileName = value;
                }
                if ((value = options.getOptionValue("y")) != null) {
                    styleParserName = value;
                    this.processor.setConfigurationProperty(Feature.STYLE_PARSER_CLASS, value);
                }
                if ((value = options.getOptionValue("?")) != null) {
                    this.badUsage("");
                }
                if (!config.getEditionCode().equals("HE")) {
                    String lic = options.getOptionValue("license");
                    if (lic == null || "on".equals(lic)) {
                        config.displayLicenseMessage();
                    } else {
                        config.disableLicensing();
                    }
                }
                this.applyLocalOptions(options, config);
                if (options.getOptionValue("it") != null && useAssociatedStylesheet) {
                    this.badUsage("-it and -a options cannot be used together");
                }
                if (options.getOptionValue("xsiloc") != null && options.getOptionValue("val") == null) {
                    System.err.println("-xsiloc is ignored when -val is absent");
                }
                List<String> positional = options.getPositionalOptions();
                int currentPositionalOption = 0;
                if (this.run && options.getOptionValue("it") == null && sourceFileName == null) {
                    if (positional.size() == currentPositionalOption) {
                        this.badUsage("No source file name");
                    }
                    sourceFileName = positional.get(currentPositionalOption++);
                }
                if (!useAssociatedStylesheet && styleFileName == null) {
                    if (positional.size() == currentPositionalOption) {
                        this.badUsage("No stylesheet file name");
                    }
                    styleFileName = positional.get(currentPositionalOption++);
                }
                if (currentPositionalOption < positional.size()) {
                    this.badUsage("Unrecognized option: " + positional.get(currentPositionalOption));
                }
                if ((value = options.getOptionValue("scmin")) != null) {
                    config.importComponents(new StreamSource(value));
                }
                if (additionalSchemas != null) {
                    CommandLineOptions.loadAdditionalSchemas(config, additionalSchemas);
                }
                options.applyStaticParams(this.compiler);
                List<Source> sources = new ArrayList<Source>();
                if (sourceFileName != null) {
                    boolean useSAXSource = this.sourceParserName != null || dtdValidation;
                    wholeDirectory = CommandLineOptions.loadDocuments(sourceFileName, this.useURLs, this.processor, useSAXSource, sources);
                    sources = this.preprocess(sources);
                    if (wholeDirectory) {
                        if (outputFileName == null) {
                            this.quit("To process a directory, -o must be specified", 2);
                        } else if (outputFileName.equals(sourceFileName)) {
                            this.quit("Output directory must be different from input", 2);
                        } else {
                            outputFile = new File(outputFileName);
                            if (!outputFile.isDirectory()) {
                                this.quit("Input is a directory, but output is not", 2);
                            }
                        }
                    }
                }
                if (outputFileName != null && !wholeDirectory && (outputFile = new File(outputFileName)).isDirectory()) {
                    this.quit("Output is a directory, but input is not", 2);
                }
                if (useAssociatedStylesheet) {
                    if (wholeDirectory) {
                        this.processDirectoryAssoc(sources, outputFile, options);
                    } else {
                        this.processFileAssoc((Source)sources.get(0), null, outputFile, options);
                    }
                    break block157;
                }
                long startTime = Transform.now();
                boolean isURI = this.useURLs || CommandLineOptions.isImplicitURI(styleFileName);
                XsltExecutable sheet = null;
                Source styleSource = null;
                if (isURI) {
                    styleSource = config.getURIResolver().resolve(styleFileName, null);
                    if (styleSource == null) {
                        styleSource = config.getSystemURIResolver().resolve(styleFileName, null);
                    }
                } else if (styleFileName.equals("-")) {
                    String sysId = new File(System.getProperty("user.dir")).toURI().toASCIIString();
                    if (styleParserName == null) {
                        styleSource = new StreamSource(System.in, sysId);
                    } else if (Version.platform.isJava()) {
                        styleParser = config.getStyleParser();
                        InputSource inputSource = new InputSource(System.in);
                        inputSource.setSystemId(sysId);
                        styleSource = new SAXSource(styleParser, inputSource);
                    } else {
                        styleSource = new StreamSource(System.in, sysId);
                    }
                } else {
                    PackageLibrary library = config.getDefaultXsltCompilerInfo().getPackageLibrary();
                    PackageDetails details = library.findDetailsForAlias(styleFileName);
                    if (details != null) {
                        XsltPackage pack = this.compiler.obtainPackageWithAlias(styleFileName);
                        sheet = pack.link();
                    } else {
                        File sheetFile = new File(styleFileName);
                        if (!sheetFile.exists()) {
                            this.quit("Stylesheet file " + sheetFile + " does not exist", 2);
                        }
                        if (styleParserName == null) {
                            styleSource = new StreamSource(sheetFile.toURI().toString());
                        } else {
                            InputSource eis = new InputSource(sheetFile.toURI().toString());
                            styleParser = config.getStyleParser();
                            styleSource = new SAXSource(styleParser, eis);
                        }
                    }
                }
                if (styleSource == null && sheet == null) {
                    this.quit("URIResolver for stylesheet file must return a Source", 2);
                }
                if (sheet == null) {
                    int repeatComp;
                    if (export && !this.run) {
                        try {
                            XsltPackage pack = this.compiler.compilePackage(styleSource);
                            pack.save(new File(exportOutputFileName));
                            if (this.showTime) {
                                System.err.println("Stylesheet exported to: " + new File(exportOutputFileName).getAbsolutePath());
                            }
                            return;
                        } catch (SaxonApiException err) {
                            this.quit(err.getMessage(), 2);
                        }
                    }
                    if ((repeatComp = this.repeat) > 20) {
                        repeatComp = 20;
                    }
                    if (repeatComp == 1) {
                        sheet = this.compiler.compile(styleSource);
                        if (this.showTime) {
                            long endTime = Transform.now();
                            System.err.println("Stylesheet compilation time: " + CommandLineOptions.showExecutionTimeNano(endTime - startTime));
                        }
                    } else {
                        startTime = Transform.now();
                        long totalTime = 0L;
                        Logger logger = config.getLogger();
                        int threshold = logger instanceof StandardLogger ? ((StandardLogger)logger).getThreshold() : 0;
                        for (int j = -repeatComp; j < repeatComp; ++j) {
                            if (j == 0) {
                                startTime = Transform.now();
                                if (logger instanceof StandardLogger) {
                                    ((StandardLogger)logger).setThreshold(threshold);
                                }
                                Compilation.TIMING = true;
                            } else {
                                if (logger instanceof StandardLogger) {
                                    ((StandardLogger)logger).setThreshold(2);
                                }
                                Compilation.TIMING = false;
                            }
                            sheet = this.compiler.compile(styleSource);
                            if (!this.showTime || j < 0) continue;
                            long endTime = Transform.now();
                            long elapsed = endTime - startTime;
                            System.err.println("Stylesheet compilation time: " + CommandLineOptions.showExecutionTimeNano(elapsed));
                            startTime = endTime;
                            totalTime += elapsed;
                        }
                        if (this.showTime) {
                            System.err.println("Average compilation time: " + CommandLineOptions.showExecutionTimeNano(totalTime / (long)repeatComp));
                        }
                    }
                    if (this.schemaAware) {
                        int licenseId = sheet.getUnderlyingCompiledStylesheet().getTopLevelPackage().getLocalLicenseId();
                        config.checkLicensedFeature(2, "schema-aware XSLT", licenseId);
                    }
                }
                if (explain) {
                    Serializer out = explainOutputFileName == null ? this.processor.newSerializer(System.err) : this.processor.newSerializer(new File(explainOutputFileName));
                    out.setOutputProperty(Serializer.Property.METHOD, "xml");
                    out.setOutputProperty(Serializer.Property.INDENT, "yes");
                    out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
                    if (config.isLicensedFeature(8)) {
                        out.setOutputProperty(Serializer.Property.SAXON_INDENT_SPACES, "2");
                    }
                    sheet.explain(out);
                }
                if (export) {
                    sheet.export(new FileOutputStream(exportOutputFileName));
                    if (this.showTime) {
                        System.err.println("Stylesheet exported to: " + new File(exportOutputFileName).getAbsolutePath());
                    }
                }
                if (this.run) {
                    try {
                        if (wholeDirectory) {
                            if (threadCount > 0 && sources.size() > 1) {
                                if (threadCount > sources.size()) {
                                    threadCount = sources.size();
                                }
                                int sourcesPerThread = (int)Math.floor(sources.size() / threadCount);
                                int rem = sources.size() % threadCount;
                                th = new TransformThread[threadCount];
                                int j = 0;
                                int z = 0;
                                for (int i = 0; i < sources.size(); i += sourcesPerThread + z) {
                                    z = j < rem ? 1 : 0;
                                    th[j] = new TransformThread(i, sheet, sources.subList(i, i + sourcesPerThread + z), outputFile, options);
                                    th[j].start();
                                    ++j;
                                }
                                for (TransformThread aTh : th) {
                                    aTh.join();
                                }
                            } else {
                                this.processDirectory(sources, sheet, outputFile, options);
                            }
                        } else {
                            Source source = sources == null || sources.isEmpty() ? null : sources.get(0);
                            this.processFile(source, sheet, outputFile, options);
                        }
                    } finally {
                        if (this.closeTraceDestination && this.traceDestination != null) {
                            this.traceDestination.close();
                        }
                    }
                }
                if (options.getOptionValue("TB") != null) {
                    config.createByteCodeReport(options.getOptionValue("TB"));
                }
            } catch (TerminationException err) {
                this.quit(err.getMessage(), 1);
            } catch (SaxonApiException err) {
                this.quit(err.getMessage(), 2);
            } catch (TransformerException | TransformerFactoryConfigurationError | LicenseException err) {
                this.quit("Transformation failed: " + err.getMessage(), 2);
            } catch (Exception err2) {
                err2.printStackTrace();
                this.quit("Fatal error during transformation: " + err2.getClass().getName() + ": " + (err2.getMessage() == null ? " (no message)" : err2.getMessage()), 2);
            }
        }
    }

    protected void initializeConfiguration(Configuration config) {
    }

    public void setFactoryConfiguration(boolean schemaAware, String className) throws LicenseException {
    }

    protected void applyLocalOptions(CommandLineOptions options, Configuration config) {
    }

    public List<Source> preprocess(List<Source> sources) throws XPathException {
        return sources;
    }

    protected Configuration getConfiguration() {
        return this.processor.getUnderlyingConfiguration();
    }

    protected void quit(String message, int code) {
        System.err.println(message);
        if (!this.allowExit) {
            throw new RuntimeException(message);
        }
        System.exit(code);
    }

    private void processDirectoryAssoc(List<Source> sources, File outputDir, CommandLineOptions options) throws Exception {
        int failures = 0;
        for (Source source : sources) {
            String localName = Transform.getLocalFileName(source);
            try {
                this.processFileAssoc(source, localName, outputDir, options);
            } catch (SaxonApiException err) {
                ++failures;
                System.err.println("While processing " + localName + ": " + err.getMessage() + '\n');
            }
        }
        if (failures > 0) {
            throw new XPathException(failures + " transformation" + (failures == 1 ? "" : "s") + " failed");
        }
    }

    private File makeOutputFile(File directory, String localName, XsltExecutable sheet) {
        String mediaType = sheet.getUnderlyingCompiledStylesheet().getPrimarySerializationProperties().getProperty("media-type");
        String suffix = ".xml";
        if ("text/html".equals(mediaType)) {
            suffix = ".html";
        } else if ("text/plain".equals(mediaType)) {
            suffix = ".txt";
        }
        String prefix = localName;
        if (localName.endsWith(".xml") || localName.endsWith(".XML")) {
            prefix = localName.substring(0, localName.length() - 4);
        }
        return new File(directory, prefix + suffix);
    }

    private void processFileAssoc(Source sourceInput, String localName, File outputFile, CommandLineOptions options) throws SaxonApiException {
        if (this.showTime) {
            System.err.println("Processing " + sourceInput.getSystemId() + " using associated stylesheet");
        }
        long startTime = Transform.now();
        XdmNode sourceDoc = this.processor.newDocumentBuilder().build(sourceInput);
        Source style = this.compiler.getAssociatedStylesheet(sourceDoc.asSource(), null, null, null);
        XsltExecutable sheet = this.compiler.compile(style);
        if (this.showTime) {
            System.err.println("Prepared associated stylesheet " + style.getSystemId());
        }
        Xslt30Transformer transformer = this.newTransformer(sheet, options);
        File outFile = outputFile;
        if (outFile != null && outFile.isDirectory()) {
            outFile = this.makeOutputFile(outFile, localName, sheet);
        }
        Serializer serializer = outputFile == null ? this.processor.newSerializer(System.out) : this.processor.newSerializer(outFile);
        try {
            options.setSerializationProperties(serializer);
        } catch (IllegalArgumentException e) {
            this.quit(e.getMessage(), 2);
        }
        transformer.setGlobalContextItem(sourceDoc);
        transformer.applyTemplates(sourceDoc, (Destination)serializer);
        if (this.showTime) {
            long endTime = Transform.now();
            System.err.println("Execution time: " + CommandLineOptions.showExecutionTimeNano(endTime - startTime));
        }
    }

    protected Xslt30Transformer newTransformer(XsltExecutable sheet, CommandLineOptions options) throws SaxonApiException {
        String now;
        Configuration config = this.getConfiguration();
        Xslt30Transformer transformer = sheet.load30();
        transformer.setTraceFunctionDestination(this.traceDestination);
        String initialMode = options.getOptionValue("im");
        if (initialMode != null) {
            transformer.setInitialMode(QName.fromClarkName(initialMode));
        }
        if ((now = options.getOptionValue("now")) != null) {
            try {
                DateTimeValue currentDateTime = (DateTimeValue)DateTimeValue.makeDateTimeValue(now, config.getConversionRules()).asAtomic();
                transformer.getUnderlyingController().setCurrentDateTime(currentDateTime);
            } catch (XPathException e) {
                throw new SaxonApiException("Failed to set current time: " + e.getMessage(), e);
            }
        }
        if ("on".equals(options.getOptionValue("ea"))) {
            transformer.getUnderlyingController().setAssertionsEnabled(true);
        } else if ("off".equals(options.getOptionValue("ea"))) {
            transformer.getUnderlyingController().setAssertionsEnabled(true);
        }
        return transformer;
    }

    protected static long now() {
        return System.nanoTime();
    }

    private void processDirectory(List<Source> sources, XsltExecutable sheet, File outputDir, CommandLineOptions options) throws SaxonApiException {
        int failures = 0;
        for (Source source : sources) {
            String localName = Transform.getLocalFileName(source);
            try {
                File outputFile = this.makeOutputFile(outputDir, localName, sheet);
                this.processFile(source, sheet, outputFile, options);
            } catch (SaxonApiException err) {
                ++failures;
                System.err.println("While processing " + localName + ": " + err.getMessage() + '\n');
            }
        }
        if (failures > 0) {
            throw new SaxonApiException(failures + " transformation" + (failures == 1 ? "" : "s") + " failed");
        }
    }

    private static String getLocalFileName(Source source) {
        try {
            String path = new URI(source.getSystemId()).getPath();
            while (true) {
                int sep;
                if ((sep = path.indexOf(47)) < 0) {
                    return path;
                }
                path = path.substring(sep + 1);
            }
        } catch (URISyntaxException err) {
            throw new IllegalArgumentException(err.getMessage());
        }
    }

    protected void processFile(Source source, XsltExecutable sheet, File outputFile, CommandLineOptions options) throws SaxonApiException {
        long totalTime = 0L;
        int runs = 0;
        int halfway = this.repeat / 2 - 1;
        for (int r = 0; r < this.repeat; ++r) {
            String method;
            Serializer serializer;
            if (this.showTime) {
                String initialTemplate;
                String msg = "Processing ";
                msg = source != null ? msg + source.getSystemId() : msg + " (no source document)";
                String initialMode = options.getOptionValue("im");
                if (initialMode != null) {
                    msg = msg + " initial mode = " + initialMode;
                }
                if ((initialTemplate = options.getOptionValue("it")) != null) {
                    msg = msg + " initial template = " + (initialTemplate.isEmpty() ? "xsl:initial-template" : initialTemplate);
                }
                System.err.println(msg);
            }
            long startTime = Transform.now();
            if (r == halfway) {
                runs = 0;
                totalTime = 0L;
            }
            ++runs;
            if (r < halfway) {
                this.traceDestination = null;
            }
            Xslt30Transformer transformer = this.newTransformer(sheet, options);
            if (outputFile == null) {
                transformer.setBaseOutputURI(new File(System.getProperty("user.dir")).toURI().toASCIIString());
                serializer = this.processor.newSerializer(System.out);
            } else {
                serializer = this.processor.newSerializer(outputFile);
            }
            try {
                options.setSerializationProperties(serializer);
            } catch (IllegalArgumentException e) {
                this.quit(e.getMessage(), 2);
            }
            String buildTreeProperty = serializer.getOutputProperty(Serializer.Property.BUILD_TREE);
            boolean buildResultTree = "yes".equals(buildTreeProperty) ? true : ("no".equals(buildTreeProperty) ? false : !"json".equals(method = serializer.getOutputProperty(Serializer.Property.METHOD)) && !"adaptive".equals(method));
            String initialTemplate = options.getOptionValue("it");
            if (source != null) {
                boolean buildSourceTree;
                PreparedStylesheet pss = sheet.getUnderlyingCompiledStylesheet();
                GlobalContextRequirement requirement = pss.getGlobalContextRequirement();
                if (requirement == null) {
                    buildSourceTree = initialTemplate != null || !transformer.getUnderlyingController().getInitialMode().isDeclaredStreamable();
                } else {
                    boolean bl = buildSourceTree = !requirement.isAbsentFocus();
                }
                if (buildSourceTree) {
                    DocumentBuilder builder = this.processor.newDocumentBuilder();
                    StylesheetPackage top = pss.getTopLevelPackage();
                    if (!top.isStripsTypeAnnotations()) {
                        int validationMode = this.getConfiguration().getSchemaValidationMode();
                        if (validationMode == 1) {
                            builder.setSchemaValidator(this.processor.getSchemaManager().newSchemaValidator());
                        } else if (validationMode == 2) {
                            SchemaValidator validator = this.processor.getSchemaManager().newSchemaValidator();
                            validator.setLax(true);
                            builder.setSchemaValidator(validator);
                        }
                    }
                    builder.setDTDValidation(this.getConfiguration().getBooleanProperty(Feature.DTD_VALIDATION));
                    builder.setWhitespaceStrippingPolicy(sheet.getWhitespaceStrippingPolicy());
                    if (this.getConfiguration().getBooleanProperty(Feature.DTD_VALIDATION_RECOVERABLE)) {
                        source = new AugmentedSource(source, this.getConfiguration().getParseOptions());
                    }
                    XdmNode node = builder.build(source);
                    transformer.setGlobalContextItem(node, true);
                    source = node.asSource();
                }
            }
            options.applyFileParams(this.processor, transformer);
            if (initialTemplate != null) {
                QName initialTemplateName = initialTemplate.isEmpty() ? new QName("xsl", "http://www.w3.org/1999/XSL/Transform", "initial-template") : QName.fromClarkName(initialTemplate);
                if (buildResultTree) {
                    transformer.callTemplate(initialTemplateName, serializer);
                } else {
                    XdmValue result = transformer.callTemplate(initialTemplateName);
                    serializer.serializeXdmValue(result);
                }
            } else if (buildResultTree) {
                transformer.applyTemplates(source, (Destination)serializer);
            } else {
                XdmValue result = transformer.applyTemplates(source);
                serializer.serializeXdmValue(result);
            }
            long endTime = Transform.now();
            totalTime += endTime - startTime;
            if (this.showTime) {
                System.err.println("Execution time: " + CommandLineOptions.showExecutionTimeNano(endTime - startTime));
                CommandLineOptions.showMemoryUsed();
                if (this.repeat > 1) {
                    System.err.println("-------------------------------");
                    Runtime.getRuntime().gc();
                }
            }
            if (this.repeat == 999999 && totalTime > 60000L) break;
        }
        if (this.repeat > 1) {
            System.err.println("*** Average execution time over last " + runs + " runs: " + CommandLineOptions.showExecutionTimeNano(totalTime / (long)runs));
        }
    }

    protected void badUsage(String message) {
        if (!"".equals(message)) {
            System.err.println(message);
        }
        if (!this.showTime) {
            System.err.println(this.getConfiguration().getProductTitle());
        }
        System.err.println("Usage: see http://www.saxonica.com/documentation/index.html#!using-xsl/commandline");
        System.err.println("Format: " + CommandLineOptions.getCommandName(this) + " options params");
        CommandLineOptions options = new CommandLineOptions();
        this.setPermittedOptions(options);
        System.err.println("Options available:" + options.displayPermittedOptions());
        System.err.println("Use -XYZ:? for details of option XYZ");
        System.err.println("Params: ");
        System.err.println("  param=value           Set stylesheet string parameter");
        System.err.println("  +param=filename       Set stylesheet document parameter");
        System.err.println("  ?param=expression     Set stylesheet parameter using XPath");
        System.err.println("  !param=value          Set serialization parameter");
        if (this.allowExit) {
            if ("".equals(message)) {
                System.exit(0);
            } else {
                System.exit(2);
            }
        } else {
            throw new RuntimeException(message);
        }
    }

    private String getCommandName() {
        String s = this.getClass().getName();
        if (s.equals("cli.Saxon.Cmd.DotNetTransform")) {
            s = "Transform";
        }
        return s;
    }

    class TransformThread
    extends Thread {
        private File outputDir;
        private XsltExecutable sheet;
        private CommandLineOptions options;
        private List<Source> sources;
        private int start;

        TransformThread(int i, XsltExecutable st, List<Source> s, File out, CommandLineOptions opt) {
            this.start = i;
            this.sheet = st;
            this.sources = s;
            this.options = opt;
            this.outputDir = out;
        }

        public long getStart() {
            return this.start;
        }

        @Override
        public void run() {
            try {
                Transform.this.processDirectory(this.sources, this.sheet, this.outputDir, this.options);
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }
}

