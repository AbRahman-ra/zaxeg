/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.SourceBinding;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;

public abstract class XSLGeneralVariable
extends StyleElement {
    protected SourceBinding sourceBinding = new SourceBinding(this);

    public SourceBinding getSourceBinding() {
        return this.sourceBinding;
    }

    public StructuredQName getVariableQName() {
        return this.sourceBinding.getVariableQName();
    }

    @Override
    public StructuredQName getObjectName() {
        return this.sourceBinding.getVariableQName();
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    public boolean isGlobal() {
        return this.isTopLevel();
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.sourceBinding.validate();
    }

    @Override
    public void postValidate() throws XPathException {
        this.sourceBinding.postValidate();
    }
}

