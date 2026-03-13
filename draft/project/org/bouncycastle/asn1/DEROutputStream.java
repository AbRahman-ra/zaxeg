/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Primitive;

class DEROutputStream
extends ASN1OutputStream {
    DEROutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    void writePrimitive(ASN1Primitive aSN1Primitive, boolean bl) throws IOException {
        aSN1Primitive.toDERObject().encode(this, bl);
    }

    DEROutputStream getDERSubStream() {
        return this;
    }

    ASN1OutputStream getDLSubStream() {
        return this;
    }
}

