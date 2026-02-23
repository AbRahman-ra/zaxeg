/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import java.util.function.Function;
import net.sf.saxon.regex.RegexIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;

public interface RegularExpression {
    public boolean matches(CharSequence var1);

    public boolean containsMatch(CharSequence var1);

    public AtomicIterator tokenize(CharSequence var1);

    public RegexIterator analyze(CharSequence var1);

    public CharSequence replace(CharSequence var1, CharSequence var2) throws XPathException;

    public CharSequence replaceWith(CharSequence var1, Function<CharSequence, CharSequence> var2) throws XPathException;

    public String getFlags();
}

