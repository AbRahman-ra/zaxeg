/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.sphincs;

import org.bouncycastle.pqc.crypto.sphincs.SPHINCSKeyParameters;
import org.bouncycastle.util.Arrays;

public class SPHINCSPrivateKeyParameters
extends SPHINCSKeyParameters {
    private final byte[] keyData;

    public SPHINCSPrivateKeyParameters(byte[] byArray) {
        super(true, null);
        this.keyData = Arrays.clone(byArray);
    }

    public SPHINCSPrivateKeyParameters(byte[] byArray, String string) {
        super(true, string);
        this.keyData = Arrays.clone(byArray);
    }

    public byte[] getKeyData() {
        return Arrays.clone(this.keyData);
    }
}

