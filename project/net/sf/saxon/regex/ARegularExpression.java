/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import java.util.List;
import java.util.function.Function;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.regex.ARegexIterator;
import net.sf.saxon.regex.ATokenIterator;
import net.sf.saxon.regex.RECompiler;
import net.sf.saxon.regex.REFlags;
import net.sf.saxon.regex.REMatcher;
import net.sf.saxon.regex.REProgram;
import net.sf.saxon.regex.RESyntaxException;
import net.sf.saxon.regex.RegexIterator;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.value.StringValue;

public class ARegularExpression
implements RegularExpression {
    UnicodeString rawPattern;
    String rawFlags;
    REProgram regex;

    public ARegularExpression(CharSequence pattern, String flags, String hostLanguage, List<String> warnings, Configuration config) throws XPathException {
        REFlags reFlags;
        this.rawFlags = flags;
        try {
            reFlags = new REFlags(flags, hostLanguage);
        } catch (RESyntaxException err) {
            throw new XPathException(err.getMessage(), "FORX0001");
        }
        try {
            this.rawPattern = UnicodeString.makeUnicodeString(pattern);
            RECompiler comp2 = new RECompiler();
            comp2.setFlags(reFlags);
            this.regex = comp2.compile(this.rawPattern);
            if (warnings != null) {
                for (String s : comp2.getWarnings()) {
                    warnings.add(s);
                }
            }
            if (config != null) {
                this.regex.setBacktrackingLimit(config.getConfigurationProperty(Feature.REGEX_BACKTRACKING_LIMIT));
            }
        } catch (RESyntaxException err) {
            throw new XPathException(err.getMessage(), "FORX0002");
        }
    }

    public static ARegularExpression compile(String pattern, String flags) {
        try {
            return new ARegularExpression(pattern, flags, "XP31", null, null);
        } catch (XPathException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean matches(CharSequence input) {
        if (StringValue.isEmpty(input) && this.regex.isNullable()) {
            return true;
        }
        REMatcher matcher = new REMatcher(this.regex);
        return matcher.anchoredMatch(UnicodeString.makeUnicodeString(input));
    }

    @Override
    public boolean containsMatch(CharSequence input) {
        REMatcher matcher = new REMatcher(this.regex);
        return matcher.match(UnicodeString.makeUnicodeString(input), 0);
    }

    @Override
    public AtomicIterator tokenize(CharSequence input) {
        return new ATokenIterator(UnicodeString.makeUnicodeString(input), new REMatcher(this.regex));
    }

    @Override
    public RegexIterator analyze(CharSequence input) {
        return new ARegexIterator(UnicodeString.makeUnicodeString(input), this.rawPattern, new REMatcher(this.regex));
    }

    @Override
    public CharSequence replace(CharSequence input, CharSequence replacement) throws XPathException {
        REMatcher matcher = new REMatcher(this.regex);
        UnicodeString in = UnicodeString.makeUnicodeString(input);
        UnicodeString rep = UnicodeString.makeUnicodeString(replacement);
        try {
            return matcher.replace(in, rep);
        } catch (RESyntaxException err) {
            throw new XPathException(err.getMessage(), "FORX0004");
        }
    }

    @Override
    public CharSequence replaceWith(CharSequence input, Function<CharSequence, CharSequence> replacer) throws XPathException {
        REMatcher matcher = new REMatcher(this.regex);
        UnicodeString in = UnicodeString.makeUnicodeString(input);
        try {
            return matcher.replaceWith(in, replacer);
        } catch (RESyntaxException err) {
            throw new XPathException(err.getMessage(), "FORX0004");
        }
    }

    @Override
    public String getFlags() {
        return this.rawFlags;
    }
}

