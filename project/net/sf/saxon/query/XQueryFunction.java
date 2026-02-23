/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.ErrorExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.TailCallLoop;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.expr.UserFunctionResolvable;
import net.sf.saxon.expr.instruct.ComponentTracer;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.instruct.UserFunctionParameter;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.ICompilerService;
import net.sf.saxon.expr.parser.LoopLifter;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.Annotation;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.query.Declaration;
import net.sf.saxon.query.QueryModule;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.FunctionStreamability;
import net.sf.saxon.trans.GlobalVariableManager;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.tree.jiter.PairIterator;
import net.sf.saxon.value.SequenceType;

public class XQueryFunction
implements Declaration,
Location {
    private StructuredQName functionName;
    private List<UserFunctionParameter> arguments;
    private SequenceType resultType;
    private Expression body = null;
    private List<UserFunctionResolvable> references = new ArrayList<UserFunctionResolvable>(10);
    private Location location;
    private UserFunction compiledFunction = null;
    private boolean memoFunction;
    private NamespaceResolver namespaceResolver;
    private QueryModule staticContext;
    private boolean isUpdating = false;
    private AnnotationList annotations = AnnotationList.EMPTY;

    public XQueryFunction() {
        this.arguments = new ArrayList<UserFunctionParameter>(8);
    }

    public PackageData getPackageData() {
        return this.staticContext.getPackageData();
    }

    public void setFunctionName(StructuredQName name) {
        this.functionName = name;
    }

    public void addArgument(UserFunctionParameter argument) {
        this.arguments.add(argument);
    }

    public void setResultType(SequenceType resultType) {
        this.resultType = resultType;
    }

    public void setBody(Expression body) {
        this.body = body;
    }

    public Expression getBody() {
        return this.body;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public StructuredQName getFunctionName() {
        return this.functionName;
    }

    public String getDisplayName() {
        return this.functionName.getDisplayName();
    }

    public SymbolicName getIdentificationKey() {
        return new SymbolicName.F(this.functionName, this.arguments.size());
    }

    public static SymbolicName getIdentificationKey(StructuredQName qName, int arity) {
        return new SymbolicName.F(qName, arity);
    }

    public SequenceType getResultType() {
        return this.resultType;
    }

    public void setStaticContext(QueryModule env) {
        this.staticContext = env;
    }

    public StaticContext getStaticContext() {
        return this.staticContext;
    }

    public SequenceType[] getArgumentTypes() {
        SequenceType[] types = new SequenceType[this.arguments.size()];
        for (int i = 0; i < this.arguments.size(); ++i) {
            types[i] = this.arguments.get(i).getRequiredType();
        }
        return types;
    }

    public UserFunctionParameter[] getParameterDefinitions() {
        UserFunctionParameter[] params = new UserFunctionParameter[this.arguments.size()];
        return this.arguments.toArray(params);
    }

    public int getNumberOfArguments() {
        return this.arguments.size();
    }

    public void registerReference(UserFunctionResolvable ufc) {
        this.references.add(ufc);
    }

    public void setMemoFunction(boolean isMemoFunction) {
        this.memoFunction = isMemoFunction;
    }

    public boolean isMemoFunction() {
        return this.memoFunction;
    }

    public void setUpdating(boolean isUpdating) {
        this.isUpdating = isUpdating;
    }

    public boolean isUpdating() {
        return this.isUpdating;
    }

    public void setAnnotations(AnnotationList annotations) {
        this.annotations = annotations;
        if (this.compiledFunction != null) {
            this.compiledFunction.setAnnotations(annotations);
        }
        if (annotations.includes(Annotation.UPDATING)) {
            this.setUpdating(true);
        }
    }

    public AnnotationList getAnnotations() {
        return this.annotations;
    }

    public boolean hasAnnotation(StructuredQName name) {
        return this.annotations.includes(name);
    }

    public boolean isPrivate() {
        return this.hasAnnotation(Annotation.PRIVATE);
    }

    public void compile() throws XPathException {
        Configuration config = this.staticContext.getConfiguration();
        try {
            if (this.compiledFunction == null) {
                SlotManager map = config.makeSlotManager();
                UserFunctionParameter[] params = this.getParameterDefinitions();
                for (int i = 0; i < params.length; ++i) {
                    params[i].setSlotNumber(i);
                    map.allocateSlotNumber(params[i].getVariableQName());
                }
                RetainedStaticContext rsc = null;
                try {
                    rsc = this.getStaticContext().makeRetainedStaticContext();
                    this.body.setRetainedStaticContext(rsc);
                    ExpressionVisitor visitor = ExpressionVisitor.make(this.staticContext);
                    this.body = this.body.simplify().typeCheck(visitor, ContextItemStaticInfo.ABSENT);
                    RoleDiagnostic role = new RoleDiagnostic(5, this.functionName.getDisplayName(), 0);
                    this.body = config.getTypeChecker(false).staticTypeCheck(this.body, this.resultType, role, visitor);
                } catch (XPathException e) {
                    e.maybeSetLocation(this);
                    if (e.isReportableStatically()) {
                        throw e;
                    }
                    ErrorExpression newBody = new ErrorExpression(new XmlProcessingException(e));
                    ExpressionTool.copyLocationInfo(this.body, newBody);
                    this.body = newBody;
                }
                this.compiledFunction = config.newUserFunction(this.memoFunction, FunctionStreamability.UNCLASSIFIED);
                this.compiledFunction.setRetainedStaticContext(rsc);
                this.compiledFunction.setPackageData(this.staticContext.getPackageData());
                this.compiledFunction.setBody(this.body);
                this.compiledFunction.setFunctionName(this.functionName);
                this.compiledFunction.setParameterDefinitions(params);
                this.compiledFunction.setResultType(this.getResultType());
                this.compiledFunction.setLineNumber(this.location.getLineNumber());
                this.compiledFunction.setSystemId(this.location.getSystemId());
                this.compiledFunction.setStackFrameMap(map);
                this.compiledFunction.setUpdating(this.isUpdating);
                this.compiledFunction.setAnnotations(this.annotations);
                if (this.staticContext.getUserQueryContext().isCompileWithTracing()) {
                    this.namespaceResolver = this.staticContext.getNamespaceResolver();
                    ComponentTracer trace = new ComponentTracer(this.compiledFunction);
                    trace.setLocation(this.location);
                    this.body = trace;
                }
            }
            this.fixupReferences();
        } catch (XPathException e) {
            e.maybeSetLocation(this);
            throw e;
        }
    }

    public void optimize() throws XPathException {
        int tailCalls;
        Executable exec;
        GlobalVariableManager manager;
        Expression b2;
        this.body.checkForUpdatingSubexpressions();
        if (this.isUpdating) {
            if (ExpressionTool.isNotAllowedInUpdatingContext(this.body)) {
                XPathException err = new XPathException("The body of an updating function must be an updating expression", "XUST0002");
                err.setLocator(this.body.getLocation());
                throw err;
            }
        } else if (this.body.isUpdatingExpression()) {
            XPathException err = new XPathException("The body of a non-updating function must be a non-updating expression", "XUST0001");
            err.setLocator(this.body.getLocation());
            throw err;
        }
        ExpressionVisitor visitor = ExpressionVisitor.make(this.staticContext);
        Configuration config = this.staticContext.getConfiguration();
        Optimizer opt = visitor.obtainOptimizer();
        int arity = this.arguments.size();
        if (opt.isOptionSet(256)) {
            this.body = this.body.optimize(visitor, ContextItemStaticInfo.ABSENT);
        }
        this.body.setParentExpression(null);
        if (opt.isOptionSet(1)) {
            this.body = LoopLifter.process(this.body, visitor, ContextItemStaticInfo.ABSENT);
        }
        if (opt.isOptionSet(2) && (b2 = opt.promoteExpressionsToGlobal(this.body, manager = new GlobalVariableManager(exec = ((QueryModule)this.getStaticContext()).getExecutable()){
            final /* synthetic */ Executable val$exec;
            {
                this.val$exec = executable;
            }

            @Override
            public void addGlobalVariable(GlobalVariable variable) throws XPathException {
                PackageData pd = XQueryFunction.this.staticContext.getPackageData();
                variable.setPackageData(pd);
                SlotManager sm = pd.getGlobalSlotManager();
                int slot = sm.allocateSlotNumber(variable.getVariableQName());
                variable.compile(this.val$exec, slot);
                pd.addGlobalVariable(variable);
            }

            @Override
            public GlobalVariable getEquivalentVariable(Expression select) {
                return null;
            }
        }, visitor)) != null) {
            this.body = this.body.optimize(visitor, ContextItemStaticInfo.ABSENT);
        }
        if (opt.getOptimizerOptions().isSet(16384) && !this.isUpdating && (tailCalls = ExpressionTool.markTailFunctionCalls(this.body, this.functionName, arity)) != 0) {
            this.compiledFunction.setBody(this.body);
            this.compiledFunction.setTailRecursive(tailCalls > 0, tailCalls > 1);
            this.body = new TailCallLoop(this.compiledFunction, this.body);
        }
        this.compiledFunction.setBody(this.body);
        this.compiledFunction.computeEvaluationMode();
        ExpressionTool.allocateSlots(this.body, arity, this.compiledFunction.getStackFrameMap());
        if (config.isGenerateByteCode(HostLanguage.XQUERY)) {
            if (config.getCountDown() == 0) {
                ICompilerService compilerService = config.makeCompilerService(HostLanguage.XQUERY);
                Expression cbody = opt.compileToByteCode(compilerService, this.body, this.getFunctionName().getDisplayName(), 6);
                if (cbody != null) {
                    this.body = cbody;
                }
            } else {
                opt.injectByteCodeCandidates(this.body);
                this.body = opt.makeByteCodeCandidate(this.compiledFunction, this.body, this.getDisplayName(), 6);
            }
            this.compiledFunction.setBody(this.body);
            this.compiledFunction.computeEvaluationMode();
        }
    }

    public void fixupReferences() {
        for (UserFunctionResolvable ufc : this.references) {
            ufc.setFunction(this.compiledFunction);
        }
    }

    public void checkReferences(ExpressionVisitor visitor) throws XPathException {
        for (UserFunctionResolvable ufr : this.references) {
            if (!(ufr instanceof UserFunctionCall)) continue;
            UserFunctionCall ufc = (UserFunctionCall)ufr;
            ufc.checkFunctionCall(this.compiledFunction, visitor);
        }
        this.references = new ArrayList<UserFunctionResolvable>(0);
    }

    public void explain(ExpressionPresenter out) throws XPathException {
        out.startElement("declareFunction");
        out.emitAttribute("name", this.functionName.getDisplayName());
        out.emitAttribute("arity", "" + this.getNumberOfArguments());
        if (this.compiledFunction == null) {
            out.emitAttribute("unreferenced", "true");
        } else {
            if (this.compiledFunction.isMemoFunction()) {
                out.emitAttribute("memo", "true");
            }
            out.emitAttribute("tailRecursive", this.compiledFunction.isTailRecursive() ? "true" : "false");
            this.body.export(out);
        }
        out.endElement();
    }

    public UserFunction getUserFunction() {
        return this.compiledFunction;
    }

    public StructuredQName getObjectName() {
        return this.functionName;
    }

    @Override
    public String getSystemId() {
        return this.location.getSystemId();
    }

    @Override
    public int getLineNumber() {
        return this.location.getLineNumber();
    }

    @Override
    public String getPublicId() {
        return null;
    }

    @Override
    public int getColumnNumber() {
        return -1;
    }

    @Override
    public Location saveLocation() {
        return this;
    }

    public NamespaceResolver getNamespaceResolver() {
        return this.namespaceResolver;
    }

    public Object getProperty(String name) {
        if ("name".equals(name)) {
            return this.functionName.getDisplayName();
        }
        if ("as".equals(name)) {
            return this.resultType.toString();
        }
        return null;
    }

    public Iterator<String> getProperties() {
        return new PairIterator<String>("name", "as");
    }

    public HostLanguage getHostLanguage() {
        return HostLanguage.XQUERY;
    }
}

