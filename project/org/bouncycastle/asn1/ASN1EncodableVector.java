/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import org.bouncycastle.asn1.ASN1Encodable;

public class ASN1EncodableVector {
    static final ASN1Encodable[] EMPTY_ELEMENTS = new ASN1Encodable[0];
    private static final int DEFAULT_CAPACITY = 10;
    private ASN1Encodable[] elements;
    private int elementCount;
    private boolean copyOnWrite;

    public ASN1EncodableVector() {
        this(10);
    }

    public ASN1EncodableVector(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("'initialCapacity' must not be negative");
        }
        this.elements = n == 0 ? EMPTY_ELEMENTS : new ASN1Encodable[n];
        this.elementCount = 0;
        this.copyOnWrite = false;
    }

    public void add(ASN1Encodable aSN1Encodable) {
        if (null == aSN1Encodable) {
            throw new NullPointerException("'element' cannot be null");
        }
        int n = this.elementCount + 1;
        int n2 = this.elements.length;
        if (n > n2 | this.copyOnWrite) {
            this.reallocate(n);
        }
        this.elements[this.elementCount] = aSN1Encodable;
        this.elementCount = n;
    }

    public void addAll(ASN1EncodableVector aSN1EncodableVector) {
        if (null == aSN1EncodableVector) {
            throw new NullPointerException("'other' cannot be null");
        }
        int n = aSN1EncodableVector.size();
        if (n < 1) {
            return;
        }
        int n2 = this.elementCount + n;
        int n3 = this.elements.length;
        if (n2 > n3 | this.copyOnWrite) {
            this.reallocate(n2);
        }
        int n4 = 0;
        do {
            ASN1Encodable aSN1Encodable;
            if (null == (aSN1Encodable = aSN1EncodableVector.get(n4))) {
                throw new NullPointerException("'other' elements cannot be null");
            }
            this.elements[this.elementCount + n4] = aSN1Encodable;
        } while (++n4 < n);
        this.elementCount = n2;
    }

    public ASN1Encodable get(int n) {
        if (n >= this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(n + " >= " + this.elementCount);
        }
        return this.elements[n];
    }

    public int size() {
        return this.elementCount;
    }

    ASN1Encodable[] copyElements() {
        if (0 == this.elementCount) {
            return EMPTY_ELEMENTS;
        }
        ASN1Encodable[] aSN1EncodableArray = new ASN1Encodable[this.elementCount];
        System.arraycopy(this.elements, 0, aSN1EncodableArray, 0, this.elementCount);
        return aSN1EncodableArray;
    }

    ASN1Encodable[] takeElements() {
        if (0 == this.elementCount) {
            return EMPTY_ELEMENTS;
        }
        if (this.elements.length == this.elementCount) {
            this.copyOnWrite = true;
            return this.elements;
        }
        ASN1Encodable[] aSN1EncodableArray = new ASN1Encodable[this.elementCount];
        System.arraycopy(this.elements, 0, aSN1EncodableArray, 0, this.elementCount);
        return aSN1EncodableArray;
    }

    private void reallocate(int n) {
        int n2 = this.elements.length;
        int n3 = Math.max(n2, n + (n >> 1));
        ASN1Encodable[] aSN1EncodableArray = new ASN1Encodable[n3];
        System.arraycopy(this.elements, 0, aSN1EncodableArray, 0, this.elementCount);
        this.elements = aSN1EncodableArray;
        this.copyOnWrite = false;
    }

    static ASN1Encodable[] cloneElements(ASN1Encodable[] aSN1EncodableArray) {
        return aSN1EncodableArray.length < 1 ? EMPTY_ELEMENTS : (ASN1Encodable[])aSN1EncodableArray.clone();
    }
}

