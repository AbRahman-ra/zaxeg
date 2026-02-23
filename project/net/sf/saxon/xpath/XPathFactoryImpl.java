/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.xpath;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.Version;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.xpath.XPathEvaluator;

public class XPathFactoryImpl
extends XPathFactory
implements Configuration.ApiProvider {
    private Configuration config;
    private XPathVariableResolver variableResolver;
    private XPathFunctionResolver functionResolver;
    private static String FEATURE_SECURE_PROCESSING = "http://javax.xml.XMLConstants/feature/secure-processing";

    public XPathFactoryImpl() {
        this.config = Configuration.newConfiguration();
        this.setConfiguration(this.config);
        Version.platform.registerAllBuiltInObjectModels(this.config);
    }

    public XPathFactoryImpl(Configuration config) {
        this.config = config;
        config.setProcessor(this);
    }

    public void setConfiguration(Configuration config) {
        this.config = config;
        config.setProcessor(this);
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    @Override
    public boolean isObjectModelSupported(String model) {
        boolean debug = System.getProperty("jaxp.debug") != null;
        boolean result = this.silentIsObjectModelSupported(model);
        if (debug) {
            System.err.println("JAXP: Calling " + this.getClass().getName() + ".isObjectModelSupported(\"" + model + "\")");
            System.err.println("JAXP: -- returning " + (result ? "true" : "false (check all required libraries are on the class path"));
        }
        return result;
    }

    private boolean silentIsObjectModelSupported(String model) {
        return model.equals("http://saxon.sf.net/jaxp/xpath/om") || this.config.getExternalObjectModel(model) != null;
    }

    @Override
    public void setFeature(String feature, boolean b) throws XPathFactoryConfigurationException {
        if (feature.equals(FEATURE_SECURE_PROCESSING)) {
            this.config.setBooleanProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, !b);
        } else if (feature.equals("http://saxon.sf.net/feature/schema-validation")) {
            this.config.setSchemaValidationMode(b ? 1 : 4);
        } else {
            try {
                this.config.setBooleanProperty(feature, b);
            } catch (IllegalArgumentException err) {
                throw new XPathFactoryConfigurationException("Unknown or non-boolean feature: " + feature);
            }
        }
    }

    @Override
    public boolean getFeature(String feature) throws XPathFactoryConfigurationException {
        if (feature.equals(FEATURE_SECURE_PROCESSING)) {
            return !this.config.getBooleanProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS);
        }
        if (feature.equals("http://saxon.sf.net/feature/schema-validation")) {
            return this.config.getSchemaValidationMode() == 1;
        }
        try {
            Object o = this.config.getConfigurationProperty(feature);
            if (o instanceof Boolean) {
                return (Boolean)o;
            }
            throw new XPathFactoryConfigurationException("Configuration property " + feature + " is not a boolean (it is an instance of " + o.getClass() + ")");
        } catch (IllegalArgumentException e) {
            throw new XPathFactoryConfigurationException("Unknown feature: " + feature);
        }
    }

    @Override
    public void setXPathVariableResolver(XPathVariableResolver xPathVariableResolver) {
        this.variableResolver = xPathVariableResolver;
    }

    @Override
    public void setXPathFunctionResolver(XPathFunctionResolver xPathFunctionResolver) {
        this.functionResolver = xPathFunctionResolver;
    }

    @Override
    public XPath newXPath() {
        XPathEvaluator xpath = new XPathEvaluator(this.config);
        xpath.setXPathFunctionResolver(this.functionResolver);
        xpath.setXPathVariableResolver(this.variableResolver);
        return xpath;
    }
}

