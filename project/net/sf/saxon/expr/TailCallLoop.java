/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;

public final class TailCallLoop
extends UnaryExpression {
    UserFunction containingFunction;

    public TailCallLoop(UserFunction function, Expression body) {
        super(body);
        this.containingFunction = function;
    }

    public UserFunction getContainingFunction() {
        return this.containingFunction;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().typeCheck(visitor, contextInfo);
        return this;
    }

    @Override
    public int getImplementationMethod() {
        return this.getBaseExpression().getImplementationMethod();
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.SAME_FOCUS_ACTION;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        throw new UnsupportedOperationException("TailCallLoop.copy()");
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        TailCallInfo tail;
        UserFunction target;
        XPathContextMajor cm = (XPathContextMajor)context;
        do {
            SequenceIterator iter = this.getBaseExpression().iterate(cm);
            GroundedValue extent = iter.materialize();
            tail = cm.getTailCallInfo();
            if (tail != null) continue;
            return extent.iterate();
        } while ((target = this.establishTargetFunction(tail, cm)) == this.containingFunction);
        return this.tailCallDifferentFunction(target, cm).iterate();
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        TailCallInfo tail;
        UserFunction target;
        XPathContextMajor cm = (XPathContextMajor)context;
        do {
            Item item = this.getBaseExpression().evaluateItem(context);
            tail = cm.getTailCallInfo();
            if (tail != null) continue;
            return item;
        } while ((target = this.establishTargetFunction(tail, cm)) == this.containingFunction);
        return this.tailCallDifferentFunction(target, cm).head();
    }

    private UserFunction establishTargetFunction(TailCallInfo tail, XPathContextMajor cm) {
        if (tail instanceof TailCallFunction) {
            return ((TailCallFunction)tail).function;
        }
        if (tail instanceof TailCallComponent) {
            Component targetComponent = ((TailCallComponent)tail).component;
            cm.setCurrentComponent(targetComponent);
            return (UserFunction)targetComponent.getActor();
        }
        throw new AssertionError();
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        TailCallInfo tail;
        UserFunction target;
        XPathContextMajor cm = (XPathContextMajor)context;
        Expression operand = this.getBaseExpression();
        do {
            operand.process(output, context);
            tail = cm.getTailCallInfo();
            if (tail != null) continue;
            return;
        } while ((target = this.establishTargetFunction(tail, cm)) == this.containingFunction);
        SequenceTool.process(this.tailCallDifferentFunction(target, cm), output, operand.getLocation());
    }

    private Sequence tailCallDifferentFunction(UserFunction userFunction, XPathContextMajor cm) throws XPathException {
        cm.resetStackFrameMap(userFunction.getStackFrameMap(), userFunction.getArity());
        try {
            return userFunction.getEvaluator().evaluate(userFunction.getBody(), cm);
        } catch (XPathException err) {
            err.maybeSetLocation(this.getLocation());
            err.maybeSetContext(cm);
            throw err;
        }
    }

    @Override
    public ItemType getItemType() {
        return this.getBaseExpression().getItemType();
    }

    @Override
    public String getExpressionName() {
        return "tailCallLoop";
    }

    protected static class TailCallFunction
    implements TailCallInfo {
        public UserFunction function;

        protected TailCallFunction() {
        }
    }

    protected static class TailCallComponent
    implements TailCallInfo {
        public Component component;
        public UserFunction function;

        protected TailCallComponent() {
        }
    }

    public static interface TailCallInfo {
    }
}

