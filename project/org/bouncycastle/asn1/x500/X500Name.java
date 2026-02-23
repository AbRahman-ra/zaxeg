/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.x500;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500NameStyle;
import org.bouncycastle.asn1.x500.style.BCStyle;

public class X500Name
extends ASN1Object
implements ASN1Choice {
    private static X500NameStyle defaultStyle = BCStyle.INSTANCE;
    private boolean isHashCodeCalculated;
    private int hashCodeValue;
    private X500NameStyle style;
    private RDN[] rdns;
    private DERSequence rdnSeq;

    public X500Name(X500NameStyle x500NameStyle, X500Name x500Name) {
        this.style = x500NameStyle;
        this.rdns = x500Name.rdns;
        this.rdnSeq = x500Name.rdnSeq;
    }

    public static X500Name getInstance(ASN1TaggedObject aSN1TaggedObject, boolean bl) {
        return X500Name.getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, true));
    }

    public static X500Name getInstance(Object object) {
        if (object instanceof X500Name) {
            return (X500Name)object;
        }
        if (object != null) {
            return new X500Name(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public static X500Name getInstance(X500NameStyle x500NameStyle, Object object) {
        if (object instanceof X500Name) {
            return new X500Name(x500NameStyle, (X500Name)object);
        }
        if (object != null) {
            return new X500Name(x500NameStyle, ASN1Sequence.getInstance(object));
        }
        return null;
    }

    private X500Name(ASN1Sequence aSN1Sequence) {
        this(defaultStyle, aSN1Sequence);
    }

    private X500Name(X500NameStyle x500NameStyle, ASN1Sequence aSN1Sequence) {
        this.style = x500NameStyle;
        this.rdns = new RDN[aSN1Sequence.size()];
        boolean bl = true;
        int n = 0;
        Enumeration enumeration = aSN1Sequence.getObjects();
        while (enumeration.hasMoreElements()) {
            Object e = enumeration.nextElement();
            RDN rDN = RDN.getInstance(e);
            bl &= rDN == e;
            this.rdns[n++] = rDN;
        }
        this.rdnSeq = bl ? DERSequence.convert(aSN1Sequence) : new DERSequence(this.rdns);
    }

    public X500Name(RDN[] rDNArray) {
        this(defaultStyle, rDNArray);
    }

    public X500Name(X500NameStyle x500NameStyle, RDN[] rDNArray) {
        this.style = x500NameStyle;
        this.rdns = (RDN[])rDNArray.clone();
        this.rdnSeq = new DERSequence(this.rdns);
    }

    public X500Name(String string) {
        this(defaultStyle, string);
    }

    public X500Name(X500NameStyle x500NameStyle, String string) {
        this(x500NameStyle.fromString(string));
        this.style = x500NameStyle;
    }

    public RDN[] getRDNs() {
        return (RDN[])this.rdns.clone();
    }

    public ASN1ObjectIdentifier[] getAttributeTypes() {
        int n = this.rdns.length;
        int n2 = 0;
        for (int i = 0; i < n; ++i) {
            RDN rDN = this.rdns[i];
            n2 += rDN.size();
        }
        ASN1ObjectIdentifier[] aSN1ObjectIdentifierArray = new ASN1ObjectIdentifier[n2];
        int n3 = 0;
        for (int i = 0; i < n; ++i) {
            RDN rDN = this.rdns[i];
            n3 += rDN.collectAttributeTypes(aSN1ObjectIdentifierArray, n3);
        }
        return aSN1ObjectIdentifierArray;
    }

    public RDN[] getRDNs(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        RDN[] rDNArray = new RDN[this.rdns.length];
        int n = 0;
        for (int i = 0; i != this.rdns.length; ++i) {
            RDN rDN = this.rdns[i];
            if (!rDN.containsAttributeType(aSN1ObjectIdentifier)) continue;
            rDNArray[n++] = rDN;
        }
        if (n < rDNArray.length) {
            RDN[] rDNArray2 = new RDN[n];
            System.arraycopy(rDNArray, 0, rDNArray2, 0, rDNArray2.length);
            rDNArray = rDNArray2;
        }
        return rDNArray;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.rdnSeq;
    }

    public int hashCode() {
        if (this.isHashCodeCalculated) {
            return this.hashCodeValue;
        }
        this.isHashCodeCalculated = true;
        this.hashCodeValue = this.style.calculateHashCode(this);
        return this.hashCodeValue;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof X500Name) && !(object instanceof ASN1Sequence)) {
            return false;
        }
        ASN1Primitive aSN1Primitive = ((ASN1Encodable)object).toASN1Primitive();
        if (this.toASN1Primitive().equals(aSN1Primitive)) {
            return true;
        }
        try {
            return this.style.areEqual(this, new X500Name(ASN1Sequence.getInstance(((ASN1Encodable)object).toASN1Primitive())));
        } catch (Exception exception) {
            return false;
        }
    }

    public String toString() {
        return this.style.toString(this);
    }

    public static void setDefaultStyle(X500NameStyle x500NameStyle) {
        if (x500NameStyle == null) {
            throw new NullPointerException("cannot set style to null");
        }
        defaultStyle = x500NameStyle;
    }

    public static X500NameStyle getDefaultStyle() {
        return defaultStyle;
    }
}

