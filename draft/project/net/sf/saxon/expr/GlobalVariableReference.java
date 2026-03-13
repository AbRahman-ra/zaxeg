/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.HashSet;
import java.util.Set;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ComponentInvocation;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;

public class GlobalVariableReference
extends VariableReference
implements ComponentInvocation {
    int bindingSlot = -1;

    public GlobalVariableReference(StructuredQName name) {
        super(name);
    }

    public GlobalVariableReference(GlobalVariable var) {
        super(var);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        if (this.binding == null) {
            throw new UnsupportedOperationException("Cannot copy a variable reference whose binding is unknown");
        }
        GlobalVariableReference ref = new GlobalVariableReference(this.getVariableName());
        ref.copyFrom(this);
        return ref;
    }

    @Override
    public void setBindingSlot(int slot) {
        if (this.bindingSlot != -1) {
            throw new AssertionError((Object)"Duplicate binding slot assignment");
        }
        this.bindingSlot = slot;
    }

    @Override
    public int getBindingSlot() {
        return this.bindingSlot;
    }

    @Override
    public SymbolicName getSymbolicName() {
        return new SymbolicName(206, this.getVariableName());
    }

    public void setTarget(Component target) {
        this.binding = (GlobalVariable)target.getActor();
    }

    public Component getTarget() {
        return ((GlobalVariable)this.binding).getDeclaringComponent();
    }

    @Override
    public Component getFixedTarget() {
        Component c = this.getTarget();
        Visibility v = c.getVisibility();
        if (v == Visibility.PRIVATE || v == Visibility.FINAL) {
            return c;
        }
        return null;
    }

    @Override
    public GroundedValue evaluateVariable(XPathContext c) throws XPathException {
        if (this.bindingSlot >= 0) {
            if (c.getCurrentComponent() == null) {
                throw new AssertionError((Object)"No current component");
            }
            Component target = c.getTargetComponent(this.bindingSlot);
            if (target.isHiddenAbstractComponent()) {
                XPathException err = new XPathException("Cannot evaluate an abstract variable (" + this.getVariableName().getDisplayName() + ") with no overriding declaration", "XTDE3052");
                err.setLocation(this.getLocation());
                throw err;
            }
            GlobalVariable p = (GlobalVariable)target.getActor();
            return p.evaluateVariable(c, target);
        }
        GlobalVariable b = (GlobalVariable)this.binding;
        return b.evaluateVariable(c, b.getDeclaringComponent());
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("gVarRef", this);
        out.emitAttribute("name", this.getVariableName());
        out.emitAttribute("bSlot", "" + this.getBindingSlot());
        out.endElement();
    }

    public Set<Expression> getPreconditions() {
        HashSet<Expression> pre = new HashSet<Expression>();
        return pre;
    }
}

