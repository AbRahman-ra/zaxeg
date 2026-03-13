/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.functions.ExecutableFunctionLibrary;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.VisibilityProvenance;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.trans.rules.RuleManager;

public class PreparedStylesheet
extends Executable {
    private HashMap<URI, PreparedStylesheet> nextStylesheetCache;
    private RuleManager ruleManager;
    private HashMap<StructuredQName, NamedTemplate> namedTemplateTable;
    private Map<SymbolicName, Component> componentIndex;
    private StructuredQName defaultInitialTemplate;
    private StructuredQName defaultInitialMode;
    private String messageReceiverClassName;
    private OutputURIResolver outputURIResolver;
    private GlobalParameterSet compileTimeParams;

    public PreparedStylesheet(Compilation compilation) {
        super(compilation.getConfiguration());
        CompilerInfo compilerInfo = compilation.getCompilerInfo();
        this.setHostLanguage(HostLanguage.XSLT);
        if (compilerInfo.isSchemaAware()) {
            int localLic = compilation.getPackageData().getLocalLicenseId();
            this.getConfiguration().checkLicensedFeature(2, "schema-aware XSLT", localLic);
            this.schemaAware = true;
        }
        this.defaultInitialMode = compilerInfo.getDefaultInitialMode();
        this.defaultInitialTemplate = compilerInfo.getDefaultInitialTemplate();
        this.messageReceiverClassName = compilerInfo.getMessageReceiverClassName();
        this.outputURIResolver = compilerInfo.getOutputURIResolver();
        this.compileTimeParams = compilation.getParameters();
    }

    public XsltController newController() {
        Configuration config = this.getConfiguration();
        XsltController c = new XsltController(config, this);
        c.setMessageReceiverClassName(this.messageReceiverClassName);
        c.setOutputURIResolver(this.outputURIResolver);
        if (this.defaultInitialMode != null) {
            try {
                c.setInitialMode(this.defaultInitialMode);
            } catch (XPathException xPathException) {
                // empty catch block
            }
        }
        return c;
    }

    public GlobalParameterSet getCompileTimeParams() {
        return this.compileTimeParams;
    }

    @Override
    public void checkSuppliedParameters(GlobalParameterSet params) throws XPathException {
        for (Map.Entry<StructuredQName, GlobalParam> entry : this.getGlobalParameters().entrySet()) {
            if (!entry.getValue().isRequiredParam()) continue;
            StructuredQName req = entry.getKey();
            if (this.getCompileTimeParams().get(req) != null || params != null && params.get(req) != null) continue;
            XPathException err = new XPathException("No value supplied for required parameter " + req.getDisplayName());
            err.setErrorCode(this.getHostLanguage() == HostLanguage.XQUERY ? "XPDY0002" : "XTDE0050");
            throw err;
        }
        for (StructuredQName name : params.getKeys()) {
            GlobalParam decl = this.getGlobalParameter(name);
            if (decl != null && decl.isStatic()) {
                throw new XPathException("Parameter $" + name.getDisplayName() + " cannot be supplied dynamically because it is declared as static");
            }
            if (!this.compileTimeParams.containsKey(name)) continue;
            throw new XPathException("Parameter $" + name.getDisplayName() + " cannot be supplied dynamically because a value was already supplied at compile time");
        }
        for (StructuredQName name : this.compileTimeParams.getKeys()) {
            params.put(name, this.compileTimeParams.get(name));
        }
    }

    @Override
    public StylesheetPackage getTopLevelPackage() {
        return (StylesheetPackage)super.getTopLevelPackage();
    }

    public void setRuleManager(RuleManager rm) {
        this.ruleManager = rm;
    }

    public RuleManager getRuleManager() {
        return this.ruleManager;
    }

    public void putNamedTemplate(StructuredQName templateName, NamedTemplate template) {
        if (this.namedTemplateTable == null) {
            this.namedTemplateTable = new HashMap(32);
        }
        this.namedTemplateTable.put(templateName, template);
    }

    public StructuredQName getDefaultInitialTemplateName() {
        return this.defaultInitialTemplate;
    }

    public void setComponentIndex(Map<SymbolicName, Component> index) {
        this.componentIndex = index;
    }

    public Component getComponent(SymbolicName name) {
        return this.componentIndex.get(name);
    }

    public boolean isEligibleInitialMode(Component.M component) {
        if (component == null) {
            return false;
        }
        if (component.getVisibility() == Visibility.PUBLIC || component.getVisibility() == Visibility.FINAL) {
            return true;
        }
        if (component.getActor().isUnnamedMode()) {
            return true;
        }
        StylesheetPackage top = this.getTopLevelPackage();
        if (component.getActor().getModeName().equals(top.getDefaultMode())) {
            return true;
        }
        return !top.isDeclaredModes() && !component.getActor().isEmpty() && (component.getVisibilityProvenance() == VisibilityProvenance.DEFAULTED || component.getVisibility() != Visibility.PRIVATE);
    }

    public void explainNamedTemplates(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("namedTemplates");
        if (this.namedTemplateTable != null) {
            for (NamedTemplate t : this.namedTemplateTable.values()) {
                presenter.startElement("template");
                presenter.emitAttribute("name", t.getTemplateName().getDisplayName());
                presenter.emitAttribute("line", t.getLineNumber() + "");
                presenter.emitAttribute("module", t.getSystemId());
                if (t.getBody() != null) {
                    t.getBody().export(presenter);
                }
                presenter.endElement();
            }
        }
        presenter.endElement();
    }

    public SerializationProperties getDeclaredSerializationProperties() {
        SerializationProperties details = this.getPrimarySerializationProperties();
        return new SerializationProperties(new Properties(details.getProperties()), this.getCharacterMapIndex());
    }

    public PreparedStylesheet getCachedStylesheet(String href, String baseURI) {
        URI abs = null;
        try {
            abs = ResolveURI.makeAbsolute(href, baseURI);
        } catch (URISyntaxException uRISyntaxException) {
            // empty catch block
        }
        PreparedStylesheet result = null;
        if (abs != null && this.nextStylesheetCache != null) {
            result = this.nextStylesheetCache.get(abs);
        }
        return result;
    }

    public void putCachedStylesheet(String href, String baseURI, PreparedStylesheet pss) {
        URI abs = null;
        try {
            abs = ResolveURI.makeAbsolute(href, baseURI);
        } catch (URISyntaxException uRISyntaxException) {
            // empty catch block
        }
        if (abs != null) {
            if (this.nextStylesheetCache == null) {
                this.nextStylesheetCache = new HashMap(4);
            }
            this.nextStylesheetCache.put(abs, pss);
        }
    }

    public void explain(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("stylesheet");
        presenter.namespace("fn", "http://www.w3.org/2005/xpath-functions");
        presenter.namespace("xs", "http://www.w3.org/2001/XMLSchema");
        this.explainGlobalVariables(presenter);
        this.ruleManager.explainTemplateRules(presenter);
        this.explainNamedTemplates(presenter);
        presenter.startElement("accumulators");
        for (Accumulator acc : this.getTopLevelPackage().getAccumulatorRegistry().getAllAccumulators()) {
            acc.export(presenter);
        }
        presenter.endElement();
        FunctionLibraryList libList = this.getFunctionLibrary();
        List<FunctionLibrary> libraryList = libList.getLibraryList();
        presenter.startElement("functions");
        for (FunctionLibrary lib : libraryList) {
            if (!(lib instanceof ExecutableFunctionLibrary)) continue;
            Iterator<UserFunction> f = ((ExecutableFunctionLibrary)lib).iterateFunctions();
            while (f.hasNext()) {
                UserFunction func = f.next();
                func.export(presenter);
            }
        }
        presenter.endElement();
        presenter.endElement();
        presenter.close();
    }
}

