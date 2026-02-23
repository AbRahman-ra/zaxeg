/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.c14n.implementations;

import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class UtfHelpper {
    private static final boolean oldUtf8 = AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

        @Override
        public Boolean run() {
            return Boolean.getBoolean("org.apache.xml.security.c14n.oldUtf8");
        }
    });

    private UtfHelpper() {
    }

    public static void writeByte(String str, OutputStream out, Map<String, byte[]> cache) throws IOException {
        byte[] result = cache.get(str);
        if (result == null) {
            result = UtfHelpper.getStringInUtf8(str);
            cache.put(str, result);
        }
        out.write(result);
    }

    public static void writeCodePointToUtf8(int c, OutputStream out) throws IOException {
        if (!Character.isValidCodePoint(c) || c >= 55296 && c <= 56319 || c >= 56320 && c <= 57343) {
            out.write(63);
            return;
        }
        if (oldUtf8 && c >= 65536) {
            out.write(63);
            out.write(63);
            return;
        }
        if (c < 128) {
            out.write(c);
            return;
        }
        int extraByte = 0;
        if (c < 2048) {
            extraByte = 1;
        } else if (c < 65536) {
            extraByte = 2;
        } else if (c < 0x200000) {
            extraByte = 3;
        } else if (c < 0x4000000) {
            extraByte = 4;
        } else if (c <= Integer.MAX_VALUE) {
            extraByte = 5;
        } else {
            out.write(63);
            return;
        }
        int shift = 6 * extraByte;
        byte write = (byte)(254 << 6 - extraByte | c >>> shift);
        out.write(write);
        for (int i = extraByte - 1; i >= 0; --i) {
            write = (byte)(0x80 | c >>> (shift -= 6) & 0x3F);
            out.write(write);
        }
    }

    public static void writeStringToUtf8(String str, OutputStream out) throws IOException {
        int length = str.length();
        int i = 0;
        while (i < length) {
            int c = str.codePointAt(i);
            i += Character.charCount(c);
            if (!Character.isValidCodePoint(c) || c >= 55296 && c <= 56319 || c >= 56320 && c <= 57343) {
                out.write(63);
                continue;
            }
            if (oldUtf8 && c >= 65536) {
                out.write(63);
                out.write(63);
                continue;
            }
            if (c < 128) {
                out.write(c);
                continue;
            }
            int extraByte = 0;
            if (c < 2048) {
                extraByte = 1;
            } else if (c < 65536) {
                extraByte = 2;
            } else if (c < 0x200000) {
                extraByte = 3;
            } else if (c < 0x4000000) {
                extraByte = 4;
            } else if (c <= Integer.MAX_VALUE) {
                extraByte = 5;
            } else {
                out.write(63);
                continue;
            }
            int shift = 6 * extraByte;
            byte write = (byte)(254 << 6 - extraByte | c >>> shift);
            out.write(write);
            for (int j = extraByte - 1; j >= 0; --j) {
                write = (byte)(0x80 | c >>> (shift -= 6) & 0x3F);
                out.write(write);
            }
        }
    }

    public static byte[] getStringInUtf8(String str) {
        int length = str.length();
        boolean expanded = false;
        byte[] result = new byte[length];
        int i = 0;
        int out = 0;
        while (i < length) {
            int c = str.codePointAt(i);
            i += Character.charCount(c);
            if (!Character.isValidCodePoint(c) || c >= 55296 && c <= 56319 || c >= 56320 && c <= 57343) {
                result[out++] = 63;
                continue;
            }
            if (oldUtf8 && c >= 65536) {
                result[out++] = 63;
                result[out++] = 63;
                continue;
            }
            if (c < 128) {
                result[out++] = (byte)c;
                continue;
            }
            if (!expanded) {
                byte[] newResult = new byte[6 * length];
                System.arraycopy(result, 0, newResult, 0, out);
                result = newResult;
                expanded = true;
            }
            int extraByte = 0;
            if (c < 2048) {
                extraByte = 1;
            } else if (c < 65536) {
                extraByte = 2;
            } else if (c < 0x200000) {
                extraByte = 3;
            } else if (c < 0x4000000) {
                extraByte = 4;
            } else if (c <= Integer.MAX_VALUE) {
                extraByte = 5;
            } else {
                result[out++] = 63;
                continue;
            }
            int shift = 6 * extraByte;
            byte write = (byte)(254 << 6 - extraByte | c >>> shift);
            result[out++] = write;
            for (int j = extraByte - 1; j >= 0; --j) {
                write = (byte)(0x80 | c >>> (shift -= 6) & 0x3F);
                result[out++] = write;
            }
        }
        if (expanded) {
            byte[] newResult = new byte[out];
            System.arraycopy(result, 0, newResult, 0, out);
            result = newResult;
        }
        return result;
    }
}

