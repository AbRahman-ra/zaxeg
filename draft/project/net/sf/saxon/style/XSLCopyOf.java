/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.CopyOf;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;

public final class XSLCopyOf
extends StyleElement {
    private Expression select;
    private boolean copyNamespaces;
    private boolean copyAccumulators;
    private int validation = 3;
    private SchemaType schemaType;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        String selectAtt = null;
        String copyNamespacesAtt = null;
        String copyAccumulatorsAtt = null;
        String validationAtt = null;
        String typeAtt = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String value = att.getValue();
            String f = attName.getDisplayName();
            if (f.equals("select")) {
                selectAtt = value;
                this.select = this.makeExpression(selectAtt, att);
                continue;
            }
            if (f.equals("copy-namespaces")) {
                copyNamespacesAtt = Whitespace.trim(value);
                continue;
            }
            if (f.equals("copy-accumulators")) {
                copyAccumulatorsAtt = Whitespace.trim(value);
                continue;
            }
            if (f.equals("validation")) {
                validationAtt = Whitespace.trim(value);
                continue;
            }
            if (f.equals("type")) {
                typeAtt = Whitespace.trim(value);
                continue;
            }
            if (attName.getLocalPart().equals("read-once") && attName.hasURI("http://saxon.sf.net/")) {
                this.compileError("The saxon:read-once attribute is no longer available - use xsl:stream");
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (selectAtt == null) {
            this.reportAbsence("select");
        }
        if (copyAccumulatorsAtt == null) {
            this.copyAccumulators = false;
        } else {
            this.copyAccumulators = this.processBooleanAttribute("copy-accumulators", copyAccumulatorsAtt);
            if (this.copyAccumulators && this.isConstructingComplexContent()) {
                this.compileWarning("Copying accumulators is pointless when the copied element is immediately attached to a new parent, since that action will lose the accumulator values", "SXWN9017");
                this.copyAccumulators = false;
            }
        }
        this.copyNamespaces = copyNamespacesAtt == null ? true : this.processBooleanAttribute("copy-namespaces", copyNamespacesAtt);
        this.validation = validationAtt != null ? this.validateValidationAttribute(validationAtt) : this.getDefaultValidation();
        if (typeAtt != null) {
            this.schemaType = this.getSchemaType(typeAtt);
            if (!this.isSchemaAware()) {
                this.compileError("The @type attribute is available only with a schema-aware XSLT processor", "XTSE1660");
            }
            this.validation = 8;
        }
        if (typeAtt != null && validationAtt != null) {
            this.compileError("The @validation and @type attributes are mutually exclusive", "XTSE1505");
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.checkEmpty();
        this.select = this.typeCheck("select", this.select);
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) {
        CopyOf inst = new CopyOf(this.select, this.copyNamespaces, this.validation, this.schemaType, false);
        inst.setCopyAccumulators(this.copyAccumulators);
        inst.setSchemaAware(exec.isSchemaAware());
        return inst;
    }
}

