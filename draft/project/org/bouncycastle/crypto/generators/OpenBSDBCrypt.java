/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.generators;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.generators.BCrypt;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

public class OpenBSDBCrypt {
    private static final byte[] encodingTable;
    private static final byte[] decodingTable;
    private static final String defaultVersion = "2y";
    private static final Set<String> allowedVersions;

    private OpenBSDBCrypt() {
    }

    public static String generate(char[] cArray, byte[] byArray, int n) {
        return OpenBSDBCrypt.generate(defaultVersion, cArray, byArray, n);
    }

    public static String generate(byte[] byArray, byte[] byArray2, int n) {
        return OpenBSDBCrypt.generate(defaultVersion, byArray, byArray2, n);
    }

    public static String generate(String string, char[] cArray, byte[] byArray, int n) {
        if (cArray == null) {
            throw new IllegalArgumentException("Password required.");
        }
        return OpenBSDBCrypt.doGenerate(string, Strings.toUTF8ByteArray(cArray), byArray, n);
    }

    public static String generate(String string, byte[] byArray, byte[] byArray2, int n) {
        if (byArray == null) {
            throw new IllegalArgumentException("Password required.");
        }
        return OpenBSDBCrypt.doGenerate(string, Arrays.clone(byArray), byArray2, n);
    }

    private static String doGenerate(String string, byte[] byArray, byte[] byArray2, int n) {
        if (!allowedVersions.contains(string)) {
            throw new IllegalArgumentException("Version " + string + " is not accepted by this implementation.");
        }
        if (byArray2 == null) {
            throw new IllegalArgumentException("Salt required.");
        }
        if (byArray2.length != 16) {
            throw new DataLengthException("16 byte salt required: " + byArray2.length);
        }
        if (n < 4 || n > 31) {
            throw new IllegalArgumentException("Invalid cost factor.");
        }
        byte[] byArray3 = new byte[byArray.length >= 72 ? 72 : byArray.length + 1];
        if (byArray3.length > byArray.length) {
            System.arraycopy(byArray, 0, byArray3, 0, byArray.length);
        } else {
            System.arraycopy(byArray, 0, byArray3, 0, byArray3.length);
        }
        Arrays.fill(byArray, (byte)0);
        String string2 = OpenBSDBCrypt.createBcryptString(string, byArray3, byArray2, n);
        Arrays.fill(byArray3, (byte)0);
        return string2;
    }

    public static boolean checkPassword(String string, char[] cArray) {
        if (cArray == null) {
            throw new IllegalArgumentException("Missing password.");
        }
        return OpenBSDBCrypt.doCheckPassword(string, Strings.toUTF8ByteArray(cArray));
    }

    public static boolean checkPassword(String string, byte[] byArray) {
        if (byArray == null) {
            throw new IllegalArgumentException("Missing password.");
        }
        return OpenBSDBCrypt.doCheckPassword(string, Arrays.clone(byArray));
    }

    private static boolean doCheckPassword(String string, byte[] byArray) {
        int n;
        String string2;
        if (string == null) {
            throw new IllegalArgumentException("Missing bcryptString.");
        }
        if (string.charAt(1) != '2') {
            throw new IllegalArgumentException("not a Bcrypt string");
        }
        int n2 = string.length();
        if (n2 != 60 && (n2 != 59 || string.charAt(2) != '$')) {
            throw new DataLengthException("Bcrypt String length: " + n2 + ", 60 required.");
        }
        if (string.charAt(2) == '$' ? string.charAt(0) != '$' || string.charAt(5) != '$' : string.charAt(0) != '$' || string.charAt(3) != '$' || string.charAt(6) != '$') {
            throw new IllegalArgumentException("Invalid Bcrypt String format.");
        }
        if (string.charAt(2) == '$') {
            string2 = string.substring(1, 2);
            n = 3;
        } else {
            string2 = string.substring(1, 3);
            n = 4;
        }
        if (!allowedVersions.contains(string2)) {
            throw new IllegalArgumentException("Bcrypt version '" + string2 + "' is not supported by this implementation");
        }
        int n3 = 0;
        String string3 = string.substring(n, n + 2);
        try {
            n3 = Integer.parseInt(string3);
        } catch (NumberFormatException numberFormatException) {
            throw new IllegalArgumentException("Invalid cost factor: " + string3);
        }
        if (n3 < 4 || n3 > 31) {
            throw new IllegalArgumentException("Invalid cost factor: " + n3 + ", 4 < cost < 31 expected.");
        }
        byte[] byArray2 = OpenBSDBCrypt.decodeSaltString(string.substring(string.lastIndexOf(36) + 1, n2 - 31));
        String string4 = OpenBSDBCrypt.doGenerate(string2, byArray, byArray2, n3);
        return Strings.constantTimeAreEqual(string, string4);
    }

