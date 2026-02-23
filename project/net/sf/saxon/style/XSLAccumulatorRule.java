/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.SourceBinding;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLAccumulator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Whitespace;

public class XSLAccumulatorRule
extends StyleElement {
    private Pattern match;
    private boolean postDescent;
    private Expression select;
    private boolean capture;

    @Override
    public void prepareAttributes() {
        String matchAtt = null;
        String newValueAtt = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String value = att.getValue();
            String f = attName.getDisplayName();
            if (attName.getURI().isEmpty()) {
                switch (f) {
                    case "match": {
                        matchAtt = value;
                        break;
                    }
                    case "select": {
                        newValueAtt = value;
                        this.select = this.makeExpression(newValueAtt, att);
                        break;
                    }
                    case "phase": {
                        String phaseAtt = Whitespace.trim(value);
                        if ("start".equals(phaseAtt)) {
                            this.postDescent = false;
                            break;
                        }
                        if ("end".equals(phaseAtt)) {
                            this.postDescent = true;
                            break;
                        }
                        this.postDescent = true;
                        this.compileError("phase must be 'start' or 'end'", "XTSE0020");
                        break;
                    }
                    default: {
                        this.checkUnknownAttribute(attName);
                        break;
                    }
                }
                continue;
            }
            if (attName.hasURI("http://saxon.sf.net/")) {
                if (!this.isExtensionAttributeAllowed(attName.getDisplayName()) || !attName.getLocalPart().equals("capture")) continue;
                this.capture = this.processBooleanAttribute("saxon:capture", value);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (matchAtt == null) {
            this.reportAbsence("match");
            matchAtt = "non-existent-element";
        }
        this.match = this.makePattern(matchAtt, "match");
        if (this.capture && !this.postDescent) {
            this.compileWarning("saxon:capture has no effect on a pre-descent accumulator rule", "SXWN9000");
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.select = this.typeCheck("select", this.select);
        this.match = this.typeCheck("match", this.match);
        if (this.select != null && this.hasChildNodes()) {
            this.compileError("If the xsl:accumulator-rule element has a select attribute then it must have no children");
        }
    }

    public Expression getNewValueExpression(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        if (this.select == null) {
            this.select = this.compileSequenceConstructor(compilation, decl, true);
        }
        return this.select;
    }

    public Pattern getMatch() {
        return this.match;
    }

    public void setMatch(Pattern match) {
        this.match = match;
    }

    public boolean isPostDescent() {
        return this.postDescent;
    }

    public void setPostDescent(boolean postDescent) {
        this.postDescent = postDescent;
    }

    public boolean isCapture() {
        return this.capture;
    }

    public Expression getSelect() {
        return this.select;
    }

    public void setSelect(Expression select) {
        this.select = select;
    }

    @Override
    public SourceBinding hasImplicitBinding(StructuredQName name) {
        if (name.getLocalPart().equals("value") && name.hasURI("")) {
            SourceBinding sb = new SourceBinding(this);
            sb.setVariableQName(new StructuredQName("", "", "value"));
            assert ((XSLAccumulator)this.getParent() != null);
            sb.setDeclaredType(((XSLAccumulator)this.getParent()).getResultType());
            sb.setProperty(SourceBinding.BindingProperty.IMPLICITLY_DECLARED, true);
            return sb;
        }
        return null;
    }
}

