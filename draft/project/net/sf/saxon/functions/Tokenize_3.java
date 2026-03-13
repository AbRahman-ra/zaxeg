/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.RegexFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;

public class Tokenize_3
extends RegexFunction {
    @Override
    protected boolean allowRegexMatchingEmptyString() {
        return false;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        AtomicValue sv = (AtomicValue)arguments[0].head();
        if (sv == null) {
            return EmptySequence.getInstance();
        }
        CharSequence input = sv.getStringValueCS();
        if (input.length() == 0) {
            return EmptySequence.getInstance();
        }
        RegularExpression re = this.getRegularExpression(arguments);
        return SequenceTool.toLazySequence(re.tokenize(input));
    }
}

