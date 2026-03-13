/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.starkbank.ellipticcurve.utils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class ByteString {
    private byte[] bytes;

    public ByteString() {
        this.bytes = new byte[0];
    }

    public ByteString(byte[] bytes) {
        this.bytes = bytes;
    }

    public short getShort(int index) {
        return (short)(this.bytes[index] & 0xFF);
    }

    public ByteString substring(int start) {
        return this.substring(start, this.bytes.length);
    }

    public ByteString substring(int start, int end) {
        if (end > this.bytes.length) {
            end = this.bytes.length;
        }
        if (end < 0) {
            end = this.bytes.length - end;
        }
        if (start > end) {
            return new ByteString();
        }
        return new ByteString(Arrays.copyOfRange(this.bytes, start, end));
    }

    public byte[] getBytes() {
        return Arrays.copyOf(this.bytes, this.bytes.length);
    }

    public int length() {
        return this.bytes.length;
    }

    public boolean isEmpty() {
        return this.bytes.length == 0;
    }

    public void insert(byte[] b) {
        this.insert(this.bytes.length, b);
    }

    public void insert(int index, byte[] b) {
        byte[] result = new byte[b.length + this.bytes.length];
        System.arraycopy(this.bytes, 0, result, 0, index);
        System.arraycopy(b, 0, result, index, b.length);
        if (index < this.bytes.length) {
            System.arraycopy(this.bytes, index, result, b.length + index, this.bytes.length - index);
        }
        this.bytes = result;
    }

    public void replace(int index, byte value) {
        this.bytes[index] = value;
    }

    public String toString() {
        if (this.bytes.length == 0) {
            return "";
        }
        try {
            return new String(this.bytes, "ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }
    }
}

