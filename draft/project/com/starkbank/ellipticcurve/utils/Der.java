/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.starkbank.ellipticcurve.utils;

import com.starkbank.ellipticcurve.utils.Base64;
import com.starkbank.ellipticcurve.utils.BinaryAscii;
import com.starkbank.ellipticcurve.utils.ByteString;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

public class Der {
    private Der() {
        throw new UnsupportedOperationException("Der is a utility class and cannot be instantiated");
    }

    public static ByteString encodeSequence(ByteString ... encodedPieces) {
        int totalLen = 0;
        ByteString stringPieces = new ByteString(BinaryAscii.toBytes(48));
        for (ByteString p : encodedPieces) {
            totalLen += p.length();
            stringPieces.insert(p.getBytes());
        }
        stringPieces.insert(1, Der.encodeLength(totalLen).getBytes());
        return stringPieces;
    }

    public static ByteString encodeLength(int length) {
        assert (length >= 0);
        if (length < 128) {
            return new ByteString(BinaryAscii.toBytes(length));
        }
        String hexString = String.format("%x", length);
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }
        ByteString s = new ByteString(BinaryAscii.binaryFromHex(hexString));
        s.insert(0, BinaryAscii.toBytes(0x80 | s.length()));
        return s;
    }

    public static ByteString encodeInteger(BigInteger r) {
        ByteString s;
        short num;
        assert (r.compareTo(BigInteger.ZERO) >= 0);
        String h = String.format("%x", r);
        if (h.length() % 2 != 0) {
            h = "0" + h;
        }
        if ((num = (s = new ByteString(BinaryAscii.binaryFromHex(h))).getShort(0)) <= 127) {
            s.insert(0, BinaryAscii.toBytes(s.length()));
            s.insert(0, BinaryAscii.toBytes(2));
            return s;
        }
        int length = s.length();
        s.insert(0, BinaryAscii.toBytes(0));
        s.insert(0, BinaryAscii.toBytes(length + 1));
        s.insert(0, BinaryAscii.toBytes(2));
        return s;
    }

    public static ByteString encodeNumber(long n) {
        ByteString b128Digits = new ByteString();
        while (n != 0L) {
            b128Digits.insert(0, BinaryAscii.toBytes((int)(n & 0x7FL) | 0x80));
            n >>= 7;
        }
        if (b128Digits.isEmpty()) {
            b128Digits.insert(BinaryAscii.toBytes(0));
        }
        int lastIndex = b128Digits.length() - 1;
        b128Digits.replace(lastIndex, (byte)(b128Digits.getShort(lastIndex) & 0x7F));
        return b128Digits;
    }

    public static ByteString encodeOid(long ... pieces) {
        long first = pieces[0];
        long second = pieces[1];
        assert (first <= 2L);
        assert (second <= 39L);
        ByteString body = new ByteString();
        for (int i = 2; i < pieces.length; ++i) {
            body.insert(Der.encodeNumber(pieces[i]).getBytes());
        }
        body.insert(0, BinaryAscii.toBytes((int)(40L * first + second)));
        body.insert(0, Der.encodeLength(body.length()).getBytes());
        body.insert(0, BinaryAscii.toBytes(6));
        return body;
    }

    public static ByteString encodeBitString(ByteString s) {
        s.insert(0, Der.encodeLength(s.length()).getBytes());
        s.insert(0, BinaryAscii.toBytes(3));
        return s;
    }

    public static ByteString encodeOctetString(ByteString s) {
        s.insert(0, Der.encodeLength(s.length()).getBytes());
        s.insert(0, BinaryAscii.toBytes(4));
        return s;
    }

    public static ByteString encodeConstructed(long tag, ByteString value) {
        value.insert(0, Der.encodeLength(value.length()).getBytes());
        value.insert(0, BinaryAscii.toBytes((int)(160L + tag)));
        return value;
    }

    public static int[] readLength(ByteString string) {
        short num = string.getShort(0);
        if ((num & 0x80) == 0) {
            return new int[]{num & 0x7F, 1};
        }
        int llen = num & 0x7F;
        if (llen > string.length() - 1) {
            throw new RuntimeException("ran out of length bytes");
        }
        return new int[]{Integer.valueOf(BinaryAscii.hexFromBinary(string.substring(1, 1 + llen)), 16), 1 + llen};
    }

    public static int[] readNumber(ByteString string) {
        short d;
        int number = 0;
        int llen = 0;
        do {
            if (llen > string.length()) {
                throw new RuntimeException("ran out of length bytes");
            }
            number <<= 7;
            d = string.getShort(llen);
            number += d & 0x7F;
            ++llen;
        } while ((d & 0x80) != 0);
        return new int[]{number, llen};
    }

    public static ByteString[] removeSequence(ByteString string) {
        short n = string.getShort(0);
        if (n != 48) {
            throw new RuntimeException(String.format("wanted sequence (0x30), got 0x%02x", n));
        }
        int[] l = Der.readLength(string.substring(1));
        long endseq = 1 + l[0] + l[1];
        return new ByteString[]{string.substring(1 + l[1], (int)endseq), string.substring((int)endseq)};
    }

    public static Object[] removeInteger(ByteString string) {
        short n = string.getShort(0);
        if (n != 2) {
            throw new RuntimeException(String.format("wanted integer (0x02), got 0x%02x", n));
        }
        int[] l = Der.readLength(string.substring(1));
        int length = l[0];
        int llen = l[1];
        ByteString numberbytes = string.substring(1 + llen, 1 + llen + length);
        ByteString rest = string.substring(1 + llen + length);
        short nbytes = numberbytes.getShort(0);
        assert (nbytes < 128);
        return new Object[]{new BigInteger(BinaryAscii.hexFromBinary(numberbytes), 16), rest};
    }

    public static Object[] removeObject(ByteString string) {
        int n = string.getShort(0);
        if (n != 6) {
            throw new RuntimeException(String.format("wanted object (0x06), got 0x%02x", n));
        }
        int[] l = Der.readLength(string.substring(1));
        int length = l[0];
        int lengthlength = l[1];
        ByteString body = string.substring(1 + lengthlength, 1 + lengthlength + length);
        ByteString rest = string.substring(1 + lengthlength + length);
        ArrayList<Number> numbers = new ArrayList<Number>();
        while (!body.isEmpty()) {
            l = Der.readNumber(body);
            n = l[0];
            int ll = l[1];
            numbers.add(n);
            body = body.substring(ll);
        }
        long n0 = Integer.valueOf(numbers.remove(0).toString()).intValue();
        long first = n0 / 40L;
        long second = n0 - 40L * first;
        numbers.add(0, first);
        numbers.add(1, second);
        long[] numbersArray = new long[numbers.size()];
        for (int i = 0; i < numbers.size(); ++i) {
            numbersArray[i] = Long.valueOf(numbers.get(i).toString());
        }
        return new Object[]{numbersArray, rest};
    }

    public static ByteString[] removeBitString(ByteString string) {
        short n = string.getShort(0);
        if (n != 3) {
            throw new RuntimeException(String.format("wanted bitstring (0x03), got 0x%02x", n));
        }
        int[] l = Der.readLength(string.substring(1));
        int length = l[0];
        int llen = l[1];
        ByteString body = string.substring(1 + llen, 1 + llen + length);
        ByteString rest = string.substring(1 + llen + length);
        return new ByteString[]{body, rest};
    }

    public static ByteString[] removeOctetString(ByteString string) {
        short n = string.getShort(0);
        if (n != 4) {
            throw new RuntimeException(String.format("wanted octetstring (0x04), got 0x%02x", n));
        }
        int[] l = Der.readLength(string.substring(1));
        int length = l[0];
        int llen = l[1];
        ByteString body = string.substring(1 + llen, 1 + llen + length);
        ByteString rest = string.substring(1 + llen + length);
        return new ByteString[]{body, rest};
    }

    public static Object[] removeConstructed(ByteString string) {
        short s0 = string.getShort(0);
        if ((s0 & 0xE0) != 160) {
            throw new RuntimeException(String.format("wanted constructed tag (0xa0-0xbf), got 0x%02x", s0));
        }
        int tag = s0 & 0x1F;
        int[] l = Der.readLength(string.substring(1));
        int length = l[0];
        int llen = l[1];
        ByteString body = string.substring(1 + llen, 1 + llen + length);
        ByteString rest = string.substring(1 + llen + length);
        return new Object[]{tag, body, rest};
    }

    public static ByteString fromPem(String pem) {
        String[] pieces = pem.split("\n");
        StringBuilder d = new StringBuilder();
        for (String p : pieces) {
            if (p.isEmpty() || p.startsWith("-----")) continue;
            d.append(p.trim());
        }
        try {
            return new ByteString(Base64.decode(d.toString()));
        } catch (IOException e) {
            throw new IllegalArgumentException("Corrupted pem string! Could not decode base64 from it");
        }
    }

    public static String toPem(ByteString der, String name) {
        String b64 = Base64.encodeBytes(der.getBytes());
        StringBuilder lines = new StringBuilder();
        lines.append(String.format("-----BEGIN %s-----\n", name));
        for (int start = 0; start < b64.length(); start += 64) {
            int end = start + 64 > b64.length() ? b64.length() : start + 64;
            lines.append(String.format("%s\n", b64.substring(start, end)));
        }
        lines.append(String.format("-----END %s-----\n", name));
        return lines.toString();
    }
}

