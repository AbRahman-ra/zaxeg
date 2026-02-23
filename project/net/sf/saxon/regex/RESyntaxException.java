/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

public class RESyntaxException
extends RuntimeException {
    public RESyntaxException(String s) {
        super(s);
    }

    public RESyntaxException(String s, int offset) {
        super("Syntax error at char " + offset + " in regular expression: " + s);
    }
}

