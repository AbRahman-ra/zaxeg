/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.InstanceOfExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.expr.instruct.Copy;
import net.sf.saxon.expr.instruct.ForEach;
import net.sf.saxon.expr.instruct.UseAttributeSet;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Whitespace;

public class XSLCopy
extends StyleElement {
    private String use;
    private StructuredQName[] attributeSets = null;
    private boolean copyNamespaces = true;
    private boolean inheritNamespaces = true;
    private int validationAction = 3;
    private SchemaType schemaType = null;
    private Expression select = null;
    private boolean selectSpecified = false;

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
        String copyNamespacesAtt = null;
        String validationAtt = null;
        String typeAtt = null;
        String inheritAtt = null;
        AttributeInfo selectAtt = null;
        block16: for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            switch (f) {
                case "use-attribute-sets": {
                    this.use = value;
                    continue block16;
                }
                case "copy-namespaces": {
                    copyNamespacesAtt = Whitespace.trim(value);
                    continue block16;
                }
                case "select": {
                    selectAtt = att;
                    continue block16;
                }
                case "type": {
                    typeAtt = Whitespace.trim(value);
                    continue block16;
                }
                case "validation": {
                    validationAtt = Whitespace.trim(value);
                    continue block16;
                }
                case "inherit-namespaces": {
                    inheritAtt = Whitespace.trim(value);
                    continue block16;
                }
            }
            this.checkUnknownAttribute(attName);
        }
        if (copyNamespacesAtt != null) {
            this.copyNamespaces = this.processBooleanAttribute("copy-namespaces", copyNamespacesAtt);
        }
        if (typeAtt != null && validationAtt != null) {
            this.compileError("The type and validation attributes must not both be specified", "XTSE1505");
        }
        this.validationAction = validationAtt != null ? this.validateValidationAttribute(validationAtt) : this.getDefaultValidation();
        if (typeAtt != null) {
            this.schemaType = this.getSchemaType(typeAtt);
            if (!this.isSchemaAware()) {
                this.compileError("The @type attribute is available only with a schema-aware XSLT processor", "XTSE1660");
            }
            this.validationAction = 8;
        }
        if (inheritAtt != null) {
            this.inheritNamespaces = this.processBooleanAttribute("inherit-namespaces", inheritAtt);
        }
        if (selectAtt != null) {
            this.select = this.makeExpression(selectAtt.getValue(), selectAtt);
            this.selectSpecified = true;
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        if (this.use != null) {
            this.attributeSets = this.getUsedAttributeSets(this.use);
        }
        if (this.select == null) {
            this.select = new ContextItemExpression();
            this.select.setLocation(this.allocateLocation());
        }
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        this.select = this.typeCheck("select", this.select);
        try {
            RoleDiagnostic role = new RoleDiagnostic(4, "xsl:copy/select", 0);
            role.setErrorCode("XTTE3180");
            this.select = this.getConfiguration().getTypeChecker(false).staticTypeCheck(this.select, SequenceType.OPTIONAL_ITEM, role, this.makeExpressionVisitor());
        } catch (XPathException err) {
            this.compileError(err);
        }
        Expression content = this.compileSequenceConstructor(exec, decl, true);
        if (this.attributeSets != null) {
            Expression use = UseAttributeSet.makeUseAttributeSets(this.attributeSets, this);
            InstanceOfExpression condition = new InstanceOfExpression(new ContextItemExpression(), SequenceType.makeSequenceType(NodeKindTest.ELEMENT, 16384));
            Expression choice = Choose.makeConditional(condition, use);
            if (content == null) {
                content = choice;
            } else {
                content = Block.makeBlock(choice, content);
                content.setLocation(this.allocateLocation());
            }
        }
        if (content == null) {
            content = Literal.makeEmptySequence();
        }
        Copy inst = new Copy(this.copyNamespaces, this.inheritNamespaces, this.schemaType, this.validationAction);
        inst.setContentExpression(content);
        if (this.selectSpecified) {
            return new ForEach(this.select, inst);
        }
        return inst;
    }
}

