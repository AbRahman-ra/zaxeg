/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto;

public interface AlphabetMapper {
    public int getRadix();

    public byte[] convertToIndexes(char[] var1);

    public char[] convertToChars(byte[] var1);
}

