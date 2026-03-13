/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLChoose;
import net.sf.saxon.trans.XPathException;

public class XSLOtherwise
extends StyleElement {
    private Expression select;

    @Override
    public void prepareAttributes() {
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            if (f.equals("select")) {
                this.requireSyntaxExtensions("select");
                this.select = this.makeExpression(att.getValue(), att);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        if (!(this.getParent() instanceof XSLChoose)) {
            this.compileError("xsl:otherwise must be immediately within xsl:choose", "XTSE0010");
        }
        if (this.select != null && this.hasChildNodes()) {
            this.compileError("xsl:otherwise element must be empty if @select is present", "XTSE0010");
        }
    }

    @Override
    public boolean markTailCalls() {
        StyleElement last = this.getLastChildInstruction();
        return last != null && last.markTailCalls();
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        throw new UnsupportedOperationException("XSLOtherwise#compile() should not be called");
    }

    @Override
    public Expression compileSequenceConstructor(Compilation compilation, ComponentDeclaration decl, boolean includeParams) throws XPathException {
        if (this.select == null) {
            return super.compileSequenceConstructor(compilation, decl, includeParams);
        }
        return this.select;
    }
}

