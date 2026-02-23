/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public abstract class ExtensionFunctionCall
implements Callable {
    ExtensionFunctionDefinition definition;

    public final void setDefinition(ExtensionFunctionDefinition definition) {
        this.definition = definition;
    }

    public final ExtensionFunctionDefinition getDefinition() {
        return this.definition;
    }

    public void supplyStaticContext(StaticContext context, int locationId, Expression[] arguments) throws XPathException {
    }

    public Expression rewrite(StaticContext context, Expression[] arguments) throws XPathException {
        return null;
    }

    public void copyLocalData(ExtensionFunctionCall destination) {
    }

    @Override
    public abstract Sequence call(XPathContext var1, Sequence[] var2) throws XPathException;

    public boolean effectiveBooleanValue(XPathContext context, Sequence[] arguments) throws XPathException {
        return ExpressionTool.effectiveBooleanValue(this.call(context, arguments).iterate());
    }

    public Object getStreamingImplementation() {
        return null;
    }
}

