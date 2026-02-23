/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.StringValue;

public class StringToCodepoints
extends SystemFunction {
    @Override
    public IntegerValue[] getIntegerBounds() {
        return new IntegerValue[]{Int64Value.PLUS_ONE, Int64Value.makeIntegerValue(0x10FFFFL)};
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue val = (StringValue)arguments[0].head();
        if (val == null) {
            return EmptySequence.getInstance();
        }
        return SequenceTool.toLazySequence(val.iterateCharacters());
    }
}

