/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ExportAgent;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.CallableFunction;
import net.sf.saxon.functions.ContextAccessorFunction;
import net.sf.saxon.functions.ContextItemAccessorFunction;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.hof.CallableWithBoundFocus;
import net.sf.saxon.functions.hof.SystemFunctionWithBoundContextItem;
import net.sf.saxon.functions.hof.UserFunctionReference;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.QNameValue;

public class FunctionLookup
extends ContextAccessorFunction {
    private XPathContext boundContext = null;

    @Override
    public Expression makeFunctionCall(Expression ... arguments) {
        PackageData pack = this.getRetainedStaticContext().getPackageData();
        if (pack instanceof StylesheetPackage) {
            ((StylesheetPackage)pack).setRetainUnusedFunctions();
        }
        return super.makeFunctionCall(arguments);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && ExpressionTool.equalOrNull(this.getRetainedStaticContext(), ((FunctionLookup)o).getRetainedStaticContext());
    }

    @Override
    public Function bindContext(XPathContext context) {
        FunctionLookup bound = (FunctionLookup)SystemFunction.makeFunction("function-lookup", this.getRetainedStaticContext(), 2);
        FocusIterator focusIterator = context.getCurrentIterator();
        if (focusIterator != null) {
            XPathContextMinor c2 = context.newMinorContext();
            ManualIterator mi = new ManualIterator(context.getContextItem(), focusIterator.position());
            c2.setCurrentIterator(mi);
            bound.boundContext = c2;
        } else {
            bound.boundContext = context;
        }
        return bound;
    }

    public Function lookup(StructuredQName name, int arity, XPathContext context) throws XPathException {
        Controller controller = context.getController();
        Executable exec = controller.getExecutable();
        RetainedStaticContext rsc = this.getRetainedStaticContext();
        PackageData pd = rsc.getPackageData();
        FunctionLibraryList lib = pd instanceof StylesheetPackage ? ((StylesheetPackage)pd).getFunctionLibrary() : exec.getFunctionLibrary();
        SymbolicName.F sn = new SymbolicName.F(name, arity);
        IndependentContext ic = new IndependentContext(controller.getConfiguration());
        ic.setDefaultCollationName(rsc.getDefaultCollationName());
        ic.setBaseURI(rsc.getStaticBaseUriString());
        ic.setDecimalFormatManager(rsc.getDecimalFormatManager());
        ic.setNamespaceResolver(rsc);
        ic.setPackageData(pd);
        try {
            Visibility vis;
            Function fi = lib.getFunctionItem(sn, ic);
            if (fi instanceof UserFunction && (vis = ((UserFunction)fi).getDeclaredVisibility()) == Visibility.ABSTRACT) {
                return null;
            }
            if (fi instanceof CallableFunction) {
                ((CallableFunction)fi).setCallable(new CallableWithBoundFocus(((CallableFunction)fi).getCallable(), context));
            } else {
                if (fi instanceof ContextItemAccessorFunction) {
                    return ((ContextItemAccessorFunction)fi).bindContext(context);
                }
                if (fi instanceof SystemFunction && ((SystemFunction)fi).dependsOnContextItem()) {
                    return new SystemFunctionWithBoundContextItem((SystemFunction)fi, context);
                }
            }
            return fi;
        } catch (XPathException e) {
            if ("XPST0017".equals(e.getErrorCodeLocalPart())) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        XPathContext c = this.boundContext == null ? context : this.boundContext;
        QNameValue qname = (QNameValue)arguments[0].head();
        IntegerValue arity = (IntegerValue)arguments[1].head();
        Function fi = this.lookup(qname.getStructuredQName(), (int)arity.longValue(), c);
        if (fi == null) {
            return ZeroOrOne.empty();
        }
        if (fi instanceof ContextAccessorFunction) {
            fi = ((ContextAccessorFunction)fi).bindContext(c);
        }
        Component target = fi instanceof UserFunction ? ((UserFunction)fi).getDeclaringComponent() : null;
        ExportAgent agent = out -> this.makeFunctionCall(Literal.makeLiteral(qname), Literal.makeLiteral(arity)).export(out);
        UserFunctionReference.BoundUserFunction result = new UserFunctionReference.BoundUserFunction(agent, fi, target, c.getController());
        return new ZeroOrOne<UserFunctionReference.BoundUserFunction>(result);
    }
}

