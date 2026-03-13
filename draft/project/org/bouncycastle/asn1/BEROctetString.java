/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;

public class BEROctetString
extends ASN1OctetString {
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private final int chunkSize;
    private final ASN1OctetString[] octs;

    private static byte[] toBytes(ASN1OctetString[] aSN1OctetStringArray) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (int i = 0; i != aSN1OctetStringArray.length; ++i) {
            try {
                byteArrayOutputStream.write(aSN1OctetStringArray[i].getOctets());
                continue;
            } catch (IOException iOException) {
                throw new IllegalArgumentException("exception converting octets " + iOException.toString());
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    public BEROctetString(byte[] byArray) {
        this(byArray, 1000);
    }

    public BEROctetString(ASN1OctetString[] aSN1OctetStringArray) {
        this(aSN1OctetStringArray, 1000);
    }

    public BEROctetString(byte[] byArray, int n) {
        this(byArray, null, n);
    }

    public BEROctetString(ASN1OctetString[] aSN1OctetStringArray, int n) {
        this(BEROctetString.toBytes(aSN1OctetStringArray), aSN1OctetStringArray, n);
    }

    private BEROctetString(byte[] byArray, ASN1OctetString[] aSN1OctetStringArray, int n) {
        super(byArray);
        this.octs = aSN1OctetStringArray;
        this.chunkSize = n;
    }

    public Enumeration getObjects() {
        if (this.octs == null) {
            return new Enumeration(){
                int pos = 0;

                public boolean hasMoreElements() {
                    return this.pos < BEROctetString.this.string.length;
                }

                public Object nextElement() {
                    if (this.pos < BEROctetString.this.string.length) {
                        int n = Math.min(BEROctetString.this.string.length - this.pos, BEROctetString.this.chunkSize);
                        byte[] byArray = new byte[n];
                        System.arraycopy(BEROctetString.this.string, this.pos, byArray, 0, n);
                        this.pos += n;
                        return new DEROctetString(byArray);
                    }
                    throw new NoSuchElementException();
                }
            };
        }
        return new Enumeration(){
            int counter = 0;

            public boolean hasMoreElements() {
                return this.counter < BEROctetString.this.octs.length;
            }

            public Object nextElement() {
                if (this.counter < BEROctetString.this.octs.length) {
                    return BEROctetString.this.octs[this.counter++];
                }
                throw new NoSuchElementException();
            }
        };
    }

    boolean isConstructed() {
        return true;
    }

    int encodedLength() throws IOException {
        int n = 0;
        Enumeration enumeration = this.getObjects();
        while (enumeration.hasMoreElements()) {
            n += ((ASN1Encodable)enumeration.nextElement()).toASN1Primitive().encodedLength();
        }
        return 2 + n + 2;
    }

    void encode(ASN1OutputStream aSN1OutputStream, boolean bl) throws IOException {
        aSN1OutputStream.writeEncodedIndef(bl, 36, this.getObjects());
    }

    static BEROctetString fromSequence(ASN1Sequence aSN1Sequence) {
        int n = aSN1Sequence.size();
        ASN1OctetString[] aSN1OctetStringArray = new ASN1OctetString[n];
        for (int i = 0; i < n; ++i) {
            aSN1OctetStringArray[i] = ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(i));
        }
        return new BEROctetString(aSN1OctetStringArray);
    }
}

