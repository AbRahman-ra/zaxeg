/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.EarlyEvaluationContext;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.GlobalVariableReference;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.GlobalContextRequirement;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.CodeInjector;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.OptimizerOptions;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.functions.registry.ConstructorFunctionLibrary;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.QNameParser;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.ImportedFunctionLibrary;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.UnboundFunctionLibrary;
import net.sf.saxon.query.UndeclaredVariable;
import net.sf.saxon.query.XQueryFunction;
import net.sf.saxon.query.XQueryFunctionLibrary;
import net.sf.saxon.query.XQueryParser;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.XmlProcessingError;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trace.TraceCodeInjector;
import net.sf.saxon.trans.DecimalFormatManager;
import net.sf.saxon.trans.KeyManager;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingAbort;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.trans.XmlProcessingIncident;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;

public class QueryModule
implements StaticContext {
    private boolean isMainModule;
    private final Configuration config;
    private StaticQueryContext userQueryContext;
    private final QueryModule topModule;
    private URI locationURI;
    private String baseURI;
    private String moduleNamespace;
    private HashMap<String, String> explicitPrologNamespaces;
    private Stack<NamespaceBinding> activeNamespaces;
    private HashMap<StructuredQName, GlobalVariable> variables;
    private HashMap<StructuredQName, GlobalVariable> libraryVariables;
    private HashMap<StructuredQName, UndeclaredVariable> undeclaredVariables;
    private HashSet<String> importedSchemata;
    private HashMap<String, HashSet<String>> loadedSchemata;
    private Executable executable;
    private List<QueryModule> importers;
    private FunctionLibraryList functionLibraryList;
    private XQueryFunctionLibrary globalFunctionLibrary;
    private int localFunctionLibraryNr;
    private int importedFunctionLibraryNr;
    private int unboundFunctionLibraryNr;
    private Set<String> importedModuleNamespaces;
    private boolean inheritNamespaces = true;
    private boolean preserveNamespaces = true;
    private int constructionMode = 3;
    private String defaultFunctionNamespace;
    private String defaultElementNamespace;
    private boolean preserveSpace = false;
    private boolean defaultEmptyLeast = true;
    private String defaultCollationName;
    private int revalidationMode = 4;
    private boolean isUpdating = false;
    private ItemType requiredContextItemType = AnyItemType.getInstance();
    private DecimalFormatManager decimalFormatManager = null;
    private CodeInjector codeInjector;
    private PackageData packageData;
    private RetainedStaticContext moduleStaticContext = null;
    private Location moduleLocation;
    private OptimizerOptions optimizerOptions;

    public QueryModule(StaticQueryContext sqc) throws XPathException {
        this.config = sqc.getConfiguration();
        this.isMainModule = true;
        this.topModule = this;
        this.activeNamespaces = new Stack();
        this.baseURI = sqc.getBaseURI();
        this.defaultCollationName = sqc.getDefaultCollationName();
        try {
            this.locationURI = this.baseURI == null ? null : new URI(this.baseURI);
        } catch (URISyntaxException err) {
            throw new XPathException("Invalid location URI: " + this.baseURI);
        }
        this.executable = sqc.makeExecutable();
        this.importers = null;
        this.init(sqc);
        PackageData pd = new PackageData(this.config);
        pd.setHostLanguage(HostLanguage.XQUERY);
        pd.setSchemaAware(this.isSchemaAware());
        this.packageData = pd;
        Iterator<GlobalVariable> vars = sqc.iterateDeclaredGlobalVariables();
        while (vars.hasNext()) {
            GlobalVariable var = vars.next();
            this.declareVariable(var);
            pd.addGlobalVariable(var);
            var.setPackageData(pd);
        }
        this.executable.setTopLevelPackage(pd);
        this.executable.addPackage(pd);
        this.moduleLocation = sqc.getModuleLocation() == null ? new Loc(sqc.getSystemId(), 1, -1) : sqc.getModuleLocation();
        this.optimizerOptions = sqc.getOptimizerOptions();
    }

    public QueryModule(Configuration config, QueryModule importer) {
        this.config = config;
        this.importers = null;
        if (importer == null) {
            this.topModule = this;
        } else {
            this.topModule = importer.topModule;
            this.userQueryContext = importer.userQueryContext;
            this.importers = new ArrayList<QueryModule>(2);
            this.importers.add(importer);
        }
        this.init(this.userQueryContext);
        this.packageData = importer.getPackageData();
        this.activeNamespaces = new Stack();
        this.executable = null;
        this.optimizerOptions = importer.optimizerOptions;
    }

    private void init(StaticQueryContext sqc) {
        this.userQueryContext = sqc;
        this.variables = new HashMap(10);
        this.undeclaredVariables = new HashMap(5);
        if (this.isTopLevelModule()) {
            this.libraryVariables = new HashMap(10);
        }
        this.importedSchemata = new HashSet(5);
        this.importedModuleNamespaces = new HashSet<String>(5);
        this.moduleNamespace = null;
        this.activeNamespaces = new Stack();
        this.explicitPrologNamespaces = new HashMap(10);
        if (sqc != null) {
            this.inheritNamespaces = sqc.isInheritNamespaces();
            this.preserveNamespaces = sqc.isPreserveNamespaces();
            this.preserveSpace = sqc.isPreserveBoundarySpace();
            this.defaultEmptyLeast = sqc.isEmptyLeast();
            this.defaultFunctionNamespace = sqc.getDefaultFunctionNamespace();
            this.defaultElementNamespace = sqc.getDefaultElementNamespace();
            this.defaultCollationName = sqc.getDefaultCollationName();
            this.constructionMode = sqc.getConstructionMode();
            if (this.constructionMode == 3 && !sqc.isSchemaAware()) {
                this.constructionMode = 4;
            }
            this.requiredContextItemType = sqc.getRequiredContextItemType();
            this.isUpdating = sqc.isUpdatingEnabled();
            this.codeInjector = sqc.getCodeInjector();
            this.optimizerOptions = sqc.getOptimizerOptions();
        }
        this.initializeFunctionLibraries(sqc);
    }

    public static QueryModule makeQueryModule(String baseURI, Executable executable, QueryModule importer, String query, String namespaceURI) throws XPathException {
        Configuration config = executable.getConfiguration();
        QueryModule module = new QueryModule(config, importer);
        try {
            module.setLocationURI(new URI(baseURI));
        } catch (URISyntaxException e) {
            throw new XPathException("Invalid location URI " + baseURI, e);
        }
        module.setBaseURI(baseURI);
        module.setExecutable(executable);
        module.setModuleNamespace(namespaceURI);
        executable.addQueryLibraryModule(module);
        XQueryParser qp = (XQueryParser)config.newExpressionParser("XQ", importer.isUpdating(), 31);
        if (importer.getCodeInjector() != null) {
            qp.setCodeInjector(importer.getCodeInjector());
        } else if (config.isCompileWithTracing()) {
            qp.setCodeInjector(new TraceCodeInjector());
        }
        QNameParser qnp = new QNameParser(module.getLiveNamespaceResolver()).withAcceptEQName(importer.getXPathVersion() >= 30).withUnescaper(new XQueryParser.Unescaper(config.getValidCharacterChecker()));
        qp.setQNameParser(qnp);
        qp.parseLibraryModule(query, module);
        String namespace = module.getModuleNamespace();
        if (namespace == null) {
            XPathException err = new XPathException("Imported module must be a library module");
            err.setErrorCode("XQST0059");
            err.setIsStaticError(true);
            throw err;
        }
        if (!namespace.equals(namespaceURI)) {
            XPathException err = new XPathException("Imported module's namespace does not match requested namespace");
            err.setErrorCode("XQST0059");
            err.setIsStaticError(true);
            throw err;
        }
        return module;
    }

    private void initializeFunctionLibraries(StaticQueryContext sqc) {
        Configuration config = this.getConfiguration();
        if (this.isTopLevelModule()) {
            this.globalFunctionLibrary = new XQueryFunctionLibrary(config);
        }
        this.functionLibraryList = new FunctionLibraryList();
        this.functionLibraryList.addFunctionLibrary(this.getBuiltInFunctionSet());
        this.functionLibraryList.addFunctionLibrary(config.getBuiltInExtensionLibraryList());
        this.functionLibraryList.addFunctionLibrary(new ConstructorFunctionLibrary(config));
        this.localFunctionLibraryNr = this.functionLibraryList.addFunctionLibrary(new XQueryFunctionLibrary(config));
        this.importedFunctionLibraryNr = this.functionLibraryList.addFunctionLibrary(new ImportedFunctionLibrary(this, this.getTopLevelModule().getGlobalFunctionLibrary()));
        if (sqc != null && sqc.getExtensionFunctionLibrary() != null) {
            this.functionLibraryList.addFunctionLibrary(sqc.getExtensionFunctionLibrary());
        }
        this.functionLibraryList.addFunctionLibrary(config.getIntegratedFunctionLibrary());
        config.addExtensionBinders(this.functionLibraryList);
        this.unboundFunctionLibraryNr = this.functionLibraryList.addFunctionLibrary(new UnboundFunctionLibrary());
    }

    public BuiltInFunctionSet getBuiltInFunctionSet() {
        if (this.isUpdating()) {
            return this.config.getXQueryUpdateFunctionSet();
        }
        return this.config.getXPath31FunctionSet();
    }

    @Override
    public Configuration getConfiguration() {
        return this.config;
    }

    @Override
    public PackageData getPackageData() {
        return this.packageData;
    }

    public void setPackageData(PackageData packageData) {
        this.packageData = packageData;
    }

    public boolean isTopLevelModule() {
        return this == this.topModule;
    }

    public void setIsMainModule(boolean main) {
        this.isMainModule = main;
    }

    public boolean isMainModule() {
        return this.isMainModule;
    }

    public boolean mayImportModule(String namespace) {
        if (namespace.equals(this.moduleNamespace)) {
            return false;
        }
        if (this.importers == null) {
            return true;
        }
        for (QueryModule importer : this.importers) {
            if (importer.mayImportModule(namespace)) continue;
            return false;
        }
        return true;
    }

    public boolean isSchemaAware() {
        return this.executable.isSchemaAware();
    }

    @Override
    public OptimizerOptions getOptimizerOptions() {
        return this.optimizerOptions;
    }

    @Override
    public RetainedStaticContext makeRetainedStaticContext() {
        if (this.activeNamespaces.empty()) {
            if (this.moduleStaticContext == null) {
                this.moduleStaticContext = new RetainedStaticContext(this);
            }
            return this.moduleStaticContext;
        }
        return new RetainedStaticContext(this);
    }

    public void setInheritNamespaces(boolean inherit) {
        this.inheritNamespaces = inherit;
    }

    public boolean isInheritNamespaces() {
        return this.inheritNamespaces;
    }

    public void setPreserveNamespaces(boolean inherit) {
        this.preserveNamespaces = inherit;
    }

    public boolean isPreserveNamespaces() {
        return this.preserveNamespaces;
    }

    public void setConstructionMode(int mode) {
        this.constructionMode = mode;
    }

    public int getConstructionMode() {
        return this.constructionMode;
    }

    public void setPreserveBoundarySpace(boolean preserve) {
        this.preserveSpace = preserve;
    }

    public boolean isPreserveBoundarySpace() {
        return this.preserveSpace;
    }

    public void setEmptyLeast(boolean least) {
        this.defaultEmptyLeast = least;
    }

    public boolean isEmptyLeast() {
        return this.defaultEmptyLeast;
    }

    public XQueryFunctionLibrary getGlobalFunctionLibrary() {
        return this.globalFunctionLibrary;
    }

    public ImportedFunctionLibrary getImportedFunctionLibrary() {
        return (ImportedFunctionLibrary)this.functionLibraryList.get(this.importedFunctionLibraryNr);
    }

    public void addImportedNamespace(String uri) {
        if (this.importedModuleNamespaces == null) {
            this.importedModuleNamespaces = new HashSet<String>(5);
        }
        this.importedModuleNamespaces.add(uri);
        this.getImportedFunctionLibrary().addImportedNamespace(uri);
    }

    public boolean importsNamespace(String uri) {
        return this.importedModuleNamespaces != null && this.importedModuleNamespaces.contains(uri);
    }

    public QueryModule getTopLevelModule() {
        return this.topModule;
    }

    public Executable getExecutable() {
        return this.executable;
    }

    public void setExecutable(Executable executable) {
        this.executable = executable;
    }

    public StaticQueryContext getUserQueryContext() {
        return this.userQueryContext;
    }

    @Override
    public Location getContainingLocation() {
        return this.moduleLocation;
    }

    public void setModuleNamespace(String uri) {
        this.moduleNamespace = uri;
    }

    public String getModuleNamespace() {
        return this.moduleNamespace;
    }

    public void setLocationURI(URI uri) {
        this.locationURI = uri;
        this.moduleLocation = new Loc(this.locationURI.toString(), 1, -1);
    }

    public URI getLocationURI() {
        return this.locationURI;
    }

    @Override
    public String getSystemId() {
        return this.locationURI == null ? null : this.locationURI.toString();
    }

    public void setBaseURI(String uri) {
        this.baseURI = uri;
    }

    @Override
    public String getStaticBaseURI() {
        return this.baseURI;
    }

    public SlotManager getGlobalStackFrameMap() {
        return this.getPackageData().getGlobalSlotManager();
    }

    public void declareVariable(GlobalVariable var) throws XPathException {
        GlobalVariable old;
        StructuredQName key = var.getVariableQName();
        if (this.variables.get(key) != null && (old = this.variables.get(key)) != var && old.getUltimateOriginalVariable() != var.getUltimateOriginalVariable()) {
            String oldloc = " (see line " + old.getLineNumber();
            String oldSysId = old.getSystemId();
            if (oldSysId != null && !oldSysId.equals(var.getSystemId())) {
                oldloc = oldloc + " in module " + old.getSystemId();
            }
            oldloc = oldloc + ")";
            XPathException err = new XPathException("Duplicate definition of global variable " + var.getVariableQName().getDisplayName() + oldloc);
            err.setErrorCode("XQST0049");
            err.setIsStaticError(true);
            err.setLocation(var);
            throw err;
        }
        this.variables.put(key, var);
        this.getPackageData().addGlobalVariable(var);
        HashMap<StructuredQName, GlobalVariable> libVars = this.getTopLevelModule().libraryVariables;
        GlobalVariable old2 = libVars.get(key);
        if (old2 != null && old2 != var) {
            XPathException err = new XPathException("Duplicate definition of global variable " + var.getVariableQName().getDisplayName() + " (see line " + old2.getLineNumber() + " in module " + old2.getSystemId() + ')');
            err.setErrorCode("XQST0049");
            err.setIsStaticError(true);
            err.setLocation(var);
            throw err;
        }
        if (!this.isMainModule()) {
            libVars.put(key, var);
        }
    }

    public Iterable<GlobalVariable> getGlobalVariables() {
        return this.libraryVariables.values();
    }

    public List<GlobalVariable> fixupGlobalVariables(SlotManager globalVariableMap) throws XPathException {
        ArrayList<GlobalVariable> varDefinitions = new ArrayList<GlobalVariable>(20);
        ArrayList<Iterator<GlobalVariable>> iters = new ArrayList<Iterator<GlobalVariable>>();
        iters.add(this.variables.values().iterator());
        iters.add(this.libraryVariables.values().iterator());
        for (Iterator iterator : iters) {
            while (iterator.hasNext()) {
                GlobalVariable var = (GlobalVariable)iterator.next();
                if (varDefinitions.contains(var)) continue;
                int slot = globalVariableMap.allocateSlotNumber(var.getVariableQName());
                var.compile(this.getExecutable(), slot);
                varDefinitions.add(var);
            }
        }
        return varDefinitions;
    }

    public void lookForModuleCycles(Stack<QueryModule> referees, int lineNumber) throws XPathException {
        String uri;
        if (referees.contains(this)) {
            int s = referees.indexOf(this);
            referees.push(this);
            StringBuilder message = new StringBuilder("Circular dependency between modules. ");
            for (int i = s; i < referees.size() - 1; ++i) {
                QueryModule next = (QueryModule)referees.get(i + 1);
                if (i == s) {
                    message.append("Module ").append(this.getSystemId()).append(" references module ").append(next.getSystemId());
                    continue;
                }
                message.append(", which references module ").append(next.getSystemId());
            }
            message.append('.');
            XPathException err = new XPathException(message.toString());
            err.setErrorCode("XQST0093");
            err.setIsStaticError(true);
            Loc loc = new Loc(this.getSystemId(), lineNumber, -1);
            err.setLocator(loc);
            throw err;
        }
        referees.push(this);
        Iterator<GlobalVariable> viter = this.getModuleVariables();
        while (viter.hasNext()) {
            GlobalVariable gv = viter.next();
            Expression select = gv.getBody();
            if (select == null) continue;
            ArrayList<Binding> list = new ArrayList<Binding>(10);
            ExpressionTool.gatherReferencedVariables(select, list);
            for (Binding binding : list) {
                QueryModule sqc;
                if (!(binding instanceof GlobalVariable)) continue;
                String uri2 = ((GlobalVariable)binding).getSystemId();
                StructuredQName qName = binding.getVariableQName();
                boolean synthetic = qName.hasURI("http://saxon.sf.net/generated-variable");
                if (synthetic || uri2 == null || uri2.equals(this.getSystemId()) || (sqc = this.executable.getQueryModuleWithSystemId(uri2, this.topModule)) == null) continue;
                sqc.lookForModuleCycles(referees, ((GlobalVariable)binding).getLineNumber());
            }
            ArrayList<UserFunction> fList = new ArrayList<UserFunction>(5);
            ExpressionTool.gatherCalledFunctions(select, fList);
            for (UserFunction f : fList) {
                QueryModule sqc;
                uri = f.getSystemId();
                if (uri == null || uri.equals(this.getSystemId()) || (sqc = this.executable.getQueryModuleWithSystemId(uri, this.topModule)) == null) continue;
                sqc.lookForModuleCycles(referees, f.getLineNumber());
            }
        }
        Iterator<XQueryFunction> fiter = this.getLocalFunctionLibrary().getFunctionDefinitions();
        while (fiter.hasNext()) {
            XQueryFunction gf = fiter.next();
            Expression body = gf.getUserFunction().getBody();
            if (body == null) continue;
            ArrayList<Binding> vList = new ArrayList<Binding>(10);
            ExpressionTool.gatherReferencedVariables(body, vList);
            for (Binding b : vList) {
                QueryModule sqc;
                if (!(b instanceof GlobalVariable)) continue;
                uri = ((GlobalVariable)b).getSystemId();
                StructuredQName qName = b.getVariableQName();
                boolean synthetic = qName.hasURI("http://saxon.sf.net/") && "gg".equals(qName.getPrefix());
                if (synthetic || uri == null || uri.equals(this.getSystemId()) || (sqc = this.executable.getQueryModuleWithSystemId(uri, this.topModule)) == null) continue;
                sqc.lookForModuleCycles(referees, ((GlobalVariable)b).getLineNumber());
            }
            ArrayList<UserFunction> arrayList = new ArrayList<UserFunction>(10);
            ExpressionTool.gatherCalledFunctions(body, arrayList);
            for (UserFunction f : arrayList) {
                QueryModule sqc;
                String uri3 = f.getSystemId();
                if (uri3 == null || uri3.equals(this.getSystemId()) || (sqc = this.executable.getQueryModuleWithSystemId(uri3, this.topModule)) == null) continue;
                sqc.lookForModuleCycles(referees, f.getLineNumber());
            }
        }
        referees.pop();
    }

    public Iterator<GlobalVariable> getModuleVariables() {
        return this.variables.values().iterator();
    }

    public void checkForCircularities(List<GlobalVariable> compiledVars, XQueryFunctionLibrary globalFunctionLibrary) throws XPathException {
        Iterator<GlobalVariable> iter = compiledVars.iterator();
        Stack<Object> stack = null;
        while (iter.hasNext()) {
            GlobalVariable gv;
            if (stack == null) {
                stack = new Stack<Object>();
            }
            if ((gv = iter.next()) == null) continue;
            gv.lookForCycles(stack, globalFunctionLibrary);
        }
    }

    public void typeCheckGlobalVariables(List<GlobalVariable> compiledVars) throws XPathException {
        GlobalContextRequirement gcr;
        ExpressionVisitor visitor = ExpressionVisitor.make(this);
        for (GlobalVariable compiledVar : compiledVars) {
            compiledVar.typeCheck(visitor);
        }
        if (this.isMainModule() && (gcr = this.executable.getGlobalContextRequirement()) != null && gcr.getDefaultValue() != null) {
            ContextItemStaticInfo info = this.getConfiguration().makeContextItemStaticInfo(AnyItemType.getInstance(), true);
            gcr.setDefaultValue(gcr.getDefaultValue().typeCheck(visitor, info));
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    public Expression bindVariable(StructuredQName qName) throws XPathException {
        GlobalVariable var = this.variables.get(qName);
        if (var == null) {
            String uri = qName.getURI();
            if (!(uri.equals("") && this.isMainModule() || uri.equals(this.moduleNamespace) || this.importsNamespace(uri))) {
                XPathException err = new XPathException("Variable $" + qName.getDisplayName() + " has not been declared");
                err.setErrorCode("XPST0008");
                err.setIsStaticError(true);
                throw err;
            }
            QueryModule main = this.getTopLevelModule();
            var = main.libraryVariables.get(qName);
            if (var == null) {
                UndeclaredVariable uvar = this.undeclaredVariables.get(qName);
                if (uvar != null) {
                    GlobalVariableReference ref = new GlobalVariableReference(qName);
                    uvar.registerReference(ref);
                    return ref;
                }
                uvar = new UndeclaredVariable();
                uvar.setPackageData(main.getPackageData());
                uvar.setVariableQName(qName);
                GlobalVariableReference ref = new GlobalVariableReference(qName);
                uvar.registerReference(ref);
                this.undeclaredVariables.put(qName, uvar);
                return ref;
            }
            if (var.isPrivate()) {
                XPathException err = new XPathException("Variable $" + qName.getDisplayName() + " is private");
                err.setErrorCode("XPST0008");
                err.setIsStaticError(true);
                throw err;
            }
        } else if (var.isPrivate() && (var.getSystemId() == null || !var.getSystemId().equals(this.getSystemId()))) {
            String message = "Variable $" + qName.getDisplayName() + " is private";
            if (var.getSystemId() == null) {
                message = message + " (no base URI known)";
            }
            XPathException err = new XPathException(message, "XPST0008");
            err.setIsStaticError(true);
            throw err;
        }
        GlobalVariableReference vref = new GlobalVariableReference(qName);
        var.registerReference(vref);
        return vref;
    }

    @Override
    public FunctionLibrary getFunctionLibrary() {
        return this.functionLibraryList;
    }

    public XQueryFunctionLibrary getLocalFunctionLibrary() {
        return (XQueryFunctionLibrary)this.functionLibraryList.get(this.localFunctionLibraryNr);
    }

    public void declareFunction(XQueryFunction function) throws XPathException {
        StructuredQName name;
        SchemaType t;
        Configuration config = this.getConfiguration();
        if (function.getNumberOfArguments() == 1 && (t = config.getSchemaType(name = function.getFunctionName())) != null && t.isAtomicType()) {
            XPathException err = new XPathException("Function name " + function.getDisplayName() + " clashes with the name of the constructor function for an atomic type");
            err.setErrorCode("XQST0034");
            err.setIsStaticError(true);
            throw err;
        }
        XQueryFunctionLibrary local = this.getLocalFunctionLibrary();
        local.declareFunction(function);
        QueryModule main = this.getTopLevelModule();
        main.globalFunctionLibrary.declareFunction(function);
    }

    public void bindUnboundFunctionCalls() throws XPathException {
        UnboundFunctionLibrary lib = (UnboundFunctionLibrary)this.functionLibraryList.get(this.unboundFunctionLibraryNr);
        lib.bindUnboundFunctionReferences(this.functionLibraryList, this.getConfiguration());
    }

    public void fixupGlobalFunctions() throws XPathException {
        this.globalFunctionLibrary.fixupGlobalFunctions(this);
    }

    public void optimizeGlobalFunctions() throws XPathException {
        this.globalFunctionLibrary.optimizeGlobalFunctions(this);
    }

    public void explainGlobalFunctions(ExpressionPresenter out) throws XPathException {
        this.globalFunctionLibrary.explainGlobalFunctions(out);
    }

    public UserFunction getUserDefinedFunction(String uri, String localName, int arity) {
        return this.globalFunctionLibrary.getUserDefinedFunction(uri, localName, arity);
    }

    public void bindUnboundVariables() throws XPathException {
        for (UndeclaredVariable uv : this.undeclaredVariables.values()) {
            XPathException err;
            String uri;
            StructuredQName qName = uv.getVariableQName();
            GlobalVariable var = this.variables.get(qName);
            if (var == null && this.importsNamespace(uri = qName.getURI())) {
                QueryModule main = this.getTopLevelModule();
                var = main.libraryVariables.get(qName);
            }
            if (var == null) {
                err = new XPathException("Unresolved reference to variable $" + uv.getVariableQName().getDisplayName());
                err.setErrorCode("XPST0008");
                err.setIsStaticError(true);
                throw err;
            }
            if (var.isPrivate() && !var.getSystemId().equals(this.getSystemId())) {
                err = new XPathException("Cannot reference a private variable in a different module");
                err.setErrorCode("XPST0008");
                err.setIsStaticError(true);
                throw err;
            }
            uv.transferReferences(var);
        }
    }

    public void addImportedSchema(String targetNamespace, String baseURI, List<String> locationURIs) {
        HashSet<String> entries;
        if (this.importedSchemata == null) {
            this.importedSchemata = new HashSet(5);
        }
        this.importedSchemata.add(targetNamespace);
        HashMap<String, HashSet<String>> loadedSchemata = this.getTopLevelModule().loadedSchemata;
        if (loadedSchemata == null) {
            loadedSchemata = new HashMap(5);
            this.getTopLevelModule().loadedSchemata = loadedSchemata;
        }
        if ((entries = loadedSchemata.get(targetNamespace)) == null) {
            entries = new HashSet(locationURIs.size());
            loadedSchemata.put(targetNamespace, entries);
        }
        for (String relative : locationURIs) {
            try {
                URI abs = ResolveURI.makeAbsolute(relative, baseURI);
                entries.add(abs.toString());
            } catch (URISyntaxException uRISyntaxException) {}
        }
    }

    @Override
    public boolean isImportedSchema(String namespace) {
        return this.importedSchemata != null && this.importedSchemata.contains(namespace);
    }

    @Override
    public Set<String> getImportedSchemaNamespaces() {
        if (this.importedSchemata == null) {
            return Collections.emptySet();
        }
        return this.importedSchemata;
    }

    public void reportStaticError(XPathException err) {
        if (!err.hasBeenReported()) {
            this.reportStaticError(new XmlProcessingException(err));
            err.setHasBeenReported(true);
        }
    }

    public void reportStaticError(XmlProcessingError err) {
        this.userQueryContext.getErrorReporter().report(err);
        if (err.getFatalErrorMessage() != null) {
            throw new XmlProcessingAbort(err.getFatalErrorMessage());
        }
    }

    @Override
    public XPathContext makeEarlyEvaluationContext() {
        return new EarlyEvaluationContext(this.getConfiguration());
    }

    @Override
    public String getDefaultCollationName() {
        if (this.defaultCollationName == null) {
            this.defaultCollationName = "http://www.w3.org/2005/xpath-functions/collation/codepoint";
        }
        return this.defaultCollationName;
    }

    public void setDefaultCollationName(String collation) {
        this.defaultCollationName = collation;
    }

    public void declarePrologNamespace(String prefix, String uri) throws XPathException {
        if (prefix == null) {
            throw new NullPointerException("Null prefix supplied to declarePrologNamespace()");
        }
        if (uri == null) {
            throw new NullPointerException("Null namespace URI supplied to declarePrologNamespace()");
        }
        if (prefix.equals("xml") != uri.equals("http://www.w3.org/XML/1998/namespace")) {
            XPathException err = new XPathException("Invalid declaration of the XML namespace");
            err.setErrorCode("XQST0070");
            err.setIsStaticError(true);
            throw err;
        }
        if (this.explicitPrologNamespaces.get(prefix) != null) {
            XPathException err = new XPathException("Duplicate declaration of namespace prefix \"" + prefix + '\"');
            err.setErrorCode("XQST0033");
            err.setIsStaticError(true);
            throw err;
        }
        this.explicitPrologNamespaces.put(prefix, uri);
    }

    public void declareActiveNamespace(String prefix, String uri) {
        if (prefix == null) {
            throw new NullPointerException("Null prefix supplied to declareActiveNamespace()");
        }
        if (uri == null) {
            throw new NullPointerException("Null namespace URI supplied to declareActiveNamespace()");
        }
        NamespaceBinding entry = new NamespaceBinding(prefix, uri);
        this.activeNamespaces.push(entry);
    }

    public void undeclareNamespace() {
        this.activeNamespaces.pop();
    }

    public NamespaceResolver getLiveNamespaceResolver() {
        return new NamespaceResolver(){

            @Override
            public String getURIForPrefix(String prefix, boolean useDefault) {
                return QueryModule.this.checkURIForPrefix(prefix);
            }

            @Override
            public Iterator<String> iteratePrefixes() {
                return QueryModule.this.getNamespaceResolver().iteratePrefixes();
            }
        };
    }

    public String checkURIForPrefix(String prefix) {
        if (this.activeNamespaces != null) {
            for (int i = this.activeNamespaces.size() - 1; i >= 0; --i) {
                if (!((NamespaceBinding)this.activeNamespaces.get(i)).getPrefix().equals(prefix)) continue;
                String uri = ((NamespaceBinding)this.activeNamespaces.get(i)).getURI();
                if (uri.equals("") && !prefix.equals("")) {
                    return null;
                }
                return uri;
            }
        }
        if (prefix.isEmpty()) {
            return this.defaultElementNamespace;
        }
        String uri = this.explicitPrologNamespaces.get(prefix);
        if (uri != null) {
            return uri.isEmpty() ? null : uri;
        }
        if (this.userQueryContext != null && (uri = this.userQueryContext.getNamespaceForPrefix(prefix)) != null) {
            return uri;
        }
        return null;
    }

    @Override
    public String getDefaultElementNamespace() {
        return this.checkURIForPrefix("");
    }

    public void setDefaultElementNamespace(String uri) {
        this.defaultElementNamespace = uri;
    }

    @Override
    public String getDefaultFunctionNamespace() {
        return this.defaultFunctionNamespace;
    }

    public void setDefaultFunctionNamespace(String uri) {
        this.defaultFunctionNamespace = uri;
    }

    public void setRevalidationMode(int mode) {
        if (mode != 1 && mode != 2 && mode != 4) {
            throw new IllegalArgumentException("Invalid mode " + mode);
        }
        this.revalidationMode = mode;
    }

    public int getRevalidationMode() {
        return this.revalidationMode;
    }

    NamespaceMap getActiveNamespaceBindings() {
        if (this.activeNamespaces == null) {
            return NamespaceMap.emptyMap();
        }
        NamespaceMap result = NamespaceMap.emptyMap();
        HashSet<String> prefixes = new HashSet<String>(10);
        for (int n = this.activeNamespaces.size() - 1; n >= 0; --n) {
            NamespaceBinding an = (NamespaceBinding)this.activeNamespaces.get(n);
            if (prefixes.contains(an.getPrefix())) continue;
            prefixes.add(an.getPrefix());
            if (an.getURI().isEmpty()) continue;
            result = result.put(an.getPrefix(), an.getURI());
        }
        return result;
    }

    @Override
    public NamespaceResolver getNamespaceResolver() {
        NamespaceMap result = NamespaceMap.emptyMap();
        HashMap<String, String> userDeclaredNamespaces = this.userQueryContext.getUserDeclaredNamespaces();
        for (Map.Entry<String, String> e : userDeclaredNamespaces.entrySet()) {
            result = result.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, String> e : this.explicitPrologNamespaces.entrySet()) {
            result = result.put(e.getKey(), e.getValue());
        }
        if (!this.defaultElementNamespace.isEmpty()) {
            result = result.put("", this.defaultElementNamespace);
        }
        if (this.activeNamespaces == null) {
            return result;
        }
        HashSet<String> prefixes = new HashSet<String>(10);
        for (int n = this.activeNamespaces.size() - 1; n >= 0; --n) {
            NamespaceBinding an = (NamespaceBinding)this.activeNamespaces.get(n);
            if (prefixes.contains(an.getPrefix())) continue;
            prefixes.add(an.getPrefix());
            result = an.getURI().isEmpty() ? result.remove(an.getPrefix()) : result.put(an.getPrefix(), an.getURI());
        }
        return result;
    }

    @Override
    public ItemType getRequiredContextItemType() {
        return this.requiredContextItemType;
    }

    @Override
    public DecimalFormatManager getDecimalFormatManager() {
        if (this.decimalFormatManager == null) {
            this.decimalFormatManager = new DecimalFormatManager(HostLanguage.XQUERY, this.getXPathVersion());
        }
        return this.decimalFormatManager;
    }

    @Override
    public void issueWarning(String s, Location locator) {
        XmlProcessingIncident err = new XmlProcessingIncident(s).asWarning();
        err.setLocation(locator);
        err.setHostLanguage(HostLanguage.XQUERY);
        this.userQueryContext.getErrorReporter().report(err);
    }

    @Override
    public boolean isInBackwardsCompatibleMode() {
        return false;
    }

    public boolean isUpdating() {
        return this.isUpdating;
    }

    @Override
    public int getXPathVersion() {
        return 31;
    }

    public CodeInjector getCodeInjector() {
        return this.codeInjector;
    }

    @Override
    public KeyManager getKeyManager() {
        return this.packageData.getKeyManager();
    }

    @Override
    public ItemType resolveTypeAlias(StructuredQName typeName) {
        return this.getPackageData().obtainTypeAliasManager().getItemType(typeName);
    }

    private static class ActiveNamespace {
        public String prefix;
        public String uri;

        private ActiveNamespace() {
        }
    }
}

