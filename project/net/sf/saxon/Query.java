/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.Version;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.lib.AugmentedSource;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.lib.StandardLogger;
import net.sf.saxon.om.DocumentPool;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.query.QueryReader;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.query.UpdateAgent;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trace.Instrumentation;
import net.sf.saxon.trace.TimingCodeInjector;
import net.sf.saxon.trace.TimingTraceListener;
import net.sf.saxon.trace.XQueryTraceListener;
import net.sf.saxon.trans.CommandLineOptions;
import net.sf.saxon.trans.LicenseException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.value.DateTimeValue;
import org.xml.sax.InputSource;

public class Query {
    protected Processor processor;
    protected Configuration config;
    protected boolean showTime = false;
    protected int repeat = 1;
    protected String sourceFileName = null;
    protected String queryFileName = null;
    protected boolean useURLs = false;
    protected String outputFileName = null;
    protected String moduleURIResolverClass = null;
    protected final String uriResolverClass;
    protected boolean explain = false;
    protected boolean wrap = false;
    protected boolean projection = false;
    protected boolean streaming = false;
    protected boolean updating = false;
    protected boolean writeback = false;
    protected boolean backup = true;
    protected String explainOutputFileName = null;
    private Logger traceDestination = new StandardLogger();
    private boolean closeTraceDestination = false;
    private boolean allowExit = true;

    public Query() {
        this.uriResolverClass = null;
    }

    protected Configuration getConfiguration() {
        return this.config;
    }

    public static void main(String[] args) {
        new Query().doQuery(args, "java net.sf.saxon.Query");
    }

