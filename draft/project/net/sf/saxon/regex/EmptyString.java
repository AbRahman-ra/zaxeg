/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import net.sf.saxon.regex.UnicodeString;

public final class EmptyString
extends UnicodeString {
    public static final EmptyString THE_INSTANCE = new EmptyString();

    private EmptyString() {
    }

    @Override
    public EmptyString uSubstring(int beginIndex, int endIndex) {
        if (beginIndex == 0 && endIndex == 0) {
            return this;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int uCharAt(int pos) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int uIndexOf(int search, int pos) {
        return -1;
    }

    @Override
    public int uLength() {
        return 0;
    }

    @Override
    public boolean isEnd(int pos) {
        return pos >= 0;
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public char charAt(int index) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (start == 0 && end == 0) {
            return "";
        }
        throw new IndexOutOfBoundsException();
    }
}

