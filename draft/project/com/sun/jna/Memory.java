/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.nio.ByteBuffer;

public class Memory
extends Pointer {
    protected long size;

    public Memory(long size) {
        this.size = size;
        if (size <= 0L) {
            throw new IllegalArgumentException("Allocation size must be >= 0");
        }
        this.peer = Memory.malloc(size);
        if (this.peer == 0L) {
            throw new OutOfMemoryError("Cannot allocate " + size + " bytes");
        }
    }

    protected Memory() {
    }

    public Pointer share(long offset) {
        return this.share(offset, this.getSize() - offset);
    }

    public Pointer share(long offset, long sz) {
        this.boundsCheck(offset, sz);
        return new SharedMemory(offset);
    }

    public Pointer align(int byteBoundary) {
        if (byteBoundary <= 0) {
            throw new IllegalArgumentException("Byte boundary must be positive: " + byteBoundary);
        }
        long mask = (long)byteBoundary - 1L ^ 0xFFFFFFFFFFFFFFFFL;
        if ((this.peer & (mask ^ 0xFFFFFFFFFFFFFFFFL)) != this.peer) {
            long newPeer = this.peer + (mask ^ 0xFFFFFFFFFFFFFFFFL) & mask;
            return this.share(newPeer - this.peer, this.peer + this.size - newPeer);
        }
        return this;
    }

    protected void finalize() {
        if (this.peer != 0L) {
            Memory.free(this.peer);
            this.peer = 0L;
        }
    }

    public void clear() {
        this.clear(this.size);
    }

    public boolean isValid() {
        return this.peer != 0L;
    }

    public long getSize() {
        return this.size;
    }

    protected void boundsCheck(long off, long sz) {
        if (off < 0L) {
            throw new IndexOutOfBoundsException("Invalid offset: " + off);
        }
        if (off + sz > this.size) {
            String msg = "Bounds exceeds available space : size=" + this.size + ", offset=" + (off + sz);
            throw new IndexOutOfBoundsException(msg);
        }
    }

    public void read(long bOff, byte[] buf, int index, int length) {
        this.boundsCheck(bOff, length * 1);
        super.read(bOff, buf, index, length);
    }

    public void read(long bOff, short[] buf, int index, int length) {
        this.boundsCheck(bOff, length * 2);
        super.read(bOff, buf, index, length);
    }

    public void read(long bOff, char[] buf, int index, int length) {
        this.boundsCheck(bOff, length * 2);
        super.read(bOff, buf, index, length);
    }

    public void read(long bOff, int[] buf, int index, int length) {
        this.boundsCheck(bOff, length * 4);
        super.read(bOff, buf, index, length);
    }

    public void read(long bOff, long[] buf, int index, int length) {
        this.boundsCheck(bOff, length * 8);
        super.read(bOff, buf, index, length);
    }

    public void read(long bOff, float[] buf, int index, int length) {
        this.boundsCheck(bOff, length * 4);
        super.read(bOff, buf, index, length);
    }

    public void read(long bOff, double[] buf, int index, int length) {
        this.boundsCheck(bOff, length * 8);
        super.read(bOff, buf, index, length);
    }

    public void write(long bOff, byte[] buf, int index, int length) {
        this.boundsCheck(bOff, length * 1);
        super.write(bOff, buf, index, length);
    }

    public void write(long bOff, short[] buf, int index, int length) {
        this.boundsCheck(bOff, length * 2);
        super.write(bOff, buf, index, length);
    }

    public void write(long bOff, char[] buf, int index, int length) {
        this.boundsCheck(bOff, length * 2);
        super.write(bOff, buf, index, length);
    }

    public void write(long bOff, int[] buf, int index, int length) {
        this.boundsCheck(bOff, length * 4);
        super.write(bOff, buf, index, length);
    }

    public void write(long bOff, long[] buf, int index, int length) {
        this.boundsCheck(bOff, length * 8);
        super.write(bOff, buf, index, length);
    }

    public void write(long bOff, float[] buf, int index, int length) {
        this.boundsCheck(bOff, length * 4);
        super.write(bOff, buf, index, length);
    }

    public void write(long bOff, double[] buf, int index, int length) {
        this.boundsCheck(bOff, length * 8);
        super.write(bOff, buf, index, length);
    }

    public byte getByte(long offset) {
        this.boundsCheck(offset, 1L);
        return super.getByte(offset);
    }

    public char getChar(long offset) {
        this.boundsCheck(offset, 1L);
        return super.getChar(offset);
    }

    public short getShort(long offset) {
        this.boundsCheck(offset, 2L);
        return super.getShort(offset);
    }

    public int getInt(long offset) {
        this.boundsCheck(offset, 4L);
        return super.getInt(offset);
    }

    public long getLong(long offset) {
        this.boundsCheck(offset, 8L);
        return super.getLong(offset);
    }

    public float getFloat(long offset) {
        this.boundsCheck(offset, 4L);
        return super.getFloat(offset);
    }

    public double getDouble(long offset) {
        this.boundsCheck(offset, 8L);
        return super.getDouble(offset);
    }

    public Pointer getPointer(long offset) {
        this.boundsCheck(offset, Pointer.SIZE);
        return super.getPointer(offset);
    }

    public ByteBuffer getByteBuffer(long offset, long length) {
        this.boundsCheck(offset, length);
        return super.getByteBuffer(offset, length);
    }

    public String getString(long offset, boolean wide) {
        this.boundsCheck(offset, 0L);
        return super.getString(offset, wide);
    }

    public void setByte(long offset, byte value) {
        this.boundsCheck(offset, 1L);
        super.setByte(offset, value);
    }

    public void setChar(long offset, char value) {
        this.boundsCheck(offset, Native.WCHAR_SIZE);
        super.setChar(offset, value);
    }

    public void setShort(long offset, short value) {
        this.boundsCheck(offset, 2L);
        super.setShort(offset, value);
    }

    public void setInt(long offset, int value) {
        this.boundsCheck(offset, 4L);
        super.setInt(offset, value);
    }

    public void setLong(long offset, long value) {
        this.boundsCheck(offset, 8L);
        super.setLong(offset, value);
    }

    public void setFloat(long offset, float value) {
        this.boundsCheck(offset, 4L);
        super.setFloat(offset, value);
    }

    public void setDouble(long offset, double value) {
        this.boundsCheck(offset, 8L);
        super.setDouble(offset, value);
    }

    public void setPointer(long offset, Pointer value) {
        this.boundsCheck(offset, Pointer.SIZE);
        super.setPointer(offset, value);
    }

    public void setString(long offset, String value, boolean wide) {
        if (wide) {
            this.boundsCheck(offset, (value.length() + 1) * Native.WCHAR_SIZE);
        } else {
            this.boundsCheck(offset, value.getBytes().length + 1);
        }
        super.setString(offset, value, wide);
    }

    static native long malloc(long var0);

    static native void free(long var0);

    public String toString() {
        return "allocated@0x" + Long.toHexString(this.peer) + " (" + this.size + " bytes)";
    }

    private class SharedMemory
    extends Memory {
        public SharedMemory(long offset) {
            this.size = Memory.this.size - offset;
            this.peer = Memory.this.peer + offset;
        }

        protected void finalize() {
        }

        protected void boundsCheck(long off, long sz) {
            Memory.this.boundsCheck(this.peer - Memory.this.peer + off, sz);
        }

        public String toString() {
            return super.toString() + " (shared from " + Memory.this.toString() + ")";
        }
    }
}

