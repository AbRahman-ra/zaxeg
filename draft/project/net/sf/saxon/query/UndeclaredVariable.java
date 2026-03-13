/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import java.util.Collections;
import net.sf.saxon.expr.BindingReference;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.trans.XPathException;

public class UndeclaredVariable
extends GlobalVariable {
    public void transferReferences(GlobalVariable var) {
        for (BindingReference ref : this.references) {
            var.registerReference(ref);
        }
        this.references = Collections.emptyList();
    }

    @Override
    public void compile(Executable exec, int slot) throws XPathException {
        throw new UnsupportedOperationException("Attempt to compile a place-holder for an undeclared variable");
    }
}

