/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.ArrayList;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.StatefulSystemFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public abstract class RegexFunction
extends SystemFunction
implements StatefulSystemFunction {
    private RegularExpression staticRegex;

    public RegularExpression getStaticRegex() {
        return this.staticRegex;
    }

    private void tryToBindRegularExpression(Expression[] arguments) {
        if (arguments[1] instanceof Literal && arguments[arguments.length - 1] instanceof Literal) {
            try {
                Configuration config = this.getRetainedStaticContext().getConfiguration();
                String re = ((Literal)arguments[1]).getValue().getStringValue();
                String flags = ((Literal)arguments[arguments.length - 1]).getValue().getStringValue();
                String hostLang = "XP30";
                if (config.getXsdVersion() == 11) {
                    hostLang = hostLang + "/XSD11";
                }
                ArrayList<String> warnings = new ArrayList<String>(1);
                this.staticRegex = config.compileRegularExpression(re, flags, hostLang, warnings);
                if (!this.allowRegexMatchingEmptyString() && this.staticRegex.matches("")) {
                    this.staticRegex = null;
                }
            } catch (XPathException xPathException) {
                // empty catch block
            }
        }
    }

    @Override
    public RegexFunction copy() {
        RegexFunction copy = (RegexFunction)SystemFunction.makeFunction(this.getFunctionName().getLocalPart(), this.getRetainedStaticContext(), this.getArity());
        copy.staticRegex = this.staticRegex;
        return copy;
    }

    protected abstract boolean allowRegexMatchingEmptyString();

    @Override
    public Expression makeFunctionCall(Expression ... arguments) {
        this.tryToBindRegularExpression(arguments);
        return super.makeFunctionCall(arguments);
    }

    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        this.tryToBindRegularExpression(arguments);
        return super.makeOptimizedFunctionCall(visitor, contextInfo, arguments);
    }

    protected RegularExpression getRegularExpression(Sequence[] args) throws XPathException {
        if (this.staticRegex != null) {
            return this.staticRegex;
        }
        Configuration config = this.getRetainedStaticContext().getConfiguration();
        StringValue regexArg = (StringValue)args[1].head();
        String re = regexArg.getStringValue();
        String flags = args[args.length - 1].head().getStringValue();
        String hostLang = "XP30";
        if (config.getXsdVersion() == 11) {
            hostLang = hostLang + "/XSD11";
        }
        ArrayList<String> warnings = new ArrayList<String>(1);
        RegularExpression regex = config.compileRegularExpression(re, flags, hostLang, warnings);
        if (!this.allowRegexMatchingEmptyString() && regex.matches("")) {
            throw new XPathException("The regular expression must not be one that matches a zero-length string", "FORX0003");
        }
        return regex;
    }
}

