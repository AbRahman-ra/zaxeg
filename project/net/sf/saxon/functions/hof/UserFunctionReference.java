/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ComponentInvocation;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.ExportAgent;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.UserFunctionResolvable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.functions.AbstractFunction;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;

public class UserFunctionReference
extends Expression
implements ComponentInvocation,
UserFunctionResolvable,
Callable {
    private SymbolicName functionName;
    private UserFunction nominalTarget;
    private int bindingSlot = -1;
    private int optimizeCounter = 0;
    private int typeCheckCounter = 0;

    public UserFunctionReference(UserFunction target) {
        this.nominalTarget = target;
        this.functionName = target.getSymbolicName();
    }

    public UserFunctionReference(SymbolicName name) {
        this.functionName = name;
    }

    @Override
    public void setFunction(UserFunction function) {
        if (!function.getSymbolicName().equals(this.functionName)) {
            throw new IllegalArgumentException("Function name does not match");
        }
        this.nominalTarget = function;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        if (this.nominalTarget.getFunctionName().hasURI("http://ns.saxonica.com/anonymous-type") && this.typeCheckCounter++ < 10) {
            this.nominalTarget.typeCheck(visitor);
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        if (this.nominalTarget.getFunctionName().hasURI("http://ns.saxonica.com/anonymous-type") && this.optimizeCounter++ < 10) {
            Expression o = this.nominalTarget.getBody().optimize(visitor, ContextItemStaticInfo.ABSENT);
            this.nominalTarget.setBody(o);
            SlotManager slotManager = visitor.getConfiguration().makeSlotManager();
            for (int i = 0; i < this.getArity(); ++i) {
                slotManager.allocateSlotNumber(this.nominalTarget.getParameterDefinitions()[i].getVariableQName());
            }
            ExpressionTool.allocateSlots(o, this.getArity(), slotManager);
            this.nominalTarget.setStackFrameMap(slotManager);
        }
        return this;
    }

    @Override
    public int getBindingSlot() {
        return this.bindingSlot;
    }

    @Override
    public Component getFixedTarget() {
        return this.nominalTarget.getDeclaringComponent();
    }

    @Override
    public void setBindingSlot(int slot) {
        this.bindingSlot = slot;
    }

    @Override
    public SymbolicName getSymbolicName() {
        return this.functionName;
    }

    public FunctionItemType getFunctionItemType(TypeHierarchy th) {
        return this.nominalTarget.getFunctionItemType();
    }

    public StructuredQName getFunctionName() {
        return this.nominalTarget.getFunctionName();
    }

    public int getArity() {
        return this.nominalTarget.getArity();
    }

    @Override
    protected int computeCardinality() {
        return 16384;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public ItemType getItemType() {
        return this.nominalTarget.getFunctionItemType();
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return UType.FUNCTION;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        UserFunctionReference ref = new UserFunctionReference(this.nominalTarget);
        ref.optimizeCounter = this.optimizeCounter;
        ref.typeCheckCounter = this.typeCheckCounter;
        return ref;
    }

    @Override
    public Function evaluateItem(XPathContext context) throws XPathException {
        if (this.bindingSlot == -1) {
            return new BoundUserFunction(this, this.nominalTarget, this.nominalTarget.getDeclaringComponent(), context.getController());
        }
        Component targetComponent = context.getTargetComponent(this.bindingSlot);
        return new BoundUserFunction(this, (UserFunction)targetComponent.getActor(), targetComponent, context.getController());
    }

    @Override
    public Function call(XPathContext context, Sequence[] arguments) throws XPathException {
        return this.evaluateItem(context);
    }

    @Override
    public String getExpressionName() {
        return "UserFunctionReference";
    }

    @Override
    public String toString() {
        return this.getFunctionName().getEQName() + "#" + this.getArity();
    }

    @Override
    public String toShortString() {
        return this.getFunctionName().getDisplayName() + "#" + this.getArity();
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        ExpressionPresenter.ExportOptions options = (ExpressionPresenter.ExportOptions)out.getOptions();
        if ("JS".equals(options.target) && options.targetVersion == 1) {
            throw new XPathException("Higher-order functions are not available in Saxon-JS v1.*", "XTSE3540", this.getLocation());
        }
        if (this.nominalTarget.getDeclaringComponent() == null) {
            out.startElement("inlineFn");
            this.nominalTarget.export(out);
            out.endElement();
        } else {
            StylesheetPackage rootPackage = options.rootPackage;
            StylesheetPackage containingPackage = this.nominalTarget.getDeclaringComponent().getContainingPackage();
            if (rootPackage != null && rootPackage != containingPackage && !rootPackage.contains(containingPackage)) {
                throw new XPathException("Cannot export a package containing a reference to a user-defined function (" + this.toShortString() + ") that is not present in the package being exported");
            }
            out.startElement("ufRef");
            out.emitAttribute("name", this.nominalTarget.getFunctionName());
            out.emitAttribute("arity", this.nominalTarget.getArity() + "");
            out.emitAttribute("bSlot", "" + this.getBindingSlot());
            out.endElement();
        }
    }

    public static class BoundUserFunction
    extends AbstractFunction
    implements ContextOriginator {
        private ExportAgent agent;
        private Function function;
        private Component component;
        private Controller controller;

        public BoundUserFunction(ExportAgent agent, Function function, Component component, Controller controller) {
            this.agent = agent;
            this.function = function;
            this.component = component;
            this.controller = controller;
        }

        public Function getTargetFunction() {
            return this.function;
        }

        public Controller getController() {
            return this.controller;
        }

        @Override
        public XPathContext makeNewContext(XPathContext oldContext, ContextOriginator originator) {
            if (this.controller.getConfiguration() != oldContext.getConfiguration()) {
                throw new IllegalStateException("A function created under one Configuration cannot be called under a different Configuration");
            }
            XPathContextMajor c2 = this.controller.newXPathContext();
            c2.setTemporaryOutputState(158);
            c2.setCurrentOutputUri(null);
            c2.setCurrentComponent(this.component);
            c2.setURIResolver(oldContext.getURIResolver());
            c2.setOrigin(originator);
            return this.function.makeNewContext(c2, originator);
        }

        @Override
        public Sequence call(XPathContext context, Sequence[] args) throws XPathException {
            XPathContext c2 = this.function.makeNewContext(context, this);
            if (c2 instanceof XPathContextMajor && this.component != null) {
                ((XPathContextMajor)c2).setCurrentComponent(this.component);
            }
            return this.function.call(c2, args);
        }

        @Override
        public FunctionItemType getFunctionItemType() {
            return this.function.getFunctionItemType();
        }

        @Override
        public AnnotationList getAnnotations() {
            return this.function.getAnnotations();
        }

        @Override
        public StructuredQName getFunctionName() {
            return this.function.getFunctionName();
        }

        @Override
        public int getArity() {
            return this.function.getArity();
        }

        @Override
        public String getDescription() {
            return this.function.getDescription();
        }

        @Override
        public void export(ExpressionPresenter out) throws XPathException {
            this.agent.export(out);
        }
    }
}

