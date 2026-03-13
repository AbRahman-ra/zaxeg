/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import net.sf.saxon.tree.tiny.AppendableCharSequence;
import net.sf.saxon.tree.tiny.CharSlice;
import net.sf.saxon.tree.tiny.CompressedWhitespace;
import net.sf.saxon.tree.util.FastStringBuffer;

public final class LargeStringBuffer
implements AppendableCharSequence {
    private static final int BITS = 16;
    private static final int SEGLEN = 65536;
    private static final int MASK = 65535;
    private char[][] data = new char[1][];
    private int length = 0;
    private int segmentsUsed = 0;

    private void addSegment(char[] seg) {
        int segs = this.data.length;
        if (this.segmentsUsed + 1 > segs) {
            if (this.segmentsUsed == 32768) {
                throw new IllegalStateException("Source document too large: more than 1G characters in text nodes");
            }
            this.data = (char[][])Arrays.copyOf(this.data, segs * 2);
        }
        this.data[this.segmentsUsed++] = seg;
    }

    @Override
    public LargeStringBuffer cat(CharSequence s) {
        int lastSegLen;
        int fullSegments;
        int firstSegLen;
        char[] firstSeg;
        if (s instanceof CompressedWhitespace) {
            FastStringBuffer fsb = new FastStringBuffer(64);
            ((CompressedWhitespace)s).uncompress(fsb);
            return this.cat(fsb);
        }
        int len = s.length();
        int firstSegOffset = this.length & 0xFFFF;
        if (firstSegOffset == 0) {
            firstSeg = new char[65536];
            this.addSegment(firstSeg);
        } else {
            firstSeg = this.data[this.length >> 16];
        }
        if (len <= 65536 - firstSegOffset) {
            firstSegLen = len;
            fullSegments = 0;
            lastSegLen = 0;
        } else {
            firstSegLen = 65536 - firstSegOffset;
            fullSegments = len - firstSegLen >> 16;
            lastSegLen = len - firstSegLen & 0xFFFF;
        }
        if (s instanceof CharSlice) {
            ((CharSlice)s).getChars(0, firstSegLen, firstSeg, firstSegOffset);
            int start = firstSegLen;
            for (int i = 0; i < fullSegments; ++i) {
                char[] seg = new char[65536];
                this.addSegment(seg);
                ((CharSlice)s).getChars(start, start + 65536, seg, 0);
                start += 65536;
            }
            if (lastSegLen > 0) {
                char[] seg = new char[65536];
                this.addSegment(seg);
                ((CharSlice)s).getChars(start, len, seg, 0);
            }
            this.length += len;
        } else if (s instanceof FastStringBuffer) {
            ((FastStringBuffer)s).getChars(0, firstSegLen, firstSeg, firstSegOffset);
            int start = firstSegLen;
            for (int i = 0; i < fullSegments; ++i) {
                char[] seg = new char[65536];
                this.addSegment(seg);
                ((FastStringBuffer)s).getChars(start, start + 65536, seg, 0);
                start += 65536;
            }
            if (lastSegLen > 0) {
                char[] seg = new char[65536];
                this.addSegment(seg);
                ((FastStringBuffer)s).getChars(start, len, seg, 0);
            }
            this.length += len;
        } else {
            if (!(s instanceof String)) {
                s = s.toString();
            }
            ((String)s).getChars(0, firstSegLen, firstSeg, firstSegOffset);
            int start = firstSegLen;
            for (int i = 0; i < fullSegments; ++i) {
                char[] seg = new char[65536];
                this.addSegment(seg);
                ((String)s).getChars(start, start + 65536, seg, 0);
                start += 65536;
            }
            if (lastSegLen > 0) {
                char[] seg = new char[65536];
                this.addSegment(seg);
                ((String)s).getChars(start, len, seg, 0);
            }
            this.length += len;
        }
        return this;
    }

    @Override
    public LargeStringBuffer cat(char c) {
        return this.cat("" + c);
    }

    @Override
    public int length() {
        return this.length;
    }

    @Override
    public void setLength(int length) {
        if (length < this.length) {
            int usedInLastSegment = length & 0xFFFF;
            this.length = length;
            this.segmentsUsed = length / 65536 + (usedInLastSegment == 0 ? 0 : 1);
        }
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || index >= this.length) {
            throw new IndexOutOfBoundsException(index + "");
        }
        return this.data[index >> 16][index & 0xFFFF];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        int firstSeg = start >> 16;
        int lastSeg = end - 1 >> 16;
        if (firstSeg == lastSeg) {
            try {
                return new CharSlice(this.data[firstSeg], start & 0xFFFF, end - start);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                throw e;
            }
        }
        FastStringBuffer fsb = new FastStringBuffer(end - start);
        int firstSegLen = 65536 - (start & 0xFFFF);
        fsb.append(this.data[firstSeg], start & 0xFFFF, firstSegLen);
        int doneTo = start + firstSegLen;
        while (true) {
            ++firstSeg;
            if (doneTo + 65536 >= end) break;
            fsb.append(this.data[firstSeg]);
            doneTo += 65536;
        }
        fsb.append(this.data[firstSeg], 0, end - doneTo);
        return fsb;
    }

    @Override
    public String toString() {
        return this.subSequence(0, this.length).toString();
    }

    public boolean equals(Object other) {
        return other instanceof CharSequence && this.toString().equals(other.toString());
    }

    public int hashCode() {
        int h = 0;
        for (char[] chars : this.data) {
            for (int i = 0; i < 65536; ++i) {
                h = 31 * h + chars[i];
            }
        }
        return h;
    }

    public String substring(int start, int end) {
        return this.subSequence(start, end).toString();
    }

    public void write(Writer writer) throws IOException {
        writer.write(this.toString());
    }
}

