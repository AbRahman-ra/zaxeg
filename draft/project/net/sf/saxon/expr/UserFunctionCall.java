/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.ArrayList;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ComponentInvocation;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.EvaluationMode;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.PendingUpdateList;
import net.sf.saxon.expr.TailCallLoop;
import net.sf.saxon.expr.UserFunctionResolvable;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.Evaluator;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Whitespace;

public class UserFunctionCall
extends FunctionCall
implements UserFunctionResolvable,
ComponentInvocation,
ContextOriginator {
    private SequenceType staticType;
    private UserFunction function;
    private int bindingSlot = -1;
    private int tailCall = 0;
    private StructuredQName name;
    private boolean beingInlined = false;
    private Evaluator[] argumentEvaluators = null;
    public static final int NOT_TAIL_CALL = 0;
    public static final int FOREIGN_TAIL_CALL = 1;
    public static final int SELF_TAIL_CALL = 2;
    private static final int UNHANDLED_DEPENDENCIES = 877;
    private static int depth = 0;

    public boolean isBeingInlined() {
        return this.beingInlined;
    }

    public void setBeingInlined(boolean beingInlined) {
        this.beingInlined = beingInlined;
    }

    public final void setFunctionName(StructuredQName name) {
        this.name = name;
    }

    public void setStaticType(SequenceType type) {
        this.staticType = type;
    }

    @Override
    public void setFunction(UserFunction compiledFunction) {
        this.function = compiledFunction;
    }

    @Override
    public void setBindingSlot(int slot) {
        this.bindingSlot = slot;
    }

    @Override
    public int getBindingSlot() {
        return this.bindingSlot;
    }

    public UserFunction getFunction() {
        return this.function;
    }

    @Override
    public Component getFixedTarget() {
        Visibility v = this.function.getDeclaringComponent().getVisibility();
        if (v == Visibility.PRIVATE || v == Visibility.FINAL) {
            return this.function.getDeclaringComponent();
        }
        return null;
    }

    public boolean isTailCall() {
        return this.tailCall != 0;
    }

    public boolean isRecursiveTailCall() {
        return this.tailCall == 2;
    }

    @Override
    public final StructuredQName getFunctionName() {
        if (this.name == null) {
            return this.function.getFunctionName();
        }
        return this.name;
    }

    @Override
    public SymbolicName getSymbolicName() {
        return new SymbolicName.F(this.getFunctionName(), this.getArity());
    }

    public Component getTarget() {
        return this.function.getDeclaringComponent();
    }

    public void setArgumentEvaluationModes(EvaluationMode[] evalModes) {
        this.argumentEvaluators = new Evaluator[evalModes.length];
        for (int i = 0; i < evalModes.length; ++i) {
            this.argumentEvaluators[i] = evalModes[i].getEvaluator();
        }
    }

    public void allocateArgumentEvaluators() {
        this.argumentEvaluators = new Evaluator[this.getArity()];
        int i = 0;
        for (Operand o : this.operands()) {
            Expression arg = o.getChildExpression();
            SequenceType required = this.function.getArgumentType(i);
            int cardinality = required.getCardinality();
            this.argumentEvaluators[i] = i == 0 && this.function.getDeclaredStreamability().isConsuming() ? Evaluator.STREAMING_ARGUMENT : (this.function.getParameterDefinitions()[i].isIndexedVariable() ? Evaluator.MAKE_INDEXED_VARIABLE : (arg instanceof Literal ? Evaluator.LITERAL : (arg instanceof VariableReference ? Evaluator.VARIABLE : (cardinality == 16384 ? Evaluator.SINGLE_ITEM : ((arg.getDependencies() & 0x36D) != 0 ? Evaluator.EAGER_SEQUENCE : (!Cardinality.allowsMany(arg.getCardinality()) && arg.getCost() < 20.0 ? Evaluator.EAGER_SEQUENCE : (cardinality == 24576 ? Evaluator.OPTIONAL_ITEM : (arg instanceof Block && ((Block)arg).isCandidateForSharedAppend() ? Evaluator.SHARED_APPEND : Evaluator.MEMO_CLOSURE))))))));
            ++i;
        }
    }

    public Evaluator[] getArgumentEvaluators() {
        return this.argumentEvaluators;
    }

    @Override
    public Expression preEvaluate(ExpressionVisitor visitor) {
        return this;
    }

    @Override
    public ItemType getItemType() {
        if (this.staticType == null) {
            return AnyItemType.getInstance();
        }
        return this.staticType.getPrimaryType();
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        UserFunction f = this.getFunction();
        if (f == null) {
            return UType.ANY;
        }
        return f.getResultType().getPrimaryType().getUType();
    }

    @Override
    public int getIntrinsicDependencies() {
        return 256;
    }

    @Override
    public boolean isUpdatingExpression() {
        return this.function.isUpdating();
    }

    @Override
    protected int computeSpecialProperties() {
        if (this.function == null) {
            return super.computeSpecialProperties();
        }
        if (this.function.getBody() != null && (this.function.getDeclaredVisibility() == Visibility.PRIVATE || this.function.getDeclaredVisibility() == Visibility.FINAL)) {
            ArrayList<UserFunction> calledFunctions = new ArrayList<UserFunction>();
            ExpressionTool.gatherCalledFunctions(this.function.getBody(), calledFunctions);
            int props = calledFunctions.isEmpty() ? this.function.getBody().getSpecialProperties() : super.computeSpecialProperties();
            if (this.function.getDeterminism() != UserFunction.Determinism.PROACTIVE) {
                props |= 0x800000;
            }
            return props;
        }
        return super.computeSpecialProperties();
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        if (this.function == null) {
            throw new UnsupportedOperationException("UserFunctionCall.copy()");
        }
        UserFunctionCall ufc = new UserFunctionCall();
        ufc.setFunction(this.function);
        ufc.setStaticType(this.staticType);
        int numArgs = this.getArity();
        Expression[] a2 = new Expression[numArgs];
        for (int i = 0; i < numArgs; ++i) {
            a2[i] = this.getArg(i).copy(rebindings);
        }
        ufc.setArguments(a2);
        ExpressionTool.copyLocationInfo(this, ufc);
        return ufc;
    }

    @Override
    public int computeCardinality() {
        if (this.staticType == null) {
            return 57344;
        }
        return this.staticType.getCardinality();
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression e = super.typeCheck(visitor, contextInfo);
        if (e != this) {
            return e;
        }
        if (this.function != null) {
            this.checkFunctionCall(this.function, visitor);
            if (this.staticType == null || this.staticType == SequenceType.ANY_SEQUENCE) {
                this.staticType = this.function.getResultType();
            }
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression e = super.optimize(visitor, contextItemType);
        if (e == this && this.function != null) {
            return visitor.obtainOptimizer().tryInlineFunctionCall(this, visitor, contextItemType);
        }
        return e;
    }

    @Override
    public void resetLocalStaticProperties() {
        super.resetLocalStaticProperties();
        this.argumentEvaluators = null;
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        return this.addExternalFunctionCallToPathMap(pathMap, pathMapNodeSet);
    }

    @Override
    public int markTailFunctionCalls(StructuredQName qName, int arity) {
        this.tailCall = this.getFunctionName().equals(qName) && arity == this.getArity() ? 2 : 1;
        return this.tailCall;
    }

    @Override
    public int getImplementationMethod() {
        if (Cardinality.allowsMany(this.getCardinality())) {
            return 6;
        }
        return 1;
    }

    @Override
    public Item evaluateItem(XPathContext c) throws XPathException {
        return this.callFunction(c).head();
    }

    @Override
    public SequenceIterator iterate(XPathContext c) throws XPathException {
        return this.callFunction(c).iterate();
    }

    @Override
    public void evaluatePendingUpdates(XPathContext context, PendingUpdateList pul) throws XPathException {
        Sequence[] actualArgs = this.evaluateArguments(context);
        XPathContextMajor c2 = context.newCleanContext();
        c2.setOrigin(this);
        this.function.callUpdating(actualArgs, c2, pul);
    }

    private Sequence callFunction(XPathContext context) throws XPathException {
        XPathContextMajor c2;
        UserFunction targetFunction;
        Sequence[] actualArgs = this.evaluateArguments(context);
        if (this.isTailCall()) {
            this.requestTailCall(context, actualArgs);
            return EmptySequence.getInstance();
        }
        if (this.bindingSlot >= 0) {
            Component target = this.getTargetComponent(context);
            if (target.isHiddenAbstractComponent()) {
                throw new XPathException("Cannot call an abstract function (" + this.name.getDisplayName() + ") with no implementation", "XTDE3052");
            }
            targetFunction = (UserFunction)target.getActor();
            c2 = targetFunction.makeNewContext(context, this);
            c2.setCurrentComponent(target);
            c2.setOrigin(this);
        } else {
            targetFunction = this.function;
            c2 = targetFunction.makeNewContext(context, this);
            c2.setOrigin(this);
        }
        try {
            return targetFunction.call(c2, actualArgs);
        } catch (UncheckedXPathException e) {
            XPathException xe = e.getXPathException();
            xe.maybeSetLocation(this.getLocation());
            throw xe;
        } catch (StackOverflowError err) {
            throw new XPathException.StackOverflow("Too many nested function calls. May be due to infinite recursion", "SXLM0001", this.getLocation());
        }
    }

    private void requestTailCall(XPathContext context, Sequence[] actualArgs) throws XPathException {
        if (this.bindingSlot >= 0) {
            Component target;
            TailCallLoop.TailCallComponent info = new TailCallLoop.TailCallComponent();
            info.component = target = this.getTargetComponent(context);
            info.function = (UserFunction)target.getActor();
            if (target.isHiddenAbstractComponent()) {
                throw new XPathException("Cannot call an abstract function (" + this.name.getDisplayName() + ") with no implementation", "XTDE3052");
            }
            ((XPathContextMajor)context).requestTailCall(info, actualArgs);
        } else {
            TailCallLoop.TailCallFunction info = new TailCallLoop.TailCallFunction();
            info.function = this.function;
            ((XPathContextMajor)context).requestTailCall(info, actualArgs);
        }
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        Sequence[] actualArgs = this.evaluateArguments(context);
        if (this.isTailCall()) {
            this.requestTailCall(context, actualArgs);
            return;
        }
        if (this.bindingSlot >= 0) {
            Component target = this.getTargetComponent(context);
            UserFunction targetFunction = (UserFunction)target.getActor();
            if (target.getVisibility() == Visibility.ABSTRACT) {
                throw new XPathException("Cannot call a function defined with visibility=abstract", "XTDE3052");
            }
            XPathContextMajor c2 = targetFunction.makeNewContext(context, this);
            c2.setCurrentComponent(target);
            c2.setOrigin(this);
            targetFunction.process(c2, actualArgs, output);
        } else {
            XPathContextMajor c2 = this.function.makeNewContext(context, this);
            c2.setOrigin(this);
            this.function.process(c2, actualArgs, output);
        }
    }

    public Component getTargetComponent(XPathContext context) {
        if (this.bindingSlot == -1) {
            return this.function.getDeclaringComponent();
        }
        return context.getTargetComponent(this.bindingSlot);
    }

    @Override
    public UserFunction getTargetFunction(XPathContext context) {
        return (UserFunction)this.getTargetComponent(context).getActor();
    }

    @Override
    public Sequence[] evaluateArguments(XPathContext c) throws XPathException {
        return this.evaluateArguments(c, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Sequence[] evaluateArguments(XPathContext c, boolean streamed) throws XPathException {
        int numArgs = this.getArity();
        Sequence[] actualArgs = SequenceTool.makeSequenceArray(numArgs);
        UserFunctionCall userFunctionCall = this;
        synchronized (userFunctionCall) {
            if (this.argumentEvaluators == null) {
                this.allocateArgumentEvaluators();
            }
        }
        for (int i = 0; i < numArgs; ++i) {
            Evaluator eval = this.argumentEvaluators[i];
            if (eval == Evaluator.STREAMING_ARGUMENT && !streamed) {
                eval = Evaluator.EAGER_SEQUENCE;
            }
            actualArgs[i] = eval.evaluate(this.getArg(i), c);
            if (actualArgs[i] != null) continue;
            actualArgs[i] = EmptySequence.getInstance();
        }
        return actualArgs;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("ufCall", this);
        if (this.getFunctionName() != null) {
            out.emitAttribute("name", this.getFunctionName());
            out.emitAttribute("tailCall", this.tailCall == 0 ? "false" : (this.tailCall == 2 ? "self" : "foreign"));
        }
        out.emitAttribute("bSlot", "" + this.getBindingSlot());
        if (this.argumentEvaluators != null && this.getArity() > 0) {
            FastStringBuffer fsb = new FastStringBuffer(64);
            for (Evaluator e : this.argumentEvaluators) {
                fsb.append(e.getEvaluationMode().getCode() + " ");
            }
            out.emitAttribute("eval", Whitespace.trim(fsb));
        }
        for (Operand o : this.operands()) {
            o.getChildExpression().export(out);
        }
        if (this.getFunctionName() == null) {
            out.setChildRole("inline");
            this.function.getBody().export(out);
            out.endElement();
        }
        out.endElement();
    }

    @Override
    public String getExpressionName() {
        return "userFunctionCall";
    }

    @Override
    public Object getProperty(String name) {
        if (name.equals("target")) {
            return this.function;
        }
        return super.getProperty(name);
    }

    @Override
    public StructuredQName getObjectName() {
        return this.getFunctionName();
    }
}

