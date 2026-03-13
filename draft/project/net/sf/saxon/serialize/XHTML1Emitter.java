/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.serialize.XMLEmitter;

public class XHTML1Emitter
extends XMLEmitter {
    static Set<String> emptyTags1 = new HashSet<String>(31);
    private static String[] emptyTagNames1 = new String[]{"area", "base", "basefont", "br", "col", "embed", "frame", "hr", "img", "input", "isindex", "link", "meta", "param"};

    private boolean isRecognizedHtmlElement(NodeName name) {
        return name.hasURI("http://www.w3.org/1999/xhtml");
    }

    @Override
    protected String emptyElementTagCloser(String displayName, NodeName name) {
        if (this.isRecognizedHtmlElement(name) && emptyTags1.contains(name.getLocalPart())) {
            return " />";
        }
        return "></" + displayName + '>';
    }

    static {
        Collections.addAll(emptyTags1, emptyTagNames1);
    }
}

