/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.io.Writer;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.ContentHandlerProxy;
import net.sf.saxon.event.NamespaceDifferencer;
import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceNormalizer;
import net.sf.saxon.event.SequenceNormalizerWithItemSeparator;
import net.sf.saxon.event.SequenceNormalizerWithSpaceSeparator;
import net.sf.saxon.event.SequenceReceiver;
import net.sf.saxon.event.Sink;
import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.event.TreeReceiver;
import net.sf.saxon.lib.ExternalObjectModel;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.SaxonOutputKeys;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.query.SequenceWrapper;
import net.sf.saxon.serialize.AdaptiveEmitter;
import net.sf.saxon.serialize.CDATAFilter;
import net.sf.saxon.serialize.CharacterMap;
import net.sf.saxon.serialize.CharacterMapExpander;
import net.sf.saxon.serialize.CharacterMapIndex;
import net.sf.saxon.serialize.Emitter;
import net.sf.saxon.serialize.ExpandedStreamResult;
import net.sf.saxon.serialize.HTML40Emitter;
import net.sf.saxon.serialize.HTML50Emitter;
import net.sf.saxon.serialize.HTMLEmitter;
import net.sf.saxon.serialize.HTMLIndenter;
import net.sf.saxon.serialize.HTMLURIEscaper;
import net.sf.saxon.serialize.JSONEmitter;
import net.sf.saxon.serialize.JSONSerializer;
import net.sf.saxon.serialize.MetaTagAdjuster;
import net.sf.saxon.serialize.SerializationParamsHandler;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.serialize.TEXTEmitter;
import net.sf.saxon.serialize.UncommittedSerializer;
import net.sf.saxon.serialize.UnicodeNormalizer;
import net.sf.saxon.serialize.XHTML1Emitter;
import net.sf.saxon.serialize.XHTML5Emitter;
import net.sf.saxon.serialize.XHTMLPrefixRemover;
import net.sf.saxon.serialize.XHTMLURIEscaper;
import net.sf.saxon.serialize.XML10ContentChecker;
import net.sf.saxon.serialize.XMLEmitter;
import net.sf.saxon.serialize.XMLIndenter;
import net.sf.saxon.stax.StAXResultHandlerImpl;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigDecimalValue;

public class SerializerFactory {
    Configuration config;
    PipelineConfiguration pipe;
    private static Pattern publicIdPattern = Pattern.compile("^[\\s\\r\\na-zA-Z0-9\\-'()+,./:=?;!*#@$_%]*$");

    public SerializerFactory(Configuration config) {
        this.config = config;
    }

