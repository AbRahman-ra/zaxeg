/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.regex.BMPString;
import net.sf.saxon.regex.EmptyString;
import net.sf.saxon.regex.GeneralUnicodeString;
import net.sf.saxon.regex.LatinString;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Base64BinaryValue;

public abstract class UnicodeString
implements CharSequence,
Comparable<UnicodeString>,
AtomicMatchKey {
    private int cachedHash = -1;

    public static UnicodeString makeUnicodeString(CharSequence in) {
        if (in instanceof UnicodeString) {
            return (UnicodeString)in;
        }
        if (in.length() == 0) {
            return EmptyString.THE_INSTANCE;
        }
        int width = UnicodeString.getMaxWidth(in);
        if (width == 1) {
            return new LatinString(in);
        }
        if (width == 2) {
            return new BMPString(in);
        }
        return new GeneralUnicodeString(in);
    }

    public static UnicodeString makeUnicodeString(int[] in) {
        for (int ch : in) {
            if (ch <= 65535) continue;
            return new GeneralUnicodeString(in, 0, in.length);
        }
        FastStringBuffer fsb = new FastStringBuffer(in.length);
        for (int ch : in) {
            fsb.cat((char)ch);
        }
        return new BMPString(fsb);
    }

    public static boolean containsSurrogatePairs(CharSequence value) {
        if (value instanceof BMPString || value instanceof LatinString || value instanceof EmptyString) {
            return false;
        }
        if (value instanceof GeneralUnicodeString) {
            GeneralUnicodeString gus = (GeneralUnicodeString)value;
            for (int i = 0; i < gus.uLength(); ++i) {
                if (gus.uCharAt(i) < 65535) continue;
                return true;
            }
            return false;
        }
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            if (c < '\ud800' || c > '\udbff') continue;
            return true;
        }
        return false;
    }

    private static int getMaxWidth(CharSequence value) {
        if (value instanceof LatinString || value instanceof EmptyString) {
            return 1;
        }
        if (value instanceof BMPString) {
            return 2;
        }
        if (value instanceof GeneralUnicodeString) {
            return 4;
        }
        boolean nonLatin = false;
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            if (c > '\u00ff') {
                nonLatin = true;
            }
            if (c < '\ud800' || c > '\udbff') continue;
            return 4;
        }
        return nonLatin ? 2 : 1;
    }

    public abstract UnicodeString uSubstring(int var1, int var2);

    public abstract int uIndexOf(int var1, int var2);

    public abstract int uCharAt(int var1);

    public abstract int uLength();

    public abstract boolean isEnd(int var1);

    public int hashCode() {
        if (this.cachedHash == -1) {
            int h = 0;
            for (int i = 0; i < this.uLength(); ++i) {
                h = 31 * h + this.uCharAt(i);
            }
            this.cachedHash = h;
        }
        return this.cachedHash;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof UnicodeString)) {
            return false;
        }
        if (this.uLength() != ((UnicodeString)obj).uLength()) {
            return false;
        }
        for (int i = 0; i < this.uLength(); ++i) {
            if (this.uCharAt(i) == ((UnicodeString)obj).uCharAt(i)) continue;
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(UnicodeString other) {
        int nextb;
        int nexta;
        int c;
        int alen = this.uLength();
        int blen = other.uLength();
        int i = 0;
        int j = 0;
        do {
            if (i == alen) {
                if (j == blen) {
                    return 0;
                }
                return -1;
            }
            if (j != blen) continue;
            return 1;
        } while ((c = (nexta = this.uCharAt(i++)) - (nextb = other.uCharAt(j++))) == 0);
        return c;
    }

    private byte[] getCodepointCollationKey() {
        int len = this.uLength();
        byte[] result = new byte[len * 3];
        int j = 0;
        for (int i = 0; i < len; ++i) {
            int c = this.uCharAt(i);
            result[j++] = (byte)(c >> 16);
            result[j++] = (byte)(c >> 8);
            result[j++] = (byte)c;
        }
        return result;
    }

    @Override
    public AtomicValue asAtomic() {
        return new Base64BinaryValue(this.getCodepointCollationKey());
    }
}

