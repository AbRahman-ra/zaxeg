/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.Version;
import net.sf.saxon.event.CopyInformee;
import net.sf.saxon.event.EventSource;
import net.sf.saxon.event.FilterFactory;
import net.sf.saxon.event.LocationCopier;
import net.sf.saxon.event.NamePoolConverter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceivingContentHandler;
import net.sf.saxon.expr.number.Numberer_en;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.lib.AugmentedSource;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.ExternalObjectModel;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.StandardErrorHandler;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pull.PullProvider;
import net.sf.saxon.pull.PullPushCopier;
import net.sf.saxon.pull.PullSource;
import net.sf.saxon.pull.StaxBridge;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.sapling.SaplingDocument;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.trans.XmlProcessingIncident;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public abstract class Sender {
    private Sender() {
    }

    public static void send(Source source, Receiver receiver, ParseOptions options) throws XPathException {
        SpaceStrippingRule strippingRule;
        PipelineConfiguration pipe = receiver.getPipelineConfiguration();
        options = options == null ? new ParseOptions(pipe.getParseOptions()) : new ParseOptions(options);
        String systemId = source.getSystemId();
        if (source instanceof AugmentedSource) {
            options.merge(((AugmentedSource)source).getParseOptions());
            systemId = source.getSystemId();
            source = ((AugmentedSource)source).getContainedSource();
        }
        Configuration config = pipe.getConfiguration();
        options.applyDefaults(config);
        receiver.setSystemId(systemId);
        Receiver next = receiver;
        int schemaValidation = options.getSchemaValidationMode();
        List<FilterFactory> filters = options.getFilters();
        if (filters != null) {
            for (int i = filters.size() - 1; i >= 0; --i) {
                Receiver filter = filters.get(i).makeFilter(next);
                filter.setSystemId(source.getSystemId());
                next = filter;
            }
        }
        if ((strippingRule = options.getSpaceStrippingRule()) != null && !(strippingRule instanceof NoElementsSpaceStrippingRule)) {
            next = strippingRule.makeStripper(next);
        }
        if (source instanceof TreeInfo) {
            source = ((TreeInfo)source).getRootNode();
        }
        if (source instanceof NodeInfo) {
            int kind;
            NodeInfo ns = (NodeInfo)source;
            String baseURI = ns.getBaseURI();
            if (schemaValidation != 3) {
                next = config.getDocumentValidator(next, baseURI, options, null);
            }
            if ((kind = ns.getNodeKind()) != 9 && kind != 1) {
                throw new IllegalArgumentException("Sender can only handle document or element nodes");
            }
            next.setSystemId(baseURI);
            Loc loc = new Loc(systemId, -1, -1);
            Sender.sendDocumentInfo(ns, next, loc);
            return;
        }
        if (source instanceof PullSource) {
            Sender.sendPullSource((PullSource)source, next, options);
            return;
        }
        if (source instanceof EventSource) {
            ((EventSource)source).send(next);
            return;
        }
        if (source instanceof SAXSource) {
            Sender.sendSAXSource((SAXSource)source, next, options);
            return;
        }
        if (source instanceof StreamSource) {
            StreamSource ss = (StreamSource)source;
            boolean dtdValidation = options.getDTDValidationMode() == 1;
            Source ps = Version.platform.getParserSource(pipe, ss, schemaValidation, dtdValidation);
            if (ps == ss) {
                String url = source.getSystemId();
                InputSource is = new InputSource(url);
                is.setCharacterStream(ss.getReader());
                is.setByteStream(ss.getInputStream());
                boolean reuseParser = false;
                XMLReader parser = options.obtainXMLReader();
                if (parser == null) {
                    parser = config.getSourceParser();
                    if (options.getEntityResolver() != null && parser.getEntityResolver() == null) {
                        parser.setEntityResolver(options.getEntityResolver());
                    }
                    reuseParser = true;
                }
                SAXSource sax = new SAXSource(parser, is);
                sax.setSystemId(source.getSystemId());
                Sender.sendSAXSource(sax, next, options);
                if (reuseParser) {
                    config.reuseSourceParser(parser);
                }
            } else {
                Sender.send(ps, next, options);
            }
            return;
        }
        if (source instanceof StAXSource) {
            XMLStreamReader reader = ((StAXSource)source).getXMLStreamReader();
            if (reader == null) {
                throw new XPathException("Saxon can only handle a StAXSource that wraps an XMLStreamReader");
            }
            StaxBridge bridge = new StaxBridge();
            bridge.setXMLStreamReader(reader);
            Sender.sendPullSource(new PullSource(bridge), next, options);
            return;
        }
        if (source instanceof SaplingDocument) {
            ((SaplingDocument)source).sendTo(next);
            return;
        }
        next = Sender.makeValidator(next, source.getSystemId(), options);
        Source newSource = config.getSourceResolver().resolveSource(source, config);
        if (newSource instanceof StreamSource || newSource instanceof SAXSource || newSource instanceof NodeInfo || newSource instanceof PullSource || newSource instanceof AugmentedSource || newSource instanceof EventSource) {
            Sender.send(newSource, next, options);
            return;
        }
        List<ExternalObjectModel> externalObjectModels = config.getExternalObjectModels();
        for (ExternalObjectModel externalObjectModel : externalObjectModels) {
            ExternalObjectModel model = externalObjectModel;
            boolean done = model.sendSource(source, next);
            if (!done) continue;
            return;
        }
        throw new XPathException("A source of type " + source.getClass().getName() + " is not supported in this environment");
    }

    private static void sendDocumentInfo(NodeInfo top, Receiver receiver, Location location) throws XPathException {
        PipelineConfiguration pipe = receiver.getPipelineConfiguration();
        NamePool targetNamePool = pipe.getConfiguration().getNamePool();
        if (top.getConfiguration().getNamePool() != targetNamePool) {
            receiver = new NamePoolConverter(receiver, top.getConfiguration().getNamePool(), targetNamePool);
        }
        LocationCopier copier = new LocationCopier(top.getNodeKind() == 9);
        pipe.setComponent(CopyInformee.class.getName(), copier);
        receiver.open();
        switch (top.getNodeKind()) {
            case 9: {
                top.copy(receiver, 6, location);
                break;
            }
            case 1: {
                receiver.startDocument(0);
                top.copy(receiver, 6, location);
                receiver.endDocument();
                break;
            }
            default: {
                throw new IllegalArgumentException("Expected document or element node");
            }
        }
        receiver.close();
    }

    /*
     * WARNING - void declaration
     */
    private static void sendSAXSource(SAXSource source, Receiver receiver, ParseOptions options) throws XPathException {
        void var13_23;
        boolean xInclude;
        ErrorHandler errorHandler;
        PipelineConfiguration pipe = receiver.getPipelineConfiguration();
        XMLReader parser = source.getXMLReader();
        boolean reuseParser = false;
        Configuration config = pipe.getConfiguration();
        ErrorReporter listener = options.getErrorReporter();
        if (listener == null) {
            listener = pipe.getErrorReporter();
        }
        if ((errorHandler = options.getErrorHandler()) == null) {
            errorHandler = new StandardErrorHandler(listener);
        }
        if (parser == null) {
            parser = options.obtainXMLReader();
        }
        if (parser == null) {
            SAXSource ss = new SAXSource();
            ss.setInputSource(source.getInputSource());
            ss.setSystemId(source.getSystemId());
            parser = config.getSourceParser();
            parser.setErrorHandler(errorHandler);
            if (options.getEntityResolver() != null && parser.getEntityResolver() == null) {
                parser.setEntityResolver(options.getEntityResolver());
            }
            ss.setXMLReader(parser);
            source = ss;
            reuseParser = true;
        } else {
            Sender.configureParser(parser);
            if (parser.getErrorHandler() == null) {
                parser.setErrorHandler(errorHandler);
            }
        }
        if (!pipe.getParseOptions().isExpandAttributeDefaults()) {
            try {
                parser.setFeature("http://xml.org/sax/features/use-attributes2", true);
            } catch (SAXNotRecognizedException | SAXNotSupportedException ss) {
                // empty catch block
            }
        }
        boolean dtdRecover = options.getDTDValidationMode() == 2;
        Map<String, Boolean> parserFeatures = options.getParserFeatures();
        Map<String, Object> parserProperties = options.getParserProperties();
        if (parserFeatures != null) {
            for (Map.Entry<String, Object> entry : parserFeatures.entrySet()) {
                try {
                    String name = entry.getKey();
                    boolean value = (Boolean)entry.getValue();
                    if (name.equals("http://apache.org/xml/features/xinclude")) {
                        boolean tryAgain = false;
                        try {
                            parser.setFeature(name, value);
                        } catch (SAXNotRecognizedException | SAXNotSupportedException err) {
                            tryAgain = true;
                        }
                        if (!tryAgain) continue;
                        try {
                            parser.setFeature(name + "-aware", value);
                            continue;
                        } catch (SAXNotRecognizedException err) {
                            throw new XPathException(Sender.namedParser(parser) + " does not recognize request for XInclude processing", err);
                        } catch (SAXNotSupportedException err) {
                            throw new XPathException(Sender.namedParser(parser) + " does not support XInclude processing", err);
                        }
                    }
                    parser.setFeature(entry.getKey(), (Boolean)entry.getValue());
                } catch (SAXNotRecognizedException err) {
                    if (!((Boolean)entry.getValue()).booleanValue()) continue;
                    config.getLogger().warning(Sender.namedParser(parser) + " does not recognize the feature " + entry.getKey());
                } catch (SAXNotSupportedException err) {
                    if (!((Boolean)entry.getValue()).booleanValue()) continue;
                    config.getLogger().warning(Sender.namedParser(parser) + " does not support the feature " + entry.getKey());
                }
            }
        }
        if (parserProperties != null) {
            for (Map.Entry<String, Object> entry : parserProperties.entrySet()) {
                try {
                    parser.setProperty(entry.getKey(), entry.getValue());
                } catch (SAXNotRecognizedException err) {
                    config.getLogger().warning(Sender.namedParser(parser) + " does not recognize the property " + entry.getKey());
                } catch (SAXNotSupportedException err) {
                    config.getLogger().warning(Sender.namedParser(parser) + " does not support the property " + entry.getKey());
                }
            }
        }
        if (xInclude = options.isXIncludeAware()) {
            boolean bl;
            boolean bl2 = false;
            try {
                parser.setFeature("http://apache.org/xml/features/xinclude-aware", true);
            } catch (SAXNotRecognizedException | SAXNotSupportedException err) {
                bl = true;
            }
            if (bl) {
                try {
                    parser.setFeature("http://apache.org/xml/features/xinclude", true);
                } catch (SAXNotRecognizedException err) {
                    throw new XPathException(Sender.namedParser(parser) + " does not recognize request for XInclude processing", err);
                } catch (SAXNotSupportedException err) {
                    throw new XPathException(Sender.namedParser(parser) + " does not support XInclude processing", err);
                }
            }
        }
        receiver = Sender.makeValidator(receiver, source.getSystemId(), options);
        ContentHandler ch = parser.getContentHandler();
        if (ch instanceof ReceivingContentHandler && config.isCompatible(((ReceivingContentHandler)ch).getConfiguration())) {
            ReceivingContentHandler receivingContentHandler = (ReceivingContentHandler)ch;
            receivingContentHandler.reset();
        } else {
            ReceivingContentHandler receivingContentHandler = new ReceivingContentHandler();
            parser.setContentHandler(receivingContentHandler);
            parser.setDTDHandler(receivingContentHandler);
            try {
                parser.setProperty("http://xml.org/sax/properties/lexical-handler", receivingContentHandler);
            } catch (SAXNotRecognizedException | SAXNotSupportedException value) {
                // empty catch block
            }
        }
        var13_23.setReceiver(receiver);
        var13_23.setPipelineConfiguration(pipe);
        try {
            parser.parse(source.getInputSource());
        } catch (SAXException err) {
            Exception nested = err.getException();
            if (nested instanceof XPathException) {
                throw (XPathException)nested;
            }
            if (nested instanceof RuntimeException) {
                throw (RuntimeException)nested;
            }
            if (errorHandler instanceof StandardErrorHandler && ((StandardErrorHandler)errorHandler).getFatalErrorCount() == 0 || err instanceof SAXParseException && ((SAXParseException)err).getSystemId() == null && source.getSystemId() != null) {
                XPathException de = new XPathException("Error reported by XML parser processing " + source.getSystemId() + ": " + err.getMessage(), err);
                listener.report(new XmlProcessingException(de));
                de.setHasBeenReported(true);
                throw de;
            }
            XPathException de = new XPathException(err);
            de.setErrorCode("SXXP0003");
            de.setHasBeenReported(true);
            throw de;
        } catch (IOException err) {
            throw new XPathException("I/O error reported by XML parser processing " + source.getSystemId() + ": " + err.getMessage(), err);
        }
        if (errorHandler instanceof StandardErrorHandler) {
            int errs = ((StandardErrorHandler)errorHandler).getFatalErrorCount();
            if (errs > 0) {
                throw new XPathException("The XML parser reported " + errs + (errs == 1 ? " error" : " errors"));
            }
            errs = ((StandardErrorHandler)errorHandler).getErrorCount();
            if (errs > 0) {
                String message = "The XML parser reported " + new Numberer_en().toWords(errs).toLowerCase() + " validation error" + (errs == 1 ? "" : "s");
                if (dtdRecover) {
                    message = message + ". Processing continues, because recovery from validation errors was requested";
                    XmlProcessingIncident warning = new XmlProcessingIncident(message).asWarning();
                    listener.report(warning);
                } else {
                    throw new XPathException(message);
                }
            }
        }
        if (reuseParser) {
            config.reuseSourceParser(parser);
        }
    }

    private static String namedParser(XMLReader parser) {
        return "Selected XML parser " + parser.getClass().getName();
    }

    private static Receiver makeValidator(Receiver receiver, String systemId, ParseOptions options) throws XPathException {
        Controller controller;
        PipelineConfiguration pipe = receiver.getPipelineConfiguration();
        Configuration config = pipe.getConfiguration();
        int sv = options.getSchemaValidationMode();
        if (sv != 3 && sv != 0 && (controller = pipe.getController()) != null && !controller.getExecutable().isSchemaAware() && sv != 4) {
            throw new XPathException("Cannot use schema-validated input documents when the query/stylesheet is not schema-aware");
        }
        return receiver;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void sendPullSource(PullSource source, Receiver receiver, ParseOptions options) throws XPathException {
        PipelineConfiguration pipe = receiver.getPipelineConfiguration();
        boolean xInclude = options.isXIncludeAware();
        if (xInclude) {
            throw new XPathException("XInclude processing is not supported with a pull parser");
        }
        receiver = Sender.makeValidator(receiver, source.getSystemId(), options);
        PullProvider provider = source.getPullProvider();
        provider.setPipelineConfiguration(pipe);
        receiver.setPipelineConfiguration(pipe);
        PullPushCopier copier = new PullPushCopier(provider, receiver);
        try {
            copier.copy();
        } finally {
            if (options.isPleaseCloseAfterUse()) {
                provider.close();
            }
        }
    }

    public static void configureParser(XMLReader parser) throws XPathException {
        try {
            parser.setFeature("http://xml.org/sax/features/namespaces", true);
        } catch (SAXNotSupportedException err) {
            throw new XPathException("The SAX2 parser " + parser.getClass().getName() + " does not recognize the 'namespaces' feature", err);
        } catch (SAXNotRecognizedException err) {
            throw new XPathException("The SAX2 parser " + parser.getClass().getName() + " does not support setting the 'namespaces' feature to true", err);
        }
        try {
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        } catch (SAXNotSupportedException err) {
            throw new XPathException("The SAX2 parser " + parser.getClass().getName() + " does not recognize the 'namespace-prefixes' feature", err);
        } catch (SAXNotRecognizedException err) {
            throw new XPathException("The SAX2 parser " + parser.getClass().getName() + " does not support setting the 'namespace-prefixes' feature to false", err);
        }
    }
}

