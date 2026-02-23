/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Pointer {
    public static final int SIZE = Native.POINTER_SIZE;
    public static final Pointer NULL;
    long peer;

    public static final Pointer createConstant(long peer) {
        return new Opaque(peer);
    }

    Pointer() {
    }

    Pointer(long peer) {
        this.peer = peer;
    }

    public Pointer share(long offset) {
        return this.share(offset, 0L);
    }

    public Pointer share(long offset, long sz) {
        return new Pointer(this.peer + offset);
    }

    void clear(long size) {
        this.setMemory(0L, size, (byte)0);
    }

    public boolean equals(Object o) {
        if (o == null) {
            return this.peer == 0L;
        }
        return o instanceof Pointer && ((Pointer)o).peer == this.peer;
    }

    public int hashCode() {
        return (int)((this.peer >>> 32) + (this.peer & 0xFFFFFFFFFFFFFFFFL));
    }

    public long indexOf(long offset, byte value) {
        return Pointer._indexOf(this.peer + offset, value);
    }

    private static native long _indexOf(long var0, byte var2);

    public void read(long offset, byte[] buf, int index, int length) {
        Pointer._read(this.peer + offset, buf, index, length);
    }

    private static native void _read(long var0, byte[] var2, int var3, int var4);

    public void read(long offset, short[] buf, int index, int length) {
        Pointer._read(this.peer + offset, buf, index, length);
    }

    private static native void _read(long var0, short[] var2, int var3, int var4);

    public void read(long offset, char[] buf, int index, int length) {
        Pointer._read(this.peer + offset, buf, index, length);
    }

    private static native void _read(long var0, char[] var2, int var3, int var4);

    public void read(long offset, int[] buf, int index, int length) {
        Pointer._read(this.peer + offset, buf, index, length);
    }

    private static native void _read(long var0, int[] var2, int var3, int var4);

    public void read(long offset, long[] buf, int index, int length) {
        Pointer._read(this.peer + offset, buf, index, length);
    }

    private static native void _read(long var0, long[] var2, int var3, int var4);

    public void read(long offset, float[] buf, int index, int length) {
        Pointer._read(this.peer + offset, buf, index, length);
    }

    private static native void _read(long var0, float[] var2, int var3, int var4);

    public void read(long offset, double[] buf, int index, int length) {
        Pointer._read(this.peer + offset, buf, index, length);
    }

    private static native void _read(long var0, double[] var2, int var3, int var4);

    public void read(long offset, Pointer[] buf, int index, int length) {
        for (int i = 0; i < length; ++i) {
            buf[i + index] = this.getPointer(offset + (long)(i * SIZE));
        }
    }

    public void write(long offset, byte[] buf, int index, int length) {
        Pointer._write(this.peer + offset, buf, index, length);
    }

    private static native void _write(long var0, byte[] var2, int var3, int var4);

    public void write(long offset, short[] buf, int index, int length) {
        Pointer._write(this.peer + offset, buf, index, length);
    }

    private static native void _write(long var0, short[] var2, int var3, int var4);

    public void write(long offset, char[] buf, int index, int length) {
        Pointer._write(this.peer + offset, buf, index, length);
    }

    private static native void _write(long var0, char[] var2, int var3, int var4);

    public void write(long offset, int[] buf, int index, int length) {
        Pointer._write(this.peer + offset, buf, index, length);
    }

    private static native void _write(long var0, int[] var2, int var3, int var4);

    public void write(long offset, long[] buf, int index, int length) {
        Pointer._write(this.peer + offset, buf, index, length);
    }

    private static native void _write(long var0, long[] var2, int var3, int var4);

    public void write(long offset, float[] buf, int index, int length) {
        Pointer._write(this.peer + offset, buf, index, length);
    }

    private static native void _write(long var0, float[] var2, int var3, int var4);

    public void write(long offset, double[] buf, int index, int length) {
        Pointer._write(this.peer + offset, buf, index, length);
    }

    private static native void _write(long var0, double[] var2, int var3, int var4);

    public void write(long bOff, Pointer[] buf, int index, int length) {
        for (int i = 0; i < length; ++i) {
            this.setPointer(bOff + (long)(i * SIZE), buf[index + i]);
        }
    }

    public byte getByte(long offset) {
        return Pointer._getByte(this.peer + offset);
    }

    private static native byte _getByte(long var0);

    public char getChar(long offset) {
        return Pointer._getChar(this.peer + offset);
    }

    private static native char _getChar(long var0);

    public short getShort(long offset) {
        return Pointer._getShort(this.peer + offset);
    }

    private static native short _getShort(long var0);

    public int getInt(long offset) {
        return Pointer._getInt(this.peer + offset);
    }

    private static native int _getInt(long var0);

    public long getLong(long offset) {
        return Pointer._getLong(this.peer + offset);
    }

    private static native long _getLong(long var0);

    public NativeLong getNativeLong(long offset) {
        return new NativeLong(NativeLong.SIZE == 8 ? this.getLong(offset) : (long)this.getInt(offset));
    }

    public float getFloat(long offset) {
        return this._getFloat(this.peer + offset);
    }

    private native float _getFloat(long var1);

    public double getDouble(long offset) {
        return Pointer._getDouble(this.peer + offset);
    }

    private static native double _getDouble(long var0);

    public Pointer getPointer(long offset) {
        return Pointer._getPointer(this.peer + offset);
    }

    private static native Pointer _getPointer(long var0);

    public ByteBuffer getByteBuffer(long offset, long length) {
        return this._getDirectByteBuffer(this.peer + offset, length).order(ByteOrder.nativeOrder());
    }

    private native ByteBuffer _getDirectByteBuffer(long var1, long var3);

    public String getString(long offset, boolean wide) {
        return Pointer._getString(this.peer + offset, wide);
    }

    private static native String _getString(long var0, boolean var2);

    public String getString(long offset) {
        long len;
        String encoding = System.getProperty("jna.encoding");
        if (encoding != null && (len = this.indexOf(offset, (byte)0)) != -1L) {
            if (len > Integer.MAX_VALUE) {
                throw new OutOfMemoryError("String exceeds maximum length: " + len);
            }
            byte[] data = this.getByteArray(offset, (int)len);
            try {
                return new String(data, encoding);
            } catch (UnsupportedEncodingException e) {
                // empty catch block
            }
        }
        return this.getString(offset, false);
    }

    public byte[] getByteArray(long offset, int arraySize) {
        byte[] buf = new byte[arraySize];
        this.read(offset, buf, 0, arraySize);
        return buf;
    }

    public char[] getCharArray(long offset, int arraySize) {
        char[] buf = new char[arraySize];
        this.read(offset, buf, 0, arraySize);
        return buf;
    }

    public short[] getShortArray(long offset, int arraySize) {
        short[] buf = new short[arraySize];
        this.read(offset, buf, 0, arraySize);
        return buf;
    }

    public int[] getIntArray(long offset, int arraySize) {
        int[] buf = new int[arraySize];
        this.read(offset, buf, 0, arraySize);
        return buf;
    }

    public long[] getLongArray(long offset, int arraySize) {
        long[] buf = new long[arraySize];
        this.read(offset, buf, 0, arraySize);
        return buf;
    }

    public float[] getFloatArray(long offset, int arraySize) {
        float[] buf = new float[arraySize];
        this.read(offset, buf, 0, arraySize);
        return buf;
    }

    public double[] getDoubleArray(long offset, int arraySize) {
        double[] buf = new double[arraySize];
        this.read(offset, buf, 0, arraySize);
        return buf;
    }

    public Pointer[] getPointerArray(long base) {
        ArrayList<Pointer> array = new ArrayList<Pointer>();
        int offset = 0;
        Pointer p = this.getPointer(base);
        while (p != null) {
            array.add(p);
            p = this.getPointer(base + (long)(offset += SIZE));
        }
        return array.toArray(new Pointer[array.size()]);
    }

    public Pointer[] getPointerArray(long offset, int arraySize) {
        Pointer[] buf = new Pointer[arraySize];
        this.read(offset, buf, 0, arraySize);
        return buf;
    }

    public String[] getStringArray(long base) {
        return this.getStringArray(base, false);
    }

    public String[] getStringArray(long base, boolean wide) {
        ArrayList<String> strings = new ArrayList<String>();
        int offset = 0;
        Pointer p = this.getPointer(base);
        while (p != null) {
            strings.add(p.getString(0L, wide));
            p = this.getPointer(base + (long)(offset += SIZE));
        }
        return strings.toArray(new String[strings.size()]);
    }

    public void setMemory(long offset, long length, byte value) {
        Pointer._setMemory(this.peer + offset, length, value);
    }

    private static native void _setMemory(long var0, long var2, byte var4);

    public void setByte(long offset, byte value) {
        Pointer._setByte(this.peer + offset, value);
    }

    private static native void _setByte(long var0, byte var2);

    public void setShort(long offset, short value) {
        Pointer._setShort(this.peer + offset, value);
    }

    private static native void _setShort(long var0, short var2);

    public void setChar(long offset, char value) {
        Pointer._setChar(this.peer + offset, value);
    }

    private static native void _setChar(long var0, char var2);

    public void setInt(long offset, int value) {
        Pointer._setInt(this.peer + offset, value);
    }

    private static native void _setInt(long var0, int var2);

    public void setLong(long offset, long value) {
        Pointer._setLong(this.peer + offset, value);
    }

    private static native void _setLong(long var0, long var2);

    public void setNativeLong(long offset, NativeLong value) {
        if (NativeLong.SIZE == 8) {
            this.setLong(offset, value.longValue());
        } else {
            this.setInt(offset, value.intValue());
        }
    }

    public void setFloat(long offset, float value) {
        Pointer._setFloat(this.peer + offset, value);
    }

    private static native void _setFloat(long var0, float var2);

    public void setDouble(long offset, double value) {
        Pointer._setDouble(this.peer + offset, value);
    }

    private static native void _setDouble(long var0, double var2);

    public void setPointer(long offset, Pointer value) {
        Pointer._setPointer(this.peer + offset, value != null ? value.peer : 0L);
    }

    private static native void _setPointer(long var0, long var2);

    public void setString(long offset, String value, boolean wide) {
        Pointer._setString(this.peer + offset, value, wide);
    }

    private static native void _setString(long var0, String var2, boolean var3);

    public void setString(long offset, String value) {
        byte[] data = Native.getBytes(value);
        this.write(offset, data, 0, data.length);
        this.setByte(offset + (long)data.length, (byte)0);
    }

    public String toString() {
        return "native@0x" + Long.toHexString(this.peer);
    }

    static {
        if (SIZE == 0) {
            throw new Error("Native library not initialized");
        }
        NULL = null;
    }

    private static class Opaque
    extends Pointer {
        private String MSG = "This pointer is opaque: " + this;

        private Opaque(long peer) {
            super(peer);
        }

        public long indexOf(long offset, byte value) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void read(long bOff, byte[] buf, int index, int length) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void read(long bOff, char[] buf, int index, int length) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void read(long bOff, short[] buf, int index, int length) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void read(long bOff, int[] buf, int index, int length) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void read(long bOff, long[] buf, int index, int length) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void read(long bOff, float[] buf, int index, int length) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void read(long bOff, double[] buf, int index, int length) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void write(long bOff, byte[] buf, int index, int length) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void write(long bOff, char[] buf, int index, int length) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void write(long bOff, short[] buf, int index, int length) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void write(long bOff, int[] buf, int index, int length) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void write(long bOff, long[] buf, int index, int length) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void write(long bOff, float[] buf, int index, int length) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void write(long bOff, double[] buf, int index, int length) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public byte getByte(long bOff) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public char getChar(long bOff) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public short getShort(long bOff) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public int getInt(long bOff) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public long getLong(long bOff) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public float getFloat(long bOff) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public double getDouble(long bOff) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public Pointer getPointer(long bOff) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public String getString(long bOff, boolean wide) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void setByte(long bOff, byte value) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void setChar(long bOff, char value) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void setShort(long bOff, short value) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void setInt(long bOff, int value) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void setLong(long bOff, long value) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void setFloat(long bOff, float value) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void setDouble(long bOff, double value) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void setPointer(long offset, Pointer value) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public void setString(long offset, String value, boolean wide) {
            throw new UnsupportedOperationException(this.MSG);
        }

        public String toString() {
            return "opaque@0x" + Long.toHexString(this.peer);
        }
    }
}

