/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.NamespaceConstructor;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.style.XSLLeafNodeConstructor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class XSLNamespace
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
        int countChildren = 0;
        NodeInfo firstChild = null;
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLFallback) continue;
            if (this.select != null) {
                String errorCode = this.getErrorCodeForSelectPlusContent();
                this.compileError("An " + this.getDisplayName() + " element with a select attribute must be empty", errorCode);
            }
            ++countChildren;
            if (firstChild != null) break;
            firstChild = nodeInfo;
        }
        if (this.select == null) {
            if (countChildren == 0) {
                this.select = new StringLiteral(StringValue.EMPTY_STRING);
                this.select.setRetainedStaticContext(this.makeRetainedStaticContext());
            } else if (countChildren == 1 && firstChild.getNodeKind() == 3) {
                this.select = new StringLiteral(firstChild.getStringValueCS());
                this.select.setRetainedStaticContext(this.makeRetainedStaticContext());
            }
        }
    }

    @Override
    protected String getErrorCodeForSelectPlusContent() {
        return "XTSE0910";
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        NamespaceConstructor inst = new NamespaceConstructor(this.name);
        this.compileContent(exec, decl, inst, new StringLiteral(StringValue.SINGLE_SPACE));
        return inst;
    }
}

