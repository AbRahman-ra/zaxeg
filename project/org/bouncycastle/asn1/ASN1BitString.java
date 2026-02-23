/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1ParsingException;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DLBitString;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public abstract class ASN1BitString
extends ASN1Primitive
implements ASN1String {
    private static final char[] table = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    protected final byte[] data;
    protected final int padBits;

    protected static int getPadBits(int n) {
        int n2;
        int n3 = 0;
        for (n2 = 3; n2 >= 0; --n2) {
            if (n2 != 0) {
                if (n >> n2 * 8 == 0) continue;
                n3 = n >> n2 * 8 & 0xFF;
                break;
            }
            if (n == 0) continue;
            n3 = n & 0xFF;
            break;
        }
        if (n3 == 0) {
            return 0;
        }
        n2 = 1;
        while (((n3 <<= 1) & 0xFF) != 0) {
            ++n2;
        }
        return 8 - n2;
    }

    protected static byte[] getBytes(int n) {
        if (n == 0) {
            return new byte[0];
        }
        int n2 = 4;
        for (int i = 3; i >= 1 && (n & 255 << i * 8) == 0; --i) {
            --n2;
        }
        byte[] byArray = new byte[n2];
        for (int i = 0; i < n2; ++i) {
            byArray[i] = (byte)(n >> i * 8 & 0xFF);
        }
        return byArray;
    }

    protected ASN1BitString(byte by, int n) {
        if (n > 7 || n < 0) {
            throw new IllegalArgumentException("pad bits cannot be greater than 7 or less than 0");
        }
        this.data = new byte[]{by};
        this.padBits = n;
    }

    public ASN1BitString(byte[] byArray, int n) {
        if (byArray == null) {
            throw new NullPointerException("'data' cannot be null");
        }
        if (byArray.length == 0 && n != 0) {
            throw new IllegalArgumentException("zero length data with non-zero pad bits");
        }
        if (n > 7 || n < 0) {
            throw new IllegalArgumentException("pad bits cannot be greater than 7 or less than 0");
        }
        this.data = Arrays.clone(byArray);
        this.padBits = n;
    }

    public String getString() {
        byte[] byArray;
        StringBuffer stringBuffer = new StringBuffer("#");
        try {
            byArray = this.getEncoded();
        } catch (IOException iOException) {
            throw new ASN1ParsingException("Internal error encoding BitString: " + iOException.getMessage(), iOException);
        }
        for (int i = 0; i != byArray.length; ++i) {
            stringBuffer.append(table[byArray[i] >>> 4 & 0xF]);
            stringBuffer.append(table[byArray[i] & 0xF]);
        }
        return stringBuffer.toString();
    }

    public int intValue() {
        int n;
        int n2 = 0;
        int n3 = Math.min(4, this.data.length - 1);
        for (n = 0; n < n3; ++n) {
            n2 |= (this.data[n] & 0xFF) << 8 * n;
        }
        if (0 <= n3 && n3 < 4) {
            n = (byte)(this.data[n3] & 255 << this.padBits);
            n2 |= (n & 0xFF) << 8 * n3;
        }
        return n2;
    }

    public byte[] getOctets() {
        if (this.padBits != 0) {
            throw new IllegalStateException("attempt to get non-octet aligned data from BIT STRING");
        }
        return Arrays.clone(this.data);
    }

    public byte[] getBytes() {
        if (0 == this.data.length) {
            return this.data;
        }
        byte[] byArray = Arrays.clone(this.data);
        int n = this.data.length - 1;
        byArray[n] = (byte)(byArray[n] & 255 << this.padBits);
        return byArray;
    }

    public int getPadBits() {
        return this.padBits;
    }

    public String toString() {
        return this.getString();
    }

    public int hashCode() {
        int n = this.data.length;
        if (--n < 0) {
            return 1;
        }
        byte by = (byte)(this.data[n] & 255 << this.padBits);
        int n2 = Arrays.hashCode(this.data, 0, n);
        n2 *= 257;
        return (n2 ^= by) ^ this.padBits;
    }

    boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        int n;
        if (!(aSN1Primitive instanceof ASN1BitString)) {
            return false;
        }
        ASN1BitString aSN1BitString = (ASN1BitString)aSN1Primitive;
        if (this.padBits != aSN1BitString.padBits) {
            return false;
        }
        byte[] byArray = this.data;
        int n2 = byArray.length;
        byte[] byArray2 = aSN1BitString.data;
        if (n2 != byArray2.length) {
            return false;
        }
        if (--n2 < 0) {
            return true;
        }
        for (n = 0; n < n2; ++n) {
            if (byArray[n] == byArray2[n]) continue;
            return false;
        }
        n = (byte)(byArray[n2] & 255 << this.padBits);
        byte by = (byte)(byArray2[n2] & 255 << this.padBits);
        return n == by;
    }

    static ASN1BitString fromInputStream(int n, InputStream inputStream) throws IOException {
        if (n < 1) {
            throw new IllegalArgumentException("truncated BIT STRING detected");
        }
        int n2 = inputStream.read();
        byte[] byArray = new byte[n - 1];
        if (byArray.length != 0) {
            if (Streams.readFully(inputStream, byArray) != byArray.length) {
                throw new EOFException("EOF encountered in middle of BIT STRING");
            }
            if (n2 > 0 && n2 < 8 && byArray[byArray.length - 1] != (byte)(byArray[byArray.length - 1] & 255 << n2)) {
                return new DLBitString(byArray, n2);
            }
        }
        return new DERBitString(byArray, n2);
    }

    public ASN1Primitive getLoadedObject() {
        return this.toASN1Primitive();
    }

    ASN1Primitive toDERObject() {
        return new DERBitString(this.data, this.padBits);
    }

    ASN1Primitive toDLObject() {
        return new DLBitString(this.data, this.padBits);
    }

    abstract void encode(ASN1OutputStream var1, boolean var2) throws IOException;
}