    public void setPermittedOptions(CommandLineOptions options) {
        options.addRecognizedOption("backup", 1, "Save updated documents before overwriting");
        options.addRecognizedOption("catalog", 258, "Use specified catalog file to resolve URIs");
        options.addRecognizedOption("config", 258, "Use specified configuration file");
        options.addRecognizedOption("cr", 259, "Use specified collection URI resolver class");
        options.addRecognizedOption("dtd", 4, "Validate using DTD");
        options.setPermittedValues("dtd", new String[]{"on", "off", "recover"}, "on");
        options.addRecognizedOption("expand", 1, "Expand attribute defaults from DTD or Schema");
        options.addRecognizedOption("explain", 2, "Display compiled expression tree and optimization decisions");
        options.addRecognizedOption("ext", 1, "Allow calls to Java extension functions and xsl:result-document");
        options.addRecognizedOption("init", 3, "User-supplied net.sf.saxon.lib.Initializer class to initialize the Saxon Configuration");
        options.addRecognizedOption("l", 1, "Maintain line numbers for source documents");
        options.addRecognizedOption("mr", 259, "Use named ModuleURIResolver class");
        options.addRecognizedOption("now", 264, "Run with specified current date/time");
        options.addRecognizedOption("o", 258, "Use specified file for primary output");
        options.addRecognizedOption("opt", 265, "Enable/disable optimization options [-]cfgklmnsvwx");
        options.addRecognizedOption("outval", 260, "Action when validation of output file fails");
        options.setPermittedValues("outval", new String[]{"recover", "fatal"}, null);
        options.addRecognizedOption("p", 1, "Recognize query parameters in URI passed to doc()");
        options.addRecognizedOption("projection", 1, "Use source document projection");
        options.addRecognizedOption("q", 258, "Query filename");
        options.addRecognizedOption("qs", 265, "Query string (usually in quotes)");
        options.addRecognizedOption("quit", 257, "Quit JVM if query fails");
        options.addRecognizedOption("r", 259, "Use named URIResolver class");
        options.addRecognizedOption("repeat", 261, "Run N times for performance measurement");
        options.addRecognizedOption("s", 258, "Source file for primary input");
        options.addRecognizedOption("sa", 1, "Run in schema-aware mode");
        options.addRecognizedOption("scmin", 2, "Pre-load schema in SCM format");
        options.addRecognizedOption("stream", 1, "Execute in streamed mode");
        options.addRecognizedOption("strip", 260, "Handling of whitespace text nodes in source documents");
        options.setPermittedValues("strip", new String[]{"none", "all", "ignorable"}, null);
        options.addRecognizedOption("t", 1, "Display version and timing information");
        options.addRecognizedOption("T", 3, "Use named TraceListener class, or standard TraceListener");
        options.addRecognizedOption("TB", 2, "Trace hotspot bytecode generation to specified XML file");
        options.addRecognizedOption("TJ", 1, "Debug binding and execution of extension functions");
        options.setPermittedValues("TJ", new String[]{"on", "off"}, "on");
        options.addRecognizedOption("tree", 260, "Use specified tree model for source documents");
        options.addRecognizedOption("Tlevel", 9, "Level of detail for trace listener output");
        options.setPermittedValues("Tlevel", new String[]{"none", "low", "normal", "high"}, "normal");
        options.addRecognizedOption("Tout", 2, "File for trace listener output");
        options.addRecognizedOption("TP", 2, "Use profiling trace listener, with specified output file");
        options.addRecognizedOption("traceout", 258, "File for output of trace() messages");
        options.setPermittedValues("tree", new String[]{"linked", "tiny", "tinyc"}, null);
        options.addRecognizedOption("u", 1, "Interpret filename arguments as URIs");
        options.setPermittedValues("u", new String[]{"on", "off"}, "on");
        options.addRecognizedOption("update", 260, "Enable or disable XQuery updates, or enable the syntax but discard the updates");
        options.setPermittedValues("update", new String[]{"on", "off", "discard"}, null);
        options.addRecognizedOption("val", 4, "Apply validation to source documents");
        options.setPermittedValues("val", new String[]{"strict", "lax"}, "strict");
        options.addRecognizedOption("wrap", 1, "Wrap result sequence in XML elements");
        options.addRecognizedOption("x", 259, "Use named XMLReader class for parsing source documents");
        options.addRecognizedOption("xi", 1, "Expand XInclude directives in source documents");
        options.addRecognizedOption("xmlversion", 260, "Indicate whether XML 1.1 is supported");
        options.setPermittedValues("xmlversion", new String[]{"1.0", "1.1"}, null);
        options.addRecognizedOption("xsd", 263, "List of schema documents to be preloaded");
        options.addRecognizedOption("xsdversion", 260, "Indicate whether XSD 1.1 is supported");
        options.setPermittedValues("xsdversion", new String[]{"1.0", "1.1"}, null);
        options.addRecognizedOption("xsiloc", 1, "Load schemas named in xsi:schemaLocation (default on)");
        options.addRecognizedOption("?", 512, "Display command line help text");
    }

