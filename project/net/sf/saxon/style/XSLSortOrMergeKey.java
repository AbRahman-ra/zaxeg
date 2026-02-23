/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.net.URI;
import java.net.URISyntaxException;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.expr.sort.SortKeyDefinition;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public abstract class XSLSortOrMergeKey
extends StyleElement {
    protected SortKeyDefinition sortKeyDefinition;
    protected Expression select;
    protected Expression order;
    protected Expression dataType = null;
    protected Expression caseOrder;
    protected Expression lang;
    protected Expression collationName;
    protected Expression stable;
    protected boolean useDefaultCollation = true;

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    protected String getErrorCode() {
        return "XTSE1015";
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        if (this.select != null && this.hasChildNodes()) {
            this.compileError("An " + this.getDisplayName() + " element with a select attribute must be empty", this.getErrorCode());
        }
        if (this.select == null && !this.hasChildNodes()) {
            this.select = new ContextItemExpression();
            this.select.setRetainedStaticContext(this.getStaticContext().makeRetainedStaticContext());
        }
        if (this.useDefaultCollation) {
            this.collationName = new StringLiteral(this.getDefaultCollationName());
        }
        StringCollator stringCollator = null;
        if (this.collationName instanceof StringLiteral) {
            String collationString = ((StringLiteral)this.collationName).getStringValue();
            try {
                URI collationURI = new URI(collationString);
                if (!collationURI.isAbsolute()) {
                    URI base = new URI(this.getBaseURI());
                    collationURI = base.resolve(collationURI);
                    collationString = collationURI.toString();
                }
            } catch (URISyntaxException err) {
                this.compileError("Collation name '" + collationString + "' is not a valid URI");
                collationString = "http://www.w3.org/2005/xpath-functions/collation/codepoint";
            }
            stringCollator = this.findCollation(collationString, this.getBaseURI());
            if (stringCollator == null) {
                this.compileError("Collation " + collationString + " has not been defined", "XTDE1035");
                stringCollator = CodepointCollator.getInstance();
            }
        }
        this.select = this.typeCheck("select", this.select);
        this.order = this.typeCheck("order", this.order);
        this.caseOrder = this.typeCheck("case-order", this.caseOrder);
        this.lang = this.typeCheck("lang", this.lang);
        this.dataType = this.typeCheck("data-type", this.dataType);
        this.collationName = this.typeCheck("collation", this.collationName);
        if (this.select != null) {
            try {
                RoleDiagnostic role = new RoleDiagnostic(4, this.getDisplayName() + "//select", 0);
                this.select = this.getConfiguration().getTypeChecker(false).staticTypeCheck(this.select, SequenceType.ATOMIC_SEQUENCE, role, this.makeExpressionVisitor());
            } catch (XPathException err) {
                this.compileError(err);
            }
        }
        this.sortKeyDefinition = new SortKeyDefinition();
        this.sortKeyDefinition.setOrder(this.order);
        this.sortKeyDefinition.setCaseOrder(this.caseOrder);
        this.sortKeyDefinition.setLanguage(this.lang);
        this.sortKeyDefinition.setSortKey(this.select, true);
        this.sortKeyDefinition.setDataTypeExpression(this.dataType);
        this.sortKeyDefinition.setCollationNameExpression(this.collationName);
        this.sortKeyDefinition.setCollation(stringCollator);
        this.sortKeyDefinition.setBaseURI(this.getBaseURI());
        this.sortKeyDefinition.setStable(this.stable);
        this.sortKeyDefinition.setBackwardsCompatible(this.xPath10ModeIsEnabled());
    }

    protected Expression getStable() {
        return this.stable;
    }

    @Override
    protected void prepareAttributes() {
        String selectAtt = null;
        String orderAtt = null;
        String dataTypeAtt = null;
        String caseOrderAtt = null;
        String langAtt = null;
        String collationAtt = null;
        String stableAtt = null;
        block18: for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            switch (f) {
                case "select": {
                    selectAtt = value;
                    this.select = this.makeExpression(selectAtt, att);
                    continue block18;
                }
                case "order": {
                    orderAtt = Whitespace.trim(value);
                    this.order = this.makeAttributeValueTemplate(orderAtt, att);
                    continue block18;
                }
                case "data-type": {
                    dataTypeAtt = Whitespace.trim(value);
                    this.dataType = this.makeAttributeValueTemplate(dataTypeAtt, att);
                    continue block18;
                }
                case "case-order": {
                    caseOrderAtt = Whitespace.trim(value);
                    this.caseOrder = this.makeAttributeValueTemplate(caseOrderAtt, att);
                    continue block18;
                }
                case "lang": {
                    langAtt = Whitespace.trim(value);
                    this.lang = this.makeAttributeValueTemplate(langAtt, att);
                    continue block18;
                }
                case "collation": {
                    collationAtt = Whitespace.trim(value);
                    this.collationName = this.makeAttributeValueTemplate(collationAtt, att);
                    continue block18;
                }
                case "stable": {
                    stableAtt = Whitespace.trim(value);
                    this.stable = this.makeAttributeValueTemplate(stableAtt, att);
                    continue block18;
                }
            }
            this.checkUnknownAttribute(attName);
        }
        if (orderAtt == null) {
            this.order = new StringLiteral("ascending");
        } else {
            this.checkAttributeValue("order", orderAtt, true, new String[]{"ascending", "descending"});
        }
        if (dataTypeAtt == null) {
            this.dataType = null;
        }
        if (caseOrderAtt == null) {
            this.caseOrder = new StringLiteral("#default");
        } else {
            this.checkAttributeValue("case-order", caseOrderAtt, true, new String[]{"lower-first", "upper-first"});
            this.useDefaultCollation = false;
        }
        if (langAtt == null || langAtt.equals("")) {
            this.lang = new StringLiteral(StringValue.EMPTY_STRING);
        } else {
            ValidationFailure vf;
            String s;
            this.useDefaultCollation = false;
            if (this.lang instanceof StringLiteral && (s = ((StringLiteral)this.lang).getStringValue()).length() != 0 && (vf = StringConverter.StringToLanguage.INSTANCE.validate(s)) != null) {
                this.compileError("The lang attribute must be a valid language code", "XTDE0030");
                this.lang = new StringLiteral(StringValue.EMPTY_STRING);
            }
        }
        if (stableAtt == null) {
            this.stable = null;
        } else {
            this.checkAttributeValue("stable", stableAtt, true, StyleElement.YES_NO);
        }
        if (collationAtt != null) {
            this.useDefaultCollation = false;
        }
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        if (this.select == null) {
            Expression b = this.compileSequenceConstructor(exec, decl, true);
            if (b == null) {
                b = Literal.makeEmptySequence();
                b.setRetainedStaticContext(this.makeRetainedStaticContext());
            }
            try {
                Expression atomizedSortKey = Atomizer.makeAtomizer(b, null);
                atomizedSortKey = atomizedSortKey.simplify();
                ExpressionTool.copyLocationInfo(b, atomizedSortKey);
                this.sortKeyDefinition.setSortKey(atomizedSortKey, true);
                this.select = atomizedSortKey;
            } catch (XPathException e) {
                this.compileError(e);
            }
        }
        this.sortKeyDefinition = (SortKeyDefinition)this.sortKeyDefinition.simplify();
        return null;
    }

    public SortKeyDefinition getSortKeyDefinition() {
        return this.sortKeyDefinition;
    }
}

