/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.transform.ErrorListener;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.parser.CodeInjector;
import net.sf.saxon.expr.parser.OptimizerOptions;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.ErrorReporterToListener;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.lib.StandardErrorReporter;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.QueryLibrary;
import net.sf.saxon.query.QueryModule;
import net.sf.saxon.query.QueryReader;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.query.XQueryParser;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.TraceCodeInjector;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.SequenceType;

public class StaticQueryContext {
    private Configuration config;
    private NamePool namePool;
    private String baseURI;
    private HashMap<String, String> userDeclaredNamespaces;
    private Set<GlobalVariable> userDeclaredVariables;
    private boolean inheritNamespaces = true;
    private boolean preserveNamespaces = true;
    private int constructionMode = 3;
    private String defaultFunctionNamespace = "http://www.w3.org/2005/xpath-functions";
    private String defaultElementNamespace = "";
    private ItemType requiredContextItemType = AnyItemType.getInstance();
    private boolean preserveSpace = false;
    private boolean defaultEmptyLeast = true;
    private ModuleURIResolver moduleURIResolver;
    private ErrorReporter errorReporter = new StandardErrorReporter();
    private CodeInjector codeInjector;
    private boolean isUpdating = false;
    private String defaultCollationName;
    private Location moduleLocation;
    private OptimizerOptions optimizerOptions;

    protected StaticQueryContext() {
    }

    public StaticQueryContext(Configuration config) {
        this(config, true);
    }

    public StaticQueryContext(Configuration config, boolean initialize) {
        this.config = config;
        this.namePool = config.getNamePool();
        if (initialize) {
            this.copyFrom(config.getDefaultStaticQueryContext());
        } else {
            this.userDeclaredNamespaces = new HashMap();
            this.userDeclaredVariables = new HashSet<GlobalVariable>();
            this.optimizerOptions = config.getOptimizerOptions();
            this.clearNamespaces();
        }
    }

    public StaticQueryContext(StaticQueryContext c) {
        this.copyFrom(c);
    }

    public static StaticQueryContext makeDefaultStaticQueryContext(Configuration config) {
        StaticQueryContext sqc = new StaticQueryContext();
        sqc.config = config;
        sqc.namePool = config.getNamePool();
        sqc.reset();
        return sqc;
    }

    public void copyFrom(StaticQueryContext c) {
        this.config = c.config;
        this.namePool = c.namePool;
        this.baseURI = c.baseURI;
        this.moduleURIResolver = c.moduleURIResolver;
        if (c.userDeclaredNamespaces != null) {
            this.userDeclaredNamespaces = new HashMap<String, String>(c.userDeclaredNamespaces);
        }
        if (c.userDeclaredVariables != null) {
            this.userDeclaredVariables = new HashSet<GlobalVariable>(c.userDeclaredVariables);
        }
        this.inheritNamespaces = c.inheritNamespaces;
        this.preserveNamespaces = c.preserveNamespaces;
        this.constructionMode = c.constructionMode;
        this.defaultElementNamespace = c.defaultElementNamespace;
        this.defaultFunctionNamespace = c.defaultFunctionNamespace;
        this.requiredContextItemType = c.requiredContextItemType;
        this.preserveSpace = c.preserveSpace;
        this.defaultEmptyLeast = c.defaultEmptyLeast;
        this.moduleURIResolver = c.moduleURIResolver;
        this.errorReporter = c.errorReporter;
        this.codeInjector = c.codeInjector;
        this.isUpdating = c.isUpdating;
        this.optimizerOptions = c.optimizerOptions;
    }

    public void reset() {
        this.userDeclaredNamespaces = new HashMap(10);
        this.errorReporter = new StandardErrorReporter();
        this.constructionMode = this.getConfiguration().isLicensedFeature(4) ? 3 : 4;
        this.preserveSpace = false;
        this.defaultEmptyLeast = true;
        this.requiredContextItemType = AnyItemType.getInstance();
        this.defaultFunctionNamespace = "http://www.w3.org/2005/xpath-functions";
        this.defaultElementNamespace = "";
        this.moduleURIResolver = null;
        this.defaultCollationName = this.config.getDefaultCollationName();
        this.clearNamespaces();
        this.isUpdating = false;
        this.optimizerOptions = this.config.getOptimizerOptions();
    }

