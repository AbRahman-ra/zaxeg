/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.expr.instruct.Message;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;

public final class XSLAssert
extends StyleElement {
    private Expression test = null;
    private Expression select = null;
    private Expression errorCode = null;

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
        String testAtt = null;
        String selectAtt = null;
        String errorCodeAtt = null;
        block10: for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            switch (f) {
                case "test": {
                    testAtt = value;
                    this.test = this.makeExpression(testAtt, att);
                    continue block10;
                }
                case "select": {
                    selectAtt = value;
                    this.select = this.makeExpression(selectAtt, att);
                    continue block10;
                }
                case "error-code": {
                    errorCodeAtt = value;
                    this.errorCode = this.makeAttributeValueTemplate(errorCodeAtt, att);
                    continue block10;
                }
            }
            this.checkUnknownAttribute(attName);
        }
        if (testAtt == null) {
            this.reportAbsence("test");
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.select = this.typeCheck("select", this.select);
        this.test = this.typeCheck("test", this.test);
        this.errorCode = this.errorCode == null ? new StringLiteral("Q{http://www.w3.org/2005/xqt-errors}XTMM9001") : this.typeCheck("error-code", this.errorCode);
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        if (exec.getCompilerInfo().isAssertionsEnabled()) {
            String code;
            Expression b = this.compileSequenceConstructor(exec, decl, true);
            if (b != null) {
                if (this.select == null) {
                    this.select = b;
                } else {
                    this.select = Block.makeBlock(this.select, b);
                    this.select.setLocation(this.allocateLocation());
                }
            }
            if (this.select == null) {
                this.select = new StringLiteral("xsl:message (no content)");
            }
            if (this.errorCode instanceof StringLiteral && (code = ((StringLiteral)this.errorCode).getStringValue()).contains(":") && !code.startsWith("Q{")) {
                StructuredQName name = this.makeQName(code, null, "error-code");
                this.errorCode = new StringLiteral(name.getEQName());
            }
            Message msg = new Message(this.select, new StringLiteral("yes"), this.errorCode);
            msg.setIsAssert(true);
            if (!(this.errorCode instanceof StringLiteral)) {
                msg.setRetainedStaticContext(this.makeRetainedStaticContext());
            }
            Expression condition = SystemFunction.makeCall("not", this.test.getRetainedStaticContext(), this.test);
            return new Choose(new Expression[]{condition}, new Expression[]{msg});
        }
        return Literal.makeEmptySequence();
    }
}

