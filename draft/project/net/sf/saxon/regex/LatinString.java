/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import net.sf.saxon.regex.UnicodeString;

public final class LatinString
extends UnicodeString {
    private byte[] chars;
    public static final LatinString SINGLE_SPACE = new LatinString(new byte[]{32});

    public LatinString(CharSequence src) {
        int len = src.length();
        this.chars = new byte[len];
        for (int i = 0; i < len; ++i) {
            this.chars[i] = (byte)(src.charAt(i) & 0xFF);
        }
    }

    private LatinString(byte[] chars) {
        this.chars = chars;
    }

    @Override
    public LatinString uSubstring(int beginIndex, int endIndex) {
        byte[] s = new byte[endIndex - beginIndex];
        System.arraycopy(this.chars, beginIndex, s, 0, endIndex - beginIndex);
        return new LatinString(s);
    }

    @Override
    public int uCharAt(int pos) {
        return this.chars[pos] & 0xFF;
    }

    @Override
    public int uIndexOf(int search, int pos) {
        if (search > 255) {
            return -1;
        }
        for (int i = pos; i < this.chars.length; ++i) {
            if ((this.chars[i] & 0xFF) != search) continue;
            return i;
        }
        return -1;
    }

    @Override
    public int uLength() {
        return this.chars.length;
    }

    @Override
    public boolean isEnd(int pos) {
        return pos >= this.chars.length;
    }

    @Override
    public String toString() {
        char[] expanded = new char[this.chars.length];
        for (int i = 0; i < this.chars.length; ++i) {
            expanded[i] = (char)(this.chars[i] & 0xFF);
        }
        return new String(expanded);
    }

    @Override
    public int length() {
        return this.chars.length;
    }

    @Override
    public char charAt(int index) {
        return (char)(this.chars[index] & 0xFF);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.uSubstring(start, end);
    }
}

