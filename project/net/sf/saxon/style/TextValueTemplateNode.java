/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.style.AttributeValueTemplate;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.TextValueTemplateContext;
import net.sf.saxon.style.XSLText;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.tree.linked.TextImpl;

public class TextValueTemplateNode
extends TextImpl {
    private Expression contentExp;
    private TextValueTemplateContext staticContext;

    public TextValueTemplateNode(String value) {
        super(value);
    }

    public Expression getContentExpression() {
        return this.contentExp;
    }

    public TextValueTemplateContext getStaticContext() {
        if (this.staticContext == null) {
            this.staticContext = new TextValueTemplateContext((StyleElement)this.getParent(), this);
        }
        return this.staticContext;
    }

    public void parse() throws XPathException {
        boolean disable = false;
        NodeImpl parent = this.getParent();
        if (parent instanceof XSLText && StyleElement.isYes(parent.getAttributeValue("", "disable-output-escaping"))) {
            disable = true;
        }
        this.contentExp = AttributeValueTemplate.make(this.getStringValue(), this.getStaticContext());
        this.contentExp = new ValueOf(this.contentExp, disable, false);
        assert (this.getParent() != null);
        this.contentExp.setRetainedStaticContext(((StyleElement)this.getParent()).makeRetainedStaticContext());
    }

    public void validate() throws XPathException {
        assert (this.getParent() != null);
        this.contentExp = ((StyleElement)this.getParent()).typeCheck("tvt", this.contentExp);
    }
}

