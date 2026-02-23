/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.transform.SourceLocator;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ErrorExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.ConditionalBlock;
import net.sf.saxon.expr.instruct.SequenceInstr;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.TraceExpression;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.expr.instruct.WithParam;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.expr.sort.SortKeyDefinition;
import net.sf.saxon.expr.sort.SortKeyDefinitionList;
import net.sf.saxon.functions.Current;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.om.QNameParser;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.BasePatternWithPredicate;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.pattern.PatternThatSetsCurrent;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.XmlProcessingError;
import net.sf.saxon.style.AbsentExtensionElement;
import net.sf.saxon.style.AttributeValueTemplate;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.ExpressionContext;
import net.sf.saxon.style.LiteralResultElement;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.SourceBinding;
import net.sf.saxon.style.StylesheetComponent;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.style.TextValueTemplateNode;
import net.sf.saxon.style.XSLCallTemplate;
import net.sf.saxon.style.XSLContextItem;
import net.sf.saxon.style.XSLCopy;
import net.sf.saxon.style.XSLDocument;
import net.sf.saxon.style.XSLElement;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.style.XSLGeneralVariable;
import net.sf.saxon.style.XSLLocalParam;
import net.sf.saxon.style.XSLLocalVariable;
import net.sf.saxon.style.XSLModuleRoot;
import net.sf.saxon.style.XSLNextIteration;
import net.sf.saxon.style.XSLOnCompletion;
import net.sf.saxon.style.XSLOnEmpty;
import net.sf.saxon.style.XSLOnNonEmpty;
import net.sf.saxon.style.XSLSort;
import net.sf.saxon.style.XSLSortOrMergeKey;
import net.sf.saxon.style.XSLStylesheet;
import net.sf.saxon.style.XSLTemplate;
import net.sf.saxon.style.XSLWithParam;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.trans.XmlProcessingIncident;
import net.sf.saxon.tree.AttributeLocation;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.linked.ElementImpl;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.tree.linked.TextImpl;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.BuiltInType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Whitespace;

