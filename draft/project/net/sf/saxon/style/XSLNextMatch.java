/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.NextMatch;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.style.XSLWithParam;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Whitespace;

public class XSLNextMatch
extends StyleElement {
    private boolean useTailRecursion = false;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public boolean mayContainFallback() {
        return true;
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
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLWithParam || nodeInfo instanceof XSLFallback) continue;
            if (nodeInfo.getNodeKind() == 3) {
                if (Whitespace.isWhite(nodeInfo.getStringValueCS())) continue;
                this.compileError("No character data is allowed within xsl:next-match", "XTSE0010");
                continue;
            }
            this.compileError("Child element " + nodeInfo.getDisplayName() + " is not allowed as a child of xsl:next-match", "XTSE0010");
        }
    }

    @Override
    protected boolean markTailCalls() {
        this.useTailRecursion = true;
        return true;
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        NextMatch inst = new NextMatch(this.useTailRecursion);
        inst.setLocation(this.allocateLocation());
        inst.setActualParams(this.getWithParamInstructions(inst, exec, decl, false));
        inst.setTunnelParams(this.getWithParamInstructions(inst, exec, decl, true));
        return inst;
    }
}

