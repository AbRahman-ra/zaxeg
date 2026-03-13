/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;

public abstract class XSLModuleRoot
extends StyleElement {
    public static final int ANNOTATION_UNSPECIFIED = 0;
    public static final int ANNOTATION_STRIP = 1;
    public static final int ANNOTATION_PRESERVE = 2;

    public boolean isDeclaredModes() {
        return false;
    }

    @Override
    public void processAllAttributes() throws XPathException {
        this.prepareAttributes();
        for (NodeInfo nodeInfo : this.children(StyleElement.class::isInstance)) {
            try {
                ((StyleElement)nodeInfo).processAllAttributes();
            } catch (XPathException err) {
                ((StyleElement)nodeInfo).compileError(err);
            }
        }
    }

    @Override
    public void index(ComponentDeclaration decl, PrincipalStylesheetModule top) {
        this.compileError(this.getDisplayName() + " can appear only as the outermost element", "XTSE0010");
    }

    public int getInputTypeAnnotationsAttribute() {
        String inputTypeAnnotationsAtt = this.getAttributeValue("", "input-type-annotations");
        if (inputTypeAnnotationsAtt != null) {
            switch (inputTypeAnnotationsAtt) {
                case "strip": {
                    return 1;
                }
                case "preserve": {
                    return 2;
                }
                case "unspecified": {
                    return 0;
                }
            }
            this.compileError("Invalid value for input-type-annotations attribute. Permitted values are (strip, preserve, unspecified)", "XTSE0020");
            return 0;
        }
        return -1;
    }
}

