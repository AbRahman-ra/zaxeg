/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.x509;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.util.encoders.Hex;

public abstract class X509NameEntryConverter {
    protected ASN1Primitive convertHexEncoded(String string, int n) throws IOException {
        return ASN1Primitive.fromByteArray(Hex.decodeStrict(string, n, string.length() - n));
    }

    protected boolean canBePrintable(String string) {
        return DERPrintableString.isPrintableString(string);
    }

    public abstract ASN1Primitive getConvertedValue(ASN1ObjectIdentifier var1, String var2);
}

