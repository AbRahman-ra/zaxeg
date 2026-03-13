/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import net.sf.saxon.regex.UnicodeString;

public final class BMPString
extends UnicodeString {
    private final CharSequence src;

    public BMPString(CharSequence src) {
        this.src = src;
    }

    @Override
    public UnicodeString uSubstring(int beginIndex, int endIndex) {
        return new BMPString(this.src.subSequence(beginIndex, endIndex));
    }

    @Override
    public int uCharAt(int pos) {
        return this.src.charAt(pos);
    }

    @Override
    public int uIndexOf(int search, int pos) {
        if (search > 65535) {
            return -1;
        }
        for (int i = pos; i < this.src.length(); ++i) {
            if (this.src.charAt(i) != (char)search) continue;
            return i;
        }
        return -1;
    }

    @Override
    public int uLength() {
        return this.src.length();
    }

    @Override
    public boolean isEnd(int pos) {
        return pos >= this.src.length();
    }

    @Override
    public String toString() {
        return this.src.toString();
    }

    public CharSequence getCharSequence() {
        return this.src;
    }

    @Override
    public int length() {
        return this.src.length();
    }

    @Override
    public char charAt(int index) {
        return this.src.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.src.subSequence(start, end);
    }
}

