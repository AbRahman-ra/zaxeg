/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.URIResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.SchemaURIResolver;
import net.sf.saxon.s9api.HostLanguage;

public class PipelineConfiguration {
    private Configuration config;
    private URIResolver uriResolver;
    private SchemaURIResolver schemaURIResolver;
    private Controller controller;
    private ParseOptions parseOptions;
    private HostLanguage hostLanguage = HostLanguage.XSLT;
    private Map<String, Object> components;
    private XPathContext context;

    public PipelineConfiguration(Configuration config) {
        this.config = config;
        this.parseOptions = new ParseOptions();
    }

    public PipelineConfiguration(PipelineConfiguration p) {
        this.config = p.config;
        this.uriResolver = p.uriResolver;
        this.schemaURIResolver = p.schemaURIResolver;
        this.controller = p.controller;
        this.parseOptions = new ParseOptions(p.parseOptions);
        this.hostLanguage = p.hostLanguage;
        if (p.components != null) {
            this.components = new HashMap<String, Object>(p.components);
        }
        this.context = p.context;
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    public ErrorReporter getErrorReporter() {
        ErrorReporter reporter = this.parseOptions.getErrorReporter();
        if (reporter == null) {
            reporter = this.config.makeErrorReporter();
        }
        return reporter;
    }

    public void setErrorReporter(ErrorReporter errorReporter) {
        this.parseOptions.setErrorReporter(errorReporter);
    }

    public URIResolver getURIResolver() {
        return this.uriResolver;
    }

    public void setURIResolver(URIResolver uriResolver) {
        this.uriResolver = uriResolver;
    }

    public SchemaURIResolver getSchemaURIResolver() {
        return this.schemaURIResolver;
    }

    public void setParseOptions(ParseOptions options) {
        this.parseOptions = options;
    }

    public ParseOptions getParseOptions() {
        return this.parseOptions;
    }

    public void setUseXsiSchemaLocation(boolean recognize) {
        this.parseOptions.setUseXsiSchemaLocation(recognize);
    }

    public void setRecoverFromValidationErrors(boolean recover) {
        this.parseOptions.setContinueAfterValidationErrors(recover);
    }

    public boolean isRecoverFromValidationErrors() {
        return this.parseOptions.isContinueAfterValidationErrors();
    }

    public void setSchemaURIResolver(SchemaURIResolver resolver) {
        this.schemaURIResolver = resolver;
    }

    public Controller getController() {
        return this.controller;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public HostLanguage getHostLanguage() {
        return this.hostLanguage;
    }

    public boolean isXSLT() {
        return this.hostLanguage == HostLanguage.XSLT;
    }

    public void setHostLanguage(HostLanguage language) {
        this.hostLanguage = language;
    }

    public void setExpandAttributeDefaults(boolean expand) {
        this.parseOptions.setExpandAttributeDefaults(expand);
    }

    public void setComponent(String name, Object value) {
        if (this.components == null) {
            this.components = new HashMap<String, Object>();
        }
        this.components.put(name, value);
    }

    public Object getComponent(String name) {
        if (this.components == null) {
            return null;
        }
        return this.components.get(name);
    }

    public void setXPathContext(XPathContext context) {
        this.context = context;
    }

    public XPathContext getXPathContext() {
        return this.context;
    }
}

