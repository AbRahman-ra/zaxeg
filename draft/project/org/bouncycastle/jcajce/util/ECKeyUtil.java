/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.util;

import java.io.IOException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ECPoint;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECCurve;

public class ECKeyUtil {
    public static ECPublicKey createKeyWithCompression(ECPublicKey eCPublicKey) {
        return new ECPublicKeyWithCompression(eCPublicKey);
    }

    private static class ECPublicKeyWithCompression
    implements ECPublicKey {
        private final ECPublicKey ecPublicKey;

        public ECPublicKeyWithCompression(ECPublicKey eCPublicKey) {
            this.ecPublicKey = eCPublicKey;
        }

        public ECPoint getW() {
            return this.ecPublicKey.getW();
        }

        public String getAlgorithm() {
            return this.ecPublicKey.getAlgorithm();
        }

        public String getFormat() {
            return this.ecPublicKey.getFormat();
        }

        public byte[] getEncoded() {
            ECCurve eCCurve;
            ASN1Object aSN1Object;
            Object object;
            SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(this.ecPublicKey.getEncoded());
            X962Parameters x962Parameters = X962Parameters.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            if (x962Parameters.isNamedCurve()) {
                object = (ASN1ObjectIdentifier)x962Parameters.getParameters();
                aSN1Object = CustomNamedCurves.getByOID((ASN1ObjectIdentifier)object);
                if (aSN1Object == null) {
                    aSN1Object = ECNamedCurveTable.getByOID((ASN1ObjectIdentifier)object);
                }
                eCCurve = ((X9ECParameters)aSN1Object).getCurve();
            } else {
                if (x962Parameters.isImplicitlyCA()) {
                    throw new IllegalStateException("unable to identify implictlyCA");
                }
                object = X9ECParameters.getInstance(x962Parameters.getParameters());
                eCCurve = ((X9ECParameters)object).getCurve();
            }
            object = eCCurve.decodePoint(subjectPublicKeyInfo.getPublicKeyData().getOctets());
            aSN1Object = ASN1OctetString.getInstance(new X9ECPoint((org.bouncycastle.math.ec.ECPoint)object, true).toASN1Primitive());
            try {
                return new SubjectPublicKeyInfo(subjectPublicKeyInfo.getAlgorithm(), ((ASN1OctetString)aSN1Object).getOctets()).getEncoded();
            } catch (IOException iOException) {
                throw new IllegalStateException("unable to encode EC public key: " + iOException.getMessage());
            }
        }

        public ECParameterSpec getParams() {
            return this.ecPublicKey.getParams();
        }
    }
}

