/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DLOutputStream;

public class ASN1OutputStream {
    private OutputStream os;

    public static ASN1OutputStream create(OutputStream outputStream) {
        return new ASN1OutputStream(outputStream);
    }

    public static ASN1OutputStream create(OutputStream outputStream, String string) {
        if (string.equals("DER")) {
            return new DEROutputStream(outputStream);
        }
        if (string.equals("DL")) {
            return new DLOutputStream(outputStream);
        }
        return new ASN1OutputStream(outputStream);
    }

    public ASN1OutputStream(OutputStream outputStream) {
        this.os = outputStream;
    }

    final void writeLength(int n) throws IOException {
        if (n > 127) {
            int n2 = 1;
            int n3 = n;
            while ((n3 >>>= 8) != 0) {
                ++n2;
            }
            this.write((byte)(n2 | 0x80));
            for (int i = (n2 - 1) * 8; i >= 0; i -= 8) {
                this.write((byte)(n >> i));
            }
        } else {
            this.write((byte)n);
        }
    }

    final void write(int n) throws IOException {
        this.os.write(n);
    }

    final void write(byte[] byArray, int n, int n2) throws IOException {
        this.os.write(byArray, n, n2);
    }

    final void writeElements(ASN1Encodable[] aSN1EncodableArray) throws IOException {
        int n = aSN1EncodableArray.length;
        for (int i = 0; i < n; ++i) {
            ASN1Primitive aSN1Primitive = aSN1EncodableArray[i].toASN1Primitive();
            this.writePrimitive(aSN1Primitive, true);
        }
    }

    final void writeElements(Enumeration enumeration) throws IOException {
        while (enumeration.hasMoreElements()) {
            ASN1Primitive aSN1Primitive = ((ASN1Encodable)enumeration.nextElement()).toASN1Primitive();
            this.writePrimitive(aSN1Primitive, true);
        }
    }

    final void writeEncoded(boolean bl, int n, byte by) throws IOException {
        if (bl) {
            this.write(n);
        }
        this.writeLength(1);
        this.write(by);
    }

    final void writeEncoded(boolean bl, int n, byte[] byArray) throws IOException {
        if (bl) {
            this.write(n);
        }
        this.writeLength(byArray.length);
        this.write(byArray, 0, byArray.length);
    }

    final void writeEncoded(boolean bl, int n, byte[] byArray, int n2, int n3) throws IOException {
        if (bl) {
            this.write(n);
        }
        this.writeLength(n3);
        this.write(byArray, n2, n3);
    }

    final void writeEncoded(boolean bl, int n, byte by, byte[] byArray) throws IOException {
        if (bl) {
            this.write(n);
        }
        this.writeLength(1 + byArray.length);
        this.write(by);
        this.write(byArray, 0, byArray.length);
    }

    final void writeEncoded(boolean bl, int n, byte by, byte[] byArray, int n2, int n3, byte by2) throws IOException {
        if (bl) {
            this.write(n);
        }
        this.writeLength(2 + n3);
        this.write(by);
        this.write(byArray, n2, n3);
        this.write(by2);
    }

    final void writeEncoded(boolean bl, int n, int n2, byte[] byArray) throws IOException {
        this.writeTag(bl, n, n2);
        this.writeLength(byArray.length);
        this.write(byArray, 0, byArray.length);
    }

    final void writeEncodedIndef(boolean bl, int n, int n2, byte[] byArray) throws IOException {
        this.writeTag(bl, n, n2);
        this.write(128);
        this.write(byArray, 0, byArray.length);
        this.write(0);
        this.write(0);
    }

    final void writeEncodedIndef(boolean bl, int n, ASN1Encodable[] aSN1EncodableArray) throws IOException {
        if (bl) {
            this.write(n);
        }
        this.write(128);
        this.writeElements(aSN1EncodableArray);
        this.write(0);
        this.write(0);
    }

    final void writeEncodedIndef(boolean bl, int n, Enumeration enumeration) throws IOException {
        if (bl) {
            this.write(n);
        }
        this.write(128);
        this.writeElements(enumeration);
        this.write(0);
        this.write(0);
    }

    final void writeTag(boolean bl, int n, int n2) throws IOException {
        if (!bl) {
            return;
        }
        if (n2 < 31) {
            this.write(n | n2);
        } else {
            this.write(n | 0x1F);
            if (n2 < 128) {
                this.write(n2);
            } else {
                byte[] byArray = new byte[5];
                int n3 = byArray.length;
                byArray[--n3] = (byte)(n2 & 0x7F);
                do {
                    byArray[--n3] = (byte)((n2 >>= 7) & 0x7F | 0x80);
                } while (n2 > 127);
                this.write(byArray, n3, byArray.length - n3);
            }
        }
    }

    public void writeObject(ASN1Encodable aSN1Encodable) throws IOException {
        if (null == aSN1Encodable) {
            throw new IOException("null object detected");
        }
        this.writePrimitive(aSN1Encodable.toASN1Primitive(), true);
        this.flushInternal();
    }

    public void writeObject(ASN1Primitive aSN1Primitive) throws IOException {
        if (null == aSN1Primitive) {
            throw new IOException("null object detected");
        }
        this.writePrimitive(aSN1Primitive, true);
        this.flushInternal();
    }

    void writePrimitive(ASN1Primitive aSN1Primitive, boolean bl) throws IOException {
        aSN1Primitive.encode(this, bl);
    }

    public void close() throws IOException {
        this.os.close();
    }

    public void flush() throws IOException {
        this.os.flush();
    }

    void flushInternal() throws IOException {
    }

    DEROutputStream getDERSubStream() {
        return new DEROutputStream(this.os);
    }

    ASN1OutputStream getDLSubStream() {
        return new DLOutputStream(this.os);
    }
}

