/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.starkbank.ellipticcurve.utils;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RandomInteger {
    public static BigInteger between(BigInteger start, BigInteger end) {
        SecureRandom random = new SecureRandom();
        return new BigInteger(end.toByteArray().length * 8 - 1, random).abs().add(start);
    }
}

