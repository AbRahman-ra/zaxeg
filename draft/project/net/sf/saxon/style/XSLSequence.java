/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.SequenceInstr;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.trans.XPathException;

public class XSLSequence
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
    public boolean mayContainFallback() {
        return true;
    }

    public Expression getSelectExpression() {
        return this.select;
    }

    public void setSelectExpression(Expression select) {
        this.select = select;
    }

    @Override
    public void prepareAttributes() {
        String selectAtt = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String value = att.getValue();
            String f = attName.getDisplayName();
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
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLFallback) continue;
            if (this.select == null) break;
            this.compileError("An " + this.getDisplayName() + " element with a select attribute must be empty", "XTSE3185");
            break;
        }
        this.select = this.typeCheck("select", this.select);
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        if (this.select == null) {
            this.select = this.compileSequenceConstructor(exec, decl, false);
        }
        if (this.getConfiguration().getBooleanProperty(Feature.STRICT_STREAMABILITY)) {
            this.select = new SequenceInstr(this.select);
        }
        return this.select;
    }
}

