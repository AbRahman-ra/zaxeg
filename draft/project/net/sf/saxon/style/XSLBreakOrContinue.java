/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLCatch;
import net.sf.saxon.style.XSLChoose;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.style.XSLIf;
import net.sf.saxon.style.XSLIterate;
import net.sf.saxon.style.XSLOtherwise;
import net.sf.saxon.style.XSLTry;
import net.sf.saxon.style.XSLWhen;
import net.sf.saxon.tree.iter.AxisIterator;

public abstract class XSLBreakOrContinue
extends StyleElement {
    protected XSLIterate xslIterate = null;

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

    protected void validatePosition() {
        boolean isLast;
        NodeInfo inst;
        block5: {
            inst = this;
            isLast = true;
            do {
                if (!(inst instanceof XSLWhen)) {
                    NodeInfo sib;
                    AxisIterator sibs = inst.iterateAxis(7);
                    while ((sib = sibs.next()) != null) {
                        if (sib instanceof XSLFallback || sib instanceof XSLCatch) continue;
                        isLast = false;
                    }
                }
                if ((inst = inst.getParent()) instanceof XSLIterate) break block5;
            } while (inst instanceof XSLTry || inst instanceof XSLCatch || inst instanceof XSLWhen || inst instanceof XSLOtherwise || inst instanceof XSLIf || inst instanceof XSLChoose);
            if (inst == null) {
                this.compileError(this.getDisplayName() + " is not allowed at outermost level", "XTSE3120");
                return;
            }
            this.compileError(this.getDisplayName() + " is not allowed within " + inst.getDisplayName(), "XTSE3120");
            return;
        }
        this.xslIterate = (XSLIterate)inst;
        if (!isLast) {
            this.compileError(this.getDisplayName() + " must be the last instruction in the xsl:iterate loop", "XTSE3120");
        }
    }
}