    protected void doQuery(String[] args, String command) {
        CommandLineOptions options = new CommandLineOptions();
        this.setPermittedOptions(options);
        try {
            options.setActualOptions(args);
        } catch (XPathException err) {
            this.quit(err.getMessage(), 2);
        }
        boolean schemaAware = false;
        String configFile = options.getOptionValue("config");
        if (configFile != null) {
            try {
                this.config = Configuration.readConfiguration(new StreamSource(configFile));
                this.initializeConfiguration(this.config);
                schemaAware = this.config.isLicensedFeature(4);
            } catch (XPathException e) {
                this.quit(e.getMessage(), 2);
            }
        }
        if (this.config == null && !schemaAware) {
            schemaAware = options.testIfSchemaAware();
        }
        if (this.config == null) {
            this.config = Configuration.newConfiguration();
            this.initializeConfiguration(this.config);
        }
        this.processor = new Processor(this.config);
        this.config.setProcessor(this.processor);
        try {
            int r;
            this.parseOptions(options);
            XQueryCompiler compiler = this.processor.newXQueryCompiler();
            compiler.setSchemaAware(schemaAware);
            if (this.updating) {
                compiler.setUpdatingEnabled(true);
            }
            if (this.config.getTraceListener() != null) {
                compiler.setCompileWithTracing(true);
            }
            if (this.moduleURIResolverClass != null) {
                Object mr = this.config.getInstance(this.moduleURIResolverClass, null);
                if (!(mr instanceof ModuleURIResolver)) {
                    this.badUsage(this.moduleURIResolverClass + " is not a ModuleURIResolver");
                }
                compiler.setModuleURIResolver((ModuleURIResolver)mr);
            }
            if (this.uriResolverClass != null) {
                this.config.setURIResolver(this.config.makeURIResolver(this.uriResolverClass));
            }
            this.config.displayLicenseMessage();
            if (schemaAware && !this.config.isLicensedFeature(4)) {
                if ("EE".equals(this.config.getEditionCode())) {
                    this.quit("Installed license does not allow schema-aware query", 2);
                } else {
                    this.quit("Schema-aware query requires Saxon Enterprise Edition", 2);
                }
            }
            if (this.explain) {
                this.config.setBooleanProperty(Feature.TRACE_OPTIMIZER_DECISIONS, true);
            }
            compiler.setStreaming(this.streaming);
            Source sourceInput = null;
            if (this.sourceFileName != null) {
                sourceInput = this.processSourceFile(this.sourceFileName, this.useURLs);
            }
            long startTime = System.nanoTime();
            if (this.showTime) {
                System.err.println("Analyzing query from " + this.queryFileName);
            }
            XQueryExecutable exp = null;
            try {
                exp = this.compileQuery(compiler, this.queryFileName, this.useURLs);
                if (this.showTime) {
                    long endTime = System.nanoTime();
                    System.err.println("Analysis time: " + (double)(endTime - startTime) / 1000000.0 + " milliseconds");
                    startTime = endTime;
                }
            } catch (SaxonApiException e) {
                if (e.getCause() instanceof XPathException) {
                    XPathException err = (XPathException)e.getCause();
                    int line = -1;
                    String module = null;
                    if (err.getLocator() != null) {
                        line = err.getLocator().getLineNumber();
                        module = err.getLocator().getSystemId();
                    }
                    if (err.hasBeenReported()) {
                        this.quit("Static error(s) in query", 2);
                    } else if (line == -1) {
                        System.err.println("Static error in query: " + err.getMessage());
                    } else {
                        System.err.println("Static error at line " + line + " of " + module + ':');
                        System.err.println(err.getMessage());
                    }
                    exp = null;
                    if (this.allowExit) {
                        System.exit(2);
                    }
                    throw new RuntimeException(err);
                }
                this.quit(e.getMessage(), 2);
            }
            if (this.explain && exp != null) {
                Serializer out = this.explainOutputFileName == null || this.explainOutputFileName.equals("") ? this.processor.newSerializer(System.err) : this.processor.newSerializer(new File(this.explainOutputFileName));
                out.setOutputProperty(Serializer.Property.METHOD, "xml");
                out.setOutputProperty(Serializer.Property.INDENT, "yes");
                out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
                if (this.processor.getUnderlyingConfiguration().isLicensedFeature(8)) {
                    out.setOutputProperty(Serializer.Property.SAXON_INDENT_SPACES, "2");
                }
                exp.explain(out);
            }
            exp.getUnderlyingCompiledQuery().setAllowDocumentProjection(this.projection);
            XQueryEvaluator evaluator = exp.load();
            evaluator.setTraceFunctionDestination(this.traceDestination);
            if (options.getOptionValue("now") != null) {
                String now = options.getOptionValue("now");
                ConversionResult dt = DateTimeValue.makeDateTimeValue(now, this.config.getConversionRules());
                if (dt instanceof DateTimeValue) {
                    evaluator.getUnderlyingQueryContext().setCurrentDateTime((DateTimeValue)dt);
                } else {
                    System.err.println("Invalid dateTime: " + now + " (ignored)");
                }
            }
            if (this.uriResolverClass != null) {
                evaluator.setURIResolver(this.config.makeURIResolver(this.uriResolverClass));
            }
            this.processSource(sourceInput, exp, evaluator);
            options.setParams(this.processor, evaluator::setExternalVariable);
            startTime = System.nanoTime();
            long totalTime = 0L;
            for (r = 0; r < this.repeat; ++r) {
                try {
                    OutputStream out;
                    if (this.outputFileName != null) {
                        File outputFile = new File(this.outputFileName);
                        if (outputFile.isDirectory()) {
                            this.quit("Output is a directory", 2);
                        }
                        Query.createFileIfNecessary(outputFile);
                        out = new FileOutputStream(outputFile);
                    } else {
                        out = System.out;
                    }
                    Serializer serializer = this.processor.newSerializer(out);
                    try {
                        options.setSerializationProperties(serializer);
                    } catch (IllegalArgumentException e) {
                        this.quit(e.getMessage(), 2);
                    }
                    if (this.updating && exp.isUpdateQuery()) {
                        serializer.setOutputProperties(exp.getUnderlyingCompiledQuery().getExecutable().getPrimarySerializationProperties().getProperties());
                        this.runUpdate(exp, evaluator, serializer);
                    } else {
                        this.runQuery(exp, evaluator, sourceInput, serializer);
                    }
                } catch (SaxonApiException err) {
                    if (err.getCause() instanceof XPathException && ((XPathException)err.getCause()).hasBeenReported()) {
                        String category = ((XPathException)err.getCause()).isTypeError() ? "type" : "dynamic";
                        this.quit("Query failed with " + category + " error: " + err.getCause().getMessage(), 2);
                    }
                    throw err;
                }
                if (!this.showTime) continue;
                long endTime = System.nanoTime();
                if (r >= 3) {
                    totalTime += endTime - startTime;
                }
                if (this.repeat < 100) {
                    System.err.println("Execution time: " + CommandLineOptions.showExecutionTimeNano(endTime - startTime));
                    CommandLineOptions.showMemoryUsed();
                    Instrumentation.report();
                } else if (totalTime > 1000000000000L) break;
                startTime = endTime;
            }
            if (this.repeat > 3) {
                System.err.println("Average execution time: " + CommandLineOptions.showExecutionTimeNano(totalTime / (long)(r - 3)));
            }
            if (options.getOptionValue("TB") != null) {
                this.config.createByteCodeReport(options.getOptionValue("TB"));
            }
        } catch (TerminationException err) {
            this.quit(err.getMessage(), 1);
        } catch (SchemaException err) {
            this.quit("Schema processing failed: " + err.getMessage(), 2);
        } catch (SaxonApiException | LicenseException | XPathException err) {
            this.quit("Query processing failed: " + err.getMessage(), 2);
        } catch (TransformerFactoryConfigurationError err) {
            err.printStackTrace();
            this.quit("Query processing failed", 2);
        } catch (Exception err2) {
            err2.printStackTrace();
            this.quit("Fatal error during query: " + err2.getClass().getName() + ": " + (err2.getMessage() == null ? " (no message)" : err2.getMessage()), 2);
        }
    }

