/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLLeafNodeConstructor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.TextImpl;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class XSLText
extends XSLLeafNodeConstructor {
    private boolean disable = false;
    private StringValue value;

    @Override
    public void prepareAttributes() {
        String disableAtt = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String value = att.getValue();
            String f = attName.getDisplayName();
            if (f.equals("disable-output-escaping")) {
                disableAtt = Whitespace.trim(value);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (disableAtt != null) {
            this.disable = this.processBooleanAttribute("disable-output-escaping", disableAtt);
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.value = StringValue.EMPTY_STRING;
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof StyleElement) {
                ((StyleElement)nodeInfo).compileError("xsl:text must not contain child elements", "XTSE0010");
                return;
            }
            this.value = StringValue.makeStringValue(nodeInfo.getStringValueCS());
        }
        super.validate(decl);
    }

    @Override
    protected String getErrorCodeForSelectPlusContent() {
        return null;
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        if (this.isExpandingText()) {
            TextImpl child = (TextImpl)this.iterateAxis(3).next();
            if (child != null) {
                ArrayList<Expression> contents = new ArrayList<Expression>(10);
                this.compileContentValueTemplate(child, contents);
                Expression block = Block.makeBlock(contents);
                block.setLocation(this.allocateLocation());
                return block;
            }
            return new ValueOf(new StringLiteral(StringValue.EMPTY_STRING), this.disable, false);
        }
        return new ValueOf(Literal.makeLiteral(this.value), this.disable, false);
    }
}

