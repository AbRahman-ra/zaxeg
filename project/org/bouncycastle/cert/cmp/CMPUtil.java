/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cert.cmp;

import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.cert.cmp.CMPRuntimeException;

class CMPUtil {
    CMPUtil() {
    }

    static void derEncodeToStream(ASN1Object aSN1Object, OutputStream outputStream) {
        try {
            aSN1Object.encodeTo(outputStream, "DER");
            outputStream.close();
        } catch (IOException iOException) {
            throw new CMPRuntimeException("unable to DER encode object: " + iOException.getMessage(), iOException);
        }
    }
}

