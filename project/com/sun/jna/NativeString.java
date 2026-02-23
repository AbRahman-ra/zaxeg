/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.nio.CharBuffer;

class NativeString
implements CharSequence,
Comparable {
    private String value;
    private Pointer pointer;

    public NativeString(String string) {
        this(string, false);
    }

    public NativeString(String string, boolean wide) {
        this.value = string;
        if (string == null) {
            throw new NullPointerException("String must not be null");
        }
        if (wide) {
            int len = (string.length() + 1) * Native.WCHAR_SIZE;
            this.pointer = new Memory(len);
            this.pointer.setString(0L, string, true);
        } else {
            byte[] data = Native.getBytes(string);
            this.pointer = new Memory(data.length + 1);
            this.pointer.setString(0L, string);
        }
    }

    public int hashCode() {
        return this.value.hashCode();
    }

    public boolean equals(Object other) {
        if (other instanceof CharSequence) {
            return this.compareTo(other) == 0;
        }
        return false;
    }

    public String toString() {
        return this.value;
    }

    public Pointer getPointer() {
        return this.pointer;
    }

    public char charAt(int index) {
        return this.value.charAt(index);
    }

    public int length() {
        return this.value.length();
    }

    public CharSequence subSequence(int start, int end) {
        return CharBuffer.wrap(this.value).subSequence(start, end);
    }

    public int compareTo(Object other) {
        if (other == null) {
            return 1;
        }
        return this.value.compareTo(other.toString());
    }
}

