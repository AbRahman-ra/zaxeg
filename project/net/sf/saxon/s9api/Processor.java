/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Objects;
import javax.xml.transform.Source;
import net.sf.saxon.Configuration;
import net.sf.saxon.Version;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.PushToReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.sort.RuleBasedSubstringMatcher;
import net.sf.saxon.expr.sort.SimpleCollation;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.ExtensionFunction;
import net.sf.saxon.s9api.Push;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SchemaManager;
import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;

public class Processor
implements Configuration.ApiProvider {
    private Configuration config;
    private SchemaManager schemaManager;

    public Processor(boolean licensedEdition) {
        if (licensedEdition) {
            this.config = Configuration.newConfiguration();
            if (this.config.getEditionCode().equals("EE")) {
                this.schemaManager = this.makeSchemaManager();
            }
        } else {
            this.config = new Configuration();
        }
        this.config.setProcessor(this);
    }

    public Processor(Configuration config) {
        this.config = config;
        if (config.getEditionCode().equals("EE")) {
            this.schemaManager = this.makeSchemaManager();
        }
    }

    public Processor(Source source) throws SaxonApiException {
        try {
            this.config = Configuration.readConfiguration(source);
            this.schemaManager = this.makeSchemaManager();
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
        this.config.setProcessor(this);
    }

    public DocumentBuilder newDocumentBuilder() {
        return new DocumentBuilder(this.config);
    }

    public XPathCompiler newXPathCompiler() {
        return new XPathCompiler(this);
    }

    public XsltCompiler newXsltCompiler() {
        return new XsltCompiler(this);
    }

    public XQueryCompiler newXQueryCompiler() {
        return new XQueryCompiler(this);
    }

    public Serializer newSerializer() {
        return new Serializer(this);
    }

    public Serializer newSerializer(OutputStream stream) {
        Serializer s = new Serializer(this);
        s.setOutputStream(stream);
        return s;
    }

    public Serializer newSerializer(Writer writer) {
        Serializer s = new Serializer(this);
        s.setOutputWriter(writer);
        return s;
    }

    public Serializer newSerializer(File file) {
        Serializer s = new Serializer(this);
        s.setOutputFile(file);
        return s;
    }

    public Push newPush(Destination destination) throws SaxonApiException {
        PipelineConfiguration pipe = this.getUnderlyingConfiguration().makePipelineConfiguration();
        SerializationProperties props = new SerializationProperties();
        return new PushToReceiver(destination.getReceiver(pipe, props));
    }

    public void registerExtensionFunction(ExtensionFunction function) {
        ExtensionFunctionDefinitionWrapper wrapper = new ExtensionFunctionDefinitionWrapper(function);
        this.registerExtensionFunction(wrapper);
    }

    public void registerExtensionFunction(ExtensionFunctionDefinition function) {
        try {
            this.config.registerExtensionFunction(function);
        } catch (Exception err) {
            throw new IllegalArgumentException(err);
        }
    }

    public SchemaManager getSchemaManager() {
        return this.schemaManager;
    }

    public boolean isSchemaAware() {
        return this.config.isLicensedFeature(1);
    }

    public String getSaxonProductVersion() {
        return Version.getProductVersion();
    }

    public String getSaxonEdition() {
        return this.config.getEditionCode();
    }

    public void setXmlVersion(String version) {
        switch (version) {
            case "1.0": {
                this.config.setXMLVersion(10);
                break;
            }
            case "1.1": {
                this.config.setXMLVersion(11);
                break;
            }
            default: {
                throw new IllegalArgumentException("XmlVersion");
            }
        }
    }

    public String getXmlVersion() {
        if (this.config.getXMLVersion() == 10) {
            return "1.0";
        }
        return "1.1";
    }

    public void setConfigurationProperty(String name, Object value) {
        if (name.equals("http://saxon.sf.net/feature/configuration")) {
            this.config = (Configuration)value;
        } else {
            this.config.setConfigurationProperty(name, value);
        }
    }

    public Object getConfigurationProperty(String name) {
        return this.config.getConfigurationProperty(name);
    }

    public <T> void setConfigurationProperty(Feature<T> feature, T value) {
        if (feature == Feature.CONFIGURATION) {
            this.config = (Configuration)value;
        } else {
            this.config.setConfigurationProperty(feature, value);
        }
    }

    public <T> T getConfigurationProperty(Feature<T> feature) {
        return this.config.getConfigurationProperty(feature);
    }

    public void declareCollation(String uri, Comparator collation) {
        if (uri.equals("http://www.w3.org/2005/xpath-functions/collation/codepoint")) {
            throw new IllegalArgumentException("Cannot redeclare the Unicode codepoint collation URI");
        }
        if (uri.equals("http://www.w3.org/2005/xpath-functions/collation/html-ascii-case-insensitive")) {
            throw new IllegalArgumentException("Cannot redeclare the HTML5 caseblind collation URI");
        }
        SimpleCollation saxonCollation = collation instanceof RuleBasedCollator ? new RuleBasedSubstringMatcher(uri, (RuleBasedCollator)collation) : new SimpleCollation(uri, collation);
        this.config.registerCollation(uri, saxonCollation);
    }

    public Configuration getUnderlyingConfiguration() {
        return this.config;
    }

    public void writeXdmValue(XdmValue value, Destination destination) throws SaxonApiException {
        Objects.requireNonNull(value);
        Objects.requireNonNull(destination);
        try {
            if (destination instanceof Serializer) {
                ((Serializer)destination).serializeXdmValue(value);
            } else {
                Receiver out = destination.getReceiver(this.config.makePipelineConfiguration(), this.config.obtainDefaultSerializationProperties());
                ComplexContentOutputter tree = new ComplexContentOutputter(out);
                tree.open();
                tree.startDocument(0);
                for (XdmItem item : value) {
                    tree.append(item.getUnderlyingValue(), Loc.NONE, 524288);
                }
                tree.endDocument();
                tree.close();
                destination.closeAndNotify();
            }
        } catch (XPathException err) {
            throw new SaxonApiException(err);
        }
    }

    private SchemaManager makeSchemaManager() {
        SchemaManager manager = null;
        return manager;
    }

    private static class ExtensionFunctionDefinitionWrapper
    extends ExtensionFunctionDefinition {
        private ExtensionFunction function;

        public ExtensionFunctionDefinitionWrapper(ExtensionFunction function) {
            this.function = function;
        }

        @Override
        public StructuredQName getFunctionQName() {
            return this.function.getName().getStructuredQName();
        }

        @Override
        public int getMinimumNumberOfArguments() {
            return this.function.getArgumentTypes().length;
        }

        @Override
        public int getMaximumNumberOfArguments() {
            return this.function.getArgumentTypes().length;
        }

        @Override
        public net.sf.saxon.value.SequenceType[] getArgumentTypes() {
            SequenceType[] declaredArgs = this.function.getArgumentTypes();
            net.sf.saxon.value.SequenceType[] types = new net.sf.saxon.value.SequenceType[declaredArgs.length];
            for (int i = 0; i < declaredArgs.length; ++i) {
                types[i] = net.sf.saxon.value.SequenceType.makeSequenceType(declaredArgs[i].getItemType().getUnderlyingItemType(), declaredArgs[i].getOccurrenceIndicator().getCardinality());
            }
            return types;
        }

        @Override
        public net.sf.saxon.value.SequenceType getResultType(net.sf.saxon.value.SequenceType[] suppliedArgumentTypes) {
            SequenceType declaredResult = this.function.getResultType();
            return net.sf.saxon.value.SequenceType.makeSequenceType(declaredResult.getItemType().getUnderlyingItemType(), declaredResult.getOccurrenceIndicator().getCardinality());
        }

        @Override
        public boolean trustResultType() {
            return false;
        }

        @Override
        public boolean dependsOnFocus() {
            return false;
        }

        @Override
        public boolean hasSideEffects() {
            return false;
        }

        @Override
        public ExtensionFunctionCall makeCallExpression() {
            return new ExtensionFunctionCall(){

                @Override
                public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
                    XdmValue[] args = new XdmValue[arguments.length];
                    for (int i = 0; i < args.length; ++i) {
                        GroundedValue val = arguments[i].materialize();
                        args[i] = XdmValue.wrap(val);
                    }
                    try {
                        XdmValue result = function.call(args);
                        return result.getUnderlyingValue();
                    } catch (SaxonApiException e) {
                        throw new XPathException(e);
                    }
                }
            };
        }
    }
}

