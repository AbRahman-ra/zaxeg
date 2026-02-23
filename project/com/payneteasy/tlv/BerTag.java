/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.payneteasy.tlv;

import com.payneteasy.tlv.HexUtil;
import java.util.Arrays;

public class BerTag {
    public final byte[] bytes;

    public BerTag(byte[] aBuf) {
        this(aBuf, 0, aBuf.length);
    }

    public BerTag(byte[] aBuf, int aOffset, int aLength) {
        byte[] temp = new byte[aLength];
        System.arraycopy(aBuf, aOffset, temp, 0, aLength);
        this.bytes = temp;
    }

    public BerTag(int aFirstByte, int aSecondByte) {
        this.bytes = new byte[]{(byte)aFirstByte, (byte)aSecondByte};
    }

    public BerTag(int aFirstByte, int aSecondByte, int aFirth) {
        this.bytes = new byte[]{(byte)aFirstByte, (byte)aSecondByte, (byte)aFirth};
    }

    public BerTag(int aFirstByte) {
        this.bytes = new byte[]{(byte)aFirstByte};
    }

    public boolean isConstructed() {
        return (this.bytes[0] & 0x20) != 0;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        BerTag berTag = (BerTag)o;
        return Arrays.equals(this.bytes, berTag.bytes);
    }

    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    public String toString() {
        return (this.isConstructed() ? "+ " : "- ") + HexUtil.toHexString(this.bytes, 0, this.bytes.length);
    }
}