    public SerializerFactory(PipelineConfiguration pipe) {
        this.pipe = pipe;
        this.config = pipe.getConfiguration();
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public StreamWriterToReceiver getXMLStreamWriter(StreamResult result, Properties properties) throws XPathException {
        Receiver r = this.getReceiver(result, new SerializationProperties(properties));
        r = new NamespaceReducer(r);
        return new StreamWriterToReceiver(r);
    }

    public Receiver getReceiver(Result result, PipelineConfiguration pipe, Properties props) throws XPathException {
        return this.getReceiver(result, new SerializationProperties(props), pipe);
    }

    public Receiver getReceiver(Result result) throws XPathException {
        return this.getReceiver(result, new SerializationProperties(), this.config.makePipelineConfiguration());
    }

    public Receiver getReceiver(Result result, SerializationProperties params) throws XPathException {
        return this.getReceiver(result, params, this.config.makePipelineConfiguration());
    }

    public Receiver getReceiver(Result result, SerializationProperties params, PipelineConfiguration pipe) throws XPathException {
        String nextInChain;
        Objects.requireNonNull(result);
        Objects.requireNonNull(params);
        Objects.requireNonNull(pipe);
        Properties props = params.getProperties();
        CharacterMapIndex charMapIndex = params.getCharacterMapIndex();
        if (charMapIndex == null) {
            charMapIndex = new CharacterMapIndex();
        }
        if ((nextInChain = props.getProperty("{http://saxon.sf.net/}next-in-chain")) != null && !nextInChain.isEmpty()) {
            String href = props.getProperty("{http://saxon.sf.net/}next-in-chain");
            String base = props.getProperty("{http://saxon.sf.net/}next-in-chain-base-uri");
            if (base == null) {
                base = "";
            }
            Properties sansNext = new Properties(props);
            sansNext.setProperty("{http://saxon.sf.net/}next-in-chain", "");
            return this.prepareNextStylesheet(pipe, href, base, result);
        }
        String paramDoc = props.getProperty("parameter-document");
        if (paramDoc != null && !paramDoc.isEmpty()) {
            Source source;
            String base = props.getProperty("{http://saxon.sf.net/}parameter-document-base-uri");
            if (base == null) {
                base = result.getSystemId();
            }
            Properties props2 = new Properties(props);
            props2.setProperty("parameter-document", "");
            try {
                source = this.config.getURIResolver().resolve(paramDoc, base);
            } catch (TransformerException e) {
                throw XPathException.makeXPathException(e);
            }
            Object options = new ParseOptions();
            ((ParseOptions)options).setSchemaValidationMode(2);
            ((ParseOptions)options).setDTDValidationMode(4);
            TreeInfo doc = this.config.buildDocumentTree(source);
            SerializationParamsHandler ph = new SerializationParamsHandler();
            ph.setSerializationParams(doc.getRootNode());
            Properties paramDocProps = ph.getSerializationProperties().getProperties();
            Enumeration<?> names = paramDocProps.propertyNames();
            while (names.hasMoreElements()) {
                String name = (String)names.nextElement();
                String value = paramDocProps.getProperty(name);
                props2.setProperty(name, value);
            }
            CharacterMap charMap = ph.getCharacterMap();
            if (charMap != null) {
                props2.setProperty("use-character-maps", charMap.getName().getClarkName());
                charMapIndex.putCharacterMap(charMap.getName(), charMap);
            }
            props = props2;
            params = new SerializationProperties(props2, charMapIndex);
        }
        if (result instanceof StreamResult) {
            SequenceReceiver target;
            String method = props.getProperty("method");
            if (method == null) {
                return this.newUncommittedSerializer(result, new Sink(pipe), params);
            }
            Emitter emitter = null;
            switch (method) {
                case "html": {
                    emitter = this.newHTMLEmitter(props);
                    emitter.setPipelineConfiguration(pipe);
                    target = this.createHTMLSerializer(emitter, params, pipe);
                    break;
                }
                case "xml": {
                    emitter = this.newXMLEmitter(props);
                    emitter.setPipelineConfiguration(pipe);
                    target = this.createXMLSerializer((XMLEmitter)emitter, params);
                    break;
                }
                case "xhtml": {
                    emitter = this.newXHTMLEmitter(props);
                    emitter.setPipelineConfiguration(pipe);
                    target = this.createXHTMLSerializer(emitter, params, pipe);
                    break;
                }
                case "text": {
                    emitter = this.newTEXTEmitter();
                    emitter.setPipelineConfiguration(pipe);
                    target = this.createTextSerializer(emitter, params);
                    break;
                }
                case "json": {
                    StreamResult sr = (StreamResult)result;
                    props.setProperty("omit-xml-declaration", "yes");
                    JSONEmitter je = new JSONEmitter(pipe, sr, props);
                    JSONSerializer js = new JSONSerializer(pipe, je, props);
                    String sortOrder = props.getProperty("{http://saxon.sf.net/}property-order");
                    if (sortOrder != null) {
                        js.setPropertySorter(this.getPropertySorter(sortOrder));
                    }
                    CharacterMapExpander characterMapExpander = this.makeCharacterMapExpander(pipe, props, charMapIndex);
                    ProxyReceiver normalizer = this.makeUnicodeNormalizer(pipe, props);
                    return this.customizeJSONSerializer(js, props, characterMapExpander, normalizer);
                }
                case "adaptive": {
                    ExpandedStreamResult esr = new ExpandedStreamResult(pipe.getConfiguration(), (StreamResult)result, props);
                    Writer writer = esr.obtainWriter();
                    AdaptiveEmitter je = new AdaptiveEmitter(pipe, writer);
                    je.setOutputProperties(props);
                    CharacterMapExpander characterMapExpander = this.makeCharacterMapExpander(pipe, props, charMapIndex);
                    ProxyReceiver normalizer = this.makeUnicodeNormalizer(pipe, props);
                    return this.customizeAdaptiveSerializer(je, props, characterMapExpander, normalizer);
                }
                default: {
                    if (method.startsWith("{")) {
                        method = "Q" + method;
                    }
                    if (method.startsWith("Q{http://saxon.sf.net/}")) {
                        ProxyReceiver normalizer;
                        CharacterMapExpander characterMapExpander = this.makeCharacterMapExpander(pipe, props, charMapIndex);
                        target = this.createSaxonSerializationMethod(method, params, pipe, characterMapExpander, normalizer = this.makeUnicodeNormalizer(pipe, props), (StreamResult)result);
                        if (!(target instanceof Emitter)) break;
                        emitter = (Emitter)target;
                        break;
                    }
                    SequenceReceiver userReceiver = this.createUserDefinedOutputMethod(method, props, pipe);
                    if (userReceiver instanceof Emitter) {
                        emitter = (Emitter)userReceiver;
                        target = params.makeSequenceNormalizer(emitter);
                        break;
                    }
                    return params.makeSequenceNormalizer(userReceiver);
                }
            }
            if (emitter != null) {
                emitter.setOutputProperties(props);
                StreamResult sr = (StreamResult)result;
                emitter.setStreamResult(sr);
            }
            target.setSystemId(result.getSystemId());
            return target;
        }
        return this.getReceiverForNonSerializedResult(result, props, pipe);
    }

    private ProxyReceiver makeUnicodeNormalizer(PipelineConfiguration pipe, Properties props) throws XPathException {
        String normForm = props.getProperty("normalization-form");
        if (normForm != null && !normForm.equals("none")) {
            return this.newUnicodeNormalizer(new Sink(pipe), props);
        }
        return null;
    }

    private CharacterMapExpander makeCharacterMapExpander(PipelineConfiguration pipe, Properties props, CharacterMapIndex charMapIndex) throws XPathException {
        String useMaps = props.getProperty("use-character-maps");
        if (useMaps != null) {
            return charMapIndex.makeCharacterMapExpander(useMaps, new Sink(pipe), this);
        }
        return null;
    }

    private Receiver getReceiverForNonSerializedResult(Result result, Properties props, PipelineConfiguration pipe) throws XPathException {
        if (result instanceof Emitter) {
            if (((Emitter)result).getOutputProperties() == null) {
                ((Emitter)result).setOutputProperties(props);
            }
            return (Emitter)result;
        }
        if (result instanceof JSONSerializer) {
            if (((JSONSerializer)result).getOutputProperties() == null) {
                ((JSONSerializer)result).setOutputProperties(props);
            }
            return (JSONSerializer)result;
        }
        if (result instanceof AdaptiveEmitter) {
            if (((AdaptiveEmitter)result).getOutputProperties() == null) {
                ((AdaptiveEmitter)result).setOutputProperties(props);
            }
            return (AdaptiveEmitter)result;
        }
        if (result instanceof Receiver) {
            Receiver receiver = (Receiver)result;
            receiver.setSystemId(result.getSystemId());
            receiver.setPipelineConfiguration(pipe);
            if (((Receiver)result).handlesAppend() && "no".equals(props.getProperty("build-tree"))) {
                return receiver;
            }
            return new TreeReceiver(receiver);
        }
        if (result instanceof SAXResult) {
            ContentHandlerProxy proxy = this.newContentHandlerProxy();
            proxy.setUnderlyingContentHandler(((SAXResult)result).getHandler());
            proxy.setPipelineConfiguration(pipe);
            proxy.setOutputProperties(props);
            if ("yes".equals(props.getProperty("{http://saxon.sf.net/}supply-source-locator"))) {
                if (this.config.isCompileWithTracing() && pipe.getController() != null) {
                    pipe.getController().addTraceListener(proxy.getTraceListener());
                } else {
                    throw new XPathException("Cannot use saxon:supply-source-locator unless tracing was enabled at compile time", "SXSE0002");
                }
            }
            return this.makeSequenceNormalizer(proxy, props);
        }
        if (result instanceof StAXResult) {
            StAXResultHandlerImpl handler = new StAXResultHandlerImpl();
            Receiver r = handler.getReceiver(result, props);
            r.setPipelineConfiguration(pipe);
            return this.makeSequenceNormalizer(r, props);
        }
        if (pipe != null) {
            List<ExternalObjectModel> externalObjectModels = pipe.getConfiguration().getExternalObjectModels();
            for (ExternalObjectModel externalObjectModel : externalObjectModels) {
                ExternalObjectModel model = externalObjectModel;
                Receiver builder = model.getDocumentBuilder(result);
                if (builder == null) continue;
                builder.setSystemId(result.getSystemId());
                builder.setPipelineConfiguration(pipe);
                return new TreeReceiver(builder);
            }
        }
        throw new IllegalArgumentException("Unknown type of result: " + result.getClass());
    }

    public SequenceReceiver makeSequenceNormalizer(Receiver receiver, Properties properties) {
        String method = properties.getProperty("method");
        if ("json".equals(method) || "adaptive".equals(method)) {
            return receiver instanceof SequenceReceiver ? (SequenceReceiver)receiver : new TreeReceiver(receiver);
        }
        PipelineConfiguration pipe = receiver.getPipelineConfiguration();
        String separator = properties.getProperty("item-separator");
        SequenceNormalizer result = separator == null || "#absent".equals(separator) ? new SequenceNormalizerWithSpaceSeparator(receiver) : new SequenceNormalizerWithItemSeparator(receiver, separator);
        ((SequenceReceiver)result).setPipelineConfiguration(pipe);
        return result;
    }

    protected SequenceReceiver createHTMLSerializer(Emitter emitter, SerializationProperties params, PipelineConfiguration pipe) throws XPathException {
        String attributeOrder;
        Receiver target = emitter;
        Properties props = params.getProperties();
        if (!"no".equals(props.getProperty("indent"))) {
            target = this.newHTMLIndenter(target, props);
        }
        target = new NamespaceDifferencer(target, props);
        target = this.injectUnicodeNormalizer(params, target);
        target = this.injectCharacterMapExpander(params, target, true);
        String cdataElements = props.getProperty("cdata-section-elements");
        if (cdataElements != null && !cdataElements.isEmpty()) {
            target = this.newCDATAFilter(target, props);
        }
        if (SaxonOutputKeys.isHtmlVersion5(props)) {
            target = this.addHtml5Component(target, props);
        }
        if (!"no".equals(props.getProperty("escape-uri-attributes"))) {
            target = this.newHTMLURIEscaper(target, props);
        }
        if (!"no".equals(props.getProperty("include-content-type"))) {
            target = this.newHTMLMetaTagAdjuster(target, props);
        }
        if ((attributeOrder = props.getProperty("{http://saxon.sf.net/}attribute-order")) != null && !attributeOrder.isEmpty()) {
            target = this.newAttributeSorter(target, props);
        }
        if (params.getValidationFactory() != null) {
            target = params.getValidationFactory().makeFilter(target);
        }
        return this.makeSequenceNormalizer(target, props);
    }

    protected SequenceReceiver createTextSerializer(Emitter emitter, SerializationProperties params) throws XPathException {
        Properties props = params.getProperties();
        Receiver target = this.injectUnicodeNormalizer(params, emitter);
        target = this.injectCharacterMapExpander(params, target, false);
        target = this.addTextOutputFilter(target, props);
        if (params.getValidationFactory() != null) {
            target = params.getValidationFactory().makeFilter(target);
        }
        return this.makeSequenceNormalizer(target, props);
    }

    protected SequenceReceiver customizeJSONSerializer(JSONSerializer emitter, Properties props, CharacterMapExpander characterMapExpander, ProxyReceiver normalizer) throws XPathException {
        if (normalizer instanceof UnicodeNormalizer) {
            emitter.setNormalizer(((UnicodeNormalizer)normalizer).getNormalizer());
        }
        if (characterMapExpander != null) {
            emitter.setCharacterMap(characterMapExpander.getCharacterMap());
        }
        return emitter;
    }

    protected SequenceReceiver customizeAdaptiveSerializer(AdaptiveEmitter emitter, Properties props, CharacterMapExpander characterMapExpander, ProxyReceiver normalizer) {
        if (normalizer instanceof UnicodeNormalizer) {
            emitter.setNormalizer(((UnicodeNormalizer)normalizer).getNormalizer());
        }
        if (characterMapExpander != null) {
            emitter.setCharacterMap(characterMapExpander.getCharacterMap());
        }
        return emitter;
    }

    protected SequenceReceiver createXHTMLSerializer(Emitter emitter, SerializationProperties params, PipelineConfiguration pipe) throws XPathException {
        String attributeOrder;
        Receiver target = emitter;
        Properties props = params.getProperties();
        if (!"no".equals(props.getProperty("indent"))) {
            target = this.newXHTMLIndenter(target, props);
        }
        target = new NamespaceDifferencer(target, props);
        target = this.injectUnicodeNormalizer(params, target);
        target = this.injectCharacterMapExpander(params, target, true);
        String cdataElements = props.getProperty("cdata-section-elements");
        if (cdataElements != null && !cdataElements.isEmpty()) {
            target = this.newCDATAFilter(target, props);
        }
        if (SaxonOutputKeys.isXhtmlHtmlVersion5(props)) {
            target = this.addHtml5Component(target, props);
        }
        if (!"no".equals(props.getProperty("escape-uri-attributes"))) {
            target = this.newXHTMLURIEscaper(target, props);
        }
        if (!"no".equals(props.getProperty("include-content-type"))) {
            target = this.newXHTMLMetaTagAdjuster(target, props);
        }
        if ((attributeOrder = props.getProperty("{http://saxon.sf.net/}attribute-order")) != null && !attributeOrder.isEmpty()) {
            target = this.newAttributeSorter(target, props);
        }
        if (params.getValidationFactory() != null) {
            target = params.getValidationFactory().makeFilter(target);
        }
        return this.makeSequenceNormalizer(target, props);
    }

    public Receiver addHtml5Component(Receiver target, Properties outputProperties) {
        target = new NamespaceReducer(target);
        target = new XHTMLPrefixRemover(target);
        return target;
    }

    protected SequenceReceiver createXMLSerializer(XMLEmitter emitter, SerializationProperties params) throws XPathException {
        String cdataElements;
        Properties props = params.getProperties();
        boolean canonical = "yes".equals(props.getProperty("{http://saxon.sf.net/}canonical"));
        Receiver target = "yes".equals(props.getProperty("indent")) || canonical ? this.newXMLIndenter(emitter, props) : emitter;
        target = new NamespaceDifferencer(target, props);
        if ("1.0".equals(props.getProperty("version")) && this.config.getXMLVersion() == 11) {
            target = this.newXML10ContentChecker(target, props);
        }
        target = this.injectUnicodeNormalizer(params, target);
        if (!canonical) {
            target = this.injectCharacterMapExpander(params, target, true);
        }
        if ((cdataElements = props.getProperty("cdata-section-elements")) != null && !cdataElements.isEmpty() && !canonical) {
            target = this.newCDATAFilter(target, props);
        }
        if (canonical) {
            target = this.newAttributeSorter(target, props);
            target = this.newNamespaceSorter(target, props);
        } else {
            String attributeOrder = props.getProperty("{http://saxon.sf.net/}attribute-order");
            if (attributeOrder != null && !attributeOrder.isEmpty()) {
                target = this.newAttributeSorter(target, props);
            }
        }
        if (params.getValidationFactory() != null) {
            target = params.getValidationFactory().makeFilter(target);
        }
        return this.makeSequenceNormalizer(target, props);
    }

    protected SequenceReceiver createSaxonSerializationMethod(String method, SerializationProperties params, PipelineConfiguration pipe, CharacterMapExpander characterMapExpander, ProxyReceiver normalizer, StreamResult result) throws XPathException {
        throw new XPathException("Saxon serialization methods require Saxon-PE to be enabled");
    }

    protected SequenceReceiver createUserDefinedOutputMethod(String method, Properties props, PipelineConfiguration pipe) throws XPathException {
        Receiver userReceiver = pipe.getConfiguration().makeEmitter(method, props);
        userReceiver.setPipelineConfiguration(pipe);
        if (userReceiver instanceof ContentHandlerProxy && "yes".equals(props.getProperty("{http://saxon.sf.net/}supply-source-locator"))) {
            if (pipe.getConfiguration().isCompileWithTracing() && pipe.getController() != null) {
                pipe.getController().addTraceListener(((ContentHandlerProxy)userReceiver).getTraceListener());
            } else {
                throw new XPathException("Cannot use saxon:supply-source-locator unless tracing was enabled at compile time", "SXSE0002");
            }
        }
        return userReceiver instanceof SequenceReceiver ? (SequenceReceiver)userReceiver : new TreeReceiver(userReceiver);
    }

    protected Receiver injectCharacterMapExpander(SerializationProperties params, Receiver out, boolean useNullMarkers) throws XPathException {
        String useMaps;
        CharacterMapIndex charMapIndex = params.getCharacterMapIndex();
        if (charMapIndex != null && (useMaps = params.getProperties().getProperty("use-character-maps")) != null) {
            CharacterMapExpander expander = charMapIndex.makeCharacterMapExpander(useMaps, out, this);
            expander.setUseNullMarkers(useNullMarkers);
            return expander;
        }
        return out;
    }

    protected Receiver injectUnicodeNormalizer(SerializationProperties params, Receiver out) throws XPathException {
        Properties props = params.getProperties();
        String normForm = props.getProperty("normalization-form");
        if (normForm != null && !normForm.equals("none")) {
            return this.newUnicodeNormalizer(out, props);
        }
        return out;
    }

    protected ContentHandlerProxy newContentHandlerProxy() {
        return new ContentHandlerProxy();
    }

    protected UncommittedSerializer newUncommittedSerializer(Result result, Receiver next, SerializationProperties params) {
        return new UncommittedSerializer(result, next, params);
    }

    protected Emitter newXMLEmitter(Properties properties) {
        return new XMLEmitter();
    }

    protected Emitter newHTMLEmitter(Properties properties) {
        HTMLEmitter emitter = SaxonOutputKeys.isHtmlVersion5(properties) ? new HTML50Emitter() : new HTML40Emitter();
        return emitter;
    }

    protected Emitter newXHTMLEmitter(Properties properties) {
        boolean is5 = SaxonOutputKeys.isXhtmlHtmlVersion5(properties);
        return is5 ? new XHTML5Emitter() : new XHTML1Emitter();
    }

    public Receiver addTextOutputFilter(Receiver next, Properties properties) throws XPathException {
        return next;
    }

    protected Emitter newTEXTEmitter() {
        return new TEXTEmitter();
    }

    protected ProxyReceiver newXMLIndenter(XMLEmitter next, Properties outputProperties) {
        XMLIndenter r = new XMLIndenter(next);
        r.setOutputProperties(outputProperties);
        return r;
    }

    protected ProxyReceiver newHTMLIndenter(Receiver next, Properties outputProperties) {
        HTMLIndenter r = new HTMLIndenter(next, "html");
        r.setOutputProperties(outputProperties);
        return r;
    }

    protected ProxyReceiver newXHTMLIndenter(Receiver next, Properties outputProperties) {
        String method = "xhtml";
        String htmlVersion = outputProperties.getProperty("html-version");
        if (htmlVersion != null && htmlVersion.startsWith("5")) {
            method = "xhtml5";
        }
        HTMLIndenter r = new HTMLIndenter(next, method);
        r.setOutputProperties(outputProperties);
        return r;
    }

    protected MetaTagAdjuster newXHTMLMetaTagAdjuster(Receiver next, Properties outputProperties) {
        MetaTagAdjuster r = new MetaTagAdjuster(next);
        r.setIsXHTML(true);
        r.setOutputProperties(outputProperties);
        return r;
    }

    protected MetaTagAdjuster newHTMLMetaTagAdjuster(Receiver next, Properties outputProperties) {
        MetaTagAdjuster r = new MetaTagAdjuster(next);
        r.setIsXHTML(false);
        r.setOutputProperties(outputProperties);
        return r;
    }

    protected ProxyReceiver newHTMLURIEscaper(Receiver next, Properties outputProperties) {
        return new HTMLURIEscaper(next);
    }

    protected ProxyReceiver newXHTMLURIEscaper(Receiver next, Properties outputProperties) {
        return new XHTMLURIEscaper(next);
    }

    protected ProxyReceiver newCDATAFilter(Receiver next, Properties outputProperties) throws XPathException {
        CDATAFilter r = new CDATAFilter(next);
        r.setOutputProperties(outputProperties);
        return r;
    }

    protected Receiver newAttributeSorter(Receiver next, Properties outputProperties) throws XPathException {
        return next;
    }

    protected Receiver newNamespaceSorter(Receiver next, Properties outputProperties) throws XPathException {
        return next;
    }

    protected ProxyReceiver newXML10ContentChecker(Receiver next, Properties outputProperties) {
        return new XML10ContentChecker(next);
    }

    protected ProxyReceiver newUnicodeNormalizer(Receiver next, Properties outputProperties) throws XPathException {
        String normForm = outputProperties.getProperty("normalization-form");
        return new UnicodeNormalizer(normForm, next);
    }

    public CharacterMapExpander newCharacterMapExpander(Receiver next) {
        return new CharacterMapExpander(next);
    }

    public SequenceReceiver prepareNextStylesheet(PipelineConfiguration pipe, String href, String baseURI, Result result) throws XPathException {
        pipe.getConfiguration().checkLicensedFeature(8, "saxon:next-in-chain", -1);
        return null;
    }

    public SequenceWrapper newSequenceWrapper(Receiver destination) {
        return new SequenceWrapper(destination);
    }

    public String checkOutputProperty(String key, String value) throws XPathException {
        block65: {
            block64: {
                if (key.startsWith("{")) break block64;
                switch (key) {
                    case "allow-duplicate-names": 
                    case "escape-uri-attributes": 
                    case "include-content-type": 
                    case "indent": 
                    case "omit-xml-declaration": 
                    case "undeclare-prefixes": {
                        if (value != null) {
                            value = SerializerFactory.checkYesOrNo(key, value);
                            break;
                        }
                        break block65;
                    }
                    case "build-tree": {
                        if (value != null) {
                            value = SerializerFactory.checkYesOrNo(key, value);
                            break;
                        }
                        break block65;
                    }
                    case "byte-order-mark": {
                        if (value != null) {
                            value = SerializerFactory.checkYesOrNo(key, value);
                            break;
                        }
                        break block65;
                    }
                    case "cdata-section-elements": 
                    case "suppress-indentation": 
                    case "use-character-maps": {
                        if (value != null) {
                            value = SerializerFactory.checkListOfEQNames(key, value);
                            break;
                        }
                        break block65;
                    }
                    case "doctype-public": {
                        if (value != null) {
                            SerializerFactory.checkPublicIdentifier(value);
                            break;
                        }
                        break block65;
                    }
                    case "doctype-system": {
                        if (value != null) {
                            SerializerFactory.checkSystemIdentifier(value);
                            break;
                        }
                        break block65;
                    }
                    case "encoding": {
                        break;
                    }
                    case "html-version": {
                        if (value != null) {
                            SerializerFactory.checkDecimal(key, value);
                            break;
                        }
                        break block65;
                    }
                    case "item-separator": {
                        break;
                    }
                    case "method": 
                    case "json-node-output-method": {
                        if (value != null) {
                            value = this.checkMethod(key, value);
                            break;
                        }
                        break block65;
                    }
                    case "media-type": {
                        break;
                    }
                    case "normalization-form": {
                        if (value != null) {
                            SerializerFactory.checkNormalizationForm(value);
                            break;
                        }
                        break block65;
                    }
                    case "parameter-document": {
                        break;
                    }
                    case "standalone": {
                        if (value != null && !value.equals("omit")) {
                            value = SerializerFactory.checkYesOrNo(key, value);
                            break;
                        }
                        break block65;
                    }
                    case "version": {
                        break;
                    }
                    default: {
                        throw new XPathException("Unknown serialization parameter " + Err.wrap(key), "XQST0109");
                    }
                }
                break block65;
            }
            if (key.startsWith("{http://saxon.sf.net/}")) {
                switch (key) {
                    case "{http://saxon.sf.net/}stylesheet-version": {
                        break;
                    }
                    case "{http://saxon.sf.net/}parameter-document-base-uri": {
                        break;
                    }
                    case "{http://saxon.sf.net/}supply-source-locator": 
                    case "{http://saxon.sf.net/}unfailing": {
                        if (value == null) break;
                        value = SerializerFactory.checkYesOrNo(key, value);
                        break;
                    }
                    default: {
                        throw new XPathException("Serialization parameter " + Err.wrap(key, 8) + " is not available in Saxon-HE", "XQST0109");
                    }
                }
            }
        }
        return value;
    }

    protected static String checkYesOrNo(String key, String value) throws XPathException {
        if ("yes".equals(value) || "true".equals(value) || "1".equals(value)) {
            return "yes";
        }
        if ("no".equals(value) || "false".equals(value) || "0".equals(value)) {
            return "no";
        }
        throw new XPathException("Serialization parameter " + Err.wrap(key) + " must have the value yes|no, true|false, or 1|0", "SEPM0016");
    }

    private String checkMethod(String key, String value) throws XPathException {
        if (!("xml".equals(value) || "html".equals(value) || "xhtml".equals(value) || "text".equals(value))) {
            if (!"json-node-output-method".equals(key) && ("json".equals(value) || "adaptive".equals(value))) {
                return value;
            }
            if (value.startsWith("{")) {
                value = "Q" + value;
            }
            if (SerializerFactory.isValidEQName(value)) {
                this.checkExtensions(value);
            } else {
                throw new XPathException("Invalid value (" + value + ") for serialization method: must be xml|html|xhtml|text|json|adaptive, or a QName in 'Q{uri}local' form", "SEPM0016");
            }
        }
        return value;
    }

    private static void checkNormalizationForm(String value) throws XPathException {
        if (!NameChecker.isValidNmtoken(value)) {
            throw new XPathException("Invalid value for normalization-form: must be NFC, NFD, NFKC, NFKD, fully-normalized, or none", "SEPM0016");
        }
    }

    private static boolean isValidEQName(String value) {
        Objects.requireNonNull(value);
        if (value.isEmpty() || !value.startsWith("Q{")) {
            return false;
        }
        int closer = value.indexOf(125, 2);
        return closer >= 2 && closer != value.length() - 1 && NameChecker.isValidNCName(value.substring(closer + 1));
    }

    private static boolean isValidClarkName(String value) {
        if (value.startsWith("{")) {
            return SerializerFactory.isValidEQName("Q" + value);
        }
        return SerializerFactory.isValidEQName("Q{}" + value);
    }

    protected static void checkNonNegativeInteger(String key, String value) throws XPathException {
        try {
            int n = Integer.parseInt(value);
            if (n < 0) {
                throw new XPathException("Value of " + Err.wrap(key) + " must be a non-negative integer", "SEPM0016");
            }
        } catch (NumberFormatException err) {
            throw new XPathException("Value of " + Err.wrap(key) + " must be a non-negative integer", "SEPM0016");
        }
    }

    private static void checkDecimal(String key, String value) throws XPathException {
        if (!BigDecimalValue.castableAsDecimal(value)) {
            throw new XPathException("Value of " + Err.wrap(key) + " must be a decimal number", "SEPM0016");
        }
    }

    protected static String checkListOfEQNames(String key, String value) throws XPathException {
        StringTokenizer tok = new StringTokenizer(value, " \t\n\r", false);
        StringBuilder builder = new StringBuilder();
        while (tok.hasMoreTokens()) {
            String s = tok.nextToken();
            if (SerializerFactory.isValidEQName(s) || NameChecker.isValidNCName(s)) {
                builder.append(s);
            } else if (SerializerFactory.isValidClarkName(s)) {
                if (s.startsWith("{")) {
                    builder.append("Q").append(s);
                } else {
                    builder.append("Q{}").append(s);
                }
            } else {
                throw new XPathException("Value of " + Err.wrap(key) + " must be a list of QNames in 'Q{uri}local' notation", "SEPM0016");
            }
            builder.append(" ");
        }
        return builder.toString();
    }

    protected static String checkListOfEQNamesAllowingStar(String key, String value) throws XPathException {
        StringBuilder builder = new StringBuilder();
        StringTokenizer tok = new StringTokenizer(value, " \t\n\r", false);
        while (tok.hasMoreTokens()) {
            String s = tok.nextToken();
            if ("*".equals(s) || SerializerFactory.isValidEQName(s) || NameChecker.isValidNCName(s)) {
                builder.append(s);
            } else if (SerializerFactory.isValidClarkName(s)) {
                if (s.startsWith("{")) {
                    builder.append("Q").append(s);
                } else {
                    builder.append("Q{}").append(s);
                }
            } else {
                throw new XPathException("Value of " + Err.wrap(key) + " must be a list of QNames in 'Q{uri}local' notation", "SEPM0016");
            }
            builder.append(" ");
        }
        return builder.toString().trim();
    }

    private static void checkPublicIdentifier(String value) throws XPathException {
        if (!publicIdPattern.matcher(value).matches()) {
            throw new XPathException("Invalid character in doctype-public parameter", "SEPM0016");
        }
    }

    private static void checkSystemIdentifier(String value) throws XPathException {
        if (value.contains("'") && value.contains("\"")) {
            throw new XPathException("The doctype-system parameter must not contain both an apostrophe and a quotation mark", "SEPM0016");
        }
    }

    public static String parseListOfNodeNames(String value, NamespaceResolver nsResolver, boolean useDefaultNS, boolean prevalidated, String errorCode) throws XPathException {
        StringBuilder s = new StringBuilder();
        StringTokenizer st = new StringTokenizer(value, " \t\n\r", false);
        while (st.hasMoreTokens()) {
            String displayname = st.nextToken();
            if (prevalidated || nsResolver == null) {
                s.append(' ').append(displayname);
                continue;
            }
            if (displayname.startsWith("Q{")) {
                s.append(' ').append(displayname.substring(1));
                continue;
            }
            try {
                String[] parts = NameChecker.getQNameParts(displayname);
                String muri = nsResolver.getURIForPrefix(parts[0], useDefaultNS);
                if (muri == null) {
                    throw new XPathException("Namespace prefix '" + parts[0] + "' has not been declared", errorCode);
                }
                s.append(" {").append(muri).append('}').append(parts[1]);
            } catch (QNameException err) {
                throw new XPathException("Invalid element name. " + err.getMessage(), errorCode);
            }
        }
        return s.toString();
    }

    protected void checkExtensions(String key) throws XPathException {
        throw new XPathException("Serialization property " + Err.wrap(key, 8) + " is not available in Saxon-HE");
    }

    protected Comparator<AtomicValue> getPropertySorter(String sortSpecification) throws XPathException {
        throw new XPathException("Serialization property saxon:property-order is not available in Saxon-HE");
    }
}

