/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import java.util.Iterator;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.TailCallLoop;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.instruct.UserFunctionParameter;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.ICompilerService;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.OptimizerOptions;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.Annotation;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetComponent;
import net.sf.saxon.style.XSLLocalParam;
import net.sf.saxon.trans.FunctionStreamability;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Whitespace;

public class XSLFunction
extends StyleElement
implements StylesheetComponent {
    private boolean doneAttributes = false;
    private String nameAtt = null;
    private String asAtt = null;
    private String extraAsAtt = null;
    private SequenceType resultType = SequenceType.ANY_SEQUENCE;
    private SlotManager stackFrameMap;
    private boolean memoFunction = false;
    private String overrideExtensionFunctionAtt = null;
    private boolean overrideExtensionFunction = true;
    private int numberOfArguments = -1;
    private UserFunction compiledFunction;
    private Visibility visibility;
    private FunctionStreamability streamability;
    private UserFunction.Determinism determinism = UserFunction.Determinism.PROACTIVE;
    private boolean explaining;

    @Override
    public UserFunction getActor() {
        return this.compiledFunction;
    }

    @Override
    public boolean isDeclaration() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        if (this.doneAttributes) {
            return;
        }
        this.doneAttributes = true;
        AttributeMap atts = this.attributes();
        this.overrideExtensionFunctionAtt = null;
        String visibilityAtt = null;
        String cacheAtt = null;
        String newEachTimeAtt = null;
        String streamabilityAtt = null;
        block24: for (AttributeInfo att : atts) {
            NodeName name = att.getNodeName();
            String uri = name.getURI();
            String local = name.getLocalPart();
            if ("".equals(uri)) {
                switch (local) {
                    case "name": {
                        this.nameAtt = Whitespace.trim(att.getValue());
                        assert (this.nameAtt != null);
                        StructuredQName functionName = this.makeQName(this.nameAtt, null, "name");
                        if (functionName.hasURI("")) {
                            functionName = new StructuredQName("saxon", "http://saxon.sf.net/", functionName.getLocalPart());
                            this.compileError("Function name must be in a namespace", "XTSE0740");
                        }
                        this.setObjectName(functionName);
                        break;
                    }
                    case "as": {
                        this.asAtt = att.getValue();
                        break;
                    }
                    case "visibility": {
                        visibilityAtt = Whitespace.trim(att.getValue());
                        break;
                    }
                    case "streamability": {
                        streamabilityAtt = Whitespace.trim(att.getValue());
                        break;
                    }
                    case "override": {
                        String overrideAtt = Whitespace.trim(att.getValue());
                        boolean override = this.processBooleanAttribute("override", overrideAtt);
                        if (this.overrideExtensionFunctionAtt != null) {
                            if (override != this.overrideExtensionFunction) {
                                this.compileError("Attributes override-extension-function and override are both used, but do not match", "XTSE0020");
                            }
                        } else {
                            this.overrideExtensionFunctionAtt = overrideAtt;
                            this.overrideExtensionFunction = override;
                        }
                        this.compileWarning("The xsl:function/@override attribute is deprecated; use override-extension-function", "SXWN9014");
                        break;
                    }
                    case "override-extension-function": {
                        String overrideExtAtt = Whitespace.trim(att.getValue());
                        boolean overrideExt = this.processBooleanAttribute("override-extension-function", overrideExtAtt);
                        if (this.overrideExtensionFunctionAtt != null) {
                            if (overrideExt != this.overrideExtensionFunction) {
                                this.compileError("Attributes override-extension-function and override are both used, but do not match", "XTSE0020");
                            }
                        } else {
                            this.overrideExtensionFunctionAtt = overrideExtAtt;
                            this.overrideExtensionFunction = overrideExt;
                        }
                        if (!local.equals("override")) continue block24;
                        this.compileWarning("The xsl:function/@override attribute is deprecated; use override-extension-function", "SXWN9014");
                        break;
                    }
                    case "cache": {
                        cacheAtt = Whitespace.trim(att.getValue());
                        break;
                    }
                    case "new-each-time": {
                        newEachTimeAtt = Whitespace.trim(att.getValue());
                        break;
                    }
                    default: {
                        this.checkUnknownAttribute(name);
                        break;
                    }
                }
                continue;
            }
            if (uri.equals("http://saxon.sf.net/")) {
                if (!this.isExtensionAttributeAllowed(att.getNodeName().getDisplayName())) continue;
                if (local.equals("memo-function")) {
                    this.compileWarning("saxon:memo-function is deprecated: use cache='yes'", "SXWN9014");
                    if (!this.getConfiguration().isLicensedFeature(8)) continue;
                    this.memoFunction = this.processBooleanAttribute("saxon:memo-function", att.getValue());
                    continue;
                }
                if (local.equals("as")) {
                    this.isExtensionAttributeAllowed(name.getDisplayName());
                    this.extraAsAtt = att.getValue();
                    continue;
                }
                if (!local.equals("explain") || !XSLFunction.isYes(Whitespace.trim(att.getValue()))) continue;
                this.explaining = true;
                continue;
            }
            this.checkUnknownAttribute(name);
        }
        if (this.nameAtt == null) {
            this.reportAbsence("name");
            this.nameAtt = "xsl:unnamed-function-" + this.generateId();
        }
        if (this.asAtt != null) {
            try {
                this.resultType = this.makeSequenceType(this.asAtt);
            } catch (XPathException e) {
                this.compileErrorInAttribute(e.getMessage(), e.getErrorCodeLocalPart(), "as");
            }
        }
        if (this.extraAsAtt != null) {
            SequenceType extraResultType = null;
            try {
                extraResultType = this.makeExtendedSequenceType(this.extraAsAtt);
            } catch (XPathException e) {
                this.compileErrorInAttribute(e.getMessage(), e.getErrorCodeLocalPart(), "saxon:as");
                extraResultType = this.resultType;
            }
            if (this.asAtt != null) {
                Affinity rel = this.getConfiguration().getTypeHierarchy().sequenceTypeRelationship(extraResultType, this.resultType);
                if (rel == Affinity.SAME_TYPE || rel == Affinity.SUBSUMED_BY) {
                    this.resultType = extraResultType;
                } else {
                    this.compileErrorInAttribute("When both are present, @saxon:as must be a subtype of @as", "SXER7TBA", "saxon:as");
                }
            } else {
                this.resultType = extraResultType;
            }
        }
        this.visibility = visibilityAtt == null ? Visibility.PRIVATE : this.interpretVisibilityValue(visibilityAtt, "");
        if (streamabilityAtt == null) {
            this.streamability = FunctionStreamability.UNCLASSIFIED;
        } else {
            boolean streamable;
            this.streamability = this.getStreamabilityValue(streamabilityAtt);
            if (this.streamability.isStreaming() && !(streamable = this.processStreamableAtt("yes"))) {
                this.streamability = FunctionStreamability.UNCLASSIFIED;
            }
        }
        if (newEachTimeAtt != null) {
            boolean b;
            this.determinism = "maybe".equals(newEachTimeAtt) ? UserFunction.Determinism.ELIDABLE : ((b = this.processBooleanAttribute("new-each-time", newEachTimeAtt)) ? UserFunction.Determinism.PROACTIVE : UserFunction.Determinism.DETERMINISTIC);
        }
        boolean cache = false;
        if (cacheAtt != null) {
            cache = this.processBooleanAttribute("cache", cacheAtt);
        }
        if (this.determinism == UserFunction.Determinism.DETERMINISTIC || cache) {
            this.memoFunction = true;
        }
    }

    private FunctionStreamability getStreamabilityValue(String s) {
        if (s.contains(":")) {
            this.makeQName(s, null, "streamability");
            return FunctionStreamability.UNCLASSIFIED;
        }
        for (FunctionStreamability v : FunctionStreamability.values()) {
            if (!v.streamabilityStr.equals(s)) continue;
            return v;
        }
        this.invalidAttribute("streamability", "unclassified|absorbing|inspection|filter|shallow-descent|deep-descent|ascent");
        return null;
    }

    @Override
    public StructuredQName getObjectName() {
        StructuredQName qn = super.getObjectName();
        if (qn == null) {
            this.nameAtt = Whitespace.trim(this.getAttributeValue("", "name"));
            if (this.nameAtt == null) {
                return new StructuredQName("saxon", "http://saxon.sf.net/", "badly-named-function" + this.generateId());
            }
            qn = this.makeQName(this.nameAtt, null, "name");
            this.setObjectName(qn);
        }
        return qn;
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
    protected boolean isPermittedChild(StyleElement child) {
        return child instanceof XSLLocalParam;
    }

    @Override
    public Visibility getVisibility() {
        if (this.visibility == null) {
            String vAtt = this.getAttributeValue("", "visibility");
            return vAtt == null ? Visibility.PRIVATE : this.interpretVisibilityValue(Whitespace.trim(vAtt), "");
        }
        return this.visibility;
    }

    @Override
    public SymbolicName.F getSymbolicName() {
        return new SymbolicName.F(this.getObjectName(), this.getNumberOfArguments());
    }

    @Override
    public void checkCompatibility(Component component) {
        if (this.compiledFunction == null) {
            this.getCompiledFunction();
        }
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        UserFunction other = (UserFunction)component.getActor();
        if (!this.compiledFunction.getSymbolicName().equals(other.getSymbolicName())) {
            this.compileError("The overriding xsl:function " + this.nameAtt + " does not match the overridden function: the function name/arity does not match", "XTSE3070");
        }
        if (!this.compiledFunction.getDeclaredResultType().isSameType(other.getDeclaredResultType(), th)) {
            this.compileError("The overriding xsl:function " + this.nameAtt + " does not match the overridden function: the return type does not match", "XTSE3070");
        }
        if (!this.compiledFunction.getDeclaredStreamability().equals((Object)other.getDeclaredStreamability())) {
            this.compileError("The overriding xsl:function " + this.nameAtt + " does not match the overridden function: the streamability category does not match", "XTSE3070");
        }
        if (!this.compiledFunction.getDeterminism().equals((Object)other.getDeterminism())) {
            this.compileError("The overriding xsl:function " + this.nameAtt + " does not match the overridden function: the new-each-time attribute does not match", "XTSE3070");
        }
        for (int i = 0; i < this.getNumberOfArguments(); ++i) {
            if (this.compiledFunction.getArgumentType(i).isSameType(other.getArgumentType(i), th)) continue;
            this.compileError("The overriding xsl:function " + this.nameAtt + " does not match the overridden function: the type of the " + RoleDiagnostic.ordinal(i + 1) + " argument does not match", "XTSE3070");
        }
    }

    public boolean isOverrideExtensionFunction() {
        if (this.overrideExtensionFunctionAtt == null) {
            this.prepareAttributes();
        }
        return this.overrideExtensionFunction;
    }

    @Override
    public void index(ComponentDeclaration decl, PrincipalStylesheetModule top) {
        this.getCompiledFunction();
        top.indexFunction(decl);
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.stackFrameMap = this.getConfiguration().makeSlotManager();
        this.checkTopLevel("XTSE0010", true);
        int arity = this.getNumberOfArguments();
        if (arity == 0 && this.streamability != FunctionStreamability.UNCLASSIFIED) {
            this.compileError("A function with no arguments must have streamability=unclassified", "XTSE3155");
        }
    }

    @Override
    public void compileDeclaration(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        Component overridden;
        Expression exp = this.compileSequenceConstructor(compilation, decl, false);
        if (exp == null) {
            exp = Literal.makeEmptySequence();
        } else if (!Literal.isEmptySequence(exp)) {
            if (this.visibility == Visibility.ABSTRACT) {
                this.compileError("A function defined with visibility='abstract' must have no body");
            }
            exp = exp.simplify();
        }
        UserFunction fn = this.getCompiledFunction();
        fn.setBody(exp);
        fn.setStackFrameMap(this.stackFrameMap);
        this.bindParameterDefinitions(fn);
        fn.setRetainedStaticContext(this.makeRetainedStaticContext());
        fn.setOverrideExtensionFunction(this.overrideExtensionFunction);
        if (compilation.getCompilerInfo().getCodeInjector() != null) {
            compilation.getCompilerInfo().getCodeInjector().process(fn);
        }
        if ((overridden = this.getOverriddenComponent()) != null) {
            this.checkCompatibility(overridden);
        }
    }

    @Override
    public void optimize(ComponentDeclaration declaration) throws XPathException {
        int tailCalls;
        OptimizerOptions options;
        Expression exp = this.compiledFunction.getBody();
        ExpressionTool.resetPropertiesWithinSubtree(exp);
        ExpressionVisitor visitor = this.makeExpressionVisitor();
        Expression exp2 = exp.typeCheck(visitor, ContextItemStaticInfo.ABSENT);
        if (this.streamability.isStreaming()) {
            visitor.setOptimizeForStreaming(true);
        }
        exp2 = ExpressionTool.optimizeComponentBody(exp2, this.getCompilation(), visitor, ContextItemStaticInfo.ABSENT, true);
        exp2 = XSLFunction.makeTraceInstruction(this, exp2);
        this.compiledFunction.setBody(exp2);
        Optimizer optimizer = visitor.getConfiguration().obtainOptimizer();
        if (this.streamability.isStreaming()) {
            optimizer.assessFunctionStreamability(this, this.compiledFunction);
        }
        this.allocateLocalSlots(exp2);
        if (exp2 != exp) {
            this.compiledFunction.setBody(exp2);
        }
        if ((options = this.getCompilation().getCompilerInfo().getOptimizerOptions()).isSet(16384) && !this.streamability.isStreaming() && (tailCalls = ExpressionTool.markTailFunctionCalls(exp2, this.getObjectName(), this.getNumberOfArguments())) != 0) {
            this.compiledFunction.setTailRecursive(tailCalls > 0, tailCalls > 1);
            exp2 = this.compiledFunction.getBody();
            this.compiledFunction.setBody(new TailCallLoop(this.compiledFunction, exp2));
        }
        this.compiledFunction.computeEvaluationMode();
        if (this.streamability.isStreaming()) {
            this.compiledFunction.prepareForStreaming();
        } else if (visitor.getConfiguration().isDeferredByteCode(HostLanguage.XSLT)) {
            int evaluationModes = 6;
            this.compiledFunction.setBody(this.getConfiguration().obtainOptimizer().makeByteCodeCandidate(this.compiledFunction, this.compiledFunction.getBody(), this.nameAtt, evaluationModes));
        }
        if (this.explaining) {
            exp2.explain(this.getConfiguration().getLogger());
        }
    }

    @Override
    public void generateByteCode(Optimizer opt) throws XPathException {
        if (this.getCompilation().getCompilerInfo().isGenerateByteCode() && this.streamability == FunctionStreamability.UNCLASSIFIED) {
            try {
                ICompilerService compilerService = this.getConfiguration().makeCompilerService(HostLanguage.XSLT);
                Expression cbody = opt.compileToByteCode(compilerService, this.compiledFunction.getBody(), this.nameAtt, 6);
                if (cbody != null) {
                    this.compiledFunction.setBody(cbody);
                }
            } catch (Exception e) {
                System.err.println("Failed while compiling function " + this.nameAtt);
                e.printStackTrace();
                throw new XPathException(e);
            }
        }
    }

    @Override
    public SlotManager getSlotManager() {
        return this.stackFrameMap;
    }

    public SequenceType getResultType() {
        String asAtt;
        if (this.resultType == null && (asAtt = this.getAttributeValue("", "as")) != null) {
            try {
                this.resultType = this.makeSequenceType(asAtt);
            } catch (XPathException xPathException) {
                // empty catch block
            }
        }
        return this.resultType == null ? SequenceType.ANY_SEQUENCE : this.resultType;
    }

    public int getNumberOfArguments() {
        if (this.numberOfArguments == -1) {
            this.numberOfArguments = 0;
            for (NodeInfo nodeInfo : this.children()) {
                if (nodeInfo instanceof XSLLocalParam) {
                    ++this.numberOfArguments;
                    continue;
                }
                return this.numberOfArguments;
            }
        }
        return this.numberOfArguments;
    }

    public void setParameterDefinitions(UserFunction fn) {
        NodeInfo node;
        UserFunctionParameter[] params = new UserFunctionParameter[this.getNumberOfArguments()];
        fn.setParameterDefinitions(params);
        int count = 0;
        Iterator<NodeImpl> iterator = this.children().iterator();
        while (iterator.hasNext() && (node = (NodeInfo)iterator.next()) instanceof XSLLocalParam) {
            UserFunctionParameter param;
            params[count] = param = new UserFunctionParameter();
            param.setRequiredType(((XSLLocalParam)node).getRequiredType());
            param.setVariableQName(((XSLLocalParam)node).getVariableQName());
            param.setSlotNumber(((XSLLocalParam)node).getSlotNumber());
            if (count == 0 && this.streamability != FunctionStreamability.UNCLASSIFIED) {
                param.setFunctionStreamability(this.streamability);
            }
            ++count;
        }
    }

    private void bindParameterDefinitions(UserFunction fn) {
        UserFunctionParameter[] params = fn.getParameterDefinitions();
        int count = 0;
        for (NodeInfo nodeInfo : this.children(XSLLocalParam.class::isInstance)) {
            UserFunctionParameter param = params[count++];
            param.setRequiredType(((XSLLocalParam)nodeInfo).getRequiredType());
            param.setVariableQName(((XSLLocalParam)nodeInfo).getVariableQName());
            param.setSlotNumber(((XSLLocalParam)nodeInfo).getSlotNumber());
            ((XSLLocalParam)nodeInfo).getSourceBinding().fixupBinding(param);
        }
    }

    public SequenceType[] getArgumentTypes() {
        SequenceType[] types = new SequenceType[this.getNumberOfArguments()];
        int count = 0;
        for (NodeInfo nodeInfo : this.children(XSLLocalParam.class::isInstance)) {
            types[count++] = ((XSLLocalParam)nodeInfo).getRequiredType();
        }
        return types;
    }

    public UserFunction getCompiledFunction() {
        if (this.compiledFunction == null) {
            this.prepareAttributes();
            UserFunction fn = this.getConfiguration().newUserFunction(this.memoFunction, this.streamability);
            fn.setPackageData(this.getCompilation().getPackageData());
            fn.setFunctionName(this.getObjectName());
            this.setParameterDefinitions(fn);
            fn.setResultType(this.getResultType());
            fn.setLineNumber(this.getLineNumber());
            fn.setSystemId(this.getSystemId());
            fn.obtainDeclaringComponent(this);
            fn.setDeclaredVisibility(this.getDeclaredVisibility());
            fn.setDeclaredStreamability(this.streamability);
            fn.setDeterminism(this.determinism);
            ArrayList<Annotation> annotations = new ArrayList<Annotation>();
            if (this.memoFunction) {
                annotations.add(new Annotation(new StructuredQName("saxon", "http://saxon.sf.net/", "memo-function")));
            }
            fn.setAnnotations(new AnnotationList(annotations));
            fn.setOverrideExtensionFunction(this.overrideExtensionFunction);
            this.compiledFunction = fn;
        }
        return this.compiledFunction;
    }
}