public abstract class StyleElement
extends ElementImpl {
    protected String[] extensionNamespaces = null;
    private String[] excludedNamespaces = null;
    protected int version = -1;
    protected ExpressionContext staticContext = null;
    protected XmlProcessingIncident validationError = null;
    protected OnFailure reportingCircumstances = OnFailure.REPORT_ALWAYS;
    protected String defaultXPathNamespace = null;
    protected String defaultCollationName = null;
    protected StructuredQName defaultMode;
    protected boolean expandText = false;
    private StructuredQName objectName;
    private String baseURI;
    private Compilation compilation;
    private Loc savedLocation = null;
    private int defaultValidation = 0;
    protected int actionsCompleted = 0;
    public static final int ACTION_VALIDATE = 1;
    public static final int ACTION_COMPILE = 2;
    public static final int ACTION_TYPECHECK = 4;
    public static final int ACTION_OPTIMIZE = 8;
    public static final int ACTION_FIXUP = 16;
    public static final int ACTION_PROCESS_ATTRIBUTES = 32;
    static final String[] YES_NO = new String[]{"0", "1", "false", "no", "true", "yes"};

    public Compilation getCompilation() {
        return this.compilation;
    }

    public void setCompilation(Compilation compilation) {
        this.compilation = compilation;
    }

    public StylesheetPackage getPackageData() {
        return this.getPrincipalStylesheetModule().getStylesheetPackage();
    }

    @Override
    public Configuration getConfiguration() {
        return this.compilation.getConfiguration();
    }

    public ExpressionContext getStaticContext() {
        if (this.staticContext == null) {
            this.staticContext = new ExpressionContext(this, null);
        }
        return this.staticContext;
    }

    public ExpressionContext getStaticContext(StructuredQName attributeName) {
        return new ExpressionContext(this, attributeName);
    }

    @Override
    public String getBaseURI() {
        if (this.baseURI == null) {
            this.baseURI = super.getBaseURI();
        }
        return this.baseURI;
    }

    public ExpressionVisitor makeExpressionVisitor() {
        return ExpressionVisitor.make(this.getStaticContext());
    }

    public boolean isSchemaAware() {
        return this.getCompilation().isSchemaAware();
    }

    public void substituteFor(StyleElement temp) {
        this.setRawParent(temp.getRawParent());
        this.setAttributes(temp.attributes());
        this.setNamespaceMap(temp.getAllNamespaces());
        this.setNodeName(temp.getNodeName());
        this.setRawSequenceNumber(temp.getRawSequenceNumber());
        this.extensionNamespaces = temp.extensionNamespaces;
        this.excludedNamespaces = temp.excludedNamespaces;
        this.version = temp.version;
        this.staticContext = temp.staticContext;
        this.validationError = temp.validationError;
        this.reportingCircumstances = temp.reportingCircumstances;
        this.compilation = temp.compilation;
    }

    public void setValidationError(XmlProcessingIncident reason, OnFailure circumstances) {
        this.validationError = reason;
        this.reportingCircumstances = circumstances;
    }

    void setIgnoreInstruction() {
        this.reportingCircumstances = OnFailure.IGNORED_INSTRUCTION;
    }

    public boolean isInstruction() {
        return false;
    }

    public boolean isDeclaration() {
        return false;
    }

    public Visibility getVisibility() {
        String vis = this.getAttributeValue("", "visibility");
        if (vis == null) {
            return Visibility.PRIVATE;
        }
        return this.interpretVisibilityValue(vis, "");
    }

    public Visibility getDeclaredVisibility() {
        String vis = this.getAttributeValue("", "visibility");
        if (vis == null) {
            return null;
        }
        return this.interpretVisibilityValue(vis, "");
    }

    protected boolean markTailCalls() {
        return false;
    }

    protected boolean mayContainSequenceConstructor() {
        return false;
    }

    protected boolean mayContainFallback() {
        return this.mayContainSequenceConstructor();
    }

    protected boolean mayContainParam() {
        return false;
    }

    int getDefaultValidation() {
        int v = this.defaultValidation;
        NodeInfo p = this;
        while (v == 0) {
            if (!((p = p.getParent()) instanceof StyleElement)) {
                return 4;
            }
            v = p.defaultValidation;
        }
        return v;
    }

    public final StructuredQName makeQName(String lexicalQName, String errorCode, String attributeName) {
        StructuredQName qName;
        try {
            qName = StructuredQName.fromLexicalQName(lexicalQName, false, true, this);
        } catch (XPathException e) {
            e.setIsStaticError(true);
            if (errorCode == null) {
                String code = e.getErrorCodeLocalPart();
                if ("FONS0004".equals(code)) {
                    e.setErrorCode("XTSE0280");
                } else if ("FOCA0002".equals(code)) {
                    e.setErrorCode("XTSE0020");
                } else if (code == null) {
                    e.setErrorCode("XTSE0020");
                }
            } else {
                e.setErrorCode(errorCode);
            }
            if (attributeName == null) {
                e.setLocator(this);
            } else {
                e.setLocator(new AttributeLocation(this, StructuredQName.fromEQName(attributeName)));
            }
            this.compileError(e);
            qName = new StructuredQName("saxon", "http://saxon.sf.net/", "error-name");
        }
        if (NamespaceConstant.isReserved(qName.getURI())) {
            if (qName.hasURI("http://www.w3.org/1999/XSL/Transform")) {
                if (qName.getLocalPart().equals("initial-template") && (this instanceof XSLTemplate || this instanceof XSLCallTemplate)) {
                    return qName;
                }
                if (qName.getLocalPart().equals("original") && this.findAncestorElement(186) != null) {
                    return qName;
                }
            }
            XmlProcessingIncident err = new XmlProcessingIncident("Namespace prefix " + qName.getPrefix() + " refers to a reserved namespace", "XTSE0080");
            this.compileError(err);
            qName = new StructuredQName("saxon", "http://saxon.sf.net/", "error-name");
        }
        return qName;
    }

    StyleElement findAncestorElement(int fingerprint) {
        NodeInfo parent = this.getParent();
        while (parent instanceof StyleElement) {
            if (parent.getFingerprint() == fingerprint) {
                return (StyleElement)parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    public StylesheetPackage getUsedPackage() {
        return null;
    }

    public Actor getXslOriginal(int componentKind) throws XPathException {
        StyleElement container;
        StyleElement styleElement = container = componentKind == this.getFingerprint() ? this : this.findAncestorElement(componentKind);
        if (!(container instanceof StylesheetComponent)) {
            throw new XPathException("A reference to xsl:original appears within the wrong kind of component: in this case, it must be within xsl:" + this.getNamePool().getLocalName(componentKind), "XTSE0650", this);
        }
        SymbolicName originalName = ((StylesheetComponent)((Object)container)).getSymbolicName();
        StyleElement xslOverride = container.findAncestorElement(186);
        if (xslOverride == null) {
            throw new XPathException("A reference to xsl:original can be used only within an xsl:override element");
        }
        StyleElement usePackage = xslOverride.findAncestorElement(204);
        if (usePackage == null) {
            throw new XPathException("The parent of xsl:override must be an xsl:use-package element", "XTSE0010", xslOverride);
        }
        Component overridden = usePackage.getUsedPackage().getComponent(originalName);
        if (overridden == null) {
            return null;
        }
        return overridden.getActor();
    }

    Component getOverriddenComponent() {
        if (!(this instanceof StylesheetComponent)) {
            return null;
        }
        SymbolicName originalName = ((StylesheetComponent)((Object)this)).getSymbolicName();
        StyleElement xslOverride = this.findAncestorElement(186);
        if (xslOverride == null) {
            return null;
        }
        StyleElement usePackage = xslOverride.findAncestorElement(204);
        if (usePackage == null) {
            return null;
        }
        return usePackage.getUsedPackage().getComponent(originalName);
    }

    public RetainedStaticContext makeRetainedStaticContext() {
        return this.getStaticContext().makeRetainedStaticContext();
    }

    boolean changesRetainedStaticContext() {
        NodeImpl parent = this.getParent();
        return parent == null || !ExpressionTool.equalOrNull(this.getBaseURI(), parent.getBaseURI()) || this.defaultCollationName != null || this.defaultXPathNamespace != null || !(parent instanceof StyleElement) || this.getAllNamespaces() != parent.getAllNamespaces() || this.getEffectiveVersion() != ((StyleElement)parent).getEffectiveVersion();
    }

    public NamespaceResolver getNamespaceResolver() {
        return this;
    }

    public void processAllAttributes() throws XPathException {
        this.processDefaultCollationAttribute();
        this.processDefaultMode();
        this.staticContext = new ExpressionContext(this, null);
        this.processAttributes();
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof StyleElement) {
                ((StyleElement)nodeInfo).processAllAttributes();
                continue;
            }
            if (!(nodeInfo instanceof TextValueTemplateNode)) continue;
            ((TextValueTemplateNode)nodeInfo).parse();
        }
    }

    public void processStandardAttributes(String namespace) {
        this.processExtensionElementAttribute(namespace);
        this.processExcludedNamespaces(namespace);
        this.processVersionAttribute(namespace);
        this.processDefaultXPathNamespaceAttribute(namespace);
        this.processDefaultValidationAttribute(namespace);
        this.processExpandTextAttribute(namespace);
    }

    public String getAttributeValue(String clarkName) {
        FingerprintedQName nn = FingerprintedQName.fromClarkName(clarkName);
        return this.getAttributeValue(nn.getURI(), nn.getLocalPart());
    }

    final void processAttributes() {
        this.prepareAttributes();
    }

    protected void checkUnknownAttribute(NodeName nc) {
        String attributeURI = nc.getURI();
        String elementURI = this.getURI();
        String clarkName = nc.getStructuredQName().getClarkName();
        if (this.forwardsCompatibleModeIsEnabled()) {
            return;
        }
        if (this.isInstruction() && attributeURI.equals("http://www.w3.org/1999/XSL/Transform") && !elementURI.equals("http://www.w3.org/1999/XSL/Transform") && (clarkName.endsWith("}default-collation") || clarkName.endsWith("}default-mode") || clarkName.endsWith("}xpath-default-namespace") || clarkName.endsWith("}expand-text") || clarkName.endsWith("}extension-element-prefixes") || clarkName.endsWith("}exclude-result-prefixes") || clarkName.endsWith("}version") || clarkName.endsWith("}default-validation") || clarkName.endsWith("}use-when"))) {
            return;
        }
        if (elementURI.equals("http://www.w3.org/1999/XSL/Transform") && (clarkName.equals("default-collation") || clarkName.equals("default-mode") || clarkName.equals("expand-text") || clarkName.equals("xpath-default-namespace") || clarkName.equals("extension-element-prefixes") || clarkName.equals("exclude-result-prefixes") || clarkName.equals("version") || clarkName.equals("default-validation") || clarkName.equals("use-when"))) {
            return;
        }
        if ("".equals(attributeURI) || "http://www.w3.org/1999/XSL/Transform".equals(attributeURI)) {
            this.compileErrorInAttribute("Attribute " + Err.wrap(nc.getDisplayName(), 2) + " is not allowed on element " + Err.wrap(this.getDisplayName(), 1), "XTSE0090", clarkName);
        } else if ("http://saxon.sf.net/".equals(attributeURI)) {
            this.compileWarning("Unrecognized attribute in Saxon namespace: " + nc.getDisplayName(), "XTSE0090");
        }
    }

    protected abstract void prepareAttributes();

    StyleElement getLastChildInstruction() {
        StyleElement last = null;
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof StyleElement) {
                last = (StyleElement)nodeInfo;
                continue;
            }
            last = null;
        }
        return last;
    }

    public Expression makeExpression(String expression, AttributeInfo att) {
        try {
            ExpressionContext env = this.staticContext;
            if (att != null) {
                StructuredQName attName = att.getNodeName().getStructuredQName();
                env = this.getStaticContext(attName);
            }
            return ExpressionTool.make(expression, env, 0, 0, this.getCompilation().getCompilerInfo().getCodeInjector());
        } catch (XPathException err) {
            err.maybeSetLocation(this.allocateLocation());
            if (err.isReportableStatically()) {
                this.compileError(err);
            }
            ErrorExpression erexp = new ErrorExpression(new XmlProcessingException(err));
            erexp.setRetainedStaticContext(this.makeRetainedStaticContext());
            erexp.setLocation(this.allocateLocation());
            return erexp;
        }
    }

    Pattern makePattern(String pattern, String attributeName) {
        try {
            ExpressionContext env = this.getStaticContext(new StructuredQName("", "", attributeName));
            Pattern p = Pattern.make(pattern, env, this.getCompilation().getPackageData());
            p.setOriginalText(pattern);
            p.setLocation(this.allocateLocation());
            return p;
        } catch (XPathException err) {
            err.maybeSetErrorCode("XTSE0340");
            if ("XPST0003".equals(err.getErrorCodeLocalPart())) {
                err.setErrorCode("XTSE0340");
            }
            this.compileError(err);
            NodeTestPattern nsp = new NodeTestPattern(AnyNodeTest.getInstance());
            nsp.setLocation(this.allocateLocation());
            return nsp;
        }
    }

    protected Expression makeAttributeValueTemplate(String expression, AttributeInfo att) {
        ExpressionContext env;
        ExpressionContext expressionContext = env = att == null ? this.staticContext : this.getStaticContext(att.getNodeName().getStructuredQName());
        if (att != null) {
            StructuredQName attName = att.getNodeName().getStructuredQName();
            env = this.getStaticContext(attName);
        }
        try {
            return AttributeValueTemplate.make(expression, env);
        } catch (XPathException err) {
            this.compileError(err);
            return new StringLiteral(expression);
        }
    }

    void checkAttributeValue(String name, String value, boolean avt, String[] allowed) {
        if (avt && value.contains("{")) {
            return;
        }
        if (Arrays.binarySearch(allowed, value) < 0) {
            FastStringBuffer sb = new FastStringBuffer(64);
            sb.append("Invalid value for ");
            sb.append("@");
            sb.append(name);
            sb.append(". Value must be one of (");
            for (int i = 0; i < allowed.length; ++i) {
                sb.append(i == 0 ? "" : "|");
                sb.append(allowed[i]);
            }
            sb.append(")");
            this.compileError(sb.toString(), "XTSE0020");
        }
    }

    public boolean processBooleanAttribute(String name, String value) {
        String s = Whitespace.trim(value);
        if (StyleElement.isYes(s)) {
            return true;
        }
        if (StyleElement.isNo(s)) {
            return false;
        }
        this.invalidAttribute(name, "yes|no | true|false | 1|0");
        return false;
    }

    static boolean isYes(String s) {
        return "yes".equals(s) || "true".equals(s) || "1".equals(s);
    }

    static boolean isNo(String s) {
        return "no".equals(s) || "false".equals(s) || "0".equals(s);
    }

    boolean processStreamableAtt(String streamableAtt) {
        boolean streamable = this.processBooleanAttribute("streamable", streamableAtt);
        if (streamable) {
            if (!this.getConfiguration().isLicensedFeature(2)) {
                this.compileWarning("Request for streaming ignored: this Saxon configuration does not support streaming", "SXST0068");
                return false;
            }
            if ("off".equals(this.getConfiguration().getConfigurationProperty(Feature.STREAMABILITY))) {
                this.compileWarning("Request for streaming ignored: streaming is disabled in this Saxon configuration", "SXST0068");
                return false;
            }
        }
        return streamable;
    }

    public SequenceType makeSequenceType(String sequenceType) throws XPathException {
        ExpressionContext env = this.getStaticContext();
        int languageLevel = env.getXPathVersion();
        if (languageLevel == 30) {
            languageLevel = 305;
        }
        XPathParser parser = this.getConfiguration().newExpressionParser("XP", false, languageLevel);
        QNameParser qp = new QNameParser(this.staticContext.getNamespaceResolver()).withAcceptEQName(this.staticContext.getXPathVersion() >= 30).withErrorOnBadSyntax("XPST0003").withErrorOnUnresolvedPrefix("XPST0081");
        parser.setQNameParser(qp);
        return parser.parseSequenceType(sequenceType, this.staticContext);
    }

    SequenceType makeExtendedSequenceType(String sequenceType) throws XPathException {
        this.getStaticContext();
        XPathParser parser = this.getConfiguration().newExpressionParser("XP", false, 31);
        QNameParser qp = new QNameParser(this.staticContext.getNamespaceResolver()).withAcceptEQName(this.staticContext.getXPathVersion() >= 30).withErrorOnBadSyntax("XPST0003").withErrorOnUnresolvedPrefix("XPST0081");
        parser.setQNameParser(qp);
        return parser.parseExtendedSequenceType(sequenceType, this.staticContext);
    }

    void processExtensionElementAttribute(String ns) {
        String ext = this.getAttributeValue(ns, "extension-element-prefixes");
        if (ext != null) {
            int count = 0;
            StringTokenizer st1 = new StringTokenizer(ext, " \t\n\r", false);
            while (st1.hasMoreTokens()) {
                st1.nextToken();
                ++count;
            }
            this.extensionNamespaces = new String[count];
            count = 0;
            StringTokenizer st2 = new StringTokenizer(ext, " \t\n\r", false);
            while (st2.hasMoreTokens()) {
                String uri;
                String s = st2.nextToken();
                if ("#default".equals(s)) {
                    s = "";
                }
                if ((uri = this.getURIForPrefix(s, false)) == null) {
                    this.extensionNamespaces = null;
                    this.compileError("Namespace prefix " + s + " is undeclared", "XTSE1430");
                    continue;
                }
                if (NamespaceConstant.isReserved(uri)) {
                    this.compileError("Namespace " + uri + " is reserved: it cannot be used for extension instructions (perhaps exclude-result-prefixes was intended).", "XTSE0085");
                    this.extensionNamespaces[count++] = uri;
                    continue;
                }
                this.extensionNamespaces[count++] = uri;
            }
        }
    }

    void processExcludedNamespaces(String ns) {
        String ext = this.getAttributeValue(ns, "exclude-result-prefixes");
        if (ext != null) {
            if ("#all".equals(Whitespace.trim(ext))) {
                ArrayList<String> excluded = new ArrayList<String>();
                for (NamespaceBinding binding : this.getAllNamespaces()) {
                    excluded.add(binding.getURI());
                }
                this.excludedNamespaces = excluded.toArray(new String[0]);
            } else {
                int count = 0;
                StringTokenizer st1 = new StringTokenizer(ext, " \t\n\r", false);
                while (st1.hasMoreTokens()) {
                    st1.nextToken();
                    ++count;
                }
                this.excludedNamespaces = new String[count];
                count = 0;
                StringTokenizer st2 = new StringTokenizer(ext, " \t\n\r", false);
                while (st2.hasMoreTokens()) {
                    String s = st2.nextToken();
                    if ("#default".equals(s)) {
                        s = "";
                    } else if ("#all".equals(s)) {
                        this.compileError("In exclude-result-prefixes, cannot mix #all with other values", "XTSE0020");
                    }
                    String uri = this.getURIForPrefix(s, true);
                    if (uri == null) {
                        this.excludedNamespaces = null;
                        this.compileError("Namespace prefix " + s + " is not declared", "XTSE0808");
                        break;
                    }
                    this.excludedNamespaces[count++] = uri;
                    if (!s.isEmpty() || !uri.isEmpty()) continue;
                    this.compileError("Cannot exclude the #default namespace when no default namespace is declared", "XTSE0809");
                }
            }
        }
    }

    protected void processVersionAttribute(String ns) {
        String v = Whitespace.trim(this.getAttributeValue(ns, "version"));
        if (v != null) {
            ConversionResult val = BigDecimalValue.makeDecimalValue(v, true);
            if (val instanceof ValidationFailure) {
                this.version = 30;
                this.compileError("The version attribute must be a decimal literal", "XTSE0110");
            } else {
                this.version = ((BigDecimalValue)val).getDecimalValue().multiply(BigDecimal.TEN).intValue();
                if (this.version < 20 && this.version != 10) {
                    this.issueWarning("Unrecognized version " + val + ": treated as 1.0", this);
                    this.version = 10;
                } else if (this.version > 20 && this.version < 30) {
                    this.issueWarning("Unrecognized version " + val + ": treated as 2.0", this);
                    this.version = 20;
                }
            }
        }
    }

    int getEffectiveVersion() {
        if (this.version == -1) {
            NodeImpl node = this.getParent();
            if (node instanceof StyleElement) {
                this.version = ((StyleElement)node).getEffectiveVersion();
            } else {
                return 20;
            }
        }
        return this.version;
    }

    protected int validateValidationAttribute(String value) {
        int code = Validation.getCode(value);
        if (code == -1) {
            String prefix = this instanceof LiteralResultElement ? "xsl:" : "";
            this.compileError("Invalid value of " + prefix + "validation attribute: '" + value + "'", "XTSE0020");
            code = this.getDefaultValidation();
        }
        if (!this.isSchemaAware()) {
            if (code == 1) {
                this.compileError("To perform validation, a schema-aware XSLT processor is needed", "XTSE1660");
            }
            code = 4;
        }
        return code;
    }

    protected boolean isExtensionAttributeAllowed(String attribute) {
        if (this.getConfiguration().isLicensedFeature(8)) {
            return true;
        }
        this.issueWarning("The option " + this.getDisplayName() + "/@" + attribute + " is ignored because it requires a Saxon-PE license", this);
        return false;
    }

    boolean forwardsCompatibleModeIsEnabled() {
        return this.getEffectiveVersion() > 30;
    }

    boolean xPath10ModeIsEnabled() {
        return this.getEffectiveVersion() < 20;
    }

    void processDefaultCollationAttribute() {
        String ns = this.getURI().equals("http://www.w3.org/1999/XSL/Transform") ? "" : "http://www.w3.org/1999/XSL/Transform";
        String v = this.getAttributeValue(ns, "default-collation");
        StringBuilder reasons = new StringBuilder();
        if (v != null) {
            StringTokenizer st = new StringTokenizer(v, " \t\n\r", false);
            while (st.hasMoreTokens()) {
                String uri = st.nextToken();
                if (uri.equals("http://www.w3.org/2005/xpath-functions/collation/codepoint")) {
                    this.defaultCollationName = uri;
                    return;
                }
                try {
                    URI collationURI = new URI(uri);
                    if (!collationURI.isAbsolute()) {
                        URI base = new URI(this.getBaseURI());
                        collationURI = base.resolve(collationURI);
                        uri = collationURI.toString();
                    }
                } catch (URISyntaxException err) {
                    this.compileError("default collation '" + uri + "' is not a valid URI");
                    uri = "http://www.w3.org/2005/xpath-functions/collation/codepoint";
                }
                try {
                    if (this.getConfiguration().getCollation(uri) != null) {
                        this.defaultCollationName = uri;
                        return;
                    }
                    if (reasons.length() != 0) {
                        reasons.append("; ");
                    }
                    reasons.append("Collation ").append(uri).append(" is not recognized");
                } catch (XPathException e) {
                    if (reasons.length() != 0) {
                        reasons.append("; ");
                    }
                    reasons.append("Collation ").append(uri).append(" is not recognized (").append(e.getMessage()).append(")");
                }
            }
            String msg = "No recognized collation URI found in default-collation attribute";
            if (reasons.length() != 0) {
                msg = msg + ". ";
                msg = msg + reasons.toString();
            }
            this.compileErrorInAttribute(msg, "XTSE0125", new StructuredQName("", ns, "default-collation").getClarkName());
        }
    }

    protected String getDefaultCollationName() {
        StyleElement e = this;
        while (true) {
            if (e.defaultCollationName != null) {
                return e.defaultCollationName;
            }
            NodeImpl p = e.getParent();
            if (!(p instanceof StyleElement)) break;
            e = (StyleElement)p;
        }
        return this.getConfiguration().getDefaultCollationName();
    }

    StringCollator findCollation(String name, String baseURI) throws XPathException {
        return this.getConfiguration().getCollation(name, baseURI);
    }

    void processDefaultMode() {
        String ns = this.getURI().equals("http://www.w3.org/1999/XSL/Transform") ? "" : "http://www.w3.org/1999/XSL/Transform";
        String v = this.getAttributeValue(ns, "default-mode");
        if (v != null) {
            this.defaultMode = v.equals("#unnamed") ? Mode.UNNAMED_MODE_NAME : this.makeQName(v, null, "default-mode");
        }
        PrincipalStylesheetModule psm = this.compilation.getPrincipalStylesheetModule();
        StructuredQName checkedName = this.defaultMode;
        if (psm != null && psm.isDeclaredModes()) {
            psm.addFixupAction(() -> {
                if (psm.getRuleManager().obtainMode(checkedName, false) == null) {
                    XPathException err = new XPathException("Mode " + checkedName.getDisplayName() + " is not declared in an xsl:mode declaration", "XTSE3085");
                    err.setLocation(this);
                    throw err;
                }
            });
        }
    }

    StructuredQName getDefaultMode() throws XPathException {
        if (this.defaultMode == null) {
            this.processDefaultMode();
            if (this.defaultMode == null) {
                NodeImpl p = this.getParent();
                if (p instanceof StyleElement) {
                    this.defaultMode = ((StyleElement)p).getDefaultMode();
                    return this.defaultMode;
                }
                this.defaultMode = Mode.UNNAMED_MODE_NAME;
                return this.defaultMode;
            }
        }
        return this.defaultMode;
    }

    private boolean definesExtensionElement(String uri) {
        if (this.extensionNamespaces == null) {
            return false;
        }
        for (String extensionNamespace : this.extensionNamespaces) {
            if (!extensionNamespace.equals(uri)) continue;
            return true;
        }
        return false;
    }

    public boolean isExtensionNamespace(String uri) {
        NodeInfo anc = this;
        while (anc instanceof StyleElement) {
            if (anc.definesExtensionElement(uri)) {
                return true;
            }
            anc = anc.getParent();
        }
        return false;
    }

    private boolean definesExcludedNamespace(String uri) {
        if (this.excludedNamespaces == null) {
            return false;
        }
        for (String excludedNamespace : this.excludedNamespaces) {
            if (!excludedNamespace.equals(uri)) continue;
            return true;
        }
        return false;
    }

    boolean isExcludedNamespace(String uri) {
        if (uri.equals("http://www.w3.org/1999/XSL/Transform") || uri.equals("http://www.w3.org/XML/1998/namespace")) {
            return true;
        }
        if (this.isExtensionNamespace(uri)) {
            return true;
        }
        NodeInfo anc = this;
        while (anc instanceof StyleElement) {
            if (anc.definesExcludedNamespace(uri)) {
                return true;
            }
            anc = anc.getParent();
        }
        return false;
    }

    void processDefaultXPathNamespaceAttribute(String ns) {
        String v = this.getAttributeValue(ns, "xpath-default-namespace");
        if (v != null) {
            this.defaultXPathNamespace = v;
        }
    }

    public String getDefaultXPathNamespace() {
        NodeInfo anc = this;
        while (anc instanceof StyleElement) {
            String x = anc.defaultXPathNamespace;
            if (x != null) {
                return x;
            }
            anc = anc.getParent();
        }
        return this.compilation.getCompilerInfo().getDefaultElementNamespace();
    }

    void processExpandTextAttribute(String ns) {
        NodeImpl parent;
        String v = this.getAttributeValue(ns, "expand-text");
        this.expandText = v != null ? this.processBooleanAttribute("expand-text", v) : (parent = this.getParent()) instanceof StyleElement && ((StyleElement)parent).expandText;
    }

    void processDefaultValidationAttribute(String ns) {
        String v = this.getAttributeValue(ns, "default-validation");
        if (v != null) {
            int val = Validation.getCode(v);
            if (val == 4 || val == 3) {
                this.defaultValidation = val;
            } else {
                this.compileErrorInAttribute("@default-validation must be preserve|strip", "XTSE0020", "default-validation");
            }
        }
    }

    boolean isExpandingText() {
        return this.expandText;
    }

    public SchemaType getSchemaType(String typeAtt) {
        try {
            String lname;
            String uri;
            if (typeAtt.startsWith("Q{")) {
                StructuredQName q = this.makeQName(typeAtt, "XTSE1520", "type");
                uri = q.getURI();
                lname = q.getLocalPart();
            } else {
                String[] parts = NameChecker.getQNameParts(typeAtt);
                lname = parts[1];
                if ("".equals(parts[0])) {
                    uri = this.getDefaultXPathNamespace();
                } else {
                    uri = this.getURIForPrefix(parts[0], false);
                    if (uri == null) {
                        this.compileError("Namespace prefix for type annotation is undeclared", "XTSE1520");
                        return null;
                    }
                }
            }
            if (uri.equals("http://www.w3.org/2001/XMLSchema")) {
                SchemaType t = BuiltInType.getSchemaTypeByLocalName(lname);
                if (t == null) {
                    this.compileError("Unknown built-in type " + typeAtt, "XTSE1520");
                    return null;
                }
                return t;
            }
            if (!this.getPrincipalStylesheetModule().isImportedSchema(uri)) {
                this.compileError("There is no imported schema for the namespace of type " + typeAtt, "XTSE1520");
                return null;
            }
            StructuredQName qName = new StructuredQName("", uri, lname);
            SchemaType stype = this.getConfiguration().getSchemaType(qName);
            if (stype == null) {
                this.compileError("There is no type named " + typeAtt + " in an imported schema", "XTSE1520");
            }
            return stype;
        } catch (QNameException err) {
            this.compileError("Invalid type name. " + err.getMessage(), "XTSE1520");
            return null;
        }
    }

    public SimpleType getTypeAnnotation(SchemaType schemaType) {
        return (SimpleType)schemaType;
    }

    public void validate(ComponentDeclaration decl) throws XPathException {
    }

    public void postValidate() throws XPathException {
    }

    public void index(ComponentDeclaration decl, PrincipalStylesheetModule top) throws XPathException {
    }

    public Expression typeCheck(String name, Expression exp) throws XPathException {
        if (exp == null) {
            return null;
        }
        Configuration config = this.getConfiguration();
        if (config.getBooleanProperty(Feature.STRICT_STREAMABILITY)) {
            return exp;
        }
        try {
            exp = exp.typeCheck(this.makeExpressionVisitor(), config.makeContextItemStaticInfo(Type.ITEM_TYPE, true));
            exp = ExpressionTool.resolveCallsToCurrentFunction(exp);
            return exp;
        } catch (XPathException err) {
            if (err.isReportableStatically()) {
                err.setLocation(new AttributeLocation(this, StructuredQName.fromClarkName(name)));
                this.compileError(err);
                return exp;
            }
            ErrorExpression erexp = new ErrorExpression(new XmlProcessingException(err));
            ExpressionTool.copyLocationInfo(exp, erexp);
            return erexp;
        }
    }

    void allocateLocalSlots(Expression exp) {
        SlotManager slotManager = this.getContainingSlotManager();
        if (slotManager == null) {
            throw new AssertionError((Object)"Slot manager has not been allocated");
        }
        int firstSlot = slotManager.getNumberOfVariables();
        int highWater = ExpressionTool.allocateSlots(exp, firstSlot, slotManager);
        if (highWater > firstSlot) {
            slotManager.setNumberOfVariables(highWater);
        }
    }

    public Pattern typeCheck(String name, Pattern pattern) throws XPathException {
        if (pattern == null) {
            return null;
        }
        try {
            ItemType cit = Type.ITEM_TYPE;
            pattern = pattern.typeCheck(this.makeExpressionVisitor(), this.getConfiguration().makeContextItemStaticInfo(cit, true));
            boolean usesCurrent = false;
            for (Operand o : pattern.operands()) {
                Expression filter = o.getChildExpression();
                if (!ExpressionTool.callsFunction(filter, Current.FN_CURRENT, false)) continue;
                usesCurrent = true;
                break;
            }
            if (usesCurrent) {
                PatternThatSetsCurrent p2 = new PatternThatSetsCurrent(pattern);
                pattern.bindCurrent(p2.getCurrentBinding());
                pattern = p2;
            }
            return pattern;
        } catch (XPathException err) {
            if (err.isReportableStatically()) {
                XPathException e2 = new XPathException("Error in " + name + " pattern", err);
                e2.setLocator(this);
                e2.setErrorCodeQName(err.getErrorCodeQName());
                throw e2;
            }
            BasePatternWithPredicate p = new BasePatternWithPredicate(new NodeTestPattern(ErrorType.getInstance()), new ErrorExpression(new XmlProcessingException(err)));
            p.setLocation(this.allocateLocation());
            return p;
        }
    }

    public void fixupReferences() throws XPathException {
        for (NodeInfo nodeInfo : this.children(StyleElement.class::isInstance)) {
            ((StyleElement)nodeInfo).fixupReferences();
        }
    }

    public SlotManager getContainingSlotManager() {
        NodeImpl node = this;
        while (true) {
            NodeImpl next = node.getParent();
            assert (next != null);
            if (next instanceof XSLModuleRoot || next.getFingerprint() == 186) {
                if (node instanceof StylesheetComponent) {
                    return ((StylesheetComponent)((Object)node)).getSlotManager();
                }
                return null;
            }
            node = next;
        }
    }

    public void validateSubtree(ComponentDeclaration decl, boolean excludeStylesheet) throws XPathException {
        if (this.isActionCompleted(1)) {
            return;
        }
        this.setActionCompleted(1);
        if (this.validationError != null) {
            if (this.reportingCircumstances == OnFailure.REPORT_ALWAYS) {
                this.compileError(this.validationError);
            } else if (this.reportingCircumstances == OnFailure.REPORT_UNLESS_FORWARDS_COMPATIBLE && !this.forwardsCompatibleModeIsEnabled()) {
                this.compileError(this.validationError);
            } else if (this.reportingCircumstances == OnFailure.REPORT_STATICALLY_UNLESS_FALLBACK_AVAILABLE) {
                boolean hasFallback = false;
                for (NodeInfo nodeInfo : this.children(XSLFallback.class::isInstance)) {
                    hasFallback = true;
                    ((XSLFallback)nodeInfo).validateSubtree(decl, false);
                }
                if (!hasFallback) {
                    this.compileError(this.validationError);
                }
            } else if (this.reportingCircumstances == OnFailure.REPORT_DYNAMICALLY_UNLESS_FALLBACK_AVAILABLE) {
                for (NodeInfo nodeInfo : this.children(XSLFallback.class::isInstance)) {
                    ((XSLFallback)nodeInfo).validateSubtree(decl, false);
                }
            }
        } else {
            try {
                this.validate(decl);
            } catch (XPathException err) {
                this.compileError(err);
            }
            this.validateChildren(decl, excludeStylesheet);
            if (this.getCompilation().getErrorCount() == 0) {
                this.postValidate();
            }
        }
    }

    protected void validateChildren(ComponentDeclaration decl, boolean excludeStylesheet) throws XPathException {
        boolean containsInstructions = this.mayContainSequenceConstructor();
        StyleElement lastChild = null;
        boolean endsWithTextTemplate = false;
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof StyleElement) {
                if (excludeStylesheet && nodeInfo instanceof XSLStylesheet) continue;
                endsWithTextTemplate = false;
                if (containsInstructions && !((StyleElement)nodeInfo).isInstruction() && !this.isPermittedChild((StyleElement)nodeInfo)) {
                    ((StyleElement)nodeInfo).compileError("An " + this.getDisplayName() + " element must not contain an " + nodeInfo.getDisplayName() + " element", "XTSE0010");
                }
                ((StyleElement)nodeInfo).validateSubtree(decl, excludeStylesheet);
                lastChild = (StyleElement)nodeInfo;
                continue;
            }
            endsWithTextTemplate = this.examineTextNode(nodeInfo);
        }
        if (lastChild instanceof XSLLocalVariable && !(this instanceof XSLStylesheet) && !endsWithTextTemplate) {
            lastChild.compileWarning("A variable with no following sibling instructions has no effect", "SXWN9001");
        }
    }

    private boolean examineTextNode(NodeInfo node) throws XPathException {
        if (node instanceof TextValueTemplateNode) {
            ((TextValueTemplateNode)node).validate();
            return !(((TextValueTemplateNode)node).getContentExpression() instanceof Literal);
        }
        return false;
    }

    protected boolean isPermittedChild(StyleElement child) {
        return false;
    }

    public PrincipalStylesheetModule getPrincipalStylesheetModule() {
        return this.getCompilation().getPrincipalStylesheetModule();
    }

    public StylesheetPackage getContainingPackage() {
        PrincipalStylesheetModule psm = this.getPrincipalStylesheetModule();
        return psm == null ? null : psm.getStylesheetPackage();
    }

    void checkSortComesFirst(boolean sortRequired) {
        boolean sortFound = false;
        boolean nonSortFound = false;
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLSort) {
                if (nonSortFound) {
                    ((XSLSort)nodeInfo).compileError("Within " + this.getDisplayName() + ", xsl:sort elements must come before other instructions", "XTSE0010");
                }
                sortFound = true;
                continue;
            }
            if (nodeInfo.getNodeKind() == 3) {
                if (Whitespace.isWhite(nodeInfo.getStringValueCS())) continue;
                nonSortFound = true;
                continue;
            }
            nonSortFound = true;
        }
        if (sortRequired && !sortFound) {
            this.compileError(this.getDisplayName() + " must have at least one xsl:sort child", "XTSE0010");
        }
    }

    public void checkTopLevel(String errorCode, boolean allowOverride) {
        NodeImpl parent = this.getParent();
        assert (parent != null);
        if (parent.getFingerprint() == 186) {
            if (!allowOverride) {
                this.compileError("Element " + this.getDisplayName() + " is not allowed as a child of xsl:override");
            }
        } else if (!this.isTopLevel()) {
            this.compileError("Element " + this.getDisplayName() + " must be top-level (a child of xsl:stylesheet, xsl:transform, or xsl:package)", errorCode);
        }
    }

    public void checkEmpty() {
        if (this.hasChildNodes()) {
            this.compileError("Element must be empty", "XTSE0260");
        }
    }

    public void reportAbsence(String attribute) {
        this.compileError("Element must have an " + Err.wrap(attribute, 2) + " attribute", "XTSE0010");
    }

    public Expression compile(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        return null;
    }

    protected boolean isWithinDeclaredStreamableConstruct() {
        String streamableAtt;
        if (this.getURI().equals("http://www.w3.org/1999/XSL/Transform") && (streamableAtt = this.getAttributeValue("streamable")) != null) {
            return this.processStreamableAtt(streamableAtt);
        }
        NodeImpl parent = this.getParent();
        return parent instanceof StyleElement && ((StyleElement)parent).isWithinDeclaredStreamableConstruct();
    }

    protected String generateId() {
        FastStringBuffer buff = new FastStringBuffer(16);
        this.generateId(buff);
        return buff.toString();
    }

    public void compileDeclaration(Compilation compilation, ComponentDeclaration decl) throws XPathException {
    }

    public Expression compileSequenceConstructor(Compilation compilation, ComponentDeclaration decl, boolean includeParams) throws XPathException {
        boolean containsEmptyTest = false;
        for (NodeInfo nodeInfo : this.children()) {
            int fp = nodeInfo.getFingerprint();
            if (fp != 183 && fp != 184) continue;
            containsEmptyTest = true;
        }
        if (containsEmptyTest) {
            ArrayList<NodeInfo> vars = new ArrayList<NodeInfo>();
            ArrayList<NodeInfo> arrayList = new ArrayList<NodeInfo>();
            ArrayList<NodeInfo> others = new ArrayList<NodeInfo>();
            for (NodeInfo nodeInfo : this.children()) {
                int fp = nodeInfo.getFingerprint();
                if (fp == 206 || fp == 189) {
                    vars.add(nodeInfo);
                    continue;
                }
                if (fp == 183) {
                    arrayList.add(nodeInfo);
                    continue;
                }
                others.add(nodeInfo);
            }
            vars.addAll(others);
            vars.addAll(arrayList);
            return this.compileSequenceConstructor(compilation, decl, new ListIterator(vars), includeParams);
        }
        return this.compileSequenceConstructor(compilation, decl, this.iterateAxis(3), includeParams);
    }

    public Expression compileSequenceConstructor(Compilation compilation, ComponentDeclaration decl, SequenceIterator iter, boolean includeParams) throws XPathException {
        NodeInfo node;
        Location locationId = this.allocateLocation();
        ArrayList<Expression> contents = new ArrayList<Expression>(10);
        boolean containsSpecials = false;
        while ((node = (NodeInfo)iter.next()) != null) {
            Expression child;
            if (node.getNodeKind() == 3) {
                if (this.isExpandingText()) {
                    this.compileContentValueTemplate((TextImpl)node, contents);
                    continue;
                }
                AxisIterator lookahead = node.iterateAxis(7);
                NodeInfo sibling = lookahead.next();
                if (sibling instanceof XSLLocalParam || sibling instanceof XSLSort || sibling instanceof XSLContextItem || sibling instanceof XSLOnCompletion) continue;
                ValueOf text = new ValueOf(new StringLiteral(node.getStringValue()), false, false);
                text.setLocation(this.allocateLocation());
                contents.add(text);
                continue;
            }
            if (node instanceof XSLLocalVariable) {
                XSLLocalVariable var = (XSLLocalVariable)node;
                SourceBinding sourceBinding = var.getSourceBinding();
                var.compileLocalVariable(compilation, decl);
                Expression tail = this.compileSequenceConstructor(compilation, decl, iter, includeParams);
                if (tail == null || Literal.isEmptySequence(tail)) continue;
                LetExpression let = new LetExpression();
                let.setInstruction(true);
                let.setRequiredType(var.getRequiredType());
                let.setVariableQName(sourceBinding.getVariableQName());
                let.setSequence(sourceBinding.getSelectExpression());
                let.setAction(tail);
                sourceBinding.fixupBinding(let);
                locationId = ((StyleElement)node).allocateLocation();
                let.setLocation(locationId);
                contents.add(let);
                if (!var.changesRetainedStaticContext()) continue;
                let.setRetainedStaticContext(this.makeRetainedStaticContext());
                continue;
            }
            if (!(node instanceof StyleElement)) continue;
            StyleElement snode = (StyleElement)node;
            int fp = snode.getFingerprint();
            if (fp == 183 || fp == 184) {
                containsSpecials = true;
            }
            if (snode.validationError != null && !(snode instanceof AbsentExtensionElement)) {
                child = snode.reportingCircumstances == OnFailure.REPORT_IF_INSTANTIATED ? new ErrorExpression(snode.validationError) : this.fallbackProcessing(compilation, decl, snode);
            } else {
                child = snode.compile(compilation, decl);
                if (child != null) {
                    if (snode.changesRetainedStaticContext()) {
                        child.setRetainedStaticContext(snode.makeRetainedStaticContext());
                    }
                    if ((includeParams || !(node instanceof XSLLocalParam)) && this.getCompilation().getCompilerInfo().isCompileWithTracing()) {
                        child = StyleElement.makeTraceInstruction(snode, child);
                    }
                }
            }
            if (child == null) continue;
            contents.add(child);
        }
        if (containsSpecials) {
            return new ConditionalBlock(contents);
        }
        Expression block = Block.makeBlock(contents);
        if (block.getLocation() == null) {
            block.setLocation(locationId);
        }
        if (block.getLocalRetainedStaticContext() == null) {
            block.setRetainedStaticContext(this.makeRetainedStaticContext());
        }
        return block;
    }

    void compileContentValueTemplate(TextImpl node, List<Expression> contents) {
        if (node instanceof TextValueTemplateNode) {
            Expression exp = ((TextValueTemplateNode)node).getContentExpression();
            if (this.getConfiguration().getBooleanProperty(Feature.STRICT_STREAMABILITY) && !(exp instanceof Literal)) {
                exp = new SequenceInstr(exp);
            }
            contents.add(exp);
        } else {
            contents.add(new StringLiteral(node.getStringValue()));
        }
    }

    static Expression makeTraceInstruction(StyleElement source, Expression child) {
        if (child instanceof TraceExpression && !(source instanceof StylesheetComponent)) {
            return child;
        }
        if (source instanceof XSLOnEmpty || source instanceof XSLOnNonEmpty) {
            return child;
        }
        return child;
    }

    Expression fallbackProcessing(Compilation exec, ComponentDeclaration decl, StyleElement instruction) throws XPathException {
        Expression fallback = null;
        for (NodeInfo nodeInfo : this.children(XSLFallback.class::isInstance)) {
            Expression b = ((XSLFallback)nodeInfo).compileSequenceConstructor(exec, decl, true);
            if (b == null) {
                b = Literal.makeEmptySequence();
            }
            if (fallback == null) {
                fallback = b;
                continue;
            }
            fallback = Block.makeBlock(fallback, b);
            fallback.setLocation(this.allocateLocation());
        }
        if (fallback != null) {
            return fallback;
        }
        return new ErrorExpression(instruction.validationError);
    }

    protected Location allocateLocation() {
        if (this.savedLocation == null) {
            this.savedLocation = new Loc(this);
        }
        return this.savedLocation;
    }

    SortKeyDefinitionList makeSortKeys(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        int numberOfSortKeys = 0;
        for (NodeInfo nodeInfo : this.children(XSLSortOrMergeKey.class::isInstance)) {
            ((XSLSortOrMergeKey)nodeInfo).compile(compilation, decl);
            if (nodeInfo instanceof XSLSort && numberOfSortKeys != 0 && ((XSLSort)nodeInfo).getStable() != null) {
                this.compileError("stable attribute may appear only on the first xsl:sort element", "XTSE1017");
            }
            ++numberOfSortKeys;
        }
        if (numberOfSortKeys > 0) {
            SortKeyDefinition[] keys = new SortKeyDefinition[numberOfSortKeys];
            boolean bl = false;
            for (NodeInfo nodeInfo : this.children(XSLSortOrMergeKey.class::isInstance)) {
                keys[++var5_7] = (SortKeyDefinition)((XSLSortOrMergeKey)nodeInfo).getSortKeyDefinition().simplify();
            }
            return new SortKeyDefinitionList(keys);
        }
        return null;
    }

    StructuredQName[] getUsedAttributeSets(String use) {
        ArrayList<StructuredQName> nameList = new ArrayList<StructuredQName>(4);
        StringTokenizer st = new StringTokenizer(use, " \t\n\r", false);
        while (st.hasMoreTokens()) {
            String asetname = st.nextToken();
            StructuredQName name = this.makeQName(asetname, "XTSE0710", "use-attribute-sets");
            nameList.add(name);
        }
        return nameList.toArray(new StructuredQName[0]);
    }

    Visibility interpretVisibilityValue(String s, String flags) {
        for (Visibility v : Visibility.values()) {
            if (!v.show().equals(s) || !flags.contains("h") && s.equals("hidden") || !flags.contains("a") && s.equals("absent")) continue;
            return v;
        }
        this.invalidAttribute("visibility", "public|final|private|abstract" + (flags.contains("h") ? "|hidden" : "") + (flags.contains("a") ? "|absent" : ""));
        return null;
    }

    public WithParam[] getWithParamInstructions(Expression parent, Compilation compilation, ComponentDeclaration decl, boolean tunnel) throws XPathException {
        int count = 0;
        for (NodeInfo nodeInfo : this.children(XSLWithParam.class::isInstance)) {
            XSLWithParam xSLWithParam = (XSLWithParam)nodeInfo;
            if (xSLWithParam.getSourceBinding().hasProperty(SourceBinding.BindingProperty.TUNNEL) != tunnel) continue;
            ++count;
        }
        if (count == 0) {
            return WithParam.EMPTY_ARRAY;
        }
        WithParam[] array = new WithParam[count];
        count = 0;
        for (NodeInfo nodeInfo : this.children(XSLWithParam.class::isInstance)) {
            XSLWithParam wp = (XSLWithParam)nodeInfo;
            if (wp.getSourceBinding().hasProperty(SourceBinding.BindingProperty.TUNNEL) != tunnel) continue;
            WithParam p = wp.compileWithParam(parent, compilation, decl);
            if (wp.getParent() instanceof XSLNextIteration && wp.hasChildNodes()) {
                SequenceType required = ((XSLNextIteration)wp.getParent()).getDeclaredParamType(wp.getSourceBinding().getVariableQName());
                wp.checkAgainstRequiredType(required);
                p.getSelectOperand().setChildExpression(wp.sourceBinding.getSelectExpression());
            }
            array[count++] = p;
        }
        return array;
    }

    public void compileError(XmlProcessingError error) {
        XmlProcessingIncident.maybeSetHostLanguage(error, HostLanguage.XSLT);
        if (error.getLocation() == null || (error.getLocation() instanceof Loc || error.getLocation() instanceof Expression) && !(this instanceof StylesheetComponent)) {
            XmlProcessingIncident.maybeSetLocation(error, this);
        }
        this.getCompilation().reportError(error);
    }

    public void compileError(XPathException err) {
        if (err.getLocator() == null) {
            err.setLocation(this);
        }
        XmlProcessingIncident se = new XmlProcessingIncident(err.getMessage(), err.getErrorCodeLocalPart(), err.getLocator());
        se.setHostLanguage(HostLanguage.XSLT);
        this.compileError(se);
    }

    public void compileError(String message) {
        this.compileError(message, "XTSE0010");
    }

    public void compileError(String message, StructuredQName errorCode) {
        XmlProcessingIncident error = new XmlProcessingIncident(message, errorCode.getEQName(), this);
        error.setHostLanguage(HostLanguage.XSLT);
        this.compileError(error);
    }

    public void compileError(String message, String errorCode) {
        this.compileError(new XPathException(message, errorCode, this));
    }

    public void compileError(String message, String errorCode, Location loc) {
        this.compileError(new XPathException(message, errorCode, loc));
    }

    public void compileErrorInAttribute(String message, String errorCode, String attributeName) {
        StructuredQName att = StructuredQName.fromClarkName(attributeName);
        AttributeLocation location = new AttributeLocation(this, att);
        this.compileError(new XPathException(message, errorCode, location));
    }

    protected void invalidAttribute(String attributeName, String allowedValues) {
        this.compileErrorInAttribute("Attribute " + this.getDisplayName() + "/@" + attributeName + " must be " + allowedValues, "XTSE0020", attributeName);
    }

    protected void requireSyntaxExtensions(String attributeName) {
        if (!this.getConfiguration().getBooleanProperty(Feature.ALLOW_SYNTAX_EXTENSIONS)) {
            this.compileErrorInAttribute("Attribute " + this.getDisplayName() + "/@" + attributeName + " is allowed only if syntax extensions are enabled", "XTSE0020", attributeName);
        }
    }

    void undeclaredNamespaceError(String prefix, String errorCode, String attributeName) {
        if (errorCode == null) {
            errorCode = "XTSE0280";
        }
        this.compileErrorInAttribute("Undeclared namespace prefix " + Err.wrap(prefix), errorCode, attributeName);
    }

    public void compileWarning(String message, StructuredQName errorCode) {
        this.getCompilation().reportWarning(message, errorCode.getEQName(), this);
    }

    public void compileWarning(String message, String errorCode) {
        this.getCompilation().reportWarning(message, errorCode, this);
    }

    public void compileWarning(String message, String errorCode, Location location) {
        this.getCompilation().reportWarning(message, errorCode, location);
    }

    protected void issueWarning(XPathException error) {
        if (error.getLocator() == null) {
            error.setLocator(this);
        }
        this.getCompilation().reportWarning(error);
    }

    protected void issueWarning(String message, SourceLocator locator) {
        XPathException tce = new XPathException(message);
        if (locator == null) {
            tce.setLocator(this);
        } else {
            tce.setLocator(locator);
        }
        this.issueWarning(tce);
    }

    public boolean isTopLevel() {
        return this.getParent() instanceof XSLModuleRoot;
    }

    boolean isConstructingComplexContent() {
        if (!this.isInstruction()) {
            return false;
        }
        NodeInfo parent = this.getParent();
        while (parent instanceof StyleElement && ((StyleElement)parent).isInstruction()) {
            if (parent instanceof XSLGeneralVariable) {
                return ((XSLGeneralVariable)parent).getAttributeValue("as") == null;
            }
            if (parent instanceof XSLElement || parent instanceof LiteralResultElement || parent instanceof XSLDocument || parent instanceof XSLCopy) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public SourceBinding getBindingInformation(StructuredQName name) {
        return null;
    }

    public SourceBinding bindVariable(StructuredQName qName) {
        SourceBinding decl = this.bindLocalVariable(qName);
        if (decl != null) {
            return decl;
        }
        SourceBinding binding = this.getPrincipalStylesheetModule().getGlobalVariableBinding(qName);
        if (binding == null || Navigator.isAncestorOrSelf(binding.getSourceElement(), this)) {
            return null;
        }
        return binding;
    }

    public SourceBinding bindLocalVariable(StructuredQName qName) {
        block6: {
            NodeInfo curr = this;
            NodeInfo prev = this;
            SourceBinding implicit = this.hasImplicitBinding(qName);
            if (implicit != null) {
                return implicit;
            }
            if (!this.isTopLevel()) {
                SourceBinding sourceBinding;
                AxisIterator preceding = curr.iterateAxis(11);
                do {
                    curr = preceding.next();
                    while (curr == null) {
                        curr = prev.getParent();
                        if (curr instanceof StyleElement && (implicit = ((StyleElement)curr).hasImplicitBinding(qName)) != null) {
                            return implicit;
                        }
                        while (curr instanceof StyleElement && !((StyleElement)curr).seesAvuncularVariables()) {
                            curr = curr.getParent();
                        }
                        prev = curr;
                        if (curr.getParent() instanceof XSLModuleRoot) break;
                        preceding = curr.iterateAxis(11);
                        curr = preceding.next();
                    }
                    if (curr.getParent() instanceof XSLModuleRoot) break block6;
                } while (!(curr instanceof XSLGeneralVariable) || (sourceBinding = ((XSLGeneralVariable)curr).getBindingInformation(qName)) == null);
                return sourceBinding;
            }
        }
        return null;
    }

    protected boolean seesAvuncularVariables() {
        return true;
    }

    protected SourceBinding hasImplicitBinding(StructuredQName name) {
        return null;
    }

    public StructuredQName getObjectName() {
        return this.objectName;
    }

    public void setObjectName(StructuredQName qName) {
        this.objectName = qName;
    }

    public Iterator<String> getProperties() {
        ArrayList<String> list = new ArrayList<String>(10);
        for (AttributeInfo att : this.attributes()) {
            list.add(att.getNodeName().getStructuredQName().getClarkName());
        }
        return list.iterator();
    }

    boolean isActionCompleted(int action) {
        return (this.actionsCompleted & action) != 0;
    }

    void setActionCompleted(int action) {
        this.actionsCompleted |= action;
    }

    public static enum OnFailure {
        REPORT_ALWAYS,
        REPORT_UNLESS_FORWARDS_COMPATIBLE,
        REPORT_IF_INSTANTIATED,
        REPORT_STATICALLY_UNLESS_FALLBACK_AVAILABLE,
        REPORT_DYNAMICALLY_UNLESS_FALLBACK_AVAILABLE,
        IGNORED_INSTRUCTION;

    }
}

