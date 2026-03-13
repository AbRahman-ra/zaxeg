/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;

public class StaticBaseUri
extends SystemFunction {
    @Override
    public AnyURIValue call(XPathContext context, Sequence[] args) throws XPathException {
        return new AnyURIValue(this.getRetainedStaticContext().getStaticBaseUriString());
    }

    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        PackageData pd = this.getRetainedStaticContext().getPackageData();
        if (pd.isRelocatable()) {
            return super.makeFunctionCall(arguments);
        }
        return Literal.makeLiteral(new AnyURIValue(this.getRetainedStaticContext().getStaticBaseUriString()));
    }
}

