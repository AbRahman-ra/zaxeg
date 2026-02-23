/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.CollatingFunctionFixed;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Base64BinaryValue;

public class CollationKeyFn
extends CollatingFunctionFixed {
    private static Base64BinaryValue getCollationKey(String s, StringCollator collator) {
        AtomicValue val = collator.getCollationKey(s).asAtomic();
        if (val instanceof Base64BinaryValue) {
            return (Base64BinaryValue)val;
        }
        throw new IllegalStateException("Collation key must be Base64Binary");
    }

    @Override
    public Base64BinaryValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        String in = arguments[0].head().getStringValue();
        StringCollator collator = this.getStringCollator();
        return CollationKeyFn.getCollationKey(in, collator);
    }
}

