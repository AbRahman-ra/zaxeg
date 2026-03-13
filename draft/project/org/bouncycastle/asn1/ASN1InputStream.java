/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Exception;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1StreamParser;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.BERApplicationSpecificParser;
import org.bouncycastle.asn1.BEROctetString;
import org.bouncycastle.asn1.BEROctetStringParser;
import org.bouncycastle.asn1.BERPrivateParser;
import org.bouncycastle.asn1.BERSequenceParser;
import org.bouncycastle.asn1.BERSetParser;
import org.bouncycastle.asn1.BERTaggedObjectParser;
import org.bouncycastle.asn1.BERTags;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERExternalParser;
import org.bouncycastle.asn1.DERGeneralString;
import org.bouncycastle.asn1.DERGraphicString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERNumericString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERT61String;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.DERUniversalString;
import org.bouncycastle.asn1.DERVideotexString;
import org.bouncycastle.asn1.DERVisibleString;
import org.bouncycastle.asn1.DLApplicationSpecific;
import org.bouncycastle.asn1.DLExternal;
import org.bouncycastle.asn1.DLFactory;
import org.bouncycastle.asn1.DLPrivate;
import org.bouncycastle.asn1.DefiniteLengthInputStream;
import org.bouncycastle.asn1.IndefiniteLengthInputStream;
import org.bouncycastle.asn1.LazyEncodedSequence;
import org.bouncycastle.asn1.StreamUtil;
import org.bouncycastle.util.io.Streams;

