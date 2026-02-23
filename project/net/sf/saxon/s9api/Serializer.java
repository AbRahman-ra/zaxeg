/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.event.CloseNotifier;
import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceivingContentHandler;
import net.sf.saxon.event.Sender;
import net.sf.saxon.event.SequenceNormalizer;
import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.s9api.AbstractDestination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.serialize.CharacterMap;
import net.sf.saxon.serialize.CharacterMapIndex;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.ContentHandler;

public class Serializer
extends AbstractDestination {
    private Processor processor;
    private Map<StructuredQName, String> properties = new HashMap<StructuredQName, String>(10);
    private StreamResult result = new StreamResult();
    private CharacterMapIndex characterMap = null;
    private boolean mustClose = false;
    private static Map<String, Property> standardProperties = new HashMap<String, Property>();

    protected Serializer(Processor processor) {
        this.setProcessor(processor);
    }

    public void setProcessor(Processor processor) {
        this.processor = Objects.requireNonNull(processor);
    }

    public Processor getProcessor() {
        return this.processor;
    }

    public void setOutputProperties(Properties suppliedProperties) {
        for (String name : suppliedProperties.stringPropertyNames()) {
            this.properties.put(StructuredQName.fromClarkName(name), suppliedProperties.getProperty(name));
        }
    }

    public void setOutputProperties(SerializationProperties suppliedProperties) {
        this.setOutputProperties(suppliedProperties.getProperties());
        this.setCharacterMap(suppliedProperties.getCharacterMapIndex());
    }

    public void setCloseOnCompletion(boolean value) {
        this.mustClose = value;
    }

    public void setCharacterMap(CharacterMapIndex characterMap) {
        CharacterMapIndex existingIndex = this.characterMap;
        if (existingIndex == null || existingIndex.isEmpty()) {
            existingIndex = characterMap;
        } else if (characterMap != null && !characterMap.isEmpty() && existingIndex != characterMap) {
            existingIndex = existingIndex.copy();
            for (CharacterMap map : characterMap) {
                existingIndex.putCharacterMap(map.getName(), map);
            }
        }
        this.characterMap = existingIndex;
    }

    public void setOutputProperty(Property property, String value) {
        SerializerFactory sf = this.processor.getUnderlyingConfiguration().getSerializerFactory();
        try {
            value = sf.checkOutputProperty(property.toString(), value);
        } catch (XPathException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        if (value == null) {
            this.properties.remove(property.getQName().getStructuredQName());
        } else {
            this.properties.put(property.getQName().getStructuredQName(), value);
        }
    }

    public String getOutputProperty(Property property) {
        return this.properties.get(property.getQName().getStructuredQName());
    }

    public void setOutputProperty(QName property, String value) {
        SerializerFactory sf = this.processor.getUnderlyingConfiguration().getSerializerFactory();
        String uri = property.getNamespaceURI();
        if (uri.isEmpty() || uri.equals("http://saxon.sf.net/")) {
            try {
                value = sf.checkOutputProperty(property.getClarkName(), value);
            } catch (XPathException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            if (uri.equals("http://saxon.sf.net/") && property.getLocalName().equals("next-in-chain")) {
                throw new IllegalArgumentException("saxon:next-in-chain is not a valid serialization property");
            }
        }
        if (value == null) {
            this.properties.remove(property.getStructuredQName());
        } else {
            this.properties.put(property.getStructuredQName(), value);
        }
    }

    public String getOutputProperty(QName property) {
        return this.properties.get(property.getStructuredQName());
    }

    public void setOutputWriter(Writer writer) {
        this.result.setOutputStream(null);
        this.result.setSystemId((String)null);
        this.result.setWriter(writer);
        this.mustClose = false;
    }

    public void setOutputStream(OutputStream stream) {
        this.result.setWriter(null);
        this.result.setSystemId((String)null);
        this.result.setOutputStream(stream);
        this.mustClose = false;
    }

    public void setOutputFile(File file) {
        this.result.setOutputStream(null);
        this.result.setWriter(null);
        this.result.setSystemId(file);
        this.setDestinationBaseURI(file.toURI());
        this.mustClose = true;
    }

    public void serializeNode(XdmNode node) throws SaxonApiException {
        StreamResult res = this.result;
        if (res.getOutputStream() == null && res.getWriter() == null && res.getSystemId() == null) {
            throw new IllegalStateException("Either an outputStream, or a Writer, or a File must be supplied");
        }
        this.serializeNodeToResult(node, res);
    }

    public void serializeXdmValue(XdmValue value) throws SaxonApiException {
        if (value instanceof XdmNode) {
            this.serializeNode((XdmNode)value);
        } else {
            try {
                SerializationProperties properties = new SerializationProperties(this.getLocallyDefinedProperties(), this.characterMap);
                QueryResult.serializeSequence((SequenceIterator)value.getUnderlyingValue().iterate(), this.processor.getUnderlyingConfiguration(), (Result)this.result, properties);
            } catch (XPathException e) {
                throw new SaxonApiException(e);
            }
        }
        this.closeAndNotify();
    }

    public void serialize(Source source) throws SaxonApiException {
        try {
            SerializerFactory sf = this.processor.getUnderlyingConfiguration().getSerializerFactory();
            Receiver tr = sf.getReceiver(this.result, new SerializationProperties(this.getLocallyDefinedProperties()));
            Sender.send(source, tr, this.processor.getUnderlyingConfiguration().getParseOptions());
            this.closeAndNotify();
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public String serializeToString(Source source) throws SaxonApiException {
        try {
            SerializerFactory sf = this.processor.getUnderlyingConfiguration().getSerializerFactory();
            StringWriter sw = new StringWriter();
            Receiver tr = sf.getReceiver(new StreamResult(sw), new SerializationProperties(this.getLocallyDefinedProperties()));
            Sender.send(source, tr, this.processor.getUnderlyingConfiguration().getParseOptions());
            this.closeAndNotify();
            return sw.toString();
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public String serializeNodeToString(XdmNode node) throws SaxonApiException {
        StringWriter sw = new StringWriter();
        StreamResult sr = new StreamResult(sw);
        this.serializeNodeToResult(node, sr);
        return sw.toString();
    }

    private void serializeNodeToResult(XdmNode node, Result res) throws SaxonApiException {
        try {
            QueryResult.serialize(node.getUnderlyingNode(), res, this.getLocallyDefinedProperties());
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public StreamWriterToReceiver getXMLStreamWriter() throws SaxonApiException {
        PipelineConfiguration pipe = this.processor.getUnderlyingConfiguration().makePipelineConfiguration();
        Receiver r = this.getReceiver(pipe, this.getSerializationProperties());
        r = new NamespaceReducer(r);
        return new StreamWriterToReceiver(r);
    }

    public ContentHandler getContentHandler() throws SaxonApiException {
        PipelineConfiguration pipe = this.processor.getUnderlyingConfiguration().makePipelineConfiguration();
        Receiver r = this.getReceiver(pipe, this.getSerializationProperties());
        r = new NamespaceReducer(r);
        ReceivingContentHandler rch = new ReceivingContentHandler();
        rch.setReceiver(r);
        rch.setPipelineConfiguration(r.getPipelineConfiguration());
        return rch;
    }

    public Object getOutputDestination() {
        if (this.result.getOutputStream() != null) {
            return this.result.getOutputStream();
        }
        if (this.result.getWriter() != null) {
            return this.result.getWriter();
        }
        String systemId = this.result.getSystemId();
        if (systemId != null) {
            try {
                return new File(new URI(systemId));
            } catch (URISyntaxException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Receiver getReceiver(PipelineConfiguration pipe, SerializationProperties params) throws SaxonApiException {
        try {
            SerializerFactory sf = pipe.getConfiguration().getSerializerFactory();
            SerializationProperties mergedParams = this.getSerializationProperties().combineWith(params);
            Receiver target = sf.getReceiver((Result)this.result, mergedParams, pipe);
            if (this.helper.getListeners() != null) {
                if (target instanceof SequenceNormalizer) {
                    ((SequenceNormalizer)target).onClose(this.helper.getListeners());
                } else {
                    target = new CloseNotifier(target, this.helper.getListeners());
                }
            }
            if (target.getSystemId() == null && this.getDestinationBaseURI() != null) {
                target.setSystemId(this.getDestinationBaseURI().toASCIIString());
            }
            return target;
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public Properties getCombinedOutputProperties(Properties defaultOutputProperties) {
        Properties props = defaultOutputProperties == null ? new Properties() : new Properties(defaultOutputProperties);
        for (StructuredQName p : this.properties.keySet()) {
            String value = this.properties.get(p);
            props.setProperty(p.getClarkName(), value);
        }
        return props;
    }

    protected Properties getLocallyDefinedProperties() {
        Properties props = new Properties();
        for (StructuredQName p : this.properties.keySet()) {
            String value = this.properties.get(p);
            props.setProperty(p.getClarkName(), value);
        }
        return props;
    }

    public SerializationProperties getSerializationProperties() {
        return new SerializationProperties(this.getLocallyDefinedProperties(), this.characterMap);
    }

    protected Result getResult() {
        return this.result;
    }

    @Override
    public void close() throws SaxonApiException {
        if (this.mustClose) {
            Writer writer;
            OutputStream stream = this.result.getOutputStream();
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException err) {
                    throw new SaxonApiException("Failed while closing output file", err);
                }
            }
            if ((writer = this.result.getWriter()) != null) {
                try {
                    writer.close();
                } catch (IOException err) {
                    throw new SaxonApiException("Failed while closing output file", err);
                }
            }
        }
    }

    public static Property getProperty(QName name) {
        String clarkName = name.getClarkName();
        Property prop = standardProperties.get(clarkName);
        if (prop != null) {
            return prop;
        }
        throw new IllegalArgumentException("Unknown serialization property " + clarkName);
    }

    public boolean isMustCloseAfterUse() {
        return this.mustClose;
    }

    static {
        for (Property p : Property.values()) {
            standardProperties.put(p.name, p);
        }
    }

    public static enum Property {
        METHOD("method"),
        VERSION("version"),
        ENCODING("encoding"),
        OMIT_XML_DECLARATION("omit-xml-declaration"),
        STANDALONE("standalone"),
        DOCTYPE_PUBLIC("doctype-public"),
        DOCTYPE_SYSTEM("doctype-system"),
        CDATA_SECTION_ELEMENTS("cdata-section-elements"),
        INDENT("indent"),
        MEDIA_TYPE("media-type"),
        USE_CHARACTER_MAPS("use-character-maps"),
        INCLUDE_CONTENT_TYPE("include-content-type"),
        UNDECLARE_PREFIXES("undeclare-prefixes"),
        ESCAPE_URI_ATTRIBUTES("escape-uri-attributes"),
        BYTE_ORDER_MARK("byte-order-mark"),
        NORMALIZATION_FORM("normalization-form"),
        ITEM_SEPARATOR("item-separator"),
        HTML_VERSION("html-version"),
        BUILD_TREE("build-tree"),
        SAXON_INDENT_SPACES("{http://saxon.sf.net/}indent-spaces"),
        SAXON_LINE_LENGTH("{http://saxon.sf.net/}line-length"),
        SAXON_ATTRIBUTE_ORDER("{http://saxon.sf.net/}attribute-order"),
        SAXON_CANONICAL("{http://saxon.sf.net/}canonical"),
        SAXON_NEWLINE("{http://saxon.sf.net/}newline"),
        SAXON_SUPPRESS_INDENTATION("suppress-indentation"),
        SAXON_DOUBLE_SPACE("{http://saxon.sf.net/}double-space"),
        SAXON_STYLESHEET_VERSION("{http://saxon.sf.net/}stylesheet-version"),
        SAXON_CHARACTER_REPRESENTATION("{http://saxon.sf.net/}character-representation"),
        SAXON_RECOGNIZE_BINARY("{http://saxon.sf.net/}recognize-binary"),
        SAXON_REQUIRE_WELL_FORMED("{http://saxon.sf.net/}require-well-formed"),
        SAXON_WRAP("{http://saxon.sf.net/}wrap-result-sequence"),
        SAXON_SUPPLY_SOURCE_LOCATOR("{http://saxon.sf.net/}supply-source-locator");

        private String name;

        private Property(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public QName getQName() {
            return QName.fromClarkName(this.name);
        }

        public static Property get(String s) {
            for (Property p : Property.values()) {
                if (!p.name.equals(s)) continue;
                return p;
            }
            return null;
        }
    }
}

