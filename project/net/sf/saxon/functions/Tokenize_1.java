/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Whitespace;

public class Tokenize_1
extends SystemFunction {
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        AtomicValue sv = (AtomicValue)arguments[0].head();
        if (sv == null) {
            return EmptySequence.getInstance();
        }
        CharSequence input = sv.getStringValueCS();
        return SequenceTool.toLazySequence(new Whitespace.Tokenizer(input));
    }
}

