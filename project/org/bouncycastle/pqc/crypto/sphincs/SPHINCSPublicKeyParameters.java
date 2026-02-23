/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.sphincs;

import org.bouncycastle.pqc.crypto.sphincs.SPHINCSKeyParameters;
import org.bouncycastle.util.Arrays;

public class SPHINCSPublicKeyParameters
extends SPHINCSKeyParameters {
    private final byte[] keyData;

    public SPHINCSPublicKeyParameters(byte[] byArray) {
        super(false, null);
        this.keyData = Arrays.clone(byArray);
    }

    public SPHINCSPublicKeyParameters(byte[] byArray, String string) {
        super(false, string);
        this.keyData = Arrays.clone(byArray);
    }

    public byte[] getKeyData() {
        return Arrays.clone(this.keyData);
    }
}

