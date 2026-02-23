/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.XSLSortOrMergeKey;
import net.sf.saxon.trans.XPathException;

public class XSLSort
extends XSLSortOrMergeKey {
    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        super.validate(decl);
        this.stable = this.typeCheck("stable", this.stable);
        this.sortKeyDefinition.setStable(this.stable);
    }

    @Override
    public Expression getStable() {
        return this.stable;
    }
}

