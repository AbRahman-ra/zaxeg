/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.io.File;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.functions.AccessorFn;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.Initializer;
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmEmptySequence;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlCatalogResolver;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.value.DayTimeDurationValue;
import net.sf.saxon.value.NumericValue;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class CommandLineOptions {
    public static final int TYPE_BOOLEAN = 1;
    public static final int TYPE_FILENAME = 2;
    public static final int TYPE_CLASSNAME = 3;
    public static final int TYPE_ENUMERATION = 4;
    public static final int TYPE_INTEGER = 5;
    public static final int TYPE_QNAME = 6;
    public static final int TYPE_FILENAME_LIST = 7;
    public static final int TYPE_DATETIME = 8;
    public static final int TYPE_STRING = 9;
    public static final int TYPE_INTEGER_PAIR = 10;
    public static final int VALUE_REQUIRED = 256;
    public static final int VALUE_PROHIBITED = 512;
    private HashMap<String, Integer> recognizedOptions = new HashMap();
    private HashMap<String, String> optionHelp = new HashMap();
    private Properties namedOptions = new Properties();
    private Properties configOptions = new Properties();
    private Map<String, Set<String>> permittedValues = new HashMap<String, Set<String>>();
    private Map<String, String> defaultValues = new HashMap<String, String>();
    private List<String> positionalOptions = new ArrayList<String>();
    private Properties paramValues = new Properties();
    private Properties paramExpressions = new Properties();
    private Properties paramFiles = new Properties();
    private Properties serializationParams = new Properties();
    private static DayTimeDurationValue milliSecond = new DayTimeDurationValue(1, 0, 0, 0, 0L, 1000);

    public void addRecognizedOption(String option, int optionProperties, String helpText) {
        this.recognizedOptions.put(option, optionProperties);
        this.optionHelp.put(option, helpText);
        if ((optionProperties & 0xFF) == 1) {
            this.setPermittedValues(option, new String[]{"on", "off"}, "on");
        }
    }

    public void setPermittedValues(String option, String[] values, String defaultValue) {
        HashSet<String> valueSet = new HashSet<String>(Arrays.asList(values));
        this.permittedValues.put(option, valueSet);
        if (defaultValue != null) {
            this.defaultValues.put(option, defaultValue);
        }
    }

    private static String displayPermittedValues(Set<String> permittedValues) {
        FastStringBuffer sb = new FastStringBuffer(20);
        for (String val : permittedValues) {
            if ("".equals(val)) {
                sb.append("\"\"");
            } else {
                sb.append(val);
            }
            sb.cat('|');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public void setActualOptions(String[] args) throws XPathException {
        for (String arg : args) {
            if ("-".equals(arg)) {
                this.positionalOptions.add(arg);
                continue;
            }
            if (arg.equals("--?")) {
                System.err.println("Configuration features:" + CommandLineOptions.featureKeys());
                continue;
            }
            if (arg.charAt(0) == '-') {
                String msg;
                int prop;
                String option;
                int colon;
                String value = "";
                if (arg.length() > 5 && arg.charAt(1) == '-') {
                    colon = arg.indexOf(58);
                    if (colon > 0 && colon < arg.length() - 1) {
                        option = arg.substring(2, colon);
                        value = arg.substring(colon + 1);
                        this.configOptions.setProperty(option, value);
                        continue;
                    }
                    if (colon > 0 && colon == arg.length() - 1) {
                        option = arg.substring(2, colon);
                        this.configOptions.setProperty(option, "");
                        continue;
                    }
                    option = arg.substring(2);
                    this.configOptions.setProperty(option, "true");
                    continue;
                }
                colon = arg.indexOf(58);
                if (colon > 0 && colon < arg.length() - 1) {
                    option = arg.substring(1, colon);
                    value = arg.substring(colon + 1);
                } else {
                    option = arg.substring(1);
                }
                if (this.recognizedOptions.get(option) == null) {
                    throw new XPathException("Command line option -" + option + " is not recognized. Options available: " + this.displayPermittedOptions());
                }
                if (this.namedOptions.getProperty(option) != null) {
                    throw new XPathException("Command line option -" + option + " appears more than once");
                }
                if ("?".equals(value)) {
                    this.displayOptionHelp(option);
                    throw new XPathException("No processing requested");
                }
                if ("".equals(value)) {
                    prop = this.recognizedOptions.get(option);
                    if ((prop & 0x100) != 0) {
                        msg = "Command line option -" + option + " requires a value";
                        if (this.permittedValues.get(option) != null) {
                            msg = msg + ": permitted values are " + CommandLineOptions.displayPermittedValues(this.permittedValues.get(option));
                        }
                        throw new XPathException(msg);
                    }
                    String defaultValue = this.defaultValues.get(option);
                    if (defaultValue != null) {
                        value = defaultValue;
                    }
                } else {
                    prop = this.recognizedOptions.get(option);
                    if ((prop & 0x200) != 0) {
                        msg = "Command line option -" + option + " does not expect a value";
                        throw new XPathException(msg);
                    }
                }
                Set<String> permitted = this.permittedValues.get(option);
                if (permitted != null && !permitted.contains(value)) {
                    throw new XPathException("Bad option value " + arg + ": permitted values are " + CommandLineOptions.displayPermittedValues(permitted));
                }
                this.namedOptions.setProperty(option, value);
                continue;
            }
            int eq = arg.indexOf(61);
            if (eq >= 1) {
                char ch;
                String keyword = arg.substring(0, eq);
                String value = "";
                if (eq < arg.length() - 1) {
                    value = arg.substring(eq + 1);
                }
                if ((ch = arg.charAt(0)) == '!' && eq >= 2) {
                    this.serializationParams.setProperty(keyword.substring(1), value);
                    continue;
                }
                if (ch == '?' && eq >= 2) {
                    this.paramExpressions.setProperty(keyword.substring(1), value);
                    continue;
                }
                if (ch == '+' && eq >= 2) {
                    this.paramFiles.setProperty(keyword.substring(1), value);
                    continue;
                }
                this.paramValues.setProperty(keyword, value);
                continue;
            }
            this.positionalOptions.add(arg);
        }
    }

    public boolean definesParameterValues() {
        return !this.serializationParams.isEmpty() || !this.paramExpressions.isEmpty() || !this.paramFiles.isEmpty() || !this.paramValues.isEmpty();
    }

    public boolean testIfSchemaAware() {
        return this.getOptionValue("sa") != null || this.getOptionValue("outval") != null || this.getOptionValue("val") != null || this.getOptionValue("vlax") != null || this.getOptionValue("xsd") != null || this.getOptionValue("xsdversion") != null;
    }

    public void applyToConfiguration(Processor processor) throws TransformerException {
        Configuration config = processor.getUnderlyingConfiguration();
        Enumeration<?> e = this.configOptions.propertyNames();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            String[] value = this.configOptions.getProperty(name);
            String fullName = "http://saxon.sf.net/feature/" + name;
            if (!name.startsWith("parserFeature?") && !name.startsWith("parserProperty?")) {
                Feature<?> f = Feature.byName(fullName);
                if (f == null) {
                    throw new XPathException("Unknown configuration feature " + name);
                }
                if (f.type == Boolean.class) {
                    Configuration.requireBoolean(name, value);
                } else if (f.type == Integer.class) {
                    Integer.valueOf((String)value);
                } else if (f.type != String.class) {
                    throw new XPathException("Property --" + name + " cannot be supplied as a string");
                }
            }
            try {
                processor.getUnderlyingConfiguration().setConfigurationProperty(fullName, value);
            } catch (IllegalArgumentException err) {
                throw new XPathException("Incorrect value for --" + name + ": " + err.getMessage());
            }
        }
        String value = this.getOptionValue("catalog");
        if (value != null) {
            if (this.getOptionValue("r") != null) {
                throw new XPathException("Cannot use -catalog and -r together");
            }
            if (this.getOptionValue("x") != null) {
                throw new XPathException("Cannot use -catalog and -x together");
            }
            if (this.getOptionValue("y") != null) {
                throw new XPathException("Cannot use -catalog and -y together");
            }
            CharSequence sb = new StringBuilder();
            if (this.getOptionValue("u") != null || CommandLineOptions.isImplicitURI(value)) {
                for (String s : value.split(";")) {
                    Source sourceInput = null;
                    try {
                        sourceInput = config.getURIResolver().resolve(s, null);
                    } catch (TransformerException transformerException) {
                        // empty catch block
                    }
                    if (sourceInput == null) {
                        sourceInput = config.getSystemURIResolver().resolve(s, null);
                    }
                    ((StringBuilder)sb).append(sourceInput.getSystemId()).append(';');
                }
            } else {
                for (String s : value.split(";")) {
                    File catalogFile = new File(s);
                    if (!catalogFile.exists()) {
                        throw new XPathException("Catalog file not found: " + s);
                    }
                    ((StringBuilder)sb).append(catalogFile.toURI().toASCIIString()).append(';');
                }
            }
            value = ((StringBuilder)sb).toString();
            try {
                config.getClass("org.apache.xml.resolver.CatalogManager", false, null);
                XmlCatalogResolver.setCatalog(value, config, this.getOptionValue("t") != null);
            } catch (XPathException err) {
                throw new XPathException("Failed to load Apache catalog resolver library", err);
            }
        }
        if ((value = this.getOptionValue("dtd")) != null) {
            switch (value) {
                case "on": {
                    config.setBooleanProperty(Feature.DTD_VALIDATION, true);
                    config.getParseOptions().setDTDValidationMode(1);
                    break;
                }
                case "off": {
                    config.setBooleanProperty(Feature.DTD_VALIDATION, false);
                    config.getParseOptions().setDTDValidationMode(4);
                    break;
                }
                case "recover": {
                    config.setBooleanProperty(Feature.DTD_VALIDATION, true);
                    config.setBooleanProperty(Feature.DTD_VALIDATION_RECOVERABLE, true);
                    config.getParseOptions().setDTDValidationMode(2);
                }
            }
        }
        if ((value = this.getOptionValue("ea")) != null) {
            boolean on = Configuration.requireBoolean("ea", value);
            config.getDefaultXsltCompilerInfo().setAssertionsEnabled(on);
        }
        if ((value = this.getOptionValue("expand")) != null) {
            boolean on = Configuration.requireBoolean("expand", value);
            config.getParseOptions().setExpandAttributeDefaults(on);
        }
        if ((value = this.getOptionValue("ext")) != null) {
            boolean on = Configuration.requireBoolean("ext", value);
            config.setBooleanProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, on);
        }
        if ((value = this.getOptionValue("l")) != null) {
            boolean on = Configuration.requireBoolean("l", value);
            config.setBooleanProperty(Feature.LINE_NUMBERING, on);
        }
        if ((value = this.getOptionValue("m")) != null) {
            config.setConfigurationProperty(Feature.MESSAGE_EMITTER_CLASS, value);
        }
        if ((value = this.getOptionValue("opt")) != null) {
            config.setConfigurationProperty(Feature.OPTIMIZATION_LEVEL, value);
        }
        if ((value = this.getOptionValue("or")) != null) {
            Object resolver = config.getInstance(value, null);
            if (resolver instanceof OutputURIResolver) {
                config.setConfigurationProperty(Feature.OUTPUT_URI_RESOLVER, (OutputURIResolver)resolver);
            } else {
                throw new XPathException("Class " + value + " is not an OutputURIResolver");
            }
        }
        if ((value = this.getOptionValue("outval")) != null) {
            Boolean isRecover = "recover".equals(value);
            config.setConfigurationProperty(Feature.VALIDATION_WARNINGS, isRecover);
            config.setConfigurationProperty(Feature.VALIDATION_COMMENTS, isRecover);
        }
        if ((value = this.getOptionValue("r")) != null) {
            config.setURIResolver(config.makeURIResolver(value));
        }
        if ((value = this.getOptionValue("strip")) != null) {
            config.setConfigurationProperty(Feature.STRIP_WHITESPACE, value);
        }
        if ((value = this.getOptionValue("T")) != null) {
            config.setCompileWithTracing(true);
        }
        if ((value = this.getOptionValue("TJ")) != null) {
            boolean on = Configuration.requireBoolean("TJ", value);
            config.setBooleanProperty(Feature.TRACE_EXTERNAL_FUNCTIONS, on);
        }
        if ((value = this.getOptionValue("tree")) != null) {
            switch (value) {
                case "linked": {
                    config.setTreeModel(0);
                    break;
                }
                case "tiny": {
                    config.setTreeModel(1);
                    break;
                }
                case "tinyc": {
                    config.setTreeModel(2);
                }
            }
        }
        if ((value = this.getOptionValue("val")) != null) {
            if ("strict".equals(value)) {
                processor.setConfigurationProperty(Feature.SCHEMA_VALIDATION, 1);
            } else if ("lax".equals(value)) {
                processor.setConfigurationProperty(Feature.SCHEMA_VALIDATION, 2);
            }
        }
        if ((value = this.getOptionValue("x")) != null) {
            processor.setConfigurationProperty(Feature.SOURCE_PARSER_CLASS, value);
        }
        if ((value = this.getOptionValue("xi")) != null) {
            boolean on = Configuration.requireBoolean("xi", value);
            processor.setConfigurationProperty(Feature.XINCLUDE, on);
        }
        if ((value = this.getOptionValue("xmlversion")) != null) {
            processor.setConfigurationProperty(Feature.XML_VERSION, value);
        }
        if ((value = this.getOptionValue("xsdversion")) != null) {
            processor.setConfigurationProperty(Feature.XSD_VERSION, value);
        }
        if ((value = this.getOptionValue("xsiloc")) != null) {
            boolean on = Configuration.requireBoolean("xsiloc", value);
            processor.setConfigurationProperty(Feature.USE_XSI_SCHEMA_LOCATION, on);
        }
        if ((value = this.getOptionValue("y")) != null) {
            processor.setConfigurationProperty(Feature.STYLE_PARSER_CLASS, value);
        }
        if ((value = this.getOptionValue("init")) != null) {
            Initializer initializer = (Initializer)config.getInstance(value, null);
            initializer.initialize(config);
        }
    }

    public String displayPermittedOptions() {
        String[] options = new String[this.recognizedOptions.size()];
        options = new ArrayList<String>(this.recognizedOptions.keySet()).toArray(options);
        Arrays.sort(options, Collator.getInstance());
        FastStringBuffer sb = new FastStringBuffer(100);
        for (String opt : options) {
            sb.append(" -");
            sb.append(opt);
        }
        sb.append(" --?");
        return sb.toString();
    }

    private void displayOptionHelp(String option) {
        System.err.println("Help for -" + option + " option");
        int prop = this.recognizedOptions.get(option);
        if ((prop & 0x200) == 0) {
            switch (prop & 0xFF) {
                case 1: {
                    System.err.println("Value: on|off");
                    break;
                }
                case 5: {
                    System.err.println("Value: integer");
                    break;
                }
                case 2: {
                    System.err.println("Value: file name");
                    break;
                }
                case 7: {
                    System.err.println("Value: list of file names, semicolon-separated");
                    break;
                }
                case 3: {
                    System.err.println("Value: Java fully-qualified class name");
                    break;
                }
                case 6: {
                    System.err.println("Value: QName in Clark notation ({uri}local)");
                    break;
                }
                case 9: {
                    System.err.println("Value: string");
                    break;
                }
                case 10: {
                    System.err.println("Value: int,int");
                    break;
                }
                case 4: {
                    String message = "Value: one of ";
                    message = message + CommandLineOptions.displayPermittedValues(this.permittedValues.get(option));
                    System.err.println(message);
                    break;
                }
            }
        }
        System.err.println("Meaning: " + this.optionHelp.get(option));
    }

    public String getOptionValue(String option) {
        return this.namedOptions.getProperty(option);
    }

    public List<String> getPositionalOptions() {
        return this.positionalOptions;
    }

    public void setParams(Processor processor, ParamSetter paramSetter) throws SaxonApiException {
        String value;
        String name;
        Enumeration<?> e = this.paramValues.propertyNames();
        while (e.hasMoreElements()) {
            name = (String)e.nextElement();
            value = this.paramValues.getProperty(name);
            paramSetter.setParam(QName.fromClarkName(name), new XdmAtomicValue(value, ItemType.UNTYPED_ATOMIC));
        }
        this.applyFileParameters(processor, paramSetter);
        e = this.paramExpressions.propertyNames();
        while (e.hasMoreElements()) {
            name = (String)e.nextElement();
            value = this.paramExpressions.getProperty(name);
            XPathCompiler xpc = processor.newXPathCompiler();
            XPathExecutable xpe = xpc.compile(value);
            XdmValue val = xpe.load().evaluate();
            paramSetter.setParam(QName.fromClarkName(name), val);
        }
    }

    private void applyFileParameters(Processor processor, ParamSetter paramSetter) throws SaxonApiException {
        boolean useURLs = "on".equals(this.getOptionValue("u"));
        Enumeration<?> e = this.paramFiles.propertyNames();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            String value = this.paramFiles.getProperty(name);
            ArrayList<Source> sourceList = new ArrayList<Source>();
            CommandLineOptions.loadDocuments(value, useURLs, processor, true, sourceList);
            if (!sourceList.isEmpty()) {
                ArrayList<XdmNode> nodeList = new ArrayList<XdmNode>(sourceList.size());
                DocumentBuilder builder = processor.newDocumentBuilder();
                for (Source s : sourceList) {
                    nodeList.add(builder.build(s));
                }
                XdmValue nodes = new XdmValue(nodeList);
                paramSetter.setParam(QName.fromClarkName(name), nodes);
                continue;
            }
            paramSetter.setParam(QName.fromClarkName(name), XdmEmptySequence.getInstance());
        }
    }

    public void setSerializationProperties(Serializer serializer) {
        Enumeration<?> e = this.serializationParams.propertyNames();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            String value = this.serializationParams.getProperty(name);
            if (name.startsWith("saxon:")) {
                name = "{http://saxon.sf.net/}" + name.substring(6);
            }
            serializer.setOutputProperty(QName.fromClarkName(name), value);
        }
    }

    public void applyStaticParams(XsltCompiler compiler) throws SaxonApiException {
        String value;
        String name;
        Processor processor = compiler.getProcessor();
        Enumeration<?> e = this.paramValues.propertyNames();
        while (e.hasMoreElements()) {
            name = (String)e.nextElement();
            value = this.paramValues.getProperty(name);
            compiler.setParameter(QName.fromClarkName(name), new XdmAtomicValue(value, ItemType.UNTYPED_ATOMIC));
        }
        e = this.paramExpressions.propertyNames();
        while (e.hasMoreElements()) {
            name = (String)e.nextElement();
            value = this.paramExpressions.getProperty(name);
            XPathCompiler xpc = processor.newXPathCompiler();
            XPathExecutable xpe = xpc.compile(value);
            XdmValue val = xpe.load().evaluate();
            compiler.setParameter(QName.fromClarkName(name), val);
        }
    }

    public void applyFileParams(Processor processor, Xslt30Transformer transformer) throws SaxonApiException {
        if (!this.paramFiles.isEmpty()) {
            HashMap params = new HashMap();
            this.applyFileParameters(processor, params::put);
            transformer.setStylesheetParameters(params);
        }
    }

    public static boolean loadDocuments(String sourceFileName, boolean useURLs, Processor processor, boolean useSAXSource, List<Source> sources) throws SaxonApiException {
        Source sourceInput;
        Configuration config = processor.getUnderlyingConfiguration();
        if (useURLs || CommandLineOptions.isImplicitURI(sourceFileName)) {
            Source sourceInput2;
            try {
                sourceInput2 = config.getURIResolver().resolve(sourceFileName, null);
                if (sourceInput2 == null) {
                    sourceInput2 = config.getSystemURIResolver().resolve(sourceFileName, null);
                }
            } catch (TransformerException e) {
                throw new SaxonApiException(e);
            }
            sources.add(sourceInput2);
            return false;
        }
        if (sourceFileName.equals("-")) {
            Source sourceInput3;
            if (useSAXSource) {
                XMLReader parser = config.getSourceParser();
                sourceInput3 = new SAXSource(parser, new InputSource(System.in));
            } else {
                sourceInput3 = new StreamSource(System.in);
            }
            sources.add(sourceInput3);
            return false;
        }
        File sourceFile = new File(sourceFileName);
        if (!sourceFile.exists()) {
            throw new SaxonApiException("Source file " + sourceFile + " does not exist");
        }
        if (sourceFile.isDirectory()) {
            XMLReader parser = config.getSourceParser();
            String[] files = sourceFile.list();
            if (files != null) {
                for (String file1 : files) {
                    Source sourceInput4;
                    File file = new File(sourceFile, file1);
                    if (file.isDirectory() || file.isHidden()) continue;
                    if (useSAXSource) {
                        InputSource eis = new InputSource(file.toURI().toString());
                        sourceInput4 = new SAXSource(parser, eis);
                    } else {
                        sourceInput4 = new StreamSource(file.toURI().toString());
                    }
                    sources.add(sourceInput4);
                }
            }
            return true;
        }
        if (useSAXSource) {
            InputSource eis = new InputSource(sourceFile.toURI().toString());
            sourceInput = new SAXSource(config.getSourceParser(), eis);
        } else {
            sourceInput = new StreamSource(sourceFile.toURI().toString());
        }
        sources.add(sourceInput);
        return false;
    }

    public static boolean isImplicitURI(String name) {
        return name.startsWith("http:") || name.startsWith("https:") || name.startsWith("file:") || name.startsWith("classpath:");
    }

    public static void loadAdditionalSchemas(Configuration config, String additionalSchemas) throws SchemaException {
        StringTokenizer st = new StringTokenizer(additionalSchemas, File.pathSeparator);
        while (st.hasMoreTokens()) {
            String schema = st.nextToken();
            File schemaFile = new File(schema);
            if (!schemaFile.exists()) {
                throw new SchemaException("Schema document " + schema + " not found");
            }
            config.addSchemaSource(new StreamSource(schemaFile));
        }
    }

    public static String featureKeys() {
        int index = "http://saxon.sf.net/feature/".length();
        StringBuilder sb = new StringBuilder();
        Feature.getNames().forEachRemaining(s -> sb.append("\n  ").append(s.substring(index)));
        return sb.toString();
    }

    public static String showExecutionTimeNano(long nanosecs) {
        if ((double)nanosecs < 1.0E9) {
            return (double)nanosecs / 1000000.0 + "ms";
        }
        try {
            double millisecs = (double)nanosecs / 1000000.0;
            DayTimeDurationValue d = milliSecond.multiply(millisecs);
            long days = ((NumericValue)d.getComponent(AccessorFn.Component.DAY)).longValue();
            long hours = ((NumericValue)d.getComponent(AccessorFn.Component.HOURS)).longValue();
            long minutes = ((NumericValue)d.getComponent(AccessorFn.Component.MINUTES)).longValue();
            BigDecimal seconds = ((NumericValue)d.getComponent(AccessorFn.Component.SECONDS)).getDecimalValue();
            FastStringBuffer fsb = new FastStringBuffer(256);
            if (days > 0L) {
                fsb.append(days + "days ");
            }
            if (hours > 0L) {
                fsb.append(hours + "h ");
            }
            if (minutes > 0L) {
                fsb.append(minutes + "m ");
            }
            fsb.append(seconds + "s");
            return fsb + " (" + (double)nanosecs / 1000000.0 + "ms)";
        } catch (XPathException e) {
            return (double)nanosecs / 1000000.0 + "ms";
        }
    }

    public static String getCommandName(Object command) {
        String s = command.getClass().getName();
        if (s.startsWith("cli.Saxon.Cmd.DotNet")) {
            s = s.substring("cli.Saxon.Cmd.DotNet".length());
        }
        return s;
    }

    public static void showMemoryUsed() {
        long value = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.err.println("Memory used: " + value / 1000000L + "Mb");
    }

    public static interface ParamSetter {
        public void setParam(QName var1, XdmValue var2);
    }
}

