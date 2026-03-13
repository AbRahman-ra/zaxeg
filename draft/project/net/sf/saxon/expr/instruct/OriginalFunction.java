/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.functions.AbstractFunction;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;

public class OriginalFunction
extends AbstractFunction
implements Function,
ContextOriginator {
    private UserFunction userFunction;
    private Component component;

    public OriginalFunction(Component component) {
        this.component = component;
        this.userFunction = (UserFunction)component.getActor();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] args) throws XPathException {
        XPathContextMajor c2 = this.userFunction.makeNewContext(context, this);
        c2.setCurrentComponent(this.component);
        return this.userFunction.call(c2, args);
    }

    @Override
    public FunctionItemType getFunctionItemType() {
        return this.userFunction.getFunctionItemType();
    }

    @Override
    public StructuredQName getFunctionName() {
        return this.userFunction.getFunctionName();
    }

    @Override
    public int getArity() {
        return this.userFunction.getArity();
    }

    @Override
    public String getDescription() {
        return this.userFunction.getDescription();
    }

    public String getContainingPackageName() {
        return this.component.getContainingPackage().getPackageName();
    }

    public Component getComponent() {
        return this.component;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        ExpressionPresenter.ExportOptions options = (ExpressionPresenter.ExportOptions)out.getOptions();
        out.startElement("origF");
        out.emitAttribute("name", this.getFunctionName());
        out.emitAttribute("arity", "" + this.getArity());
        out.emitAttribute("pack", options.packageMap.get(this.component.getContainingPackage()) + "");
        out.endElement();
    }
}

