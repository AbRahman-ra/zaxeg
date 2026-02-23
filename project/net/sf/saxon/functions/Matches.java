/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.RegexFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.StringValue;

public class Matches
extends RegexFunction {
    @Override
    protected boolean allowRegexMatchingEmptyString() {
        return true;
    }

    public boolean evalMatches(AtomicValue input, AtomicValue regex, CharSequence flags, XPathContext context) throws XPathException {
        RegularExpression re;
        if (regex == null) {
            return false;
        }
        try {
            String lang = "XP30";
            if (context.getConfiguration().getXsdVersion() == 11) {
                lang = lang + "/XSD11";
            }
            re = context.getConfiguration().compileRegularExpression(regex.getStringValueCS(), flags.toString(), lang, null);
        } catch (XPathException err) {
            XPathException de = new XPathException(err);
            de.maybeSetErrorCode("FORX0002");
            de.setXPathContext(context);
            throw de;
        }
        return re.containsMatch(input.getStringValueCS());
    }

    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        RegularExpression re = this.getRegularExpression(arguments);
        StringValue arg = (StringValue)arguments[0].head();
        CharSequence in = arg == null ? "" : arg.getStringValueCS();
        boolean result = re.containsMatch(in);
        return BooleanValue.get(result);
    }

    @Override
    public String getCompilerName() {
        return "MatchesCompiler";
    }
}