    public void setConfiguration(Configuration config) {
        if (this.config != null && this.config != config) {
            throw new IllegalArgumentException("Configuration cannot be changed dynamically");
        }
        this.config = config;
        this.namePool = config.getNamePool();
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public Executable makeExecutable() {
        Executable executable = new Executable(this.config);
        executable.setSchemaAware(this.isSchemaAware());
        executable.setHostLanguage(HostLanguage.XQUERY);
        return executable;
    }

    public void setSchemaAware(boolean aware) {
        if (aware) {
            throw new UnsupportedOperationException("Schema-awareness requires Saxon-EE");
        }
    }

    public boolean isSchemaAware() {
        return false;
    }

    public void setStreaming(boolean option) {
        if (option) {
            throw new UnsupportedOperationException("Streaming requires Saxon-EE");
        }
    }

    public boolean isStreaming() {
        return false;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    public void setLanguageVersion(int version) {
        if (version != 10 && version != 30 && version != 31) {
            throw new IllegalArgumentException("languageVersion = " + version);
        }
    }

    public int getLanguageVersion() {
        return 31;
    }

    public FunctionLibrary getExtensionFunctionLibrary() {
        return null;
    }

    public boolean isCompileWithTracing() {
        return this.codeInjector instanceof TraceCodeInjector;
    }

    public void setCompileWithTracing(boolean trace) {
        this.codeInjector = trace ? new TraceCodeInjector() : null;
    }

    public void setCodeInjector(CodeInjector injector) {
        this.codeInjector = injector;
    }

    public CodeInjector getCodeInjector() {
        return this.codeInjector;
    }

    public boolean isUpdating() {
        return this.isUpdating;
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

    public void setModuleLocation(Location location) {
        this.moduleLocation = location;
    }

    public Location getModuleLocation() {
        return this.moduleLocation;
    }

    public void setOptimizerOptions(OptimizerOptions options) {
        this.optimizerOptions = options;
    }

    public OptimizerOptions getOptimizerOptions() {
        return this.optimizerOptions;
    }

    public XQueryExpression compileQuery(String query) throws XPathException {
        XQueryParser qp = (XQueryParser)this.config.newExpressionParser("XQ", this.isUpdating, 31);
        if (this.codeInjector != null) {
            qp.setCodeInjector(this.codeInjector);
        } else if (this.config.isCompileWithTracing()) {
            qp.setCodeInjector(new TraceCodeInjector());
        }
        qp.setStreaming(this.isStreaming());
        QueryModule mainModule = new QueryModule(this);
        return qp.makeXQueryExpression(query, mainModule, this.config);
    }

    public synchronized XQueryExpression compileQuery(Reader source) throws XPathException, IOException {
        int n;
        char[] buffer = new char[4096];
        StringBuilder sb = new StringBuilder(4096);
        while ((n = source.read(buffer)) > 0) {
            sb.append(buffer, 0, n);
        }
        return this.compileQuery(sb.toString());
    }

    public synchronized XQueryExpression compileQuery(InputStream source, String encoding) throws XPathException {
        try {
            String query = QueryReader.readInputStream(source, encoding, this.config.getValidCharacterChecker());
            return this.compileQuery(query);
        } catch (UncheckedXPathException e) {
            throw e.getXPathException();
        }
    }

    public void compileLibrary(String query) throws XPathException {
        throw new XPathException("Separate compilation of query libraries requires Saxon-EE");
    }

    public void compileLibrary(Reader source) throws XPathException, IOException {
        throw new XPathException("Separate compilation of query libraries requires Saxon-EE");
    }

    public void compileLibrary(InputStream source, String encoding) throws XPathException, IOException {
        throw new UnsupportedOperationException("Separate compilation of query libraries requires Saxon-EE");
    }

    public QueryLibrary getCompiledLibrary(String namespace) {
        return null;
    }

    public Collection<QueryLibrary> getCompiledLibraries() {
        return Collections.emptySet();
    }

    public void declareNamespace(String prefix, String uri) {
        if (prefix == null) {
            throw new NullPointerException("Null prefix supplied to declareNamespace()");
        }
        if (uri == null) {
            throw new NullPointerException("Null namespace URI supplied to declareNamespace()");
        }
        if (prefix.equals("xml") != uri.equals("http://www.w3.org/XML/1998/namespace")) {
            throw new IllegalArgumentException("Misdeclaration of XML namespace");
        }
        if (prefix.equals("xmlns") || uri.equals("http://www.w3.org/2000/xmlns/")) {
            throw new IllegalArgumentException("Misdeclaration of xmlns namespace");
        }
        if (prefix.isEmpty()) {
            this.defaultElementNamespace = uri;
        }
        if (uri.isEmpty()) {
            this.userDeclaredNamespaces.remove(prefix);
        } else {
            this.userDeclaredNamespaces.put(prefix, uri);
        }
    }

    public void clearNamespaces() {
        this.userDeclaredNamespaces.clear();
        this.declareNamespace("xml", "http://www.w3.org/XML/1998/namespace");
        this.declareNamespace("xs", "http://www.w3.org/2001/XMLSchema");
        this.declareNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        this.declareNamespace("fn", "http://www.w3.org/2005/xpath-functions");
        this.declareNamespace("local", "http://www.w3.org/2005/xquery-local-functions");
        this.declareNamespace("err", "http://www.w3.org/2005/xqt-errors");
        this.declareNamespace("saxon", "http://saxon.sf.net/");
        this.declareNamespace("", "");
    }

    protected HashMap<String, String> getUserDeclaredNamespaces() {
        return this.userDeclaredNamespaces;
    }

    public Iterator<String> iterateDeclaredPrefixes() {
        return this.userDeclaredNamespaces.keySet().iterator();
    }

    public String getNamespaceForPrefix(String prefix) {
        return this.userDeclaredNamespaces.get(prefix);
    }

    public String getDefaultFunctionNamespace() {
        return this.defaultFunctionNamespace;
    }

    public void setDefaultFunctionNamespace(String defaultFunctionNamespace) {
        this.defaultFunctionNamespace = defaultFunctionNamespace;
    }

    public void setDefaultElementNamespace(String uri) {
        this.defaultElementNamespace = uri;
        this.declareNamespace("", uri);
    }

    public String getDefaultElementNamespace() {
        return this.defaultElementNamespace;
    }

    public void declareGlobalVariable(StructuredQName qName, SequenceType type, Sequence value, boolean external) throws XPathException {
        if (value == null && !external) {
            throw new NullPointerException("No initial value for declared variable");
        }
        if (value != null && !type.matches(value, this.getConfiguration().getTypeHierarchy())) {
            throw new XPathException("Value of declared variable does not match its type");
        }
        GlobalVariable var = external ? new GlobalParam() : new GlobalVariable();
        var.setVariableQName(qName);
        var.setRequiredType(type);
        if (value != null) {
            var.setBody(Literal.makeLiteral(value.materialize()));
        }
        if (this.userDeclaredVariables == null) {
            this.userDeclaredVariables = new HashSet<GlobalVariable>();
        }
        this.userDeclaredVariables.add(var);
    }

    public Iterator<GlobalVariable> iterateDeclaredGlobalVariables() {
        if (this.userDeclaredVariables == null) {
            List empty = Collections.emptyList();
            return empty.iterator();
        }
        return this.userDeclaredVariables.iterator();
    }

    public void clearDeclaredGlobalVariables() {
        this.userDeclaredVariables = null;
    }

    public void setModuleURIResolver(ModuleURIResolver resolver) {
        this.moduleURIResolver = resolver;
    }

    public ModuleURIResolver getModuleURIResolver() {
        return this.moduleURIResolver;
    }

    public void declareDefaultCollation(String name) {
        StringCollator c;
        if (name == null) {
            throw new NullPointerException();
        }
        try {
            c = this.getConfiguration().getCollation(name);
        } catch (XPathException e) {
            c = null;
        }
        if (c == null) {
            throw new IllegalStateException("Unknown collation " + name);
        }
        this.defaultCollationName = name;
    }

    public String getDefaultCollationName() {
        return this.defaultCollationName;
    }

    public void setRequiredContextItemType(ItemType type) {
        this.requiredContextItemType = type;
    }

    public ItemType getRequiredContextItemType() {
        return this.requiredContextItemType;
    }

    public NamePool getNamePool() {
        return this.namePool;
    }

    public String getSystemId() {
        return this.baseURI;
    }

    public String getBaseURI() {
        return this.baseURI;
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

    public void setErrorListener(ErrorListener listener) {
        this.setErrorReporter(new ErrorReporterToListener(listener));
    }

    public ErrorListener getErrorListener() {
        if (this.errorReporter instanceof ErrorReporterToListener) {
            return ((ErrorReporterToListener)this.errorReporter).getErrorListener();
        }
        return null;
    }

    public void setErrorReporter(ErrorReporter reporter) {
        this.errorReporter = reporter;
    }

    public ErrorReporter getErrorReporter() {
        return this.errorReporter;
    }

    public void setUpdatingEnabled(boolean updating) {
        this.isUpdating = updating;
    }

    public boolean isUpdatingEnabled() {
        return this.isUpdating;
    }
}

