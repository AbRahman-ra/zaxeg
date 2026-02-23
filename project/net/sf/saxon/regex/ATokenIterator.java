/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import net.sf.saxon.regex.REMatcher;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.value.StringValue;

public class ATokenIterator
implements AtomicIterator {
    private UnicodeString input;
    private REMatcher matcher;
    private UnicodeString current;
    private int prevEnd = 0;

    public ATokenIterator(UnicodeString input, REMatcher matcher) {
        this.input = input;
        this.matcher = matcher;
        this.prevEnd = 0;
    }

    @Override
    public StringValue next() {
        if (this.prevEnd < 0) {
            this.current = null;
            return null;
        }
        if (this.matcher.match(this.input, this.prevEnd)) {
            int start = this.matcher.getParenStart(0);
            this.current = this.input.uSubstring(this.prevEnd, start);
            this.prevEnd = this.matcher.getParenEnd(0);
        } else {
            this.current = this.input.uSubstring(this.prevEnd, this.input.uLength());
            this.prevEnd = -1;
        }
        return this.currentStringValue();
    }

    private StringValue currentStringValue() {
        return StringValue.makeStringValue(this.current);
    }
}

