/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1SetParser;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.BERSet;
import org.bouncycastle.asn1.BERTaggedObject;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DLSet;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Iterable;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class ASN1Set
extends ASN1Primitive
implements Iterable<ASN1Encodable> {
    protected final ASN1Encodable[] elements;
    protected final boolean isSorted;

    public static ASN1Set getInstance(Object object) {
        ASN1Primitive aSN1Primitive;
        if (object == null || object instanceof ASN1Set) {
            return (ASN1Set)object;
        }
        if (object instanceof ASN1SetParser) {
            return ASN1Set.getInstance(((ASN1SetParser)object).toASN1Primitive());
        }
        if (object instanceof byte[]) {
            try {
                return ASN1Set.getInstance(ASN1Primitive.fromByteArray((byte[])object));
            } catch (IOException iOException) {
                throw new IllegalArgumentException("failed to construct set from byte[]: " + iOException.getMessage());
            }
        }
        if (object instanceof ASN1Encodable && (aSN1Primitive = ((ASN1Encodable)object).toASN1Primitive()) instanceof ASN1Set) {
            return (ASN1Set)aSN1Primitive;
        }
        throw new IllegalArgumentException("unknown object in getInstance: " + object.getClass().getName());
    }

    public static ASN1Set getInstance(ASN1TaggedObject aSN1TaggedObject, boolean bl) {
        if (bl) {
            if (!aSN1TaggedObject.isExplicit()) {
                throw new IllegalArgumentException("object implicit - explicit expected.");
            }
            return ASN1Set.getInstance(aSN1TaggedObject.getObject());
        }
        ASN1Primitive aSN1Primitive = aSN1TaggedObject.getObject();
        if (aSN1TaggedObject.isExplicit()) {
            if (aSN1TaggedObject instanceof BERTaggedObject) {
                return new BERSet(aSN1Primitive);
            }
            return new DLSet(aSN1Primitive);
        }
        if (aSN1Primitive instanceof ASN1Set) {
            ASN1Set aSN1Set = (ASN1Set)aSN1Primitive;
            if (aSN1TaggedObject instanceof BERTaggedObject) {
                return aSN1Set;
            }
            return (ASN1Set)aSN1Set.toDLObject();
        }
        if (aSN1Primitive instanceof ASN1Sequence) {
            ASN1Sequence aSN1Sequence = (ASN1Sequence)aSN1Primitive;
            ASN1Encodable[] aSN1EncodableArray = aSN1Sequence.toArrayInternal();
            if (aSN1TaggedObject instanceof BERTaggedObject) {
                return new BERSet(false, aSN1EncodableArray);
            }
            return new DLSet(false, aSN1EncodableArray);
        }
        throw new IllegalArgumentException("unknown object in getInstance: " + aSN1TaggedObject.getClass().getName());
    }

    protected ASN1Set() {
        this.elements = ASN1EncodableVector.EMPTY_ELEMENTS;
        this.isSorted = true;
    }

    protected ASN1Set(ASN1Encodable aSN1Encodable) {
        if (null == aSN1Encodable) {
            throw new NullPointerException("'element' cannot be null");
        }
        this.elements = new ASN1Encodable[]{aSN1Encodable};
        this.isSorted = true;
    }

    protected ASN1Set(ASN1EncodableVector aSN1EncodableVector, boolean bl) {
        ASN1Encodable[] aSN1EncodableArray;
        if (null == aSN1EncodableVector) {
            throw new NullPointerException("'elementVector' cannot be null");
        }
        if (bl && aSN1EncodableVector.size() >= 2) {
            aSN1EncodableArray = aSN1EncodableVector.copyElements();
            ASN1Set.sort(aSN1EncodableArray);
        } else {
            aSN1EncodableArray = aSN1EncodableVector.takeElements();
        }
        this.elements = aSN1EncodableArray;
        this.isSorted = bl || aSN1EncodableArray.length < 2;
    }

    protected ASN1Set(ASN1Encodable[] aSN1EncodableArray, boolean bl) {
        if (Arrays.isNullOrContainsNull(aSN1EncodableArray)) {
            throw new NullPointerException("'elements' cannot be null, or contain null");
        }
        ASN1Encodable[] aSN1EncodableArray2 = ASN1EncodableVector.cloneElements(aSN1EncodableArray);
        if (bl && aSN1EncodableArray2.length >= 2) {
            ASN1Set.sort(aSN1EncodableArray2);
        }
        this.elements = aSN1EncodableArray2;
        this.isSorted = bl || aSN1EncodableArray2.length < 2;
    }

    ASN1Set(boolean bl, ASN1Encodable[] aSN1EncodableArray) {
        this.elements = aSN1EncodableArray;
        this.isSorted = bl || aSN1EncodableArray.length < 2;
    }

    public Enumeration getObjects() {
        return new Enumeration(){
            private int pos = 0;

            public boolean hasMoreElements() {
                return this.pos < ASN1Set.this.elements.length;
            }

            public Object nextElement() {
                if (this.pos < ASN1Set.this.elements.length) {
                    return ASN1Set.this.elements[this.pos++];
                }
                throw new NoSuchElementException();
            }
        };
    }

    public ASN1Encodable getObjectAt(int n) {
        return this.elements[n];
    }

    public int size() {
        return this.elements.length;
    }

    public ASN1Encodable[] toArray() {
        return ASN1EncodableVector.cloneElements(this.elements);
    }

    public ASN1SetParser parser() {
        int n = this.size();
        return new ASN1SetParser(){
            private int pos = 0;

            public ASN1Encodable readObject() throws IOException {
                ASN1Encodable aSN1Encodable;
                if (n == this.pos) {
                    return null;
                }
                if ((aSN1Encodable = ASN1Set.this.elements[this.pos++]) instanceof ASN1Sequence) {
                    return ((ASN1Sequence)aSN1Encodable).parser();
                }
                if (aSN1Encodable instanceof ASN1Set) {
                    return ((ASN1Set)aSN1Encodable).parser();
                }
                return aSN1Encodable;
            }

            public ASN1Primitive getLoadedObject() {
                return ASN1Set.this;
            }

            public ASN1Primitive toASN1Primitive() {
                return ASN1Set.this;
            }
        };
    }

    @Override
    public int hashCode() {
        int n = this.elements.length;
        int n2 = n + 1;
        while (--n >= 0) {
            n2 += this.elements[n].toASN1Primitive().hashCode();
        }
        return n2;
    }

    @Override
    ASN1Primitive toDERObject() {
        ASN1Encodable[] aSN1EncodableArray;
        if (this.isSorted) {
            aSN1EncodableArray = this.elements;
        } else {
            aSN1EncodableArray = (ASN1Encodable[])this.elements.clone();
            ASN1Set.sort(aSN1EncodableArray);
        }
        return new DERSet(true, aSN1EncodableArray);
    }

    @Override
    ASN1Primitive toDLObject() {
        return new DLSet(this.isSorted, this.elements);
    }

    @Override
    boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof ASN1Set)) {
            return false;
        }
        ASN1Set aSN1Set = (ASN1Set)aSN1Primitive;
        int n = this.size();
        if (aSN1Set.size() != n) {
            return false;
        }
        DERSet dERSet = (DERSet)this.toDERObject();
        DERSet dERSet2 = (DERSet)aSN1Set.toDERObject();
        for (int i = 0; i < n; ++i) {
            ASN1Primitive aSN1Primitive2;
            ASN1Primitive aSN1Primitive3 = dERSet.elements[i].toASN1Primitive();
            if (aSN1Primitive3 == (aSN1Primitive2 = dERSet2.elements[i].toASN1Primitive()) || aSN1Primitive3.asn1Equals(aSN1Primitive2)) continue;
            return false;
        }
        return true;
    }

    @Override
    boolean isConstructed() {
        return true;
    }

    @Override
    abstract void encode(ASN1OutputStream var1, boolean var2) throws IOException;

    public String toString() {
        int n = this.size();
        if (0 == n) {
            return "[]";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append('[');
        int n2 = 0;
        while (true) {
            stringBuffer.append(this.elements[n2]);
            if (++n2 >= n) break;
            stringBuffer.append(", ");
        }
        stringBuffer.append(']');
        return stringBuffer.toString();
    }

    @Override
    public Iterator<ASN1Encodable> iterator() {
        return new Arrays.Iterator<ASN1Encodable>(this.toArray());
    }

    private static byte[] getDEREncoded(ASN1Encodable aSN1Encodable) {
        try {
            return aSN1Encodable.toASN1Primitive().getEncoded("DER");
        } catch (IOException iOException) {
            throw new IllegalArgumentException("cannot encode object added to SET");
        }
    }

    private static boolean lessThanOrEqual(byte[] byArray, byte[] byArray2) {
        int n = byArray[0] & 0xFFFFFFDF;
        int n2 = byArray2[0] & 0xFFFFFFDF;
        if (n != n2) {
            return n < n2;
        }
        int n3 = Math.min(byArray.length, byArray2.length) - 1;
        for (int i = 1; i < n3; ++i) {
            if (byArray[i] == byArray2[i]) continue;
            return (byArray[i] & 0xFF) < (byArray2[i] & 0xFF);
        }
        return (byArray[n3] & 0xFF) <= (byArray2[n3] & 0xFF);
    }

    private static void sort(ASN1Encodable[] aSN1EncodableArray) {
        Object object;
        int n = aSN1EncodableArray.length;
        if (n < 2) {
            return;
        }
        Object object2 = aSN1EncodableArray[0];
        Object object3 = aSN1EncodableArray[1];
        byte[] byArray = ASN1Set.getDEREncoded((ASN1Encodable)object2);
        byte[] byArray2 = ASN1Set.getDEREncoded((ASN1Encodable)object3);
        if (ASN1Set.lessThanOrEqual(byArray2, byArray)) {
            ASN1Encodable aSN1Encodable = object3;
            object3 = object2;
            object2 = aSN1Encodable;
            object = byArray2;
            byArray2 = byArray;
            byArray = object;
        }
        for (int i = 2; i < n; ++i) {
            ASN1Encodable aSN1Encodable;
            byte[] byArray3;
            object = aSN1EncodableArray[i];
            byte[] byArray4 = ASN1Set.getDEREncoded((ASN1Encodable)object);
            if (ASN1Set.lessThanOrEqual(byArray2, byArray4)) {
                aSN1EncodableArray[i - 2] = object2;
                object2 = object3;
                byArray = byArray2;
                object3 = object;
                byArray2 = byArray4;
                continue;
            }
            if (ASN1Set.lessThanOrEqual(byArray, byArray4)) {
                aSN1EncodableArray[i - 2] = object2;
                object2 = object;
                byArray = byArray4;
                continue;
            }
            int n2 = i - 1;
            while (--n2 > 0 && !ASN1Set.lessThanOrEqual(byArray3 = ASN1Set.getDEREncoded(aSN1Encodable = aSN1EncodableArray[n2 - 1]), byArray4)) {
                aSN1EncodableArray[n2] = aSN1Encodable;
            }
            aSN1EncodableArray[n2] = object;
        }
        aSN1EncodableArray[n - 2] = object2;
        aSN1EncodableArray[n - 1] = object3;
    }
}