    protected void initializeConfiguration(Configuration config) {
    }

    protected void parseOptions(CommandLineOptions options) throws TransformerException {
        String scmInput;
        String qv;
        options.applyToConfiguration(this.processor);
        this.allowExit = !"off".equals(options.getOptionValue("quit"));
        this.backup = "on".equals(options.getOptionValue("backup"));
        this.explainOutputFileName = options.getOptionValue("explain");
        this.explain = this.explainOutputFileName != null;
        this.moduleURIResolverClass = options.getOptionValue("mr");
        this.outputFileName = options.getOptionValue("o");
        this.streaming = "on".equals(options.getOptionValue("stream"));
        String value = options.getOptionValue("p");
        if ("on".equals(value)) {
            this.config.setParameterizedURIResolver();
            this.useURLs = true;
        }
        this.projection = "on".equals(options.getOptionValue("projection"));
        value = options.getOptionValue("q");
        if (value != null) {
            this.queryFileName = value;
        }
        if ((value = options.getOptionValue("qs")) != null) {
            this.queryFileName = "{" + value + "}";
        }
        if ((qv = options.getOptionValue("qversion")) != null && !"3.1".equals(qv)) {
            System.err.println("-qversion ignored: 3.1 is assumed");
        }
        if ((value = options.getOptionValue("repeat")) != null) {
            this.repeat = Integer.parseInt(value);
        }
        this.sourceFileName = options.getOptionValue("s");
        value = options.getOptionValue("t");
        if ("on".equals(value)) {
            System.err.println(this.config.getProductTitle());
            System.err.println(Version.platform.getPlatformVersion());
            this.config.setTiming(true);
            this.showTime = true;
        }
        if ((value = options.getOptionValue("traceout")) != null) {
            switch (value) {
                case "#err": {
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
                    try {
                        this.traceDestination = new StandardLogger(new File(value));
                        this.closeTraceDestination = true;
                        break;
                    } catch (FileNotFoundException e) {
                        this.badUsage("Trace output file " + value + " cannot be created");
                    }
                }
            }
        }
        if ((value = options.getOptionValue("T")) != null) {
            if ("".equals(value)) {
                this.makeXQueryTraceListener(options);
            } else {
                this.config.setTraceListenerClass(value);
            }
            this.config.setLineNumbering(true);
        }
        if ((value = options.getOptionValue("Tout")) != null) {
            this.config.setTraceListenerOutputFile(value);
            if (options.getOptionValue("T") == null) {
                this.makeXQueryTraceListener(options);
            }
        }
        if ((value = options.getOptionValue("TB")) != null) {
            this.config.setBooleanProperty(Feature.MONITOR_HOT_SPOT_BYTE_CODE, true);
        }
        if ((value = options.getOptionValue("TP")) != null) {
            TimingTraceListener listener = new TimingTraceListener();
            this.config.setTraceListener(listener);
            this.config.setLineNumbering(true);
            this.config.getDefaultStaticQueryContext().setCodeInjector(new TimingCodeInjector());
            if (!value.isEmpty()) {
                try {
                    listener.setOutputDestination(new StandardLogger(new File(value)));
                } catch (FileNotFoundException e) {
                    this.badUsage("Trace output file " + value + " cannot be created");
                }
            }
        }
        if ((value = options.getOptionValue("u")) != null) {
            this.useURLs = "on".equals(value);
        }
        if ((value = options.getOptionValue("update")) != null) {
            if (!"off".equals(value)) {
                this.updating = true;
            }
            this.writeback = !"discard".equals(value);
        }
        this.wrap = "on".equals(options.getOptionValue("wrap"));
        value = options.getOptionValue("x");
        if (value != null) {
            this.config.setSourceParserClass(value);
        }
        String additionalSchemas = options.getOptionValue("xsd");
        value = options.getOptionValue("?");
        if (value != null) {
            this.badUsage("");
        }
        if (options.getOptionValue("xsiloc") != null && options.getOptionValue("val") == null) {
            System.err.println("-xsiloc is ignored when -val is absent");
        }
        this.applyLocalOptions(options, this.config);
        List<String> positional = options.getPositionalOptions();
        int currentPositionalOption = 0;
        if (this.queryFileName == null) {
            if (positional.size() == currentPositionalOption) {
                this.badUsage("No query file name");
            }
            this.queryFileName = positional.get(currentPositionalOption++);
        }
        if (currentPositionalOption < positional.size()) {
            this.badUsage("Unrecognized option: " + positional.get(currentPositionalOption));
        }
        if ((scmInput = options.getOptionValue("scmin")) != null) {
            this.config.importComponents(new StreamSource(scmInput));
        }
        if (additionalSchemas != null) {
            CommandLineOptions.loadAdditionalSchemas(this.config, additionalSchemas);
        }
    }

