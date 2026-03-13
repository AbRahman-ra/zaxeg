/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.GlobalContextRequirement;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.QueryModule;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.serialize.CharacterMapIndex;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;

public class Executable {
    private Configuration config;
    private PackageData topLevelPackage;
    private List<PackageData> packages = new ArrayList<PackageData>();
    private Properties defaultOutputProperties;
    private CharacterMapIndex characterMapIndex;
    private HashMap<String, List<QueryModule>> queryLibraryModules;
    private HashSet<String> queryLocationHintsProcessed;
    private FunctionLibraryList functionLibrary;
    private HostLanguage hostLanguage = HostLanguage.XSLT;
    private Map<StructuredQName, GlobalParam> globalParams = new HashMap<StructuredQName, GlobalParam>();
    private HashMap<StructuredQName, Properties> outputDeclarations = null;
    private boolean createsSecondaryResult = false;
    protected boolean schemaAware = false;
    private GlobalContextRequirement globalContextRequirement = null;

    public Executable(Configuration config) {
        this.setConfiguration(config);
    }

    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public PackageData getTopLevelPackage() {
        return this.topLevelPackage;
    }

    public void setTopLevelPackage(PackageData topLevelPackage) {
        this.topLevelPackage = topLevelPackage;
    }

    public void addPackage(PackageData data) {
        this.packages.add(data);
    }

    public Iterable<PackageData> getPackages() {
        return this.packages;
    }

    public void setHostLanguage(HostLanguage language) {
        this.hostLanguage = language;
    }

    public HostLanguage getHostLanguage() {
        return this.hostLanguage;
    }

    public FunctionLibraryList getFunctionLibrary() {
        return this.functionLibrary;
    }

    public void setFunctionLibrary(FunctionLibraryList functionLibrary) {
        this.functionLibrary = functionLibrary;
    }

    public void setCharacterMapIndex(CharacterMapIndex cmi) {
        this.characterMapIndex = cmi;
    }

    public CharacterMapIndex getCharacterMapIndex() {
        if (this.characterMapIndex == null) {
            this.characterMapIndex = new CharacterMapIndex();
        }
        return this.characterMapIndex;
    }

    public void setDefaultOutputProperties(Properties properties) {
        this.defaultOutputProperties = properties;
    }

    public SerializationProperties getPrimarySerializationProperties() {
        if (this.defaultOutputProperties == null) {
            this.defaultOutputProperties = new Properties();
        }
        Properties props = this.defaultOutputProperties;
        return new SerializationProperties(props, this.getCharacterMapIndex());
    }

    public void setOutputProperties(StructuredQName qName, Properties properties) {
        if (this.outputDeclarations == null) {
            this.outputDeclarations = new HashMap(5);
        }
        this.outputDeclarations.put(qName, properties);
    }

    public Properties getOutputProperties() {
        return new Properties(this.defaultOutputProperties);
    }

    public Properties getOutputProperties(StructuredQName qName) {
        if (this.outputDeclarations == null) {
            return null;
        }
        return this.outputDeclarations.get(qName);
    }

    public void addQueryLibraryModule(QueryModule module) {
        String uri;
        List<QueryModule> existing;
        if (this.queryLibraryModules == null) {
            this.queryLibraryModules = new HashMap(5);
        }
        if ((existing = this.queryLibraryModules.get(uri = module.getModuleNamespace())) == null) {
            existing = new ArrayList<QueryModule>(5);
            existing.add(module);
            this.queryLibraryModules.put(uri, existing);
        } else if (!existing.contains(module)) {
            existing.add(module);
        }
    }

    public List<QueryModule> getQueryLibraryModules(String namespace) {
        if (this.queryLibraryModules == null) {
            return null;
        }
        return this.queryLibraryModules.get(namespace);
    }

    public QueryModule getQueryModuleWithSystemId(String systemId, QueryModule topModule) {
        if (systemId.equals(topModule.getSystemId())) {
            return topModule;
        }
        Iterator miter = this.getQueryLibraryModules();
        while (miter.hasNext()) {
            QueryModule sqc = (QueryModule)miter.next();
            String uri = sqc.getSystemId();
            if (uri == null || !uri.equals(systemId)) continue;
            return sqc;
        }
        return null;
    }

    public Iterator getQueryLibraryModules() {
        if (this.queryLibraryModules == null) {
            return Collections.EMPTY_LIST.iterator();
        }
        ArrayList<QueryModule> modules = new ArrayList<QueryModule>();
        for (List<QueryModule> queryModules : this.queryLibraryModules.values()) {
            modules.addAll(queryModules);
        }
        return modules.iterator();
    }

