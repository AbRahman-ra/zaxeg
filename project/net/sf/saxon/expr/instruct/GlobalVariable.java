/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.function.BiConsumer;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.BindingReference;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.Bindery;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.Declaration;
import net.sf.saxon.query.XQueryFunction;
import net.sf.saxon.query.XQueryFunctionLibrary;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trace.TraceableComponent;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public class GlobalVariable
extends Actor
implements Binding,
Declaration,
TraceableComponent,
ContextOriginator {
    protected List<BindingReference> references = new ArrayList<BindingReference>(10);
    private StructuredQName variableQName;
    private SequenceType requiredType;
    private boolean indexed;
    private boolean isPrivate = false;
    private boolean isAssignable = false;
    private GlobalVariable originalVariable;
    private int binderySlotNumber;
    private boolean isRequiredParam;
    private boolean isStatic;

    public void init(Expression select, StructuredQName qName) {
        this.variableQName = qName;
        this.setBody(select);
    }

    @Override
    public SymbolicName getSymbolicName() {
        return new SymbolicName(206, this.variableQName);
    }

    @Override
    public String getTracingTag() {
        return "xsl:variable";
    }

    @Override
    public void gatherProperties(BiConsumer<String, Object> consumer) {
        consumer.accept("name", this.getVariableQName());
    }

    public void setStatic(boolean declaredStatic) {
        this.isStatic = declaredStatic;
    }

    public boolean isStatic() {
        return this.isStatic;
    }

    public void setRequiredType(SequenceType required) {
        this.requiredType = required;
    }

    @Override
    public SequenceType getRequiredType() {
        return this.requiredType;
    }

    private Configuration getConfiguration() {
        return this.getPackageData().getConfiguration();
    }

    public void setOriginalVariable(GlobalVariable var) {
        this.originalVariable = var;
    }

    public GlobalVariable getOriginalVariable() {
        return this.originalVariable;
    }

    public GlobalVariable getUltimateOriginalVariable() {
        if (this.originalVariable == null) {
            return this;
        }
        return this.originalVariable.getUltimateOriginalVariable();
    }

    public void setUnused(boolean unused) {
        this.binderySlotNumber = -9234;
    }

    public boolean isUnused() {
        return this.binderySlotNumber == -9234;
    }

    public boolean isPrivate() {
        return this.isPrivate;
    }

    public void setPrivate(boolean b) {
        this.isPrivate = b;
    }

    public void setAssignable(boolean assignable) {
        this.isAssignable = assignable;
    }

    @Override
    public final boolean isAssignable() {
        return this.isAssignable;
    }

    @Override
    public StructuredQName getObjectName() {
        return this.getVariableQName();
    }

    @Override
    public Object getProperty(String name) {
        return null;
    }

    @Override
    public Iterator<String> getProperties() {
        List list = Collections.emptyList();
        return list.iterator();
    }

    public HostLanguage getHostLanguage() {
        return this.getPackageData().getHostLanguage();
    }

    public void setIndexedVariable() {
        this.indexed = true;
    }

    public boolean isIndexedVariable() {
        return this.indexed;
    }

    public void setContainsLocals(SlotManager map) {
        this.setStackFrameMap(map);
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    public void registerReference(BindingReference ref) {
        this.references.add(ref);
    }

    public Iterator iterateReferences() {
        return this.references.iterator();
    }

    public int getBinderySlotNumber() {
        return this.binderySlotNumber;
    }

    public void setBinderySlotNumber(int s) {
        if (!this.isUnused()) {
            this.binderySlotNumber = s;
        }
    }

    public void setRequiredParam(boolean requiredParam) {
        this.isRequiredParam = requiredParam;
    }

    public boolean isRequiredParam() {
        return this.isRequiredParam;
    }

    public void compile(Executable exec, int slot) throws XPathException {
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        this.setBinderySlotNumber(slot);
        if (this instanceof GlobalParam) {
            this.setRequiredParam(this.getBody() == null);
        }
        SequenceType type = this.getRequiredType();
        for (BindingReference ref : this.references) {
            Affinity relation;
            ref.fixup(this);
            GroundedValue constantValue = null;
            int properties = 0;
            Expression select = this.getBody();
            if (select instanceof Literal && !(this instanceof GlobalParam) && ((relation = th.relationship(select.getItemType(), type.getPrimaryType())) == Affinity.SAME_TYPE || relation == Affinity.SUBSUMED_BY)) {
                constantValue = ((Literal)select).getValue();
                type = SequenceType.makeSequenceType(SequenceTool.getItemType(constantValue, th), SequenceTool.getCardinality(constantValue));
            }
            if (select != null) {
                properties = select.getSpecialProperties();
            }
            ref.setStaticType(type, constantValue, properties |= 0x800000);
        }
        if (this.isRequiredParam()) {
            exec.registerGlobalParameter((GlobalParam)this);
        }
    }

    public void typeCheck(ExpressionVisitor visitor) throws XPathException {
        Expression value = this.getBody();
        if (value != null) {
            value.checkForUpdatingSubexpressions();
            if (value.isUpdatingExpression()) {
                throw new XPathException("Initializing expression for global variable must not be an updating expression", "XUST0001");
            }
            RoleDiagnostic role = new RoleDiagnostic(3, this.getVariableQName().getDisplayName(), 0);
            ContextItemStaticInfo cit = this.getConfiguration().makeContextItemStaticInfo(AnyItemType.getInstance(), true);
            Expression value2 = TypeChecker.strictTypeCheck(value.simplify().typeCheck(visitor, cit), this.getRequiredType(), role, visitor.getStaticContext());
            value2 = value2.optimize(visitor, cit);
            this.setBody(value2);
            SlotManager map = this.getConfiguration().makeSlotManager();
            int slots = ExpressionTool.allocateSlots(value2, 0, map);
            if (slots > 0) {
                this.setContainsLocals(map);
            }
            if (this.getRequiredType() == SequenceType.ANY_SEQUENCE && !(this instanceof GlobalParam)) {
                try {
                    ItemType itemType = value.getItemType();
                    int cardinality = value.getCardinality();
                    this.setRequiredType(SequenceType.makeSequenceType(itemType, cardinality));
                    GroundedValue constantValue = null;
                    if (value2 instanceof Literal) {
                        constantValue = ((Literal)value2).getValue();
                    }
                    for (BindingReference reference : this.references) {
                        if (!(reference instanceof VariableReference)) continue;
                        ((VariableReference)reference).refineVariableType(itemType, cardinality, constantValue, value.getSpecialProperties());
                    }
                } catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }

    public void lookForCycles(Stack<Object> referees, XQueryFunctionLibrary globalFunctionLibrary) throws XPathException {
        if (referees.contains(this)) {
            int s = referees.indexOf(this);
            referees.push(this);
            StringBuilder messageBuilder = new StringBuilder("Circular definition of global variable: $" + this.getVariableQName().getDisplayName());
            for (int i = s; i < referees.size() - 1; ++i) {
                Location next;
                if (i != s) {
                    messageBuilder.append(", which");
                }
                if (referees.get(i + 1) instanceof GlobalVariable) {
                    next = (GlobalVariable)referees.get(i + 1);
                    messageBuilder.append(" uses $").append(((GlobalVariable)next).getVariableQName().getDisplayName());
                    continue;
                }
                if (!(referees.get(i + 1) instanceof XQueryFunction)) continue;
                next = (XQueryFunction)referees.get(i + 1);
                messageBuilder.append(" calls ").append(((XQueryFunction)next).getFunctionName().getDisplayName()).append("#").append(((XQueryFunction)next).getNumberOfArguments()).append("()");
            }
            String message = messageBuilder.toString();
            message = message + '.';
            XPathException err = new XPathException(message);
            String errorCode = this.getPackageData().isXSLT() ? "XTDE0640" : (s == 0 && referees.size() == 2 ? "XPST0008" : "XQDY0054");
            err.setErrorCode(errorCode);
            err.setIsStaticError(true);
            err.setLocation(this.getLocation());
            throw err;
        }
        Expression select = this.getBody();
        if (select != null) {
            referees.push(this);
            ArrayList<Binding> list = new ArrayList<Binding>(10);
            ExpressionTool.gatherReferencedVariables(select, list);
            for (Binding b : list) {
                if (!(b instanceof GlobalVariable)) continue;
                ((GlobalVariable)b).lookForCycles(referees, globalFunctionLibrary);
            }
            ArrayList<SymbolicName> flist = new ArrayList<SymbolicName>();
            ExpressionTool.gatherCalledFunctionNames(select, flist);
            for (SymbolicName s : flist) {
                XQueryFunction f = globalFunctionLibrary.getDeclarationByKey(s);
                if (referees.contains(f)) continue;
                GlobalVariable.lookForFunctionCycles(f, referees, globalFunctionLibrary);
            }
            referees.pop();
        }
    }

    private static void lookForFunctionCycles(XQueryFunction f, Stack<Object> referees, XQueryFunctionLibrary globalFunctionLibrary) throws XPathException {
        Expression body = f.getBody();
        referees.push(f);
        ArrayList<Binding> list = new ArrayList<Binding>(10);
        ExpressionTool.gatherReferencedVariables(body, list);
        for (Binding b : list) {
            if (!(b instanceof GlobalVariable)) continue;
            ((GlobalVariable)b).lookForCycles(referees, globalFunctionLibrary);
        }
        ArrayList<SymbolicName> flist = new ArrayList<SymbolicName>();
        ExpressionTool.gatherCalledFunctionNames(body, flist);
        for (SymbolicName s : flist) {
            XQueryFunction qf = globalFunctionLibrary.getDeclarationByKey(s);
            if (referees.contains(qf)) continue;
            GlobalVariable.lookForFunctionCycles(qf, referees, globalFunctionLibrary);
        }
        referees.pop();
    }

    public GroundedValue getSelectValue(XPathContext context, Component target) throws XPathException {
        Expression select = this.getBody();
        if (select == null) {
            throw new AssertionError((Object)("*** No select expression for global variable $" + this.getVariableQName().getDisplayName() + "!!"));
        }
        if (select instanceof Literal) {
            return ((Literal)select).getValue();
        }
        try {
            Controller controller = context.getController();
            Executable exec = controller.getExecutable();
            boolean hasAccessToGlobalContext = true;
            if (exec instanceof PreparedStylesheet) {
                hasAccessToGlobalContext = target == null || target.getDeclaringPackage() == ((PreparedStylesheet)exec).getTopLevelPackage();
            }
            XPathContextMajor c2 = context.newCleanContext();
            c2.setOrigin(this);
            if (hasAccessToGlobalContext) {
                ManualIterator mi = new ManualIterator(context.getController().getGlobalContextItem());
                c2.setCurrentIterator(mi);
            } else {
                c2.setCurrentIterator(null);
            }
            if (this.getStackFrameMap() != null) {
                c2.openStackFrame(this.getStackFrameMap());
            }
            c2.setCurrentComponent(target);
            int savedOutputState = c2.getTemporaryOutputState();
            c2.setTemporaryOutputState(206);
            c2.setCurrentOutputUri(null);
            GroundedValue result = this.indexed ? c2.getConfiguration().makeSequenceExtent(select, 10000, c2) : select.iterate(c2).materialize();
            c2.setTemporaryOutputState(savedOutputState);
            return result;
        } catch (XPathException e) {
            if (!this.getVariableQName().hasURI("http://saxon.sf.net/generated-variable")) {
                e.setIsGlobalError(true);
            }
            throw e;
        }
    }

    @Override
    public GroundedValue evaluateVariable(XPathContext context) throws XPathException {
        Controller controller = context.getController();
        assert (controller != null);
        Bindery b = controller.getBindery(this.getPackageData());
        GroundedValue v = b.getGlobalVariable(this.getBinderySlotNumber());
        if (v != null) {
            return v;
        }
        return this.actuallyEvaluate(context, null);
    }

    public GroundedValue evaluateVariable(XPathContext context, Component target) throws XPathException {
        Controller controller = context.getController();
        assert (controller != null);
        Bindery b = controller.getBindery(this.getPackageData());
        if (b == null) {
            throw new AssertionError();
        }
        GroundedValue v = b.getGlobalVariable(this.getBinderySlotNumber());
        if (v != null) {
            if (v instanceof Bindery.FailureValue) {
                throw (XPathException)((Bindery.FailureValue)v).getObject();
            }
            return v;
        }
        return this.actuallyEvaluate(context, target);
    }

    protected GroundedValue actuallyEvaluate(XPathContext context, Component target) throws XPathException {
        Controller controller = context.getController();
        assert (controller != null);
        Bindery b = controller.getBindery(this.getPackageData());
        try {
            GlobalVariable.setDependencies(this, context);
            boolean go = b.setExecuting(this);
            if (!go) {
                return b.getGlobalVariable(this.getBinderySlotNumber());
            }
            GroundedValue value = this.getSelectValue(context, target);
            if (this.indexed) {
                value = controller.getConfiguration().obtainOptimizer().makeIndexedValue(value.iterate());
            }
            return b.saveGlobalVariableValue(this, value);
        } catch (XPathException err) {
            b.setNotExecuting(this);
            if (err instanceof XPathException.Circularity) {
                err.setErrorCode(this.getPackageData().isXSLT() ? "XTDE0640" : "XQDY0054");
                err.setXPathContext(context);
                err.setIsGlobalError(true);
                b.setGlobalVariable(this, new Bindery.FailureValue(err));
                err.setLocation(this.getLocation());
                throw err;
            }
            throw err;
        }
    }

    protected static void setDependencies(GlobalVariable var, XPathContext context) throws XPathException {
        Controller controller = context.getController();
        if (!(context instanceof XPathContextMajor)) {
            context = GlobalVariable.getMajorCaller(context);
        }
        while (context != null) {
            do {
                ContextOriginator origin;
                if (!((origin = ((XPathContextMajor)context).getOrigin()) instanceof GlobalVariable)) continue;
                controller.registerGlobalVariableDependency((GlobalVariable)origin, var);
                return;
            } while ((context = GlobalVariable.getMajorCaller(context)) != null);
        }
    }

    private static XPathContextMajor getMajorCaller(XPathContext context) {
        XPathContext caller;
        for (caller = context.getCaller(); caller != null && !(caller instanceof XPathContextMajor); caller = caller.getCaller()) {
        }
        return (XPathContextMajor)caller;
    }

    @Override
    public IntegerValue[] getIntegerBoundsForVariable() {
        return this.getBody() == null ? null : this.getBody().getIntegerBounds();
    }

    public int getLocalSlotNumber() {
        return 0;
    }

    public void setVariableQName(StructuredQName s) {
        this.variableQName = s;
    }

    @Override
    public StructuredQName getVariableQName() {
        return this.variableQName;
    }

    public String getDescription() {
        if (this.variableQName.hasURI("http://saxon.sf.net/generated-variable")) {
            return "optimizer-generated global variable select=\"" + this.getBody().toShortString() + '\"';
        }
        return "global variable " + this.getVariableQName().getDisplayName();
    }

    @Override
    public void addReference(VariableReference ref, boolean isLoopingReference) {
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        String flags;
        Visibility vis;
        boolean asParam = this instanceof GlobalParam && !this.isStatic();
        presenter.startElement(asParam ? "globalParam" : "globalVariable");
        presenter.emitAttribute("name", this.getVariableQName());
        presenter.emitAttribute("as", this.getRequiredType().toAlphaCode());
        presenter.emitAttribute("line", this.getLineNumber() + "");
        presenter.emitAttribute("module", this.getSystemId());
        if (this.getStackFrameMap() != null) {
            presenter.emitAttribute("slots", this.getStackFrameMap().getNumberOfVariables() + "");
        }
        if (this.getDeclaringComponent() != null && (vis = this.getDeclaringComponent().getVisibility()) != null) {
            presenter.emitAttribute("visibility", vis.toString());
        }
        if (!(flags = this.getFlags()).isEmpty()) {
            presenter.emitAttribute("flags", flags);
        }
        if (this.getBody() != null) {
            this.getBody().export(presenter);
        }
        presenter.endElement();
    }

    protected String getFlags() {
        String flags = "";
        if (this.isAssignable) {
            flags = flags + "a";
        }
        if (this.indexed) {
            flags = flags + "x";
        }
        if (this.isRequiredParam) {
            flags = flags + "r";
        }
        if (this.isStatic) {
            flags = flags + "s";
        }
        return flags;
    }
}

