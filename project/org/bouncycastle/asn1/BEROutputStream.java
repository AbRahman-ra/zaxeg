/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.OutputStream;
import org.bouncycastle.asn1.ASN1OutputStream;

class BEROutputStream
extends ASN1OutputStream {
    BEROutputStream(OutputStream outputStream) {
        super(outputStream);
    }
}

