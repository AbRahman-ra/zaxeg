/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.LimitedInputStream;
import org.bouncycastle.util.io.Streams;

class DefiniteLengthInputStream
extends LimitedInputStream {
    private static final byte[] EMPTY_BYTES = new byte[0];
    private final int _originalLength;
    private int _remaining;

    DefiniteLengthInputStream(InputStream inputStream, int n, int n2) {
        super(inputStream, n2);
        if (n < 0) {
            throw new IllegalArgumentException("negative lengths not allowed");
        }
        this._originalLength = n;
        this._remaining = n;
        if (n == 0) {
            this.setParentEofDetect(true);
        }
    }

    int getRemaining() {
        return this._remaining;
    }

    public int read() throws IOException {
        if (this._remaining == 0) {
            return -1;
        }
        int n = this._in.read();
        if (n < 0) {
            throw new EOFException("DEF length " + this._originalLength + " object truncated by " + this._remaining);
        }
        if (--this._remaining == 0) {
            this.setParentEofDetect(true);
        }
        return n;
    }

    public int read(byte[] byArray, int n, int n2) throws IOException {
        if (this._remaining == 0) {
            return -1;
        }
        int n3 = Math.min(n2, this._remaining);
        int n4 = this._in.read(byArray, n, n3);
        if (n4 < 0) {
            throw new EOFException("DEF length " + this._originalLength + " object truncated by " + this._remaining);
        }
        if ((this._remaining -= n4) == 0) {
            this.setParentEofDetect(true);
        }
        return n4;
    }

    void readAllIntoByteArray(byte[] byArray) throws IOException {
        if (this._remaining != byArray.length) {
            throw new IllegalArgumentException("buffer length not right for data");
        }
        if (this._remaining == 0) {
            return;
        }
        int n = this.getLimit();
        if (this._remaining >= n) {
            throw new IOException("corrupted stream - out of bounds length found: " + this._remaining + " >= " + n);
        }
        if ((this._remaining -= Streams.readFully(this._in, byArray)) != 0) {
            throw new EOFException("DEF length " + this._originalLength + " object truncated by " + this._remaining);
        }
        this.setParentEofDetect(true);
    }

    byte[] toByteArray() throws IOException {
        if (this._remaining == 0) {
            return EMPTY_BYTES;
        }
        int n = this.getLimit();
        if (this._remaining >= n) {
            throw new IOException("corrupted stream - out of bounds length found: " + this._remaining + " >= " + n);
        }
        byte[] byArray = new byte[this._remaining];
        if ((this._remaining -= Streams.readFully(this._in, byArray)) != 0) {
            throw new EOFException("DEF length " + this._originalLength + " object truncated by " + this._remaining);
        }
        this.setParentEofDetect(true);
        return byArray;
    }
}

