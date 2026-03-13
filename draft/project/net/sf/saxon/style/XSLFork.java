/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.Fork;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.style.XSLForEachGroup;
import net.sf.saxon.style.XSLSequence;
import net.sf.saxon.trans.XPathException;

public class XSLFork
extends StyleElement {
    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return false;
    }

    @Override
    public void prepareAttributes() {
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            this.checkUnknownAttribute(attName);
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        int foundGroup = 0;
        int foundSequence = 0;
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLSequence) {
                ++foundSequence;
                continue;
            }
            if (nodeInfo instanceof XSLForEachGroup) {
                ++foundGroup;
                continue;
            }
            if (nodeInfo instanceof XSLFallback) continue;
            this.compileError(nodeInfo.getDisplayName() + " cannot appear as a child of xsl:fork");
        }
        if (foundGroup > 1) {
            this.compileError("xsl:fork contains more than one xsl:for-each-group instruction");
        }
        if (foundGroup > 0 && foundSequence > 0) {
            this.compileError("Cannot mix xsl:sequence and xsl:for-each-group within xsl:fork");
        }
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        Expression content = this.compileSequenceConstructor(exec, decl, true);
        if (content instanceof Block) {
            return new Fork(((Block)content).getOperanda());
        }
        return content;
    }
}

