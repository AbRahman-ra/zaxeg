/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.RegexFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class Replace
extends RegexFunction {
    private boolean replacementChecked = false;

    @Override
    protected boolean allowRegexMatchingEmptyString() {
        return false;
    }

    @Override
    public Expression makeFunctionCall(Expression ... arguments) {
        String rep;
        boolean maybeQ;
        boolean bl = maybeQ = arguments.length == 4 && (!(arguments[3] instanceof StringLiteral) || ((StringLiteral)arguments[3]).getStringValue().contains("q"));
        if (arguments[2] instanceof StringLiteral && !maybeQ && Replace.checkReplacement(rep = ((StringLiteral)arguments[2]).getStringValue()) == null) {
            this.replacementChecked = true;
        }
        return super.makeFunctionCall(arguments);
    }

    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        String msg;
        StringValue arg0 = (StringValue)arguments[0].head();
        CharSequence input = arg0 == null ? "" : arg0.getStringValueCS();
        StringValue arg2 = (StringValue)arguments[2].head();
        CharSequence replacement = arg2.getStringValueCS();
        RegularExpression re = this.getRegularExpression(arguments);
        if (!re.getFlags().contains("q") && !this.replacementChecked && (msg = Replace.checkReplacement(replacement)) != null) {
            throw new XPathException(msg, "FORX0004", context);
        }
        CharSequence res = re.replace(input, replacement);
        return StringValue.makeStringValue(res);
    }

    public static String checkReplacement(CharSequence rep) {
        for (int i = 0; i < rep.length(); ++i) {
            char next;
            char c = rep.charAt(i);
            if (c == '$') {
                if (i + 1 < rep.length()) {
                    if ((next = rep.charAt(++i)) >= '0' && next <= '9') continue;
                    return "Invalid replacement string in replace(): $ sign must be followed by digit 0-9";
                }
                return "Invalid replacement string in replace(): $ sign at end of string";
            }
            if (c != '\\') continue;
            if (i + 1 < rep.length()) {
                if ((next = rep.charAt(++i)) == '\\' || next == '$') continue;
                return "Invalid replacement string in replace(): \\ character must be followed by \\ or $";
            }
            return "Invalid replacement string in replace(): \\ character at end of string";
        }
        return null;
    }
}

