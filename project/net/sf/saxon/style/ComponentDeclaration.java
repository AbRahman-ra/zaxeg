/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetModule;

public class ComponentDeclaration {
    private StyleElement sourceElement;
    private StylesheetModule module;

    public ComponentDeclaration(StylesheetModule module, StyleElement source) {
        this.module = module;
        this.sourceElement = source;
    }

    public StylesheetModule getModule() {
        return this.module;
    }

    public StyleElement getSourceElement() {
        return this.sourceElement;
    }

    public int getPrecedence() {
        return this.module.getPrecedence();
    }
}

