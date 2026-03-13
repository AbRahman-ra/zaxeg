/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize.codenorm;

import net.sf.saxon.Configuration;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.serialize.codenorm.NormalizerData;
import net.sf.saxon.serialize.codenorm.UnicodeDataParserFromXML;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;

public class Normalizer {
    private int form;
    static final int COMPATIBILITY_MASK = 1;
    static final int COMPOSITION_MASK = 2;
    public static final int D = 0;
    public static final int C = 2;
    public static final int KD = 1;
    public static final int KC = 3;
    public static final int NO_ACTION = 8;
    private static NormalizerData data = null;

    private Normalizer(int form) {
        this.form = form;
    }

    public static synchronized Normalizer make(int form, Configuration config) throws XPathException {
        if (data == null) {
            data = UnicodeDataParserFromXML.build(config);
        }
        return new Normalizer(form);
    }

    public CharSequence normalize(CharSequence source) {
        if (this.form == 8 || source.length() == 0) {
            return source;
        }
        FastStringBuffer target = new FastStringBuffer(source.length() + 8);
        this.internalDecompose(source, target);
        if ((this.form & 2) != 0) {
            this.internalCompose(target);
        }
        return target;
    }

    private void internalDecompose(CharSequence source, FastStringBuffer target) {
        FastStringBuffer buffer = new FastStringBuffer(8);
        boolean canonical = (this.form & 1) == 0;
        int i = 0;
        while (i < source.length()) {
            buffer.setLength(0);
            int ch32 = source.charAt(i++);
            if (ch32 < 128) {
                target.cat((char)ch32);
                continue;
            }
            if (UTF16CharacterSet.isHighSurrogate(ch32)) {
                char low = source.charAt(i++);
                ch32 = UTF16CharacterSet.combinePair((char)ch32, low);
            }
            data.getRecursiveDecomposition(canonical, ch32, buffer);
            int j = 0;
            while (j < buffer.length()) {
                int k;
                int ch;
                if (UTF16CharacterSet.isHighSurrogate(ch = buffer.charAt(j++))) {
                    char low = buffer.charAt(j++);
                    ch = UTF16CharacterSet.combinePair((char)ch, low);
                }
                int chClass = data.getCanonicalClass(ch);
                if (chClass != 0) {
                    int step;
                    for (k = target.length(); k > 0; k -= step) {
                        step = 1;
                        int ch2 = target.charAt(k - 1);
                        if (UTF16CharacterSet.isSurrogate(ch2)) {
                            step = 2;
                            char high = target.charAt(k - 2);
                            ch2 = UTF16CharacterSet.combinePair(high, (char)ch2);
                        }
                        if (data.getCanonicalClass(ch2) <= chClass) break;
                    }
                }
                if (ch < 65536) {
                    target.insert(k, (char)ch);
                    continue;
                }
                target.insertWideChar(k, ch);
            }
        }
    }

    private void internalCompose(FastStringBuffer target) {
        int lastClass;
        int starterPos = 0;
        int starterCh = target.charAt(0);
        int compPos = 1;
        if (UTF16CharacterSet.isHighSurrogate(starterCh)) {
            starterCh = UTF16CharacterSet.combinePair((char)starterCh, target.charAt(1));
            ++compPos;
        }
        if ((lastClass = data.getCanonicalClass(starterCh)) != 0) {
            lastClass = 256;
        }
        int oldLen = target.length();
        int decompPos = compPos;
        while (decompPos < target.length()) {
            int ch;
            if (UTF16CharacterSet.isHighSurrogate(ch = target.charAt(decompPos++))) {
                ch = UTF16CharacterSet.combinePair((char)ch, target.charAt(decompPos++));
            }
            int chClass = data.getCanonicalClass(ch);
            char composite = data.getPairwiseComposition(starterCh, ch);
            if (composite != '\uffff' && (lastClass < chClass || lastClass == 0)) {
                Normalizer.setCharAt(target, starterPos, composite);
                starterCh = composite;
                continue;
            }
            if (chClass == 0) {
                starterPos = compPos;
                starterCh = ch;
            }
            lastClass = chClass;
            Normalizer.setCharAt(target, compPos, ch);
            if (target.length() != oldLen) {
                decompPos += target.length() - oldLen;
                oldLen = target.length();
            }
            compPos += ch < 65536 ? 1 : 2;
        }
        target.setLength(compPos);
    }

    private static void setCharAt(FastStringBuffer target, int offset, int ch32) {
        if (ch32 < 65536) {
            if (UTF16CharacterSet.isHighSurrogate(target.charAt(offset))) {
                target.setCharAt(offset, (char)ch32);
                target.removeCharAt(offset + 1);
            } else {
                target.setCharAt(offset, (char)ch32);
            }
        } else if (UTF16CharacterSet.isHighSurrogate(target.charAt(offset))) {
            target.setCharAt(offset, UTF16CharacterSet.highSurrogate(ch32));
            target.setCharAt(offset + 1, UTF16CharacterSet.lowSurrogate(ch32));
        } else {
            target.setCharAt(offset, UTF16CharacterSet.highSurrogate(ch32));
            target.insert(offset + 1, UTF16CharacterSet.lowSurrogate(ch32));
        }
    }
}

