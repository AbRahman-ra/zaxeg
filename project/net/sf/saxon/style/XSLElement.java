/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.ComputedElement;
import net.sf.saxon.expr.instruct.ElementCreator;
import net.sf.saxon.expr.instruct.FixedElement;
import net.sf.saxon.expr.instruct.UseAttributeSet;
import net.sf.saxon.lib.StandardURIChecker;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;

public class XSLElement
extends StyleElement {
    private Expression elementName;
    private Expression namespace = null;
    private String use;
    private StructuredQName[] attributeSets = null;
    private int validation;
    private SchemaType schemaType = null;
    private boolean inheritNamespaces = true;

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
        String nameAtt = null;
        String namespaceAtt = null;
        String validationAtt = null;
        String typeAtt = null;
        String inheritAtt = null;
        block16: for (AttributeInfo att : this.attributes()) {
            String f;
            NodeName attName = att.getNodeName();
            String value = att.getValue();
            switch (f = attName.getDisplayName()) {
                case "name": {
                    nameAtt = Whitespace.trim(value);
                    this.elementName = this.makeAttributeValueTemplate(nameAtt, att);
                    continue block16;
                }
                case "namespace": {
                    namespaceAtt = value;
                    this.namespace = this.makeAttributeValueTemplate(namespaceAtt, att);
                    continue block16;
                }
                case "validation": {
                    validationAtt = Whitespace.trim(value);
                    continue block16;
                }
                case "type": {
                    typeAtt = Whitespace.trim(value);
                    continue block16;
                }
                case "inherit-namespaces": {
                    inheritAtt = Whitespace.trim(value);
                    continue block16;
                }
                case "use-attribute-sets": {
                    this.use = value;
                    continue block16;
                }
            }
            this.checkUnknownAttribute(attName);
        }
        if (nameAtt == null) {
            this.reportAbsence("name");
        } else if (this.elementName instanceof StringLiteral && !NameChecker.isQName(((StringLiteral)this.elementName).getStringValue())) {
            this.compileError("Element name " + Err.wrap(((StringLiteral)this.elementName).getStringValue()) + " is not a valid QName", "XTDE0820");
            this.elementName = new StringLiteral("saxon-error-element");
        }
        if (namespaceAtt != null && this.namespace instanceof StringLiteral && !StandardURIChecker.getInstance().isValidURI(((StringLiteral)this.namespace).getStringValue())) {
            this.compileError("The value of the namespace attribute must be a valid URI", "XTDE0835");
        }
        this.validation = validationAtt != null ? this.validateValidationAttribute(validationAtt) : this.getDefaultValidation();
        if (typeAtt != null) {
            if (!this.isSchemaAware()) {
                this.compileError("The @type attribute is available only with a schema-aware XSLT processor", "XTSE1660");
            }
            this.schemaType = this.getSchemaType(typeAtt);
            this.validation = 8;
        }
        if (typeAtt != null && validationAtt != null) {
            this.compileError("The @validation and @type attributes are mutually exclusive", "XTSE1505");
        }
        if (inheritAtt != null) {
            this.inheritNamespaces = this.processBooleanAttribute("inherit-namespaces", inheritAtt);
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        if (this.use != null) {
            this.attributeSets = this.getUsedAttributeSets(this.use);
        }
        this.elementName = this.typeCheck("name", this.elementName);
        this.namespace = this.typeCheck("namespace", this.namespace);
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        if (this.elementName instanceof StringLiteral) {
            String[] parts;
            String qName = ((StringLiteral)this.elementName).getStringValue();
            try {
                parts = NameChecker.getQNameParts(qName);
            } catch (QNameException e) {
                this.compileErrorInAttribute("Invalid element name: " + qName, "XTDE0820", "name");
                return null;
            }
            String nsuri = null;
            if (this.namespace instanceof StringLiteral) {
                nsuri = ((StringLiteral)this.namespace).getStringValue();
                if (nsuri.isEmpty()) {
                    parts[0] = "";
                }
            } else if (this.namespace == null && (nsuri = this.getURIForPrefix(parts[0], true)) == null) {
                this.undeclaredNamespaceError(parts[0], "XTDE0830", "name");
            }
            if (nsuri != null) {
                FingerprintedQName qn = new FingerprintedQName(parts[0], nsuri, parts[1]);
                qn.obtainFingerprint(this.getNamePool());
                FixedElement inst = new FixedElement(qn, NamespaceMap.emptyMap(), this.inheritNamespaces, true, this.schemaType, this.validation);
                inst.setLocation(this.allocateLocation());
                return this.compileContentExpression(exec, decl, inst);
            }
        }
        ComputedElement inst = new ComputedElement(this.elementName, this.namespace, this.schemaType, this.validation, this.inheritNamespaces, false);
        inst.setLocation(this.allocateLocation());
        return this.compileContentExpression(exec, decl, inst);
    }

    private Expression compileContentExpression(Compilation exec, ComponentDeclaration decl, ElementCreator inst) throws XPathException {
        Expression content = this.compileSequenceConstructor(exec, decl, true);
        if (this.attributeSets != null) {
            Expression use = UseAttributeSet.makeUseAttributeSets(this.attributeSets, this);
            if (content == null) {
                content = use;
            } else {
                content = Block.makeBlock(use, content);
                content.setLocation(this.allocateLocation());
            }
        }
        if (content == null) {
            content = Literal.makeEmptySequence();
        }
        inst.setContentExpression(content);
        return inst;
    }
}

