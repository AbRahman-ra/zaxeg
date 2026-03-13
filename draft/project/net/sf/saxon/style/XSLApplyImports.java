/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.ApplyImports;
import net.sf.saxon.expr.instruct.WithParam;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLOverride;
import net.sf.saxon.style.XSLWithParam;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.value.Whitespace;

public class XSLApplyImports
extends StyleElement {
    @Override
    public boolean isInstruction() {
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
            if (nodeInfo instanceof XSLWithParam) continue;
            if (nodeInfo.getNodeKind() == 3) {
                if (Whitespace.isWhite(nodeInfo.getStringValueCS())) continue;
                this.compileError("No character data is allowed within xsl:apply-imports", "XTSE0010");
                continue;
            }
            this.compileError("Child element " + nodeInfo.getDisplayName() + " is not allowed as a child of xsl:apply-imports", "XTSE0010");
        }
        for (NodeImpl parent = this.getParent(); parent != null; parent = parent.getParent()) {
            if (!(parent instanceof XSLOverride)) continue;
            this.compileError("xsl:apply-imports cannot be used in a template rule declared within xsl:override", "XTSE3460");
        }
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        ApplyImports inst = new ApplyImports();
        WithParam[] nonTunnels = this.getWithParamInstructions(inst, exec, decl, false);
        WithParam[] tunnels = this.getWithParamInstructions(inst, exec, decl, true);
        inst.setActualParams(nonTunnels);
        inst.setTunnelParams(tunnels);
        return inst;
    }
}

