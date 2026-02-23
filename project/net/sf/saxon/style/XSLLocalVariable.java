/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.EnumSet;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.SourceBinding;
import net.sf.saxon.style.XSLGeneralVariable;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

public class XSLLocalVariable
extends XSLGeneralVariable {
    private static EnumSet<SourceBinding.BindingProperty> permittedAttributes = EnumSet.of(SourceBinding.BindingProperty.SELECT, SourceBinding.BindingProperty.AS);

    @Override
    public SourceBinding getBindingInformation(StructuredQName name) {
        if (name.equals(this.sourceBinding.getVariableQName())) {
            return this.sourceBinding;
        }
        return null;
    }

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        this.sourceBinding.prepareAttributes(permittedAttributes);
    }

    public SequenceType getRequiredType() {
        return this.sourceBinding.getInferredType(true);
    }

    @Override
    public void fixupReferences() throws XPathException {
        this.sourceBinding.fixupReferences(null);
        super.fixupReferences();
    }

    public void compileLocalVariable(Compilation exec, ComponentDeclaration decl) throws XPathException {
        this.sourceBinding.handleSequenceConstructor(exec, decl);
    }
}

