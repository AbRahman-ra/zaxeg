/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trans.XPathException;

public interface PatternParser {
    public Pattern parsePattern(String var1, StaticContext var2) throws XPathException;
}

