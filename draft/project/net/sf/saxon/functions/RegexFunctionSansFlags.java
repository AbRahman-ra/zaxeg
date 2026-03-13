/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class RegexFunctionSansFlags
extends SystemFunction {
    private SystemFunction addFlagsArgument() {
        Configuration config = this.getRetainedStaticContext().getConfiguration();
        SystemFunction fixed = config.makeSystemFunction(this.getFunctionName().getLocalPart(), this.getArity() + 1);
        fixed.setRetainedStaticContext(this.getRetainedStaticContext());
        return fixed;
    }

    @Override
    public Expression makeFunctionCall(Expression ... arguments) {
        SystemFunction withFlags = this.addFlagsArgument();
        Expression[] newArgs = new Expression[arguments.length + 1];
        System.arraycopy(arguments, 0, newArgs, 0, arguments.length);
        newArgs[arguments.length] = new StringLiteral("");
        return withFlags.makeFunctionCall(newArgs);
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] args) throws XPathException {
        SystemFunction withFlags = this.addFlagsArgument();
        Sequence[] newArgs = new Sequence[args.length + 1];
        System.arraycopy(args, 0, newArgs, 0, args.length);
        newArgs[args.length] = StringValue.EMPTY_STRING;
        return withFlags.call(context, newArgs);
    }
}

