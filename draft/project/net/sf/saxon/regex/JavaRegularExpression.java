/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.sf.saxon.regex.JRegexIterator;
import net.sf.saxon.regex.JTokenIterator;
import net.sf.saxon.regex.RegexIterator;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.value.StringValue;

public class JavaRegularExpression
implements RegularExpression {
    Pattern pattern;
    String javaRegex;
    int flagBits;

    public JavaRegularExpression(CharSequence javaRegex, String flags) throws XPathException {
        this.flagBits = JavaRegularExpression.setFlags(flags);
        this.javaRegex = javaRegex.toString();
        try {
            this.pattern = Pattern.compile(this.javaRegex, this.flagBits & 0xFFFFFFFB);
        } catch (PatternSyntaxException e) {
            throw new XPathException("Incorrect syntax for Java regular expression", e);
        }
    }

    public String getJavaRegularExpression() {
        return this.javaRegex;
    }

    public int getFlagBits() {
        return this.flagBits;
    }

    @Override
    public RegexIterator analyze(CharSequence input) {
        return new JRegexIterator(input.toString(), this.pattern);
    }

    @Override
    public boolean containsMatch(CharSequence input) {
        return this.pattern.matcher(input).find();
    }

    @Override
    public boolean matches(CharSequence input) {
        return this.pattern.matcher(input).matches();
    }

    @Override
    public CharSequence replace(CharSequence input, CharSequence replacement) throws XPathException {
        Matcher matcher = this.pattern.matcher(input);
        try {
            return matcher.replaceAll(replacement.toString());
        } catch (IndexOutOfBoundsException e) {
            throw new XPathException(e.getMessage(), "FORX0004");
        }
    }

    @Override
    public CharSequence replaceWith(CharSequence input, Function<CharSequence, CharSequence> replacement) throws XPathException {
        throw new XPathException("saxon:replace-with() is not supported with the Java regex engine");
    }

    @Override
    public AtomicIterator<StringValue> tokenize(CharSequence input) {
        if (input.length() == 0) {
            return EmptyIterator.ofAtomic();
        }
        return new JTokenIterator(input, this.pattern);
    }

    public static int setFlags(CharSequence inFlags) throws XPathException {
        int flags = 1;
        block10: for (int i = 0; i < inFlags.length(); ++i) {
            char c = inFlags.charAt(i);
            switch (c) {
                case 'd': {
                    flags |= 1;
                    continue block10;
                }
                case 'm': {
                    flags |= 8;
                    continue block10;
                }
                case 'i': {
                    flags |= 2;
                    continue block10;
                }
                case 's': {
                    flags |= 0x20;
                    continue block10;
                }
                case 'x': {
                    flags |= 4;
                    continue block10;
                }
                case 'u': {
                    flags |= 0x40;
                    continue block10;
                }
                case 'q': {
                    flags |= 0x10;
                    continue block10;
                }
                case 'c': {
                    flags |= 0x80;
                    continue block10;
                }
                default: {
                    XPathException err = new XPathException("Invalid character '" + c + "' in regular expression flags");
                    err.setErrorCode("FORX0001");
                    throw err;
                }
            }
        }
        return flags;
    }

    @Override
    public String getFlags() {
        String flags = "";
        if ((this.flagBits & 1) != 0) {
            flags = flags + 'd';
        }
        if ((this.flagBits & 8) != 0) {
            flags = flags + 'm';
        }
        if ((this.flagBits & 2) != 0) {
            flags = flags + 'i';
        }
        if ((this.flagBits & 0x20) != 0) {
            flags = flags + 's';
        }
        if ((this.flagBits & 4) != 0) {
            flags = flags + 'x';
        }
        if ((this.flagBits & 0x40) != 0) {
            flags = flags + 'u';
        }
        if ((this.flagBits & 0x10) != 0) {
            flags = flags + 'q';
        }
        if ((this.flagBits & 0x80) != 0) {
            flags = flags + 'c';
        }
        return flags;
    }
}

