/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.UnparsedText;
import net.sf.saxon.functions.UnparsedTextFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.StringValue;

public class UnparsedTextAvailable
extends UnparsedTextFunction
implements Callable {
    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue hrefVal = (StringValue)arguments[0].head();
        if (hrefVal == null) {
            return BooleanValue.FALSE;
        }
        String encoding = this.getArity() == 2 ? arguments[1].head().getStringValue() : null;
        return BooleanValue.get(this.evalUnparsedTextAvailable(hrefVal, encoding, context));
    }

    public boolean evalUnparsedTextAvailable(StringValue hrefVal, String encoding, XPathContext context) {
        try {
            UnparsedText.evalUnparsedText(hrefVal, this.getStaticBaseUriString(), encoding, context);
            return true;
        } catch (XPathException err) {
            return false;
        }
    }
}

