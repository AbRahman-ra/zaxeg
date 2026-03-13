/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.ProcessingInstruction;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.XSLLeafNodeConstructor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class XSLProcessingInstruction
extends XSLLeafNodeConstructor {
    Expression name;

    @Override
    public void prepareAttributes() {
        this.name = this.prepareAttributesNameAndSelect();
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.name = this.typeCheck("name", this.name);
        this.select = this.typeCheck("select", this.select);
        super.validate(decl);
    }

    @Override
    protected String getErrorCodeForSelectPlusContent() {
        return "XTSE0880";
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        ProcessingInstruction inst = new ProcessingInstruction(this.name);
        this.compileContent(exec, decl, inst, new StringLiteral(StringValue.SINGLE_SPACE));
        return inst;
    }
}