    private void makeXQueryTraceListener(CommandLineOptions options) {
        XQueryTraceListener listener = new XQueryTraceListener();
        String value = options.getOptionValue("Tout");
        if (value != null) {
            try {
                listener.setOutputDestination(new StandardLogger(new PrintStream(value)));
            } catch (FileNotFoundException e) {
                this.badUsage("Cannot write to " + value);
            }
        }
        if ((value = options.getOptionValue("Tlevel")) != null) {
            switch (value) {
                case "none": {
                    listener.setLevelOfDetail(0);
                    break;
                }
                case "low": {
                    listener.setLevelOfDetail(1);
                    break;
                }
                case "normal": {
                    listener.setLevelOfDetail(2);
                    break;
                }
                case "high": {
                    listener.setLevelOfDetail(3);
                }
            }
        }
        this.config.setTraceListener(listener);
    }

    protected void applyLocalOptions(CommandLineOptions options, Configuration config) {
    }

    protected Source processSourceFile(String sourceFileName, boolean useURLs) throws TransformerException {
        Source sourceInput;
        if (useURLs || CommandLineOptions.isImplicitURI(sourceFileName)) {
            sourceInput = this.config.getURIResolver().resolve(sourceFileName, null);
            if (sourceInput == null) {
                sourceInput = this.config.getSystemURIResolver().resolve(sourceFileName, null);
            }
        } else if (sourceFileName.equals("-")) {
            String sysId = new File(System.getProperty("user.dir")).toURI().toASCIIString();
            sourceInput = new StreamSource(System.in, sysId);
        } else {
            File sourceFile = new File(sourceFileName);
            if (!sourceFile.exists()) {
                this.quit("Source file " + sourceFile + " does not exist", 2);
            }
            if (Version.platform.isJava()) {
                InputSource eis = new InputSource(sourceFile.toURI().toString());
                sourceInput = new SAXSource(eis);
            } else {
                sourceInput = new StreamSource(sourceFile.toURI().toString());
            }
        }
        return sourceInput;
    }

