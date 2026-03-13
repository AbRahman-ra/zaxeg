/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.ComputedAttribute;
import net.sf.saxon.expr.instruct.FixedAttribute;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.lib.StandardURIChecker;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.XSLLeafNodeConstructor;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public final class XSLAttribute
extends XSLLeafNodeConstructor {
    private Expression attributeName;
    private Expression separator;
    private Expression namespace = null;
    private int validationAction = 3;
    private SimpleType schemaType;

    @Override
    public void prepareAttributes() {
        String nameAtt = null;
        String namespaceAtt = null;
        String selectAtt = null;
        String separatorAtt = null;
        String validationAtt = null;
        String typeAtt = null;
        block16: for (AttributeInfo att : this.attributes()) {
            String f;
            NodeName attName = att.getNodeName();
            String value = att.getValue();
            switch (f = attName.getDisplayName()) {
                case "name": {
                    nameAtt = Whitespace.trim(value);
                    this.attributeName = this.makeAttributeValueTemplate(nameAtt, att);
                    continue block16;
                }
                case "namespace": {
                    namespaceAtt = Whitespace.trim(value);
                    this.namespace = this.makeAttributeValueTemplate(namespaceAtt, att);
                    continue block16;
                }
                case "select": {
                    selectAtt = value;
                    this.select = this.makeExpression(selectAtt, att);
                    continue block16;
                }
                case "separator": {
                    separatorAtt = value;
                    this.separator = this.makeAttributeValueTemplate(separatorAtt, att);
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
            }
            this.checkUnknownAttribute(attName);
        }
        if (nameAtt == null) {
            this.reportAbsence("name");
            return;
        }
        if (this.attributeName instanceof StringLiteral) {
            if (!NameChecker.isQName(((StringLiteral)this.attributeName).getStringValue())) {
                this.invalidAttributeName("Attribute name " + Err.wrap(nameAtt) + " is not a valid QName");
            }
            if (nameAtt.equals("xmlns") && this.namespace == null) {
                this.invalidAttributeName("Invalid attribute name: xmlns");
            }
            if (nameAtt.startsWith("xmlns:")) {
                if (namespaceAtt == null) {
                    this.invalidAttributeName("Invalid attribute name: " + Err.wrap(nameAtt));
                } else {
                    nameAtt = nameAtt.substring(6);
                    this.attributeName = new StringLiteral(nameAtt);
                }
            }
        }
        if (namespaceAtt != null && this.namespace instanceof StringLiteral && !StandardURIChecker.getInstance().isValidURI(((StringLiteral)this.namespace).getStringValue())) {
            this.compileError("The value of the namespace attribute must be a valid URI", "XTDE0865");
        }
        if (separatorAtt == null) {
            this.separator = selectAtt == null ? new StringLiteral(StringValue.EMPTY_STRING) : new StringLiteral(StringValue.SINGLE_SPACE);
        }
        this.validationAction = validationAtt != null ? this.validateValidationAttribute(validationAtt) : this.getDefaultValidation();
        if (typeAtt != null) {
            if (!this.isSchemaAware()) {
                this.compileError("The @type attribute is available only with a schema-aware XSLT processor", "XTSE1660");
            } else {
                SchemaType type = this.getSchemaType(typeAtt);
                if (type == null) {
                    this.compileError("Unknown attribute type " + typeAtt, "XTSE1520");
                } else if (type.isSimpleType()) {
                    this.schemaType = (SimpleType)type;
                } else {
                    this.compileError("Type annotation for attributes must be a simple type", "XTSE1530");
                }
                this.validationAction = 8;
            }
        }
        if (typeAtt != null && validationAtt != null) {
            this.compileError("The validation and type attributes are mutually exclusive", "XTSE1505");
            this.validationAction = this.getDefaultValidation();
            this.schemaType = null;
        }
    }

    private void invalidAttributeName(String message) {
        this.compileErrorInAttribute(message, "XTDE0850", "name");
        this.attributeName = new StringLiteral("saxon-error-attribute");
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        if (this.schemaType != null && this.schemaType.isNamespaceSensitive()) {
            this.compileErrorInAttribute("Validation at attribute level must not specify a namespace-sensitive type (xs:QName or xs:NOTATION)", "XTTE1545", "type");
        }
        this.attributeName = this.typeCheck("name", this.attributeName);
        this.namespace = this.typeCheck("namespace", this.namespace);
        this.select = this.typeCheck("select", this.select);
        this.separator = this.typeCheck("separator", this.separator);
        super.validate(decl);
    }

    @Override
    protected String getErrorCodeForSelectPlusContent() {
        return "XTSE0840";
    }

    @Override
    public Instruction compile(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        NamespaceMap nsContext = null;
        if (this.attributeName instanceof StringLiteral) {
            String[] parts;
            String qName = Whitespace.trim(((StringLiteral)this.attributeName).getStringValue());
            try {
                parts = NameChecker.getQNameParts(qName);
            } catch (QNameException e) {
                return null;
            }
            if (this.namespace == null) {
                String nsuri = "";
                if (!parts[0].equals("") && (nsuri = this.getURIForPrefix(parts[0], false)) == null) {
                    this.undeclaredNamespaceError(parts[0], "XTDE0860", "name");
                    return null;
                }
                FingerprintedQName attributeName = new FingerprintedQName(parts[0], nsuri, parts[1]);
                attributeName.obtainFingerprint(this.getNamePool());
                FixedAttribute inst = new FixedAttribute(attributeName, this.validationAction, this.schemaType);
                inst.setInstruction(true);
                inst.setLocation(this.allocateLocation());
                this.compileContent(compilation, decl, inst, this.separator);
                return inst;
            }
            if (this.namespace instanceof StringLiteral) {
                String nsuri = ((StringLiteral)this.namespace).getStringValue();
                if (nsuri.equals("")) {
                    parts[0] = "";
                } else if (parts[0].equals("")) {
                    String p;
                    NodeInfo ns;
                    AxisIterator iter = this.iterateAxis(8);
                    while ((ns = iter.next()) != null) {
                        if (!ns.getStringValue().equals(nsuri)) continue;
                        parts[0] = ns.getLocalPart();
                        break;
                    }
                    if (parts[0].equals("") && (p = this.getNamePool().suggestPrefixForURI(((StringLiteral)this.namespace).getStringValue())) != null) {
                        parts[0] = p;
                    }
                    if (parts[0].equals("")) {
                        parts[0] = "ns0";
                    }
                }
                FingerprintedQName nodeName = new FingerprintedQName(parts[0], nsuri, parts[1]);
                nodeName.obtainFingerprint(this.getNamePool());
                FixedAttribute inst = new FixedAttribute(nodeName, this.validationAction, this.schemaType);
                inst.setInstruction(true);
                this.compileContent(compilation, decl, inst, this.separator);
                return inst;
            }
        } else if (this.namespace == null) {
            nsContext = this.getAllNamespaces();
        }
        ComputedAttribute inst = new ComputedAttribute(this.attributeName, this.namespace, nsContext, this.validationAction, this.schemaType, false);
        inst.setInstruction(true);
        inst.setLocation(this.allocateLocation());
        this.compileContent(compilation, decl, inst, this.separator);
        return inst;
    }
}

