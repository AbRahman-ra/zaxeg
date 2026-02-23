/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.BreakInstr;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.XSLBreakOrContinue;
import net.sf.saxon.trans.XPathException;

public class XSLBreak
extends XSLBreakOrContinue {
    private Expression select;

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
    protected boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.validatePosition();
        if (this.xslIterate == null) {
            this.compileError(this.getDisplayName() + " must be a descendant of an xsl:iterate instruction", "XTSE3120");
        }
        if (this.select != null && this.hasChildNodes()) {
            this.compileError("An xsl:break element with a select attribute must be empty", "XTSE3125");
        }
        this.select = this.typeCheck("select", this.select);
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        Expression val = this.select;
        if (val == null) {
            val = this.compileSequenceConstructor(exec, decl, false);
        }
        BreakInstr brake = new BreakInstr();
        brake.setRetainedStaticContext(this.makeRetainedStaticContext());
        return Block.makeBlock(val, brake);
    }
}