    protected XQueryExecutable compileQuery(XQueryCompiler compiler, String queryFileName, boolean useURLs) throws SaxonApiException, IOException {
        XQueryExecutable exp;
        if (queryFileName.equals("-")) {
            InputStreamReader queryReader = new InputStreamReader(System.in);
            compiler.setBaseURI(new File(System.getProperty("user.dir")).toURI());
            exp = compiler.compile(queryReader);
        } else if (queryFileName.startsWith("{") && queryFileName.endsWith("}")) {
            String q = queryFileName.substring(1, queryFileName.length() - 1);
            compiler.setBaseURI(new File(System.getProperty("user.dir")).toURI());
            exp = compiler.compile(q);
        } else {
            if (useURLs || CommandLineOptions.isImplicitURI(queryFileName)) {
                StreamSource[] sources;
                ModuleURIResolver resolver = compiler.getModuleURIResolver();
                boolean isStandardResolver = false;
                if (resolver == null) {
                    resolver = this.getConfiguration().getStandardModuleURIResolver();
                    isStandardResolver = true;
                }
                while (true) {
                    String[] locations = new String[]{queryFileName};
                    try {
                        sources = resolver.resolve(null, null, locations);
                    } catch (Exception e) {
                        if (e instanceof XPathException) {
                            throw new SaxonApiException(e);
                        }
                        XPathException xe = new XPathException("Exception in ModuleURIResolver: ", e);
                        xe.setErrorCode("XQST0059");
                        throw new SaxonApiException(xe);
                    }
                    if (sources != null) break;
                    if (isStandardResolver) {
                        this.quit("System problem: standard ModuleURIResolver returned null", 4);
                        continue;
                    }
                    resolver = this.getConfiguration().getStandardModuleURIResolver();
                    isStandardResolver = true;
                }
                if (sources.length != 1 || !(sources[0] instanceof StreamSource)) {
                    this.quit("Module URI Resolver must return a single StreamSource", 2);
                }
                try {
                    String queryText = QueryReader.readSourceQuery(sources[0], this.config.getValidCharacterChecker());
                    exp = compiler.compile(queryText);
                } catch (XPathException e) {
                    throw new SaxonApiException(e);
                }
            }
            try (FileInputStream queryStream = new FileInputStream(queryFileName);){
                compiler.setBaseURI(new File(queryFileName).toURI());
                exp = compiler.compile(queryStream);
            }
        }
        return exp;
    }

