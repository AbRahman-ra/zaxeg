/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.regex.Pattern;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.AtomicSequenceConverter;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.CardinalityChecker;
import net.sf.saxon.expr.EagerLetExpression;
import net.sf.saxon.expr.EquivalenceComparison;
import net.sf.saxon.expr.ErrorExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.InstanceOfExpression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.OrExpression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.TryCatch;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.CountClause;
import net.sf.saxon.expr.flwor.FLWORExpression;
import net.sf.saxon.expr.flwor.ForClause;
import net.sf.saxon.expr.flwor.GroupByClause;
import net.sf.saxon.expr.flwor.LetClause;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.expr.flwor.OrderByClause;
import net.sf.saxon.expr.flwor.TupleExpression;
import net.sf.saxon.expr.flwor.WhereClause;
import net.sf.saxon.expr.flwor.WindowClause;
import net.sf.saxon.expr.instruct.AttributeCreator;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.expr.instruct.Comment;
import net.sf.saxon.expr.instruct.ComputedAttribute;
import net.sf.saxon.expr.instruct.ComputedElement;
import net.sf.saxon.expr.instruct.CopyOf;
import net.sf.saxon.expr.instruct.DocumentInstr;
import net.sf.saxon.expr.instruct.ElementCreator;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.FixedAttribute;
import net.sf.saxon.expr.instruct.FixedElement;
import net.sf.saxon.expr.instruct.GlobalContextRequirement;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.NamespaceConstructor;
import net.sf.saxon.expr.instruct.ParentNodeConstructor;
import net.sf.saxon.expr.instruct.ProcessingInstruction;
import net.sf.saxon.expr.instruct.ResultDocument;
import net.sf.saxon.expr.instruct.SimpleNodeConstructor;
import net.sf.saxon.expr.instruct.TraceExpression;
import net.sf.saxon.expr.instruct.UserFunctionParameter;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.parser.Tokenizer;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.expr.sort.GenericAtomicComparer;
import net.sf.saxon.expr.sort.SortKeyDefinition;
import net.sf.saxon.functions.ExecutableFunctionLibrary;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.functions.StringJoin;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.registry.ConstructorFunctionLibrary;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.StandardURIChecker;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.om.QNameParser;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.pattern.UnionQNameTest;
import net.sf.saxon.query.Annotation;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.query.QueryLibrary;
import net.sf.saxon.query.QueryModule;
import net.sf.saxon.query.QueryReader;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.query.XQueryFunction;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.CharacterMap;
import net.sf.saxon.serialize.CharacterMapIndex;
import net.sf.saxon.serialize.SerializationParamsHandler;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.trans.DecimalFormatManager;
import net.sf.saxon.trans.DecimalSymbols;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.NamespaceResolverWithDefault;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.QualifiedNameValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;
import net.sf.saxon.z.IntHashSet;

