/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import net.sf.saxon.regex.RESyntaxException;

public class REFlags {
    private boolean caseIndependent;
    private boolean multiLine;
    private boolean singleLine;
    private boolean allowWhitespace;
    private boolean literal;
    private boolean xpath20;
    private boolean xpath30;
    private boolean xsd11;
    private boolean debug;
    private boolean allowUnknownBlockNames = false;

    public REFlags(String flags, String language) {
        char c;
        int i;
        if (!language.equals("XSD10") && language.contains("XSD11")) {
            this.allowUnknownBlockNames = !language.contains("XP");
            this.xsd11 = true;
        }
        if (language.contains("XP20")) {
            this.xpath20 = true;
        } else if (language.contains("XP30")) {
            this.xpath20 = true;
            this.xpath30 = true;
        }
        int semi = flags.indexOf(59);
        int endStd = semi >= 0 ? semi : flags.length();
        block12: for (i = 0; i < endStd; ++i) {
            c = flags.charAt(i);
            switch (c) {
                case 'i': {
                    this.caseIndependent = true;
                    continue block12;
                }
                case 'm': {
                    this.multiLine = true;
                    continue block12;
                }
                case 's': {
                    this.singleLine = true;
                    continue block12;
                }
                case 'q': {
                    this.literal = true;
                    if (this.xpath30) continue block12;
                    throw new RESyntaxException("'q' flag requires XPath 3.0 to be enabled");
                }
                case 'x': {
                    this.allowWhitespace = true;
                    continue block12;
                }
                default: {
                    throw new RESyntaxException("Unrecognized flag '" + c + "'");
                }
            }
        }
        block13: for (i = semi + 1; i < flags.length(); ++i) {
            c = flags.charAt(i);
            switch (c) {
                case 'g': {
                    this.debug = true;
                    continue block13;
                }
                case 'k': {
                    this.allowUnknownBlockNames = true;
                    continue block13;
                }
                case 'K': {
                    this.allowUnknownBlockNames = false;
                }
            }
        }
    }

    public boolean isCaseIndependent() {
        return this.caseIndependent;
    }

    public boolean isMultiLine() {
        return this.multiLine;
    }

    public boolean isSingleLine() {
        return this.singleLine;
    }

    public boolean isAllowWhitespace() {
        return this.allowWhitespace;
    }

    public boolean isLiteral() {
        return this.literal;
    }

    public boolean isAllowsXPath20Extensions() {
        return this.xpath20;
    }

    public boolean isAllowsXPath30Extensions() {
        return this.xpath30;
    }

    public boolean isAllowsXSD11Syntax() {
        return this.xsd11;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public void setAllowUnknownBlockNames(boolean allow) {
        this.allowUnknownBlockNames = allow;
    }

    public boolean isAllowUnknownBlockNames() {
        return this.allowUnknownBlockNames;
    }
}

