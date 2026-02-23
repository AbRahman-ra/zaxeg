/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.instruct.DocumentInstr;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;

public class XSLDocument
extends StyleElement {
    private int validationAction = 4;
    private SchemaType schemaType = null;

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
        String validationAtt = null;
        String typeAtt = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            if (f.equals("validation")) {
                validationAtt = Whitespace.trim(value);
                continue;
            }
            if (f.equals("type")) {
                typeAtt = Whitespace.trim(value);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        this.validationAction = validationAtt == null ? this.getDefaultValidation() : this.validateValidationAttribute(validationAtt);
        if (typeAtt != null) {
            if (!this.isSchemaAware()) {
                this.compileError("The @type attribute is available only with a schema-aware XSLT processor", "XTSE1660");
            }
            this.schemaType = this.getSchemaType(typeAtt);
            this.validationAction = 8;
        }
        if (typeAtt != null && validationAtt != null) {
            this.compileError("The @validation and @type attributes are mutually exclusive", "XTSE1505");
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        DocumentInstr inst = new DocumentInstr(false, null);
        inst.setValidationAction(this.validationAction, this.schemaType);
        Expression b = this.compileSequenceConstructor(exec, decl, true);
        if (b == null) {
            b = Literal.makeEmptySequence();
        }
        inst.setContentExpression(b);
        inst.setLocation(this.allocateLocation());
        return inst;
    }
}