    private static String createBcryptString(String string, byte[] byArray, byte[] byArray2, int n) {
        if (!allowedVersions.contains(string)) {
            throw new IllegalArgumentException("Version " + string + " is not accepted by this implementation.");
        }
        StringBuilder stringBuilder = new StringBuilder(60);
        stringBuilder.append('$');
        stringBuilder.append(string);
        stringBuilder.append('$');
        stringBuilder.append(n < 10 ? "0" + n : Integer.toString(n));
        stringBuilder.append('$');
        OpenBSDBCrypt.encodeData(stringBuilder, byArray2);
        byte[] byArray3 = BCrypt.generate(byArray, byArray2, n);
        OpenBSDBCrypt.encodeData(stringBuilder, byArray3);
        return stringBuilder.toString();
    }

    private static void encodeData(StringBuilder stringBuilder, byte[] byArray) {
        if (byArray.length != 24 && byArray.length != 16) {
            throw new DataLengthException("Invalid length: " + byArray.length + ", 24 for key or 16 for salt expected");
        }
        boolean bl = false;
        if (byArray.length == 16) {
            bl = true;
            byte[] byArray2 = new byte[18];
            System.arraycopy(byArray, 0, byArray2, 0, byArray.length);
            byArray = byArray2;
        } else {
            byArray[byArray.length - 1] = 0;
        }
        int n = byArray.length;
        for (int i = 0; i < n; i += 3) {
            int n2 = byArray[i] & 0xFF;
            int n3 = byArray[i + 1] & 0xFF;
            int n4 = byArray[i + 2] & 0xFF;
            stringBuilder.append((char)encodingTable[n2 >>> 2 & 0x3F]);
            stringBuilder.append((char)encodingTable[(n2 << 4 | n3 >>> 4) & 0x3F]);
            stringBuilder.append((char)encodingTable[(n3 << 2 | n4 >>> 6) & 0x3F]);
            stringBuilder.append((char)encodingTable[n4 & 0x3F]);
        }
        if (bl) {
            stringBuilder.setLength(stringBuilder.length() - 2);
        } else {
            stringBuilder.setLength(stringBuilder.length() - 1);
        }
    }

    private static byte[] decodeSaltString(String string) {
        int n;
        char[] cArray = string.toCharArray();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(16);
        if (cArray.length != 22) {
            throw new DataLengthException("Invalid base64 salt length: " + cArray.length + " , 22 required.");
        }
        for (int i = 0; i < cArray.length; ++i) {
            n = cArray[i];
            if (n <= 122 && n >= 46 && (n <= 57 || n >= 65)) continue;
            throw new IllegalArgumentException("Salt string contains invalid character: " + n);
        }
        char[] cArray2 = new char[24];
        System.arraycopy(cArray, 0, cArray2, 0, cArray.length);
        cArray = cArray2;
        n = cArray.length;
        for (int i = 0; i < n; i += 4) {
            byte by = decodingTable[cArray[i]];
            byte by2 = decodingTable[cArray[i + 1]];
            byte by3 = decodingTable[cArray[i + 2]];
            byte by4 = decodingTable[cArray[i + 3]];
            byteArrayOutputStream.write(by << 2 | by2 >> 4);
            byteArrayOutputStream.write(by2 << 4 | by3 >> 2);
            byteArrayOutputStream.write(by3 << 6 | by4);
        }
        byte[] byArray = byteArrayOutputStream.toByteArray();
        byte[] byArray2 = new byte[16];
        System.arraycopy(byArray, 0, byArray2, 0, byArray2.length);
        byArray = byArray2;
        return byArray;
    }

    static {
        int n;
        encodingTable = new byte[]{46, 47, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57};
        decodingTable = new byte[128];
        allowedVersions = new HashSet<String>();
        allowedVersions.add("2");
        allowedVersions.add("2x");
        allowedVersions.add("2a");
        allowedVersions.add(defaultVersion);
        allowedVersions.add("2b");
        for (n = 0; n < decodingTable.length; ++n) {
            OpenBSDBCrypt.decodingTable[n] = -1;
        }
        for (n = 0; n < encodingTable.length; ++n) {
            OpenBSDBCrypt.decodingTable[OpenBSDBCrypt.encodingTable[n]] = (byte)n;
        }
    }
}

