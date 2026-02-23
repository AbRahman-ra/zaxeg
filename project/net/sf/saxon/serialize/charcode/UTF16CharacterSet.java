/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize.charcode;

import java.util.function.IntPredicate;
import net.sf.saxon.serialize.charcode.CharacterSet;

public class UTF16CharacterSet
implements CharacterSet {
    private static UTF16CharacterSet theInstance = new UTF16CharacterSet();
    public static final int NONBMP_MIN = 65536;
    public static final int NONBMP_MAX = 0x10FFFF;
    public static final char SURROGATE1_MIN = '\ud800';
    public static final char SURROGATE1_MAX = '\udbff';
    public static final char SURROGATE2_MIN = '\udc00';
    public static final char SURROGATE2_MAX = '\udfff';

    private UTF16CharacterSet() {
    }

    public static UTF16CharacterSet getInstance() {
        return theInstance;
    }

    @Override
    public boolean inCharset(int c) {
        return true;
    }

    @Override
    public String getCanonicalName() {
        return "UTF-16";
    }

    public static int combinePair(char high, char low) {
        return (high - 55296) * 1024 + (low - 56320) + 65536;
    }

    public static char highSurrogate(int ch) {
        return (char)((ch - 65536 >> 10) + 55296);
    }

    public static char lowSurrogate(int ch) {
        return (char)((ch - 65536 & 0x3FF) + 56320);
    }

    public static boolean isSurrogate(int c) {
        return (c & 0xF800) == 55296;
    }

    public static boolean isHighSurrogate(int ch) {
        return 55296 <= ch && ch <= 56319;
    }

    public static boolean isLowSurrogate(int ch) {
        return 56320 <= ch && ch <= 57343;
    }

    public static boolean containsSurrogates(CharSequence s) {
        for (int i = 0; i < s.length(); ++i) {
            if (!UTF16CharacterSet.isSurrogate(s.charAt(i))) continue;
            return true;
        }
        return false;
    }

    public static int firstInvalidChar(CharSequence chars, IntPredicate predicate) {
        for (int c = 0; c < chars.length(); ++c) {
            int ch32 = chars.charAt(c);
            if (UTF16CharacterSet.isHighSurrogate(ch32)) {
                char low = chars.charAt(c++);
                ch32 = UTF16CharacterSet.combinePair((char)ch32, low);
            }
            if (predicate.test(ch32)) continue;
            return ch32;
        }
        return -1;
    }

    public static void main(String[] args) {
        System.err.println(Integer.toHexString(UTF16CharacterSet.highSurrogate(983039)));
        System.err.println(Integer.toHexString(UTF16CharacterSet.lowSurrogate(983039)));
    }
}

