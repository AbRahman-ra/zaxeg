/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ErrorExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.ICompilerService;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.ExpressionContext;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetComponent;
import net.sf.saxon.style.StylesheetModule;
import net.sf.saxon.style.XSLLocalParam;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.SimpleMode;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.trans.rules.Rule;
import net.sf.saxon.trans.rules.RuleManager;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Whitespace;

public final class XSLTemplate
extends StyleElement
implements StylesheetComponent {
    private String matchAtt = null;
    private String modeAtt = null;
    private String nameAtt = null;
    private String priorityAtt = null;
    private String asAtt = null;
    private String visibilityAtt = null;
    private StructuredQName[] modeNames;
    private String diagnosticId;
    private Pattern match;
    private boolean prioritySpecified;
    private double priority;
    private SlotManager stackFrameMap;
    private NamedTemplate compiledNamedTemplate;
    private Map<StructuredQName, TemplateRule> compiledTemplateRules = new HashMap<StructuredQName, TemplateRule>();
    private SequenceType requiredType = SequenceType.ANY_SEQUENCE;
    private boolean declaresRequiredType = false;
    private boolean hasRequiredParams = false;
    private boolean isTailRecursive = false;
    private Visibility visibility = Visibility.PRIVATE;
    private ItemType requiredContextItemType = AnyItemType.getInstance();
    private boolean mayOmitContextItem = true;
    private boolean absentFocus = false;
    private boolean jitCompilationDone = false;
    private boolean explaining;

    @Override
    public NamedTemplate getActor() {
        return this.compiledNamedTemplate;
    }

    @Override
    public void setCompilation(Compilation compilation) {
        super.setCompilation(compilation);
    }

    @Override
    public boolean isDeclaration() {
        return true;
    }

    public boolean isDeferredCompilation(Compilation compilation) {
        return compilation.isPreScan() && this.getTemplateName() == null && !compilation.isLibraryPackage();
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    protected boolean mayContainParam() {
        return true;
    }

    @Override
    protected boolean isWithinDeclaredStreamableConstruct() {
        try {
            for (Mode m : this.getApplicableModes()) {
                if (!m.isDeclaredStreamable()) continue;
                return true;
            }
        } catch (XPathException e) {
            return false;
        }
        return false;
    }

    public void setContextItemRequirements(ItemType type, boolean mayBeOmitted, boolean absentFocus) {
        this.requiredContextItemType = type;
        this.mayOmitContextItem = mayBeOmitted;
        this.absentFocus = absentFocus;
    }

    @Override
    protected boolean isPermittedChild(StyleElement child) {
        return child instanceof XSLLocalParam || child.getFingerprint() == 144;
    }

    public StructuredQName getTemplateName() {
        String nameAtt;
        if (this.getObjectName() == null && (nameAtt = this.getAttributeValue("", "name")) != null) {
            this.setObjectName(this.makeQName(nameAtt, null, "name"));
        }
        return this.getObjectName();
    }

    @Override
    public SymbolicName getSymbolicName() {
        if (this.getTemplateName() == null) {
            return null;
        }
        return new SymbolicName(200, this.getTemplateName());
    }

    public ItemType getRequiredContextItemType() {
        return this.requiredContextItemType;
    }

    public boolean isMayOmitContextItem() {
        return this.mayOmitContextItem;
    }

    @Override
    public void checkCompatibility(Component component) {
        SequenceType req;
        NamedTemplate other = (NamedTemplate)component.getActor();
        if (!this.getSymbolicName().equals(other.getSymbolicName())) {
            throw new IllegalArgumentException();
        }
        SequenceType sequenceType = req = this.requiredType == null ? SequenceType.ANY_SEQUENCE : this.requiredType;
        if (!req.equals(other.getRequiredType())) {
            this.compileError("The overriding template has a different required type from the overridden template", "XTSE3070");
            return;
        }
        if (!this.requiredContextItemType.equals(other.getRequiredContextItemType()) || this.mayOmitContextItem != other.isMayOmitContextItem() || this.absentFocus != other.isAbsentFocus()) {
            this.compileError("The required context item for the overriding template differs from that of the overridden template", "XTSE3070");
            return;
        }
        List<NamedTemplate.LocalParamInfo> otherParams = other.getLocalParamDetails();
        HashSet<StructuredQName> overriddenParams = new HashSet<StructuredQName>();
        for (NamedTemplate.LocalParamInfo localParamInfo : otherParams) {
            XSLLocalParam lp1 = this.getParam(localParamInfo.name);
            if (lp1 == null) {
                if (!localParamInfo.isTunnel) {
                    this.compileError("The overridden template declares a parameter " + localParamInfo.name.getDisplayName() + " which is not declared in the overriding template", "XTSE3070");
                }
                return;
            }
            if (!lp1.getRequiredType().equals(localParamInfo.requiredType)) {
                lp1.compileError("The parameter " + localParamInfo.name.getDisplayName() + " has a different required type in the overridden template", "XTSE3070");
                return;
            }
            if (lp1.isRequiredParam() != localParamInfo.isRequired && !localParamInfo.isTunnel) {
                lp1.compileError("The parameter " + localParamInfo.name.getDisplayName() + " is " + (lp1.isRequiredParam() ? "required" : "optional") + " in the overriding template, but " + (localParamInfo.isRequired ? "required" : "optional") + " in the overridden template", "XTSE3070");
                return;
            }
            if (lp1.isTunnelParam() != localParamInfo.isTunnel) {
                lp1.compileError("The parameter " + localParamInfo.name.getDisplayName() + " is a " + (lp1.isTunnelParam() ? "tunnel" : "non-tunnel") + " parameter in the overriding template, but " + (localParamInfo.isTunnel ? "tunnel" : "non-tunnel") + " parameter in the overridden template", "XTSE3070");
                return;
            }
            overriddenParams.add(localParamInfo.name);
        }
        for (NodeInfo nodeInfo : this.children(XSLLocalParam.class::isInstance)) {
            if (overriddenParams.contains(((XSLLocalParam)nodeInfo).getObjectName()) || !((XSLLocalParam)nodeInfo).isRequiredParam()) continue;
            ((XSLLocalParam)nodeInfo).compileError("An overriding template cannot introduce a required parameter that is not declared in the overridden template", "XTSE3070");
        }
    }

    public XSLLocalParam getParam(StructuredQName name) {
        for (NodeInfo nodeInfo : this.children(XSLLocalParam.class::isInstance)) {
            if (!name.equals(((XSLLocalParam)nodeInfo).getObjectName())) continue;
            return (XSLLocalParam)nodeInfo;
        }
        return null;
    }

    @Override
    public void prepareAttributes() {
        AttributeMap atts = this.attributes();
        String extraAsAtt = null;
        for (AttributeInfo att : atts) {
            NodeName name = att.getNodeName();
            String f = name.getDisplayName();
            if (f.equals("mode")) {
                this.modeAtt = Whitespace.trim(att.getValue());
                continue;
            }
            if (f.equals("name")) {
                this.nameAtt = Whitespace.trim(att.getValue());
                continue;
            }
            if (f.equals("match")) {
                this.matchAtt = att.getValue();
                continue;
            }
            if (f.equals("priority")) {
                this.priorityAtt = Whitespace.trim(att.getValue());
                continue;
            }
            if (f.equals("as")) {
                this.asAtt = att.getValue();
                continue;
            }
            if (f.equals("visibility")) {
                this.visibilityAtt = Whitespace.trim(att.getValue());
                continue;
            }
            if (name.hasURI("http://saxon.sf.net/")) {
                this.isExtensionAttributeAllowed(name.getDisplayName());
                if (name.getLocalPart().equals("as")) {
                    extraAsAtt = att.getValue();
                    continue;
                }
                if (!name.getLocalPart().equals("explain")) continue;
                this.explaining = XSLTemplate.isYes(Whitespace.trim(att.getValue()));
                continue;
            }
            this.checkUnknownAttribute(name);
        }
        try {
            if (this.modeAtt == null) {
                if (this.matchAtt != null) {
                    StructuredQName defaultMode = this.getDefaultMode();
                    if (defaultMode == null) {
                        defaultMode = Mode.UNNAMED_MODE_NAME;
                    }
                    this.modeNames = new StructuredQName[1];
                    this.modeNames[0] = defaultMode;
                }
            } else {
                if (this.matchAtt == null) {
                    this.compileError("The mode attribute must be absent if the match attribute is absent", "XTSE0500");
                }
                this.getModeNames();
            }
        } catch (XPathException err) {
            err.maybeSetErrorCode("XTSE0280");
            if (err.getErrorCodeLocalPart().equals("XTSE0020")) {
                err.setErrorCode("XTSE0550");
            }
            err.setIsStaticError(true);
            this.compileError(err);
        }
        if (this.nameAtt != null) {
            if (this.getObjectName() == null) {
                StructuredQName qName = this.makeQName(this.nameAtt, "XTSE0280", "name");
                this.setObjectName(qName);
            }
            if (this.compiledNamedTemplate != null) {
                this.compiledNamedTemplate.setTemplateName(this.getObjectName());
            }
            this.diagnosticId = this.nameAtt;
        }
        boolean bl = this.prioritySpecified = this.priorityAtt != null;
        if (this.prioritySpecified) {
            if (this.matchAtt == null) {
                this.compileError("The priority attribute must be absent if the match attribute is absent", "XTSE0500");
            }
            try {
                if (!BigDecimalValue.castableAsDecimal(this.priorityAtt)) {
                    this.compileError("Invalid numeric value for priority (" + this.priority + ')', "XTSE0530");
                }
                this.priority = Double.parseDouble(this.priorityAtt);
            } catch (NumberFormatException err) {
                this.compileError("Invalid numeric value for priority (" + this.priority + ')', "XTSE0530");
            }
        }
        if (this.matchAtt != null) {
            this.match = this.makePattern(this.matchAtt, "match");
            if (this.diagnosticId == null) {
                this.diagnosticId = "match=\"" + this.matchAtt + '\"';
                if (this.modeAtt != null) {
                    this.diagnosticId = this.diagnosticId + " mode=\"" + this.modeAtt + '\"';
                }
            }
        }
        if (this.match == null && this.nameAtt == null) {
            this.compileError("xsl:template must have a name or match attribute (or both)", "XTSE0500");
        }
        if (this.asAtt != null) {
            try {
                this.requiredType = this.makeSequenceType(this.asAtt);
                this.declaresRequiredType = true;
            } catch (XPathException e) {
                this.compileErrorInAttribute(e.getMessage(), e.getErrorCodeLocalPart(), "as");
            }
        }
        if (extraAsAtt != null) {
            SequenceType extraResultType;
            this.declaresRequiredType = true;
            try {
                extraResultType = this.makeExtendedSequenceType(extraAsAtt);
            } catch (XPathException e) {
                this.compileErrorInAttribute(e.getMessage(), e.getErrorCodeLocalPart(), "saxon:as");
                extraResultType = this.requiredType;
            }
            if (this.asAtt != null) {
                Affinity rel = this.getConfiguration().getTypeHierarchy().sequenceTypeRelationship(extraResultType, this.requiredType);
                if (rel == Affinity.SAME_TYPE || rel == Affinity.SUBSUMED_BY) {
                    this.requiredType = extraResultType;
                } else {
                    this.compileErrorInAttribute("When both are present, @saxon:as must be a subtype of @as", "SXER7TBA", "saxon:as");
                }
            } else {
                this.requiredType = extraResultType;
            }
        }
        if (this.visibilityAtt != null) {
            this.visibility = this.interpretVisibilityValue(this.visibilityAtt, "");
            if (this.nameAtt == null) {
                this.compileError("xsl:template/@visibility can be specified only if the template has a @name attribute", "XTSE0020");
            } else {
                this.compiledNamedTemplate.setDeclaredVisibility(this.getVisibility());
            }
        }
    }

    @Override
    public void processAllAttributes() throws XPathException {
        if (!this.isDeferredCompilation(this.getCompilation())) {
            super.processAllAttributes();
        } else {
            this.processDefaultCollationAttribute();
            this.processDefaultMode();
            this.staticContext = new ExpressionContext(this, null);
            this.processAttributes();
        }
    }

    public StructuredQName[] getModeNames() throws XPathException {
        if (this.modeNames == null) {
            if (this.modeAtt == null) {
                this.modeAtt = this.getAttributeValue("mode");
                if (this.modeAtt == null) {
                    this.modeAtt = "#default";
                }
            }
            boolean allModes = false;
            String[] tokens = Whitespace.trim(this.modeAtt).split("[ \t\n\r]+");
            int count = tokens.length;
            this.modeNames = new StructuredQName[count];
            count = 0;
            for (String s : tokens) {
                StructuredQName mname;
                if ("#default".equals(s)) {
                    mname = this.getDefaultMode();
                    if (mname == null) {
                        mname = Mode.UNNAMED_MODE_NAME;
                    }
                } else if ("#unnamed".equals(s)) {
                    mname = Mode.UNNAMED_MODE_NAME;
                } else if ("#all".equals(s)) {
                    allModes = true;
                    mname = Mode.OMNI_MODE;
                } else {
                    mname = this.makeQName(s, "XTSE0550", "mode");
                }
                for (int e = 0; e < count; ++e) {
                    if (!this.modeNames[e].equals(mname)) continue;
                    this.compileError("In the list of modes, the value " + s + " is duplicated", "XTSE0550");
                }
                this.modeNames[count++] = mname;
            }
            if (allModes && count > 1) {
                this.compileError("mode='#all' cannot be combined with other modes", "XTSE0550");
            }
        }
        return this.modeNames;
    }

    public Set<Mode> getApplicableModes() throws XPathException {
        StructuredQName[] names = this.getModeNames();
        HashSet<Mode> modes = new HashSet<Mode>(names.length);
        RuleManager mgr = this.getPrincipalStylesheetModule().getRuleManager();
        for (StructuredQName name : names) {
            if (name.equals(Mode.OMNI_MODE)) {
                modes.add(mgr.getUnnamedMode());
                modes.addAll(mgr.getAllNamedModes());
                continue;
            }
            Mode mode = mgr.obtainMode(name, false);
            if (mode == null) continue;
            modes.add(mode);
        }
        return modes;
    }

    public boolean isOmniMode() throws XPathException {
        for (StructuredQName name : this.getModeNames()) {
            if (!name.equals(Mode.OMNI_MODE)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.stackFrameMap = this.getConfiguration().makeSlotManager();
        this.checkTopLevel("XTSE0010", true);
        if (this.match != null) {
            this.match = this.typeCheck("match", this.match);
            if (this.match.getItemType() instanceof ErrorType) {
                this.issueWarning(new XPathException("Pattern will never match anything", "SXWN9015", this));
            }
            if (this.getPrincipalStylesheetModule().isDeclaredModes()) {
                RuleManager manager = this.getPrincipalStylesheetModule().getRuleManager();
                if (this.modeNames != null) {
                    for (StructuredQName name : this.modeNames) {
                        if (name.equals(Mode.UNNAMED_MODE_NAME) && !manager.isUnnamedModeExplicit()) {
                            this.compileError("The unnamed mode has not been declared in an xsl:mode declaration", "XTSE3085");
                        }
                        if (manager.obtainMode(name, false) != null) continue;
                        this.compileError("Mode name " + name.getDisplayName() + " has not been declared in an xsl:mode declaration", "XTSE3085");
                    }
                } else if (!manager.isUnnamedModeExplicit()) {
                    this.compileError("The unnamed mode has not been declared in an xsl:mode declaration", "XTSE3085");
                }
            }
            if (this.visibility == Visibility.ABSTRACT) {
                this.compileError("An abstract template must have no match attribute");
            }
        }
        boolean hasContent = false;
        for (NodeInfo nodeInfo : this.children(StyleElement.class::isInstance)) {
            if (nodeInfo.getFingerprint() == 144) continue;
            if (nodeInfo instanceof XSLLocalParam) {
                if (!((XSLLocalParam)nodeInfo).isRequiredParam()) continue;
                this.hasRequiredParams = true;
                continue;
            }
            hasContent = true;
        }
        if (this.visibility == Visibility.ABSTRACT && hasContent) {
            this.compileError("A template with visibility='abstract' must have no body");
        }
    }

    @Override
    public void validateSubtree(ComponentDeclaration decl, boolean excludeStylesheet) throws XPathException {
        if (!this.isDeferredCompilation(this.getCompilation())) {
            super.validateSubtree(decl, excludeStylesheet);
        } else {
            try {
                this.validate(decl);
            } catch (XPathException err) {
                this.compileError(err);
            }
        }
    }

    @Override
    public void index(ComponentDeclaration decl, PrincipalStylesheetModule top) throws XPathException {
        if (this.getTemplateName() != null) {
            if (this.compiledNamedTemplate == null) {
                this.compiledNamedTemplate = new NamedTemplate(this.getTemplateName());
            }
            top.indexNamedTemplate(decl);
        }
    }

    @Override
    public boolean markTailCalls() {
        StyleElement last = this.getLastChildInstruction();
        return last != null && last.markTailCalls();
    }

    @Override
    public void compileDeclaration(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        if (this.isDeferredCompilation(compilation)) {
            this.createSkeletonTemplate(compilation, decl);
            return;
        }
        if (compilation.getCompilerInfo().getOptimizerOptions().isSet(16384)) {
            this.isTailRecursive = this.markTailCalls();
        }
        Expression body = this.compileSequenceConstructor(compilation, decl, true);
        body.restoreParentPointers();
        RetainedStaticContext rsc = this.makeRetainedStaticContext();
        if (body.getRetainedStaticContext() == null) {
            body.setRetainedStaticContext(rsc);
        }
        if (this.match != null && compilation.getConfiguration().getBooleanProperty(Feature.STRICT_STREAMABILITY) && this.isWithinDeclaredStreamableConstruct()) {
            this.checkStrictStreamability(body);
        }
        if (this.getTemplateName() != null) {
            this.compileNamedTemplate(compilation, body, decl);
        }
        if (this.match != null) {
            this.compileTemplateRule(compilation, body, decl);
        }
    }

    private void checkStrictStreamability(Expression body) throws XPathException {
        this.getConfiguration().checkStrictStreamability(this, body);
    }

    private void compileNamedTemplate(Compilation compilation, Expression body, ComponentDeclaration decl) throws XPathException {
        RetainedStaticContext rsc = body.getRetainedStaticContext();
        this.compiledNamedTemplate.setPackageData(rsc.getPackageData());
        this.compiledNamedTemplate.setBody(body);
        this.compiledNamedTemplate.setStackFrameMap(this.stackFrameMap);
        this.compiledNamedTemplate.setSystemId(this.getSystemId());
        this.compiledNamedTemplate.setLineNumber(this.getLineNumber());
        this.compiledNamedTemplate.setHasRequiredParams(this.hasRequiredParams);
        this.compiledNamedTemplate.setRequiredType(this.requiredType);
        this.compiledNamedTemplate.setContextItemRequirements(this.requiredContextItemType, this.mayOmitContextItem, this.absentFocus);
        this.compiledNamedTemplate.setRetainedStaticContext(rsc);
        this.compiledNamedTemplate.setDeclaredVisibility(this.getDeclaredVisibility());
        Component overridden = this.getOverriddenComponent();
        if (overridden != null) {
            this.checkCompatibility(overridden);
        }
        ContextItemStaticInfo cisi = this.getConfiguration().makeContextItemStaticInfo(this.requiredContextItemType, this.mayOmitContextItem);
        Expression body2 = this.refineTemplateBody(body, cisi);
        this.compiledNamedTemplate.setBody(body2);
        if (this.getCompilation().getCompilerInfo().getCodeInjector() != null) {
            this.getCompilation().getCompilerInfo().getCodeInjector().process(this.compiledNamedTemplate);
        }
    }

    private Expression refineTemplateBody(Expression body, ContextItemStaticInfo cisi) {
        Expression old = body;
        try {
            body = body.simplify();
        } catch (XPathException e) {
            if (e.isReportableStatically()) {
                this.compileError(e);
            }
            body = new ErrorExpression(new XmlProcessingException(e));
            ExpressionTool.copyLocationInfo(old, body);
        }
        Configuration config = this.getConfiguration();
        if (this.visibility != Visibility.ABSTRACT) {
            try {
                if (this.requiredType != null && this.requiredType != SequenceType.ANY_SEQUENCE) {
                    RoleDiagnostic role = new RoleDiagnostic(7, this.diagnosticId, 0);
                    role.setErrorCode("XTTE0505");
                    body = config.getTypeChecker(false).staticTypeCheck(body, this.requiredType, role, this.makeExpressionVisitor());
                }
            } catch (XPathException err) {
                if (err.isReportableStatically()) {
                    this.compileError(err);
                }
                body = new ErrorExpression(new XmlProcessingException(err));
                ExpressionTool.copyLocationInfo(old, body);
            }
        }
        try {
            ExpressionVisitor visitor = this.makeExpressionVisitor();
            body = body.typeCheck(visitor, cisi);
        } catch (XPathException e) {
            this.compileError(e);
        }
        return body;
    }

    public void compileTemplateRule(Compilation compilation, Expression body, ComponentDeclaration decl) {
        ItemType contextItemType;
        Configuration config = this.getConfiguration();
        if (this.getTemplateName() != null) {
            body = body.copy(new RebindingMap());
        }
        if ((contextItemType = this.match.getItemType()).equals(ErrorType.getInstance())) {
            contextItemType = AnyItemType.getInstance();
        }
        ContextItemStaticInfo cisi = config.makeContextItemStaticInfo(contextItemType, false);
        body = this.refineTemplateBody(body, cisi);
        boolean needToCopy = false;
        for (TemplateRule rule : this.compiledTemplateRules.values()) {
            if (needToCopy) {
                body = body.copy(new RebindingMap());
            }
            this.setCompiledTemplateRuleProperties(rule, body);
            needToCopy = true;
            rule.updateSlaveCopies();
            if (compilation.getCompilerInfo().getCodeInjector() == null) continue;
            compilation.getCompilerInfo().getCodeInjector().process(rule);
        }
    }

    private void createSkeletonTemplate(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        StructuredQName[] modes = this.modeNames;
        if (this.isOmniMode()) {
            ArrayList<StructuredQName> all = new ArrayList<StructuredQName>();
            all.add(Mode.UNNAMED_MODE_NAME);
            RuleManager mgr = this.getCompilation().getPrincipalStylesheetModule().getRuleManager();
            for (Mode m : mgr.getAllNamedModes()) {
                all.add(m.getModeName());
            }
            modes = all.toArray(new StructuredQName[0]);
        }
        for (StructuredQName modeName : modes) {
            TemplateRule templateRule = this.compiledTemplateRules.get(modeName);
            if (templateRule == null) {
                templateRule = this.getConfiguration().makeTemplateRule();
            }
            templateRule.prepareInitializer(compilation, decl, modeName);
            this.compiledTemplateRules.put(modeName, templateRule);
            RetainedStaticContext rsc = this.makeRetainedStaticContext();
            templateRule.setPackageData(rsc.getPackageData());
            this.setCompiledTemplateRuleProperties(templateRule, null);
        }
    }

    private void setCompiledTemplateRuleProperties(TemplateRule templateRule, Expression body) {
        templateRule.setMatchPattern(this.match);
        templateRule.setBody(body);
        templateRule.setStackFrameMap(this.stackFrameMap);
        templateRule.setSystemId(this.getSystemId());
        templateRule.setLineNumber(this.getLineNumber());
        templateRule.setHasRequiredParams(this.hasRequiredParams);
        templateRule.setRequiredType(this.requiredType);
        templateRule.setContextItemRequirements(this.requiredContextItemType, this.absentFocus);
    }

    public synchronized void jitCompile(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        if (!this.jitCompilationDone) {
            this.jitCompilationDone = true;
            compilation.setPreScan(false);
            this.processAllAttributes();
            this.checkForJitCompilationErrors(compilation);
            this.validateSubtree(decl, false);
            this.checkForJitCompilationErrors(compilation);
            this.compileDeclaration(compilation, decl);
            this.checkForJitCompilationErrors(compilation);
        }
    }

    private void checkForJitCompilationErrors(Compilation compilation) throws XPathException {
        if (compilation.getErrorCount() > 0) {
            XPathException e = new XPathException("Errors were reported during JIT compilation of template rule with match=\"" + this.matchAtt + "\"", "SXST0001", this);
            e.setHasBeenReported(true);
            throw e;
        }
    }

    public void register(ComponentDeclaration declaration) throws XPathException {
        if (this.match != null) {
            StylesheetModule module = declaration.getModule();
            RuleManager mgr = this.getCompilation().getPrincipalStylesheetModule().getRuleManager();
            ExpressionVisitor visitor = ExpressionVisitor.make(this.getStaticContext());
            for (StructuredQName modeName : this.getModeNames()) {
                TemplateRule rule;
                Mode mode = mgr.obtainMode(modeName, false);
                if (mode == null) {
                    if (mgr.existsOmniMode()) {
                        Mode omniMode = mgr.obtainMode(Mode.OMNI_MODE, true);
                        mode = mgr.obtainMode(modeName, true);
                        SimpleMode.copyRules(omniMode.getActivePart(), mode.getActivePart());
                    } else {
                        mode = mgr.obtainMode(modeName, true);
                    }
                } else {
                    boolean ok = this.getPrincipalStylesheetModule().checkAcceptableModeForPackage(this, mode);
                    if (!ok) {
                        return;
                    }
                }
                Pattern match1 = this.match.copy(new RebindingMap());
                String typed = mode.getActivePart().getPropertyValue("typed");
                if ("strict".equals(typed) || "lax".equals(typed)) {
                    Pattern match2;
                    try {
                        match2 = match1.convertToTypedPattern(typed);
                    } catch (XPathException e) {
                        e.maybeSetLocation(this);
                        throw e;
                    }
                    if (match2 != match1) {
                        ContextItemStaticInfo info = this.getConfiguration().makeContextItemStaticInfo(AnyItemType.getInstance(), false);
                        ExpressionTool.copyLocationInfo(this.match, match2);
                        match2.setOriginalText(this.match.toString());
                        match1 = match2 = match2.typeCheck(visitor, info);
                    }
                    if (this.modeNames.length == 1) {
                        this.match = match2;
                    }
                }
                if ((rule = this.compiledTemplateRules.get(modeName)) == null) {
                    rule = this.getConfiguration().makeTemplateRule();
                    this.compiledTemplateRules.put(modeName, rule);
                }
                double prio = this.prioritySpecified ? this.priority : Double.NaN;
                mgr.registerRule(match1, rule, mode, module, prio, mgr.allocateSequenceNumber(), 0);
                if (mode.isDeclaredStreamable()) {
                    rule.setDeclaredStreamable(true);
                    if (!match1.isMotionless()) {
                        boolean fallback = this.getConfiguration().getBooleanProperty(Feature.STREAMING_FALLBACK);
                        String message = "Template rule is declared streamable but the match pattern is not motionless";
                        if (fallback) {
                            message = message + "\n  * Falling back to non-streaming implementation";
                            this.getStaticContext().issueWarning(message, this);
                            rule.setDeclaredStreamable(false);
                            this.getCompilation().setFallbackToNonStreaming(true);
                        } else {
                            throw new XPathException(message, "XTSE3430", this);
                        }
                    }
                }
                if (mode.getDefaultResultType() != null && !this.declaresRequiredType) {
                    rule.setRequiredType(mode.getDefaultResultType());
                }
                if (!mode.getModeName().equals(Mode.OMNI_MODE)) continue;
                this.compiledTemplateRules.put(Mode.UNNAMED_MODE_NAME, rule);
                mgr.registerRule(match1, rule, mgr.getUnnamedMode(), module, prio, mgr.allocateSequenceNumber(), 0);
                for (Mode m : mgr.getAllNamedModes()) {
                    if (!(m instanceof SimpleMode)) continue;
                    TemplateRule ruleCopy = rule.copy();
                    if (m.isDeclaredStreamable()) {
                        ruleCopy.setDeclaredStreamable(true);
                    }
                    this.compiledTemplateRules.put(m.getModeName(), ruleCopy);
                    mgr.registerRule(match1.copy(new RebindingMap()), ruleCopy, m, module, prio, mgr.allocateSequenceNumber(), 0);
                }
            }
        }
    }

    public void allocatePatternSlotNumbers() {
        if (this.match != null) {
            for (TemplateRule templateRule : this.compiledTemplateRules.values()) {
                for (Rule r : templateRule.getRules()) {
                    int slots;
                    Pattern match = r.getPattern();
                    int nextFree = 0;
                    if ((match.getDependencies() & 1) != 0) {
                        nextFree = 1;
                    }
                    if ((slots = match.allocateSlots(this.getSlotManager(), nextFree)) == 0 && (match.getDependencies() & 0x100) != 0) {
                        slots = 1;
                    }
                    if (slots <= 0) continue;
                    RuleManager mgr = this.getCompilation().getPrincipalStylesheetModule().getRuleManager();
                    boolean appliesToAll = false;
                    for (StructuredQName nc : this.modeNames) {
                        if (nc.equals(Mode.OMNI_MODE)) {
                            appliesToAll = true;
                            break;
                        }
                        Mode mode = mgr.obtainMode(nc, true);
                        mode.getActivePart().allocatePatternSlots(slots);
                    }
                    if (!appliesToAll) continue;
                    for (Mode m : mgr.getAllNamedModes()) {
                        m.getActivePart().allocatePatternSlots(slots);
                    }
                    mgr.getUnnamedMode().getActivePart().allocatePatternSlots(slots);
                }
            }
        }
    }

    @Override
    public void optimize(ComponentDeclaration declaration) throws XPathException {
        ExpressionVisitor visitor;
        ContextItemStaticInfo cisi;
        Configuration config = this.getConfiguration();
        if (this.compiledNamedTemplate != null) {
            Expression body = this.compiledNamedTemplate.getBody();
            cisi = this.getConfiguration().makeContextItemStaticInfo(this.requiredContextItemType, this.mayOmitContextItem);
            visitor = this.makeExpressionVisitor();
            body = body.typeCheck(visitor, cisi);
            body = ExpressionTool.optimizeComponentBody(body, this.getCompilation(), visitor, cisi, true);
            this.compiledNamedTemplate.setBody(body);
            this.allocateLocalSlots(body);
            if (this.explaining) {
                Logger err = this.getConfiguration().getLogger();
                err.info("Optimized expression tree for named template at line " + this.getLineNumber() + " in " + this.getSystemId() + ':');
                body.explain(err);
            }
            body.restoreParentPointers();
            if (config.isDeferredByteCode(HostLanguage.XSLT) && !this.isTailRecursive) {
                Optimizer opt = config.obtainOptimizer();
                int evaluationModes = 6;
                this.compiledNamedTemplate.setBody(opt.makeByteCodeCandidate(this.compiledNamedTemplate, body, this.diagnosticId, evaluationModes));
            }
        }
        if (this.match != null) {
            ItemType contextItemType = this.getContextItemTypeForTemplateRule();
            cisi = config.makeContextItemStaticInfo(contextItemType, false);
            cisi.setContextPostureStriding();
            visitor = this.makeExpressionVisitor();
            this.match.resetLocalStaticProperties();
            this.match = this.match.optimize(visitor, cisi);
            if (!this.isDeferredCompilation(this.getCompilation())) {
                Expression body = this.compiledTemplateRules.values().stream().findFirst().map(TemplateRule::getBody).orElse(null);
                ExpressionTool.resetPropertiesWithinSubtree(body);
                Optimizer opt = this.getConfiguration().obtainOptimizer();
                try {
                    for (TemplateRule compiledTemplateRule : this.compiledTemplateRules.values()) {
                        Expression templateRuleBody = this.compiledTemplateRules.size() > 1 ? body.copy(new RebindingMap()) : body;
                        visitor.setOptimizeForStreaming(compiledTemplateRule.isDeclaredStreamable());
                        templateRuleBody = templateRuleBody.typeCheck(visitor, cisi);
                        templateRuleBody = ExpressionTool.optimizeComponentBody(templateRuleBody, this.getCompilation(), visitor, cisi, true);
                        compiledTemplateRule.setBody(templateRuleBody);
                        opt.checkStreamability(this, compiledTemplateRule);
                        this.allocateLocalSlots(templateRuleBody);
                        for (Rule r : compiledTemplateRule.getRules()) {
                            Pattern match = r.getPattern();
                            ContextItemStaticInfo info = this.getConfiguration().makeContextItemStaticInfo(match.getItemType(), false);
                            info.setContextPostureStriding();
                            Pattern m2 = match.optimize(visitor, info);
                            if (this.compiledTemplateRules.size() > 1) {
                                m2 = m2.copy(new RebindingMap());
                            }
                            if (m2 == match) continue;
                            r.setPattern(m2);
                        }
                        if (visitor.getConfiguration().isDeferredByteCode(HostLanguage.XSLT) && !this.isTailRecursive) {
                            int evaluationModes = 6;
                            compiledTemplateRule.setBody(opt.makeByteCodeCandidate(compiledTemplateRule, templateRuleBody, this.diagnosticId, evaluationModes));
                        }
                        if (!this.explaining) continue;
                        Logger err = this.getConfiguration().getLogger();
                        err.info("Optimized expression tree for template rule at line " + this.getLineNumber() + " in " + this.getSystemId() + ':');
                        templateRuleBody.explain(err);
                    }
                } catch (XPathException e) {
                    e.maybeSetLocation(this);
                    this.compileError(e);
                }
            }
        }
    }

    public ItemType getContextItemTypeForTemplateRule() throws XPathException {
        Configuration config = this.getConfiguration();
        ItemType contextItemType = this.match.getItemType();
        if (contextItemType.equals(ErrorType.getInstance())) {
            contextItemType = AnyItemType.getInstance();
        }
        if (this.requiredContextItemType != AnyItemType.getInstance()) {
            Affinity rel = config.getTypeHierarchy().relationship(contextItemType, this.requiredContextItemType);
            switch (rel) {
                case DISJOINT: {
                    XPathException e = new XPathException("The declared context item type is inconsistent with the match pattern", "XPTY0004", this);
                    e.setIsTypeError(true);
                    throw e;
                }
                case SUBSUMED_BY: 
                case OVERLAPS: 
                case SAME_TYPE: {
                    break;
                }
                case SUBSUMES: {
                    contextItemType = this.requiredContextItemType;
                }
            }
        }
        return contextItemType;
    }

    @Override
    public void generateByteCode(Optimizer opt) throws XPathException {
        if (this.getCompilation().getCompilerInfo().isGenerateByteCode() && !this.isTailRecursive) {
            ICompilerService compilerService = this.getConfiguration().makeCompilerService(HostLanguage.XSLT);
            if (this.getTemplateName() != null) {
                try {
                    Expression exp = this.compiledNamedTemplate.getBody();
                    Expression cbody = opt.compileToByteCode(compilerService, exp, this.nameAtt, 4);
                    if (cbody != null) {
                        this.compiledNamedTemplate.setBody(cbody);
                    }
                } catch (Exception e) {
                    System.err.println("Failed while compiling named template " + this.nameAtt);
                    e.printStackTrace();
                    throw new XPathException(e);
                }
            }
            for (TemplateRule compiledTemplateRule : this.compiledTemplateRules.values()) {
                if (compiledTemplateRule.isDeclaredStreamable()) continue;
                try {
                    Expression cbody;
                    Expression exp = compiledTemplateRule.getBody();
                    if (exp == null || (cbody = opt.compileToByteCode(compilerService, exp, this.matchAtt, 4)) == null) continue;
                    compiledTemplateRule.setBody(cbody);
                } catch (Exception e) {
                    System.err.println("Failed while compiling template rule with match = '" + this.matchAtt + "'");
                    e.printStackTrace();
                    throw new XPathException(e);
                }
            }
        }
    }

    @Override
    public SlotManager getSlotManager() {
        return this.stackFrameMap;
    }

    public NamedTemplate getCompiledNamedTemplate() {
        return this.compiledNamedTemplate;
    }

    public Pattern getMatch() {
        return this.match;
    }

    public Map<StructuredQName, TemplateRule> getTemplateRulesByMode() {
        return this.compiledTemplateRules;
    }
}

