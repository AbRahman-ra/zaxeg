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
import org.bouncycastle.asn1.ASN1SequenceParser;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.BERSequence;
import org.bouncycastle.asn1.BERTaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Iterable;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class ASN1Sequence
extends ASN1Primitive
implements Iterable<ASN1Encodable> {
    ASN1Encodable[] elements;

    public static ASN1Sequence getInstance(Object object) {
        ASN1Primitive aSN1Primitive;
        if (object == null || object instanceof ASN1Sequence) {
            return (ASN1Sequence)object;
        }
        if (object instanceof ASN1SequenceParser) {
            return ASN1Sequence.getInstance(((ASN1SequenceParser)object).toASN1Primitive());
        }
        if (object instanceof byte[]) {
            try {
                return ASN1Sequence.getInstance(ASN1Sequence.fromByteArray((byte[])object));
            } catch (IOException iOException) {
                throw new IllegalArgumentException("failed to construct sequence from byte[]: " + iOException.getMessage());
            }
        }
        if (object instanceof ASN1Encodable && (aSN1Primitive = ((ASN1Encodable)object).toASN1Primitive()) instanceof ASN1Sequence) {
            return (ASN1Sequence)aSN1Primitive;
        }
        throw new IllegalArgumentException("unknown object in getInstance: " + object.getClass().getName());
    }

    public static ASN1Sequence getInstance(ASN1TaggedObject aSN1TaggedObject, boolean bl) {
        if (bl) {
            if (!aSN1TaggedObject.isExplicit()) {
                throw new IllegalArgumentException("object implicit - explicit expected.");
            }
            return ASN1Sequence.getInstance(aSN1TaggedObject.getObject());
        }
        ASN1Primitive aSN1Primitive = aSN1TaggedObject.getObject();
        if (aSN1TaggedObject.isExplicit()) {
            if (aSN1TaggedObject instanceof BERTaggedObject) {
                return new BERSequence(aSN1Primitive);
            }
            return new DLSequence(aSN1Primitive);
        }
        if (aSN1Primitive instanceof ASN1Sequence) {
            ASN1Sequence aSN1Sequence = (ASN1Sequence)aSN1Primitive;
            if (aSN1TaggedObject instanceof BERTaggedObject) {
                return aSN1Sequence;
            }
            return (ASN1Sequence)aSN1Sequence.toDLObject();
        }
        throw new IllegalArgumentException("unknown object in getInstance: " + aSN1TaggedObject.getClass().getName());
    }

    protected ASN1Sequence() {
        this.elements = ASN1EncodableVector.EMPTY_ELEMENTS;
    }

    protected ASN1Sequence(ASN1Encodable aSN1Encodable) {
        if (null == aSN1Encodable) {
            throw new NullPointerException("'element' cannot be null");
        }
        this.elements = new ASN1Encodable[]{aSN1Encodable};
    }

    protected ASN1Sequence(ASN1EncodableVector aSN1EncodableVector) {
        if (null == aSN1EncodableVector) {
            throw new NullPointerException("'elementVector' cannot be null");
        }
        this.elements = aSN1EncodableVector.takeElements();
    }

    protected ASN1Sequence(ASN1Encodable[] aSN1EncodableArray) {
        if (Arrays.isNullOrContainsNull(aSN1EncodableArray)) {
            throw new NullPointerException("'elements' cannot be null, or contain null");
        }
        this.elements = ASN1EncodableVector.cloneElements(aSN1EncodableArray);
    }

    ASN1Sequence(ASN1Encodable[] aSN1EncodableArray, boolean bl) {
        this.elements = bl ? ASN1EncodableVector.cloneElements(aSN1EncodableArray) : aSN1EncodableArray;
    }

    public ASN1Encodable[] toArray() {
        return ASN1EncodableVector.cloneElements(this.elements);
    }

    ASN1Encodable[] toArrayInternal() {
        return this.elements;
    }

    public Enumeration getObjects() {
        return new Enumeration(){
            private int pos = 0;

            public boolean hasMoreElements() {
                return this.pos < ASN1Sequence.this.elements.length;
            }

            public Object nextElement() {
                if (this.pos < ASN1Sequence.this.elements.length) {
                    return ASN1Sequence.this.elements[this.pos++];
                }
                throw new NoSuchElementException();
            }
        };
    }

    public ASN1SequenceParser parser() {
        int n = this.size();
        return new ASN1SequenceParser(){
            private int pos = 0;

            public ASN1Encodable readObject() throws IOException {
                ASN1Encodable aSN1Encodable;
                if (n == this.pos) {
                    return null;
                }
                if ((aSN1Encodable = ASN1Sequence.this.elements[this.pos++]) instanceof ASN1Sequence) {
                    return ((ASN1Sequence)aSN1Encodable).parser();
                }
                if (aSN1Encodable instanceof ASN1Set) {
                    return ((ASN1Set)aSN1Encodable).parser();
                }
                return aSN1Encodable;
            }

            public ASN1Primitive getLoadedObject() {
                return ASN1Sequence.this;
            }

            public ASN1Primitive toASN1Primitive() {
                return ASN1Sequence.this;
            }
        };
    }

    public ASN1Encodable getObjectAt(int n) {
        return this.elements[n];
    }

    public int size() {
        return this.elements.length;
    }

    @Override
    public int hashCode() {
        int n = this.elements.length;
        int n2 = n + 1;
        while (--n >= 0) {
            n2 *= 257;
            n2 ^= this.elements[n].toASN1Primitive().hashCode();
        }
        return n2;
    }

    @Override
    boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof ASN1Sequence)) {
            return false;
        }
        ASN1Sequence aSN1Sequence = (ASN1Sequence)aSN1Primitive;
        int n = this.size();
        if (aSN1Sequence.size() != n) {
            return false;
        }
        for (int i = 0; i < n; ++i) {
            ASN1Primitive aSN1Primitive2;
            ASN1Primitive aSN1Primitive3 = this.elements[i].toASN1Primitive();
            if (aSN1Primitive3 == (aSN1Primitive2 = aSN1Sequence.elements[i].toASN1Primitive()) || aSN1Primitive3.asn1Equals(aSN1Primitive2)) continue;
            return false;
        }
        return true;
    }

    @Override
    ASN1Primitive toDERObject() {
        return new DERSequence(this.elements, false);
    }

    @Override
    ASN1Primitive toDLObject() {
        return new DLSequence(this.elements, false);
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
        return new Arrays.Iterator<ASN1Encodable>(this.elements);
    }
}

