/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import java.util.Iterator;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.ApplyTemplates;
import net.sf.saxon.expr.instruct.CallTemplate;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.trace.ContextStackFrame;
import net.sf.saxon.trans.rules.BuiltInRuleSet;

public class ContextStackIterator
implements Iterator<ContextStackFrame> {
    private XPathContextMajor next;

    public ContextStackIterator(XPathContext context) {
        if (!(context instanceof XPathContextMajor)) {
            context = ContextStackIterator.getMajorCaller(context);
        }
        this.next = (XPathContextMajor)context;
    }

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    @Override
    public ContextStackFrame next() {
        XPathContextMajor context = this.next;
        if (context == null) {
            return null;
        }
        ContextOriginator origin = context.getOrigin();
        if (origin instanceof Controller) {
            this.next = ContextStackIterator.getMajorCaller(context);
            return new ContextStackFrame.CallingApplication();
        }
        if (origin instanceof BuiltInRuleSet) {
            this.next = ContextStackIterator.getMajorCaller(context);
            return new ContextStackFrame.BuiltInTemplateRule(context);
        }
        if (origin instanceof UserFunction) {
            ContextStackFrame.FunctionCall sf = new ContextStackFrame.FunctionCall();
            UserFunction ufc = (UserFunction)origin;
            sf.setLocation(ufc.getLocation());
            sf.setFunctionName(ufc.getFunctionName());
            sf.setContextItem(context.getContextItem());
            sf.setContext(context);
            this.next = ContextStackIterator.getMajorCaller(context);
            return sf;
        }
        if (origin instanceof UserFunctionCall) {
            ContextStackFrame.FunctionCall sf = new ContextStackFrame.FunctionCall();
            UserFunctionCall ufc = (UserFunctionCall)origin;
            sf.setLocation(ufc.getLocation());
            sf.setFunctionName(ufc.getFunctionName());
            sf.setContextItem(context.getContextItem());
            sf.setContext(context);
            this.next = ContextStackIterator.getMajorCaller(context);
            return sf;
        }
        if (origin instanceof ApplyTemplates) {
            ContextStackFrame.ApplyTemplates sf = new ContextStackFrame.ApplyTemplates();
            ApplyTemplates loc = (ApplyTemplates)origin;
            sf.setLocation(loc.getLocation());
            sf.setContextItem(context.getContextItem());
            sf.setContext(context);
            this.next = ContextStackIterator.getMajorCaller(context);
            return sf;
        }
        if (origin instanceof CallTemplate) {
            ContextStackFrame.CallTemplate sf = new ContextStackFrame.CallTemplate();
            CallTemplate loc = (CallTemplate)origin;
            sf.setLocation(loc.getLocation());
            sf.setTemplateName(loc.getObjectName());
            sf.setContextItem(context.getContextItem());
            sf.setContext(context);
            this.next = ContextStackIterator.getMajorCaller(context);
            return sf;
        }
        if (origin instanceof GlobalVariable) {
            ContextStackFrame.VariableEvaluation sf = new ContextStackFrame.VariableEvaluation();
            GlobalVariable var = (GlobalVariable)origin;
            sf.setLocation(var.getLocation());
            sf.setContextItem(context.getContextItem());
            sf.setVariableName(var.getVariableQName());
            sf.setComponent(var);
            sf.setContext(context);
            this.next = ContextStackIterator.getMajorCaller(context);
            return sf;
        }
        this.next = ContextStackIterator.getMajorCaller(context);
        ContextStackFrame csf = this.next();
        if (csf == null) {
            return new ContextStackFrame.CallingApplication();
        }
        return csf;
    }

    private static XPathContextMajor getMajorCaller(XPathContext context) {
        XPathContext caller;
        for (caller = context.getCaller(); caller != null && !(caller instanceof XPathContextMajor); caller = caller.getCaller()) {
        }
        return (XPathContextMajor)caller;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

