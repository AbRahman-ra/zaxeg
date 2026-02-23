/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.pqc.crypto.lms.Composer;
import org.bouncycastle.pqc.crypto.lms.LMOtsParameters;
import org.bouncycastle.pqc.crypto.lms.LMOtsPublicKey;
import org.bouncycastle.pqc.crypto.lms.LMS;
import org.bouncycastle.pqc.crypto.lms.LMSContext;
import org.bouncycastle.pqc.crypto.lms.LMSContextBasedVerifier;
import org.bouncycastle.pqc.crypto.lms.LMSKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSParameters;
import org.bouncycastle.pqc.crypto.lms.LMSSignature;
import org.bouncycastle.pqc.crypto.lms.LMSigParameters;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public class LMSPublicKeyParameters
extends LMSKeyParameters
implements LMSContextBasedVerifier {
    private final LMSigParameters parameterSet;
    private final LMOtsParameters lmOtsType;
    private final byte[] I;
    private final byte[] T1;

    public LMSPublicKeyParameters(LMSigParameters lMSigParameters, LMOtsParameters lMOtsParameters, byte[] byArray, byte[] byArray2) {
        super(false);
        this.parameterSet = lMSigParameters;
        this.lmOtsType = lMOtsParameters;
        this.I = Arrays.clone(byArray2);
        this.T1 = Arrays.clone(byArray);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static LMSPublicKeyParameters getInstance(Object object) throws IOException {
        if (object instanceof LMSPublicKeyParameters) {
            return (LMSPublicKeyParameters)object;
        }
        if (object instanceof DataInputStream) {
            int n = ((DataInputStream)object).readInt();
            LMSigParameters lMSigParameters = LMSigParameters.getParametersForType(n);
            LMOtsParameters lMOtsParameters = LMOtsParameters.getParametersForType(((DataInputStream)object).readInt());
            byte[] byArray = new byte[16];
            ((DataInputStream)object).readFully(byArray);
            byte[] byArray2 = new byte[lMSigParameters.getM()];
            ((DataInputStream)object).readFully(byArray2);
            return new LMSPublicKeyParameters(lMSigParameters, lMOtsParameters, byArray2, byArray);
        }
        if (object instanceof byte[]) {
            InputStream inputStream = null;
            try {
                inputStream = new DataInputStream(new ByteArrayInputStream((byte[])object));
                LMSPublicKeyParameters lMSPublicKeyParameters = LMSPublicKeyParameters.getInstance(inputStream);
                return lMSPublicKeyParameters;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
        if (object instanceof InputStream) {
            return LMSPublicKeyParameters.getInstance(Streams.readAll((InputStream)object));
        }
        throw new IllegalArgumentException("cannot parse " + object);
    }

    public byte[] getEncoded() throws IOException {
        return this.toByteArray();
    }

    public LMSigParameters getSigParameters() {
        return this.parameterSet;
    }

    public LMOtsParameters getOtsParameters() {
        return this.lmOtsType;
    }

    public LMSParameters getLMSParameters() {
        return new LMSParameters(this.getSigParameters(), this.getOtsParameters());
    }

    public byte[] getT1() {
        return Arrays.clone(this.T1);
    }

    boolean matchesT1(byte[] byArray) {
        return Arrays.constantTimeAreEqual(this.T1, byArray);
    }

    public byte[] getI() {
        return Arrays.clone(this.I);
    }

    byte[] refI() {
        return this.I;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        LMSPublicKeyParameters lMSPublicKeyParameters = (LMSPublicKeyParameters)object;
        if (!this.parameterSet.equals(lMSPublicKeyParameters.parameterSet)) {
            return false;
        }
        if (!this.lmOtsType.equals(lMSPublicKeyParameters.lmOtsType)) {
            return false;
        }
        if (!Arrays.areEqual(this.I, lMSPublicKeyParameters.I)) {
            return false;
        }
        return Arrays.areEqual(this.T1, lMSPublicKeyParameters.T1);
    }

    public int hashCode() {
        int n = this.parameterSet.hashCode();
        n = 31 * n + this.lmOtsType.hashCode();
        n = 31 * n + Arrays.hashCode(this.I);
        n = 31 * n + Arrays.hashCode(this.T1);
        return n;
    }

    byte[] toByteArray() {
        return Composer.compose().u32str(this.parameterSet.getType()).u32str(this.lmOtsType.getType()).bytes(this.I).bytes(this.T1).build();
    }

    public LMSContext generateLMSContext(byte[] byArray) {
        try {
            return this.generateOtsContext(LMSSignature.getInstance(byArray));
        } catch (IOException iOException) {
            throw new IllegalStateException("cannot parse signature: " + iOException.getMessage());
        }
    }

    LMSContext generateOtsContext(LMSSignature lMSSignature) {
        int n = this.getOtsParameters().getType();
        if (lMSSignature.getOtsSignature().getType().getType() != n) {
            throw new IllegalArgumentException("ots type from lsm signature does not match ots signature type from embedded ots signature");
        }
        return new LMOtsPublicKey(LMOtsParameters.getParametersForType(n), this.I, lMSSignature.getQ(), null).createOtsContext(lMSSignature);
    }

    public boolean verify(LMSContext lMSContext) {
        return LMS.verifySignature(this, lMSContext);
    }
}

