/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Null;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.its.BitmapSspRange;
import org.bouncycastle.asn1.its.SequenceOfOctetString;

public class SspRange
extends ASN1Object {
    private final boolean isAll;
    private final SequenceOfOctetString opaque;
    private final BitmapSspRange bitmapSspRange;

    private SspRange() {
        this.isAll = true;
        this.opaque = null;
        this.bitmapSspRange = null;
    }

    private SspRange(SequenceOfOctetString sequenceOfOctetString) {
        this.isAll = false;
        if (sequenceOfOctetString.size() != 2) {
            this.opaque = sequenceOfOctetString;
            this.bitmapSspRange = null;
        } else {
            BitmapSspRange bitmapSspRange;
            this.opaque = SequenceOfOctetString.getInstance(sequenceOfOctetString);
            try {
                bitmapSspRange = BitmapSspRange.getInstance(sequenceOfOctetString);
            } catch (IllegalArgumentException illegalArgumentException) {
                bitmapSspRange = null;
            }
            this.bitmapSspRange = bitmapSspRange;
        }
    }

    public SspRange(BitmapSspRange bitmapSspRange) {
        this.isAll = false;
        this.bitmapSspRange = bitmapSspRange;
        this.opaque = null;
    }

    public static SspRange getInstance(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof SspRange) {
            return (SspRange)object;
        }
        if (object instanceof ASN1Null) {
            return new SspRange();
        }
        if (object instanceof ASN1Sequence) {
            return new SspRange(SequenceOfOctetString.getInstance(object));
        }
        if (object instanceof byte[]) {
            try {
                return SspRange.getInstance(ASN1Primitive.fromByteArray((byte[])object));
            } catch (IOException iOException) {
                throw new IllegalArgumentException("unable to parse encoded general name");
            }
        }
        throw new IllegalArgumentException("unknown object in getInstance: " + object.getClass().getName());
    }

    public boolean isAll() {
        return this.isAll;
    }

    public boolean maybeOpaque() {
        return this.opaque != null;
    }

    public BitmapSspRange getBitmapSspRange() {
        return this.bitmapSspRange;
    }

    public SequenceOfOctetString getOpaque() {
        return this.opaque;
    }

    public ASN1Primitive toASN1Primitive() {
        if (this.isAll) {
            return DERNull.INSTANCE;
        }
        if (this.bitmapSspRange != null) {
            return this.bitmapSspRange.toASN1Primitive();
        }
        return this.opaque.toASN1Primitive();
    }
}

