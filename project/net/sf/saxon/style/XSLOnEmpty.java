/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.OnEmptyExpr;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.XSLCatch;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.style.XSLSequence;
import net.sf.saxon.trans.XPathException;

public final class XSLOnEmpty
extends XSLSequence {
    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        super.validate(decl);
        this.iterateAxis(7).forEachOrFail(next -> {
            if (!(next instanceof XSLFallback) && !(next instanceof XSLCatch)) {
                this.compileError("xsl:on-empty must be the last instruction in the sequence constructor");
            }
        });
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        Expression e = super.compile(exec, decl);
        return new OnEmptyExpr(e);
    }
}