    protected void explain(XQueryExpression exp) throws FileNotFoundException, XPathException {
        OutputStream explainOutput = this.explainOutputFileName == null || "".equals(this.explainOutputFileName) ? System.err : new FileOutputStream(new File(this.explainOutputFileName));
        SerializationProperties props = ExpressionPresenter.makeDefaultProperties(this.config);
        Receiver diag = this.config.getSerializerFactory().getReceiver(new StreamResult(explainOutput), props);
        ExpressionPresenter expressionPresenter = new ExpressionPresenter(this.config, diag);
        exp.explain(expressionPresenter);
    }

    protected void processSource(Source sourceInput, XQueryExecutable exp, XQueryEvaluator evaluator) throws SaxonApiException {
        if (sourceInput != null && !this.streaming) {
            DocumentBuilder builder = this.processor.newDocumentBuilder();
            if (exp.isUpdateQuery()) {
                builder.setTreeModel(TreeModel.LINKED_TREE);
            }
            if (this.showTime) {
                System.err.println("Processing " + sourceInput.getSystemId());
            }
            if (!exp.getUnderlyingCompiledQuery().usesContextItem()) {
                System.err.println("Source document ignored - query can be evaluated without reference to the context item");
                return;
            }
            if (this.projection) {
                builder.setDocumentProjectionQuery(exp);
                if (this.explain) {
                    exp.getUnderlyingCompiledQuery().explainPathMap();
                }
            }
            builder.setDTDValidation(this.getConfiguration().getBooleanProperty(Feature.DTD_VALIDATION));
            if (this.getConfiguration().getBooleanProperty(Feature.DTD_VALIDATION_RECOVERABLE)) {
                sourceInput = new AugmentedSource(sourceInput, this.getConfiguration().getParseOptions());
            }
            XdmNode doc = builder.build(sourceInput);
            evaluator.setContextItem(doc);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void runQuery(XQueryExecutable exp, XQueryEvaluator evaluator, Source input, Destination destination) throws SaxonApiException {
        block9: {
            try {
                if (this.wrap) {
                    try {
                        XQueryExpression e = exp.getUnderlyingCompiledQuery();
                        SequenceIterator results = e.iterator(evaluator.getUnderlyingQueryContext());
                        NodeInfo resultDoc = QueryResult.wrap(results, this.config);
                        XdmValue wrappedResultDoc = XdmValue.wrap(resultDoc);
                        this.processor.writeXdmValue(wrappedResultDoc, destination);
                        destination.closeAndNotify();
                        break block9;
                    } catch (XPathException e1) {
                        throw new SaxonApiException(e1);
                    }
                }
                if (this.streaming) {
                    evaluator.runStreamed(input, destination);
                } else {
                    evaluator.run(destination);
                }
            } finally {
                if (this.closeTraceDestination && this.traceDestination != null) {
                    this.traceDestination.close();
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void runUpdate(XQueryExecutable exp, XQueryEvaluator evaluator, Serializer serializer) throws SaxonApiException {
        block13: {
            try {
                if (serializer.getOutputProperty(Serializer.Property.METHOD) == null) {
                    serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
                }
                if (this.writeback) {
                    ArrayList errors = new ArrayList(3);
                    UpdateAgent agent = (node, controller) -> {
                        try {
                            DocumentPool pool = controller.getDocumentPool();
                            String documentURI = pool.getDocumentURI(node);
                            if (documentURI != null) {
                                Query.rewriteToDisk(node, serializer, this.backup, this.showTime ? System.err : null);
                            } else if (this.showTime) {
                                System.err.println("Updated document discarded because it was not read using doc()");
                            }
                        } catch (SaxonApiException err) {
                            System.err.println(err.getMessage());
                            errors.add(err);
                        }
                    };
                    evaluator.run();
                    try {
                        exp.getUnderlyingCompiledQuery().runUpdate(evaluator.getUnderlyingQueryContext(), agent);
                    } catch (XPathException e) {
                        throw new SaxonApiException(e);
                    }
                    if (!errors.isEmpty()) {
                        throw (SaxonApiException)errors.get(0);
                    }
                    break block13;
                }
                try {
                    if (evaluator.getContextItem() != null) {
                        Set<MutableNodeInfo> affectedDocuments = exp.getUnderlyingCompiledQuery().runUpdate(evaluator.getUnderlyingQueryContext());
                        Item initial = evaluator.getContextItem().getUnderlyingValue().head();
                        if (initial instanceof NodeInfo && affectedDocuments.contains(initial)) {
                            this.processor.writeXdmValue(evaluator.getContextItem(), serializer);
                        }
                    }
                } catch (XPathException e) {
                    throw new SaxonApiException(e);
                }
            } finally {
                if (this.closeTraceDestination && this.traceDestination != null) {
                    this.traceDestination.close();
                }
            }
        }
    }

    private static void rewriteToDisk(NodeInfo doc, Serializer serializer, boolean backup, PrintStream log) throws SaxonApiException {
        URI u;
        switch (doc.getNodeKind()) {
            case 9: {
                break;
            }
            case 1: {
                NodeInfo parent = doc.getParent();
                if (parent == null || parent.getNodeKind() == 9) break;
                throw new SaxonApiException("Cannot rewrite an element node unless it is top-level");
            }
            default: {
                throw new SaxonApiException("Node to be rewritten must be a document or element node");
            }
        }
        String uri = doc.getSystemId();
        if (uri == null || uri.isEmpty()) {
            throw new SaxonApiException("Cannot rewrite a document with no known URI");
        }
        try {
            u = new URI(uri);
        } catch (URISyntaxException e) {
            throw new SaxonApiException("SystemId of updated document is not a valid URI: " + uri);
        }
        File existingFile = new File(u);
        File dir = existingFile.getParentFile();
        if (backup && existingFile.exists()) {
            boolean success;
            File backupFile = new File(dir, existingFile.getName() + ".bak");
            if (log != null) {
                log.println("Creating backup file " + backupFile);
            }
            if (!(success = existingFile.renameTo(backupFile))) {
                throw new SaxonApiException("Failed to create backup file of " + backupFile);
            }
        }
        if (!existingFile.exists()) {
            if (log != null) {
                log.println("Creating file " + existingFile);
            }
            try {
                existingFile.createNewFile();
            } catch (IOException e) {
                throw new SaxonApiException("Failed to create new file " + existingFile);
            }
        } else if (log != null) {
            log.println("Overwriting file " + existingFile);
        }
        serializer.setOutputFile(existingFile);
        serializer.getProcessor().writeXdmValue(XdmValue.wrap(doc), serializer);
    }

    protected void quit(String message, int code) {
        System.err.println(message);
        if (!this.allowExit) {
            throw new RuntimeException(message);
        }
        System.exit(code);
    }

    protected void badUsage(String message) {
        if (!"".equals(message)) {
            System.err.println(message);
        }
        if (!this.showTime) {
            System.err.println(this.config.getProductTitle());
        }
        System.err.println("Usage: see http://www.saxonica.com/documentation/index.html#!using-xquery/commandline");
        System.err.println("Format: " + CommandLineOptions.getCommandName(this) + " options params");
        CommandLineOptions options = new CommandLineOptions();
        this.setPermittedOptions(options);
        System.err.println("Options available:" + options.displayPermittedOptions());
        System.err.println("Use -XYZ:? for details of option XYZ or --? to list configuration features");
        System.err.println("Params: ");
        System.err.println("  param=value           Set query string parameter");
        System.err.println("  +param=filename       Set query document parameter");
        System.err.println("  ?param=expression     Set query parameter using XPath");
        System.err.println("  !param=value          Set serialization parameter");
        if (!this.allowExit) {
            throw new RuntimeException(message);
        }
        System.exit("".equals(message) ? 0 : 2);
    }

    public static void createFileIfNecessary(File file) throws IOException {
        if (!file.exists()) {
            File directory = file.getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }
            file.createNewFile();
        }
    }

    private String getCommandName() {
        String s = this.getClass().getName();
        if (s.equals("cli.Saxon.Cmd.DotNetQuery")) {
            s = "Query";
        }
        return s;
    }
}

