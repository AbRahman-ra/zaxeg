/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import net.sf.saxon.regex.BMPString;
import net.sf.saxon.regex.GeneralUnicodeString;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.tree.tiny.AppendableCharSequence;
import net.sf.saxon.tree.tiny.CharSlice;
import net.sf.saxon.tree.tiny.CompressedWhitespace;
import net.sf.saxon.tree.util.CharSequenceConsumer;

public final class FastStringBuffer
implements AppendableCharSequence,
CharSequenceConsumer {
    public static final int C16 = 16;
    public static final int C64 = 64;
    public static final int C256 = 256;
    public static final int C1024 = 1024;
    private char[] array;
    private int used = 0;

    public FastStringBuffer(int initialSize) {
        this.array = new char[initialSize];
    }

    public FastStringBuffer(CharSequence cs) {
        this.array = new char[cs.length()];
        this.cat(cs);
    }

    public FastStringBuffer cat(String s) {
        int len = s.length();
        this.ensureCapacity(len);
        s.getChars(0, len, this.array, this.used);
        this.used += len;
        return this;
    }

    public void append(String s) {
        this.cat(s);
    }

    public void append(CharSlice s) {
        int len = s.length();
        this.ensureCapacity(len);
        s.copyTo(this.array, this.used);
        this.used += len;
    }

    public void append(FastStringBuffer s) {
        int len = s.length();
        this.ensureCapacity(len);
        s.getChars(0, len, this.array, this.used);
        this.used += len;
    }

    public void append(StringBuffer s) {
        int len = s.length();
        this.ensureCapacity(len);
        s.getChars(0, len, this.array, this.used);
        this.used += len;
    }

    @Override
    public FastStringBuffer cat(CharSequence s) {
        int len = s.length();
        this.ensureCapacity(len);
        if (s instanceof CharSlice) {
            ((CharSlice)s).copyTo(this.array, this.used);
        } else if (s instanceof String) {
            ((String)s).getChars(0, len, this.array, this.used);
        } else if (s instanceof FastStringBuffer) {
            ((FastStringBuffer)s).getChars(0, len, this.array, this.used);
        } else {
            if (s instanceof CompressedWhitespace) {
                ((CompressedWhitespace)s).uncompress(this);
                return this;
            }
            if (s instanceof BMPString) {
                this.cat(((BMPString)s).getCharSequence());
                return this;
            }
            if (s instanceof GeneralUnicodeString) {
                for (int i = 0; i < ((GeneralUnicodeString)s).uLength(); ++i) {
                    this.appendWideChar(((GeneralUnicodeString)s).uCharAt(i));
                }
                return this;
            }
            s.toString().getChars(0, len, this.array, this.used);
        }
        this.used += len;
        return this;
    }

    public void append(CharSequence s) {
        this.cat(s);
    }

    public void append(char[] srcArray, int start, int length) {
        this.ensureCapacity(length);
        System.arraycopy(srcArray, start, this.array, this.used, length);
        this.used += length;
    }

    public void append(char[] srcArray) {
        int length = srcArray.length;
        this.ensureCapacity(length);
        System.arraycopy(srcArray, 0, this.array, this.used, length);
        this.used += length;
    }

    @Override
    public FastStringBuffer cat(char ch) {
        this.ensureCapacity(1);
        this.array[this.used++] = ch;
        return this;
    }

    public void appendWideChar(int ch) {
        if (ch > 65535) {
            this.cat(UTF16CharacterSet.highSurrogate(ch));
            this.cat(UTF16CharacterSet.lowSurrogate(ch));
        } else {
            this.cat((char)ch);
        }
    }

    public void append(UnicodeString str) {
        if (str instanceof BMPString) {
            this.cat(((BMPString)str).getCharSequence());
        } else {
            for (int i = 0; i < str.uLength(); ++i) {
                this.appendWideChar(str.uCharAt(i));
            }
        }
    }

    public void prependWideChar(int ch) {
        if (ch > 65535) {
            this.prepend(UTF16CharacterSet.lowSurrogate(ch));
            this.prepend(UTF16CharacterSet.highSurrogate(ch));
        } else {
            this.prepend((char)ch);
        }
    }

    @Override
    public int length() {
        return this.used;
    }

    public boolean isEmpty() {
        return this.used == 0;
    }

    @Override
    public char charAt(int index) {
        if (index >= this.used) {
            throw new IndexOutOfBoundsException("" + index);
        }
        return this.array[index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new CharSlice(this.array, start, end - start);
    }

    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        if (srcBegin < 0) {
            throw new StringIndexOutOfBoundsException(srcBegin);
        }
        if (srcEnd > this.used) {
            throw new StringIndexOutOfBoundsException(srcEnd);
        }
        if (srcBegin > srcEnd) {
            throw new StringIndexOutOfBoundsException(srcEnd - srcBegin);
        }
        System.arraycopy(this.array, srcBegin, dst, dstBegin, srcEnd - srcBegin);
    }

    public int indexOf(char ch) {
        for (int i = 0; i < this.used; ++i) {
            if (this.array[i] != ch) continue;
            return i;
        }
        return -1;
    }

    @Override
    public String toString() {
        this.condense();
        return new String(this.array, 0, this.used);
    }

    public boolean equals(Object other) {
        return other instanceof CharSequence && this.toString().equals(other.toString());
    }

    public int hashCode() {
        int h = 0;
        for (int i = 0; i < this.used; ++i) {
            h = 31 * h + this.array[i];
        }
        return h;
    }

    public char[] toCharArray() {
        if (this.used == this.array.length) {
            return this.array;
        }
        char[] chars = new char[this.used];
        System.arraycopy(this.array, 0, chars, 0, this.used);
        return chars;
    }

    public void setCharAt(int index, char ch) {
        if (index < 0 || index > this.used) {
            throw new IndexOutOfBoundsException("" + index);
        }
        this.array[index] = ch;
    }

    public void insert(int index, char ch) {
        if (index < 0 || index > this.used) {
            throw new IndexOutOfBoundsException("" + index);
        }
        this.ensureCapacity(1);
        System.arraycopy(this.array, index, this.array, index + 1, this.used - index);
        ++this.used;
        this.array[index] = ch;
    }

    public void insertWideChar(int index, int ch) {
        if (index < 0 || index > this.used) {
            throw new IndexOutOfBoundsException("" + index);
        }
        if (ch > 65535) {
            this.ensureCapacity(2);
            System.arraycopy(this.array, index, this.array, index + 2, this.used - index);
            this.used += 2;
            this.array[index] = UTF16CharacterSet.highSurrogate(ch);
            this.array[index + 1] = UTF16CharacterSet.lowSurrogate(ch);
        } else {
            this.ensureCapacity(1);
            System.arraycopy(this.array, index, this.array, index + 1, this.used - index);
            ++this.used;
            this.array[index] = (char)ch;
        }
    }

    public void removeCharAt(int index) {
        if (index < 0 || index > this.used) {
            throw new IndexOutOfBoundsException("" + index);
        }
        --this.used;
        System.arraycopy(this.array, index + 1, this.array, index, this.used - index);
    }

    public void prepend(char ch) {
        char[] a2 = new char[this.array.length + 1];
        System.arraycopy(this.array, 0, a2, 1, this.used);
        a2[0] = ch;
        ++this.used;
        this.array = a2;
    }

    public void prepend(CharSequence str) {
        int len = str.length();
        char[] a2 = new char[this.array.length + len];
        System.arraycopy(this.array, 0, a2, len, this.used);
        for (int i = 0; i < len; ++i) {
            a2[i] = str.charAt(i);
        }
        this.used += len;
        this.array = a2;
    }

    public void prependRepeated(char ch, int repeat) {
        if (repeat > 0) {
            char[] a2 = new char[this.array.length + repeat];
            System.arraycopy(this.array, 0, a2, repeat, this.used);
            Arrays.fill(a2, 0, repeat, ch);
            this.used += repeat;
            this.array = a2;
        }
    }

    @Override
    public void setLength(int length) {
        if (length < 0 || length > this.used) {
            return;
        }
        this.used = length;
    }

    public void ensureCapacity(int extra) {
        if (this.used + extra > this.array.length) {
            int newlen = this.array.length * 2;
            if (newlen < this.used + extra) {
                newlen = this.used + extra * 2;
            }
            this.array = Arrays.copyOf(this.array, newlen);
        }
    }

    public FastStringBuffer condense() {
        if (this.array.length - this.used > 256 || this.array.length > this.used * 2 && this.array.length - this.used > 20) {
            this.array = Arrays.copyOf(this.array, this.used);
        }
        return this;
    }

    public void write(Writer writer) throws IOException {
        writer.write(this.array, 0, this.used);
    }

    public static String diagnosticPrint(CharSequence in) {
        FastStringBuffer buff = new FastStringBuffer(in.length() * 2);
        for (int i = 0; i < in.length(); ++i) {
            char c = in.charAt(i);
            if (c > ' ' && c < '\u007f') {
                buff.cat(c);
                continue;
            }
            buff.append("\\u");
            for (int d = 12; d >= 0; d -= 4) {
                buff.cat("0123456789ABCDEF".charAt(c >> d & 0xF));
            }
        }
        return buff.toString();
    }
}

