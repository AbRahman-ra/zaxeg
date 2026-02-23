/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.xml.transform.URIResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.accum.AccumulatorRegistry;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.AttributeSet;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.functions.DocumentFn;
import net.sf.saxon.functions.ExecutableFunctionLibrary;
import net.sf.saxon.om.Action;
import net.sf.saxon.om.DocumentKey;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.query.XQueryFunction;
import net.sf.saxon.query.XQueryFunctionLibrary;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.serialize.CharacterMap;
import net.sf.saxon.serialize.CharacterMapIndex;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.SourceBinding;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetComponent;
import net.sf.saxon.style.StylesheetModule;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.style.XSLAccept;
import net.sf.saxon.style.XSLApplyTemplates;
import net.sf.saxon.style.XSLAttributeSet;
import net.sf.saxon.style.XSLCharacterMap;
import net.sf.saxon.style.XSLExpose;
import net.sf.saxon.style.XSLFunction;
import net.sf.saxon.style.XSLGlobalParam;
import net.sf.saxon.style.XSLGlobalVariable;
import net.sf.saxon.style.XSLImportSchema;
import net.sf.saxon.style.XSLInclude;
import net.sf.saxon.style.XSLLocalParam;
import net.sf.saxon.style.XSLMode;
import net.sf.saxon.style.XSLModuleRoot;
import net.sf.saxon.style.XSLNamespaceAlias;
import net.sf.saxon.style.XSLOutput;
import net.sf.saxon.style.XSLOverride;
import net.sf.saxon.style.XSLPackage;
import net.sf.saxon.style.XSLStylesheet;
import net.sf.saxon.style.XSLTemplate;
import net.sf.saxon.style.XSLUsePackage;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.trans.ComponentTest;
import net.sf.saxon.trans.DecimalFormatManager;
import net.sf.saxon.trans.GlobalVariableManager;
import net.sf.saxon.trans.KeyDefinition;
import net.sf.saxon.trans.KeyDefinitionSet;
import net.sf.saxon.trans.KeyManager;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Timer;
import net.sf.saxon.trans.TypeAliasManager;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.VisibilityProvenance;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.RuleManager;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.linked.DocumentImpl;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.value.Whitespace;
import net.sf.saxon.z.IntHashMap;