    public void addQueryLocationHintProcessed(String uri) {
        if (this.queryLocationHintsProcessed == null) {
            this.queryLocationHintsProcessed = new HashSet();
        }
        this.queryLocationHintsProcessed.add(uri);
    }

    public boolean isQueryLocationHintProcessed(String uri) {
        return this.queryLocationHintsProcessed != null && this.queryLocationHintsProcessed.contains(uri);
    }

    public void fixupQueryModules(QueryModule main) throws XPathException {
        main.bindUnboundVariables();
        if (this.queryLibraryModules != null) {
            for (List<QueryModule> queryModules : this.queryLibraryModules.values()) {
                for (QueryModule env : queryModules) {
                    env.bindUnboundVariables();
                }
            }
        }
        List<GlobalVariable> varDefinitions = main.fixupGlobalVariables(main.getGlobalStackFrameMap());
        main.bindUnboundFunctionCalls();
        if (this.queryLibraryModules != null) {
            for (List<QueryModule> queryModules : this.queryLibraryModules.values()) {
                for (QueryModule env : queryModules) {
                    env.bindUnboundFunctionCalls();
                }
            }
        }
        main.checkForCircularities(varDefinitions, main.getGlobalFunctionLibrary());
        main.fixupGlobalFunctions();
        main.typeCheckGlobalVariables(varDefinitions);
        main.optimizeGlobalFunctions();
    }

    public void explainGlobalVariables(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("globalVariables");
        for (PackageData pack : this.getPackages()) {
            for (GlobalVariable var : pack.getGlobalVariableList()) {
                var.export(presenter);
            }
        }
        presenter.endElement();
    }

    public void registerGlobalParameter(GlobalParam param) {
        this.globalParams.put(param.getVariableQName(), param);
    }

    public Map<StructuredQName, GlobalParam> getGlobalParameters() {
        return this.globalParams;
    }

    public GlobalParam getGlobalParameter(StructuredQName name) {
        return this.globalParams.get(name);
    }

    public void checkSuppliedParameters(GlobalParameterSet params) throws XPathException {
    }

    public void setCreatesSecondaryResult(boolean flag) {
        this.createsSecondaryResult = flag;
    }

    public boolean createsSecondaryResult() {
        return this.createsSecondaryResult;
    }

    public void setGlobalContextRequirement(GlobalContextRequirement requirement) {
        this.globalContextRequirement = requirement;
    }

    public GlobalContextRequirement getGlobalContextRequirement() {
        return this.globalContextRequirement;
    }

    public Item checkInitialContextItem(Item contextItem, XPathContext context) throws XPathException {
        block10: {
            TypeHierarchy th;
            block9: {
                if (this.globalContextRequirement == null) {
                    return contextItem;
                }
                if (contextItem != null && this.globalContextRequirement.isAbsentFocus()) {
                    throw new XPathException("The global context item is required to be absent", "XPDY0002");
                }
                th = this.config.getTypeHierarchy();
                if (contextItem != null) break block9;
                if (!this.globalContextRequirement.isMayBeOmitted()) {
                    throw new XPathException("A global context item is required, but none has been supplied", "XTDE3086");
                }
                if (this.globalContextRequirement.getDefaultValue() == null) break block10;
                try {
                    contextItem = this.globalContextRequirement.getDefaultValue().evaluateItem(context);
                } catch (XPathException e) {
                    if ("XPDY0002".equals(e.getErrorCodeLocalPart()) && !e.getMessage().contains("last()") && !e.getMessage().contains("position()")) {
                        e.setErrorCode("XQDY0054");
                    }
                    throw e;
                }
                if (contextItem == null) {
                    throw new XPathException("The context item cannot be initialized to an empty sequence", "XPTY0004");
                }
                for (ItemType type : this.globalContextRequirement.getRequiredItemTypes()) {
                    if (type.matches(contextItem, th)) continue;
                    RoleDiagnostic role = new RoleDiagnostic(20, "defaulted global context item", 0);
                    String s = role.composeErrorMessage(type, contextItem, th);
                    throw new XPathException(s, "XPTY0004");
                }
                break block10;
            }
            for (ItemType type : this.globalContextRequirement.getRequiredItemTypes()) {
                if (type.matches(contextItem, this.config.getTypeHierarchy())) continue;
                RoleDiagnostic role = new RoleDiagnostic(20, "supplied global context item", 0);
                String s = role.composeErrorMessage(type, contextItem, th);
                throw new XPathException(s, this.getHostLanguage() == HostLanguage.XSLT ? "XTTE0590" : "XPTY0004");
            }
        }
        return contextItem;
    }

    public void setSchemaAware(boolean aware) {
        this.schemaAware = aware;
    }

    public boolean isSchemaAware() {
        return this.schemaAware;
    }
}

