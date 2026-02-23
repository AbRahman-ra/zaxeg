/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.value.StringValue;

public final class GeneralUnicodeString
extends UnicodeString {
    private int[] chars;
    private int start;
    private int end;
    private CharSequence charSequence;

    public GeneralUnicodeString(CharSequence in) {
        this.chars = StringValue.expand(in);
        this.start = 0;
        this.end = this.chars.length;
        this.charSequence = in;
    }

    GeneralUnicodeString(int[] chars, int start, int end) {
        this.chars = chars;
        this.start = start;
        this.end = end;
    }

    @Override
    public UnicodeString uSubstring(int beginIndex, int endIndex) {
        if (endIndex > this.chars.length) {
            throw new IndexOutOfBoundsException("endIndex=" + endIndex + "; sequence size=" + this.chars.length);
        }
        if (beginIndex < 0 || beginIndex > endIndex) {
            throw new IndexOutOfBoundsException("beginIndex=" + beginIndex + "; endIndex=" + endIndex);
        }
        return new GeneralUnicodeString(this.chars, this.start + beginIndex, this.start + endIndex);
    }

    @Override
    public int uCharAt(int pos) {
        return this.chars[this.start + pos];
    }

    @Override
    public int uIndexOf(int search, int pos) {
        for (int i = pos; i < this.uLength(); ++i) {
            if (this.chars[this.start + i] != search) continue;
            return i;
        }
        return -1;
    }

    @Override
    public int uLength() {
        return this.end - this.start;
    }

    @Override
    public boolean isEnd(int pos) {
        return pos >= this.end - this.start;
    }

    @Override
    public String toString() {
        this.obtainCharSequence();
        this.charSequence = this.charSequence.toString();
        return (String)this.charSequence;
    }

    private CharSequence obtainCharSequence() {
        if (this.charSequence == null) {
            int[] c = this.chars;
            if (this.start != 0) {
                c = new int[this.end - this.start];
                System.arraycopy(this.chars, this.start, c, 0, this.end - this.start);
            }
            this.charSequence = StringValue.contract(c, this.end - this.start);
        }
        return this.charSequence;
    }

    @Override
    public int length() {
        return this.obtainCharSequence().length();
    }

    @Override
    public char charAt(int index) {
        return this.obtainCharSequence().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.obtainCharSequence().subSequence(start, end);
    }
}

