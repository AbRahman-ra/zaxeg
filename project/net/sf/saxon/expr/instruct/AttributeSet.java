/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.Stack;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;

public class AttributeSet
extends Actor {
    StructuredQName attributeSetName;
    private boolean declaredStreamable;

    @Override
    public SymbolicName getSymbolicName() {
        return new SymbolicName(136, this.attributeSetName);
    }

    public void setName(StructuredQName attributeSetName) {
        this.attributeSetName = attributeSetName;
    }

    public void setDeclaredStreamable(boolean value) {
        this.declaredStreamable = value;
    }

    public boolean isDeclaredStreamable() {
        return this.declaredStreamable;
    }

    @Override
    public void setStackFrameMap(SlotManager stackFrameMap) {
        if (stackFrameMap != null) {
            super.setStackFrameMap(stackFrameMap);
        }
    }

    public int getFocusDependencies() {
        return this.body.getDependencies() & 0x1E;
    }

    public void expand(Outputter output, XPathContext context) throws XPathException {
        Stack<AttributeSet> stack = ((XsltController)context.getController()).getAttributeSetEvaluationStack();
        if (stack.contains(this)) {
            throw new XPathException("Attribute set " + this.getObjectName().getEQName() + " invokes itself recursively", "XTDE0640");
        }
        stack.push(this);
        this.getBody().process(output, context);
        stack.pop();
        if (stack.isEmpty()) {
            ((XsltController)context.getController()).releaseAttributeSetEvaluationStack();
        }
    }

    public StructuredQName getObjectName() {
        return this.attributeSetName;
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("attributeSet");
        presenter.emitAttribute("name", this.getObjectName());
        presenter.emitAttribute("line", this.getLineNumber() + "");
        presenter.emitAttribute("module", this.getSystemId());
        presenter.emitAttribute("slots", this.getStackFrameMap().getNumberOfVariables() + "");
        presenter.emitAttribute("binds", this.getDeclaringComponent().getComponentBindings().size() + "");
        if (this.isDeclaredStreamable()) {
            presenter.emitAttribute("flags", "s");
        }
        this.getBody().export(presenter);
        presenter.endElement();
    }
}

