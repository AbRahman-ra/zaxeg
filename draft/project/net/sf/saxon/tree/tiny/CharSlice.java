/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.io.IOException;
import java.io.Writer;
import net.sf.saxon.tree.util.FastStringBuffer;

public final class CharSlice
implements CharSequence {
    private char[] array;
    private int offset;
    private int count;

    public CharSlice(char[] array) {
        this.array = array;
        this.offset = 0;
        this.count = array.length;
    }

    public CharSlice(char[] array, int start, int length) {
        this.array = array;
        this.offset = start;
        this.count = length;
        if (start + length > array.length) {
            throw new IndexOutOfBoundsException("start(" + start + ") + length(" + length + ") > size(" + array.length + ')');
        }
    }

    @Override
    public int length() {
        return this.count;
    }

    public void setLength(int length) {
        this.count = length;
    }

    @Override
    public char charAt(int index) {
        return this.array[this.offset + index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new CharSlice(this.array, this.offset + start, end - start);
    }

    @Override
    public String toString() {
        return new String(this.array, this.offset, this.count);
    }

    public boolean equals(Object other) {
        if (other instanceof CharSlice) {
            CharSlice cs2 = (CharSlice)other;
            if (this.count != cs2.count) {
                return false;
            }
            int limit = this.offset + this.count;
            int j = this.offset;
            int k = cs2.offset;
            while (j < limit) {
                if (this.array[j++] == cs2.array[k++]) continue;
                return false;
            }
            return true;
        }
        if (other instanceof CharSequence) {
            return this.count == ((CharSequence)other).length() && this.toString().equals(other.toString());
        }
        return false;
    }

    public int hashCode() {
        int end = this.offset + this.count;
        int h = 0;
        for (int i = this.offset; i < end; ++i) {
            h = 31 * h + this.array[i];
        }
        return h;
    }

    public int indexOf(char c) {
        int end = this.offset + this.count;
        for (int i = this.offset; i < end; ++i) {
            if (this.array[i] != c) continue;
            return i - this.offset;
        }
        return -1;
    }

    public String substring(int start, int end) {
        return new String(this.array, this.offset + start, end - start);
    }

    public void copyTo(char[] destination, int destOffset) {
        System.arraycopy(this.array, this.offset, destination, destOffset, this.count);
    }

    public void getChars(int start, int end, char[] destination, int destOffset) {
        System.arraycopy(this.array, this.offset + start, destination, destOffset, end - start);
    }

    public char[] toCharArray() {
        char[] chars = new char[this.count];
        System.arraycopy(this.array, this.offset, chars, 0, this.count);
        return chars;
    }

    public static char[] toCharArray(CharSequence in) {
        if (in instanceof String) {
            return ((String)in).toCharArray();
        }
        if (in instanceof CharSlice) {
            return ((CharSlice)in).toCharArray();
        }
        if (in instanceof FastStringBuffer) {
            return ((FastStringBuffer)in).toCharArray();
        }
        return in.toString().toCharArray();
    }

    public void write(Writer writer) throws IOException {
        writer.write(this.array, this.offset, this.count);
    }
}

