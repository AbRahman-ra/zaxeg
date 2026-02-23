/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize.charcode;

import net.sf.saxon.serialize.charcode.CharacterSet;

public final class UTF8CharacterSet
implements CharacterSet {
    private static UTF8CharacterSet theInstance = new UTF8CharacterSet();

    private UTF8CharacterSet() {
    }

    public static UTF8CharacterSet getInstance() {
        return theInstance;
    }

    @Override
    public boolean inCharset(int c) {
        return true;
    }

    @Override
    public String getCanonicalName() {
        return "UTF-8";
    }

    public static int getUTF8Encoding(char in, char in2, byte[] out) {
        char i = in;
        if (i <= '\u007f') {
            out[0] = (byte)i;
            return 1;
        }
        if (i <= '\u07ff') {
            out[0] = (byte)(0xC0 | in >> 6 & 0x1F);
            out[1] = (byte)(0x80 | in & 0x3F);
            return 2;
        }
        if (i >= '\ud800' && i <= '\udbff') {
            char j = in2;
            if (j < '\udc00' || j > '\udfff') {
                throw new IllegalArgumentException("Malformed Unicode Surrogate Pair (" + i + ',' + j + ')');
            }
            byte xxxxxx = (byte)(j & 0x3F);
            byte yyyyyy = (byte)((i & 3) << 4 | j >> 6 & 0xF);
            byte zzzz = (byte)(i >> 2 & 0xF);
            byte uuuuu = (byte)((i >> 6 & 0xF) + 1);
            out[0] = (byte)(0xF0 | uuuuu >> 2 & 7);
            out[1] = (byte)(0x80 | (uuuuu & 3) << 4 | zzzz);
            out[2] = (byte)(0x80 | yyyyyy);
            out[3] = (byte)(0x80 | xxxxxx);
            return 4;
        }
        if (i >= '\udc00' && i <= '\udfff') {
            return 0;
        }
        out[0] = (byte)(0xE0 | in >> 12 & 0xF);
        out[1] = (byte)(0x80 | in >> 6 & 0x3F);
        out[2] = (byte)(0x80 | in & 0x3F);
        return 3;
    }

    public static int decodeUTF8(byte[] in, int used) throws IllegalArgumentException {
        int bottom = 0;
        for (int i = 1; i < used; ++i) {
            if ((in[i] & 0xC0) != 128) {
                throw new IllegalArgumentException("Byte " + (i + 1) + " in UTF-8 sequence has wrong top bits");
            }
            bottom = (bottom << 6) + (in[i] & 0x3F);
        }
        if ((in[0] & 0x80) == 0) {
            if (used == 1) {
                return in[0];
            }
            throw new IllegalArgumentException("UTF8 single byte expected");
        }
        if ((in[0] & 0xE0) == 192) {
            if (used != 2) {
                throw new IllegalArgumentException("UTF8 sequence of two bytes expected");
            }
            return ((in[0] & 0x1F) << 6) + bottom;
        }
        if ((in[0] & 0xF0) == 224) {
            if (used != 3) {
                throw new IllegalArgumentException("UTF8 sequence of three bytes expected");
            }
            return ((in[0] & 0xF) << 12) + bottom;
        }
        if ((in[0] & 0xF8) == 248) {
            if (used != 4) {
                throw new IllegalArgumentException("UTF8 sequence of four bytes expected");
            }
            return ((in[0] & 7) << 24) + bottom;
        }
        throw new IllegalArgumentException("UTF8 invalid first byte");
    }
}

