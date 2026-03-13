/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.Message;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Whitespace;

public final class XSLMessage
extends StyleElement {
    private Expression terminate = null;
    private Expression select = null;
    private Expression errorCode = null;
    private Expression timer = null;

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
        String terminateAtt = null;
        String selectAtt = null;
        String errorCodeAtt = null;
        block10: for (AttributeInfo att : this.attributes()) {
            String f;
            String value = att.getValue();
            NodeName attName = att.getNodeName();
            switch (f = attName.getDisplayName()) {
                case "terminate": {
                    terminateAtt = Whitespace.trim(value);
                    this.terminate = this.makeAttributeValueTemplate(terminateAtt, att);
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
            if (attName.hasURI("http://saxon.sf.net/") && attName.getLocalPart().equals("time")) {
                this.isExtensionAttributeAllowed(attName.getDisplayName());
                boolean timed = this.processBooleanAttribute("saxon:time", value);
                if (!timed) continue;
                this.timer = this.makeExpression("format-dateTime(Q{http://saxon.sf.net/}timestamp(),'[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01].[f,3-3] - ')", att);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (terminateAtt == null) {
            terminateAtt = "no";
            this.terminate = this.makeAttributeValueTemplate(terminateAtt, null);
        }
        this.checkAttributeValue("terminate", terminateAtt, true, StyleElement.YES_NO);
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.select = this.typeCheck("select", this.select);
        this.terminate = this.typeCheck("terminate", this.terminate);
        this.errorCode = this.errorCode == null ? new StringLiteral("Q{http://www.w3.org/2005/xqt-errors}XTMM9000") : this.typeCheck("error-code", this.errorCode);
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
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
        if (this.timer != null) {
            this.select = Block.makeBlock(this.timer, this.select);
        }
        if (this.select == null) {
            this.select = new StringLiteral("xsl:message (no content)");
        }
        if (this.errorCode instanceof StringLiteral && (code = ((StringLiteral)this.errorCode).getStringValue()).contains(":") && !code.startsWith("Q{")) {
            StructuredQName name = this.makeQName(code, null, "error-code");
            this.errorCode = new StringLiteral(name.getEQName());
        }
        Message m = new Message(this.select, this.terminate, this.errorCode);
        m.setRetainedStaticContext(this.makeRetainedStaticContext());
        return m;
    }
}

