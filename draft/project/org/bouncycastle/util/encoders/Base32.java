/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.util.encoders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Base32Encoder;
import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.Encoder;
import org.bouncycastle.util.encoders.EncoderException;

public class Base32 {
    private static final Encoder encoder = new Base32Encoder();

    public static String toBase32String(byte[] byArray) {
        return Base32.toBase32String(byArray, 0, byArray.length);
    }

    public static String toBase32String(byte[] byArray, int n, int n2) {
        byte[] byArray2 = Base32.encode(byArray, n, n2);
        return Strings.fromByteArray(byArray2);
    }

    public static byte[] encode(byte[] byArray) {
        return Base32.encode(byArray, 0, byArray.length);
    }

    public static byte[] encode(byte[] byArray, int n, int n2) {
        int n3 = encoder.getEncodedLength(n2);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(n3);
        try {
            encoder.encode(byArray, n, n2, byteArrayOutputStream);
        } catch (Exception exception) {
            throw new EncoderException("exception encoding base32 string: " + exception.getMessage(), exception);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static int encode(byte[] byArray, OutputStream outputStream) throws IOException {
        return encoder.encode(byArray, 0, byArray.length, outputStream);
    }

    public static int encode(byte[] byArray, int n, int n2, OutputStream outputStream) throws IOException {
        return encoder.encode(byArray, n, n2, outputStream);
    }

    public static byte[] decode(byte[] byArray) {
        int n = byArray.length / 8 * 5;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(n);
        try {
            encoder.decode(byArray, 0, byArray.length, byteArrayOutputStream);
        } catch (Exception exception) {
            throw new DecoderException("unable to decode base32 data: " + exception.getMessage(), exception);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] decode(String string) {
        int n = string.length() / 8 * 5;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(n);
        try {
            encoder.decode(string, byteArrayOutputStream);
        } catch (Exception exception) {
            throw new DecoderException("unable to decode base32 string: " + exception.getMessage(), exception);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static int decode(String string, OutputStream outputStream) throws IOException {
        return encoder.decode(string, outputStream);
    }

    public static int decode(byte[] byArray, int n, int n2, OutputStream outputStream) {
        try {
            return encoder.decode(byArray, n, n2, outputStream);
        } catch (Exception exception) {
            throw new DecoderException("unable to decode base32 data: " + exception.getMessage(), exception);
        }
    }
}

