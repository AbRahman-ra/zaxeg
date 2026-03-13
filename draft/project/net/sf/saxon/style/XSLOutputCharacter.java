/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLCharacterMap;
import net.sf.saxon.trans.XPathException;

public class XSLOutputCharacter
extends StyleElement {
    private int codepoint = -1;
    private String replacementString = null;

    @Override
    public void prepareAttributes() {
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            if (f.equals("character")) {
                String s = value;
                switch (s.length()) {
                    case 0: {
                        this.compileError("character attribute must not be zero-length", "XTSE0020");
                        this.codepoint = 256;
                        break;
                    }
                    case 1: {
                        this.codepoint = s.charAt(0);
                        break;
                    }
                    case 2: {
                        if (UTF16CharacterSet.isHighSurrogate(s.charAt(0)) && UTF16CharacterSet.isLowSurrogate(s.charAt(1))) {
                            this.codepoint = UTF16CharacterSet.combinePair(s.charAt(0), s.charAt(1));
                            break;
                        }
                        this.compileError("character attribute must be a single XML character", "XTSE0020");
                        this.codepoint = 256;
                        break;
                    }
                    default: {
                        this.compileError("character attribute must be a single XML character", "XTSE0020");
                        this.codepoint = 256;
                        break;
                    }
                }
                continue;
            }
            if (f.equals("string")) {
                this.replacementString = value;
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (this.codepoint == -1) {
            this.reportAbsence("character");
            return;
        }
        if (this.replacementString == null) {
            this.reportAbsence("string");
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        if (!(this.getParent() instanceof XSLCharacterMap)) {
            this.compileError("xsl:output-character may appear only as a child of xsl:character-map", "XTSE0010");
        }
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        return null;
    }

    public int getCodePoint() {
        return this.codepoint;
    }

    public String getReplacementString() {
        return this.replacementString;
    }
}

