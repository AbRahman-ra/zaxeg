/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import com.sun.jna.FromNativeContext;
import com.sun.jna.NativeMapped;

public abstract class IntegerType
extends Number
implements NativeMapped {
    private int size;
    private Number value;

    public IntegerType(int size) {
        this(size, 0L);
    }

    public IntegerType(int size, long value) {
        this.size = size;
        this.setValue(value);
    }

    public void setValue(long value) {
        long truncated = value;
        switch (this.size) {
            case 1: {
                truncated = (byte)value;
                this.value = new Byte((byte)value);
                break;
            }
            case 2: {
                truncated = (short)value;
                this.value = new Short((short)value);
                break;
            }
            case 4: {
                truncated = (int)value;
                this.value = new Integer((int)value);
                break;
            }
            case 8: {
                this.value = new Long(value);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported size: " + this.size);
            }
        }
        if (this.size < 8) {
            long mask = (1L << this.size * 8) - 1L ^ 0xFFFFFFFFFFFFFFFFL;
            if (value < 0L && truncated != value || value >= 0L && (mask & value) != 0L) {
                throw new IllegalArgumentException("Argument value 0x" + Long.toHexString(value) + " exceeds native capacity (" + this.size + " bytes) mask=0x" + Long.toHexString(mask));
            }
        }
    }

    public Object toNative() {
        return this.value;
    }

    public Object fromNative(Object nativeValue, FromNativeContext context) {
        long value = nativeValue == null ? 0L : ((Number)nativeValue).longValue();
        try {
            IntegerType number = (IntegerType)this.getClass().newInstance();
            number.setValue(value);
            return number;
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Can't instantiate " + this.getClass());
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Not allowed to instantiate " + this.getClass());
        }
    }

    public Class nativeType() {
        return this.value.getClass();
    }

    public int intValue() {
        return this.value.intValue();
    }

    public long longValue() {
        return this.value.longValue();
    }

    public float floatValue() {
        return this.value.floatValue();
    }

    public double doubleValue() {
        return this.value.doubleValue();
    }

    public boolean equals(Object rhs) {
        return rhs instanceof IntegerType && this.value.equals(((IntegerType)rhs).value);
    }

    public String toString() {
        return this.value.toString();
    }

    public int hashCode() {
        return this.value.hashCode();
    }
}