public class XQueryParser
extends XPathParser {
    public static final String XQUERY10 = "1.0";
    public static final String XQUERY30 = "3.0";
    public static final String XQUERY31 = "3.1";
    private boolean memoFunction = false;
    private boolean streaming = false;
    private int errorCount = 0;
    private XPathException firstError = null;
    protected Executable executable;
    private boolean foundCopyNamespaces = false;
    private boolean foundBoundarySpaceDeclaration = false;
    private boolean foundOrderingDeclaration = false;
    private boolean foundEmptyOrderingDeclaration = false;
    private boolean foundDefaultCollation = false;
    private boolean foundConstructionDeclaration = false;
    private boolean foundDefaultFunctionNamespace = false;
    private boolean foundDefaultElementNamespace = false;
    private boolean foundBaseURIDeclaration = false;
    private boolean foundContextItemDeclaration = false;
    private boolean foundDefaultDecimalFormat = false;
    private boolean preambleProcessed = false;
    public final Set<String> importedModules = new HashSet<String>(5);
    final List<String> namespacesToBeSealed = new ArrayList<String>(10);
    final List<Import> schemaImports = new ArrayList<Import>(5);
    final List<Import> moduleImports = new ArrayList<Import>(5);
    private final Set<StructuredQName> outputPropertiesSeen = new HashSet<StructuredQName>(4);
    private Properties parameterDocProperties;
    private static final Pattern encNamePattern = Pattern.compile("^[A-Za-z]([A-Za-z0-9._\\x2D])*$");
    public static final StructuredQName SAXON_MEMO_FUNCTION = new StructuredQName("saxon", "http://saxon.sf.net/", "memo-function");

    public XQueryParser() {
        this.setLanguage(XPathParser.ParsedLanguage.XQUERY, 31);
    }

    private XQueryParser newParser() {
        XQueryParser qp = new XQueryParser();
        qp.setLanguage(this.language, 31);
        qp.setParserExtension(this.parserExtension);
        return qp;
    }

    public XQueryExpression makeXQueryExpression(String query, QueryModule mainModule, Configuration config) throws XPathException {
        try {
            GlobalContextRequirement requirement;
            this.setLanguage(XPathParser.ParsedLanguage.XQUERY, 31);
            query = config.getXMLVersion() == 10 ? XQueryParser.normalizeLineEndings10(query) : XQueryParser.normalizeLineEndings11(query);
            Executable exec = mainModule.getExecutable();
            if (exec == null) {
                exec = new Executable(config);
                exec.setHostLanguage(HostLanguage.XQUERY);
                exec.setTopLevelPackage(mainModule.getPackageData());
                this.setExecutable(exec);
            }
            if ((requirement = exec.getGlobalContextRequirement()) != null) {
                requirement.addRequiredItemType(mainModule.getRequiredContextItemType());
            } else if (mainModule.getRequiredContextItemType() != null && mainModule.getRequiredContextItemType() != AnyItemType.getInstance()) {
                GlobalContextRequirement req = new GlobalContextRequirement();
                req.setExternal(true);
                req.addRequiredItemType(mainModule.getRequiredContextItemType());
                exec.setGlobalContextRequirement(req);
            }
            Properties outputProps = new Properties(config.getDefaultSerializationProperties());
            if (outputProps.getProperty("method") == null) {
                outputProps.setProperty("method", "xml");
            }
            this.parameterDocProperties = new Properties(outputProps);
            exec.setDefaultOutputProperties(new Properties(this.parameterDocProperties));
            FunctionLibraryList libList = new FunctionLibraryList();
            libList.addFunctionLibrary(new ExecutableFunctionLibrary(config));
            exec.setFunctionLibrary(libList);
            this.setExecutable(exec);
            this.setCodeInjector(mainModule.getCodeInjector());
            Expression exp = this.parseQuery(query, mainModule);
            if (this.streaming) {
                this.env.getConfiguration().checkLicensedFeature(4, "streaming", -1);
            }
            exec.fixupQueryModules(mainModule);
            FunctionLibraryList userlib = exec.getFunctionLibrary();
            FunctionLibraryList lib = new FunctionLibraryList();
            lib.addFunctionLibrary(mainModule.getBuiltInFunctionSet());
            lib.addFunctionLibrary(config.getBuiltInExtensionLibraryList());
            lib.addFunctionLibrary(new ConstructorFunctionLibrary(config));
            lib.addFunctionLibrary(config.getIntegratedFunctionLibrary());
            lib.addFunctionLibrary(mainModule.getGlobalFunctionLibrary());
            config.addExtensionBinders(lib);
            lib.addFunctionLibrary(userlib);
            exec.setFunctionLibrary(lib);
            XQueryExpression queryExp = config.makeXQueryExpression(exp, mainModule, this.streaming);
            return queryExp;
        } catch (XPathException e) {
            if (!e.hasBeenReported()) {
                this.reportError(e);
            }
            throw e;
        }
    }

    private static String normalizeLineEndings11(String in) {
        if (in.indexOf(13) < 0 && in.indexOf(133) < 0 && in.indexOf(8232) < 0) {
            return in;
        }
        FastStringBuffer sb = new FastStringBuffer(in.length());
        block4: for (int i = 0; i < in.length(); ++i) {
            char ch = in.charAt(i);
            switch (ch) {
                case '\u0085': 
                case '\u2028': {
                    sb.cat('\n');
                    continue block4;
                }
                case '\r': {
                    if (i < in.length() - 1 && (in.charAt(i + 1) == '\n' || in.charAt(i + 1) == '\u0085')) {
                        sb.cat('\n');
                        ++i;
                        continue block4;
                    }
                    sb.cat('\n');
                    continue block4;
                }
                default: {
                    sb.cat(ch);
                }
            }
        }
        return sb.toString();
    }

    private static String normalizeLineEndings10(String in) {
        if (in.indexOf(13) < 0) {
            return in;
        }
        FastStringBuffer sb = new FastStringBuffer(in.length());
        for (int i = 0; i < in.length(); ++i) {
            char ch = in.charAt(i);
            if (ch == '\r') {
                if (i < in.length() - 1 && in.charAt(i + 1) == '\n') {
                    sb.cat('\n');
                    ++i;
                    continue;
                }
                sb.cat('\n');
                continue;
            }
            sb.cat(ch);
        }
        return sb.toString();
    }

    public Executable getExecutable() {
        return this.executable;
    }

    public void setExecutable(Executable exec) {
        this.executable = exec;
    }

    @Override
    protected void customizeTokenizer(Tokenizer t) {
        t.isXQuery = true;
    }

    public void setStreaming(boolean option) {
        this.streaming = option;
    }

    public boolean isStreaming() {
        return this.streaming;
    }

    private Expression parseQuery(String queryString, QueryModule env) throws XPathException {
        this.env = Objects.requireNonNull(env);
        this.charChecker = env.getConfiguration().getValidCharacterChecker();
        this.language = XPathParser.ParsedLanguage.XQUERY;
        this.t = new Tokenizer();
        this.t.languageLevel = 31;
        this.t.isXQuery = true;
        try {
            this.t.tokenize(Objects.requireNonNull(queryString), 0, -1);
        } catch (XPathException err) {
            this.grumble(err.getMessage());
        }
        this.parseVersionDeclaration();
        this.allowSaxonExtensions = this.t.allowSaxonExtensions = env.getConfiguration().getBooleanProperty(Feature.ALLOW_SYNTAX_EXTENSIONS);
        QNameParser qp = new QNameParser(env.getLiveNamespaceResolver()).withAcceptEQName(true).withUnescaper(new Unescaper(env.getConfiguration().getValidCharacterChecker()));
        this.setQNameParser(qp);
        this.parseProlog();
        this.processPreamble();
        Expression exp = this.parseExpression();
        exp = this.makeTracer(exp, null);
        if (this.t.currentToken != 0) {
            this.grumble("Unexpected token " + this.currentTokenDisplay() + " beyond end of query");
        }
        this.setLocation(exp);
        ExpressionTool.setDeepRetainedStaticContext(exp, env.makeRetainedStaticContext());
        if (this.errorCount == 0) {
            return exp;
        }
        XPathException err = new XPathException("One or more static errors were reported during query analysis");
        err.setHasBeenReported(true);
        err.setErrorCodeQName(this.firstError.getErrorCodeQName());
        throw err;
    }

    public final void parseLibraryModule(String queryString, QueryModule env) throws XPathException {
        this.env = env;
        Configuration config = env.getConfiguration();
        this.charChecker = config.getValidCharacterChecker();
        queryString = config.getXMLVersion() == 10 ? XQueryParser.normalizeLineEndings10(queryString) : XQueryParser.normalizeLineEndings11(queryString);
        Executable exec = env.getExecutable();
        if (exec == null) {
            throw new IllegalStateException("Query library module has no associated Executable");
        }
        this.executable = exec;
        this.t = new Tokenizer();
        this.t.languageLevel = 31;
        this.t.isXQuery = true;
        QNameParser qp = new QNameParser(env.getLiveNamespaceResolver()).withAcceptEQName(true).withUnescaper(new Unescaper(config.getValidCharacterChecker()));
        this.setQNameParser(qp);
        try {
            this.t.tokenize(queryString, 0, -1);
        } catch (XPathException err) {
            this.grumble(err.getMessage());
        }
        this.parseVersionDeclaration();
        this.parseModuleDeclaration();
        this.parseProlog();
        this.processPreamble();
        if (this.t.currentToken != 0) {
            this.grumble("Unrecognized content found after the variable and function declarations in a library module");
        }
        if (this.errorCount != 0) {
            err = new XPathException("Static errors were reported in the imported library module");
            err.setErrorCodeQName(this.firstError.getErrorCodeQName());
            throw err;
        }
    }

    private void reportError(XPathException exception) throws XPathException {
        ++this.errorCount;
        if (this.firstError == null) {
            this.firstError = exception;
        }
        ((QueryModule)this.env).reportStaticError(exception);
        throw exception;
    }

    private void parseVersionDeclaration() throws XPathException {
        if (this.t.currentToken == 88) {
            this.nextToken();
            this.expect(202);
            String queryVersion = this.unescape(this.t.currentTokenValue).toString();
            Object[] allowedVersions = new String[]{XQUERY10, XQUERY30, XQUERY31};
            if (Arrays.binarySearch(allowedVersions, queryVersion) < 0) {
                this.grumble("Invalid XQuery version " + queryVersion, "XQST0031");
            }
            this.nextToken();
            if ("encoding".equals(this.t.currentTokenValue)) {
                this.nextToken();
                this.expect(202);
                if (!encNamePattern.matcher(this.unescape(this.t.currentTokenValue)).matches()) {
                    this.grumble("Encoding name contains invalid characters", "XQST0087");
                }
                this.nextToken();
            }
            this.expect(149);
            this.nextToken();
        } else if (this.t.currentToken == 89) {
            this.nextToken();
            this.expect(202);
            if (!encNamePattern.matcher(this.t.currentTokenValue).matches()) {
                this.grumble("Encoding name contains invalid characters", "XQST0087");
            }
            this.nextToken();
            this.expect(149);
            this.nextToken();
        }
    }

    private void parseModuleDeclaration() throws XPathException {
        this.expect(101);
        this.nextToken();
        this.expect(201);
        String prefix = this.t.currentTokenValue;
        this.nextToken();
        this.expect(6);
        this.nextToken();
        this.expect(202);
        String uri = this.uriLiteral(this.t.currentTokenValue);
        this.checkProhibitedPrefixes(prefix, uri);
        if (uri.isEmpty()) {
            this.grumble("Module namespace cannot be \"\"", "XQST0088");
            uri = "http://saxon.fallback.namespace/";
        }
        this.nextToken();
        this.expect(149);
        this.nextToken();
        try {
            ((QueryModule)this.env).setModuleNamespace(uri);
            ((QueryModule)this.env).declarePrologNamespace(prefix, uri);
            this.executable.addQueryLibraryModule((QueryModule)this.env);
        } catch (XPathException err) {
            err.setLocator(this.makeLocation());
            this.reportError(err);
        }
    }

    private void parseProlog() throws XPathException {
        boolean allowModuleDecl = true;
        boolean allowDeclarations = true;
        while (true) {
            try {
                while (true) {
                    if (this.t.currentToken == 101) {
                        String uri = ((QueryModule)this.env).getModuleNamespace();
                        if (uri == null) {
                            this.grumble("Module declaration must not be used in a main module");
                        } else {
                            this.grumble("Module declaration appears more than once");
                        }
                        if (!allowModuleDecl) {
                            this.grumble("Module declaration must precede other declarations in the query prolog");
                        }
                    }
                    allowModuleDecl = false;
                    block1 : switch (this.t.currentToken) {
                        case 90: {
                            if (!allowDeclarations) {
                                this.grumble("Namespace declarations cannot follow variables, functions, or options");
                            }
                            this.parseNamespaceDeclaration();
                            break;
                        }
                        case 123: {
                            this.processPreamble();
                            if (allowDeclarations) {
                                this.sealNamespaces(this.namespacesToBeSealed, this.env.getConfiguration());
                                allowDeclarations = false;
                            }
                            this.nextToken();
                            this.expect(106);
                            AnnotationList annotationList = this.parseAnnotationsList();
                            if (this.isKeyword("function")) {
                                annotationList.check(this.env.getConfiguration(), "DF");
                                this.parseFunctionDeclaration(annotationList);
                                break;
                            }
                            if (this.isKeyword("variable")) {
                                annotationList.check(this.env.getConfiguration(), "DV");
                                this.parseVariableDeclaration(annotationList);
                                break;
                            }
                            this.grumble("Annotations can appear only in 'declare variable' and 'declare function'");
                            break;
                        }
                        case 91: {
                            this.nextToken();
                            this.expect(201);
                            switch (this.t.currentTokenValue) {
                                case "element": {
                                    if (!allowDeclarations) {
                                        this.grumble("Namespace declarations cannot follow variables, functions, or options");
                                    }
                                    this.parseDefaultElementNamespace();
                                    break block1;
                                }
                                case "function": {
                                    if (!allowDeclarations) {
                                        this.grumble("Namespace declarations cannot follow variables, functions, or options");
                                    }
                                    this.parseDefaultFunctionNamespace();
                                    break block1;
                                }
                                case "collation": {
                                    if (!allowDeclarations) {
                                        this.grumble("Collation declarations must appear earlier in the prolog");
                                    }
                                    this.parseDefaultCollation();
                                    break block1;
                                }
                                case "order": {
                                    if (!allowDeclarations) {
                                        this.grumble("Order declarations must appear earlier in the prolog");
                                    }
                                    this.parseDefaultOrder();
                                    break block1;
                                }
                                case "decimal-format": {
                                    this.nextToken();
                                    this.parseDefaultDecimalFormat();
                                    break block1;
                                }
                            }
                            this.grumble("After 'declare default', expected 'element', 'function', or 'collation'");
                            break;
                        }
                        case 94: {
                            if (!allowDeclarations) {
                                this.grumble("'declare boundary-space' must appear earlier in the query prolog");
                            }
                            this.parseBoundarySpaceDeclaration();
                            break;
                        }
                        case 107: {
                            if (!allowDeclarations) {
                                this.grumble("'declare ordering' must appear earlier in the query prolog");
                            }
                            this.parseOrderingDeclaration();
                            break;
                        }
                        case 108: {
                            if (!allowDeclarations) {
                                this.grumble("'declare copy-namespaces' must appear earlier in the query prolog");
                            }
                            this.parseCopyNamespacesDeclaration();
                            break;
                        }
                        case 93: {
                            if (!allowDeclarations) {
                                this.grumble("'declare base-uri' must appear earlier in the query prolog");
                            }
                            this.parseBaseURIDeclaration();
                            break;
                        }
                        case 95: {
                            if (!allowDeclarations) {
                                this.grumble("'declare decimal-format' must appear earlier in the query prolog");
                            }
                            this.parseDecimalFormatDeclaration();
                            break;
                        }
                        case 96: {
                            if (!allowDeclarations) {
                                this.grumble("Import schema must appear earlier in the prolog");
                            }
                            this.parseSchemaImport();
                            break;
                        }
                        case 97: {
                            if (!allowDeclarations) {
                                this.grumble("Import module must appear earlier in the prolog");
                            }
                            this.parseModuleImport();
                            break;
                        }
                        case 98: {
                            if (allowDeclarations) {
                                this.sealNamespaces(this.namespacesToBeSealed, this.env.getConfiguration());
                                allowDeclarations = false;
                            }
                            this.processPreamble();
                            this.parseVariableDeclaration(AnnotationList.EMPTY);
                            break;
                        }
                        case 99: {
                            if (allowDeclarations) {
                                this.sealNamespaces(this.namespacesToBeSealed, this.env.getConfiguration());
                                allowDeclarations = false;
                            }
                            this.processPreamble();
                            this.parseContextItemDeclaration();
                            break;
                        }
                        case 100: {
                            if (allowDeclarations) {
                                this.sealNamespaces(this.namespacesToBeSealed, this.env.getConfiguration());
                                allowDeclarations = false;
                            }
                            this.processPreamble();
                            this.parseFunctionDeclaration(AnnotationList.EMPTY);
                            break;
                        }
                        case 122: {
                            this.nextToken();
                            if (!this.isKeyword("function")) {
                                this.grumble("expected 'function' after 'declare updating");
                            }
                            if (allowDeclarations) {
                                this.sealNamespaces(this.namespacesToBeSealed, this.env.getConfiguration());
                                allowDeclarations = false;
                            }
                            this.processPreamble();
                            this.parserExtension.parseUpdatingFunctionDeclaration(this);
                            break;
                        }
                        case 109: {
                            if (allowDeclarations) {
                                this.sealNamespaces(this.namespacesToBeSealed, this.env.getConfiguration());
                                allowDeclarations = false;
                            }
                            this.parseOptionDeclaration();
                            break;
                        }
                        case 124: {
                            this.checkSyntaxExtensions("declare type");
                            if (allowDeclarations) {
                                this.sealNamespaces(this.namespacesToBeSealed, this.env.getConfiguration());
                                allowDeclarations = false;
                            }
                            this.parseTypeAliasDeclaration();
                            break;
                        }
                        case 92: {
                            if (!allowDeclarations) {
                                this.grumble("'declare construction' must appear earlier in the query prolog");
                            }
                            this.parseConstructionDeclaration();
                            break;
                        }
                        case 110: {
                            if (!allowDeclarations) {
                                this.grumble("'declare revalidation' must appear earlier in the query prolog");
                            }
                            this.parserExtension.parseRevalidationDeclaration(this);
                            break;
                        }
                        case 0: {
                            String uri = ((QueryModule)this.env).getModuleNamespace();
                            if (uri == null) {
                                this.grumble("The main module must contain a query expression after any declarations in the prolog");
                                break;
                            }
                            return;
                        }
                        default: {
                            return;
                        }
                    }
                    this.expect(149);
                    this.nextToken();
                }
            } catch (XPathException err) {
                if (err.getLocator() == null) {
                    err.setLocator(this.makeLocation());
                }
                if (!err.hasBeenReported()) {
                    ++this.errorCount;
                    if (this.firstError == null) {
                        this.firstError = err;
                    }
                    this.reportError(err);
                }
                while (this.t.currentToken != 149) {
                    this.nextToken();
                    if (this.t.currentToken == 0) {
                        return;
                    }
                    if (this.t.currentToken == 215) {
                        this.t.lookAhead();
                        continue;
                    }
                    if (this.t.currentToken != 217) continue;
                    this.parsePseudoXML(true);
                }
                this.nextToken();
                continue;
            }
            break;
        }
    }

    @Override
    protected AnnotationList parseAnnotationsList() throws XPathException {
        ArrayList<Annotation> annotations = new ArrayList<Annotation>();
        int options = 0;
        do {
            StructuredQName qName;
            String uri;
            this.t.setState(1);
            this.nextToken();
            this.expect(201);
            this.t.setState(0);
            if (this.t.currentTokenValue.indexOf(58) < 0) {
                uri = "http://www.w3.org/2012/xquery";
                qName = new StructuredQName("", uri, this.t.currentTokenValue);
            } else {
                qName = this.makeStructuredQName(this.t.currentTokenValue, "");
                assert (qName != null);
                uri = qName.getURI();
            }
            Annotation annotation = new Annotation(qName);
            if (uri.equals("http://www.w3.org/2012/xquery")) {
                if (!(qName.equals(Annotation.PRIVATE) || qName.equals(Annotation.PUBLIC) || qName.equals(Annotation.UPDATING) || qName.equals(Annotation.SIMPLE))) {
                    this.grumble("Unrecognized variable or function annotation " + qName.getDisplayName(), "XQST0045");
                }
                annotation.addAnnotationParameter(new Int64Value(options));
            } else if (this.isReservedInQuery(uri)) {
                this.grumble("The annotation " + this.t.currentTokenValue + " is in a reserved namespace", "XQST0045");
            } else if (uri.equals("")) {
                this.grumble("The annotation " + this.t.currentTokenValue + " is in no namespace", "XQST0045");
            }
            this.nextToken();
            if (this.t.currentToken == 5) {
                this.nextToken();
                if (this.t.currentToken == 204) {
                    this.grumble("Annotation parameter list cannot be empty");
                }
                while (true) {
                    Literal arg;
                    switch (this.t.currentToken) {
                        case 202: {
                            arg = (Literal)this.parseStringLiteral(false);
                            break;
                        }
                        case 209: {
                            arg = (Literal)this.parseNumericLiteral(false);
                            break;
                        }
                        default: {
                            this.grumble("Annotation parameter must be a literal");
                            return null;
                        }
                    }
                    GroundedValue val = arg.getValue();
                    if (val instanceof StringValue || val instanceof NumericValue) {
                        annotation.addAnnotationParameter((AtomicValue)val);
                    } else {
                        this.grumble("Annotation parameter must be a string or number");
                    }
                    if (this.t.currentToken == 204) {
                        this.nextToken();
                        break;
                    }
                    this.expect(7);
                    this.nextToken();
                }
            }
            annotations.add(annotation);
        } while (this.t.currentToken == 106);
        return new AnnotationList(annotations);
    }

    private void sealNamespaces(List namespacesToBeSealed, Configuration config) {
        for (Object aNamespacesToBeSealed : namespacesToBeSealed) {
            String ns = (String)aNamespacesToBeSealed;
            config.sealNamespace(ns);
        }
    }

    private void processPreamble() throws XPathException {
        if (this.preambleProcessed) {
            return;
        }
        this.preambleProcessed = true;
        if (this.foundDefaultCollation) {
            String collationName = this.env.getDefaultCollationName();
            try {
                URI collationURI = new URI(collationName);
                if (!collationURI.isAbsolute()) {
                    URI base = new URI(this.env.getStaticBaseURI());
                    collationURI = base.resolve(collationURI);
                    collationName = collationURI.toString();
                }
            } catch (URISyntaxException err) {
                this.grumble("Default collation name '" + collationName + "' is not a valid URI", "XQST0046");
                collationName = "http://www.w3.org/2005/xpath-functions/collation/codepoint";
            }
            if (this.env.getConfiguration().getCollation(collationName) == null) {
                this.grumble("Default collation name '" + collationName + "' is not a recognized collation", "XQST0038");
                collationName = "http://www.w3.org/2005/xpath-functions/collation/codepoint";
            }
            ((QueryModule)this.env).setDefaultCollationName(collationName);
        }
        for (Import imp : this.schemaImports) {
            try {
                this.applySchemaImport(imp);
            } catch (XPathException err) {
                if (err.hasBeenReported()) continue;
                err.maybeSetLocation(this.makeLocation(imp.offset));
                throw err;
            }
        }
        for (Import imp : this.moduleImports) {
            try {
                this.applyModuleImport(imp);
            } catch (XPathException err) {
                if (err.hasBeenReported()) continue;
                err.maybeSetLocation(this.makeLocation(imp.offset));
                throw err;
            }
        }
    }

    private void parseDefaultCollation() throws XPathException {
        if (this.foundDefaultCollation) {
            this.grumble("default collation appears more than once", "XQST0038");
        }
        this.foundDefaultCollation = true;
        this.nextToken();
        this.expect(202);
        String uri = this.uriLiteral(this.t.currentTokenValue);
        ((QueryModule)this.env).setDefaultCollationName(uri);
        this.nextToken();
    }

    private void parseDefaultOrder() throws XPathException {
        if (this.foundEmptyOrderingDeclaration) {
            this.grumble("empty ordering declaration appears more than once", "XQST0069");
        }
        this.foundEmptyOrderingDeclaration = true;
        this.nextToken();
        if (!this.isKeyword("empty")) {
            this.grumble("After 'declare default order', expected keyword 'empty'");
        }
        this.nextToken();
        if (this.isKeyword("least")) {
            ((QueryModule)this.env).setEmptyLeast(true);
        } else if (this.isKeyword("greatest")) {
            ((QueryModule)this.env).setEmptyLeast(false);
        } else {
            this.grumble("After 'declare default order empty', expected keyword 'least' or 'greatest'");
        }
        this.nextToken();
    }

    private void parseBoundarySpaceDeclaration() throws XPathException {
        if (this.foundBoundarySpaceDeclaration) {
            this.grumble("'declare boundary-space' appears more than once", "XQST0068");
        }
        this.foundBoundarySpaceDeclaration = true;
        this.nextToken();
        this.expect(201);
        if ("preserve".equals(this.t.currentTokenValue)) {
            ((QueryModule)this.env).setPreserveBoundarySpace(true);
        } else if ("strip".equals(this.t.currentTokenValue)) {
            ((QueryModule)this.env).setPreserveBoundarySpace(false);
        } else {
            this.grumble("boundary-space must be 'preserve' or 'strip'");
        }
        this.nextToken();
    }

    private void parseOrderingDeclaration() throws XPathException {
        if (this.foundOrderingDeclaration) {
            this.grumble("ordering mode declaration appears more than once", "XQST0065");
        }
        this.foundOrderingDeclaration = true;
        this.nextToken();
        this.expect(201);
        if (!"ordered".equals(this.t.currentTokenValue) && !"unordered".equals(this.t.currentTokenValue)) {
            this.grumble("ordering mode must be 'ordered' or 'unordered'");
        }
        this.nextToken();
    }

    private void parseCopyNamespacesDeclaration() throws XPathException {
        if (this.foundCopyNamespaces) {
            this.grumble("declare copy-namespaces appears more than once", "XQST0055");
        }
        this.foundCopyNamespaces = true;
        this.nextToken();
        this.expect(201);
        if ("preserve".equals(this.t.currentTokenValue)) {
            ((QueryModule)this.env).setPreserveNamespaces(true);
        } else if ("no-preserve".equals(this.t.currentTokenValue)) {
            ((QueryModule)this.env).setPreserveNamespaces(false);
        } else {
            this.grumble("copy-namespaces must be followed by 'preserve' or 'no-preserve'");
        }
        this.nextToken();
        this.expect(7);
        this.nextToken();
        this.expect(201);
        if ("inherit".equals(this.t.currentTokenValue)) {
            ((QueryModule)this.env).setInheritNamespaces(true);
        } else if ("no-inherit".equals(this.t.currentTokenValue)) {
            ((QueryModule)this.env).setInheritNamespaces(false);
        } else {
            this.grumble("After the comma in the copy-namespaces declaration, expected 'inherit' or 'no-inherit'");
        }
        this.nextToken();
    }

    private void parseConstructionDeclaration() throws XPathException {
        int val;
        if (this.foundConstructionDeclaration) {
            this.grumble("declare construction appears more than once", "XQST0067");
        }
        this.foundConstructionDeclaration = true;
        this.nextToken();
        this.expect(201);
        if ("preserve".equals(this.t.currentTokenValue)) {
            val = 3;
        } else if ("strip".equals(this.t.currentTokenValue)) {
            val = 4;
        } else {
            this.grumble("construction mode must be 'preserve' or 'strip'");
            val = 4;
        }
        ((QueryModule)this.env).setConstructionMode(val);
        this.nextToken();
    }

    protected void parseRevalidationDeclaration() throws XPathException {
        this.grumble("declare revalidation is allowed only in XQuery Update");
    }

    private void parseSchemaImport() throws XPathException {
        this.ensureSchemaAware("import schema");
        Import sImport = new Import();
        String prefix = null;
        sImport.namespaceURI = null;
        sImport.locationURIs = new ArrayList<String>(5);
        sImport.offset = this.t.currentTokenStartOffset;
        this.nextToken();
        if (this.isKeyword("namespace")) {
            prefix = this.readNamespaceBinding();
        } else if (this.isKeyword("default")) {
            this.nextToken();
            if (!this.isKeyword("element")) {
                this.grumble("In 'import schema', expected 'element namespace'");
            }
            this.nextToken();
            if (!this.isKeyword("namespace")) {
                this.grumble("In 'import schema', expected keyword 'namespace'");
            }
            this.nextToken();
            prefix = "";
        }
        if (this.t.currentToken == 202) {
            String uri = this.uriLiteral(this.t.currentTokenValue);
            this.checkProhibitedPrefixes(prefix, uri);
            sImport.namespaceURI = uri;
            this.nextToken();
            if (this.isKeyword("at")) {
                this.nextToken();
                this.expect(202);
                sImport.locationURIs.add(this.uriLiteral(this.t.currentTokenValue));
                this.nextToken();
                while (this.t.currentToken == 7) {
                    this.nextToken();
                    this.expect(202);
                    sImport.locationURIs.add(this.uriLiteral(this.t.currentTokenValue));
                    this.nextToken();
                }
            } else if (this.t.currentToken != 149) {
                this.grumble("After the target namespace URI, expected 'at' or ';'");
            }
        } else {
            this.grumble("After 'import schema', expected 'namespace', 'default', or a string-literal");
        }
        if (prefix != null) {
            try {
                if (prefix.isEmpty()) {
                    ((QueryModule)this.env).setDefaultElementNamespace(sImport.namespaceURI);
                } else {
                    if (sImport.namespaceURI == null || "".equals(sImport.namespaceURI)) {
                        this.grumble("A prefix cannot be bound to the null namespace", "XQST0057");
                    }
                    ((QueryModule)this.env).declarePrologNamespace(prefix, sImport.namespaceURI);
                }
            } catch (XPathException err) {
                err.setLocator(this.makeLocation());
                this.reportError(err);
            }
        }
        Iterator<Import> iterator = this.schemaImports.iterator();
        while (iterator.hasNext()) {
            Import schemaImport;
            Import imp = schemaImport = iterator.next();
            if (!imp.namespaceURI.equals(sImport.namespaceURI)) continue;
            this.grumble("Schema namespace '" + sImport.namespaceURI + "' is imported more than once", "XQST0058");
            break;
        }
        this.schemaImports.add(sImport);
    }

    private String readNamespaceBinding() throws XPathException {
        this.t.setState(0);
        this.nextToken();
        this.expect(201);
        String prefix = this.t.currentTokenValue;
        this.nextToken();
        this.expect(6);
        this.nextToken();
        return prefix;
    }

    protected void ensureSchemaAware(String featureName) throws XPathException {
        if (!this.env.getConfiguration().isLicensedFeature(4)) {
            throw new XPathException("This Saxon version and license does not allow use of '" + featureName + "'", "XQST0009");
        }
        this.env.getConfiguration().checkLicensedFeature(4, featureName, -1);
        this.getExecutable().setSchemaAware(true);
        this.getStaticContext().getPackageData().setSchemaAware(true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void applySchemaImport(Import sImport) throws XPathException {
        Configuration config;
        Configuration configuration = config = this.env.getConfiguration();
        synchronized (configuration) {
            if (!config.isSchemaAvailable(sImport.namespaceURI)) {
                if (!sImport.locationURIs.isEmpty()) {
                    try {
                        PipelineConfiguration pipe = config.makePipelineConfiguration();
                        config.readMultipleSchemas(pipe, this.env.getStaticBaseURI(), sImport.locationURIs, sImport.namespaceURI);
                        this.namespacesToBeSealed.add(sImport.namespaceURI);
                    } catch (SchemaException err) {
                        this.grumble("Error in schema " + sImport.namespaceURI + ": " + err.getMessage(), "XQST0059", sImport.offset);
                    }
                } else if (sImport.namespaceURI.equals("http://www.w3.org/XML/1998/namespace") || sImport.namespaceURI.equals("http://www.w3.org/2005/xpath-functions") || sImport.namespaceURI.equals("http://www.w3.org/2001/XMLSchema-instance")) {
                    config.addSchemaForBuiltInNamespace(sImport.namespaceURI);
                } else {
                    this.grumble("Unable to locate requested schema " + sImport.namespaceURI, "XQST0059", sImport.offset);
                }
            }
            ((QueryModule)this.env).addImportedSchema(sImport.namespaceURI, this.env.getStaticBaseURI(), sImport.locationURIs);
        }
    }

    private void parseModuleImport() throws XPathException {
        QueryModule thisModule = (QueryModule)this.env;
        Import mImport = new Import();
        String prefix = null;
        mImport.namespaceURI = null;
        mImport.locationURIs = new ArrayList<String>(5);
        mImport.offset = this.t.currentTokenStartOffset;
        this.nextToken();
        if (this.t.currentToken == 201 && this.t.currentTokenValue.equals("namespace")) {
            prefix = this.readNamespaceBinding();
        }
        if (this.t.currentToken == 202) {
            String uri = this.uriLiteral(this.t.currentTokenValue);
            this.checkProhibitedPrefixes(prefix, uri);
            mImport.namespaceURI = uri;
            if (mImport.namespaceURI.isEmpty()) {
                this.grumble("Imported module namespace cannot be \"\"", "XQST0088");
                mImport.namespaceURI = "http://saxon.fallback.namespace/line" + this.t.getLineNumber();
            }
            if (this.importedModules.contains(mImport.namespaceURI)) {
                this.grumble("Two 'import module' declarations specify the same module namespace", "XQST0047");
            }
            this.importedModules.add(mImport.namespaceURI);
            ((QueryModule)this.env).addImportedNamespace(mImport.namespaceURI);
            this.nextToken();
            if (this.isKeyword("at")) {
                do {
                    this.nextToken();
                    this.expect(202);
                    mImport.locationURIs.add(this.uriLiteral(this.t.currentTokenValue));
                    this.nextToken();
                } while (this.t.currentToken == 7);
            }
        } else {
            this.grumble("After 'import module', expected 'namespace' or a string-literal");
        }
        if (prefix != null) {
            try {
                if (!mImport.namespaceURI.equals(thisModule.getModuleNamespace()) || !mImport.namespaceURI.equals(thisModule.checkURIForPrefix(prefix))) {
                    thisModule.declarePrologNamespace(prefix, mImport.namespaceURI);
                }
            } catch (XPathException err) {
                err.setLocator(this.makeLocation());
                this.reportError(err);
            }
        }
        this.moduleImports.add(mImport);
    }

    public void applyModuleImport(Import mImport) throws XPathException {
        List<QueryModule> list;
        List<Object> existingModules;
        for (int i = 0; i < mImport.locationURIs.size(); ++i) {
            try {
                String uri = mImport.locationURIs.get(i);
                URI abs = ResolveURI.makeAbsolute(uri, this.env.getStaticBaseURI());
                mImport.locationURIs.set(i, abs.toString());
                continue;
            } catch (URISyntaxException e) {
                this.grumble("Invalid URI " + mImport.locationURIs.get(i) + ": " + e.getMessage(), "XQST0046", mImport.offset);
            }
        }
        QueryLibrary lib = ((QueryModule)this.env).getUserQueryContext().getCompiledLibrary(mImport.namespaceURI);
        if (lib != null) {
            this.executable.addQueryLibraryModule(lib);
            existingModules = new ArrayList<QueryLibrary>();
            existingModules.add(lib);
            lib.link((QueryModule)this.env);
        } else if (!this.env.getConfiguration().getBooleanProperty(Feature.XQUERY_MULTIPLE_MODULE_IMPORTS)) {
            List<QueryModule> list2 = this.executable.getQueryLibraryModules(mImport.namespaceURI);
            if (list2 != null && !list2.isEmpty()) {
                return;
            }
        } else {
            for (int h = mImport.locationURIs.size() - 1; h >= 0; --h) {
                if (!this.executable.isQueryLocationHintProcessed(mImport.locationURIs.get(h))) continue;
                mImport.locationURIs.remove(h);
            }
        }
        if (mImport.locationURIs.isEmpty() && (list = this.executable.getQueryLibraryModules(mImport.namespaceURI)) != null && !list.isEmpty()) {
            return;
        }
        ModuleURIResolver resolver = ((QueryModule)this.env).getUserQueryContext().getModuleURIResolver();
        String[] hints = new String[mImport.locationURIs.size()];
        for (int h = 0; h < hints.length; ++h) {
            hints[h] = mImport.locationURIs.get(h);
        }
        StreamSource[] sources = null;
        if (resolver != null) {
            try {
                sources = resolver.resolve(mImport.namespaceURI, this.env.getStaticBaseURI(), hints);
            } catch (XPathException err) {
                this.grumble("Failed to resolve URI of imported module: " + err.getMessage(), "XQST0059", mImport.offset);
            }
        }
        if (sources == null) {
            if (hints.length == 0) {
                this.grumble("Cannot locate module for namespace " + mImport.namespaceURI, "XQST0059", mImport.offset);
            }
            resolver = this.env.getConfiguration().getStandardModuleURIResolver();
            sources = resolver.resolve(mImport.namespaceURI, this.env.getStaticBaseURI(), hints);
        }
        for (String hint : mImport.locationURIs) {
            this.executable.addQueryLocationHintProcessed(hint);
        }
        for (int m = 0; m < sources.length; ++m) {
            StreamSource ss = sources[m];
            String baseURI = ss.getSystemId();
            if (baseURI == null) {
                if (m < hints.length) {
                    baseURI = hints[m];
                    ss.setSystemId(hints[m]);
                } else {
                    this.grumble("No base URI available for imported module", "XQST0059", mImport.offset);
                }
            }
            existingModules = this.executable.getQueryLibraryModules(mImport.namespaceURI);
            boolean loaded = false;
            if (existingModules != null && m < hints.length) {
                for (QueryModule queryModule : existingModules) {
                    URI uri = queryModule.getLocationURI();
                    if (uri == null || !uri.toString().equals(mImport.locationURIs.get(m))) continue;
                    loaded = true;
                    break;
                }
            }
            if (loaded) break;
            try {
                String queryText = QueryReader.readSourceQuery(ss, this.charChecker);
                try {
                    if (ss.getInputStream() != null) {
                        ss.getInputStream().close();
                    } else if (ss.getReader() != null) {
                        ss.getReader().close();
                    }
                } catch (IOException iOException) {
                    throw new XPathException("Failure while closing file for imported query module");
                }
                QueryModule.makeQueryModule(baseURI, this.executable, (QueryModule)this.env, queryText, mImport.namespaceURI);
                continue;
            } catch (XPathException err) {
                err.maybeSetLocation(this.makeLocation());
                this.reportError(err);
            }
        }
    }

    private void parseBaseURIDeclaration() throws XPathException {
        if (this.foundBaseURIDeclaration) {
            this.grumble("Base URI Declaration may only appear once", "XQST0032");
        }
        this.foundBaseURIDeclaration = true;
        this.nextToken();
        this.expect(202);
        String uri = this.uriLiteral(this.t.currentTokenValue);
        try {
            URI baseURI = new URI(uri);
            if (!baseURI.isAbsolute()) {
                String oldBase = this.env.getStaticBaseURI();
                uri = ResolveURI.makeAbsolute(uri, oldBase).toString();
            }
            ((QueryModule)this.env).setBaseURI(uri);
        } catch (URISyntaxException err) {
            ((QueryModule)this.env).setBaseURI(uri);
        }
        this.nextToken();
    }

    private void parseDecimalFormatDeclaration() throws XPathException {
        this.nextToken();
        this.expect(201);
        StructuredQName formatName = this.makeStructuredQName(this.t.currentTokenValue, "");
        if (this.env.getDecimalFormatManager().getNamedDecimalFormat(formatName) != null) {
            this.grumble("Duplicate declaration of decimal-format " + formatName.getDisplayName(), "XQST0111");
        }
        this.nextToken();
        this.parseDecimalFormatProperties(formatName);
    }

    private void parseDefaultDecimalFormat() throws XPathException {
        if (this.foundDefaultDecimalFormat) {
            this.grumble("Duplicate declaration of default decimal-format", "XQST0111");
        }
        this.foundDefaultDecimalFormat = true;
        this.parseDecimalFormatProperties(null);
    }

    private void parseDecimalFormatProperties(StructuredQName formatName) throws XPathException {
        int outerOffset = this.t.currentTokenStartOffset;
        DecimalFormatManager dfm = this.env.getDecimalFormatManager();
        DecimalSymbols dfs = formatName == null ? dfm.getDefaultDecimalFormat() : dfm.obtainNamedDecimalFormat(formatName);
        dfs.setHostLanguage(HostLanguage.XQUERY, 31);
        HashSet<String> propertyNames = new HashSet<String>(10);
        block30: while (this.t.currentToken != 149) {
            int offset = this.t.currentTokenStartOffset;
            String propertyName = this.t.currentTokenValue;
            if (propertyNames.contains(propertyName)) {
                this.grumble("Property name " + propertyName + " is defined more than once", "XQST0114", offset);
            }
            this.nextToken();
            this.expect(6);
            this.nextToken();
            this.expect(202);
            String propertyValue = this.unescape(this.t.currentTokenValue).toString();
            this.nextToken();
            propertyNames.add(propertyName);
            switch (propertyName) {
                case "decimal-separator": {
                    dfs.setDecimalSeparator(propertyValue);
                    continue block30;
                }
                case "grouping-separator": {
                    dfs.setGroupingSeparator(propertyValue);
                    continue block30;
                }
                case "infinity": {
                    dfs.setInfinity(propertyValue);
                    continue block30;
                }
                case "minus-sign": {
                    dfs.setMinusSign(propertyValue);
                    continue block30;
                }
                case "NaN": {
                    dfs.setNaN(propertyValue);
                    continue block30;
                }
                case "percent": {
                    dfs.setPercent(propertyValue);
                    continue block30;
                }
                case "per-mille": {
                    dfs.setPerMille(propertyValue);
                    continue block30;
                }
                case "zero-digit": {
                    try {
                        dfs.setZeroDigit(propertyValue);
                        continue block30;
                    } catch (XPathException err) {
                        err.setErrorCode("XQST0097");
                        throw err;
                    }
                }
                case "digit": {
                    dfs.setDigit(propertyValue);
                    continue block30;
                }
                case "pattern-separator": {
                    dfs.setPatternSeparator(propertyValue);
                    continue block30;
                }
                case "exponent-separator": {
                    dfs.setExponentSeparator(propertyValue);
                    continue block30;
                }
            }
            this.grumble("Unknown decimal-format property: " + propertyName, "XPST0003", offset);
        }
        try {
            dfs.checkConsistency(formatName);
        } catch (XPathException err) {
            this.grumble(err.getMessage(), "XQST0098", outerOffset);
        }
    }

    private void parseDefaultFunctionNamespace() throws XPathException {
        if (this.foundDefaultFunctionNamespace) {
            this.grumble("default function namespace appears more than once", "XQST0066");
        }
        this.foundDefaultFunctionNamespace = true;
        this.nextToken();
        this.expect(201);
        if (!"namespace".equals(this.t.currentTokenValue)) {
            this.grumble("After 'declare default function', expected 'namespace'");
        }
        this.nextToken();
        this.expect(202);
        String uri = this.uriLiteral(this.t.currentTokenValue);
        if (uri.equals("http://www.w3.org/XML/1998/namespace") || uri.equals("http://www.w3.org/2000/xmlns/")) {
            this.grumble("Reserved namespace used as default element/type namespace", "XQST0070");
        }
        ((QueryModule)this.env).setDefaultFunctionNamespace(uri);
        this.nextToken();
    }

    private void parseDefaultElementNamespace() throws XPathException {
        if (this.foundDefaultElementNamespace) {
            this.grumble("default element namespace appears more than once", "XQST0066");
        }
        this.foundDefaultElementNamespace = true;
        this.nextToken();
        this.expect(201);
        if (!"namespace".equals(this.t.currentTokenValue)) {
            this.grumble("After 'declare default element', expected 'namespace'");
        }
        this.nextToken();
        this.expect(202);
        String uri = this.uriLiteral(this.t.currentTokenValue);
        if (uri.equals("http://www.w3.org/XML/1998/namespace") || uri.equals("http://www.w3.org/2000/xmlns/")) {
            this.grumble("Reserved namespace used as default element/type namespace", "XQST0070");
        }
        ((QueryModule)this.env).setDefaultElementNamespace(uri);
        this.nextToken();
    }

    private void parseNamespaceDeclaration() throws XPathException {
        this.nextToken();
        this.expect(201);
        String prefix = this.t.currentTokenValue;
        if (!NameChecker.isValidNCName(prefix)) {
            this.grumble("Invalid namespace prefix " + Err.wrap(prefix));
        }
        this.nextToken();
        this.expect(6);
        this.nextToken();
        this.expect(202);
        String uri = this.uriLiteral(this.t.currentTokenValue);
        this.checkProhibitedPrefixes(prefix, uri);
        if ("xml".equals(prefix)) {
            this.grumble("Namespace prefix 'xml' cannot be declared", "XQST0070");
        }
        try {
            ((QueryModule)this.env).declarePrologNamespace(prefix, uri);
        } catch (XPathException err) {
            err.setLocator(this.makeLocation());
            this.reportError(err);
        }
        this.nextToken();
    }

    private void checkProhibitedPrefixes(String prefix, String uri) throws XPathException {
        if (prefix != null && !prefix.isEmpty() && !NameChecker.isValidNCName(prefix)) {
            this.grumble("The namespace prefix " + Err.wrap(prefix) + " is not a valid NCName");
        }
        if (prefix == null) {
            prefix = "";
        }
        if (uri == null) {
            uri = "";
        }
        if ("xmlns".equals(prefix)) {
            this.grumble("The namespace prefix 'xmlns' cannot be redeclared", "XQST0070");
        }
        if (uri.equals("http://www.w3.org/2000/xmlns/")) {
            this.grumble("The xmlns namespace URI is reserved", "XQST0070");
        }
        if (uri.equals("http://www.w3.org/XML/1998/namespace") && !prefix.equals("xml")) {
            this.grumble("The XML namespace cannot be bound to any prefix other than 'xml'", "XQST0070");
        }
        if (prefix.equals("xml") && !uri.equals("http://www.w3.org/XML/1998/namespace")) {
            this.grumble("The prefix 'xml' cannot be bound to any namespace other than http://www.w3.org/XML/1998/namespace", "XQST0070");
        }
    }

    private void parseVariableDeclaration(AnnotationList annotations) throws XPathException {
        int offset = this.t.currentTokenStartOffset;
        GlobalVariable var = new GlobalVariable();
        var.setPackageData(this.env.getPackageData());
        var.setLineNumber(this.t.getLineNumber() + 1);
        var.setSystemId(this.env.getSystemId());
        if (annotations != null) {
            var.setPrivate(annotations.includes(Annotation.PRIVATE));
        }
        this.nextToken();
        this.expect(21);
        this.t.setState(1);
        this.nextToken();
        this.expect(201);
        String varName = this.t.currentTokenValue;
        StructuredQName varQName = this.makeStructuredQName(this.t.currentTokenValue, "");
        assert (varQName != null);
        var.setVariableQName(varQName);
        String uri = varQName.getURI();
        String moduleURI = ((QueryModule)this.env).getModuleNamespace();
        if (moduleURI != null && !moduleURI.equals(uri)) {
            this.grumble("A variable declared in a library module must be in the module namespace", "XQST0048", offset);
        }
        this.nextToken();
        SequenceType requiredType = SequenceType.ANY_SEQUENCE;
        if (this.t.currentToken == 71) {
            this.t.setState(2);
            this.nextToken();
            requiredType = this.parseSequenceType();
        }
        var.setRequiredType(requiredType);
        if (this.t.currentToken == 58) {
            this.t.setState(0);
            this.nextToken();
            Expression exp = this.parseExprSingle();
            var.setBody(this.makeTracer(exp, varQName));
        } else if (this.t.currentToken == 201) {
            if ("external".equals(this.t.currentTokenValue)) {
                GlobalParam par = new GlobalParam();
                par.setPackageData(this.env.getPackageData());
                par.setLineNumber(var.getLineNumber());
                par.setSystemId(var.getSystemId());
                par.setVariableQName(var.getVariableQName());
                par.setRequiredType(var.getRequiredType());
                var = par;
                this.nextToken();
                if (this.t.currentToken == 58) {
                    this.t.setState(0);
                    this.nextToken();
                    Expression exp = this.parseExprSingle();
                    var.setBody(this.makeTracer(exp, varQName));
                }
            } else {
                this.grumble("Variable must either be initialized or be declared as external");
            }
        } else {
            this.grumble("Expected ':=' or 'external' in variable declaration");
        }
        QueryModule qenv = (QueryModule)this.env;
        RetainedStaticContext rsc = this.env.makeRetainedStaticContext();
        var.setRetainedStaticContext(rsc);
        if (var.getBody() != null) {
            ExpressionTool.setDeepRetainedStaticContext(var.getBody(), rsc);
        }
        if (qenv.getModuleNamespace() != null && !uri.equals(qenv.getModuleNamespace())) {
            this.grumble("Variable " + Err.wrap(varName, 5) + " is not defined in the module namespace");
        }
        try {
            qenv.declareVariable(var);
        } catch (XPathException e) {
            this.grumble(e.getMessage(), e.getErrorCodeQName(), -1);
        }
    }

    private void parseContextItemDeclaration() throws XPathException {
        RoleDiagnostic role;
        Expression exp;
        int offset = this.t.currentTokenStartOffset;
        this.nextToken();
        if (!this.isKeyword("item")) {
            this.grumble("After 'declare context', expected 'item'");
        }
        if (this.foundContextItemDeclaration) {
            this.grumble("More than one context item declaration found", "XQST0099", offset);
        }
        this.foundContextItemDeclaration = true;
        GlobalContextRequirement req = new GlobalContextRequirement();
        req.setAbsentFocus(false);
        this.t.setState(1);
        this.nextToken();
        ItemType requiredType = AnyItemType.getInstance();
        if (this.t.currentToken == 71) {
            this.t.setState(2);
            this.nextToken();
            requiredType = this.parseItemType();
        }
        req.addRequiredItemType(requiredType);
        if (this.t.currentToken == 58) {
            if (!((QueryModule)this.env).isMainModule()) {
                this.grumble("The context item must not be initialized in a library module", "XQST0113");
            }
            this.t.setState(0);
            this.nextToken();
            exp = this.parseExprSingle();
            exp.setRetainedStaticContext(this.env.makeRetainedStaticContext());
            role = new RoleDiagnostic(13, "context item declaration", 0);
            exp = CardinalityChecker.makeCardinalityChecker(exp, 16384, role);
            ExpressionVisitor visitor = ExpressionVisitor.make(this.env);
            exp = exp.simplify();
            ContextItemStaticInfo info = this.env.getConfiguration().makeContextItemStaticInfo(AnyItemType.getInstance(), true);
            exp.setRetainedStaticContext(this.env.makeRetainedStaticContext());
            exp = exp.typeCheck(visitor, info);
            req.setDefaultValue(exp);
            req.setExternal(false);
        } else if (this.t.currentToken == 201 && "external".equals(this.t.currentTokenValue)) {
            req.setAbsentFocus(false);
            req.setExternal(true);
            this.nextToken();
            if (this.t.currentToken == 58) {
                if (!((QueryModule)this.env).isMainModule()) {
                    this.grumble("The context item must not be initialized in a library module", "XQST0113");
                }
                this.t.setState(0);
                this.nextToken();
                exp = this.parseExprSingle();
                role = new RoleDiagnostic(13, "context item declaration", 0);
                exp = CardinalityChecker.makeCardinalityChecker(exp, 16384, role);
                exp.setRetainedStaticContext(this.env.makeRetainedStaticContext());
                req.setDefaultValue(exp);
            }
        } else {
            this.grumble("Expected ':=' or 'external' in context item declaration");
        }
        Executable exec = this.getExecutable();
        if (exec.getGlobalContextRequirement() != null) {
            GlobalContextRequirement gcr = exec.getGlobalContextRequirement();
            if (gcr.getDefaultValue() == null && req.getDefaultValue() != null) {
                gcr.setDefaultValue(req.getDefaultValue());
            }
            for (ItemType otherType : gcr.getRequiredItemTypes()) {
                TypeHierarchy th;
                Affinity rel;
                if (otherType == AnyItemType.getInstance() || (rel = (th = this.env.getConfiguration().getTypeHierarchy()).relationship(requiredType, otherType)) != Affinity.DISJOINT) continue;
                this.grumble("Different modules specify incompatible requirements for the type of the initial context item", "XPTY0004");
            }
            gcr.addRequiredItemType(requiredType);
        } else {
            exec.setGlobalContextRequirement(req);
        }
    }

    /*
     * WARNING - void declaration
     */
    public void parseFunctionDeclaration(AnnotationList annotations) throws XPathException {
        UserFunctionParameter[] params;
        String moduleURI;
        StructuredQName qName;
        String uri;
        if (annotations.includes(SAXON_MEMO_FUNCTION)) {
            if (this.env.getConfiguration().getEditionCode().equals("HE")) {
                this.warning("saxon:memo-function option is ignored under Saxon-HE");
            } else {
                this.memoFunction = true;
            }
        }
        int offset = this.t.currentTokenStartOffset;
        this.t.setState(0);
        this.nextToken();
        this.expect(35);
        if (this.t.currentTokenValue.indexOf(58) < 0) {
            uri = this.env.getDefaultFunctionNamespace();
            qName = new StructuredQName("", uri, this.t.currentTokenValue);
        } else {
            qName = this.makeStructuredQName(this.t.currentTokenValue, "");
            uri = qName.getURI();
        }
        if (uri.isEmpty()) {
            this.grumble("The function must be in a namespace", "XQST0060");
        }
        if ((moduleURI = ((QueryModule)this.env).getModuleNamespace()) != null && !moduleURI.equals(uri)) {
            this.grumble("A function in a library module must be in the module namespace", "XQST0048");
        }
        if (this.isReservedInQuery(uri)) {
            this.grumble("The function name " + this.t.currentTokenValue + " is in a reserved namespace", "XQST0045");
        }
        XQueryFunction func = new XQueryFunction();
        func.setFunctionName(qName);
        func.setResultType(SequenceType.ANY_SEQUENCE);
        func.setBody(null);
        Location loc = this.makeNestedLocation(this.env.getContainingLocation(), this.t.getLineNumber(offset), this.t.getColumnNumber(offset), null);
        func.setLocation(loc);
        func.setStaticContext((QueryModule)this.env);
        func.setMemoFunction(this.memoFunction);
        func.setUpdating(annotations.includes(Annotation.UPDATING));
        func.setAnnotations(annotations);
        this.nextToken();
        HashSet<StructuredQName> paramNames = new HashSet<StructuredQName>(8);
        boolean external = false;
        if (this.t.currentToken != 204) {
            while (true) {
                void var11_11;
                this.expect(21);
                this.nextToken();
                this.expect(201);
                StructuredQName argQName = this.makeStructuredQName(this.t.currentTokenValue, "");
                if (paramNames.contains(argQName)) {
                    this.grumble("Duplicate parameter name " + Err.wrap(this.t.currentTokenValue, 5), "XQST0039");
                }
                paramNames.add(argQName);
                SequenceType sequenceType = SequenceType.ANY_SEQUENCE;
                this.nextToken();
                if (this.t.currentToken == 71) {
                    this.nextToken();
                    SequenceType sequenceType2 = this.parseSequenceType();
                }
                UserFunctionParameter arg = new UserFunctionParameter();
                arg.setRequiredType((SequenceType)var11_11);
                arg.setVariableQName(argQName);
                func.addArgument(arg);
                this.declareRangeVariable(arg);
                if (this.t.currentToken == 204) break;
                if (this.t.currentToken == 7) {
                    this.nextToken();
                    continue;
                }
                this.grumble("Expected ',' or ')' after function argument, found '" + Token.tokens[this.t.currentToken] + '\'');
            }
        }
        this.t.setState(1);
        this.nextToken();
        if (this.t.currentToken == 71) {
            if (func.isUpdating()) {
                this.grumble("Cannot specify a return type for an updating function", "XUST0028");
            }
            this.t.setState(2);
            this.nextToken();
            func.setResultType(this.parseSequenceType());
        }
        if (this.isKeyword("external")) {
            external = true;
        } else {
            Expression body;
            this.expect(59);
            this.t.setState(0);
            this.nextToken();
            if (this.t.currentToken == 215) {
                body = Literal.makeEmptySequence();
                body.setRetainedStaticContext(this.env.makeRetainedStaticContext());
                this.setLocation(body);
                func.setBody(body);
            } else {
                body = this.parseExpression();
                func.setBody(body);
                ExpressionTool.setDeepRetainedStaticContext(body, this.env.makeRetainedStaticContext());
            }
            this.expect(215);
            this.lookAhead();
        }
        for (UserFunctionParameter param : params = func.getParameterDefinitions()) {
            this.undeclareRangeVariable();
        }
        this.t.setState(0);
        this.nextToken();
        QueryModule queryModule = (QueryModule)this.env;
        if (external) {
            this.parserExtension.handleExternalFunctionDeclaration(this, func);
        } else {
            try {
                queryModule.declareFunction(func);
            } catch (XPathException e) {
                this.grumble(e.getMessage(), e.getErrorCodeQName(), -1);
            }
        }
        this.memoFunction = false;
    }

    protected void parseTypeAliasDeclaration() throws XPathException {
        this.parserExtension.parseTypeAliasDeclaration(this);
    }

    private void parseOptionDeclaration() throws XPathException {
        this.nextToken();
        this.expect(201);
        String defaultUri = "http://www.w3.org/2012/xquery";
        StructuredQName varName = this.makeStructuredQName(this.t.currentTokenValue, defaultUri);
        assert (varName != null);
        String uri = varName.getURI();
        if (uri.isEmpty()) {
            this.grumble("The QName identifying an option declaration must be prefixed", "XPST0081");
            return;
        }
        this.nextToken();
        this.expect(202);
        String value = this.unescape(this.t.currentTokenValue).toString();
        if (uri.equals("http://www.w3.org/2010/xslt-xquery-serialization")) {
            this.parseOutputDeclaration(varName, value);
        } else if (uri.equals("http://saxon.sf.net/")) {
            String localName;
            block5 : switch (localName = varName.getLocalPart()) {
                case "output": {
                    this.setOutputProperty(value);
                    break;
                }
                case "memo-function": {
                    switch (value = value.trim()) {
                        case "true": {
                            this.memoFunction = true;
                            if (!this.env.getConfiguration().getEditionCode().equals("HE")) break block5;
                            this.warning("saxon:memo-function option is ignored under Saxon-HE");
                            break;
                        }
                        case "false": {
                            this.memoFunction = false;
                            break;
                        }
                        default: {
                            this.warning("Value of saxon:memo-function must be 'true' or 'false'");
                            break;
                        }
                    }
                    break;
                }
                case "allow-cycles": {
                    this.warning("Value of saxon:allow-cycles is ignored");
                    break;
                }
                default: {
                    this.warning("Unknown Saxon option declaration: " + varName.getDisplayName());
                }
            }
        }
        this.nextToken();
    }

    protected void parseOutputDeclaration(StructuredQName varName, String value) throws XPathException {
        if (!((QueryModule)this.env).isMainModule()) {
            this.grumble("Output declarations must not appear in a library module", "XQST0108");
        }
        String localName = varName.getLocalPart();
        if (this.outputPropertiesSeen.contains(varName)) {
            this.grumble("Duplicate output declaration (" + varName + ")", "XQST0110");
        }
        this.outputPropertiesSeen.add(varName);
        switch (localName) {
            case "parameter-document": {
                Source source;
                try {
                    source = this.env.getConfiguration().getURIResolver().resolve(value, this.env.getStaticBaseURI());
                } catch (TransformerException e) {
                    throw XPathException.makeXPathException(e);
                }
                ParseOptions options = new ParseOptions();
                options.setSchemaValidationMode(2);
                options.setDTDValidationMode(4);
                TreeInfo doc = this.env.getConfiguration().buildDocumentTree(source);
                SerializationParamsHandler ph = new SerializationParamsHandler(this.parameterDocProperties);
                ph.setSerializationParams(doc.getRootNode());
                CharacterMap characterMap = ph.getCharacterMap();
                if (characterMap == null) break;
                CharacterMapIndex index = new CharacterMapIndex();
                index.putCharacterMap(characterMap.getName(), characterMap);
                this.getExecutable().setCharacterMapIndex(index);
                this.parameterDocProperties.setProperty("use-character-maps", characterMap.getName().getClarkName());
                break;
            }
            case "use-character-maps": {
                this.grumble("Output declaration use-character-maps cannot appear except in a parameter file", "XQST0109");
                break;
            }
            default: {
                Properties props = this.getExecutable().getPrimarySerializationProperties().getProperties();
                ResultDocument.setSerializationProperty(props, "", localName, value, this.env.getNamespaceResolver(), false, this.env.getConfiguration());
                break;
            }
        }
    }

    private void setOutputProperty(String property) {
        int equals = property.indexOf("=");
        if (equals < 0) {
            this.badOutputProperty("no equals sign");
        } else if (equals == 0) {
            this.badOutputProperty("starts with '=");
        }
        String keyword = Whitespace.trim(property.substring(0, equals));
        String value = equals == property.length() - 1 ? "" : Whitespace.trim(property.substring(equals + 1));
        Properties props = this.getExecutable().getPrimarySerializationProperties().getProperties();
        try {
            StructuredQName name = this.makeStructuredQName(keyword, "");
            String lname = name.getLocalPart();
            String uri = name.getURI();
            ResultDocument.setSerializationProperty(props, uri, lname, value, this.env.getNamespaceResolver(), false, this.env.getConfiguration());
        } catch (XPathException e) {
            this.badOutputProperty(e.getMessage());
        }
    }

    private void badOutputProperty(String s) {
        this.warning("Invalid serialization property (" + s + ")");
    }

    @Override
    protected Expression parseFLWORExpression() throws XPathException {
        FLWORExpression flwor = new FLWORExpression();
        int exprOffset = this.t.currentTokenStartOffset;
        ArrayList<Clause> clauseList = new ArrayList<Clause>(4);
        while (true) {
            int offset = this.t.currentTokenStartOffset;
            if (this.t.currentToken == 211) {
                this.parseForClause(flwor, clauseList);
            } else if (this.t.currentToken == 216) {
                this.parseLetClause(flwor, clauseList);
            } else if (this.t.currentToken == 220) {
                this.parseCountClause(clauseList);
            } else if (this.t.currentToken == 72) {
                this.parseGroupByClause(flwor, clauseList);
            } else if (this.t.currentToken == 73 || this.t.currentToken == 74) {
                this.parseWindowClause(flwor, clauseList);
            } else if (this.t.currentToken == 28 || this.isKeyword("where")) {
                this.nextToken();
                Expression condition = this.parseExprSingle();
                WhereClause clause = new WhereClause(flwor, condition);
                clause.setRepeated(XQueryParser.containsLoopingClause(clauseList));
                clauseList.add(clause);
            } else {
                if (!this.isKeyword("stable") && !this.isKeyword("order")) break;
                if (this.isKeyword("stable")) {
                    this.nextToken();
                    if (!this.isKeyword("order")) {
                        this.grumble("'stable' must be followed by 'order by'");
                    }
                }
                TupleExpression tupleExpression = new TupleExpression();
                ArrayList<LocalVariableReference> vars = new ArrayList<LocalVariableReference>();
                for (Clause c : clauseList) {
                    for (LocalVariableBinding b : c.getRangeVariables()) {
                        vars.add(new LocalVariableReference(b));
                    }
                }
                tupleExpression.setVariables(vars);
                this.t.setState(1);
                this.nextToken();
                if (!this.isKeyword("by")) {
                    this.grumble("'order' must be followed by 'by'");
                }
                this.t.setState(0);
                this.nextToken();
                List sortSpecList = this.parseSortDefinition();
                SortKeyDefinition[] keys = new SortKeyDefinition[sortSpecList.size()];
                for (int i = 0; i < keys.length; ++i) {
                    SortSpec spec = (SortSpec)sortSpecList.get(i);
                    SortKeyDefinition key = new SortKeyDefinition();
                    key.setSortKey(((SortSpec)sortSpecList.get((int)i)).sortKey, false);
                    key.setOrder(new StringLiteral(spec.ascending ? "ascending" : "descending"));
                    key.setEmptyLeast(spec.emptyLeast);
                    if (spec.collation != null) {
                        StringCollator comparator = this.env.getConfiguration().getCollation(spec.collation);
                        if (comparator == null) {
                            this.grumble("Unknown collation '" + spec.collation + '\'', "XQST0076");
                        }
                        key.setCollation(comparator);
                    }
                    keys[i] = key;
                }
                OrderByClause clause = new OrderByClause(flwor, keys, tupleExpression);
                clause.setRepeated(XQueryParser.containsLoopingClause(clauseList));
                clauseList.add(clause);
            }
            this.setLocation((Clause)clauseList.get(clauseList.size() - 1), offset);
        }
        int returnOffset = this.t.currentTokenStartOffset;
        this.expect(25);
        this.t.setState(0);
        this.nextToken();
        Expression returnExpression = this.parseExprSingle();
        returnExpression = this.makeTracer(returnExpression, null);
        for (int i = clauseList.size() - 1; i >= 0; --i) {
            Clause clause = (Clause)clauseList.get(i);
            for (int n = 0; n < clause.getRangeVariables().length; ++n) {
                this.undeclareRangeVariable();
            }
        }
        flwor.init(clauseList, returnExpression);
        this.setLocation(flwor, exprOffset);
        return flwor;
    }

    protected LetExpression makeLetExpression() {
        if (((QueryModule)this.env).getUserQueryContext().isCompileWithTracing()) {
            return new EagerLetExpression();
        }
        return new LetExpression();
    }

    protected static boolean containsLoopingClause(List<Clause> clauseList) {
        for (Clause c : clauseList) {
            if (!FLWORExpression.isLoopingClause(c)) continue;
            return true;
        }
        return false;
    }

    private void parseForClause(FLWORExpression flwor, List<Clause> clauseList) throws XPathException {
        boolean first = true;
        do {
            ForClause clause = new ForClause();
            clause.setRepeated(!first || XQueryParser.containsLoopingClause(clauseList));
            this.setLocation(clause, this.t.currentTokenStartOffset);
            if (first) {
                // empty if block
            }
            clauseList.add(clause);
            this.nextToken();
            if (first) {
                first = false;
            }
            this.expect(21);
            this.nextToken();
            this.expect(201);
            StructuredQName varQName = this.makeStructuredQName(this.t.currentTokenValue, "");
            SequenceType type = SequenceType.SINGLE_ITEM;
            this.nextToken();
            boolean explicitType = false;
            if (this.t.currentToken == 71) {
                explicitType = true;
                this.nextToken();
                type = this.parseSequenceType();
            }
            boolean allowingEmpty = false;
            if (this.isKeyword("allowing")) {
                allowingEmpty = true;
                clause.setAllowingEmpty(true);
                if (!explicitType) {
                    type = SequenceType.OPTIONAL_ITEM;
                }
                this.nextToken();
                if (!this.isKeyword("empty")) {
                    this.grumble("After 'allowing', expected 'empty'");
                }
                this.nextToken();
            }
            if (explicitType && !allowingEmpty && type.getCardinality() != 16384) {
                this.warning("Occurrence indicator on singleton range variable has no effect");
                type = SequenceType.makeSequenceType(type.getPrimaryType(), 16384);
            }
            LocalVariableBinding binding = new LocalVariableBinding(varQName, type);
            clause.setRangeVariable(binding);
            if (this.isKeyword("at")) {
                this.nextToken();
                this.expect(21);
                this.nextToken();
                this.expect(201);
                StructuredQName posQName = this.makeStructuredQName(this.t.currentTokenValue, "");
                if (!this.scanOnly && posQName.equals(varQName)) {
                    this.grumble("The two variables declared in a single 'for' clause must have different names", "XQST0089");
                }
                LocalVariableBinding pos = new LocalVariableBinding(posQName, SequenceType.SINGLE_INTEGER);
                clause.setPositionVariable(pos);
                this.nextToken();
            }
            this.expect(31);
            this.nextToken();
            clause.initSequence(flwor, this.parseExprSingle());
            this.declareRangeVariable(clause.getRangeVariable());
            if (clause.getPositionVariable() != null) {
                this.declareRangeVariable(clause.getPositionVariable());
            }
            if (!allowingEmpty) continue;
            this.checkForClauseAllowingEmpty(flwor, clause);
        } while (this.t.currentToken == 7);
    }

    private void checkForClauseAllowingEmpty(FLWORExpression flwor, ForClause clause) throws XPathException {
        SequenceType type;
        if (!this.allowXPath30Syntax) {
            this.grumble("The 'allowing empty' option requires XQuery 3.0");
        }
        if (!Cardinality.allowsZero((type = clause.getRangeVariable().getRequiredType()).getCardinality())) {
            this.warning("When 'allowing empty' is specified, the occurrence indicator on the range variable type should be '?'");
        }
    }

    private void parseLetClause(FLWORExpression flwor, List<Clause> clauseList) throws XPathException {
        boolean first = true;
        do {
            LetClause clause = new LetClause();
            this.setLocation(clause, this.t.currentTokenStartOffset);
            clause.setRepeated(XQueryParser.containsLoopingClause(clauseList));
            if (first) {
                // empty if block
            }
            clauseList.add(clause);
            this.nextToken();
            if (first) {
                first = false;
            }
            this.expect(21);
            this.nextToken();
            this.expect(201);
            String var = this.t.currentTokenValue;
            StructuredQName varQName = this.makeStructuredQName(var, "");
            SequenceType type = SequenceType.ANY_SEQUENCE;
            this.nextToken();
            if (this.t.currentToken == 71) {
                this.nextToken();
                type = this.parseSequenceType();
            }
            LocalVariableBinding v = new LocalVariableBinding(varQName, type);
            this.expect(58);
            this.nextToken();
            clause.initSequence(flwor, this.parseExprSingle());
            clause.setRangeVariable(v);
            this.declareRangeVariable(v);
        } while (this.t.currentToken == 7);
    }

    private void parseCountClause(List<Clause> clauseList) throws XPathException {
        do {
            CountClause clause = new CountClause();
            this.setLocation(clause, this.t.currentTokenStartOffset);
            clause.setRepeated(XQueryParser.containsLoopingClause(clauseList));
            clauseList.add(clause);
            this.nextToken();
            this.expect(21);
            this.nextToken();
            this.expect(201);
            String var = this.t.currentTokenValue;
            StructuredQName varQName = this.makeStructuredQName(var, "");
            SequenceType type = SequenceType.ANY_SEQUENCE;
            this.nextToken();
            LocalVariableBinding v = new LocalVariableBinding(varQName, type);
            clause.setRangeVariable(v);
            this.declareRangeVariable(v);
        } while (this.t.currentToken == 7);
    }

    private void parseGroupByClause(FLWORExpression flwor, List<Clause> clauseList) throws XPathException {
        int z;
        GroupByClause clause = new GroupByClause(this.env.getConfiguration());
        this.setLocation(clause, this.t.currentTokenStartOffset);
        clause.setRepeated(XQueryParser.containsLoopingClause(clauseList));
        ArrayList<StructuredQName> variableNames = new ArrayList<StructuredQName>();
        ArrayList<String> collations = new ArrayList<String>();
        this.nextToken();
        while (true) {
            SequenceType type = SequenceType.ANY_SEQUENCE;
            StructuredQName varQName = this.readVariableName();
            if (this.t.currentToken == 71) {
                this.nextToken();
                type = this.parseSequenceType();
                if (this.t.currentToken != 58) {
                    this.grumble("In group by, if the type is declared then it must be followed by ':= value'");
                }
            }
            if (this.t.currentToken == 58) {
                LetClause letClause = new LetClause();
                clauseList.add(letClause);
                this.nextToken();
                LocalVariableBinding v = new LocalVariableBinding(varQName, type);
                Expression value = this.parseExprSingle();
                RoleDiagnostic role = new RoleDiagnostic(20, "grouping key", 0);
                Expression atomizedValue = Atomizer.makeAtomizer(value, role);
                letClause.initSequence(flwor, atomizedValue);
                letClause.setRangeVariable(v);
                this.declareRangeVariable(v);
            }
            variableNames.add(varQName);
            if (this.isKeyword("collation")) {
                this.nextToken();
                this.expect(202);
                collations.add(this.t.currentTokenValue);
                this.nextToken();
            } else {
                collations.add(this.env.getDefaultCollationName());
            }
            if (this.t.currentToken != 7) break;
            this.nextToken();
        }
        TupleExpression groupingTupleExpr = new TupleExpression();
        TupleExpression retainedTupleExpr = new TupleExpression();
        ArrayList<LocalVariableReference> groupingRefs = new ArrayList<LocalVariableReference>();
        ArrayList<LocalVariableReference> retainedRefs = new ArrayList<LocalVariableReference>();
        ArrayList<LocalVariableBinding> groupedBindings = new ArrayList<LocalVariableBinding>();
        for (StructuredQName q : variableNames) {
            boolean found = false;
            block2: for (int i = clauseList.size() - 1; i >= 0; --i) {
                for (LocalVariableBinding b : clauseList.get(i).getRangeVariables()) {
                    if (!q.equals(b.getVariableQName())) continue;
                    groupedBindings.add(b);
                    groupingRefs.add(new LocalVariableReference(b));
                    found = true;
                    break block2;
                }
            }
            if (found) continue;
            this.grumble("The grouping variable " + q.getDisplayName() + " must be the name of a variable bound earlier in the FLWOR expression", "XQST0094");
        }
        groupingTupleExpr.setVariables(groupingRefs);
        clause.initGroupingTupleExpression(flwor, groupingTupleExpr);
        ArrayList<LocalVariableBinding> ungroupedBindings = new ArrayList<LocalVariableBinding>();
        for (int i = clauseList.size() - 1; i >= 0; --i) {
            for (LocalVariableBinding b : clauseList.get(i).getRangeVariables()) {
                if (groupedBindings.contains(b)) continue;
                ungroupedBindings.add(b);
                retainedRefs.add(new LocalVariableReference(b));
            }
        }
        retainedTupleExpr.setVariables(retainedRefs);
        clause.initRetainedTupleExpression(flwor, retainedTupleExpr);
        LocalVariableBinding[] bindings = new LocalVariableBinding[groupedBindings.size() + ungroupedBindings.size()];
        int k = 0;
        for (LocalVariableBinding b : groupedBindings) {
            bindings[k] = new LocalVariableBinding(b.getVariableQName(), b.getRequiredType());
            ++k;
        }
        for (LocalVariableBinding b : ungroupedBindings) {
            ItemType itemType = b.getRequiredType().getPrimaryType();
            bindings[k] = new LocalVariableBinding(b.getVariableQName(), SequenceType.makeSequenceType(itemType, 57344));
            ++k;
        }
        for (z = groupedBindings.size(); z < bindings.length; ++z) {
            this.declareRangeVariable(bindings[z]);
        }
        for (z = 0; z < groupedBindings.size(); ++z) {
            this.declareRangeVariable(bindings[z]);
        }
        clause.setVariableBindings(bindings);
        GenericAtomicComparer[] comparers = new GenericAtomicComparer[collations.size()];
        XPathContext context = this.env.makeEarlyEvaluationContext();
        for (int i = 0; i < comparers.length; ++i) {
            StringCollator coll = this.env.getConfiguration().getCollation((String)collations.get(i));
            comparers[i] = (GenericAtomicComparer)GenericAtomicComparer.makeAtomicComparer(BuiltInAtomicType.ANY_ATOMIC, BuiltInAtomicType.ANY_ATOMIC, coll, context);
        }
        clause.setComparers(comparers);
        clauseList.add(clause);
    }

    private StructuredQName readVariableName() throws XPathException {
        this.expect(21);
        this.nextToken();
        this.expect(201);
        String name = this.t.currentTokenValue;
        this.nextToken();
        return this.makeStructuredQName(name, "");
    }

    private void parseWindowClause(FLWORExpression flwor, List<Clause> clauseList) throws XPathException {
        WindowClause clause = new WindowClause();
        this.setLocation(clause, this.t.currentTokenStartOffset);
        clause.setRepeated(XQueryParser.containsLoopingClause(clauseList));
        clause.setIsSlidingWindow(this.t.currentToken == 74);
        this.nextToken();
        if (!this.isKeyword("window")) {
            this.grumble("after 'sliding' or 'tumbling', expected 'window', but found " + this.currentTokenDisplay());
        }
        this.nextToken();
        StructuredQName windowVarName = this.readVariableName();
        SequenceType windowType = SequenceType.ANY_SEQUENCE;
        if (this.t.currentToken == 71) {
            this.nextToken();
            windowType = this.parseSequenceType();
        }
        LocalVariableBinding windowVar = new LocalVariableBinding(windowVarName, windowType);
        clause.setVariableBinding(0, windowVar);
        SequenceType windowItemTypeMandatory = SequenceType.SINGLE_ITEM;
        SequenceType windowItemTypeOptional = SequenceType.OPTIONAL_ITEM;
        this.expect(31);
        this.nextToken();
        clause.initSequence(flwor, this.parseExprSingle());
        if (!this.isKeyword("start")) {
            this.grumble("in window clause, expected 'start', but found " + this.currentTokenDisplay());
        }
        this.t.setState(1);
        this.nextToken();
        if (this.t.currentToken == 21) {
            LocalVariableBinding startItemVar = new LocalVariableBinding(this.readVariableName(), windowItemTypeMandatory);
            clause.setVariableBinding(1, startItemVar);
            this.declareRangeVariable(startItemVar);
        }
        if (this.isKeyword("at")) {
            this.nextToken();
            LocalVariableBinding startPositionVar = new LocalVariableBinding(this.readVariableName(), SequenceType.SINGLE_INTEGER);
            clause.setVariableBinding(2, startPositionVar);
            this.declareRangeVariable(startPositionVar);
        }
        if (this.isKeyword("previous")) {
            this.nextToken();
            LocalVariableBinding startPreviousItemVar = new LocalVariableBinding(this.readVariableName(), windowItemTypeOptional);
            clause.setVariableBinding(3, startPreviousItemVar);
            this.declareRangeVariable(startPreviousItemVar);
        }
        if (this.isKeyword("next")) {
            this.nextToken();
            LocalVariableBinding startNextItemVar = new LocalVariableBinding(this.readVariableName(), windowItemTypeOptional);
            clause.setVariableBinding(4, startNextItemVar);
            this.declareRangeVariable(startNextItemVar);
        }
        if (!this.isKeyword("when")) {
            this.grumble("Expected 'when' condition for window start, but found " + this.currentTokenDisplay());
        }
        this.t.setState(0);
        this.nextToken();
        clause.initStartCondition(flwor, this.parseExprSingle());
        if (this.isKeyword("only")) {
            clause.setIncludeUnclosedWindows(false);
            this.nextToken();
        }
        if (this.isKeyword("end")) {
            this.t.setState(1);
            this.nextToken();
            if (this.t.currentToken == 21) {
                LocalVariableBinding endItemVar = new LocalVariableBinding(this.readVariableName(), windowItemTypeMandatory);
                clause.setVariableBinding(5, endItemVar);
                this.declareRangeVariable(endItemVar);
            }
            if (this.isKeyword("at")) {
                this.nextToken();
                LocalVariableBinding endPositionVar = new LocalVariableBinding(this.readVariableName(), SequenceType.SINGLE_INTEGER);
                clause.setVariableBinding(6, endPositionVar);
                this.declareRangeVariable(endPositionVar);
            }
            if (this.isKeyword("previous")) {
                this.nextToken();
                LocalVariableBinding endPreviousItemVar = new LocalVariableBinding(this.readVariableName(), windowItemTypeOptional);
                clause.setVariableBinding(7, endPreviousItemVar);
                this.declareRangeVariable(endPreviousItemVar);
            }
            if (this.isKeyword("next")) {
                this.nextToken();
                LocalVariableBinding endNextItemVar = new LocalVariableBinding(this.readVariableName(), windowItemTypeOptional);
                clause.setVariableBinding(8, endNextItemVar);
                this.declareRangeVariable(endNextItemVar);
            }
            if (!this.isKeyword("when")) {
                this.grumble("Expected 'when' condition for window end, but found " + this.currentTokenDisplay());
            }
            this.t.setState(0);
            this.nextToken();
            clause.initEndCondition(flwor, this.parseExprSingle());
        } else if (clause.isSlidingWindow()) {
            this.grumble("A sliding window requires an end condition");
        }
        this.declareRangeVariable(windowVar);
        clauseList.add(clause);
    }

    public static Expression makeStringJoin(Expression exp, StaticContext env) {
        ItemType t = (exp = Atomizer.makeAtomizer(exp, null)).getItemType();
        if (!t.equals(BuiltInAtomicType.STRING) && !t.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
            exp = new AtomicSequenceConverter(exp, BuiltInAtomicType.STRING);
            ((AtomicSequenceConverter)exp).allocateConverterStatically(env.getConfiguration(), false);
        }
        if (exp.getCardinality() == 16384) {
            return exp;
        }
        RetainedStaticContext rsc = new RetainedStaticContext(env);
        Expression fn = SystemFunction.makeCall("string-join", rsc, exp, new StringLiteral(StringValue.SINGLE_SPACE));
        ExpressionTool.copyLocationInfo(exp, fn);
        return fn;
    }

    private List parseSortDefinition() throws XPathException {
        ArrayList<SortSpec> sortSpecList = new ArrayList<SortSpec>(5);
        while (true) {
            SortSpec sortSpec = new SortSpec();
            sortSpec.sortKey = this.parseExprSingle();
            sortSpec.ascending = true;
            sortSpec.emptyLeast = ((QueryModule)this.env).isEmptyLeast();
            sortSpec.collation = this.env.getDefaultCollationName();
            if (this.isKeyword("ascending")) {
                this.nextToken();
            } else if (this.isKeyword("descending")) {
                sortSpec.ascending = false;
                this.nextToken();
            }
            if (this.isKeyword("empty")) {
                this.nextToken();
                if (this.isKeyword("greatest")) {
                    sortSpec.emptyLeast = false;
                    this.nextToken();
                } else if (this.isKeyword("least")) {
                    sortSpec.emptyLeast = true;
                    this.nextToken();
                } else {
                    this.grumble("'empty' must be followed by 'greatest' or 'least'");
                }
            }
            if (this.isKeyword("collation")) {
                sortSpec.collation = this.readCollationName();
            }
            sortSpecList.add(sortSpec);
            if (this.t.currentToken != 7) break;
            this.nextToken();
        }
        return sortSpecList;
    }

    protected String readCollationName() throws XPathException {
        this.nextToken();
        this.expect(202);
        String collationName = this.uriLiteral(this.t.currentTokenValue);
        try {
            URI collationURI = new URI(collationName);
            if (!collationURI.isAbsolute()) {
                URI base = new URI(this.env.getStaticBaseURI());
                collationURI = base.resolve(collationURI);
                collationName = collationURI.toString();
            }
        } catch (URISyntaxException err) {
            this.grumble("Collation name '" + collationName + "' is not a valid URI", "XQST0046");
            collationName = "http://www.w3.org/2005/xpath-functions/collation/codepoint";
        }
        this.nextToken();
        return collationName;
    }

    @Override
    protected Expression parseTypeswitchExpression() throws XPathException {
        Expression defaultAction;
        int offset = this.t.currentTokenStartOffset;
        this.nextToken();
        Expression operand = this.parseExpression();
        ArrayList<List<SequenceType>> types = new ArrayList<List<SequenceType>>(10);
        ArrayList<Expression> actions = new ArrayList<Expression>(10);
        this.expect(204);
        this.nextToken();
        LetExpression outerLet = this.makeLetExpression();
        outerLet.setRequiredType(SequenceType.ANY_SEQUENCE);
        outerLet.setVariableQName(new StructuredQName("zz", "http://saxon.sf.net/", "zz_typeswitchVar"));
        outerLet.setSequence(operand);
        while (this.t.currentToken == 67) {
            Expression action;
            List<SequenceType> typeList;
            int caseOffset = this.t.currentTokenStartOffset;
            this.nextToken();
            if (this.t.currentToken == 21) {
                this.nextToken();
                this.expect(201);
                String var = this.t.currentTokenValue;
                StructuredQName varQName = this.makeStructuredQName(var, "");
                this.nextToken();
                this.expect(71);
                this.nextToken();
                typeList = this.parseSequenceTypeList();
                action = this.makeTracer(this.parseTypeswitchReturnClause(varQName, outerLet), varQName);
                if (action instanceof TraceExpression) {
                    ((TraceExpression)action).setProperty("type", typeList.get(0).toString());
                }
            } else {
                typeList = this.parseSequenceTypeList();
                action = this.makeTracer(this.parseExprSingle(), null);
                if (action instanceof TraceExpression) {
                    ((TraceExpression)action).setProperty("type", typeList.get(0).toString());
                }
            }
            types.add(typeList);
            actions.add(action);
        }
        if (types.isEmpty()) {
            this.grumble("At least one case clause is required in a typeswitch");
        }
        this.expect(212);
        int defaultOffset = this.t.currentTokenStartOffset;
        this.nextToken();
        if (this.t.currentToken == 21) {
            this.nextToken();
            this.expect(201);
            String var = this.t.currentTokenValue;
            StructuredQName varQName = this.makeStructuredQName(var, "");
            this.nextToken();
            this.expect(25);
            this.nextToken();
            defaultAction = this.makeTracer(this.parseTypeswitchReturnClause(varQName, outerLet), varQName);
        } else {
            this.t.treatCurrentAsOperator();
            this.expect(25);
            this.nextToken();
            defaultAction = this.makeTracer(this.parseExprSingle(), null);
        }
        Expression lastAction = defaultAction;
        for (int i = types.size() - 1; i >= 0; --i) {
            LocalVariableReference var = new LocalVariableReference(outerLet);
            this.setLocation(var);
            Expression ioe = new InstanceOfExpression(var, (SequenceType)((List)types.get(i)).get(0));
            for (int j = 1; j < ((List)types.get(i)).size(); ++j) {
                ioe = new OrExpression(ioe, new InstanceOfExpression(var.copy(new RebindingMap()), (SequenceType)((List)types.get(i)).get(j)));
            }
            this.setLocation(ioe);
            Expression ife = Choose.makeConditional(ioe, (Expression)actions.get(i), lastAction);
            this.setLocation(ife);
            lastAction = ife;
        }
        outerLet.setAction(lastAction);
        return this.makeTracer(outerLet, null);
    }

    private List<SequenceType> parseSequenceTypeList() throws XPathException {
        ArrayList<SequenceType> typeList = new ArrayList<SequenceType>();
        while (true) {
            SequenceType type = this.parseSequenceType();
            typeList.add(type);
            this.t.treatCurrentAsOperator();
            if (this.t.currentToken != 1) break;
            this.nextToken();
        }
        this.expect(25);
        this.nextToken();
        return typeList;
    }

    private Expression parseTypeswitchReturnClause(StructuredQName varQName, LetExpression outerLet) throws XPathException {
        LetExpression innerLet = this.makeLetExpression();
        innerLet.setRequiredType(SequenceType.ANY_SEQUENCE);
        innerLet.setVariableQName(varQName);
        innerLet.setSequence(new LocalVariableReference(outerLet));
        this.declareRangeVariable(innerLet);
        Expression action = this.parseExprSingle();
        this.undeclareRangeVariable();
        innerLet.setAction(action);
        return innerLet;
    }

    @Override
    protected Expression parseSwitchExpression() throws XPathException {
        int offset = this.t.currentTokenStartOffset;
        this.nextToken();
        Expression operand = this.parseExpression();
        this.expect(204);
        this.nextToken();
        ArrayList<Expression> conditions = new ArrayList<Expression>(10);
        ArrayList<Expression> actions = new ArrayList<Expression>(10);
        LetExpression outerLet = this.makeLetExpression();
        outerLet.setRequiredType(SequenceType.OPTIONAL_ATOMIC);
        outerLet.setVariableQName(new StructuredQName("zz", "http://saxon.sf.net/", "zz_switchVar"));
        outerLet.setSequence(Atomizer.makeAtomizer(operand, null));
        do {
            ArrayList<Expression> caseExpressions = new ArrayList<Expression>(4);
            this.expect(67);
            do {
                this.nextToken();
                Expression c = this.parseExprSingle();
                caseExpressions.add(c);
            } while (this.t.currentToken == 67);
            this.expect(25);
            this.nextToken();
            Expression action = this.parseExprSingle();
            for (int i = 0; i < caseExpressions.size(); ++i) {
                EquivalenceComparison vc = new EquivalenceComparison(new LocalVariableReference(outerLet), 50, (Expression)caseExpressions.get(i));
                if (i == 0) {
                    conditions.add(vc);
                    actions.add(action);
                    continue;
                }
                OrExpression orExpr = new OrExpression((Expression)conditions.remove(conditions.size() - 1), vc);
                conditions.add(orExpr);
            }
        } while (this.t.currentToken == 67);
        this.expect(212);
        this.nextToken();
        this.expect(25);
        this.nextToken();
        Expression defaultExpr = this.parseExprSingle();
        conditions.add(Literal.makeLiteral(BooleanValue.TRUE));
        actions.add(defaultExpr);
        Choose choice = new Choose(conditions.toArray(new Expression[0]), actions.toArray(new Expression[conditions.size()]));
        outerLet.setAction(choice);
        return this.makeTracer(outerLet, null);
    }

    @Override
    protected Expression parseValidateExpression() throws XPathException {
        int offset = this.t.currentTokenStartOffset;
        int mode = 1;
        boolean foundCurly = false;
        SchemaType requiredType = null;
        this.ensureSchemaAware("validate expression");
        switch (this.t.currentToken) {
            case 103: {
                mode = 1;
                this.nextToken();
                break;
            }
            case 104: {
                mode = 2;
                this.nextToken();
                break;
            }
            case 105: {
                mode = 8;
                this.nextToken();
                this.expect(60);
                if (!NameChecker.isQName(this.t.currentTokenValue)) {
                    this.grumble("Schema type name expected after 'validate type");
                }
                if ((requiredType = this.env.getConfiguration().getSchemaType(this.makeStructuredQName(this.t.currentTokenValue, this.env.getDefaultElementNamespace()))) == null) {
                    this.grumble("Unknown schema type " + this.t.currentTokenValue, "XQST0104");
                }
                foundCurly = true;
                break;
            }
            case 60: {
                if (!this.t.currentTokenValue.equals("validate")) {
                    throw new AssertionError((Object)"shouldn't be parsing a validate expression");
                }
                mode = 1;
                foundCurly = true;
            }
        }
        if (!foundCurly) {
            this.expect(59);
        }
        this.nextToken();
        Expression exp = this.parseExpression();
        if (exp instanceof ParentNodeConstructor) {
            ((ParentNodeConstructor)exp).setValidationAction(mode, mode == 8 ? requiredType : null);
        } else {
            exp = new CopyOf(exp, true, mode, requiredType, true);
            this.setLocation(exp);
            ((CopyOf)exp).setRequireDocumentOrElement(true);
        }
        this.expect(215);
        this.t.lookAhead();
        this.nextToken();
        return this.makeTracer(exp, null);
    }

    @Override
    protected Expression parseExtensionExpression() throws XPathException {
        Expression expr;
        int c;
        SchemaType requiredType = null;
        CharSequence trimmed = Whitespace.removeLeadingWhitespace(this.t.currentTokenValue);
        int len = trimmed.length();
        for (c = 0; c < len && " \t\r\n".indexOf(trimmed.charAt(c)) < 0; ++c) {
        }
        String qname = trimmed.subSequence(0, c).toString();
        String pragmaContents = "";
        while (c < len && " \t\r\n".indexOf(trimmed.charAt(c)) >= 0) {
            ++c;
        }
        if (c < len) {
            pragmaContents = trimmed.subSequence(c, len).toString();
        }
        boolean validateType = false;
        boolean streaming = false;
        StructuredQName pragmaName = this.makeStructuredQName(qname, "");
        assert (pragmaName != null);
        String uri = pragmaName.getURI();
        String localName = pragmaName.getLocalPart();
        if (uri.equals("http://saxon.sf.net/")) {
            if ("validate-type".equals(localName)) {
                if (!this.env.getConfiguration().isLicensedFeature(4)) {
                    this.warning("Ignoring saxon:validate-type. To use this feature you need the Saxon-EE processor from http://www.saxonica.com/");
                } else {
                    String typeName = Whitespace.trim(pragmaContents);
                    if (!NameChecker.isQName(typeName)) {
                        this.grumble("Schema type name expected in saxon:validate-type pragma: found " + Err.wrap(typeName));
                    }
                    if ((requiredType = this.env.getConfiguration().getSchemaType(this.makeStructuredQName(typeName, this.env.getDefaultElementNamespace()))) == null) {
                        this.grumble("Unknown schema type " + typeName);
                    }
                    validateType = true;
                }
            } else {
                this.warning("Ignored pragma " + qname + " (unrecognized Saxon pragma)");
            }
        }
        this.nextToken();
        if (this.t.currentToken == 218) {
            expr = this.parseExtensionExpression();
        } else {
            this.expect(59);
            this.nextToken();
            if (this.t.currentToken == 215) {
                this.t.lookAhead();
                this.nextToken();
                this.grumble("Unrecognized pragma, with no fallback expression", "XQST0079");
            }
            expr = this.parseExpression();
            this.expect(215);
            this.t.lookAhead();
            this.nextToken();
        }
        if (validateType) {
            if (expr instanceof ParentNodeConstructor) {
                ((ParentNodeConstructor)expr).setValidationAction(8, requiredType);
                return expr;
            }
            if (expr instanceof AttributeCreator) {
                if (!(requiredType instanceof SimpleType)) {
                    this.grumble("The type used for validating an attribute must be a simple type");
                }
                ((AttributeCreator)expr).setSchemaType((SimpleType)requiredType);
                ((AttributeCreator)expr).setValidationAction(8);
                return expr;
            }
            CopyOf copy = new CopyOf(expr, true, 8, requiredType, true);
            copy.setLocation(this.makeLocation());
            return copy;
        }
        return expr;
    }

    @Override
    protected Expression parseConstructor() throws XPathException {
        int offset = this.t.currentTokenStartOffset;
        switch (this.t.currentToken) {
            case 217: {
                Expression tag = this.parsePseudoXML(false);
                this.lookAhead();
                this.t.setState(3);
                this.nextToken();
                return tag;
            }
            case 60: {
                String nodeKind;
                switch (nodeKind = this.t.currentTokenValue) {
                    case "validate": {
                        this.grumble("A validate expression is not allowed within a path expression");
                        break;
                    }
                    case "ordered": 
                    case "unordered": {
                        this.nextToken();
                        Expression content = this.t.currentToken == 215 && this.allowXPath31Syntax ? Literal.makeEmptySequence() : this.parseExpression();
                        this.expect(215);
                        this.lookAhead();
                        this.nextToken();
                        return content;
                    }
                    case "document": {
                        return this.parseDocumentConstructor(offset);
                    }
                    case "element": {
                        return this.parseComputedElementConstructor(offset);
                    }
                    case "attribute": {
                        return this.parseComputedAttributeConstructor(offset);
                    }
                    case "text": {
                        return this.parseTextNodeConstructor(offset);
                    }
                    case "comment": {
                        return this.parseCommentConstructor(offset);
                    }
                    case "processing-instruction": {
                        return this.parseProcessingInstructionConstructor(offset);
                    }
                    case "namespace": {
                        return this.parseNamespaceConstructor(offset);
                    }
                    default: {
                        this.grumble("Unrecognized node constructor " + this.t.currentTokenValue + "{}");
                    }
                }
            }
            case 61: {
                return this.parseNamedElementConstructor(offset);
            }
            case 62: {
                return this.parseNamedAttributeConstructor(offset);
            }
            case 64: {
                return this.parseNamedNamespaceConstructor(offset);
            }
            case 63: {
                return this.parseNamedProcessingInstructionConstructor(offset);
            }
        }
        return new ErrorExpression();
    }

    private Expression parseDocumentConstructor(int offset) throws XPathException {
        this.nextToken();
        Expression content = this.t.currentToken == 215 && this.allowXPath31Syntax ? Literal.makeEmptySequence() : this.parseExpression();
        this.expect(215);
        this.lookAhead();
        this.nextToken();
        DocumentInstr doc = new DocumentInstr(false, null);
        if (!((QueryModule)this.env).isPreserveNamespaces()) {
            content = new CopyOf(content, false, 3, null, true);
        }
        doc.setValidationAction(((QueryModule)this.env).getConstructionMode(), null);
        doc.setContentExpression(content);
        this.setLocation(doc, offset);
        return doc;
    }

    private Expression parseComputedElementConstructor(int offset) throws XPathException {
        this.nextToken();
        Expression name = this.parseExpression();
        this.expect(215);
        this.lookAhead();
        this.nextToken();
        this.expect(59);
        this.t.setState(0);
        this.nextToken();
        Expression content = null;
        if (this.t.currentToken != 215) {
            content = this.parseExpression();
            if (content instanceof ElementCreator && ((ElementCreator)content).getSchemaType() == null) {
                ((ElementCreator)content).setValidationAction(3, null);
            }
            this.expect(215);
        }
        this.lookAhead();
        this.nextToken();
        if (name instanceof Literal) {
            NodeName elemName;
            GroundedValue vName = ((Literal)name).getValue();
            if (vName instanceof StringValue && !(vName instanceof AnyURIValue)) {
                String lex = ((StringValue)vName).getStringValue();
                try {
                    QNameParser oldQP = this.getQNameParser();
                    this.setQNameParser(oldQP.withUnescaper(null));
                    elemName = this.makeNodeName(lex, true);
                    this.setQNameParser(oldQP);
                    elemName.obtainFingerprint(this.env.getConfiguration().getNamePool());
                } catch (XPathException staticError) {
                    String code = staticError.getErrorCodeLocalPart();
                    if ("XPST0008".equals(code) || "XPST0081".equals(code)) {
                        staticError.setErrorCode("XQDY0074");
                    } else if ("XPST0003".equals(code)) {
                        this.grumble("Invalid QName in element constructor: " + lex, "XQDY0074", offset);
                        return new ErrorExpression();
                    }
                    staticError.setLocator(this.makeLocation());
                    staticError.setIsStaticError(false);
                    return new ErrorExpression(new XmlProcessingException(staticError));
                }
            } else if (vName instanceof QualifiedNameValue) {
                String uri = ((QualifiedNameValue)vName).getNamespaceURI();
                elemName = new FingerprintedQName("", uri, ((QualifiedNameValue)vName).getLocalName());
                elemName.obtainFingerprint(this.env.getConfiguration().getNamePool());
            } else {
                this.grumble("Element name must be either a string or a QName", "XPTY0004", offset);
                return new ErrorExpression();
            }
            FixedElement inst = new FixedElement(elemName, ((QueryModule)this.env).getActiveNamespaceBindings(), ((QueryModule)this.env).isInheritNamespaces(), true, null, ((QueryModule)this.env).getConstructionMode());
            if (content == null) {
                content = Literal.makeEmptySequence();
            }
            if (!((QueryModule)this.env).isPreserveNamespaces()) {
                content = new CopyOf(content, false, 3, null, true);
            }
            inst.setContentExpression(content);
            this.setLocation(inst, offset);
            return this.makeTracer(inst, elemName.getStructuredQName());
        }
        NamespaceResolverWithDefault ns = new NamespaceResolverWithDefault(this.env.getNamespaceResolver(), this.env.getDefaultElementNamespace());
        ComputedElement inst = new ComputedElement(name, null, null, ((QueryModule)this.env).getConstructionMode(), ((QueryModule)this.env).isInheritNamespaces(), true);
        this.setLocation(inst);
        if (content == null) {
            content = Literal.makeEmptySequence();
        }
        if (!((QueryModule)this.env).isPreserveNamespaces()) {
            content = new CopyOf(content, false, 3, null, true);
        }
        inst.setContentExpression(content);
        this.setLocation(inst, offset);
        return this.makeTracer(inst, null);
    }

    private Expression parseNamedElementConstructor(int offset) throws XPathException {
        NodeName nodeName = this.makeNodeName(this.t.currentTokenValue, true);
        Expression content = null;
        this.nextToken();
        if (this.t.currentToken != 215) {
            content = this.parseExpression();
            this.expect(215);
        }
        this.lookAhead();
        this.nextToken();
        FixedElement el2 = new FixedElement(nodeName, ((QueryModule)this.env).getActiveNamespaceBindings(), ((QueryModule)this.env).isInheritNamespaces(), true, null, ((QueryModule)this.env).getConstructionMode());
        this.setLocation(el2, offset);
        if (content == null) {
            content = Literal.makeEmptySequence();
        }
        if (!((QueryModule)this.env).isPreserveNamespaces()) {
            content = new CopyOf(content, false, 3, null, true);
        }
        el2.setContentExpression(content);
        return this.makeTracer(el2, nodeName.getStructuredQName());
    }

    private Expression parseComputedAttributeConstructor(int offset) throws XPathException {
        this.nextToken();
        Expression name = this.parseExpression();
        this.expect(215);
        this.lookAhead();
        this.nextToken();
        this.expect(59);
        this.t.setState(0);
        this.nextToken();
        Expression content = null;
        if (this.t.currentToken != 215) {
            content = this.parseExpression();
            this.expect(215);
        }
        this.lookAhead();
        this.nextToken();
        if (name instanceof Literal) {
            GroundedValue vName = ((Literal)name).getValue();
            if (vName instanceof StringValue && !(vName instanceof AnyURIValue)) {
                NodeName attributeName;
                String lex = ((StringValue)vName).getStringValue();
                if (lex.equals("xmlns") || lex.startsWith("xmlns:")) {
                    this.grumble("Cannot create a namespace using an attribute constructor", "XQDY0044", offset);
                }
                try {
                    QNameParser oldQP = this.getQNameParser();
                    this.setQNameParser(oldQP.withUnescaper(null));
                    attributeName = this.makeNodeName(lex, false);
                    this.setQNameParser(oldQP);
                } catch (XPathException staticError) {
                    String code = staticError.getErrorCodeLocalPart();
                    staticError.setLocator(this.makeLocation());
                    if ("XPST0008".equals(code) || "XPST0081".equals(code)) {
                        staticError.setErrorCode("XQDY0074");
                    } else if ("XPST0003".equals(code)) {
                        this.grumble("Invalid QName in attribute constructor: " + lex, "XQDY0074", offset);
                        return new ErrorExpression();
                    }
                    throw staticError;
                }
                if (attributeName.getPrefix().isEmpty() && !attributeName.hasURI("")) {
                    attributeName = new FingerprintedQName("_", attributeName.getURI(), attributeName.getLocalPart(), attributeName.getFingerprint());
                }
                FixedAttribute fatt = new FixedAttribute(attributeName, 4, null);
                fatt.setRejectDuplicates();
                this.makeSimpleContent(content, fatt, offset);
                return this.makeTracer(fatt, null);
            }
            if (vName instanceof QNameValue) {
                QNameValue qnv = (QNameValue)vName;
                FingerprintedQName attributeName = new FingerprintedQName(qnv.getPrefix(), qnv.getNamespaceURI(), qnv.getLocalName());
                attributeName.obtainFingerprint(this.env.getConfiguration().getNamePool());
                FixedAttribute fatt = new FixedAttribute(attributeName, 4, null);
                fatt.setRejectDuplicates();
                this.makeSimpleContent(content, fatt, offset);
                return this.makeTracer(fatt, null);
            }
        }
        ComputedAttribute att = new ComputedAttribute(name, null, this.env.getNamespaceResolver(), 4, null, true);
        att.setRejectDuplicates();
        this.makeSimpleContent(content, att, offset);
        return this.makeTracer(att, null);
    }

    private Expression parseNamedAttributeConstructor(int offset) throws XPathException {
        NodeName attributeName;
        String warning = null;
        if (this.t.currentTokenValue.equals("xmlns") || this.t.currentTokenValue.startsWith("xmlns:")) {
            warning = "Cannot create a namespace declaration using an attribute constructor";
        }
        if (!(attributeName = this.makeNodeName(this.t.currentTokenValue, false)).getURI().equals("") && attributeName.getPrefix().equals("")) {
            attributeName = new FingerprintedQName("_", attributeName.getURI(), attributeName.getLocalPart());
        }
        Expression attContent = null;
        this.nextToken();
        if (this.t.currentToken != 215) {
            attContent = this.parseExpression();
            this.expect(215);
        }
        this.lookAhead();
        this.nextToken();
        if (warning == null) {
            FixedAttribute att2 = new FixedAttribute(attributeName, 4, null);
            att2.setRejectDuplicates();
            att2.setRetainedStaticContext(this.env.makeRetainedStaticContext());
            this.makeSimpleContent(attContent, att2, offset);
            return this.makeTracer(att2, attributeName.getStructuredQName());
        }
        this.warning(warning);
        return new ErrorExpression(warning, "XQDY0044", false);
    }

    private Expression parseTextNodeConstructor(int offset) throws XPathException {
        this.nextToken();
        Expression value = this.t.currentToken == 215 && this.allowXPath31Syntax ? Literal.makeEmptySequence() : this.parseExpression();
        this.expect(215);
        this.lookAhead();
        this.nextToken();
        Expression select = XQueryParser.stringify(value, true, this.env);
        ValueOf vof = new ValueOf(select, false, true);
        this.setLocation(vof, offset);
        return this.makeTracer(vof, null);
    }

    private Expression parseCommentConstructor(int offset) throws XPathException {
        this.nextToken();
        Expression value = this.t.currentToken == 215 && this.allowXPath31Syntax ? Literal.makeEmptySequence() : this.parseExpression();
        this.expect(215);
        this.lookAhead();
        this.nextToken();
        Comment com = new Comment();
        this.makeSimpleContent(value, com, offset);
        return this.makeTracer(com, null);
    }

    private Expression parseProcessingInstructionConstructor(int offset) throws XPathException {
        this.nextToken();
        Expression name = this.parseExpression();
        this.expect(215);
        this.lookAhead();
        this.nextToken();
        this.expect(59);
        this.t.setState(0);
        this.nextToken();
        Expression content = null;
        if (this.t.currentToken != 215) {
            content = this.parseExpression();
            this.expect(215);
        }
        this.lookAhead();
        this.nextToken();
        ProcessingInstruction pi = new ProcessingInstruction(name);
        this.makeSimpleContent(content, pi, offset);
        return this.makeTracer(pi, null);
    }

    private Expression parseNamedProcessingInstructionConstructor(int offset) throws XPathException {
        String target = this.t.currentTokenValue;
        String warning = null;
        if (target.equalsIgnoreCase("xml")) {
            warning = "A processing instruction must not be named 'xml' in any combination of upper and lower case";
        }
        if (!NameChecker.isValidNCName(target)) {
            this.grumble("Invalid processing instruction name " + Err.wrap(target));
        }
        StringLiteral piName = new StringLiteral(target);
        Expression piContent = null;
        this.nextToken();
        if (this.t.currentToken != 215) {
            piContent = this.parseExpression();
            this.expect(215);
        }
        this.lookAhead();
        this.nextToken();
        if (warning == null) {
            ProcessingInstruction pi2 = new ProcessingInstruction(piName);
            this.makeSimpleContent(piContent, pi2, offset);
            return this.makeTracer(pi2, null);
        }
        this.warning(warning);
        return new ErrorExpression(warning, "XQDY0064", false);
    }

    @Override
    protected Expression parseTryCatchExpression() throws XPathException {
        if (!this.allowXPath30Syntax) {
            this.grumble("try/catch requires XQuery 3.0");
        }
        int offset = this.t.currentTokenStartOffset;
        this.nextToken();
        Expression tryExpr = this.t.currentToken == 215 && this.allowXPath31Syntax ? Literal.makeEmptySequence() : this.parseExpression();
        TryCatch tryCatch = new TryCatch(tryExpr);
        this.setLocation(tryCatch, offset);
        this.expect(215);
        this.lookAhead();
        this.nextToken();
        boolean foundOneCatch = false;
        ArrayList<QNameTest> tests = new ArrayList<QNameTest>();
        while (this.isKeyword("catch")) {
            tests.clear();
            foundOneCatch = true;
            boolean seenCurly = false;
            do {
                this.nextToken();
                String tokv = this.t.currentTokenValue;
                switch (this.t.currentToken) {
                    case 201: {
                        this.nextToken();
                        tests.add(this.makeQNameTest((short)1, tokv));
                        break;
                    }
                    case 60: {
                        this.nextToken();
                        tests.add(this.makeQNameTest((short)1, tokv));
                        seenCurly = true;
                        break;
                    }
                    case 208: {
                        this.nextToken();
                        tests.add(this.makeNamespaceTest((short)1, tokv));
                        break;
                    }
                    case 70: {
                        this.nextToken();
                        tokv = this.t.currentTokenValue;
                        if (this.t.currentToken != 201) {
                            if (this.t.currentToken == 60) {
                                seenCurly = true;
                            } else {
                                this.grumble("Expected name after '*:'");
                            }
                        }
                        this.nextToken();
                        tests.add(this.makeLocalNameTest((short)1, tokv));
                        break;
                    }
                    case 17: 
                    case 207: {
                        this.nextToken();
                        tests.add(AnyNodeTest.getInstance());
                        break;
                    }
                    default: {
                        this.grumble("Unrecognized name test");
                        return null;
                    }
                }
            } while (this.t.currentToken == 1 && !this.t.currentTokenValue.equals("union"));
            if (!seenCurly) {
                this.expect(59);
                this.nextToken();
            }
            QNameTest test = tests.size() == 1 ? (QNameTest)tests.get(0) : new UnionQNameTest(tests);
            ++this.catchDepth;
            Expression catchExpr = this.t.currentToken == 215 && this.allowXPath31Syntax ? Literal.makeEmptySequence() : this.parseExpression();
            tryCatch.addCatchExpression(test, catchExpr);
            this.expect(215);
            this.lookAhead();
            this.nextToken();
            --this.catchDepth;
        }
        if (!foundOneCatch) {
            this.grumble("After try{}, expected 'catch'");
        }
        return tryCatch;
    }

    private Expression parseNamespaceConstructor(int offset) throws XPathException {
        if (!this.allowXPath30Syntax) {
            this.grumble("Namespace node constructors require XQuery 3.0");
        }
        this.nextToken();
        Expression nameExpr = this.parseExpression();
        this.expect(215);
        this.lookAhead();
        this.nextToken();
        this.expect(59);
        this.t.setState(0);
        this.nextToken();
        Expression content = null;
        if (this.t.currentToken != 215) {
            content = this.parseExpression();
            this.expect(215);
        }
        this.lookAhead();
        this.nextToken();
        NamespaceConstructor instr = new NamespaceConstructor(nameExpr);
        this.setLocation(instr);
        this.makeSimpleContent(content, instr, offset);
        return this.makeTracer(instr, null);
    }

    private Expression parseNamedNamespaceConstructor(int offset) throws XPathException {
        String target;
        if (!this.allowXPath30Syntax) {
            this.grumble("Namespace node constructors require XQuery 3.0");
        }
        if (!NameChecker.isValidNCName(target = this.t.currentTokenValue)) {
            this.grumble("Invalid namespace prefix " + Err.wrap(target));
        }
        StringLiteral nsName = new StringLiteral(target);
        Expression nsContent = null;
        this.nextToken();
        if (this.t.currentToken != 215) {
            nsContent = this.parseExpression();
            this.expect(215);
        }
        this.lookAhead();
        this.nextToken();
        NamespaceConstructor instr = new NamespaceConstructor(nsName);
        this.makeSimpleContent(nsContent, instr, offset);
        return this.makeTracer(instr, null);
    }

    protected void makeSimpleContent(Expression content, SimpleNodeConstructor inst, int offset) {
        if (content == null) {
            inst.setSelect(new StringLiteral(StringValue.EMPTY_STRING));
        } else {
            inst.setSelect(XQueryParser.stringify(content, false, this.env));
        }
        this.setLocation(inst, offset);
    }

    private Expression parsePseudoXML(boolean allowEndTag) throws XPathException {
        try {
            Expression exp;
            int offset = this.t.inputOffset;
            char c = this.t.nextChar();
            switch (c) {
                case '!': {
                    c = this.t.nextChar();
                    if (c == '-') {
                        exp = this.parseCommentConstructor();
                        break;
                    }
                    if (c == '[') {
                        this.grumble("A CDATA section is allowed only in element content");
                        return null;
                    }
                    this.grumble("Expected '--' or '[CDATA[' after '<!'");
                    return null;
                }
                case '?': {
                    exp = this.parsePIConstructor();
                    break;
                }
                case '/': {
                    if (allowEndTag) {
                        FastStringBuffer sb = new FastStringBuffer(16);
                        while ((c = this.t.nextChar()) != '>') {
                            sb.cat(c);
                        }
                        return new StringLiteral(sb.toString());
                    }
                    this.grumble("Unmatched XML end tag");
                    return new ErrorExpression();
                }
                default: {
                    this.t.unreadChar();
                    exp = this.parseDirectElementConstructor(allowEndTag);
                }
            }
            this.setLocation(exp, offset);
            return exp;
        } catch (StringIndexOutOfBoundsException e) {
            this.grumble("End of input encountered while parsing direct constructor");
            return new ErrorExpression();
        }
    }

    private Expression parseDirectElementConstructor(boolean isNested) throws XPathException, StringIndexOutOfBoundsException {
        AttributeDetails a;
        char c;
        NamePool pool = this.env.getConfiguration().getNamePool();
        boolean changesContext = false;
        int offset = this.t.inputOffset - 1;
        FastStringBuffer buff = new FastStringBuffer(64);
        int namespaceCount = 0;
        while ((c = this.t.nextChar()) != ' ' && c != '\n' && c != '\r' && c != '\t' && c != '/' && c != '>') {
            buff.cat(c);
        }
        String elname = buff.toString();
        if (elname.isEmpty()) {
            this.grumble("Expected element name after '<'");
        }
        LinkedHashMap<String, AttributeDetails> attributes = new LinkedHashMap<String, AttributeDetails>(10);
        while ((c = this.skipSpaces(c)) != '/' && c != '>') {
            int pos;
            int end;
            boolean isNamespace;
            int attOffset = this.t.inputOffset - 1;
            buff.setLength(0);
            do {
                buff.cat(c);
            } while ((c = this.t.nextChar()) != ' ' && c != '\n' && c != '\r' && c != '\t' && c != '=');
            String attName = buff.toString();
            if (!NameChecker.isQName(attName)) {
                this.grumble("Invalid attribute name " + Err.wrap(attName, 2));
            }
            c = this.skipSpaces(c);
            this.expectChar(c, '=');
            c = this.t.nextChar();
            char delim = c = this.skipSpaces(c);
            boolean bl = isNamespace = "xmlns".equals(attName) || attName.startsWith("xmlns:");
            if (isNamespace) {
                end = this.makeNamespaceContent(this.t.input, this.t.inputOffset, delim);
                changesContext = true;
            } else {
                Expression avt;
                try {
                    avt = this.makeAttributeContent(this.t.input, this.t.inputOffset, delim, true);
                } catch (XPathException err) {
                    if (!err.hasBeenReported()) {
                        this.grumble(err.getMessage());
                    }
                    throw err;
                }
                end = (int)((Int64Value)((Literal)avt).getValue()).longValue();
            }
            String val = this.t.input.substring(this.t.inputOffset - 1, end + 1);
            Object rval = this.t.input.substring(this.t.inputOffset, end);
            String tail = val;
            while ((pos = tail.indexOf(10)) >= 0) {
                this.t.incrementLineNumber(this.t.inputOffset - 1 + pos);
                tail = tail.substring(pos + 1);
            }
            this.t.inputOffset = end + 1;
            if (isNamespace) {
                String prefix;
                FastStringBuffer sb = new FastStringBuffer(((String)rval).length());
                boolean prevDelim = false;
                boolean prevOpenCurly = false;
                boolean prevCloseCurly = false;
                for (int i = 0; i < ((String)rval).length(); ++i) {
                    char n = ((String)rval).charAt(i);
                    if (n == delim) {
                        boolean bl2 = prevDelim = !prevDelim;
                        if (prevDelim) continue;
                    }
                    if (n == '{') {
                        boolean bl3 = prevOpenCurly = !prevOpenCurly;
                        if (prevOpenCurly) {
                            continue;
                        }
                    } else if (prevOpenCurly) {
                        this.grumble("Namespace must not contain an unescaped opening brace", "XQST0022");
                    }
                    if (n == '}') {
                        boolean bl4 = prevCloseCurly = !prevCloseCurly;
                        if (prevCloseCurly) {
                            continue;
                        }
                    } else if (prevCloseCurly) {
                        this.grumble("Namespace must not contain an unescaped closing brace", "XPST0003");
                    }
                    sb.cat(n);
                }
                if (prevOpenCurly) {
                    this.grumble("Namespace must not contain an unescaped opening brace", "XQST0022");
                }
                if (prevCloseCurly) {
                    this.grumble("Namespace must not contain an unescaped closing brace", "XPST0003");
                }
                rval = sb.toString();
                String uri = this.uriLiteral((String)rval);
                if (!StandardURIChecker.getInstance().isValidURI(uri)) {
                    this.grumble("Namespace must be a valid URI value", "XQST0046");
                }
                if ("xmlns".equals(attName)) {
                    prefix = "";
                    if (uri.equals("http://www.w3.org/XML/1998/namespace")) {
                        this.grumble("Cannot have the XML namespace as the default namespace", "XQST0070");
                    }
                } else {
                    prefix = attName.substring(6);
                    if (prefix.equals("xml") && !uri.equals("http://www.w3.org/XML/1998/namespace")) {
                        this.grumble("Cannot bind the prefix 'xml' to a namespace other than the XML namespace", "XQST0070");
                    } else if (uri.equals("http://www.w3.org/XML/1998/namespace") && !prefix.equals("xml")) {
                        this.grumble("Cannot bind a prefix other than 'xml' to the XML namespace", "XQST0070");
                    } else if (prefix.equals("xmlns")) {
                        this.grumble("Cannot use xmlns as a namespace prefix", "XQST0070");
                    }
                    if (uri.isEmpty() && this.env.getConfiguration().getXMLVersion() == 10) {
                        this.grumble("Namespace URI must not be empty", "XQST0085");
                    }
                }
                ++namespaceCount;
                ((QueryModule)this.env).declareActiveNamespace(prefix, uri);
            }
            if (attributes.get(attName) != null) {
                if (isNamespace) {
                    this.grumble("Duplicate namespace declaration " + attName, "XQST0071", attOffset);
                } else {
                    this.grumble("Duplicate attribute name " + attName, "XQST0040", attOffset);
                }
            }
            a = new AttributeDetails();
            a.value = val;
            a.startOffset = attOffset;
            attributes.put(attName, a);
            c = this.t.nextChar();
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == '/' || c == '>') continue;
            this.grumble("There must be whitespace after every attribute except the last");
        }
        StructuredQName qName = null;
        if (this.scanOnly) {
            qName = StandardNames.getStructuredQName(151);
        } else {
            try {
                String[] parts = NameChecker.getQNameParts(elname);
                String namespace = ((QueryModule)this.env).checkURIForPrefix(parts[0]);
                if (namespace == null) {
                    this.grumble("Undeclared prefix in element name " + Err.wrap(elname, 1), "XPST0081", offset);
                }
                qName = new StructuredQName(parts[0], namespace, parts[1]);
            } catch (QNameException e) {
                this.grumble("Invalid element name " + Err.wrap(elname, 1), "XPST0003", offset);
                qName = StandardNames.getStructuredQName(151);
            }
        }
        int validationMode = ((QueryModule)this.env).getConstructionMode();
        FingerprintedQName fqn = new FingerprintedQName(qName.getPrefix(), qName.getURI(), qName.getLocalPart(), pool.allocateFingerprint(qName.getURI(), qName.getLocalPart()));
        FixedElement elInst = new FixedElement(fqn, ((QueryModule)this.env).getActiveNamespaceBindings(), ((QueryModule)this.env).isInheritNamespaces(), !isNested, null, validationMode);
        this.setLocation(elInst, offset);
        ArrayList<Expression> contents = new ArrayList<Expression>(10);
        IntHashSet attFingerprints = new IntHashSet(attributes.size());
        for (Map.Entry entry : attributes.entrySet()) {
            Expression select;
            String attName = (String)entry.getKey();
            a = (AttributeDetails)entry.getValue();
            String attValue = a.value;
            int attOffset = a.startOffset;
            if ("xmlns".equals(attName) || attName.startsWith("xmlns:") || this.scanOnly) continue;
            FingerprintedQName attributeName = null;
            try {
                int key;
                String[] parts = NameChecker.getQNameParts(attName);
                String attNamespace = parts[0].isEmpty() ? "" : ((QueryModule)this.env).checkURIForPrefix(parts[0]);
                if (attNamespace == null) {
                    this.grumble("Undeclared prefix in attribute name " + Err.wrap(attName, 2), "XPST0081", attOffset);
                }
                if (attFingerprints.contains(key = (attributeName = new FingerprintedQName(parts[0], attNamespace, parts[1])).obtainFingerprint(pool))) {
                    this.grumble("Duplicate expanded attribute name " + attName, "XQST0040", attOffset);
                }
                attFingerprints.add(key);
            } catch (QNameException e) {
                this.grumble("Invalid attribute name " + Err.wrap(attName, 2), "XPST0003", attOffset);
            }
            assert (attributeName != null);
            FixedAttribute attInst = new FixedAttribute(attributeName, 4, null);
            this.setLocation(attInst);
            try {
                select = this.makeAttributeContent(attValue, 1, attValue.charAt(0), false);
            } catch (XPathException err) {
                err.setIsStaticError(true);
                throw err;
            }
            attInst.setRetainedStaticContext(this.env.makeRetainedStaticContext());
            attInst.setSelect(select);
            attInst.setRejectDuplicates();
            this.setLocation(attInst);
            contents.add(this.makeTracer(attInst, attributeName.getStructuredQName()));
        }
        if (c == '/') {
            this.expectChar(this.t.nextChar(), '>');
        } else {
            this.readElementContent(elname, contents);
        }
        Expression[] elk = new Expression[contents.size()];
        for (int i = 0; i < contents.size(); ++i) {
            if (validationMode != 4) {
                ((Expression)contents.get(i)).suppressValidation(validationMode);
            }
            elk[i] = (Expression)contents.get(i);
        }
        Block block = new Block(elk);
        if (changesContext) {
            block.setRetainedStaticContext(this.env.makeRetainedStaticContext());
        }
        elInst.setContentExpression(block);
        for (int n = 0; n < namespaceCount; ++n) {
            ((QueryModule)this.env).undeclareNamespace();
        }
        return this.makeTracer(elInst, qName);
    }

    private Expression makeAttributeContent(String avt, int start, char terminator, boolean scanOnly) throws XPathException {
        Location loc = this.makeLocation();
        ArrayList<Expression> components = new ArrayList<Expression>(10);
        int last = start;
        int len = avt.length();
        while (last < len) {
            XPathException e;
            int i2 = avt.indexOf(terminator, last);
            if (i2 < 0) {
                e = new XPathException("Attribute constructor is not properly terminated");
                e.setIsStaticError(true);
                throw e;
            }
            int i0 = avt.indexOf("{", last);
            int i1 = avt.indexOf("{{", last);
            int i8 = avt.indexOf("}", last);
            int i9 = avt.indexOf("}}", last);
            if (!(i0 >= 0 && i2 >= i0 || i8 >= 0 && i2 >= i8)) {
                this.addStringComponent(components, avt, last, i2);
                if (i2 + 1 < avt.length() && avt.charAt(i2 + 1) == terminator) {
                    components.add(new StringLiteral(terminator + ""));
                    last = i2 + 2;
                    continue;
                }
                last = i2;
                break;
            }
            if (i8 >= 0 && (i0 < 0 || i8 < i0)) {
                if (i8 != i9) {
                    e = new XPathException("Closing curly brace in attribute value template \"" + avt + "\" must be doubled");
                    e.setIsStaticError(true);
                    throw e;
                }
                this.addStringComponent(components, avt, last, i8 + 1);
                last = i8 + 2;
                continue;
            }
            if (i1 >= 0 && i1 == i0) {
                this.addStringComponent(components, avt, last, i1 + 1);
                last = i1 + 2;
                continue;
            }
            if (i0 >= 0) {
                if (i0 > last) {
                    this.addStringComponent(components, avt, last, i0);
                }
                XQueryParser parser = this.newParser();
                parser.executable = this.executable;
                parser.setAllowAbsentExpression(this.allowXPath31Syntax);
                parser.setScanOnly(scanOnly);
                parser.setRangeVariableStack(this.rangeVariables);
                parser.setCatchDepth(this.catchDepth);
                Expression exp = parser.parse(avt, i0 + 1, 215, this.env);
                if (!scanOnly) {
                    exp = exp.simplify();
                }
                last = parser.getTokenizer().currentTokenStartOffset + 1;
                components.add(XQueryParser.makeStringJoin(exp, this.env));
                continue;
            }
            throw new IllegalStateException("Internal error parsing direct attribute constructor");
        }
        if (scanOnly) {
            return Literal.makeLiteral(Int64Value.makeIntegerValue(last));
        }
        if (components.isEmpty()) {
            return new StringLiteral(StringValue.EMPTY_STRING);
        }
        if (components.size() == 1) {
            return (Expression)components.get(0);
        }
        Expression[] args = new Expression[components.size()];
        components.toArray(args);
        RetainedStaticContext rsc = new RetainedStaticContext(this.env);
        Expression fn = SystemFunction.makeCall("concat", rsc, args);
        assert (fn != null);
        fn.setLocation(loc);
        return fn;
    }

    private void addStringComponent(List<Expression> components, String avt, int start, int end) throws XPathException {
        if (start < end) {
            FastStringBuffer sb = new FastStringBuffer(end - start);
            block6: for (int i = start; i < end; ++i) {
                char c = avt.charAt(i);
                switch (c) {
                    case '&': {
                        int semic = avt.indexOf(59, i);
                        if (semic < 0) {
                            this.grumble("No closing ';' found for entity or character reference");
                            continue block6;
                        }
                        String entity = avt.substring(i + 1, semic);
                        sb.append(new Unescaper(this.env.getConfiguration().getValidCharacterChecker()).analyzeEntityReference(entity));
                        i = semic;
                        continue block6;
                    }
                    case '<': {
                        this.grumble("The < character must not appear in attribute content");
                        continue block6;
                    }
                    case '\t': 
                    case '\n': {
                        sb.cat(' ');
                        continue block6;
                    }
                    case '\r': {
                        sb.cat(' ');
                        if (i + 1 >= end || avt.charAt(i + 1) != '\n') continue block6;
                        ++i;
                        continue block6;
                    }
                    default: {
                        sb.cat(c);
                    }
                }
            }
            components.add(new StringLiteral(sb.toString()));
        }
    }

    private int makeNamespaceContent(String avt, int start, char terminator) throws XPathException {
        int last = start;
        int len = avt.length();
        while (last < len) {
            int i2 = avt.indexOf(terminator, last);
            if (i2 < 0) {
                XPathException e = new XPathException("Namespace declaration is not properly terminated");
                e.setIsStaticError(true);
                throw e;
            }
            if (i2 + 1 < avt.length() && avt.charAt(i2 + 1) == terminator) {
                last = i2 + 2;
                continue;
            }
            last = i2;
            break;
        }
        return last;
    }

    private void readElementContent(String startTag, List<Expression> components) throws XPathException {
        try {
            boolean afterEnclosedExpr = false;
            while (true) {
                Expression exp;
                char c;
                FastStringBuffer text = new FastStringBuffer(64);
                boolean containsEntities = false;
                while (true) {
                    if ((c = this.t.nextChar()) == '<') {
                        if (this.t.nextChar() == '!') {
                            if (this.t.nextChar() == '[') {
                                this.readCDATASection(text);
                                containsEntities = true;
                                continue;
                            }
                            this.t.unreadChar();
                            this.t.unreadChar();
                            break;
                        }
                        this.t.unreadChar();
                        break;
                    }
                    if (c == '&') {
                        text.append(this.readEntityReference());
                        containsEntities = true;
                        continue;
                    }
                    if (c == '}') {
                        c = this.t.nextChar();
                        if (c != '}') {
                            this.grumble("'}' must be written as '}}' within element content");
                        }
                        text.cat(c);
                        continue;
                    }
                    if (c == '{') {
                        c = this.t.nextChar();
                        if (c != '{') {
                            c = '{';
                            break;
                        }
                        text.cat(c);
                        continue;
                    }
                    if (!this.charChecker.test(c) && !UTF16CharacterSet.isSurrogate(c)) {
                        this.grumble("Character code " + c + " is not a valid XML character");
                    }
                    text.cat(c);
                }
                if (!(text.isEmpty() || !(containsEntities | ((QueryModule)this.env).isPreserveBoundarySpace()) && Whitespace.isWhite(text))) {
                    ValueOf inst = new ValueOf(new StringLiteral(new StringValue(text.condense())), false, false);
                    this.setLocation(inst);
                    components.add(inst);
                    afterEnclosedExpr = false;
                }
                if (c == '<') {
                    exp = this.parsePseudoXML(true);
                    if (exp instanceof StringLiteral) {
                        String endTag = ((StringLiteral)exp).getStringValue();
                        if (Whitespace.isWhitespace(endTag.charAt(0))) {
                            this.grumble("End tag contains whitespace before the name");
                        }
                        if ((endTag = Whitespace.trim(endTag)).equals(startTag)) {
                            return;
                        }
                        this.grumble("End tag </" + endTag + "> does not match start tag <" + startTag + '>', "XQST0118");
                        continue;
                    }
                    components.add(exp);
                    continue;
                }
                if (afterEnclosedExpr) {
                    Expression previousComponent = components.get(components.size() - 1);
                    boolean previousComponentIsNodeTest = true;
                    UType previousItemType = previousComponent.getStaticUType(UType.ANY);
                    previousComponentIsNodeTest = UType.ANY_NODE.subsumes(previousItemType);
                    if (!previousComponentIsNodeTest) {
                        ValueOf inst = new ValueOf(new StringLiteral(StringValue.EMPTY_STRING), false, false);
                        this.setLocation(inst);
                        components.add(inst);
                    }
                }
                this.t.unreadChar();
                this.t.setState(0);
                this.lookAhead();
                this.nextToken();
                if (this.t.currentToken == 215 && this.allowXPath31Syntax) {
                    components.add(Literal.makeEmptySequence());
                } else {
                    exp = this.parseExpression();
                    if (!((QueryModule)this.env).isPreserveNamespaces()) {
                        exp = new CopyOf(exp, false, 3, null, true);
                    }
                    components.add(exp);
                    this.expect(215);
                }
                afterEnclosedExpr = true;
            }
        } catch (StringIndexOutOfBoundsException err) {
            this.grumble("No closing end tag found for direct element constructor");
            return;
        }
    }

    private Expression parsePIConstructor() throws XPathException {
        try {
            String target;
            FastStringBuffer pi = new FastStringBuffer(64);
            int firstSpace = -1;
            while (!pi.toString().endsWith("?>")) {
                char c = this.t.nextChar();
                if (firstSpace < 0 && " \t\r\n".indexOf(c) >= 0) {
                    firstSpace = pi.length();
                }
                pi.cat(c);
            }
            pi.setLength(pi.length() - 2);
            String data = "";
            if (firstSpace < 0) {
                target = pi.toString();
            } else {
                target = pi.toString().substring(0, firstSpace);
                ++firstSpace;
                while (firstSpace < pi.length() && " \t\r\n".indexOf(pi.charAt(firstSpace)) >= 0) {
                    ++firstSpace;
                }
                data = pi.toString().substring(firstSpace);
            }
            if (!NameChecker.isValidNCName(target)) {
                this.grumble("Invalid processing instruction name " + Err.wrap(target));
            }
            if (target.equalsIgnoreCase("xml")) {
                this.grumble("A processing instruction must not be named 'xml' in any combination of upper and lower case");
            }
            ProcessingInstruction instruction = new ProcessingInstruction(new StringLiteral(target));
            instruction.setSelect(new StringLiteral(data));
            this.setLocation(instruction);
            return instruction;
        } catch (StringIndexOutOfBoundsException err) {
            this.grumble("No closing '?>' found for processing instruction");
            return null;
        }
    }

    private void readCDATASection(FastStringBuffer cdata) throws XPathException {
        try {
            char c = this.t.nextChar();
            this.expectChar(c, 'C');
            c = this.t.nextChar();
            this.expectChar(c, 'D');
            c = this.t.nextChar();
            this.expectChar(c, 'A');
            c = this.t.nextChar();
            this.expectChar(c, 'T');
            c = this.t.nextChar();
            this.expectChar(c, 'A');
            c = this.t.nextChar();
            this.expectChar(c, '[');
            while (!cdata.toString().endsWith("]]>")) {
                cdata.cat(this.t.nextChar());
            }
            cdata.setLength(cdata.length() - 3);
        } catch (StringIndexOutOfBoundsException err) {
            this.grumble("No closing ']]>' found for CDATA section");
        }
    }

    private Expression parseCommentConstructor() throws XPathException {
        try {
            char c = this.t.nextChar();
            this.expectChar(c, '-');
            FastStringBuffer comment = new FastStringBuffer(256);
            while (!comment.toString().endsWith("--")) {
                comment.cat(this.t.nextChar());
            }
            if (this.t.nextChar() != '>') {
                this.grumble("'--' is not permitted in an XML comment");
            }
            CharSequence commentText = comment.subSequence(0, comment.length() - 2);
            Comment instruction = new Comment();
            instruction.setSelect(new StringLiteral(new StringValue(commentText)));
            this.setLocation(instruction);
            return instruction;
        } catch (StringIndexOutOfBoundsException err) {
            this.grumble("No closing '-->' found for comment constructor");
            return null;
        }
    }

    public static Expression stringify(Expression exp, boolean noNodeIfEmpty, StaticContext env) {
        if (exp instanceof StringLiteral) {
            return exp;
        }
        if (exp.getLocalRetainedStaticContext() == null) {
            exp.setRetainedStaticContext(env.makeRetainedStaticContext());
        }
        exp = Atomizer.makeAtomizer(exp, null);
        exp = new AtomicSequenceConverter(exp, BuiltInAtomicType.STRING);
        exp = SystemFunction.makeCall("string-join", exp.getRetainedStaticContext(), exp, new StringLiteral(StringValue.SINGLE_SPACE));
        assert (exp != null);
        if (noNodeIfEmpty) {
            ((StringJoin)((SystemFunctionCall)exp).getTargetFunction()).setReturnEmptyIfEmpty(true);
        }
        return exp;
    }

    @Override
    protected Literal makeStringLiteral(String token) throws XPathException {
        StringLiteral lit;
        if (token.indexOf(38) == -1) {
            lit = new StringLiteral(token);
        } else {
            CharSequence sb = this.unescape(token);
            lit = new StringLiteral(StringValue.makeStringValue(sb));
        }
        this.setLocation(lit);
        return lit;
    }

    @Override
    protected CharSequence unescape(String token) throws XPathException {
        return new Unescaper(this.env.getConfiguration().getValidCharacterChecker()).unescape(token);
    }

    private String readEntityReference() throws XPathException {
        try {
            char c;
            FastStringBuffer sb = new FastStringBuffer(64);
            while ((c = this.t.nextChar()) != ';') {
                sb.cat(c);
            }
            String entity = sb.toString();
            return new Unescaper(this.env.getConfiguration().getValidCharacterChecker()).analyzeEntityReference(entity);
        } catch (StringIndexOutOfBoundsException err) {
            this.grumble("No closing ';' found for entity or character reference");
            return "";
        }
    }

    @Override
    public Expression parseStringConstructor() throws XPathException {
        int offset = this.t.currentTokenStartOffset;
        if (!this.allowXPath31Syntax) {
            throw new XPathException("String constructor expressions require XQuery 3.1");
        }
        ArrayList<Expression> components = new ArrayList<Expression>();
        components.add(new StringLiteral(this.t.currentTokenValue));
        this.t.next();
        block2: while (true) {
            boolean emptyExpression;
            boolean bl = emptyExpression = this.t.currentToken == 215;
            if (emptyExpression) {
                components.add(new StringLiteral(StringValue.EMPTY_STRING));
            } else {
                Expression enclosed = this.parseExpression();
                Expression stringJoin = SystemFunction.makeCall("string-join", this.env.makeRetainedStaticContext(), enclosed, new StringLiteral(" "));
                components.add(stringJoin);
            }
            if (this.t.currentToken != 215) {
                this.grumble("Expected '}' after enclosed expression in string constructor");
            }
            FastStringBuffer sb = new FastStringBuffer(256);
            char c = this.t.nextChar();
            if (c != '`') {
                this.grumble("Expected '}`' after enclosed expression in string constructor");
            }
            int prior = 0;
            int penult = 0;
            try {
                while (true) {
                    c = this.t.nextChar();
                    if (prior == 96 && c == '{') {
                        sb.setLength(sb.length() - 1);
                        components.add(new StringLiteral(sb));
                        this.t.lookAhead();
                        this.t.next();
                        if (this.t.currentToken != 215) continue block2;
                        components.add(Literal.makeEmptySequence());
                        sb.setLength(0);
                        continue;
                    }
                    if (penult == 93 && prior == 96 && c == '`') {
                        sb.setLength(sb.length() - 2);
                        components.add(new StringLiteral(sb));
                        this.t.lookAhead();
                        this.t.next();
                        break block2;
                    }
                    sb.cat(c);
                    penult = prior;
                    prior = c;
                }
            } catch (StringIndexOutOfBoundsException e) {
                this.grumble("String constructor is missing ]`` terminator ");
                continue;
            }
            break;
        }
        Expression[] args = components.toArray(new Expression[0]);
        Expression result = SystemFunction.makeCall("concat", this.env.makeRetainedStaticContext(), args);
        this.setLocation(result, offset);
        return result;
    }

    public String uriLiteral(String in) throws XPathException {
        return Whitespace.applyWhitespaceNormalization(2, this.unescape(in)).toString();
    }

    protected void lookAhead() throws XPathException {
        try {
            this.t.lookAhead();
        } catch (XPathException err) {
            this.grumble(err.getMessage());
        }
    }

    @Override
    protected boolean atStartOfRelativePath() {
        return this.t.currentToken == 217 || super.atStartOfRelativePath();
    }

    @Override
    protected void testPermittedAxis(int axis, String errorCode) throws XPathException {
        super.testPermittedAxis(axis, errorCode);
        if (axis == 8 && this.language == XPathParser.ParsedLanguage.XQUERY) {
            this.grumble("The namespace axis is not available in XQuery", errorCode);
        }
    }

    private char skipSpaces(char c) throws StringIndexOutOfBoundsException {
        while (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
            c = this.t.nextChar();
        }
        return c;
    }

    private void expectChar(char actual, char expected) throws XPathException {
        if (actual != expected) {
            this.grumble("Expected '" + expected + "', found '" + actual + '\'');
        }
    }

    @Override
    protected String getLanguage() {
        return "XQuery";
    }

    private static class Import {
        String namespaceURI;
        List<String> locationURIs;
        int offset;

        private Import() {
        }
    }

    private static class AttributeDetails {
        String value;
        int startOffset;

        private AttributeDetails() {
        }
    }

    public static class Unescaper {
        private final IntPredicate characterChecker;

        public Unescaper(IntPredicate characterChecker) {
            this.characterChecker = characterChecker;
        }

        public CharSequence unescape(String token) throws XPathException {
            FastStringBuffer sb = new FastStringBuffer(token.length());
            for (int i = 0; i < token.length(); ++i) {
                char c = token.charAt(i);
                if (c == '&') {
                    int semic = token.indexOf(59, i);
                    if (semic < 0) {
                        throw new XPathException("No closing ';' found for entity or character reference", "XPST0003");
                    }
                    String entity = token.substring(i + 1, semic);
                    sb.append(this.analyzeEntityReference(entity));
                    i = semic;
                    continue;
                }
                sb.cat(c);
            }
            return sb;
        }

        private String analyzeEntityReference(String entity) throws XPathException {
            if ("lt".equals(entity)) {
                return "<";
            }
            if ("gt".equals(entity)) {
                return ">";
            }
            if ("amp".equals(entity)) {
                return "&";
            }
            if ("quot".equals(entity)) {
                return "\"";
            }
            if ("apos".equals(entity)) {
                return "'";
            }
            if (entity.length() < 2 || entity.charAt(0) != '#') {
                throw new XPathException("invalid character reference &" + entity + ';', "XPST0003");
            }
            return this.parseCharacterReference(entity);
        }

        private String parseCharacterReference(String entity) throws XPathException {
            int value = 0;
            if (entity.charAt(1) == 'x') {
                if (entity.length() < 3) {
                    throw new XPathException("No hex digits in hexadecimal character reference", "XPST0003");
                }
                entity = entity.toLowerCase();
                for (int i = 2; i < entity.length(); ++i) {
                    int digit = "0123456789abcdef".indexOf(entity.charAt(i));
                    if (digit < 0) {
                        throw new XPathException("Invalid hex digit '" + entity.charAt(i) + "' in character reference", "XPST0003");
                    }
                    if ((value = value * 16 + digit) <= 0x10FFFF) continue;
                    throw new XPathException("Character reference exceeds Unicode codepoint limit", "XQST0090");
                }
            } else {
                for (int i = 1; i < entity.length(); ++i) {
                    int digit = "0123456789".indexOf(entity.charAt(i));
                    if (digit < 0) {
                        throw new XPathException("Invalid digit '" + entity.charAt(i) + "' in decimal character reference", "XPST0003");
                    }
                    if ((value = value * 10 + digit) <= 0x10FFFF) continue;
                    throw new XPathException("Character reference exceeds Unicode codepoint limit", "XQST0090");
                }
            }
            if (!this.characterChecker.test(value)) {
                throw new XPathException("Invalid XML character reference x" + Integer.toHexString(value), "XQST0090");
            }
            if (value <= 65535) {
                return "" + (char)value;
            }
            assert (value <= 0x10FFFF);
            return "" + (char)(0xD800 | (value -= 65536) >> 10) + (char)(0xDC00 | value & 0x3FF);
        }
    }

    private static class SortSpec {
        public Expression sortKey;
        public boolean ascending;
        public boolean emptyLeast;
        public String collation;

        private SortSpec() {
        }
    }
}