public class ASN1InputStream
extends FilterInputStream
implements BERTags {
    private final int limit;
    private final boolean lazyEvaluate;
    private final byte[][] tmpBuffers;

    public ASN1InputStream(InputStream inputStream) {
        this(inputStream, StreamUtil.findLimit(inputStream));
    }

    public ASN1InputStream(byte[] byArray) {
        this((InputStream)new ByteArrayInputStream(byArray), byArray.length);
    }

    public ASN1InputStream(byte[] byArray, boolean bl) {
        this(new ByteArrayInputStream(byArray), byArray.length, bl);
    }

    public ASN1InputStream(InputStream inputStream, int n) {
        this(inputStream, n, false);
    }

    public ASN1InputStream(InputStream inputStream, boolean bl) {
        this(inputStream, StreamUtil.findLimit(inputStream), bl);
    }

    public ASN1InputStream(InputStream inputStream, int n, boolean bl) {
        super(inputStream);
        this.limit = n;
        this.lazyEvaluate = bl;
        this.tmpBuffers = new byte[11][];
    }

    int getLimit() {
        return this.limit;
    }

    protected int readLength() throws IOException {
        return ASN1InputStream.readLength(this, this.limit, false);
    }

    protected void readFully(byte[] byArray) throws IOException {
        if (Streams.readFully(this, byArray) != byArray.length) {
            throw new EOFException("EOF encountered in middle of object");
        }
    }

    protected ASN1Primitive buildObject(int n, int n2, int n3) throws IOException {
        boolean bl = (n & 0x20) != 0;
        DefiniteLengthInputStream definiteLengthInputStream = new DefiniteLengthInputStream(this, n3, this.limit);
        if ((n & 0xC0) == 192) {
            return new DLPrivate(bl, n2, definiteLengthInputStream.toByteArray());
        }
        if ((n & 0x40) != 0) {
            return new DLApplicationSpecific(bl, n2, definiteLengthInputStream.toByteArray());
        }
        if ((n & 0x80) != 0) {
            return new ASN1StreamParser(definiteLengthInputStream).readTaggedObject(bl, n2);
        }
        if (bl) {
            switch (n2) {
                case 4: {
                    ASN1EncodableVector aSN1EncodableVector = this.readVector(definiteLengthInputStream);
                    ASN1OctetString[] aSN1OctetStringArray = new ASN1OctetString[aSN1EncodableVector.size()];
                    for (int i = 0; i != aSN1OctetStringArray.length; ++i) {
                        ASN1Encodable aSN1Encodable = aSN1EncodableVector.get(i);
                        if (!(aSN1Encodable instanceof ASN1OctetString)) {
                            throw new ASN1Exception("unknown object encountered in constructed OCTET STRING: " + aSN1Encodable.getClass());
                        }
                        aSN1OctetStringArray[i] = (ASN1OctetString)aSN1Encodable;
                    }
                    return new BEROctetString(aSN1OctetStringArray);
                }
                case 16: {
                    if (this.lazyEvaluate) {
                        return new LazyEncodedSequence(definiteLengthInputStream.toByteArray());
                    }
                    return DLFactory.createSequence(this.readVector(definiteLengthInputStream));
                }
                case 17: {
                    return DLFactory.createSet(this.readVector(definiteLengthInputStream));
                }
                case 8: {
                    return new DLExternal(this.readVector(definiteLengthInputStream));
                }
            }
            throw new IOException("unknown tag " + n2 + " encountered");
        }
        return ASN1InputStream.createPrimitiveDERObject(n2, definiteLengthInputStream, this.tmpBuffers);
    }

    ASN1EncodableVector readVector(DefiniteLengthInputStream definiteLengthInputStream) throws IOException {
        ASN1Primitive aSN1Primitive;
        if (definiteLengthInputStream.getRemaining() < 1) {
            return new ASN1EncodableVector(0);
        }
        ASN1InputStream aSN1InputStream = new ASN1InputStream(definiteLengthInputStream);
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        while ((aSN1Primitive = aSN1InputStream.readObject()) != null) {
            aSN1EncodableVector.add(aSN1Primitive);
        }
        return aSN1EncodableVector;
    }

    public ASN1Primitive readObject() throws IOException {
        int n = this.read();
        if (n <= 0) {
            if (n == 0) {
                throw new IOException("unexpected end-of-contents marker");
            }
            return null;
        }
        int n2 = ASN1InputStream.readTagNumber(this, n);
        boolean bl = (n & 0x20) != 0;
        int n3 = this.readLength();
        if (n3 < 0) {
            if (!bl) {
                throw new IOException("indefinite-length primitive encoding encountered");
            }
            IndefiniteLengthInputStream indefiniteLengthInputStream = new IndefiniteLengthInputStream(this, this.limit);
            ASN1StreamParser aSN1StreamParser = new ASN1StreamParser(indefiniteLengthInputStream, this.limit);
            if ((n & 0xC0) == 192) {
                return new BERPrivateParser(n2, aSN1StreamParser).getLoadedObject();
            }
            if ((n & 0x40) != 0) {
                return new BERApplicationSpecificParser(n2, aSN1StreamParser).getLoadedObject();
            }
            if ((n & 0x80) != 0) {
                return new BERTaggedObjectParser(true, n2, aSN1StreamParser).getLoadedObject();
            }
            switch (n2) {
                case 4: {
                    return new BEROctetStringParser(aSN1StreamParser).getLoadedObject();
                }
                case 16: {
                    return new BERSequenceParser(aSN1StreamParser).getLoadedObject();
                }
                case 17: {
                    return new BERSetParser(aSN1StreamParser).getLoadedObject();
                }
                case 8: {
                    return new DERExternalParser(aSN1StreamParser).getLoadedObject();
                }
            }
            throw new IOException("unknown BER object encountered");
        }
        try {
            return this.buildObject(n, n2, n3);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ASN1Exception("corrupted stream detected", illegalArgumentException);
        }
    }

    static int readTagNumber(InputStream inputStream, int n) throws IOException {
        int n2 = n & 0x1F;
        if (n2 == 31) {
            n2 = 0;
            int n3 = inputStream.read();
            if ((n3 & 0x7F) == 0) {
                throw new IOException("corrupted stream - invalid high tag number found");
            }
            while (n3 >= 0 && (n3 & 0x80) != 0) {
                n2 |= n3 & 0x7F;
                n2 <<= 7;
                n3 = inputStream.read();
            }
            if (n3 < 0) {
                throw new EOFException("EOF found inside tag value.");
            }
            n2 |= n3 & 0x7F;
        }
        return n2;
    }

    static int readLength(InputStream inputStream, int n, boolean bl) throws IOException {
        int n2 = inputStream.read();
        if (n2 < 0) {
            throw new EOFException("EOF found when length expected");
        }
        if (n2 == 128) {
            return -1;
        }
        if (n2 > 127) {
            int n3 = n2 & 0x7F;
            if (n3 > 4) {
                throw new IOException("DER length more than 4 bytes: " + n3);
            }
            n2 = 0;
            for (int i = 0; i < n3; ++i) {
                int n4 = inputStream.read();
                if (n4 < 0) {
                    throw new EOFException("EOF found reading length");
                }
                n2 = (n2 << 8) + n4;
            }
            if (n2 < 0) {
                throw new IOException("corrupted stream - negative length found");
            }
            if (n2 >= n && !bl) {
                throw new IOException("corrupted stream - out of bounds length found: " + n2 + " >= " + n);
            }
        }
        return n2;
    }

    private static byte[] getBuffer(DefiniteLengthInputStream definiteLengthInputStream, byte[][] byArray) throws IOException {
        int n = definiteLengthInputStream.getRemaining();
        if (n >= byArray.length) {
            return definiteLengthInputStream.toByteArray();
        }
        byte[] byArray2 = byArray[n];
        if (byArray2 == null) {
            byArray[n] = new byte[n];
            byArray2 = byArray[n];
        }
        definiteLengthInputStream.readAllIntoByteArray(byArray2);
        return byArray2;
    }

    private static char[] getBMPCharBuffer(DefiniteLengthInputStream definiteLengthInputStream) throws IOException {
        int n = definiteLengthInputStream.getRemaining();
        if (0 != (n & 1)) {
            throw new IOException("malformed BMPString encoding encountered");
        }
        char[] cArray = new char[n / 2];
        int n2 = 0;
        byte[] byArray = new byte[8];
        while (n >= 8) {
            if (Streams.readFully(definiteLengthInputStream, byArray, 0, 8) != 8) {
                throw new EOFException("EOF encountered in middle of BMPString");
            }
            cArray[n2] = (char)(byArray[0] << 8 | byArray[1] & 0xFF);
            cArray[n2 + 1] = (char)(byArray[2] << 8 | byArray[3] & 0xFF);
            cArray[n2 + 2] = (char)(byArray[4] << 8 | byArray[5] & 0xFF);
            cArray[n2 + 3] = (char)(byArray[6] << 8 | byArray[7] & 0xFF);
            n2 += 4;
            n -= 8;
        }
        if (n > 0) {
            if (Streams.readFully(definiteLengthInputStream, byArray, 0, n) != n) {
                throw new EOFException("EOF encountered in middle of BMPString");
            }
            int n3 = 0;
            do {
                int n4 = byArray[n3++] << 8;
                int n5 = byArray[n3++] & 0xFF;
                cArray[n2++] = (char)(n4 | n5);
            } while (n3 < n);
        }
        if (0 != definiteLengthInputStream.getRemaining() || cArray.length != n2) {
            throw new IllegalStateException();
        }
        return cArray;
    }

    static ASN1Primitive createPrimitiveDERObject(int n, DefiniteLengthInputStream definiteLengthInputStream, byte[][] byArray) throws IOException {
        switch (n) {
            case 3: {
                return ASN1BitString.fromInputStream(definiteLengthInputStream.getRemaining(), definiteLengthInputStream);
            }
            case 30: {
                return new DERBMPString(ASN1InputStream.getBMPCharBuffer(definiteLengthInputStream));
            }
            case 1: {
                return ASN1Boolean.fromOctetString(ASN1InputStream.getBuffer(definiteLengthInputStream, byArray));
            }
            case 10: {
                return ASN1Enumerated.fromOctetString(ASN1InputStream.getBuffer(definiteLengthInputStream, byArray));
            }
            case 24: {
                return new ASN1GeneralizedTime(definiteLengthInputStream.toByteArray());
            }
            case 27: {
                return new DERGeneralString(definiteLengthInputStream.toByteArray());
            }
            case 22: {
                return new DERIA5String(definiteLengthInputStream.toByteArray());
            }
            case 2: {
                return new ASN1Integer(definiteLengthInputStream.toByteArray(), false);
            }
            case 5: {
                return DERNull.INSTANCE;
            }
            case 18: {
                return new DERNumericString(definiteLengthInputStream.toByteArray());
            }
            case 6: {
                return ASN1ObjectIdentifier.fromOctetString(ASN1InputStream.getBuffer(definiteLengthInputStream, byArray));
            }
            case 4: {
                return new DEROctetString(definiteLengthInputStream.toByteArray());
            }
            case 19: {
                return new DERPrintableString(definiteLengthInputStream.toByteArray());
            }
            case 20: {
                return new DERT61String(definiteLengthInputStream.toByteArray());
            }
            case 28: {
                return new DERUniversalString(definiteLengthInputStream.toByteArray());
            }
            case 23: {
                return new ASN1UTCTime(definiteLengthInputStream.toByteArray());
            }
            case 12: {
                return new DERUTF8String(definiteLengthInputStream.toByteArray());
            }
            case 26: {
                return new DERVisibleString(definiteLengthInputStream.toByteArray());
            }
            case 25: {
                return new DERGraphicString(definiteLengthInputStream.toByteArray());
            }
            case 21: {
                return new DERVideotexString(definiteLengthInputStream.toByteArray());
            }
        }
        throw new IOException("unknown tag " + n + " encountered");
    }
}

