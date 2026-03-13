/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.util;

import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.crypto.AlphabetMapper;

public class BasicAlphabetMapper
implements AlphabetMapper {
    private Map<Character, Integer> indexMap = new HashMap<Character, Integer>();
    private Map<Integer, Character> charMap = new HashMap<Integer, Character>();

    public BasicAlphabetMapper(String string) {
        this(string.toCharArray());
    }

    public BasicAlphabetMapper(char[] cArray) {
        for (int i = 0; i != cArray.length; ++i) {
            if (this.indexMap.containsKey(Character.valueOf(cArray[i]))) {
                throw new IllegalArgumentException("duplicate key detected in alphabet: " + cArray[i]);
            }
            this.indexMap.put(Character.valueOf(cArray[i]), i);
            this.charMap.put(i, Character.valueOf(cArray[i]));
        }
    }

    public int getRadix() {
        return this.indexMap.size();
    }

    public byte[] convertToIndexes(char[] cArray) {
        byte[] byArray;
        if (this.indexMap.size() <= 256) {
            byArray = new byte[cArray.length];
            for (int i = 0; i != cArray.length; ++i) {
                byArray[i] = this.indexMap.get(Character.valueOf(cArray[i])).byteValue();
            }
        } else {
            byArray = new byte[cArray.length * 2];
            for (int i = 0; i != cArray.length; ++i) {
                int n = this.indexMap.get(Character.valueOf(cArray[i]));
                byArray[i * 2] = (byte)(n >> 8 & 0xFF);
                byArray[i * 2 + 1] = (byte)(n & 0xFF);
            }
        }
        return byArray;
    }

    public char[] convertToChars(byte[] byArray) {
        char[] cArray;
        if (this.charMap.size() <= 256) {
            cArray = new char[byArray.length];
            for (int i = 0; i != byArray.length; ++i) {
                cArray[i] = this.charMap.get(byArray[i] & 0xFF).charValue();
            }
        } else {
            if ((byArray.length & 1) != 0) {
                throw new IllegalArgumentException("two byte radix and input string odd length");
            }
            cArray = new char[byArray.length / 2];
            for (int i = 0; i != byArray.length; i += 2) {
                cArray[i / 2] = this.charMap.get(byArray[i] << 8 & 0xFF00 | byArray[i + 1] & 0xFF).charValue();
            }
        }
        return cArray;
    }
}

