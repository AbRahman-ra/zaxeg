/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize.charcode;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import net.sf.saxon.serialize.charcode.CharacterSet;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.tree.tiny.CharSlice;

public class JavaCharacterSet
implements CharacterSet {
    public static HashMap<Charset, JavaCharacterSet> map;
    private CharsetEncoder encoder;
    private byte[] charinfo = new byte[65536];
    private static final byte GOOD = 1;
    private static final byte BAD = 2;

    private JavaCharacterSet(Charset charset) {
        this.encoder = charset.newEncoder();
    }

    public static synchronized JavaCharacterSet makeCharSet(Charset charset) {
        JavaCharacterSet c;
        if (map == null) {
            map = new HashMap(10);
        }
        if ((c = map.get(charset)) == null) {
            c = new JavaCharacterSet(charset);
            map.put(charset, c);
        }
        return c;
    }

    @Override
    public final boolean inCharset(int c) {
        if (c <= 127) {
            return true;
        }
        if (c <= 65535) {
            if (this.charinfo[c] == 1) {
                return true;
            }
            if (this.charinfo[c] == 2) {
                return false;
            }
            if (this.encoder.canEncode((char)c)) {
                this.charinfo[c] = 1;
                return true;
            }
            this.charinfo[c] = 2;
            return false;
        }
        char[] cc = new char[]{UTF16CharacterSet.highSurrogate(c), UTF16CharacterSet.lowSurrogate(c)};
        return this.encoder.canEncode(new CharSlice(cc));
    }

    @Override
    public String getCanonicalName() {
        return this.encoder.charset().name();
    }
}

