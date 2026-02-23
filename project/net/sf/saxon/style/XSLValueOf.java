/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.XSLLeafNodeConstructor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public final class XSLValueOf
extends XSLLeafNodeConstructor {
    private boolean disable = false;
    private Expression separator;

    @Override
    public void prepareAttributes() {
        String selectAtt = null;
        String disableAtt = null;
        String separatorAtt = null;
        block10: for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            switch (f) {
                case "disable-output-escaping": {
                    disableAtt = Whitespace.trim(value);
                    continue block10;
                }
                case "select": {
                    selectAtt = value;
                    this.select = this.makeExpression(selectAtt, att);
                    continue block10;
                }
                case "separator": {
                    separatorAtt = value;
                    this.separator = this.makeAttributeValueTemplate(separatorAtt, att);
                    continue block10;
                }
            }
            this.checkUnknownAttribute(attName);
        }
        if (disableAtt != null) {
            this.disable = this.processBooleanAttribute("disable-output-escaping", disableAtt);
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        super.validate(decl);
        this.select = this.typeCheck("select", this.select);
        this.separator = this.typeCheck("separator", this.separator);
    }

    @Override
    protected String getErrorCodeForSelectPlusContent() {
        return "XTSE0870";
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        Configuration config = this.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        if (this.separator == null && this.select != null && this.xPath10ModeIsEnabled()) {
            this.select = config.getTypeChecker(true).processValueOf(this.select, config);
        } else if (this.separator == null) {
            this.separator = this.select == null ? new StringLiteral(StringValue.EMPTY_STRING) : new StringLiteral(StringValue.SINGLE_SPACE);
        }
        ValueOf inst = new ValueOf(this.select, this.disable, false);
        inst.setRetainedStaticContext(this.makeRetainedStaticContext());
        this.compileContent(exec, decl, inst, this.separator);
        return inst;
    }
}

