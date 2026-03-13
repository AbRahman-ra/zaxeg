/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.apache.log4j.Logger;

public class EcryptionUtils {
    private static final Logger LOG = Logger.getLogger(EcryptionUtils.class);

    public static String encodeBase64(String decodedInpupt) {
        return Base64.getEncoder().encodeToString(decodedInpupt.getBytes());
    }

    public static String encodeBytesBase64(byte[] decodedInpupt) {
        return Base64.getEncoder().encodeToString(decodedInpupt);
    }

    public static String decodeBase64(String encodedInpupt) {
        return new String(Base64.getDecoder().decode(encodedInpupt));
    }

    public static String hashString(String input) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return EcryptionUtils.bytesToHex(hash);
    }

    public static byte[] hashString(byte[] input) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input);
        return hash;
    }

    public static byte[] hashStringToBytes(String input) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return hash;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; ++i) {
            String hex = Integer.toHexString(0xFF & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

