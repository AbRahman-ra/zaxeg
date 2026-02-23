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
import net.sf.saxon.style.XSLMerge;
import net.sf.saxon.trans.XPathException;

public class XSLMergeAction
extends StyleElement {
    @Override
    public boolean isInstruction() {
        return false;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        Expression content = this.compileSequenceConstructor(exec, decl, true);
        return content;
    }

    @Override
    protected void prepareAttributes() {
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            this.checkUnknownAttribute(attName);
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        if (!(this.getParent() instanceof XSLMerge)) {
            this.compileError("xsl:merge-action may appear only as a child of xsl:merge", "XTSE0010");
        }
    }
}