public class PrincipalStylesheetModule
extends StylesheetModule
implements GlobalVariableManager {
    private StylesheetPackage stylesheetPackage;
    private boolean declaredModes;
    private HashMap<StructuredQName, ComponentDeclaration> globalVariableIndex = new HashMap(20);
    private HashMap<StructuredQName, ComponentDeclaration> templateIndex = new HashMap(20);
    private HashMap<SymbolicName, ComponentDeclaration> functionIndex = new HashMap(8);
    private KeyManager keyManager;
    private DecimalFormatManager decimalFormatManager;
    private RuleManager ruleManager;
    private AccumulatorRegistry accumulatorManager = null;
    private int numberOfAliases = 0;
    private List<ComponentDeclaration> namespaceAliasList = new ArrayList<ComponentDeclaration>(5);
    private HashMap<String, NamespaceBinding> namespaceAliasMap;
    private Set<String> aliasResultUriSet;
    private Map<StructuredQName, List<ComponentDeclaration>> attributeSetDeclarations = new HashMap<StructuredQName, List<ComponentDeclaration>>();
    private HashMap<DocumentKey, XSLModuleRoot> moduleCache = new HashMap(4);
    private TypeAliasManager typeAliasManager;
    private CharacterMapIndex characterMapIndex;
    private List<Action> fixupActions = new ArrayList<Action>();
    private boolean needsDynamicOutputProperties = false;

    public PrincipalStylesheetModule(XSLPackage sourceElement) throws XPathException {
        super(sourceElement, 0);
        this.declaredModes = sourceElement.isDeclaredModes();
        this.stylesheetPackage = this.getConfiguration().makeStylesheetPackage();
        CompilerInfo compilerInfo = sourceElement.getCompilation().getCompilerInfo();
        this.stylesheetPackage.setTargetEdition(compilerInfo.getTargetEdition());
        this.stylesheetPackage.setRelocatable(compilerInfo.isRelocatable());
        this.stylesheetPackage.setJustInTimeCompilation(compilerInfo.isJustInTimeCompilation());
        this.stylesheetPackage.setImplicitPackage(!sourceElement.getLocalPart().equals("package"));
        this.keyManager = this.stylesheetPackage.getKeyManager();
        this.decimalFormatManager = this.stylesheetPackage.getDecimalFormatManager();
        this.ruleManager = new RuleManager(this.stylesheetPackage, compilerInfo);
        this.ruleManager.getUnnamedMode().makeDeclaringComponent(Visibility.PRIVATE, this.stylesheetPackage);
        this.stylesheetPackage.setRuleManager(this.ruleManager);
        this.stylesheetPackage.setDeclaredModes(this.declaredModes);
        StructuredQName defaultMode = sourceElement.getDefaultMode();
        this.stylesheetPackage.setDefaultMode(sourceElement.getDefaultMode());
        if (defaultMode != null) {
            this.ruleManager.obtainMode(defaultMode, !this.declaredModes);
        }
        this.characterMapIndex = new CharacterMapIndex();
        this.stylesheetPackage.setCharacterMapIndex(this.characterMapIndex);
        this.typeAliasManager = this.getConfiguration().makeTypeAliasManager();
        this.stylesheetPackage.setTypeAliasManager(this.typeAliasManager);
        try {
            this.setInputTypeAnnotations(sourceElement.getInputTypeAnnotationsAttribute());
        } catch (XPathException xPathException) {
            // empty catch block
        }
    }

    public Component getComponent(SymbolicName name) {
        return this.stylesheetPackage.getComponentIndex().get(name);
    }

    @Override
    public PrincipalStylesheetModule getPrincipalStylesheetModule() {
        return this;
    }

    public StylesheetPackage getStylesheetPackage() {
        return this.stylesheetPackage;
    }

    public KeyManager getKeyManager() {
        return this.keyManager;
    }

    public DecimalFormatManager getDecimalFormatManager() {
        return this.decimalFormatManager;
    }

    public RuleManager getRuleManager() {
        return this.ruleManager;
    }

    public boolean isDeclaredModes() {
        return this.declaredModes;
    }

    public void addFixupAction(Action action) {
        this.fixupActions.add(action);
    }

    public void setNeedsDynamicOutputProperties(boolean b) {
        this.needsDynamicOutputProperties = b;
    }

    public CharacterMapIndex getCharacterMapIndex() {
        return this.characterMapIndex;
    }

    public TypeAliasManager getTypeAliasManager() {
        return this.typeAliasManager;
    }

    public void declareXQueryFunction(XQueryFunction function) throws XPathException {
        XQueryFunctionLibrary lib = this.getStylesheetPackage().getXQueryFunctionLibrary();
        if (this.getStylesheetPackage().getFunction(function.getUserFunction().getSymbolicName()) != null) {
            throw new XPathException("Duplication declaration of " + function.getUserFunction().getSymbolicName(), "XQST0034");
        }
        lib.declareFunction(function);
    }

    public void putStylesheetDocument(DocumentKey key, XSLStylesheet module) {
        this.moduleCache.put(key, module);
    }

    public XSLModuleRoot getStylesheetDocument(DocumentKey key) {
        XSLModuleRoot sheet = this.moduleCache.get(key);
        if (sheet != null) {
            XPathException warning = new XPathException("Stylesheet module " + key + " is included or imported more than once. This is permitted, but may lead to errors or unexpected behavior");
            sheet.issueWarning(warning);
        }
        return sheet;
    }

    public void preprocess(Compilation compilation) throws XPathException {
        ComponentDeclaration decl2;
        Timer timer = compilation.timer;
        this.spliceUsePackages((XSLPackage)this.getRootElement(), this.getRootElement().getCompilation());
        if (Compilation.TIMING) {
            timer.report("spliceIncludes");
        }
        this.importSchemata();
        if (Compilation.TIMING) {
            timer.report("importSchemata");
        }
        this.getTypeAliasManager().processAllDeclarations(this.topLevel);
        this.buildIndexes();
        if (Compilation.TIMING) {
            timer.report("buildIndexes");
        }
        this.checkForSchemaAwareness();
        if (Compilation.TIMING) {
            timer.report("checkForSchemaAwareness");
        }
        this.processAllAttributes();
        if (Compilation.TIMING) {
            timer.report("processAllAttributes");
        }
        this.collectNamespaceAliases();
        if (Compilation.TIMING) {
            timer.report("collectNamespaceAliases");
        }
        for (ComponentDeclaration decl2 : this.topLevel) {
            StyleElement inst = decl2.getSourceElement();
            if (inst.isActionCompleted(16)) continue;
            inst.setActionCompleted(16);
            inst.fixupReferences();
        }
        if (Compilation.TIMING) {
            timer.report("fixupReferences");
        }
        XSLPackage top = (XSLPackage)this.getStylesheetElement();
        decl2 = new ComponentDeclaration(this, top);
        if (!top.isActionCompleted(1)) {
            top.setActionCompleted(1);
            top.validate(null);
            for (ComponentDeclaration d : this.topLevel) {
                d.getSourceElement().validateSubtree(d, false);
            }
        }
        if (Compilation.TIMING) {
            timer.report("validate");
        }
        Properties props = this.gatherOutputProperties(null);
        props.setProperty("{http://saxon.sf.net/}stylesheet-version", this.getStylesheetPackage().getVersion() + "");
        this.getStylesheetPackage().setDefaultOutputProperties(props);
        HashSet<StructuredQName> outputNames = new HashSet<StructuredQName>(5);
        for (ComponentDeclaration outputDecl : this.topLevel) {
            XSLOutput out;
            StructuredQName qName;
            if (!(outputDecl.getSourceElement() instanceof XSLOutput) || (qName = (out = (XSLOutput)outputDecl.getSourceElement()).getFormatQName()) == null) continue;
            outputNames.add(qName);
        }
        if (outputNames.isEmpty()) {
            if (this.needsDynamicOutputProperties) {
                throw new XPathException("The stylesheet contains xsl:result-document instructions that calculate the output format name at run-time, but there are no named xsl:output declarations", "XTDE1460");
            }
        } else {
            for (StructuredQName qName : outputNames) {
                Properties oprops = this.gatherOutputProperties(qName);
                this.getStylesheetPackage().setNamedOutputProperties(qName, oprops);
            }
        }
        if (Compilation.TIMING) {
            timer.report("Register output formats");
        }
        for (ComponentDeclaration d : this.topLevel) {
            StyleElement inst = d.getSourceElement();
            if (!(inst instanceof XSLCharacterMap)) continue;
            XSLCharacterMap xcm = (XSLCharacterMap)inst;
            StructuredQName qn = xcm.getCharacterMapName();
            IntHashMap<String> map = new IntHashMap<String>();
            xcm.assemble(map);
            this.characterMapIndex.putCharacterMap(xcm.getCharacterMapName(), new CharacterMap(qn, map));
        }
        if (Compilation.TIMING) {
            timer.report("Index character maps");
        }
    }

    protected void spliceUsePackages(XSLPackage xslpackage, Compilation compilation) throws XPathException {
        ArrayList<XSLUsePackage> useDeclarations = new ArrayList<XSLUsePackage>();
        PrincipalStylesheetModule.gatherUsePackageDeclarations(compilation, xslpackage, useDeclarations);
        HashSet<SymbolicName> overrides = new HashSet<SymbolicName>();
        for (XSLUsePackage use : useDeclarations) {
            this.gatherOverridingDeclarations(use, compilation, overrides);
        }
        StylesheetPackage thisPackage = this.getStylesheetPackage();
        for (XSLUsePackage use : useDeclarations) {
            List<XSLAccept> acceptors = use.getAcceptors();
            thisPackage.addComponentsFromUsedPackage(use.getUsedPackage(), acceptors, overrides);
        }
        for (XSLUsePackage use : useDeclarations) {
            use.gatherRuleOverrides(this, overrides);
        }
        this.spliceIncludes();
    }

    private static void gatherUsePackageDeclarations(Compilation compilation, StyleElement wrapper, List<XSLUsePackage> declarations) throws XPathException {
        for (NodeInfo nodeInfo : wrapper.children()) {
            if (nodeInfo instanceof XSLUsePackage) {
                declarations.add((XSLUsePackage)nodeInfo);
                continue;
            }
            if (!(nodeInfo instanceof XSLInclude)) continue;
            String href = Whitespace.trim(nodeInfo.getAttributeValue("", "href"));
            URIResolver resolver = compilation.getCompilerInfo().getURIResolver();
            DocumentKey key = DocumentFn.computeDocumentKey(href, nodeInfo.getBaseURI(), compilation.getPackageData(), resolver, false);
            TreeInfo includedTree = compilation.getStylesheetModules().get(key);
            StyleElement incWrapper = (StyleElement)((DocumentImpl)includedTree.getRootNode()).getDocumentElement();
            PrincipalStylesheetModule.gatherUsePackageDeclarations(compilation, incWrapper, declarations);
        }
    }

    private void gatherOverridingDeclarations(XSLUsePackage use, Compilation compilation, Set<SymbolicName> overrides) throws XPathException {
        use.findUsedPackage(compilation.getCompilerInfo());
        use.gatherNamedOverrides(this, this.topLevel, overrides);
    }

    protected void importSchemata() throws XPathException {
        for (int i = this.topLevel.size() - 1; i >= 0; --i) {
            ComponentDeclaration decl = (ComponentDeclaration)this.topLevel.get(i);
            if (!(decl.getSourceElement() instanceof XSLImportSchema)) continue;
            XPathException xe = new XPathException("xsl:import-schema requires Saxon-EE");
            xe.setErrorCode("XTSE1650");
            xe.setLocator(decl.getSourceElement());
            throw xe;
        }
    }

    private void buildIndexes() throws XPathException {
        for (int i = this.topLevel.size() - 1; i >= 0; --i) {
            ComponentDeclaration decl = (ComponentDeclaration)this.topLevel.get(i);
            decl.getSourceElement().index(decl, this);
        }
    }

    public void processAllAttributes() throws XPathException {
        this.getRootElement().processDefaultCollationAttribute();
        this.getRootElement().processDefaultMode();
        this.getRootElement().prepareAttributes();
        for (XSLModuleRoot xss : this.moduleCache.values()) {
            xss.prepareAttributes();
        }
        for (ComponentDeclaration decl : this.topLevel) {
            StyleElement inst = decl.getSourceElement();
            if (inst.isActionCompleted(32)) continue;
            inst.setActionCompleted(32);
            try {
                inst.processAllAttributes();
            } catch (XPathException err) {
                decl.getSourceElement().compileError(err);
            }
        }
    }

    protected void indexFunction(ComponentDeclaration decl) {
        HashMap<SymbolicName, Component> componentIndex = this.stylesheetPackage.getComponentIndex();
        XSLFunction sourceFunction = (XSLFunction)decl.getSourceElement();
        UserFunction compiledFunction = sourceFunction.getCompiledFunction();
        Component declaringComponent = compiledFunction.obtainDeclaringComponent(sourceFunction);
        SymbolicName.F sName = sourceFunction.getSymbolicName();
        if (sName != null) {
            Component other = componentIndex.get(sName);
            if (other == null) {
                componentIndex.put(sName, declaringComponent);
                this.functionIndex.put(sName, decl);
            } else if (other.getDeclaringPackage() == this.getStylesheetPackage()) {
                ComponentDeclaration otherFunction;
                int otherPrecedence;
                int thisPrecedence = decl.getPrecedence();
                if (thisPrecedence == (otherPrecedence = (otherFunction = this.functionIndex.get(sName)).getPrecedence())) {
                    sourceFunction.compileError("Duplicate named function (see line " + otherFunction.getSourceElement().getLineNumber() + " of " + otherFunction.getSourceElement().getSystemId() + ')', "XTSE0770");
                } else if (thisPrecedence >= otherPrecedence) {
                    componentIndex.put(sName, declaringComponent);
                    this.functionIndex.put(sName, decl);
                }
            } else if (sourceFunction.findAncestorElement(186) != null) {
                componentIndex.put(sName, declaringComponent);
                this.functionIndex.put(sName, decl);
            } else {
                sourceFunction.compileError("Function " + sName.getShortName() + " conflicts with a public function in package " + other.getDeclaringPackage().getPackageName(), "XTSE3050");
            }
        }
    }

    protected void indexVariableDeclaration(ComponentDeclaration decl) throws XPathException {
        XSLGlobalVariable varDecl = (XSLGlobalVariable)decl.getSourceElement();
        StructuredQName qName = varDecl.getSourceBinding().getVariableQName();
        GlobalVariable compiledVariable = (GlobalVariable)varDecl.getActor();
        Component declaringComponent = compiledVariable.obtainDeclaringComponent(varDecl);
        HashMap<SymbolicName, Component> componentIndex = this.stylesheetPackage.getComponentIndex();
        if (qName != null) {
            SymbolicName sName = varDecl.getSymbolicName();
            Component other = componentIndex.get(sName);
            if (other == null) {
                this.globalVariableIndex.put(qName, decl);
                componentIndex.put(new SymbolicName(206, qName), varDecl.getActor().getDeclaringComponent());
            } else if (other.getDeclaringPackage() == this.getStylesheetPackage()) {
                ComponentDeclaration otherVarDecl;
                int otherPrecedence;
                int thisPrecedence = decl.getPrecedence();
                if (thisPrecedence == (otherPrecedence = (otherVarDecl = this.globalVariableIndex.get(sName.getComponentName())).getPrecedence())) {
                    StyleElement v2 = otherVarDecl.getSourceElement();
                    if (v2 == varDecl) {
                        varDecl.compileError("Global variable or parameter $" + qName.getDisplayName() + " is declared more than once (caused by including the containing module more than once)", "XTSE0630");
                    } else {
                        varDecl.compileError("Duplicate global variable/parameter declaration (see line " + v2.getLineNumber() + " of " + v2.getSystemId() + ')', "XTSE0630");
                    }
                } else if (thisPrecedence < otherPrecedence && varDecl != otherVarDecl.getSourceElement()) {
                    varDecl.setRedundant(true);
                } else if (varDecl != otherVarDecl.getSourceElement()) {
                    ((XSLGlobalVariable)otherVarDecl.getSourceElement()).setRedundant(true);
                    this.globalVariableIndex.put(qName, decl);
                    componentIndex.put(new SymbolicName(206, qName), varDecl.getActor().getDeclaringComponent());
                }
            } else if (varDecl.findAncestorElement(186) != null) {
                componentIndex.put(sName, declaringComponent);
                this.globalVariableIndex.put(sName.getComponentName(), decl);
            } else {
                String kind = varDecl instanceof XSLGlobalParam ? "parameter" : "variable";
                varDecl.compileError("Global " + kind + " $" + sName.getComponentName().getDisplayName() + " conflicts with a public variable/parameter in package " + other.getDeclaringPackage().getPackageName(), "XTSE3050");
            }
        }
    }

    public SourceBinding getGlobalVariableBinding(StructuredQName qName) {
        ComponentDeclaration decl = this.globalVariableIndex.get(qName);
        return decl == null ? null : ((XSLGlobalVariable)decl.getSourceElement()).getSourceBinding();
    }

    protected void indexNamedTemplate(ComponentDeclaration decl) throws XPathException {
        HashMap<SymbolicName, Component> componentIndex = this.stylesheetPackage.getComponentIndex();
        XSLTemplate sourceTemplate = (XSLTemplate)decl.getSourceElement();
        SymbolicName sName = sourceTemplate.getSymbolicName();
        if (sName != null) {
            Component other = componentIndex.get(sName);
            if (other == null) {
                NamedTemplate compiledTemplate = ((XSLTemplate)decl.getSourceElement()).getCompiledNamedTemplate();
                Component declaringComponent = compiledTemplate.obtainDeclaringComponent(sourceTemplate);
                componentIndex.put(sName, declaringComponent);
                PrincipalStylesheetModule.setLocalParamDetails(sourceTemplate, compiledTemplate);
                this.templateIndex.put(sName.getComponentName(), decl);
            } else if (other.getDeclaringPackage() == this.getStylesheetPackage()) {
                ComponentDeclaration otherTemplate;
                int otherPrecedence;
                int thisPrecedence = decl.getPrecedence();
                if (thisPrecedence == (otherPrecedence = (otherTemplate = this.templateIndex.get(sName.getComponentName())).getPrecedence())) {
                    String errorCode = sourceTemplate.getParent() instanceof XSLOverride ? "XTSE3055" : "XTSE0660";
                    sourceTemplate.compileError("Duplicate named template (see line " + otherTemplate.getSourceElement().getLineNumber() + " of " + otherTemplate.getSourceElement().getSystemId() + ')', errorCode);
                } else if (thisPrecedence >= otherPrecedence) {
                    NamedTemplate compiledTemplate = new NamedTemplate(sName.getComponentName());
                    Component declaringComponent = compiledTemplate.obtainDeclaringComponent(sourceTemplate);
                    componentIndex.put(sName, declaringComponent);
                    this.templateIndex.put(sName.getComponentName(), decl);
                    PrincipalStylesheetModule.setLocalParamDetails(sourceTemplate, compiledTemplate);
                }
            } else if (sourceTemplate.findAncestorElement(186) != null) {
                NamedTemplate compiledTemplate = sourceTemplate.getCompiledNamedTemplate();
                Component declaringComponent = compiledTemplate.obtainDeclaringComponent(sourceTemplate);
                componentIndex.put(sName, declaringComponent);
                this.templateIndex.put(sName.getComponentName(), decl);
            } else {
                sourceTemplate.compileError("Named template " + sName.getComponentName().getDisplayName() + " conflicts with a public named template in package " + other.getDeclaringPackage().getPackageName(), "XTSE3050");
            }
        }
    }

    private static void setLocalParamDetails(XSLTemplate source, NamedTemplate nt) throws XPathException {
        AxisIterator kids = source.iterateAxis(3, NodeKindTest.ELEMENT);
        ArrayList<NamedTemplate.LocalParamInfo> details = new ArrayList<NamedTemplate.LocalParamInfo>();
        kids.forEachOrFail(child -> {
            if (child instanceof XSLLocalParam) {
                XSLLocalParam lp = (XSLLocalParam)child;
                lp.prepareTemplateSignatureAttributes();
                NamedTemplate.LocalParamInfo info = new NamedTemplate.LocalParamInfo();
                info.name = lp.getVariableQName();
                info.requiredType = lp.getRequiredType();
                info.isRequired = lp.isRequiredParam();
                info.isTunnel = lp.isTunnelParam();
                details.add(info);
            }
        });
        nt.setLocalParamDetails(details);
    }

    public NamedTemplate getNamedTemplate(StructuredQName name) {
        HashMap<SymbolicName, Component> componentIndex = this.stylesheetPackage.getComponentIndex();
        Component component = componentIndex.get(new SymbolicName(200, name));
        return component == null ? null : (NamedTemplate)component.getActor();
    }

    protected void indexAttributeSet(ComponentDeclaration decl) throws XPathException {
        XSLAttributeSet sourceAttributeSet = (XSLAttributeSet)decl.getSourceElement();
        StructuredQName name = sourceAttributeSet.getAttributeSetName();
        List<ComponentDeclaration> entries = this.attributeSetDeclarations.get(name);
        if (entries == null) {
            entries = new ArrayList<ComponentDeclaration>();
            this.attributeSetDeclarations.put(name, entries);
        } else {
            String thisVis = Whitespace.trim(sourceAttributeSet.getAttributeValue("", "visibility"));
            String firstVis = Whitespace.trim(entries.get(0).getSourceElement().getAttributeValue("", "visibility"));
            if (thisVis == null ? firstVis != null : !thisVis.equals(firstVis)) {
                throw new XPathException("Visibility attributes on attribute-sets sharing the same name must all be the same", "XTSE0010");
            }
        }
        entries.add(0, decl);
    }

    public List<ComponentDeclaration> getAttributeSetDeclarations(StructuredQName name) {
        return this.attributeSetDeclarations.get(name);
    }

    public void combineAttributeSets(Compilation compilation) throws XPathException {
        HashMap<StructuredQName, AttributeSet> index = new HashMap<StructuredQName, AttributeSet>();
        for (Map.Entry<StructuredQName, List<ComponentDeclaration>> entry : this.attributeSetDeclarations.entrySet()) {
            AttributeSet as = new AttributeSet();
            as.setName(entry.getKey());
            as.setPackageData(this.stylesheetPackage);
            StyleElement firstDecl = entry.getValue().get(0).getSourceElement();
            as.setSystemId(firstDecl.getSystemId());
            as.setLineNumber(firstDecl.getLineNumber());
            index.put(entry.getKey(), as);
            Component declaringComponent = as.getDeclaringComponent();
            if (declaringComponent == null) {
                declaringComponent = as.makeDeclaringComponent(Visibility.PRIVATE, this.stylesheetPackage);
            }
            this.stylesheetPackage.addComponent(declaringComponent);
        }
        for (Map.Entry<StructuredQName, List<ComponentDeclaration>> entry : this.attributeSetDeclarations.entrySet()) {
            XSLAttributeSet src;
            ArrayList<Expression> content = new ArrayList<Expression>();
            Visibility vis = null;
            boolean explicitVisibility = false;
            boolean streamable = false;
            ArrayList<ComponentDeclaration> entries = new ArrayList<ComponentDeclaration>();
            HashSet<XSLAttributeSet> elements = new HashSet<XSLAttributeSet>();
            for (int i = entry.getValue().size() - 1; i >= 0; --i) {
                ComponentDeclaration decl = entry.getValue().get(i);
                src = (XSLAttributeSet)decl.getSourceElement();
                if (elements.contains(src)) continue;
                entries.add(0, decl);
                elements.add(src);
            }
            for (ComponentDeclaration decl : entries) {
                src = (XSLAttributeSet)decl.getSourceElement();
                streamable |= src.isDeclaredStreamable();
                src.compileDeclaration(compilation, decl);
                content.addAll(src.getContainedInstructions());
                vis = src.getVisibility();
                explicitVisibility = explicitVisibility || src.getAttributeValue("", "visibility") != null;
            }
            AttributeSet aSet = (AttributeSet)index.get(entry.getKey());
            aSet.setDeclaredStreamable(streamable);
            Expression block = Block.makeBlock(content);
            aSet.setBody(block);
            SlotManager frame = this.getConfiguration().makeSlotManager();
            ExpressionTool.allocateSlots(block, 0, frame);
            aSet.setStackFrameMap(frame);
            VisibilityProvenance provenance = explicitVisibility ? VisibilityProvenance.EXPLICIT : VisibilityProvenance.DEFAULTED;
            aSet.getDeclaringComponent().setVisibility(vis, provenance);
            if (!streamable) continue;
            this.checkStreamability(aSet);
        }
    }

    protected void checkStreamability(AttributeSet aSet) throws XPathException {
    }

    protected void getAttributeSets(StructuredQName name, List<ComponentDeclaration> list) {
        for (ComponentDeclaration decl : this.topLevel) {
            XSLAttributeSet t;
            if (!(decl.getSourceElement() instanceof XSLAttributeSet) || !(t = (XSLAttributeSet)decl.getSourceElement()).getAttributeSetName().equals(name)) continue;
            list.add(decl);
        }
    }

    public void indexMode(ComponentDeclaration decl) {
        XSLMode sourceMode = (XSLMode)decl.getSourceElement();
        StructuredQName modeName = sourceMode.getObjectName();
        if (modeName == null) {
            return;
        }
        SymbolicName sName = new SymbolicName(174, modeName);
        Mode other = this.getStylesheetPackage().getRuleManager().obtainMode(modeName, false);
        if (other != null && other.getDeclaringComponent().getDeclaringPackage() != this.getStylesheetPackage()) {
            sourceMode.compileError("Mode " + sName.getComponentName().getDisplayName() + " conflicts with a public mode declared in package " + other.getDeclaringComponent().getDeclaringPackage().getPackageName(), "XTSE3050");
        }
    }

    public boolean checkAcceptableModeForPackage(XSLTemplate template, Mode mode) {
        StylesheetPackage templatePack = template.getPackageData();
        if (mode.getDeclaringComponent() == null) {
            return true;
        }
        StylesheetPackage modePack = mode.getDeclaringComponent().getDeclaringPackage();
        if (templatePack != modePack) {
            NodeImpl parent = template.getParent();
            boolean bad = false;
            if (!(parent instanceof XSLOverride)) {
                bad = true;
            } else {
                NodeInfo grandParent = parent.getParent();
                if (!(grandParent instanceof XSLUsePackage)) {
                    bad = true;
                } else {
                    SymbolicName modeName = mode.getSymbolicName();
                    Component.M usedMode = (Component.M)((XSLUsePackage)grandParent).getUsedPackage().getComponent(modeName);
                    if (usedMode == null) {
                        bad = true;
                    } else if (usedMode.getVisibility() == Visibility.FINAL) {
                        bad = true;
                    }
                }
            }
            if (bad) {
                template.compileError("A template rule cannot be added to a mode declared in a used package unless the xsl:template declaration appears within an xsl:override child of the appropriate xsl:use-package element", "XTSE3050");
                return false;
            }
        }
        return true;
    }

    private void checkForSchemaAwareness() {
        Compilation compilation = this.getRootElement().getCompilation();
        if (!compilation.isSchemaAware() && this.getConfiguration().isLicensedFeature(2)) {
            for (ComponentDeclaration decl : this.topLevel) {
                StyleElement node = decl.getSourceElement();
                if (!(node instanceof XSLImportSchema)) continue;
                compilation.setSchemaAware(true);
                return;
            }
        }
    }

    public AccumulatorRegistry getAccumulatorManager() {
        return this.accumulatorManager;
    }

    public void setAccumulatorManager(AccumulatorRegistry accumulatorManager) {
        this.accumulatorManager = accumulatorManager;
        this.stylesheetPackage.setAccumulatorRegistry(accumulatorManager);
    }

    protected void addNamespaceAlias(ComponentDeclaration node) {
        this.namespaceAliasList.add(node);
        ++this.numberOfAliases;
    }

    protected NamespaceBinding getNamespaceAlias(String uri) {
        return this.namespaceAliasMap.get(uri);
    }

    protected boolean isAliasResultNamespace(String uri) {
        return this.aliasResultUriSet.contains(uri);
    }

    private void collectNamespaceAliases() {
        this.namespaceAliasMap = new HashMap(this.numberOfAliases);
        this.aliasResultUriSet = new HashSet<String>(this.numberOfAliases);
        HashSet<String> aliasesAtThisPrecedence = new HashSet<String>();
        int currentPrecedence = -1;
        for (int i = 0; i < this.numberOfAliases; ++i) {
            ComponentDeclaration decl = this.namespaceAliasList.get(i);
            XSLNamespaceAlias xna = (XSLNamespaceAlias)decl.getSourceElement();
            String scode = xna.getStylesheetURI();
            NamespaceBinding resultBinding = xna.getResultNamespaceBinding();
            int prec = decl.getPrecedence();
            if (currentPrecedence != prec) {
                currentPrecedence = prec;
                aliasesAtThisPrecedence.clear();
            }
            if (aliasesAtThisPrecedence.contains(scode) && !this.namespaceAliasMap.get(scode).getURI().equals(resultBinding.getURI())) {
                xna.compileError("More than one alias is defined for the same namespace", "XTSE0810");
            }
            if (this.namespaceAliasMap.get(scode) == null) {
                this.namespaceAliasMap.put(scode, resultBinding);
                this.aliasResultUriSet.add(resultBinding.getURI());
            }
            aliasesAtThisPrecedence.add(scode);
        }
        this.namespaceAliasList = null;
    }

    protected boolean hasNamespaceAliases() {
        return this.numberOfAliases > 0;
    }

    public Properties gatherOutputProperties(StructuredQName formatQName) throws XPathException {
        boolean found = formatQName == null;
        Configuration config = this.getConfiguration();
        Properties details = new Properties(config.getDefaultSerializationProperties());
        HashMap<String, Integer> precedences = new HashMap<String, Integer>(10);
        for (int i = this.topLevel.size() - 1; i >= 0; --i) {
            ComponentDeclaration decl = (ComponentDeclaration)this.topLevel.get(i);
            if (!(decl.getSourceElement() instanceof XSLOutput)) continue;
            XSLOutput xo = (XSLOutput)decl.getSourceElement();
            if (!(formatQName == null ? xo.getFormatQName() == null : formatQName.equals(xo.getFormatQName()))) continue;
            found = true;
            xo.gatherOutputProperties(details, precedences, decl.getPrecedence());
        }
        if (!found) {
            this.compileError("Requested output format " + formatQName.getDisplayName() + " has not been defined", "XTDE1460");
        }
        return details;
    }

    protected void compile(Compilation compilation) throws XPathException {
        block38: {
            try {
                AccumulatorRegistry accumulatorRegistry;
                StyleElement node;
                StyleElement snode;
                Timer timer = compilation.timer;
                Configuration config = this.getConfiguration();
                XQueryFunctionLibrary queryFunctions = this.stylesheetPackage.getXQueryFunctionLibrary();
                Iterator<XQueryFunction> qf = queryFunctions.getFunctionDefinitions();
                while (qf.hasNext()) {
                    XQueryFunction f = qf.next();
                    f.fixupReferences();
                }
                if (Compilation.TIMING) {
                    timer.report("fixup Query functions");
                }
                boolean allowImplicit = !this.getStylesheetPackage().isDeclaredModes();
                for (ComponentDeclaration decl : this.topLevel) {
                    snode = decl.getSourceElement();
                    if (snode instanceof XSLMode) {
                        this.getRuleManager().obtainMode(snode.getObjectName(), true);
                    }
                    if (!allowImplicit) continue;
                    this.registerImplicitModes(snode, this.getRuleManager());
                }
                for (ComponentDeclaration decl : this.topLevel) {
                    snode = decl.getSourceElement();
                    if (!(snode instanceof XSLTemplate)) continue;
                    ((XSLTemplate)snode).register(decl);
                }
                if (Compilation.TIMING) {
                    timer.report("register templates");
                }
                this.adjustExposedVisibility();
                if (Compilation.TIMING) {
                    timer.report("adjust exposed visibility");
                }
                for (ComponentDeclaration decl : this.topLevel) {
                    snode = decl.getSourceElement();
                    if (snode.isActionCompleted(2)) continue;
                    snode.setActionCompleted(2);
                    snode.compileDeclaration(compilation, decl);
                }
                if (Compilation.TIMING) {
                    timer.report("compile top-level objects (" + this.topLevel.size() + ")");
                }
                for (ComponentDeclaration decl : this.functionIndex.values()) {
                    node = decl.getSourceElement();
                    if (node.isActionCompleted(4)) continue;
                    node.setActionCompleted(4);
                    if (node.getVisibility() == Visibility.ABSTRACT) continue;
                    ((XSLFunction)node).getCompiledFunction().typeCheck(node.makeExpressionVisitor());
                }
                if (Compilation.TIMING) {
                    timer.report("typeCheck functions (" + this.functionIndex.size() + ")");
                }
                if (compilation.getErrorCount() > 0) {
                    return;
                }
                this.optimizeTopLevel();
                if (Compilation.TIMING) {
                    timer.report("optimize top level");
                }
                for (ComponentDeclaration decl : this.functionIndex.values()) {
                    node = decl.getSourceElement();
                    if (node.isActionCompleted(8)) continue;
                    node.setActionCompleted(8);
                    ((StylesheetComponent)((Object)node)).optimize(decl);
                }
                if (Compilation.TIMING) {
                    timer.report("optimize functions");
                }
                this.getDecimalFormatManager().checkConsistency();
                if (Compilation.TIMING) {
                    timer.report("check decimal formats");
                }
                RuleManager ruleManager = this.getRuleManager();
                ruleManager.checkConsistency();
                ruleManager.computeRankings();
                if (!compilation.isFallbackToNonStreaming()) {
                    ruleManager.invertStreamableTemplates();
                }
                if (config.obtainOptimizer().isOptionSet(2048)) {
                    ruleManager.optimizeRules();
                }
                if (Compilation.TIMING) {
                    timer.report("build template rule tables");
                }
                ExecutableFunctionLibrary overriding = new ExecutableFunctionLibrary(config);
                ExecutableFunctionLibrary underriding = new ExecutableFunctionLibrary(config);
                for (Component component : this.stylesheetPackage.getComponentIndex().values()) {
                    Visibility visibility = component.getVisibility();
                    if (!(component.getActor() instanceof UserFunction)) continue;
                    UserFunction userFunction = (UserFunction)component.getActor();
                    if (userFunction.isOverrideExtensionFunction()) {
                        overriding.addFunction(userFunction);
                        continue;
                    }
                    underriding.addFunction(userFunction);
                }
                this.getStylesheetPackage().setFunctionLibraryDetails(null, overriding, underriding);
                if (Compilation.TIMING) {
                    timer.report("build runtime function tables");
                }
                for (ComponentDeclaration componentDeclaration : this.topLevel) {
                    NamedTemplate namedTemplate;
                    StyleElement styleElement = componentDeclaration.getSourceElement();
                    if (!(styleElement instanceof XSLTemplate) || (namedTemplate = ((XSLTemplate)styleElement).getActor()) == null || namedTemplate.getTemplateName() != null) continue;
                    namedTemplate.allocateAllBindingSlots(this.stylesheetPackage);
                }
                if (Compilation.TIMING) {
                    timer.report("allocate binding slots to named templates");
                }
                HashMap<SymbolicName, Component> componentIndex = this.stylesheetPackage.getComponentIndex();
                for (Component component : componentIndex.values()) {
                    Actor actor = component.getActor();
                    if (actor == null) continue;
                    actor.allocateAllBindingSlots(this.stylesheetPackage);
                }
                if (Compilation.TIMING) {
                    timer.report("allocate binding slots to component references");
                }
                KeyManager keyManager = this.getKeyManager();
                for (KeyDefinitionSet keyDefinitionSet : keyManager.getAllKeyDefinitionSets()) {
                    for (KeyDefinition keyDef : keyDefinitionSet.getKeyDefinitions()) {
                        keyDef.makeDeclaringComponent(Visibility.PRIVATE, this.getStylesheetPackage());
                        keyDef.allocateAllBindingSlots(this.stylesheetPackage);
                    }
                }
                if (Compilation.TIMING) {
                    timer.report("allocate binding slots to key definitions");
                }
                if ((accumulatorRegistry = this.getAccumulatorManager()) != null) {
                    for (Accumulator acc : accumulatorRegistry.getAllAccumulators()) {
                        acc.allocateAllBindingSlots(this.stylesheetPackage);
                    }
                }
                if (Compilation.TIMING) {
                    timer.report("allocate binding slots to accumulators");
                }
                if (compilation.getCompilerInfo().isGenerateByteCode() && !config.isDeferredByteCode(HostLanguage.XSLT)) {
                    if (Compilation.TIMING) {
                        config.getLogger().info("Generating byte code...");
                    }
                    Optimizer optimizer = config.obtainOptimizer();
                    for (ComponentDeclaration decl : this.topLevel) {
                        StyleElement inst = decl.getSourceElement();
                        if (!(inst instanceof StylesheetComponent)) continue;
                        ((StylesheetComponent)((Object)inst)).generateByteCode(optimizer);
                    }
                }
                if (Compilation.TIMING) {
                    timer.report("inject byte code candidates");
                    timer.reportCumulative("total compile time");
                }
            } catch (RuntimeException err) {
                if (compilation.getErrorCount() != 0) break block38;
                throw err;
            }
        }
    }

    private void registerImplicitModes(StyleElement element, RuleManager manager) {
        NodeInfo child;
        String modeAtt;
        if ((element instanceof XSLApplyTemplates || element instanceof XSLTemplate) && (modeAtt = element.getAttributeValue("mode")) != null) {
            String[] tokens;
            for (String s : tokens = Whitespace.trim(modeAtt).split("[ \t\n\r]+")) {
                if (s.startsWith("#")) continue;
                StructuredQName modeName = element.makeQName(s, null, "mode");
                SymbolicName sName = new SymbolicName(174, modeName);
                HashMap<SymbolicName, Component> componentIndex = this.getStylesheetPackage().getComponentIndex();
                Component existing = componentIndex.get(sName);
                if (existing != null && existing.getDeclaringPackage() != this.getStylesheetPackage()) {
                    if (!(element instanceof XSLTemplate) || element.getParent() instanceof XSLOverride) continue;
                    element.compileError("A template rule cannot be added to a mode declared in a used package unless the xsl:template declaration appears within an xsl:override child of the appropriate xsl:use-package element", "XTSE3050");
                    continue;
                }
                manager.obtainMode(modeName, true);
            }
        }
        AxisIterator kids = element.iterateAxis(3);
        while ((child = kids.next()) != null) {
            if (!(child instanceof StyleElement)) continue;
            this.registerImplicitModes((StyleElement)child, manager);
        }
    }

    public void optimizeTopLevel() throws XPathException {
        for (ComponentDeclaration decl : this.topLevel) {
            StyleElement node = decl.getSourceElement();
            if (node instanceof StylesheetComponent && !(node instanceof XSLFunction) && !node.isActionCompleted(8)) {
                node.setActionCompleted(8);
                ((StylesheetComponent)((Object)node)).optimize(decl);
            }
            if (!(node instanceof XSLTemplate)) continue;
            ((XSLTemplate)node).allocatePatternSlotNumbers();
        }
    }

    protected boolean isImportedSchema(String targetNamespace) {
        return this.stylesheetPackage.getSchemaNamespaces().contains(targetNamespace);
    }

    protected void addImportedSchema(String targetNamespace) {
        this.stylesheetPackage.getSchemaNamespaces().add(targetNamespace);
    }

    protected Set<String> getImportedSchemaTable() {
        return this.stylesheetPackage.getSchemaNamespaces();
    }

    public ComponentDeclaration getCharacterMap(StructuredQName name) {
        for (int i = this.topLevel.size() - 1; i >= 0; --i) {
            XSLCharacterMap t;
            ComponentDeclaration decl = (ComponentDeclaration)this.topLevel.get(i);
            if (!(decl.getSourceElement() instanceof XSLCharacterMap) || !(t = (XSLCharacterMap)decl.getSourceElement()).getCharacterMapName().equals(name)) continue;
            return decl;
        }
        return null;
    }

    public void adjustExposedVisibility() throws XPathException {
        ArrayList<XSLExpose> exposeDeclarations = new ArrayList<XSLExpose>();
        for (ComponentDeclaration decl : this.topLevel) {
            if (!(decl.getSourceElement() instanceof XSLExpose)) continue;
            exposeDeclarations.add(0, (XSLExpose)decl.getSourceElement());
        }
        if (exposeDeclarations.isEmpty()) {
            return;
        }
        NamePool pool = this.getConfiguration().getNamePool();
        HashMap<SymbolicName, Component> componentIndex = this.stylesheetPackage.getComponentIndex();
        block1: for (Component component : componentIndex.values()) {
            int fp = component.getComponentKind();
            ComponentTest exactNameTest = new ComponentTest(fp, new NameTest(1, new FingerprintedQName(component.getActor().getComponentName(), pool), pool), -1);
            ComponentTest exactFunctionTest = null;
            if (fp == 158) {
                Function fn = (Function)((Object)component.getActor());
                exactFunctionTest = new ComponentTest(fp, new NameTest(1, new FingerprintedQName(fn.getFunctionName(), pool), pool), fn.getArity());
            }
            boolean matched = false;
            for (XSLExpose exposure : exposeDeclarations) {
                Set<ComponentTest> explicitComponentTests = exposure.getExplicitComponentTests();
                if (!explicitComponentTests.contains(exactNameTest) && (exactFunctionTest == null || !explicitComponentTests.contains(exactFunctionTest))) continue;
                component.setVisibility(exposure.getVisibility(), VisibilityProvenance.EXPOSED);
                matched = true;
                break;
            }
            if (matched || component.getVisibilityProvenance() != VisibilityProvenance.DEFAULTED) continue;
            block3: for (XSLExpose exposure : exposeDeclarations) {
                for (ComponentTest test : exposure.getWildcardComponentTests()) {
                    if (!test.isPartialWildcard() || !test.matches(component.getActor())) continue;
                    if (exposure.getVisibility() == Visibility.ABSTRACT && component.getVisibility() != Visibility.ABSTRACT) {
                        XPathException err = new XPathException("The non-abstract component " + component.getActor().getSymbolicName() + " cannot be made abstract by means of xsl:expose", "XTSE3025");
                        err.setLocation(exposure);
                        throw err;
                    }
                    component.setVisibility(exposure.getVisibility(), VisibilityProvenance.EXPOSED);
                    matched = true;
                    break block3;
                }
            }
            if (matched) continue;
            for (XSLExpose exposure : exposeDeclarations) {
                for (ComponentTest test : exposure.getWildcardComponentTests()) {
                    if (!test.matches(component.getActor())) continue;
                    if (exposure.getVisibility() == Visibility.ABSTRACT && component.getVisibility() != Visibility.ABSTRACT) {
                        XPathException err = new XPathException("The non-abstract component " + component.getActor().getSymbolicName() + " cannot be made abstract by means of xsl:expose", "XTSE3025");
                        err.setLocation(exposure);
                        throw err;
                    }
                    component.setVisibility(exposure.getVisibility(), VisibilityProvenance.EXPOSED);
                    continue block1;
                }
            }
        }
    }

    protected void compileError(String message, String errorCode) throws XPathException {
        XPathException tce = new XPathException(message, errorCode);
        this.compileError(tce);
    }

    protected void compileError(XPathException error) {
        error.setIsStaticError(true);
        this.getRootElement().compileError(error);
    }

    protected void fixup() throws XPathException {
        for (Action a : this.fixupActions) {
            a.doAction();
        }
    }

    protected void complete() throws XPathException {
        this.stylesheetPackage.complete();
    }

    public SlotManager getSlotManager() {
        return null;
    }

    @Override
    public GlobalVariable getEquivalentVariable(Expression select) {
        return null;
    }

    @Override
    public void addGlobalVariable(GlobalVariable variable) {
        this.addGlobalVariable(variable, Visibility.PRIVATE);
    }

    public void addGlobalVariable(GlobalVariable variable, Visibility visibility) {
        Component component = variable.makeDeclaringComponent(visibility, this.getStylesheetPackage());
        if (variable.getPackageData() == null) {
            variable.setPackageData(this.stylesheetPackage);
        }
        if (visibility == Visibility.HIDDEN) {
            this.stylesheetPackage.addHiddenComponent(component);
        } else {
            this.stylesheetPackage.getComponentIndex().put(new SymbolicName(206, variable.getVariableQName()), component);
        }
    }
}

