/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.value.StringValue;

public class JTokenIterator
implements AtomicIterator<StringValue> {
    private CharSequence input;
    private Pattern pattern;
    private Matcher matcher;
    private CharSequence current;
    private int prevEnd = 0;

    public JTokenIterator(CharSequence input, Pattern pattern) {
        this.input = input;
        this.pattern = pattern;
        this.matcher = pattern.matcher(input);
        this.prevEnd = 0;
    }

    @Override
    public StringValue next() {
        if (this.prevEnd < 0) {
            this.current = null;
            return null;
        }
        if (this.matcher.find()) {
            this.current = this.input.subSequence(this.prevEnd, this.matcher.start());
            this.prevEnd = this.matcher.end();
        } else {
            this.current = this.input.subSequence(this.prevEnd, this.input.length());
            this.prevEnd = -1;
        }
        return StringValue.makeStringValue(this.current);
    }
}

