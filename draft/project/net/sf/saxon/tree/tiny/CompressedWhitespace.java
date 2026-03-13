/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.io.IOException;
import java.io.Writer;
import net.sf.saxon.tree.util.FastStringBuffer;

public class CompressedWhitespace
implements CharSequence {
    private static char[] WHITE_CHARS = new char[]{'\t', '\n', '\r', ' '};
    private static int[] CODES = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1, -1, -1, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 3};
    private long value;

    public CompressedWhitespace(long compressedValue) {
        this.value = compressedValue;
    }

    public static CharSequence compress(CharSequence in) {
        int inlen = in.length();
        if (inlen == 0) {
            return in;
        }
        int runlength = 1;
        int outlength = 0;
        for (int i = 0; i < inlen; ++i) {
            char c = in.charAt(i);
            if (c <= ' ' && CODES[c] >= 0) {
                if (i == inlen - 1 || c != in.charAt(i + 1) || runlength == 63) {
                    runlength = 1;
                    if (++outlength <= 8) continue;
                    return in;
                }
                ++runlength;
                continue;
            }
            return in;
        }
        int ix = 0;
        runlength = 1;
        int[] out = new int[outlength];
        for (int i = 0; i < inlen; ++i) {
            char c = in.charAt(i);
            if (i == inlen - 1 || c != in.charAt(i + 1) || runlength == 63) {
                out[ix++] = CODES[c] << 6 | runlength;
                runlength = 1;
                continue;
            }
            ++runlength;
        }
        long value = 0L;
        for (int i = 0; i < outlength; ++i) {
            value = value << 8 | (long)out[i];
        }
        return new CompressedWhitespace(value <<= 8 * (8 - outlength));
    }

    public FastStringBuffer uncompress(FastStringBuffer buffer) {
        if (buffer == null) {
            buffer = new FastStringBuffer(this.length());
        }
        CompressedWhitespace.uncompress(this.value, buffer);
        return buffer;
    }

    public static void uncompress(long value, FastStringBuffer buffer) {
        byte b;
        for (int s = 56; s >= 0 && (b = (byte)(value >>> s & 0xFFL)) != 0; s -= 8) {
            char c = WHITE_CHARS[b >>> 6 & 3];
            int len = b & 0x3F;
            buffer.ensureCapacity(len);
            for (int j = 0; j < len; ++j) {
                buffer.cat(c);
            }
        }
    }

    public long getCompressedValue() {
        return this.value;
    }

    @Override
    public int length() {
        int c;
        int count = 0;
        long val = this.value;
        for (int s = 56; s >= 0 && (c = (int)(val >>> s & 0x3FL)) != 0; s -= 8) {
            count += c;
        }
        return count;
    }

    @Override
    public char charAt(int index) {
        byte b;
        int count = 0;
        long val = this.value;
        for (int s = 56; s >= 0 && (b = (byte)(val >>> s & 0xFFL)) != 0; s -= 8) {
            if ((count += b & 0x3F) <= index) continue;
            return WHITE_CHARS[b >>> 6 & 3];
        }
        throw new IndexOutOfBoundsException(index + "");
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.uncompress(null).subSequence(start, end);
    }

    public boolean equals(Object obj) {
        if (obj instanceof CompressedWhitespace) {
            return this.value == ((CompressedWhitespace)obj).value;
        }
        return this.uncompress(null).equals(obj);
    }

    public int hashCode() {
        return this.uncompress(null).hashCode();
    }

    @Override
    public String toString() {
        return this.uncompress(null).toString();
    }

    public void write(Writer writer) throws IOException {
        byte b;
        long val = this.value;
        for (int s = 56; s >= 0 && (b = (byte)(val >>> s & 0xFFL)) != 0; s -= 8) {
            char c = WHITE_CHARS[b >>> 6 & 3];
            int len = b & 0x3F;
            for (int j = 0; j < len; ++j) {
                writer.write(c);
            }
        }
    }

    public void writeEscape(boolean[] specialChars, Writer writer) throws IOException {
        byte b;
        long val = this.value;
        for (int s = 56; s >= 0 && (b = (byte)(val >>> s & 0xFFL)) != 0; s -= 8) {
            char c = WHITE_CHARS[b >>> 6 & 3];
            int len = b & 0x3F;
            if (specialChars[c]) {
                String e = "";
                if (c == '\n') {
                    e = "&#xA;";
                } else if (c == '\r') {
                    e = "&#xD;";
                } else if (c == '\t') {
                    e = "&#x9;";
                }
                for (int j = 0; j < len; ++j) {
                    writer.write(e);
                }
                continue;
            }
            for (int j = 0; j < len; ++j) {
                writer.write(c);
            }
        }
    }
}

