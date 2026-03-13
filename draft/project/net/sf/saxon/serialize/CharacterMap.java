/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.tree.tiny.CompressedWhitespace;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.Whitespace;
import net.sf.saxon.z.IntHashMap;
import net.sf.saxon.z.IntIterator;

public class CharacterMap {
    private StructuredQName name;
    private IntHashMap<String> charMap;
    private int min = Integer.MAX_VALUE;
    private int max = 0;
    private boolean mapsWhitespace = false;

    public CharacterMap(StructuredQName name, IntHashMap<String> map) {
        this.name = name;
        this.charMap = map;
        this.init();
    }

    public CharacterMap(Iterable<CharacterMap> list) {
        this.charMap = new IntHashMap(64);
        for (CharacterMap map : list) {
            IntIterator keys = map.charMap.keyIterator();
            while (keys.hasNext()) {
                int next = keys.next();
                this.charMap.put(next, map.charMap.get(next));
            }
        }
        this.init();
    }

    private void init() {
        IntIterator keys = this.charMap.keyIterator();
        while (keys.hasNext()) {
            int next = keys.next();
            if (next < this.min) {
                this.min = next;
            }
            if (next > this.max) {
                this.max = next;
            }
            if (this.mapsWhitespace || !Whitespace.isWhitespace(next)) continue;
            this.mapsWhitespace = true;
        }
        if (this.min > 55296) {
            this.min = 55296;
        }
    }

    public StructuredQName getName() {
        return this.name;
    }

    public CharSequence map(CharSequence in, boolean insertNulls) {
        if (!this.mapsWhitespace && in instanceof CompressedWhitespace) {
            return in;
        }
        boolean move = false;
        int i = 0;
        while (i < in.length()) {
            char c;
            if ((c = in.charAt(i++)) < this.min || c > this.max) continue;
            move = true;
            break;
        }
        if (!move) {
            return in;
        }
        FastStringBuffer buffer = new FastStringBuffer(in.length() * 2);
        int i2 = 0;
        while (i2 < in.length()) {
            char c;
            if ((c = in.charAt(i2++)) >= this.min && c <= this.max) {
                if (UTF16CharacterSet.isHighSurrogate(c)) {
                    char d;
                    int s;
                    String rep;
                    if ((rep = this.charMap.get(s = UTF16CharacterSet.combinePair(c, d = in.charAt(i2++)))) == null) {
                        buffer.cat(c);
                        buffer.cat(d);
                        continue;
                    }
                    if (insertNulls) {
                        buffer.cat('\u0000');
                        buffer.append(rep);
                        buffer.cat('\u0000');
                        continue;
                    }
                    buffer.append(rep);
                    continue;
                }
                String rep = this.charMap.get(c);
                if (rep == null) {
                    buffer.cat(c);
                    continue;
                }
                if (insertNulls) {
                    buffer.cat('\u0000');
                    buffer.append(rep);
                    buffer.cat('\u0000');
                    continue;
                }
                buffer.append(rep);
                continue;
            }
            buffer.cat(c);
        }
        return buffer;
    }

    public void export(ExpressionPresenter out) {
        out.startElement("charMap");
        out.emitAttribute("name", this.name);
        IntIterator iter = this.charMap.keyIterator();
        while (iter.hasNext()) {
            int c = iter.next();
            String s = this.charMap.get(c);
            out.startElement("m");
            out.emitAttribute("c", c + "");
            out.emitAttribute("s", s);
            out.endElement();
        }
        out.endElement();
    }
}

