/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.QNameValue;

public class FunctionName
extends SystemFunction {
    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        Function f = (Function)arguments[0].head();
        assert (f != null);
        StructuredQName name = f.getFunctionName();
        if (name == null) {
            return ZeroOrOne.empty();
        }
        if (name.hasURI("http://ns.saxonica.com/anonymous-type")) {
            return ZeroOrOne.empty();
        }
        QNameValue result = new QNameValue(name, BuiltInAtomicType.QNAME);
        return new ZeroOrOne<QNameValue>(result);
    }
}

