/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public final class UTF8Writer
extends Writer {
    private static final int MIN_BUF_LEN = 32;
    private static final int DEFAULT_BUF_LEN = 4000;
    static final int SURR1_FIRST = 55296;
    static final int SURR1_LAST = 56319;
    static final int SURR2_FIRST = 56320;
    static final int SURR2_LAST = 57343;
    protected OutputStream _out;
    protected byte[] _outBuffer;
    protected final int _outBufferLast;
    protected int _outPtr;
    int _surrogate = 0;

    public UTF8Writer(OutputStream out) {
        this(out, 4000);
    }

    public UTF8Writer(OutputStream out, int bufferLength) {
        if (bufferLength < 32) {
            bufferLength = 32;
        }
        this._out = out;
        this._outBuffer = new byte[bufferLength];
        this._outBufferLast = bufferLength - 4;
        this._outPtr = 0;
    }

    @Override
    public void close() throws IOException {
        if (this._out != null) {
            this._flushBuffer();
            this._outBuffer = null;
            this._out.close();
            this._out = null;
            if (this._surrogate != 0) {
                int code = this._surrogate;
                this._surrogate = 0;
                this.throwIllegal(code);
            }
        }
    }

    @Override
    public void flush() throws IOException {
        this._flushBuffer();
        this._out.flush();
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        this.write(cbuf, 0, cbuf.length);
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (len < 2) {
            if (len == 1) {
                this.write(cbuf[off]);
            }
            return;
        }
        if (this._surrogate > 0) {
            second = cbuf[off++];
            --len;
            this.write(this._convertSurrogate(second));
        }
        outPtr = this._outPtr;
        outBuf = this._outBuffer;
        outBufLast = this._outBufferLast;
        len += off;
        block0: while (off < len) {
            if (outPtr >= outBufLast) {
                this._out.write(outBuf, 0, outPtr);
                outPtr = 0;
            }
            if ((c = cbuf[off++]) >= 128) ** GOTO lbl28
            outBuf[outPtr++] = (byte)c;
            maxInCount = len - off;
            maxOutCount = outBufLast - outPtr;
            if (maxInCount > maxOutCount) {
                maxInCount = maxOutCount;
            }
            maxInCount += off;
            while (off < maxInCount) {
                if ((c = cbuf[off++]) < 128) {
                    outBuf[outPtr++] = (byte)c;
                    continue;
                }
lbl28:
                // 3 sources

                if (c < 2048) {
                    outBuf[outPtr++] = (byte)(192 | c >> 6);
                    outBuf[outPtr++] = (byte)(128 | c & 63);
                    continue block0;
                }
                if (c < 55296 || c > 57343) {
                    outBuf[outPtr++] = (byte)(224 | c >> 12);
                    outBuf[outPtr++] = (byte)(128 | c >> 6 & 63);
                    outBuf[outPtr++] = (byte)(128 | c & 63);
                    continue block0;
                }
                if (c > 56319) {
                    this._outPtr = outPtr;
                    this.throwIllegal(c);
                }
                this._surrogate = c;
                if (off >= len) break block0;
                if ((c = this._convertSurrogate(cbuf[off++])) > 0x10FFFF) {
                    this._outPtr = outPtr;
                    this.throwIllegal(c);
                }
                outBuf[outPtr++] = (byte)(240 | c >> 18);
                outBuf[outPtr++] = (byte)(128 | c >> 12 & 63);
                outBuf[outPtr++] = (byte)(128 | c >> 6 & 63);
                outBuf[outPtr++] = (byte)(128 | c & 63);
                continue block0;
            }
        }
        this._outPtr = outPtr;
    }

    @Override
    public void write(int c) throws IOException {
        if (this._surrogate > 0) {
            c = this._convertSurrogate(c);
        } else if (c >= 55296 && c <= 57343) {
            if (c > 56319) {
                this.throwIllegal(c);
            }
            this._surrogate = c;
            return;
        }
        if (this._outPtr >= this._outBufferLast) {
            this._flushBuffer();
        }
        if (c < 128) {
            this._outBuffer[this._outPtr++] = (byte)c;
        } else {
            int ptr = this._outPtr;
            if (c < 2048) {
                this._outBuffer[ptr++] = (byte)(0xC0 | c >> 6);
                this._outBuffer[ptr++] = (byte)(0x80 | c & 0x3F);
            } else if (c <= 65535) {
                this._outBuffer[ptr++] = (byte)(0xE0 | c >> 12);
                this._outBuffer[ptr++] = (byte)(0x80 | c >> 6 & 0x3F);
                this._outBuffer[ptr++] = (byte)(0x80 | c & 0x3F);
            } else {
                if (c > 0x10FFFF) {
                    this.throwIllegal(c);
                }
                this._outBuffer[ptr++] = (byte)(0xF0 | c >> 18);
                this._outBuffer[ptr++] = (byte)(0x80 | c >> 12 & 0x3F);
                this._outBuffer[ptr++] = (byte)(0x80 | c >> 6 & 0x3F);
                this._outBuffer[ptr++] = (byte)(0x80 | c & 0x3F);
            }
            this._outPtr = ptr;
        }
    }

    @Override
    public void write(String str) throws IOException {
        this.write(str, 0, str.length());
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void write(String str, int off, int len) throws IOException {
        if (len < 2) {
            if (len == 1) {
                this.write(str.charAt(off));
            }
            return;
        }
        if (this._surrogate > 0) {
            second = str.charAt(off++);
            --len;
            this.write(this._convertSurrogate(second));
        }
        outPtr = this._outPtr;
        outBuf = this._outBuffer;
        outBufLast = this._outBufferLast;
        len += off;
        block0: while (off < len) {
            if (outPtr >= outBufLast) {
                this._out.write(outBuf, 0, outPtr);
                outPtr = 0;
            }
            if ((c = str.charAt(off++)) >= 128) ** GOTO lbl28
            outBuf[outPtr++] = (byte)c;
            maxInCount = len - off;
            maxOutCount = outBufLast - outPtr;
            if (maxInCount > maxOutCount) {
                maxInCount = maxOutCount;
            }
            maxInCount += off;
            while (off < maxInCount) {
                if ((c = (int)str.charAt(off++)) < 128) {
                    outBuf[outPtr++] = (byte)c;
                    continue;
                }
lbl28:
                // 3 sources

                if (c < 2048) {
                    outBuf[outPtr++] = (byte)(192 | c >> 6);
                    outBuf[outPtr++] = (byte)(128 | c & 63);
                    continue block0;
                }
                if (c < 55296 || c > 57343) {
                    outBuf[outPtr++] = (byte)(224 | c >> 12);
                    outBuf[outPtr++] = (byte)(128 | c >> 6 & 63);
                    outBuf[outPtr++] = (byte)(128 | c & 63);
                    continue block0;
                }
                if (c > 56319) {
                    this._outPtr = outPtr;
                    this.throwIllegal(c);
                }
                this._surrogate = c;
                if (off >= len) break block0;
                if ((c = this._convertSurrogate(str.charAt(off++))) > 0x10FFFF) {
                    this._outPtr = outPtr;
                    this.throwIllegal(c);
                }
                outBuf[outPtr++] = (byte)(240 | c >> 18);
                outBuf[outPtr++] = (byte)(128 | c >> 12 & 63);
                outBuf[outPtr++] = (byte)(128 | c >> 6 & 63);
                outBuf[outPtr++] = (byte)(128 | c & 63);
                continue block0;
            }
        }
        this._outPtr = outPtr;
    }

    private void _flushBuffer() throws IOException {
        if (this._outPtr > 0 && this._outBuffer != null) {
            this._out.write(this._outBuffer, 0, this._outPtr);
            this._outPtr = 0;
        }
    }

    private final int _convertSurrogate(int secondPart) throws IOException {
        int firstPart = this._surrogate;
        this._surrogate = 0;
        if (secondPart < 56320 || secondPart > 57343) {
            throw new IOException("Broken surrogate pair: first char 0x" + Integer.toHexString(firstPart) + ", second 0x" + Integer.toHexString(secondPart) + "; illegal combination");
        }
        return 65536 + (firstPart - 55296 << 10) + (secondPart - 56320);
    }

    private void throwIllegal(int code) throws IOException {
        if (code > 0x10FFFF) {
            throw new IOException("Illegal character point (0x" + Integer.toHexString(code) + ") to output; max is 0x10FFFF as per RFC 3629");
        }
        if (code >= 55296) {
            if (code <= 56319) {
                throw new IOException("Unmatched first part of surrogate pair (0x" + Integer.toHexString(code) + ")");
            }
            throw new IOException("Unmatched second part of surrogate pair (0x" + Integer.toHexString(code) + ")");
        }
        throw new IOException("Illegal character point (0x" + Integer.toHexString(code) + ") to output");
    }
}

