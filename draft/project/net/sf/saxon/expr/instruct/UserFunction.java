/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.Objects;
import java.util.function.BiConsumer;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.EvaluationMode;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.PendingUpdateList;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.UserFunctionParameter;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.Evaluator;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trace.TraceableComponent;
import net.sf.saxon.trans.FunctionStreamability;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;

public class UserFunction
extends Actor
implements Function,
ContextOriginator,
TraceableComponent {
    private static final int MAX_INLININGS = 100;
    private StructuredQName functionName;
    private boolean tailCalls = false;
    private boolean tailRecursive = false;
    private UserFunctionParameter[] parameterDefinitions;
    private SequenceType resultType;
    private SequenceType declaredResultType;
    protected Evaluator evaluator = null;
    private boolean isUpdating = false;
    private int inlineable = -1;
    private int inliningCount = 0;
    private boolean overrideExtensionFunction = true;
    private AnnotationList annotations = AnnotationList.EMPTY;
    private FunctionStreamability declaredStreamability = FunctionStreamability.UNCLASSIFIED;
    private Determinism determinism = Determinism.PROACTIVE;
    private int refCount = 0;

    public void setFunctionName(StructuredQName name) {
        this.functionName = name;
    }

    @Override
    public StructuredQName getFunctionName() {
        return this.functionName;
    }

    @Override
    public String getDescription() {
        StructuredQName name = this.getFunctionName();
        if (name.hasURI("http://ns.saxonica.com/anonymous-type")) {
            boolean first = true;
            StringBuilder sb = new StringBuilder("function(");
            for (UserFunctionParameter param : this.getParameterDefinitions()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append("$").append(param.getVariableQName().getDisplayName());
            }
            sb.append("){");
            Expression body = this.getBody();
            if (body == null) {
                sb.append("...");
            } else {
                sb.append(body.toShortString());
            }
            sb.append("}");
            return sb.toString();
        }
        return name.getDisplayName();
    }

    @Override
    public String getTracingTag() {
        return "xsl:function";
    }

    @Override
    public void gatherProperties(BiConsumer<String, Object> consumer) {
        consumer.accept("name", this.getFunctionName());
        consumer.accept("arity", this.getArity());
    }

    @Override
    public StructuredQName getObjectName() {
        return this.functionName;
    }

    @Override
    public SymbolicName.F getSymbolicName() {
        return new SymbolicName.F(this.functionName, this.getArity());
    }

    @Override
    public FunctionItemType getFunctionItemType() {
        SequenceType[] argTypes = new SequenceType[this.parameterDefinitions.length];
        for (int i = 0; i < this.parameterDefinitions.length; ++i) {
            UserFunctionParameter ufp = this.parameterDefinitions[i];
            argTypes[i] = ufp.getRequiredType();
        }
        return new SpecificFunctionType(argTypes, this.resultType, this.annotations);
    }

    @Override
    public OperandRole[] getOperandRoles() {
        OperandRole[] roles = new OperandRole[this.getArity()];
        OperandUsage first = null;
        switch (this.declaredStreamability) {
            case UNCLASSIFIED: {
                SequenceType required = this.getArgumentType(0);
                first = OperandRole.getTypeDeterminedUsage(required.getPrimaryType());
                break;
            }
            case ABSORBING: {
                first = OperandUsage.ABSORPTION;
                break;
            }
            case INSPECTION: {
                first = OperandUsage.INSPECTION;
                break;
            }
            case FILTER: {
                first = OperandUsage.TRANSMISSION;
                break;
            }
            case SHALLOW_DESCENT: {
                first = OperandUsage.TRANSMISSION;
                break;
            }
            case DEEP_DESCENT: {
                first = OperandUsage.TRANSMISSION;
                break;
            }
            case ASCENT: {
                first = OperandUsage.TRANSMISSION;
            }
        }
        roles[0] = new OperandRole(0, first, this.getArgumentType(0));
        for (int i = 1; i < roles.length; ++i) {
            SequenceType required = this.getArgumentType(i);
            roles[i] = new OperandRole(0, OperandRole.getTypeDeterminedUsage(required.getPrimaryType()), required);
        }
        return roles;
    }

    public boolean acceptsNodesWithoutAtomization() {
        for (int i = 0; i < this.getArity(); ++i) {
            ItemType type = this.getArgumentType(i).getPrimaryType();
            if (!(type instanceof NodeTest) && type != AnyItemType.getInstance()) continue;
            return true;
        }
        return false;
    }

    public boolean isOverrideExtensionFunction() {
        return this.overrideExtensionFunction;
    }

    public void setOverrideExtensionFunction(boolean overrideExtensionFunction) {
        this.overrideExtensionFunction = overrideExtensionFunction;
    }

    public void setAnnotations(AnnotationList list) {
        this.annotations = Objects.requireNonNull(list);
    }

    @Override
    public AnnotationList getAnnotations() {
        return this.annotations;
    }

    public void setDeterminism(Determinism determinism) {
        this.determinism = determinism;
    }

    public Determinism getDeterminism() {
        return this.determinism;
    }

    public void computeEvaluationMode() {
        this.evaluator = this.tailRecursive ? ExpressionTool.eagerEvaluator(this.getBody()) : ExpressionTool.lazyEvaluator(this.getBody(), true);
    }

    public Boolean isInlineable() {
        if (this.inlineable != -1) {
            return this.inlineable > 0 && this.inliningCount < 100;
        }
        if (this.body == null) {
            return null;
        }
        if (this.body.hasSpecialProperty(0x2000000) || this.tailCalls) {
            return false;
        }
        Component component = this.getDeclaringComponent();
        if (component != null) {
            Visibility visibility = this.getDeclaringComponent().getVisibility();
            if (visibility == Visibility.PRIVATE || visibility == Visibility.FINAL) {
                if (this.inlineable < 0) {
                    return null;
                }
                return this.inlineable > 0;
            }
            return false;
        }
        return null;
    }

    public void setInlineable(boolean inlineable) {
        this.inlineable = inlineable ? 1 : 0;
    }

    public void markAsInlined() {
        ++this.inliningCount;
    }

    public void setParameterDefinitions(UserFunctionParameter[] params) {
        this.parameterDefinitions = params;
    }

    public UserFunctionParameter[] getParameterDefinitions() {
        return this.parameterDefinitions;
    }

    public void setResultType(SequenceType resultType) {
        this.declaredResultType = resultType;
        this.resultType = resultType;
    }

    public void setTailRecursive(boolean tailCalls, boolean recursiveTailCalls) {
        this.tailCalls = tailCalls;
        this.tailRecursive = recursiveTailCalls;
    }

    public boolean containsTailCalls() {
        return this.tailCalls;
    }

    public boolean isTailRecursive() {
        return this.tailRecursive;
    }

    public void setUpdating(boolean isUpdating) {
        this.isUpdating = isUpdating;
    }

    public boolean isUpdating() {
        return this.isUpdating;
    }

    public void setDeclaredStreamability(FunctionStreamability streamability) {
        this.declaredStreamability = streamability == null ? FunctionStreamability.UNCLASSIFIED : streamability;
    }

    public FunctionStreamability getDeclaredStreamability() {
        return this.declaredStreamability;
    }

    public SequenceType getResultType() {
        if (this.resultType == SequenceType.ANY_SEQUENCE && this.getBody() != null && !UserFunction.containsUserFunctionCalls(this.getBody())) {
            this.resultType = SequenceType.makeSequenceType(this.getBody().getItemType(), this.getBody().getCardinality());
        }
        return this.resultType;
    }

    public SequenceType getDeclaredResultType() {
        return this.declaredResultType;
    }

    private static boolean containsUserFunctionCalls(Expression exp) {
        if (exp instanceof UserFunctionCall) {
            return true;
        }
        for (Operand o : exp.operands()) {
            if (!UserFunction.containsUserFunctionCalls(o.getChildExpression())) continue;
            return true;
        }
        return false;
    }

    public SequenceType getArgumentType(int n) {
        return this.parameterDefinitions[n].getRequiredType();
    }

    public Evaluator getEvaluator() {
        if (this.evaluator == null) {
            this.computeEvaluationMode();
        }
        return this.evaluator;
    }

    public void setEvaluationMode(EvaluationMode mode) {
        this.evaluator = mode.getEvaluator();
    }

    @Override
    public int getArity() {
        return this.parameterDefinitions.length;
    }

    public boolean isMemoFunction() {
        return false;
    }

    public void typeCheck(ExpressionVisitor visitor) throws XPathException {
        Expression exp = this.getBody();
        if (exp instanceof ValueOf && ((ValueOf)exp).getSelect().getItemType().isAtomicType() && this.declaredResultType.getPrimaryType().isAtomicType() && this.declaredResultType.getPrimaryType() != BuiltInAtomicType.STRING) {
            visitor.getStaticContext().issueWarning("A function that computes atomic values should use xsl:sequence rather than xsl:value-of", this.getLocation());
        }
        ExpressionTool.resetPropertiesWithinSubtree(exp);
        Expression exp2 = exp;
        try {
            ContextItemStaticInfo info = ContextItemStaticInfo.ABSENT;
            exp2 = exp.typeCheck(visitor, info);
            if (this.resultType != null) {
                RoleDiagnostic role = new RoleDiagnostic(5, this.functionName == null ? "" : this.functionName.getDisplayName() + "#" + this.getArity(), 0);
                role.setErrorCode(this.getPackageData().isXSLT() && this.getFunctionName() != null ? "XTTE0780" : "XPTY0004");
                exp2 = visitor.getConfiguration().getTypeChecker(false).staticTypeCheck(exp2, this.resultType, role, visitor);
            }
        } catch (XPathException err) {
            err.maybeSetLocation(this.getLocation());
            throw err;
        }
        if (exp2 != exp) {
            this.setBody(exp2);
        }
    }

    @Override
    public XPathContextMajor makeNewContext(XPathContext oldContext, ContextOriginator originator) {
        XPathContextMajor c2 = oldContext.newCleanContext();
        c2.setTemporaryOutputState(158);
        c2.setCurrentOutputUri(null);
        c2.setCurrentComponent(this.getDeclaringComponent());
        c2.setOrigin(originator);
        return c2;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] actualArgs) throws XPathException {
        Sequence result;
        if (this.evaluator == null) {
            this.computeEvaluationMode();
        }
        XPathContextMajor c2 = (XPathContextMajor)context;
        c2.setStackFrame(this.getStackFrameMap(), actualArgs);
        try {
            result = this.evaluator.evaluate(this.getBody(), c2);
        } catch (XPathException err) {
            err.maybeSetLocation(this.getLocation());
            err.maybeSetContext(c2);
            throw err;
        } catch (Exception err2) {
            String message = "Internal error evaluating function " + (this.functionName == null ? "(unnamed)" : this.functionName.getDisplayName()) + (this.getLineNumber() > 0 ? " at line " + this.getLineNumber() : "") + (this.getSystemId() != null ? " in module " + this.getSystemId() : "");
            throw new RuntimeException(message, err2);
        }
        return result;
    }

    public void process(XPathContextMajor context, Sequence[] actualArgs, Outputter output) throws XPathException {
        context.setStackFrame(this.getStackFrameMap(), actualArgs);
        this.getBody().process(output, context);
    }

    public Sequence call(Sequence[] actualArgs, Controller controller) throws XPathException {
        return this.call(controller.newXPathContext(), actualArgs);
    }

    public void callUpdating(Sequence[] actualArgs, XPathContextMajor context, PendingUpdateList pul) throws XPathException {
        context.setStackFrame(this.getStackFrameMap(), actualArgs);
        try {
            this.getBody().evaluatePendingUpdates(context, pul);
        } catch (XPathException err) {
            err.maybeSetLocation(this.getLocation());
            throw err;
        }
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("function");
        if (this.getFunctionName() != null) {
            presenter.emitAttribute("name", this.getFunctionName());
            presenter.emitAttribute("line", this.getLineNumber() + "");
            presenter.emitAttribute("module", this.getSystemId());
            presenter.emitAttribute("eval", this.getEvaluator().getEvaluationMode().getCode() + "");
        }
        String flags = "";
        flags = this.determinism == Determinism.PROACTIVE ? flags + "p" : (this.determinism == Determinism.ELIDABLE ? flags + "e" : flags + "d");
        if (this.isMemoFunction()) {
            flags = flags + "m";
        }
        switch (this.declaredStreamability) {
            case UNCLASSIFIED: {
                flags = flags + "U";
                break;
            }
            case ABSORBING: {
                flags = flags + "A";
                break;
            }
            case INSPECTION: {
                flags = flags + "I";
                break;
            }
            case FILTER: {
                flags = flags + "F";
                break;
            }
            case SHALLOW_DESCENT: {
                flags = flags + "S";
                break;
            }
            case DEEP_DESCENT: {
                flags = flags + "D";
                break;
            }
            case ASCENT: {
                flags = flags + "C";
            }
        }
        presenter.emitAttribute("flags", flags);
        presenter.emitAttribute("as", this.getDeclaredResultType().toAlphaCode());
        presenter.emitAttribute("slots", this.getStackFrameMap().getNumberOfVariables() + "");
        for (UserFunctionParameter p : this.parameterDefinitions) {
            presenter.startElement("arg");
            presenter.emitAttribute("name", p.getVariableQName());
            presenter.emitAttribute("as", p.getRequiredType().toAlphaCode());
            presenter.endElement();
        }
        presenter.setChildRole("body");
        this.getBody().export(presenter);
        presenter.endElement();
    }

    @Override
    public boolean isExportable() {
        return this.refCount > 0 || this.getDeclaredVisibility() != null && this.getDeclaredVisibility() != Visibility.PRIVATE || ((StylesheetPackage)this.getPackageData()).isRetainUnusedFunctions();
    }

    @Override
    public boolean isTrustedResultType() {
        return false;
    }

    @Override
    public boolean isMap() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean deepEquals(Function other, XPathContext context, AtomicComparer comparer, int flags) throws XPathException {
        XPathException err = new XPathException("Cannot compare functions using deep-equal", "FOTY0015");
        err.setIsTypeError(true);
        err.setXPathContext(context);
        throw err;
    }

    @Override
    public Function itemAt(int n) {
        return n == 0 ? this : null;
    }

    @Override
    public GroundedValue subsequence(int start, int length) {
        return start <= 0 && start + length > 0 ? this : EmptySequence.getInstance();
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public boolean effectiveBooleanValue() throws XPathException {
        return ExpressionTool.effectiveBooleanValue(this);
    }

    @Override
    public UserFunction reduce() {
        return this;
    }

    @Override
    public UserFunction head() {
        return this;
    }

    @Override
    public String getStringValue() {
        throw new UnsupportedOperationException("A function has no string value");
    }

    @Override
    public CharSequence getStringValueCS() {
        return this.getStringValue();
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        throw new XPathException("Functions cannot be atomized", "FOTY0013");
    }

    public void incrementReferenceCount() {
        ++this.refCount;
    }

    public int getReferenceCount() {
        return this.refCount;
    }

    public void prepareForStreaming() throws XPathException {
    }

    public static enum Determinism {
        DETERMINISTIC,
        PROACTIVE,
        ELIDABLE;

    }
}

