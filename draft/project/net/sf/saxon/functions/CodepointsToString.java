/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.function.IntPredicate;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.StringValue;

public class CodepointsToString
extends SystemFunction
implements Callable {
    public static CharSequence unicodeToString(SequenceIterator chars, IntPredicate checker) throws XPathException {
        FastStringBuffer sb = new FastStringBuffer(64);
        NumericValue nextInt;
        while ((nextInt = (NumericValue)chars.next()) != null) {
            long next = nextInt.longValue();
            if (next < 0L || next > Integer.MAX_VALUE || !checker.test((int)next)) {
                throw new XPathException("codepoints-to-string(): invalid XML character [x" + Integer.toHexString((int)next) + ']', "FOCH0001");
            }
            sb.appendWideChar((int)next);
        }
        return sb.condense();
    }

    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        SequenceIterator chars = arguments[0].iterate();
        return new StringValue(CodepointsToString.unicodeToString(chars, context.getConfiguration().getValidCharacterChecker()));
    }

    @Override
    public String getStreamerName() {
        return "CodepointsToString";
    }
}

