/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.signers;

import java.io.IOException;
import java.math.BigInteger;

public interface DSAEncoding {
    public BigInteger[] decode(BigInteger var1, byte[] var2) throws IOException;

    public byte[] encode(BigInteger var1, BigInteger var2, BigInteger var3) throws IOException;
}

