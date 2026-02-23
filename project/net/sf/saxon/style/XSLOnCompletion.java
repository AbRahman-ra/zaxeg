/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.style.XSLIterate;
import net.sf.saxon.style.XSLLocalParam;
import net.sf.saxon.trans.XPathException;

public class XSLOnCompletion
extends StyleElement {
    private Expression select;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        String selectAtt = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            if (f.equals("select")) {
                selectAtt = value;
                this.select = this.makeExpression(selectAtt, att);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        StyleElement parent = (StyleElement)this.getParent();
        if (!(parent instanceof XSLIterate)) {
            this.compileError("xsl:on-completion is not allowed as a child of " + parent.getDisplayName(), "XTSE0010");
        }
        this.iterateAxis(11, NodeKindTest.ELEMENT).forEach(sib -> {
            if (!(sib instanceof XSLFallback) && !(sib instanceof XSLLocalParam)) {
                this.compileWarning("The rules for xsl:iterate have changed (see W3C bug 24179): xsl:on-completion must now be the first child of xsl:iterate after the xsl:param elements", "XTSE0010");
            }
        });
        if (this.select != null && this.iterateAxis(3).next() != null) {
            this.compileError("An xsl:on-completion element with a select attribute must be empty", "XTSE3125");
        }
        this.select = this.typeCheck("select", this.select);
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        if (this.select == null) {
            return this.compileSequenceConstructor(exec, decl, true);
        }
        return this.select;
    }
}

