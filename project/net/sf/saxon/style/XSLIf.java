/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;

public class XSLIf
extends StyleElement {
    private Expression test;
    private Expression thenExp;
    private Expression elseExp;

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
        block10: for (AttributeInfo att : this.attributes()) {
            String f;
            NodeName attName = att.getNodeName();
            switch (f = attName.getDisplayName()) {
                case "test": {
                    this.test = this.makeExpression(att.getValue(), att);
                    continue block10;
                }
                case "then": {
                    this.requireSyntaxExtensions("then");
                    this.thenExp = this.makeExpression(att.getValue(), att);
                    continue block10;
                }
                case "else": {
                    this.requireSyntaxExtensions("else");
                    this.elseExp = this.makeExpression(att.getValue(), att);
                    continue block10;
                }
            }
            this.checkUnknownAttribute(attName);
        }
        if (this.test == null) {
            this.reportAbsence("test");
        }
    }

    public static Expression prepareTestAttribute(StyleElement se) {
        AttributeInfo testAtt = null;
        for (AttributeInfo att : se.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            if (f.equals("test")) {
                testAtt = att;
                continue;
            }
            se.checkUnknownAttribute(attName);
        }
        if (testAtt == null) {
            return null;
        }
        return se.makeExpression(testAtt.getValue(), testAtt);
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.test = this.typeCheck("test", this.test);
        if (this.thenExp != null && this.hasChildNodes()) {
            this.compileError("xsl:if element must be empty if @then is present", "XTSE0010");
        }
    }

    @Override
    public boolean markTailCalls() {
        StyleElement last = this.getLastChildInstruction();
        return last != null && last.markTailCalls();
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        Expression[] actions;
        Expression[] conditions;
        Expression action;
        if (this.test instanceof Literal) {
            GroundedValue testVal = ((Literal)this.test).getValue();
            try {
                if (testVal.effectiveBooleanValue()) {
                    return this.compileSequenceConstructor(exec, decl, true);
                }
                return null;
            } catch (XPathException xPathException) {
                // empty catch block
            }
        }
        if ((action = this.compileSequenceConstructor(exec, decl, true)) == null) {
            return null;
        }
        if (this.elseExp == null) {
            conditions = new Expression[]{this.test};
            actions = new Expression[]{action};
        } else {
            conditions = new Expression[]{this.test, Literal.makeLiteral(BooleanValue.TRUE)};
            actions = new Expression[]{action, this.elseExp};
        }
        Choose choose = new Choose(conditions, actions);
        choose.setInstruction(true);
        return choose;
    }

    @Override
    public Expression compileSequenceConstructor(Compilation compilation, ComponentDeclaration decl, boolean includeParams) throws XPathException {
        if (this.thenExp == null) {
            return super.compileSequenceConstructor(compilation, decl, includeParams);
        }
        return this.thenExp;
    }
}

