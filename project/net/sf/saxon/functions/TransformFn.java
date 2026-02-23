/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.Version;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.OptionsParameter;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.functions.Serialize;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.AugmentedSource;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.ResultDocumentResolver;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.RawDestination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XmlProcessingError;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltPackage;
import net.sf.saxon.serialize.CharacterMap;
import net.sf.saxon.serialize.CharacterMapIndex;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.StylesheetCache;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.wrapper.RebasedDocument;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DecimalValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;
import org.xml.sax.InputSource;

public class TransformFn
extends SystemFunction
implements Callable {
    private static String[] transformOptionNames30 = new String[]{"package-name", "package-version", "package-node", "package-location", "static-params", "global-context-item", "template-params", "tunnel-params", "initial-function", "function-params"};
    private static final String dummyBaseOutputUriScheme = "dummy";

    private boolean isTransformOptionName30(String string) {
        for (String s : transformOptionNames30) {
            if (!s.equals(string)) continue;
            return true;
        }
        return false;
    }

    public static OptionsParameter makeOptionsParameter() {
        OptionsParameter op = new OptionsParameter();
        op.addAllowedOption("xslt-version", SequenceType.SINGLE_DECIMAL);
        op.addAllowedOption("stylesheet-location", SequenceType.SINGLE_STRING);
        op.addAllowedOption("stylesheet-node", SequenceType.SINGLE_NODE);
        op.addAllowedOption("stylesheet-text", SequenceType.SINGLE_STRING);
        op.addAllowedOption("stylesheet-base-uri", SequenceType.SINGLE_STRING);
        op.addAllowedOption("base-output-uri", SequenceType.SINGLE_STRING);
        op.addAllowedOption("stylesheet-params", SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, 16384));
        op.addAllowedOption("source-node", SequenceType.SINGLE_NODE);
        op.addAllowedOption("source-location", SequenceType.SINGLE_STRING);
        op.addAllowedOption("initial-mode", SequenceType.SINGLE_QNAME);
        op.addAllowedOption("initial-match-selection", SequenceType.ANY_SEQUENCE);
        op.addAllowedOption("initial-template", SequenceType.SINGLE_QNAME);
        op.addAllowedOption("delivery-format", SequenceType.SINGLE_STRING);
        op.addAllowedOption("serialization-params", SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, 16384));
        op.addAllowedOption("vendor-options", SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, 16384));
        op.addAllowedOption("cache", SequenceType.SINGLE_BOOLEAN);
        op.addAllowedOption("package-name", SequenceType.SINGLE_STRING);
        op.addAllowedOption("package-version", SequenceType.SINGLE_STRING);
        op.addAllowedOption("package-node", SequenceType.SINGLE_NODE);
        op.addAllowedOption("package-location", SequenceType.SINGLE_STRING);
        op.addAllowedOption("static-params", SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, 16384));
        op.addAllowedOption("global-context-item", SequenceType.SINGLE_ITEM);
        op.addAllowedOption("template-params", SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, 16384));
        op.addAllowedOption("tunnel-params", SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, 16384));
        op.addAllowedOption("initial-function", SequenceType.SINGLE_QNAME);
        op.addAllowedOption("function-params", ArrayItemType.SINGLE_ARRAY);
        op.addAllowedOption("requested-properties", SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, 16384));
        op.addAllowedOption("post-process", SequenceType.makeSequenceType(new SpecificFunctionType(new SequenceType[]{SequenceType.SINGLE_STRING, SequenceType.ANY_SEQUENCE}, SequenceType.ANY_SEQUENCE), 16384));
        return op;
    }

    private void checkTransformOptions(Map<String, Sequence> options, XPathContext context, boolean isXslt30Processor) throws XPathException {
        if (options.isEmpty()) {
            throw new XPathException("No transformation options supplied", "FOXT0002");
        }
        for (String keyName : options.keySet()) {
            if (!this.isTransformOptionName30(keyName) || isXslt30Processor) continue;
            throw new XPathException("The transform option " + keyName + " is only available when using an XSLT 3.0 processor", "FOXT0002");
        }
    }

    private String checkStylesheetMutualExclusion(Map<String, Sequence> map) throws XPathException {
        return this.exactlyOneOf(map, "stylesheet-location", "stylesheet-node", "stylesheet-text");
    }

    private String checkStylesheetMutualExclusion30(Map<String, Sequence> map) throws XPathException {
        String styleOption = this.exactlyOneOf(map, "stylesheet-location", "stylesheet-node", "stylesheet-text", "package-name", "package-node", "package-location");
        if (styleOption.equals("package-location")) {
            throw new XPathException("The transform option " + styleOption + " is not implemented in Saxon", "FOXT0002");
        }
        return styleOption;
    }

    private String checkInvocationMutualExclusion(Map<String, Sequence> options) throws XPathException {
        return this.oneOf(options, "initial-mode", "initial-template");
    }

    private String oneOf(Map<String, Sequence> map, String ... keys) throws XPathException {
        String found = null;
        for (String s : keys) {
            if (map.get(s) == null) continue;
            if (found != null) {
                throw new XPathException("The following transform options are mutually exclusive: " + this.enumerate(keys), "FOXT0002");
            }
            found = s;
        }
        return found;
    }

    private String exactlyOneOf(Map<String, Sequence> map, String ... keys) throws XPathException {
        String found = this.oneOf(map, keys);
        if (found == null) {
            throw new XPathException("One of the following transform options must be present: " + this.enumerate(keys));
        }
        return found;
    }

    private String enumerate(String ... keys) {
        boolean first = true;
        FastStringBuffer buffer = new FastStringBuffer(256);
        for (String k : keys) {
            if (first) {
                first = false;
            } else {
                buffer.append(" | ");
            }
            buffer.append(k);
        }
        return buffer.toString();
    }

    private String checkInvocationMutualExclusion30(Map<String, Sequence> map) throws XPathException {
        return this.oneOf(map, "initial-mode", "initial-template", "initial-function");
    }

    private void unsuitable(String option, String value) throws XPathException {
        throw new XPathException("No XSLT processor is available with xsl:" + option + " = " + value, "FOXT0001");
    }

    private boolean asBoolean(AtomicValue value) throws XPathException {
        if (value instanceof BooleanValue) {
            return ((BooleanValue)value).getBooleanValue();
        }
        if (value instanceof StringValue) {
            String s = Whitespace.normalizeWhitespace(value.getStringValue()).toString();
            if (s.equals("yes") || s.equals("true") || s.equals("1")) {
                return true;
            }
            if (s.equals("no") || s.equals("false") || s.equals("0")) {
                return false;
            }
        }
        throw new XPathException("Unrecognized boolean value " + value, "FOXT0002");
    }

    private void setRequestedProperties(Map<String, Sequence> options, Processor processor) throws XPathException {
        Item option;
        MapItem requestedProps = (MapItem)options.get("requested-properties").head();
        AtomicIterator<? extends AtomicValue> optionIterator = requestedProps.keys();
        block32: while ((option = optionIterator.next()) != null) {
            String localName;
            StructuredQName optionName = ((QNameValue)((AtomicValue)option).head()).getStructuredQName();
            AtomicValue value = (AtomicValue)requestedProps.get((AtomicValue)option).head();
            if (!optionName.hasURI("http://www.w3.org/1999/XSL/Transform")) continue;
            switch (localName = optionName.getLocalPart()) {
                case "vendor-url": {
                    if (value.getStringValue().contains("saxonica.com") || value.getStringValue().equals("Saxonica")) break;
                    this.unsuitable("vendor-url", value.getStringValue());
                    break;
                }
                case "product-name": {
                    if (value.getStringValue().equals("SAXON")) break;
                    this.unsuitable("vendor-url", value.getStringValue());
                    break;
                }
                case "product-version": {
                    if (Version.getProductVersion().startsWith(value.getStringValue())) break;
                    this.unsuitable("product-version", value.getStringValue());
                    break;
                }
                case "is-schema-aware": {
                    boolean b = this.asBoolean(value);
                    if (b) {
                        if (processor.getUnderlyingConfiguration().isLicensedFeature(2)) {
                            processor.setConfigurationProperty(Feature.XSLT_SCHEMA_AWARE, true);
                            break;
                        }
                        this.unsuitable("is-schema-aware", value.getStringValue());
                        break;
                    }
                    if (!processor.getUnderlyingConfiguration().isLicensedFeature(2)) break;
                    this.unsuitable("is-schema-aware", value.getStringValue());
                    break;
                }
                case "supports-serialization": {
                    boolean b = this.asBoolean(value);
                    if (b) break;
                    this.unsuitable("supports-serialization", value.getStringValue());
                    break;
                }
                case "supports-backwards-compatibility": {
                    boolean b = this.asBoolean(value);
                    if (b) break;
                    this.unsuitable("supports-backwards-compatibility", value.getStringValue());
                    break;
                }
                case "supports-namespace-axis": {
                    boolean b = this.asBoolean(value);
                    if (b) break;
                    this.unsuitable("supports-namespace-axis", value.getStringValue());
                    break;
                }
                case "supports-streaming": {
                    boolean b = this.asBoolean(value);
                    if (b) {
                        if (processor.getUnderlyingConfiguration().isLicensedFeature(2)) break;
                        this.unsuitable("supports-streaming", value.getStringValue());
                        break;
                    }
                    if (!processor.getUnderlyingConfiguration().isLicensedFeature(2)) break;
                    processor.setConfigurationProperty(Feature.STREAMABILITY, "off");
                    break;
                }
                case "supports-dynamic-evaluation": {
                    boolean b = this.asBoolean(value);
                    if (b) break;
                    processor.setConfigurationProperty(Feature.DISABLE_XSL_EVALUATE, true);
                    break;
                }
                case "supports-higher-order-functions": {
                    boolean b = this.asBoolean(value);
                    if (b) break;
                    this.unsuitable("supports-higher-order-functions", value.getStringValue());
                    break;
                }
                case "xpath-version": {
                    String v = value.getStringValue();
                    try {
                        if (!(Double.parseDouble(v) > 3.1)) continue block32;
                        this.unsuitable("xpath-version", value.getStringValue());
                    } catch (NumberFormatException nfe) {
                        this.unsuitable("xpath-version", value.getStringValue());
                    }
                    break;
                }
                case "xsd-version": {
                    String v = value.getStringValue();
                    try {
                        if (!(Double.parseDouble(v) > 1.1)) continue block32;
                        this.unsuitable("xsd-version", value.getStringValue());
                    } catch (NumberFormatException nfe) {
                        this.unsuitable("xsd-version", value.getStringValue());
                    }
                    break;
                }
            }
        }
    }

    private void setStaticParams(Map<String, Sequence> options, XsltCompiler xsltCompiler, boolean allowTypedNodes) throws XPathException {
        Item param;
        MapItem staticParamsMap = (MapItem)options.get("static-params").head();
        AtomicIterator<? extends AtomicValue> paramIterator = staticParamsMap.keys();
        while ((param = paramIterator.next()) != null) {
            if (!(param instanceof QNameValue)) {
                throw new XPathException("Parameter names in static-params must be supplied as QNames", "FOXT0002");
            }
            QName paramName = new QName(((QNameValue)param).getStructuredQName());
            GroundedValue value = staticParamsMap.get((AtomicValue)param);
            if (!allowTypedNodes) {
                this.checkSequenceIsUntyped(value);
            }
            XdmValue paramVal = XdmValue.wrap(value);
            xsltCompiler.setParameter(paramName, paramVal);
        }
    }

    private XsltExecutable getStylesheet(Map<String, Sequence> options, XsltCompiler xsltCompiler, String styleOptionStr, XPathContext context) throws XPathException {
        boolean cacheable;
        StringValue styleBaseUri;
        Item styleOptionItem = options.get(styleOptionStr).head();
        URI stylesheetBaseUri = null;
        Sequence seq = options.get("stylesheet-base-uri");
        if (seq != null && !(stylesheetBaseUri = URI.create((styleBaseUri = (StringValue)seq.head()).getStringValue())).isAbsolute()) {
            URI staticBase = this.getRetainedStaticContext().getStaticBaseUri();
            stylesheetBaseUri = staticBase.resolve(styleBaseUri.getStringValue());
        }
        ArrayList<XmlProcessingError> compileErrors = new ArrayList<XmlProcessingError>();
        ErrorReporter originalReporter = xsltCompiler.getErrorReporter();
        xsltCompiler.setErrorReporter(error -> {
            if (!error.isWarning()) {
                compileErrors.add(error);
            }
            originalReporter.report(error);
        });
        boolean bl = cacheable = options.get("static-params") == null;
        if (options.get("cache") != null) {
            cacheable &= ((BooleanValue)options.get("cache").head()).getBooleanValue();
        }
        StylesheetCache cache = context.getController().getStylesheetCache();
        XsltExecutable executable = null;
        switch (styleOptionStr) {
            case "stylesheet-location": {
                Source style;
                String stylesheetLocation = styleOptionItem.getStringValue();
                if (cacheable) {
                    executable = cache.getStylesheetByLocation(stylesheetLocation);
                }
                if (executable != null) break;
                try {
                    String base = this.getStaticBaseUriString();
                    style = xsltCompiler.getURIResolver().resolve(stylesheetLocation, base);
                    if (style == null) {
                        style = xsltCompiler.getProcessor().getUnderlyingConfiguration().getSystemURIResolver().resolve(stylesheetLocation, base);
                    }
                } catch (TransformerException e) {
                    throw new XPathException(e);
                }
                try {
                    executable = xsltCompiler.compile(style);
                } catch (SaxonApiException e) {
                    return this.reportCompileError(e, compileErrors);
                }
                if (!cacheable) break;
                cache.setStylesheetByLocation(stylesheetLocation, executable);
                break;
            }
            case "stylesheet-node": 
            case "package-node": {
                NodeInfo stylesheetNode = (NodeInfo)styleOptionItem;
                if (stylesheetBaseUri != null && !stylesheetNode.getBaseURI().equals(stylesheetBaseUri.toASCIIString())) {
                    String newBaseUri = stylesheetBaseUri.toASCIIString();
                    RebasedDocument rebased = new RebasedDocument(stylesheetNode.getTreeInfo(), node -> newBaseUri, node -> newBaseUri);
                    stylesheetNode = rebased.getRootNode();
                }
                if (cacheable) {
                    executable = cache.getStylesheetByNode(stylesheetNode);
                }
                if (executable != null) break;
                Source source = stylesheetNode;
                if (stylesheetBaseUri != null) {
                    source = AugmentedSource.makeAugmentedSource(source);
                    source.setSystemId(stylesheetBaseUri.toASCIIString());
                }
                try {
                    executable = xsltCompiler.compile(source);
                } catch (SaxonApiException e) {
                    this.reportCompileError(e, compileErrors);
                }
                if (!cacheable) break;
                cache.setStylesheetByNode(stylesheetNode, executable);
                break;
            }
            case "stylesheet-text": {
                String stylesheetText = styleOptionItem.getStringValue();
                if (cacheable) {
                    executable = cache.getStylesheetByText(stylesheetText);
                }
                if (executable != null) break;
                StringReader sr = new StringReader(stylesheetText);
                SAXSource style = new SAXSource(new InputSource(sr));
                if (stylesheetBaseUri != null) {
                    style.setSystemId(stylesheetBaseUri.toASCIIString());
                }
                try {
                    executable = xsltCompiler.compile(style);
                } catch (SaxonApiException e) {
                    this.reportCompileError(e, compileErrors);
                }
                if (!cacheable) break;
                cache.setStylesheetByText(stylesheetText, executable);
                break;
            }
            case "package-name": {
                String packageName = Whitespace.trim(styleOptionItem.getStringValue());
                String packageVersion = null;
                if (options.get("package-version") != null) {
                    packageVersion = options.get("package-version").head().getStringValue();
                }
                try {
                    XsltPackage pack = xsltCompiler.obtainPackage(packageName, packageVersion);
                    if (pack == null) {
                        throw new XPathException("Cannot locate package " + packageName + " version " + packageVersion, "FOXT0002");
                    }
                    executable = pack.link();
                    break;
                } catch (SaxonApiException e) {
                    if (e.getCause() instanceof XPathException) {
                        throw (XPathException)e.getCause();
                    }
                    throw new XPathException(e);
                }
            }
        }
        return executable;
    }

    private XsltExecutable reportCompileError(SaxonApiException e, List<XmlProcessingError> compileErrors) throws XPathException {
        Iterator<XmlProcessingError> iterator = compileErrors.iterator();
        if (iterator.hasNext()) {
            XmlProcessingError te = iterator.next();
            XPathException xe = XPathException.fromXmlProcessingError(te);
            xe.maybeSetErrorCode("FOXT0002");
            throw xe;
        }
        if (e.getCause() instanceof XPathException) {
            throw (XPathException)e.getCause();
        }
        throw new XPathException(e);
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        Sequence result;
        Map<String, Sequence> options = this.getDetails().optionDetails.processSuppliedOptions((MapItem)arguments[0].head(), context);
        Sequence vendorOptionsValue = options.get("vendor-options");
        MapItem vendorOptions = vendorOptionsValue == null ? null : (MapItem)vendorOptionsValue.head();
        Configuration targetConfig = context.getConfiguration();
        boolean allowTypedNodes = true;
        int schemaValidation = 0;
        if (vendorOptions != null) {
            GroundedValue optionValue = vendorOptions.get(new QNameValue("", "http://saxon.sf.net/", "configuration"));
            if (optionValue != null) {
                NodeInfo configFile = (NodeInfo)optionValue.head();
                targetConfig = Configuration.readConfiguration(configFile, targetConfig);
                allowTypedNodes = false;
                if (!context.getConfiguration().getBooleanProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS)) {
                    targetConfig.setBooleanProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, false);
                }
            }
            if ((optionValue = vendorOptions.get(new QNameValue("", "http://saxon.sf.net/", "schema-validation"))) != null) {
                String valOption = optionValue.head().getStringValue();
                schemaValidation = Validation.getCode(valOption);
            }
        }
        Processor processor = new Processor(true);
        processor.setConfigurationProperty(Feature.CONFIGURATION, targetConfig);
        boolean isXslt30Processor = true;
        this.checkTransformOptions(options, context, isXslt30Processor);
        boolean useXslt30Processor = isXslt30Processor;
        if (options.get("xslt-version") != null) {
            BigDecimalValue xsltVersion = (BigDecimalValue)options.get("xslt-version").head();
            if (xsltVersion.compareTo(BigDecimalValue.THREE) >= 0 && !isXslt30Processor || xsltVersion.compareTo(BigDecimalValue.THREE) > 0 && isXslt30Processor) {
                throw new XPathException("The transform option xslt-version is higher than the XSLT version supported by this processor", "FOXT0002");
            }
            useXslt30Processor = xsltVersion.compareTo(BigDecimalValue.THREE) == 0;
        }
        String principalInput = this.oneOf(options, "source-node", "source-location", "initial-match-selection");
        String invocationName = "invocation";
        String invocationOption = this.checkInvocationMutualExclusion30(options);
        if (invocationOption != null) {
            invocationName = invocationOption;
        }
        if (!invocationName.equals("initial-template") && !invocationName.equals("initial-function") && principalInput == null) {
            invocationName = "initial-template";
            options.put("initial-template", new QNameValue("", "http://www.w3.org/1999/XSL/Transform", "initial-template"));
        }
        if (invocationName.equals("initial-function") && options.get("function-params") == null) {
            throw new XPathException("Use of the transform option initial-function requires the function parameters to be supplied using the option function-params", "FOXT0002");
        }
        if (!invocationName.equals("initial-function") && options.get("function-params") != null) {
            throw new XPathException("The transform option function-params can only be used if the option initial-function is also used", "FOXT0002");
        }
        String styleOption = this.checkStylesheetMutualExclusion30(options);
        if (options.get("requested-properties") != null) {
            this.setRequestedProperties(options, processor);
        }
        XsltCompiler xsltCompiler = processor.newXsltCompiler();
        xsltCompiler.setURIResolver(context.getURIResolver());
        xsltCompiler.setJustInTimeCompilation(false);
        if (options.get("static-params") != null) {
            this.setStaticParams(options, xsltCompiler, allowTypedNodes);
        }
        XsltExecutable sheet = this.getStylesheet(options, xsltCompiler, styleOption, context);
        Xslt30Transformer transformer = sheet.load30();
        String deliveryFormat = "document";
        NodeInfo sourceNode = null;
        String sourceLocation = null;
        XdmValue initialMatchSelection = null;
        QName initialTemplate = null;
        QName initialMode = null;
        String baseOutputUri = null;
        HashMap<QName, XdmValue> stylesheetParams = new HashMap<QName, XdmValue>();
        MapItem serializationParamsMap = null;
        Object serializedResult = null;
        Object serializedResultFile = null;
        XdmItem globalContextItem = null;
        HashMap<QName, XdmValue> templateParams = new HashMap<QName, XdmValue>();
        HashMap<QName, XdmValue> tunnelParams = new HashMap<QName, XdmValue>();
        QName initialFunction = null;
        XdmValue[] functionParams = null;
        Function postProcessor = null;
        String principalResultKey = "output";
        block40: for (String name : options.keySet()) {
            Sequence value = options.get(name);
            Item head = value.head();
            switch (name) {
                case "source-node": {
                    sourceNode = (NodeInfo)head;
                    if (allowTypedNodes) break;
                    this.checkSequenceIsUntyped(sourceNode);
                    break;
                }
                case "source-location": {
                    sourceLocation = head.getStringValue();
                    break;
                }
                case "initial-template": {
                    initialTemplate = new QName(((QNameValue)head).getStructuredQName());
                    break;
                }
                case "initial-mode": {
                    initialMode = new QName(((QNameValue)head).getStructuredQName());
                    break;
                }
                case "initial-match-selection": {
                    initialMatchSelection = XdmValue.wrap(value);
                    if (allowTypedNodes) break;
                    this.checkSequenceIsUntyped(value);
                    break;
                }
                case "delivery-format": {
                    deliveryFormat = head.getStringValue();
                    if (deliveryFormat.equals("document") || deliveryFormat.equals("serialized") || deliveryFormat.equals("raw")) break;
                    throw new XPathException("The transform option delivery-format should be one of: document|serialized|raw ", "FOXT0002");
                }
                case "base-output-uri": {
                    principalResultKey = baseOutputUri = head.getStringValue();
                    break;
                }
                case "serialization-params": {
                    serializationParamsMap = (MapItem)head;
                    break;
                }
                case "stylesheet-params": {
                    MapItem params = (MapItem)head;
                    this.processParams(params, stylesheetParams, allowTypedNodes);
                    break;
                }
                case "global-context-item": {
                    if (!allowTypedNodes && head instanceof NodeInfo && ((NodeInfo)head).getTreeInfo().isTyped()) {
                        throw new XPathException("Schema-validated nodes cannot be passed to fn:transform() when it runs under a different Saxon Configuration", "FOXT0002");
                    }
                    globalContextItem = (XdmItem)XdmValue.wrap(head);
                    break;
                }
                case "template-params": {
                    MapItem params = (MapItem)head;
                    this.processParams(params, templateParams, allowTypedNodes);
                    break;
                }
                case "tunnel-params": {
                    MapItem params = (MapItem)head;
                    this.processParams(params, tunnelParams, allowTypedNodes);
                    break;
                }
                case "initial-function": {
                    initialFunction = new QName(((QNameValue)head).getStructuredQName());
                    break;
                }
                case "function-params": {
                    ArrayItem functionParamsArray = (ArrayItem)head;
                    functionParams = new XdmValue[functionParamsArray.arrayLength()];
                    for (int i = 0; i < functionParams.length; ++i) {
                        GroundedValue argVal = functionParamsArray.get(i);
                        if (!allowTypedNodes) {
                            this.checkSequenceIsUntyped(argVal);
                        }
                        functionParams[i] = XdmValue.wrap(argVal);
                    }
                    continue block40;
                }
                case "post-process": {
                    postProcessor = (Function)head;
                }
            }
        }
        if (baseOutputUri == null) {
            baseOutputUri = this.getStaticBaseUriString();
        } else {
            try {
                URI base = new URI(baseOutputUri);
                if (!base.isAbsolute()) {
                    base = this.getRetainedStaticContext().getStaticBaseUri().resolve(baseOutputUri);
                    baseOutputUri = base.toASCIIString();
                }
            } catch (URISyntaxException err) {
                throw new XPathException("Invalid base output URI " + baseOutputUri, "FOXT0002");
            }
        }
        Deliverer deliverer = Deliverer.makeDeliverer(processor, deliveryFormat);
        deliverer.setTransformer(transformer);
        deliverer.setBaseOutputUri(baseOutputUri);
        deliverer.setPrincipalResultKey(principalResultKey);
        deliverer.setPostProcessor(postProcessor, context);
        XsltController controller = transformer.getUnderlyingController();
        controller.setResultDocumentResolver(deliverer);
        Destination destination = deliverer.getPrimaryDestination(serializationParamsMap);
        try {
            transformer.setStylesheetParameters(stylesheetParams);
            transformer.setBaseOutputURI(baseOutputUri);
            transformer.setInitialTemplateParameters(templateParams, false);
            transformer.setInitialTemplateParameters(tunnelParams, true);
            if (schemaValidation == 1 || schemaValidation == 2) {
                if (sourceNode != null) {
                    sourceNode = TransformFn.validate(sourceNode, targetConfig, schemaValidation);
                } else if (sourceLocation != null) {
                    try {
                        String base = this.getStaticBaseUriString();
                        Source ss = xsltCompiler.getURIResolver().resolve(sourceLocation, base);
                        if (ss == null && (ss = targetConfig.getURIResolver().resolve(sourceLocation, base)) == null) {
                            throw new XPathException("Cannot locate document at sourceLocation " + sourceLocation, "FOXT0003");
                        }
                        ParseOptions parseOptions = new ParseOptions(targetConfig.getParseOptions());
                        parseOptions.setSchemaValidationMode(schemaValidation);
                        TreeInfo tree = targetConfig.buildDocumentTree(ss, parseOptions);
                        sourceNode = tree.getRootNode();
                        sourceLocation = null;
                    } catch (TransformerException e) {
                        throw XPathException.makeXPathException(e);
                    }
                }
                if (globalContextItem instanceof XdmNode) {
                    NodeInfo v = TransformFn.validate(((XdmNode)globalContextItem).getUnderlyingNode(), targetConfig, schemaValidation);
                    globalContextItem = (XdmNode)XdmValue.wrap(v);
                }
            }
            if (sourceNode != null && globalContextItem == null) {
                transformer.setGlobalContextItem(new XdmNode(sourceNode.getRoot()));
            }
            if (globalContextItem != null) {
                transformer.setGlobalContextItem(globalContextItem);
            }
            if (initialTemplate != null) {
                transformer.callTemplate(initialTemplate, destination);
                result = deliverer.getPrimaryResult();
            } else if (initialFunction != null) {
                transformer.callFunction(initialFunction, functionParams, destination);
                result = deliverer.getPrimaryResult();
            } else {
                if (initialMode != null) {
                    transformer.setInitialMode(initialMode);
                }
                if (initialMatchSelection == null && sourceNode != null) {
                    initialMatchSelection = XdmValue.wrap(sourceNode);
                }
                if (initialMatchSelection == null && sourceLocation != null) {
                    StreamSource stream = new StreamSource(sourceLocation);
                    if (transformer.getUnderlyingController().getInitialMode().isDeclaredStreamable()) {
                        transformer.applyTemplates(stream, destination);
                    } else {
                        transformer.transform(stream, destination);
                    }
                    result = deliverer.getPrimaryResult();
                } else {
                    transformer.applyTemplates(initialMatchSelection, destination);
                    result = deliverer.getPrimaryResult();
                }
            }
        } catch (SaxonApiException e) {
            if (e.getCause() instanceof XPathException) {
                XPathException e2 = (XPathException)e.getCause();
                e2.setIsGlobalError(false);
                throw e2;
            }
            throw new XPathException(e);
        }
        HashTrieMap resultMap = new HashTrieMap();
        resultMap = deliverer.populateResultMap(resultMap);
        if (result != null) {
            StringValue resultKey = new StringValue(principalResultKey);
            resultMap = resultMap.addEntry(resultKey, result.materialize());
        }
        return resultMap;
    }

    private void processParams(MapItem suppliedParams, Map<QName, XdmValue> checkedParams, boolean allowTypedNodes) throws XPathException {
        Item param;
        AtomicIterator<? extends AtomicValue> paramIterator = suppliedParams.keys();
        while ((param = paramIterator.next()) != null) {
            if (!(param instanceof QNameValue)) {
                throw new XPathException("The names of parameters must be supplied as QNames", "FOXT0002");
            }
            QName paramName = new QName(((QNameValue)param).getStructuredQName());
            GroundedValue value = suppliedParams.get((AtomicValue)param);
            if (!allowTypedNodes) {
                this.checkSequenceIsUntyped(value);
            }
            XdmValue paramVal = XdmValue.wrap(value);
            checkedParams.put(paramName, paramVal);
        }
    }

    private void checkSequenceIsUntyped(Sequence value) throws XPathException {
        Item item;
        SequenceIterator iter = value.iterate();
        while ((item = iter.next()) != null) {
            if (!(item instanceof NodeInfo) || !((NodeInfo)item).getTreeInfo().isTyped()) continue;
            throw new XPathException("Schema-validated nodes cannot be passed to fn:transform() when it runs under a different Saxon Configuration", "FOXT0002");
        }
    }

    private static NodeInfo validate(NodeInfo node, Configuration config, int validation) throws XPathException {
        ParseOptions options = new ParseOptions(config.getParseOptions());
        options.setSchemaValidationMode(validation);
        return config.buildDocumentTree(node, options).getRootNode();
    }

    private static class RawDeliverer
    extends Deliverer {
        private Map<String, XdmValue> results = new ConcurrentHashMap<String, XdmValue>();
        private RawDestination primaryDestination = new RawDestination();

        @Override
        public Destination getPrimaryDestination(MapItem serializationParamsMap) {
            return this.primaryDestination;
        }

        @Override
        public Sequence getPrimaryResult() throws XPathException {
            GroundedValue actualResult = this.primaryDestination.getXdmValue().getUnderlyingValue();
            return this.postProcess(this.baseOutputUri, actualResult);
        }

        @Override
        public Receiver resolve(XPathContext context, String href, String baseUri, SerializationProperties properties) throws XPathException {
            URI absolute = this.getAbsoluteUri(href, baseUri);
            RawDestination destination = new RawDestination();
            destination.onClose(() -> {
                destination.close();
                this.results.put(absolute.toASCIIString(), destination.getXdmValue());
            });
            PipelineConfiguration pipe = context.getController().makePipelineConfiguration();
            return destination.getReceiver(pipe, properties);
        }

        @Override
        public HashTrieMap populateResultMap(HashTrieMap resultMap) {
            for (Map.Entry<String, XdmValue> entry : this.results.entrySet()) {
                String uri = entry.getKey();
                resultMap = resultMap.addEntry(new StringValue(uri), entry.getValue().getUnderlyingValue());
            }
            return resultMap;
        }
    }

    private static class SerializedDeliverer
    extends Deliverer {
        private final Processor processor;
        private Map<String, String> results = new ConcurrentHashMap<String, String>();
        private Map<String, StringWriter> workInProgress = new ConcurrentHashMap<String, StringWriter>();
        private StringWriter primaryWriter;

        public SerializedDeliverer(Processor processor) {
            this.processor = processor;
        }

        @Override
        public Destination getPrimaryDestination(MapItem serializationParamsMap) throws XPathException {
            Serializer serializer = this.makeSerializer(this.processor, serializationParamsMap);
            this.primaryWriter = new StringWriter();
            serializer.setOutputWriter(this.primaryWriter);
            return serializer;
        }

        @Override
        public Sequence getPrimaryResult() throws XPathException {
            String str = this.primaryWriter.toString();
            if (str.isEmpty()) {
                return null;
            }
            return this.postProcess(this.baseOutputUri, new StringValue(str));
        }

        @Override
        public Receiver resolve(XPathContext context, String href, String baseUri, SerializationProperties properties) throws XPathException {
            URI absolute = this.getAbsoluteUri(href, baseUri);
            if (absolute.getScheme().equals(TransformFn.dummyBaseOutputUriScheme)) {
                throw new XPathException("The location of output documents is undefined: use the transform option base-output-uri", "FOXT0002");
            }
            StringWriter writer = new StringWriter();
            Serializer serializer = this.makeSerializer(this.processor, null);
            serializer.setCharacterMap(properties.getCharacterMapIndex());
            serializer.setOutputWriter(writer);
            serializer.onClose(() -> this.results.put(absolute.toASCIIString(), writer.toString()));
            try {
                PipelineConfiguration pipe = context.getController().makePipelineConfiguration();
                Receiver out = serializer.getReceiver(pipe, properties);
                out.setSystemId(absolute.toASCIIString());
                return out;
            } catch (SaxonApiException e) {
                throw XPathException.makeXPathException(e);
            }
        }

        @Override
        public HashTrieMap populateResultMap(HashTrieMap resultMap) {
            for (Map.Entry<String, String> entry : this.results.entrySet()) {
                String uri = entry.getKey();
                resultMap = resultMap.addEntry(new StringValue(uri), new StringValue(entry.getValue()));
            }
            return resultMap;
        }
    }

    private static class DocumentDeliverer
    extends Deliverer {
        private Map<String, TreeInfo> results = new ConcurrentHashMap<String, TreeInfo>();
        private XdmDestination destination = new XdmDestination();

        @Override
        public Destination getPrimaryDestination(MapItem serializationParamsMap) {
            return this.destination;
        }

        @Override
        public Sequence getPrimaryResult() throws XPathException {
            XdmNode node = this.destination.getXdmNode();
            return node == null ? null : this.postProcess(this.baseOutputUri, node.getUnderlyingNode());
        }

        @Override
        public Receiver resolve(XPathContext context, String href, String baseUri, SerializationProperties properties) throws XPathException {
            URI absolute = this.getAbsoluteUri(href, baseUri);
            XdmDestination destination = new XdmDestination();
            destination.setDestinationBaseURI(absolute);
            destination.onClose(() -> {
                XdmNode root = destination.getXdmNode();
                this.results.put(absolute.toASCIIString(), root.getUnderlyingValue().getTreeInfo());
            });
            PipelineConfiguration pipe = context.getController().makePipelineConfiguration();
            return destination.getReceiver(pipe, properties);
        }

        @Override
        public HashTrieMap populateResultMap(HashTrieMap resultMap) {
            for (Map.Entry<String, TreeInfo> entry : this.results.entrySet()) {
                String uri = entry.getKey();
                resultMap = resultMap.addEntry(new StringValue(uri), entry.getValue().getRootNode());
            }
            return resultMap;
        }
    }

    private static abstract class Deliverer
    implements ResultDocumentResolver {
        protected Xslt30Transformer transformer;
        protected String baseOutputUri;
        protected String principalResultKey;
        protected Function postProcessor;
        protected XPathContext context;
        protected HashTrieMap resultMap = new HashTrieMap();

        private Deliverer() {
        }

        public static Deliverer makeDeliverer(Processor processor, String deliveryFormat) {
            switch (deliveryFormat) {
                case "document": {
                    return new DocumentDeliverer();
                }
                case "serialized": {
                    return new SerializedDeliverer(processor);
                }
                case "raw": {
                    return new RawDeliverer();
                }
            }
            throw new IllegalArgumentException("delivery-format");
        }

        public final void setTransformer(Xslt30Transformer transformer) {
            this.transformer = transformer;
        }

        public final void setPrincipalResultKey(String key) {
            this.principalResultKey = key;
        }

        public final void setBaseOutputUri(String uri) {
            this.baseOutputUri = uri;
        }

        public void setPostProcessor(Function postProcessor, XPathContext context) {
            this.postProcessor = postProcessor;
            this.context = context;
        }

        protected URI getAbsoluteUri(String href, String baseUri) throws XPathException {
            URI absolute;
            try {
                absolute = ResolveURI.makeAbsolute(href, baseUri);
            } catch (URISyntaxException e) {
                throw XPathException.makeXPathException(e);
            }
            return absolute;
        }

        public abstract HashTrieMap populateResultMap(HashTrieMap var1) throws XPathException;

        public abstract Destination getPrimaryDestination(MapItem var1) throws XPathException;

        protected Serializer makeSerializer(Processor processor, MapItem serializationParamsMap) throws XPathException {
            Serializer serializer = processor.newSerializer();
            if (serializationParamsMap != null) {
                Item param;
                AtomicIterator<? extends AtomicValue> paramIterator = serializationParamsMap.keys();
                while ((param = paramIterator.next()) != null) {
                    QName paramName;
                    if (param instanceof StringValue) {
                        paramName = new QName(((AtomicValue)param).getStringValue());
                    } else if (param instanceof QNameValue) {
                        paramName = new QName(((QNameValue)((AtomicValue)param).head()).getStructuredQName());
                    } else {
                        throw new XPathException("Serialization parameters must be strings or QNames", "XPTY0004");
                    }
                    String paramValue = null;
                    GroundedValue supplied = serializationParamsMap.get((AtomicValue)param);
                    if (supplied.getLength() <= 0) continue;
                    if (supplied.getLength() == 1) {
                        Item val = supplied.itemAt(0);
                        if (val instanceof StringValue) {
                            paramValue = val.getStringValue();
                        } else if (val instanceof BooleanValue) {
                            paramValue = ((BooleanValue)val).getBooleanValue() ? "yes" : "no";
                        } else if (val instanceof DecimalValue) {
                            paramValue = val.getStringValue();
                        } else if (val instanceof QNameValue) {
                            paramValue = ((QNameValue)val).getStructuredQName().getEQName();
                        } else if (val instanceof MapItem && paramName.getClarkName().equals("use-character-maps")) {
                            CharacterMap charMap = Serialize.toCharacterMap((MapItem)val);
                            CharacterMapIndex charMapIndex = new CharacterMapIndex();
                            charMapIndex.putCharacterMap(charMap.getName(), charMap);
                            serializer.setCharacterMap(charMapIndex);
                            String existing = serializer.getOutputProperty(Serializer.Property.USE_CHARACTER_MAPS);
                            if (existing == null) {
                                serializer.setOutputProperty(Serializer.Property.USE_CHARACTER_MAPS, charMap.getName().getEQName());
                                continue;
                            }
                            serializer.setOutputProperty(Serializer.Property.USE_CHARACTER_MAPS, existing + " " + charMap.getName().getEQName());
                            continue;
                        }
                    }
                    if (paramValue == null) {
                        Item it;
                        UnfailingIterator iter = supplied.iterate();
                        paramValue = "";
                        while ((it = iter.next()) != null) {
                            if (it instanceof QNameValue) {
                                paramValue = paramValue + " " + ((QNameValue)it).getStructuredQName().getEQName();
                                continue;
                            }
                            throw new XPathException("Value of serialization parameter " + paramName.getEQName() + " not recognized", "XPTY0004");
                        }
                    }
                    Serializer.Property prop = Serializer.getProperty(paramName);
                    if (paramName.getClarkName().equals("cdata-section-elements") || paramName.getClarkName().equals("suppress-indentation")) {
                        String existing = serializer.getOutputProperty(paramName);
                        if (existing == null) {
                            serializer.setOutputProperty(prop, paramValue);
                            continue;
                        }
                        serializer.setOutputProperty(prop, existing + paramValue);
                        continue;
                    }
                    serializer.setOutputProperty(prop, paramValue);
                }
            }
            return serializer;
        }

        public abstract Sequence getPrimaryResult() throws XPathException;

        public GroundedValue postProcess(String uri, Sequence result) throws XPathException {
            if (this.postProcessor != null) {
                result = this.postProcessor.call(this.context.newCleanContext(), new Sequence[]{new StringValue(uri), result});
            }
            return result.materialize();
        }
    }
}

