/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.sapling;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.sapling.SaplingComment;
import net.sf.saxon.sapling.SaplingDocument;
import net.sf.saxon.sapling.SaplingElement;
import net.sf.saxon.sapling.SaplingProcessingInstruction;
import net.sf.saxon.sapling.SaplingText;

public class Saplings {
    private Saplings() {
    }

    public static SaplingDocument doc() {
        return new SaplingDocument();
    }

    public static SaplingDocument doc(String baseUri) {
        return new SaplingDocument(baseUri);
    }

    public static SaplingElement elem(String name) {
        return new SaplingElement(name);
    }

    public static SaplingElement elem(QName qName) {
        return new SaplingElement(qName);
    }

    public static SaplingText text(String value) {
        return new SaplingText(value);
    }

    public static SaplingComment comment(String value) {
        return new SaplingComment(value);
    }

    public static SaplingProcessingInstruction pi(String target, String data) {
        return new SaplingProcessingInstruction(target, data);
    }
}

